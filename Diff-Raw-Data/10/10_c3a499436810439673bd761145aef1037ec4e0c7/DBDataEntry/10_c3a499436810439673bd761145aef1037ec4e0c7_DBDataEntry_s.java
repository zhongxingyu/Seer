 package db;
 
 import models.*;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * Created: 12-12-2012
  * @version: 0.1
  * Filename: DBDataEntry.java
  * Description:
  * @changes
  */
 
 public class DBDataEntry implements IFDBDataEntry
 {
 
     private DataAccess _da;
     public DBDataEntry()
     {
         _da = DataAccess.getInstance();
     }
 
     /**
      * Retrieve all DataEntries from database
      *
      * @return ArrayList<DataEntry>
      */
     @Override
     public ArrayList<DataEntry> getAllDataEntries() throws Exception
     {
         ArrayList<DataEntry> returnList = new ArrayList<DataEntry>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM DataEntries");
         _da.setSqlCommandText(query);
         ResultSet dataEntries = _da.callCommandGetResultSet();
 
         while(dataEntries.next())
         {
             DataEntry dataEntry = buildDataEntry(dataEntries);
             returnList.add(dataEntry);
         }
 
         return returnList;
     }
 
     /**
      * Retrieve all DataEntries from database
      *
      * @param user                    the User which the DataEntries belongs to
      * @return ArrayList<DataEntry>
      */
     @Override
     public ArrayList<DataEntry> getAllDataEntriesByUser(User user) throws Exception
     {
         ArrayList<DataEntry> returnList = new ArrayList<DataEntry>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM DataEntries WHERE userId = ?");
         query.setInt(1, user.getUserId());
         _da.setSqlCommandText(query);
         ResultSet dataEntries = _da.callCommandGetResultSet();
 
         while(dataEntries.next())
         {
             DataEntry dataEntry = buildDataEntry(dataEntries);
             returnList.add(dataEntry);
         }
 
         return returnList;
     }
 
     /**
      * Retrieve all DataEntries from database
      *
      * @param timeSheet               the TimeSheet which the DataEntries are associated to
      * @return ArrayList<DataEntry>
      */
     @Override
     public ArrayList<DataEntry> getAllDataEntriesByTimeSheet(TimeSheet timeSheet) throws Exception
     {
         ArrayList<DataEntry> returnList = new ArrayList<DataEntry>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM DataEntries WHERE sheetId = ?");
         query.setInt(1, timeSheet.getSheetId());
         _da.setSqlCommandText(query);
         ResultSet dataEntries = _da.callCommandGetResultSet();
 
         while(dataEntries.next())
         {
             DataEntry dataEntry = buildDataEntry(dataEntries);
             returnList.add(dataEntry);
         }
 
         return returnList;
     }
 
     /**
      * Retrieve all DataEntries from database
      *
      * @param task                    the Task which the DataEntries are associated to
      * @return ArrayList<DataEntry>
      */
     @Override
     public ArrayList<DataEntry> getAllDataEntriesByTask(Task task) throws Exception
     {
         ArrayList<DataEntry> returnList = new ArrayList<DataEntry>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM DataEntries WHERE taskId = ?");
         query.setInt(1, task.getTaskId());
         _da.setSqlCommandText(query);
         ResultSet dataEntries = _da.callCommandGetResultSet();
 
         while(dataEntries.next())
         {
             DataEntry dataEntry = buildDataEntry(dataEntries);
             returnList.add(dataEntry);
         }
 
         return returnList;
     }
 
     /**
      * Get a specific DataEntry by id
      *
      * @param value                   the id of the DataEntry you want returned
      * @return Log
      */
     @Override
     public DataEntry getDataEntryById(int value) throws Exception
     {
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM DataEntries WHERE entryId = ?");
         query.setInt(1, value);
         _da.setSqlCommandText(query);
         ResultSet entryResult = _da.callCommandGetRow();
 
         if(entryResult.next())
             return buildDataEntry(entryResult);
 
         return null;
     }
 
     /**
      * Inserts a new DataEntry in the database
      *
      * @param dataEntry               the object containing the information you want stored
      * @param sheetId                 the id of the TimeSheet which the DataEntry are associated to
      * @return int                    returns the number of rows affected
      */
     @Override
     public int insertDataEntry(DataEntry dataEntry, int sheetId) throws Exception
     {
         if(dataEntry == null || sheetId < 0)
             return 0;
 
         PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO DataEntries (sheetId, taskId, userId, startDate, endDate, entryRemark, creationDate, editedDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
 
         query.setInt(1, sheetId);
         query.setInt(2, dataEntry.getTask().getTaskId());
         query.setInt(3, dataEntry.getUser().getUserId());
         query.setDate(4, (java.sql.Date) dataEntry.getStartDate());
         query.setDate(5, (java.sql.Date) dataEntry.getEndDate());
         query.setString(6, dataEntry.getEntryRemark());
         query.setDate(7, (java.sql.Date) dataEntry.getCreationDate());
         query.setDate(8, (java.sql.Date) dataEntry.getEditedDate());
         _da.setSqlCommandText(query);
 
         return _da.callCommand();
     }
 
     /**
      * Update a existing DataEntry in database
      *
      * @param dataEntry               the object containing the updated information you want stored
      * @return int                    returns the number of rows affected
      */
     @Override
     public int updateDataEntry(DataEntry dataEntry) throws Exception
     {
         if(dataEntry == null)
             return 0;
 
         PreparedStatement query = _da.getCon().prepareStatement("UPDATE DataEntries SET taskId = ?, userId = ?, startDate = ?, endDate = ?, entryRemark = ?, editedDate = ? WHERE entryId = ?");
         query.setInt(1, dataEntry.getTask().getTaskId());
         query.setInt(2, dataEntry.getUser().getUserId());
         query.setDate(3, (java.sql.Date) dataEntry.getStartDate());
         query.setDate(4, (java.sql.Date) dataEntry.getEndDate());
         query.setString(5, dataEntry.getEntryRemark());
         query.setDate(6, (java.sql.Date) dataEntry.getEditedDate());
         query.setInt(7, dataEntry.getEntryId());
         _da.setSqlCommandText(query);
 
         return _da.callCommand();
     }
 
     /**
      * Delete an existing DataEntry from the database
      *
      * @param dataEntry               the object containing the DataEntry, which should be deleted from the database
      * @return int                    returns the number of rows affected
      */
     @Override
     public int deleteDataEntry(DataEntry dataEntry) throws Exception
     {
         if(dataEntry == null)
             return 0;
 
         PreparedStatement query = _da.getCon().prepareStatement("DELETE FROM DataEntries WHERE entryId = ?");
         query.setInt(1, dataEntry.getEntryId());
         _da.setSqlCommandText(query);
 
         return _da.callCommand();
     }
 
     private DataEntry buildDataEntry(ResultSet row) throws Exception
     {
         if(row == null)
             return null;
 
         int entryId = row.getInt("entryId");
         Task task = dbTask.getTaskById(row.getInt("taskId"));
         User user = dbUser.getUserById(row.getInt("userId"));
         Date startDate = row.getDate("startDate");
         Date endDate = row.getDate("endDate");
         String entryRemark = row.getString("entryRemark");
         Date creationDate = row.getDate("creationDate");
         Date editedDate = row.getDate("editedDate");
 
         return new DataEntry(entryId, task, user, startDate, endDate, entryRemark, creationDate, editedDate);
     }
 }
