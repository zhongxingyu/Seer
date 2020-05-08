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
 import jason.asSemantics.Circumstance;
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
   <p>Internal action: <b><code>.desire(<i>D</i>)</code></b>.
   
   <p>Description: checks whether <i>D</i> is a desire: <i>D</i> is a desire
   either if there is an event with <code>+!D</code> as triggering
   event or it is a goal in one of the agent's intentions.
   
   <p>Example:<ul> 
 
   <li> <code>.desire(go(1,3))</code>: true if <code>go(1,3)</code>
   is a desire of the agent.
 
   </ul>
 
   @see jason.stdlib.intend
   @see jason.stdlib.drop_all_desires
   @see jason.stdlib.drop_all_events
   @see jason.stdlib.drop_all_intentions
   @see jason.stdlib.drop_intention
   @see jason.stdlib.drop_desire
   @see jason.stdlib.succeed_goal
   @see jason.stdlib.fail_goal
   @see jason.stdlib.current_intention
   @see jason.stdlib.suspend
   @see jason.stdlib.resume
 
 */
 public class desire extends intend {
 
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         try {
             return desires(ts.getC(),(Literal)args[0],un);
         } catch (ArrayIndexOutOfBoundsException e) {
             throw new JasonException("The internal action 'desire' has not received the required argument.");
         } catch (Exception e) {
             throw new JasonException("Error in internal action 'desire': " + e, e);
         }
     }
     
     public boolean desires(Circumstance C, Literal l, Unifier un) {
         Trigger teFromL = new Trigger(TEOperator.add, TEType.achieve, l);
 
        // we need to check the slected event in this cycle (already removed from E)
         if (C.getSelectedEvent() != null) {
             Trigger t = C.getSelectedEvent().getTrigger();
             Intention i = C.getSelectedEvent().getIntention(); 
             if (i != Intention.EmptyInt && i.size() > 0) {
                 t = (Trigger) t.clone();
                 t.apply(i.peek().getUnif());
             }
            if (un.unifies(t, teFromL)) {
                 return true;
             }
         }
 
         for (Event ei : C.getEvents()) {
             Trigger t = ei.getTrigger();
             Intention i = ei.getIntention(); 
             if (i != Intention.EmptyInt && i.size() > 0) {
                 t = (Trigger) t.clone();
                 t.apply(i.peek().getUnif());
             }
            if (un.unifies(t, teFromL)) {
                 return true;
             }
         }
 
         return super.intends(C, l, un); // Int subset Des (see the formal definitions)
     }
 }
