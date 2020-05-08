 package org.wwald.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.wwald.model.Competency;
 import org.wwald.model.Course;
 import org.wwald.model.CourseEnrollmentStatus;
 import org.wwald.model.Mentor;
 import org.wwald.model.NonExistentCourse;
 import org.wwald.model.Role;
 import org.wwald.model.StaticPagePOJO;
 import org.wwald.model.StatusUpdate;
 import org.wwald.model.User;
 import org.wwald.model.UserCourseStatus;
 import org.wwald.model.UserMeta;
 import org.wwald.util.CompetencyUniqueIdGenerator;
 import org.wwald.view.UserForm;
 import org.wwald.view.UserForm.Field;
 
 /**
  * This class implements the {@link IDataFacade} interface with an RDBMS
  * database. The code talks to the database through the JDBC interface.
  * @author pshah
  *
  */
 public class DataFacadeRDBMSImpl implements IDataFacade {
 	
 	private static final String METHOD_NOT_IMPLEMENTED = "method not implemented";
 	private final String NULL_CONN_ERROR_MSG = "conn cannot be null";	
 	private static Logger cLogger = Logger.getLogger(DataFacadeRDBMSImpl.class);
 	
 	public DataFacadeRDBMSImpl() {		
 			
 	}
 
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveCourses(Connection)
 	 */
 	public List<Course> retreiveCourses(Connection conn) throws DataException {
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		List<Course> courses = null;
 		
 		String sqlToFetchAllCourses = "SELECT * FROM COURSE;";
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlToFetchAllCourses);
 			
 			courses = buildCourseObjectsFromResultSet(rs);
 			buildCompetenciesForCourses(conn, courses);
 			buildMentorsForCourses(conn, courses);
 		} catch(SQLException sqle) {
 			String msg = "Could not build courses";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		
 		return courses;
 	}
 
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveCourseWiki(Connection)
 	 */
 	public String retreiveCourseWiki(Connection conn) throws DataException {
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		String wikiContents = "";
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(Sql.RETREIVE_COURSES_WIKI);
 			if(rs.next()) {
 				wikiContents = rs.getString(2);
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not fetch courses wiki " +
 						 "contents from the database";
 			cLogger.warn(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return wikiContents;
 	}
 
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveCourse(Connection, String)
 	 */
 	public Course retreiveCourse(Connection conn, 
 								 String id) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(id == null) {
 			throw new NullPointerException("id cannot be null");
 		}
 		Course course = null;
 		try {
 			String sql = String.format(Sql.RETREIVE_COURSE, 
 					 				   wrapForSQL(id));
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while(rs.next()) {
 				String title = rs.getString(2);
 				String description = rs.getString(3);
 				course = new Course(id, title, description);
 				if(course != null) {
 					List<Course> courses = new ArrayList<Course>();
 					courses.add(course);
 					buildCompetenciesForCourses(conn, courses);
 					buildMentorsForCourses(conn, courses);
 					course = courses.get(0);
 				}
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive course '" + id + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		
 		return course;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#insertCourse(Connection, Course)
 	 */
 	public void insertCourse(Connection conn, 
 							 Course course) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(course == null) {
 			String msg = "course cannot be null";
 			throw new NullPointerException(msg);
 		}
 		Statement stmt = null;
 
 		try {
 			//create course
 			String sql = String.format(Sql.INSERT_COURSE_BASIC, 
 					 				   wrapForSQL(course.getId()), 
 					 				   wrapForSQL(course.getTitle()));
 			stmt = conn.createStatement(); 
 			stmt.executeUpdate(sql);
 			
 			//create course_competency_wiki 
 			stmt = conn.createStatement();
 			sql = String.format(Sql.INSERT_COMPETENCY_BASIC, 
 					 			wrapForSQL(course.getId()));
 			stmt.executeUpdate(sql);
 		} catch(SQLException sqle) {
 			String msg = "Could not create new course " + course;
 			cLogger.error(msg, sqle);
 		}
 	}
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateCourseWiki(Connection, String)
 	 */
 	//TODO: Document the wiki contents
 	//TODO: Refactor method name to updateCoursesWiki
 	public void updateCourseWiki(Connection conn, 
 								 String wikiContents) 
 		throws DataException {
 		
 		//TODO: If we change the course title then the changes should be reflected in the db
 		//Also we may want to do some basic validating parsing here... or somewhere before we
 		//save the contents
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(wikiContents == null) {
 			throw new NullPointerException("wikiContents cannot be null");
 		}
 		String coursesWikiContents = (String)wikiContents;
 		//TODO: Use Sql.java
 		String sql = String.format(Sql.UPDATE_COURSES_WIKI, 
 				 				   wrapForSQL(coursesWikiContents));
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			int rowsUpdated = stmt.executeUpdate(sql);
 			if(rowsUpdated > 0) cLogger.info("CoursesWiki updated");
 			else cLogger.info("CoursesWiki not updated");
 		} catch(SQLException sqle) {
 			String msg = "Could not update CoursesWiki with new data '" + 
 						 wikiContents + "'" ;
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#getCourseEnrollmentStatus(Connection, UserMeta, Course)
 	 */
 	public CourseEnrollmentStatus getCourseEnrollmentStatus(Connection conn, 
 															UserMeta userMeta, 
 															Course course) 
 		throws DataException {
 		
 		String sqlTemplate = Sql.RETREIVE_COURSE_ENROLLMENT_STATUS;
 		String sql = String.format(sqlTemplate,
 								   wrapForSQL(course.getId()),
 								   userMeta.getUserid());
 		List<CourseEnrollmentStatus> statuses = new ArrayList<CourseEnrollmentStatus>();
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			
 			while(rs.next()) {
 				String courseId = rs.getString("course_id");
 				int userid = rs.getInt("userid");
 				int userCourseStatusId = rs.getInt("course_enrollment_action_id");
 				Timestamp tstamp = rs.getTimestamp("tstamp");
 				CourseEnrollmentStatus courseEnrollmentStatus = 
 					new CourseEnrollmentStatus(courseId, 
 							userid, 
 							UserCourseStatus.getUserCourseStatus(userCourseStatusId), 
 							tstamp);
 				statuses.add(courseEnrollmentStatus);
 			}
 			
 		} catch(SQLException sqle) {
 			String msg = "Could not get course enrollment status for course_id " + 
 						 course.getId() + " userid " + userMeta.getUserid();
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		if(statuses.size() > 0) {
 			Collections.sort(statuses, 
 							 CourseEnrollmentStatus.getTimestampComparator());
 			return statuses.get(statuses.size()-1);
 		}
 		else {
 			return new CourseEnrollmentStatus(course.getId(), 
 											  userMeta.getUserid(), 
 											  UserCourseStatus.UNENROLLED, 
 											  null);
 		}
 	}
 		
 	/**
 	 * @see org.wwald.service.IDataFacade#addCourseEnrollmentAction(Connection, CourseEnrollmentStatus)
 	 */
 	public void addCourseEnrollmentAction(Connection conn, 
 										  CourseEnrollmentStatus courseEnrollmentStatus) 
 		throws DataException {
 		
 		Timestamp timestamp = new Timestamp((new Date()).getTime());
 		String sql = 
 			String.format(Sql.INSERT_COURSE_ENROLLMENT_STATUS,
 						  wrapForSQL(courseEnrollmentStatus.getCourseId()),
 						  courseEnrollmentStatus.getUserid(),
 						  courseEnrollmentStatus.getUserCourseStatus().getId(),
 						  wrapForSQL(timestamp.toString()));
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(sql);
 		} catch(SQLException sqle) {
 			String msg = "Could not add CourseEnrollmentStatus " + 
 						 courseEnrollmentStatus;
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveCompetenciesWiki(Connection, String)
 	 */
 	//TODO: We return an empty string when the course does not exist as 
 	//well as when the competency wiki for that course is empty. I think we
 	//should return a null when the course does not exist
 	public String retreiveCompetenciesWiki(Connection conn, 
 										   String courseId) 
 														throws DataException {		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(courseId == null) {
 			throw new NullPointerException("courseId cannot be null");
 		}
 		String wikiContents = "";
 		String sql = Sql.RETREIVE_COMPETENCIES_WIKI;
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = 
 				stmt.executeQuery(String.format(sql, wrapForSQL(courseId)));
 			if(rs.next()) {
 				wikiContents = rs.getString(2);
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not get contents of CompetenciesWiki table";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return wikiContents;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveCompetency(Connection, String, String)
 	 */
 	public Competency retreiveCompetency(Connection conn, 
 										 String courseId, 
 										 String sCompetencyId) 
 			throws DataException {
 		//check method params
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(courseId == null) {
 			throw new NullPointerException("courseId cannot be null");
 		}
 		if(sCompetencyId == null) {
 			throw new NullPointerException("sCompetencyId cannot be null");
 		}
 		try {
 			int competencyId = Integer.parseInt(sCompetencyId);
 			if(competencyId < 0) {
 				String msg = "sCompetencyId should be a positive integer";
 				throw new IllegalArgumentException(msg);
 			}
 		} catch(NumberFormatException nfe) {
 			String msg = "sCompetencyId should be a valid positive integer";
 			throw new IllegalArgumentException(msg, nfe);
 		}
 		
 		//TODO: We should not retreive Course to get Competency
 		Competency competency = null;
 		Course course = retreiveCourse(conn, courseId);
 		competency = course.getCompetency(sCompetencyId);
 		return competency;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateCompetenciesWikiContents(Connection, String, String)
 	 */
 	public void updateCompetenciesWikiContents(Connection conn, 
 											   String courseId, 
 											   String contents) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(courseId == null) {
 			throw new NullPointerException("courseId cannot be null");
 		}
 		if(contents == null) {
 			//throw new NullPointerException("contents cannot be null");
 			contents = "";
 		}
 		String competenciesWikiContents = (String)contents; 
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			String sql = String.format(Sql.UPDATE_COMPETENCIES_WIKI, 
 									   wrapForSQL(competenciesWikiContents), 
 									   wrapForSQL(courseId));
 			int rowsUpdated = stmt.executeUpdate(sql);
 			if(rowsUpdated > 0) { 
 				cLogger.info("CompetenciesWiki updated");
 			}
 			else { 
 				cLogger.info("CompetenciesWiki not updated");
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not update CompetenciesWiki due to an Exception";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#insertCompetency(Connection, Course, String)
 	 */
 	public Competency insertCompetency(Connection conn, 
 									   Course course, 
 									   String competencyTitle) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(course == null) {
 			throw new NullPointerException("course should not be null");
 		}
 		if(competencyTitle == null) {
 			throw new NullPointerException("competencyTitle should not be null");
 		}
 		
 		//TODO CompetencyUniqueIdGenerator needs to go
 		Competency competency = 
 			new Competency(CompetencyUniqueIdGenerator.getNextCompetencyId(conn), 
 						   competencyTitle, 
 						   "", 
 						   "");
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			String sql = String.format(Sql.INSERT_COMPETENCY, 
 					   				   competency.getId(),
 					   				   wrapForSQL(course.getId()),
 					   				   wrapForSQL(competencyTitle),
 					   				   wrapForSQL(""),
 					   				   wrapForSQL(""));
 			
 			int rowsUpdated = stmt.executeUpdate(sql);
 			if(rowsUpdated == 0) {
 				String msg = "Could not insert competency '" + 
 							 competencyTitle + 
 							 "' in course '" + 
 							 course.getId() + 
 							 "'";
 				throw new DataException(msg);
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not insert competency for title '" + 
 						 competencyTitle + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return competency;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateCompetency(Connection, String, Competency)
 	 */
 	public void updateCompetency(Connection conn, 
 								 String courseId, 
 								 Competency competency) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(courseId == null) {
 			throw new NullPointerException("courseId should not be null");
 		}
 		if(competency == null) {
 			throw new NullPointerException("competency should not be null");
 		}
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			String sql = String.format(Sql.UPDATE_COMPETENCY, 
 									   wrapForSQL(competency.getDescription()), 
 									   wrapForSQL(competency.getResource()), 
 									   String.valueOf(competency.getId()));
 			stmt.executeUpdate(sql);
 		} catch (SQLException e) {
 			String msg = "Could not update competency with these new values";
 			cLogger.error(msg, e);
 		}
 		
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#deleteCompetency(Connection, Competency)
 	 */
 	public void deleteCompetency(Connection conn, 
 								 Competency competency) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#deleteMentor(Connection, Mentor)
 	 */
 	public void deleteMentor(Connection conn, 
 							 Mentor mentor) 
 		throws DataException {
 
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#insertMentor(Connection, Mentor)
 	 */
 	public void insertMentor(Connection conn, 
 							 Mentor mentor) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 
 		
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveMentorsForCompetency(Connection)
 	 */
 	//TOTO: Where is the Competency?
 	public List<Mentor> retreiveMentorsForCompetency(Connection conn) 
 		throws DataException {
 		
 		if(true)
 			throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 		
 		return null;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveAllCompetencies(Connection)
 	 */
 	public List<Competency> retreiveAllCompetencies(Connection conn) 
 		throws DataException {
 		
 		if(true)
 			throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 		
 		return null;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveAllMentors(Connection)
 	 */
 	public List<Mentor> retreiveAllMentors(Connection conn) throws DataException {
 		List<Mentor> mentors = new ArrayList<Mentor>();
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(Sql.RETREIVE_ALL_MENTORS);
 			while(rs.next()) {
 				Mentor mentor = new Mentor();
 				mentor.setUserid(rs.getInt("userid"));
 				mentor.setIdentifier(rs.getString("identifier"));
 				String role = rs.getString("role");
 				mentor.setRole(Role.valueOf(role));
				mentor.setLoginVia(UserMeta.LoginVia.valueOf(rs.getString("login_via")));
 				mentors.add(mentor);
 			}			
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive mentors";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		
 		return mentors;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveCompetenciesForCourse(Connection, Course)
 	 */
 	public List<Competency> retreiveCompetenciesForCourse(Connection conn, 
 														  Course course) 
 		throws DataException {
 		
 		if(true)
 			throw new RuntimeException("method not implemented");
 		
 		return null;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveMentorsForCourse(Connection)
 	 */
 	public List<Mentor> retreiveMentorsForCourse(Connection conn) 
 		throws DataException {
 		
 		//TODO: Need to provide a Course object as a param
 		if(true)
 			throw new RuntimeException("method not implemented");
 		
 		return null;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateCourse(Connection, Course)
 	 */
 	public void updateCourse(Connection conn, 
 							 Course course) 
 		throws DataException {
 		
 		String sql = String.format(Sql.UPDATE_COURSE, 
 								   wrapForSQL(course.getTitle()), 
 								   wrapForSQL(course.getDescription()), 
 								   wrapForSQL(course.getId()));
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(course == null) {
 			throw new NullPointerException("course cannot be null");
 		}
 		try {
 			cLogger.info("Executing SQL '''" + sql + "'''");
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(sql);
 			
 			//TODO: Good Samaritan is the default mentor for every course... if the mentor was removed then add this one
 			sql = String.format(Sql.UPDATE_COURSE_MENTORS, 
 								course.getMentor().getUserid(), 
 								wrapForSQL(course.getId()));
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sql);
 		} catch(SQLException sqle) {
 			String msg = "Could not update course " + course.getId();
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateMentor(Connection, Mentor)
 	 */
 	public void updateMentor(Connection conn, Mentor mentor) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#upsertCompetency(Connection, Competency)
 	 */
 	public void upsertCompetency(Connection conn, 
 								 Competency competency) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#upsertCourse(Connection, Course)
 	 */
 	public void upsertCourse(Connection conn, 
 							 Course course) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#upsertMentor(Connection, Mentor)
 	 */
 	public void upsertMentor(Connection conn, 
 							 Mentor mentor) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#getStatusUpdates(Connection)
 	 */
 	public List<StatusUpdate> getStatusUpdates(Connection conn) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		
 		try {
 			List<CourseEnrollmentStatus> courseEnrollmentStatuses = 
 				getAllCourseEnrollmentStatuses(conn);
 			List<StatusUpdate> statusUpdates = new ArrayList<StatusUpdate>();
 //			DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
 			//TODO: Factor this out into a separate StatusUpdateFormat
 			for(CourseEnrollmentStatus courseEnrollmentStatus : courseEnrollmentStatuses) {
 				
 				Timestamp timestamp = courseEnrollmentStatus.getTimestamp();
 				int userid = courseEnrollmentStatus.getUserid();
 				UserMeta userMeta = retreiveUserMeta(conn, userid);
 				String enrollmentStatus = 
 					getEnrollmentStatusWithSurroundingText(courseEnrollmentStatus.getUserCourseStatus());
 				String courseId = courseEnrollmentStatus.getCourseId();
 				
 				StatusUpdate statusUpdate = new StatusUpdate();
 				statusUpdate.setTimestamp(timestamp);
 				statusUpdate.setUserMeta(userMeta);
 				statusUpdate.setEnrollmentStatus(enrollmentStatus);
 				statusUpdate.setCourseId(courseId);
 				
 				statusUpdates.add(statusUpdate);
 			}    	
 	    	return statusUpdates;
 		} catch(SQLException sqle) {
 			String msg = "Could not get status updates due to an exception";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#insertUser(Connection, User, UserMeta)
 	 */
 	public void insertUser(Connection conn, 
 						   User user, 
 						   UserMeta userMeta) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(user == null) {
 			throw new NullPointerException("user cannot be null");
 		}
 		if(userMeta == null) {
 			throw new NullPointerException("userMeta cannot be null");
 		}
 		//TODO: This method must return the UserMeta object with the updated UserMeta
 		try {
 //			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			Statement stmt = conn.createStatement();
 			String sql = String.format(Sql.INSERT_USER, 
 									   wrapForSQL(user.getUsername()),
 									   wrapForSQL(user.getEncryptedPassword()),
 									   wrapForSQL(user.getEmail()),
 									   wrapForSQL(userMeta.getIdentifier()),
 									   wrapForSQL(userMeta.getLoginVia().toString()),
 									   wrapForSQL(Role.STUDENT.toString())); //forcing a new user to have the role of student
 			cLogger.info("Executing SQL '" + sql + "'");
 			stmt.executeUpdate(sql);			
 		} catch(SQLException sqle) {
 			String msg = "Could not insert User due to an exception";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateUser(Connection, User, Field...)
 	 */
 	public void updateUser(Connection conn, 
 						   User user, 
 						   UserForm.Field... userFields) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(user == null) {
 			throw new NullPointerException("user cannot be null");
 		}
 		for(UserForm.Field field : userFields) {
 			if(field.equals(UserForm.Field.USERNAME)) {
 				String msg = "updateUser should never be given UserForm.Field.USERNAME";
 				throw new IllegalArgumentException(msg);
 			}
 		}
 		try {
 			Statement stmt = conn.createStatement();
 			String sql = null;
 			if(userFields == null || userFields.length == 0) {
 				sql = String.format(Sql.UPDATE_USER, 
 						   wrapForSQL(user.getEmail()),
 						   wrapForSQL(user.getEncryptedPassword()),
 						   wrapForSQL(user.getUsername()));
 			}
 			else {
 				sql = "UPDATE USER SET ";
 				for(UserForm.Field field : userFields) {
 					sql += field.getDbColName() + "=%s, ";
 				}
 				//remove the comma
 				sql = sql.substring(0,sql.length()-2);
 				sql = sql + " ";
 				//get back to building the sql
 				sql += "WHERE username=%s;";
 				sql = String.format(sql, getUserFieldValues(userFields, user));
 			}
 			int rowsUpdated = stmt.executeUpdate(sql);
 			if(rowsUpdated == 0) {
 				String msg = "Tried updating user but 0 rows were affected '" + 
 							 user + "'";
 				cLogger.warn(msg);
 			}
 			else {
 				String msg = "User updated new values '" + user + "'";
 				cLogger.info(msg);
 			}
 			
 		} catch(SQLException sqle) {
 			String msg = "Could not update user due to an exception";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 		
 	private String[] getUserFieldValues(Field[] userFields, 
 										User user) {
 		String retVal[] = null;
 		List<String> userFieldValues = new ArrayList<String>();
 		for(Field userField : userFields) {
 			switch(userField) {
 				case PASSWORD:
 					userFieldValues.add(wrapForSQL(user.getEncryptedPassword()));
 					break;
 				case EMAIL:
 					userFieldValues.add(wrapForSQL(user.getEmail()));
 					break;
 			}
 		}
 		userFieldValues.add(wrapForSQL(user.getUsername()));
 		retVal = new String[userFieldValues.size()];
 		retVal = userFieldValues.toArray(retVal);
 		return retVal;
 	}
 
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveAllUsers(Connection)
 	 */
 	public List<User> retreiveAllUsers(Connection conn) throws DataException {
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		List<User> users = new ArrayList<User>();
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(Sql.RETREIVE_ALL_USERS);
 			while(rs.next()) {
 				String userName = rs.getString("username");
 				String email = rs.getString("email");
 				User newUser = new User(userName, email);
 				users.add(newUser);
 			}
 			return users;
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive Users from the database";
 			throw new DataException(msg, sqle);
 		}
 		
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreivePassword(Connection, String)
 	 */
 	public String retreivePassword(Connection conn, 
 								   String username) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(username == null) {
 			throw new NullPointerException("username should not be null");
 		}
 		String password = null;
 		String sql = String.format(Sql.RETREIVE_PASSWORD, wrapForSQL(username));
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			if(rs.next()) {
 				password = rs.getString("password");
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive password for user '" + username + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return password;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveUserByUsername(Connection, String)
 	 */
 	public User retreiveUserByUsername(Connection conn, 
 									   String username) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(username == null) {
 			throw new NullPointerException("username should not be null");
 		}
 		User user = null;
 		try {
 			Statement stmt = conn.createStatement();
 			String query = String.format(Sql.RETREIVE_USER_BY_USERNAME, 
 										 wrapForSQL(username));
 			ResultSet rs  = stmt.executeQuery(query);
 			if(rs.next()) {
 				String userName = rs.getString("username");				
 				String email = rs.getString("email");
 				user = new User(userName, email);
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive User from database";
 			throw new DataException(msg, sqle);
 		}
 		return user; 
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#insertUserMeta(Connection, UserMeta)
 	 */
 	public void insertUserMeta(Connection conn, 
 							   UserMeta userMeta) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(userMeta == null) {
 			throw new NullPointerException("userMeta should not be null");
 		}
 		String sql = String.format(Sql.INSERT_USER_META, 
 								   wrapForSQL(userMeta.getIdentifier()), 								    
 								   wrapForSQL(userMeta.getLoginVia().toString()),
 								   wrapForSQL(userMeta.getRole().toString()));
 		try {
 			Statement stmt = conn.createStatement();
 			cLogger.info("Executing SQL '" + sql + "'");
 			int rows = stmt.executeUpdate(sql);
 			System.out.println("rows updated " + rows);
 		} catch(SQLException sqle) {
 			String msg = "Could not insert UserMeta";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveUserMeta(Connection, int)
 	 */
 	public UserMeta retreiveUserMeta(Connection conn, 
 									 int userid) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(userid < 0) {
 			String msg = "userid should be a positive integer";
 			throw new IllegalArgumentException(msg);
 		}
 		UserMeta userMeta = null;
 		String sql = String.format(Sql.RETREIVE_USER_META, String.valueOf(userid));
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			if(rs.next()) {
 				userMeta = new UserMeta();
 				userMeta.setUserid(rs.getInt("userid"));
 				userMeta.setIdentifier(rs.getString("identifier"));
 				userMeta.setRole(Role.valueOf(rs.getString("role")));
 				userMeta.setLoginVia(UserMeta.LoginVia.valueOf(rs.getString("login_via")));
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive UserMeta for userid '" + userid + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return userMeta;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveUserMetaByIdentifierLoginVia(Connection, String, org.wwald.model.UserMeta.LoginVia)
 	 */
 	public UserMeta retreiveUserMetaByIdentifierLoginVia(Connection conn,
 													 	 String identifier, 
 													 	 UserMeta.LoginVia loginVia) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(identifier == null) {
 			throw new NullPointerException("identifier should not be null");
 		}
 		if(loginVia == null) {
 			throw new NullPointerException("loginVia should not be null");
 		}
 		
 		UserMeta userMeta = null;
 		try {
 			Statement stmt = conn.createStatement();
 			String query = String.format(Sql.RETREIVE_USER_META_BY_IDETIFIER_LOGIN_VIA, 
 										 wrapForSQL(identifier),
 										 wrapForSQL(loginVia.toString()));
 			ResultSet rs = stmt.executeQuery(query);
 			if(rs.next()) {
 				int userid = rs.getInt("userid");
 				String userMetaIdentifer = rs.getString("identifier");
 				String userMetaLoginVia = rs.getString("login_via");
 				userMeta = new UserMeta();
 				userMeta.setUserid(userid);
 				userMeta.setIdentifier(userMetaIdentifer);
 				userMeta.setLoginVia(UserMeta.LoginVia.valueOf(userMetaLoginVia));
 				userMeta.setRole(Role.valueOf(rs.getString("role")));
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive UserMeta for '" + 
 						 identifier + "' '" + 
 						 loginVia + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return userMeta;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveAllUserMeta(Connection)
 	 */
 	public List<UserMeta> retreiveAllUserMeta(Connection conn) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		List<UserMeta> allUserMeta = new ArrayList<UserMeta>();
 		
 		try {
 			Statement stmt = conn.createStatement();
 			cLogger.info("Executing Sql '" + Sql.RETREIVE_ALL_USER_META + "'");
 			ResultSet rs = stmt.executeQuery(Sql.RETREIVE_ALL_USER_META);
 			while(rs.next()) {
 				UserMeta userMeta = new UserMeta();
 				userMeta.setUserid(rs.getInt("userid"));
 				userMeta.setIdentifier(rs.getString("identifier"));
 				userMeta.setRole(Role.valueOf(rs.getString("role")));
 				userMeta.setLoginVia(UserMeta.LoginVia.valueOf(rs.getString("login_via")));
 				allUserMeta.add(userMeta);
 			}
 		} catch(SQLException sqle) {
 			String msg = "Could not fetch all UserMeta objects";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 		return allUserMeta;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#updateUserMetaRole(Connection, UserMeta)
 	 */
 	public void updateUserMetaRole(Connection conn, 
 								   UserMeta userMeta) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(userMeta == null) {
 			String msg = "userMeta should not be null";
 			throw new NullPointerException(msg);
 		}
 		String sql = String.format(Sql.UPDATE_USER_META_ROLE, 
 								   wrapForSQL(userMeta.getRole().toString()),
 								   userMeta.getUserid());
 		try {
 			Statement stmt = conn.createStatement();
 			cLogger.info("Executing SQL '" + sql + "'");
 			stmt.executeUpdate(sql);
 		} catch(SQLException sqle) {
 			String msg = "Could not update userMeta '" + userMeta + "'";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveStaticPage(Connection, String)
 	 */
 	public StaticPagePOJO retreiveStaticPage(Connection c, 
 											 String id) 
 		throws DataException {
 		
 		if(c == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(id == null) {
 			String msg = "id should not be null";
 			throw new NullPointerException(msg);
 		}
 		StaticPagePOJO page = null;
 		String sql = String.format(Sql.RETREIVE_STATIC_PAGE, wrapForSQL(id));
 		try {
 			Statement stmt = c.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			if(rs.next()) {
 				String pageContents = rs.getString("contents");
 				page = new StaticPagePOJO(id, pageContents);
 			}
 			return page;
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive static page " + id;
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#upsertStaticPage(Connection, StaticPagePOJO)
 	 */
 	public void upsertStaticPage(Connection conn, 
 								 StaticPagePOJO page) 
 		throws DataException {
 		
 		if(conn == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(page == null) {
 			String msg = "page should not be null";
 			throw new NullPointerException(msg);
 		}
 		StaticPagePOJO preexistingPage = retreiveStaticPage(conn, page.getId());
 		if(preexistingPage == null) {
 			insertStaticPage(conn, page);
 		}
 		else {
 			updateStaticPage(conn, page);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveFromKvTable(Connection, String)
 	 */
 	public String retreiveFromKvTable(Connection c, 
 									  String k) 
 		throws DataException {
 		
 		if(true)
 			throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 
 		return null;
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#upsertKvTable(Connection, String, String)
 	 */
 	public void upsertKvTable(Connection c, 
 							  String k, 
 							  String v) 
 		throws DataException {
 		
 		throw new RuntimeException(METHOD_NOT_IMPLEMENTED);
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#retreiveFromKvTableClob(Connection, String)
 	 */
 	public String retreiveFromKvTableClob(Connection c, 
 										  String k) 
 		throws DataException {
 		
 		if(c == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(k == null) {
 			String msg = "k should not be null";
 			throw new NullPointerException(msg);
 		}
 		String retVal = null;
 		try {
 			Statement stmt = c.createStatement();
 			ResultSet rs = 
 				stmt.executeQuery(String.format(Sql.RETREIVE_KVTABLE_CLOB,
 												wrapForSQL(k)));
 			if(rs.next()) {
 				retVal = rs.getString("v");
 			}
 			return retVal;
 		} catch(SQLException sqle) {
 			String msg = "Could not retreive value from KVTABLE_CLOB";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	
 	/**
 	 * @see org.wwald.service.IDataFacade#upsertKvTableClob(Connection, String, String)
 	 */
 	public void upsertKvTableClob(Connection c, 
 								  String k, 
 								  String v) 
 		throws DataException {
 		
 		if(c == null) {
 			throw new NullPointerException(NULL_CONN_ERROR_MSG);
 		}
 		if(k == null) {
 			String msg = "k should not be null";
 			throw new NullPointerException(msg);
 		}
 		if(v == null) {
 			String msg = "v should not be null";
 			throw new NullPointerException(msg);
 		}
 		try {
 			String existingVal = retreiveFromKvTableClob(c, k);
 			Statement stmt = c.createStatement();
 			String sql = null;
 			if(existingVal == null) {
 				sql = String.format(Sql.INSERT_KVTABLE_CLOB, 
 									wrapForSQL(k), 
 									wrapForSQL(v));
 			}
 			else {
 				sql = String.format(Sql.UPDATE_KVTABLE_CLOB, 
 									wrapForSQL(v), 
 									wrapForSQL(k));
 			}
 			stmt.executeUpdate(sql);
 		} catch(DataException de) {
 			String msg = "Could not check if the key already exists " +
 						 "before upserting the value in " +
 						 "KVTABLE_CLOB ('" + k + "','" + v + "')";
 			cLogger.error(msg, de);
 			throw new DataException(msg, de);
 		} catch(SQLException sqle) {
 			String msg = "Could not upsert into " +
 						 "KVTABLE_CLOB ('" + k + "','" + v + "')";
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 
 	private void insertStaticPage(Connection conn, 
 								  StaticPagePOJO page) 
 		throws DataException {
 		
 		String sql = String.format(Sql.INSERT_STATIC_PAGE, 
 								   wrapForSQL(page.getId()), 
 								   wrapForSQL(page.getContents()));
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(sql); 
 		} catch(SQLException sqle) {
 			String msg = "Could not insert page " + page;
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	private void updateStaticPage(Connection conn, 
 								  StaticPagePOJO page) 
 		throws DataException {
 		
 		String sql = String.format(Sql.UPDATE_STATIC_PAGE, 
 								   wrapForSQL(page.getContents()), 
 								   wrapForSQL(page.getId()));
 		
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(sql);
 		} catch(SQLException sqle) {
 			String msg = "Could not update page " + page;
 			cLogger.error(msg, sqle);
 			throw new DataException(msg, sqle);
 		}
 	}
 	
 	/**
 	 * This method parses the courses wiki contents and returns a List of Course objects. 
 	 * Every course which exists in the database is returned as a {@link Course} object in  
 	 * the returned List, whereas every course which does not exist in the database is
 	 * represented as a {@link NonExistentCourse} object.
 	 * NOTE: This method should never return a null.
 	 * @param conn Connection to the database
 	 * @param wikiContent The contents of the wiki as a String
 	 * @return courses A List of Course objects which were mentioned in the wiki contents 
 	 * @throws IOException
 	 */
 	private List<Course> 
 			buildCourseObjectsFromCoursesWikiContent(Connection conn,
 													 String wikiContent) 
 		throws IOException, DataException {
 		
 		List<Course> courses = new ArrayList<Course>();
 		BufferedReader bufferedReader = new BufferedReader(new StringReader(
 				wikiContent));
 		String line = null;
 		while ((line = bufferedReader.readLine()) != null) {
 			String lineTokens[] = line.split("\\|");
 			String courseId = lineTokens[0];
 			String courseTitle = lineTokens[1];
 			// It does not matter if the courseTitle is null, but as a rule we
 			// want the wikiContent
 			// to contain both for each course
 			if (courseId != null && courseTitle != null) {
 				Course course = retreiveCourse(conn, courseId.trim());
 				if (course == null) {
 					course = new NonExistentCourse(courseId, courseTitle);
 				}
 				
 				courses.add(course);
 			}
 
 		}
 		return courses;
 	}
 	
 	private List<Course> buildCourseObjectsFromResultSet(ResultSet rs) 
 		throws SQLException {
 		
 		if(rs == null) {
 			return null;
 		}
 		List<Course> courses = new ArrayList<Course>();
 		while(rs.next()) {
 			String id = rs.getString(1);
 			String title = rs.getString(2);
 			String description = rs.getString(3);
 			Course course = new Course(id, title, description);
 			courses.add(course);
 		}
 		return courses;
 	}
 	
 	private void buildCompetenciesForCourses(Connection conn, 
 											 List<Course> courses) 
 		throws SQLException, DataException {
 					 
 		String sql = Sql.RETREIVE_COMPETENCIES_WIKI;
 		for(Course course : courses) {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(String.format(sql, wrapForSQL(course.getId())));
 			List<Competency> competencies = buildCompetencyObjectsFromResultSet(conn, course, rs);
 			course.setCompetencies(competencies);
 			
 		}
 	}
 
 	private void buildMentorsForCourses(Connection conn, 
 										List<Course> courses) 
 		throws SQLException {
 		
 		String sqlToGetMentorIdsForCourse = Sql.RETREIVE_MENTORS_FOR_COURSE;
 		for(Course course : courses) {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = 
 				stmt.executeQuery(String.format(sqlToGetMentorIdsForCourse, 
 												wrapForSQL(course.getId())));
 			
 			List<Mentor> mentors = new ArrayList<Mentor>();
 			while(rs.next()) {
 				int mentorUserid = rs.getInt("mentor_userid");
 				Statement stmt1 = conn.createStatement();
 				ResultSet mentorsResultSet = 
 					stmt1.executeQuery(String.format(Sql.RETREIVE_USER_META, 
 													 mentorUserid));
 				while(mentorsResultSet.next()) {
 					String role = mentorsResultSet.getString("role");
 					Mentor mentor = new Mentor();
 					mentor.setUserid(mentorUserid);
 					mentor.setIdentifier(mentorsResultSet.getString("identifier"));
 					mentor.setLoginVia(UserMeta.LoginVia.valueOf(mentorsResultSet.getString("login_via")));
 					mentor.setRole(Role.valueOf(role));
 					if(mentor.getRole().equals(Role.MENTOR)) {
 						mentors.add(mentor);
 					}
 					else {
 						String msg = "Found a user in COURSE_MENTOR table " +
 									 "who is not a mentor " + mentorUserid;
 						cLogger.warn(msg);
 					}
 				}
 			}
 			
 			if(mentors.size() == 0) {
 				cLogger.warn("Mentors were not found for course : " + course.getId());				
 			}
 			else if(mentors.size() == 1){
 				course.setMentor(mentors.get(0));
 			}
 			else if(mentors.size() > 1) {
 				course.setMentor(mentors.get(0));
 				cLogger.warn("Found multiple mentors for course " + course.getId());
 			}
 		}
 	}
 
 	//TODO: course is being pased all round the place.... make safe
 	private List<Competency> 
 		buildCompetencyObjectsFromResultSet(Connection conn, 
 											Course course, 
 											ResultSet rs) 
 		throws SQLException, DataException {
 		
 		if(rs == null) {
 			return null;
 		}
 		List<Competency> competencies = new ArrayList<Competency>();
 		if(rs.next()) {
 			String competencyWikiContents = rs.getString("contents");
 			String competencyTitles[] = 
 				parseForCompetencyTitles(competencyWikiContents);
 			
 			for(String competencyTitle : competencyTitles) {
 				String sqlToFetchCompetency = Sql.RETREIVE_COMPETENCIES_BY_COURSE_AND_COMPETENCY_TITLE;
 				Statement stmt = conn.createStatement();
 				String finalSql = String.format(sqlToFetchCompetency, 
 												wrapForSQL(course.getId()),
 												wrapForSQL(competencyTitle));
 				ResultSet rsForCompetencies = stmt.executeQuery(finalSql);
 				if(rsForCompetencies.next()) {
 					int id = rsForCompetencies.getInt(1);
 					String title = rsForCompetencies.getString(3);
 					String description = rsForCompetencies.getString(4);
 					String resource = rsForCompetencies.getString(5);
 					Competency competency = new Competency(id, 
 														   title, 
 														   description, 
 														   resource);
 					competencies.add(competency);
 				}
 				else {
 					Competency competency = insertCompetency(conn, 
 															 course, 
 															 competencyTitle);
 					if(competency != null) {
 						competencies.add(competency);
 					}
 				}
 			}
 		}
 		return competencies;
 	}
 
 	private String[] parseForCompetencyTitles(String competencyWikiContents) {
 		List<String> titlesList = new ArrayList<String>();
 		BufferedReader bufferedReader = 
 			new BufferedReader(new StringReader(competencyWikiContents));
 		String line = null;
 		try {
 			while((line = bufferedReader.readLine()) != null) {
 				titlesList.add(line.trim());
 			}
 		} catch(IOException ioe) {
 			
 		}
 		return titlesList.toArray(new String[titlesList.size()]);
 	}
 
 	private List<CourseEnrollmentStatus> 
 		getAllCourseEnrollmentStatuses(Connection conn) 
 		throws SQLException {
 		
 		//TODO: Limit this query to 5 rows
 		String sql = Sql.RETREIVE_ALL_COURSE_ENROLLMENT_STATUSES;
 		List<CourseEnrollmentStatus> statuses = 
 			new ArrayList<CourseEnrollmentStatus>();
 		
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		
 		while(rs.next()) {
 			String courseId = rs.getString("course_id");
 			int userid = rs.getInt("userid");
 			int userCourseStatusId = rs.getInt("course_enrollment_action_id");
 			Timestamp tstamp = rs.getTimestamp("tstamp");
 			CourseEnrollmentStatus courseEnrollmentStatus = 
 				new CourseEnrollmentStatus(courseId, 
 										   userid, 
 										   UserCourseStatus.
 										   	getUserCourseStatus(userCourseStatusId), 
 										   						tstamp);
 			statuses.add(courseEnrollmentStatus);
 		}	
 		 
 		return statuses;
 	}
 	
 	/**
 	 * Wraps the specified String with single quotes and replaces all instances
 	 * of single quotes within the String with two single quotes. Thus it makes
 	 * the String suitable for representing values in SQL statements.
 	 * Eg: hello -> 'hello'
 	 * Susan's -> 'Susan''s'
 	 * @param s
 	 * @return The String which has been made suitable (as explained above) for
 	 * representing values in SQL statements 
 	 */
 	public static String wrapForSQL(String s) {
 		if(s == null) {
 			s = "";
 		}
 		String escapedStr = s.replaceAll("'", "''");
 		return "'" + escapedStr + "'";
 	}
 	
 	private String getEnrollmentStatusWithSurroundingText(
 			UserCourseStatus userCourseStatus) {
 		String retVal = "";
 		switch(userCourseStatus) {
 			case COMPLETED:
 				retVal = UserCourseStatus.COMPLETED.toString();
 				break;
 			case DROPPED:
 				retVal = UserCourseStatus.DROPPED.toString();
 				break;
 			case ENROLLED:
 				retVal = UserCourseStatus.ENROLLED.toString() + " in ";
 				break;
 			default:
 				retVal = userCourseStatus.toString();
 		}
 		return retVal;
 	}	
 
 }
