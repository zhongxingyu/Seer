 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.stdlib;
 
 import jason.asSemantics.Circumstance;
 import jason.asSemantics.CircumstanceListener;
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.Event;
 import jason.asSemantics.Intention;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Atom;
 import jason.asSyntax.InternalActionLiteral;
 import jason.asSyntax.NumberTerm;
 import jason.asSyntax.NumberTermImpl;
 import jason.asSyntax.PlanBody;
 import jason.asSyntax.PlanBodyImpl;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.PlanBody.BodyType;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 
 /**
   <p>Internal action: <b><code>.wait(<i>E</i>,<i>T</i>)</code></b>.
   
   <p>Description: suspend the intention for the time specified by <i>T</i> (in
   milliseconds) or until some event <i>E</i> happens. The events are
   strings in AgentSpeak syntax, e.g. <code>"+bel(33)"</code>,
   <code>"+!go(X,Y)"</code>. 
   
   Although the argument is a string, the variables
   in the string will be unified with the event, i.e., the unifier might have
   values for X and Y after the execution of <code>.wait("+!go(X,Y)")</code>.
 
   <p>Parameters:<ul>
   <li><i>+ event</i> (string): the event to wait for.<br/>
   <li>+ timeout (number).<br/>
   </ul>
   
   
   <p>Examples:<ul>
   <li> <code>.wait(1000)</code>: suspend the intention for 1 second.
 
   <li> <code>.wait("+b(1)")</code>: suspend the intention until the belief
   <code>b(1)</code> is added in the belief base.
 
   <li> <code>.wait("+!g", 2000)</code>: suspend the intention until the goal
   <code>g</code> is triggered or 2 seconds have passed, whatever happens
   first. In case the event does not happens in two seconds, the internal action
   fails. 
 
   <li> <code>.wait("+!g", 2000, EventTime)</code>: suspend the intention until the goal
   <code>g</code> is triggered or 2 seconds have passed, whatever happens
   first. 
   Hence this use of .wait has three arguments, in case the event does not happen in 
   two seconds, the internal action does not fail (as in the previous example).
   The third argument will be unified to the 
   elapsed time (in miliseconds) from the start of .wait until the event or timeout. </ul>
 
   @see jason.stdlib.at
 
  */
 public class wait extends DefaultInternalAction {
 
     public static final Atom waitAtom = new Atom(".wait"); 
     //static Logger logger = Logger.getLogger(wait.class.getName());
 
     @Override
     public boolean canBeUsedInContext() {
         return false;
     }
 
     @Override
     public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         long timeout = -1;
         Trigger te = null;
         
         Term elapseTime = null;
         try {
             if (args[0].isNumeric()) {
                 // time in milliseconds
                 NumberTerm time = (NumberTerm)args[0];
                 timeout = (long) time.solve();
                 // Thread.sleep((long) time.solve());
             } else if (args[0].isString()) {
                 // wait for event
                 StringTerm st = (StringTerm) args[0];
                 st.apply(un);
                 te = Trigger.parseTrigger(st.getString());
 
                 if (args.length >= 2)
                     timeout = (long) ((NumberTerm) args[1]).solve();
                 if (args.length == 3)
                 	elapseTime = args[2];
             }
         } catch (Exception e) {
             ts.getLogger().log(Level.SEVERE, "Error at .wait.", e);
             return false;
         }
         WaitEvent wet = new WaitEvent(te, un, ts, timeout, elapseTime);
         wet.start();
         return true;
     }    
 
     @Override
     public boolean suspendIntention() {
         return true;
     } 
     
     private List<WaitEvent> threads = Collections.synchronizedList(new ArrayList<WaitEvent>());
     
     public void stopAllWaits() {
         for (WaitEvent t: threads) {
             t.interrupt();
         }
     }
 
 
     class WaitEvent extends Thread implements CircumstanceListener {
         Trigger          te;
         String           sTE; // a string version of TE
         Unifier          un;
         Intention        si;
         TransitionSystem ts;
         Circumstance     c;
         boolean          ok      = false;
         boolean          drop    = false;
         boolean          stopByTimeout = false;
         Term             elapseTimeTerm;
         long             timeout = -1;
         long             elapseTime;
         
         WaitEvent(Trigger te, Unifier un, TransitionSystem ts, long to, Term elapseTimeTerm) {
             super("wait "+te);
             this.te = te;
             this.un = un;
             this.ts = ts;
             c = ts.getC();
             si = c.getSelectedIntention();
             this.timeout = to;
             this.elapseTimeTerm = elapseTimeTerm;
 
             // register listener
             c.addEventListener(this);
             
             if (te != null) {
                 sTE = te.toString();
             } else {
                 sTE = "time"+(timeout);
             }
             sTE = si.getId()+"/"+sTE;
             c.getPendingIntentions().put(sTE, si);
             
             threads.add(this);
         }
 
         public void run() {
             try {
                 waitEvent();
 
                 // unregister (for not to receive intentionAdded again)
                 c.removeEventListener(this);
 
                 // add SI again in C.I if it was not removed and this 
                 // wait was not dropped
                 if (c.getPendingIntentions().remove(sTE) == si && !c.getIntentions().contains(si) && !drop) {
                     if (stopByTimeout && te != null && elapseTimeTerm == null) {
                         // fail the .wait
                     	PlanBody body = si.peek().getPlan().getBody();
                     	body.add(1, new PlanBodyImpl(BodyType.internalAction, new InternalActionLiteral(".fail")));
                     } 
                     si.peek().removeCurrentStep();
                     if (elapseTimeTerm != null)
                     	si.peek().getUnif().unifies(elapseTimeTerm, new NumberTermImpl(elapseTime));
                     if (si.isSuspended()) { // if the intention was suspended by .suspend
                     	String k = suspend.SUSPENDED_INT+si.getId();
                     	c.getPendingIntentions().put(k, si);
                     } else {
                     	c.addIntention(si);
                     }
                 }
 
             } catch (Exception e) {
                 ts.getLogger().log(Level.SEVERE, "Error at .wait thread", e);
             } finally {
                 threads.remove(this);
             }
         }
 
         synchronized public void waitEvent() {
             long init = System.currentTimeMillis();
             elapseTime = 0;
             while (!ok && !drop) {
                 try {
                     if (timeout == -1) {
                         wait();
                     } else {
                         long to = timeout - elapseTime;
                         if (to <= 0)
                             to = 100;
                         wait(to);
                         elapseTime = System.currentTimeMillis() - init;
                         if (elapseTime >= timeout) {
                             stopByTimeout = true;
                             break;
                         }
                     }
                 } catch (InterruptedException e)  {
                     drop = true;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
            //elapseTime = System.currentTimeMillis() - init;
         }
 
         synchronized public void eventAdded(Event e) {
             if (te != null && !drop && un.unifies(te, e.getTrigger())) {
                 ok = true;
                 notifyAll();
             }
         }
 
         synchronized public void intentionDropped(Intention i) {
             if (i.equals(si)) {
                 ok = false;
                 drop = true;
                 notifyAll();
             }
         }
 
         public void intentionAdded(Intention i) { }
         
         public String toString() {
             return sTE;
         }
     }
 }
