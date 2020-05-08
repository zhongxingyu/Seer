 package com.voracious.dragons.server;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.voracious.dragons.common.GameInfo;
 import com.voracious.dragons.common.Turn;
 
 public class DBHandler {
 	public static final String dbfile = "game_data.sqlite";
 	private static final String[] tableNames = { "Game", "Player", "Winner", "Turn", "Spectator" };
 	private static Logger logger = Logger.getLogger(DBHandler.class);
 	private Connection conn;
 
 	private PreparedStatement checkHash;
 	private PreparedStatement registerUser;
 	private PreparedStatement numGames,numWins,turnsPerGame, times,latestTurnByMeNum,gameList, latestTurnData;
 	private PreparedStatement storeTurn,storeSpect,storeWinner,storeGame,playersInGame,latestTurn,myGames;
 	private PreparedStatement findMaxGameId, gameState, updateGameState;
 	
 	private PreparedStatement setGameOver;
 	public void init() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
 
 			// Check if the tables are already there or not
 			DatabaseMetaData meta = conn.getMetaData();
 			ResultSet tableResults = meta.getTables(null, null, "%", null);
 
 			ArrayList<String> tables = new ArrayList<String>();
 			while (tableResults.next()) {
 				tables.add(tableResults.getString("TABLE_NAME"));
 			}
 			
 			String[] tablesInDb = {null};
 			tablesInDb = tables.toArray(tablesInDb);
 			//TODO: There are extra tables that happen to come after ours in the sort get rid of those
 			
 			Arrays.sort(tablesInDb);
 			Arrays.sort(tableNames);
 
 			boolean equal = true;
 			for(int i=0; i<tableNames.length; i++){
 				if(tablesInDb[i] == null){
 					equal = false;
 					break;
 				}
 				
 				if(!tablesInDb[i].equals(tableNames[i])){
 					equal = false;
 				}
 			}
 			
 			if (!equal) {
 				conn.close();
 				new File(dbfile).delete();
 				conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
 				createDatabase();
 			}
 			
 			prepareStatements();
 		} catch (ClassNotFoundException e) {
 			logger.error("Could not load database", e);
 		} catch (SQLException e) {
 			logger.error("Could not load database", e);
 		}
 	}
 	
 	private void prepareStatements(){
 		try {
 		    checkHash = conn.prepareStatement("SELECT passhash " +
                     "FROM Player " +
                     "WHERE pid = ?");
 
             registerUser = conn.prepareStatement("INSERT INTO Player VALUES(?, ?)");
             
             numGames = conn.prepareStatement(
                     "SELECT count(*) AS answer " +
                     "FROM Game " +
                     "WHERE (pid1 = ? OR pid2 = ?) AND inProgress = ?;");
             
             numWins=conn.prepareStatement(
                     "SELECT count(gid) AS answer " +
                     "FROM Winner " +
                     "WHERE pid=? " +
                     "GROUP BY pid;");
             
             turnsPerGame=conn.prepareStatement(
                     "SELECT gid, count(*) AS answer " +
                     "FROM Turn " +
                     "WHERE pid=? " +
                     "GROUP BY gid;");
             
             myGames = conn.prepareStatement(
                     "SELECT gid " +
                     "FROM Game " +
                     "WHERE (pid1 = ? OR pid2 = ?) AND inProgress = ?;");
             
             times=conn.prepareStatement(
                     "SELECT gid, tnum, timeStamp " +
                     "FROM Turn " +
                     "WHERE pid=? " +
                     "ORDER BY gid ASC, timeStamp ASC;");
                     
             latestTurnData = conn.prepareStatement(
                     "SELECT turnString " +
                     "FROM Turn " +
                     "WHERE gid=? AND pid=?");
             
             latestTurnByMeNum=conn.prepareStatement(
                     "SELECT Max(tnum) AS answer " +
                     "FROM Turn " +
                     "WHERE gid=? AND pid=?");
             
             latestTurn=conn.prepareStatement(
                     "SELECT pid as id, MAX(tnum) AS answer " +
                     "FROM Turn " +
                     "WHERE gid=? " +
                     "GROUP BY pid;");
             
             playersInGame = conn.prepareStatement(
                     "SELECT pid1, pid2 " +
                     "FROM Game " +
                     "WHERE gid = ?;");
             
             gameList=conn.prepareStatement(
                     "SELECT gid, timeStamp, tnum, pid " +
                     "FROM Turn " +
                     "WHERE gid IN ( " +
                     "    SELECT gid " +
                     "    FROM Game " +
                     "    WHERE pid1=? OR pid2=?) " +
                     "GROUP BY gid " +
                     "HAVING timeStamp = Max(timeStamp);");
             
             storeTurn=conn.prepareStatement(
                     "INSERT INTO Turn(gid, tnum, pid, turnString) VALUES(?,?,?,?);");
             
             storeSpect=conn.prepareStatement(
                     "INSERT INTO Spectator VALUES(?,?);");
             
             storeWinner=conn.prepareStatement(
                     "INSERT INTO Winner VALUES(?,?);");
             
             //assuming a game inserted will be inprogress
             storeGame=conn.prepareStatement(
                      "INSERT INTO Game (pid1,pid2,inProgress,gameState) VALUES(?,?,1,?)");
             
             findMaxGameId=conn.prepareStatement(
             		"SELECT MAX(gid) AS answer FROM Game;");
             
             updateGameState = conn.prepareStatement("UPDATE Game SET gameState=? WHERE gid=?");
             
             this.setGameOver=conn.prepareStatement("UPDATE Game Set inProgress=0 WHERE gid=?;");
             
             gameState = conn.prepareStatement("SELECT gameState FROM Game WHERE gid = ?;");
 		} catch (SQLException e) {
 			logger.error("Error preparing statements", e);
 		}
 	}
 
 	private void createDatabase() {
 		Statement query;
 		try {
 			query = conn.createStatement();
 			query.executeUpdate("CREATE TABLE Player (pid VARCHAR(15) PRIMARY KEY NOT NULL," +
 					            "\n                     passhash CHAR(60) NOT NULL)");
 			
 			query.executeUpdate("CREATE TABLE Game (gid INTEGER PRIMARY KEY AUTOINCREMENT," +
 					            "\n                   pid1 VARCHAR(15) NOT NULL REFERENCES Player(pid), " +
 					            "\n                   pid2 VARCHAR(15) NOT NULL REFERENCES Player(pid)," +
 					            "\n                   inProgress BOOLEAN NOT NULL," +
 					            "\n                   gameState VARCHAR(20))");
 			
 			query.executeUpdate("CREATE TABLE Winner (gid INTEGER PRIMARY KEY NOT NULL REFERENCES Game(gid)," +
 					            "\n                     pid VARCHAR(15) NOT NULL REFERENCES Player(pid))");
 			
 			query.executeUpdate("CREATE TABLE Spectator (gid INTEGER PRIMARY KEY NOT NULL REFERENCES Game(gid)," +
 					            "\n                        pid VARCHAR(15) NOT NULL REFERENCES Player(pid))");
 			
 			query.executeUpdate("CREATE TABLE Turn (gid INTEGER NOT NULL REFERENCES Game(gid)," +
 					            "\n                   tnum INTEGER NOT NULL," +
 					            "\n                   timeStamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
 					            "\n                   pid VARCHAR(15) NOT NULL REFERENCES Player(pid)," +
 					            "\n                   turnString VARCHAR(60) NOT NULL," +
 					            "\n                   PRIMARY KEY(gid, pid, tnum))");
 		} catch (SQLException e) {
 			logger.error("Could not create tables", e);
 		}
 	}
 	
 	public int insertGame(String PID1, String PID2, String gameStateString){
 		try {
 			storeGame.setString(1, PID1);
 			storeGame.setString(2, PID2);
 			storeGame.setString(3, gameStateString);
 			storeGame.executeUpdate();
 			
 			ResultSet ret=findMaxGameId.executeQuery();
 			ret.next();
 			return ret.getInt("answer");
 		} catch (SQLException e) {
 			logger.error("Could not add to the game table",e);
 			return -1;
 		}
 	}
 	
 	public void insertWinner(int GID,String PID){
 		try {
 			storeWinner.setInt(1, GID);
 			storeWinner.setString(2, PID);
 			storeWinner.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not add to the winner table",e);
 		}
 		
 		
 	}
 	
 	public void insertSpectator(int GID,String PID){
 		try {
 			storeSpect.setInt(1, GID);
 			storeSpect.setString(2, PID);
 			storeSpect.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not add to the specatator table",e);
 		}
 		
 		
 	}
 	
 	public boolean insertTurn(int GID,String PID,String TURNSTRING){
 		try{
 			storeTurn.setInt(1,GID);
 	
 			latestTurn.setInt(1, GID);
 			ResultSet ret=latestTurn.executeQuery();
 			
 			int turnNum = 1;
 			
 			int p1turnNum = 0;
 			int p2turnNum = 0;
 			if(ret.next()){
     			String p1id = ret.getString("id");
     			p1turnNum = ret.getInt("answer");
     			
     			if(p1id.equals(PID))
     			    turnNum = ++p1turnNum;
     			
     			if(ret.next()){
         			String p2id = ret.getString("id");
         			p2turnNum = ret.getInt("answer");
         			
         			if(p2id.equals(PID))
                         turnNum = ++p2turnNum;
         			
         			if((p1id.equals(PID) && p1turnNum-1 > p2turnNum) || (p2id.equals(PID) && p2turnNum-1 > p1turnNum))
         				return false;
         			
     			}else{
     			    if(!p1id.equals(PID)){
     			        turnNum = p2turnNum = 1;
     			    }else{
     			        return false;
     			    }
     			}
 			}
 			
 			storeTurn.setInt(2, turnNum);
 			storeTurn.setString(3, PID);
 			storeTurn.setString(4, TURNSTRING);
 			storeTurn.executeUpdate();
 
 			return p1turnNum == p2turnNum && p1turnNum != 0 && p2turnNum != 0;
 		}
 		catch(SQLException e){
 			logger.error("Could not add to the turn table", e);
 			return false;
 		}
 	}
 	
 	public String getPasswordHash(String pid){
 		try {
 			checkHash.setString(1, pid);
 			ResultSet rs = checkHash.executeQuery();
 			if(!rs.next()){
 				return null;
 			}
 
 			return rs.getString("passhash");
 		} catch (SQLException e) {
 			logger.error("Could not check hash", e);
 			return null;
 		}
 	}
 	
 	public void registerUser(String uid, String passhash){
 		try {
 			registerUser.setString(1, uid);
 			registerUser.setString(2, passhash);
 			registerUser.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("Could not register user", e);
 		}
 	}
 
 	public int numGames(String PID, int inPlay){
 	    //the int is to know if the games being counted are over or still occurring
         //0 false
         //1 true
         try{
             numGames.setString(1, PID);
             numGames.setString(2, PID);
             numGames.setInt(3, inPlay);
             ResultSet ret = numGames.executeQuery();
             
             if(!ret.next())
                 return 0;
             
             return ret.getInt("answer");
         }catch(SQLException e){
             logger.error("Could not count the number of games",e);
             return -1;
         }
 	}
 	
    public int countWins(String PID){
         try{
             numWins.setString(1, PID);
             ResultSet ret = numWins.executeQuery();
             
             if(!ret.next())
                 return 0;
             
             return ret.getInt("answer");
         }catch(SQLException e){
             logger.error("Could not count the number of wins", e);
             return -1;
         }
     }
     
     public double aveTurns(String PID) {
         try {
             turnsPerGame.setString(1, PID);
             ResultSet ret = turnsPerGame.executeQuery();
             
             int totalTurns = 0;
             int numGames = 0;
             while(ret.next()){
                 totalTurns += ret.getInt("answer");
                 numGames++;
             }
             
             if(numGames == 0)
                 return 0.0;
             
             return totalTurns/numGames;
         } catch (SQLException e) {
             logger.error("Could not count the ave number of turns", e);
             return -1.0;
         }
     }
     
     public long aveTime(String PID){
         try {
             times.setString(1, PID);
             ResultSet timeRes = times.executeQuery();
             
             int turnCounter = 0;
             long sum = 0;
             //calculates the total time
             Timestamp temp=new Timestamp(0);
             while(timeRes.next()){
                 int tnum = timeRes.getInt("tnum");
                 Timestamp time = timeRes.getTimestamp("timeStamp");
 
                 if(tnum == 0) {
                     temp = time;
                 }else{
                     sum += time.getTime() - temp.getTime();
                     temp = time;
                     
                     turnCounter++;
                 }
             }
             
             if(turnCounter == 0)
                 return 0;
             
             return sum/turnCounter;
         } catch (SQLException e) {
             logger.error("Could not count the ave time between turns", e);
             return -1;
         }
     }
     
     public Boolean isPlayer1(String pid, int gid){
         boolean result = false;
         try {
             playersInGame.setInt(1, gid);
         
             ResultSet prs = playersInGame.executeQuery();
             while(prs.next()){
                 String pid1 = prs.getString("pid1");
                 if(!pid.equals(pid1)){
                     result = true;
                 }else{
                     result = false;
                 }
             }
         } catch (SQLException e) {
             logger.error("Could not get other player", e);
         }
         
         return result;
     }
 	
 	public String getOtherPid(String pid, int gid){
 	    String otherPlayer = "";
 	    try {
             playersInGame.setInt(1, gid);
         
             ResultSet prs = playersInGame.executeQuery();
             while(prs.next()){
                 String pid1 = prs.getString("pid1");
                 String pid2 = prs.getString("pid2");
                 if(!pid.equals(pid1)){
                     otherPlayer = pid1;
                 }else{
                     otherPlayer = pid2;
                 }
             }
 	    } catch (SQLException e) {
             logger.error("Could not get other player", e);
         }
 	    
         return otherPlayer;
 	}
 	
 	public List<GameInfo> getGameList(String pid){
 	    List<GameInfo> result = new ArrayList<>();
 	    
 	    try {
             gameList.setString(1, pid);
             gameList.setString(2, pid);
             ResultSet rs = gameList.executeQuery();
             
 	        //ResultSet rs = conn.createStatement().executeQuery("SELECT gid, timeStamp, tnum, pid FROM Turn WHERE gid = 1 GROUP BY gid;");
 	        
             while(rs.next()){
                 int gid = rs.getInt("gid");
                 int tnum = rs.getInt("tnum");
                 Timestamp ts = rs.getTimestamp("timeStamp");
                 String tpid = rs.getString("pid");
                 
                 String otherPlayer = "";
                 
                 if(!pid.equals(tpid)){
                     otherPlayer = tpid;
                 }else{
                     otherPlayer = this.getOtherPid(pid, gid);
                 }
                 
                 boolean canMakeTurn = false;
                 
                 if(!pid.equals(tpid)){
                     canMakeTurn = true;
                 }else{
                     latestTurnByMeNum.setInt(1, gid);
                     latestTurnByMeNum.setString(2, otherPlayer);
                     ResultSet lturnrs = latestTurnByMeNum.executeQuery();
                     
                     int othersTurnNum = 0xfffffff;
                     while(lturnrs.next()){
                         othersTurnNum = lturnrs.getInt("answer");
                     }
                     
                     if(tnum == othersTurnNum){
                         canMakeTurn = true;
                     }
                 }
                 
                 result.add(new GameInfo(gid, otherPlayer, ts.getTime(), pid.equals(tpid), canMakeTurn, isPlayer1(pid, gid)));
             }
         } catch (SQLException e) {
             logger.error("Could not get game list", e);
         }
 	    
 	    if(result.size() != numGames(pid, 1)){
 	        try {
                 myGames.setString(1, pid);
                 myGames.setString(2, pid);
                 myGames.setInt(3, 1);
                 ResultSet rs = myGames.executeQuery();
                 while(rs.next()){
                     int gid = rs.getInt("gid");
                     result.add(new GameInfo(gid, this.getOtherPid(pid, gid), 0, false, true, isPlayer1(pid, gid)));
                 }
             } catch (SQLException e) {
                 logger.error("Could not get game list", e);
             }
 	    }
 	    
 	    return result;
 	}
 	
 	public String getGameState(int gid){
 	    try {
 	        gameState.setInt(1, gid);
             return gameState.executeQuery().getString("gameState");
         } catch (SQLException e) {
             logger.error(e);
             return null;
         }
 	}
 	
 	public boolean isInGame(String pid, int gid){
 	    try {
             playersInGame.setInt(1, gid);
             ResultSet rs = playersInGame.executeQuery();
             if(rs.next()){
                 return pid.equals(rs.getString("pid1")) || pid.equals(rs.getString("pid2"));
             }else{
                 return false;
             }
         } catch (SQLException e) {
             logger.error(e);
             return false;
         }
 	}
 
     public Turn getLatestTurn(int gid, String pid) {
         try {
             latestTurnData.setInt(1, gid);
             latestTurnData.setString(2, pid);
             ResultSet rs = latestTurnData.executeQuery();
             if(rs.next())
                 return new Turn(rs.getString("turnString"));
         } catch (SQLException e) {
             logger.error(e);
         }
         
         return null;
     }
 
     public void updateGameState(int gid, String gamestate) {
         try {
             updateGameState.setString(1, gamestate);
             updateGameState.setInt(2, gid);
             updateGameState.executeUpdate();
         } catch (SQLException e) {
             logger.error(e);
         }
     }
     
     public void updateProgress(int gid){
     	try{
    		setGameOver.setInt(0, gid);
     		setGameOver.executeUpdate();
     	}
     	catch(SQLException e) {
             logger.error(e);
     	}
     }
 }
