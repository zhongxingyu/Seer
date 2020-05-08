 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import op.sample.spring.exception.TestException;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 
 public class TestExceptionTest {
 	private static ClassPathXmlApplicationContext appContext = null;
 	
 	
 	@Before
 	public void setUp() throws Exception {
		appContext = new ClassPathXmlApplicationContext("opSample-beans-config.xml");
 	}
 
 	@Test
 	public void testTestException() {
 		try {
 			appContext.getBean(TestException.class).testException();
 			assertTrue(true);
 		} catch (Exception e) {
 			assertFalse(false);
 		}
 	}
 
 }
