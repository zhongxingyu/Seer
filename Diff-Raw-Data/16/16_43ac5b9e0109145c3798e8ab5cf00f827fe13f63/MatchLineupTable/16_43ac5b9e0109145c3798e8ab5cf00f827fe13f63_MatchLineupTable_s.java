 package ho.core.db;
 
 import ho.core.model.match.MatchLineup;
 import ho.core.model.match.MatchType;
 import ho.core.util.HOLogger;
 
 import java.sql.ResultSet;
 import java.sql.Types;
 
 
 public final class MatchLineupTable extends AbstractTable {
 
 	/** tablename **/
 	public final static String TABLENAME = "MATCHLINEUP";
 	
 	protected MatchLineupTable(JDBCAdapter  adapter){
 		super(TABLENAME,adapter);
 	}
 
 	@Override
 	protected void initColumns() {
 		columns = new ColumnDescriptor[10];
 		columns[0]= new ColumnDescriptor("MatchID",Types.INTEGER,false,true);
 		columns[1]= new ColumnDescriptor("MatchTyp",Types.INTEGER,false);
 		columns[2]= new ColumnDescriptor("HeimName",Types.VARCHAR,false,256);
 		columns[3]= new ColumnDescriptor("HeimID",Types.INTEGER,false);
 		columns[4]= new ColumnDescriptor("GastName",Types.VARCHAR,false,256);
 		columns[5]= new ColumnDescriptor("GastID",Types.INTEGER,false);
 		columns[6]= new ColumnDescriptor("FetchDate",Types.VARCHAR,false,256);
 		columns[7]= new ColumnDescriptor("MatchDate",Types.VARCHAR,false,256);
 		columns[8]= new ColumnDescriptor("ArenaID",Types.INTEGER,false);
 		columns[9]= new ColumnDescriptor("ArenaName",Types.VARCHAR,false,256);
 
 	}
 	
 	@Override
 	protected String[] getCreateIndizeStatements() {
 		return new String[] {
 			"CREATE INDEX IMATCHLINEUP_1 ON " + getTableName() + "(" + columns[0].getColumnName() + ")"};
 	}	
 
 	/**
 	 * TODO Missing Method Documentation
 	 *
 	 * @param matchID TODO Missing Method Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	MatchLineup getMatchLineup(int matchID) {
 		MatchLineup lineup = null;
 		String sql = null;
 		ResultSet rs = null;
 
 		try {
 			sql = "SELECT * FROM "+getTableName()+" WHERE MatchID = " + matchID;
 
 			rs = adapter.executeQuery(sql);
 
 			rs.first();
 
 			// Plan auslesen
 			lineup = new MatchLineup();
 			lineup.setArenaID(rs.getInt("ArenaID"));
 			lineup.setArenaName(DBManager.deleteEscapeSequences(rs.getString("ArenaName")));
 			lineup.setFetchDatum(rs.getString("FetchDate"));
 			lineup.setGastId(rs.getInt("GastID"));
 			lineup.setGastName(DBManager.deleteEscapeSequences(rs.getString("GastName")));
 			lineup.setHeimId(rs.getInt("HeimID"));
 			lineup.setHeimName(DBManager.deleteEscapeSequences(rs.getString("HeimName")));
 			lineup.setMatchID(matchID);
 			lineup.setMatchTyp(MatchType.getById(rs.getInt("MatchTyp")));
 			lineup.setSpielDatum(rs.getString("MatchDate"));
 
 			lineup.setHeim(DBManager.instance().getMatchLineupTeam(matchID, lineup.getHeimId()));
 			lineup.setGast(DBManager.instance().getMatchLineupTeam(matchID, lineup.getGastId()));
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(),"DB.getMatchLineup Error" + e);
 
 			//HOLogger.instance().log(getClass(),e);
 			lineup = null;
 		}
 
 		return lineup;
 	}
 
 	/**
 	 * Ist das Match schon in der Datenbank vorhanden?
 	 *
 	 * @param matchid TODO Missing Constructuor Parameter Documentation
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	boolean isMatchLineupVorhanden(int matchid) {
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
 
 	/**
 	 * speichert ein Matchlineup
 	 *
 	 * @param lineup TODO Missing Constructuor Parameter Documentation
 	 */
 	void storeMatchLineup(MatchLineup lineup) {
 		if (lineup != null) {
 			//Vorhandene Einträge entfernen
 			final String[] where = { "MatchID" };
 			final String[] werte = { "" + lineup.getMatchID()};			
 			delete(where, werte);
 
 			String sql = null;
 			//saven
 			try {
 				//insert vorbereiten
 				sql = "INSERT INTO "+getTableName()+" ( MatchID, MatchTyp, HeimName, HeimID, GastName, GastID, FetchDate, MatchDate, ArenaID, ArenaName ) VALUES(";
 				sql
 					+= (lineup.getMatchID()
 						+ ","
						+ lineup.getMatchTyp()
 						+ ", '"
 						+ DBManager.insertEscapeSequences(lineup.getHeimName())
 						+ "',"
 						+ lineup.getHeimId()
 						+ ",'"
 						+ DBManager.insertEscapeSequences(lineup.getGastName())
 						+ "', "
 						+ lineup.getGastId()
 						+ ", '"
 						+ lineup.getStringFetchDate()
 						+ "', '"
 						+ lineup.getStringSpielDate()
 						+ "', "
 						+ lineup.getArenaID()
 						+ ", '"
 						+ DBManager.insertEscapeSequences(lineup.getArenaName())
 						+ "' )");
 				adapter.executeUpdate(sql);
 
 				//Einträge noch saven
 				DBManager.instance().storeMatchLineupTeam((ho.core.model.match.MatchLineupTeam) lineup.getHeim(), lineup.getMatchID());
 				DBManager.instance().storeMatchLineupTeam((ho.core.model.match.MatchLineupTeam) lineup.getGast(), lineup.getMatchID());
 			} catch (Exception e) {
 				HOLogger.instance().log(getClass(),"DB.storeMatchLineup Error" + e);
 				HOLogger.instance().log(getClass(),e);
 			}
 		}
 	}
 	
 	void updateMatchLineup(MatchLineup lineup) {
 		
 		// first - delete all old players.
 		((MatchLineupPlayerTable) DBManager.instance().getTable(MatchLineupPlayerTable.TABLENAME)).deleteMatchLineupPlayers(lineup.getMatchID());
 		
 		// and store new
 		storeMatchLineup(lineup);
 		
 		// Substitutions should be safely taken care of without a special delete
 	}
 	
 }
