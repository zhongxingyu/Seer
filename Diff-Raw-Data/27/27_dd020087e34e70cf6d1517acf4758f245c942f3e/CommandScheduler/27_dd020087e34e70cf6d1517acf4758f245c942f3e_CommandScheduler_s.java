 package commands;
 
 import commands.abstracts.Command;
 import commands.abstracts.CommandFactory;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ExecutorService;
 
 /**
  * User: Ivan Lyutov
  * Date: 11/25/12
  * Time: 1:21 AM
  */
 public class CommandScheduler implements Runnable {
    private static final int commandLimit = 100;
    private static final String selectFormat = "select id, name, status from commands " +
            "where status='%s' limit  %s for update";
    private static final String updateFormat = "update commands set status='%s' where id in(%s)";
     private DataSource dataSource;
     private ExecutorService commandsPool;
     private CommandFactory commandFactory;
     private Log logger;
 
     public CommandScheduler(DataSource dataSource, ExecutorService commandsPool, CommandFactory commandFactory) {
         this.dataSource = dataSource;
         this.commandsPool = commandsPool;
         this.commandFactory = commandFactory;
     }
 
     public CommandScheduler(DataSource dataSource, ExecutorService commandsPool, CommandFactory commandFactory, Log logger) {
         this.dataSource = dataSource;
         this.commandsPool = commandsPool;
         this.commandFactory = commandFactory;
         this.logger = logger;
     }
 
     @Override
     public void run() {
         ResultSet resultSet = null;
         try(Connection connection = dataSource.getConnection()) {
             connection.setAutoCommit(false);
            PreparedStatement selectStatement = connection.prepareStatement(String.format(selectFormat,
                                                                                           CommandOne.Status.NEW,
                                                                                          commandLimit));
             Statement updateStatement = connection.createStatement();
             List<Integer> ids = null;
             Queue<Command> taskQueue = null;
             Command command = null;
             while (true) {
                 resultSet = selectStatement.executeQuery();
                 //TODO: real world exiting conditions
                 if (!resultSet.isBeforeFirst()) {
                     break;
                 }
                 ids = new ArrayList<Integer>();
                 taskQueue = new LinkedList<Command>();
                 while (resultSet.next()) {
                     command = commandFactory.createCommand(resultSet.getInt("id"),
                                                            resultSet.getString("name"),
                                                            Command.Status.valueOf(resultSet.getString("status")));
                     taskQueue.add(command);
                     ids.add(command.getId());
                 }
                updateStatement.executeUpdate(String.format(updateFormat,
                                                             Command.Status.IN_PROGRESS,
                                                             StringUtils.join(ids.toArray(), ",")));
                 connection.commit();
                 while (!taskQueue.isEmpty()) {
                     commandsPool.execute(taskQueue.remove());
                 }
             }
         } catch (SQLException e) {
             if (logger != null) {
                 logger.error(e.getMessage(), e);
             } else {
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
         }
     }
 }
