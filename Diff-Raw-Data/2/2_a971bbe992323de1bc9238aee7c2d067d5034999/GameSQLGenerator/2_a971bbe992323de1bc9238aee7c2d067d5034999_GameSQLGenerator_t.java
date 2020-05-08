 package codeGenerator;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import jsonObjects.boxScoreObjects.GameInfoJson;
 import jsonObjects.boxScoreObjects.GameSummaryJson;
 
 public class GameSQLGenerator 
 {
 	private int attendance, homeTeamID, awayTeamID;
 	private String nbaGameID, gameTime, gameDate, season, broadcaster;
 	private int gameID;
 	
 	public GameSQLGenerator(GameInfoJson gameInfo, GameSummaryJson gameSummary)
 	{
 		this.attendance = gameInfo.getAttendance();
 		this.gameTime = gameInfo.getGameTime();
 		this.nbaGameID = gameSummary.getGameID();
 		this.homeTeamID = gameSummary.getHomeID();
 		this.awayTeamID = gameSummary.getAwayID();
 		this.gameDate = gameSummary.getGameDate();
 		this.season = gameSummary.getSeason();
 		this.broadcaster = gameSummary.getBroadcaster();
 	}
 
 	public int getAttendance() { return attendance; }
 	public int getHomeTeamID() { return homeTeamID; }
 	public int getAwayTeamID() { return awayTeamID; }
 	public int getGameID() { return gameID; }
 	public String getNBAGameID() { return nbaGameID; }
 	public String getGameTime() { return gameTime; }
 	public String getGameDate() { return gameDate; }
 	public String getSeason() { return season; }
 	public String getBroadcaster() { return broadcaster; }
 	
 	public void compile(String path,
 			String userName, String password)
 	{
 		Connection conn;
 		PreparedStatement stmt;
 		ResultSet rs;
 		Date convertedDate = new Date();
 		
 		try 
 		{
 			convertedDate = new SimpleDateFormat("yyyyMMdd").parse(gameDate.substring(0, 11));
 		} 
 		catch (ParseException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`game` (`date_played`,`game_time`," +
 					"`attendance`, `broadcaster`,`game_id`) VALUES (?,?,?,?,?);");
 			
 			
 			stmt.setDate(1, convertDate(convertedDate));
 			stmt.setInt(2, convertTime(this.gameTime));
 			stmt.setInt(3, this.attendance);
 			stmt.setString(4, this.broadcaster);
 			stmt.setString(5, this.nbaGameID);
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		        this.gameID = rs.getInt(1);
 		    } 
 		    else 
 		    {
 		        //TODO throw an exception from here
 		    }
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`game_teams` (`game_id`,`home_team_id`,`away_team_id`)" +
 					"VALUES (?,?,?);");
 			
 			stmt.setInt(1, this.gameID);
 			stmt.setInt(2, this.homeTeamID);
 			stmt.setInt(3, this.awayTeamID);
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`game_season` (`game_id`,`season_id`)" +
 					"VALUES (?,?);");
 			
 			stmt.setInt(1, this.gameID);
 			stmt.setInt(2, getSeasonID(this.season, path, userName, password));
 			stmt.executeUpdate();
 			
 			
 			stmt.close();
 			conn.close();
 			
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private java.sql.Date convertDate(Date date)
 	{
 		return new java.sql.Date(date.getTime());
 	}
 	
 	private int convertTime(String time)
 	{
 		int total;
 		
 		total = Integer.parseInt(time.substring(0, 1)) * 60;
		total += Integer.parseInt(time.substring(2, 4));
 		return total;
 	}
 	
 	@SuppressWarnings("deprecation")
 	private int getSeasonID(String season, String path, String userName, String password)
 	{
 		Connection conn;
 		PreparedStatement stmt;
 		ResultSet rs;
 		int seasonID = -1;
 		java.sql.Date seasonStart, seasonEnd, startDate;
 		
 		try 
 		{
 			int startYear = Integer.parseInt(season.substring(0,4));
 			startDate = new java.sql.Date(startYear, 12, 31);
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 			stmt = conn.prepareStatement("SELECT * FROM `nba`.`season`");
 			rs = stmt.executeQuery();
 			
 			while(rs.next())
 		    {
 		    	seasonStart = rs.getDate("start_date");
 		    	seasonEnd = rs.getDate("end_date");
 		    	
 		    	if (startDate.after(seasonStart) && startDate.before(seasonEnd))
 		    	{
 		    		seasonID = rs.getInt("season_id");
 		    		break;
 		    	}
 		    }
 			
 			stmt.close();
 			conn.close();
 			return seasonID;
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return seasonID;
 	}
 	
 }
