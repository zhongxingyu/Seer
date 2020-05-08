 /** Tests the MyMath class using Junit4.
  * 
  * To keep the tests very simple, we only use one type of
  * assertion: assertEquals(). And we only put tests in the
  * class even though we could have used lots more advanced
  * Junit4 features.
  * */
package devilry.sandbox.junit4examples;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 
 
 /**
  * @author Espen Angell Kristiansen
  */
 public class MyMathTest {
 
 	@Test public void sum(){   // the @Test decorator marks this as a test 
 		assertEquals(4, MyMath.sum(2, 2));
		assertEquals(4.7, MyMath.sum(2.4, 2.3));
 	}
 
 	@Test public void div(){
 		assertEquals(2, MyMath.div(4, 2));
 		assertEquals(1, MyMath.div(3, 2));
 
 		assertEquals(2, MyMath.div(4, 2));
		assertEquals(1.5, MyMath.div(3, 2));
 	}
}
