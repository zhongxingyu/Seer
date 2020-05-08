 /*
  * Manage Data to and fro DB
  */
 package db;
 
 import db.MySQL_Connection;
 import db.query.Insert;
 import db.query.Select;
 import java.awt.List;
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  *
  * @author malasuk
  */
 public class DBManager {
     
     // Create MySQL connection
     MySQL_Connection conn = new MySQL_Connection();
 
 
     // Constructor
     public DBManager() {
         // ask db to connect. <rigid>
         conn.dbConnect();        
     }
     
     
     /* --- Select ---
      * 
      */
     public java.util.List<Map> doSelect(String sql){
         // create sql object --> probably a bad idea
         Select select = new Select();
         // List Map to store result
         java.util.List<Map> records = new ArrayList<Map>();
         
         try{
             records = select.getAllRecords(conn, sql);
         }catch (Exception e){
             System.out.println(e);
         }finally{
             return records;
         }
     }
         
         
         
     /* --- Add New Order ---
      *  LEFT OFF HERE  
      * YOUR insert probably wont work.  
      * surround te statement in a try catch
      * idk about passing around the connection.  its kinda getting skechy
      */
    public void setOrder(String sql){
         Insert insert = new Insert();
        
        try{
            insert.doInsert(conn, sql);
        }catch(Exception e){
            // do something      
        }finally{
            // then do this
        }
        
        
     }
         
         
     
     
     
     
     
     
     
     
     
 }
