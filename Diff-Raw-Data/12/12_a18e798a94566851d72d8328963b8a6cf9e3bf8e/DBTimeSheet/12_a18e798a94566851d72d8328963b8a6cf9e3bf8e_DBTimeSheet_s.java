 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 import models.Client;
 import models.TimeSheet;
 import models.User;
 
 public class DBTimeSheet implements IFDBTimeSheet
 {
 	
 	private DataAccess _da;
 	public DBTimeSheet()
 	{
 		_da = DataAccess.getInstance();
 	}
 	
 
 	/**
 	 * Get all the TimeSheets from the database
 	 * 
 	 * @return ArrayList<TimeSheet>  
 	 */
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheets() throws Exception
     {
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets");
 		_da.setSqlCommandText(query);
 		ResultSet timeSheets = _da.callCommandGetResultSet();
 		
 		while (timeSheets.next())
 		{
 			TimeSheet timeSheet = buildTimeSheet(timeSheets);
 			returnList.add(timeSheet);
 		}
 
 		return returnList;
 	}
 	
 
 	/**
 	 * Get a specific TimeSheet record by id
 	 * 
 	 * @param sheetId						the id of the record to be returned
 	 * @return TimeSheet
 	 */
 	@Override
 	public TimeSheet getTimeSheetById(int sheetId) throws Exception
     {
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE sheetId = ?");
 		query.setInt(1, sheetId);
 		_da.setSqlCommandText(query);
 		ResultSet timeSheetResult = _da.callCommandGetRow();
 
         if(timeSheetResult.next())
             return buildTimeSheet(timeSheetResult);
 
         return null;
 	}
 
 
 	/**
 	 * Retrieve all TimeSheet records assigned to a specific user
 	 * 
 	 * @param user							the user to find TimeSheet records for
 	 * @return ArrayList<TimeSheet>
 	 */
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsByUser(User user) throws Exception
     {
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE userId = ?");
 		_da.setSqlCommandText(query);
 		ResultSet timeSheets = _da.callCommandGetResultSet();
 		
 		while (timeSheets.next())
 		{
 			TimeSheet timeSheet = buildTimeSheet(timeSheets);
 			returnList.add(timeSheet);
 		}
 		
 		return returnList;
 	}
 	
 
 	/**
 	 * Retrieve all TimeSheet records connected to a specific client
 	 * 
 	 * @param client							the client to find TimeSheet records for
 	 * @return ArrayList<TimeSheet>
 	 */
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsByClient(Client client) throws Exception
     {
 		ArrayList<TimeSheet>  returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE clientId = ?");
 		_da.setSqlCommandText(query);
 		ResultSet timeSheets = _da.callCommandGetResultSet();
 		
 		while (timeSheets.next())
 		{
 			TimeSheet timeSheet = buildTimeSheet(timeSheets);
 			returnList.add(timeSheet);
 		}
 		
 		return returnList;
 	}
 
 	
 	/**
 	 * Insert a new TimeSheet into the database
 	 * 
 	 * @param timeSheet							the object containing information to be stored
 	 * @return 									returns the number of rows affected
 	 */
 	@Override
 	public int insertTimeSheet(TimeSheet timeSheet) throws Exception
     {
 		if (timeSheet == null)
 		    return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO TimeSheets (userId, clientId, note, creationDate, editedDate) VALUES (?, ?, ?, ?, ?)");
 		query.setInt(1, timeSheet.getUser().getUserId());
 		query.setInt(2, timeSheet.getClient().getClientId());
 		query.setString(3, timeSheet.getNote());
 		query.setDate(4, (java.sql.Date)timeSheet.getCreationDate());
 		query.setDate(5, (java.sql.Date)timeSheet.getEditedDate());
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 
 	
 	/**
 	 * Update a TimeSheet already existing in the database
 	 * 
 	 * @param timeSheet 							the object containing the updated information you need to store
 	 * @return int									returns the number of rows affected
 	 */
 	@Override
 	public int updateTimeSheet(TimeSheet timeSheet) throws Exception
     {
 		if (timeSheet == null)
 			return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("UPDATE TimeSheets SET userId = ?, clientId = ?, note = ?, creationDate = ?, editedDate = ? WHERE sheetId = ?");
 		query.setInt(1, timeSheet.getUser().getUserId());
 		query.setInt(2, timeSheet.getClient().getClientId());
 		query.setString(3, timeSheet.getNote());
 		query.setDate(4, (java.sql.Date)timeSheet.getCreationDate());
 		query.setDate(5, (java.sql.Date)timeSheet.getEditedDate());
         query.setInt(6, timeSheet.getSheetId());
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 
 	
 	/**
 	 * Deletes a TimeSheet existing in the database
 	 * 
 	 * @param timeSheet 							the object containing the TimeSheet that you need to delete from the database
 	 * @return										returns the number of rows affected
 	 */
 	@Override
 	public int deleteTimeSheet(TimeSheet timeSheet) throws Exception
     {
 		if (timeSheet == null)
 		    return 0;
 		
 		int rowsAffected = 0;
 		PreparedStatement query = _da.getCon().prepareStatement("DELETE FROM TimeSheets WHERE sheetId = ?");
 		query.setInt(1, timeSheet.getSheetId());
 		_da.setSqlCommandText(query);
 		rowsAffected += _da.callCommand();
 		
 		return rowsAffected;
 	}
 
     /**
      * Retrieves all TimeSheets by user between startDate and endDate
      *
      * @param user						the user whose TimeSheets are assigned to
      * @param startDate					the first date of the date interval
      * @param endDate					the last date of the date interval
      * @return	ArrayList<TimeSheet>
      * @throws Exception
      */
     public ArrayList<TimeSheet> getAllTimeSheetsByUser(User user, Date startDate, Date endDate) throws Exception
     {
 		ArrayList<TimeSheet> filterList = new ArrayList<TimeSheet>();
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
		PreparedStatement query = _da.getCon().prepareStatement("SELECT FROM TimeSheets WHERE userId = ?");
 		_da.setSqlCommandText(query);
 		ResultSet timesheets = _da.callCommandGetResultSet();
 		
 		while (timesheets.next()) 
 		{
 			TimeSheet timeSheet = buildTimeSheet(timesheets);
 			filterList.add(timeSheet);
 		}
 		
 		int index = 0;
 		while (index < filterList.size())
 		{
 			if (filterList.get(index).getCreationDate().compareTo(startDate) > 0 && filterList.get(index).getEditedDate().compareTo(endDate) < 0) {
 				returnList.add(filterList.get(index));
 			}
 		}
 		return returnList;
     }

 
     /**
      * Retrieves all TimeSheets by client between startDate and endDate
      *
      * @param client					the client whose TimeSheets are assigned to
      * @param startDate					the first date of the date interval
      * @param endDate					the last date of the date interval
      * @return	ArrayList<TimeSheet>
      * @throws Exception
      */
     public ArrayList<TimeSheet> getAllTimeSheetsByClient(Client client, Date startDate, Date endDate) throws Exception
     {
     	ArrayList<TimeSheet> filterList = new ArrayList<TimeSheet>();
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
		PreparedStatement query = _da.getCon().prepareStatement("SELECT FROM TimeSheets WHERE clientId = ?");
 		_da.setSqlCommandText(query);
 		ResultSet timesheets = _da.callCommandGetResultSet();
 		
 		while (timesheets.next()) 
 		{
 			TimeSheet timeSheet = buildTimeSheet(timesheets);
 			filterList.add(timeSheet);
 		}
 		
 		int index = 0;
 		while (index < filterList.size())
 		{
 			if (filterList.get(index).getCreationDate().compareTo(startDate) > 0 && filterList.get(index).getEditedDate().compareTo(endDate) < 0) {
 				returnList.add(filterList.get(index));
 			}
 		}
 		return returnList;
     }
 
 	
 	private TimeSheet buildTimeSheet(ResultSet row) throws Exception
 	{
 		if (row == null)
 			return null;
 
         DBUser dbu = new DBUser();
         DBClient dbc = new DBClient();
 
 		int sheetId = row.getInt("sheetId");
 		User user = dbu.getUserById(row.getInt("userId"));
 		Client client = dbc.getClientById(row.getInt("clientId"));	
 		String note = row.getString("note");
 		Date creationDate = row.getDate("creationDate");
 		Date editedDate = row.getDate("editedDate");
 		
 		return new TimeSheet(sheetId, user, client, note, creationDate, editedDate);
 	}
 }
