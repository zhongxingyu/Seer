 package gov.nih.nci.cadsr.cadsrpasswordchange.core;
 
 import gov.nih.nci.cadsr.cadsrpasswordchange.domain.UserSecurityQuestion;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTimeUtils;
 
 public class PasswordChangeDAO implements PasswordChange {
 
     public static String _jndiUser = "java:/jdbc/caDSR";
     public static String _jndiSystem = "java:/jdbc/caDSRPasswordChange";
 	private Connection conn;
 	private DataSource datasource;
     private static final String  QUESTION_TABLE_NAME = "SBREXT.USER_SECURITY_QUESTIONS";
 
     protected static final String SELECT_COLUMNS = "ua_name, question1, answer1, question2, answer2, question3, answer3, date_modified, attempted_count";
 
     protected static final String PK_CONDITION = "ua_name=?";
 
     private static final String SQL_INSERT = "INSERT INTO " + QUESTION_TABLE_NAME + " (ua_name,question1,answer1,question2,answer2,question3,answer3,date_modified) VALUES (?,?,?,?,?,?,?,?)";
 
     private Logger logger = Logger.getLogger(PasswordChangeDAO.class);
 
     public PasswordChangeDAO(DataSource datasource) {
     	this.datasource = datasource;
     }
 
     public PasswordChangeDAO(Connection conn) {
     	this.conn = conn;
     }
 
     //CADSRPASSW-46
     private Connection getConnection() throws Exception {
 		DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
         logger.debug("got DataSource for " + _jndiSystem);    	
 //        conn = ds.getConnection();
       logger.debug("got connection from jboss pool [" + _jndiSystem + "]");
         conn = ds.getConnection(PropertyHelper.getDatabaseUserID(), PropertyHelper.getDatabasePassword());
         
 //        String jdbcurl = PropertyHelper.getDatabaseURL();
 //        logger.debug("got connection using direct jdbc url [" + jdbcurl + "]");
 //        Properties info = new Properties();
 //        info.put( "user", PropertyHelper.getDatabaseUserID() );
 //        logger.debug("with user id [" + PropertyHelper.getDatabaseUserID() + "]");
 //        info.put( "password", PropertyHelper.getDatabasePassword() );
 //        Connection conn = DriverManager.getConnection(jdbcurl, info);
 
         conn.setAutoCommit(true);
         return conn;
     }
 
 	public boolean checkValidUser(String username) throws Exception {
 		boolean retVal = false;
 		
 		logger.info ("checkValidUser(username) user: " + username);
 		if(username == null) {
 			throw new Exception("Username can not be NULL or empty.");
 		}
 				
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 	        if(conn == null) {
 //			DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //	        logger.debug("got DataSource for " + _jndiSystem);
 //	        conn = ds.getConnection();
 	        conn = getConnection();
 	        }
 	        logger.debug("connected");
 			stmt = conn.prepareStatement("select * from SBR.USER_ACCOUNTS_VIEW where UPPER(UA_NAME) = ?");	//CADSRPASSW-15
 			stmt.setString(1, username.toUpperCase());
 			rs = stmt.executeQuery();
 			if(rs.next()) {
 				//assuming all user Ids are unique/no duplicate
 				retVal = true;
 			}
 	    } catch (Exception e) {
 	    	logger.debug(e.getMessage());
         }
         finally {
             if (rs != null) { try { rs.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
             if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
 	    }
 		
 		
         logger.info("checkValidUser(): " + retVal);
         return retVal;
 	}
 
 	public UserBean checkValidUser(String username, String password) throws Exception {
 
 		if(datasource == null) {
 			throw new Exception("DataSource is empty or NULL.");
 		}
 		if(username == null || password == null) {
 			throw new Exception("username and/or password is empty or NULL.");
 		}
 		
 		logger.info ("checkValidUser(username, password) user: " + username);
 
 		UserBean userBean = new UserBean(username);
 		
 		try {
 	        conn = datasource.getConnection(username.toUpperCase(), password); 	//CADSRPASSW-15 - should not matter in Oracle 10g but will help in a higher version
 	        logger.debug("connected");	        
 			userBean.setLoggedIn(true);
 			userBean.setResult(new Result(ResultCode.NOT_EXPIRED));
 	    } catch (Exception ex) {
 	    	logger.debug(ex.getMessage());
 			Result result = ConnectionUtil.decode(ex);
 			
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
         			conn = null;
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
 		PreparedStatement stmt = null;
 		boolean isConnectionException = true;  // use to modify returned messages when exceptions are system issues instead of password change issues  
 
 		try {
 	        if(conn == null) {
 //				DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //		        logger.debug("got DataSource for " + _jndiSystem);
 //		        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
 	        logger.debug("connected");
 	        
 	        isConnectionException = false;
 
 			// can't use parameters with PreparedStatement and "alter user", create a single string
 	        // (must quote password to retain capitalization for verification function)
 	        //String alterUser = "alter user ? identified by \"?\" replace \"?\";
 			String alterUser = "alter user " + user + " identified by \"" + newPassword.trim() + "\" replace " + "\"" + password.trim() + "\"";
 	        stmt = conn.prepareStatement(alterUser);
 	        //stmt.setString(1, user);	//this is not an oversight, it just didn't work for some reason at runtime (missing user or role or invalid column index)
             //stmt.setString(2, password);
 			//stmt.setString(3, newPassword);
 			logger.debug("attempted to alter user " + user);
 			stmt.execute();
 			result = new Result(ResultCode.PASSWORD_CHANGED);
 		} catch (Exception ex) {
 			logger.debug(ex.getMessage());
 			if (isConnectionException) {
 				result = new Result(ResultCode.UNKNOWN_ERROR);  // error not related to user, provide a generic error 
 			} else {
 				result = ConnectionUtil.decode(ex);
 			}
 		} finally {
             if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
 		}
 
        logger.info("returning ResultCode " + result.getResultCode().toString());        
        return result;
 	}
 	
     private void params( PreparedStatement pstmt, Object[] params) throws SQLException {
         int i = 1;
         for (Object param : params) {
             if (param != null) {
                 pstmt.setObject( i++, param );
             }
         }
     }
 	
     public UserSecurityQuestion findByPrimaryKey( String uaName ) throws Exception {
     	PreparedStatement pstmt = null;
         String sql = null;
         ResultSet rs = null;
         UserSecurityQuestion q = null;
         //begin CADSRPASSW-15
         if(uaName != null) {
         	uaName = uaName.toUpperCase();
         }
         //end CADSRPASSW-15
         try {
             sql = "select * from " + QUESTION_TABLE_NAME + " where UPPER(ua_name) = ?";		//CADSRPASSW-15
 
             logger.debug("findByPrimaryKey sql : " + sql);
 
 	        if(conn == null) {
 //            DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //	        logger.debug("got DataSource for " + _jndiSystem);
 //	        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
             pstmt = conn.prepareStatement( sql );
             pstmt.setString(1, uaName);
 			rs = pstmt.executeQuery();
 			int count = 0;
 			logger.debug("findByPrimaryKey: " + count);    			
 			if(rs.next()) {
 				q = new UserSecurityQuestion();
 				q.setUaName(rs.getString("ua_name"));
 				q.setQuestion1(rs.getString("question1"));
 				q.setAnswer1(decode(rs.getString("answer1")));
 				q.setQuestion2(rs.getString("question2"));
 				q.setAnswer2(decode(rs.getString("answer2")));
 				q.setQuestion3(rs.getString("question3"));
 				q.setAnswer3(decode(rs.getString("answer3")));
 				q.setDateModified(rs.getTimestamp("date_modified"));
 				q.setAttemptedCount(rs.getLong("attempted_count"));
 			}
 			logger.debug("findByPrimaryKey: " + count + " q " + q); 			
         }
         catch (SQLException e) {
             throw new Exception( e );
         }
         finally {
             if (rs != null) { try { rs.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
             if (pstmt != null) {  try { pstmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
         }
         return q;
     }
 
     public UserSecurityQuestion findByUaName( String uaName ) throws Exception {
     	return findByPrimaryKey( uaName );
     }
 
     public UserSecurityQuestion[] findAll( ) throws Exception {
     	PreparedStatement pstmt = null;
         ResultSet rs = null;
         String sql = null;
         ArrayList<UserSecurityQuestion> qList = new ArrayList<UserSecurityQuestion>();
         try {
             sql = "select * from " + QUESTION_TABLE_NAME;
 
 	        if(conn == null) {
 //            DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //	        logger.debug("got DataSource for " + _jndiSystem);
 //	        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
 	        
             pstmt = conn.prepareStatement( sql );
 			rs = pstmt.executeQuery();
 			UserSecurityQuestion q = null;
 			while(rs.next()) {
 				q = new UserSecurityQuestion();
 				q.setUaName(rs.getString("ua_name"));
 				q.setQuestion1(rs.getString("question1"));
 				q.setAnswer1(decode(rs.getString("answer1")));
 				q.setQuestion2(rs.getString("question2"));
 				q.setAnswer2(decode(rs.getString("answer2")));
 				q.setQuestion3(rs.getString("question3"));
 				q.setAnswer3(decode(rs.getString("answer3")));
 				q.setDateModified(rs.getTimestamp("date_modified"));
 				qList.add(q);
 			}
         }
         catch (SQLException e) {
             throw new Exception( e );
         }
         finally {
            if (rs != null) { try { rs.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
            if (pstmt != null) {  try { pstmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
         }
         return toArray(qList);
     }
 
     public boolean deleteByPrimaryKey( String uaName ) throws Exception {
     	//no requirement to delete anything
 
     	return false;
     }
 
     private void checkMaxLength( String name, String value, int maxLength)
             throws Exception {
 
         if ( value != null && value.length() > maxLength ) {
             throw new Exception("Value of column '" + name + "' cannot have more than " + maxLength + " chars");
         }
     }
     
     public void insert( UserSecurityQuestion dto ) throws Exception {
         PreparedStatement stmt = null;
 
         try {
 	        if(conn == null) {
 //        	DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //	        logger.debug("got DataSource for " + _jndiSystem);
 //	        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
 	        stmt = conn.prepareStatement( SQL_INSERT );
 
             if ( dto.getUaName() == null ) {
                 throw new Exception("Value of column 'ua_name' cannot be null");
             }
             checkMaxLength( "ua_name", dto.getUaName(), 30 );
             stmt.setString( 1, dto.getUaName() );
 
             if ( dto.getQuestion1() == null ) {
                 throw new Exception("Value of column 'question1' cannot be null");
             }
             checkMaxLength( "question1", dto.getQuestion1(), 500 );
             stmt.setString( 2, dto.getQuestion1() );
 
             if ( dto.getAnswer1() == null ) {
                 throw new Exception("Value of column 'answer1' cannot be null");
             }
             stmt.setString( 3, dto.getAnswer1() );
             
             if ( dto.getQuestion2() == null ) {
                 throw new Exception("Value of column 'question2' cannot be null");
             }
             checkMaxLength( "question2", dto.getQuestion2(), 500 );
             stmt.setString( 4, dto.getQuestion2() );
 
             if ( dto.getAnswer2() == null ) {
                 throw new Exception("Value of column 'answer2' cannot be null");
             }
             stmt.setString( 5, dto.getAnswer2() );
 
             if ( dto.getQuestion3() == null ) {
                 throw new Exception("Value of column 'question3' cannot be null");
             }
             checkMaxLength( "question3", dto.getQuestion3(), 500 );
             stmt.setString( 6, dto.getQuestion3() );
 
             if ( dto.getAnswer3() == null ) {
                 throw new Exception("Value of column 'answer3' cannot be null");
             }
             stmt.setString( 7, dto.getAnswer3() );
 
             if ( dto.getDateModified() == null ) {
                 dto.setDateModified( new Timestamp(DateTimeUtils.currentTimeMillis()) );
             }
             stmt.setTimestamp( 8, dto.getDateModified() );
          
         	logger.debug("updating questions '" + dto.toString() + "'");            
             int n = stmt.executeUpdate();
         	logger.debug("updating questions done");
         }
         catch (SQLException e) {
         	logger.error(e);
             throw new Exception( e );
         }
         finally {
            if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
         }
     }
 
     public boolean update( String uaName, UserSecurityQuestion dto ) throws Exception {
         StringBuffer sb = new StringBuffer();
         ArrayList<Object> params = new ArrayList<Object>();
 
         if ( dto.getUaName() != null ) {
             checkMaxLength( "ua_name", dto.getUaName(), 30 );
             sb.append( "ua_name=?" );
             params.add( dto.getUaName());
         }
 
         if ( dto.getQuestion1() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             checkMaxLength( "question1", dto.getQuestion1(), 500 );
             sb.append( "question1=?" );
             params.add( dto.getQuestion1());
         }
 
         if ( dto.getAnswer1() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             sb.append( "answer1=?" );
             params.add( dto.getAnswer1());
         }
 
         if ( dto.getQuestion2() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             checkMaxLength( "question2", dto.getQuestion2(), 500 );
             sb.append( "question2=?" );
             params.add( dto.getQuestion2());
         }
 
         if ( dto.getAnswer2() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             sb.append( "answer2=?" );
             params.add( dto.getAnswer2());
         }
 
         if ( dto.getQuestion3() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             checkMaxLength( "question3", dto.getQuestion3(), 500 );
             sb.append( "question3=?" );
             params.add( dto.getQuestion3());
         }
 
         if ( dto.getAnswer3() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             sb.append( "answer3=?" );
             params.add( dto.getAnswer3());
         }
 
         if ( dto.getDateModified() != null ) {
             if (sb.length() > 0) {
                 sb.append( ", " );
             }
 
             sb.append( "date_modified=?" );
             params.add( dto.getDateModified());
         }
         
         if (sb.length() > 0) {
             sb.append( ", " );
         }
         if ( dto.getAttemptedCount() == null ) {
             sb.append( "attempted_count=NULL" );
         }
         else {
             sb.append( "attempted_count=?" );
             params.add( dto.getAttemptedCount());
         }
         logger.info("PasswordChangeDAO:update attempted_count = " + dto.getAttemptedCount());
 
         if (sb.length() == 0) {
             return false;
         }
 
         params.add( uaName );
 
         Object[] oparams = new Object[ params.size() ];
 
         return updateOne( sb.toString(), PK_CONDITION, params.toArray( oparams ));
     }
 
     private boolean updateOne( String setstring, String cond, Object... params) throws Exception {
         int ret = executeUpdate( getUpdateSql( setstring, cond ), params );
 
         if (ret != 1) {
         	logger.error("Not able to save record into the database");
             throw new Exception("Not able to save record into the database");
         }
         
         if (ret > 1) {
             throw new Exception("More than one record updated");
         }
 
         return ret == 1;
     }
     
     private int executeUpdate( String sql, Object... params) throws Exception {
         Statement stmt = null;
 
         try {
             if (params != null && params.length > 0) {
             	logger.debug("updating questions with sql params = [");
                 for(Object obj : params){
                 	logger.debug("**********" + obj.toString() + "**********");
             	}            	
             	logger.debug("]");
             	logger.debug("updating questions sql = [" + sql + "]");
     	        if(conn == null) {
 //            	DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //    	        logger.debug("got DataSource for " + _jndiSystem);
 //    	        conn = ds.getConnection();
     		        conn = getConnection();
     	        }
                 PreparedStatement pstmt = conn.prepareStatement( sql );
                 stmt = pstmt;
 
                 params( pstmt, params);
 
                return pstmt.executeUpdate();
             }
             else {
             	logger.debug("creating questions sql = [" + sql + "]");
                 stmt = conn.createStatement();
 
                 return stmt.executeUpdate( sql );
             }
         }
         catch (SQLException e) {
             throw new Exception( e );
         }
         finally {
            if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
         }
     }
     
     private String getUpdateSql(String setstring, String cond) {
         return getUpdateSql(setstring) + getSqlCondition( cond );
     }
 
     /**
      * Returns the condition starting with " WHERE " or an empty string.
      */
     private String getSqlCondition(String cond) {
         return cond != null && cond.length() > 0 ? (" WHERE " + cond) : "";
     }
 
     private String getUpdateSql(String setstring) {
         return "UPDATE " + QUESTION_TABLE_NAME + " SET " + setstring;
     }
     
     private String encode(String text) {
     	return text;
     }
 
     private String decode(String text) {
     	return text;
     }
 
     private UserSecurityQuestion fetch( ResultSet rs ) throws Exception {
         UserSecurityQuestion dto = new UserSecurityQuestion();
         dto.setUaName( rs.getString( 1 ));
         dto.setQuestion1( rs.getString( 2 ));
         dto.setAnswer1( decode(rs.getString( 3 )));
         dto.setQuestion2( rs.getString( 4 ));
         dto.setAnswer2( decode(rs.getString( 5 )));
         dto.setQuestion3( rs.getString( 6 ));
         dto.setAnswer3( decode(rs.getString( 7 )));
         dto.setDateModified( rs.getTimestamp( 8 ));
 
         return dto;
     }
 
     private UserSecurityQuestion[] toArray(ArrayList<UserSecurityQuestion> list ) {
         UserSecurityQuestion[] ret = new UserSecurityQuestion[ list.size() ];
         return list.toArray( ret );
     }
 
 	public Result resetPassword(String user, String newPassword) {
 
 		logger.info("resetPassword  user " + user );
 		
 		Result result = new Result(ResultCode.UNKNOWN_ERROR);  // (should get replaced)
 		PreparedStatement stmt = null;
 		boolean isConnectionException = true;  // use to modify returned messages when exceptions are system issues instead of password change issues  
 		
 		try {
 	        if(conn == null) {				
 //	        	DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //		        logger.debug("got DataSource for " + _jndiSystem);
 //		        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
 	        logger.debug("connected");
 
 	        isConnectionException = false;
 		
 			// can't use parameters with PreparedStatement and "alter user", create a single string
 	        // (must quote password to retain capitalization for verification function)
 			String alterUser = "alter user " + user + " identified by \"" + newPassword.trim() + "\" account unlock";
 			stmt = conn.prepareStatement(alterUser);
 			logger.debug("attempted to alter user " + user);
 			stmt.execute();
 			result = new Result(ResultCode.PASSWORD_CHANGED);
 		} catch (Exception ex) {
 			logger.debug(ex.getMessage());
 				if (isConnectionException) {
 					result = new Result(ResultCode.UNKNOWN_ERROR);  // error not related to user, provide a generic error 
 				} else {
 					result = ConnectionUtil.decode(ex);
 				}
 		} finally {
             if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
 		}
 
        return result;
 	}
 	
 	/**
 	 * This should be moved into a common utility class.
 	 * @param toolName
 	 * @param property
 	 * @return
 	 */
 	public String getToolProperty(String toolName, String property) {
 
 		logger.info("accessing sbrext.tool_options_view_ext table, toolName '" + toolName + "', property '" + property + "'");
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String value = null;
 		
 		try {
 	        if(conn == null) {				
 //	        	DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //		        logger.debug("got DataSource for " + _jndiSystem);
 //		        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
 	        logger.debug("connected");
 
 			stmt = conn.prepareStatement("select value from sbrext.tool_options_view_ext where Tool_name = ? and Property = ?");
 			stmt.setString(1, toolName);
 			stmt.setString(2, property);
 			rs = stmt.executeQuery();
 			if(rs.next()) {
 				//assuming all user Ids are unique/no duplicate
 				value = rs.getString("value");
 				logger.info ("getToolProperty: toolName '" + toolName + "', property '" + property + "' value '" + value + "'");			
 			}
 		} catch (Exception ex) {
 			logger.debug(ex.getMessage());
 		} finally {
             if (rs != null) { try { rs.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
             if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
 		}
 
        return value;
 	}
 	
 	public String getAccountStatus(String user) {
 
 		logger.info("getAccountStatus  user " + user );
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String retVal = "";
 		
 		try {
 	        if(conn == null) {				
 //	        	DataSource ds = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
 //		        logger.debug("got DataSource for " + _jndiSystem);
 //		        conn = ds.getConnection();
 		        conn = getConnection();
 	        }
 	        logger.debug("connected");
 	        
 			String sql = "select account_status from sys.cadsr_users where upper(username) =  ?";
 	        stmt = conn.prepareStatement(sql);
 	        stmt.setString(1, user.toUpperCase());
 			logger.debug("attempted to alter user " + user);
 			rs = stmt.executeQuery();
 			if(rs.next()) {
 				retVal = (String)rs.getString("account_status");
 			}
 		} catch (Exception ex) {
 			logger.debug(ex.getMessage());
 		} finally {
             if (rs != null) { try { rs.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
             if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { logger.error(e.getMessage()); } }
         	if (conn != null) { try { conn.close(); conn = null; } catch (SQLException e) { logger.error(e.getMessage()); } }
 		}
 
        logger.info("returning account status " + retVal);        
        return retVal;
 	}
 	
 }
