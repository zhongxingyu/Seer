 package com.od.jtimeseries.server.servermetrics.jmx;
 
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 
 import javax.management.MBeanServerConnection;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 06-Feb-2010
  * Time: 11:38:11
  *
  * A default service to execute JMX connection based tasks
  *
  * This service maintains a list of open connections to jmx services, so that connections are not always closed after use
  * There is an upper limit on the number of connections which will be kept open for possible reuse, and a maximum connection age
  *
  * The intention is to prevent closing and reopening connections if there are several metrics defined which 
  * make use of the same service URL - if there is already a connection in the active list, it may be reused
  *
  * n.b. The current implementation does not actually block the calling thread creating a new connection if the limit for reuse is reached,
  * and could temporarily create more connections than the limit - in practise this is bounded by the number of calling threads, which is
  * the number of threads in the scheduler service in the case of Jmx Metrics (default == 3). Ideally this class should be enhanced so that the
  * calling thread blocks when the limit is reached.
  */
 public class DefaultJmxExecutorService implements JmxExecutorService {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(DefaultJmxExecutorService.class);
 
     private Map<String, ?> connectorEnvironment;
 
     private List<JmxConnectionWrapper> activeConnections = new LinkedList<JmxConnectionWrapper>();
     private int connectionLimit = 10;
     private int maxConnectionAgeMillis = 60000;
 
     public DefaultJmxExecutorService(int connectionLimit, int maxConnectionAgeMillis) {
         this.connectionLimit = connectionLimit;
         this.maxConnectionAgeMillis = maxConnectionAgeMillis;
         startConnectionPruningThread();
     }
 
     public void setConnectorEnvironment(Map<String, ?> connectorEnvironment) {
         this.connectorEnvironment = connectorEnvironment;
     }
 
     private void startConnectionPruningThread() {
 
         ScheduledExecutorService pruningExecutor = Executors.newSingleThreadScheduledExecutor();
 
         //set the name of the executor thread
         pruningExecutor.execute(new Runnable() {
             public void run() {
                 Thread.currentThread().setName("DefaultJmxExecutorService Connection Pruning");
             }
         });
 
         pruningExecutor.scheduleAtFixedRate(new Runnable() {
             public void run() {
                 pruneConnections();
             }},
             30,
             30,
             TimeUnit.SECONDS
         );
     }
 
     public void executeTask(JmxExecutorTask task) throws JmxExecutionException {
         JMXServiceURL serviceUrl = task.getServiceURL();
         JmxConnectionWrapper connection = null;
         try {
             connection = getConnection(serviceUrl, connection);
             task.executeTask(connection.getConnection());
         } catch (Throwable t) {
             throw new JmxExecutionException(serviceUrl, t);
         } finally {
             returnConnection(connection);
         }
     }
 
     private void returnConnection(JmxConnectionWrapper connection) {
         //take back the lock when returning connection to connection cache
         synchronized (this) {
             if ( connection != null) {
                 //add to the start of the list of active connections (the most recently used)
                 activeConnections.add(0, connection);
                 //prune any excess connections
                 pruneByConnectionCount();
             }
         }
     }
 
     private JmxConnectionWrapper getConnection(JMXServiceURL serviceUrl, JmxConnectionWrapper connection) throws IOException {
         logMethods.logDebug("Getting JMX connection to service " + serviceUrl);
         synchronized(this) {
             connection = removeFromActiveConnections(serviceUrl);
         }
 
         //surrender the lock to allow other threads to process tasks
         if ( connection == null) {
             logMethods.logDebug("Creating JMX connection to service " + serviceUrl);
             connection = new JmxConnectionWrapper(serviceUrl);
         } else {
             logMethods.logDebug("Reusing JMX connection to service " + serviceUrl + " from active connections list");
         }
         return connection;
     }
 
     private JmxConnectionWrapper removeFromActiveConnections(JMXServiceURL serviceUrl) {
         JmxConnectionWrapper result = null;
         Iterator<JmxConnectionWrapper> i = activeConnections.iterator();
         JmxConnectionWrapper connection = null;
         while (i.hasNext()) {
             connection = i.next();
             if (connection.isForService(serviceUrl)) {
                 i.remove();
                 result = connection;
                 break;
             }
         }
         return result;
     }
 
     //Remove any connections past the age limit
     //If after this there are still more connections than the limit, remove the connections from the end of the list
     //(these should be the least recently used)
     private synchronized void pruneConnections() {
         pruneByAge();
         pruneByConnectionCount();
     }
 
     private void pruneByAge() {
         JmxConnectionWrapper currentConnection;
         Iterator<JmxConnectionWrapper> i = activeConnections.iterator();
         while(i.hasNext()) {
             currentConnection = i.next();
             if ( currentConnection.getAge() > maxConnectionAgeMillis) {
                 logMethods.logDebug("Closing JMX connection " + currentConnection + " which is " + currentConnection.getAge() + " millis old");
                 i.remove();
                 currentConnection.close();
             }
         }
     }
 
     private void pruneByConnectionCount() {
         while ( activeConnections.size() > connectionLimit ) {
             JmxConnectionWrapper w = activeConnections.remove(activeConnections.size() - 1);
             logMethods.logDebug("Closing JMX connection " + w + " due to active connection limit");
             w.close();
         }
     }
 
     private class JmxConnectionWrapper {
 
         private JMXConnector connector;
         private JMXServiceURL serviceURL;
         private MBeanServerConnection connection;
         private long lastUsageTime;
 
         private JmxConnectionWrapper(JMXServiceURL serviceURL) throws IOException {
             this.serviceURL = serviceURL;
             openConnection(serviceURL);
         }
 
         private void openConnection(JMXServiceURL serviceURL) throws IOException {
             this.connector = JMXConnectorFactory.connect(serviceURL, connectorEnvironment);
             this.connection = connector.getMBeanServerConnection();
         }
 
         public boolean isForService(JMXServiceURL url) {
             return serviceURL.equals(url);
         }
 
         public MBeanServerConnection getConnection() {
             lastUsageTime = System.currentTimeMillis();
             return connection;
         }
 
         public void close() {
             try {
                 connector.close();
             } catch (IOException e) {
                 logMethods.logError("Failed to close JMX connection to " + serviceURL, e);
             }
         }
 
         public long getAge() {
             return System.currentTimeMillis() - lastUsageTime;
         }
 
         public String toString() {
             return "JmxConnectionWrapper { " + serviceURL + " }";
         }
     }
 
     public String toString() {
         return "DefaultJmxExecutorService, max connections: " + connectionLimit + " max age ms:" + maxConnectionAgeMillis;
     }
 
 }
