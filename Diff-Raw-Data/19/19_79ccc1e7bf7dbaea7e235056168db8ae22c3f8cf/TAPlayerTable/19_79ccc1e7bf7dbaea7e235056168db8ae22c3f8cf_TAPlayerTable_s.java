 package ho.core.db;
 
 import ho.core.model.UserParameter;
 import ho.core.util.HTCalendarFactory;
 import ho.module.teamAnalyzer.vo.PlayerInfo;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Calendar;
 
 /**
  * The Table UserConfiguration contain all User properties.
  * CONFIG_KEY = Primary Key, fieldname of the class
  * CONFIG_VALUE = value of the field, save as VARCHAR. Convert to right datatype if loaded
  * 
  * @since 1.36
  *
  */
 final class TAPlayerTable extends AbstractTable {
 	final static String TABLENAME = "TA_PLAYER";
 
 	protected TAPlayerTable(JDBCAdapter adapter) {
 		super(TABLENAME, adapter);
 	}
 
 	@Override
 	protected void initColumns() {
 		columns = new ColumnDescriptor[9];
		columns[0] = new ColumnDescriptor("TEAMID", Types.INTEGER, false, true);
 		columns[1] = new ColumnDescriptor("PLAYERID", Types.INTEGER, true );
 		columns[2] = new ColumnDescriptor("STATUS", Types.INTEGER, true );
 		columns[3] = new ColumnDescriptor("SPECIALEVENT", Types.INTEGER, true );
 		columns[4] = new ColumnDescriptor("TSI", Types.INTEGER, true );
 		columns[5] = new ColumnDescriptor("FORM", Types.INTEGER, true );
 		columns[6] = new ColumnDescriptor("AGE", Types.INTEGER, true );
 		columns[7] = new ColumnDescriptor("EXPERIENCE", Types.INTEGER, true );
 		columns[8] = new ColumnDescriptor("WEEK", Types.INTEGER, true );
 		
 	}
 
     PlayerInfo getPlayerInfo(int playerId, int week, int season) {
         int weekNumber = week + (season * 16);
 
         String query = "select * from TA_PLAYER where PLAYERID=" + playerId
                        + " and week=" + weekNumber;
         
         ResultSet rs = DBManager.instance().getAdapter().executeQuery(query);
 
         try {
             rs.next();
             PlayerInfo info = new PlayerInfo();
             info.setPlayerId(playerId);
             info.setAge(rs.getInt("AGE"));
             info.setForm(rs.getInt("FORM"));
             info.setTSI(rs.getInt("TSI"));
             info.setSpecialEvent(rs.getInt("SPECIALEVENT"));
             info.setTeamId(rs.getInt("TEAMID"));
             info.setExperience(rs.getInt("EXPERIENCE"));
             info.setStatus(rs.getInt("STATUS"));
             return info;
         } catch (SQLException e) {
             return new PlayerInfo();
         }
     }
 
     /**
      * Returns the specialEvent code for a player
      *
      * @param playerId the playerId
      *
      * @return a numeric code
      */
     PlayerInfo getPlayerInfo(int playerId) {
         Integer HTWeek = getCurrentHTWeek();
         Integer HTSeason = getCurrentHTSeason();
         return getPlayerInfo(playerId, HTWeek, HTSeason);
     }
     
     PlayerInfo getPreviousPlayeInfo(int playerId) {
         ResultSet rs = DBManager.instance().getAdapter().executeQuery("SELECT WEEK FROM TA_PLAYER WHERE WEEK<"
                                                                     + getCurrentWeekNumber()
                                                                     + " AND PLAYERID=" + playerId
                                                                     + " ORDER BY WEEK DESC");
 
         try {
             if (rs.next()) {
                 int week = rs.getInt("WEEK");
                 int season = week / 16;
                 int wk = week % 16;
                 return getPlayerInfo(playerId, wk, season);
             }
         } catch (SQLException e) {
         }
 
         return new PlayerInfo();
     }
     
 	   /**
      * Add a player to a team
      *
      * @param info
      */
     void addPlayer(PlayerInfo info) {
     	DBManager.instance().getAdapter().executeUpdate("insert into TA_PLAYER values ("
                                                       + info.getTeamId() + ", "
                                                       + info.getPlayerId() + ", "
                                                       + info.getStatus() + " , "
                                                       + info.getSpecialEvent() + ", "
                                                       + info.getTSI() + ", " + info.getForm()
                                                       + ", " + info.getAge() + ", "
                                                       + info.getExperience() + ", "
                                                       + getCurrentWeekNumber() + ")");
     }
     
 	void updatePlayer(PlayerInfo info) {
     	DBManager.instance().getAdapter().executeUpdate("update TA_PLAYER set "
                                                       + "   SPECIALEVENT=" + info.getSpecialEvent()
                                                       + " , TSI=" + info.getTSI() + " , FORM="
                                                       + +info.getForm() + " , AGE=" + info.getAge()
                                                       + " , EXPERIENCE=" + info.getExperience()
                                                       + " , STATUS=" + info.getStatus()
                                                       + " where PLAYERID=" + info.getPlayerId()
                                                       + " and WEEK=" + getCurrentWeekNumber());
     }
 	
 	private Integer getCurrentHTSeason() {
         Calendar date = Calendar.getInstance();
         date.add(Calendar.HOUR, UserParameter.instance().TimeZoneDifference);
         return HTCalendarFactory.getHTSeason(date.getTime());
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private Integer getCurrentHTWeek() {
         Calendar date = Calendar.getInstance();
         date.add(Calendar.HOUR, UserParameter.instance().TimeZoneDifference);
         return HTCalendarFactory.getHTWeek(date.getTime());
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getCurrentWeekNumber() {
         return getCurrentHTWeek() + (getCurrentHTSeason() * 16);
     }
 	
     void deleteOldPlayers(){
     	adapter.executeUpdate("DELETE FROM TA_PLAYER WHERE WEEK<"
                 + (getCurrentWeekNumber() - 10));
     }
 
 }
