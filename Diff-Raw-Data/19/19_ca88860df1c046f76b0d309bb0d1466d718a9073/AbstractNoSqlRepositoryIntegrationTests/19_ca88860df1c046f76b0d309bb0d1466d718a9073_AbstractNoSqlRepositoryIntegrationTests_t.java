 package fr.soat.it;
 
 import java.io.IOException;
 
 import org.apache.cassandra.config.ConfigurationException;
 import org.apache.thrift.transport.TTransportException;
 import org.cassandraunit.DataLoader;
 import org.cassandraunit.dataset.json.ClassPathJsonDataSet;
 import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 import com.netflix.astyanax.AstyanaxContext;
 import com.netflix.astyanax.Keyspace;
 import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
 import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
 import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
 import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
 import com.netflix.astyanax.thrift.ThriftFamilyFactory;
 
 /**
  * @author Mouhcine MOULOU
  *
  */
 public abstract class AbstractNoSqlRepositoryIntegrationTests {
 	
 	@BeforeClass 
     public static void startCassandra() throws TTransportException, IOException, ConfigurationException {  
         EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml");  
     }  
 	
 	@Before  
     public void setUp() throws IOException,   
      TTransportException, ConfigurationException, InterruptedException  
     {  
         DataLoader dataLoader = new DataLoader("TestCluster",   
          "localhost:9160");  
         dataLoader.load(new ClassPathJsonDataSet("dataset.json"));  
   
 		AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
 				.forCluster("TestCluster")
 				.forKeyspace("test_keyspace")
 				.withAstyanaxConfiguration(
 						new AstyanaxConfigurationImpl()
 								.setDiscoveryType(NodeDiscoveryType.NONE))
 				.withConnectionPoolConfiguration(
 						new ConnectionPoolConfigurationImpl(
 								"testConnectionPool").setPort(9160)
 								.setMaxConnsPerHost(1)
 								.setSeeds("localhost:9160"))
 				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
 				.buildKeyspace(ThriftFamilyFactory.getInstance());
   
         context.start();
         
         // FIXME
        Keyspace keyspace = context.getEntity();  
//        cassandraAccessor accessor = new CassandraAccessor(new ObjectMapper(), keyspace);  
     }  
   
     @After
     public void clearCassandra() {  
         EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();  
     }  
   
     @AfterClass  
     public static void stopCassandra() {  
         EmbeddedCassandraServerHelper.stopEmbeddedCassandra();  
     }
 
 }
