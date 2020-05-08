 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hive.metastore;
 
 import static org.apache.hadoop.hive.metastore.MetaStoreUtils.DEFAULT_DATABASE_NAME;
 import static org.apache.hadoop.hive.metastore.MetaStoreUtils.isIndexTable;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.security.auth.login.LoginException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
 import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
 import org.apache.hadoop.hive.metastore.api.BusiTypeColumn;
 import org.apache.hadoop.hive.metastore.api.BusiTypeDatacenter;
 import org.apache.hadoop.hive.metastore.api.Busitype;
 import org.apache.hadoop.hive.metastore.api.ColumnStatistics;
 import org.apache.hadoop.hive.metastore.api.ConfigValSecurityException;
 import org.apache.hadoop.hive.metastore.api.CreatePolicy;
 import org.apache.hadoop.hive.metastore.api.Database;
 import org.apache.hadoop.hive.metastore.api.Device;
 import org.apache.hadoop.hive.metastore.api.EquipRoom;
 import org.apache.hadoop.hive.metastore.api.FieldSchema;
 import org.apache.hadoop.hive.metastore.api.FileOperationException;
 import org.apache.hadoop.hive.metastore.api.GeoLocation;
 import org.apache.hadoop.hive.metastore.api.GlobalSchema;
 import org.apache.hadoop.hive.metastore.api.HiveObjectPrivilege;
 import org.apache.hadoop.hive.metastore.api.HiveObjectRef;
 import org.apache.hadoop.hive.metastore.api.Index;
 import org.apache.hadoop.hive.metastore.api.InvalidInputException;
 import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
 import org.apache.hadoop.hive.metastore.api.InvalidOperationException;
 import org.apache.hadoop.hive.metastore.api.InvalidPartitionException;
 import org.apache.hadoop.hive.metastore.api.MSOperation;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.hadoop.hive.metastore.api.Node;
 import org.apache.hadoop.hive.metastore.api.NodeGroup;
 import org.apache.hadoop.hive.metastore.api.Order;
 import org.apache.hadoop.hive.metastore.api.Partition;
 import org.apache.hadoop.hive.metastore.api.PartitionEventType;
 import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
 import org.apache.hadoop.hive.metastore.api.PrincipalType;
 import org.apache.hadoop.hive.metastore.api.PrivilegeBag;
 import org.apache.hadoop.hive.metastore.api.Role;
 import org.apache.hadoop.hive.metastore.api.SFile;
 import org.apache.hadoop.hive.metastore.api.SFileLocation;
 import org.apache.hadoop.hive.metastore.api.SFileRef;
 import org.apache.hadoop.hive.metastore.api.SplitValue;
 import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
 import org.apache.hadoop.hive.metastore.api.Subpartition;
 import org.apache.hadoop.hive.metastore.api.Table;
 import org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore;
 import org.apache.hadoop.hive.metastore.api.Type;
 import org.apache.hadoop.hive.metastore.api.UnknownDBException;
 import org.apache.hadoop.hive.metastore.api.UnknownPartitionException;
 import org.apache.hadoop.hive.metastore.api.UnknownTableException;
 import org.apache.hadoop.hive.metastore.api.User;
 import org.apache.hadoop.hive.metastore.model.MetaStoreConst;
 import org.apache.hadoop.hive.shims.HadoopShims;
 import org.apache.hadoop.hive.shims.ShimLoader;
 import org.apache.hadoop.hive.thrift.HadoopThriftAuthBridge;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.util.StringUtils;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TMemoryBuffer;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransport;
 import org.apache.thrift.transport.TTransportException;
 
 /**
  * Hive Metastore Client.
  */
 public class HiveMetaStoreClient implements IMetaStoreClient {
   ThriftHiveMetastore.Iface client = null;
   private TTransport transport = null;
   private boolean isConnected = false;
   private URI metastoreUris[];
   private final HiveMetaHookLoader hookLoader;
   private final HiveConf conf;
   private String tokenStrForm;
   private final boolean localMetaStore;
   private final HashMap<String,HiveMetaStoreClient> remore_dc_map = new HashMap<String,HiveMetaStoreClient>();
 
   // for thrift connects
   private int retries = 5;
   private int retryDelaySeconds = 0;
 
   static final private Log LOG = LogFactory.getLog("hive.metastore");
 
   public HiveMetaStoreClient(String msUri, Integer retry, Integer retryDelay, HiveMetaHookLoader hookLoader)
   throws MetaException {
     this.hookLoader = hookLoader;
     this.conf = new HiveConf(HiveMetaStoreClient.class);
 
     localMetaStore = (msUri == null) ? true : msUri.trim().isEmpty();
     if (localMetaStore) {
       // instantiate the metastore server handler directly instead of connecting
       // through the network
       client = HiveMetaStore.newHMSHandler("hive client", conf);
       isConnected = true;
       return;
     }
 
     // get the number retries
     retries = retry;
     retryDelaySeconds = retryDelay;
 
     // user wants file store based configuration
     if (msUri != null) {
       String metastoreUrisString[] = msUri.split(",");
       metastoreUris = new URI[metastoreUrisString.length];
       try {
         int i = 0;
         for (String s : metastoreUrisString) {
           URI tmpUri = new URI(s);
           if (tmpUri.getScheme() == null) {
             throw new IllegalArgumentException("URI: " + s
                 + " does not have a scheme");
           }
           metastoreUris[i++] = tmpUri;
 
         }
       } catch (IllegalArgumentException e) {
         throw (e);
       } catch (Exception e) {
         MetaStoreUtils.logAndThrowMetaException(e);
       }
     } else {
       LOG.error("NOT getting uris from user args");
       throw new MetaException("MetaStoreURIs not found in conf file");
     }
     // finally open the store
     open();
   }
 
   public HiveMetaStoreClient(HiveConf conf)
     throws MetaException {
     this(conf, null);
   }
 
   public HiveMetaStoreClient(HiveConf conf, HiveMetaHookLoader hookLoader)
     throws MetaException {
 
     this.hookLoader = hookLoader;
     if (conf == null) {
       conf = new HiveConf(HiveMetaStoreClient.class);
     }
     this.conf = conf;
 
     String msUri = conf.getVar(HiveConf.ConfVars.METASTOREURIS);
     localMetaStore = (msUri == null) ? true : msUri.trim().isEmpty();
     if (localMetaStore) {
       // instantiate the metastore server handler directly instead of connecting
       // through the network
       client = HiveMetaStore.newHMSHandler("hive client", conf);
       isConnected = true;
       return;
     }
 
     // get the number retries
     retries = HiveConf.getIntVar(conf, HiveConf.ConfVars.METASTORETHRIFTCONNECTIONRETRIES);
     retryDelaySeconds = conf.getIntVar(ConfVars.METASTORE_CLIENT_CONNECT_RETRY_DELAY);
 
     // user wants file store based configuration
     if (conf.getVar(HiveConf.ConfVars.METASTOREURIS) != null) {
       String metastoreUrisString[] = conf.getVar(
           HiveConf.ConfVars.METASTOREURIS).split(",");
       metastoreUris = new URI[metastoreUrisString.length];
       try {
         int i = 0;
         for (String s : metastoreUrisString) {
           URI tmpUri = new URI(s);
           if (tmpUri.getScheme() == null) {
             throw new IllegalArgumentException("URI: " + s
                 + " does not have a scheme");
           }
           metastoreUris[i++] = tmpUri;
 
         }
       } catch (IllegalArgumentException e) {
         throw (e);
       } catch (Exception e) {
         MetaStoreUtils.logAndThrowMetaException(e);
       }
     } else if (conf.getVar(HiveConf.ConfVars.METASTOREDIRECTORY) != null) {
       metastoreUris = new URI[1];
       try {
         metastoreUris[0] = new URI(conf
             .getVar(HiveConf.ConfVars.METASTOREDIRECTORY));
       } catch (URISyntaxException e) {
         MetaStoreUtils.logAndThrowMetaException(e);
       }
     } else {
       LOG.error("NOT getting uris from conf");
       throw new MetaException("MetaStoreURIs not found in conf file");
     }
     // finally open the store
     open();
   }
 
   /**
    * Swaps the first element of the metastoreUris array with a random element from the
    * remainder of the array.
    */
   private void promoteRandomMetaStoreURI() {
     if (metastoreUris.length <= 1) {
       return;
     }
     Random rng = new Random();
     int index = rng.nextInt(metastoreUris.length - 1) + 1;
     URI tmp = metastoreUris[0];
     metastoreUris[0] = metastoreUris[index];
     metastoreUris[index] = tmp;
   }
 
   public void reconnect() throws MetaException {
     if (localMetaStore) {
       // For direct DB connections we don't yet support reestablishing connections.
       throw new MetaException("For direct MetaStore DB connections, we don't support retries" +
           " at the client level.");
     } else {
       // Swap the first element of the metastoreUris[] with a random element from the rest
       // of the array. Rationale being that this method will generally be called when the default
       // connection has died and the default connection is likely to be the first array element.
       promoteRandomMetaStoreURI();
       open();
       // must re-authenticated here!
       String user_name = conf.getVar(HiveConf.ConfVars.HIVE_USER);
       String passwd = conf.getVar(HiveConf.ConfVars.HIVE_USERPWD);
       try {
         this.authentication(user_name, passwd);
       } catch (NoSuchObjectException e) {
         e.printStackTrace();
         throw new MetaException(e.getMessage());
       } catch (TException e) {
         e.printStackTrace();
         throw new MetaException(e.getMessage());
       }
     }
   }
 
   /**
    * @param dbname
    * @param tbl_name
    * @param new_tbl
    * @throws InvalidOperationException
    * @throws MetaException
    * @throws TException
    * @see
    *   org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#alter_table(
    *   java.lang.String, java.lang.String,
    *   org.apache.hadoop.hive.metastore.api.Table)
    */
   public void alter_table(String dbname, String tbl_name, Table new_tbl)
       throws InvalidOperationException, MetaException, TException {
     client.alter_table(dbname, tbl_name, new_tbl);
   }
 
   /**
    * @param dbname
    * @param name
    * @param part_vals
    * @param newPart
    * @throws InvalidOperationException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#rename_partition(
    *      java.lang.String, java.lang.String, java.util.List, org.apache.hadoop.hive.metastore.api.Partition)
    */
   public void renamePartition(final String dbname, final String name, final List<String> part_vals, final Partition newPart)
       throws InvalidOperationException, MetaException, TException {
     client.rename_partition(dbname, name, part_vals, newPart);
   }
 
   private void open() throws MetaException {
     isConnected = false;
     TTransportException tte = null;
     HadoopShims shim = ShimLoader.getHadoopShims();
     boolean useSasl = conf.getBoolVar(ConfVars.METASTORE_USE_THRIFT_SASL);
     boolean useFramedTransport = conf.getBoolVar(ConfVars.METASTORE_USE_THRIFT_FRAMED_TRANSPORT);
     int clientSocketTimeout = conf.getIntVar(ConfVars.METASTORE_CLIENT_SOCKET_TIMEOUT);
 
     for (int attempt = 0; !isConnected && attempt < retries; ++attempt) {
       for (URI store : metastoreUris) {
         LOG.info("Trying to connect to metastore with URI " + store);
         try {
           transport = new TSocket(store.getHost(), store.getPort(), 1000 * clientSocketTimeout);
           if (useSasl) {
             // Wrap thrift connection with SASL for secure connection.
             try {
               HadoopThriftAuthBridge.Client authBridge =
                 ShimLoader.getHadoopThriftAuthBridge().createClient();
 
               // check if we should use delegation tokens to authenticate
               // the call below gets hold of the tokens if they are set up by hadoop
               // this should happen on the map/reduce tasks if the client added the
               // tokens into hadoop's credential store in the front end during job
               // submission.
               String tokenSig = conf.get("hive.metastore.token.signature");
               // tokenSig could be null
               tokenStrForm = shim.getTokenStrForm(tokenSig);
 
               if(tokenStrForm != null) {
                 // authenticate using delegation tokens via the "DIGEST" mechanism
                 transport = authBridge.createClientTransport(null, store.getHost(),
                     "DIGEST", tokenStrForm, transport);
               } else {
                 String principalConfig =
                     conf.getVar(HiveConf.ConfVars.METASTORE_KERBEROS_PRINCIPAL);
                 transport = authBridge.createClientTransport(
                     principalConfig, store.getHost(), "KERBEROS", null,
                     transport);
               }
             } catch (IOException ioe) {
               LOG.error("Couldn't create client transport", ioe);
               throw new MetaException(ioe.toString());
             }
           } else if (useFramedTransport) {
             transport = new TFramedTransport(transport);
           }
 
           client = new ThriftHiveMetastore.Client(new TBinaryProtocol(transport));
           try {
             transport.open();
             isConnected = true;
           } catch (TTransportException e) {
             tte = e;
             if (LOG.isDebugEnabled()) {
               LOG.warn("Failed to connect to the MetaStore Server...", e);
             } else {
               // Don't print full exception trace if DEBUG is not on.
               LOG.warn("Failed to connect to the MetaStore Server...");
             }
           }
 
           if (isConnected && !useSasl && conf.getBoolVar(ConfVars.METASTORE_EXECUTE_SET_UGI)){
             // Call set_ugi, only in unsecure mode.
             try {
               UserGroupInformation ugi = shim.getUGIForConf(conf);
               client.set_ugi(ugi.getUserName(), Arrays.asList(ugi.getGroupNames()));
             } catch (LoginException e) {
               LOG.warn("Failed to do login. set_ugi() is not successful, " +
                        "Continuing without it.", e);
             } catch (IOException e) {
               LOG.warn("Failed to find ugi of client set_ugi() is not successful, " +
                   "Continuing without it.", e);
             } catch (TException e) {
               LOG.warn("set_ugi() not successful, Likely cause: new client talking to old server. "
                   + "Continuing without it.", e);
             }
           }
         } catch (MetaException e) {
           LOG.error("Unable to connect to metastore with URI " + store
                     + " in attempt " + attempt, e);
         }
         if (isConnected) {
           break;
         }
       }
       // Wait before launching the next round of connection retries.
       if (retryDelaySeconds > 0) {
         try {
           LOG.info("Waiting " + retryDelaySeconds + " seconds before next connection attempt.");
           Thread.sleep(retryDelaySeconds * 1000);
         } catch (InterruptedException ignore) {}
       }
     }
 
     if (!isConnected) {
       throw new MetaException("Could not connect to meta store using any of the URIs provided." +
         " Most recent failure: " + StringUtils.stringifyException(tte));
     }
     LOG.info("Connected to metastore.");
   }
 
   public String getTokenStrForm() throws IOException {
     return tokenStrForm;
    }
 
   public void close() {
     isConnected = false;
     try {
       if (null != client) {
         client.shutdown();
       }
     } catch (TException e) {
       LOG.error("Unable to shutdown local metastore client", e);
     }
     // Transport would have got closed via client.shutdown(), so we dont need this, but
     // just in case, we make this call.
     if ((transport != null) && transport.isOpen()) {
       transport.close();
     }
   }
 
   /**
    * Creates a partition ,added by zjw
    *
    * @param tbl
    *          table for which partition needs to be created
    * @param partSpec
    *          partition keys and their values
    * @param location
    *          location of this partition
    * @param partParams
    *          partition parameters
    * @return created partition object
    * @throws HiveException
    *           if table doesn't exist or partition already exists
    */
   public Partition createPartition(Table tbl, String partitionName, Map<String, String> partSpec) throws MetaException,TException {
      return this.createPartition(tbl, partitionName, partSpec, null, null, null, null, 0, null, null, null, null, null);
   }
 
   /**
    * Creates a partition  ,added by zjw
    *
    * @param tbl
    *          table for which partition needs to be created
    * @param partSpec
    *          partition keys and their values
    * @param location
    *          location of this partition
    * @param partParams
    *          partition parameters
    * @param inputFormat the inputformat class
    * @param outputFormat the outputformat class
    * @param numBuckets the number of buckets
    * @param cols the column schema
    * @param serializationLib the serde class
    * @param serdeParams the serde parameters
    * @param bucketCols the bucketing columns
    * @param sortCols sort columns and order
    *
    * @return created partition object
    * @throws HiveException
    *           if table doesn't exist or partition already exists
    */
   private Partition createPartition(Table tbl, String partitionName, Map<String, String> partSpec,
       Path location, Map<String, String> partParams, String inputFormat, String outputFormat,
       int numBuckets, List<FieldSchema> cols,
       String serializationLib, Map<String, String> serdeParams,
       List<String> bucketCols, List<Order> sortCols) throws MetaException,TException {
 
     org.apache.hadoop.hive.metastore.api.Partition partition = null;
     List<String> pvals = new ArrayList<String>();
     for (String val : partSpec.values()) {
         pvals.add(val);
     }
 
     org.apache.hadoop.hive.metastore.api.Partition inPart = new org.apache.hadoop.hive.metastore.api.Partition();
     inPart.setDbName(tbl.getDbName());
     inPart.setTableName(tbl.getTableName());
     inPart.setValues(pvals);
 
     StorageDescriptor sd = new StorageDescriptor();
     try {
       // replace with THRIFT-138
       TMemoryBuffer buffer = new TMemoryBuffer(1024);
       TBinaryProtocol prot = new TBinaryProtocol(buffer);
       tbl.getSd().write(prot);
 
       sd.read(prot);
     } catch (TException e) {
       LOG.error("Could not create a copy of StorageDescription");
       throw new TException("Could not create a copy of StorageDescription",e);
     }
 
     inPart.setSd(sd);
     if(partitionName != null){
       inPart.setPartitionName(partitionName);
     }
 
     if (partParams != null) {
       inPart.setParameters(partParams);
     }
     if (inputFormat != null) {
       inPart.getSd().setInputFormat(inputFormat);
     }
     if (outputFormat != null) {
       inPart.getSd().setOutputFormat(outputFormat);
     }
     if (numBuckets != -1) {
       inPart.getSd().setNumBuckets(numBuckets);
     }
     if (cols != null) {
       inPart.getSd().setCols(cols);
     }
     if (serializationLib != null) {
         inPart.getSd().getSerdeInfo().setSerializationLib(serializationLib);
     }
     if (serdeParams != null) {
       inPart.getSd().getSerdeInfo().setParameters(serdeParams);
     }
     if (bucketCols != null) {
       inPart.getSd().setBucketCols(bucketCols);
     }
     if (sortCols != null) {
       inPart.getSd().setSortCols(sortCols);
     }
 
     if (tbl.getPartitionKeys() == null || tbl.getPartitionKeys().isEmpty()){
         throw new MetaException("Invalid partition for table " + tbl.getTableName());
     }
 
 
     try {
 
       LOG.warn("---zjw-- before hive add_partition.");
       partition = add_partition(inPart);
     } catch (Exception e) {
       LOG.error(StringUtils.stringifyException(e));
       throw new TException(e);
     }
 
     return partition;
   }
 
 
   /**
    * @param new_part
    * @return the added partition
    * @throws InvalidObjectException
    * @throws AlreadyExistsException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#add_partition(org.apache.hadoop.hive.metastore.api.Partition)
    */
   public Partition add_partition(Partition new_part)
       throws InvalidObjectException, AlreadyExistsException, MetaException,
       TException {
     if(new_part ==null ||
         new_part.getPartitionName()== null
         || "".equals(new_part.getPartitionName().trim())){
       throw new InvalidObjectException("Partition is null or partition name is not setted.");
     }
     return deepCopy(client.add_partition(new_part));
   }
 
   /**
    * @param new_parts
    * @throws InvalidObjectException
    * @throws AlreadyExistsException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#add_partitions(List)
    */
   public int add_partitions(List<Partition> new_parts)
       throws InvalidObjectException, AlreadyExistsException, MetaException,
       TException {
     return client.add_partitions(new_parts);
   }
 
   /**
    * @param table_name
    * @param db_name
    * @param part_vals
    * @return the appended partition
    * @throws InvalidObjectException
    * @throws AlreadyExistsException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#append_partition(java.lang.String,
    *      java.lang.String, java.util.List)
    */
   public Partition appendPartition(String db_name, String table_name,
       List<String> part_vals) throws InvalidObjectException,
       AlreadyExistsException, MetaException, TException {
     return deepCopy(client.append_partition(db_name, table_name, part_vals));
   }
 
   public Partition appendPartition(String dbName, String tableName, String partName)
       throws InvalidObjectException, AlreadyExistsException,
              MetaException, TException {
     return deepCopy(
         client.append_partition_by_name(dbName, tableName, partName));
   }
 
   /**
    * Create a new Database
    * @param db
    * @throws AlreadyExistsException
    * @throws InvalidObjectException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_database(Database)
    */
   public void createDatabase(Database db)
       throws AlreadyExistsException, InvalidObjectException, MetaException, TException {
     client.create_database(db);
   }
 
   /**
    * @param tbl
    * @throws MetaException
    * @throws NoSuchObjectException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_table(org.apache.hadoop.hive.metastore.api.Table)
    */
   public void createTable(Table tbl) throws AlreadyExistsException,
       InvalidObjectException, MetaException, NoSuchObjectException, TException {
     HiveMetaHook hook = getHook(tbl);
     if (hook != null) {
       hook.preCreateTable(tbl);
     }
     boolean success = false;
     try {
       client.create_table(tbl);
       if (hook != null) {
         hook.commitCreateTable(tbl);
       }
       success = true;
     } finally {
       if (!success && (hook != null)) {
         hook.rollbackCreateTable(tbl);
       }
     }
   }
 
   @Override
   public void createTableByUser(Table tbl, User user) throws AlreadyExistsException,
   InvalidObjectException, MetaException, NoSuchObjectException, TException {
     HiveMetaHook hook = getHook(tbl);
     if (hook != null) {
       hook.preCreateTable(tbl);
     }
     boolean success = false;
     try {
       client.create_table_by_user(tbl, user);
       if (hook != null) {
         hook.commitCreateTable(tbl);
       }
       success = true;
     } finally {
       if (!success && (hook != null)) {
         hook.rollbackCreateTable(tbl);
       }
     }
   }
 
   /**
    * @param type
    * @return true or false
    * @throws AlreadyExistsException
    * @throws InvalidObjectException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_type(org.apache.hadoop.hive.metastore.api.Type)
    */
   public boolean createType(Type type) throws AlreadyExistsException,
       InvalidObjectException, MetaException, TException {
     return client.create_type(type);
   }
 
   /**
    * @param name
    * @throws NoSuchObjectException
    * @throws InvalidOperationException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_database(java.lang.String, boolean, boolean)
    */
   public void dropDatabase(String name)
       throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
     dropDatabase(name, true, false, false);
   }
 
   public void dropDatabase(String name, boolean deleteData, boolean ignoreUnknownDb)
       throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
     dropDatabase(name, deleteData, ignoreUnknownDb, false);
   }
 
   public void dropDatabase(String name, boolean deleteData, boolean ignoreUnknownDb, boolean cascade)
       throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
     try {
       getDatabase(name);
     } catch (NoSuchObjectException e) {
       if (!ignoreUnknownDb) {
         throw e;
       }
       return;
     }
 
     if (cascade) {
        List<String> tableList = getAllTables(name);
        for (String table : tableList) {
          try {
             dropTable(name, table, deleteData, false);
          } catch (UnsupportedOperationException e) {
            // Ignore Index tables, those will be dropped with parent tables
          }
         }
     }
     client.drop_database(name, deleteData, cascade);
   }
 
 
   /**
    * @param tbl_name
    * @param db_name
    * @param part_vals
    * @return true or false
    * @throws NoSuchObjectException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String,
    *      java.lang.String, java.util.List, boolean)
    */
   public boolean dropPartition(String db_name, String tbl_name,
       List<String> part_vals) throws NoSuchObjectException, MetaException,
       TException {
     return dropPartition(db_name, tbl_name, part_vals, true);
   }
 
   public boolean dropPartition(String dbName, String tableName, String partName, boolean deleteData)
       throws NoSuchObjectException, MetaException, TException {
     return client.drop_partition_by_name(dbName, tableName, partName, deleteData);
   }
   /**
    * @param db_name
    * @param tbl_name
    * @param part_vals
    * @param deleteData
    *          delete the underlying data or just delete the table in metadata
    * @return true or false
    * @throws NoSuchObjectException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String,
    *      java.lang.String, java.util.List, boolean)
    */
   public boolean dropPartition(String db_name, String tbl_name,
       List<String> part_vals, boolean deleteData) throws NoSuchObjectException,
       MetaException, TException {
     return client.drop_partition(db_name, tbl_name, part_vals, deleteData);
   }
 
   /**
    * @param name
    * @param dbname
    * @throws NoSuchObjectException
    * @throws ExistingDependentsException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_table(java.lang.String,
    *      java.lang.String, boolean)
    */
   public void dropTable(String dbname, String name)
       throws NoSuchObjectException, MetaException, TException {
     dropTable(dbname, name, true, true);
   }
 
   /** {@inheritDoc} */
   @Deprecated
   public void dropTable(String tableName, boolean deleteData)
       throws MetaException, UnknownTableException, TException, NoSuchObjectException {
     dropTable(DEFAULT_DATABASE_NAME, tableName, deleteData, false);
   }
 
   /**
    * @param dbname
    * @param name
    * @param deleteData
    *          delete the underlying data or just delete the table in metadata
    * @throws NoSuchObjectException
    * @throws ExistingDependentsException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_table(java.lang.String,
    *      java.lang.String, boolean)
    */
   public void dropTable(String dbname, String name, boolean deleteData,
       boolean ignoreUknownTab) throws MetaException, TException,
       NoSuchObjectException, UnsupportedOperationException {
 
     Table tbl;
     try {
       tbl = getTable(dbname, name);
     } catch (NoSuchObjectException e) {
       if (!ignoreUknownTab) {
         throw e;
       }
       return;
     }
     if (isIndexTable(tbl)) {
       throw new UnsupportedOperationException("Cannot drop index tables");
     }
     HiveMetaHook hook = getHook(tbl);
     if (hook != null) {
       hook.preDropTable(tbl);
     }
     boolean success = false;
     try {
       client.drop_table(dbname, name, deleteData);
       if (hook != null) {
         hook.commitDropTable(tbl, deleteData);
       }
       success=true;
     } catch (NoSuchObjectException e) {
       if (!ignoreUknownTab) {
         throw e;
       }
     } finally {
       if (!success && (hook != null)) {
         hook.rollbackDropTable(tbl);
       }
     }
   }
 
   /**
    * @param type
    * @return true if the type is dropped
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_type(java.lang.String)
    */
   public boolean dropType(String type) throws NoSuchObjectException, MetaException, TException {
     return client.drop_type(type);
   }
 
   /**
    * @param name
    * @return map of types
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_type_all(java.lang.String)
    */
   public Map<String, Type> getTypeAll(String name) throws MetaException,
       TException {
     Map<String, Type> result = null;
     Map<String, Type> fromClient = client.get_type_all(name);
     if (fromClient != null) {
       result = new LinkedHashMap<String, Type>();
       for (String key : fromClient.keySet()) {
         result.put(key, deepCopy(fromClient.get(key)));
       }
     }
     return result;
   }
 
   /** {@inheritDoc} */
   public List<String> getDatabases(String databasePattern)
     throws MetaException {
     try {
       return client.get_databases(databasePattern);
     } catch (Exception e) {
       MetaStoreUtils.logAndThrowMetaException(e);
     }
     return null;
   }
 
   /** {@inheritDoc} */
   public List<String> getAllDatabases() throws MetaException {
     try {
       return client.get_all_databases();
     } catch (Exception e) {
       MetaStoreUtils.logAndThrowMetaException(e);
     }
     return null;
   }
 
   /**
    * @param tbl_name
    * @param db_name
    * @param max_parts
    * @return list of partitions
    * @throws NoSuchObjectException
    * @throws MetaException
    * @throws TException
    */
   public List<Partition> listPartitions(String db_name, String tbl_name,
       short max_parts) throws NoSuchObjectException, MetaException, TException {
     return deepCopyPartitions(
         client.get_partitions(db_name, tbl_name, max_parts));
   }
 
   @Override
   public List<Partition> listPartitions(String db_name, String tbl_name,
       List<String> part_vals, short max_parts)
       throws NoSuchObjectException, MetaException, TException {
     return deepCopyPartitions(
         client.get_partitions_ps(db_name, tbl_name, part_vals, max_parts));
   }
 
   @Override
   public List<Partition> listPartitionsWithAuthInfo(String db_name,
       String tbl_name, short max_parts, String user_name, List<String> group_names)
        throws NoSuchObjectException, MetaException, TException {
     return deepCopyPartitions(
         client.get_partitions_with_auth(db_name, tbl_name, max_parts, user_name, group_names));
   }
 
   @Override
   public List<Partition> listPartitionsWithAuthInfo(String db_name,
       String tbl_name, List<String> part_vals, short max_parts,
       String user_name, List<String> group_names) throws NoSuchObjectException,
       MetaException, TException {
     return deepCopyPartitions(client.get_partitions_ps_with_auth(db_name,
         tbl_name, part_vals, max_parts, user_name, group_names));
   }
 
   /**
    * Get list of partitions matching specified filter
    * @param db_name the database name
    * @param tbl_name the table name
    * @param filter the filter string,
    *    for example "part1 = \"p1_abc\" and part2 <= "\p2_test\"". Filtering can
    *    be done only on string partition keys.
    * @param max_parts the maximum number of partitions to return,
    *    all partitions are returned if -1 is passed
    * @return list of partitions
    * @throws MetaException
    * @throws NoSuchObjectException
    * @throws TException
    */
   public List<Partition> listPartitionsByFilter(String db_name, String tbl_name,
       String filter, short max_parts) throws MetaException,
          NoSuchObjectException, TException {
     return deepCopyPartitions(
         client.get_partitions_by_filter(db_name, tbl_name, filter, max_parts));
   }
 
   /**
    * @param name
    * @return the database
    * @throws NoSuchObjectException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_database(java.lang.String)
    */
   public Database getDatabase(String name) throws NoSuchObjectException,
       MetaException, TException {
     return deepCopy(client.get_database(name));
   }
 
   /**
    * @param tbl_name
    * @param db_name
    * @param part_vals
    * @return the partition
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_partition(java.lang.String,
    *      java.lang.String, java.util.List)
    */
   public Partition getPartition(String db_name, String tbl_name,
       List<String> part_vals) throws NoSuchObjectException, MetaException, TException {
     return deepCopy(client.get_partition(db_name, tbl_name, part_vals));
   }
 
   public List<Partition> getPartitionsByNames(String db_name, String tbl_name,
       List<String> part_names) throws NoSuchObjectException, MetaException, TException {
     return deepCopyPartitions(client.get_partitions_by_names(db_name, tbl_name, part_names));
   }
 
   @Override
   public Partition getPartitionWithAuthInfo(String db_name, String tbl_name,
       List<String> part_vals, String user_name, List<String> group_names)
       throws MetaException, UnknownTableException, NoSuchObjectException,
       TException {
     return deepCopy(client.get_partition_with_auth(db_name, tbl_name, part_vals, user_name, group_names));
   }
 
   /**
    * @param name
    * @param dbname
    * @return the table
    * @throws NoSuchObjectException
    * @throws MetaException
    * @throws TException
    * @throws NoSuchObjectException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_table(java.lang.String,
    *      java.lang.String)
    */
   public Table getTable(String dbname, String name) throws MetaException,
       TException, NoSuchObjectException {
     return deepCopy(client.get_table(dbname, name));
   }
 
   /** {@inheritDoc} */
   @Deprecated
   public Table getTable(String tableName) throws MetaException, TException,
       NoSuchObjectException {
     return getTable(DEFAULT_DATABASE_NAME, tableName);
   }
 
   /** {@inheritDoc} */
   public List<Table> getTableObjectsByName(String dbName, List<String> tableNames)
       throws MetaException, InvalidOperationException, UnknownDBException, TException {
     return deepCopyTables(client.get_table_objects_by_name(dbName, tableNames));
   }
 
   /** {@inheritDoc} */
   public List<String> listTableNamesByFilter(String dbName, String filter, short maxTables)
       throws MetaException, TException, InvalidOperationException, UnknownDBException {
     return client.get_table_names_by_filter(dbName, filter, maxTables);
   }
 
   /**
    * @param name
    * @return the type
    * @throws MetaException
    * @throws TException
    * @throws NoSuchObjectException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_type(java.lang.String)
    */
   public Type getType(String name) throws NoSuchObjectException, MetaException, TException {
     return deepCopy(client.get_type(name));
   }
 
   /** {@inheritDoc} */
   public List<String> getTables(String dbname, String tablePattern) throws MetaException {
     try {
       return client.get_tables(dbname, tablePattern);
     } catch (Exception e) {
       MetaStoreUtils.logAndThrowMetaException(e);
     }
     return null;
   }
 
   /** {@inheritDoc} */
   public List<String> getAllTables(String dbname) throws MetaException {
     try {
       return client.get_all_tables(dbname);
     } catch (Exception e) {
       MetaStoreUtils.logAndThrowMetaException(e);
     }
     return null;
   }
 
   public boolean tableExists(String databaseName, String tableName) throws MetaException,
       TException, UnknownDBException {
     try {
       client.get_table(databaseName, tableName);
     } catch (NoSuchObjectException e) {
       return false;
     }
     return true;
   }
 
   /** {@inheritDoc} */
   @Deprecated
   public boolean tableExists(String tableName) throws MetaException,
       TException, UnknownDBException {
     return tableExists(DEFAULT_DATABASE_NAME, tableName);
   }
 
   public List<String> listPartitionNames(String dbName, String tblName,
       short max) throws MetaException, TException {
     return client.get_partition_names(dbName, tblName, max);
   }
 
   @Override
   public List<String> listPartitionNames(String db_name, String tbl_name,
       List<String> part_vals, short max_parts)
       throws MetaException, TException, NoSuchObjectException {
     return client.get_partition_names_ps(db_name, tbl_name, part_vals, max_parts);
   }
 
   public void alter_partition(String dbName, String tblName, Partition newPart)
       throws InvalidOperationException, MetaException, TException {
     client.alter_partition(dbName, tblName, newPart);
   }
 
   public void alter_partitions(String dbName, String tblName, List<Partition> newParts)
   throws InvalidOperationException, MetaException, TException {
     client.alter_partitions(dbName, tblName, newParts);
 }
 
   public void alterDatabase(String dbName, Database db)
       throws MetaException, NoSuchObjectException, TException {
     client.alter_database(dbName, db);
   }
   /**
    * @param db
    * @param tableName
    * @throws UnknownTableException
    * @throws UnknownDBException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_fields(java.lang.String,
    *      java.lang.String)
    */
   public List<FieldSchema> getFields(String db, String tableName)
       throws MetaException, TException, UnknownTableException,
       UnknownDBException {
     return deepCopyFieldSchemas(client.get_fields(db, tableName));
   }
 
   /**
    * create an index
    * @param index the index object
    * @param indexTable which stores the index data
    * @throws InvalidObjectException
    * @throws MetaException
    * @throws NoSuchObjectException
    * @throws TException
    * @throws AlreadyExistsException
    */
   public void createIndex(Index index, Table indexTable) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException {
     client.add_index(index, indexTable);
   }
 
   /**
    * @param dbname
    * @param base_tbl_name
    * @param idx_name
    * @param new_idx
    * @throws InvalidOperationException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#alter_index(java.lang.String,
    *      java.lang.String, java.lang.String, org.apache.hadoop.hive.metastore.api.Index)
    */
   public void alter_index(String dbname, String base_tbl_name, String idx_name, Index new_idx)
       throws InvalidOperationException, MetaException, TException {
     client.alter_index(dbname, base_tbl_name, idx_name, new_idx);
   }
 
   /**
    * @param dbName
    * @param tblName
    * @param indexName
    * @return the index
    * @throws MetaException
    * @throws UnknownTableException
    * @throws NoSuchObjectException
    * @throws TException
    */
   public Index getIndex(String dbName, String tblName, String indexName)
       throws MetaException, UnknownTableException, NoSuchObjectException,
       TException {
     return deepCopy(client.get_index_by_name(dbName, tblName, indexName));
   }
 
   /**
    * list indexes of the give base table
    * @param dbName
    * @param tblName
    * @param max
    * @return the list of indexes
    * @throws NoSuchObjectException
    * @throws MetaException
    * @throws TException
    */
   public List<String> listIndexNames(String dbName, String tblName, short max)
       throws MetaException, TException {
     return client.get_index_names(dbName, tblName, max);
   }
 
   /**
    * list all the index names of the give base table.
    *
    * @param dbName
    * @param tblName
    * @param max
    * @return list of indexes
    * @throws MetaException
    * @throws TException
    */
   public List<Index> listIndexes(String dbName, String tblName, short max)
       throws NoSuchObjectException, MetaException, TException {
     return client.get_indexes(dbName, tblName, max);
   }
 
   /** {@inheritDoc} */
   public boolean updateTableColumnStatistics(ColumnStatistics statsObj)
     throws NoSuchObjectException, InvalidObjectException, MetaException, TException,
     InvalidInputException{
     return client.update_table_column_statistics(statsObj);
   }
 
   /** {@inheritDoc} */
   public boolean updatePartitionColumnStatistics(ColumnStatistics statsObj)
     throws NoSuchObjectException, InvalidObjectException, MetaException, TException,
     InvalidInputException{
     return client.update_partition_column_statistics(statsObj);
   }
 
   /** {@inheritDoc} */
   public ColumnStatistics getTableColumnStatistics(String dbName, String tableName,String colName)
     throws NoSuchObjectException, MetaException, TException, InvalidInputException,
     InvalidObjectException {
     return client.get_table_column_statistics(dbName, tableName, colName);
   }
 
   /** {@inheritDoc} */
   public ColumnStatistics getPartitionColumnStatistics(String dbName, String tableName,
     String partName, String colName) throws NoSuchObjectException, MetaException, TException,
     InvalidInputException, InvalidObjectException {
     return client.get_partition_column_statistics(dbName, tableName, partName, colName);
   }
 
   /** {@inheritDoc} */
   public boolean deletePartitionColumnStatistics(String dbName, String tableName, String partName,
     String colName) throws NoSuchObjectException, InvalidObjectException, MetaException,
     TException, InvalidInputException
   {
     return client.delete_partition_column_statistics(dbName, tableName, partName, colName);
   }
 
   /** {@inheritDoc} */
   public boolean deleteTableColumnStatistics(String dbName, String tableName, String colName)
     throws NoSuchObjectException, InvalidObjectException, MetaException, TException,
     InvalidInputException
   {
     return client.delete_table_column_statistics(dbName, tableName, colName);
   }
 
   /**
    * @param db
    * @param tableName
    * @throws UnknownTableException
    * @throws UnknownDBException
    * @throws MetaException
    * @throws TException
    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_schema(java.lang.String,
    *      java.lang.String)
    */
   public List<FieldSchema> getSchema(String db, String tableName)
       throws MetaException, TException, UnknownTableException,
       UnknownDBException {
     return deepCopyFieldSchemas(client.get_schema(db, tableName));
   }
 
   public String getConfigValue(String name, String defaultValue)
       throws TException, ConfigValSecurityException {
     return client.get_config_value(name, defaultValue);
   }
 
   public Partition getPartition(String db, String tableName, String partName)
       throws MetaException, TException, UnknownTableException, NoSuchObjectException {
     return deepCopy(client.get_partition_by_name(db, tableName, partName));
   }
 
   public Partition appendPartitionByName(String dbName, String tableName, String partName)
       throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
     return deepCopy(
         client.append_partition_by_name(dbName, tableName, partName));
   }
 
   public boolean dropPartitionByName(String dbName, String tableName, String partName, boolean deleteData)
       throws NoSuchObjectException, MetaException, TException {
     return client.drop_partition_by_name(dbName, tableName, partName, deleteData);
   }
 
   private HiveMetaHook getHook(Table tbl) throws MetaException {
     if (hookLoader == null) {
       return null;
     }
     return hookLoader.getHook(tbl);
   }
 
   @Override
   public List<String> partitionNameToVals(String name) throws MetaException, TException {
     return client.partition_name_to_vals(name);
   }
 
   @Override
   public Map<String, String> partitionNameToSpec(String name) throws MetaException, TException{
     return client.partition_name_to_spec(name);
   }
 
   /**
    * @param partition
    * @return
    */
   private Partition deepCopy(Partition partition) {
     Partition copy = null;
     if (partition != null) {
       copy = new Partition(partition);
     }
     return copy;
   }
 
   private Database deepCopy(Database database) {
     Database copy = null;
     if (database != null) {
       copy = new Database(database);
     }
     return copy;
   }
 
   private Table deepCopy(Table table) {
     Table copy = null;
     if (table != null) {
       copy = new Table(table);
     }
     return copy;
   }
 
   private Index deepCopy(Index index) {
     Index copy = null;
     if (index != null) {
       copy = new Index(index);
     }
     return copy;
   }
 
   private Type deepCopy(Type type) {
     Type copy = null;
     if (type != null) {
       copy = new Type(type);
     }
     return copy;
   }
 
   private FieldSchema deepCopy(FieldSchema schema) {
     FieldSchema copy = null;
     if (schema != null) {
       copy = new FieldSchema(schema);
     }
     return copy;
   }
 
   private List<Partition> deepCopyPartitions(List<Partition> partitions) {
     List<Partition> copy = null;
     if (partitions != null) {
       copy = new ArrayList<Partition>();
       for (Partition part : partitions) {
         copy.add(deepCopy(part));
       }
     }
     return copy;
   }
 
   private List<Table> deepCopyTables(List<Table> tables) {
     List<Table> copy = null;
     if (tables != null) {
       copy = new ArrayList<Table>();
       for (Table tab : tables) {
         copy.add(deepCopy(tab));
       }
     }
     return copy;
   }
 
   private List<FieldSchema> deepCopyFieldSchemas(List<FieldSchema> schemas) {
     List<FieldSchema> copy = null;
     if (schemas != null) {
       copy = new ArrayList<FieldSchema>();
       for (FieldSchema schema : schemas) {
         copy.add(deepCopy(schema));
       }
     }
     return copy;
   }
 
   @Override
   public boolean dropIndex(String dbName, String tblName, String name,
       boolean deleteData) throws NoSuchObjectException, MetaException,
       TException {
     return client.drop_index_by_name(dbName, tblName, name, deleteData);
   }
 
   @Override
   public boolean grant_role(String roleName, String userName,
       PrincipalType principalType, String grantor, PrincipalType grantorType,
       boolean grantOption) throws MetaException, TException {
     return client.grant_role(roleName, userName, principalType, grantor,
         grantorType, grantOption);
   }
 
   @Override
   public boolean create_role(Role role)
       throws MetaException, TException {
     return client.create_role(role);
   }
 
   @Override
   public boolean drop_role(String roleName) throws MetaException, TException {
     return client.drop_role(roleName);
   }
 
   @Override
   public List<Role> list_roles(String principalName,
       PrincipalType principalType) throws MetaException, TException {
     return client.list_roles(principalName, principalType);
   }
 
   @Override
   public List<String> listRoleNames() throws MetaException, TException {
     return client.get_role_names();
   }
 
   @Override
   public boolean grant_privileges(PrivilegeBag privileges)
       throws MetaException, TException {
     return client.grant_privileges(privileges);
   }
 
   @Override
   public boolean revoke_role(String roleName, String userName,
       PrincipalType principalType) throws MetaException, TException {
     return client.revoke_role(roleName, userName, principalType);
   }
 
   @Override
   public boolean revoke_privileges(PrivilegeBag privileges) throws MetaException,
       TException {
     return client.revoke_privileges(privileges);
   }
 
   @Override
   public PrincipalPrivilegeSet get_privilege_set(HiveObjectRef hiveObject,
       String userName, List<String> groupNames) throws MetaException,
       TException {
     return client.get_privilege_set(hiveObject, userName, groupNames);
   }
 
   @Override
   public List<HiveObjectPrivilege> list_privileges(String principalName,
       PrincipalType principalType, HiveObjectRef hiveObject)
       throws MetaException, TException {
     return client.list_privileges(principalName, principalType, hiveObject);
   }
 
   public String getDelegationToken(String renewerKerberosPrincipalName) throws
   MetaException, TException, IOException {
     //a convenience method that makes the intended owner for the delegation
     //token request the current user
     String owner = conf.getUser();
     return getDelegationToken(owner, renewerKerberosPrincipalName);
   }
 
   @Override
   public String getDelegationToken(String owner, String renewerKerberosPrincipalName) throws
   MetaException, TException {
     if(localMetaStore) {
       throw new UnsupportedOperationException("getDelegationToken() can be " +
           "called only in thrift (non local) mode");
     }
     return client.get_delegation_token(owner, renewerKerberosPrincipalName);
   }
 
   @Override
   public long renewDelegationToken(String tokenStrForm) throws MetaException, TException {
     if(localMetaStore) {
       throw new UnsupportedOperationException("renewDelegationToken() can be " +
           "called only in thrift (non local) mode");
     }
     return client.renew_delegation_token(tokenStrForm);
 
   }
 
   @Override
   public void cancelDelegationToken(String tokenStrForm) throws MetaException, TException {
     if(localMetaStore) {
       throw new UnsupportedOperationException("renewDelegationToken() can be " +
           "called only in thrift (non local) mode");
     }
     client.cancel_delegation_token(tokenStrForm);
   }
 
   /**
    * Creates a synchronized wrapper for any {@link IMetaStoreClient}.
    * This may be used by multi-threaded applications until we have
    * fixed all reentrancy bugs.
    *
    * @param client unsynchronized client
    *
    * @return synchronized client
    */
   public static IMetaStoreClient newSynchronizedClient(
       IMetaStoreClient client) {
     return (IMetaStoreClient) Proxy.newProxyInstance(
       HiveMetaStoreClient.class.getClassLoader(),
       new Class [] { IMetaStoreClient.class },
       new SynchronizedHandler(client));
   }
 
   private static class SynchronizedHandler implements InvocationHandler {
     private final IMetaStoreClient client;
     private static final Object lock = SynchronizedHandler.class;
 
     SynchronizedHandler(IMetaStoreClient client) {
       this.client = client;
     }
 
     public Object invoke(Object proxy, Method method, Object [] args)
         throws Throwable {
       try {
         synchronized (lock) {
           return method.invoke(client, args);
         }
       } catch (InvocationTargetException e) {
         throw e.getTargetException();
       }
     }
   }
 
   @Override
   public void markPartitionForEvent(String db_name, String tbl_name, Map<String,String> partKVs, PartitionEventType eventType)
       throws MetaException, TException, NoSuchObjectException, UnknownDBException, UnknownTableException,
       InvalidPartitionException, UnknownPartitionException {
     assert db_name != null;
     assert tbl_name != null;
     assert partKVs != null;
     client.markPartitionForEvent(db_name, tbl_name, partKVs, eventType);
   }
 
   @Override
   public boolean isPartitionMarkedForEvent(String db_name, String tbl_name, Map<String,String> partKVs, PartitionEventType eventType)
       throws MetaException, NoSuchObjectException, UnknownTableException, UnknownDBException, TException,
       InvalidPartitionException, UnknownPartitionException {
     assert db_name != null;
     assert tbl_name != null;
     assert partKVs != null;
     return client.isPartitionMarkedForEvent(db_name, tbl_name, partKVs, eventType);
   }
 
   @Override
   public SFile create_file(String node_name, int repnr, String db_name, String table_name, List<SplitValue> values)
       throws FileOperationException, TException {
 
     if ("".equals(node_name)) {
       node_name = null;
     }
     if ("".equals(db_name)) {
       db_name = null;
     }
     if ("".equals(table_name)) {
       table_name = null;
     }
     if (repnr == 0) {
       repnr = 1;
     }
 
     return client.create_file(node_name, repnr, db_name, table_name, values);
   }
 
   @Override
   public Node add_node(String node_name, List<String> ipl) throws MetaException, TException {
     assert node_name != null;
     return client.add_node(node_name, ipl);
   }
 
   @Override
   public int close_file(SFile file) throws FileOperationException, TException {
     assert file != null;
     return client.close_file(file);
   }
 
   @Override
   public SFile get_file_by_id(long fid) throws FileOperationException, MetaException, TException {
     assert fid >= 0;
     return client.get_file_by_id(fid);
   }
 
   @Override
   public Node get_node(String node_name) throws MetaException, TException {
     assert node_name != null;
     return client.get_node(node_name);
   }
 
   @Override
   public int rm_file_logical(SFile file) throws FileOperationException, MetaException, TException {
     assert file != null;
     return client.rm_file_logical(file);
   }
 
   @Override
   public int restore_file(SFile file) throws FileOperationException, MetaException, TException {
     assert file != null;
     return client.restore_file(file);
   }
 
   @Override
   public int rm_file_physical(SFile file) throws FileOperationException, MetaException, TException {
     assert file != null;
     return client.rm_file_physical(file);
   }
 
   //added by liulichao
 @Override
 public boolean create_user(User user) throws InvalidObjectException,
     MetaException, TException {
    assert user != null;
    return client.create_user(user);
 }
 
 @Override
 public boolean drop_user(String user_name) throws NoSuchObjectException,
     MetaException, TException {
   assert user_name != null;
   return client.drop_user(user_name);
 }
 
 @Override
 public boolean modify_user(User user)
     throws NoSuchObjectException, MetaException, TException {
   assert user != null;
   return client.modify_user(user);
 
 }
 
 @Override
 public List<String> list_users_names() throws MetaException, TException {
   return client.list_users_names();
 }
 
 @Override
 public List<String> list_users(Database db) throws MetaException, TException {
   assert db != null;
   return client.list_users(db);
 }
 
 @Override
 public boolean authentication(String user_name, String passwd)
     throws NoSuchObjectException, MetaException, TException {
   return client.authentication(user_name, passwd);
 }
 //added by liulichao
 
 
 
   @Override
   public Node alter_node(String node_name, List<String> ipl, int status) throws MetaException,
       TException {
     assert node_name != null;
     assert ipl != null;
     assert (status >= 0 && status < MetaStoreConst.MNodeStatus.__MAX__);
     return client.alter_node(node_name, ipl, status);
   }
 
   @Override
   public int add_partition_files(Partition part, List<SFile> files) throws TException {
     assert part != null;
     assert files != null;
     return client.add_partition_files(part, files);
   }
 
   @Override
   public int drop_partition_files(Partition part, List<SFile> files) throws TException {
     assert part != null;
     assert files != null;
     return client.drop_partition_files(part, files);
   }
 
   @Override
   public List<String> get_partition_names(String db_name, String tbl_name, short max_parts)
       throws MetaException, TException {
       return client.get_partition_names(db_name, tbl_name, max_parts);
   }
 
   @Override
   public boolean add_partition_index(Index index, Partition part) throws MetaException, AlreadyExistsException, TException {
     assert index != null;
     assert part != null;
     return client.add_partition_index(index, part);
   }
 
   @Override
   // ignore non exist files.
   public List<SFile> get_files_by_ids(List<Long> fids) throws FileOperationException,
       MetaException, TException {
     List<SFile> lsf = new ArrayList<SFile>();
     if (fids.size() > 0) {
       for (Long id : fids) {
         // FIXME: ignore nonexist files.
         try {
           lsf.add(client.get_file_by_id(id));
         } catch (FileOperationException e) {
         }
       }
     }
 
     return lsf;
   }
 
   @Override
   public Boolean del_node(String node_name) throws MetaException, TException {
     assert node_name != null;
     if (client.del_node(node_name) > 0) {
       return true;
     } else {
       return false;
     }
   }
 
   @Override
   public List<Node> get_all_nodes() throws MetaException, TException {
     return client.get_all_nodes();
   }
 
   @Override
   public boolean drop_partition_index(Index index, Partition part) throws MetaException, InvalidObjectException, TException {
     assert index != null;
     assert part != null;
     return client.drop_partition_index(index, part);
   }
 
   @Override
   public boolean add_partition_index_files(Index index, Partition part, List<SFile> file,
       List<Long> originfid) throws MetaException, TException {
     assert index != null;
     assert part != null;
     assert file != null;
     assert originfid != null;
     return client.add_partition_index_files(index, part, file, originfid);
   }
 
   @Override
   public boolean drop_partition_index_files(Index index, Partition part, List<SFile> file)
       throws MetaException, TException {
     assert index != null;
     assert part != null;
     assert file != null;
     return client.drop_partition_index_files(index, part, file);
   }
 
   @Override
   public String getDMStatus() throws MetaException, TException {
     return client.getDMStatus();
   }
 
   @Override
   public boolean addDatawareHouseSql(Integer dwNum, String sql) throws MetaException, TException {
     return client.add_datawarehouse_sql(dwNum, sql);
   }
 
   public List<SFileRef> get_partition_index_files(Index index, Partition part)
       throws MetaException, TException {
     assert index != null;
     assert part != null;
     return client.get_partition_index_files(index, part);
   }
 
   @Override
   public int add_subpartition_files(Subpartition subpart, List<SFile> files) throws TException {
     // TODO Auto-generated method stub
     return client.add_subpartition_files(subpart, files);
   }
 
   @Override
   public int drop_subpartition_files(Subpartition subpart, List<SFile> files) throws TException {
     // TODO Auto-generated method stub
     return client.drop_subpartition_files(subpart, files);
   }
 
   @Override
   public boolean add_subpartition_index(Index index, Subpartition subpart) throws MetaException, AlreadyExistsException, TException {
     // TODO Auto-generated method stub
     return client.add_subpartition_index(index, subpart);
   }
 
   @Override
   public boolean drop_subpartition_index(Index index, Subpartition subpart) throws MetaException, InvalidObjectException, TException {
     // TODO Auto-generated method stub
     return client.drop_subpartition_index(index, subpart);
   }
 
   @Override
   public boolean add_subpartition_index_files(Index index, Subpartition subpart, List<SFile> file,
       List<Long> originfid) throws MetaException, TException {
     // TODO Auto-generated method stub
     return client.add_subpartition_index_files(index, subpart, file, originfid);
   }
 
   @Override
   public boolean drop_subpartition_index_files(Index index, Subpartition subpart, List<SFile> file)
       throws MetaException, TException {
     // TODO Auto-generated method stub
     return client.drop_subpartition_index_files(index, subpart, file);
   }
 
   @Override
   public List<String> getSubPartitions(String dbName, String tabName, String partName)
       throws MetaException, TException {
     Partition part = client.get_partition_by_name(dbName, tabName, partName);
     List<Subpartition> subparts = client.get_subpartitions(dbName, tabName, part);
     List<String> subpartNames = new ArrayList<String>();
     for(Subpartition sp : subparts ){
       subpartNames.add(sp.getPartitionName());
     }
     return subpartNames;
   }
 
   @Override
   public Database get_local_attribution() throws MetaException, TException {
     return client.get_local_attribution();
   }
 
   @Override
   public List<Database> get_all_attributions() throws MetaException, TException {
     return client.get_all_attributions();
   }
 
   @Override
   public Database get_attribution(String name) throws NoSuchObjectException, MetaException, TException {
     assert name != null;
     return client.get_attribution(name);
   }
 
   @Override
   public void create_attribution(Database database) throws AlreadyExistsException,
       InvalidObjectException, MetaException, TException {
     assert database != null;
     client.create_attribution(database);
   }
 
   @Override
   public void update_attribution(Database database) throws NoSuchObjectException,
       InvalidOperationException, MetaException, TException {
     assert database != null;
     client.update_attribution(database);
   }
 
   @Override
   public String getMP(String node_name, String devid) throws MetaException, TException {
     assert node_name != null;
     assert devid != null;
     return client.getMP(node_name, devid);
   }
 
   @Override
   public SFile get_file_by_name(String node, String devid, String location)
       throws FileOperationException, MetaException, TException {
     assert node != null;
     assert devid != null;
     assert location != null;
     return client.get_file_by_name(node, devid, location);
   }
 
   @Override
   public List<BusiTypeColumn> get_all_busi_type_cols() throws MetaException, TException {
     return client.get_all_busi_type_cols();
   }
 
   @Override
   public List<BusiTypeDatacenter> get_all_busi_type_datacenters() throws MetaException,TException {
     return client.get_all_busi_type_datacenters();
   }
 
   @Override
   public void append_busi_type_datacenter(BusiTypeDatacenter busiTypeDatacenter)
     throws InvalidObjectException, MetaException, TException {
     client.append_busi_type_datacenter(busiTypeDatacenter);
   }
 
   @Override
   public List<SFileRef> get_subpartition_index_files(Index index, Subpartition subpart)
       throws MetaException, TException {
     assert index != null;
     assert subpart != null;
     return client.get_subpartition_index_files(index, subpart);
   }
 
   @Override
   public List<SFileLocation> migrate2_stage1(String dbName, String tableName,
       List<String> partNames, String to_dc) throws MetaException, TException {
     assert dbName != null;
     assert tableName != null;
     assert partNames != null;
     assert to_dc != null;
     return client.migrate2_stage1(dbName, tableName, partNames, to_dc);
   }
 
   @Override
   public boolean migrate2_stage2(String dbName, String tableName, List<String> partNames,
       String to_dc, String to_db, String to_nas_devid) throws MetaException, TException {
     assert dbName != null;
     assert tableName != null;
     assert partNames != null;
     assert to_dc != null;
     assert to_nas_devid != null;
     return client.migrate2_stage2(dbName, tableName, partNames, to_dc, to_db, to_nas_devid);
   }
 
   @Override
   public boolean migrate2_in(Table tbl, List<Partition> parts, List<Index> idxs, String from_dc, String to_nas_devid,
       Map<Long, SFileLocation> fileMap) throws MetaException, TException {
     assert tbl != null;
     assert parts != null;
     assert from_dc != null;
     assert to_nas_devid != null;
     assert fileMap != null;
     return client.migrate2_in(tbl, parts, idxs, from_dc, to_nas_devid, fileMap);
   }
 
   @Override
   public IMetaStoreClient getRemoteDbMSC(String db_name) throws MetaException, TException {
     HiveMetaStoreClient rc = null;
     Database db =  null;
     if( !remore_dc_map.containsKey(db_name.toLowerCase())){
       db = get_attribution(db_name);
       rc = new HiveMetaStoreClient(db.getParameters().get("service.metastore.uri"),
           HiveConf.getIntVar(conf, HiveConf.ConfVars.METASTORETHRIFTCONNECTIONRETRIES),
           conf.getIntVar(ConfVars.METASTORE_CLIENT_CONNECT_RETRY_DELAY),
           null);
       remore_dc_map.put(db.getName(), rc);
     }else{
       rc = remore_dc_map.get(db_name);
     }
     return rc;
   }
 
   @Override
   public List<Busitype> showBusitypes() throws MetaException, TException {
     return client.showBusitypes();
   }
 
   @Override
   public int createBusitype(Busitype bt) throws MetaException, TException {
     return client.createBusitype(bt);
   }
 
   @Override
   public boolean online_filelocation(SFile file) throws MetaException, TException {
     assert file != null;
     return client.online_filelocation(file);
   }
 
   @Override
   public boolean toggle_safemode() throws MetaException, TException {
     return client.toggle_safemode();
   }
 
   @Override
   public Device createDevice(String devid, int prop, String node_name) throws MetaException,
       TException {
     assert devid != null;
     assert node_name != null;
     return client.create_device(devid, prop, node_name);
   }
 
   @Override
   public boolean delDevice(String devid) throws MetaException, TException {
     assert devid != null;
     return client.del_device(devid);
   }
 
   @Override
   public Device getDevice(String devid) throws MetaException, TException {
     assert devid != null;
     return client.get_device(devid);
   }
 
   @Override
   public boolean onlineDevice(String devid) throws MetaException, TException {
     assert devid != null;
     Device dev = client.get_device(devid);
     dev.setStatus(MetaStoreConst.MDeviceStatus.ONLINE);
     client.modify_device(dev, null);
 
     return true;
   }
 
   @Override
   public boolean offlineDevice(String devid) throws MetaException, TException {
     assert devid != null;
     Device dev = client.get_device(devid);
     dev.setStatus(MetaStoreConst.MDeviceStatus.OFFLINE);
     client.modify_device(dev, null);
 
     return true;
   }
 
   @Override
   public Device changeDeviceLocation(Device dev, Node node) throws MetaException, TException {
     assert dev != null;
     return client.modify_device(dev, node);
   }
 
 /**
  * @author cry
  */
 
   @Override
   public boolean addEquipRoom(EquipRoom er) throws MetaException, TException {
     return client.addEquipRoom(er);
   }
 
   @Override
   public boolean modifyEquipRoom(EquipRoom er) throws MetaException, TException {
     return client.modifyEquipRoom(er);
   }
 
   @Override
   public boolean deleteEquipRoom(EquipRoom er) throws MetaException, TException {
     return client.deleteEquipRoom(er);
   }
 
   @Override
   public List<EquipRoom> listEquipRoom() throws MetaException, TException {
     return client.listEquipRoom();
   }
 
   @Override
   public boolean addGeoLocation(GeoLocation gl) throws MetaException, TException {
     return client.addGeoLocation(gl);
   }
 
   @Override
   public boolean modifyGeoLocation(GeoLocation gl) throws MetaException, TException {
     return client.modifyGeoLocation(gl);
   }
 
   @Override
   public boolean deleteGeoLocation(GeoLocation gl) throws MetaException, TException {
     return client.deleteGeoLocation(gl);
   }
 
   @Override
   public List<GeoLocation> listGeoLocation() throws MetaException, TException {
     return client.listGeoLocation();
   }
 
   @Override
   public GeoLocation getGeoLocationByName(String geoLocName) throws MetaException, TException {
     return client.getGeoLocationByName(geoLocName);
   }
 
   @Override
   public boolean addNodeAssignment(String nodeName, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.addNodeAssignment(nodeName, dbName);
   }
 
   @Override
   public boolean deleteNodeAssignment(String nodeName, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.deleteNodeAssignment(nodeName, dbName);
   }
 
   @Override
   public boolean user_authority_check(User user, Table tbl, List<MSOperation> ops)
       throws MetaException, TException {
     assert user != null;
     assert tbl != null;
     assert ops != null;
     return client.user_authority_check(user, tbl, ops);
   }
 
   @Override
   public boolean createSchema(GlobalSchema schema) throws AlreadyExistsException,
       InvalidObjectException, MetaException, TException {
     // TODO Auto-generated method stub
     LOG.info("2 createSchema");
     return client.createSchema(schema);
   }
 
   @Override
   public boolean modifySchema(String schemaName, GlobalSchema schema) throws MetaException,
       TException {
     return client.modifySchema(schemaName, schema);
   }
 
   @Override
   public boolean deleteSchema(String schemaName) throws MetaException, TException {
     return client.deleteSchema(schemaName);
   }
 
   @Override
   public List<GlobalSchema> listSchemas() throws MetaException, TException {
     return client.listSchemas();
   }
 
   @Override
   public GlobalSchema getSchemaByName(String schemaName) throws NoSuchObjectException,
       MetaException, TException {
     return client.getSchemaByName(schemaName);
   }
 
   @Override
   public List<NodeGroup> getTableNodeGroups(String dbName, String tabName) throws MetaException,
       TException {
     return client.getTableNodeGroups(dbName, tabName);
   }
 
   @Override
   public List<Long> listTableFiles(String dbName, String tabName, int begin, int end)
       throws MetaException, TException {
     return client.listTableFiles(dbName, tabName, begin, end);
   }
 
   @Override
   public List<SFile> filterTableFiles(String dbName, String tabName, List<SplitValue> values)
       throws MetaException, TException {
    assert dbName != null;
    assert tabName != null;
    assert values != null;
     return client.filterTableFiles(dbName, tabName, values);
   }
 
   @Override
   public boolean addNodeGroup(NodeGroup ng) throws AlreadyExistsException, MetaException,
       TException {
     return client.addNodeGroup(ng);
   }
 
   @Override
   public boolean modifyNodeGroup(String schemaName, NodeGroup ng) throws MetaException, TException {
     return client.modifyNodeGroup(schemaName, ng);
   }
 
   @Override
   public boolean deleteNodeGroup(NodeGroup ng) throws MetaException, TException {
     return client.deleteNodeGroup(ng);
   }
 
   @Override
   public List<NodeGroup> listNodeGroups() throws MetaException, TException {
     return client.listNodeGroups();
   }
 
   @Override
   public List<NodeGroup> listDBNodeGroups(String dbName) throws MetaException, TException {
     return client.listDBNodeGroups(dbName);
   }
 
   @Override
   public boolean addTableNodeDist(String db, String tab, List<String> ng) throws MetaException,
       TException {
     return client.addTableNodeDist(db, tab, ng);
   }
 
   @Override
   public boolean deleteTableNodeDist(String db, String tab, List<String> ng) throws MetaException,
       TException {
     return client.deleteTableNodeDist(db, tab, ng);
   }
 
   @Override
   public List<NodeGroup> listTableNodeDists(String dbName, String tabName) throws MetaException,
       TException {
     return client.listTableNodeDists(dbName, tabName);
   }
 
   @Override
   public boolean assiginSchematoDB(String dbName, String schemaName,
       List<FieldSchema> fileSplitKeys, List<FieldSchema> part_keys, List<NodeGroup> ngs)
       throws InvalidObjectException, NoSuchObjectException, MetaException, TException {
     return client.assiginSchematoDB(dbName, schemaName, fileSplitKeys, part_keys, ngs);
   }
 
   @Override
   public List<NodeGroup> listNodeGroups(List<String> ngNames) throws MetaException, TException {
     return client.listNodeGroupByNames(ngNames);
   }
 
   @Override
   public List<GeoLocation> getGeoLocationByNames(List<String> geoLocNames) throws MetaException,
       TException {
     return client.getGeoLocationByNames(geoLocNames);
   }
 
   @Override
   public List<Node> listNodes() throws MetaException, TException {
     return client.listNodes();
   }
 
   @Override
   public boolean addUserAssignment(String userName, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.addUserAssignment(userName, dbName);
   }
 
   @Override
   public boolean deleteUserAssignment(String userName, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.deleteUserAssignment(userName, dbName);
   }
 
   @Override
   public List<User> listUsers() throws MetaException, TException {
     return client.listUsers();
   }
 
   @Override
   public boolean addRoleAssignment(String roleName, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.addRoleAssignment(roleName, dbName);
   }
 
   @Override
   public boolean deleteRoleAssignment(String roleName, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.deleteRoleAssignment(roleName, dbName);
   }
 
   @Override
   public List<Role> listRoles() throws MetaException, TException {
     return client.listRoles();
   }
 
   @Override
   public boolean addNodeGroupAssignment(NodeGroup ng, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.addNodeGroupAssignment(ng, dbName);
   }
 
   @Override
   public boolean deleteNodeGroupAssignment(NodeGroup ng, String dbName) throws MetaException,
       NoSuchObjectException, TException {
     return client.deleteNodeGroupAssignment(ng, dbName);
   }
 
   @Override
   public String getNodeInfo() throws MetaException, TException {
     return client.getNodeInfo();
   }
 
   @Override
   public boolean migrate_in(Table tbl, Map<Long, SFile> files, List<Index> idxs, String from_db,
       String to_devid, Map<Long, SFileLocation> fileMap) throws MetaException, TException {
     assert tbl != null;
     assert files != null;
     assert idxs != null;
     assert from_db != null;
     assert to_devid != null;
     assert fileMap != null;
     return client.migrate_in(tbl, files, idxs, from_db, to_devid, fileMap);
   }
 
   @Override
   public List<SFileLocation> migrate_stage1(String dbName, String tableName, List<Long> files,
       String to_db) throws MetaException, TException {
     assert dbName != null;
     assert tableName != null;
     assert files != null;
     assert to_db != null;
     return client.migrate_stage1(dbName, tableName, files, to_db);
   }
 
   @Override
   public boolean migrate_stage2(String dbName, String tableName, List<Long> files, String from_db,
       String to_db, String to_devid, String user, String password) throws MetaException, TException {
     assert dbName != null;
     assert tableName != null;
     assert files != null;
     assert from_db != null;
     assert to_db != null;
     assert to_devid != null;
     assert user != null;
     assert password != null;
     return client.migrate_stage2(dbName, tableName, files, from_db, to_db, to_devid, user, password);
   }
 
   @Override
   public void truncTableFiles(String dbName, String tabName) throws MetaException, TException {
     assert dbName != null;
     assert tabName != null;
     client.truncTableFiles(dbName, tabName);
   }
 
   @Override
   public String pingPong(String str) throws MetaException, TException {
     assert str != null;
     return client.pingPong(str);
   }
 
   @Override
   public List<Long> listFilesByDigest(String digest) throws MetaException, TException {
     assert digest != null;
     return client.listFilesByDigest(digest);
   }
 
   @Override
   public SFile create_file_by_policy(CreatePolicy policy, int repnr, String db_name,
       String table_name, List<SplitValue> values) throws FileOperationException, TException {
     assert policy != null;
     assert db_name != null;
     assert table_name != null;
     assert values != null;
     return client.create_file_by_policy(policy, repnr, db_name, table_name, values);
   }
 
   @Override
   public boolean reopen_file(long fid) throws FileOperationException, MetaException, TException {
     return client.reopen_file(fid);
   }
 
 }
