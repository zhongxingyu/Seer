 package com.frcscout.admin;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.frcscout.sql.DBConnection;
 import com.frcscout.sql.MySQLConnection;
 import com.frcscout.util.StringUtil;
 
 public class UserAccountBean {
     public enum Role {
         team_member, scout, administrator
     }
 
     private Integer id;
     private String email;
     private String password;
     private String firstName;
     private String lastName;
     private Role role;
     private Connection conn;
     private DBConnection dbconn;
     
     public UserAccountBean() {
         dbconn = new MySQLConnection();
         id = null;
         email = null;
         password = null;
         firstName = null;
         lastName = null;
         role = null;
     }
     
     public void loadUser(String id) {
         if (!StringUtil.isBlank(id)) {
             PreparedStatement st = null;
             ResultSet rs = null;
             try {
                 conn = dbconn.getConnection();
                 st = conn.prepareStatement("SELECT * FROM users WHERE id = " + id + " limit 1");
                 rs = st.executeQuery();
                 if (rs.next()) {
                     setId(rs.getInt("id"));
                     setEmail(rs.getString("email"));
                     setPassword(rs.getString("password"));
                     setFirstName(rs.getString("first_name"));
                     setLastName(rs.getString("last_name"));
                 } else {
                     System.out.println("user " + this.email.toString() + " not found");
                 }
                 st = conn.prepareStatement("SELECT * FROM user_roles WHERE email = '" + email + "'");
                 rs = st.executeQuery();
                 rs.last();
                 setRole(rs.getRow());
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
         }
     }
     
     @SuppressWarnings("unchecked")
     public String loadUsers() {
         PreparedStatement st = null;
         PreparedStatement ust = null;
         ResultSet rs = null;
         ResultSet urs = null;
         JSONArray json = new JSONArray();
         try {
             conn = dbconn.getConnection();
             st = conn.prepareStatement("SELECT * FROM users WHERE active = 1");
             rs = st.executeQuery();
             while (rs.next()) {
                 JSONObject o = new JSONObject();
                 o.put("id", rs.getInt("id"));
                 o.put("email", rs.getString("email"));
                 o.put("first_name", rs.getString("first_name"));
                 o.put("last_name", rs.getString("last_name"));
                 ust = conn.prepareStatement("SELECT * FROM user_roles WHERE email = '" + rs.getString("email") + "'");
                 urs = ust.executeQuery();
                 if (urs.next()) {
                     urs.last();
                     Role s = intToRole(urs.getRow());
                     o.put("role", s.toString());
                 }
                 json.add(o);
             }
             urs.close();
             ust.close();
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
     
     public void updateUser() {
         if (id != null) {
             PreparedStatement st = null;
             String q = "UPDATE users SET email = ?, first_Name = ?, last_name = ? ";
             if (!StringUtil.isBlank(password)) {
                 q += ",password = MD5(?)";
             }
             q += "WHERE id = ?";
             try {
                 conn = dbconn.getConnection();
                 conn.setAutoCommit(false);
                 st = conn.prepareStatement(q);
                 st.setString(1, this.email);
                 st.setString(2, this.firstName);
                 st.setString(3, this.lastName);
                 if (!StringUtil.isBlank(password)) {
                     st.setString(4, this.password);
                     st.setInt(5, this.id);
                 } else {
                     st.setInt(4, this.id);
                 }
                 st.executeUpdate();
                deleteUserRoles(email, conn);
                insertUserRoles(email, role, conn);
                 conn.commit();
             } catch (SQLException e) {
                 e.printStackTrace();
                 try {
                     conn.rollback();
                 } catch (SQLException o) {
                     o.printStackTrace();
                 }
             } finally {
                 try {
                     conn.close();
                     st.close();
                 } catch (SQLException e) {
                     System.out.println("Unable to close connection");
                 }
             }
         } else {
             System.out.println("Unable to update user because email is null");
         }
     }
     
     public void insertUser() {
         PreparedStatement st = null;
         String q = "INSERT INTO users SET email= ?, password = MD5(?), first_name = ?, last_name = ?";
         try {
             conn = dbconn.getConnection();
             conn.setAutoCommit(false);
             st = conn.prepareStatement(q);
             st.setString(1, this.email);
             st.setString(2, this.password);
             st.setString(3, this.firstName);
             st.setString(4, this.lastName);
             st.executeUpdate();
             insertUserRoles(this.email, this.role, conn);
             conn.commit();
         } catch (SQLException e) {
             e.printStackTrace();
             try {
                 conn.rollback();
             } catch (SQLException o) {
                 o.printStackTrace();
             }
         } finally {
             try {
                 conn.close();
                 st.close();
             } catch (SQLException e) {
                 System.out.println("Unable to close connection");
             }
         }
     }
     
     public void deleteUser() {
         if (id != null) {
             loadUser(id.toString());
             PreparedStatement st = null;
             String q = "UPDATE users SET active = ? where email = ?";
             try {
                 conn = dbconn.getConnection();
                 conn.setAutoCommit(false);
                 st = conn.prepareStatement(q);
                 st.setInt(1, 0);
                 st.setString(2, this.email);
                 st.executeUpdate();
                 deleteUserRoles(email, conn);
                 conn.commit();
             } catch (SQLException e) {
                 e.printStackTrace();
                 try {
                     conn.rollback();
                 } catch (SQLException o) {
                     o.printStackTrace();
                 }
             } finally {
                 try {
                     conn.close();
                     st.close();
                 } catch (SQLException e) {
                     System.out.println("Unable to close connection");
                 }
             }
         } else {
             System.out.println("Unable to delete user because email is null");
         }
     }
     
     private void deleteUserRoles(String email, Connection conn) throws SQLException{
         PreparedStatement st = null;
         String q = "DELETE FROM user_roles WHERE email = ?";
         try {
             st = conn.prepareStatement(q);
             st.setString(1, email);
             st.executeUpdate();
         } catch (SQLException e) {
             throw e;
         } finally {
             try {
                 st.close();
             } catch (SQLException e) {
                 System.out.println("Unable to close statement");
             }
             
         }
     }
     
     private void insertUserRoles(String email, Role role, Connection conn) throws SQLException {
         PreparedStatement st = null;
         String q;
         try {
             switch (role) {
                 case administrator: {
                     q = "INSERT INTO user_roles SET email = ?, roles = ?";
                     st = conn.prepareStatement(q);
                     st.setString(1, email);
                     st.setString(2, Role.administrator.toString());
                     st.executeUpdate();
                 }
                 case scout: {
                     q = "INSERT INTO user_roles SET email = ?, roles = ?";
                     st = conn.prepareStatement(q);
                     st.setString(1, email);
                     st.setString(2, Role.scout.toString());
                     st.executeUpdate();
                 }
                 case team_member: {
                     q = "INSERT INTO user_roles SET email = ?, roles = ?";
                     st = conn.prepareStatement(q);
                     st.setString(1, email);
                     st.setString(2, Role.team_member.toString());
                     st.executeUpdate();
                 }
             }
         } catch (SQLException e) {
             throw e;
         } finally {
             try {
                 st.close();
             } catch (SQLException e) {
                 System.out.println("Unable to close statement");
             }
         }
     }
     
     public void setEmail(String email) {
         if (!StringUtil.isBlank(email)) {
             this.email = email.trim();
         } else {
             this.email = "";
         }
     }
     
     public void setPassword (String password) {
         if (!StringUtil.isBlank(password)) {
             this.password = password.trim();
         } else {
             this.password = "";
         }
     }
     
     public void setFirstName(String firstName) {
         if (!StringUtil.isBlank(firstName)) {
             this.firstName = firstName.trim();;
         } else {
             this.firstName = "";
         }
     }
     
     public void setLastName(String lastName) {
         if (!StringUtil.isBlank(lastName)) {
             this.lastName = lastName.trim();
         } else {
             this.lastName = "";
         }
     }
     
     public void setRole(int role) {
         this.role = intToRole(role);
     }
     
     private Role intToRole(int r) {
         if (r == 3) {
             return Role.administrator;
         } else if (r == 2) {
             return Role.scout;
         } else if (r == 1) {
             return Role.team_member;
         }
         return null;
     }
     
     public void setId(int id) {
         this.id = Integer.valueOf(id);
     }
     
     public String getEmail() {
         return this.email;
     }
 
     public String getFirstName() {
       return this.firstName;
     }
     
     public String getLastName() {
         return this.lastName;
     }
     
     public int getId() {
         return this.id.intValue();
     }
     
     public int getRole() {
         return this.role.ordinal() + 1;
     }
 }
