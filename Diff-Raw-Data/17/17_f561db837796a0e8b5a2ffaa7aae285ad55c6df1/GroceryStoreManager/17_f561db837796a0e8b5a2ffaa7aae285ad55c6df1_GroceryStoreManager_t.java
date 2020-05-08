 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.grocerypricebook.model.dbmanagers;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import net.grocerypricebook.model.GroceryStore;
 import net.grocerypricebook.model.JDBCUtilities;
 
 /**
  *
  * @author dmoney1323
  */
 public class GroceryStoreManager {
     public GroceryStoreManager(){
         
     }
     public void addNewGroceryStore(int userID, String name,String state,String city,String address,String zip)throws SQLException{
         int zipp = Integer.parseInt(zip);
         
         Connection con;
         Statement stmt;
         String query;
         try{
             con = JDBCUtilities.getConnection();
             stmt = con.createStatement(); 
 //            (\"" + userID + "\", \"" + name + "\")";
 
             query = "INSERT INTO grocery_stores(name,address,state,city,zip,user_id) VALUES(\"" + name + "\", \"" + address + "\",\"" + state + "\",\"" + city + "\",\"" + zip + "\",\"" + userID + "\")";
 
             stmt.executeUpdate(query);
             con.close();
             
         } catch (SQLException e){
             System.out.println(e); 
             throw e;
         }
     }
     public void deleteGroceryStore(int store_id,int userId)throws SQLException{
         
          Connection con;
          Statement stmt;
          String query;
          
          try{
              con = JDBCUtilities.getConnection();
              stmt =con.createStatement();
 //             \"" +name + "\"
              query = "DELETE FROM grocery_stores WHERE id= \"" + store_id + "\" AND user_id = \"" +userId +"\"";
              stmt.executeUpdate(query);
              con.close();
              
          }catch(SQLException e){
              System.out.println(e);
              throw e;
          }
         
     }
     public ArrayList<GroceryStore> getStores()throws SQLException{
         ArrayList<GroceryStore> gsl = new ArrayList<GroceryStore>();
         
         Connection con;
         Statement stmt;
         String query;
         ResultSet rs;
         
          try{
             con = JDBCUtilities.getConnection();
             stmt = con.createStatement(); 
             query = "SELECT id,name,address,state,city,zip,user_id FROM grocery_stores";
             
             rs = stmt.executeQuery(query);
             while(rs.next()){
                 gsl.add(new GroceryStore(rs.getInt("id"),rs.getString("name"),rs.getString("state"),rs.getString("city"),rs.getString("address"),rs.getInt("zip"),rs.getInt("user_id")));
             }
             con.close();
         } catch (SQLException e){
             System.out.println(e); 
             throw e;
         }
         
         return gsl;
     }
 
     public ArrayList<GroceryStore> getStores(int userId)throws SQLException{
         ArrayList<GroceryStore> gsl = new ArrayList<GroceryStore>();
         
         Connection con;
         Statement stmt;
         String query;
         ResultSet rs;
         
          try{
             con = JDBCUtilities.getConnection();
             stmt = con.createStatement(); 
             query = "SELECT id,name,address,state,city,zip,user_id FROM grocery_stores WHERE (user_id = 1 OR user_id = " + userId + ")";
             
             rs = stmt.executeQuery(query);
             while(rs.next()){
                gsl.add(new GroceryStore(rs.getInt("id"),rs.getString("name"),rs.getString("state"),rs.getString("city"),rs.getString("address"),rs.getInt("zip"),rs.getInt("user_id")));
                 
             }
             con.close();
         } catch (SQLException e){
             System.out.println(e); 
             throw e;
         }
         
         return gsl;
     }
 
     public void editGroceryStore(int store_id, int user_id, String name,String state,String city,String address,String zip)throws SQLException{
         int zipp = Integer.parseInt(zip);
 		Connection con;
 		Statement stmt;
 		String query;
 		try{
 			con = JDBCUtilities.getConnection();
 			stmt = con.createStatement();
 			query = "UPDATE grocery_stores SET name = \""+name+"\",state = \""+state+"\",city = \""+city+"\",address = \""+address+"\",zip = \""+zipp+"\" WHERE id= \"" + store_id + "\" AND user_id = \"" +user_id +"\"";
 			stmt.executeUpdate(query);
 			con.close();
 		} catch (SQLException e){
 			System.out.println(e);
 		}	
     }
 }
