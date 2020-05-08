 package org.jboss.qa;
 
 import java.sql.Connection;
 import java.sql.SQLException;
import java.sql.Statement;
 
 import javax.annotation.Resource;
 import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
 import javax.sql.DataSource;
 
 @Stateless
 public class TestBean {
   @Resource(lookup = "java:jboss/datasources/TestDS")
   DataSource ds;
 
   public void go() {
     Connection c = null;
     
     try {
       c = ds.getConnection();
      
      System.out.println("Go - " + ds.toString() + " - " + c.toString());
      
      // String nativeCreate = c.nativeSQL("CREATE TABLE test (id int)");
      String nativeInsert = c.nativeSQL("INSERT INTO test (id) VALUES (3)");
      Statement statement = c.createStatement();
      statement.execute(nativeInsert);
      
     } catch (SQLException e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     } finally {
       try {
         c.close();
       } catch(SQLException e2) {
         e2.printStackTrace();
       }
     }
     
   }
 }
