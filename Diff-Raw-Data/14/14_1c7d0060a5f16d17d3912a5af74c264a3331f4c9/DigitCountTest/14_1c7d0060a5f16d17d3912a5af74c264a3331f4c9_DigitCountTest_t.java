 package countingDigits;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class DigitCountTest {
 
 	@Test
 	public void digit_count_of_314159() {
		assertEquals("Incorrect, with a call of digitCount(314159)", 6, DigitCount.digitCount(314159));
 	}
 
 	
 	@Test
 	public void digit_count_of_0() {
		assertEquals("Incorrect, with a call of digitCount(0)", 1, DigitCount.digitCount(0));
 	}
 	
 	@Test
 	public void digit_count_of_8() {
		assertEquals("Incorrect, with a call of digitCount(8)", 1, DigitCount.digitCount(8));
 	}
 	
 	@Test
 	public void digit_count_of_1040() {
		assertEquals("Incorrect, with a call of digitCount(1040)", 4, DigitCount.digitCount(1040));
 	}
 	
 	@Test
 	public void digit_count_of_314159265() {
		assertEquals("Incorrect, with a call of digitCount(314159265)", 9, DigitCount.digitCount(314159265));
 	}
 	
 }
