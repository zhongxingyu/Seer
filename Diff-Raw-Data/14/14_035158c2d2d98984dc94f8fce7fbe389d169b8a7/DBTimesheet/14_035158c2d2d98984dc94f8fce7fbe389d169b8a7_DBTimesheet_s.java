 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 import models.Client;
 import models.TimeSheet;
 import models.User;
 
 public class DBTimesheet implements IFDBTimesheet{
 	
 	private DataAccess _da;
 	
 	public DBTimesheet()
 	{
 		_da = DataAccess.getInstance();
 	}
 	
 
 	/**
 	 * Get all the timesheets from the database
 	 * 
 	 * @return ArrayList<TimeSheet>  
 	 */
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheets() throws Exception {
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM TimeSheets");
 		_da.setSqlCommandText(query);
 		ResultSet timesheets = _da.callCommandGetResultSet();
 		
 		while (timesheets.next()) 
 		{
 			TimeSheet timeSheet = buildTimeSheet(timesheets);
 			returnList.add(timeSheet);
 		}
 		return returnList;
 	}
 	
 
 	/**
 	 * Get a specific timesheet record by id
 	 * 
 	 * @param sheetId						the id of the record to be returned
 	 * @return Timesheet
 	 */
 	@Override
 	public TimeSheet getTimesheetById(int sheetId) throws Exception {
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT FROM TimeSheets WHERE sheetId = ?");
 		query.setInt(1, sheetId);
 		_da.setSqlCommandText(query);
 		ResultSet timeSheetResult = _da.callCommandGetRow();
 		timeSheetResult.next();
 		
 		return buildTimeSheet(timeSheetResult);
 	}
 
 
 	/**
 	 * Retrieve all timesheet records assigned to a specific user
 	 * 
 	 * @param User							the user to find timesheet records for
 	 * @return ArrayList<Timesheet>
 	 */
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsByUser(User user) throws Exception {
 		ArrayList<TimeSheet> returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT FROM TimeSheets WHERE userId = ?");
 		_da.setSqlCommandText(query);
 		ResultSet timesheets = _da.callCommandGetResultSet();
 		
 		while (timesheets.next()) 
 		{
 			TimeSheet timeSheet = buildTimeSheet(timesheets);
 			returnList.add(timeSheet);
 		}
 		
 		return returnList;
 	}
 	
 
 	/**
 	 * Retrieve all timesheet records connected to a specific client
 	 * 
 	 * @param Client							the client to find timesheet records for
 	 * @return ArrayList<Timesheet>
 	 */
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsByClient(Client client) throws Exception {
 		ArrayList<TimeSheet>  returnList = new ArrayList<TimeSheet>();
 		
 		PreparedStatement query = _da.getCon().prepareStatement("SELECT FROM TimeSheets WHERE clientId = ?");
 		_da.setSqlCommandText(query);
 		ResultSet timesheets = _da.callCommandGetResultSet();
 		
 		while (timesheets.next()) 
 		{
 			TimeSheet timeSheet = buildTimeSheet(timesheets);
 			returnList.add(timeSheet);
 		}
 		
 		return returnList;
 	}
 
 	
 	/**
 	 * Insert a new timesheet into the database
 	 * 
 	 * @param TimesSheet							the object containing information to be stored
 	 * @return 									returns the number of rows affected
 	 */
 	@Override
 	public int insertTimeSheet(TimeSheet timesheet) throws Exception {
 		if (timesheet == null)
 		return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO TimeSheets (sheetId, userId, clientId, note, createdDate, editedDate) VALUES (?, ?, ?, ?, ?, ?)");
 		query.setInt(1, timesheet.getSheetId());
 		query.setInt(2, timesheet.getUser().getUserId());
 		query.setInt(3, timesheet.getClient().getClientId());
		query.setString(1, timesheet.getNote());
		query.setDate(4, timesheet.getcreationDate();
		query.setDate(5, timesheet.getEditedDate());
 		_da.setSqlCommandText(query);

 		return _da.callCommand();
 	}
 
 	
 	/**
 	 * Update a timesheet already existing in the database
 	 * 
 	 * @param TimeSheet							the object containing the updated information you need to store
 	 * @return int									returns the number of rows affected
 	 */
 	@Override
 	public int updateTimeSheet(TimeSheet timesheet) throws Exception {
 		if (timesheet == null)
 			return 0;
 		
 		PreparedStatement query = _da.getCon().prepareStatement("UPDATE TimeSheets SET sheetId = ?, userId = ?, clientId = ?, note = ?, createdDate = ?, editedDate = ?");
 		query.setInt(1, timesheet.getSheetId());
 		query.setInt(2, timesheet.getUser().getUserId());
 		query.setInt(3, timesheet.getClient().getClientId());
 		query.setString(4, timesheet.getNote());
		query.setDate(5, timesheet.getcreationDate();
		query.setDate(6, timesheet.getEditedDate());
 		_da.setSqlCommandText(query);
 		
 		return _da.callCommand();
 	}
 
 	
 	/**
 	 * Deletes a timesheet existing in the database
 	 * 
 	 * @param TimeSheet							the object containing the timesheet that you need to delete from the database
 	 * @return										returns the number of rows affected
 	 */
 	@Override
 	public int deleteTimeSheet(TimeSheet timesheet) throws Exception {
 		if (timesheet == null)
 		return 0;
 		
 		int rowsAffected = 0;
 		PreparedStatement query = _da.getCon().prepareStatement("DELETE FROM TimeSheets WHERE sheetId = ?");
 		query.setInt(1, timesheet.getSheetId());
 		_da.setSqlCommandText(query);
 		rowsAffected += _da.callCommand();
 		
 		return rowsAffected;
 	}
 
 	
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsBeforeDataByUser(User user,
 			Date date) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsAfterDateByUser(User user,
 			Date date) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsBeforeDataByClient(
 			Client client, Date date) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ArrayList<TimeSheet> getAllTimeSheetsAfterDataByClient(
 			Client client, Date date) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	public TimeSheet buildTimeSheet(ResultSet row) throws Exception
 	{
 		if (row == null)
 			return null;
 		
 		int sheetId = row.getInt("sheetId");
 		DBUser dbu = new DBUser();
 		User user = dbu.getUserById(row.getInt("userId"));
 		DBClient dbc = new DBClient();
 		Client client = dbc.getClientById(row.getInt("clientId"));	
 		String note = row.getString("note");
 		Date createdDate = row.getDate("createdDate");
 		Date editedDate = row.getDate("editedDate");
 		
 		return new TimeSheet(sheetId, user, client, note, createdDate, editedDate);
 	}
 }
