 package model;
 
 import java.sql.*;
 import java.io.*;
 import java.util.ArrayList;
 
 /**
  * A bunch of methods for operating the database.
  *
  * @author liufeng & aprilbugnot
  * @author jordan & kurtisschmidt
  */
 public abstract class Database {
     /**
      * Initialize the database. Creates three tables.
      */
     public static void init() {
         try {
             Class.forName("org.sqlite.JDBC");
 
             Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
             Statement statement = connection.createStatement();
             
             statement.addBatch("CREATE TABLE IF NOT EXISTS song(accessNumber INTEGER PRIMARY KEY, title TEXT, performer TEXT, recordingTitle TEXT, recordingType TEXT, year TEXT, length INTEGER, popularity INTEGER, playCount INTEGER, addedTime DATETIME, lastPlayed DATETIME, priority REAL);");
             statement.addBatch("CREATE TABLE IF NOT EXISTS schedule(startTime DATETIME PRIMARY KEY, duration INTEGER);");
             statement.addBatch("CREATE TABLE IF NOT EXISTS songSchedule(accessNumber INTEGER CONSTRAINT fk_song_id REFERENCES song(accessNumber), startTime DATETIME CONSTRAINT fk_schedule_id REFERENCES schedule(startTime) ON DELETE CASCADE, trackNumber INTEGER, PRIMARY KEY(startTime, trackNumber));");
             statement.addBatch("CREATE TRIGGER IF NOT EXISTS fki_songSchedule_accessNumber_song_accessNumber BEFORE INSERT ON [songSchedule] FOR EACH ROW BEGIN   SELECT RAISE(ROLLBACK, 'insert on table \"songSchedule\" violates foreign key constraint \"fki_songSchedule_accessNumber_song_accessNumber\"')   WHERE NEW.accessNumber IS NOT NULL AND (SELECT accessNumber FROM song WHERE accessNumber = NEW.accessNumber) IS NULL;END;");
             statement.addBatch("CREATE TRIGGER IF NOT EXISTS fku_songSchedule_accessNumber_song_accessNumber BEFORE UPDATE ON [songSchedule]  FOR EACH ROW BEGIN     SELECT RAISE(ROLLBACK, 'update on table \"songSchedule\" violates foreign key constraint \"fku_songSchedule_accessNumber_song_accessNumber\"')       WHERE NEW.accessNumber IS NOT NULL AND (SELECT accessNumber FROM song WHERE accessNumber = NEW.accessNumber) IS NULL;END;");
             statement.addBatch("CREATE TRIGGER IF NOT EXISTS fkd_songSchedule_accessNumber_song_accessNumber BEFORE DELETE ON song FOR EACH ROW BEGIN   SELECT RAISE(ROLLBACK, 'delete on table \"song\" violates foreign key constraint \"fkd_songSchedule_accessNumber_song_accessNumber\"')   WHERE (SELECT accessNumber FROM songSchedule WHERE accessNumber = OLD.accessNumber) IS NOT NULL;END;");
             statement.addBatch("CREATE TRIGGER IF NOT EXISTS fki_songSchedule_startTime_schedule_startTime BEFORE INSERT ON [songSchedule] FOR EACH ROW BEGIN   SELECT RAISE(ROLLBACK, 'insert on table \"songSchedule\" violates foreign key constraint \"fki_songSchedule_startTime_schedule_startTime\"')   WHERE NEW.startTime IS NOT NULL AND (SELECT startTime FROM schedule WHERE startTime = NEW.startTime) IS NULL;END;");
             statement.addBatch("CREATE TRIGGER IF NOT EXISTS fku_songSchedule_startTime_schedule_startTime BEFORE UPDATE ON [songSchedule]  FOR EACH ROW BEGIN     SELECT RAISE(ROLLBACK, 'update on table \"songSchedule\" violates foreign key constraint \"fku_songSchedule_startTime_schedule_startTime\"')       WHERE NEW.startTime IS NOT NULL AND (SELECT startTime FROM schedule WHERE startTime = NEW.startTime) IS NULL;END;");
             statement.addBatch("CREATE TRIGGER IF NOT EXISTS fkdc_songSchedule_startTime_schedule_startTime BEFORE DELETE ON schedule FOR EACH ROW BEGIN      DELETE FROM songSchedule WHERE songSchedule.startTime = OLD.startTime;END;");
             statement.executeBatch();
             connection.close();
             statement.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * updates the new song data from the library text file.
      * 
      * Adds the following 12 info into the song table:
      *
      * <ol>
      *   <li>accessNumber</li>
      *   <li>title</li>
      *   <li>performer</li>
      *   <li>recordingTitle</li>
      *   <li>recordingType</li>
      *   <li>year</li>
      *   <li>length</li>
      *   <li>popularity</li>
      *   <li>playCount</li>
      *   <li>addedTime</li>
      *   <li>lastPlayed</li>
      *   <li>priority</li>
      * </ol>
      *
      * NOTE: should be executed at least once a week.
      */
     public static void updateSongInfo() {
         try {
             Class.forName("org.sqlite.JDBC");
             Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
             
             BufferedReader in = new BufferedReader(new FileReader("library.txt"));
             String line = in.readLine();
             while (line != null) {
                 String[] token = line.split(";");
                 String title = token[0];
                 String performer = token[1];
                 String recordingTitle = token[2];
                 String recordingType = token[3];
                 String year = token[4];
                 int length = Integer.parseInt(token[5]);
                 int accessNumber = Integer.parseInt(token[6]);
                 int popularity = Integer.parseInt(token[7]);
                 int playCount = Integer.parseInt(token[8]);
                 Time addedTime = new Time().getCurrentTime();
                 Time lastPlayed = null;
                 double priority = 0;
 
                 PreparedStatement prepStatement = connection.prepareStatement("insert into song values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                 prepStatement.setInt(1, accessNumber);
                 prepStatement.setString(2, title);
                 prepStatement.setString(3, performer);
                 prepStatement.setString(4, recordingTitle);
                 prepStatement.setString(5, recordingType);
                 prepStatement.setString(6, year);
                 prepStatement.setInt(7, length);
                 prepStatement.setInt(8, popularity);
                 prepStatement.setInt(9, playCount);
                 prepStatement.setObject(10, addedTime);
                 prepStatement.setObject(11, lastPlayed);
                 prepStatement.setDouble(12, priority);
                 prepStatement.addBatch();
 
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("select * from song where accessNumber = " + accessNumber + ";");
                 if (!rs.next()) {
 
                     connection.setAutoCommit(false);
                     prepStatement.executeBatch();
                     connection.setAutoCommit(true);
                 }
 
                 line = in.readLine();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Get song from database ordered by priority and title
      *
      * @return an <code>ArrayList</code> instance.
      */
     public static ArrayList getSongsByPriority(){
         String sqlStatement = "SELECT * FROM song ORDER BY priority, title;";
 
         return getSongsFromDB(sqlStatement);
     }
 
     /**
      * Get a list of songs from database by the title of the song.
      * @param titls <code>String</code> instance of the titls of the song
      * @return If <code>title</code> is null, return an <code>ArrayList</code>
      *         holding all songs in database ordered by title;
      *         otherwise return an <code>ArrayList</code> holding only
      *         the song with the title in param.
      */
     public static ArrayList getSongs( String title ){
         String sqlStatement;
 
         if(title == null){
             sqlStatement = "SELECT * FROM song ORDER BY title;";
         }else{
            sqlStatement = "SELECT * FROM song WHERE title = \"" + title + "\" ORDER BY title;";
         }
 
         return getSongsFromDB(sqlStatement);
     }
 
     /**
      * Get songs from database by the SQL statement supplied in param.
      * @param sqlStatement
      * @return an <code>ArrayList</code>
      */
     private static ArrayList getSongsFromDB(String sqlStatement){
         ArrayList songs = new ArrayList();
         Song currSong;
         if(sqlStatement != null && !sqlStatement.equals("")){
             try {
                 Class.forName("org.sqlite.JDBC");
                 Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sqlStatement);
 
                 while (rs.next()) {
                     String title = rs.getString("title");
                     String performer = rs.getString("performer");
                     String recordingTitle = rs.getString("recordingTitle");
                     String recordingType = rs.getString("recordingType");
                     String year = rs.getString("year");
                     int    length = rs.getInt("length");
                     int    accessNumber = rs.getInt("accessNumber");
                     int    popularity = rs.getInt("popularity");
                     int    playCount = rs.getInt("playCount");
                     Time   addedTime = new Time(rs.getString("addedTime"));
                     Time   lastPlayed = new Time(rs.getString("lastPlayed"));
                     double priority = rs.getDouble("priority");
 
                     currSong = new Song(title, performer, recordingTitle, recordingType, year, length, accessNumber, popularity, playCount, addedTime, lastPlayed, priority);
                     songs.add(currSong);
                 }
                 rs.close();
                 statement.close();
                 connection.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return songs;
     }
 
     /**
      * Modify the <code>popularity</code> of the song in the database.
      * @param title the title of the song to be modified.
      * @param newPopularity the new popularity of the song.
      */
     public static void changeSongPopularity( String title, int newPopularity){
         if(title != null && !title.equals("")){
             String sql = "UPDATE song SET popularity = " + newPopularity +
                    " WHERE title = \"" + title + "\";";
 
             try {
                 Class.forName("org.sqlite.JDBC");
                 Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
                 Statement statement = connection.createStatement();
                 statement.executeQuery(sql);
 
                 statement.close();
                 connection.close();
             } catch (Exception e) {
                 if(!e.toString().equals("java.sql.SQLException: query does not return ResultSet"))
                     e.printStackTrace();
             }
         }
     }
 
     /**
      * Modify the <code>priority</code> of the song to the database.
      * @param title the title of the song to be modified.
      * @param priority
      */
     static void changeSongPriority(String title, double priority) {
         if (title != null && !title.equals("")) {
             String sql = "update song set priority = " + priority +
                    " where title = \"" + title + "\";";
 
             try {
                 Class.forName("org.sqlite.JDBC");
                 Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
                 Statement statement = connection.createStatement();
                 statement.executeQuery(sql);
 
                 statement.close();
                 connection.close();
             } catch (Exception e) {
                 if(!e.toString().equals("java.sql.SQLException: query does not return ResultSet"))
                     e.printStackTrace();
             }
         }
     }
 
     /**
      * Saves the current play count for a song to the database
      * @param song the song for which the play count is to be saved to the database
      */
     public static void savePlayCount(Song song){
         if(song != null){
             String sql = "update song set playCount = " + song.getNumberOfPlays() +
                    " where title = \"" + song.getTitle() + "\";";
 
             try {
                 Class.forName("org.sqlite.JDBC");
                 Connection connection = DriverManager.getConnection("jdbc:sqlite:song.db");
                 Statement statement = connection.createStatement();
                 statement.executeQuery(sql);
 
                 statement.close();
                 connection.close();
             } catch (Exception e) {
                System.out.println(sql);
                 if(!e.toString().equals("java.sql.SQLException: query does not return ResultSet"))
                     e.printStackTrace();
             }
         }
     }
 }
