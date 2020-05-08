 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor. this is a test part of a commit
  */
 package loginscreen;
 
 
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author ChrisO
  */
 //declaration of the variables for this class
 class Student{
 
     static PreparedStatement prestmt;
     static ResultSet rs;
     static Connection c;
 
     //empty Constructor
     public Student()
     {}
 
 
     public static String getFullName(String loginN, Connection c){
 	String result;
         String user_query = "SELECT concat(f_name, ' ' , l_name) as fullname FROM users where login_Name = ?";
         try{
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()){result = rs.getString("fullname");}
             else{result = "notFound";}}
             catch (SQLException e){
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
             }
 
     //this query calls for the login name from the users table that matches the
     //parameter value which was entered in the
     public static String getFirstName(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("f_Name");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
      public static String getLastName(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("l_name");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
     public static String getPassword(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("pword");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
     public static void setPassword(String loginN, String password, Connection c){
 	String user_query = "update users set pword = ? where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, password);
             prestmt.setString(2, loginN);
             prestmt.executeUpdate();
         }
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
         }
     }
 
 	public static String getConfirmPassword(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("pword_val");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
     public static String getMajor(String loginN, Connection c){
         String result;
         String user_query = "select * ,(select degree_desc from degree_prog where degree_prog.iddegree_prog = users.major) as degree_desc from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next())
             {result =
             rs.getString("degree_desc");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
 	public static String getMajorNum(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("major");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
 	public static String getSemester(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("sem_start");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
 	public static String getEmail(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("s_email");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
 	public static String getYear(String loginN, Connection c){
         String result;
         String user_query = "select * from users where login_Name = ?";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, loginN);
             rs = prestmt.executeQuery();
             if (rs.next()) {result = rs.getString("gr_date");}
             else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
     }
 
 	public static void createAccount(String firstN, String lastN, String major, String semester, String year, String email, String username, String password, String passval, Connection c){
             String user_query = "INSERT INTO users (f_name, l_name, login_name, pword, pword_val,major, sem_start, gr_date, s_email)Values(?,?,?,?,?,(SELECT iddegree_prog from degree_prog where degree_desc = ?), (Select semester_id from semesters where semester_desc = ?),?,?)";
        // String user_query = "INSERT INTO users (f_Name, l_Name, login_Name, pword, pword_val, major, sem_start, gr_date, s_email)VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, firstN);
             prestmt.setString(2, lastN);
             prestmt.setString(3, username);
             prestmt.setString(4, password);
             prestmt.setString(5, passval);
             prestmt.setString(6, major);
             prestmt.setString(7, semester);
             prestmt.setString(8, "2013");
             prestmt.setString(9, email);
             prestmt.execute();
         }
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
         }
 
         }
 
         public static void updateAccount(String firstN, String lastN, String major, String semester,String year, String email,String password, String passval,String loginName,Connection c){
            String user_query = "Update users SET f_name = ? ,l_name=?, major = (select iddegree_prog from degree_prog where degree_desc = ?), sem_start=(Select semester_id from semesters where semester_desc = ?), gr_date=?, s_email=?, pword=?, pword_val=? where login_name = ? ";
 
         try {
             prestmt = c.prepareStatement(user_query);
             prestmt.setString(1, firstN);
             prestmt.setString(2, lastN);
             prestmt.setString(3, major);
             prestmt.setString(4, semester);
             prestmt.setString(5, year);
             prestmt.setString(6, email);
             prestmt.setString(7, password);
             prestmt.setString(8, passval);
             prestmt.setString(9, loginName);
             prestmt.execute();

         }
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
         }
//System.out.printf(firstN);
//System.out.printf(lastN);
//System.out.printf(major);
//System.out.printf(semester);
//System.out.printf(year);
//System.out.printf(email);
//System.out.printf(password);
//System.out.printf(passval);
//System.out.printf(loginName);
         }
         public static String geteditAccount(String loginN, Connection c){
         String result;
          String user_query = "Select f_name, l_name, (select degree_desc from degree_prog where degree_prog.iddegree_prog = users.major)as degree_desc, (select semester_desc from semesters where semester_id = users.sem_start)"
                            + "as semester_desc, gr_date, s_email from users where login_name = ? ";
      try {
         prestmt = c.prepareStatement(user_query);
         prestmt.setString(1, loginN);
         rs = prestmt.executeQuery();
         if (rs.next()) {result =
         rs.getString("f_name")+" "+
         rs.getString("lname")+" "+
         rs.getString("degree_desc")+" "+
         rs.getString("semester_desc")+" "+
         rs.getString("gr_date")+" "+
         rs.getString("s_email");}
         else{result = "notFound";}}
         catch (SQLException e) {
             JOptionPane.showMessageDialog(null, e);
             result = null;}
         return result;
         }
 
 
 
 
 }
