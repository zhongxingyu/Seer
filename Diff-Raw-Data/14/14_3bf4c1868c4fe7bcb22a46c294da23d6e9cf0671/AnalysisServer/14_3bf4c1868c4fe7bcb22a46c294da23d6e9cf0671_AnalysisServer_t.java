 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.*;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.ExceptionListener;
 import javax.jms.ObjectMessage;
 import javax.jms.QueueConnection;
 import javax.jms.QueueConnectionFactory;
 import javax.jms.Queue;
 import javax.jms.QueueSession;
 import javax.jms.QueueSender;
 import javax.jms.QueueReceiver;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.jms.DeliveryMode;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This class implements the analysis server module.
  * 
  * @author Michael A. Harris
  * 
  * 
  * AnalysisServer should register handlers which are specifed from a
  * configuration file.
  */
 public class AnalysisServer implements MessageListener, ExceptionListener, AnalysisResultSender {
 
	public static String version = "3.1";
 
 	private boolean debugRcommands = false;
 
 	private static String JBossMQ_locationIp = "156.40.128.136:1099"; // my machine
 																		
 	private static int numComputeThreads = 1;
 
 	private static String RserverIp = "156.40.134.31"; // cbiodev501
 
 	private static String RdataFileName = "/h1/harrismic/Rembrandt/RdataFiles/dataAndFunctions.R";
 
 	private RThreadPoolExecutor executor;
 
 	private QueueConnection queueConnection;
 
 	private Queue requestQueue;
 
 	private Queue resultQueue;
 
 	private QueueSession queueSession;
 	
 	private Hashtable contextProperties = new Hashtable();
 	
 	private String factoryJNDI = null;
 	
 	private static long reconnectWaitTimeMS = 10000L;
 
 	/**
 	 * Subscriber
 	 */
 	// TopicSubscriber topicSubscriber;
 	private QueueReceiver requestReceiver;
 
 	private QueueSender resultSender;
 
 	/**
 	 * Destination to subscribe to
 	 */
 	// Topic topic;
 	/**
 	 * Sets up all the JMS fixtures.
 	 * 
 	 * Use close() when finished with object.
 	 * 
 	 * @param factoryJNDI
 	 *            name of the topic connection factory to look up.
 	 * @param topicJNDI
 	 *            name of the topic destination to look up
 	 */
 	public AnalysisServer(String factoryJNDI, String serverPropertiesFileName) throws JMSException,
 			NamingException {
 		
 		this.factoryJNDI = factoryJNDI;
 
 		// load properties from a properties file
 		Properties analysisServerConfigProps = new Properties();
 
 		try {
 			analysisServerConfigProps.load(new FileInputStream(
 					serverPropertiesFileName));
 			JBossMQ_locationIp = analysisServerConfigProps
 					.getProperty("jmsmq_location");
 			RserverIp = analysisServerConfigProps
 					.getProperty("rserve_location");
 			numComputeThreads = Integer.parseInt(analysisServerConfigProps
 					.getProperty("num_compute_threads"));
 			RdataFileName = analysisServerConfigProps
 					.getProperty("RdefinitionFile");
 			debugRcommands = Boolean.parseBoolean(analysisServerConfigProps
 					.getProperty("debugRcommands"));
 			reconnectWaitTimeMS = Long.parseLong(analysisServerConfigProps
 					.getProperty("reconnectWaitTimeMS"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace(System.out);
 		} catch (IOException e) {
 			e.printStackTrace(System.out);
 		} catch (NumberFormatException nfException) {
 			nfException.printStackTrace(System.out);
 		}
 		
 		// initialize the compute threads
 		
 		executor = new RThreadPoolExecutor(numComputeThreads, RserverIp,
 				RdataFileName, this);
 		
 		executor.setDebugRcommmands(debugRcommands);
 		
 		//establish the JMS queue connections
 		contextProperties.put(Context.INITIAL_CONTEXT_FACTORY,
 		   "org.jnp.interfaces.NamingContextFactory");
 		contextProperties.put(Context.PROVIDER_URL, JBossMQ_locationIp);
 		contextProperties.put("java.naming.rmi.security.manager", "yes");
 		contextProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.naming");
 		
 		establishQueueConnection();
 		
 
 		System.out.println("AnalysisServer version=" + version
 				+ " successfully initialized. numComputeThreads=" + numComputeThreads + " RserverIp=" + RserverIp + " RdataFileName=" + RdataFileName);
 
 	}
 
 	public AnalysisServer(String factoryJNDI) throws JMSException,
 	NamingException {
 	  this(factoryJNDI, "analysisServer.properties");
 	}
 	
 	/**
 	 * Establish a connection to the JMS queues.  If it is not possible
 	 * to connect then this method will sleep for reconnectWaitTimeMS milliseconds and
 	 * then try to connect again.  
 	 *
 	 */
 	private void establishQueueConnection() {
         
 		boolean connected = false;
 		Context context = null;
 		
 		while (!connected) {
 		
 			try {
 				
 			  System.out.println("Attempting to establish queue connection with provider: " + contextProperties.get(Context.PROVIDER_URL));
 				
 			  //Get the initial context with given properties
 			  context = new InitialContext(contextProperties);
 	
 			  requestQueue = (Queue) context.lookup("queue/AnalysisRequest");
 			  resultQueue = (Queue) context.lookup("queue/AnalysisResponse");
 			  QueueConnectionFactory qcf = (QueueConnectionFactory) context
 					.lookup(factoryJNDI);
 	
 			  queueConnection = qcf.createQueueConnection();
 			  queueConnection.setExceptionListener(this);
 				
 			  queueSession = queueConnection.createQueueSession(false,
 						QueueSession.AUTO_ACKNOWLEDGE);
 				
 			  requestReceiver = queueSession.createReceiver(requestQueue);
 		
 			  requestReceiver.setMessageListener(this);
 				 
 			  resultSender = queueSession.createSender(resultQueue);
 			  
 			  queueConnection.start();
 			  
 			  connected = true;
 			  System.out.println("  successfully established queue connection.");
 			  System.out.println("Now listening for requests...");
 			  
 			}
 			catch (Exception ex) {
 			  System.out.println("  could not establish connection. Will try again in  " + Long.toString(reconnectWaitTimeMS/1000L) + " seconds..."); 
 			  try { 
 			    Thread.sleep(reconnectWaitTimeMS);
 			  }
 			  catch (Exception ex2) {
 			    System.out.println("Caugh exception while trying to sleep.." + ex2.getMessage());
 			    ex2.printStackTrace(System.out);
 			    return;
 			  }
 		    }
 		}
 	}
 	
 
 	/**
 	 * Implementation of the MessageListener interface, messages will be
 	 * received through this method.
 	 */
 	public void onMessage(Message m) {
 
 		// Unpack the message, be careful when casting to the correct
 		// message type. onMessage should not throw any application
 		// exceptions.
 		try {
 
 			// String msg = ((TextMessage)m).getText();
 			ObjectMessage msg = (ObjectMessage) m;
 			AnalysisRequest request = (AnalysisRequest) msg.getObject();
 			System.out.println("AnalysisProcessor got request: " + request);
 
 			if (request instanceof ClassComparisonRequest) {
 				processClassComparisonRequest((ClassComparisonRequest) request);
 			} else if (request instanceof HierarchicalClusteringRequest) {
 				processHierarchicalClusteringRequest((HierarchicalClusteringRequest) request);
 			} else if (request instanceof PrincipalComponentAnalysisRequest) {
 				processPrincipalComponentAnalysisRequest((PrincipalComponentAnalysisRequest) request);
 			}
 
 			// sendResult(request);
 
 		} catch (JMSException ex) {
 
 			System.err.println("AnalysisProcessor exception: " + ex);
 			ex.printStackTrace();
 
 		}
 
 	}
 	
 	
 	/**
 	 * Process a class comparison analysis request.
 	 * 
 	 * @param ccRequest
 	 */
 	public void processClassComparisonRequest(ClassComparisonRequest ccRequest) {
 		executor.execute(new ClassComparisonTaskR(ccRequest, true));
 	}
 
 	/**
 	 * Process a hierarchicalClusteringAnalysisRequest.
 	 * 
 	 * @param hcRequest
 	 */
 	public void processHierarchicalClusteringRequest(
 			HierarchicalClusteringRequest hcRequest) {
 		executor.execute(new HierarchicalClusteringTaskR(hcRequest, true));
 	}
 
 	/**
 	 * Process a PrincipalComponentAnalysisRequest.
 	 * 
 	 * @param pcaRequest
 	 */
 	public void processPrincipalComponentAnalysisRequest(
 			PrincipalComponentAnalysisRequest pcaRequest) {
 		executor.execute(new PrincipalComponentAnalysisTaskR(pcaRequest, true));
 	}
 
 	public void sendException(AnalysisServerException analysisServerException) {
 		try {
 			System.out
 					.println("AnalysisServer sending AnalysisServerException sessionId="
 							+ analysisServerException.getFailedRequest()
 									.getSessionId()
 							+ " taskId="
 							+ analysisServerException.getFailedRequest()
 									.getTaskId() + " msg=" + analysisServerException.getMessage());
 			ObjectMessage msg = queueSession
 					.createObjectMessage(analysisServerException);
 			resultSender.send(msg, DeliveryMode.NON_PERSISTENT,
 					Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
 		} catch (JMSException ex) {
 			ex.printStackTrace(System.out);
 		}
 	}
 
 	
 
 	public void sendResult(AnalysisResult result) {
 
 		try {
 //			System.out
 //					.println("AnalysisServer sending result for sessionId="
 //							+ result.getSessionId() + " taskId="
 //							+ result.getTaskId());
 			ObjectMessage msg = queueSession.createObjectMessage(result);
 			resultSender.send(msg, DeliveryMode.NON_PERSISTENT,
 					Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
 		} catch (JMSException ex) {
 			ex.printStackTrace(System.out);
 		}
 	}
 
 	/**
 	 * Close session and connection.
 	 */
 	public void close() throws JMSException {
 
 		queueSession.close();
 		queueConnection.close();
 
 	}
 
 	/**
 	 * Run an example subscribing to topic testTopic. Only works up to and
 	 * including JBoss 2.4.0
 	 */
 	public static void main(String[] args) {
 
 		try {
 			if (args.length > 0) {
 			  String serverPropsFile = args[0];
 			  AnalysisServer server = new AnalysisServer("ConnectionFactory", serverPropsFile);
 			}
 			else {
 			  AnalysisServer server = new AnalysisServer("ConnectionFactory");
 			}
 		} 
 		catch (Exception ex) {
 
 			System.err
 					.println("An exception occurred while testing AnalysisProcessor: "
 							+ ex);
 			ex.printStackTrace();
 
 		}
 
 	}
 
 	/**
 	 * If there is a problem with the connection then re-establish 
 	 * the connection.
 	 */
 	public void onException(JMSException exception) {
 	  System.out.println("onException: caught JMSexception: " + exception.getMessage());
 	  
	  try
      {
         queueConnection.setExceptionListener(null);
         close();
      }
      catch (JMSException c)
      {
        System.out.println("Ignoring exception thrown when closing broken connection msg=" + c.getMessage());
        c.printStackTrace(System.out);
      }
	  
 	  //attempt to re-establish the queue connection
 	  establishQueueConnection();
 	}
 
 }
