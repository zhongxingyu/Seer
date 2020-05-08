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
 //----------------------------------------------------------------------------
 
 
 package jason.stdlib;
 
 import jason.JasonException;
 import jason.asSemantics.ActionExec;
 import jason.asSemantics.Circumstance;
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.Event;
 import jason.asSemantics.Intention;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.Trigger.TEOperator;
 import jason.asSyntax.Trigger.TEType;
 
 /**
   <p>Internal action:
   <b><code>.succeed_goal(<i>G</i>)</code></b>.
   
   <p>Description: remove goals <i>G</i> from the agent circumstance as if a plan
   for such goal had successfully finished. <i>G</i>
   is a goal if there is a triggering event <code>+!G</code> in any plan within any
   intention; also note that intentions can be suspended hence appearing
   in E, PA, or PI as well.
 
   <p>Example:<ul> 
 
   <li> <code>.succeed_goal(go(X,3))</code>: stops any attempt to achieve goals such as
   <code>!go(1,3)</code> as if it had already been achieved.
 
   </ul>
 
   (Note: this internal action was introduced in a DALT 2006 paper, where it was called .dropGoal(G,true).)
 
   @see jason.stdlib.intend
   @see jason.stdlib.desire
   @see jason.stdlib.drop_all_desires
   @see jason.stdlib.drop_all_events
   @see jason.stdlib.drop_all_intentions
   @see jason.stdlib.drop_intention
   @see jason.stdlib.drop_desire
   @see jason.stdlib.fail_goal
   @see jason.stdlib.current_intention
   @see jason.stdlib.suspend
   @see jason.stdlib.resume
 
  */
 public class succeed_goal extends DefaultInternalAction {
 
     @Override public int getMinArgs() { return 1; }
     @Override public int getMaxArgs() { return 1; }
 
     @Override protected void checkArguments(Term[] args) throws JasonException {
         super.checkArguments(args); // check number of arguments
         if (!args[0].isLiteral())
             throw JasonException.createWrongArgument(this,"first argument must be a literal");
     }
     
 
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         checkArguments(args);
         drop(ts, (Literal)args[0], un);
         return true;
     }
     
     public void drop(TransitionSystem ts, Literal l, Unifier un) throws Exception {
         Trigger g = new Trigger(TEOperator.add, TEType.achieve, l);
         Circumstance C = ts.getC();
         
         for (Intention i: C.getIntentions()) {
             if (dropIntention(i, g, ts, un) > 1) {
                 C.removeIntention(i);
             }
         }
         
         // dropping the current intention?
         dropIntention(C.getSelectedIntention(), g, ts, un);
             
         // dropping G in Events
         for (Event e: C.getEvents()) {
             // test in the intention
             Intention i = e.getIntention();
             if (dropIntention(i, g, ts, un) > 1) {
                 C.removeEvent(e);
             } else {
                 // test in the event
                 Trigger t = e.getTrigger();
                 if (i != Intention.EmptyInt && i.size() > 0) {
                     t = (Trigger) t.clone();
                     t.apply(i.peek().getUnif());
                 }
                 if (un.unifies(t, g)) {
                     dropInEvent(ts,e,i);
                 }
             }
         }
         
         // dropping from Pending Actions
         for (ActionExec a: C.getPendingActions().values()) {
             Intention i = a.getIntention();
             int r = dropIntention(i, g, ts, un);
             if (r > 0) { // i was changed
                 C.dropPendingAction(i); // remove i from PA
                 if (r == 1) {           // i must continue running
                     C.addIntention(i);  // and put the intention back in I
                 }                       // if r > 1, the event was generated and i will be back soon
             }
         }
         
         // dropping from Pending Intentions
         for (Intention i: C.getPendingIntentions().values()) {
             int r = dropIntention(i, g, ts, un);
             if (r > 0) { 
                 C.dropPendingIntention(i); 
                 if (r == 1) { 
                     C.addIntention(i); 
                 }
             }
         }
     }
     
     /* returns: >0 the intention was changed
      *           1 = intention must continue running
      *           2 = fail event was generated and added in C.E
      *           3 = simply removed without event
      */
     int dropIntention(Intention i, Trigger g, TransitionSystem ts, Unifier un) throws JasonException {
        if (i != null && i.dropGoal(g, un) && !i.isFinished()) {  // could be finished after i.dropGoal() !!
         	// continue the intention
         	i.peek().removeCurrentStep();
         	ts.applyClrInt(i);
         	return 1;
         }
         return 0;        
     }
     
     void dropInEvent(TransitionSystem ts, Event e, Intention i) throws Exception {
         Circumstance C = ts.getC();
         C.removeEvent(e);
         if (i != null) {
             i.peek().removeCurrentStep();
             ts.applyClrInt(i);
             C.addIntention(i);
         }
     }
 }
