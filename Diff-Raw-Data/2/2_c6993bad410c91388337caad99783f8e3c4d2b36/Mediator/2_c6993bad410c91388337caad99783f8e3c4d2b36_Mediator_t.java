/*UPDATED BY JOHN DIXON THIS COMMENT LINE ONLY FOR TEST PURPOSES 02/19/2013 11:50PM
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package loginscreen;
 import java.sql.*;
 import javax.swing.*;
 /**
  *
  * @author ChrisO
  */
 class Mediator {
     //Diction dict;
     Connection conn = null;
     PreparedStatement pst = null;
     ResultSet rs = null;
 	Student stu = new Student();
 
    public Mediator(Connection c){
 		conn = c;
      // public static Connection ConnectDb(){
         // try{
             // Class.forName("com.mysql.jdbc.Driver");
             // conn = DriverManager.getConnection("jdbc:mysql://www.savianconsultants.com:3306/savianconsultants_com_2","a0000a6f_1","century4last" );
             // JOptionPane.showMessageDialog(null, "Connection Established");
             //return conn;
         // }catch (Exception e){
             // JOptionPane.showMessageDialog(null, e);
             //return null;
         // }
      }
 
 
    // public String getPassword(String value){
            // String s = stu.getPassword(value);
            // return s.getPassword();
        // }
 
     public Boolean verifyLogin(String un, String pw){
 
         Boolean compareValue = false;
         String sql = "select * from users where login_Name = jedixon65 and password = password";
         try {System.out.print("check");
             pst = conn.prepareStatement(sql);
             pst.setString(1, un);
             System.out.print(un);
             pst.setString(2, pw);
             rs = pst.executeQuery();
 
             while (rs.next()) {
                 compareValue = true;
                 //JOptionPane.showMessageDialog(null, "username and password are correct");
                 //Added this line so when login is successful the StartupScreen.java will initialize.
                 //new StartupScreen().setVisible(true);
             }
         }
         catch (Exception e) {
             //logger file should be here
             //erased this line test1234566666
             //JOptionPane.showMessageDialog(null, e);
         }
        return compareValue;
     }
 
     //public Connection ConnectDb(){return conn;}
 }
