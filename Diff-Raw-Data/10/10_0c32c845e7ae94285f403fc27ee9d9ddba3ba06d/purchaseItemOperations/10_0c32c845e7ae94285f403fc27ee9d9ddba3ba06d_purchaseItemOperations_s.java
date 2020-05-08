 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.swing.event.EventListenerList;
 
 
 
 public class purchaseItemOperations {
  Connection con = AMSOracleConnection.getInstance().getConnection();
  PreparedStatement ps = null;
  EventListenerList listenerList = new EventListenerList();
  
 	boolean insert(String receiptId, int upc, int qty){
 		try{
 			ps = con.prepareStatement("INSERT INTO PurchaseItem VALUES (?,?,?)");
 			ps.setString(1, receiptId);
 			ps.setInt(2, upc);
 			ps.setInt(3, qty);
 			ps.executeUpdate();
 			con.commit();
 			return true;
 		}
 		catch (SQLException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 		    fireExceptionGenerated(event);
 		    
 		    try
 		    {
 			con.rollback();
 			return false; 
 		    }
 		    catch (SQLException ex2)
 		    {
 			 event = new ExceptionEvent(this, ex2.getMessage());
 			 fireExceptionGenerated(event);
 			 return false; 
 		    }
 		}
 		
 	}
 	
 	private void fireExceptionGenerated(ExceptionEvent ex) {
 		// Guaranteed to return a non-null array
 		Object[] listeners = listenerList.getListenerList();
 
 		// Process the listeners last to first, notifying
 		// those that are interested in this event.
 		// I have no idea why the for loop counts backwards by 2
 		// and the array indices are the way they are.
 		for (int i = listeners.length-2; i>=0; i-=2) 
 		{
 		    if (listeners[i]==ExceptionListener.class) 
 		    {
 			((ExceptionListener)listeners[i+1]).exceptionGenerated(ex);
 		    }
 	         }
 		
 	}
 
 	boolean delete(){
 		
 	}
 	
 	boolean display(){
		
 	}
 }
