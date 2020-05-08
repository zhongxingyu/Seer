 package org.wwald.model;
 
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.jasypt.util.password.BasicPasswordEncryptor;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.wwald.model.UserMeta.LoginVia;
 import org.wwald.service.DataException;
 import org.wwald.service.DataFacadeRDBMSImpl;
 import org.wwald.service.Sql;
 import org.wwald.util.CompetencyUniqueIdGenerator;
 import org.wwald.view.UserForm;
 
 import util.DataInitializer;
 import util.TestObjectsRepository;
 import util.UsersFileParser.UserUserMeta;
 
 public class DataFacadeRDBMSImplTest {
 
 	private DataFacadeRDBMSImpl dataFacade;
 	private Connection conn;
 	public static final String DATABASE_ID = "localhost";
 
 	static {
 		try {
 			//ClassPathUpdater.add( "~/tmp/wwaldtestdata" );
 		} catch( Exception e ) {
 			e.printStackTrace();
 		}
 	}
 	
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
 		Course newCourse = new Course("NC", "New Course Title", null);
 		this.dataFacade.insertCourse(this.conn, newCourse);
 		String retreiveCourseSql = 
 			String.format(Sql.RETREIVE_COURSE, 
 						  DataInitializer.wrapForSQL(newCourse.getId()), 
 						  DataInitializer.wrapForSQL(newCourse.getTitle()));
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
 	public void testUpdateCoursesWiki() throws Exception {
 		String newCoursesWiki = "NC | New Course Title";
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
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCourseWikiWithNullConn() throws Exception {
 		this.dataFacade.updateCourseWiki(null, "");
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testUpdateCourseWikiWithNullContents() throws Exception {
 		this.dataFacade.updateCourseWiki(this.conn, null);
 	}
 	
 	@Test
 	public void testGetCourseEnrollmentStatusWhenNotEnrolled() throws Exception {
 		UserMeta userMeta = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		CourseEnrollmentStatus courseEnrollmentStatus = 
 			this.dataFacade.getCourseEnrollmentStatus(conn, userMeta, course);
 		assertNotNull(courseEnrollmentStatus);
 		assertEquals(UserCourseStatus.UNENROLLED, 
 					 courseEnrollmentStatus.getUserCourseStatus());
 	}
 	
 	@Test
 	public void testGetCourseEnrollmentStatusWhenEnrolled() throws Exception {
 		//create some test objects
 		UserMeta userMeta = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		
 		Timestamp timestamp = new Timestamp((new Date()).getTime());
 		
 		//insert course enrollment status for the test objects
 		String sql = 
 			String.format(Sql.INSERT_COURSE_ENROLLMENT_STATUS,
 						  DataInitializer.wrapForSQL(course.getId()),
 						  userMeta.getUserid(),
 						  UserCourseStatus.ENROLLED.getId(),
 						  DataInitializer.wrapForSQL(timestamp.toString()));
 		Statement stmt = conn.createStatement();
 		stmt.executeUpdate(sql);
 		
 		//retreive and verify course enrollment status
 		CourseEnrollmentStatus courseEnrollmentStatus = 
 			this.dataFacade.getCourseEnrollmentStatus(conn, userMeta, course);
 		assertNotNull(courseEnrollmentStatus);
 		assertEquals(UserCourseStatus.ENROLLED, 
 					 courseEnrollmentStatus.getUserCourseStatus());
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testGetCourseEnrollmentStatusForNullConnection() throws Exception {
 		UserMeta userMeta = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("dvidakovich").userMeta;
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		CourseEnrollmentStatus courseEnrollmentStatus = 
 			this.dataFacade.getCourseEnrollmentStatus(null, userMeta, course);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testGetCourseEnrollmentStatusForNullUser() throws Exception {
 		Course course = TestObjectsRepository.getInstance().getCourse("Physics");
 		CourseEnrollmentStatus courseEnrollmentStatus = 
 			this.dataFacade.getCourseEnrollmentStatus(this.conn, null, course);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testGetCourseEnrollmentStatusForNullCourse() throws Exception {
 		UserMeta userMeta = 
 			TestObjectsRepository.
 				getInstance().getUserUserMeta("dvidakovich").userMeta;
 		CourseEnrollmentStatus courseEnrollmentStatus = 
 			this.dataFacade.getCourseEnrollmentStatus(this.conn, userMeta, null);
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
 		String content = "Lecture 1\n Lecture 2\n Lecture 3";
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
 		assertEquals(content, retreivedContent);
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
