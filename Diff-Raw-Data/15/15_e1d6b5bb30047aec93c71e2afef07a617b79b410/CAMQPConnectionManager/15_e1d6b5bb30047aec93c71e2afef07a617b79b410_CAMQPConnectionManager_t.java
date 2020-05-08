 package net.dovemq.transport.connection;
 
import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 
 import net.jcip.annotations.GuardedBy;
 
 import org.apache.log4j.Logger;
 
 /**
  * Manager of outstanding AMQP connections
  * @author tejdas
  *
  */
 public final class CAMQPConnectionManager
 {
     private static final Logger log = Logger.getLogger(CAMQPConnectionManager.class);
 
     private static CAMQPConnectionManager connectionManager = null;
 
     private static final ScheduledExecutorService _scheduledExecutor = Executors.newScheduledThreadPool(8);
 
     static ScheduledExecutorService getScheduledExecutor()
     {
         return _scheduledExecutor;
     }
 
     @GuardedBy("this")
     private CAMQPConnectionObserver connectionObserver = null;
 
     private String containerId = null;
 
     @GuardedBy("this")
     private boolean shutdownInProgress = false;
 
     private final Map<CAMQPConnectionKey, CAMQPConnection> openConnections = new ConcurrentHashMap<CAMQPConnectionKey, CAMQPConnection>();
 
     public static synchronized void initialize(String containerId)
     {
         if (connectionManager == null)
         {
             connectionManager = new CAMQPConnectionManager(containerId);
         }
     }
     
     private static synchronized CAMQPConnectionManager getConnectionManager()
     {
         return connectionManager ;
     }
 
     public static CAMQPConnection getCAMQPConnection(String targetContainerId)
     {
         return getConnectionManager().getCAMQPConnectionInternal(targetContainerId);
     }
 
     private CAMQPConnection getCAMQPConnectionInternal(String targetContainerId)
     {
         CAMQPConnectionKey key = new CAMQPConnectionKey(targetContainerId, 0);
         synchronized (this)
         {
             return openConnections.get(key);
         }
     }
 
     public static CAMQPConnection getAnyCAMQPConnection(String targetContainerId)
     {
         return getConnectionManager().getAnyCAMQPConnectionInternal(targetContainerId);
     }
 
     private CAMQPConnection getAnyCAMQPConnectionInternal(String targetContainerId)
     {
         synchronized (this)
         {
             Collection<CAMQPConnection> amqpConnections = openConnections.values();
             for (CAMQPConnection amqpConnection : amqpConnections)
             {
                 if (amqpConnection.getRemoteContainerId().equalsIgnoreCase(targetContainerId))
                     return amqpConnection;
             }
         }
         return null;
     }
 
     public static void registerConnectionObserver(CAMQPConnectionObserver connectionAcceptor)
     {
         synchronized (getConnectionManager())
         {
             getConnectionManager().connectionObserver = connectionAcceptor;
         }
     }
     
     private synchronized CAMQPConnectionObserver getConnectionObserver()
     {
         return connectionObserver;
     }
 
     private CAMQPConnectionManager(String containerId)
     {
         super();
         String hostName = "localhost";
         try
         {
            InetAddress localMachine = InetAddress.getLocalHost();
            //hostName = localMachine.getCanonicalHostName();
            hostName = localMachine.getHostAddress();
             log.debug("hostName: " + hostName);
         }
         catch (java.net.UnknownHostException uhe)
         {
             log.error("Caught UnknownHostException while resolving canonicalHostName: " + uhe.getMessage());
             // handle exception
         }
         this.containerId = String.format("%s@%s", containerId, hostName);
        System.out.println("DoveMQ endpoint ID: " + this.containerId);
     }
 
     public static String getContainerId()
     {
         return getConnectionManager().containerId;
     }
 
     static Collection<String> listConnections()
     {
         return getConnectionManager().listConnectionsInternal();
     }
 
     private synchronized Collection<String> listConnectionsInternal()
     {
         Collection<String> connectionList = new ArrayList<String>();
         Set<CAMQPConnectionKey> keys = openConnections.keySet();
         for (CAMQPConnectionKey k : keys)
         {
             connectionList.add(k.getRemoteContainerId());
         }
         return connectionList;
     }
 
     static void connectionClosed(CAMQPConnectionKey key)
     {
         getConnectionManager().connectionClosedInternal(key);
     }
 
     static void connectionAborted(CAMQPConnectionKey key)
     {
         CAMQPConnection abortedConnection = getConnectionManager().connectionClosedInternal(key);
         if (abortedConnection != null)
         {
             abortedConnection.aborted();
         }
     }
 
     private synchronized CAMQPConnection connectionClosedInternal(CAMQPConnectionKey key)
     {
         CAMQPConnection connection = openConnections.remove(key);
         if ((openConnections.size() == 0) && shutdownInProgress)
         {
             notifyAll();
         }
         return connection;
     }
 
     static void connectionCreated(CAMQPConnectionKey key, CAMQPConnection connection)
     {
         getConnectionManager().connectionCreatedInternal(key, connection);
     }
     
     private synchronized void connectionCreatedInternal(CAMQPConnectionKey key, CAMQPConnection connection)
     {
         if (shutdownInProgress)
         {
             log.error("Shutdown is already in progress: cannot add CAMQPConnection to ConnectionManager's openConnectionsList");
             return; // TODO handle error
         }
         openConnections.put(key, connection);
     }
 
     static void connectionAccepted(CAMQPConnectionStateActor stateActor, CAMQPConnectionKey key)
     {
         getConnectionManager().connectionAcceptedInternal(stateActor, key);
     }
 
     private void connectionAcceptedInternal(CAMQPConnectionStateActor stateActor, CAMQPConnectionKey key)
     {
         CAMQPConnection amqpConnection = new CAMQPConnection(stateActor);
         connectionCreatedInternal(key, amqpConnection);
         CAMQPConnectionObserver observer = getConnectionObserver();;
         if (observer != null)
         {
             observer.connectionAccepted(amqpConnection);
         }
     }
 
     public static void connectionCloseInitiatedByRemotePeer(CAMQPConnectionKey key)
     {      
         getConnectionManager().connectionCloseInitiatedByRemotePeerInternal(key);
     }
     
     private void connectionCloseInitiatedByRemotePeerInternal(CAMQPConnectionKey key)
     {
         CAMQPConnectionObserver observer = getConnectionObserver();
         if (observer != null)
         {
             CAMQPConnection connection = null;
             synchronized (this)
             {
                 connection = openConnections.get(key);
             }
             if (connection != null)
             {
                 observer.connectionCloseInitiatedByRemotePeer(connection);
             }
             else
             {
                 log.warn("Connection not found in the openConnections list: " + key.getRemoteContainerId() + "  " + key.getEphemeralPort());
             }
         }
     }
     
     public static void shutdown()
     {
         getConnectionManager().shutdownInternal();
         _scheduledExecutor.shutdown();
     }
 
     private void shutdownInternal()
     {
         Collection<CAMQPConnection> openConnectionList = new ArrayList<CAMQPConnection>();
         synchronized (this)
         {
             if (shutdownInProgress)
             {
                 return;
             }
             shutdownInProgress = true;
             Collection<CAMQPConnection> connList = openConnections.values();
             openConnectionList.addAll(connList);
         }
         for (CAMQPConnection connection : openConnectionList)
         {
             connection.closeAsync();
         }
         synchronized (this)
         {
             try
             {
                 while (openConnections.size() > 0)
                 {
                     wait();
                 }
             }
             catch (InterruptedException e)
             {
                 Thread.currentThread().interrupt();
             }
         }
     }
 }
