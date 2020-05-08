 package net.thumbtack.research.nosql.clients;
 
 
 import com.aerospike.client.*;
 import com.aerospike.client.policy.ClientPolicy;
 import com.aerospike.client.policy.WritePolicy;
 import net.thumbtack.research.nosql.Configurator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.ByteBuffer;
 
 public class AerospikeClientDB implements Database {
     private static final String DEFAULT_HOST = "localhost";
     private static final int DEFAULT_PORT = 3000;
 
     private static final int DEFAULT_TIMEOUT = 3000;
     private static final int DEFAULT_RETRIES = 3;
     private static final int DEFAULT_SLEEP_BETWEEN_RETRIES = 10;
 
     private static final String NAMESPACE_PROPERTY = "aerospike.nameSpace";
     private static final String DEFAULT_NAMESPACE = "test";
     private static final String SET_NAME_PROPERTY = "aerospike.setName";
     private static final String DEFAULT_SET_NAME = "test";
     private static final String COLUMN_NAME_PROPERTY = "aerospike.columnName";
     private static final String DEFAULT_COLUMN_NAME = "column_name";
 
     private static final Logger log = LoggerFactory.getLogger(AerospikeClientDB.class);
 
     private AerospikeClient client;
     private String nameSpace;
     private String setName;
     private WritePolicy writePolicy;
     private String columnName;
 
     @Override
     public void init(Configurator configurator) {
         try {
             client = new AerospikeClient(
                     new ClientPolicy(),
                     configurator.getNextDbHost(DEFAULT_HOST),
                     configurator.getDbPort(DEFAULT_PORT)
             );
             nameSpace = configurator.getString(NAMESPACE_PROPERTY, DEFAULT_NAMESPACE);
             setName = configurator.getString(SET_NAME_PROPERTY, DEFAULT_SET_NAME);
             columnName = configurator.getString(COLUMN_NAME_PROPERTY, DEFAULT_COLUMN_NAME);
 
             writePolicy = new WritePolicy();
             writePolicy.timeout = DEFAULT_TIMEOUT;
             writePolicy.maxRetries = DEFAULT_RETRIES;
             writePolicy.sleepBetweenRetries = DEFAULT_SLEEP_BETWEEN_RETRIES;
         } catch (AerospikeException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void write(String key, ByteBuffer value) {
         try {
             client.put(writePolicy, createKey(key), new Bin(columnName, value.array()));
         } catch (AerospikeException e) {
            log.error(e.toString());
            throw new RuntimeException(e);
         }
     }
 
     @Override
     public ByteBuffer read(String key) {
         try {
             Record record = client.get(writePolicy, createKey(key));
             byte[] value = (byte[]) record.bins.get(columnName);
             return ByteBuffer.wrap(value);
         } catch (AerospikeException e) {
             log.error(e.toString());
             throw new RuntimeException(e);
         } catch (NullPointerException e) {
         }
         return null;
     }
 
     @Override
     public void close() throws Exception {
         client.close();
     }
 
     private Key createKey(String key) throws AerospikeException {
         return new Key(nameSpace, setName, key);
     }
 }
