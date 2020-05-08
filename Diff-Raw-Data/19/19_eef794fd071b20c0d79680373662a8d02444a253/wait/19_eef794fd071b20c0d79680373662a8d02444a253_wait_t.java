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
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
 //   Revision 1.1  2006/02/27 18:43:52  jomifred
 //   creation of the RuntimeServices interface
 //
 //   Revision 1.7  2005/12/31 16:29:58  jomifred
 //   add operator =..
 //
 //   Revision 1.6  2005/12/23 00:51:00  jomifred
 //   StringTerm is now an interface implemented by StringTermImpl
 //
 //   Revision 1.5  2005/12/22 00:04:19  jomifred
 //   ListTerm is now an interface implemented by ListTermImpl
 //
 //   Revision 1.4  2005/11/09 23:39:01  jomifred
 //   works for strings, numbers, ...
 //
 //   Revision 1.3  2005/08/12 22:20:10  jomifred
 //   add cvs keywords
 //
 //
 //----------------------------------------------------------------------------
 
 package jason.stdlib;
 
 import jason.asSemantics.Circumstance;
 import jason.asSemantics.CircumstanceListener;
 import jason.asSemantics.Event;
 import jason.asSemantics.Intention;
 import jason.asSemantics.InternalAction;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.NumberTerm;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 
 import java.util.logging.Level;
 
 public class wait implements InternalAction {
	// TODO: implement timeout for wait event
    
 	/**
 	 * args[0] is either a number or a string, if number it is the time (in ms), 
 	 * if string it is the trigger event to be waited.
 	 * 
 	 * E.g.: .wait(1000)       // waits 1 second
 	 *       .wait("+!t(50)")  // waits the event +!t(50)
 	 * 
 	 */
 	public boolean execute(final TransitionSystem ts, Unifier un, Term[] args)	throws Exception {
 		if (args[0].isNumber()) {
 			// time in mile seconds
 			NumberTerm time = (NumberTerm)args[0].clone();
 			un.apply((Term)time);
 			try {
 				Thread.sleep((long)time.solve());
 			} catch (Exception e) {		}
 			return true;
 		} else if (args[0].isString()) {
 			// wait event
 			try {
 				StringTerm st = (StringTerm)args[0];
 				un.apply((Term)st);
 				Trigger te = Trigger.parseTrigger(st.getString());
 				new WaitEvent(te, un, ts).start();
 				return true;
 			} catch (Exception e) {
 				ts.getLogger().log(Level.SEVERE, "Error at .wait.",e);
 			}
 		}
 		return false;
 	}
 
 	class WaitEvent extends Thread implements CircumstanceListener {
 		Trigger te;
 		Unifier un; 
 		Intention si;
 		TransitionSystem ts;
 		Circumstance c;
        boolean ok = false;
 		
 		WaitEvent(Trigger te, Unifier un, TransitionSystem ts) {
 			this.te = te;
 			this.un = un;
 			this.ts = ts;
 			c = ts.getC();
 			si = c.getSelectedIntention();
 		}
 
 		public void run() {
 			try {
 				// register listener
 				c.addEventListener(this);		
 		
 				waitEvent();
 
 				// unregister (to not receive intentionAdded again)
 				c.removeEventListener(this);
 
 				// add SI again in C.I
 				c.addIntention(si);
 				
 			} catch (Exception e) {
 				ts.getLogger().log(Level.SEVERE, "Error at .wait thread",e);
 			}
 		}
 		
 		synchronized public void waitEvent() {
            while (!ok) {
                try {
                    wait();
    			} catch (Exception e) {
    			    e.printStackTrace();
                }
            }
 		}		
 
 		synchronized public void eventAdded(Event e) {
 			if (un.unifies(te,e.getTrigger())) {
                ok = true;
 				notifyAll();
 			}
 		}
 		
 		public void intentionAdded(Intention i) {
 			// if the .wait intention where is being added in C.I, remove it
 			if (i == si) {
 				if (!c.removeIntention(si)) {
 					ts.getLogger().warning("The following intentions sould be removed, but wasn't!"+si+"\nWait intention is"+i);
 				}
 			}
 		}
 
 	}	
 }
 
