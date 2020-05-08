 package common.ci.cinta.autotests;
 
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.assertNull;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import common.ci.cinta.utils.CommonUtils;
 
 public class CommonUtilsTest {
 	
 	@SuppressWarnings("unused")
 	@DataProvider(name = "digitCandidates")
 	private Object[][] dpCandidates() {
 		return new Object[][] {
 			{"123fdfdfd", "123"},
 			{"1fdfdfd", "1"},
 			{"fdfdfd1", ""},
 			{"f1dfdfd1", ""},
 			{"232223232323fdfdfd1", "232223232323"},
 			{"555f555f555", "555"},
//			{"f555f555", "555"},
 		};
 	}
 	
 	
 	@Test(dataProvider = "digitCandidates")
 	public void getLeadingDigitsTest(String candidate, String expectedDigit) {
 		String input = String.format("input: %-30s   expectedDigit: %-30s", candidate, expectedDigit);
 		String result = CommonUtils.getLeadingDigits(candidate);
 		System.out.println(String.format("%-60s   result: %-15s", input, result));
 		assertTrue(result.equals(expectedDigit));
 	}
 	
 	@Test
 	public void checkForNullTest() {
 		String input = String.format("input: %-30s   expectedDigit: %-30s", null, null);
 		String result = CommonUtils.getLeadingDigits(null);
 		System.out.println(String.format("%-60s   result: %-15s", input, result));
 		assertNull(result);
 	}
 	
 	
 
 }
