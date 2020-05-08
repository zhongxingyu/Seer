 package org.wwald.service;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.jasypt.util.password.BasicPasswordEncryptor;
 import org.wwald.WWALDSession;
 import org.wwald.model.ConnectionPool;
 import org.wwald.model.Course;
 import org.wwald.model.CourseEnrollmentStatus;
 import org.wwald.model.User;
 import org.wwald.model.UserCourseStatus;
 import org.wwald.model.UserMeta;
 
import util.DataInitializer;

 public class ApplicationFacade {
 	
 	private IDataFacade dataFacade;
 	
 	private static Logger cLogger = Logger.getLogger(ApplicationFacade.class);
 	
 	public ApplicationFacade(IDataFacade dataFacade) {
 		this.dataFacade = dataFacade;
 	}
 	
 	public UserMeta login(String username, String password, String databaseId) throws ApplicationException{
 		User user = null;
 		UserMeta userMeta = null;
 		try {
 			cLogger.info("Trying to login user '" + username + "'");
 			String passwordInDb = dataFacade.retreivePassword(ConnectionPool.getConnection(databaseId), username);
 			BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
 			if(passwordInDb != null && passwordEncryptor.checkPassword(password, passwordInDb)) {
 				user = dataFacade.retreiveUserByUsername(ConnectionPool.getConnection(databaseId), username);
 				if(user == null) {
 					cLogger.error("THIS SHOULD NEVER HAPPEN. COULD GET PASSWORD BUT COULD NOT GET USER OBJECT");
 				}
 				cLogger.info("User " + username + " logged in succesully");
 				userMeta = 
 					dataFacade.
 						retreiveUserMetaByIdentifierLoginVia(ConnectionPool.getConnection(databaseId), 
 														 	 user.getUsername(), 
 														 	 UserMeta.LoginVia.INTERNAL);
 			}
 			else {
 				cLogger.info("User " + username + " NOT logged in");
 			}
 		} catch(DataException de) {
 			String msg = "Could not not login user " + username;
 			cLogger.error(msg, de);
 			throw new ApplicationException(msg, de);
 		}
 		return userMeta; 
 	}
 	
 	public void logout() {
 		UserMeta userMeta = WWALDSession.get().getUserMeta();
 		String msg = "Logging out user ";
 		if(userMeta != null) {
 			msg += userMeta.getIdentifier();
 		}
 		cLogger.info(msg);
 		WWALDSession.get().invalidateNow();
 	}
 	
 	public void enrollInCourse(UserMeta user, 
 							   Course course, 
 							   String databaseId) 
 		throws ApplicationException {
 		
 		try {
 			UserCourseStatus userCourseStatus = 
 				getUserCourseStatus(user, course, databaseId);
 			if(!userCourseStatus.equals(UserCourseStatus.ENROLLED)) {
 				//update the UserCourseStatus
 				CourseEnrollmentStatus courseEnrollmentStatus = 
 					new CourseEnrollmentStatus(course.getId(), 
 											   user.getUserid(), 
 											   UserCourseStatus.ENROLLED,
 											   new Timestamp((new Date()).getTime()));
 				this.
 					dataFacade.
 						addCourseEnrollmentAction(ConnectionPool.getConnection(databaseId), 
 												  courseEnrollmentStatus);
 				
 				//TODO: Audit point because step 1 of a 2 step action is done
 				String msg1 = "'" + user.getUserid() + 
 							  "' course '" + course.getId() + 
 							  "' - Added CourseEnrollmentAction";
 				String auditMsg1 = getAuditMsg(msg1, "EnrollInCourse", 1);
 				cLogger.info(auditMsg1);
 				
 				//add entry in COURSE_ENROLLMENTS table
 				this.dataFacade.
 					insertCourseEnrollment(ConnectionPool.getConnection(databaseId), 
 										   user, 
 										   course);
 				
 				//TODO: Audit point
 				String msg2 = "'" + user.getUserid() + 
 							  "' course '" + course.getId() + 
 							  "' - Added CourseEnrollment";
 				String auditMsg2 = getAuditMsg(msg2, "EnrollInCourse", 2);
 				cLogger.info(auditMsg2);
 				cLogger.info(getAuditMsg("", "EnrollInCourse", 0));
 			}
 			else {
 				String msg = "Cannot enroll user " + user + 
 				 " in course " + course + 
 				 " because user is already enrolled in this course";
 				cLogger.error(msg);
 				throw new ApplicationException(msg);
 			}
 		} catch(DataException de) {
 			String msg = "Could not enroll user '" + 
 						 user + "' in course '" + course.getId() + "'";
 			cLogger.error(msg, de);
 			throw new ApplicationException(msg, de);
 		}
 	}
 	
 	public void dropCourse(UserMeta userMeta, 
 						   Course course, 
 						   String databaseId) 
 		throws ApplicationException {
 		
 		try {
 			//add course enrollment status for status updates
 			UserCourseStatus userCourseStatus = 
 				getUserCourseStatus(userMeta, course, databaseId);
 			
 			if(userCourseStatus.equals(UserCourseStatus.ENROLLED)) {
 				CourseEnrollmentStatus courseEnrollmentStatus = 
 					new CourseEnrollmentStatus(course.getId(), 
 											   userMeta.getUserid(), 
 											   UserCourseStatus.DROPPED,
 											   new Timestamp((new Date()).getTime()));
 				
 				this.dataFacade.
 					addCourseEnrollmentAction(ConnectionPool.getConnection(databaseId), 
 											  courseEnrollmentStatus);
 				
 				//TODO: Add audit point for step 1 of this 2 step action
 				String msg1 = "'" + userMeta.getUserid() + 
 				   			  "' in course '" + course.getId() + 
 				   			  "' - Added CourseEnrollmentStatus";
 				String auditMsg1 = getAuditMsg(msg1, "DroppedCourse", 1);
 				cLogger.info(auditMsg1);
 				
 				//now delete row from course_enrollment table
 				this.dataFacade.
 					deleteCourseEnrollment(ConnectionPool.getConnection(databaseId), 
 										   userMeta, 
 										   course);
 				
 				//TODO: Add audit point for step 2 of this 2 step action
 				String msg2 = "Dropping user '" + userMeta.getUserid() + 
 				   			  "' in course '" + course.getId() + 
 				   			  "' - Deleted CourseEnrollment";
 				String auditMsg2 = getAuditMsg(msg2, "DroppedCourse", 2);
 				cLogger.info(auditMsg2);
 				cLogger.info(getAuditMsg("", "DroppedCourse", 0));
 			}
 			else {
 				String msg = "Could not drop user " + userMeta + 
 				 " from course " + course + 
 				 " because user is not enrolled in this course";
 				cLogger.error(msg);
 				throw new ApplicationException(msg);
 			}
 		} catch(DataException de) {
 			String msg = "Could not drop user '" + userMeta + 
 						 "' from course '" + course.getId() + "'";
 			cLogger.error(msg, de);
 			throw new ApplicationException(msg, de);
 		}
 	}
 	
 	public UserCourseStatus getUserCourseStatus(UserMeta userMeta, 
 												Course course, 
 												String databaseId) 
 		throws DataException {
 		
 		UserCourseStatus retVal = UserCourseStatus.UNENROLLED;
 		
 		String sql = 
 			String.format(Sql.RETREIVE_COURSE_ENROLLMENTS_BY_USER_AND_COURSE, 
 						  userMeta.getUserid(), 
						  DataInitializer.wrapForSQL(course.getId()));
 		try {
 			Connection conn = ConnectionPool.getConnection(databaseId);
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			if(rs.next()) {
 				retVal = UserCourseStatus.ENROLLED;
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not get course enrollment status for user '" + 
 						 userMeta.getUserid() + 
 						 " in course '" + 
 						 course.getId() + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return retVal;
 	}
 	
 	private String getAuditMsg(String msg, 
 							   String auditPointId, 
 							   int auditPoint) {
 		
 		if(auditPoint < 0) {
 			throw new IllegalArgumentException("auditPoint cannot be < 0");
 		}
 		
 		StringBuffer buff = new StringBuffer();
 		buff.append("AUDIT POINT (");
 		buff.append(auditPointId + ") ");
 		if(auditPoint == 0) {
 			buff.append("COMPLETED");
 		}
 		else if(auditPoint > 0) {
 			buff.append("[");
 			buff.append(auditPoint);
 			buff.append("] - ");
 			buff.append(msg);
 		}
 		
 		return buff.toString();
 	}
 }
