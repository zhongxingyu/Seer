 /**
  * 
  */
 package AP2DX.test;
 
 import AP2DX.*;
 
 import static org.junit.Assert.*;
 
 import java.net.Socket;
 
 import junit.framework.TestCase;
 
 import mockit.Mock;
 import mockit.MockUp;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * @author jjwt
  *
  */
 public class ConnectionTestCase extends TestCase {
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link AP2DX.Connection#Connection(java.lang.String, int)}.
 	 */
 	@Test
 	public void testConnection() {
 		/*
 		 * Why do we need this????
 		 * 
 		 * new MockUp<Socket>() {
 			@Mock
 			void $init(String host, int port) {
 				//pass
 			}
 		};*/
 		
 		Connection conn = null;
 		try {
 			conn = new Connection("uva.nl", 80, Module.USARSIM);
 			assertNotNull(conn);
 		}
 		catch (Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Test method for {@link AP2DX.Connection#sendMessage(java.lang.String)}.
 	 */
 	@Test
 	public void testSendMessage() {
 		try {
 			Connection conn = new Connection("uva.nl", 80, Module.USARSIM);
 			assertTrue(conn.sendMessage("Message to server."));
 		} catch(Exception e) {
 			fail();
 		}
 	}
 
 	/**
 	 * Test method for {@link AP2DX.Connection#readMessage()}.
 	 */
 	@Test
 	@Ignore("Not implemented")
 	public void testReadMessage() {
 		fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for {@link AP2DX.Connection#close()}.
 	 */
 	@Test
 	public void testClose() {
		try {
			Connection conn = new Connection("uva.nl", 80, Module.USARSIM);
			assertTrue(conn.close());
		} catch(Exception e) {
			fail();
		}
 	}
 
 }
