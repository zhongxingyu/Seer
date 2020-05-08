 package com.github.btarb24.NetherTest;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Logger;
 import java.sql.*;
 
 import org.bukkit.entity.Player;
 
 public class DbAccess 
 {
 	private final String DB_MINS = "minutesUsed";
 	private final String DB_TIME = "lastActivity";
 	private final String DB_NAME = "name";
 	private final String DB_URL = "jdbc:mysql://127.0.0.1:3306/nether?user=root&password=imdeity";
 	
 	private Connection _connection = null; //the mysql db connection
 	private Statement _statement = null;   //the db statement to reuse
 	private Logger _logger = null;         //logger from the main class
 	
 	public DbAccess(Logger logger)
 	{
 		_logger = logger;
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");	
 			_connection = DriverManager.getConnection (DB_URL);
 			_statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		}
 		catch (ClassNotFoundException e) { _logger.severe(e.getMessage()); }
 		catch (SQLException e) {  _logger.severe(e.getMessage()); }
 	}
 	
 	/* checks the db to see if they have proper permissions and if they
 	 * have time remaining to enter the nether.  Throws db exceptions
 	 * to allow for sensible retries.
 	 */
 	public boolean canEnter(Player player) throws SQLException
 	{
 		//TODO: check and/or disable use of /chest?
 		
 		//get the player's record from the db
 		ResultSet rs = retrieveRecord(player);
 		
 		//get the last activity time
 		Calendar cal = getTimestampFromDb(rs);
 		
 		//add the max frequency to the last activity time
 		cal.add(Calendar.HOUR, NetherTest.ENTRANCE_FREQUENCY);
 		Calendar currentTime = Calendar.getInstance(); 
 		
 		//amount of minutes they've used (only loaded if they're still within the a session group)
 		int minutes = 0;
 		
 		if (cal.before(currentTime))
 		{//the user gets to start a completely new set of sessions
 			
 			//reset minutes used to 0 since they're just starting a new session group
 			rs.updateInt(DB_MINS, 0);
 			//update lastActivity to the current time
 			updateTimestamp(rs);
 			
 			//push the changes to the db
 			rs.updateRow();			
 		}
 		else
 		{
 			//the user is still in a prior session
 			
 			//get the minutes used within this session.
 			minutes = rs.getInt(DB_MINS);
 			
 			//they've used too many minutes.. throw up an error and fail out
 			if (minutes > NetherTest.MAX_SESSION_LENGTH)
 			{
 				//calc how long until they can try again
 				int calcMin =  getMinuteDiff(cal, currentTime);
 				int calcHour = getHourDiff(cal, currentTime);
				if (calcMin > 0)
 					calcHour--;
 				
 				//output the informational error message
 				player.sendMessage(String.format("You've exceeded the %d minute maximum per %d hours", NetherTest.MAX_SESSION_LENGTH, NetherTest.ENTRANCE_FREQUENCY));
 				player.sendMessage(String.format("Please wait %d hours %d minutes to try again", calcHour, calcMin) );
 				return false;
 			}
 			else
 			{ //they still have minutes left.. let them use em up
 				//update lastActivity to the current time
 				updateTimestamp(rs);
 				
 				//push the changes to the db
 				rs.updateRow();					
 			}
 		}
 		
 		//Permission granted! inform them of how many minutes they have left
 		player.sendMessage(String.format("Welcome to the Nether World. You have %d minutes left.", NetherTest.MAX_SESSION_LENGTH - minutes));
 		
 		//cleanup //don't let it throw if we only have the exception on cleanup!
 		if (rs != null)
 		{
 			try { rs.close(); } catch (SQLException e) { }
 		}
 		
 		return true;
 	}
 	
 	public void exitNether(Player player)
 	{
 		ResultSet rs = null;
 		try {
 			rs = retrieveRecord(player);
 
 			//get the last activity time
 			Calendar cal = getTimestampFromDb(rs);
 
 			//current time
 			Calendar currentTime = Calendar.getInstance(); 
 			
 			//how many minutes were just spent in nether
 			int minutesSpent = getAbsoluteMinuteDiff(currentTime, cal);
			
			player.sendMessage(String.format("%d - %d", currentTime.getTime().getTime(), cal.getTime().getTime()));
			
 			//how many minutes were previously spent in nether
 			int previousMinutes = rs.getInt(DB_MINS);
 			
 			//add it and save it to the db
 			int totalMinutes = minutesSpent + previousMinutes;
 			rs.updateInt(DB_MINS, totalMinutes);
 			
 			//and update the last activity time
 			updateTimestamp(rs);
 			
 			//push the changes to the db
 			rs.updateRow();	
 
 			player.sendMessage(String.format("You just spent %d minutes in Nether and a total of %d. The limit is %d.", minutesSpent, totalMinutes, NetherTest.MAX_SESSION_LENGTH));
 			
 		} catch (SQLException e) {
 			_logger.warning("Couldn't access db to exit nether. -- " + e.getMessage());
 			return;
 		}
 		
 		//cleanup //don't let it throw if we only have the exception on cleanup!
 		if (rs != null)
 		{
 			try { rs.close(); } catch (SQLException e) { }
 		}
 	}
 	
 	public void outputInfo(Player player)
 	{
 		ResultSet rs = null;
 		
 		try {
 			rs = retrieveRecord(player);
 
 			//get the last activity time
 			Calendar cal = getTimestampFromDb(rs);
 
 			//how many minutes were previously spent in nether
 			int previousMinutes = rs.getInt(DB_MINS);
 
 			//calc how long until they can try again
 			Calendar currentTime = Calendar.getInstance(); 
 			int calcMin =  60- getMinuteDiff(currentTime, cal);
 			int calcHour = NetherTest.ENTRANCE_FREQUENCY - getHourDiff(currentTime, cal);	
			if (calcMin > 0)
 				calcHour--;
 			
 			player.sendMessage(String.format(
 					"You've used %d of your %d minutes within a %d hour period. Your minutes will refresh if you do not re-enter the nether for %d hours %d minutes.",
 					previousMinutes,
 					NetherTest.MAX_SESSION_LENGTH,
 					NetherTest.ENTRANCE_FREQUENCY,
 					calcHour,
 					calcMin));
 		} 
 		catch (SQLException e) {
 			player.sendMessage("Sorry, an error occurred while pulling up your details. Please try again later.");
 		}
 		
 		//cleanup //don't let it throw if we only have the exception on cleanup!
 		if (rs != null)
 		{
 			try { rs.close(); } catch (SQLException e) { }
 		}
 	}
 
 	public void EndNetherSession(Player player)
 	{//Player needs their session minutes maxed out so that they can't join until time expires
 
 		ResultSet rs = null;
 		
 		try {
 			//get their record
 			rs = retrieveRecord(player);
 
 			//set time to now
 			updateTimestamp(rs);
 
 			//max minutes
 			rs.updateInt(DB_MINS, Integer.MAX_VALUE);
 
 			//push the changes to the db
 			rs.updateRow();	
 		} 
 		catch (SQLException e) {
 			_logger.warning("Hmm, i couldn't persist an endNetherSession to the db?  -- " + e.getMessage());
 		}
 		
 		//cleanup //don't let it throw if we only have the exception on cleanup!
 		if (rs != null)
 		{
 			try { rs.close(); } catch (SQLException e) { }
 		}
 	}
 	
 	private ResultSet retrieveRecord(Player player) throws SQLException
 	{
 		//build query and execute it
 		String query = String.format("SELECT * FROM activity WHERE name = '%s'", player.getName());
 		ResultSet rs = _statement.executeQuery(query);	
 		
 		//move to first record if there is one
 		if (! rs.next())
 		{//they don't have a record yet.. make one so we can return it
 			rs.moveToInsertRow();//move to insert row
 			rs.updateString(DB_NAME, player.getName()); //make a record (other fields are auto-pop)
 			rs.insertRow(); //persist into result set and into db
 			rs.absolute(1); //move it to the first row (now that we have one)
 		}
 		
 		//return the result set with it positioned on the correct (and updatable) row
 		return rs;
 	}
 	
 	private int getHourDiff(Calendar future, Calendar now)
 	{
 		//sanity check
 		if (future.before(now))
 			return 0;
 		
 		//3,600,000 milliseconds in an hour
 		long hour = 3600000;
 		
 		//get the minutes of each
 		long hours1 = future.getTimeInMillis() / hour;
 		long hours2 = now.getTimeInMillis() / hour;
 		
 		//figure out the difference
 		int dif = (int) (hours1 - hours2);
 				
 		return dif;
 	}
 	
 	private int getMinuteDiff(Calendar future, Calendar now)
 	{	
 		//get total minutes different
 		int mins = getAbsoluteMinuteDiff(future, now);
 		
 		//now mod it so that it isn't greater than an hour
 		int remainder = mins % 60;
 		
 		return remainder;
 	}
 	
 	private int getAbsoluteMinuteDiff(Calendar future, Calendar now)
 	{
 		//sanity check
 		if (future.before(now))
 			return 0;
 		
 		//60,000 milliseconds in a minute
 		long min = 60000;
 		
 		//get the minutes of each
 		long mins1 = future.getTimeInMillis() / min;
 		long mins2 = now.getTimeInMillis() / min;
 		
 		//figure out the difference
 		int dif = (int) (mins1 - mins2);
 				
 		return dif;
 	}
 	
 	private Calendar getTimestampFromDb(ResultSet rs) throws SQLException
 	{
 		//prep a cal to use
 		Calendar cal = Calendar.getInstance();
 		
 		//get the val from db
 		Timestamp dbTime = rs.getTimestamp(DB_TIME, cal);
 		
 		//push it into the cal
 		cal.setTime(dbTime);
 		
 		return cal;
 	}
 	
 	private void updateTimestamp(ResultSet rs) throws SQLException
 	{
 		//get the current timestamp
 		Calendar cal = Calendar.getInstance();
 		Date date = cal.getTime();
 		long t = date.getTime();
 		Timestamp ts = new Timestamp(t);
 		
 		//update it in the result set (doesn't push to db until you call update row)
 		rs.updateTimestamp(DB_TIME, ts);
 	}
 		
 	protected void finalize()
 	{
 		//close the connection to release it back to the pool
 		if (_connection != null)
 		{
 			try 
 			{
 				_connection.close();
 			}
 			catch (SQLException e) 
 			{//eat me ..nom nom nom
 				e.printStackTrace();
 			}
 		}
 	}
 }
