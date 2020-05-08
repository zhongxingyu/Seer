 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import com.google.inject.Inject;
 
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.TimeStampedAction;
 import uk.ac.imperial.presage2.core.network.Message;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.rules.MessagesToRuleEngine;
 
 /**
  * @author dws04
  *
  */
 public class IPConMsgToRuleEngine extends MessagesToRuleEngine {
 	
 	final private Logger logger = Logger.getLogger(this.getClass());
 
 	private final StatefulKnowledgeSession sessionPtr;
 
 	@Inject
 	public IPConMsgToRuleEngine(StatefulKnowledgeSession session) {
 		super(session);
 		sessionPtr = session;
 	}
 	
 	/**
 	 * Inserts the IPConAction to the kbase if the message is an instanceof IPConActionMsg,
 	 * otherwise inserts the message to the kbase.
 	 * 
 	 * @param m Message to be processed
 	 * @return Message the message that was processed
 	 * @see uk.ac.imperial.presage2.rules.MessagesToRuleEngine#constrainMessage(uk.ac.imperial.presage2.core.network.Message)
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public Message constrainMessage(Message m){
 		logger.trace("Checking " + m + " for constraints");
 		if (m instanceof IPConActionMsg) {
			//logger.trace(m + " is an IPConActionMsg");
 			IPConAction action = ((IPConActionMsg)m).getData();
			//logger.trace("Action was " + action);
 			if (action instanceof TimeStampedAction) {
 				((TimeStampedAction) action).setT(SimTime.get().intValue());
				//logger.trace("Set the time to " + SimTime.get().intValue());
 			}
			//logger.trace("Inserting...");
 			sessionPtr.insert(action);
			//logger.trace("Inserted.");
 			logger.trace("Inserting " + ((IPConActionMsg)m).getData() + " to kbase. Message was " + m + ".");
 			return m;
 		}
 		else {
 			logger.trace("Not inserting " + m + " to kbase.");
 			return m;
 		}
 	}
 
 }
