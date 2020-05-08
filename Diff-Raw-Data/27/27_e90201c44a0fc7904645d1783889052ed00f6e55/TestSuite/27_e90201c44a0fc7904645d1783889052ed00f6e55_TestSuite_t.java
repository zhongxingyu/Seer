 package smail.cli.test;
 
 import com.netflix.astyanax.Keyspace;
 import com.netflix.astyanax.model.ConsistencyLevel;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.Test;
 
 // @author lyubentodorov
 // @licence - MIT
 // Available at http://lyuben.herokuapp.com/casstor/ 
 // Source at https://github.com/lyubent/CassTor/ 
 //
 public class TestSuite {
     
     private static final String __SEEDS__ = "134.36.36.188";
     private static final String __CLUSTER__ = "Test Cluster";
     private static final String __KEYSPACE__ = "TestKS";
     public static final String ANSI_PURPLE = "\u001B[35m";
     public static final String ANSI_GREEN = "\u001B[32m";
     
     @Test
     public void runTests(){
         SchemaTest schemaTester = new SchemaTest(getTestAstyanaxContext());
         QueryingTest queryTester = new QueryingTest(getTestAstyanaxContext());
         
         try {
             
             // Create the keyspace  WORKS
             org.junit.Assert.assertTrue(schemaTester.createKS());
             
             
             //Create the column family WORKS
             org.junit.Assert.assertTrue(schemaTester.createCF());
             
             
             //insert and email into cassandra
             String key = queryTester.insertEmail();
             org.junit.Assert.assertNotNull(key);
             
             
             //delete an email from cassandra
             //String key = "TestUsr@1359483724";
             org.junit.Assert.assertTrue(queryTester.deleteEmail(key));
             
             
             //drop the previously created column family
             org.junit.Assert.assertTrue(schemaTester.dropCF());
             
             
             //drop the previously created keyspace
             org.junit.Assert.assertTrue(schemaTester.dropKS());
                         
             System.out.println(ANSI_GREEN + "\t============\tTest:\tUnit tests PASSED\t==========\n\n");
             
         } catch(Exception ex){
             System.out.println(ANSI_PURPLE + "\t============\tTest:\tUnit tests FAILED\t============\n\n");
             Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     
     
     //Test context, we dont want to mess with the live data when testing.
     private Keyspace getTestAstyanaxContext(){
         
         com.netflix.astyanax.AstyanaxContext<Keyspace> context = 
                 new com.netflix.astyanax.AstyanaxContext.Builder()
         .forCluster(__CLUSTER__)
         .forKeyspace(__KEYSPACE__) //NetworkKS
         .withAstyanaxConfiguration(
          new com.netflix.astyanax.impl.AstyanaxConfigurationImpl()      
        .setDefaultReadConsistencyLevel(ConsistencyLevel.CL_ALL) // Data should be consistent
        .setDefaultWriteConsistencyLevel(ConsistencyLevel.CL_ALL)
         .setDiscoveryType(com.netflix.astyanax.connectionpool.NodeDiscoveryType.NONE) // NONE FOR BASIK KS
         .setCqlVersion("3.0.0")) //using CQL3 (fails, its still CQL2)
         .withConnectionPoolConfiguration(
          new com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl("MyConnectionPool")
         .setPort(9160)
         .setMaxConnsPerHost(10)
         .setSeeds(__SEEDS__))
         .withConnectionPoolMonitor(
          new com.netflix.astyanax.connectionpool.impl.Slf4jConnectionPoolMonitorImpl())
         .buildKeyspace(com.netflix.astyanax.thrift.ThriftFamilyFactory.getInstance());
         
         context.start();
         Keyspace ks = context.getEntity();
         
         //returns Keyspace obj
         return context.getEntity();
     }
 }
