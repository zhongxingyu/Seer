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
 
 package jason.infra.saci;
 
 import jason.JasonException;
 import jason.control.ExecutionControl;
 import jason.control.ExecutionControlInfraTier;
 import jason.runtime.RuntimeServicesInfraTier;
 
 import org.w3c.dom.Document;
 
 import saci.Message;
 import saci.MessageHandler;
 
 /**
  * Concrete execution control implementation based on saci distributed
  * infrastructure.
  */
 public class SaciExecutionControl extends saci.Agent implements ExecutionControlInfraTier {
 
     private ExecutionControl fUserControl;
 
     public void initAg(String[] args) throws JasonException {
         // create the user controller
         try {
             System.out.println("Creating controller from " + args[0]);
             fUserControl = (ExecutionControl) Class.forName(args[0]).newInstance();
             fUserControl.setExecutionControlInfraTier(this);
             // create parameters array
             String[] p = new String[args.length-1];
             for (int i=0; i<p.length; i++) {
                 p[i] = args[i+1];
             }
             fUserControl.init(p);
         } catch (Exception e) {
             System.err.println("Error " + e);
             e.printStackTrace();
             throw new JasonException("The user execution control class instantiation '" + args[1] + "' has failed!" + e.getMessage());
         }
 
         try {
             // message handler for "informCycleFinished"
             mbox.addMessageHandler("cycleFinished", "tell", null, "AS-ExecControl", new MessageHandler() {
                 public boolean processMessage(saci.Message m) {
                     String sender = (String) m.get("sender");
                     boolean breakpoint = false;
                     if (m.get("breakpoint") != null) {
                         breakpoint = m.get("breakpoint").equals("true");
                     }
                     fUserControl.receiveFinishedCycle(sender, breakpoint);
                     return true; // no other message handler gives this
                                     // message
                 }
             });
 
         } catch (Exception e) {
             System.err.println("Error adding message handler for agent:" + e);
             e.printStackTrace();
         }
     }
 
     public void stopAg() {
         fUserControl.stop();
         super.stopAg();
     }
 
     public void run() {
         try {
             Thread.sleep(1000); // gives a time to agents enter in wait
         } catch (Exception e) {
         }
         informAllAgsToPerformCycle();
     }
 
     public ExecutionControl getUserControl() {
         return fUserControl;
     }
 
     /**
      * @see jason.control.ExecutionControlInfraTier#informAgToPerformCycle(java.lang.String)
      */
     public void informAgToPerformCycle(String agName) {
         Message m = new Message("(tell)");
         m.put("ontology", "AS-ExecControl");
         m.put("receiver", agName);
         m.put("content", "performCycle");
         try {
             mbox.sendMsg(m);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @see jason.control.ExecutionControlInfraTier#informAllAgsToPerformCycle()
      */
     public void informAllAgsToPerformCycle() {
         Message m = new Message("(tell)");
         m.put("ontology", "AS-ExecControl");
         m.put("content", "performCycle");
         try {
             mbox.broadcast(m);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @see jason.control.ExecutionControlInfraTier#getAgState(java.lang.String)
      */
     public Document getAgState(String agName) {
         Message m = new Message("(ask)");
         m.put("ontology", "AS-ExecControl");
         m.put("receiver", agName);
         m.put("content", "agState");
         try {
             Message r = mbox.ask(m);
 
            if (r == null) {
                System.err.println("No agent state received! (possibly timeout in ask)");
            } else {
                return (Document) r.get("content");
            }
         } catch (Exception e) {
             System.err.println("Error receiving agent state " + e);
             e.printStackTrace();
         }
         return null;
     }
 
     public RuntimeServicesInfraTier getRuntimeServices() {
         return new SaciRuntimeServices(getSociety());
     }
 }
