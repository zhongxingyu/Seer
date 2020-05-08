 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package loginscreen;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author ChrisO
  */
 public class DataB {
 
     static Connection conn = null;
     static PreparedStatement pst;
     static ResultSet rs;
     public DataB()
     {}
 
     public static Connection openConnectDb(){
 
         try{
             Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://www.savianconsultants.com:3306/savianconsultants_com_3","a0000a6f_1","century4last" );
             //JOptionPane.showMessageDialog(null, "Connection Established");
             return conn;
         }catch (Exception e){
             JOptionPane.showMessageDialog(null, e);
             return null;}}
 
 
     public static void closeConnectDb(){
         try{
             conn.close();}
         catch (Exception e){
             JOptionPane.showMessageDialog(null, e);}}
 
 //    public static String query(String loginN, String value, String table){
 //        conn = DataB.openConnectDb();
 //        String sql = "SELECT " + value + " FROM " + table + " WHERE login_Name=" + loginN;
 //        System.out.println(sql);
 //        try {
 //            pst = conn.prepareStatement(sql);
 //            rs = pst.executeQuery();
 //            if (rs.next()) {
 //                String results = rs.getString(value);
 //                return results;
 //            }
 //            else {
 //                JOptionPane.showMessageDialog(null, "" + value + " does not exist in " + table + ".");
 //                return null;
 //                }
 //        } catch (Exception e) {
 //            JOptionPane.showMessageDialog(null, e);
 //            return null;
 //        }}
 //    public static String query2(String loginN, String value, String table){
 //       Connection conn = DataB.openConnectDb();
 //        String user_query = "select * from ? where login_Name = ?";
 //        try {
 //            PreparedStatement prestmt = conn.prepareStatement(user_query);
 //            prestmt.setString(1, table);
 //            prestmt.setString(2, loginN);
 //            ResultSet rs = prestmt.executeQuery();
 //            System.out.println(rs.getString("pword"));
 //            if (rs.next()) {
 //                return rs.getString(value);}
 //            else{return null;}
 //        }
 //        catch (SQLException e) {
 //            JOptionPane.showMessageDialog(null, e);
 //            return "false";
 //        }
 //    }
 }
