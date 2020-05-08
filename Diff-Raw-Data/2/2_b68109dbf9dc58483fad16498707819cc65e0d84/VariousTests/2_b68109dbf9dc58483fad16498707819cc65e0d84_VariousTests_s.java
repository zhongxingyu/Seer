 package various;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import org.junit.*;
 
 public class VariousTests {
 
 	@Test
 	public void testIntegerUnboxingUsingObjects() {
 		Object[] arrayOfIntegers = new Object[] {
 				1, 2, 3 };
 
 		assertEquals(arrayOfIntegers[0], 1);
 		assertEquals(arrayOfIntegers[1], 2);
 		assertEquals(arrayOfIntegers[2], 3);
 	}
 
 	@Test
 	public void testStringValueOfOnPrimitiveInteger() {
 		assertEquals("1", String.valueOf(1));
 		assertEquals("5", String.valueOf(5));
 		assertEquals("100", String.valueOf(100));
 		assertEquals("666", String.valueOf(666));
 	}
 
 	@Test
	public void testConvertionFromArrayOfCharsToString() {
 		char[] chars = new char[] {
 				'a', 'b' };
 		assertThat(chars.toString(), is(not("ab")));
 	}
 
 	@Test
 	public void testOperatorPrecedence() {
 		int x = 2;
 		int y = 3;
 		assertThat(x + y + "x + y" + x + y, is("5x + y23"));
 	}
 }
