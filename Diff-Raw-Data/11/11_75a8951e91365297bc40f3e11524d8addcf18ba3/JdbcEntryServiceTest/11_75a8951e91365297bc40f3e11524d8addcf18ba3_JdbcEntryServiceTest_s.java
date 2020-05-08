 package ca.cutterslade.edbafakac.db.jdbc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ca.cutterslade.edbafakac.db.Entry;
 
// CSOFF: NestedTryDepth
 public class JdbcEntryServiceTest {
 
   private JdbcEntryService service;
 
   @Before
   public void setUp() throws SQLException, IOException {
     service = new JdbcEntryService();
     service.createTable();
   }
 
   @After
   public void tearDown() throws SQLException {
     try {
       final Connection connection = service.getConnection();
       try {
         final Statement statement = connection.createStatement();
         try {
           statement.execute("SHUTDOWN");
         }
         finally {
           statement.close();
         }
       }
       finally {
         connection.close();
       }
     }
     finally {
       service.close();
     }
   }
 
   @Test
   public void testSetUp() throws SQLException {
     final Connection connection = service.getConnection();
     try {
       final Statement statement = connection.createStatement();
       try {
         statement.execute("select * from edbafakac.entries");
         final ResultSet resultSet = statement.getResultSet();
         try {
           final ResultSetMetaData metaData = resultSet.getMetaData();
           assertEquals(3, metaData.getColumnCount());
           assertEquals("ENTRY_KEY", metaData.getColumnName(1));
           assertEquals("PROPERTY_KEY", metaData.getColumnName(2));
           assertEquals("PROPERTY_VALUE", metaData.getColumnName(3));
           assertFalse(resultSet.next());
         }
         finally {
           resultSet.close();
         }
       }
       finally {
         statement.close();
       }
     }
     finally {
       connection.close();
     }
   }
 
   @Test
   public void testEmptyEntry() {
     Entry entry = service.getNewEntry();
     service.saveEntry(entry);
     entry = service.getEntry(entry.getKey());
     assertTrue(entry.getProperties().isEmpty());
   }
 
 }
