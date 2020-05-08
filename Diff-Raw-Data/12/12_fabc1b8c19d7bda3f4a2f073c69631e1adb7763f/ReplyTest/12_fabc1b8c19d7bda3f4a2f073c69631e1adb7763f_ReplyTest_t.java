 package evaluationserver.server.execution;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ReplyTest {
 	
 	public ReplyTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 	
 	@Before
 	public void setUp() {
 	}
 	
 	@After
 	public void tearDown() {
 	}
 
 	/**
 	 * Test of fromCode method, of class Reply.
 	 */
 	@Test
 	public void testFromCode() {
 		System.out.println("fromCode");
 		assertEquals(Reply.ACCEPTED, Reply.fromCode("AC"));
		assertEquals(Reply.MEMORY_LIMIT_EXCEEDED, Reply.fromCode(" ML "));
 		assertEquals(Reply.COMPILE_ERROR, Reply.fromCode("\n ce \n\r\n "));
 	}
 }
