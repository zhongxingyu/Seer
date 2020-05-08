 //Test connection default rollback mechanism
 //close the connection without committing the transaction
 import java.sql.*;
 public class ConnTest1 
 {
     public static void main(String[] args) 
     {
        try
        {
            Class.forName("csql.jdbc.JdbcSqlDriver");
            Connection con = DriverManager.getConnection("jdbc:csql", "root", "manager");
            Statement cStmt = con.createStatement();
 	   int ret =0;
            cStmt.execute("CREATE TABLE T1 (f1 integer, f2 char (20));");
 		   
 	   PreparedStatement stmt = null;
            stmt = con.prepareStatement("INSERT INTO T1 VALUES (?, ?);");
            int count=0;
            for (int i =0 ; i< 10 ; i++) {
              stmt.setInt(1, i);
              stmt.setString(2, String.valueOf(i+100));
              ret = stmt.executeUpdate();
              if (ret !=1) break;
              count++;
            }
            con.close();
            System.out.println("Total tuples inserted "+ count);
 
            con = DriverManager.getConnection("jdbc:csql", "root", "manager");
 	   cStmt = con.createStatement();
            System.out.println("Listing tuples:");
 	   ResultSet rs = cStmt.executeQuery("SELECT * from T1;");
            count =0;
 	   while (rs.next())
 	   {
 	       System.out.println("Tuple value is " + rs.getInt(1)+ " "+ rs.getString(2));
                count++;
 	   }
            System.out.println("Total tuples selected "+ count);
 	   rs.close();
 	   con.commit();
            cStmt.execute("DROP TABLE T1;");
            con.close();
         }catch(Exception e) {
             System.out.println("Exception in Test: "+e);
             e.getStackTrace();
        }
     }
 }
