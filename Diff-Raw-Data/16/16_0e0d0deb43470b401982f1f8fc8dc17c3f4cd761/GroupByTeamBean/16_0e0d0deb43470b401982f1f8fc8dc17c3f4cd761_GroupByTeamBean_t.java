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
            st = conn.prepareStatement("SELECT m.event_id, e.name, avg(m.auton_top)*6 + avg(m.auton_middle)*4  + avg(m.auton_bottom)*2 as auton, avg(m.teleop_top)*3 + avg(m.teleop_middle)*2 + avg(m.teleop_bottom) + avg(m.teleop_pyramid)*5 as teleop, avg(m.pyramid_level)*10 as climb, avg(m.auton_top)*6 + avg(m.auton_middle)*4  + avg(m.auton_bottom)*2 + avg(m.teleop_top)*3 + avg(m.teleop_middle)*2 + avg(m.teleop_bottom) + avg(m.teleop_pyramid)*5 + avg(m.pyramid_level)*10 as total FROM match_record_2013 m, events e WHERE m.team_id = ? and m.event_id = e.id GROUP BY event_id");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("id", rs.getInt("event_id"));
                o.put("name", rs.getString("name"));
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
                o.put("name", "total");
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
            st = conn.prepareStatement("SELECT m.event_id, e.name, sum(m.auton_top)*6 + sum(m.auton_middle)*4  + sum(m.auton_bottom)*2 + sum(m.teleop_top)*3 + sum(m.teleop_middle)*2 + sum(m.teleop_bottom) + sum(m.teleop_pyramid)*5 + sum(m.pyramid_level)*10 as total FROM match_record_2013 m, events e WHERE team_id = ? and m.event_id = e.id GROUP BY event_id");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("id", rs.getInt("event_id"));
                o.put("name", rs.getString("name"));
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
            st = conn.prepareStatement("SELECT m.event_id, e.name, m.match_number, (m.auton_top)*6 + (m.auton_middle)*4  + (m.auton_bottom)*2 as auton, (m.teleop_top)*3 + (m.teleop_middle)*2 + (m.teleop_bottom) + (m.teleop_pyramid)*5 as teleop, (m.pyramid_level)*10 as climb, (m.auton_top)*6 + (m.auton_middle)*4  + (m.auton_bottom)*2 + (m.teleop_top)*3 + (m.teleop_middle)*2 + (m.teleop_bottom) + (m.teleop_pyramid)*5 + (m.pyramid_level)*10 as total FROM match_record_2013 m, events e WHERE team_id = ? AND m.event_id = e.id");
             st.setInt(1, getSelectedTeam());
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("event_id", rs.getInt("event_id"));
                o.put("event_name", rs.getString("name"));
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
