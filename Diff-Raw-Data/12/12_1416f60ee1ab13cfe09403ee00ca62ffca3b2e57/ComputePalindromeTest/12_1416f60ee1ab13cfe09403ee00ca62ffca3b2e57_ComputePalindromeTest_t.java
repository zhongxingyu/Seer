 package es.sixto.computePalindrome;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class ComputePalindromeTest {
 
 	@Test
 	public void testIsPalindrome() {
 		ComputePalindrome validatePalindrome = new ComputePalindrome();
 		boolean result = validatePalindrome.isPalindrome(121);
 		assertEquals(true, result);
 	}
 
 	@Test
 	public void testIsPalindromeFail() {
 		ComputePalindrome validatePalindrome = new ComputePalindrome();
 		boolean result = validatePalindrome.isPalindrome(195);
 		assertEquals(false, result);
 	}
 	
 	@Test
 	public void testComputePalindromeOK() {
 		ComputePalindrome validatePalindrome = new ComputePalindrome();
 		int result = validatePalindrome.computePalindrome(195);
 		assertEquals(9339, result);
 	}
 	
 	@Test
 	public void testComputePalindromeStringOK() {
 		ComputePalindrome validatePalindrome = new ComputePalindrome();
 		String result = validatePalindrome.computePalindrome("195");
 		assertEquals("4 9339", result);
 		result = validatePalindrome.computePalindrome("121");
 		assertEquals("0 121", result);
 	}
 	
 	@Test
 	public void testComputePalindromeStringFromFileOK() {
 		ComputePalindrome validatePalindrome = new ComputePalindrome();
 		String filePath = "./src/test/resources/simplePalindromeOK";
 		String[] result = validatePalindrome.computePalindromeFromFile(filePath);
 		assertEquals("4 9339", result[0]);
 	}
	
	@Test
	public void testComputePalindromeStringFromFileComplexOK() {
		ComputePalindrome validatePalindrome = new ComputePalindrome();
		String filePath = "./src/test/resources/complexPalindromeOK";
		String[] result = validatePalindrome.computePalindromeFromFile(filePath);
		assertEquals("1 11", result[0]);
		assertEquals("4 9339", result[1]);
		assertEquals("2 121", result[2]);
		assertEquals("24 8813200023188", result[2]);
	}
 }
