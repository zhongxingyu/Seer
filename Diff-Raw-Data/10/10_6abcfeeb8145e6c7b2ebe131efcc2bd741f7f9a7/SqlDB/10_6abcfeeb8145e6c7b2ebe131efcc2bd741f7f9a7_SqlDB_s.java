 package FRC_Score_Sys;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SqlDB {
 
	private String SQLDBVER = "A";
 	
 	private Connection	c;
 	private String		DBfile	= "score_data.db";
 
 	private ExceptionClass Except = new ExceptionClass("SQLDB"); 
 	
 	public SqlDB() {
 		System.out.println("Starting SQL Server..");
 		File file = new File(DBfile);
 		Boolean doNew = false;
 		if (!file.exists()) {
 			doNew = true;
 			System.out.println("DB File not found. Will create.");
 		} else {
 			System.out.println("DB File found. using existing DB.");
 		}
 		file = null;
 		try {
 			System.out.println("Looking for SQLLite Driver..");
 			Class.forName("org.sqlite.JDBC");
 			System.out.println("Found. Connecting to Database..");
 			c = DriverManager.getConnection("jdbc:sqlite:" + DBfile);
 			if (doNew) {
 				if (GenerateNewDatabase()) {
 					System.out.println("Inserting Default Options..");
 					CreateOptions();
 				} else {
 					Except.ExceptionHandler("Constructor",null,true,true,"Failed to create database tables.");
 				}
 			} else {
				//TODO: FIX THIS CHECK!
 				String DBV = FetchOption("SQLDBVER"); 
				if(DBV.equals(SQLDBVER)){
 					Except.ExceptionHandler("Constructor",null, true,true,"Your DB version is out-dated."
 							+ "\nYour Version: '"+DBV+"'"
 							+ "\nReq Version : '"+SQLDBVER+"'");
 				}
 			}
 			System.out.println("DB Subsystem up and running!");
 		} catch (Exception e) {
 			Except.ExceptionHandler("Constructor",e, true,true);
 		}
 	}
 
 	public int AddMatchToDB(String[] matchInfo) {
 		try {
 			PreparedStatement s = c
 					.prepareStatement("INSERT INTO MATCHES VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
 			int i = 1;
 			boolean first = false;
 			System.out.println("==== AddingMatchToDB ====");
 			for (String item : matchInfo) {
 				System.out.print("Evaluating '" + item + "'.. ");
 				if (!first) {
 					System.out.println("Match ID");
 					item = PadString(item);
 					s.setString(i, "QQ" + item);
 					first = true;
 				} else {
 					System.out.println("Data");
 					s.setInt(i, Integer.parseInt(item));
 				}
 				i = i + 1;
 			}
 			int ret = s.executeUpdate();
 			s.close();
 			return ret;
 		} catch (Exception e) {
 			Except.ExceptionHandler("AddMatchedToDB",e, false, true, "Something was really wrong with the match import.");
 			return 0;
 		}
 	}
 
 	private String BuildUpdateString(String q, String clr, String field) {
 		return q + " " + clr + field + " = ?,";
 	}
 
 	public void close() {
 		System.out.println("SQL Lite DB Closing Gracefully. Goodnight!");
 		try {
 			c.close();
 		} catch (Exception e) {
 			Except.ExceptionHandler("DBClose",e, false, false);
 		}
 	}
 
 	
 	
 	
 
 	public List<SingleMatch> FetchMatch(String id) {
 		System.out.println("Match Fetch Requested for id " + id);
 		List<SingleMatch> ScoreList = new ArrayList<SingleMatch>();
 		try {
 			PreparedStatement s = c.prepareStatement("SELECT * FROM MATCHES WHERE id = ? LIMIT 1");
 			s.setString(1, id);
 			ResultSet rs = s.executeQuery();
 			String[] clrs = { "R", "B" };
 			while (rs.next()) {
 				for (String clr : clrs) {
 					System.out.println("Gathering " + clr + "'s");
 					SingleMatch Scores = new SingleMatch(id, clr);
 					Scores.Robot1 = rs.getInt(clr + "1Robot");
 					Scores.Robot2 = rs.getInt(clr + "2Robot");
 					Scores.Robot3 = rs.getInt(clr + "3Robot");
 
 					Scores.Sur1 = rs.getBoolean(clr + "1Sur");
 					Scores.Sur2 = rs.getBoolean(clr + "2Sur");
 					Scores.Sur3 = rs.getBoolean(clr + "3Sur");
 
 					Scores.Climb1 = rs.getInt(clr + "1Climb");
 					Scores.Climb2 = rs.getInt(clr + "2Climb");
 					Scores.Climb3 = rs.getInt(clr + "3Climb");
 
 					Scores.Dq1 = rs.getBoolean(clr + "1Dq");
 					Scores.Dq2 = rs.getBoolean(clr + "2Dq");
 					Scores.Dq3 = rs.getBoolean(clr + "3Dq");
 
 					Scores.DisksLA = rs.getInt(clr + "DisksLA");
 					Scores.DisksLT = rs.getInt(clr + "DisksLT");
 					Scores.DisksMA = rs.getInt(clr + "DisksMA");
 					Scores.DisksMT = rs.getInt(clr + "DisksMT");
 					Scores.DisksHA = rs.getInt(clr + "DisksHA");
 					Scores.DisksHT = rs.getInt(clr + "DisksHT");
 					Scores.DisksP = rs.getInt(clr + "DisksP");
 
 					Scores.Foul = rs.getInt(clr + "Foul");
 					Scores.TFoul = rs.getInt(clr + "TFoul");
 
 					Scores.Score = rs.getInt(clr + "Score");
 
 					ScoreList.add(Scores);
 				}
 			}
 
 		} catch (Exception e) {
 			Except.ExceptionHandler("FetchMatch",e, false, true, "We were unable to fetch that match?");
 		}
 		System.out.println("Match Fetched. Here ya go!");
 		return ScoreList;
 	}
 
 	public List<MatchListObj> FetchMatchList(String type) {
 		System.out.println("Match List Fetch Requested for type " + type);
 		List<MatchListObj> WholeList = new ArrayList<MatchListObj>();
 		try {
 			PreparedStatement s = c.prepareStatement("SELECT id,Saved,RScore,BScore FROM MATCHES WHERE id LIKE ?");
 			s.setString(1, type + "%");
 			ResultSet rs = s.executeQuery();
 			while (rs.next()) {
 				MatchListObj MLO = new MatchListObj(rs.getString("ID"));
 				int BScore = rs.getInt("BScore");
 				int RScore = rs.getInt("RScore");
 				if (rs.getBoolean("Saved")) {
 					MLO.Played = true;
 					if (BScore == RScore) {
 						MLO.Clr = MLO.color_yellow;
 						MLO.Score = BScore + " to " + RScore;
 					}
 					if (BScore > RScore) {
 						MLO.Clr = MLO.color_blue;
 						MLO.Score = BScore + " to " + RScore;
 					}
 					if (BScore < RScore) {
 						MLO.Clr = MLO.color_red;
 						MLO.Score = RScore + " to " + BScore;
 					}
 				}
 				WholeList.add(MLO);
 			}
 
 		} catch (Exception e) {
 			Except.ExceptionHandler("FetchMatchList",e, false, true, "Match list can not be loaded.");
 		}
 		return WholeList;
 	}
 
 	private String FormatSQLBuild(String PrevString, String field, String Type, String Clr) {
 		return FormatSQLBuild(PrevString, field, Type, Clr, "");
 
 	}
 
 	private String FormatSQLBuild(String PrevString, String field, String Type, String Clr, String Options) {
 		String out = PrevString;
 		out = out + " " + Clr + field;
 		out = out + " " + Type;
 		if (Options != "") {
 			out = out + " " + Options;
 		}
 		out = out + " NOT NULL,";
 		return out;
 	}
 
 	//@formatter:off
 	private boolean GenerateNewDatabase() {
 		System.out.println("Creating new database tables..");
 		String[] clrs = {"R", "B"};
 		
 		boolean Table1Success = false;
 		
 		String q = "CREATE TABLE MATCHES (";
 		q = FormatSQLBuild(q, "ID", "TEXT", "", "PRIMARY KEY NOT NULL");
 		
 		for(String clr : clrs){
 			q = FormatSQLBuild(q, "1Robot",		"INT", clr);
 			q = FormatSQLBuild(q, "1Sur",		"BOOLEAN", clr);
 			q = FormatSQLBuild(q, "2Robot",		"INT", clr);
 			q = FormatSQLBuild(q, "2Sur",		"BOOLEAN", clr);
 			q = FormatSQLBuild(q, "3Robot",		"INT", clr);
 			q = FormatSQLBuild(q, "3Sur",		"BOOLEAN", clr);
 		}
 		
 		for(String clr : clrs){
 			q = FormatSQLBuild(q, "DisksLA",	"INT", clr);
 			q = FormatSQLBuild(q, "DisksLT",	"INT", clr);
 			q = FormatSQLBuild(q, "DisksMA",	"INT", clr);
 			q = FormatSQLBuild(q, "DisksMT",	"INT", clr);
 			q = FormatSQLBuild(q, "DisksHA",	"INT", clr);
 			q = FormatSQLBuild(q, "DisksHT",	"INT", clr);
 			q = FormatSQLBuild(q, "DisksP",		"INT", clr);
 			q = FormatSQLBuild(q, "1Climb",		"INT", clr);
 			q = FormatSQLBuild(q, "2Climb",		"INT", clr);
 			q = FormatSQLBuild(q, "3Climb",		"INT", clr);
 			q = FormatSQLBuild(q, "1Dq",		"BOOLEAN", clr);
 			q = FormatSQLBuild(q, "2Dq",		"BOOLEAN", clr);
 			q = FormatSQLBuild(q, "3Dq",		"BOOLEAN", clr);
 			q = FormatSQLBuild(q, "Foul",		"INT", clr);
 			q = FormatSQLBuild(q, "TFoul",		"INT", clr);
 			q = FormatSQLBuild(q, "Score",		"INT", clr);
 		}
 		q = FormatSQLBuild(q, "Saved", "BOOLEAN", "");
 		
 		q = q.substring(0, q.length() - 1);
 		q = q + ")";
 		int cre = PerformInternalUpdateQuery(q);
 		if (cre != 0) {
 			System.out.println("Matches Table Create failed!");
 		} else {
 			System.out.println("Matches Table Create successful!");
 			Table1Success = true;
 		}
 		
 		q = "CREATE TABLE OPTIONS (ID TEXT PRIMARY KEY NOT NULL, VAL TEXT NOT NULL)";
 		cre = PerformInternalUpdateQuery(q);
 		if (cre != 0) {
 			System.out.println("Options Table Create failed!");
 			return false;
 		} else {
 			System.out.println("Options Table Create successful!");
 			if(Table1Success) return true;
 			return false;
 		}
 	}
 
 	//@formatter:on
 	private String PadString(String inStr) {
 		if (inStr.length() >= 4) {
 			return inStr;
 		}
 		String out = inStr;
 		while (out.length() != 4) {
 			out = "0" + out;
 		}
 		return out;
 	}
 
 	public int PerformInternalUpdateQuery(String q) {
 		try {
 			Statement st = c.createStatement();
 			int ret = st.executeUpdate(q);
 			st.close();
 			return ret;
 		} catch (Exception e) {
 			Except.ExceptionHandler("PerformInternalUpdt", e, false, false);
 			return -1;
 		}
 	}
 	public String FetchOption(String Name){
 		try{
 			String q = "SELECT VAL FROM OPTIONS WHERE ID=?";
 			PreparedStatement s = c.prepareStatement(q);
 			s.setString(1, Name);
 			ResultSet rs = s.executeQuery();
 			String ret = "";
 			while (rs.next()) {
 				ret = rs.getString("VAL");
 			}
 			return ret;
 		} catch (Exception e){
			Except.ExceptionHandler("UpdateOption", e, false, false);
 			return "";
 		}
 		
 	}
 	public boolean UpdateOption(String Name, String Val){
 		try{
 			String q = "UPDATE OPTIONS SET VAL=? WHERE ID=?";
 			PreparedStatement s = c.prepareStatement(q);
 			s.setString(1, Val);
 			s.setString(2, Name);
 			int cre = s.executeUpdate();
 			if(cre != 1){
 				return false;
 			} else {
 				return true;
 			}
 		} catch (Exception e){
 			Except.ExceptionHandler("UpdateOption", e, false, false);
 			return false;
 		}
 		
 	}
 	private boolean CreateOptions(){
 		String q = "INSERT INTO OPTIONS VALUES ('SQLDBVER', '"+SQLDBVER+"')";
 		int cre = PerformInternalUpdateQuery(q);
 		if(cre != 1){
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	public boolean SaveMatchChanges(List<SingleMatch> Match) {
 		try {
 			String q = "UPDATE MATCHES SET";
 			String[] clrs = { "R", "B" };
 			// Build the query
 			System.out.println("Saving Match to DB, building query..");
 			for (String clr : clrs) {
 				System.out.println("Making " + clr + "'s columns..");
 				q = BuildUpdateString(q, clr, "DisksLA");
 				q = BuildUpdateString(q, clr, "DisksLT");
 				q = BuildUpdateString(q, clr, "DisksMA");
 				q = BuildUpdateString(q, clr, "DisksMT");
 				q = BuildUpdateString(q, clr, "DisksHA");
 				q = BuildUpdateString(q, clr, "DisksHT");
 				q = BuildUpdateString(q, clr, "DisksP");
 				q = BuildUpdateString(q, clr, "1Climb");
 				q = BuildUpdateString(q, clr, "2Climb");
 				q = BuildUpdateString(q, clr, "3Climb");
 				q = BuildUpdateString(q, clr, "1Dq");
 				q = BuildUpdateString(q, clr, "2Dq");
 				q = BuildUpdateString(q, clr, "3Dq");
 				q = BuildUpdateString(q, clr, "Foul");
 				q = BuildUpdateString(q, clr, "TFoul");
 				q = BuildUpdateString(q, clr, "Score");
 			}
 			q = q + " Saved = 1 WHERE ID = ?";
 			PreparedStatement s = c.prepareStatement(q);
 			SingleMatch B = new SingleMatch();
 			SingleMatch R = new SingleMatch();
 			for (SingleMatch m : Match) {
 				if (m.aColor() == "R") {
 					R = m;
 				}
 				if (m.aColor() == "B") {
 					B = m;
 				}
 			}
 			System.out.println("Filling Columns..");
 			// Red
 			s.setInt(1, R.DisksLA);
 			s.setInt(2, R.DisksLT);
 			s.setInt(3, R.DisksMA);
 			s.setInt(4, R.DisksMT);
 			s.setInt(5, R.DisksHA);
 			s.setInt(6, R.DisksHT);
 			s.setInt(7, R.DisksP);
 			s.setInt(8, R.Climb1);
 			s.setInt(9, R.Climb2);
 			s.setInt(10, R.Climb3);
 			s.setBoolean(11, R.Dq1);
 			s.setBoolean(12, R.Dq2);
 			s.setBoolean(13, R.Dq3);
 			s.setInt(14, R.Foul);
 			s.setInt(15, R.TFoul);
 			s.setInt(16, R.Score);
 			// Blue
 			s.setInt(17, B.DisksLA);
 			s.setInt(18, B.DisksLT);
 			s.setInt(19, B.DisksMA);
 			s.setInt(20, B.DisksMT);
 			s.setInt(21, B.DisksHA);
 			s.setInt(22, B.DisksHT);
 			s.setInt(23, B.DisksP);
 			s.setInt(24, B.Climb1);
 			s.setInt(25, B.Climb2);
 			s.setInt(26, B.Climb3);
 			s.setBoolean(27, B.Dq1);
 			s.setBoolean(28, B.Dq2);
 			s.setBoolean(29, B.Dq3);
 			s.setInt(30, B.Foul);
 			s.setInt(31, B.TFoul);
 			// WHERE
 			s.setInt(32, B.Score);
 			s.setString(33, B.MatchID());
 			System.out.println("Performing DB Update..");
 			int ret = s.executeUpdate();
 			if (ret != 1) {
 				System.out.println("Update Failed.");
 				return false;
 			}
 			System.out.println("Update Complete!");
 			return true;
 		} catch (Exception e) {
 			Except.ExceptionHandler("UpdateMatch", e, false, true, "Match update failed?");
 		}
 
 		return false;
 	}
 }
