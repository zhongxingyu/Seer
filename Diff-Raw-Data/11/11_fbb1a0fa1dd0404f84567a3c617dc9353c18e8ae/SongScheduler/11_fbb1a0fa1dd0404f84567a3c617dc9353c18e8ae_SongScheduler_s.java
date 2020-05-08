 package model;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * A bunch of methods for generating schedules.
  * 
  * @author liufeng & aprilbugnot
  */
 public class SongScheduler {
     private Schedule[][] tentativeSchedule = new Schedule[7][24];
     private Time startTime;
 
     /**
      * Constructor for initialize the tentativeSchedule array.
      * 
      * @author kurtisschmidt & jordan
      * @param firstHour the start hour of tentativeSchedule[0][0];
      */
     public SongScheduler( Time firstHour ) {
         startTime = firstHour;
         for (int i = 0; i < 7; i++ )
         {
             for ( int j = 0; j < 24; j++ ) {
                 tentativeSchedule[i][j] = getScheduleFromDB(firstHour);
                 firstHour = firstHour.getNextHour();
             }
         }
     }
 
     /**
      * Commit the each schedules in the tentativeSchedule array
      * to the database.
      *
      * @author kurtisschmidt & jordan
      */
     public void commit(){
         //commit the schedules for each day
         for(int i=0; i < tentativeSchedule.length; i++){
             //commit the schedules for each hour of a day
             for(int j=0; j < tentativeSchedule[i].length; j++){
                 //if the schdule is not empty then commit it
                 if(!tentativeSchedule[i][j].isEmpty())
                     commitSchedule(tentativeSchedule[i][j]);
             }
         }
     }
 
     /**
      * Commit the <code>candidate</code> schedule to the database.
      *
      * @param candidate the schedule to be commited.
      */
     private void commitSchedule(Schedule candidate) {
         //if the schedule that if trying to be committed is a null pointer don't commit
         if(candidate != null){
             candidate.updateSongsInSchedule();
             try {
                 Class.forName("org.sqlite.JDBC");
                 Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
 
                 PreparedStatement prepStatement = connection.prepareStatement("SELECT * FROM schedule WHERE startTime = ?;");
                 prepStatement.setObject(1, candidate.getTime());
                 ResultSet rs = prepStatement.executeQuery();
 
                 if(rs.getRow() > 0){
                     rs.close();
                     prepStatement = connection.prepareStatement("UPDATE schedule SET duration = ? WHERE startTime = ?;");
                     prepStatement.setInt(1, candidate.getDuration());
                     prepStatement.setObject(2, candidate.getTime());
                     prepStatement.executeUpdate();
                 }else{
                     rs.close();
                     prepStatement = connection.prepareStatement("INSERT INTO schedule VALUES (?, ?);");
                     prepStatement.setObject(1, candidate.getTime());
                     prepStatement.setInt(2, candidate.getDuration());
                     prepStatement.executeUpdate();
                 }
 
                prepStatement = connection.prepareStatement("insert into songSchedule values (?, ?, ?);");
                 prepStatement.setObject(2, candidate.getTime());
 
                 Iterator<Song> songList = candidate.iterator();
                 int trackNumber = 1;
                 while (songList.hasNext()) {
                     prepStatement.setInt(3, trackNumber);
                     trackNumber++;
                     Song song = songList.next();
                     prepStatement.setInt(1, song.getAccessNumber());
                     updateSongData(song, connection);
                     prepStatement.executeUpdate();
                 }
 
                 prepStatement.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Update some of the property values for <code>song</code> to
      * the database. The changed property values are:
      *
      * <ul>
      *   <li>Increase <code>playCount</code> by 1.</li>
      *   <li>Set <code>lastPlayed</code> to the current time.</li>
      *   <li>Update the <code>priority</code>.</li>
      * </ul>
      *
      * @param song The Song object indicates the song to be updated.
      * @param connection The connection to the database.
      */
     private void updateSongData(Song song, Connection connection) {
         try {
             PreparedStatement prepStatement = connection.prepareStatement("update song set lastPlayed = ?, priority = ?, playCount = ? where accessNumber = ?;");
             song.addNumberOfPlays();
             song.setLastPlayed(new Time());
             song.updatePriority();
 
             prepStatement.setObject(1, song.getLastPlayed());
             prepStatement.setDouble(2, song.getPriority());
             prepStatement.setInt(3, song.getNumberOfPlays());
             prepStatement.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * generateOneHour
      *
      * Assuming no schedule exists at the given time, generates an
      * one-hour schedule of eligible random songs.
      *
      * @author liufeng & aprilbugnot
      * @param startTime The start time of the schedule.
      */
     public void generateOneHour(Time startTime) {
         Schedule result = getSchedule(startTime);
         ArrayList songs = Database.getSongsByPriority();
         Iterator<Object> iter = songs.iterator();
 
         result.clear();
 
         while ( result.underMin() && iter.hasNext() )
         {
             Song current = (Song)iter.next();
 
             if ( canAddThisSong( current, startTime) )
             {
                 result.add( current );
             }
 
             // do this check for preventing a song is too long that makes
             // the schedule longer than 48 minutes.
             if ( result.overMax() ) {
                 result.remove( current );
             }
         }
     }
 
     /**
      * Generate a schedule starts from <code>startTime</code> with
      * the length of <code>hours</code> hours.
      * 
      * NOTE: it simply calls the <code>generateOneHour()</code>
      *       method several times. Maybe we need to change it to
      *       a better way.
      *
      * @param startTime
      * @param hours
      */
     public void generateMultipleHours(Time startTime, int hours) {
         for (int i = 0; i < hours; i++) {
             generateOneHour(startTime);
             startTime = startTime.getNextHour();
         }
     }
 
     /**
      * Generate a schedule starts from <code>startTime</code> with
      * the length of <code>days</code> days.
      *
      * NOTE: we have the same problem as the
      * <code>generateMultipleHours()</code> method.
      *
      * @param startTime
      * @param days
      */
     public void generateDays(Time startTime, int days) {
         for (int i = 0; i < 24 * days; i++) {
             generateOneHour(startTime);
             startTime = startTime.getNextHour();
         }
     }
 
     /**
      * autoCorrect
      *
      * This function will automatically add and remove elements from a list
      * until it fits the 43-48 minute mark.  
      * 
      * Right now it is lazy by only adding the first song it finds that fits the criteria.
      * As well, the implementation has some serious downfalls. It cannot handle any
      * problems where it relies on finding songs that work.
      * @param startTime
      */
     public void autoCorrect( Time startTime )
     {
         Schedule schedule = getSchedule( startTime );
         Iterator<Song> iter = schedule.iterator();
         ArrayList songs = Database.getSongsByPriority();
         int duration;
         int postAdd;
         int postRemove;
         Song song;
 
         while ( schedule.underMin() || schedule.overMax() )
         {
             duration = schedule.getDuration();
 
             if ( schedule.underMin() )
             {
                 for ( int i = 0; i < songs.size(); i++ )
                 {
                     song = (Song)songs.get( i );
                     postAdd = duration + song.getLength();
 
                     if ( postAdd < schedule.MAX_SCHEDULE_LENGTH && canAddThisSong( song, startTime ))
                     {
                         schedule.add( song );
                         duration = schedule.getDuration();
                     }
                 }
             }
             else if ( schedule.overMax() )
             {
 
                 while( iter.hasNext() )
                 {
                     song = iter.next();
                     postRemove = duration - song.getLength();
 
                     if ( postRemove > schedule.MIN_SCHEDULE_LENGTH )
                     {
                         schedule.remove( song );
                         duration = schedule.getDuration();
                     }
                 }
             }
         }
     }
 
     /**
      * canAddThisSong
      *
      * Checks schedules before and after the startTime -- among the tentative schedules,
      * as well as the database -- to see whether they already contain the given song.
      * Returns true if we are free to add the song (i.e. the preceeding and proceeding
      * schedules do not contain the song), or false if we cannot add the song.
      *
      * @author liufeng & aprilbugnot
      * @param song
      * @param startTime
      * @param connection
      * @return boolean
      */
     private boolean canAddThisSong(Song song, Time startTime) {
         Time previousHour = startTime.getPreviousHour();
         Time nextHour = startTime.getNextHour();
         Schedule previousSchedule;
         Schedule nextSchedule;
         boolean result = true;
 
         if ( previousHour.before( this.startTime ) )
         {
             previousSchedule = getScheduleFromDB( previousHour );
         }
         else
         {
             previousSchedule = getSchedule( previousHour );
         }
 
         if ( nextHour.after( tentativeSchedule[6][23].getTime() ) )
         {
             nextSchedule = getScheduleFromDB( nextHour );
         }
         else
         {
             nextSchedule = getSchedule( nextHour );
         }
 
         if ( previousSchedule.contains(song) )
             result = false;
         if ( nextSchedule.contains(song) )
             result = false;
         return result;
     }
 
     /**
      * getScheduleFromDB
      *
      * If a tentative schedule exists for the specified time -- first check tentative schedule,
      * then check database -- return this Schedule object;
      * If no schedule exists for the specified time, return null.
      *
      * @author aprilbugnot
      * @param startTime
      * @return Schedule of the specified time, or null if no schedule exists
      */
     public Schedule getScheduleFromDB(Time startTime) {
         Schedule result = new Schedule(startTime);
         
           //get the schedule from db
           try {
                 Class.forName("org.sqlite.JDBC");
 
                 Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
 
                 PreparedStatement prepStatement = connection.prepareStatement("select song.* from song inner join songSchedule on song.accessNumber = songSchedule.accessNumber where startTime = ?;");
                 prepStatement.setObject(1, startTime);
                 ResultSet rs = prepStatement.executeQuery();
 
                 if (rs != null) {
                     result = new Schedule(startTime);
 
                     while (rs.next()) {
                         Song song = new Song(
                                 rs.getString("title"),
                                 rs.getString("performer"),
                                 rs.getString("recordingTitle"),
                                 rs.getString("recordingType"),
                                 rs.getString("year"),
                                 rs.getInt("length"),
                                 rs.getInt("accessNumber"),
                                 rs.getInt("popularity"),
                                 rs.getInt("playCount"),
                                (Time)rs.getObject("addedTime"),
                                (Time)rs.getObject("lastPlayed"),
                                 rs.getDouble("priority"));
 
                         result.add(song);
                     }
                 }
 
                 rs.close();
                 prepStatement.close();
                 connection.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         return result;
     }
 
     /**
      * Returns the Schedule held at the specified time.
      * @param time 
      */
     public Schedule getSchedule( Time time ) {
         return tentativeSchedule[time.getDay() - this.startTime.getDay()][time.getHour()];
     }
 }
