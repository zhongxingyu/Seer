 package com.nearinfinity.blur.thrift;
 
 import java.util.List;
 
 import org.apache.thrift.TException;
 import org.apache.zookeeper.ZooKeeper;
 
 import com.nearinfinity.blur.log.Log;
 import com.nearinfinity.blur.log.LogFactory;
 import com.nearinfinity.blur.manager.clusterstatus.ClusterStatus;
 import com.nearinfinity.blur.manager.indexserver.utils.CreateTable;
 import com.nearinfinity.blur.manager.indexserver.utils.DisableTable;
 import com.nearinfinity.blur.manager.indexserver.utils.EnableTable;
 import com.nearinfinity.blur.manager.indexserver.utils.RemoveTable;
 import com.nearinfinity.blur.thrift.generated.BlurException;
 import com.nearinfinity.blur.thrift.generated.TableDescriptor;
 import com.nearinfinity.blur.thrift.generated.Blur.Iface;
 
 public abstract class TableAdmin implements Iface {
 
   private static final Log LOG = LogFactory.getLog(TableAdmin.class);
   protected ZooKeeper _zookeeper;
   protected ClusterStatus _clusterStatus;
 
   @Override
   public final void createTable(TableDescriptor tableDescriptor) throws BlurException, TException {
     try {
       //@todo Remove this once issue #27 is resolved
       if (tableDescriptor.compressionBlockSize > 32768) {
         tableDescriptor.compressionBlockSize = 32768;
       } else if (tableDescriptor.compressionBlockSize < 8192) {
         tableDescriptor.compressionBlockSize = 8192;
       }
       CreateTable.createTable(_zookeeper, tableDescriptor);
     } catch (Exception e) {
       LOG.error("Unknown error during create of [table={0}, tableDescriptor={1}]", e, tableDescriptor.name, tableDescriptor);
       throw new BException(e.getMessage(), e);
     }
     if (tableDescriptor.isEnabled) {
       enableTable(tableDescriptor.name);
     }
   }
 
   @Override
   public final void disableTable(String table) throws BlurException, TException {
     try {
       String cluster = _clusterStatus.getCluster(true,table);
       DisableTable.disableTable(_zookeeper, cluster, table);
     } catch (Exception e) {
       LOG.error("Unknown error during disable of [table={0}]", e, table);
       throw new BException(e.getMessage(), e);
     }
   }
 
   @Override
   public final void enableTable(String table) throws BlurException, TException {
     try {
       String cluster = _clusterStatus.getCluster(true,table);
       EnableTable.enableTable(_zookeeper, cluster, table);
     } catch (Exception e) {
       LOG.error("Unknown error during enable of [table={0}]", e, table);
       throw new BException(e.getMessage(), e);
     }
   }
 
   @Override
   public final void removeTable(String table, boolean deleteIndexFiles) throws BlurException, TException {
     try {
       String cluster = _clusterStatus.getCluster(true,table);
       RemoveTable.removeTable(_zookeeper, cluster, table, deleteIndexFiles);
     } catch (Exception e) {
       LOG.error("Unknown error during remove of [table={0}]", e, table);
       throw new BException(e.getMessage(), e);
     }
   }
 
   public boolean isTableEnabled(boolean useCache, String cluster, String table) {
     return _clusterStatus.isEnabled(useCache, cluster, table);
   }
   
   public void checkTable(String cluster, String table) throws BlurException {
     if (inSafeMode(true,table)) {
       throw new BlurException("Cluster for [" + table + "] is in safe mode",null);
     }
     if (tableExists(true,cluster,table)) {
       if (isTableEnabled(true,cluster,table)) {
         return;
       }
       throw new BlurException("Table [" + table + "] exists, but is not enabled",null);
     } else {
       throw new BlurException("Table [" + table + "] does not exist",null);
     }
   }
   
   @Override
   public final List<String> controllerServerList() throws BlurException, TException {
     try {
       return _clusterStatus.getControllerServerList();
     } catch (Exception e) {
       LOG.error("Unknown error while trying to get a controller list.", e);
       throw new BException("Unknown error while trying to get a controller list.", e);
     }
   }
   
   @Override
   public final List<String> shardServerList(String cluster) throws BlurException, TException {
     try {
       return _clusterStatus.getShardServerList(cluster);
     } catch (Exception e) {
       LOG.error("Unknown error while trying to get a shard server list.", e);
       throw new BException("Unknown error while trying to get a shard server list.", e);
     }
   }
   
   @Override
   public final List<String> shardClusterList() throws BlurException, TException {
     try {
       return _clusterStatus.getClusterList();
     } catch (Exception e) {
       LOG.error("Unknown error while trying to get a cluster list.", e);
       throw new BException("Unknown error while trying to get a cluster list.", e);
     }
   }
   
   @Override
   public final TableDescriptor describe(final String table) throws BlurException, TException {
     try {
       String cluster = _clusterStatus.getCluster(true,table);
       return _clusterStatus.getTableDescriptor(true,cluster,table);
     } catch (Exception e) {
       LOG.error("Unknown error while trying to describe a table [" + table + "].", e);
       throw new BException("Unknown error while trying to describe a table [" + table + "].", e);
     }
   }
   
   @Override
   public final List<String> tableListByCluster(String cluster) throws BlurException, TException {
     try {
       return _clusterStatus.getTableList(cluster);
     } catch (Exception e) {
       LOG.error("Unknown error while trying to get a table list by cluster [" + cluster + "].", e);
       throw new BException("Unknown error while trying to get a table list by cluster [" + cluster + "].", e);
     }
   }
   
   @Override
   public final List<String> tableList() throws BlurException, TException {
     try {
       return _clusterStatus.getTableList();
     } catch (Exception e) {
       LOG.error("Unknown error while trying to get a table list.", e);
       throw new BException("Unknown error while trying to get a table list.", e);
     }
   }
 
   private boolean inSafeMode(boolean useCache, String table) {
     String cluster = _clusterStatus.getCluster(true,table);
     return _clusterStatus.isInSafeMode(cluster);
   }
 
   public boolean tableExists(boolean useCache, String cluster, String table) {
     return _clusterStatus.exists(useCache,cluster,table);
   }
   
   public ClusterStatus getClusterStatus() {
     return _clusterStatus;
   }
 
   public void setClusterStatus(ClusterStatus clusterStatus) {
     _clusterStatus = clusterStatus;
   }
   
   public void setZookeeper(ZooKeeper zookeeper) {
     _zookeeper = zookeeper;
   }
 
 }
