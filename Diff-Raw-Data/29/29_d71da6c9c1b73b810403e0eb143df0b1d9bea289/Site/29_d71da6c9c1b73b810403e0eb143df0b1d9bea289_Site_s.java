 /* This file is part of VoltDB.
  * Copyright (C) 2008-2012 VoltDB Inc.
  *
  * VoltDB is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * VoltDB is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.voltdb.iv2;
 
 import java.util.concurrent.Future;
 
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.voltcore.logging.Level;
 import org.voltcore.logging.VoltLogger;
 import org.voltcore.utils.CoreUtils;
 import org.voltdb.BackendTarget;
 import org.voltdb.CatalogContext;
 import org.voltdb.CatalogSpecificPlanner;
 import org.voltdb.DependencyPair;
 import org.voltdb.HsqlBackend;
 import org.voltdb.LoadedProcedureSet;
 import org.voltdb.ParameterSet;
 import org.voltdb.ProcedureRunner;
 import org.voltdb.SiteProcedureConnection;
 import org.voltdb.SiteSnapshotConnection;
 import org.voltdb.SnapshotSiteProcessor;
 import org.voltdb.SnapshotTableTask;
 import org.voltdb.SysProcSelector;
 import org.voltdb.SystemProcedureExecutionContext;
 import org.voltdb.VoltDB;
 import org.voltdb.VoltProcedure.VoltAbortException;
 import org.voltdb.VoltTable;
 import org.voltdb.catalog.Cluster;
 import org.voltdb.catalog.Database;
 import org.voltdb.dtxn.SiteTracker;
 import org.voltdb.dtxn.TransactionState;
 import org.voltdb.exceptions.EEException;
 import org.voltdb.jni.ExecutionEngine;
 import org.voltdb.jni.ExecutionEngineIPC;
 import org.voltdb.jni.ExecutionEngineJNI;
 import org.voltdb.jni.MockExecutionEngine;
 import org.voltdb.utils.LogKeys;
 
 public class Site implements Runnable, SiteProcedureConnection, SiteSnapshotConnection
 {
     private static final VoltLogger hostLog = new VoltLogger("HOST");
 
     // Set to false trigger shutdown.
     volatile boolean m_shouldContinue = true;
 
     // HSId of this site's initiator.
     final long m_siteId;
 
     // Partition count is important for some reason.
     final int m_numberOfPartitions;
 
     // What type of EE is controlled
     final BackendTarget m_backend;
 
     // Is the site in a rejoining mode.
     boolean m_isRejoining;
 
     // Enumerate execution sites by host.
     private static final AtomicInteger siteIndexCounter = new AtomicInteger(0);
     private final int m_siteIndex = siteIndexCounter.getAndIncrement();
 
     // Manages pending tasks.
     final SiteTaskerQueue m_scheduler;
 
     // Almighty execution engine and its HSQL sidekick
     ExecutionEngine m_ee;
     HsqlBackend m_hsql;
 
     // Each execution site manages snapshot using a SnapshotSiteProcessor
     private SnapshotSiteProcessor m_snapshotter;
 
     // Current catalog
     CatalogContext m_context;
 
     // Currently available procedure
     LoadedProcedureSet m_loadedProcedures;
 
     // Current topology
     int m_partitionId;
 
     // Need temporary access to some startup parameters in order to
     // initialize EEs in the right thread.
     private static class StartupConfig
     {
         final String m_serializedCatalog;
         final long m_txnId;
         StartupConfig(final String serCatalog, final long txnId)
         {
             m_serializedCatalog = serCatalog;
             m_txnId = txnId;
         }
     }
     private StartupConfig m_startupConfig = null;
 
 
     // Undo token state for the corresponding EE.
     public final static long kInvalidUndoToken = -1L;
     long latestUndoToken = 0L;
 
     @Override
     public long getNextUndoToken()
     {
         return ++latestUndoToken;
     }
 
     @Override
     public long getLatestUndoToken()
     {
         return latestUndoToken;
     }
 
     // Advanced in complete transaction.
     long m_lastCommittedTxnId = 0L;
     long m_currentTxnId = Long.MIN_VALUE;
 
     SiteProcedureConnection getSiteProcedureConnection()
     {
         return this;
     }
 
 
     /**
      * SystemProcedures are "friends" with ExecutionSites and granted
      * access to internal state via m_systemProcedureContext.
      */
     SystemProcedureExecutionContext m_sysprocContext = new SystemProcedureExecutionContext() {
         @Override
         public Database getDatabase() {
             return m_context.database;
         }
 
         @Override
         public Cluster getCluster() {
             return m_context.cluster;
         }
 
         @Override
         public long getLastCommittedTxnId() {
             return m_lastCommittedTxnId;
         }
 
         @Override
         public long getCurrentTxnId() {
             return m_currentTxnId;
         }
 
         @Override
         public long getNextUndo() {
             return getNextUndoToken();
         }
 
         @Override
         public HashMap<String, ProcedureRunner> getProcedures() {
             throw new RuntimeException("Not implemented in iv2");
             // return m_loadedProcedures.procs;
         }
 
         @Override
         public long getSiteId() {
             return m_siteId;
         }
 
         @Override
         public boolean isLowestSiteId()
         {
             // FUTURE: should pass this status in at construction.
             long lowestSiteId = VoltDB.instance().getSiteTrackerForSnapshot().getLowestSiteForHost(getHostId());
             System.out.println("\t\tLowest. Site: " + m_siteId + " lowest: " + lowestSiteId);
             return m_siteId == lowestSiteId;
         }
 
 
         @Override
         public int getHostId() {
             return CoreUtils.getHostIdFromHSId(m_siteId);
         }
 
         @Override
         public int getPartitionId() {
             return m_partitionId;
         }
 
         @Override
         public long getCatalogCRC() {
             return m_context.getCatalogCRC();
         }
 
         @Override
         public SiteTracker getSiteTracker() {
             throw new RuntimeException("Not implemented in iv2");
         }
 
         @Override
         public SiteTracker getSiteTrackerForSnapshot() {
             return VoltDB.instance().getSiteTrackerForSnapshot();
         }
 
         @Override
         public int getNumberOfPartitions() {
             return m_numberOfPartitions;
         }
 
         @Override
         public SiteProcedureConnection getSiteProcedureConnection()
         {
             return Site.this;
         }
 
         @Override
         public SiteSnapshotConnection getSiteSnapshotConnection()
         {
             return Site.this;
         }
 
         @Override
         public void updateBackendLogLevels() {
             Site.this.updateBackendLogLevels();
         }
 
         @Override
         public boolean updateCatalog(String diffCmds, CatalogContext context, CatalogSpecificPlanner csp) {
             throw new RuntimeException("Not implemented in iv2");
         }
     };
 
     /** Create a new execution site and the corresponding EE */
     public Site(
             SiteTaskerQueue scheduler,
             long siteId,
             BackendTarget backend,
             CatalogContext context,
             String serializedCatalog,
             long txnId,
             int partitionId,
             int numPartitions,
             boolean createForRejoin)
     {
         m_siteId = siteId;
         m_context = context;
         m_partitionId = partitionId;
         m_numberOfPartitions = numPartitions;
         m_scheduler = scheduler;
         m_backend = backend;
         m_isRejoining = createForRejoin;
 
         // need this later when running in the final thread.
         m_startupConfig = new StartupConfig(serializedCatalog, txnId);
     }
 
     /** Update the loaded procedures. */
     void setLoadedProcedures(LoadedProcedureSet loadedProcedure)
     {
         m_loadedProcedures = loadedProcedure;
     }
 
     /** Thread specific initialization */
     void initialize(String serializedCatalog, long txnId)
     {
         if (m_backend == BackendTarget.NONE) {
             m_hsql = null;
             m_ee = new MockExecutionEngine();
         }
         else if (m_backend == BackendTarget.HSQLDB_BACKEND) {
             m_hsql = HsqlBackend.initializeHSQLBackend(m_siteId,
                                                        m_context);
             m_ee = new MockExecutionEngine();
         }
         else {
             m_hsql = null;
             m_ee = initializeEE(serializedCatalog, txnId);
         }
         // IZZY- Get me from the deployment file in some sane way somehow some day
         int snapshotPriority = 6;
         m_snapshotter = new SnapshotSiteProcessor(new Runnable() {
             @Override
             public void run() {
                 hostLog.info("Creating new SnapshotTask");
                 m_scheduler.offer(new SnapshotTask());
             }
         },
         snapshotPriority);
     }
 
     /** Create a native VoltDB execution engine */
     ExecutionEngine initializeEE(String serializedCatalog, final long txnId)
     {
         String hostname = CoreUtils.getHostnameOrAddress();
         ExecutionEngine eeTemp = null;
         try {
             if (m_backend == BackendTarget.NATIVE_EE_JNI) {
                 System.out.println("Creating JNI EE.");
                 eeTemp =
                     new ExecutionEngineJNI(
                         m_context.cluster.getRelativeIndex(),
                         m_siteId,
                         m_partitionId,
                         CoreUtils.getHostIdFromHSId(m_siteId),
                         hostname,
                         m_context.cluster.getDeployment().get("deployment").
                         getSystemsettings().get("systemsettings").getMaxtemptablesize(),
                         m_numberOfPartitions);
                 eeTemp.loadCatalog( txnId, serializedCatalog);
                 // TODO: export integration will require a tick.
                 // lastTickTime = EstTime.currentTimeMillis();
                 // eeTemp.tick(lastTickTime, txnId);
             }
             else {
                 // set up the EE over IPC
                 eeTemp =
                     new ExecutionEngineIPC(
                             m_context.cluster.getRelativeIndex(),
                             m_siteId,
                             m_partitionId,
                             CoreUtils.getHostIdFromHSId(m_siteId),
                             hostname,
                             m_context.cluster.getDeployment().get("deployment").
                             getSystemsettings().get("systemsettings").getMaxtemptablesize(),
                             m_backend,
                             VoltDB.instance().getConfig().m_ipcPorts.remove(0),
                             m_numberOfPartitions);
                 eeTemp.loadCatalog( 0, serializedCatalog);
                 // TODO: export integration will require a tick.
                 // lastTickTime = EstTime.currentTimeMillis();
                 // eeTemp.tick( lastTickTime, 0);
             }
         }
         // just print error info an bail if we run into an error here
         catch (final Exception ex) {
             hostLog.l7dlog( Level.FATAL, LogKeys.host_ExecutionSite_FailedConstruction.name(),
                             new Object[] { m_siteId, m_siteIndex }, ex);
             VoltDB.crashLocalVoltDB(ex.getMessage(), true, ex);
         }
         return eeTemp;
     }
 
 
     @Override
     public void run()
     {
         Thread.currentThread().setName("Iv2ExecutionSite: " + CoreUtils.hsIdToString(m_siteId));
         initialize(m_startupConfig.m_serializedCatalog, m_startupConfig.m_txnId);
         m_startupConfig = null; // release the serializedCatalog bytes.
 
         try {
             while (m_shouldContinue) {
                 runLoop();
             }
         }
         catch (final RuntimeException e) {
             hostLog.l7dlog(Level.ERROR, LogKeys.host_ExecutionSite_RuntimeException.name(), e);
             throw e;
         }
         catch (final InterruptedException e) {
             // acceptable - this is how site blocked on an empty scheduler terminates.
         }
         shutdown();
     }
 
     void runLoop() throws InterruptedException
     {
         SiteTasker task = m_scheduler.poll();
         if (task != null) {
             if (task instanceof TransactionTask) {
                 m_currentTxnId = ((TransactionTask)task).getMpTxnId();
             }
             if (m_isRejoining) {
                 task.runForRejoin(getSiteProcedureConnection());
             }
             else {
                 task.run(getSiteProcedureConnection());
             }
         }
 
         m_snapshotter.doSnapshotWork(m_ee, true);
     }
 
     public void startShutdown()
     {
         m_shouldContinue = false;
     }
 
     void shutdown()
     {
         try {
             if (m_hsql != null) {
                 HsqlBackend.shutdownInstance();
             }
             if (m_ee != null) {
                 m_ee.release();
             }
             if (m_snapshotter != null) {
                 m_snapshotter.shutdown();
             }
         } catch (InterruptedException e) {
             hostLog.warn("Interrupted shutdown execution site.", e);
         }
     }
 
     //
     // SiteSnapshotConnection interface
     //
     @Override
     public void initiateSnapshots(Deque<SnapshotTableTask> tasks, long txnId, int numLiveHosts) {
         m_snapshotter.initiateSnapshots(m_ee, tasks, txnId, numLiveHosts);
     }
 
     /*
      * Do snapshot work exclusively until there is no more. Also blocks
      * until the syncing and closing of snapshot data targets has completed.
      */
     @Override
     public HashSet<Exception> completeSnapshotWork() throws InterruptedException {
         return m_snapshotter.completeSnapshotWork(m_ee);
     }
 
     //
     // Legacy SiteProcedureConnection needed by ProcedureRunner
     //
     @Override
     public long getCorrespondingSiteId()
     {
         return m_siteId;
     }
 
     @Override
     public int getCorrespondingPartitionId()
     {
         return m_partitionId;
     }
 
     @Override
     public int getCorrespondingHostId()
     {
         return CoreUtils.getHostIdFromHSId(m_siteId);
     }
 
     @Override
     public void loadTable(long txnId, String clusterName, String databaseName,
             String tableName, VoltTable data) throws VoltAbortException
     {
        throw new RuntimeException("Ain't gonna do it.");
     }
 
     @Override
     public void loadTable(long txnId, int tableId, VoltTable data)
     {
         long undo_token = getNextUndoToken();
         m_ee.loadTable(tableId, data,
                 txnId,
                 m_lastCommittedTxnId,
                 undo_token);
         m_ee.releaseUndoToken(undo_token);
         // I don't think this call is strictly necessary, but undo token
         // management is so scrod in general that I'm going to leave it since
         // it supposedly 'works'  --izzy
         getNextUndoToken();
     }
 
     @Override
     public void updateBackendLogLevels()
     {
         m_ee.setLogLevels(org.voltdb.jni.EELoggers.getLogLevels());
     }
 
     @Override
     public VoltTable[] executeQueryPlanFragmentsAndGetResults(
             long[] planFragmentIds, int numFragmentIds,
             ParameterSet[] parameterSets, int numParameterSets, long txnId,
             boolean readOnly) throws EEException
     {
         return m_ee.executeQueryPlanFragmentsAndGetResults(
             planFragmentIds,
             numFragmentIds,
             parameterSets,
             numParameterSets,
             txnId,
             m_lastCommittedTxnId,
             readOnly ? Long.MAX_VALUE : getNextUndoToken());
     }
 
     @Override
     public VoltTable executePlanFragment(long planFragmentId, int inputDepId,
                                          ParameterSet parameterSet, long txnId,
                                          boolean readOnly) throws EEException
     {
         return m_ee.executePlanFragment(planFragmentId,
                                         inputDepId,
                                         parameterSet,
                                         txnId,
                                         m_lastCommittedTxnId,
                                         readOnly ? Long.MAX_VALUE : getNextUndoToken());
     }
 
     @Override
     public long getReplicatedDMLDivisor()
     {
         return m_numberOfPartitions;
     }
 
     @Override
     public void simulateExecutePlanFragments(long txnId, boolean readOnly)
     {
         throw new RuntimeException("Not supported in IV2.");
     }
 
     @Override
     public Map<Integer, List<VoltTable>> recursableRun(
             TransactionState currentTxnState)
     {
         return currentTxnState.recursableRun(this);
     }
 
     @Override
     public void truncateUndoLog(boolean rollback, long beginUndoToken, long txnId)
     {
         if (rollback) {
             m_ee.undoUndoToken(beginUndoToken);
         }
         else {
             assert(latestUndoToken != Site.kInvalidUndoToken);
             assert(latestUndoToken >= beginUndoToken);
             if (latestUndoToken > beginUndoToken) {
                 m_ee.releaseUndoToken(latestUndoToken);
             }
             m_lastCommittedTxnId = txnId;
         }
     }
 
     @Override
     public void stashWorkUnitDependencies(Map<Integer, List<VoltTable>> dependencies)
     {
         m_ee.stashWorkUnitDependencies(dependencies);
     }
 
     @Override
     public DependencyPair executePlanFragment(
             TransactionState txnState,
             Map<Integer, List<VoltTable>> dependencies, long fragmentId,
             ParameterSet params)
     {
         ProcedureRunner runner = m_loadedProcedures.getSysproc(fragmentId);
         return runner.executePlanFragment(txnState, dependencies, fragmentId, params);
     }
 
     @Override
     public HsqlBackend getHsqlBackendIfExists()
     {
         return m_hsql;
     }
 
     @Override
     public long[] getUSOForExportTable(String signature)
     {
         return m_ee.getUSOForExportTable(signature);
     }
 
     @Override
     public VoltTable executeCustomPlanFragment(String plan, int inputDepId,
                                                long txnId, ParameterSet params, boolean readOnly)
     {
         return m_ee.executeCustomPlanFragment(plan, inputDepId, txnId,
                                               m_lastCommittedTxnId,
                                               readOnly ? Long.MAX_VALUE : getNextUndoToken(),
                                               params);
     }
 
     @Override
     public void toggleProfiler(int toggle)
     {
         m_ee.toggleProfiler(toggle);
     }
 
     @Override
     public void quiesce()
     {
         m_ee.quiesce(m_lastCommittedTxnId);
     }
 
     @Override
     public void exportAction(boolean syncAction,
                              int ackOffset,
                              Long sequenceNumber,
                              Integer partitionId, String tableSignature)
     {
         m_ee.exportAction(syncAction, ackOffset, sequenceNumber,
                           partitionId, tableSignature);
     }
 
     @Override
     public VoltTable[] getStats(SysProcSelector selector, int[] locators,
                                 boolean interval, Long now)
     {
         return m_ee.getStats(selector, locators, interval, now);
     }
 
     @Override
     public Future<?> doSnapshotWork(boolean ignoreQuietPeriod)
     {
         return m_snapshotter.doSnapshotWork(m_ee, ignoreQuietPeriod);
     }
 
     @Override
     public void setRejoinComplete() {
         m_isRejoining = false;
     }
 }
