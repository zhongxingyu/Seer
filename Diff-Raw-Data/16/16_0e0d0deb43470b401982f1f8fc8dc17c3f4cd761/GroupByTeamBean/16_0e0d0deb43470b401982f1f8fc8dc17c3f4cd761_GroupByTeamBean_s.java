 package com.frcscout.teammember;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.frcscout.sql.DBConnection;
 import com.frcscout.sql.MySQLConnection;
 
 public class GroupByTeamBean {
     private Integer selectedTeam;
     private Connection conn;
     private DBConnection dbconn;
     
     public GroupByTeamBean() {
         this.dbconn = new MySQLConnection();
         this.conn = null;
         this.selectedTeam = null;
     }
     
     @SuppressWarnings("unchecked")
     public String getEventAveragesTable() {
         PreparedStatement st = null;
         ResultSet rs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
            st = conn.prepareStatement("SELECT event_id, avg(auton_top)*6 + avg(auton_middle)*4  + avg(auton_bottom)*2 as auton, avg(teleop_top)*3 + avg(teleop_middle)*2 + avg(teleop_bottom) + avg(teleop_pyramid)*5 as teleop, avg(pyramid_level)*10 as climb, avg(auton_top)*6 + avg(auton_middle)*4  + avg(auton_bottom)*2 + avg(teleop_top)*3 + avg(teleop_middle)*2 + avg(teleop_bottom) + avg(teleop_pyramid)*5 + avg(pyramid_level)*10 as total FROM match_record_2013 WHERE team_id = ? GROUP BY event_id");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("id", rs.getInt("event_id"));
                 o.put("autonomous", rs.getInt("auton"));
                 o.put("teleop", rs.getInt("teleop"));
                 o.put("climb", rs.getInt("climb"));
                 o.put("total_points", rs.getInt("total"));
                 json.add(o);
             }
             st = conn.prepareStatement("SELECT avg(auton_top)*6 + avg(auton_middle)*4  + avg(auton_bottom)*2 as auton, avg(teleop_top)*3 + avg(teleop_middle)*2 + avg(teleop_bottom) + avg(teleop_pyramid)*5 as teleop, avg(pyramid_level)*10 as climb, avg(auton_top)*6 + avg(auton_middle)*4  + avg(auton_bottom)*2 + avg(teleop_top)*3 + avg(teleop_middle)*2 + avg(teleop_bottom) + avg(teleop_pyramid)*5 + avg(pyramid_level)*10 as total FROM match_record_2013 where team_id = ?");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             if (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("id", "total");
                 o.put("autonomous", rs.getInt("auton"));
                 o.put("teleop", rs.getInt("teleop"));
                 o.put("climb", rs.getInt("climb"));
                 o.put("total_points", rs.getInt("total"));
                 json.add(o);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 conn.close();
                 st.close();
                 rs.close();
             }catch (SQLException e) {
                 System.out.println("Error closing query");
             }
         }
         return json.toString();
     }
     
     @SuppressWarnings("unchecked")
     public String getEventsLineChart() {
         PreparedStatement st = null;
         ResultSet rs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
            st = conn.prepareStatement("SELECT event_id, sum(auton_top)*6 + sum(auton_middle)*4  + sum(auton_bottom)*2 + sum(teleop_top)*3 + avg(teleop_middle)*2 + sum(teleop_bottom) + sum(teleop_pyramid)*5 + sum(pyramid_level)*10 as total FROM match_record_2013 WHERE team_id = ? GROUP BY event_id");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("id", rs.getInt("event_id"));
                 o.put("total_points", rs.getInt("total"));
                 json.add(o);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 conn.close();
                 st.close();
                 rs.close();
             }catch (SQLException e) {
                 System.out.println("Error closing query");
             }
         }
         return json.toString();
     }
     
     @SuppressWarnings("unchecked")
     public String getTeamRadarChart() {
         PreparedStatement st = null;
         ResultSet rs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
             st = conn.prepareStatement("SELECT sum(auton_top)*6 + sum(auton_middle)*4  + sum(auton_bottom)*2 as auton, sum(teleop_top)*3 + sum(teleop_middle)*2 + sum(teleop_bottom) + sum(teleop_pyramid)*5 as teleop, sum(pyramid_level)*10 as climb FROM match_record_2013 where team_id = ?");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             if (rs.next()) {
                 JSONObject o1 = new JSONObject();
                 o1.put("category", "autonomous");
                 o1.put("total", rs.getInt("auton"));
                 json.add(o1);
                 JSONObject o2 = new JSONObject();
                  o2.put("category", "teleop");
                  o2.put("total", rs.getInt("teleop"));
                  json.add(o2);
                 JSONObject o3 = new JSONObject();
                  o3.put("category", "climb");
                  o3.put("total", rs.getInt("climb"));
                  json.add(o3);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 conn.close();
                 st.close();
                 rs.close();
             }catch (SQLException e) {
                 System.out.println("Error closing query");
             }
         }
         return json.toString();
     }
     
     public String getTeamPicture() {
         //TODO
         return null;
     }
     
     @SuppressWarnings("unchecked")
     public String getTeamMatchTable() {
         PreparedStatement st = null;
         ResultSet rs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
            st = conn.prepareStatement("SELECT event_id, match_number, (auton_top)*6 + (auton_middle)*4  + (auton_bottom)*2 as auton, (teleop_top)*3 + (teleop_middle)*2 + (teleop_bottom) + (teleop_pyramid)*5 as teleop, (pyramid_level)*10 as climb, (auton_top)*6 + (auton_middle)*4  + (auton_bottom)*2 + (teleop_top)*3 + (teleop_middle)*2 + (teleop_bottom) + (teleop_pyramid)*5 + (pyramid_level)*10 as total FROM match_record_2013 WHERE team_id = ?");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("event_id", rs.getInt("event_id"));
                 o.put("match_id", rs.getInt("match_number"));
                 o.put("autonomous", rs.getInt("auton"));
                 o.put("teleop", rs.getInt("teleop"));
                 o.put("climb", rs.getInt("climb"));
                 o.put("total_points", rs.getInt("total"));
                 json.add(o);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 conn.close();
                 st.close();
                 rs.close();
             }catch (SQLException e) {
                 System.out.println("Error closing query");
             }
         }
         return json.toString();
     }
     
     public int getSelectedTeam() {
         if (this.selectedTeam != null) {
             return this.selectedTeam.intValue();
         } else {
             return -1;
         }
     }
     
     public void setSelectedTeam(int team) {
         if (team > 0) {
             this.selectedTeam = Integer.valueOf(team);
         }
     }
 }
