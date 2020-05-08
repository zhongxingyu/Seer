 package eionet.util;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * 
  * @author Jaanus Heinlaid
  *
  */
 public class DbTransactionRobust {
 	
	protected Connection conn = null;
 	
 	/**
 	 * 
 	 * @param conn
 	 */
	protected DbTransactionRobust(Connection conn){
 		this.conn = conn;
 	}
 
 	/**
 	 * 
 	 *
 	 */
 	private void start(){
 		try {
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
 	public static DbTransactionRobust start(Connection conn){
 		DbTransactionRobust tx = new DbTransactionRobust(conn);
 		tx.start();
 		return tx;
 	}
 	
 	/**
 	 * 
 	 *
 	 */
 	public void commit(){
 		try {
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
 			conn.setAutoCommit(true);
 		}
 		catch (SQLException e){
 		}
 	}
 }
