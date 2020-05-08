 package de.hattrickorganizer.database;
 
 import java.sql.ResultSet;
 import java.sql.Types;
 import java.util.Vector;
 
 import plugins.IMatchKurzInfo;
 import plugins.IMatchLineup;
 import plugins.ISpielePanel;
 import de.hattrickorganizer.model.matches.MatchKurzInfo;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.MyHelper;
 
 public final class MatchesKurzInfoTable extends AbstractTable {
 
 	/** tablename **/
 	public final static String TABLENAME = "MATCHESKURZINFO";
 	
 	protected MatchesKurzInfoTable(JDBCAdapter  adapter){
 		super(TABLENAME,adapter);
 	}
 	
 	@Override
 	protected void initColumns() {
 		columns = new ColumnDescriptor[11];
 		columns[0]= new ColumnDescriptor("MatchID",Types.INTEGER,false,true);
 		columns[1]= new ColumnDescriptor("MatchTyp",Types.INTEGER,false);
 		columns[2]= new ColumnDescriptor("HeimName",Types.VARCHAR,false,256);
 		columns[3]= new ColumnDescriptor("HeimID",Types.INTEGER,false);
 		columns[4]= new ColumnDescriptor("GastName",Types.VARCHAR,false,256);
 		columns[5]= new ColumnDescriptor("GastID",Types.INTEGER,false);
 		columns[6]= new ColumnDescriptor("MatchDate",Types.VARCHAR,false,256);
 		columns[7]= new ColumnDescriptor("HeimTore",Types.INTEGER,false);
 		columns[8]= new ColumnDescriptor("GastTore",Types.INTEGER,false);
 		columns[9]= new ColumnDescriptor("Aufstellung",Types.BOOLEAN,false);
 		columns[10]= new ColumnDescriptor("Status",Types.INTEGER,false);
 
 	}
 
 	@Override
 	protected String[] getCreateIndizeStatements() {
 		return new String[] { "CREATE INDEX IMATCHKURZINFO_1 ON " + getTableName() + "(" + columns[0].getColumnName() + ")" };
 	}
 	
 	/**
 	 * Wichtig: Wenn die Teamid = -1 ist muss der Matchtyp ALLE_SPIELE sein!
 	 *
 	 * @param teamId Die Teamid oder -1 für alle
 	 * @param matchtyp Welche Matches? Konstanten im SpielePanel!
 	 * @param asc TODO Missing Constructuor Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public MatchKurzInfo[] getMatchesKurzInfo(int teamId, int matchtyp, boolean asc) {
 		MatchKurzInfo[] matches = new MatchKurzInfo[0];
 		MatchKurzInfo match = null;
 		String sql = null;
 		ResultSet rs = null;
 		final Vector<IMatchKurzInfo> liste = new Vector<IMatchKurzInfo>();
 
 		//Ohne Matchid nur AlleSpiele möglich!
 		if ((teamId < 0) && (matchtyp != ISpielePanel.ALLE_SPIELE)) {
 			return matches;
 		}
 
 		try {
 			sql = "SELECT * FROM "+getTableName();
 
 			if ((teamId > -1) && (matchtyp != ISpielePanel.ALLE_SPIELE) && (matchtyp != ISpielePanel.NUR_FREMDE_SPIELE)) {
 				sql += (" WHERE ( GastID = " + teamId + " OR HeimID = " + teamId + " )");
 			}
 
 			if ((teamId > -1) && (matchtyp == ISpielePanel.NUR_FREMDE_SPIELE)) {
 				sql += (" WHERE ( GastID != " + teamId + " AND HeimID != " + teamId + " )");
 			}
 
 			//Nur eigene gewählt
 			if (matchtyp >= 10) {
 				matchtyp = matchtyp - 10;
 
 				sql += (" AND Status=" + IMatchKurzInfo.FINISHED);
 			}
 
 			//Matchtypen
 			switch (matchtyp) {
 				case ISpielePanel.NUR_EIGENE_SPIELE :
 
 					//Nix zu tun, da die teamId die einzige Einschränkung ist
 					break;
 
 				case ISpielePanel.NUR_EIGENE_PFLICHTSPIELE :
 					sql += (" AND ( MatchTyp=" + IMatchLineup.QUALISPIEL);
 					sql += (" OR MatchTyp=" + IMatchLineup.LIGASPIEL);
 					sql += (" OR MatchTyp=" + IMatchLineup.POKALSPIEL + " )");
 					break;
 
 				case ISpielePanel.NUR_EIGENE_POKALSPIELE :
 					sql += (" AND MatchTyp=" + IMatchLineup.POKALSPIEL);
 					break;
 
 				case ISpielePanel.NUR_EIGENE_LIGASPIELE :
 					sql += (" AND MatchTyp=" + IMatchLineup.LIGASPIEL);
 					break;
 
 				case ISpielePanel.NUR_EIGENE_FREUNDSCHAFTSSPIELE :
 					sql += (" AND ( MatchTyp=" + IMatchLineup.TESTSPIEL);
 					sql += (" OR MatchTyp=" + IMatchLineup.TESTPOKALSPIEL);
 					sql += (" OR MatchTyp=" + IMatchLineup.INT_TESTCUPSPIEL);
 					sql += (" OR MatchTyp=" + IMatchLineup.INT_TESTSPIEL + " )");
 					break;
 			}
 
 			//nicht desc
 			sql += " ORDER BY MatchDate";
 
 			if (!asc) {
 				sql += " DESC";
 			}
 
 			rs = adapter.executeQuery(sql);
 
 			rs.beforeFirst();
 
 			while (rs.next()) {
 				match = new MatchKurzInfo();
 				match.setMatchDate(rs.getString("MatchDate"));
 				match.setGastID(rs.getInt("GastID"));
 				match.setGastName(DBZugriff.deleteEscapeSequences(rs.getString("GastName")));
 				match.setHeimID(rs.getInt("HeimID"));
 				match.setHeimName(DBZugriff.deleteEscapeSequences(rs.getString("HeimName")));
 				match.setMatchID(rs.getInt("MatchID"));
 				match.setGastTore(rs.getInt("GastTore"));
 				match.setHeimTore(rs.getInt("HeimTore"));
 
 				match.setMatchTyp(rs.getInt("MatchTyp"));
 				match.setMatchStatus(rs.getInt("Status"));
 				match.setAufstellung(rs.getBoolean("Aufstellung"));
 
 				//Adden
 				liste.add(match);
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(),"DB.getMatchesKurzInfo Error" + e);
 		}
 
 		matches = new MatchKurzInfo[liste.size()];
 		MyHelper.copyVector2Array(liste, matches);
 
 		return matches;
 	}
 	
 	/**
 	 * Check if a match is already in the database.
 	 */
 	public boolean isMatchVorhanden(int matchid) {
 		boolean vorhanden = false;
 
 		try {
 			final String sql = "SELECT MatchId FROM "+getTableName()+" WHERE MatchId=" + matchid;
 			final ResultSet rs = adapter.executeQuery(sql);
 
 			rs.beforeFirst();
 
 			if (rs.next()) {
 				vorhanden = true;
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(),"DatenbankZugriff.isMatchVorhanden : " + e);
 		}
 
 		return vorhanden;
 	}
 
 	/////////////////////////////////////////////////////////////////////////////////
 	//MatchesASP MatchKurzInfo
 	////////////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Get all matches with a certain status for the given team from the database.
 	 * 
 	 * @param teamId the teamid or -1 for all matches
 	 * @param matchStatus the match status (e.g. IMatchKurzInfo.UPCOMING) or -1 to ignore this parameter
 	 */
 	public MatchKurzInfo[] getMatchesKurzInfo(final int teamId, final int matchStatus) {
 		MatchKurzInfo[] matches = new MatchKurzInfo[0];
 		MatchKurzInfo match = null;
 		String sql = null;
 		ResultSet rs = null;
 		final Vector<IMatchKurzInfo> liste = new Vector<IMatchKurzInfo>();
 
 		try {
 			sql = "SELECT * FROM "+getTableName();
 
 			if (teamId > -1 && matchStatus > -1) {
				sql += (" WHERE GastID=" + teamId + " OR HeimID=" + teamId + " AND Status=" + matchStatus);
 			} else if (teamId > -1) {
 				sql += (" WHERE GastID=" + teamId + " OR HeimID=" + teamId);
			} else if (teamId > -1) {
 				sql += (" WHERE Status=" + matchStatus);
 			}
 
 			sql += " ORDER BY MatchDate DESC";
 			rs = adapter.executeQuery(sql);
			//rs.beforeFirst();
 
 			while (rs.next()) {
 				match = new MatchKurzInfo();
 				match.setMatchDate(rs.getString("MatchDate"));
 				match.setGastID(rs.getInt("GastID"));
 				match.setGastName(DBZugriff.deleteEscapeSequences(rs.getString("GastName")));
 				match.setHeimID(rs.getInt("HeimID"));
 				match.setHeimName(DBZugriff.deleteEscapeSequences(rs.getString("HeimName")));
 				match.setMatchID(rs.getInt("MatchID"));
 				match.setGastTore(rs.getInt("GastTore"));
 				match.setHeimTore(rs.getInt("HeimTore"));
 				match.setMatchTyp(rs.getInt("MatchTyp"));
 				match.setMatchStatus(rs.getInt("Status"));
 				match.setAufstellung(rs.getBoolean("Aufstellung"));
 				liste.add(match);
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(),"DB.getMatchesKurzInfo Error" + e);
 		}
 
 		matches = new MatchKurzInfo[liste.size()];
		MyHelper.copyVector2Array(liste, matches);

 		return matches;
 	}
 	
 	/**
 	 * Get all matches for the given team from the database.
 	 * 
 	 * @param teamId the teamid or -1 for all matches
 	 */
 	public MatchKurzInfo[] getMatchesKurzInfo(final int teamId) {
 		return getMatchesKurzInfo(teamId, -1);
 	}
 
 	/**
 	 * Saves matches into the databse.
 	 */
 	public void storeMatchKurzInfos(MatchKurzInfo[] matches) {
 		String sql = null;
 		final String[] where = { "MatchID" };
 		final String[] werte = new String[1];
 
 		for (int i = 0;(matches != null) && (i < matches.length); i++) {
 			werte[0] = "" + matches[i].getMatchID();
 			delete(where, werte);
 		
 			try {
 				//insert vorbereiten
 				sql = "INSERT INTO "+getTableName()+" (  MatchID, MatchTyp, HeimName, HeimID, GastName, GastID, MatchDate, HeimTore, GastTore, Aufstellung, Status ) VALUES(";
 				sql += (matches[i].getMatchID() + "," + matches[i].getMatchTyp() + ", '"
 						+ DBZugriff.insertEscapeSequences(matches[i].getHeimName()) + "', " + matches[i].getHeimID() + ", '"
 						+ DBZugriff.insertEscapeSequences(matches[i].getGastName()) + "', ");
 				sql += (matches[i].getGastID() + ", '" + matches[i].getMatchDate() + "', " + matches[i].getHeimTore() + ", "
 						+ matches[i].getGastTore() + ", " + matches[i].isAufstellung() + ", " + matches[i].getMatchStatus() + " )");
 				adapter.executeUpdate(sql);
 			} catch (Exception e) {
 				HOLogger.instance().log(getClass(),"DB.storeMatchKurzInfos Error" + e);
 				HOLogger.instance().log(getClass(),e);
 			}
 		}
 	}
 	
 }
