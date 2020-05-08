 /* 
  * Leks13
  * GPL v3
  */
 package ru.leks13.jabbertimer;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import org.jivesoftware.smack.XMPPException;
 
 public class Sql {
 
     private static String DBPATH;
 
     public static void setBasePath(String path) {
         DBPATH = path;
     }
 
     public static void add(Long time, String jid, Long id, String note) throws ClassNotFoundException, SQLException, XMPPException {
         Class.forName("org.sqlite.JDBC");
         Connection bd = DriverManager.getConnection("jdbc:sqlite:" + DBPATH);
         ResultSet rs;
         int i = 1;
         try (java.sql.Statement st = bd.createStatement()) {
             st.execute("create table if not exists 'TABLE1' ('time' long, 'jid' text, 'id' int, 'note' text);");
             PreparedStatement ps = bd.prepareStatement("insert into 'TABLE1' ('time', 'jid', 'id','note') values (?,?,?,?); ");
             ps.setLong(1, time);
             ps.setString(2, jid);
             ps.setLong(3, id);
             ps.setString(4, note);
             ps.execute();
             st.close();
 
         } finally {
             bd.close();
         }
     }
 
     public static ResultSet timer(Long time) throws ClassNotFoundException, SQLException, XMPPException {
         Class.forName("org.sqlite.JDBC");
         Connection bd = DriverManager.getConnection("jdbc:sqlite:" + DBPATH);
         ResultSet rs;
         int i = 1;
         try (java.sql.Statement st = bd.createStatement()) {
             st.execute("create table if not exists 'TABLE1' ('time' long, 'jid' text, 'id' int, 'note' text);");
             PreparedStatement ps = bd.prepareStatement("SELECT * FROM TABLE1 WHERE time = ?;");
             ps.setLong(1, time);
             rs = ps.executeQuery();
             while (rs.next()) {
                 long j = rs.getLong("id");
                 String jid = rs.getString("jid");
                 String msg = rs.getString("note");
                 XmppNet.sendMessage(jid, msg);
                 st.execute("DELETE FROM TABLE1 WHERE id=" + j + ";");
                 st.close();
                 i++;
             }
 
         } finally {
             bd.close();
         }
 
         return rs;
     }
 
     public static String listOfTimer(String jid, long currentTime) throws ClassNotFoundException, SQLException {
         Class.forName("org.sqlite.JDBC");
         ResultSet rs;
         Connection bd = DriverManager.getConnection("jdbc:sqlite:" + DBPATH);
        String f = "";
         java.util.Date time;
         try (java.sql.Statement st = bd.createStatement()) {
             st.execute("create table if not exists 'TABLE1' ('time' long, 'jid' text, 'id' int, 'note' text);");
             PreparedStatement ps = bd.prepareStatement("SELECT * FROM TABLE1 WHERE jid =? AND time>" + currentTime / 1000 + ";");
             ps.setString(1, jid);
             rs = ps.executeQuery();
             while (rs.next()) {
                 String timeBase = rs.getString("time");
                 String S = new SimpleDateFormat("dd.MM.yyyy HH:mm Z").format(Long.valueOf(timeBase) * 1000);
                f += S + "\n";
             }
             st.close();
            if (f.length() == 0) {
                f = "No timers";
             }
         } finally {
             bd.close();
         }

        return f;
     }
 
     public static String listOfNote(String jid) throws ClassNotFoundException, SQLException {
         Class.forName("org.sqlite.JDBC");
         ResultSet rs;
         Connection bd = DriverManager.getConnection("jdbc:sqlite:" + DBPATH);
         String res = "";
         java.util.Date time;
         try (java.sql.Statement st = bd.createStatement()) {
             st.execute("create table if not exists 'TABLE1' ('time' long, 'jid' text, 'id' int, 'note' text);");
             PreparedStatement ps = bd.prepareStatement("SELECT * FROM TABLE1 WHERE jid = ? AND time=0;");
             ps.setString(1, jid);
             rs = ps.executeQuery();
             while (rs.next()) {
                 String note = rs.getString("note");
                 String id = rs.getString("id");
                 res = "#" + id + "\n" + note + "\n\n" + res;
             }
             st.close();
         } finally {
             bd.close();
         }
         if (res.length() == 0) {
             res = "No notes";
         }
         return res;
     }
 
     public static void deleteNote(String jid, String id) throws ClassNotFoundException, SQLException {
         Class.forName("org.sqlite.JDBC");
         ResultSet rs;
         try (Connection bd = DriverManager.getConnection("jdbc:sqlite:" + DBPATH)) {
             try (java.sql.Statement st = bd.createStatement()) {
                 st.execute("create table if not exists 'TABLE1' ('time' long, 'jid' text, 'id' int, 'note' text);");
                 PreparedStatement ps = bd.prepareStatement("DELETE FROM TABLE1 WHERE id=? AND jid=?;");
                 ps.setString(1, id);
                 ps.setString(2, jid);
                 ps.execute();
                 st.close();
             } catch (SQLException e) {
             } finally {
                 bd.close();
             }
         }
     }
 }
