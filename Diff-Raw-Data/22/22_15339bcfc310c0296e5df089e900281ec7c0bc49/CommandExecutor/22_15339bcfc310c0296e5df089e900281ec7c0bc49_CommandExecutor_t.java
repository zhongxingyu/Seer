 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.HashSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
 
 /**
  * User: Ivan Lyutov
  * Date: 11/15/12
  * Time: 3:26 PM
  */
 public class CommandExecutor implements Runnable {
     final static int taskLimit = 3;
 
     @Override
     public void run() {
         DataSource dataSource = new ComboPooledDataSource();
         Connection connection = null;
         PreparedStatement selectStatement = null;
         PreparedStatement updateStatement = null;
         try {
             try {
                 connection = dataSource.getConnection();
                 connection.setAutoCommit(false);
                selectStatement = connection.prepareStatement("select id, name, status from commands " +
                                                               "where status='NEW' limit " + taskLimit + " for update");
                 updateStatement = connection.prepareStatement("update commands set status=? WHERE id=?");
                 ExecutorService executor = Executors.newCachedThreadPool();
                 HashSet<Command> commands = new HashSet<Command>(taskLimit);
                 ResultSet resultSet = null;
                 while(true) {
                     resultSet =  selectStatement.executeQuery();
                     if (!resultSet.isBeforeFirst()) {
                         break;
                     }
 
                     while (resultSet.next()) {
                         commands.add( new Command(resultSet.getInt("id"),
                                                   resultSet.getString("name"),
                                                   Command.Status.valueOf(resultSet.getString("status"))) );
                         updateStatement.setString(1, Command.Status.IN_PROGRESS.toString());
                         updateStatement.setInt(2, resultSet.getInt("id"));
                         updateStatement.executeUpdate();
                     }
                     connection.commit();
 
                     for(Command command : commands) {
                         executor.execute(command);
                     }
                     commands.clear();
                 }
                 executor.shutdown();

                while (!executor.isTerminated()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
             } finally {
                 if (connection != null) {
                     connection.close();
                 }
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         }
     }
 }
