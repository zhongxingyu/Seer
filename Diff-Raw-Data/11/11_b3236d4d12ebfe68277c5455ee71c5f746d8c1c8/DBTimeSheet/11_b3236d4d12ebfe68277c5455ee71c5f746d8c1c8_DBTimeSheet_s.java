 package db;
 
 import models.Client;
 import models.DataEntry;
 import models.TimeSheet;
 import models.User;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class DBTimeSheet implements IFDBTimeSheet
 {
 	private DataAccess _da;
     private String _sortExpression = "";
 
     public String getSortExpression()
     { return _sortExpression; }
     public void setSortExpression(String value)
     { _sortExpression = value; }
 
 	public DBTimeSheet()
 	{
 		_da = DataAccess.getInstance();
 	}
 
     public DBTimeSheet(String sortExpression)
     {
         _da = DataAccess.getInstance();
         _sortExpression = sortExpression;
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
 		
		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets" + _sortExpression);
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
		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE sheetId = ?" + _sortExpression);
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
 		
		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE userId = ?" + _sortExpression);
 		query.setInt(1, user.getUserId());
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
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE userId = ? AND creationDate BETWEEN = ? AND = ? " + _sortExpression);
 		query.setInt(1, user.getUserId());
 		query.setString(2, _da.dateToSqlDate(startDate));
 		query.setString(3, _da.dateToSqlDate(endDate));
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
 		
		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE clientId = ?" + _sortExpression);
 		query.setInt(1, client.getClientId());
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
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets WHERE clientId = ? AND creationDate BETWEEN = ? AND = ? " + _sortExpression);
         query.setInt(1, client.getClientId());
         query.setString(2, _da.dateToSqlDate(startDate));
         query.setString(3, _da.dateToSqlDate(endDate));
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
 		
 		PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO TimeSheets (caseId, userId, clientId, note, creationDate, editedDate) VALUES (?, ?, ?, ?, ?, ?)");
         query.setString(1, timeSheet.getCaseId());
 		query.setInt(2, timeSheet.getUser().getUserId());
 		query.setInt(3, timeSheet.getClient().getClientId());
 		query.setString(4, timeSheet.getNote());
 		query.setString(5, _da.dateToSqlDate(timeSheet.getCreationDate()));
 		query.setString(6, _da.dateToSqlDate(timeSheet.getEditedDate()));
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
 		
 		PreparedStatement query = _da.getCon().prepareStatement("UPDATE TimeSheets SET caseId = ?, userId = ?, clientId = ?, note = ?, editedDate = ? WHERE sheetId = ?");
         query.setString(1, timeSheet.getCaseId());
         query.setInt(2, timeSheet.getUser().getUserId());
 		query.setInt(3, timeSheet.getClient().getClientId());
 		query.setString(4, timeSheet.getNote());
 		query.setString(5, _da.dateToSqlDate(timeSheet.getEditedDate()));
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
 
 	
 	private TimeSheet buildTimeSheet(ResultSet row) throws Exception
 	{
 		if (row == null)
 			return null;
 
         DBUser dbu = new DBUser();
         DBClient dbc = new DBClient();
         DBDataEntry dbDataEntry = new DBDataEntry();
 
 		int sheetId = row.getInt("sheetId");
         String caseId = row.getString("caseId");
 		User user = dbu.getUserById(row.getInt("userId"));
 		Client client = dbc.getClientById(row.getInt("clientId"));	
 		String note = row.getString("note");
 		Date creationDate = row.getDate("creationDate");
 		Date editedDate = row.getDate("editedDate");
 		
 		TimeSheet timeSheet = new TimeSheet(sheetId, caseId, user, client, note, creationDate, editedDate);
         ArrayList<DataEntry> dataEntries = dbDataEntry.getAllDataEntriesByTimeSheet(timeSheet);
         timeSheet.setDataEntries(dataEntries);
 
         return timeSheet;
 	}
 }
