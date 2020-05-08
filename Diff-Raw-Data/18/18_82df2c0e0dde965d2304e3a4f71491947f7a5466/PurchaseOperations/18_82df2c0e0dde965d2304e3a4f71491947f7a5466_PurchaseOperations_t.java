 import java.sql.Date;
 import java.sql.SQLException;
 import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
 
 
 
 public class PurchaseOperations extends AbstractTableOperations {
 	
 	//TODO test 
 	boolean insert(Date date,String cid,String cardno, String expdate, Date expected,Date delivered){
 		try{
 			ps=con.prepareStatement("INSERT INTO purchase VALUES (purchase_receiptId.nextval,?,?,?,?,?,?)");
 			if (date!=null){
 				ps.setDate(1, date);
 			}
 			else{
 				ps.setDate(1,null);
 			}
 			if(cid!=null){
 				ps.setString(2,cid);
 			}
 			else{
 				ps.setString(2,null);
 			}
 			if (cardno!=null){
 				ps.setString(3,cardno);
 			}
 			else{
 				ps.setString(3, null);
 			}
 			if(expdate!=null){
 				ps.setString(4,expdate);
 			}
 			else{
 				ps.setString(4,null);
 			}
 			if(expected!=null){
 				ps.setDate(5, expected);
 			}
 			else{
 				ps.setDate(5,null);
 			}
 			if(delivered!=null){
 				ps.setDate(6,delivered);
 			}
 			else{
 				ps.setDate(6,null);
 			}
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
	//TODO: PLEASE TEST THIS IN GUI via display in SQL+. - Ian
	boolean updateDeliveryDate(Integer receiptId,String stringdate){
 		try{
 			ps = con.prepareStatement("UPDATE purchase SET delivereddate = ? WHERE receiptId = ?");
 			if(receiptId!=null){
 				ps.setInt(2, receiptId);
 			}
 			else ps.setNull(2,Types.INTEGER);
			if(stringdate!=null){
				SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yy");
				java.util.Date utilDate = fm.parse(stringdate);
				java.sql.Date sqldate = new java.sql.Date(utilDate.getTime());
				ps.setDate(1,sqldate);
 			}
 			else{
 				ps.setDate(1,null);
 			}
 			ps.executeUpdate();
 			con.commit();
 			return true;
 		}
		catch(SQLException | ParseException ex){
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
 	
 	
 	//TODO test 
 	boolean delete(Integer receiptId, Integer upc){
 		try {
 			ps = con.prepareStatement("DELETE FROM purchase WHERE receiptId = ? AND upc = ?");
 			ps.setInt(1, receiptId);
 			ps.setInt(2, upc);
 			ps.executeUpdate();
 			con.commit();
 			return true;
 		}
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false;
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false;
 			}
 		}
 	}
 	
 	//TODO
 	void display(){
 		
 	}
 	
 
 
 }
