 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package infopharma.data;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author dfernandez
  */
 public class AccountDBAccess extends DBAccess{
     
     public HashMap<Integer, String> getUserTypes(){
         HashMap<Integer, String> userTypes = new HashMap<Integer, String>();
         Connection connection = null;
         Statement statement = null;
         ResultSet resultSet = null;
         String sql = "SELECT * FROM UserRoles";
         try{
             connection = makeConnection();
             statement = (Statement) connection.createStatement();
             resultSet = statement.executeQuery(sql);
             while(resultSet.next()){
                 int roleID = resultSet.getInt("roleID");
                 String roleType = resultSet.getString("roleType");
                 userTypes.put(roleID, roleType);
             }
         }catch(SQLException ex){
             
         }
         return userTypes;
     }
     
     public ArrayList<Integer> getDiscountPlans(){
         ArrayList<Integer> discountPlans = new ArrayList<Integer>();
         Connection connection = null;
         Statement statement = null;
         ResultSet resultSet = null;
        String sql = "SELECT * FROM UserRoles";
         try{
             connection = makeConnection();
             statement = (Statement) connection.createStatement();
             resultSet = statement.executeQuery(sql);
             while(resultSet.next()){
                 int discountID = resultSet.getInt("discountPlanID");
                 discountPlans.add(discountID);
             }
         }catch(SQLException ex){
             
         }
         return discountPlans;
     }
     
 }
