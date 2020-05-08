 package mi.parser;
 
 import mi.stream.ICharStream;
import mi.stream.StringStream;
 
 import java.util.ArrayList;
 
 /**
  * @author goldolphin
  *         2014-02-13 20:30
  */
 public class Parser {
     private final State startState;
     private final Nonterm[] nontermTable;
     private final boolean useHeadSet;
 
     Parser(State startState, Nonterm[] nontermTable, boolean useHeadSet) {
         this.startState = startState;
         this.nontermTable = nontermTable;
         this.useHeadSet = useHeadSet;
     }
 
     public boolean parse(ICharStream stream) {
         SubParsers subParsers = new SubParsers();
         SubParsers backup = new SubParsers();
         SubParsers toConsume = new SubParsers();
         SubParsers success = new SubParsers();
 
         subParsers.add(startState, GraphStack.<Transition.NontermTransition>newBottom(
                 new Transition.NontermTransition(Nonterm.ANY, null)));
 
         while (true) {
            System.out.format("subParsers = %d, toConsume = %d, success = %d\n", subParsers.size(), toConsume.size(), success.size());
             if (subParsers.size() == 0) {
                 // Transitions need consuming input
                 int num = toConsume.size();
                 if (num == 0) {
                     break;
                 }
 
                 char c = stream.poll();
                 if (c == stream.EOF) {
                     break;
                 }
                 for (int i = 0; i < num; i++) {
                     State state = toConsume.getState(i);
                     GraphStack<Transition.NontermTransition> stack = toConsume.getStack(i);
                     Transition.NontermTransition stackTop = stack.getData();
                     int head = useHeadSet ? stackTop.nontermId : Nonterm.ANY;
                     State next = state.getTermTransition(c, head);
                     if (next != null) {
                         subParsers.add(next, stack);
                     }
                 }
                toConsume.clear();
             }
 
             // Transitions without consuming input
             backup.clear();
 
             int num = subParsers.size();
             for (int i = 0; i < num; i++) {
                 State state = subParsers.getState(i);
                 GraphStack<Transition.NontermTransition> stack = subParsers.getStack(i);
                 Transition.NontermTransition stackTop = stack.getData();
                 int head = useHeadSet ? stackTop.nontermId : Nonterm.ANY;
 
                 // Nonterm transitions
                 for (Transition.NontermTransition transition: state.getNontermTransitions()) {
                     if (transition.containsHeadNonterm(head)) {
                         backup.add(startState, stack.push(transition));
                     }
                 }
 
                 // Accepting state
                 if (state.hasAcceptedNonterm()) {
                     Nonterm accepted = nontermTable[state.getAcceptedNontermId()];
                     // Accepting transition
                     if (head == Nonterm.ANY || head == accepted.id) {
                         if (stackTop.target == null) {
                             if (stream.peek() == stream.EOF) {
                                 success.add(stackTop.target, stack.pop());
                             }
                         } else {
                             backup.add(stackTop.target, stack.pop());
                         }
                     }
 
                     // Leftmost transitions
                     Transition.NontermTransition leftmostTransition = accepted.getLeftMostTransition();
                     if (leftmostTransition != null && leftmostTransition.containsHeadNonterm(head)) {
                         backup.add(leftmostTransition.target, stack);
                     }
                 }
 
                 // Add to toConsume
                if (state.canConsume()) {
                    toConsume.add(state, stack);
                }
             }
 
             // Swap subParsers & backup
             SubParsers temp = subParsers;
             subParsers = backup;
             backup = temp;
         }
 
         if (stream.peek() == stream.EOF && success.size() > 0) {
             return true;
         }
         return false;
     }
 
     private static class SubParsers {
         private ArrayList<State> states = new ArrayList<>();
         private ArrayList<GraphStack<Transition.NontermTransition>> stacks = new ArrayList<>();
 
         public void add(State state, GraphStack<Transition.NontermTransition> stack) {
             states.add(state);
             stacks.add(stack);
         }
 
         public State getState(int i) {
             return states.get(i);
         }
 
         public GraphStack<Transition.NontermTransition> getStack(int i) {
             return stacks.get(i);
         }
 
         public int size() {
             return states.size();
         }
 
         public void clear() {
             states.clear();
             stacks.clear();
         }
     }
 }
