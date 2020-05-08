 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * User: Ivan Lyutov
  * Date: 11/15/12
  * Time: 3:06 PM
  */
 public class Command implements Runnable  {
     private int id;
     private String name;
     public Status status;
 
     public Command(int id, String name, Status status) {
         this.id = id;
         this.name = name;
         this.status = status;
     }
 
     public int getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public Status getStatus() {
         return status;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
 
     public void run() {
         ComboPooledDataSource dataSource = DataPool.getDataSource();
         try {
            System.out.println("executing " + name);
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("update commands set status='" + Status.DONE + "' where id=" + id);
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         }
     }
 
     public static enum Status {
         NEW("NEW"), IN_PROGRESS("IN_PROGRESS"), DONE("DONE");
 
         private String value;
 
         private Status(final String value) {
             this.value = value;
         }
     }
 }
