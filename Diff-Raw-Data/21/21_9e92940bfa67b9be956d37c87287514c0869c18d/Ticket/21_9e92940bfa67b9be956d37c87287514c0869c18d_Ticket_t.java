 package mvc.Model;
 /******************
  * Imports
  ******************/
 import mvc.View.Error_Frame;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import lib.db.Database;
 
 public class Ticket extends MainModel {
 	
     public Integer TID;
     public Integer customer_CID;	 	 	
     public Integer employee_EID;
     public Integer CategoryID;
     public Integer StatusID;
     public String Topic;
     public String Problem;
     public String Note;
     public String Solution;
     public String created_on;
     public String last_update;
         
     public Ticket(Integer TID, Integer customer_CID, Integer employee_EID, Integer CategoryID, Integer StatusID,
                   String Topic, String Problem,String Note, String Solution, String created_on, String last_update) {
         this.TID    =  TID;
         this.customer_CID = customer_CID;
         this.employee_EID = employee_EID;
         this.CategoryID = CategoryID;
         this.StatusID = StatusID;
         this.Topic = Topic;
         this.Problem = Problem;
         this.Note = Note;
         this.Solution = Solution;
         this.created_on = created_on;
         this.last_update = last_update;
     }
 	
     /*************************************** 
      * Generic
      * load table ticket from the database, 
      * save it into a arraylist and return it
      ***************************************/
         public static ArrayList<Ticket> showAll() {
             ArrayList<Ticket> tickets = new ArrayList<Ticket>();
             Database db = dbconnect();
 	try {
 		db.prepare("SELECT * FROM ticket ORDER BY TID");
 		ResultSet rs = db.executeQuery();         
 		while(rs.next()) {
                    tickets.add(TicketObject(rs));
 		}
 	           db.close();      
 
         } catch (SQLException e) {
            Error_Frame.Error(e.toString()); 
         } 
                 return tickets;
 	}
         
       /*************************
         * Create new ticket
         * Placeholder "?" - Using db.bind_param
         * to prevent SQL injection
         ************************/
         public void newTicket() {
 		Database db = dbconnect();
                 String noEmployee = null;
 		try {
 		String query = "INSERT INTO ticket "
                         + "(customer_CID, employee_EID, CategoryID, StatusID ,"
                         + "Topic ,Problem ,Note, Solution, created_on, "
                         + "last_update) VALUES "
                         + "(?,?,?,?,?,?,?,?,?,?)";
                 if (this.employee_EID != null) {
                     noEmployee = this.employee_EID.toString();
                 }
 		db.prepare(query);
                 db.bind_param(1, this.customer_CID.toString());
                 db.bind_param(2, noEmployee);
                 db.bind_param(3, this.CategoryID.toString());
                 db.bind_param(4, this.StatusID.toString());
                 db.bind_param(5, this.Topic);
                 db.bind_param(6, this.Problem);
                 db.bind_param(7, this.Note);
                 db.bind_param(8, this.Solution);
                 db.bind_param(9, this.created_on);
                 db.bind_param(10, this.last_update);
 		db.executeUpdate();
 		db.close();
                 
 		} catch(SQLException e){	
 			Error_Frame.Error(e.toString()); 
 		}  
         }
 	
 	/*******************
          * update ticket
          *********************/
         public void updateTicket(Integer ID) {
 	Database db = dbconnect();
         String noEmployee = null;
 		try {
 		String query = "UPDATE ticket SET customer_CID = ?, "
                         + "employee_EID = ?, CategoryID = ?, "
                         + "StatusID = ?, Topic = ?, "
                         + "Problem = ?, Note = ?, "
                         + "Solution = ?, created_on = ?, "
                         + "last_update = ? WHERE TID = ?";       
                         
                         if (this.employee_EID != null) {
                             noEmployee = this.employee_EID.toString();
                         }
                         
 			db.prepare(query);
                         db.bind_param(1, this.customer_CID.toString());
                         db.bind_param(2, noEmployee);
                         db.bind_param(3, this.CategoryID.toString());
                         db.bind_param(4, this.StatusID.toString());
                         db.bind_param(5, this.Topic);
                         db.bind_param(6, this.Problem);
                         db.bind_param(7, this.Note);
                         db.bind_param(8, this.Solution);
                         db.bind_param(9, this.created_on);
                         db.bind_param(10, this.last_update);
                         db.bind_param(11, ID.toString());
 			db.executeUpdate();
 			db.close();
 			} catch(SQLException e){	
 				Error_Frame.Error(e.toString()); 
 			}  
         }
         
         /*****************************************
          * search Ticket ID, save the information 
          * in a array and return it
          ******************************************/
         public static String[] searchTicket(Integer ID) {
             Database db = dbconnect();
             String [] Array = null;
 	try {
 		String query = ("SELECT * FROM ticket WHERE TID = ?");
                 db.prepare(query);
                 db.bind_param(1, ID.toString());
 		ResultSet rs = db.executeQuery();    
                 while(rs.next()) {
                    Array = new String[]{rs.getString(rs.getMetaData().getColumnName(2)), 
                                         rs.getString(rs.getMetaData().getColumnName(3)),
                                         rs.getString(rs.getMetaData().getColumnName(4)),
                                         rs.getString(rs.getMetaData().getColumnName(5)),
                                         rs.getString(rs.getMetaData().getColumnName(6)),
                                         rs.getString(rs.getMetaData().getColumnName(7)),
                                         rs.getString(rs.getMetaData().getColumnName(8)),
                                         rs.getString(rs.getMetaData().getColumnName(9)),
                                         rs.getString(rs.getMetaData().getColumnName(10)),
                                         rs.getString(rs.getMetaData().getColumnName(11))};
 		}
 	        db.close();      
 
         } catch (SQLException e) {
             Error_Frame.Error(e.toString());
         } 
         return Array;
     }
         
         /************************************************
          *  get all IDs from required database table and 
          *  and set it into comboboxes
          **************************************************/
         public static ArrayList Ticket_ComboBox(Integer ID) {
             ArrayList number = new ArrayList();
             Database db = dbconnect();
             String query;
             Object noEmployee = "";
             boolean _int = false;
 	try {
                 switch (ID) {
                 case 1:  query = ("SELECT CID FROM customer WHERE bDeleted = 0 ORDER BY CID");
                          _int = true;
                          break;
                 case 2:  query = ("SELECT username FROM employee WHERE bDeleted = 0 ORDER BY EID");
                          number.add(noEmployee);
                          break;
                 case 3:  query = ("SELECT description FROM problem_category WHERE bDeleted = 0 ORDER BY CategoryID");
                          break;
                 default: query = ("SELECT description FROM ticket_status WHERE bDeleted = 0 ORDER BY StatusID");
                          break;
                 }
                 db.prepare(query);
 		ResultSet rs = db.executeQuery();   
                 while(rs.next()) {
                     if (_int) {
                         number.add(rs.getInt(1));
                     } else {
                         number.add(rs.getString(1));
                     }
 		}
 	        db.close();      
 
         } catch (SQLException e) {
             Error_Frame.Error(e.toString());
         } 
         return number;
     }
         
         /*******************************
          * set resultset to a Ticket object
          *******************************/
         private static Ticket TicketObject(ResultSet rs) {
             Ticket newTicket = null;
         try {
             newTicket = new Ticket(rs.getInt(rs.getMetaData().getColumnName(1)), 
                                    rs.getInt(rs.getMetaData().getColumnName(2)), 
                                    rs.getInt(rs.getMetaData().getColumnName(3)),
                                    rs.getInt(rs.getMetaData().getColumnName(4)),
                                    rs.getInt(rs.getMetaData().getColumnName(5)),
                                    rs.getString(rs.getMetaData().getColumnName(6)),
                                    rs.getString(rs.getMetaData().getColumnName(7)),
                                    rs.getString(rs.getMetaData().getColumnName(8)),
                                    rs.getString(rs.getMetaData().getColumnName(9)),
                                    rs.getString(rs.getMetaData().getColumnName(10)),
                                    rs.getString(rs.getMetaData().getColumnName(11)));    
         } catch (SQLException e) {
             Error_Frame.Error(e.toString());
         }
         return newTicket;
     }
     
         /********************************
          *  save current ticket into a 
          *  object for the JTable
          *********************************/
     public Object[] TableArray() {
        Object[] Array = {this.TID, this.customer_CID, this.employee_EID, this.CategoryID, 
                                 this.StatusID, this.Topic, this.Problem, this.Note, this.Solution, this.created_on,
                                 this.last_update};
         return Array;
     }
 }
