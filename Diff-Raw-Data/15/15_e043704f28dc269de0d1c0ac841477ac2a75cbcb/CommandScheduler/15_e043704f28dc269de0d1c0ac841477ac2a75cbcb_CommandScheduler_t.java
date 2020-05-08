 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * User: Ivan Lyutov
  * Date: 11/25/12
  * Time: 1:21 AM
  */
 public class CommandScheduler implements Runnable {
     private static final int commandLimit = 10;
     private DataSource dataSource;
     private BlockingQueue<Runnable> commands;
 
     public CommandScheduler(DataSource dataSource, BlockingQueue<Runnable> commands) {
         this.commands = commands;
         this.dataSource = dataSource;
     }
 
     @Override
     public void run() {
         ResultSet resultSet = null;
         try {
             Connection connection = dataSource.getConnection();
             connection.setAutoCommit(false);
             PreparedStatement selectStatement = connection.prepareStatement("select id, name, status from commands " +
                                                                             "where status='" + Command.Status.NEW +
                                                                             "' limit " + commandLimit + " for update");
             Statement updateStatement = connection.createStatement();
             try {
                 while (true) {
                     resultSet = selectStatement.executeQuery();
                     if (!resultSet.isBeforeFirst()) {
                         break;
                     }
 
                     while (resultSet.next()) {
                        commands.add(new Command(resultSet.getInt("id"),
                                                  resultSet.getString("name"),
                                                  Command.Status.valueOf(resultSet.getString("status"))));
                         updateStatement.addBatch("update commands set status='" + Command.Status.IN_PROGRESS +
                                                  "' where id=" + resultSet.getInt("id"));
                     }
                     updateStatement.executeBatch();
                     connection.commit();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
     }
 }
