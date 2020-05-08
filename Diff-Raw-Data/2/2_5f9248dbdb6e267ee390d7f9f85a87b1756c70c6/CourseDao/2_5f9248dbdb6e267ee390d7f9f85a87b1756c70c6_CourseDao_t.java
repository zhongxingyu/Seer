 package model.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Set;
 
 import model.Course;
 import model.LectureCourse;
 import model.Major;
 import model.OptionalCourse;
 
 public class CourseDao extends Dao<Course> {
 
     public CourseDao(Connection conn) {
         super(conn);
     }
 
     @Override
     public boolean create(Course obj) {
         PreparedStatement addStudent;
         try {
             addStudent = conn.prepareStatement("INSERT INTO courses (cid,cname,ctype,mid) VALUES(?,?,?,?)");
             addStudent.setInt(1, obj.getId());
             addStudent.setString(2, obj.getName());
             if(obj.isLecture()) addStudent.setString(3, "lecture");
             else addStudent.setString(3, "optional");
             if(obj.getMajor() != null) addStudent.setInt(4, obj.getMajor().getId());
             else addStudent.setNull(4, 0);
             return addStudent.executeUpdate() == 1;
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public boolean delete(Course obj) {
         try {
             return !this.conn.createStatement().execute("DELETE FROM courses WHERE cid = " + obj.getId());
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public boolean update(Course obj) {
         try {
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT * FROM courses WHERE cid = " + obj.getId());
             rs.first();
             rs.updateString("cname", obj.getName());
             if(obj.isLecture()) rs.updateString("ctype", "lecture");
             else rs.updateString("ctype", "optional");
             rs.updateRow();
             rs.close();
             stat.close();
             return true;
 
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public Course find(int id) {
         Course course = null;
         try {
             ResultSet rs = this.conn.createStatement()
                     .executeQuery("SELECT * FROM courses WHERE cid = " + id);
                 if(rs.getString("ctype").equals("lecture")) course = new LectureCourse(rs.getInt("cid"),rs.getString("cname"));
                else course = new OptionalCourse(rs.getInt("cid"),rs.getString("cname"));
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return course;
     }
 
     @Override
     public Set<Course> findAll() {
         Set<Course> set = null;
         try {
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select * from courses");
             set = new HashSet<Course>();
             while (rs.next()) {
                 if(rs.getString("ctype").equals("lecture"))  set.add(new LectureCourse(rs.getInt("cid"),rs.getString("cname")));
                 else set.add(new OptionalCourse(rs.getInt("cid"),rs.getString("cname")));
             }
         } catch (SQLException e) {
             System.err.println(e.getMessage());
         }
         return set;
     }
     
     public Set<Course> findByMajor(Major m) {
         Set<Course> set = null;
         try {
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select * from courses where mid = " + m.getId());
             set = new HashSet<Course>();
             while (rs.next()) {
                 if(rs.getString("ctype").equals("lecture"))  set.add(new LectureCourse(rs.getInt("cid"),rs.getString("cname")));
                 else set.add(new OptionalCourse(rs.getInt("cid"),rs.getString("cname")));
             }
         } catch (SQLException e) {
             System.err.println(e.getMessage());
         }
         return set;
     }
 
 }
