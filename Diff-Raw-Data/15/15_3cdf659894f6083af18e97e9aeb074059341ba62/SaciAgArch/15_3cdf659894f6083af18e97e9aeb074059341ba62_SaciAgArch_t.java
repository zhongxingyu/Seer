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
 import jason.architecture.AgArch;
 import jason.architecture.AgArchInfraTier;
 import jason.asSemantics.ActionExec;
 import jason.asSemantics.TransitionSystem;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Term;
 import jason.infra.centralised.RunCentralisedMAS;
 import jason.mas2j.ClassParameters;
 import jason.mas2j.parser.mas2j;
 import jason.runtime.MASConsoleGUI;
 import jason.runtime.RuntimeServicesInfraTier;
 import jason.runtime.Settings;
 
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 
 import saci.Config;
 import saci.MBoxChangedListener;
 import saci.MBoxSAg;
 import saci.Message;
 import saci.MessageHandler;
 
 /**
  * This class provides an agent architecture when using SACI
  * Infrastructure to run the MAS.
  * 
  * <p> Execution sequence: initAg, run (perceive, checkMail, act),
  * stopAg.
  */
 public class SaciAgArch extends saci.Agent implements AgArchInfraTier {
 
 	private static final long serialVersionUID = 1L;
 
 	// to get the percepts via SACI we need an extra mailbox (the
 	// normal mbox is used for inter-agent communication
     private MBoxSAg  mboxPercept = null;
 
     /** the user implementation of the architecture */
     protected AgArch userAgArch;
 
     private Map<String,ActionExec> myPA = new HashMap<String,ActionExec>();
     
     private Logger   logger;
 
     /**
      * Method used by SACI to initialise the agent:
      * args[0] is the agent architecture class, 
      * args[1] is the user Agent class,
      * args[2] is the user BB class,
      * args[3] is the AgentSpeak source file,
      * args[4] "options",
      * args[5] options.
      */
     public void initAg(String[] args) throws JasonException {
         // create a logger
         RunCentralisedMAS.setupLogger();
         logger = Logger.getLogger(SaciAgArch.class.getName() + "." + getAgName());
 
         // create the jasonId console
         if (MASConsoleGUI.hasConsole()) { // the logger created the MASConsole
             MASConsoleGUI.get().setTitle("MAS Console - " + getSociety());
             MASConsoleGUI.get().setAsDefaultOut();
         }
 
         // set the agent class
         try {
             String archClassName = null;
             if (args.length < 1) { // error
                 throw new JasonException("The Agent Architecture class name was not informed for the SaciAgArch creation!");
             } else {
                 archClassName = args[0].trim();
             }
 
             String agClassName = null;
             if (args.length < 2) { // error
                 throw new JasonException("The Agent class name was not informed for the CentralisedAgArch creation!");
             } else {
                 agClassName = args[1].trim();
             }
 
             // parse bb class
             mas2j parser = new mas2j(new StringReader(args[2].replace('$','\"')));
             ClassParameters bbPars = parser.classDef();
             
             // TODO: get and register user directives
 
             String asSource = null;
             if (args.length < 3) { // error
                 throw new JasonException("The AgentSpeak source file was not informed, cannot create the Agent!");
             } else {
                 asSource = args[3].trim();
             }
             Settings stts = new Settings();
             if (args.length > 4) {
                 if (args[4].equals("options")) {
                     stts.setOptions("[" + args[5] + "]");
                 }
             }
             userAgArch = (AgArch) Class.forName(archClassName).newInstance();
             userAgArch.setArchInfraTier(this);
             userAgArch.initAg(agClassName, bbPars, asSource, stts);
             logger.setLevel(userAgArch.getTS().getSettings().logLevel());
         } catch (Exception e) {
             running = false;
             throw new JasonException("as2j: error creating the agent class! - " + e.getMessage());
         }
 
         // enter in the Environment society
         try {
             Config c = new Config();
             c.set("society.name", getMBox().getSociety() + "-env");
             mboxPercept = new MBoxSAg(getMBox().getName(), c);
             mboxPercept.init();
             mboxPercept.setMboxChangedListener(new MBoxChangedListener() {
                 public void mboxChanged() {
                     wake();
                 }
             });
 
             mboxPercept.addMessageHandler("performCycle", "tell", null, "AS-ExecControl", new MessageHandler() {
                 public boolean processMessage(saci.Message m) {
                     int cycle = Integer.parseInt(m.get("cycle").toString());
                     userAgArch.setCycleNumber(cycle);
                     //userAgArch.getTS().receiveSyncSignal();
                     receiveSyncSignal();
                     return true; // no other message handler gives this message
                 }
             });
 
             //final Transformer stateSerializer = TransformerFactory.newInstance().newTransformer();
             mboxPercept.addMessageHandler("agState", "ask", null, "AS-ExecControl", new MessageHandler() {
                 public boolean processMessage(saci.Message m) {
                     saci.Message r = new saci.Message("(tell)");
                     r.put("receiver", m.get("sender"));
                     r.put("in-reply-to", m.get("reply-with"));
                     r.put("ontology", m.get("ontology"));
 
                     try {
                         Document agStateDoc = userAgArch.getTS().getAg().getAgState();
 
                         // serialize
                         // StringWriter so = new StringWriter();
                         // stateSerializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
                         // stateSerializer.transform(new DOMSource(agStateDoc),
                         // new StreamResult(so));
                         // r.put("content", so.toString());
                         r.putWithoutSerialization("content", agStateDoc);
 
                         mboxPercept.sendMsg(r);
                     } catch (Exception e) {
                         logger.log(Level.SEVERE, "Error sending message " + r, e);
                     }
                     return true; // no other message handler gives this message
                 }
             });
 
             getMBox().setMboxChangedListener(new MBoxChangedListener() {
                 public void mboxChanged() {
                     wake();
                 }
             });
 
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error entering the environment's society.", e);
         }
     }
 
     public String getAgName() {
         return super.getName();
     }
 
     public void stopAg() {
         running = false;
         new Thread() {
             public void run() {
                 userAgArch.stopAg();
             }
         }.start();
         receiveSyncSignal();
         wake(); // in case the agent is waiting messages
     }
 
 
     private Object sleepSync = new Object();
     
     public void sleep() {
         try {
             if (!userAgArch.getTS().getSettings().isSync()) {
                 logger.fine("Entering in sleep mode....");
                 synchronized (sleepSync) {
                     sleepSync.wait(1000); // wait for messages
                 }
             }
         } catch (InterruptedException e) {
         } catch (Exception e) {
             logger.log(Level.WARNING,"Error waiting mgs", e);
         }
     }
     
     public void wake() {
         synchronized (sleepSync) {
             sleepSync.notifyAll(); // notify sleep method
         }
     }
     
     public void run() {
         while (running) {
             TransitionSystem ts = userAgArch.getTS();
             while (running) {
                 if (ts.getSettings().isSync()) {
                     waitSyncSignal();
                     ts.reasoningCycle();
                     boolean isBreakPoint = false;
                     try {
                         isBreakPoint = ts.getC().getSelectedOption().getPlan().hasBreakpoint();
                         if (logger.isLoggable(Level.FINE)) logger.fine("Informing controller that I finished a reasoning cycle "+userAgArch.getCycleNumber()+". Breakpoint is " + isBreakPoint);
                     } catch (NullPointerException e) {
                         // no problem, there is no sel opt, no plan ....
                     }
                     informCycleFinished(isBreakPoint, userAgArch.getCycleNumber());
                 } else {
                     ts.reasoningCycle();
                 }
             }
         }
 
         super.stopAg();
         mboxPercept.disconnect();
         //if (MASConsoleGUI.hasConsole()) { // the logger created the MASConsole
         //    MASConsoleGUI.get().close();
         //}
 
         logger.fine("finished running.\n");
     }
 
     // Default functions for the overall agent architecture (based on SACI)
     // they facilitate things a lot in case the programmer doesn't need
     // anything special
 
     // Default perception assumes Complete and Accurate sensing.
     // In the case of the SACI Architecture, the results of requests
    // for action execution is also received here.
 
     @SuppressWarnings("unchecked")
     public List perceive() {
         if (!running) {
             return null;
         }
 
         List percepts = null;
 
         saci.Message askMsg = new saci.Message("(ask-all :receiver environment :ontology AS-Perception :content getPercepts)");
 
         // asks current environment state (positive percepts)
         saci.Message m = null;
         try {
             m = mboxPercept.ask(askMsg);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error receiving percepts.", e);
         }
         if (m != null) {
             Object content = m.get("content");
             if (content != null && content.toString().startsWith("[")) {
                 percepts = ListTermImpl.parseList(content.toString()).getAsList();
                 if (logger.isLoggable(Level.FINE)) {
                     logger.fine("received percepts: " + percepts);
                 }
             } else {
                 percepts = null; // used to indicate that are nothing new in
                                  // the environment, no BUF needed
             }
         }
 
         // check if there are feedbacks on requested action executions
         try {
             do {
                 m = mboxPercept.receive();
                 if (m != null) {
                     if (m.get("ontology") != null) {
                         if (((String) m.get("ontology")).equals("AS-Action")) {
                             String irt = (String) m.get("in-reply-to");
                             if (irt != null) {
                                 ActionExec a = myPA.remove(irt); //userAgArh.getTS().getC().getPendingActions().remove(irt);
                                 // was it a pending action?
                                 if (a != null) {
                                     if (((String) m.get("content")).equals("ok")) {
                                         a.setResult(true);
                                     } else {
                                         a.setResult(false);
                                     }
                                     userAgArch.getTS().getC().addFeedbackAction(a);
                                 } else {
                                     logger.log(Level.SEVERE, "Error: received feedback for an Action that is not pending.");
                                 }
                             } else {
                                 throw new JasonException("Cannot identify executed action.");
                             }
                         }
                     }
                 }
             } while (m != null);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error receiving message.", e);
         }
         return percepts;
     }
 
     // this is used by the .send internal action in stdlib
     /** the saci implementation of the sendMsg interface */
     public void sendMsg(jason.asSemantics.Message m) throws Exception {
         saci.Message msaci = jasonToKQML(m);
         msaci.put("receiver", m.getReceiver());
         if (m.getInReplyTo() != null) {
             msaci.put("in-reply-to", m.getInReplyTo());
         }
         getMBox().sendMsg(msaci);
     }
 
     public void broadcast(jason.asSemantics.Message m) throws Exception {
         saci.Message msaci = jasonToKQML(m);
         getMBox().broadcast(msaci);
     }
     
     private saci.Message jasonToKQML(jason.asSemantics.Message m) {
         saci.Message msaci = new saci.Message("(" + m.getIlForce() + ")");
         if (m.getPropCont() instanceof Term) { // send content as string if it is a Term (it is better for interoperability)
         	msaci.put("content", m.getPropCont().toString());        	
         } else {
         	msaci.put("content", m.getPropCont());
         }
         msaci.put("reply-with", m.getMsgId());
         msaci.put("language", "AgentSpeak");
         return msaci;
     }
 
     // Deafult procedure for checking messages
     public void checkMail() {
         if (!running) {
             return;
         }
         if (getMBox() == null) {
             logger.warning("I have no mail box!");
             return;
         }
 
         saci.Message m = null;
         do {
             try {
                 m = getMBox().receive();
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Error receiving message.", e);
             }
             if (m != null) {
                 if (logger.isLoggable(Level.FINE)) {
                     logger.fine("Received message: " + m + ". Content class is " + m.get("content").getClass().getName());
                 }
                 String ilForce = (String) m.get("performative");
                 String sender = (String) m.get("sender");
                 String receiver = (String) m.get("receiver");
                 String replyWith = (String) m.get("reply-with");
                 String irt = (String) m.get("in-reply-to");
 
                 Object propCont = m.get("content");
                 if (propCont != null) {
                 	/*
                     String sPropCont = propCont.toString();
                     if (sPropCont.startsWith("\"")) { // deal with a term enclosed by "
                         sPropCont = sPropCont.substring(1, sPropCont.length() - 1);
                         if (DefaultTerm.parse(sPropCont) != null) {
                             // it was a term with "
                             propCont = sPropCont.trim();
                         }
                     }
                     */
 
                     jason.asSemantics.Message im = new jason.asSemantics.Message(ilForce, sender, receiver, propCont, replyWith);
                     if (irt != null) {
                         im.setInReplyTo(irt);
                     }
                     userAgArch.getTS().getC().getMailBox().add(im);
                 }
             }
         } while (m != null);
     }
 
     // Default acting on the environment
     public void act(ActionExec action, List<ActionExec> feedback) {
         if (!running) {
             return;
         }
         TransitionSystem ts = userAgArch.getTS();
         try {
             Term acTerm = action.getActionTerm();
             logger.info("doing: " + acTerm);
 
             String rw = mboxPercept.getRW();
             saci.Message m = new saci.Message("(ask :receiver environment :ontology AS-Action :content execute)");
             m.put("action", acTerm.toString());
             m.put("reply-with", rw);
             m.put("verbose", new Integer(ts.getSettings().verbose()).toString());
 
             mboxPercept.sendMsg(m);
 
             myPA.put(rw, action); //ts.getC().getPendingActions().put(rw, action);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error sending action " + ts.getC().getAction(), e);
         }
     }
 
     public boolean canSleep() {
         try {
             return getMBox().getMessages(null, 1, 0, false).size() == 0 && isRunning();
         } catch (Exception e) {
             return true;
         }
     }
 
     
     private Object  syncMonitor       = new Object(); 
     private boolean inWaitSyncMonitor = false;
 
     /**
      * waits for a signal to continue the execution (used in synchronized
      * execution mode)
      */
     private void waitSyncSignal() {
         try {
             synchronized (syncMonitor) {
                 inWaitSyncMonitor = true;
                 syncMonitor.wait();
                 inWaitSyncMonitor = false;
             }
         } catch (InterruptedException e) {
         } catch (Exception e) {
             logger.log(Level.WARNING,"Error waiting sync (1)", e);
         }
     }
 
     /**
      * inform this agent that it can continue, if it is in sync mode and
      * wainting a signal
      */
     private void receiveSyncSignal() {
         if (userAgArch.getTS().getSettings().isSync()) {
             try {
                 synchronized (syncMonitor) {
                     while (!inWaitSyncMonitor && isRunning()) {
                         // waits the agent to enter in waitSyncSignal
                         syncMonitor.wait(50); 
                     }
                     syncMonitor.notifyAll();
                 }
             } catch (InterruptedException e) {
             } catch (Exception e) {
                 logger.log(Level.WARNING,"Error waiting sync (2)", e);
             }
         }
     }
     
     private static Message cycleFinished = new Message("(tell :receiver controller :ontology AS-ExecControl :content cycleFinished)");
 
     /** 
      *  Informs the infrastructure tier controller that the agent 
      *  has finished its reasoning cycle (used in sync mode).
      *  
      *  <p><i>breakpoint</i> is true in case the agent selected one plan 
      *  with the "breakpoint" annotation.  
      */ 
     public void informCycleFinished(boolean breakpoint, int cycle) {
         // send a message to the executionControl agent
         Message m = (Message) cycleFinished.clone();
         if (breakpoint) {
             m.put("breakpoint", "true");
         }
         m.put("cycle", String.valueOf(cycle));
         mboxPercept.sendMsg(m);
     }
 
     public RuntimeServicesInfraTier getRuntimeServices() {
         return new SaciRuntimeServices(getSociety());
     }
 
 }
