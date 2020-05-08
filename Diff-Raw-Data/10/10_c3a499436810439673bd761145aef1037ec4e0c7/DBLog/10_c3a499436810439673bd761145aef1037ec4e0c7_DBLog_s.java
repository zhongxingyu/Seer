 package db;
 
 import models.Log;
 import models.User;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * Created: 12-12-2012
  * @version: 0.1
  * Filename: DBLog.java
  * Description:
  * @changes
  */
 
 public class DBLog implements IFDBLog
 {
 
     private DataAccess _da;
     public DBLog()
     {
         _da = DataAccess.getInstance();
     }
 
     /**
      * Retrieve all log data from database
      *
      * @return ArrayList<Log>
      */
     @Override
     public ArrayList<Log> getAllLogs() throws Exception
     {
         ArrayList<Log> returnList = new ArrayList<Log>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM Logs");
         _da.setSqlCommandText(query);
         ResultSet logData = _da.callCommandGetResultSet();
 
         while(logData.next())
         {
             Log log = buildLog(logData);
             returnList.add(log);
         }
 
         return returnList;
     }
 
     /**
      * Get a specific log entry by user
      *
      * @param user the user which the exception occurred to
      * @return ArrayList<Log>
      */
     @Override
     public ArrayList<Log> getAllLogsByUser(User user) throws Exception
     {
         ArrayList<Log> returnList = new ArrayList<Log>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM Logs WHERE userId = ?");
         query.setInt(1, user.getUserId());
         _da.setSqlCommandText(query);
         ResultSet logData = _da.callCommandGetResultSet();
 
         while(logData.next())
         {
             Log log = buildLog(logData);
             returnList.add(log);
         }
 
         return returnList;
     }
 
     /**
      * Get a specific log entry by id
      *
      * @param value the id of the log entry you want returned
      * @return Log
      */
     @Override
     public Log getLogById(int value) throws Exception
     {
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM Logs WHERE logId = ?");
         query.setLong(1, value);
         _da.setSqlCommandText(query);
         ResultSet logResult = _da.callCommandGetRow();
 
         if(logResult.next())
             return buildLog(logResult);
 
         return null;
     }
 
     /**
      * Inserts a new log entry in the database
      *
      * @param logEntry the object containing the information you want stored
      * @return int                    returns the number of rows affected
      */
     @Override
     public int insertLog(Log logEntry) throws Exception
     {
         if(logEntry == null)
             return 0;
 
         PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO Logs (userId, userDetails, exception, exceptionLocation, createdDate) VALUES (?, ?, ?, ?, ?)");
 
         query.setInt(1, logEntry.getUser().getUserId());
         query.setString(2, logEntry.getUserDetails());
         query.setString(3, logEntry.getException());
         query.setString(4, logEntry.getExceptionLocation());
         query.setDate(5, (java.sql.Date) logEntry.getCreatedDate());
         _da.setSqlCommandText(query);
 
         return _da.callCommand();
     }
 
     private Log buildLog(ResultSet row) throws Exception
     {
         if(row == null)
             return null;
 
         int logId = row.getInt("logId");
         User user = dbUser.getUserById(row.getInt("userId"));
         String userDetails = row.getString("userDetails");
         String exception = row.getString("exception");
         String exceptionLocation = row.getString("exceptionLocation");
         Date createdDate = row.getDate("createdDate");
 
         return new Log(logId, user, userDetails, exception, exceptionLocation, createdDate);
     }
 }
