 package edu.harvard.cs262.grading.test;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import edu.harvard.cs262.grading.server.services.Assignment;
 import edu.harvard.cs262.grading.server.services.AssignmentImpl;
 import edu.harvard.cs262.grading.server.services.Grade;
 import edu.harvard.cs262.grading.server.services.GradeCompilerService;
 import edu.harvard.cs262.grading.server.services.GradeCompilerServiceServer;
 import edu.harvard.cs262.grading.server.services.GradeStorageService;
 import edu.harvard.cs262.grading.server.services.InvalidGraderForStudentException;
 import edu.harvard.cs262.grading.server.services.MongoGradeStorageService;
 import edu.harvard.cs262.grading.server.services.MongoSubmissionStorageService;
 import edu.harvard.cs262.grading.server.services.ScoreImpl;
 import edu.harvard.cs262.grading.server.services.SharderServiceServer;
 import edu.harvard.cs262.grading.server.services.Student;
 import edu.harvard.cs262.grading.server.services.StudentImpl;
 import edu.harvard.cs262.grading.server.services.Submission;
 import edu.harvard.cs262.grading.server.services.SubmissionReceiverService;
 import edu.harvard.cs262.grading.server.services.SubmissionReceiverServiceServer;
 import edu.harvard.cs262.grading.server.services.SubmissionStorageService;
 import static org.junit.Assert.*;
 
 public class TestGradeCompilerService {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 	
 	@Test
 	public void testGradeStorage() throws Exception {
 		// instantiate the storage services locally
 		GradeStorageService gradeStorage = new MongoGradeStorageService();
 		gradeStorage.init();
 		SubmissionStorageService submissionStorage = new MongoSubmissionStorageService();
 		submissionStorage.init();
 
 		// create students
 		Student[] students = 
 			{new StudentImpl(100,"a@harvard.edu","Kenny","Yu"),
 			 new StudentImpl(101,"b@harvard.edu","Jim","Danz"),
 			 new StudentImpl(102,"c@harvard.edu","Willie","Yao"),
 			 new StudentImpl(103,"d@harvard.edu","Tony","Ho"),
 			 new StudentImpl(104,"e@harvard.edu","Stefan","Muller")};
 		
 		// create assignments
 		Assignment assignment = new AssignmentImpl(66,"hw");
 		
 		// instantiate a sandboxed submission receiver service
 		SubmissionReceiverService receiver = new SubmissionReceiverServiceServer(submissionStorage);
 		
 		// Kenny and Jim submit
 		Submission submission1 = receiver.submit(students[0], assignment, (new String("cheese")).getBytes());
 		Submission submission2 = receiver.submit(students[1], assignment, (new String("cheddar")).getBytes());
 		
 		// instantiate a sandboxed sharder service
 		SharderServiceServer sharder = new SharderServiceServer();
 		sharder.init(true);
 		
 		Map<Student, Set<Student>> gradermap = new HashMap<Student, Set<Student>>();
		HashSet<Student> willieGradees = new HashSet<Student>();
 		willieGradees.add(students[0]);
 		willieGradees.add(students[1]);
 		sharder.putShard(assignment, gradermap);
 		
 		// instantiate a sandboxed grade compiler service
 		GradeCompilerService service = new GradeCompilerServiceServer(gradeStorage, submissionStorage, sharder);
 		
 		// Willie and Tony grade both Kenny and Jim
 		// Stefan grades Kenny
 		// Kenny grades Jim
 		Grade grade1 = service.storeGrade(students[2], submission1, new ScoreImpl(50,100), "good");
 		Grade grade2 = service.storeGrade(students[2], submission2, new ScoreImpl(60,100), "better");
 		Grade grade3 = service.storeGrade(students[3], submission1, new ScoreImpl(70,100), "okay");
 		Grade grade4 = service.storeGrade(students[3], submission2, new ScoreImpl(80,100), "mediocre");
 		Grade grade5 = service.storeGrade(students[4], submission1, new ScoreImpl(90,100), "awesome");
 		Grade grade6 = service.storeGrade(students[0], submission2, new ScoreImpl(100,100), "perfect");
 		
 		try {
 			service.storeGrade(students[0], submission1, new ScoreImpl(100,100), "impossible");
 			fail("student should not be allowed to grade him/herself");
 		} catch (InvalidGraderForStudentException e) {
 			assertTrue(true);
 		}
 		
 		// assert the graders are correctly mapped
 		Set<Student> kennyGraders = service.getGraders(submission1);
 		assertEquals(3,kennyGraders.size());
 		assertTrue(kennyGraders.contains(students[2]));
 		assertTrue(kennyGraders.contains(students[3]));
 		assertTrue(kennyGraders.contains(students[4]));
 		
 		Set<Student> jimGraders = service.getGraders(submission2);
 		assertEquals(3,jimGraders.size());
 		assertTrue(jimGraders.contains(students[0]));
 		assertTrue(jimGraders.contains(students[2]));
 		assertTrue(jimGraders.contains(students[3]));
 		
 		// assert the grades were successfully submitted
 		Map<Submission, List<Grade>> grades = service.getCompiledGrades(assignment);
 		List<Grade> kennyGrades = grades.get(submission1);
 		assertEquals(3,kennyGrades.size());
 		assertTrue(kennyGrades.contains(grade1));
 		assertTrue(kennyGrades.contains(grade3));
 		assertTrue(kennyGrades.contains(grade5));
 		
 		List<Grade> jimGrades = grades.get(submission2);
 		assertEquals(3,jimGrades.size());
 		assertTrue(jimGrades.contains(grade2));
 		assertTrue(jimGrades.contains(grade4));
 		assertTrue(jimGrades.contains(grade6));		
 	}
 
 }
