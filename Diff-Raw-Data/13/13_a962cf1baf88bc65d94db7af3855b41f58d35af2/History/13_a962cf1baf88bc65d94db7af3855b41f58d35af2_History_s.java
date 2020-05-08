 package Helpdesk.java.helpdesk.mvc.Model;
 /******************
  * Imports
  ******************/
 import Helpdesk.java.helpdesk.mvc.View.Error_Frame;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import Helpdesk.java.helpdesk.lib.db.Database;
 
     public class History extends MainModel{
     public Integer TicketID;
     public String changed_on;
     public String column_name;
     public String column_value;
     
     public History(Integer TicketID, String changed_on, String column_name, String column_value) {
         this.TicketID       = TicketID;
         this.changed_on     = changed_on;
         this.column_name    = column_name;
         this.column_value   = column_value;
     }
     
     /*************************************** 
      * Generic
      * load table history from the database, 
      * save it into a arraylist and return it
      ***************************************/
 	 public static ArrayList<History> showHistory() {
             ArrayList<History> histories = new ArrayList<History>();
             Database db = dbconnect();
             try {
                 db.prepare("SELECT * FROM ticket_history ORDER BY changed_on desc");
                 ResultSet rs = db.executeQuery();
 		while(rs.next()) {
                      histories.add(HistoryObject(rs));
                 }
 			db.close();
 			
         } catch (SQLException e) {
             Error_Frame.Error(e.toString());
         } 
             return histories;
 	}
         
         /*****************************************
          * Search history ID, save the information 
          * in a array and return it
          * Placeholder "?" - Using db.bind_param
          * to prevent SQL injection
          ******************************************/
         public static String[] searchHistory(Integer ID,String changed, String name) {
             Database db = dbconnect();
             String [] Array = null;
             try {
 		String query = ("SELECT * FROM ticket_history WHERE ticket_TID = ? "
                                 + "AND changed_on = ? AND column_name = ?");
                 db.prepare(query);
                 db.bind_param(1, ID.toString());
                 db.bind_param(2, changed);
                 db.bind_param(3, name);
 		ResultSet rs = db.executeQuery();    
                 while(rs.next()) {
                    Array = new String[]{rs.getString(rs.getMetaData().getColumnName(2)),
                                         rs.getString(rs.getMetaData().getColumnName(3)),
                                         rs.getString(rs.getMetaData().getColumnName(4))};
 		}
 	        db.close();      
 
             } catch (SQLException e) {
                 Error_Frame.Error(e.toString());
             } 
             return Array;
         }
          
         /*******************************
          * set resultset to a History object
          *******************************/
         private static History HistoryObject(ResultSet rs) {
             History newHistory = null;
         try {
             newHistory = new History(rs.getInt(rs.getMetaData().getColumnName(1)), 
                                      rs.getString(rs.getMetaData().getColumnName(2)),
                                      rs.getString(rs.getMetaData().getColumnName(3)),
                                      rs.getString(rs.getMetaData().getColumnName(4)));    
         } catch (SQLException e) {
             Error_Frame.Error(e.toString());
         }
         return newHistory;
     }
     
         /********************************
          *  save current history into a 
          *  object for the JTable
          *********************************/
     public Object[] Array() {
         
         if (this.column_value.equals("1") && this.column_name.equals("CategoryID")) this.column_value = "Hardware Problem";
         if (this.column_value.equals("2") && this.column_name.equals("CategoryID")) this.column_value = "Software Problem";
         if (this.column_value.equals("3") && this.column_name.equals("CategoryID")) this.column_value = "Activation Problems";
         if (this.column_value.equals("4") && this.column_name.equals("CategoryID")) this.column_value = "Other";
         if (this.column_value.equals("1") && this.column_name.equals("StatusID")) this.column_value = "Open";
         if (this.column_value.equals("2") && this.column_name.equals("StatusID")) this.column_value = "In process";
         if (this.column_value.equals("3") && this.column_name.equals("StatusID")) this.column_value = "Closed";
         
        Object[] Array = {this.TicketID, this.changed_on, this.column_name, this.column_value };
         return Array;
     }
 }
