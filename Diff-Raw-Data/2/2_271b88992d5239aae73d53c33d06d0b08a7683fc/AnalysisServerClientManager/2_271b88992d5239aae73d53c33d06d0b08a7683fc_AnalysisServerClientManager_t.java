 package gov.nih.nci.rembrandt.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.server.AnalysisRequestSender;
 import gov.nih.nci.caintegrator.analysis.server.AnalysisResultReceiver;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 import gov.nih.nci.caintegrator.service.findings.AnalysisFinding;
 import gov.nih.nci.rembrandt.cache.BusinessTierCache;
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.jms.DeliveryMode;
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.ObjectMessage;
 import javax.jms.Queue;
 import javax.jms.QueueConnection;
 import javax.jms.QueueConnectionFactory;
 import javax.jms.QueueReceiver;
 import javax.jms.QueueSender;
 import javax.jms.QueueSession;
 import javax.jms.Session;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author sahnih
  * singleton object
  */
 public class AnalysisServerClientManager implements MessageListener, ExceptionListener, AnalysisRequestSender, AnalysisResultReceiver{
 	private static Logger logger = Logger.getLogger(AnalysisServerClientManager.class);
 	private BusinessTierCache _cacheManager = ApplicationFactory.getBusinessTierCache();
 	
     private Properties messagingProps;
 	private QueueSession queueSession;
  
 	private QueueSender requestSender;
 	private QueueReceiver resultReceiver;
 
 	private Queue requestQueue;
 	private Queue resultQueue;
 	private QueueConnection queueConnection;
     private static AnalysisServerClientManager instance = null;
 	/**
 	 * @param properties
 	 * @throws NamingException 
 	 * @throws JMSException 
 	 */
 	@SuppressWarnings("unchecked")
 	private AnalysisServerClientManager() throws NamingException, JMSException {
 		logger.debug("Inside AnalysisServerClientManager");
 		messagingProps = ApplicationContext.getJMSProperties();
 		//Populate with needed properties
 		Hashtable props = new Hashtable();
 		props.put(Context.INITIAL_CONTEXT_FACTORY,
 		    "org.jnp.interfaces.NamingContextFactory");
 		props.put(Context.PROVIDER_URL, messagingProps.getProperty("JBOSS_URL"));
 		props.put("java.naming.rmi.security.manager", "yes");
 		props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming");
 		
 //		   Get the initial context with given properties
 		Context context = new InitialContext(props);  
 		  
 	    // Get the connection factory
 
 	    QueueConnectionFactory queueConnectionFactory =
 	      (QueueConnectionFactory)context.lookup(messagingProps.getProperty("FACTORY_JNDI"));
 
 	    // Create the connection
 	    queueConnection = queueConnectionFactory.createQueueConnection();
 
 	    queueConnection.setExceptionListener(this);
 	    
 	    // Create the session
 	    queueSession = queueConnection.createQueueSession(
 	      // No transaction
 	      false,
 	      // Auto ack
 	      Session.AUTO_ACKNOWLEDGE);
 
 	    // Look up the destination
 	    requestQueue = (Queue)context.lookup("queue/AnalysisRequest");
 	    resultQueue = (Queue)context.lookup("queue/AnalysisResponse");
 
 	    
 	    // Create a publisher
 	    requestSender = queueSession.createSender(requestQueue);
 		resultReceiver = queueSession.createReceiver(resultQueue);
 		resultReceiver.setMessageListener(this);
 		
 		queueConnection.start();
 	}
 	
 	/**
 	 * JMS notification about a new message
 	 */
 	public void onMessage(Message message) {
 		 //String msg = ((TextMessage)m).getText();
 	      ObjectMessage msg = (ObjectMessage) message;
 	      try {
 			Object result = msg.getObject();
 			if( result instanceof AnalysisResult){
 				receiveResult((AnalysisResult) result);
 			}
 			else if( result instanceof AnalysisServerException){
 				receiveException((AnalysisServerException) result);
 			}
 			
 		} catch (JMSException e) {
 			logger.error(e);
 		}
 		
 	}
 	
 	/**
 	 * JMS notification about an exception
 	 */
     public void onException(JMSException jmsException) {
 	  logger.error(jmsException);	
 	}
     /***
      * @param analysisResult is the result 
      */
     public void receiveResult(AnalysisResult analysisResult) {
     	String sessionId = analysisResult.getSessionId();
 		String taskId = analysisResult.getTaskId();
 		logger.debug("AnalysisResult session: "+sessionId+" & task: "+taskId+" has been returned");
 		logger.debug("Retreiving finding for session: "+sessionId+" & task: "+taskId+" from cache");
 		AnalysisFinding finding = (AnalysisFinding)_cacheManager.getSessionFinding(sessionId, taskId);
 		if(finding != null){
 			finding.setAnalysisResult(analysisResult);
 			finding.setStatus(FindingStatus.Completed);
 			logger.debug("Following task has been completed:/n  SessionId: "+sessionId+"/n  TaskId: "+taskId);
 			_cacheManager.addToSessionCache(sessionId,taskId,finding);
 			logger.debug("Following finding has been placed in cache:/n  SessionId: "+sessionId+"/n  TaskId: "+taskId);
 		}
 	}
 
 	public void receiveException(AnalysisServerException analysisServerException) {
 		String sessionId = analysisServerException.getFailedRequest().getSessionId();
 		String taskId = analysisServerException.getFailedRequest().getTaskId();
 		logger.debug("AnalysisServerException session: "+sessionId+" & task: "+taskId+" has been returned");
 		AnalysisFinding finding = (AnalysisFinding)_cacheManager.getSessionFinding(sessionId, taskId);
 		if(finding != null){
 			FindingStatus newStatus = FindingStatus.Error;
			newStatus.setComment(analysisServerException.getMessage());
 			finding.setStatus(newStatus);
 			logger.debug("Retreiving finding for session: "+sessionId+" & task: "+taskId+" from cache");
 			_cacheManager.addToSessionCache(sessionId,taskId+"_analysisServerException",analysisServerException);
 			_cacheManager.addToSessionCache(sessionId,taskId,finding);
 			logger.debug("Following finding has been placed in cache:/n  SessionId: "+sessionId+"/n  TaskId: "+taskId);
 			logger.error(analysisServerException);
 		}
 	}
 
 	/**
 	 * @return Returns the instance.
 	 */
 	public static AnalysisServerClientManager getInstance()  throws NamingException, JMSException{
 		//first time
 		if(instance == null){
 			try {
 				instance = new AnalysisServerClientManager();
 			} catch (NamingException e) {
 				logger.error(e.getMessage());
 				throw e;
 			} catch (JMSException e) {
 				logger.error(e.getMessage());
 				throw e;
 			}
 		}
 		return instance;
 	}
 
 	/**
 	 * Send an AnalysisRequest to the JMS request queue. Note this method does not store anything
 	 * in the cache. 
 	 * @throws JMSException 
 	 * @see sendRequest(Query query, AnalysisRequest request)
 	 */
 	public void sendRequest(AnalysisRequest request) throws JMSException {
 		ObjectMessage msg;
 		try {
 		    // Create a message
 			msg = queueSession.createObjectMessage(request);
 			// Send the message
 		    requestSender.send(msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
 			logger.debug("sendRequest session: "+request.getSessionId()+" & task: "+request.getTaskId()+" has been sent to the JMQ");
 
 		} catch (JMSException e) {
 			logger.error(e);
 			throw e;
 		}
 	}
 	
 }
