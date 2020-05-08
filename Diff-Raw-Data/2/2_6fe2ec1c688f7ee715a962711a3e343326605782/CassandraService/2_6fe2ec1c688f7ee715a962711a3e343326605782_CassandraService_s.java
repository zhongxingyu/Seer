 package no.force.cassandra;
 
 import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.HConsistencyLevel;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.exceptions.HectorException;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.ColumnQuery;
 import me.prettyprint.hector.api.query.QueryResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.PostConstruct;
 
 @Service
 public class CassandraService {
 
 
     private static final Logger LOG = LoggerFactory.getLogger(CassandraService.class);
 
     public static final String HOST = "127.0.0.1:9160";
     public static final String CLUSTER_NAME = "Test Cluster";
 
     public static final String KEYSPACE_NAME = "Forcespace";
     private static final String COLUMN_FAMILY = "Transaction";
 
     private Cluster cluster = null;
     private Keyspace keyspace;
 
 
     @PostConstruct
     public void connect() {
         cluster = HFactory.getOrCreateCluster(CLUSTER_NAME, HOST);
         LOG.info("Cluster instantiated");
         ConfigurableConsistencyLevel ccl = new ConfigurableConsistencyLevel();
         ccl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
         keyspace = HFactory.createKeyspace(KEYSPACE_NAME, cluster, ccl);
     }
 
     public void disconnect() {
         cluster.getConnectionManager().shutdown();
     }
 
     public void execute() {
         LOG.info("Executed...");
         try {
             Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
 
 
             mutator.insert("001", COLUMN_FAMILY, HFactory.createStringColumn("name", "Sample transaction"));
 
 
             ColumnQuery<String, String, String> columnQuery = HFactory.createStringColumnQuery(keyspace);
            columnQuery.setColumnFamily(COLUMN_FAMILY).setKey("001").setName("Sample transaction");
             QueryResult<HColumn<String, String>> result = columnQuery.execute();
 
             System.out.println("Read HColumn from cassandra: " + result.get());
             System.out.println("Verify on CLI with:  get Keyspace1.Transaction['001'] ");
         } catch (HectorException e) {
             e.printStackTrace();
         }
 
     }
 
 }
