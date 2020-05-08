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
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.Intention;
 import jason.asSemantics.Message;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.BodyLiteral;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.NumberTerm;
 import jason.asSyntax.Pred;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 
 /**
   <p>Internal action: <b><code>.send</code></b>.
   
   <p>Description: sends a message to an agent.
   
   <p>Parameters:<ul>
   
   <li>+ arg[0] (atom, string, or list): the receiver of the
   message. It is the unique name of the agent that will receive the
   message (or list of names).<br/>
 
   <li>+ arg[1] (atom): the illocutionary force of the message (tell,
   achieve, ...).<br/>
   
   <li>+ arg[2] (literal): the content of the message.<br/>
   
   <li><i>+ arg[3]</i> (any term [optional]): the answer of an ask
   message (for performatives askOne and askAll).<br/> 
   
   <li><i>+ arg[4]</i> (number [optional]): timeout (in milliseconds)
   when waiting for an ask answer.<br/> 
 
   </ul>
 
   <p>Messages with an <b>ask</b> illocutionary force can optionally have
   arguments 3 and 4. In case they are given, <code>.send</code> suspends the
   intention until an answer is received and unified with <code>arg[3]</code>,
   or the message request times out as specified by
   <code>arg[4]</code>. Otherwise, the intention is not suspended and the
   answer (which is a tell message) produces a belief addition event as usual.
   
   <p>Examples (suppose that agent <code>jomi</code> is sending the
   messages):<ul>
 
   <li> <code>.send(rafael,tell,value(10))</code>: sends <code>value(10)</code>
   to the agent named <code>rafael</code>. The literal
   <code>value(10)[source(jomi)]</code> will be added as a belief in
   <code>rafael</code>'s belief base.</li>
 
   <li> <code>.send(rafael,achieve,go(10,30)</code>: sends
   <code>go(10,30)</code> to the agent named <code>rafael</code>. When
   <code>rafael</code> receives this message, an event
   <code>&lt;+!go(10,30)[source(jomi)],T&gt;</code> will be added in
   <code>rafael</code>'s event queue.</li>
 
   <li> <code>.send(rafael,askOne,value(beer,X))</code>: sends
   <code>value(beer,X)</code> to the agent named rafael. This .send
   does not suspend the jomi's intention. An event like
   <code>+value(beer,10)[source(rafael)]</code> is generated in jomi's
   side when rafael answer the ask.</li>
 
   <li> <code>.send(rafael,askOne,value(beer,X),A)</code>: sends
   <code>value(beer,X)</code> to the agent named <code>rafael</code>. This
   action suspends <code>jomi</code>'s intention until <code>rafael</code>'s
   answer is received. The answer (something like <code>value(beer,10)</code>)
   unifies with <code>A</code>.</li>
 
   <li> <code>.send(rafael,askOne,value(beer,X),A,2000)</code>: as in the
   previous example, but agent <code>jomi</code> waits for 2 seconds. If no
   message is received by then, <code>A</code> unifies with
   <code>timeout</code>.</li>
 
   </ul>
 
   @see jason.stdlib.broadcast
   @see jason.stdlib.my_name
 
 */
 public class send extends DefaultInternalAction {
     
     private boolean lastSendWasSynAsk = false; 
     
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         Term to   = null;
         Term ilf  = null;
         Term pcnt = null;
         // check parameters
         try {
             to   = args[0];
             ilf  = args[1];
             pcnt = args[2];
 	        
             un.apply(to);
             
             if (!to.isAtom() && !to.isList() && !to.isString()) {
                 throw new JasonException("The TO parameter ('"+to+"') of the internal action 'send' is not an atom or list of atoms!");
             }
 
             un.apply(ilf);
             if (! ilf.isAtom()) {
                 throw new JasonException("The illocutionary force parameter ('"+ilf+"') of the internal action 'send' is not an atom!");
             }
             un.apply(pcnt);
 	        
             // remove source annots in the content (in case it is a pred)
             try {
                 ((Pred)pcnt).delSources();
             } catch (Exception e) {}
             
         } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action 'send' to '"+to+"' has not received three arguments.");
         } 
         Message m = new Message(ilf.toString(), null, null, pcnt.toString());
 
         // async ask has a fourth argument and should suspend the intention
         lastSendWasSynAsk = m.isAsk() && args.length > 3;
         if (lastSendWasSynAsk) {
         	ts.getC().getPendingIntentions().put(m.getMsgId(), ts.getC().getSelectedIntention());
         }
 
         // tell with 4 args is a reply to
         if (m.isTell() && args.length > 3) {
             Term mid = args[3];
            un.apply(mid);
             if (! mid.isAtom()) {
                throw new JasonException("The Message ID ('"+mid+"') parameter of the internal action 'send' is not an atom!");
             }
             m.setInReplyTo(mid.toString());
         }
         
         // send the message
         try {
             if (to.isList()) {
                 if (m.isAsk()) {
                     throw new JasonException("Cannot send 'ask' to a list of receivers!");                                                   
                 } else {
                     for (Term t: (ListTerm)to) {
                         if (t.isAtom() || t.isString()) {
                             String rec = t.toString();
                             if (t.isString()) {
                                 rec = ((StringTerm)t).getString();
                             }
                             m.setReceiver(rec);
                             ts.getUserAgArch().sendMsg(m);
                         } else {
                             throw new JasonException("The TO parameter ('"+t+"') of the internal action 'send' is not an atom!");
                         }
                     }
                 }
             } else {
                 String rec = to.toString();
                 if (to.isString()) {
                     rec = ((StringTerm)to).getString();
                 }
                 m.setReceiver(rec);
                 ts.getUserAgArch().sendMsg(m);
             }
             
             if (lastSendWasSynAsk && args.length == 5) {
                 // get the timeout deadline
                Term tto = (Term)args[4];
                 un.apply(tto);
                 if (tto.isNumeric()) {
                    new CheckTimeout((long)((NumberTerm)tto).solve(), m.getMsgId(), ts.getC()).start(); 
                 } else {
                    throw new JasonException("The 5th parameter of send must be a number (timeout) and not '"+tto+"'!");
                 }
             }
             
             return true;
         } catch (Exception e) {
             throw new JasonException("Error sending message " + m + "\nError: "+e);
         }
     }
 
     @Override
     public boolean suspendIntention() {
         return lastSendWasSynAsk;
     }
     
     
     private static Structure timeoutTerm = new Structure("timeout");
     
     class CheckTimeout extends Thread {
         
         private long timeout = 0;
         private String idInPending;
         private Circumstance c;
         
         public CheckTimeout(long to, String rw, Circumstance c) {
             this.timeout = to;
             this.idInPending = rw;
             this.c = c;
         }
         
         public void run() {
             try {
                 sleep(timeout);
 
                 // if the intention is still in PI, brings it back to C.I
                 Intention intention = c.getPendingIntentions().remove(idInPending);
                 if (intention != null) {
                     // unify "timeout" with the fourth parameter of .send
                     BodyLiteral send = intention.peek().removeCurrentStep();
                     intention.peek().getUnif().unifies(send.getLiteralFormula().getTerm(3), timeoutTerm);
                     // add the intention back in C.I
                     c.addIntention(intention);
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 }
