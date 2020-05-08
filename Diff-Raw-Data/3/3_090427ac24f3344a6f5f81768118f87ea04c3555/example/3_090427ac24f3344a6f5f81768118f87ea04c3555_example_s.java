 import java.sql.*;
 
 public class example
 {
     public static void main( String argv[] )
     {
         try
         {
          Class.forName("csql.jdbc.JdbcSqlDriver");
             // Connection handle
          Connection con = DriverManager.getConnection("jdbc:csql", "praba", "manager");
             // Statement handle
          PreparedStatement stmt = con.prepareStatement("UPDATE t1 set f2 = ? WHERE f1 = ?");
          for (int i =0 ; i< 10 ; i++) {
          stmt.setInt(1, i+200);
          stmt.setInt(2, i);
          stmt.executeUpdate();
          }
          stmt.close();
          con.commit();
          con.close();
          }catch(Exception e) {
            System.out.println("example: "+e);
             e.getStackTrace();
          }
     }
 }

         

