 package org.wwald.model;
 
 
 import static junit.framework.Assert.*;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.jasypt.util.password.BasicPasswordEncryptor;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.wwald.service.CannotPerformActionException;
 import org.wwald.service.DataException;
 import org.wwald.service.DataFacadeRDBMSImpl;
 import org.wwald.service.Sql;
 import org.wwald.util.CompetencyUniqueIdGenerator;
 import org.wwald.view.UserForm;
 
 import util.DataInitializer;
 import util.TestObjectsRepository;
 
 public class DataFacadeRDBMSImplTest {
 
 	private DataFacadeRDBMSImpl dataFacade;
 	private Connection conn;
 	public static final String DATABASE_ID = "localhost";
 	
 		
 	
 	@Before
 	public void setUp() throws Exception {
 		CompetencyUniqueIdGenerator.reset(null);
 		this.dataFacade = new DataFacadeRDBMSImpl();
 		DataInitializer di = new DataInitializer();
 		this.conn = ConnectionPool.getConnection(DATABASE_ID);
 		di.initData(conn);		
 	}
 	
 	@Test
 	public void testRetreiveCourses() throws Exception {
 		List<Course> courses = dataFacade.retreiveCourses(this.conn);
 		
 		Assert.assertNotNull(courses);
 		Assert.assertEquals(3, courses.size());
 		
 		Course course0 = courses.get(0);
 		Course expectedCourse0 = TestObjectsRepository.getInstance().getCourse("Bio101");
 		compareCourses(expectedCourse0, course0);
 		
 		
 		Course course1 = courses.get(1);
 		Course expectedCourse1 = TestObjectsRepository.getInstance().getCourse("OrganicChem");
 		compareCourses(expectedCourse1, course1);
 		
 		Course course2 = courses.get(2);
 		Course expectedCourse2 = TestObjectsRepository.getInstance().getCourse("Physics");
 		compareCourses(expectedCourse2, course2);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCoursesWithNullConn() throws Exception {
 		dataFacade.retreiveCourses(null);
 	}
 	
 	@Test
 	public void testRetreiveCourseWiki() throws Exception {
 		StringBuffer expectedCourseWiki = new StringBuffer();
 		expectedCourseWiki.append("Physics | Introduction to Physics\n");
 		expectedCourseWiki.append("Bio101 | Introduction to Biology\n");
 		expectedCourseWiki.append("OrganicChem | Introduction to Organic Chemistry\n");
 		
 		String courseWiki = this.dataFacade.retreiveCourseWiki(this.conn);
 		assertEquals(expectedCourseWiki.toString(), courseWiki);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCourseWikiWithNullConn() throws Exception {
 		this.dataFacade.retreiveCourseWiki(null);
 	}
 	
 	@Test
 	public void testRetreiveCourse() throws Exception {
 		Course physicsCourse = this.dataFacade.retreiveCourse(this.conn, "Physics");
 		Course expectedPhysicsCourse = TestObjectsRepository.getInstance().getCourse("Physics");
 		compareCourses(expectedPhysicsCourse, physicsCourse);
 		
 		Course chemCourse = this.dataFacade.retreiveCourse(this.conn, "OrganicChem");
 		Course expectedChemCourse = TestObjectsRepository.getInstance().getCourse("OrganicChem");
 		compareCourses(expectedChemCourse, chemCourse);
 		
 		Course biologyCourse = this.dataFacade.retreiveCourse(this.conn, "Bio101");
 		Course expectedBIologyCourse = TestObjectsRepository.getInstance().getCourse("Bio101");
 		compareCourses(expectedBIologyCourse, biologyCourse);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCourseWithNullConnection() throws Exception {
 		this.dataFacade.retreiveCourse(null, "Bio101");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCourseWithNullCourseId() throws Exception {
 		this.dataFacade.retreiveCourse(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveCourseWithNonExistentCourseId() throws Exception {
 		Course course = this.dataFacade.retreiveCourse(this.conn, "abc");
 		assertEquals(true, course == null);
 	}
 	
 	@Test
 	public void testInsertCourse() throws Exception {
 		//call API to insert a course
 		Course newCourse = new Course("NC", "New Course Title", null);
 		this.dataFacade.insertCourse(this.conn, newCourse);
 		String retreiveCourseSql = 
 			String.format(Sql.RETREIVE_COURSE, 
 						  DataInitializer.wrapForSQL(newCourse.getId()), 
 						  DataInitializer.wrapForSQL(newCourse.getTitle()));
 		
 		//verify that the course was inserted
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(retreiveCourseSql);
 		Course retreivedCourse = null;
 		if(rs.next()) {
 			retreivedCourse = new Course();
 			retreivedCourse.setId(rs.getString("id"));
 			retreivedCourse.setTitle(rs.getString("title"));
 		}
 		assertNotNull("could not fetch the inserted course from the database", retreivedCourse);
 		assertEquals(newCourse.getId(), retreivedCourse.getId());
 		assertEquals(newCourse.getTitle(), retreivedCourse.getTitle());
 		
 		//verify that a CourseCompetenciesWiki has been created
 		String retreiveCompetenciesWiki = 
 			String.format(Sql.RETREIVE_COMPETENCIES_WIKI, 
 						  DataInitializer.wrapForSQL(newCourse.getId()));
 		Statement retreiveCompetenciesWikiStmt = this.conn.createStatement();
 		ResultSet competenciesWikiRS = 
 			retreiveCompetenciesWikiStmt.executeQuery(retreiveCompetenciesWiki);
 		competenciesWikiRS.next();
 		assertEquals(newCourse.getId(), competenciesWikiRS.getString("course_id"));
 		assertEquals("", competenciesWikiRS.getString("contents"));
 		
 		//verify that the forum has been created
 		String retreiveForumSql = String.format(Sql.RETREIVE_DISCUSSION_FORUM, 
 												DataInitializer.wrapForSQL(newCourse.getId()));
 		Statement retreiveForumStmt = this.conn.createStatement();
 		ResultSet retreiveForumRS = retreiveForumStmt.executeQuery(retreiveForumSql);
 		retreiveForumRS.next();
 		assertEquals(newCourse.getId(), retreiveForumRS.getString("id"));
 		assertEquals(newCourse.getTitle(), retreiveForumRS.getString("title"));
 		assertNotNull(retreiveForumRS.getString("description"));
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testInsertCourseWithNullConn() throws Exception {
 		this.dataFacade.insertCourse(null, new Course());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertCourseWithNullCourse() throws Exception {
 		this.dataFacade.insertCourse(this.conn, null);
 	}
 	
 	@Test
 	public void testUpdateCourseWiki1() throws Exception {
 		String newCoursesWiki = "NC | New Course Title\n";
 		this.dataFacade.updateCourseWiki(this.conn, newCoursesWiki);
 		String retreiveCoursesWikiSql = Sql.RETREIVE_COURSES_WIKI;
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(retreiveCoursesWikiSql);
 		String retreivedCoursesWiki = null;
 		int count = 0;
 		while (rs.next()) {
 			retreivedCoursesWiki = rs.getString("content");
 			count++;
 		}
 		assertNotNull("Could not retreive inserted courses wiki from the database", 
 					  retreivedCoursesWiki);
 		assertEquals("found more than 1 coures wiki records in the database", 1, count);
 		assertEquals(newCoursesWiki, retreivedCoursesWiki);
 	}
 	
 	@Test
 	public void testUpdateCourseWiki2() throws Exception {
 		StringBuffer updateCourseWiki = new StringBuffer();
 		updateCourseWiki.append("Physics | Introduction to Physics\n");
 		updateCourseWiki.append("Bio101 | Introduction to Biology -> Intro To Bio\n");
 		updateCourseWiki.append("OrganicChem | Introduction to Organic Chemistry\n");
 		
 		StringBuffer expectedCourseWiki = new StringBuffer();
 		expectedCourseWiki.append("Physics | Introduction to Physics\n");
 		expectedCourseWiki.append("Bio101 | Intro To Bio\n");
 		expectedCourseWiki.append("OrganicChem | Introduction to Organic Chemistry\n");
 		
 		this.dataFacade.updateCourseWiki(this.conn, updateCourseWiki.toString());
 		String retreiveCoursesWikiSql = Sql.RETREIVE_COURSES_WIKI;
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(retreiveCoursesWikiSql);
 		String retreivedCoursesWiki = null;
 		int count = 0;
 		while (rs.next()) {
 			retreivedCoursesWiki = rs.getString("content");
 			count++;
 		}
 		assertNotNull("Could not retreive inserted courses wiki from the database", 
 					  retreivedCoursesWiki);
 		assertEquals("found more than 1 coures wiki records in the database", 1, count);
 		assertEquals(expectedCourseWiki.toString(), retreivedCoursesWiki);
 		
 		String retreiveCourseSql = String.format(Sql.RETREIVE_COURSE, 
 												 DataInitializer.wrapForSQL("Bio101"));
 		stmt = this.conn.createStatement();
 		rs = stmt.executeQuery(retreiveCourseSql);
 		rs.next();
 		assertEquals("Intro To Bio", rs.getString("title"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCourseWikiWithNullConn() throws Exception {
 		this.dataFacade.updateCourseWiki(null, "");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCourseWikiWithNullContents() throws Exception {
 		this.dataFacade.updateCourseWiki(this.conn, null);
 	}
 	
 	@Test
 	public void testAddCourseEnrollment() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.insertCourseEnrollment(this.conn, user, course);
 		
 		//retrieve and test
 		String sql = String.format(Sql.RETREIVE_COURSE_ENROLLMENTS_BY_USER_AND_COURSE, 
 								   user.getUserid(),
 								   DataInitializer.wrapForSQL(course.getId()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		int retrievedUserid = rs.getInt("userid");
 		String courseId = rs.getString("course_id");
 		
 		assertEquals(user.getUserid(), retrievedUserid);
 		assertEquals(course.getId(), courseId);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testAddCourseEnrollmentWithNullConn() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.insertCourseEnrollment(null, user, course);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testAddCourseEnrollmentWithNullUserMeta() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.insertCourseEnrollment(this.conn, null, course);
 	}
 	
 	@Test
 	public void testDeleteCourseEnrollment_WhenEnrolled() throws Exception {
 		//first insert a course enrollment
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.insertCourseEnrollment(this.conn, user, course);
 		
 		//call api to delete the course enrollment
 		this.dataFacade.deleteCourseEnrollment(this.conn, user, course);
 		
 		//retrieve and verify
 		
 		String sql = 
 			String.format(Sql.RETREIVE_COURSE_ENROLLMENTS_BY_USER_AND_COURSE, 
 						  user.getUserid(),
 						  DataInitializer.wrapForSQL(course.getId()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		//We should not have any records in the ResultSet
 		assertEquals(false, rs.next());
 	}
 	
 	@Test
 	public void testDeleteCourseEnrollment_WhenNotEnrolled() throws Exception {
 		//call api to delete the course enrollment
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.deleteCourseEnrollment(this.conn, user, course);
 		//we do not expect anything to happen, just verifying that an
 		//Exception is not thrown
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testDeleteCourseEnrollmentWithNullConn() throws Exception {
 		//call api to delete the course enrollment
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.deleteCourseEnrollment(null, user, course);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testDeleteCourseEnrollmentWithNullUserMeta() throws Exception {
 		//call api to delete the course enrollment
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.deleteCourseEnrollment(this.conn, null, course);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testDeleteCourseEnrollmentWithNullCourse() throws Exception {
 		//call api to delete the course enrollment
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.deleteCourseEnrollment(this.conn, user, null);
 	}
 	
 	@Test
 	public void testRetreiveCourseEnrollmentsForUser_WhenEnrolled() throws Exception {
 		UserMeta user = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("mevans").userMeta;
 		Course coursePhysics = 
 			TestObjectsRepository.getInstance().getCourse("Physics");
 		Course courseOrganicChem = 
 			TestObjectsRepository.getInstance().getCourse("OrganicChem");
 		
 		//first create registration for the user in a few courses
 		this.dataFacade.insertCourseEnrollment(conn, user, coursePhysics);
 		this.dataFacade.insertCourseEnrollment(conn, user, courseOrganicChem);
 		
 		//verify if the user was indeed registered
 		List<String> courseEnrollments = 
 			this.dataFacade.retreiveCourseEnrollmentsForUser(this.conn, user);
 		assertNotNull(courseEnrollments);
 		assertEquals(2, courseEnrollments.size());
 		assertEquals("OrganicChem", courseEnrollments.get(0));
 		assertEquals("Physics", courseEnrollments.get(1));
 	}
 	
 	@Test
 	public void testRetreiveCourseEnrollmentsForUser_WhenNotEnrolled() throws Exception {
 		UserMeta user = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("mevans").userMeta;
 
 		//verify that the user is not enrolled in any course
 		List<String> courseEnrollments = 
 			this.dataFacade.retreiveCourseEnrollmentsForUser(this.conn, user);
 		assertNotNull(courseEnrollments);
 		assertEquals(0, courseEnrollments.size());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCourseEnrollmentsForUserWithNullConn() throws Exception {
 		UserMeta user = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("mevans").userMeta;
 
 		List<String> courseEnrollments = 
 			this.dataFacade.retreiveCourseEnrollmentsForUser(null, user);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCourseEnrollmentsForUserWithNullUserMeta() throws Exception {
 		UserMeta user = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("mevans").userMeta;
 
 		List<String> courseEnrollments = 
 			this.dataFacade.retreiveCourseEnrollmentsForUser(null, user);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testAddCourseEnrollmentWithNullCourse() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.insertCourseEnrollment(this.conn, user, null);
 	}
 	
 	@Test
 	public void testCheckEnrollmentByUserMetaAndCourse_WhenNotEnrolled() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		boolean enrolled = this.dataFacade.checkEnrollmentByUserMetaAndCourse(this.conn, user, course);
 		assertEquals(false, enrolled);
 	}
 	
 	@Test
 	public void testCheckEnrollmentByUserMetaAndCourse_WhenEnrolled() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.insertCourseEnrollment(this.conn, user, course);
 		
 		boolean enrolled = this.dataFacade.checkEnrollmentByUserMetaAndCourse(this.conn, user, course);
 		assertEquals(true, enrolled);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testCheckEnrollmentByUserMetaAndCourseWithNullConn() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		boolean enrolled = this.dataFacade.checkEnrollmentByUserMetaAndCourse(null, user, course);
 		assertEquals(false, enrolled);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testCheckEnrollmentByUserMetaAndCourseWithNullUserMeta() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		boolean enrolled = this.dataFacade.checkEnrollmentByUserMetaAndCourse(this.conn, null, course);
 		assertEquals(false, enrolled);
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testCheckEnrollmentByUserMetaAndCourseWithNullCourse() throws Exception {
 		UserMeta user = TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		boolean enrolled = this.dataFacade.checkEnrollmentByUserMetaAndCourse(this.conn, user, null);
 		assertEquals(false, enrolled);
 	}
 	
 	@Test
 	public void testAddCourseEnrollmentStatus() throws Exception {
 		
 		//create test objects
 		UserMeta userMeta = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		
 		Timestamp timestamp = new Timestamp((new Date()).getTime());
 		
 		//add the course enrollment status
 		CourseEnrollmentStatus status = 
 			new CourseEnrollmentStatus(course.getId(), 
 									   userMeta.getUserid(), 
 									   UserCourseStatus.ENROLLED, 
 									   timestamp);
 		this.dataFacade.addCourseEnrollmentAction(this.conn, status);
 		
 		//retrieve for verifying
 		String sql = String.format(Sql.RETREIVE_COURSE_ENROLLMENT_STATUS, 
 								   DataInitializer.wrapForSQL(course.getId()), 
 								   userMeta.getUserid());
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		CourseEnrollmentStatus retreivedCourseEnrollmentStatus = null;
 		if(rs.next()) {
 			String courseId = rs.getString("course_id");
 			int userId = rs.getInt("userid");
 			int courseEnrollmentActionId = rs.getInt("course_enrollment_action_id");
 			Timestamp tstamp = rs.getTimestamp("tstamp");
 			retreivedCourseEnrollmentStatus = 
 				new CourseEnrollmentStatus(courseId, 
 										   userId, 
 										   UserCourseStatus.getUserCourseStatus(courseEnrollmentActionId), 
 										   tstamp);
 		}
 		assertNotNull(retreivedCourseEnrollmentStatus);
 		assertEquals(status.getCourseId(), 
 					 retreivedCourseEnrollmentStatus.getCourseId());
 		assertEquals(status.getUserid(), 
 					 retreivedCourseEnrollmentStatus.getUserid());
 		assertEquals(status.getUserCourseStatus(), 
 					 retreivedCourseEnrollmentStatus.getUserCourseStatus());
 		assertEquals(status.getTimestamp().getTime(), 
 					 retreivedCourseEnrollmentStatus.getTimestamp().getTime(), 
 					 10);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testAddCourseEnrollmentStatusWithNullConn() throws Exception {
 		//create test objects
 		UserMeta userMeta = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		
 		Timestamp timestamp = new Timestamp((new Date()).getTime());
 		
 		//add the course enrollment status
 		CourseEnrollmentStatus status = 
 			new CourseEnrollmentStatus(course.getId(), 
 									   userMeta.getUserid(), 
 									   UserCourseStatus.ENROLLED, 
 									   timestamp);
 		this.dataFacade.addCourseEnrollmentAction(null, status);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testAddCourseEnrollmentStatusWithNullStatus() throws Exception {
 		this.dataFacade.addCourseEnrollmentAction(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveCompetenciesWiki() throws Exception {
 		String physicsCompetenciesWiki = this.dataFacade.retreiveCompetenciesWiki(this.conn, "Physics");
 		StringBuffer expectedPhysicsCompetenciesWiki = new StringBuffer();
 		expectedPhysicsCompetenciesWiki.append("Physics lecture 1\n");
 		expectedPhysicsCompetenciesWiki.append("Physics lecture 2\n");
 		expectedPhysicsCompetenciesWiki.append("Physics lecture 3\n");
 		assertEquals(expectedPhysicsCompetenciesWiki.toString(), physicsCompetenciesWiki);
 		
 		String chemCompetenciesWiki = this.dataFacade.retreiveCompetenciesWiki(this.conn, "OrganicChem");
 		String expectedChemCompetenciesWiki = "Introduction and Drawing Lewis Structures\n";
 		assertEquals(expectedChemCompetenciesWiki, chemCompetenciesWiki);
 		
 		//Here we test the comnpetency wiki for a course where the competency 
 		//wiki does not exist
 		String bioCompetenciesWiki = this.dataFacade.retreiveCompetenciesWiki(this.conn, "Bio");
 		String expectedBioCompetenciesWiki = "";
 		assertEquals(expectedBioCompetenciesWiki, bioCompetenciesWiki);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCompetenciesWikiWithNullConn() throws Exception {
 		this.dataFacade.retreiveCompetenciesWiki(null, "Physics");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCompetenciesWikiWithNullCourseId() throws Exception {
 		this.dataFacade.retreiveCompetenciesWiki(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveCompetenciesWikiWithNonexistentCourseId() throws Exception {
 		String competencyWiki = this.dataFacade.retreiveCompetenciesWiki(this.conn, "abc");
 		assertEquals("", competencyWiki);
 	}
 	
 	@Test
 	public void testRetreiveCompetency() throws Exception {
 		//Physics competencies
 		Course expectedPhysicsCourse = TestObjectsRepository.getInstance().getCourse("Physics");
 		Competency expectedPhysicsCompetency0 = expectedPhysicsCourse.getCompetency(0);
 		Competency expectedPhysicsCompetency1 = expectedPhysicsCourse.getCompetency(1);
 		Competency expectedPhysicsCompetency2 = expectedPhysicsCourse.getCompetency(2);
 		
 		Competency physicsCompetency0 = this.dataFacade.retreiveCompetency(conn, "Physics", "0");
 		assertEquals(expectedPhysicsCompetency0, physicsCompetency0);
 		
 		Competency physicsCompetency1 = this.dataFacade.retreiveCompetency(conn, "Physics", "1");
 		assertEquals(expectedPhysicsCompetency1, physicsCompetency1);
 		
 		Competency physicsCompetency2 = this.dataFacade.retreiveCompetency(conn, "Physics", "2");
 		assertEquals(expectedPhysicsCompetency2, physicsCompetency2);
 		
 		//Chem competencies
 		Course expectedChemCourse = TestObjectsRepository.getInstance().getCourse("OrganicChem");
 		Competency expectedChemCompetency0 = expectedChemCourse.getCompetency(3);
 		
 		Competency chemCompetency0 = this.dataFacade.retreiveCompetency(conn, "OrganicChem", "3");
 		assertEquals(expectedChemCompetency0, chemCompetency0);
 		
 		//Bio101 competencies
 		Competency bioCompetency0 = this.dataFacade.retreiveCompetency(conn, "Bio101", "0");
 		assertEquals(true, (null == bioCompetency0));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCompetencyWithNullConn() throws Exception {
 		this.dataFacade.retreiveCompetency(null, "Physics", "0");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCompetencyWithNullCourseId() throws Exception {
 		this.dataFacade.retreiveCompetency(this.conn, null, "0");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveCompetencyWithNullsCompetencyId() throws Exception {
 		this.dataFacade.retreiveCompetency(this.conn, "Physics", null);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testRetreiveCompetencyWithIncorrectsCompetencyId() throws Exception {
 		this.dataFacade.retreiveCompetency(this.conn, "Physics", "a");
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testRetreiveCompetencyWithIncorrectsCompetencyId1() throws Exception {
 		this.dataFacade.retreiveCompetency(this.conn, "Physics", "-1");
 	}
 	
 	@Test
 	public void testUpdateCompetenciesWikiContents() throws Exception {
 		//update competencies wiki
 		String courseId = "Physics";
 		String content = "Physics lecture 1 -> Lecture 1\nPhysics lecture 2\nPhysics lecture 3";
 		//a newline is being added at the end by the method that parses
 		//competencies wiki contents for title update requests
 		//TODO: Should we do anything about it???
 		String expectedContent = "Lecture 1\nPhysics lecture 2\nPhysics lecture 3\n";
 		this.dataFacade.updateCompetenciesWikiContents(this.conn, courseId, content);
 		
 		//retreive competencies wiki
 		String sql = String.format(Sql.RETREIVE_COMPETENCIES_WIKI, DataInitializer.wrapForSQL(courseId));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		String retreivedContent = null;
 		String retreivedCourseId =  null;
 		if(rs.next()) {
 			retreivedCourseId = rs.getString("course_id");
 			retreivedContent = rs.getString("contents");
 		}
 		assertNotNull(retreivedCourseId);
 		assertNotNull(retreivedContent);
 		assertEquals(courseId, retreivedCourseId);
 		assertEquals(expectedContent, retreivedContent);
 		
 		//verify that the competency title was changed
 		sql = String.format("SELECT * from COMPETENCY WHERE course_id=%s AND title=%s;", 
 							DataInitializer.wrapForSQL(courseId),
 							DataInitializer.wrapForSQL("Lecture 1"));
 		stmt = this.conn.createStatement();
 		rs = stmt.executeQuery(sql);
 		boolean competencyTitleChanged = false;
 		if(rs.next()) {
 			//System.out.println(rs.getString("title"));
 			competencyTitleChanged = true;
 		}
 		assertTrue(competencyTitleChanged);
 	}
 
 	@Test
 	public void testUpdateCompetenciesWikiContentsWithNullContent() throws Exception {
 		//update competencies wiki
 		String courseId = "Physics";
 		String content = null;
 		this.dataFacade.updateCompetenciesWikiContents(this.conn, courseId, content);
 		
 		//retreive competencies wiki
 		String sql = String.format(Sql.RETREIVE_COMPETENCIES_WIKI, DataInitializer.wrapForSQL(courseId));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		String retreivedContent = null;
 		String retreivedCourseId =  null;
 		if(rs.next()) {
 			retreivedCourseId = rs.getString("course_id");
 			retreivedContent = rs.getString("contents");
 		}
 		assertNotNull(retreivedCourseId);
 		assertNotNull(retreivedContent);
 		assertEquals(courseId, retreivedCourseId);
 		assertEquals("", retreivedContent);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCompetenciesWikiContentsWithNullConn() throws Exception {
 		//update competencies wiki
 		String courseId = "Physics";
 		String content = "Lecture 1\n Lecture 2\n Lecture 3";
 		this.dataFacade.updateCompetenciesWikiContents(null, courseId, content);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCompetenciesWikiContentsWithNullCourseId() throws Exception {
 		//update competencies wiki
 		String courseId = null;
 		String content = "Lecture 1\n Lecture 2\n Lecture 3";
 		this.dataFacade.updateCompetenciesWikiContents(this.conn, courseId, content);
 	}
 	
 	@Test
 	public void testInsertCompetency() throws Exception {
 		//get the test objects
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		
 		//insert competency and get the returned competency object
 		Competency competency = 
 			this.dataFacade.insertCompetency(this.conn, course, "Lecture 1");
 		
 		//verify the returned competency object
 		assertNotNull(competency);
 		assertEquals(true, competency.getId() >= 0);
 		
 		//retreive the competency object from the database
 		String sql = String.format(Sql.RETREIVE_COMPETENCIES_FOR_COURSE, 
 								   DataInitializer.wrapForSQL(course.getId()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		int rowCnt = 0;
 		boolean foundRecord = false;
 		while(rs.next()) {
 			if(competency.getId() == rs.getInt("id")) {
 				foundRecord = true;
 			}
 			rowCnt++;
 		}
 		assertEquals(4, rowCnt);
 		assertEquals(true, foundRecord);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertCompetencyWithNullConn() throws Exception {
 		//get the test objects
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		
 		//insert competency and get the returned competency object
 		Competency competency = 
 			this.dataFacade.insertCompetency(null, course, "Lecture 1");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertCompetencyWithNullCourse() throws Exception {
 		//insert competency and get the returned competency object
 		Competency competency = 
 			this.dataFacade.insertCompetency(this.conn, null, "Lecture 1");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertCompetencyWithNullCompetencyTitle() throws Exception {
 		//get the test objects
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		
 		//insert competency and get the returned competency object
 		Competency competency = 
 			this.dataFacade.insertCompetency(this.conn, course, null);
 	}
 	
 	@Test
 	public void testUpdateCompetency() throws Exception {
 		
 		//get and update test object
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		Competency competency = course.getCompetency(0);
 		competency.setDescription("updated desc");
 		
 		//update the competency
 		this.dataFacade.updateCompetency(this.conn, course.getId(), competency);
 		
 		//retreive updated competency and verify
 		String sql = String.format(Sql.RETREIVE_COMPETENCIES_FOR_COURSE, 
 								   DataInitializer.wrapForSQL(course.getId()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		boolean foundUpdatedRecord = false;
 		while(rs.next()) {
 			if(competency.getId() == rs.getInt("id")) {
 				if(competency.getDescription().equals(rs.getString("description"))) {
 					foundUpdatedRecord = true;
 				}
 			}
 		}
 		assertEquals(true, foundUpdatedRecord);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCompetencyWithNullConn() throws Exception {
 		
 		//get and update test object
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		Competency competency = course.getCompetency(0);
 		competency.setDescription("updated desc");
 		
 		//update the competency
 		this.dataFacade.updateCompetency(null, course.getId(), competency);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCompetencyWithNullCourseId() throws Exception {
 		
 		//get and update test object
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		Competency competency = course.getCompetency(0);
 		competency.setDescription("updated desc");
 		
 		//update the competency
 		this.dataFacade.updateCompetency(this.conn, null, competency);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCompetencyWithNullCompetency() throws Exception {
 		
 		//get and update test object
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		Competency competency = course.getCompetency(0);
 		competency.setDescription("updated desc");
 		
 		//update the competency
 		this.dataFacade.updateCompetency(this.conn, course.getId(), null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testDeleteCompetency() throws Exception {
 		//use a competency when the method is implemented
 		this.dataFacade.deleteCompetency(conn, null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testDeleteMentor() throws Exception {
 		//use a competency when the method is implemented
 		this.dataFacade.deleteMentor(conn, null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testInsertMentor() throws Exception {
 		//use a competency when the method is implemented
 		this.dataFacade.insertMentor(this.conn, null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testRetreiveMentorsForCompetency() throws Exception {
 		//use a competency when the method is implemented
 		this.dataFacade.retreiveMentorsForCompetency(conn);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testRetreiveAllCompetencies() throws Exception {
 		//use a competency when the method is implemented
 		this.dataFacade.retreiveAllCompetencies(conn);
 	}
 	
 	@Test
 	public void testRetrievAllMentors() throws Exception {
 		List<Mentor> mentors = this.dataFacade.retreiveAllMentors(this.conn);
 		assertNotNull(mentors);
 		assertEquals(1, mentors.size());
 		Mentor mentor = mentors.get(0);
 		assertEquals(2, mentor.getUserid());
 		assertEquals(Role.MENTOR, mentor.getRole());
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testRetreiveCompetenciesForCourse() throws Exception {
 		//use a course when the method is implemented
 		this.dataFacade.retreiveCompetenciesForCourse(this.conn, null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testRetreiveMentorsForCourse() throws Exception {
 		//use a course when the method is implemented
 		this.dataFacade.retreiveMentorsForCourse(this.conn);
 	}
 	
 	@Test
 	public void testUpdateCourse() throws Exception {
 		//add new mentor
 		Mentor newMentor = new Mentor();
 		newMentor.setIdentifier("MentorDude");
 		newMentor.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		newMentor.setRole(Role.MENTOR);
 		String sql = String.format(Sql.INSERT_USER_META, 
 								   DataInitializer.wrapForSQL(newMentor.getIdentifier()),
 								   DataInitializer.wrapForSQL(newMentor.getLoginVia().toString()),
 								   DataInitializer.wrapForSQL(newMentor.getRole().toString()));
 		Statement stmt = this.conn.createStatement();
 		stmt.executeUpdate(sql);
 		
 		sql = String.format(Sql.RETREIVE_USER_META_BY_IDETIFIER_LOGIN_VIA,
 							DataInitializer.wrapForSQL(newMentor.getIdentifier()),
 							DataInitializer.wrapForSQL(newMentor.getLoginVia().toString()));
 		stmt = this.conn.createStatement();
 		ResultSet newMentorRS = stmt.executeQuery(sql);
 		newMentorRS.next();
 		newMentor.setUserid(newMentorRS.getInt("userid"));
 		
 		//update the course
 		String newDescription = "new Description of the course";
 		String newTitle = "New Title for the course";
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		course.setDescription(newDescription);
 		course.setTitle(newTitle);
 		course.setMentor(newMentor);
 		this.dataFacade.updateCourse(this.conn, course);
 		
 		//verify if the course was updated
 		sql = String.format(Sql.RETREIVE_COURSE, 
 							DataInitializer.wrapForSQL(course.getId()));
 		stmt = this.conn.createStatement();
 		ResultSet updatedCourseRS = stmt.executeQuery(sql);
 		updatedCourseRS.next();
 		assertEquals(course.getId(), updatedCourseRS.getString("id"));
 		assertEquals(newDescription, updatedCourseRS.getString("description"));
 		assertEquals(newTitle, updatedCourseRS.getString("title"));
 	
 		//verify if the course mentor was updated
 		sql = String.format(Sql.RETREIVE_MENTORS_FOR_COURSE, 
 							DataInitializer.wrapForSQL(course.getId()));
 		stmt = this.conn.createStatement();
 		ResultSet mentorForCourseRS = stmt.executeQuery(sql);
 		mentorForCourseRS.next();
 		int mentorForCourseUserid = mentorForCourseRS.getInt("mentor_userid");
 		assertEquals(newMentor.getUserid(), mentorForCourseUserid);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCourseWithNullConn() throws Exception {
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		this.dataFacade.updateCourse(null, course);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCourseWithNullCourse() throws Exception {
 		this.dataFacade.updateCourse(this.conn, null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testUpdateMentor() throws Exception {
 		//we will use a real mentor when the method is implemented
 		this.dataFacade.updateMentor(this.conn, null);
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void testUpsertCompetency() throws Exception {
 		//we will use a real competency when the method is implemented
 		this.dataFacade.upsertCompetency(this.conn, null);
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void testUpsertCourse() throws Exception {
 		//we will use a real course when the method is implemented
 		this.dataFacade.upsertCourse(this.conn, null);
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void testUpsertMentor() throws Exception {
 		//we will use a real mentor when the method is implemented
 		this.dataFacade.upsertMentor(this.conn, null);
 	}
 	
 	@Test
 	public void testGetStatusUpdates() throws Exception {
 		//add a status update
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		UserMeta student = this.dataFacade.retreiveUserMetaByIdentifierLoginVia(conn, "mevans", UserMeta.LoginVia.INTERNAL);
 		Timestamp timestamp = new Timestamp(new Date().getTime());
 		String sql = String.format(Sql.INSERT_COURSE_ENROLLMENT_STATUS, 
 								   DataInitializer.wrapForSQL(course.getId()),
 								   student.getUserid(),
 								   UserCourseStatus.ENROLLED.getId(),
 								   DataInitializer.wrapForSQL(timestamp.toString()));
 		Statement stmt = this.conn.createStatement();
 		stmt.executeUpdate(sql);
 		
 		//call the API and verify
 		List<StatusUpdate> statusUpdates = 
 									this.dataFacade.getStatusUpdates(this.conn);
 		assertNotNull(statusUpdates);
 		assertEquals(1, statusUpdates.size());
 
 		StatusUpdate statusUpdate = statusUpdates.get(0);
 		assertEquals(timestamp, statusUpdate.getTimestamp());
 		assertEquals(student, statusUpdate.getUserMeta());
 		assertEquals("ENROLLED in ", statusUpdate.getEnrollmentStatus());
 		assertEquals(course.getId(), statusUpdate.getCourseId());
 	}
 	
 	@Test
 	public void testGetStatusUpdatesWhenNoneExist() throws Exception {
 		List<StatusUpdate> statusUpdates = 
 									this.dataFacade.getStatusUpdates(this.conn);
 		assertNotNull(statusUpdates);
 		assertEquals(0, statusUpdates.size());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testStatusUpdatesWithNullConn() throws Exception {
 		List<StatusUpdate> statusUpdates = 
 			this.dataFacade.getStatusUpdates(null);
 	}
 	
 	@Test
 	public void testInsertUser() throws Exception {
 		String username = "cooluser";
 		String email = "user@user.com";
 		String password = "coolpassword";
 		User user = new User();
 		user.setUsername(username);
 		user.setEmail(email);		
 		user.setPassword(password);
 		UserMeta userMeta = new UserMeta();
 		userMeta.setIdentifier(username);
 		userMeta.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		userMeta.setRole(Role.STUDENT);
 		
 		this.dataFacade.insertUser(this.conn, user, userMeta);
 		
 		String sql = String.format(Sql.RETREIVE_USER_BY_USERNAME,
 								   DataInitializer.wrapForSQL(username));
 		Statement stmt = this.conn.createStatement();
 		stmt.execute(sql);
 		ResultSet userRS = stmt.getResultSet();
 		userRS.next();
 		assertEquals(username, userRS.getString("username"));
 		assertEquals(email, userRS.getString("email"));
 		String savedPassword = userRS.getString("password");
 		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
 		passwordEncryptor.checkPassword(password, savedPassword);
 		
 		sql = String.format(Sql.RETREIVE_USER_META_BY_IDETIFIER_LOGIN_VIA,
 							DataInitializer.wrapForSQL(username),
 							DataInitializer.wrapForSQL(UserMeta.LoginVia.INTERNAL.toString()));
 		stmt = this.conn.createStatement();
 		ResultSet userMetaRS = stmt.executeQuery(sql);
 		userMetaRS.next();
 		assertEquals(username, userMetaRS.getString("identifier"));
 		assertEquals(UserMeta.LoginVia.INTERNAL.toString(), userMetaRS.getString("login_via"));
 		assertEquals(Role.STUDENT.toString(), userMetaRS.getString("role"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertUserWithNullConn() throws Exception {
 		String username = "cooluser";
 		String email = "user@user.com";
 		String password = "coolpassword";
 		User user = new User();
 		user.setUsername(username);
 		user.setEmail(email);
 		user.setPassword(password);
 		UserMeta userMeta = new UserMeta();
 		userMeta.setIdentifier(username);
 		userMeta.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		userMeta.setRole(Role.STUDENT);
 		
 		this.dataFacade.insertUser(null, user, userMeta);
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testInsertUserWithNullUser() throws Exception {
 		String username = "cooluser";
 		String email = "user@user.com";
 		String password = "coolpassword";
 		User user = new User();
 		user.setUsername(username);
 		user.setEmail(email);
 		user.setPassword(password);
 		UserMeta userMeta = new UserMeta();
 		userMeta.setIdentifier(username);
 		userMeta.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		userMeta.setRole(Role.STUDENT);
 		
 		this.dataFacade.insertUser(this.conn, null, userMeta);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertUserWithNullUserMeta() throws Exception {
 		String username = "cooluser";
 		String email = "user@user.com";
 		String password = "coolpassword";
 		User user = new User();
 		user.setUsername(username);
 		user.setEmail(email);
 		user.setPassword(password);
 		UserMeta userMeta = new UserMeta();
 		userMeta.setIdentifier(username);
 		userMeta.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		userMeta.setRole(Role.STUDENT);
 		
 		this.dataFacade.insertUser(this.conn, user, null);
 	}
 	
 	@Test
 	public void testUpdateUser_Email() throws Exception {
 		String username = "mevans";
 		User user = 
 			TestObjectsRepository.getInstance().getUserUserMeta(username).user;
 		user.setEmail("newemail@newservice.com");
 		this.dataFacade.updateUser(this.conn, user, UserForm.Field.EMAIL);
 		
 		String sql = String.format(Sql.RETREIVE_USER_BY_USERNAME, 
 								   DataInitializer.wrapForSQL(username));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		String updatedUsername = rs.getString("username");
 		assertEquals(username, updatedUsername);
 	}
 	
 	@Test
 	public void testUpdateUser_Password() throws Exception {
 		String username = "mevans";
 		String newPassword = "newpassword";
 		User user = 
 			TestObjectsRepository.getInstance().getUserUserMeta(username).user;
 		user.setPassword(newPassword);
 		this.dataFacade.updateUser(this.conn, user, UserForm.Field.PASSWORD);
 		
 		String sql = String.format(Sql.RETREIVE_USER_BY_USERNAME,
 								   DataInitializer.wrapForSQL(username));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		String updatedPassword = rs.getString("password");
 		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
 		assertEquals(true, passwordEncryptor.checkPassword(newPassword, updatedPassword));
 	}
 	
 	@Test
 	public void testUpdateUser_EmailAndPassword() throws Exception {
 		String username = "mevans";
 		String newPassword = "newpassword";
 		
 		User user = 
 			TestObjectsRepository.getInstance().getUserUserMeta(username).user;
 		user.setEmail("newemail@newservice.com");
 		user.setPassword(newPassword);
 		
 		this.dataFacade.updateUser(this.conn, user, 
 								   UserForm.Field.EMAIL, 
 								   UserForm.Field.PASSWORD);
 		
 		String sql = String.format(Sql.RETREIVE_USER_BY_USERNAME, 
 								   DataInitializer.wrapForSQL(username));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		String updatedUsername = rs.getString("username");
 		String updatedPassword = rs.getString("password");
 		assertEquals(username, updatedUsername);
 		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
 		assertEquals(true, passwordEncryptor.checkPassword(newPassword, updatedPassword));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testUpdateUser_Username() throws Exception {
 		String username = "mevans";
 		
 		User user = 
 			TestObjectsRepository.getInstance().getUserUserMeta(username).user;
 		
 		user.setUsername("newUsername");
 		user.setEmail("newemail@newservice.com");
 		
 		this.dataFacade.updateUser(this.conn, user, 
 								   UserForm.Field.EMAIL, 
 								   UserForm.Field.USERNAME);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateUserWithNullConn() throws Exception {
 		String username = "mevans";
 		
 		User user = 
 			TestObjectsRepository.getInstance().getUserUserMeta(username).user;
 		
 		User updatedUser = new User(username, user.getEmail());
 		updatedUser.setPassword(user.getPassword());
 		
 		updatedUser.setUsername("newUsername");
 		updatedUser.setEmail("newemail@newservice.com");
 		
 		this.dataFacade.updateUser(null, updatedUser, 
 								   UserForm.Field.EMAIL, 
 								   UserForm.Field.USERNAME);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateUserWithNullUser() throws Exception {
 		this.dataFacade.updateUser(this.conn, null, 
 								   UserForm.Field.EMAIL, 
 								   UserForm.Field.USERNAME);
 	}
 	
 	@Test
 	public void testRetreiveAllUsers() throws DataException {
 		User adminDude = 
 			TestObjectsRepository.getInstance().getUserUserMeta("admindude").user;
 		User mevans = 
 			TestObjectsRepository.getInstance().getUserUserMeta("mevans").user;
 		User dvidakovich = 
 			TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").user;
 		
 		List<User> users = this.dataFacade.retreiveAllUsers(this.conn);
 		assertNotNull(users);
 		assertEquals(3, users.size());
 		
 		boolean mevansFound = false;
 		boolean adminDudeFound = false;
 		boolean dvidakovichFound = false;
 		
 		for(User user : users) {
 			if(user.getUsername().equals("admindude")) {
 				assertEquals(adminDude, user);
 				adminDudeFound = true;
 			} else if(user.getUsername().equals("mevans")) {
 				assertEquals(mevans, user);
 				mevansFound = true;
 			} else if(user.getUsername().equals("dvidakovich")) {
 				assertEquals(dvidakovich, user);
 				dvidakovichFound = true;
 			}
 		}
 		assertEquals(true, adminDudeFound && mevansFound && dvidakovichFound);
 	}
 	
 	@Test
 	public void retreiveAllUsersWhenThereAreNone() {
 		//TODO: Implement
 		System.out.println("Need to implement this test - retreiveAllUsersWhenThereAreNone");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveAllUsersWithNullConn() throws Exception {
 		List<User> users = this.dataFacade.retreiveAllUsers(null);
 	}	
 	
 	@Test
 	public void testRetreivePassword() throws Exception {
 		String password = this.dataFacade.retreivePassword(this.conn, "mevans");
 		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
 		assertEquals(true, passwordEncryptor.checkPassword("mevans", password));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreivePasswordWithNullConn() throws Exception {
 		String password = this.dataFacade.retreivePassword(null, "mevans");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreivePasswordWithNullUsername() throws Exception {
 		String password = this.dataFacade.retreivePassword(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveUserByUsername() throws Exception {
 		String username = "dvidakovich";
 		User user = this.dataFacade.retreiveUserByUsername(this.conn, username);
 		User expectedUser = TestObjectsRepository.getInstance().getUserUserMeta(username).user;
 		assertEquals(expectedUser, user);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveUserByUsernameWithNullConn() throws Exception {
 		String username = "dvidakovich";
 		User user = this.dataFacade.retreiveUserByUsername(null, username);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveUserByUsernameWithNullUsername() throws Exception {
 		User user = this.dataFacade.retreiveUserByUsername(this.conn, null);
 	}
 	
 	@Test
 	public void testInsertUserMeta() throws Exception {
 		UserMeta userMeta = new UserMeta();
 		userMeta.setIdentifier("newuser");
 		userMeta.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		userMeta.setRole(Role.STUDENT);
 
 		this.dataFacade.insertUserMeta(this.conn, userMeta);
 		
 		String sql = String.format(Sql.RETREIVE_USER_META_BY_IDETIFIER_LOGIN_VIA, 
 								   DataInitializer.wrapForSQL(userMeta.getIdentifier()),
 								   DataInitializer.wrapForSQL(userMeta.getLoginVia().toString()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		assertEquals(userMeta.getIdentifier(), rs.getString("identifier"));
 		assertEquals(userMeta.getLoginVia().toString(), rs.getString("login_via"));
 		assertEquals(userMeta.getRole().toString(), rs.getString("role"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertUserMetaWithNullConn() throws Exception {
 		UserMeta userMeta = new UserMeta();
 		userMeta.setIdentifier("newuser");
 		userMeta.setLoginVia(UserMeta.LoginVia.INTERNAL);
 		userMeta.setRole(Role.STUDENT);
 
 		this.dataFacade.insertUserMeta(null, userMeta);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertUserMetaWithNullUserMeta() throws Exception {
 		this.dataFacade.insertUserMeta(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveUserMeta() throws Exception {
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(Sql.RETREIVE_ALL_USER_META);
 		
 		int userid = -1;
 		String identifier = "";
 		String role = null;
 		String loginVia = null;
 		
 		while(rs.next()) {
 			userid = rs.getInt("userid");
 			identifier = rs.getString("identifier");
 			role = rs.getString("role");
 			loginVia = rs.getString("login_via");
 			break;
 		}
 		
 		UserMeta userMeta = this.dataFacade.retreiveUserMeta(this.conn, userid);
 		assertEquals(userid, userMeta.getUserid());
 		assertEquals(identifier, userMeta.getIdentifier());
 		assertEquals(role, userMeta.getRole().toString());
 		assertEquals(loginVia, userMeta.getLoginVia().toString());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveUserMetaWithNullConn() throws Exception {		
 		UserMeta userMeta = this.dataFacade.retreiveUserMeta(null, 0);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testRetreiveUserMetaWithIncorrectUserid() throws Exception {		
 		UserMeta userMeta = this.dataFacade.retreiveUserMeta(this.conn, -1);
 	}
 	
 	@Test
 	public void testRetreiveUserMetaByIdentifierLoginVia() throws Exception {
 		String identifier = "mevans";
 		UserMeta.LoginVia loginVia = UserMeta.LoginVia.INTERNAL;
 		Role role = Role.MENTOR;
 		
 		UserMeta retreivedUserMeta =
 			this.dataFacade.retreiveUserMetaByIdentifierLoginVia(conn, 
 																 identifier, 
 																 loginVia);
 		assertEquals(identifier, retreivedUserMeta.getIdentifier());
 		assertEquals(loginVia, retreivedUserMeta.getLoginVia());
 		assertEquals(role, retreivedUserMeta.getRole());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveUserMetaByIdentifierLoginViaForNullConn() throws Exception {
 		String identifier = "mevans";
 		UserMeta.LoginVia loginVia = UserMeta.LoginVia.INTERNAL;
 		
 		UserMeta retreivedUserMeta =
 			this.dataFacade.retreiveUserMetaByIdentifierLoginVia(null, 
 																 identifier, 
 																 loginVia);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveUserMetaByIdentifierLoginViaForNullIdentifier() throws Exception {
 		UserMeta.LoginVia loginVia = UserMeta.LoginVia.INTERNAL;
 		
 		UserMeta retreivedUserMeta =
 			this.dataFacade.retreiveUserMetaByIdentifierLoginVia(this.conn, 
 																 null, 
 																 loginVia);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveUserMetaByIdentifierLoginViaForNullLoginVia() throws Exception {
 		String identifier = "mevans";
 		
 		UserMeta retreivedUserMeta =
 			this.dataFacade.retreiveUserMetaByIdentifierLoginVia(this.conn, 
 																 identifier, 
 																 null);
 	}
 	
 	@Test
 	public void testRetreiveAllUserMeta() throws Exception {
 		List<UserMeta> allUserMeta = this.dataFacade.retreiveAllUserMeta(this.conn);
 		assertNotNull(allUserMeta);
 		assertEquals(3, allUserMeta.size());
 		
 		Map<String, UserMeta> expectedUserMetaMap = new HashMap<String, UserMeta>();
 		expectedUserMetaMap.put("mevans", TestObjectsRepository.getInstance().getUserUserMeta("mevans").userMeta);
 		expectedUserMetaMap.put("admindude", TestObjectsRepository.getInstance().getUserUserMeta("admindude").userMeta);
 		expectedUserMetaMap.put("dvidakovich", TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta);
 
 		for (UserMeta userMeta : allUserMeta) {
 			String identifier = userMeta.getIdentifier();
 			UserMeta expectedUserMeta = expectedUserMetaMap.get(identifier);
 			assertEquals(expectedUserMeta.getIdentifier(), userMeta.getIdentifier());
 			assertEquals(expectedUserMeta.getLoginVia(), userMeta.getLoginVia());
 			assertEquals(expectedUserMeta.getRole(), userMeta.getRole());
 		}
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveAllUserMetaForNullConn() throws Exception {
 		List<UserMeta> allUserMeta = this.dataFacade.retreiveAllUserMeta(null);
 	}
 	
 	@Test
 	public void testUpdateUserMetaRole() throws Exception {
 		//get a UserMeta object
 		UserMeta userMeta = 
 			this.dataFacade.retreiveUserMetaByIdentifierLoginVia(this.conn, 
 																 "mevans", 				
 																 UserMeta.LoginVia.INTERNAL);
 		//update the Role
 		userMeta.setRole(Role.ADMIN);
 		
 		//call the API under test to update the Role of the UserMeta
 		this.dataFacade.updateUserMetaRole(this.conn, userMeta);
 		
 		//verify the UserMeta... this time let's get the actual data row
 		String sql = String.format(Sql.RETREIVE_USER_META_BY_IDETIFIER_LOGIN_VIA, 
 								   DataInitializer.wrapForSQL("mevans"), 
 								   DataInitializer.wrapForSQL(UserMeta.LoginVia.INTERNAL.toString()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		assertEquals("mevans", rs.getString("identifier"));
 		assertEquals(Role.ADMIN.toString(), rs.getString("role"));		
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testUpdateUserMetaRoleWithNullConn() throws Exception {
 		//get a UserMeta object
 		UserMeta userMeta = 
 			this.dataFacade.retreiveUserMetaByIdentifierLoginVia(this.conn, 
 																 "mevans", 				
 																 UserMeta.LoginVia.INTERNAL);
 		//update the Role
 		userMeta.setRole(Role.ADMIN);
 		
 		//call the API under test to update the Role of the UserMeta
 		this.dataFacade.updateUserMetaRole(null, userMeta);		
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateUserMetaRoleWithNullUserMeta() throws Exception {
 		//call the API under test to update the Role of the UserMeta
 		this.dataFacade.updateUserMetaRole(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveStaticPage() throws Exception {
 		StaticPagePOJO aboutPagePojo = 
 			this.dataFacade.retreiveStaticPage(this.conn, "about");
 		String expectedAboutPage = "This is the about page.\n";
 		String aboutPage = aboutPagePojo.getContents();
 		assertEquals(expectedAboutPage, aboutPage);
 		
 		StaticPagePOJO tosPagePojo = 
 			this.dataFacade.retreiveStaticPage(this.conn, "tos");
 		String expectedTos = "This is the terms of service page.\n";
 		String tosPage = tosPagePojo.getContents();
 		assertEquals(expectedTos, tosPage);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveStaticPageWithNullConn() throws Exception {
 		StaticPagePOJO aboutPagePojo = 
 			this.dataFacade.retreiveStaticPage(null, "about");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveStaticPageWithNullId() throws Exception {
 		StaticPagePOJO aboutPagePojo = 
 			this.dataFacade.retreiveStaticPage(this.conn, null);
 	}
 	
 	@Test
 	public void testUpsertStaticPage() throws Exception {
 		//upsert (update) the about page
 		String updatedAboutPage = "This is the new about page";
 		StaticPagePOJO aboutPage = new StaticPagePOJO("about", updatedAboutPage);
 		this.dataFacade.upsertStaticPage(this.conn, aboutPage);
 		
 		String sql = String.format(Sql.RETREIVE_STATIC_PAGE, 
 								   DataInitializer.wrapForSQL("about"));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		String updatedAboutPageFromDb = rs.getString("contents");
 		assertEquals(updatedAboutPage, updatedAboutPageFromDb);
 		
 		//upsert (insert) credits page
 		String newCreditsPage = "This is the credits page";
 		StaticPagePOJO creditsPage = new StaticPagePOJO("credits", newCreditsPage);
 		this.dataFacade.upsertStaticPage(this.conn, creditsPage);
 		sql = String.format(Sql.RETREIVE_STATIC_PAGE, 
 							DataInitializer.wrapForSQL("credits"));
 		stmt = this.conn.createStatement();
 		rs = stmt.executeQuery(sql);
 		rs.next();
 		String newCreditsPageFromDb = rs.getString("contents");
 		assertEquals(newCreditsPage, newCreditsPageFromDb);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpsertStaticPageWithNullConn() throws Exception {
 		//upsert (update) the about page
 		String updatedAboutPage = "This is the new about page";
 		StaticPagePOJO aboutPage = new StaticPagePOJO("about", updatedAboutPage);
 		this.dataFacade.upsertStaticPage(null, aboutPage);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpsertStaticPageWithNullStaticPage() throws Exception {
 		this.dataFacade.upsertStaticPage(this.conn, null);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testRetreiveFromKvTable() throws Exception {
 		this.dataFacade.retreiveFromKvTable(this.conn, "");
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void testUpsertKvTable() throws Exception {
 		this.dataFacade.upsertKvTable(this.conn, "", "");
 	}
 	
 	@Test
 	public void testRetreiveFromKvTableClob() throws Exception {
 		String googleAnalyticsVal = 
 			this.dataFacade.retreiveFromKvTableClob(this.conn, "google_analytics");
 		assertEquals("This is my Google Analytics code", googleAnalyticsVal);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveFromKvTableClobWithNullC() throws Exception {
 		String googleAnalyticsVal = 
 			this.dataFacade.retreiveFromKvTableClob(null, "google_analytics");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveFromKvTableClobWithNullK() throws Exception {
 		String googleAnalyticsVal = 
 			this.dataFacade.retreiveFromKvTableClob(this.conn, null);
 	}
 	
 	@Test
 	public void testUpsertKvTableClob_Update() throws Exception {
 		//upsert (update)
 		String k = "google_analytics";
 		String updatedV = "This is the updated Google analytics code";
 		this.dataFacade.upsertKvTableClob(this.conn, k, updatedV);
 		
 		//fetch the data and verify
 		String sql = String.format(Sql.RETREIVE_KVTABLE_CLOB, DataInitializer.wrapForSQL(k));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		String fetchedV = rs.getString("v");
 		assertEquals(updatedV, fetchedV);
 	}
 	
 	@Test
 	public void testUpsertKvTableClob_Insert() throws Exception {
 		//upsert (update)
 		String k = "alexa_analytics";
 		String v = "This is the Alexa analytics code";
 		this.dataFacade.upsertKvTableClob(this.conn, k, v);
 		
 		//fetch the data and verify
 		String sql = String.format(Sql.RETREIVE_KVTABLE_CLOB, DataInitializer.wrapForSQL(k));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		String fetchedV = rs.getString("v");
 		assertEquals(v, fetchedV);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpsertKvTableClobWithNullConn() throws Exception {
 		this.dataFacade.upsertKvTableClob(null, "", "");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpsertKvTableClobWithNullK() throws Exception {
 		this.dataFacade.upsertKvTableClob(this.conn, null, "");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpsertKvTableClobWithNullV() throws Exception {
 		this.dataFacade.upsertKvTableClob(this.conn, "", null);
 	}
 	
 	@Test
 	public void retreiveAllDiscussionForums() throws Exception {
 		//first let's add some forums
 		String forumIds[] = {"Bio101", "OrganicChem", "Physics"};
 		String forumTitles[] = {"Introduction to Biology", "Introduction to Organic Chemistry", "Introduction to Physics"};
 		String forumDescs[] = {"Introduction to Biology", "Introduction to Organic Chemistry", "Introduction to Physics"};
 //		for(int i=0; i<3; i++) {
 //			String sql = String.format(Sql.INSERT_DISCUSSION_FORUM, 
 //									   DataInitializer.wrapForSQL(forumIds[i]), 
 //									   DataInitializer.wrapForSQL(forumTitles[i]), 
 //									   DataInitializer.wrapForSQL(forumDescs[i]));
 //			Statement stmt = this.conn.createStatement();
 //			stmt.executeUpdate(sql);
 //		}
 		
 		//verify if the forums can be retreived with our API
 		List<Forum> forums = 
 			this.dataFacade.retreiveAllDiscussionForums(this.conn);
 		assertNotNull(forums);
 		assertEquals(3, forums.size());
 		for(int i=0; i<3; i++) {
 			Forum forum = forums.get(i);
 			assertEquals(forumIds[i], forum.getId());
 			assertEquals(forumTitles[i], forum.getTitle());
 			assertEquals(forumDescs[i], forum.getDescription());
 		}
 		
 	}
 	
 //	@Test
 //	public void retreiveAllDiscussionForums_WhenNoneExist() throws Exception {
 //		List<Forum> forums = 
 //			this.dataFacade.retreiveAllDiscussionForums(this.conn);
 //		assertNotNull(forums);
 //		assertEquals(0, forums.size());
 //	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveAllDiscussionForumsWithNullConn() throws Exception {
 		this.dataFacade.retreiveAllDiscussionForums(null);
 	}
 	
 	@Test
 	public void testRetreiveDiscussionForum() throws Exception {
 		String forumId = "Physics";
 		String expectedTitleNContents = "Introduction to Physics";
 		
 		Forum forum = 
 			this.dataFacade.retreiveDiscussionForum(this.conn, forumId);
 		assertNotNull(forum);
 		assertEquals(forumId, forum.getId());
 		assertEquals(expectedTitleNContents, forum.getTitle());
 		assertEquals(expectedTitleNContents, forum.getDescription());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveDiscussionForumWithNullConn() throws Exception {
 		String forumId = "Physics";
 		this.dataFacade.retreiveDiscussionForum(null, forumId);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveDiscussionForumWithNullForumId() throws Exception {
 		this.dataFacade.retreiveDiscussionForum(this.conn, null);
 	}
 	
 	@Test
 	public void testInsertDiscussionForum() throws Exception {
 		String id = "RDBMS";
 		String title = "Relational datbase management systems";
 		String description = "description of the forum";
 		Forum forum = new Forum(id, title, description);
 		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		String sql = "SELECT * FROM DISCUSSION WHERE id='RDBMS'";
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		assertEquals(id, rs.getString("id"));
 		assertEquals(title, rs.getString("title"));
 		assertEquals(description, rs.getString("description"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertDiscussionForumWithNullConn() throws Exception {
 		String id = "RDBMS";
 		String title = "Relational datbase management systems";
 		String description = "description of the forum";
 		Forum forum = new Forum(id, title, description);
 		
 		this.dataFacade.insertDiscussionForum(null, forum);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertDiscussionForumWithNullForum() throws Exception {
 		this.dataFacade.insertDiscussionForum(this.conn, null);
 	}
 	
 	@Test
 	public void testDeleteDiscussionForum() throws Exception {
 		//first let's insert a discussion forum
 		String id = "RDBMS";
 		String title = "Relational datbase management systems";
 		String description = "description of the forum";
 		Forum forum = new Forum(id, title, description);		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		
 		//delete the discussion forum
 		this.dataFacade.deleteDiscussionForum(this.conn, forum);
 		
 		//ensure that the forum was deleted
 		String sql = String.format(Sql.RETREIVE_DISCUSSION_FORUM, DataInitializer.wrapForSQL(forum.getId()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		assertFalse(rs.next());
 	}
 	
 	@Test(expected=CannotPerformActionException.class)
 	public void testDeleteDiscussionForum_WhenQuestionsExist() throws Exception {
 		//first let's insert a discussion forum and a question
 		String forumId = "RDBMS";
 		String forumTitle = "Relational datbase management systems";
 		String forumDescription = "description of the forum";
 		Forum forum = new Forum(forumId, forumTitle, forumDescription);		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		
 		Question question = new Question("What is an RDBMS", "Please explain what an RDBMS is.", forumId);
 		//Note: Adding a question where the userid is null in the database
 		String insertQuestionSql = 
 			String.format(Sql.INSERT_QUESTION,
 						  null,
 						  DataInitializer.wrapForSQL(question.getDiscussionId()), 
 						  DataInitializer.wrapForSQL(question.getTitle()), 
 						  DataInitializer.wrapForSQL(question.getContents()));
 		Statement insertQuestionStatement = this.conn.createStatement();
 		insertQuestionStatement.executeUpdate(insertQuestionSql);
 		
 		//delete the discussion forum
 		this.dataFacade.deleteDiscussionForum(this.conn, forum);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testDeleteDiscussionForumWithNullConn() throws Exception {
 		String id = "RDBMS";
 		String title = "Relational datbase management systems";
 		String description = "description of the forum";
 		Forum forum = new Forum(id, title, description);
 		
 		this.dataFacade.deleteDiscussionForum(null, forum);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testDeleteDiscussionForumWithNullForum() throws Exception {
 		this.dataFacade.deleteDiscussionForum(this.conn, null);
 	}
 	
 	@Test
 	public void testInsertQuestion() throws Exception {
 		//first let's create the forum for the question
 		String forumId = "RDBMS";
 		String forumTitle = "Relational datbase management systems";
 		String forumDescription = "description of the forum";
 		Forum forum = new Forum(forumId, forumTitle, forumDescription);		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		
 		Question question = new Question("What is an RDBMS", "Please explain what an RDBMS is.", forumId);
 		
 		//call the API we want to test - insertQuestion
 		this.dataFacade.insertQuestion(this.conn, question);
 		
 		//verify if the question was inserted
 		String sql = String.format(Sql.RETREIVE_QUESTIONS_FOR_DISCUSSION_FORUM, 
 								   DataInitializer.wrapForSQL(question.getDiscussionId()));
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		rs.next();
 		assertEquals(forumId, rs.getString("discussion_id"));
 		assertEquals(question.getTitle(), rs.getString("title"));
 		assertEquals(question.getContents(), rs.getString("contents"));
 	}
 	
 	@Test(expected=DataException.class)
 	public void testInsertQuestion_WithWrongForumId() throws Exception {
 		//first let's create the forum for the question
 		String forumId = "RDBMS";
 		String forumTitle = "Relational datbase management systems";
 		String forumDescription = "description of the forum";
 		Forum forum = new Forum(forumId, forumTitle, forumDescription);		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		
 		Question question = new Question("What is an RDBMS", "Please explain what an RDBMS is.", forumId+"incorrect");
 		
 		//call the API we want to test - insertQuestion
 		this.dataFacade.insertQuestion(this.conn, question);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertQuestionWithNullConn() throws Exception {
 		//first let's create the forum for the question
 		String forumId = "RDBMS";
 		String forumTitle = "Relational datbase management systems";
 		String forumDescription = "description of the forum";
 		Forum forum = new Forum(forumId, forumTitle, forumDescription);		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		
 		Question question = new Question("What is an RDBMS", "Please explain what an RDBMS is.", forumId);
 		
 		//call the API we want to test - insertQuestion
 		this.dataFacade.insertQuestion(null, question);
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testInsertQuestionWithNullQuestion() throws Exception {
 		//first let's create the forum for the question
 		String forumId = "RDBMS";
 		String forumTitle = "Relational datbase management systems";
 		String forumDescription = "description of the forum";
 		Forum forum = new Forum(forumId, forumTitle, forumDescription);		
 		this.dataFacade.insertDiscussionForum(this.conn, forum);
 		
 		//call the API we want to test - insertQuestion
 		this.dataFacade.insertQuestion(this.conn, null);
 	}
 	
 	@Test
 	public void testRetreiveAllQuestionsForForum() throws Exception {
 //		//let's first insert three questions
 		String forumId = "Physics";
 		String forumTitle = "This is a Physics forum";
 		String forumDescription = "description of the forum";
 		Forum forum = new Forum(forumId, forumTitle, forumDescription);
 //		Question question1 = new Question("Newton's first law of motion", 
 //										 "Please explain Newtons first law of motion.", 
 //										 forumId);		
 //		
 //		Question question2 = new Question("Newton's second law of motion", 
 //				 						  "Please explain Newton's second law of motion.", 
 //				 						  forumId);
 //		
 //		Question question3 = new Question("Newton's third law of motion", 
 //				  						  "Please explain Newton's third law of motion.", 
 //				  						  forumId);
 //		Question insertedQuestions[] = {question1, question2, question3};
 //		
 //		this.dataFacade.insertQuestion(this.conn, question1);
 //		this.dataFacade.insertQuestion(this.conn, question2);
 //		this.dataFacade.insertQuestion(this.conn, question3);
 		
 		//let's call the API to retreive and verify the questions
 		String expectedTitleTemplate = "Question ";
 		String expectedContentsTemplate = "Contents for question ";
 		
 		List<Question> questions = 
 			this.dataFacade.retreiveAllQuestionsForForum(this.conn, forum);
 		assertNotNull(questions);
 		assertEquals(3, questions.size());
 
 		for(int i=0; i<3; i++) {
 			assertEquals(i, questions.get(i).getId());
 			assertEquals(expectedTitleTemplate + i, questions.get(i).getTitle());
 			assertEquals(expectedContentsTemplate + i, questions.get(i).getContents());
 			assertEquals(forum.getId(), questions.get(i).getDiscussionId());
 		}
 		
 		
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveQuestionWithNullConn() throws Exception {
 		//TODO: Should we create an API to get forums from TestObjectRepository
 		String forumId = "Physics";
 		int questionId = 0;
 		this.dataFacade.retreiveQuestion(null, forumId, questionId);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveQuestionWithNullForumId() throws Exception {
 		//TODO: Should we create an API to get forums from TestObjectRepository
 		String forumId = "Physics";
 		int questionId = 0;
 		this.dataFacade.retreiveQuestion(this.conn, null, questionId);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testRetreiveQuestionWithInvalidQuestionId() throws Exception {
 		//TODO: Should we create an API to get forums from TestObjectRepository
 		String forumId = "Physics";
 		int questionId = -1;
 		this.dataFacade.retreiveQuestion(this.conn, forumId, questionId);
 	}
 	
 	@Test
 	public void testRetreiveQuestion() throws Exception {
 		//TODO: Should we create an API to get forums from TestObjectRepository
 		String forumId = "Physics";
 		int questionId = 0;
 		Question question = 
 			this.dataFacade.retreiveQuestion(this.conn, forumId, questionId);
 		assertNotNull(question);
 		assertEquals(0, question.getId());
 		assertEquals("Question 0", question.getTitle());
 		assertEquals("Contents for question 0", question.getContents());
 		assertEquals(TestObjectsRepository.getInstance().getUserUserMeta("dvidakovich").userMeta,
 					 question.getUserMeta());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveAnswersForQuestionWithNullConn() throws Exception {
 		this.dataFacade.retreiveAnswersForQuestion(null, 0);
 	}
 	
 	@Test
 	public void testRetreiveAnswersForQuestionWithZeroANswers() throws Exception {
 		List<Answer> answers = 
 			this.dataFacade.retreiveAnswersForQuestion(this.conn, 0);
 		assertNotNull(answers);
 		assertEquals(0, answers.size());
 	}
 	
 	@Test
 	public void testRetreiveAnswersForQuestionWithThreeANswers() throws Exception {
 		//let's add a few answers for question id 0
 		UserMeta user = 
 			TestObjectsRepository.getInstance().
 				getUserUserMeta("dvidakovich").userMeta;
 		Answer answers[] = new Answer[] {new Answer(0, user, 0, "first answer"),
 										 new Answer(0, user, 0, "second answer"),
 										 new Answer(0, user, 0, "third answer")};
 		for(Answer answer : answers) {
 			this.dataFacade.insertAnswer(this.conn, answer);
 		}
 		
 		List<Answer> retreivedAnswers = 
 			this.dataFacade.retreiveAnswersForQuestion(this.conn, 0);
 		assertNotNull(retreivedAnswers);
 		assertEquals(answers.length, retreivedAnswers.size());
 		for(Answer answer : retreivedAnswers) {
 			assertEquals(user, answer.getUserMeta());
 		}
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertAnswerWithNullConn() throws Exception {
 		Answer answer = new Answer(0, "the answer body");
 		this.dataFacade.insertAnswer(null, answer);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertAnswerWithNullAnswer() throws Exception {
 		this.dataFacade.insertAnswer(this.conn, null);
 	}
 	
 	@Test
 	public void testInsertAnswer() throws Exception {
 		Answer answer = new Answer(0, "the answer body");
 		answer.setQuestionId(0);
 		
 		this.dataFacade.insertAnswer(this.conn, answer);
 		
 		String sql = String.format(Sql.RETREIVE_ANSWER, answer.getId());
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		assertTrue(rs.next());
 		assertEquals(answer.getId(), rs.getInt("id"));
 		assertEquals(answer.getQuestionId(), rs.getInt("question_id"));
 		assertEquals(answer.getContents(), rs.getString("contents"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testIsQuestionAnsweredWithNullConn() throws Exception {
 		int questionId = 0;
 		this.dataFacade.isQuestionAnswered(null, questionId);
 	}
 	
 	@Test
 	public void testIsQuestionAnsweredWhenItIs() throws Exception {
 		int questionId = 0;
 		this.dataFacade.markQuestionAsAnswered(this.conn, questionId);
 		assertTrue(this.dataFacade.isQuestionAnswered(this.conn, questionId));
 	}
 	
 	@Test
 	public void testIsQuestionAnsweredWhenItIsNot() throws Exception {
 		int questionId = 0;
 		assertFalse(this.dataFacade.isQuestionAnswered(this.conn, questionId));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testMarkQuestionAsAnsweredWithNullConn() throws Exception {
 		this.dataFacade.markQuestionAsAnswered(null, 0);
 	}
 	
 	@Test(expected=DataException.class)
 	public void testMarkQuestionAsAnsweredWithNullIncorrectQuestionIdMinusOne() 
 		throws Exception {
 		
 		this.dataFacade.markQuestionAsAnswered(this.conn, -1);
 	}
 	
 	@Test(expected=DataException.class)
 	public void testMarkQuestionAsAnsweredWithNullIncorrectQuestionIdBigNumber() 
 		throws Exception {
 		
 		this.dataFacade.markQuestionAsAnswered(this.conn, 1000);
 	}
 	
 	@Test
 	public void testMarkQuestionAsAnswered() throws Exception {
 		this.dataFacade.markQuestionAsAnswered(this.conn, 0);
 		String sql = String.format(Sql.RETREIVE_QUESTIONS_ANSWERED, "0");
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		assertTrue(rs.next());
 		assertEquals(0, rs.getInt("question_id"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testMarkQuestionAsUnansweredWithNullConn() throws Exception {
 		this.dataFacade.markQuestionAsUnanswered(null, 0);
 	}
 	
 	@Test
 	public void testMarkQuestionAsUnanswered() throws Exception {
 		int questionId = 0;
 		this.dataFacade.markQuestionAsAnswered(conn, questionId);
 		this.dataFacade.markQuestionAsUnanswered(this.conn, questionId);
 		String sql = String.format(Sql.RETREIVE_QUESTIONS_ANSWERED, questionId);
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		assertFalse(rs.next());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertQuestionTimestampWithNullConn() throws Exception {
 		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
 		this.dataFacade.insertQuestionTimestamp(null, questionId, timestamp, locale);		
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testInsertQuestionTimestampWithNullLocale() throws Exception {
 		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
 		this.dataFacade.insertQuestionTimestamp(this.conn, questionId, timestamp, null);		
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testInsertQuestionTimestampWithBadQuestionId() throws Exception {
 		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
 		this.dataFacade.insertQuestionTimestamp(this.conn, -1, timestamp, locale);		
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testInsertQuestionTimestampWithBadTimestamp() throws Exception {
 		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
 		this.dataFacade.insertQuestionTimestamp(this.conn, questionId, -1, locale);
 	}
 	
 	@Test
 	public void testInsertQuestionTimestamp() throws Exception {
		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
		this.dataFacade.insertQuestionTimestamp(this.conn, questionId, timestamp, locale);
 		
		String sql = String.format(Sql.RETREIVE_QUESTION_TIMESTAMP, questionId);
 		Statement stmt = this.conn.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		assertTrue(rs.next());
 		assertEquals(timestamp, rs.getLong("tstamp"));
 		assertEquals(locale.toString(), rs.getString("locale"));
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testRetreiveQuestionTimestampWithNullConn() throws Exception {
 		//first let us insert some test data
 		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
 		this.dataFacade.retreiveQuestionTimestamp(null, questionId);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testRetreiveQuestionTimestampWithBadQuestionId() throws Exception {
 		//first let us insert some test data
 		int questionId = 0;
 		Locale locale = Locale.getDefault();
 		long timestamp = new Date().getTime();
 		this.dataFacade.retreiveQuestionTimestamp(this.conn, -1);
 	}
 	
 	@Test
 	public void testRetreiveQuestionTimestamp() throws Exception {
		//first let us insert some test data
		int questionId = 0;
		Locale locale = Locale.getDefault();
		long timestamp = new Date().getTime();
		this.dataFacade.insertQuestionTimestamp(this.conn, questionId, timestamp, locale);
 		long retreivedTimestamp = 
			this.dataFacade.retreiveQuestionTimestamp(this.conn, questionId);
		assertEquals(timestamp, retreivedTimestamp);
 	}
 	
 	@Test
 	public void testWrapForSQL() {
 		String origStr1 = "str";
 		String expectedStr1 = "'str'";
 		assertEquals(expectedStr1, DataFacadeRDBMSImpl.wrapForSQL(origStr1));
 		
 		String origStr2 = "a'b";
 		String expectedStr2 = "'a''b'";
 		assertEquals(expectedStr2, DataFacadeRDBMSImpl.wrapForSQL(origStr2));
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		Statement stmt = this.conn.createStatement();
 		stmt.execute("SHUTDOWN");
 		ConnectionPool.closeConnection(DATABASE_ID);
 	}
 	
 	private void compareCourses(Course expectedCourse, Course course) {
 		assertNotNull(course);
 		assertEquals(expectedCourse.getId(), course.getId());
 		assertEquals(expectedCourse.getTitle(), course.getTitle());
 		assertEquals(expectedCourse.getDescription(), course.getDescription());
 		assertEquals(TestObjectsRepository.getInstance().getFullyPopulatedMentor(), course.getMentor());
 		List<Competency> courseCompetencies = course.getCompetencies();
 		List<Competency> expectedCourseCompetencies = expectedCourse.getCompetencies();
 		assertEquals(expectedCourseCompetencies.size(), courseCompetencies.size());
 		for(int i=0; i<expectedCourseCompetencies.size(); i++) {
 			assertEquals(expectedCourseCompetencies.get(i), courseCompetencies.get(i));
 		}
 	}
 
 }
