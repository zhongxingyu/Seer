 package com.seeburger.vfs2.provider.jdbctable.test;
 
 import static org.junit.Assert.fail;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.h2.jdbcx.JdbcDataSource;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import com.googlecode.flyway.core.Flyway;
import com.seeburger.vfs2.provider.jdbctable.JdbcDialectBase;
 
 
 public class SimpleH2Test extends SimpleTestsBase
 {
     static boolean skip = false;
 
     @BeforeClass
     public static void setupDatabase() throws SQLException
     {
         System.out.println("Starting H2 database");
         JdbcDataSource ds = new JdbcDataSource();
         ds.setUser("VFSTEST");
         ds.setPassword("secret");
         // make sure mem database is not anonymous and will not get droped on conenction close
         ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;JMX=TRUE");
 
         Flyway flyway = new Flyway();
         flyway.setDataSource(ds);
         flyway.setLocations("db/migration/h2_derby");
         flyway.setValidateOnMigrate(true);
         flyway.setCleanOnValidationError(true);
         flyway.migrate();
 
         Connection c = ds.getConnection();
         DatabaseMetaData md = c.getMetaData();
         ResultSet rs = md.getTables(null, null, null, new String[] { "TABLE" });
         while(rs.next())
         {
             System.out.println(rs.getString(1)+"|"+rs.getString(2)+"|"+rs.getString(3)+"|"+rs.getString(4));
         }
         rs.close(); c.close();
 
        SimpleTestsBase.dialect = new JdbcDialectBase("tBlobs", ds);
         SimpleTestsBase.dataSource = ds;
     }
 
     @AfterClass
     public static void destroyDatabase()
     {
         SimpleTestsBase.dataSource = null;
     }
 
     void verifyDatabase()
     {
         if (skip)
             return;
 
         Connection c = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try
         {
             c = dataSource.getConnection();
             ps = c.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.SESSIONS");
             rs = ps.executeQuery();
             int count = 0;
             while(rs.next())
             {
                 count++;
                 //System.out.println(" " + count + "  " + rs.getString(1)+"|"+rs.getString(2)+"|"+rs.getString(3)+"|"+rs.getString(4));
             }
 
             if (count > 1)
             {
                 skip = true;
                 fail("After this test there are " + count + " sessions active");
             }
         }
         catch (SQLException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         finally
         {
             safeClose(rs, ps, c);
         }
 
     }
 
     private void safeClose(ResultSet rs, PreparedStatement ps, Connection c)
     {
         try { rs.close(); } catch (Exception ignored) { }
         try { ps.close(); } catch (Exception ignored) { }
         try { c.close(); } catch (Exception ignored) { }
     }
 }
 
 
 
