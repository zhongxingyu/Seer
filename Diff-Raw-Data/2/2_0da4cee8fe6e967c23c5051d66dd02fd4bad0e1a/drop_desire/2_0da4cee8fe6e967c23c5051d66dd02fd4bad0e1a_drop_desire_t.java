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
 
 import java.util.Iterator;
 
 
 /**
   <p>Internal action: <b><code>.drop_desire(<i>D</i>)</code></b>.
   
   <p>Description: removes desire <i>D</i> from the agent circumstance. 
   This internal action simply removes all <i>+!D</i> entries
   (those for which <code>.desire(D)</code> would succeed) from both 
   the set of events and the set of intentions.
   No event is produced as a consequence of dropping desires.
 
   <p>Example:<ul> 
 
   <li> <code>.drop_desire(go(X,3))</code>: remove desires such as
   <code>&lt;+!go(1,3),_&gt;</code> from the set of events and 
   intentions having plans with triggering events such as 
   <code>+!go(1,3)<code>.
 
   </ul>
  
   @see jason.stdlib.current_intention
   @see jason.stdlib.desire
   @see jason.stdlib.drop_all_desires
   @see jason.stdlib.drop_all_events
   @see jason.stdlib.drop_all_intentions
   @see jason.stdlib.drop_event
   @see jason.stdlib.drop_intention
   @see jason.stdlib.succeed_goal
   @see jason.stdlib.fail_goal
   @see jason.stdlib.intend
 
 
  */
 public class drop_desire extends drop_intention {
     
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         try {
             dropEvt(ts.getC(), (Literal)args[0], un);
             dropInt(ts.getC(), (Literal)args[0], un);
             return true;
         } catch (ArrayIndexOutOfBoundsException e) {
             throw new JasonException("The internal action 'drop_desire' has not received the required argument.");
         } catch (Exception e) {
             throw new JasonException("Error in internal action 'drop_desire': " + e, e);
         }
     }
     
     public void dropEvt(Circumstance C, Literal l, Unifier un) {
         Trigger te = new Trigger(TEOperator.add, TEType.achieve, l);
         Iterator<Event> ie = C.getEvents().iterator();
         while (ie.hasNext()) {
         	Event  ei = ie.next();
             Trigger t = ei.getTrigger();
             if (ei.getIntention() != Intention.EmptyInt) {
                 t = (Trigger) t.clone();
                 t.apply(ei.getIntention().peek().getUnif());
             }
            if (un.copy().unifiesNoUndo(te, t)) {
                 // old implementation: t.setTrigType(Trigger.TEDel); // Just changing "+!g" to "-!g"
             	ie.remove();
             }
         }
     }
 }
