 package eionet.util;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * 
  * @author Jaanus Heinlaid
  *
  */
public class DbTransactionPolite extends DbTransactionRobust{
 	
 	private boolean isAnotherTransactionRunning = false;
 
 	/**
 	 * 
 	 * @param conn
 	 */
 	private DbTransactionPolite(Connection conn){
		super(conn);
 	}
 
 	/**
 	 * 
 	 *
 	 */
 	private void start(){
 		boolean anotherTransactionRunning = false;
 		try{
 			anotherTransactionRunning = conn.getAutoCommit()==false;
 			if (!anotherTransactionRunning)
 				conn.setAutoCommit(false);
 		}
 		catch (SQLException e){
 		}
 	}
 	
 	/**
 	 * 
 	 * @param conn
 	 * @return
 	 */
 	public static DbTransactionPolite start(Connection conn){
 		DbTransactionPolite tx = new DbTransactionPolite(conn);
 		tx.start();
 		return tx;
 	}
 	
 	/**
 	 * 
 	 *
 	 */
 	public void commit(){
 		try {
 			if (!isAnotherTransactionRunning)
 				conn.commit();
 		}
 		catch (SQLException e){
 		}
 		
 	}
 	
 	/**
 	 * 
 	 *
 	 */
 	public void rollback(){
 		try {
 			if (!isAnotherTransactionRunning)
 				conn.rollback();
 		}
 		catch (SQLException e){
 		}
 		
 	}
 	
 	/**
 	 * 
 	 *
 	 */
 	public void end(){
 		try {
 			if (!isAnotherTransactionRunning)
 				conn.setAutoCommit(true);
 		}
 		catch (SQLException e){
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean getIsAnotherTransactionRunning() {
 		return isAnotherTransactionRunning;
 	}
 }
