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
   <p>Internal action: <b><code>.drop_intention(<i>I</i>)</code></b>.
   
   <p>Description: removes intentions <i>I</i> from the set of
   intentions of the agent (suspended intentions are also considered).
   No event is produced.
 
   <p>Example:<ul> 
 
   <li> <code>.drop_intention(go(1,3))</code>: removes an intention having a plan
    with triggering event
   <code>+!go(1,3)</code> in the agent's current circumstance.
 
   </ul>
 
   @see jason.stdlib.intend
   @see jason.stdlib.desire
   @see jason.stdlib.drop_all_desires
   @see jason.stdlib.drop_all_events
   @see jason.stdlib.drop_all_intentions
   @see jason.stdlib.drop_desire
   @see jason.stdlib.succeed_goal
   @see jason.stdlib.fail_goal
   @see jason.stdlib.current_intention
   @see jason.stdlib.suspend
   @see jason.stdlib.resume
 
  */
 public class drop_intention extends DefaultInternalAction {
     
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         try {
             dropInt(ts.getC(),(Literal)args[0],un);
             return true;
         } catch (ArrayIndexOutOfBoundsException e) {
             throw new JasonException("The internal action 'drop_intention' has not received the required argument.");
         } catch (Exception e) {
             throw new JasonException("Error in internal action 'drop_intention': " + e, e);
         }
     }
     
     public void dropInt(Circumstance C, Literal l, Unifier un) {
         Unifier bak = un.copy();
         
         Trigger g = new Trigger(TEOperator.add, TEType.achieve, l);
         for (Intention i: C.getIntentions()) {
             if (i.hasTrigger(g, un)) {
                 C.removeIntention(i);
                 un = bak.copy();
             }
         }
         
         // intention may be suspended in E
         for (Event e: C.getEvents()) {
             Intention i = e.getIntention();
             if (i != null && i.hasTrigger(g, un)) {
                 C.removeEvent(e);
                 un = bak.copy();
             }
         }
         
         // intention may be suspended in PA! (in the new semantics)
         for (ActionExec a: C.getPendingActions().values()) {
             Intention i = a.getIntention();
             if (i.hasTrigger(g, un)) {
                 C.dropPendingAction(i);
                 un = bak.copy();
             }
         }
 
         // intention may be suspended in PI! (in the new semantics)
         for (Intention i: C.getPendingIntentions().values()) {
            if (i != null && i.hasTrigger(g, un)) {
                 C.dropPendingIntention(i);
                 un = bak.copy();
             }
         }
     }
 }
