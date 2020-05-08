 package com.datastax.drivers.jdbc.pool.cassandra.dao;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 
 import com.datastax.drivers.jdbc.pool.cassandra.AbstractBaseTest;
 
 
 public class CassandraServiceImplTest extends AbstractBaseTest {
     
    private static String keySpace = "default";
     
     private CassandraService cassandraService;
     
     @Autowired
     public void setCassandraService(CassandraService cassandraService) {
         this.cassandraService = cassandraService;
     }
 
     @Before
     public void setup() throws Exception {
         Assert.assertNotNull(cassandraService);
         Assert.assertNotNull(ctx);
 
         String dropKS = "DROP KEYSPACE " + keySpace;
         try {
             jdbcTemplate.execute(dropKS);
         } catch (DataAccessException e) {
           // Let's skip it for now as it can be due to the keyspace does not exist.
           // If there is a serious issue with the connection it will become obvious
           // during the CREATE KEYSPACE.
         }
 
         String createKS = "CREATE KEYSPACE " + keySpace +
                             " WITH strategy_class =  'org.apache.cassandra.locator.SimpleStrategy' " + 
                             " AND strategy_options:replication_factor=1";
         
         jdbcTemplate.execute(createKS);
         
         String usersCFCreation = "CREATE COLUMNFAMILY users (KEY int PRIMARY KEY);";
         jdbcTemplate.execute(usersCFCreation);
     }
     
     @Test
     public void test() throws Exception {
         System.out.println("Done!");
     }
     
     @Test
     public void testDB() throws Exception {
         
     }
    
 
 }
