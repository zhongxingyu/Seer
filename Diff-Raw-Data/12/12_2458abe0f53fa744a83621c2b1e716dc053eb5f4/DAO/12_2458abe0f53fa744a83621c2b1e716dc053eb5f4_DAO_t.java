 package gov.nih.nci.cadsr.cadsrpasswordchange.core;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 
 public class DAO {
 
     private Logger logger = Logger.getLogger(DAO.class);  
     
     private static String _jndiUser = "java:/jdbc/caDSR";
     private static String _jndiSystem = "java:/jdbc/caDSRPasswordChange";
     
     public DAO() { 	
     }
     
 	public UserBean checkValidUser(String username, String password) {
 
 		logger.info ("checkValidUser user: " + username);
 
 		UserBean userBean = new UserBean(username);
 		
 		Connection conn = null;
 		try {
 			Context envContext = new InitialContext();	
 	        DataSource ds = (DataSource)envContext.lookup(_jndiUser);
 	        logger.debug("got DataSource for " + _jndiUser);
 	        conn = ds.getConnection(username, password);
 	        logger.debug("connected");	        
 			userBean.setLoggedIn(true);
 			userBean.setResult(new Result(ResultCode.NOT_EXPIRED));
 	    } catch (Exception ex) {
 	    	logger.debug(ex.getMessage());
 			Result result = decode(ex);
 			
 	    	// expired passwords are acceptable as logins
 			if (result.getResultCode() == ResultCode.EXPIRED) {
 				userBean.setLoggedIn(true);
 				logger.debug("considering expired password acceptable login");
 				
 			}
 			
 			userBean.setResult(result);
         }
         finally {
         	if (conn != null) {
         		try {
         			conn.close();
         		} catch (Exception ex) {
         			logger.error(ex.getMessage());
         		}
 	        }
 	    }
         logger.info("returning isLoggedIn " + userBean.isLoggedIn());        
         return userBean;
 	}
 	
 
 	public Result changePassword(String user, String password, String newPassword) {
 
 		logger.info("changePassword  user " + user );
 		
 		Result result = new Result(ResultCode.UNKNOWN_ERROR);  // (should get replaced)
 		Connection conn = null;
 		PreparedStatement pstmt = null;
		boolean isConnectionException = true;  // use to modify returned messages when exceptions are system issues instead of password change issues  
 		
 		try {
 			Context envContext = new InitialContext();	
 			DataSource ds = (DataSource)envContext.lookup(_jndiSystem);
 	        logger.debug("got DataSource for " + _jndiSystem);
 			conn = ds.getConnection();
	        logger.debug("connected");
	        
	        isConnectionException = false;
 		
 			// can't use parameters with PreparedStatement and "alter user", create a single string
 	        // (must quote password to retain capitalization for verification function)
 			String alterUser = "alter user " + user + " identified by \"" + newPassword + "\" replace " + password;
 			pstmt = conn.prepareStatement(alterUser);
 			logger.debug("attempted to alter user " + user);
 			pstmt.execute();
 			result = new Result(ResultCode.PASSWORD_CHANGED);
 		} catch (Exception ex) {
 			logger.debug(ex.getMessage());
			if (isConnectionException)
				result = new Result(ResultCode.UNKNOWN_ERROR);  // error not related to user, provide a generic error 
			else
				result = decode(ex);
 		} finally {
 			if (pstmt != null) {
 				try {
 					pstmt.close();
 				} catch (Exception ex) {
 					logger.error(ex.getMessage());
 				}
 			}
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (Exception ex) {
 					logger.error(ex.getMessage());
 				}
 			}
 		}
 
        logger.info("returning ResultCode " + result.getResultCode().toString());        
        return result;
 	}	
 	
 	private Result decode(Exception ex) {
 	
 		logger.info("decode");
 		
 		Result result = null;
 		
 		if (!(ex instanceof java.sql.SQLException)) {
 			result = new Result(ResultCode.UNKNOWN_ERROR);
 		}
 		
 		int found = -1;
 		String errorMessage = ex.getMessage();
 		logger.debug("errorMessage: " + errorMessage);
 		
 		// SQLException error code is vendor-specific and was just returning a zero
 		// have to parse message for codes
 
 		// Oracle codes
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-01017");  // invalid username/password
 			if (found != -1)
 				result = new Result(ResultCode.INVALID_LOGIN);
 		}
 
 		if (result == null) {		
 			found = errorMessage.indexOf("ORA-28000");  // locked
 			if (found != -1)
 				result = new Result(ResultCode.LOCKED_OUT);
 		}
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-28001");  // expired password
 			if (found != -1)
 				result = new Result(ResultCode.EXPIRED);
 		}
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-28007");
 			if (found != -1)
 				result = new Result(ResultCode.PASSWORD_REUSED);
 		}
 		
 		// custom codes (from stored procedure)
 		if (result == null) {		
 			found = errorMessage.indexOf("ORA-20000");
 			if (found != -1)
 				result = new Result(ResultCode.PASSWORD_LENGTH, errorMessage.substring(found + 11));
 		}
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-20001");
 			if (found != -1)
 				result = new Result(ResultCode.UNSUPPORTED_CHARACTERS, errorMessage.substring(found + 11));
 		}
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-20002");
 			if (found != -1)
 				result = new Result(ResultCode.NOT_ENOUGH_GROUPS, errorMessage.substring(found + 11));
 		}
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-20003");
 			if (found != -1)
 				result = new Result(ResultCode.TOO_RECENT, errorMessage.substring(found + 11));		
 		}
 		
 		if (result == null) {
 			found = errorMessage.indexOf("ORA-20004");
 			if (found != -1)
 				result = new Result(ResultCode.START_NOT_LETTER, errorMessage.substring(found + 11));		
 		}
 		
 		// check for unspecified custom error (range is -20000 to -20999)
 		if (result == null) {	
 			found = errorMessage.indexOf("ORA-20");
 			if (found != -1) {
 				// check that the next three characters are digits (resulting in 000-999)
 				if ( Character.isDigit(errorMessage.charAt(found + 6))
 						&& Character.isDigit(errorMessage.charAt(found + 7))
 							&& Character.isDigit(errorMessage.charAt(found + 8)) )
 					result = new Result(ResultCode.UNKNOWN_CUSTOM, errorMessage.substring(found + 11));
 			}
 		}
 
 		if (result == null)
 			result = new Result(ResultCode.UNKNOWN_ERROR);
 
 		logger.info("returning ResultCode " + result.getResultCode().toString() + " " + result.getMessage());
 		return result;
 	}
 	
 	
 }
