 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.datastax.drivers.jdbc.pool.cassandra.connection;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraClientMonitor.Counter;
 import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HectorException;
 import com.datastax.drivers.jdbc.pool.cassandra.jdbc.CassandraConnectionHandle;
 import com.datastax.drivers.jdbc.pool.cassandra.service.ExceptionsTranslator;
 import com.datastax.drivers.jdbc.pool.cassandra.service.ExceptionsTranslatorImpl;
 import com.datastax.drivers.jdbc.pool.cassandra.service.FailoverPolicy;
 import com.datastax.drivers.jdbc.pool.cassandra.service.JmxMonitor;
 import com.datastax.drivers.jdbc.pool.cassandra.service.Operation;
 import com.datastax.drivers.jdbc.pool.cassandra.service.OperationType;
 
 public class HConnectionManager {
 
   private static final Logger log = LoggerFactory.getLogger(HConnectionManager.class);
 
   private final ConcurrentMap<CassandraHost,HClientPool> hostPools;
   private final ConcurrentMap<CassandraHost,HClientPool> suspendedHostPools;  
   private final Collection<HClientPool> hostPoolValues;
   private final String clusterName;
   private final LoadBalancingPolicy loadBalancingPolicy;
   private final CassandraHostConfigurator cassandraHostConfigurator;
   private final CassandraClientMonitor monitor;
   final ExceptionsTranslator exceptionsTranslator;
 
   private CassandraHostRetryService cassandraHostRetryService;
   private NodeAutoDiscoverService nodeAutoDiscoverService;
   private HostTimeoutTracker hostTimeoutTracker;
 
   private HOpTimer timer;
 
   private FailoverPolicy failoverPolicy;
 
   public HConnectionManager(String clusterName, CassandraHostConfigurator cassandraHostConfigurator) {
     loadBalancingPolicy = cassandraHostConfigurator.getLoadBalancingPolicy();
     hostPools = new ConcurrentHashMap<CassandraHost, HClientPool>();
     suspendedHostPools = new ConcurrentHashMap<CassandraHost, HClientPool>();
     this.clusterName = clusterName;
 
     if ( cassandraHostConfigurator.getRetryDownedHosts() ) {
       cassandraHostRetryService = new CassandraHostRetryService(this, cassandraHostConfigurator);
     }
 
     for ( CassandraHost host : cassandraHostConfigurator.buildCassandraHosts()) {
       try {
         HClientPool hcp = loadBalancingPolicy.createConnection(host);
         hostPools.put(host,hcp);
       } catch (SQLException e) {
         log.error("Could not start connection pool for host {}", host);
         if ( cassandraHostRetryService != null ) {
           cassandraHostRetryService.add(host);
         }
       }
     }
 
     if ( cassandraHostConfigurator.getUseHostTimeoutTracker() ) {
       hostTimeoutTracker = new HostTimeoutTracker(this, cassandraHostConfigurator);
     }
 
     monitor = JmxMonitor.getInstance().getCassandraMonitor(this);
     exceptionsTranslator = new ExceptionsTranslatorImpl();
     this.cassandraHostConfigurator = cassandraHostConfigurator;
     hostPoolValues = hostPools.values();
     
     /*
     if ( cassandraHostConfigurator.getAutoDiscoverHosts() ) {
       nodeAutoDiscoverService = new NodeAutoDiscoverService(this, cassandraHostConfigurator);
       if ( cassandraHostConfigurator.getRunAutoDiscoveryAtStartup() ) {
         nodeAutoDiscoverService.doAddNodes();
       }
     }
     */
 
     timer = cassandraHostConfigurator.getOpTimer();
     failoverPolicy = cassandraHostConfigurator.getFailoverPolicy();
   }
 
   /**
    * Returns true if the host was successfully added. In any sort of failure exceptions are 
    * caught and logged, returning false.
    * @param cassandraHost
    * @return
    */
   public boolean addCassandraHost(CassandraHost cassandraHost) {
     if ( !getHosts().contains(cassandraHost) ) {
       HClientPool pool = null;
       try {
         cassandraHostConfigurator.applyConfig(cassandraHost);
         pool = cassandraHostConfigurator.getLoadBalancingPolicy().createConnection(cassandraHost);
         hostPools.putIfAbsent(cassandraHost, pool);
         log.info("Added host {} to pool", cassandraHost.getName());
         return true;
       } catch (SQLException ex) {
         log.error("General exception host to HConnectionManager: " + cassandraHost, ex);
       }
     } else {
       log.info("Host already existed for pool {}", cassandraHost.getName());
     }
     return false;
   }
 
   /**
    * Remove the {@link CassandraHost} from the pool, bypassing retry service. This
    * would be called on a host that is known to be going away. Gracefully shuts down
    * the underlying connections via {@link HClientPool#shutdown()}. This method
    * will also:
    * <ul>
    * <li>shutdown pools in the suspended state, removing them from the underlying
    * suspended map.</li>
    * <li>remove hosts from {@link CassandraHostRetryService} if contained therein</li></ul>
    * 
    * @param cassandraHost
    */
   public boolean removeCassandraHost(CassandraHost cassandraHost) {
     boolean removed = getHosts().contains(cassandraHost);
     if ( removed ) {
     	HClientPool pool = hostPools.remove(cassandraHost);
       if ( pool == null ) {
         log.info("removeCassandraHost looking for host {} in suspendedHostPools", cassandraHost);
         pool = suspendedHostPools.remove(cassandraHost);
       }
       if ( pool != null ) {
         pool.shutdown();
       } else {
         removed = false;
         log.info("removeCassandraHost attempt miss for CassandraHost {} May have been beaten by another thread?", cassandraHost);
       }
     } else if ( cassandraHostRetryService != null && cassandraHostRetryService.contains(cassandraHost)) {
         log.info("Host {} not in active pools, but found in retry service.", cassandraHost);
         removed = cassandraHostRetryService.remove(cassandraHost);
     } else {
         log.info("Remove requested on a host that was not found in active or disabled pools: {}", cassandraHost);    
     }
     log.info("Remove status for CassandraHost pool {} was {}", cassandraHost, removed);
     return removed;
   }
   
   /**
    * Remove the {@link HClientPool} referenced by the {@link CassandraHost} from 
    * the active host pools. This does not shut down the pool, only removes it as a candidate from
    * future operations.
    * @param cassandraHost
    * @return true if the operation was successful.
    */
   public boolean suspendCassandraHost(CassandraHost cassandraHost) {
     HClientPool pool = hostPools.remove(cassandraHost);
     boolean removed = pool != null;
     if ( removed ) {      
       suspendedHostPools.put(cassandraHost, pool);
     }
     log.info("Suspend operation status was {} for CassandraHost {}", removed, cassandraHost);
     return removed;
   }
 
   /** 
    * The opposite of suspendCassandraHost, places the pool back into selection
    * @param cassandraHost
    * @return true if this operation was successful. A no-op returning false 
    * if there was no such host in the underlying suspendedHostPool map.
    */
   public boolean unsuspendCassandraHost(CassandraHost cassandraHost) {
     HClientPool pool = suspendedHostPools.remove(cassandraHost);
     boolean readded = pool != null;
     if ( readded ) {      
       boolean alreadyThere = hostPools.putIfAbsent(cassandraHost, pool) != null;
       if ( alreadyThere ) {
         log.error("Unsuspend called on a pool that was already active for CassandraHost {}", cassandraHost);
         pool.shutdown();
       }
     }
     log.info("UN-Suspend operation status was {} for CassandraHost {}", readded, cassandraHost);
     return readded;
   }
   
   /**
    * Returns a Set of {@link CassandraHost} which are in the suspended status
    * @return
    */
   public Set<CassandraHost> getSuspendedCassandraHosts() {
     return suspendedHostPools.keySet();
   }
   
   public Set<CassandraHost> getHosts() {
     return Collections.unmodifiableSet(hostPools.keySet());
   }
 
   public List<String> getStatusPerPool() {
     List<String> stats = new ArrayList<String>();
     for (HClientPool clientPool : hostPools.values()) {
         stats.add(clientPool.getStatusAsString());
     }
     return stats;
   }
 
   /**
    * Borrow a client using the failover mechanism.
    */
   public CassandraConnectionHandle borrowClient() throws SQLException {
 
     Operation<CassandraConnectionHandle> op = new Operation<CassandraConnectionHandle>(OperationType.BORROW_CLIENT) {
 
       @Override
       public CassandraConnectionHandle execute(CassandraConnectionHandle connection) throws SQLException {
         return connection;
       }
     };
 
     this.operateWithFailover(op);
     op.getResult().setManager(this);
     return op.getResult();
   }
 
   public void operateWithFailover(Operation<?> op) throws SQLException {
     final Object timerToken = timer.start(); 
     int retries = Math.min(failoverPolicy.numRetries, hostPools.size());
     HClientPool pool = null;
     boolean success = false;
     boolean retryable = false;
     boolean firstTime = true;
     CassandraConnectionHandle currentConnection = op.getConnection();
     Set<CassandraHost> excludeHosts = new HashSet<CassandraHost>();
 
     while ( !success ) {
 
       try {
 
         // Let's not borrow a connection the first time for regular operation since JDBC approach starts by 
         // the client acquiring a connection. Except for when we are only acquiring a connection through the 
         // failover mechanism OperationType.BORROW_CLIENT). 
         if (op.operationType == OperationType.BORROW_CLIENT || !firstTime) {
           // Try a new host/connection
           pool = getClientFromLBPolicy(excludeHosts);
           currentConnection  = (CassandraConnectionHandle) pool.borrowClient();
           currentConnection.setManager(this);
 
           // Set the new connection
           op.setConnection(currentConnection);
         }
 
         firstTime = false;
 
         op.executeAndSetResult(currentConnection);
         success = true;
         timer.stop(timerToken, op.stopWatchTagName, true);
         break;
 
       } catch (Exception ex) {
 
         if ( exceptionsTranslator.isUnrecoverable(ex)) {
           // break out on HUnavailableException as well since we can no longer satisfy the CL
           throw (SQLException) ex;
 
         } else if (exceptionsTranslator.hasTimedout(ex)) {
 
           // DO NOT decrement retries, we will be keep retrying on timeouts until it comes back
           // if HLT.checkTimeout(cassandraHost): suspendHost(cassandraHost);
           doTimeoutCheck(pool.getCassandraHost());
 
           retryable = true;
 
           monitor.incCounter(Counter.RECOVERABLE_TIMED_OUT_EXCEPTIONS);
           currentConnection.close();
           // TODO timecheck on how long we've been waiting on timeouts here
           // suggestion per user moores on hector-users
 
         } else if (exceptionsTranslator.isATransportError(ex)) {
 
           // client can be null in this situation
           if ( currentConnection != null ) {
             currentConnection.close();
           }
 
           markHostAsDown(pool.getCassandraHost());
           excludeHosts.add(pool.getCassandraHost());
           retryable = true;
 
           monitor.incCounter(Counter.RECOVERABLE_TRANSPORT_EXCEPTIONS);
 
         } else if (exceptionsTranslator.isPoolExhausted(ex)) {
           retryable = true;
           if ( hostPools.size() == 1 ) {
             throw new SQLException(ex);
           }
           monitor.incCounter(Counter.POOL_EXHAUSTED);
           excludeHosts.add(pool.getCassandraHost());
 
         } else {
 
           // something strange happened. Added here as suggested by sbridges.
           // I think this gives a sane way to future-proof against any API additions
           // that we don't add in time. 
           retryable = false;
         }
 
         if ( retries <= 0 || retryable == false) {
           if (ex instanceof SQLException)
             throw (SQLException) ex;
           else
             throw new SQLException(ex);
         }
 
         log.warn("Could not fullfill request on this host {}", pool.getCassandraHost());
         log.warn("Exception: ", ex);
         monitor.incCounter(Counter.SKIP_HOST_SUCCESS);
        sleepBetweenHostSkips(failoverPolicy);
 
       } finally {
         --retries;
         if ( !success ) {
           monitor.incCounter(op.failCounter);
           timer.stop(timerToken, op.stopWatchTagName, false);
         }
         releaseClient(currentConnection);
       }
     }
   }
 
 
   public HOpTimer getTimer() {
     return timer;
   }
 
 
   public void setTimer(HOpTimer timer) {
     this.timer = timer;
   }
 
 
   /**
    * Use the HostTimeoutCheck and initiate a suspend if and only if
    * we are configured for such AND there is more than one operating host pool
    * @param cassandraHost
    */
   private void doTimeoutCheck(CassandraHost cassandraHost) {
     if ( hostTimeoutTracker != null && hostPools.size() > 1) {
       if (hostTimeoutTracker.checkTimeout(cassandraHost) ) {
         suspendCassandraHost(cassandraHost);
       }
     }
   }
 
 
   /**
   * Sleeps for the specified time as determined by sleepBetweenHostsMilli.
   * In many cases failing over to other hosts is done b/c the cluster is too busy, so the sleep b/w
   * hosts may help reduce load on the cluster.
   */
     private void sleepBetweenHostSkips(FailoverPolicy failoverPolicy) {
       if (failoverPolicy.sleepBetweenHostsMilli > 0) {
         if ( log.isDebugEnabled() ) {
           log.debug("Will sleep for {} millisec", failoverPolicy.sleepBetweenHostsMilli);
         }
         try {
           Thread.sleep(failoverPolicy.sleepBetweenHostsMilli);
         } catch (InterruptedException e) {
           log.warn("Sleep between hosts interrupted", e);
         }
       }
     }
 
   private HClientPool getClientFromLBPolicy(Set<CassandraHost> excludeHosts) {
     if ( hostPools.isEmpty() ) {
       throw new HectorException("All host pools marked down. Retry burden pushed out to client.");
     }        
     return loadBalancingPolicy.getPool(hostPoolValues, excludeHosts);    
   }
 
   public void releaseClient(CassandraConnectionHandle connectionHandle) throws SQLException {
     if (connectionHandle == null ) return;
     if (connectionHandle.isClosed) return;
 
     HClientPool pool = hostPools.get(connectionHandle.getCassandraHost());
     if ( pool == null ) {
       pool = suspendedHostPools.get(connectionHandle.getCassandraHost());
     }
     if ( pool != null ) {
       pool.releaseClient(connectionHandle);
     } else {
       log.info("Client {} released to inactive or dead pool. Closing.", connectionHandle.getCassandraHost());
       closeQuietly(connectionHandle);
     }
     connectionHandle.isClosed = true;
   }
 
   private void closeQuietly(CassandraConnectionHandle connectionHandle) {
     try {
       connectionHandle.getInternalConnection().close();
     } catch (SQLException e) {
       log.info("Unexpected error while closing the connection: " + connectionHandle.getCassandraHost());
     }
   }
 
 
   void markHostAsDown(CassandraHost cassandraHost) {
     log.error("MARK HOST AS DOWN TRIGGERED for host {}", cassandraHost.getName());
     HClientPool pool = hostPools.remove(cassandraHost);
     if ( pool != null ) {
       log.error("Pool state on shutdown: {}", pool.getStatusAsString());
       pool.shutdown();
       if ( cassandraHostRetryService != null ) 
         cassandraHostRetryService.add(cassandraHost);
     }
   }
 
   public Set<CassandraHost> getDownedHosts() {
     return cassandraHostRetryService.getDownedHosts();
   }
 
   public Collection<HClientPool> getActivePools() {
     return Collections.unmodifiableCollection(hostPools.values());
   }
 
   public String getClusterName() {
     return clusterName;
   }
 
   public void shutdown() {
     log.info("Shutdown called on HConnectionManager");
     if ( cassandraHostRetryService != null )
       cassandraHostRetryService.shutdown();
     if ( nodeAutoDiscoverService != null )
       nodeAutoDiscoverService.shutdown();
     if ( hostTimeoutTracker != null ) 
       hostTimeoutTracker.shutdown();
 
     for (HClientPool pool : hostPools.values()) {
       try {
         pool.shutdown();
       } catch (IllegalArgumentException iae) {
         log.error("Out of order in HConnectionManager shutdown()?: {}", iae.getMessage());
       }
     }
   }
 
 
 }
