 /**
  * Copyright (c) 2004, Regents of the University of California
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.stack;
 
 import avrora.core.Instr;
 import avrora.core.Program;
 import avrora.sim.IORegisterConstants;
 import avrora.util.StringUtil;
 import avrora.util.Terminal;
 import avrora.Avrora;
 import avrora.Main;
 
 import java.util.*;
 
 /**
  * The <code>Analyzer</code> class implements the analysis phase that determines
  * the transition relation between the states in the abstract state space. It is
  * modelled on the simulator, but only does abstract interpretation rather than
  * executing the entire program.
  *
  * @author Ben L. Titzer
  */
 public class Analyzer {
 
     protected final Program program;
     protected final StateSpace space;
     ContextSensitivePolicy policy;
     AbstractInterpreter interpreter;
     protected PropagationList proplist;
     protected int proplength;
     protected int propprogress;
 
     protected HashMap aggregationPoints;
 
     long buildTime;
     long traverseTime;
     boolean unbounded;
     Path maximalPath;
 
     public static final int NORMAL_EDGE = 0;
     public static final int PUSH_EDGE = 1;
     public static final int POP_EDGE = 2;
     public static final int CALL_EDGE = 3;
     public static final int INT_EDGE = 4;
     public static final int RET_EDGE = 5;
     public static final int RETI_EDGE = 6;
     public static final int SPECIAL_EDGE = 7;
 
     public static final int NORMAL_STATE = 0;
     public static final int RET_STATE = 1;
     public static final int RETI_STATE = 2;
 
     public static final String[] EDGE_NAMES = {"", "PUSH", "POP", "CALL", "INT", "RET", "RETI", "SPECIAL"};
     public static final int[]    EDGE_DELTA = {0, 1, -1, 2, 2, 0, 0, 0};
 
     public static boolean TRACE;
     public static boolean running;
 
     private class PropagationList {
         final StateSpace.State start;
         final StateSpace.State caller;
         final PropagationList next;
 
         PropagationList(StateSpace.State st, StateSpace.State c, PropagationList p) {
             start = st;
             caller = c;
             next = p;
         }
     }
 
     public Analyzer(Program p) {
         program = p;
         space = new StateSpace(p);
         policy = new ContextSensitivePolicy();
         interpreter = new AbstractInterpreter(program, policy);
         aggregationPoints = new HashMap();
     }
 
     /**
      * The <code>run()</code> method begins the analysis. The entrypoint of the program
      * with an initial default state serves as the first state to start exploring.
      */
     public void run() {
         running = true;
         if ( Main.MONITOR_STATES.get() )
             new MonitorThread().start();
 
         long start = System.currentTimeMillis();
         buildReachableStateSpace();
         long check = System.currentTimeMillis();
         buildTime = check - start;
         findMaximalPath();
         traverseTime = System.currentTimeMillis() - check;
         running = false;
     }
 
     private class MonitorThread extends Thread {
         public void run() {
             try {
                 while ( running ) {
                     sleep(5000);
                     Terminal.println("Count: "+space.getTotalStateCount()+" states, "+
                             space.getTotalEdgeCount()+" edges, "+
                             space.getFrontierCount()+" frontier, "+
                             proplength+" propagations, "+
                             propprogress+", in progress");
                 }
             } catch ( InterruptedException e ) {
                 e.printStackTrace();
             }
         }
     }
 
     private void buildReachableStateSpace() {
         StateSpace.State s = space.getEdenState();
 
         while ( s != null || proplist != null) {
             if ( s != null )
                 processFrontierState(s);
             else processPropagationList();
             s = space.getFrontierState();
         }
     }
 
     private void processPropagationList() {
         PropagationList list = proplist;
         proplist = null;
         propprogress = proplength;
         proplength = 0;
         // process the list of propagations to be done;
         // propagate calling states to reachable return states
         for ( ; list != null; list = list.next ) {
             HashSet callers = new HashSet();
             callers.add(list.caller);
 
             if ( TRACE ) {
                 Terminal.print("Propagating ");
                 StatePrinter.printStateName(list.caller);
                 Terminal.print(" to ");
                 StatePrinter.printStateName(list.start);
                 Terminal.nextln();
             }
             propagate(list.start, callers);
             propprogress--;
         }
     }
 
     private void propagate(StateSpace.State t, HashSet newCalls) {
         Object mark = new Object();
 
         propagate(t, newCalls, mark);
     }
 
     private void newAggregationPoint(StateSpace.State t) {
         if ( aggregationPoints.containsKey(t) ) return;
         aggregationPoints.put(t, new HashSet());
     }
 
     // propagate call sites to all reachable return states
     private void propagate(StateSpace.State t, HashSet newCalls, Object mark) {
         // visited this node already?
         if (t.mark == mark) return;
         t.mark = mark;
 
         // return state? if so, set up call edges
         if ( t.getType() != NORMAL_STATE ) {
             processCalls(newCalls, t);
             return;
         }
 
         // if the target is an aggregation point, add all of them
         HashSet agg = getCallers(t);
        if ( agg != null ) agg.addAll(newCalls);
 
         for (StateSpace.Link link = t.outgoing; link != null; link = link.next) {
 
             // do not follow call edges
             if (link.type == CALL_EDGE) continue;
 
             // propagate these calls to all children
             propagate(link.target, newCalls, mark);
         }
     }
 
     private HashSet getCallers(StateSpace.State t) {
         return (HashSet)aggregationPoints.get(t);
     }
 
     private void processCalls(HashSet newCalls, StateSpace.State retstate) {
         HashSet prevcallers = getCallers(retstate);
         if ( prevcallers == null ) {
             throw Avrora.failure("State should be an aggregation point: "+retstate.getUniqueName());
         }
 
         MutableState rstate = retstate.copy();
         boolean interrupt = retstate.getType() == RETI_STATE;
 
         Iterator i = newCalls.iterator();
         while ( i.hasNext() ) {
             StateSpace.State caller = (StateSpace.State)i.next();
             if ( !prevcallers.contains(caller) ) {
                 addReturnEdge(caller, rstate, interrupt);
             }
         }
     }
 
     private void addReturnEdge(StateSpace.State caller, MutableState rstate, boolean interrupt) {
         int cpc = caller.getPC();
         int npc;
 
         if ( interrupt ) {
             npc = cpc;
             rstate.setFlag_I(AbstractArithmetic.TRUE);
         } else {
             npc = cpc + program.readInstr(cpc).getSize();
         }
 
         rstate.setPC(npc);
         StateSpace.State t = space.getStateFor(rstate);
 
         if ( TRACE ) {
             printFullState("Adding return edge", t);
             traceProducedState(t);
         }
 
         if (!space.isExplored(t)) {
             space.addFrontier(t);
         }
         // set up the return edge
         space.addEdge(caller, t);
         // post caller's callers to the returned state
         post(t, getCallers(caller));
     }
 
     private void post(StateSpace.State state, HashSet callers) {
         Iterator i = callers.iterator();
         while ( i.hasNext() ) {
             StateSpace.State caller = (StateSpace.State)i.next();
             post(state, caller);
         }
     }
 
     private void post(StateSpace.State state, StateSpace.State caller) {
         proplist = new PropagationList(state, caller, proplist);
         proplength++;
     }
 
 
     private void processFrontierState(StateSpace.State s) {
         traceState(s);
 
         // get the frontier information (call sites)
         policy.frontierState = s;
         policy.edgeType = NORMAL_EDGE;
 
         // add this to explored states
         space.addState(s);
 
         // compute the possible next states
         interpreter.computeNextStates(s);
     }
 
     private void findMaximalPath() {
         // the stack hashmap contains a mapping between states on the stack
         // and the current stack depth at which they are being explored
         HashMap stack = new HashMap();
         StateSpace.State state = space.getEdenState();
 
         try {
             maximalPath = findMaximalPath(state, stack, 0);
         } catch (UnboundedStackException e) {
             unbounded = true;
             maximalPath = e.path;
         }
     }
 
     private class Path {
         final int depth;
         final StateSpace.State state;
         final StateSpace.Link link;
         final Path tail;
 
         Path(int d, StateSpace.State s, StateSpace.Link l, Path p) {
             state = s;
             depth = d;
             link = l;
             tail = p;
         }
     }
 
     private Path findMaximalPath(StateSpace.State s, HashMap stack, int depth) {
         // record this node and the stack depth at which we first encounter it
         stack.put(s, new Integer(depth));
 
         int maxdepth = 0;
         Path maxtail = null;
         StateSpace.Link maxlink = null;
         for (StateSpace.Link link = s.outgoing; link != null; link = link.next) {
 
             StateSpace.State t = link.target;
             if (stack.containsKey(t)) {
                 // cycle detected. check that the depth when reentering is the same
                 int prevdepth = ((Integer) stack.get(t)).intValue();
                 if (depth + link.weight != prevdepth) {
                     stack.remove(s);
                     throw new UnboundedStackException(new Path(depth + link.weight, s, link, null));
                 }
             } else {
                 Path tail;
 
                 if (link.target.mark instanceof Path) {
                     // node has already been visited and marked with the
                     // maximum amount of stack depth that it can add.
                     tail = ((Path) link.target.mark);
                 } else {
                     // node has not been seen before, traverse it
                     try {
                         tail = findMaximalPath(link.target, stack, depth + link.weight);
                     } catch ( UnboundedStackException e) {
                         // this node is part of an unbounded cycle, add it to the path
                         // and rethrow the exception
                         e.path = new Path(depth + link.weight, s, link, e.path);
                         stack.remove(s);
                         throw e;
                     }
                 }
 
                 // compute maximum added stack depth by following this link
                 int extra = link.weight + tail.depth;
 
                 // remember the most added stack depth from following any of the links
                 if (extra > maxdepth) {
                     maxdepth = extra;
                     maxtail = tail;
                     maxlink = link;
                 }
             }
         }
         // we are finished with this node, remember how much deeper it can take us
         stack.remove(s);
         Path maxpath = new Path(maxdepth, s, maxlink, maxtail);
         s.mark = maxpath;
         return maxpath;
     }
 
 
     private class UnboundedStackException extends RuntimeException {
         Path path;
 
         UnboundedStackException(Path p) {
             path = p;
         }
     }
 
     /**
      * The <code>report()</code> method generates a textual report after the
      * analysis has been completed. It reports information about states, reachable
      * states, time for analysis, and of course, the maximum stack size for the
      * program.
      */
     public void report() {
         printQuantity("Total cached states   ", "" + space.getTotalStateCount());
         printQuantity("Total reachable states", "" + space.getStatesInSpaceCount());
         printQuantity("Time to build graph   ", StringUtil.milliAsString(buildTime));
         printQuantity("Time to traverse graph", StringUtil.milliAsString(traverseTime));
         if ( unbounded )
             printQuantity("Maximum stack depth   ", "unbounded");
         else
             printQuantity("Maximum stack depth   ", "" + maximalPath.depth+" bytes" );
         printPath(maximalPath);
     }
 
     private void printPath(Path p) {
         int depth = 0;
         int cntr = 1;
         boolean summary = Main.TRACE_SUMMARY.get();
         for ( Path path = p; path != null && path.link != null; path = path.tail ) {
 
             if ( summary && path.link.weight == 0 ) {
                 int pc = path.state.getPC();
                 int npc = pc + program.readInstr(pc).getSize();
                 if ( path.link.target.getPC() == npc ) {
                     cntr++;
                     continue;
                 }
             }
 
             printFullState("["+cntr+"] Depth: "+depth, path.state);
             Terminal.print("    ");
             StatePrinter.printEdge(path.link.type, path.link.weight, path.link.target);
             depth += path.link.weight;
             cntr++;
         }
     }
 
     private void printQuantity(String q, String v) {
         Terminal.printBrightGreen(q);
         Terminal.println(": " + v);
     }
 
     /**
      * The <code>ContextSensitive</code> class implements the context-sensitive
      * analysis similar to 1-CFA. It is an implementation of the <code>Analyzer.Policy</code>
      * interface that determines what should be done in the case of a call, return, push,
      * pop, indirect call, etc. The context-sensitive analysis does not model the
      * contents of the stack, so pushes and pops essentially only modify the height of
      * the stack.
      *
      * @author Ben L. Titzer
      */
     public class ContextSensitivePolicy implements AnalyzerPolicy {
 
         public StateSpace.State frontierState;
         protected int edgeType;
 
         /**
          * The <code>call()</code> method is called by the abstract interpreter when it
          * encounters a call instruction within the program. Different policies may
          * handle calls differently. This context-sensitive analysis keeps track of
          * the call site every time a new method is entered. Thus the states coming
          * into a method call are merged according to the call site, instead of all
          * merged together.
          *
          * @param s              the current abstract state
          * @param target_address the concrete target address of the call
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState call(MutableState s, int target_address) {
             s.setPC(target_address);
             addEdge(frontierState, CALL_EDGE, s);
             newAggregationPoint(frontierState);
             post(space.getStateFor(s), frontierState);
 
             // do not continue abstract interpretation after this state; edges will
             // be inserted later that represent possible return states
             return null;
         }
 
         /**
          * The <code>interrupt()</code> is called by the abstract interrupt when it
          * encounters a place in the program when an interrupt might occur.
          *
          * @param s   the abstract state just before interrupt
          * @param num the interrupt number that might occur
          * @return the state of the program after the interrupt, null if there is
          *         no next state
          */
         public MutableState interrupt(MutableState s, int num) {
             s.setFlag_I(AbstractArithmetic.FALSE);
             s.setPC((num - 1) * 4);
             addEdge(frontierState, INT_EDGE, s);
             newAggregationPoint(frontierState);
             post(space.getStateFor(s), frontierState);
 
             // do not continue abstract interpretation after this state; edges will
             // be inserted later that represent possible return states
             return null;
         }
 
         /**
          * The <code>ret()</code> method is called by the abstract interpreter when it
          * encounters a return within the program. In the context-sensitive analysis,
          * the return state must be connected with the call site. This is done by
          * accessing the call site list which is stored in this frontier state and
          * inserting edges for each call site.
          *
          * @param s the current abstract state
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState ret(MutableState s) {
             frontierState.setType(RET_STATE);
             newAggregationPoint(frontierState);
 
             // do not continue abstract interpretation after this state; this state
             // is a return state and therefore is a dead end
             return null;
         }
 
         /**
          * The <code>reti()</code> method is called by the abstract interpreter when it
          * encounters a return from an interrupt within the program.
          *
          * @param s the current abstract state
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState reti(MutableState s) {
             frontierState.setType(RETI_STATE);
             newAggregationPoint(frontierState);
 
             // do not continue abstract interpretation after this state; this state
             // is a return state and therefore is a dead end
             return null;
         }
 
         /**
          * The <code>indirectCall()</code> method is called by the abstract interpreter
          * when it encounters an indirect call within the program. The abstract values
          * of the address are given as parameters, so that a policy can choose to compute
          * possible targets or be conservative or whatever it so chooses.
          *
          * @param s        the current abstract state
          * @param addr_low the (abstract) low byte of the address
          * @param addr_hi  the (abstract) high byte of the address
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState indirectCall(MutableState s, char addr_low, char addr_hi) {
             int callsite = s.pc;
             List iedges = program.getIndirectEdges(callsite);
             if (iedges == null)
                 throw Avrora.failure("No control flow information for indirect call at: " +
                         StringUtil.addrToString(callsite));
             Iterator i = iedges.iterator();
             while (i.hasNext()) {
                 int target_address = ((Integer) i.next()).intValue();
                 call(s, target_address);
             }
 
             return null;
         }
 
         /**
          * The <code>indirectJump()</code> method is called by the abstract interpreter
          * when it encounters an indirect jump within the program. The abstract values
          * of the address are given as parameters, so that a policy can choose to compute
          * possible targets or be conservative or whatever it so chooses.
          *
          * @param s        the current abstract state
          * @param addr_low the (abstract) low byte of the address
          * @param addr_hi  the (abstract) high byte of the address
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState indirectJump(MutableState s, char addr_low, char addr_hi) {
             int callsite = s.pc;
             List iedges = program.getIndirectEdges(callsite);
             if (iedges == null)
                 throw Avrora.failure("No control flow information for indirect jump at: " +
                         StringUtil.addrToString(callsite));
             Iterator i = iedges.iterator();
             while (i.hasNext()) {
                 int target_address = ((Integer) i.next()).intValue();
                 s.setPC(target_address);
                 pushState(s);
             }
 
             return null;
         }
 
         /**
          * The <code>indirectCall()</code> method is called by the abstract interpreter
          * when it encounters an indirect call within the program. The abstract values
          * of the address are given as parameters, so that a policy can choose to compute
          * possible targets or be conservative or whatever it so chooses.
          *
          * @param s        the current abstract state
          * @param addr_low the (abstract) low byte of the address
          * @param addr_hi  the (abstract) high byte of the address
          * @param ext      the (abstract) extended part of the address
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState indirectCall(MutableState s, char addr_low, char addr_hi, char ext) {
             throw new Error("extended indirect calls not supported");
         }
 
         /**
          * The <code>indirectJump()</code> method is called by the abstract interpreter
          * when it encounters an indirect jump within the program. The abstract values
          * of the address are given as parameters, so that a policy can choose to compute
          * possible targets or be conservative or whatever it so chooses.
          *
          * @param s        the current abstract state
          * @param addr_low the (abstract) low byte of the address
          * @param addr_hi  the (abstract) high byte of the address
          * @param ext      the (abstract) extended part of the address
          * @return null because the correct state transitions are inserted by
          *         the policy, and the abstract interpreter should not be concerned.
          */
         public MutableState indirectJump(MutableState s, char addr_low, char addr_hi, char ext) {
             throw new Error("extended indirect jumps not supported");
         }
 
         /**
          * The <code>push()</code> method is called by the abstract interpreter when
          * a push to the stack is encountered in the program. The policy can then choose
          * what outgoing and/or modelling of the stack needs to be done.
          *
          * @param s   the current abstract state
          * @param val the abstract value to push onto the stack
          */
         public void push(MutableState s, char val) {
             edgeType = PUSH_EDGE;
         }
 
         /**
          * The <code>pop()</code> method is called by the abstract interpreter when
          * a pop from the stack is ecountered in the program. The policy can then
          * choose to either return whatever information it has about the stack
          * contents, or return an UNKNOWN value.
          *
          * @param s the current abstract state
          * @return the abstract value popped from the stack
          */
         public char pop(MutableState s) {
             edgeType = POP_EDGE;
             // we do not model the stack, so popping values returns unknown
             return AbstractArithmetic.UNKNOWN;
         }
 
         /**
          * The <code>pushState</code> method is called by the abstract interpreter when
          * a state is forked by the abstract interpreter (for example when a branch
          * condition is not known and both branches must be taken.
          *
          * @param newState the new state created
          */
         public void pushState(MutableState newState) {
             addEdge(frontierState, edgeType, newState);
         }
 
         private void addEdge(StateSpace.State from, int type, MutableState to) {
             StateSpace.State t = space.getStateFor(to);
             traceProducedState(t);
             addEdge(type, from, t, EDGE_DELTA[type]);
             pushFrontier(t);
         }
 
         private void pushFrontier(StateSpace.State t) {
             // CASE 4: self loop
             if (t == frontierState) return;
 
             if (space.isExplored(t)) {
                 // CASE 3: state is already explored
                 // do nothing; propagation phase will push callers to reachable returns
             } else if (space.isFrontier(t)) {
                 // CASE 2: state is already on frontier
                 // do nothing; propagation phase will push callers to reachable returns
             } else {
                 // CASE 1: new state, add to frontier
                 space.addFrontier(t);
             }
         }
 
         private void addEdge(int type, StateSpace.State s, StateSpace.State t, int weight) {
             traceEdge(type, s, t, weight);
             space.addEdge(s, t, type, weight);
         }
 
     }
 
 
     //-----------------------------------------------------------------------
     //             D E B U G G I N G   A N D   T R A C I N G
     //-----------------------------------------------------------------------
     //-----------------------------------------------------------------------
 
 
     private void traceState(StateSpace.State s) {
         if (TRACE) {
             printFullState("Exploring", s);
         }
     }
 
     private void printFullState(String head, StateSpace.State s) {
         Terminal.print(head+" ");
         StatePrinter.printStateName(s);
         Terminal.nextln();
         Instr instr = program.readInstr(s.getPC());
         String str = StringUtil.leftJustify(instr.toString(), 14);
         StatePrinter.printState(str, s);
     }
 
     private void traceProducedState(StateSpace.State s) {
         if (TRACE) {
             String str;
             if (space.isExplored(s)) {
                 str = "        E ==> ";
             } else if (space.isFrontier(s)) {
                 str = "        F ==> ";
 
             } else {
                 str = "        N ==> ";
             }
             StatePrinter.printState(str, s);
         }
     }
 
     public static void traceEdge(int type, StateSpace.State s, StateSpace.State t, int weight) {
         if (!TRACE) return;
         Terminal.print("adding edge ");
         StatePrinter.printEdge(s, type, weight, t);
     }
 
 }
