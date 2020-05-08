 package org.jdryad.node;
 
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 
 import org.jdryad.com.Communicator;
 import org.jdryad.com.HostID;
 import org.jdryad.com.Message;
 import org.jdryad.com.Reactor;
 import org.jdryad.com.messages.JDAGMessageType;
 import org.jdryad.com.messages.ProtobufMessageMarshallerFactory;
 import org.jdryad.com.messages.SimpleMessage;
 import org.jdryad.com.messages.UpAndLiveProtos;
 import org.jdryad.com.messages.ExecuteVertexProtos.ExecuteVertexMessage;
 import org.jdryad.com.messages.ExecuteVertexStatusProtos.ExecuteVertexStatusMessage;
 import org.jdryad.com.rabbitmq.RabbitMQCommunicator;
 import org.jdryad.com.rabbitmq.RabbitMQConfiguration;
 import org.jdryad.common.Application;
 import org.jdryad.common.ApplicationExecutor;
 import org.jdryad.config.ConfigurationProvider;
 import org.jdryad.dag.ExecutionContext;
 import org.jdryad.dag.ExecutionGraphID;
 import org.jdryad.dag.ExecutionResult;
 import org.jdryad.dag.GraphVertexID;
 import org.jdryad.dag.VertexID;
 
 /**
  * The executor which sits on a node and executes the functions sent by the
  * master.
  *
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public class NodeExecutor implements Application
 {
     private static final String NODE_CONFIG_FILE = "node/config.properties";
 
     private static final String COMM_CONFIG_FILE = "node/commConfig.properties";
 
     private final Communicator myCommunicator;
 
     private final NodeConfig myConfig;
 
     private final PaceMaker myPaceMaker;
 
     private final ExecutorService myExecutorService;
 
     private final ExecutionContext myExecutionContext;
 
     private final ScheduledExecutorService myScheduler;
 
     private final Map<GraphVertexID, Future<ExecutionResult>>
         myFunToExecutionStatusMap;
 
     /**
      * Responsible for sending heartbeats to the master.
      */
     private class PaceMaker implements Runnable
     {
 
          private long myStartTime;
 
          /**
          * CTOR
          */
         public PaceMaker()
         {
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void run()
         {
             UpAndLiveProtos.UpAndAliveMessage payload =
                 UpAndLiveProtos.UpAndAliveMessage
                                .newBuilder()
                                .setAliveMillis(System.currentTimeMillis() -
                                                   myStartTime)
                                .build();
             myCommunicator.sendMessage(new HostID(myConfig.getMasterHostID()),
                                        new SimpleMessage(
                                            JDAGMessageType.UP_AND_ALIVE_MESSAGE,
                                            payload));
         }
 
         /**
          * Starts sending heartbeats to the master.
          */
         public void start()
         {
         	myStartTime = System.currentTimeMillis();
         	myScheduler.scheduleAtFixedRate(
         	    PaceMaker.this, 10, 10, TimeUnit.SECONDS);
         }
     }
 
     /**
      * Responsible for checking the results of execution.
      */
     private class ResultChecker implements Runnable
     {
         /**
          * {@inheritDoc}
          */
         @Override
         public void run()
         {
             for (Map.Entry<GraphVertexID, Future<ExecutionResult>> entry :
                     myFunToExecutionStatusMap.entrySet())
             {
                 Future<ExecutionResult> future = entry.getValue();
                 ExecutionResult result = null;
                 if (future.isDone()) {
                     try {
                         result = future.get();
                     }
                     catch (InterruptedException e) {
                          result = ExecutionResult.ERROR;
                     }
                     catch (ExecutionException e) {
                         result = ExecutionResult.ERROR;
                     }
                     ExecuteVertexStatusMessage status =
                         ExecuteVertexStatusMessage.newBuilder()
                                                   .setGraphId(entry.getKey()
                                                                    .getGraphID()
                                                                    .getID())
                                                   .setVertexId(entry.getKey()
                                                                     .getVertexID()
                                                                     .getName())
                                                   .setExecutionStatus(
                                                       result.getIntegerRepresentation())
                                                   .build();
                     myCommunicator.sendMessage(
                         new HostID(myConfig.getMasterHostID()),
                         new SimpleMessage(JDAGMessageType.EXECUTE_VERTEX_STATUS_MESSAGE,
                                           status));
                 }
             }
         }
     }
 
     /** Reactor for <node>ExecuteVertex</code> messages */
     private class ExecuteVertexReactor implements Reactor
     {
     	/**
     	 * {@inheritDoc}
     	 */
 		@Override
 		public void process(Message m)
 		{
 			ExecuteVertexMessage message =
 				(ExecuteVertexMessage)
 				    ((SimpleMessage) m).getPayload();
 			ExecutableVertex v =
 				new ExecutableVertex(message, myExecutionContext);
 			Future<ExecutionResult> result =
 			    myExecutorService.submit(v);
 			myFunToExecutionStatusMap.put(
 		        new GraphVertexID(new ExecutionGraphID(message.getGraphId()),
 		                          new VertexID(message.getVertexId())),
 		        result);
 		}
     }
 
     /**
      * CTOR
      */
     public NodeExecutor(Communicator communicator, NodeConfig config)
     {
         myCommunicator = communicator;
         myConfig = config;
         myPaceMaker = new PaceMaker();
         ThreadFactoryBuilder tfBuilder = new ThreadFactoryBuilder();
         ThreadFactory namedFactory =
             tfBuilder.setNameFormat(NodeExecutor.class.getSimpleName())
                      .build();
         myExecutorService = Executors.newFixedThreadPool(2, namedFactory);
         ThreadFactory namedSchedulerFactory =
             tfBuilder.setNameFormat(NodeExecutor.class.getSimpleName() + " sch")
                      .build();
         myScheduler = Executors.newScheduledThreadPool(2, namedSchedulerFactory);
         myExecutionContext = new SimpleExecutionContext();
         myFunToExecutionStatusMap =
         	new HashMap<GraphVertexID, Future<ExecutionResult>>();
     }
 
     /**
      * Starts the executor responsible for executing the functions sent in by
      * the master.
      */
     public void start()
     {
     	myPaceMaker.start();
     	myScheduler.scheduleWithFixedDelay(
            new ResultChecker(), 1, 1, TimeUnit.SECONDS);
     	myCommunicator.attachReactor(JDAGMessageType.EXECUTE_VERTEX_MESSAGE,
     			                     new ExecuteVertexReactor());
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void stop()
     {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * The main method.
      */
     public static void main(String[] args)
     {
         NodeConfig nodeConfig =
             ConfigurationProvider.makeConfiguration(
                 NodeConfig.class, NODE_CONFIG_FILE);
         RabbitMQConfiguration commConfig =
             ConfigurationProvider.makeConfiguration(
                 RabbitMQConfiguration.class, COMM_CONFIG_FILE);
         RabbitMQCommunicator comm =
             new RabbitMQCommunicator(commConfig,
                                      new ProtobufMessageMarshallerFactory());
         NodeExecutor exec = new NodeExecutor(comm, nodeConfig);
         ApplicationExecutor applicationExecutor =
             new ApplicationExecutor(exec);
         applicationExecutor.run();
     }
 }
