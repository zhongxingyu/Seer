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
 import jason.asSyntax.NumberTerm;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 
 import java.util.logging.Level;
 
 public class wait extends DefaultInternalAction {
 
     /**
      * args[0] is either a number or a string, if number it is the time (in ms),
      * if string it is the trigger event to be waited. this second use also
      * receive the timeout (in ms) as parameter.
      * 
     * E.g.: .wait(1000) // waits 1 second 
     * .wait("+!t(50)") // waits the event
     * +!t(50) .wait("+!t(50)", 2000) // waits the event +!t(50) for 2 seconds
      * 
      */
     @Override
     public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         long timeout = -1;
         Trigger te = null;
         try {
             if (args[0].isNumeric()) {
                 // time in mile seconds
                 NumberTerm time = (NumberTerm) args[0].clone();
                 un.apply(time);
                 timeout = (long) time.solve();
                 // Thread.sleep((long) time.solve());
             } else if (args[0].isString()) {
                 // wait event
                 StringTerm st = (StringTerm) args[0];
                 un.apply((Term) st);
                 te = Trigger.parseTrigger(st.getString());
 
                 if (args.length == 2) {
                     NumberTerm tot = (NumberTerm) args[1].clone();
                     un.apply(tot);
                     timeout = (long) tot.solve();
                 }
 
             }
         } catch (Exception e) {
             ts.getLogger().log(Level.SEVERE, "Error at .wait.", e);
             return false;
         }
         WaitEvent wet = new WaitEvent(te, un, ts, timeout);
         wet.start();
         return true;
     }    
 
     @Override
     public boolean suspendIntention() {
         return true;
     }  
 
     class WaitEvent extends Thread implements CircumstanceListener {
         Trigger          te;
         String           sTE; // an string version of TE
         Unifier          un;
         Intention        si;
         TransitionSystem ts;
         Circumstance     c;
         boolean          ok      = false;
         boolean          drop    = false;
         long             timeout = -1;
         
         WaitEvent(Trigger te, Unifier un, TransitionSystem ts, long to) {
             this.te = te;
             this.un = un;
             this.ts = ts;
             c = ts.getC();
             si = c.getSelectedIntention();
             this.timeout = to;
 
             // register listener
             c.addEventListener(this);
             
             if (te != null) {
                 sTE = te.toString();
             } else {
                 sTE = "time"+(timeout);
             }
             sTE = si.getId()+"/"+sTE;
             c.getPendingIntentions().put(sTE, si);
         }
 
         public void run() {
             try {
                 waitEvent();
 
                 // unregister (to not receive intentionAdded again)
                 c.removeEventListener(this);
 
                // add SI again in C.I if it was removed and this wait was dropped
                 if (!c.getIntentions().contains(si) && !drop) {
                     si.peek().removeCurrentStep();
                     c.addIntention(si);
                 }
                 c.getPendingIntentions().remove(sTE);
 
             } catch (Exception e) {
                 ts.getLogger().log(Level.SEVERE, "Error at .wait thread", e);
             }
         }
 
         synchronized public void waitEvent() {
             long init = System.currentTimeMillis();
             long pass = 0;
             while (!ok || drop) {
                 try {
                     if (timeout == -1) {
                         wait();
                     } else {
                         long to = timeout - pass;
                         if (to <= 0)
                             to = 100;
                         wait(to);
                         pass = System.currentTimeMillis() - init;
                         if (pass >= timeout) {
                             break;
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
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
 
         public void intentionAdded(Intention i) {  }
         
         public String toString() {
             return sTE;
         }
 
     }
 }
