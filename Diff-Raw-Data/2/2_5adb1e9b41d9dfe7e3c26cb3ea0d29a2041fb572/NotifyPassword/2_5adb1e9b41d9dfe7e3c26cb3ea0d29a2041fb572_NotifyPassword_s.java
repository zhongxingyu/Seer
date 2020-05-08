 package gov.nih.nci.cadsr.cadsrpasswordchange.core;

 import gov.nih.nci.cadsr.cadsrpasswordchange.domain.User;
 
 import java.io.FileInputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import oracle.jdbc.pool.OracleDataSource;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.DateTimeUtils;
 import org.joda.time.Interval;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 
 public class NotifyPassword {
 
 	private static org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(NotifyPassword.class);
 	private static PasswordNotify dao;
 	public static String emailSubject;
 	public static String emailBody;
     private Properties          _propList;
     private Connection          _conn;
     private String              _dsurl;
     private String              _user;
     private String              _pswd;
     private String              _webHost;
     private String              _processingNotificationDays;
     
     public NotifyPassword(Connection conn) {
     	this._conn = conn;
     }
 
     /**
      * Open a single simple connection to the database. No pooling is necessary.
      *
      * @param _dsurl
      *        The Oracle TNSNAME entry describing the database location.
      * @param user_
      *        The ORACLE user id.
      * @param pswd_
      *        The password which must match 'user_'.
      * @return The database error code.
      */
     private int open() throws Exception
     {
         // If we already have a connection, don't bother.
         if (_conn != null) {
             //return 0;
         	_conn.close();	//CADSRPASSW-56
         }
 
         try
         {
 //            OracleDataSource ods = new OracleDataSource();
 //            String parts[] = _dsurl.split("[:]");
 //            ods.setDriverType("thin");
             _logger.info("NotifyPassword v1.0 build 21");
 //            String connString=_dsurl;
 //            ods.setURL(connString);
 //            ods.setUser(_user);
 //            ods.setPassword(_pswd);
 //            _logger.info("NotifyPassword:open _dsurl[" + _dsurl + "] via _user["+ _user + "]");
 //            _conn = ods.getConnection(_user, _pswd);
 
           _logger.debug("got connection using direct jdbc url [" + _dsurl + "]");
           Properties info = new Properties();
           info.put( "user", _user );
           _logger.debug("with user id [" + _user + "]");
           info.put( "password", _pswd );
           Class.forName("oracle.jdbc.driver.OracleDriver");
           _conn = DriverManager.getConnection(_dsurl, info);
 
             _logger.info("connected to the database");
             _conn.setAutoCommit(true);
             return 0;
         }
         catch (SQLException ex)
         {
             throw ex;
         }
     }
 
     /**
      * Load the properties from the XML file specified.
      *
      * @param propFile_ the properties file.
      */
     private void loadProp(String propFile_) throws Exception
     {
         _propList = new Properties();
 
         _logger.debug("\n\nLoading properties...\n\n");
 
         try
         {
             FileInputStream in = new FileInputStream(propFile_);
             _propList.loadFromXML(in);
             in.close();
         }
         catch (Exception ex)
         {
         	ex.printStackTrace();
             throw ex;
         }
 
         _dsurl = _propList.getProperty(Constants._DSURL);
         if (_dsurl == null) {
             _logger.error("Missing " + Constants._DSURL + " connection string in " + propFile_);
             System.exit(-1);
         }
         
         _user = _propList.getProperty(Constants._DSUSER);
         if (_user == null) {
             _logger.error("Missing " + Constants._DSUSER + " in " + propFile_);
             System.exit(-1);
         }
         
         _pswd = _propList.getProperty(Constants._DSPSWD);
         if (_pswd == null) {
             _logger.error("Missing " + Constants._DSPSWD + " in " + propFile_);
             System.exit(-1);
         }
         
         _webHost = _propList.getProperty(Constants._WEBHOST);
         if (_webHost == null) {
             _logger.error("Missing " + Constants._WEBHOST + " in " + propFile_);
             System.exit(-1);
         }
 
     }
     
 	public void doAll(String propFile_) throws Exception {
 		_logger.debug("NotifyPassword.doAll entered ...");
         loadProp(propFile_);
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		_processingNotificationDays = dao.getProcessTypes();
 		if(_processingNotificationDays != null) {
 			try {
 				List<String> types = new ArrayList<String>(Arrays.asList(_processingNotificationDays.split(","))); 	//note: no space in between the , separator
 				int size = types.size();
 				int index = 1;
 				for (String t : types) {
 					_logger.info("Notification type " + t + " started ...");
 					process(Integer.valueOf(t).intValue(), size, index, types);
 					++index;
 					_logger.debug("Notification type " + t + " processed.");
 				}
 				_logger.debug(".doAll.");
 			} catch (Exception e) {
 				//e.printStackTrace();
 				_logger.error(CommonUtil.toString(e));
 			}
 		} else {
 			_logger.error("Missing processing types. Please check EMAIL.NOTIFY_TYPE property value in the table sbrext.tool_options_view_ext.");
 		}
 		
         if (_conn != null)
         {
             _conn.close();
             _conn = null;
         }
 		_logger.debug("NotifyPassword.doAll done.");
 	}
 
 	private void process(int days, int size, int index, List<String>types) throws Exception {
 		_logger.debug("\nNotifyPassword.process entered ...");
 		
 		List<User> recipients = null;
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		recipients = dao.getPasswordExpiringList(days, size, index, types);
 		if (recipients != null && recipients.size() > 0) {
 			for (User u : recipients) {
 				if(u != null && u.getElectronicMailAddress() != null) {
 					_logger.info("Processing user [" + u.getUsername() + "] attempted [" + u.getAttemptedCount() + "] type [" + u.getProcessingType() + "] password updated ["
 							+ u.getPasswordChangedDate() + "] email [" + u.getElectronicMailAddress()
 							+ "] expiry date [" + u.getExpiryDate() + "]");
 					if(isNotificationValid(u, days, size, index)) {
 						_logger.info("NotifyPassword.process saving into queue for user: " + u.getUsername());
 						saveIntoQueue(u, days);
 						_logger.debug("NotifyPassword.process queued email for user: " + u.getUsername() + " under type " + days);
 						_logger.info("NotifyPassword.process sending email for user: " + u.getUsername() + " under type " + days);
 
 						try {
 							if(sendEmail(u, days)) {
 								_logger.debug("NotifyPassword.sendEmail *** DONE ***");
 								_logger.info("NotifyPassword.process updating success for user: " + u.getUsername() + " under type " + days);
 								updateStatus(u, Constants.SUCCESS + String.valueOf(days), days);
 								_logger.debug("NotifyPassword.process updated success for user: " + u.getUsername() + " under type " + days);
 							} else {
 								_logger.info("NotifyPassword.process updating failure for user: " + u.getUsername() + " under type " + days);
 								updateStatus(u, Constants.FAILED + String.valueOf(days), days);
 								_logger.debug("NotifyPassword.process updated failure for user: " + u.getUsername() + " under type " + days);
 							}
 						} catch (Exception e) {
 							e.printStackTrace();
 							_logger.error(e);
 							_logger.info("NotifyPassword.process updating failure for user: " + u.getUsername() + " under type " + days);
 							updateStatus(u, Constants.UNKNOWN + String.valueOf(days), days);
 							_logger.debug("NotifyPassword.process updated failure for user: " + u.getUsername() + " under type " + days);
 						}
 					} else {
 						_logger.info("isNotificationValid is not valid, notification aborted for user: " + u.getUsername());
 						updateStatus(u, Constants.INVALID + String.valueOf(days), days);
 						_logger.debug("status date updated for user " + u);
 					}
 				} else if(u.getElectronicMailAddress() == null) {
 					_logger.info("isNotificationValid is not valid, email is NULL for user: " + u.getUsername());
 				}
 			}
 		} else {
 			_logger.info("------- No user for notification of " + days + " found ------- ");
 		}
 
 		_logger.debug("NotifyPassword.process done.\n\n");
 	}
 
 	/**
 	 * Add or update the queue with the outgoing email.
 	 */
 	private void saveIntoQueue(User user, int daysLeft) throws Exception {
 		_logger.debug("saveIntoQueue entered");
 		_logger.info("saveIntoQueue:user [" + user + "] type " + daysLeft);
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		user.setProcessingType(String.valueOf(daysLeft));
 		_logger.info("saveIntoQueue:type " + daysLeft + " set");
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		dao.updateQueue(user);
 		_logger.debug("saveIntoQueue done");
 	}
 	
 	private boolean sendEmail(User user, int daysLeft) throws Exception {
 		boolean retVal = false;
 
 		_logger.debug("NotifyPassword.sendEmail entered ...");
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		String adminEmailAddress = dao.getAdminEmailAddress();
 		_logger.debug("NotifyPassword.sendEmail adminEmailAddress [" + adminEmailAddress + "]");
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		String emailSubject = dao.getEmailSubject();
 		_logger.debug("NotifyPassword.sendEmail emailSubject [" + emailSubject + "]");
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		String emailBody = EmailHelper.handleExpiryDateToken(dao.getEmailBody(), user.getExpiryDate());
 		emailBody = EmailHelper.handleHostToken(emailBody, _webHost);		//CADSRPASSW-63
 		_logger.debug("NotifyPassword.sendEmail emailBody [" + emailBody + "]");
 		emailBody = EmailHelper.handleUserIDToken(emailBody, user);		//CADSRPASSW-62
 		_logger.info("sendEmail:user id = [" + user.getUsername() + "] body processed = [" + emailBody + "]");
 		String emailAddress = user.getElectronicMailAddress();
 		_logger.debug("NotifyPassword.sendEmail emailAddress [" + emailAddress + "]");
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		String host = dao.getHostName();
 		_logger.debug("NotifyPassword.sendEmail host [" + host + "]");
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		String port = dao.getHostPort();
 		_logger.debug("NotifyPassword.sendEmail port [" + port + "]");
 		EmailSending ms = new EmailSending(adminEmailAddress, "dummy", host, port, emailAddress, emailSubject, emailBody);
 		_logger.debug("NotifyPassword.sendEmail sending email ...");
 
 //_logger.info("isNotificationValid: FOR TEST ONLY *** this should be removed *** ===> sendEmail retVal hardcoded to true");
 //retVal = true;	//open this just for test
 
 		retVal = ms.send();		//uncomment this!!!
 		_logger.debug("NotifyPassword.ms.send() is " + retVal);
 
 		return retVal;
 	}
 	
 	/**
 	 * Method to make sure the latest processing details is reflected with the passed user.
 	 * @param user
 	 * @return
 	 * @throws Exception 
 	 */
 	private User refresh(User user) throws Exception {
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		return dao.loadQueue(user);
 	}
 
 	/**
 	 * Update the status of the delivery, sent or not sent.
 	 * 
 	 * @param users list of users affected
 	 * @throws Exception 
 	 */
 	private void updateStatus(User user, String status, int daysLeft) throws Exception {
 		if(user == null) {
 			throw new Exception("User is NULL or empty.");
 		}
 		user = refresh(user);
 		int currentCount = user.getAttemptedCount();
 		String dStatus = user.getDeliveryStatus();
 		if(dStatus == null) {
 			dStatus = "";
 		}
         open();
 		dao = new PasswordNotifyDAO(_conn);
 		user.setProcessingType(String.valueOf(daysLeft));
 		if(status != null && status.equals(Constants.SUCCESS + String.valueOf(daysLeft))) {
 			user.setAttemptedCount(++currentCount);
 			_logger.info("user id [" + user.getUsername() + "] status = [" + status + "] attempted count [" + user.getAttemptedCount() + "]");
 			int index = dStatus.indexOf(Constants.SUCCESS + String.valueOf(daysLeft));
 			if(index == -1) {
 				if(dStatus.length() > 0) {
 					user.setDeliveryStatus(dStatus + " " + status);
 				} else {
 					user.setDeliveryStatus(status);
 				}
 			}
 		} else {
 			int indexF = dStatus.indexOf(Constants.FAILED + String.valueOf(daysLeft));
 			int indexI = dStatus.indexOf(Constants.INVALID + String.valueOf(daysLeft));
 			int indexU = dStatus.indexOf(Constants.UNKNOWN + String.valueOf(daysLeft));
 			//uncomment the following just for test
 			if(indexF == -1 && indexI == -1 && indexU == -1) {
 				if(dStatus.length() > 0) {
 					user.setDeliveryStatus(dStatus + " " + status);
 				} else {
 					user.setDeliveryStatus(status);
 				}
 			}
 			_logger.debug("user id [" + user.getUsername() + "] status = [" + status + "]");
 		}
 		user.setDateModified(new Timestamp(DateTimeUtils.currentTimeMillis()));
 		dao = new PasswordNotifyDAO(_conn);
 		dao.updateQueue(user);
 	}
 
 	public boolean isNotificationValid(User user, int daysLeft, int totalNotificationTypes, int currentNotificationIndex) throws Exception {
 		_logger.debug("isNotificationValid entered");
 		boolean ret = false;
 		boolean daysCondition = false;
 		boolean deliveryStatus = false;
 		String processedType = null;
 		int attempted = -1;
 		String status = null;
 		long daysSincePasswordChange = -1;
 
 		_logger.info("isNotificationValid: calculating last password change time (to see if the password has been changed) ...");
 		java.sql.Date passwordChangedDate = user.getPasswordChangedDate();
 		if(passwordChangedDate == null) {
 			throw new Exception("Not able to determine what is the password changed date or password change date is empty (from sys.cadsr_users view).");
 		}
 
 		if(totalNotificationTypes != currentNotificationIndex && !isAlreadySent(user, daysLeft)) {
 			_logger.info("isNotificationValid: type " + daysLeft + " is not the last notification type");
 			daysSincePasswordChange = CommonUtil.calculateDays(passwordChangedDate, new Date(DateTimeUtils.currentTimeMillis()));
 //_logger.info("isNotificationValid: FOR TEST ONLY *** this should be removed *** ===> daysSincePasswordChange hardcoded to X (see below)");
 //daysSincePasswordChange = 15;	//open this just for test
 			_logger.info("isNotificationValid: last password change time was " + daysSincePasswordChange);
 	
 			if(daysSincePasswordChange != 0 && !isChangedRecently(daysLeft, daysSincePasswordChange, user)) {	//not recently changed (today)
 				_logger.info("isNotificationValid: password was not recently changed");
 					if(user != null) {
 						_logger.debug("isNotificationValid: checking user ...");
 						//not the last type - send only once
 						if(user.getDeliveryStatus() == null && user.getProcessingType() == null) {
 							//has not been processed at all
 							ret = true;
 							_logger.debug("isNotificationValid is true: has not been processed before");
 						}
 						else 
 						if(user.getDeliveryStatus() != null && user.getDeliveryStatus().indexOf(Constants.SUCCESS + String.valueOf(daysLeft)) == -1) {
 							//processed but was not successful for whatever reason
 							ret = true;
 							_logger.debug("isNotificationValid is true: processed but was not successful (thus should retry)");
 						}					 
 						/*else 
 						if(user.getDeliveryStatus() != null && user.getDeliveryStatus().equals(Constants.FAILED)) {
 							//processed but failed
 							ret = true;
 							_logger.debug("isNotificationValid is true: processed but failed");
 						} else 
 						if(user.getProcessingType() != null && !user.getProcessingType().equals(String.valueOf(daysLeft))) {
 							//it is different type of notification
 							ret = true;
 							_logger.debug("isNotificationValid is true: it is of different processing type, current type is " + daysLeft + " but the user's last processed type was " + user.getProcessingType());
 						} else {
 							_logger.info("isNotificationValid is false: none of the condition(s) met");
 						}*/
 						_logger.debug("isNotificationValid: check user done");
 					} else {
 						throw new Exception("User is NULL or empty.");
 					}
 			}
 			else 
 			if(daysSincePasswordChange == 0 || isChangedRecently(daysLeft, daysSincePasswordChange, user)) {	//reset everything if changed today OR if changed after the last check point
 				//=== begin - KEEP THIS BLOCK, though not doing anything, but for logging
 				_logger.debug("isNotificationValid is false");
 		        //open();
 				//dao = new PasswordNotifyDAO(_conn);
 				//_logger.debug("isNotificationValid: removing the user [" + user + "] removed from the queue ...");
 				//dao.removeQueue(user);	//CADSRPASSW-70 CADSRPASSW-72
 				_logger.info("isNotificationValid is false: user [" + user + " due to password change today or recently change.");
 				//=== end - KEEP THIS BLOCK, though not doing anything, but for logging
 			}
 		} else
 		if(totalNotificationTypes == currentNotificationIndex) {
 			_logger.info("isNotificationValid: type " + daysLeft + " is the last notification type");
 			if(daysLeft != Constants.DEACTIVATED_VALUE) {
 				_logger.info("isNotificationValid: type " + daysLeft + " (the last notification type) is active");
 				//the last notification type
 				Calendar start = Calendar.getInstance();
 				start.setTime(passwordChangedDate);
 				_logger.info("isNotificationValid: checking for day(s) since password change and if the password was recently changed within the days of the type");
 //				if(daysSincePasswordChange >= 1 && !isChangedRecently(daysLeft, daysSincePasswordChange)) {
 				if(isOverADaySinceLastSent(user)) {
 					ret = true;
 					_logger.debug("isNotificationValid is true: current type is " + daysLeft + "(daily notification) and it has been over a day since the last notice");
 				} else {
 					_logger.debug("isNotificationValid is false: current type is " + daysLeft + "(daily notification) and it has not been over a day since the last notice sent");
 				}
 				_logger.info("isNotificationValid is " + ret + ": it has been " + daysSincePasswordChange + " day(s) since the password change");
 			} else {
 				_logger.debug("daily notification is disabled (types = '"+ _processingNotificationDays + "').");
 			}
 		}
 
 		_logger.debug("isNotificationValid exiting with ret " + ret + " ...");
 		
 //_logger.info("isNotificationValid: FOR TEST ONLY *** this should be removed *** ===> isNotificationValid: ret hardcoded to true");
 //ret = true;
 
 		return ret;
 	}
 	
 	private boolean isOverADaySinceLastSent(User user) throws Exception {
 		boolean retVal = false;
 
 		_logger.debug("isOverADaySinceLastSent entered");
 		LocalTime currentTime = new LocalTime();
 //		LocalTime start = new LocalTime(11, 30);
 //		LocalTime end = new LocalTime(12, 30);
 //		LocalInterval interval = new LocalInterval(start, end);
 //		DateTime test = new DateTime(2010, 5, 25, 16, 0, 0, 0);
 //		System.out.println(interval.contains(test));
 		int currentHour = currentTime.getHourOfDay();
 		if(currentHour == 12) {
 			retVal = true;
 		}
 		_logger.debug("isOverADaySinceLastSent: exiting with ret " + retVal + " ...");
 		
 		return retVal;
 	}
 
 	/**
 	 * Method to check if the password is changed within the days of the type, e.g. if the type is 7 and the changed happened within 7 days,
 	 * then the return is true, otherwise it is false.
 	 * 
 	 * @param daysLeft	the type e.g. 14, 7 or 4
 	 * @param daysSincePasswordChange	the password changed date/time since now
 	 * @return
 	 * @throws Exception 
 	 */
 	private boolean isChangedRecently(int daysLeft, long daysSincePasswordChange, User user) throws Exception {
 		boolean ret = false;
 		_logger.debug("isChangedRecently entered");
 		if(user == null || user.getCreatedDate() == null || user.getPasswordChangedDate() == null) {
 			throw new Exception("Not able to determine if an account is newly created or not, user " + user + "]");
 		}
 
 		if(daysSincePasswordChange <= daysLeft) {
 			ret = true;
 			_logger.info("type " + daysLeft + "] isChangedRecently:daysSincePasswordChange is " + daysSincePasswordChange + " which is <= " + daysLeft + ", thus set to " + ret);
 		}
 		//=== if it is a newly created account, it is NOT recently changed obviously
 		if((user.getCreatedDate().compareTo(user.getPasswordChangedDate()) == 0)) {
 			ret = false;	//new account, not a true change of password
 			_logger.info("type " + daysLeft + "] isChangedRecently: however this is a new account created on " + user.getCreatedDate() + ", thus set to " + ret);
 		}
 		
 //ret = false;	//open this just for test
 //_logger.info("isNotificationValid: FOR TEST ONLY *** this should be removed *** ===> isChangedRecently hardcoded to false");
 		_logger.debug("isChangedRecently is " + ret);
 		return ret;
 	}
 
 	/**
 	 * Method to check if notification type of the user has been sent or otherwise. If sent, return true otherwise false;
 	 * 
 	 * @param user
 	 * @param daysLeft	the type e.g. 14, 7 or 4
 	 * @return
 	 * @throws Exception
 	 */
 	private boolean isAlreadySent(User user, int daysLeft) throws Exception {
 		_logger.info("isAlreadySent user " + user );
         if(user == null || user.getUsername() == null) {
         	throw new Exception("User/ID is NULL or empty.");
         }
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		boolean retVal = false;
 		String s = null;
 		String t = null;
 		
 		try {
 			open();
 			_logger.debug("connected");
 	        if(_conn == null) {
 	        	throw new Exception("Connection is NULL or empty.");
 	        }
 			String sql = "select delivery_status, processing_type from SBREXT.PASSWORD_NOTIFICATION where upper(ua_name) =  ?";
 	        stmt = _conn.prepareStatement(sql);
 	        stmt.setString(1, user.getUsername().toUpperCase());
 	        _logger.debug("isAlreadySent:check user [" + user + "] sent status");
 			rs = stmt.executeQuery();
 			if(rs.next()) {
 				s = rs.getString("delivery_status");
 				t = rs.getString("processing_type");
 			}
 	        _logger.debug("isAlreadySent: user [" + user + "] sent status [" + s + "]");
 		} catch (Exception ex) {
 			_logger.debug(ex.getMessage());
 		} finally {
             if (rs != null) { try { rs.close(); } catch (SQLException e) { _logger.error(e.getMessage()); } }
             if (stmt != null) {  try { stmt.close(); } catch (SQLException e) { _logger.error(e.getMessage()); } }
         	if (_conn != null) { try { _conn.close(); _conn = null; } catch (SQLException e) { _logger.error(e.getMessage()); } }
 		}
 
 		if(s != null && s.indexOf(Constants.SUCCESS + String.valueOf(daysLeft)) > -1) {
 			retVal = true;
 		}
        _logger.info("returning isAlreadySent [" + retVal + "]");
        
        return retVal;
 	}
 
 	/**
 	 * To run this in Eclipse -
 	 * 
 	 * 1. Copy log4j.properties from bin/ into java/ folder
 	 * 2. Add java/ folder into the Run classpath
 	 * 3. Add program arguments "[full path]\config.xml" in the Run
 	 */
 	public static void main(String[] args) {
         if (args.length != 1)
         {
         	System.err.println(NotifyPassword.class.getName() + " config.xml");
             return;
         }
 		//=== open this for test
 //        LocalDateTime time = new LocalDateTime()/*.withDayOfWeek(DateTimeConstants.MONDAY)*/.withHourOfDay(12);
 //        LocalDateTime time = new LocalDateTime().plusDays(12);	//12 days later, should be Mon (11/5)
 //        LocalDateTime time = new LocalDateTime().plusDays(1);	//1 days later
 //        DateTimeUtils.setCurrentMillisFixed(time.toDateTime().toInstant().getMillis());
         
         System.out.println("Current DateTime is " + new Date(DateTimeUtils.currentTimeMillis()));
 		NotifyPassword np = new NotifyPassword(null);
 
 		try {
 			_logger.info("");
 			_logger.info(NotifyPassword.class.getClass().getName() + " begins");
 			np.doAll(args[0]);
 		} catch (Exception ex) {
 			_logger.error(ex.toString(), ex);
 		}
 	}
 }
