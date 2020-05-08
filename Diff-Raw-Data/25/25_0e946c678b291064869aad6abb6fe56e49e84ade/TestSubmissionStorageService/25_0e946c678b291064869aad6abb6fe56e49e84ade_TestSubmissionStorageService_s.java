 package edu.harvard.cs262.grading.test;
 
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.Random;
 
 import javax.sql.rowset.serial.SerialException;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mongodb.MongoException;
 
 import edu.harvard.cs262.grading.server.services.Assignment;
 import edu.harvard.cs262.grading.server.services.AssignmentImpl;
 import edu.harvard.cs262.grading.server.services.MongoSubmissionStorageService;
 import edu.harvard.cs262.grading.server.services.Student;
 import edu.harvard.cs262.grading.server.services.StudentImpl;
 import edu.harvard.cs262.grading.server.services.Submission;
 import edu.harvard.cs262.grading.server.services.SubmissionImpl;
 
 import static org.junit.Assert.*;
 
 @SuppressWarnings("unused")
 public class TestSubmissionStorageService {
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
 	public void testMongoInit() throws UnknownHostException, MongoException,
 			SerialException, SQLException, RemoteException {
 		MongoSubmissionStorageService service = new MongoSubmissionStorageService();
 		service.init();
 		service.heartbeat();
 
 		Random r = new Random();
 
 		Student student = new StudentImpl(50);
 		Assignment assn = new AssignmentImpl(50);
 
 		String contents = "Hello World, " + r.nextInt(100) + " times.";
 
 		byte[] b = contents.getBytes();
 		Submission sub = new SubmissionImpl(student, assn, b);
 
 		service.storeSubmission(sub);
 
		assert (service.getLatestSubmission(student, assn).getContents()
				.equals(contents.getBytes()));
 	}
 }
