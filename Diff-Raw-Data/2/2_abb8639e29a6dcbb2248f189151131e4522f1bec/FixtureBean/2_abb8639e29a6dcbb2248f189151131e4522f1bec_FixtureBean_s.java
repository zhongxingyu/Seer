 package failkidz.fkzteam.beans;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.Statement;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 public class FixtureBean {
 	private Connection conn;
 
 	private int homeID;
 	private int awayID;
 	private int homeScore;
 	private int awayScore;
 	private int gameOrder;
 
 	public FixtureBean(int homeID, int awayID,int gameOrder){
 		this.homeID = homeID;
 		this.awayID = awayID;
 		this.homeScore = -1;
 		this.awayScore = -1;
 		this.gameOrder = gameOrder;
 	}
 	
 	public FixtureBean(int homeID,int awayID,int homeScore,int awayScore,int gameOrder){
 		this.homeID = homeID;
 		this.awayID = awayID;
 		this.homeScore = homeScore;
 		this.awayScore = awayScore;
 		this.gameOrder = gameOrder;
 	}
 	
 	/**
 	 * This will create the insert statement to the database 
 	 */
 	public void insert() {
 		this.initDatabase();
 		String query = "INSERT INTO game VALUES(" + this.homeID + ", " + this.awayID + ", " + this.homeScore + ", " + this.awayScore + ", " + this.gameOrder + ");";
 		this.execute(query);
 	}
 	
 	public void update(){
 		this.initDatabase();
		String query = "UPDATE game SET homescore="+this.homeScore+", awayscore="+this.awayScore+" WHERE homeid="+this.homeID + ", awayid="+this.awayID+";";
 		this.execute(query);
 	}
 
 
 	/**
 	 * Executes the query
 	 * 
 	 * @param query
 	 */
 	private void execute(String query){
 		Statement stmt = null;
 		try{
 			stmt = conn.createStatement();
 			stmt.execute(query);
 		}
 		catch(SQLException e){
 		}
 		finally{
 			try {
 				stmt.close();
 				conn.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private ResultSet executeQuery(String query){
 		ResultSet rs = null;
 		Statement stmt = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(query);
 		}
 		catch(SQLException e){
 		}
 		finally{
 			try {
 				stmt.close();
 				rs.close();
 				conn.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return rs;
 	}
 
 
 	/**
 	 * Connection to the database
 	 */
 	private void initDatabase(){
 		try{
 			Context initCtx = new InitialContext();
 			Context envCtx = (Context) initCtx.lookup("java:comp/env");
 			DataSource ds = (DataSource)envCtx.lookup("jdbc/db");
 			conn = ds.getConnection();
 		}
 		catch(SQLException e){
 
 		}
 		catch(NamingException e){
 
 		}
 	}
 
 
 	/**
 	 * Generates the HTML rows in the game schedule
 	 *  
 	 * @return game schedule
 	 */
 	public String getHtmlRow(){
 		StringBuilder sb = new StringBuilder();
 		sb.append("<tr>\n");
 		sb.append("<td>"+this.getHomeID()+"</td>\n");
 		sb.append("<td>"+this.getAwayID()+"</td>\n");
 		sb.append("<td>"+this.getHomeScore()+"</td>\n");
 		sb.append("<td>"+this.getAwayScore()+"</td>\n");
 		sb.append("</tr>\n");
 
 		return sb.toString();
 	}
 	
 	public String getTeamName(int teamID){
 		String query = "SELECT teamname FROM teams WHERE id="+teamID+";";
 		initDatabase();
 		try {
 			return executeQuery(query).getString(1);
 		} catch (SQLException e) {
 			return null;
 		}
 	}
 
 
 	/*
 	 * A lot of getters and setters 
 	 * 
 	 */
 
 	public int getHomeID() {
 		return homeID;
 	}
 
 
 	public void setHomeID(int homeID) {
 		this.homeID = homeID;
 	}
 
 
 	public int getAwayID() {
 		return awayID;
 	}
 
 
 	public void setAwayID(int awayID) {
 		this.awayID = awayID;
 	}
 
 
 	public int getHomeScore() {
 		return homeScore;
 	}
 
 
 	public void setHomeScore(int homeScore) {
 		this.homeScore = homeScore;
 	}
 
 
 	public int getAwayScore() {
 		return awayScore;
 	}
 
 
 	public void setAwayScore(int awayScore) {
 		this.awayScore = awayScore;
 	}
 
 
 
 
 }
