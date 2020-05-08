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
//   Revision 1.14  2005/10/07 18:53:35  jomifred
//   fix a bug in console (it tried to use X11 event with Console logger)
//
 //   Revision 1.13  2005/08/16 21:03:42  jomifred
 //   add some comments on TODOs
 //
 //   Revision 1.12  2005/08/15 17:41:36  jomifred
 //   AgentArchitecture renamed to AgArchInterface
 //
 //   Revision 1.11  2005/08/13 13:55:35  jomifred
 //   java doc updated
 //
 //   Revision 1.10  2005/08/12 22:19:26  jomifred
 //   add cvs keywords
 //
 //
 //----------------------------------------------------------------------------
 
 
 package jason.architecture;
 
 
import jIDE.MASConsoleGUI;
 import jIDE.RunCentralisedMAS;
 import jason.JasonException;
 import jason.asSemantics.ActionExec;
 import jason.asSemantics.Agent;
 import jason.asSemantics.TransitionSystem;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.Term;
 
 import java.io.File;
 import java.util.List;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.w3c.dom.Document;
 
 import saci.Config;
 import saci.MBoxChangedListener;
 import saci.MBoxSAg;
 import saci.Message;
 import saci.MessageHandler;
 
 /**
  * This class provides an agent architecture when using SACI Infra Structure
  * to run the MAS.
  * 
  * <p>Execution sequence: initAg, run (perceive, checkMail, act), stopAg.
  */
 
 public class SaciAgArch extends saci.Agent implements AgArchInterface {
     
     // to get the percepts via SACI (the normal mbox is used for inter-agent com.)
     private MBoxSAg mboxPercept = null;
 
 	private TransitionSystem fTS = null;
 
 	private Logger logger;
     
     // this is used by SACI to initialize the agent
     public void initAg(String[] args) throws JasonException {
 
        
     	// create a logger
     	logger = Logger.getLogger(SaciAgArch.class.getName()+"."+getAgName());
         if (new File(RunCentralisedMAS.logPropFile).exists()) {
         	PropertyConfigurator.configure(RunCentralisedMAS.logPropFile);
         } else {
         	PropertyConfigurator.configure(SaciAgArch.class.getResource("/"+RunCentralisedMAS.logPropFile));
         }

    	// create the jasonId console
        if (MASConsoleGUI.hasConsole()) { // the logger created the MASConsole
        	MASConsoleGUI.get().setTitle("MAS Console - "+getSociety());
        	MASConsoleGUI.get().setAsDefaultOut();
        }

         // set the agent class
         try {
             String className = null;
             if (args.length < 1) { // error
             	running = false;
                 throw new JasonException("The Agent class name was not informed for the SaciAgArch creation!");
             } else {
                 className = args[0].trim();
             }
             Agent ag = (Agent)Class.forName(className).newInstance();
             fTS = ag.initAg(args, this);
     		logger.setLevel(fTS.getSettings().log4JLevel());
         } catch (Exception e) {
             throw new JasonException("as2j: error creating the agent class! - "+e.getMessage());
         }
         
         // enter in the Environment society
         try {
             Config c = new Config();
             c.set("society.name", getMBox().getSociety()+"-env");
             mboxPercept = new MBoxSAg( getMBox().getName(), c);
             mboxPercept.init();
             mboxPercept.setMboxChangedListener(new MBoxChangedListener() {
                 public void mboxChanged() {
                     if (fTS != null) {
                         fTS.newMessageHasArrived();
                     }
                 }
             });
             
             mboxPercept.addMessageHandler("performCycle", "tell", null, "AS-ExecControl", new MessageHandler() {
                 public boolean processMessage(saci.Message m) {
                     fTS.receiveSyncSignal();
                     return true; // no other message handler gives this message
                 }
             });
 
             final Transformer stateSerializer = TransformerFactory.newInstance().newTransformer();
             mboxPercept.addMessageHandler("agState", "ask", null, "AS-ExecControl", new MessageHandler() {
                     public boolean processMessage(saci.Message m) {
                         saci.Message r = new saci.Message("(tell)");
                         r.put("receiver", m.get("sender"));
                         r.put("in-reply-to", m.get("reply-with"));
                         r.put("ontology", m.get("ontology"));
                         
                         try {
                             Document agStateDoc = fTS.getAg().getAgState();
                             
                             // serialize
                             //StringWriter so = new StringWriter();
                             //stateSerializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
                             //stateSerializer.transform(new DOMSource(agStateDoc), new StreamResult(so));
                             //r.put("content", so.toString());
                             r.putWithoutSerialization("content", agStateDoc);
 
                             mboxPercept.sendMsg(r);
                         } catch (Exception e) {
                         	logger.error("Error sending message "+r,e);
                         }
                         return true; // no other message handler gives this message
                     }
                 });
                 
             getMBox().setMboxChangedListener(new MBoxChangedListener() {
                 public void mboxChanged() {
                     if (fTS != null) {
                         fTS.newMessageHasArrived();
                     }
                 }
             });
 
             
         } catch (Exception e) {
         	logger.error("Error entering the environment's society.",e);
         }
     }
     
     public String getAgName() {
     	return super.getName();
     }
     
     public void stopAg() {
         super.stopAg();
         mboxPercept.disconnect();
     }
     
     public void run() {
         while (running) {
             fTS.reasoningCycle();
         }
         logger.debug("finished running.\n");
     }
     
     // Default functions for the overall agent architecture (based on SACI)
     // they facilitate things a lot in case the programmer doesn't need
     // anything special
     
     // Default perception assumes Complete and Accurate sensing.
     // In the case of the SACI Architecture, the results of requests
     // for action execution is also recieved here.
     public List perceive() {
         if (! running) {
             return null;
         }
 		
 		List percepts = null;
         
         saci.Message askMsg = new saci.Message("(ask-all :receiver environment :ontology AS-Perception :content getPercepts)");
         
         // asks current environment state (positive percepts)
         saci.Message m = null;
         try {
             m = mboxPercept.ask(askMsg);
         } catch (Exception e) {
         	logger.error("Error receiving perceptions.",e);
         }
         if (m != null) {
 			String content = (String) m.get("content");
 			if (content != null) {
 				percepts = ListTerm.parseList(content).getAsList();
 				if (logger.isDebugEnabled()) {
 					logger.debug("received percepts: "+percepts);
 				}
 			} else {
 				percepts = null; // used to indicate that are nothing new in the environment, no BRF needed
 			}
         }
         
 	
         // check if there are feedbacks on requested action executions
         try {
             do {
                 m = mboxPercept.receive();
                 if (m != null) {
                     if (m.get("ontology") != null) {
                         if ( ((String)m.get("ontology")).equals("AS-Action")) {
                             String irt = (String)m.get("in-reply-to");
                             if (irt != null) {
                                 ActionExec a = (ActionExec)fTS.getC().getPendingActions().remove(irt);
                                 // was it a pending action?
                                 if (a != null) {
                                     if (((String)m.get("content")).equals("ok")) {
                                         a.setResult(true);
                                     }
                                     else {
                                         a.setResult(false);
                                     }
                                     fTS.getC().getFeedbackActions().add(a);
                                 }
                                 else {
                                 	logger.error("Error: received feedback for an Action that is not pending.");
                                 }
                             }
                             else {
                                 throw new JasonException("Cannot identify executed action.");
                             }
                         }
                     }
                 }
             } while (m != null);
         } catch (Exception e) {
         	logger.error("Error receiving message.",e);
         }
 		return percepts;
     }
     
     // this is used by the .send internal action in stdlib
     /** the saci implementation of the sendMsg interface */
     public void sendMsg(jason.asSemantics.Message m) throws Exception {
     	// suspend intention if it is an ask
     	if (m.isAsk()) {
             fTS.getC().getPendingActions().put(m.getMsgId(), fTS.getC().getSelectedIntention());    		
     	}
         saci.Message msaci = new saci.Message("("+m.getIlForce()+")");
         msaci.put("receiver", m.getReceiver());
         msaci.put("content", m.getPropCont());
         msaci.put("reply-with", m.getMsgId());
         if (m.getInReplyTo() != null) {
         	msaci.put("in-reply-to", m.getInReplyTo());
         }
         getMBox().sendMsg(msaci);
     }
 
     public void broadcast(jason.asSemantics.Message m) throws Exception {
         saci.Message msaci = new saci.Message("("+m.getIlForce()+")");
         msaci.put("content", m.getPropCont());
         getMBox().broadcast(msaci);
     }
     
     
     // Deafult procedure for checking messages
     public void checkMail() {
         if (! running) {
             return;
         }
         if (getMBox() == null) {
             logger.warn("I have no mail box!");
             return;
         }
         
         saci.Message m  = null;
         do {
             try {
                 m  = getMBox().receive();
             } catch (Exception e) {
             	logger.error("Error receiving message.",e);
             }
             if (m != null) {
                 String ilForce   = (String)m.get("performative");
                 String sender    = (String)m.get("sender");
                 String receiver  = (String)m.get("receiver");
                 String replyWith = (String)m.get("reply-with");
                 String irt       = (String)m.get("in-reply-to");
                 
                 String propCont  = null;
                 if (m.get("content") != null) {
                     propCont = m.get("content").toString();
                     if (propCont.startsWith("\"")) {
                     	propCont = propCont.substring(1,propCont.length()-1);
                     	if (Term.parse(propCont) == null) {
                     		// it was a string indeed
                     		propCont = m.get("content").toString();
                     	}
                     }
                     if (Term.parse(propCont) != null) { // the contents are well formed
                         jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, receiver, propCont, replyWith);
                         if (irt != null) {
                         	im.setInReplyTo(irt);
                         }
                         fTS.getC().getMB().add(im);
                         logger.info("received message: " + im);
                     } else {
                         logger.warn("Warning! Message received cannot be handled: "+m);
                     }
                 }
             }
         } while (m != null);
     }
     
     // Default acting on the environment
     // it gets action from CA; it must be NULL after that!
     public void act() {
         if (! running) {
             return;
         }
         if (fTS.getC().getAction() == null)
             return;
         try {
         	Term acTerm = fTS.getC().getAction().getActionTerm();
         	logger.info("doing: "+acTerm);
 
             String rw = mboxPercept.getRW();
             saci.Message m = new saci.Message("(ask :receiver environment :ontology AS-Action :content execute)");
             m.put("action", acTerm.toString());
             m.put("reply-with", rw);
             m.put("verbose", new Integer(fTS.getSettings().verbose()).toString());
             
             mboxPercept.sendMsg(m);
             
             fTS.getC().getPendingActions().put(rw, fTS.getC().getAction());
         } catch (Exception e) {
         	logger.error("Error sending action "+ fTS.getC().getAction(),e);
         }
     }
 
     private static Message cycleFinished = new Message("(tell :receiver controller :ontology AS-ExecControl :content cycleFinished)");
     
 	/** inform the remote controller that this agent's cycle was finished (used in sync mode) */ 
 	public void informCycleFinished(boolean breakpoint) {
 		// send a message to the executionControl agent
 	    Message m = (Message)cycleFinished.clone();
 	    if (breakpoint) {
 	    	m.put("breakpoint","true");
 	    }
 		mboxPercept.sendMsg(m);
 	}
 
 }
