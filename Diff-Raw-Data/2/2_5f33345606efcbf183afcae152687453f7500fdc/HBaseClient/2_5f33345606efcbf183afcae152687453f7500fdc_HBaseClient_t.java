 package org.hbase.async;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.accumulo.core.Constants;
 import org.apache.accumulo.core.client.AccumuloException;
 import org.apache.accumulo.core.client.AccumuloSecurityException;
 import org.apache.accumulo.core.client.BatchDeleter;
 import org.apache.accumulo.core.client.BatchScanner;
 import org.apache.accumulo.core.client.BatchWriterConfig;
 import org.apache.accumulo.core.client.Connector;
 import org.apache.accumulo.core.client.Instance;
 import org.apache.accumulo.core.client.MultiTableBatchWriter;
 import org.apache.accumulo.core.client.MutationsRejectedException;
 import org.apache.accumulo.core.client.TableNotFoundException;
 import org.apache.accumulo.core.client.ZooKeeperInstance;
 import org.apache.accumulo.core.client.security.tokens.PasswordToken;
 import org.apache.accumulo.core.data.Key;
 import org.apache.accumulo.core.data.Mutation;
 import org.apache.accumulo.core.data.Range;
 import org.apache.accumulo.core.data.Value;
 import org.apache.curator.RetryPolicy;
 import org.apache.curator.framework.CuratorFramework;
 import org.apache.curator.framework.CuratorFrameworkFactory;
 import org.apache.curator.framework.recipes.atomic.AtomicValue;
 import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
 import org.apache.curator.retry.ExponentialBackoffRetry;
 import org.apache.hadoop.io.Text;
 
 import com.stumbleupon.async.Deferred;
 
 public class HBaseClient {
 
   public static final byte[] EMPTY_ARRAY = new byte[0];
 
   private static final BatchWriterConfig batchWriterConfig = new BatchWriterConfig();
   
   String keepers = "localhost";
   String instanceId = "test";
   String user = "root";
   String password = "secret";
   
   private Connector conn;
   private CuratorFramework curator;
   private Map<String, DistributedAtomicLong> counters = new HashMap<String, DistributedAtomicLong>();
   private MultiTableBatchWriter mtbw;
   
   public HBaseClient(String zkq) {
     // TODO: get instance, user and password from zkq
     keepers = zkq;
     Instance instance = new ZooKeeperInstance(instanceId, keepers);
     try {
       conn = instance.getConnector(user, new PasswordToken(password.getBytes()));
       curator = CuratorFrameworkFactory.newClient(keepers, new ExponentialBackoffRetry(1000, 3));
       curator.start();
     } catch (AccumuloException e) {
       throw new RuntimeException(e);
     } catch (AccumuloSecurityException e) {
       throw new RuntimeException(e);
     }
   }
   
   public HBaseClient(String zkq, String string) {
       this(zkq);
   }
 
   // use curator/zookeeper to implement globally unique numbers
   synchronized public Deferred<Long> atomicIncrement(AtomicIncrementRequest atomicIncrementRequest) {
     String kind = new String(atomicIncrementRequest.getKind());
     DistributedAtomicLong counter = counters.get(kind);
     if (counter == null) {
       RetryPolicy policy = new ExponentialBackoffRetry(10, Integer.MAX_VALUE, 1000);
       counters.put(kind, counter = new DistributedAtomicLong(curator, zooPath("/counters/" + kind), policy));
     }
     AtomicValue<Long> increment;
     try {
       increment = counter.increment();
     } catch (Exception e) {
       return Deferred.fromError(e);
     }
     if (increment.succeeded())
       return Deferred.fromResult(increment.postValue());
     return Deferred.fromError(new Exception("failed to increment " + kind));
   }
   
   private String zooPath(String path) {
     return "/opentsdb" + path;
   }
   
   synchronized private void update(String table, Mutation m) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
     if (mtbw == null) 
       mtbw = conn.createMultiTableBatchWriter(batchWriterConfig);
     mtbw.getBatchWriter(table).addMutation(m);
   }
 
   // This just does an update, but the one place where it is called is only 
   // doing a double-check that the expected location is empty.
   public Deferred<Boolean> compareAndSet(PutRequest put, byte[] value) {
     try {
       update(put.getTable(), put.getMutation());
     } catch (Exception e) {
       return Deferred.fromError(e);
     }
     return Deferred.fromResult(new Boolean(true));
   }
 
   // the deleterow implementation is slow; could use iterators to make it efficient
   public Deferred<Object> delete(DeleteRequest request) {
     try {
       if (request.isDeleteRow()) {
         BatchDeleter deleter = conn.createBatchDeleter(request.getTable(), Constants.NO_AUTHS, batchWriterConfig.getMaxWriteThreads(), batchWriterConfig);
         deleter.setRanges(Collections.singletonList(new Range(new Text(request.getKey()))));
         deleter.delete();
       } else {
         update(request.getTable(), request.getDeleteMutation());
       }
     } catch (Exception e) {
       return Deferred.fromError(e);
     }
     return Deferred.fromResult(new Object());
   }
 
   public Scanner newScanner(byte[] table) {
     return new Scanner(table, conn);
   }
 
   public Deferred<ArrayList<KeyValue>> get(GetRequest get) {
     ArrayList<KeyValue> result = new ArrayList<KeyValue>();
     BatchScanner bs;
     try {
       bs = conn.createBatchScanner(get.getTable(), Constants.NO_AUTHS, 5);
     } catch (TableNotFoundException e) {
       return Deferred.fromError(e);  
     }
     try {
      bs.setRanges(Collections.singletonList(new Range(new Text(get.key()))));
       if (get.getFamily() != null || get.getQualifier() != null) {
         if (get.getQualifier() != null)
           bs.fetchColumn(new Text(get.getFamily()), new Text(get.getQualifier()));
         else
           bs.fetchColumnFamily(new Text(get.getFamily()));
       }
       for (Entry<Key,Value> entry : bs) {
         result.add(new KeyValue(entry));
       }
     } finally {
       bs.close();
     }
     return Deferred.fromResult(result);
   }
 
   public Deferred<Object> put(PutRequest put) {
     try {
       update(put.getTable(), put.getMutation());
       return Deferred.fromResult(new Object());
     } catch (Exception ex) {
       return Deferred.fromError(ex);
     }
   }
 
   public ClientStats stats() {
     return new ClientStats();
   }
 
   public Deferred<Object> flush() {
     if (mtbw != null)
       try {
         mtbw.flush();
       } catch (MutationsRejectedException e) {
        return Deferred.fromError(e);
       }
     return Deferred.fromResult(new Object());
   }
 
   public Deferred<Object> ensureTableExists(String table) {
     if (!conn.tableOperations().exists(table))
       try {
         conn.tableOperations().create(table);
       } catch (Exception e) {
         Deferred.fromError(e);
       }
     return Deferred.fromResult(new Object());
   }
 
   public Deferred<Object> shutdown() {
     try {
       if (mtbw != null)
         mtbw.close();
       mtbw = null;
       return Deferred.fromResult(new Object()); 
     } catch (MutationsRejectedException e) {
       return Deferred.fromError(e);
     }
   }
 
   public long getFlushInterval() {
     return batchWriterConfig.getMaxLatency(TimeUnit.MILLISECONDS);
   }
   
   public void setFlushInterval(short ms) {
     batchWriterConfig.setMaxLatency((long) ms, TimeUnit.MILLISECONDS);
   }
 
 }
