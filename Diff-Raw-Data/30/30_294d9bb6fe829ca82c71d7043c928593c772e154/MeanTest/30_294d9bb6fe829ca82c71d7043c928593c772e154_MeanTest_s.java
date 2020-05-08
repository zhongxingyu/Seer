 package jscl.math.operator;
 
 import jscl.JsclMathEngine;
 import jscl.MathEngine;
 import jscl.text.ParseException;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * User: serso
  * Date: 12/26/11
  * Time: 11:15 AM
  */
 public class MeanTest {
 
 	@Test
 	public void testEvaluate() throws Exception {
 		MathEngine me = JsclMathEngine.instance;
		try {
 			me.evaluate("mean()");
 			Assert.fail();
 		} catch (ParseException e) {
 			// ok
 		}
 
 		Assert.assertEquals("0.0", me.evaluate("mean({0})"));
 		Assert.assertEquals("10.0", me.evaluate("mean({10})"));
 		Assert.assertEquals("15.0", me.evaluate("mean({10, 20})"));
 		Assert.assertEquals("15.0", me.evaluate("mean({sin(7), cos(3)})"));
		Assert.assertEquals("15.0", me.evaluate("mean({rand(), rand(), rand(), rand(), rand(), rand(), rand(), rand(), rand(), rand()})"));
 	}
 }
