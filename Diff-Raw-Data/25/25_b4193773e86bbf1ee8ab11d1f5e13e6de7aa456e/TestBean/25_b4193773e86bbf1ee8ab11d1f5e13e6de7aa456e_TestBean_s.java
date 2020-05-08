 package org.jboss.qa;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.annotation.Resource;
 import javax.ejb.Stateless;
 import javax.sql.DataSource;
 
 @Stateless
 public class TestBean {
   @Resource(lookup = "java:jboss/datasources/TestDS")
   DataSource ds;
 
   public void go() {
     Connection c = null;
     
     try {
      System.out.println("Go");
       c = ds.getConnection();
      // c.nativeSQL("CREATE TABLE TEST (id int)");
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
