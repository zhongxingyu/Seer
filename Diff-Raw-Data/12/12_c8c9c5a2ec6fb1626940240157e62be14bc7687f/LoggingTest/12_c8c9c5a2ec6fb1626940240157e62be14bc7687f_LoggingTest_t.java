 /**
  * 
  */
 package ch.krizi.utility.logging;
 
 import org.junit.Test;
 
 import ch.krizi.utility.logging.annotation.Log;
 
 /**
  * @author krizi
  * 
  */
 public class LoggingTest {
 
 	@Test
 	public void testAnno() {
 		myMethod();
 	}
 
 	@Test
 	public void testAnno2() {
 		myMethod(3, "value");
 	}
 
 	@Test
 	public void testAnno3() {
 		myMethod(999999999);
 	}
 
	@Test(expected = RuntimeException.class)
 	public void testThrowError() {
 		throwError();
 	}
 
 	@Log
 	private void throwError() {
 		throw new RuntimeException("runtaim");
 	}
 
 	@Log
 	private void myMethod() {
 	}
 
 	@Log
 	private Integer myMethod(int aNumber, String aString) {
 		return 9;
 	}
 
 	@Log
 	private Integer myMethod(int aNumber) {
 		return 1888888;
 	}
 }
