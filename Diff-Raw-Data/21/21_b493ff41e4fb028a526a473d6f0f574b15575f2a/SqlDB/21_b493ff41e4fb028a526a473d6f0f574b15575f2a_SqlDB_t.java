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
 
 	private String SQLDBVER = "5";
 
 	private Connection c;
 	private String DBfile = "score_data.db";
 
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
 					Except.ExceptionHandler("Constructor", null, true, true, "Failed to create database tables.");
 				}
 			} else {
 				String DBV = FetchOption("SQLDBVER");
 				if (!DBV.equals(SQLDBVER)) {
 					Except.ExceptionHandler("Constructor", null, true, true, "Your DB version is out-dated." + "\nYour Version: '" + DBV + "'" + "\nReq Version : '" + SQLDBVER + "'");
 				}
 			}
 			System.out.println("DB Subsystem up and running!");
 		} catch (Exception e) {
 			Except.ExceptionHandler("Constructor", e, true, true, ":/ Without a Database, we're about worthless.");
 		}
 	}
 
 	private void AddTeamToDB(int id){
 		try{
 			PreparedStatement s = c.prepareStatement("INSERT INTO TEAMS VALUES(?, 0, 0, 0, 0, 0, 0, 0)");
 			s.setInt(1, id);
 			s.executeUpdate();
 		} catch (Exception e){
 			// Supress
 		}
 	}
 	
 	public void RefreshRanks(){
 		List<TeamRankObj> Teams = FetchTeamlist(false);
 		System.out.println("Refreshing Rankings..");
 		try{
			String qR = "SELECT SUM(case RQS when 2 then 1 else 0 end) as wins, SUM(case RQS when 1 then 1 else 0 end) as ties, COUNT(*) as tot, SUM(RQS) as QS, SUM(RAP) as AP, SUM(RCP) as CP, SUM(RTP) as TP FROM MATCHES WHERE ((R1Robot=? AND R1Sur=0 AND R1Dq=0) OR (R2Robot=? AND R2Sur=0 AND R2Dq=0) OR (R3Robot=? AND R3Sur=0 AND R3Dq=0)) AND Saved=1";
			String qB = "SELECT SUM(case BQS when 2 then 1 else 0 end) as wins, SUM(case BQS when 1 then 1 else 0 end) as ties, COUNT(*) as tot, SUM(BQS) as QS, SUM(BAP) as AP, SUM(BCP) as CP, SUM(BTP) as TP FROM MATCHES WHERE ((B1Robot=? AND B1Sur=0 AND B1Dq=0) OR (B2Robot=? AND B2Sur=0 AND B2Dq=0) OR (B3Robot=? AND B3Sur=0 AND B3Dq=0)) AND Saved=1";
 			String qT = "UPDATE TEAMS SET QS=?, AP=?, CP=?, TP=?, WINS=?, TIES=?, TOT=? WHERE ID=?";
 			PreparedStatement sR = c.prepareStatement(qR);
 			PreparedStatement sB = c.prepareStatement(qB);
 			PreparedStatement sT = c.prepareStatement(qT);
 			for(TeamRankObj team : Teams){
 				sR.setInt(1, team.ID);
 				sR.setInt(2, team.ID);
 				sR.setInt(3, team.ID);
 				sB.setInt(1, team.ID);
 				sB.setInt(2, team.ID);
 				sB.setInt(3, team.ID);
 				ResultSet rsR = sR.executeQuery();
 				ResultSet rsB = sB.executeQuery();
 				TeamRankObj newRank = new TeamRankObj();
 				newRank.ID = team.ID;
 				while(rsR.next()){
 					newRank.QS = rsR.getInt("QS");
 					newRank.AP = rsR.getInt("AP");
 					newRank.CP = rsR.getInt("CP");
 					newRank.TP = rsR.getInt("TP");
 					newRank.wins = rsR.getInt("wins");
 					newRank.ties = rsR.getInt("ties");
 					newRank.tot = rsR.getInt("tot");
 				}
 				while(rsB.next()){
 					newRank.QS = newRank.QS+rsB.getInt("QS");
 					newRank.AP = newRank.AP+rsB.getInt("AP");
 					newRank.CP = newRank.CP+rsB.getInt("CP");
 					newRank.TP = newRank.TP+rsB.getInt("TP");
 					newRank.wins = newRank.wins+rsB.getInt("wins");
 					newRank.ties = newRank.ties+rsB.getInt("ties");
 					newRank.tot  = newRank.tot+rsB.getInt("tot");
 				}
 				sT.setInt(1, newRank.QS);
 				sT.setInt(2, newRank.AP);
 				sT.setInt(3, newRank.CP);
 				sT.setInt(4, newRank.TP);
 				sT.setInt(5, newRank.wins);
 				sT.setInt(6, newRank.ties);
 				sT.setInt(7, newRank.tot);
 				// WHERE
 				sT.setInt(8, newRank.ID);
 				int ret = sT.executeUpdate();
 				if(ret!=1){
 					Except.ExceptionHandler("RefreshRanks", null, false, true, "We failed to save the ranks for '"+newRank.ID+"' (Returned: "+ret+")"+ 
 							"\nWe canceled the rest of the refresh to be safe.");
 					break;
				} else {
					System.out.println("Updated Team '"+newRank.ID+"'");
				}
 			}
 			System.out.println("Ranking Updated");
 		} catch (Exception e){
 			Except.ExceptionHandler("RefreshRanks", e, false, true, "The ranks failed to refresh. This is going to be a problem. :/");
 		}
 	}
 	
 	public int AddMatchToDB(String[] matchInfo) {
 		try {
 			PreparedStatement s = c.prepareStatement("INSERT INTO MATCHES VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
 			int i = 1;
 			int first = 0;
 			System.out.println("==== AddingMatchToDB ====");
 			for (String item : matchInfo) {
 				System.out.print("Evaluating '" + item + "'.. ");
 				if (first == 0) {
 					System.out.println("Match ID");
 					item = PadString(item);
 					s.setString(i, "QQ" + item);
 					first = 1;
 				} else if(first == 1) {
 					System.out.println("TeamNumber");
 					int Team = Integer.parseInt(item);
 					AddTeamToDB(Team);
 					s.setInt(i, Team);
 					first = 2;
 				} else if(first == 2) {
 					System.out.println("Suro");
 					s.setInt(i, Integer.parseInt(item));
 					first = 1;
 				}
 				i = i + 1;
 			}
 			int ret = s.executeUpdate();
 			s.close();
 			return ret;
 		} catch (Exception e) {
 			Except.ExceptionHandler("AddMatchedToDB", e, false, true, "Something was really wrong with the match import.");
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
 			Except.ExceptionHandler("DBClose", e, false, false);
 		}
 	}
 
 	private boolean CreateEachOption(String Name, String Value, boolean Public) {
 		int Pub = 0;
 		if (Public) {
 			Pub = 1;
 		}
 		String q = "INSERT INTO OPTIONS VALUES ('" + Name + "', '" + Value + "', " + Pub + ")";
 		int cre = PerformInternalUpdateQuery(q);
 		if (cre != 1) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	private boolean CreateOptions() {
 		boolean a = CreateEachOption("SQLDBVER", SQLDBVER, false);
 		boolean b = CreateEachOption("EVENTNAME", "Mystery Event", true);
 		if (a && b) {
 			return true;
 		}
 		return false;
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
 			Except.ExceptionHandler("FetchMatch", e, false, true, "We were unable to fetch that match?");
 		}
 		System.out.println("Match Fetched. Here ya go!");
 		return ScoreList;
 	}
 
 	public List<TeamRankObj> FetchTeamlist(boolean rank){
 		System.out.println("TeamList Fetch Requested");
 		List<TeamRankObj> WholeList = new ArrayList<TeamRankObj>();
 		String rank_order = "ID";
		if(rank) rank_order = "QS DESC,AP DESC,CP DESC,TP DESC";
 		try {
 			PreparedStatement s = c.prepareStatement("SELECT * FROM TEAMS ORDER BY "+rank_order);
 			ResultSet rs = s.executeQuery();
 			while (rs.next()) {
 				TeamRankObj n = new TeamRankObj();
 				n.ID = rs.getInt("ID");
 				n.QS = rs.getInt("QS");
 				n.AP = rs.getInt("AP");
 				n.CP = rs.getInt("CP");
 				n.TP = rs.getInt("TP");
 				n.wins = rs.getInt("WINS");
 				n.ties = rs.getInt("TIES");
 				n.tot  = rs.getInt("TOT");
 				WholeList.add(n);
 			}
 		} catch (Exception e) {
 			Except.ExceptionHandler("FetchTeamList", e, false, true, "Match list can not be loaded.");
 		}
 		return WholeList;
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
 			Except.ExceptionHandler("FetchMatchList", e, false, true, "Match list can not be loaded.");
 		}
 		return WholeList;
 	}
 
 	public String FetchOption(String Name) {
 		try {
 			String q = "SELECT VAL FROM OPTIONS WHERE ID=?";
 			PreparedStatement s = c.prepareStatement(q);
 			s.setString(1, Name);
 			ResultSet rs = s.executeQuery();
 			String ret = "";
 			while (rs.next()) {
 				ret = rs.getString("VAL");
 			}
 			return ret;
 		} catch (Exception e) {
 			Except.ExceptionHandler("FetchOption", e, false, false);
 			return "";
 		}
 
 	}
 
 	public List<OptionSetPanel> FetchPublicOptions() {
 		List<OptionSetPanel> WholeList = new ArrayList<OptionSetPanel>();
 		try {
 			PreparedStatement s = c.prepareStatement("SELECT ID,VAL FROM OPTIONS WHERE PUBLIC = 1");
 			ResultSet rs = s.executeQuery();
 			while (rs.next()) {
 				OptionSetPanel newOpt = new OptionSetPanel(rs.getString("ID"), rs.getString("VAL"));
 				WholeList.add(newOpt);
 			}
 		} catch (Exception e) {
 			Except.ExceptionHandler("FetchOptionList", e, false, true, "Option list can not be loaded.");
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
 
 	private boolean GenerateNewDatabase() {
 		System.out.println("Creating new database tables..");
 		String[] clrs = { "R", "B" };
 
 		boolean Table1Success = false;
 		boolean Table2Success = false;
 
 		String q = "CREATE TABLE MATCHES (";
 		q = FormatSQLBuild(q, "ID", "TEXT", "", "PRIMARY KEY NOT NULL");
 
 		for (String clr : clrs) {
 			q = FormatSQLBuild(q, "1Robot", "INT", clr);
 			q = FormatSQLBuild(q, "1Sur", "BOOLEAN", clr);
 			q = FormatSQLBuild(q, "2Robot", "INT", clr);
 			q = FormatSQLBuild(q, "2Sur", "BOOLEAN", clr);
 			q = FormatSQLBuild(q, "3Robot", "INT", clr);
 			q = FormatSQLBuild(q, "3Sur", "BOOLEAN", clr);
 		}
 
 		for (String clr : clrs) {
 			q = FormatSQLBuild(q, "DisksLA", "INT", clr);
 			q = FormatSQLBuild(q, "DisksLT", "INT", clr);
 			q = FormatSQLBuild(q, "DisksMA", "INT", clr);
 			q = FormatSQLBuild(q, "DisksMT", "INT", clr);
 			q = FormatSQLBuild(q, "DisksHA", "INT", clr);
 			q = FormatSQLBuild(q, "DisksHT", "INT", clr);
 			q = FormatSQLBuild(q, "DisksP", "INT", clr);
 			q = FormatSQLBuild(q, "1Climb", "INT", clr);
 			q = FormatSQLBuild(q, "2Climb", "INT", clr);
 			q = FormatSQLBuild(q, "3Climb", "INT", clr);
 			q = FormatSQLBuild(q, "1Dq", "BOOLEAN", clr);
 			q = FormatSQLBuild(q, "2Dq", "BOOLEAN", clr);
 			q = FormatSQLBuild(q, "3Dq", "BOOLEAN", clr);
 			q = FormatSQLBuild(q, "Foul", "INT", clr);
 			q = FormatSQLBuild(q, "TFoul", "INT", clr);
 			q = FormatSQLBuild(q, "QS", "INT", clr);
 			q = FormatSQLBuild(q, "AP", "INT", clr);
 			q = FormatSQLBuild(q, "CP", "INT", clr);
 			q = FormatSQLBuild(q, "TP", "INT", clr);
 			q = FormatSQLBuild(q, "Score", "INT", clr);
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
 
 		q = "CREATE TABLE TEAMS (ID INT PRIMARY KEY NOT NULL, QS INT NOT NULL, AP INT NOT NULL, CP INT NOT NULL, TP INT NOT NULL, WINS INT NOT NULL, TIES INT NOT NULL, TOT INT NOT NULL)";
 		cre = PerformInternalUpdateQuery(q);
 		if (cre != 0) {
 			System.out.println("Teams Table Create failed!");
 			return false;
 		} else {
 			System.out.println("Teams Table Create successful!");
 			if (Table1Success) {
 				Table2Success = true;
 			}
 		}
 		
 		q = "CREATE TABLE OPTIONS (ID TEXT PRIMARY KEY NOT NULL, VAL TEXT NOT NULL, PUBLIC BOOLEAN NOT NULL)";
 		cre = PerformInternalUpdateQuery(q);
 		if (cre != 0) {
 			System.out.println("Options Table Create failed!");
 			return false;
 		} else {
 			System.out.println("Options Table Create successful!");
 			if (Table2Success) {
 				return true;
 			}
 			return false;
 		}
 	}
 
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
 			Except.ExceptionHandler("PerformInternalUpdt", e, true, true, "QUERY:\n" + q);
 			return -1;
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
 				q = BuildUpdateString(q, clr, "QS");
 				q = BuildUpdateString(q, clr, "AP");
 				q = BuildUpdateString(q, clr, "CP");
 				q = BuildUpdateString(q, clr, "TP");
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
 			s.setInt(16, R.QS);
 			s.setInt(17, R.AP);
 			s.setInt(18, R.CP);
 			s.setInt(19, R.TP);
 			s.setInt(20, R.Score);
 			// Blue
 			s.setInt(21, B.DisksLA);
 			s.setInt(22, B.DisksLT);
 			s.setInt(23, B.DisksMA);
 			s.setInt(24, B.DisksMT);
 			s.setInt(25, B.DisksHA);
 			s.setInt(26, B.DisksHT);
 			s.setInt(27, B.DisksP);
 			s.setInt(28, B.Climb1);
 			s.setInt(29, B.Climb2);
 			s.setInt(30, B.Climb3);
 			s.setBoolean(31, B.Dq1);
 			s.setBoolean(32, B.Dq2);
 			s.setBoolean(33, B.Dq3);
 			s.setInt(34, B.Foul);
 			s.setInt(35, B.TFoul);
 			s.setInt(36, B.QS);
 			s.setInt(37, B.AP);
 			s.setInt(38, B.CP);
 			s.setInt(39, B.TP);
 			s.setInt(40, B.Score);
 			// WHERE
 			s.setString(41, B.MatchID());
 			System.out.println("Performing DB Update..");
 			int ret = s.executeUpdate();
 			if (ret != 1) {
 				System.out.println("Update Failed."+s.getWarnings());
 				return false;
 			}
 			System.out.println("Update Complete!");
 			return true;
 		} catch (Exception e) {
 			Except.ExceptionHandler("UpdateMatch", e, false, true, "Match update failed?");
 		}
 
 		return false;
 	}
 
 	public boolean UpdateOption(String Name, String Val) {
 		try {
 			String q = "UPDATE OPTIONS SET VAL=? WHERE ID=?";
 			PreparedStatement s = c.prepareStatement(q);
 			s.setString(1, Val);
 			s.setString(2, Name);
 			int cre = s.executeUpdate();
 			if (cre != 1) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (Exception e) {
 			Except.ExceptionHandler("UpdateOption", e, false, false);
 			return false;
 		}
 
 	}
 }
