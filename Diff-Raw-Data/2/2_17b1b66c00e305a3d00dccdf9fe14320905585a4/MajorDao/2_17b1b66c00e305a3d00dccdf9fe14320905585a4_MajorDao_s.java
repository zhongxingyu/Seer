 package model.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Set;
 
 import model.Major;
 
 public class MajorDao extends Dao<Major> {
 
     public MajorDao(Connection conn) {
         super(conn);
     }
 
     @Override
     public boolean create(Major obj) {
 
         PreparedStatement addMajor;
         try {
             addMajor = conn.prepareStatement("INSERT INTO majors (mid,mname) VALUES(?,?)");
             addMajor.setInt(1, obj.getId());
             addMajor.setString(2, obj.getName());
             return addMajor.executeUpdate() == 1;
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public boolean delete(Major obj) {
         try {
            return !this.conn.createStatement().execute("DELETE FROM Majors WHERE sid = " + obj.getId());
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public boolean update(Major obj) {
        /* try {
             String req = "update Majors set sname = ?, mid = ? where sid = " + obj.getId();
             PreparedStatement updateMajor = conn.prepareStatement(req);
             updateMajor.setString(1, obj.getName());
             updateMajor.setInt(2, obj.getMajor().getId());
             updateMajor.executeQuery();
             return true;
         } catch (SQLException e) {
             obj.getMajor().getId();
             e.printStackTrace();
             return false;
         }*/
         return false;
     }
 
     @Override
     public Major find(int id) {
         Major Major = null;
         try {
             ResultSet rs = this.conn.createStatement().executeQuery("SELECT * FROM majors WHERE mid = " + id);
                 Major = new Major(id, rs.getString("mname"));
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return Major;
     }
 
     @Override
     public Set<Major> findAll() {
         Set<Major> set = null;
         try {
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select * from Majors");
             set = new HashSet<Major>();
             while (rs.next()) {
                 set.add(new Major(rs.getInt("mid"), rs.getString("mname")));
             }
         } catch (SQLException e) {
             System.err.println(e.getMessage());
         }
         return set;
     }
 
 }
