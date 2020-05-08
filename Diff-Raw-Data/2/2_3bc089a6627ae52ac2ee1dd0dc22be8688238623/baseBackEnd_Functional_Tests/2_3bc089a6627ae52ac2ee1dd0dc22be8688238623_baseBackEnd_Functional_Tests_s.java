 package tests;
 
 import java.util.*;
 
 import net.sourceforge.jwebunit.junit.WebTester;
 
 import org.json.simple.JSONObject;
 import org.junit.*;
 
 import static net.sourceforge.jwebunit.junit.JWebUnit.*; 
 
 /* 
 	These are the back end functional tests
 	They include testing the actual output of particular inputs in order to test the check.jsp functionality
 	i.e. A test for the page source section will check not just that SOMETHING is there after upload (as public void Assert_Page_Source() does),
 		 but that the source code from the original file is uploaded. (and then various checks on the correct highlighting of errors.
 
 
  */
 
 
 // This class includes all tag checks, is missing the checks on attribute particulars
 public class baseBackEnd_Functional_Tests {
 
 
 
 
 
 	// Nested classes of required, singular and self-closing elements
 
 	// for each of the functions in this class check that code with multiple of them correctly displays the isSingular error
 	public static class Singular_Tags {
 
 		@Test
 		public void Check_Singular_Doctype() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one doctype declaration in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(2, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		@Test
 		public void Check_Singular_html() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one html element in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(3, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		@Test
 		public void Check_Singular_head() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<head>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one head element in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(6, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));			
 		}
 
 		@Test
 		public void Check_Singular_body() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one body element in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		@Test
 		public void Check_Singular_title() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("<title>Test 2</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one title element in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(5, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		@Test
 		public void Check_Singular_base() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("<base href=\"http://www.test.com/\" />");
 			testingSource.add("<base href=\"http://www.test2.com/\" />");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one base element in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(6, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 		@Test
 		public void Check_Singular_main() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<main>");
 			testingSource.add("</main>");
 			testingSource.add("<main>");
 			testingSource.add("</main>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("There should only be one main element in the document", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 	}
 
 	// All of the functions in the following class should check whether ? (self closing is done or not done? Recommend which one?)
 	public static class Self_Closing_Tags {
 
 		@Test
 		public void Check_Closing_base() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<base href=\"http://www.test.com\">");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("base tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 
 		}
 
 		@Test
 		public void Check_Closing_base_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<base href=\"http://www.test.com\" />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 
 		}
 
 		@Test
 		public void Check_Closing_link() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<link href=\"test.css\">");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("link tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		@Test
 		public void Check_Closing_link_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<link href=\"test.css\" />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_Closing_meta() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("<meta charset=\"utf-8\">");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("meta tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(5, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 		@Test
 		public void Check_Closing_meta_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("<meta charset=\"utf-8\" />");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));		
 		}
 
 		@Test
 		public void Check_Closing_hr() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<hr>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("hr tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));	
 		}
 
 		@Test
 		public void Check_Closing_hr_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<hr />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_Closing_br() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<br>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("br tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 		@Test
 		public void Check_Closing_br_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<br />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_Closing_wbr() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<wbr>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("wbr tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 		@Test
 		public void Check_Closing_wbr_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<wbr />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));		
 		}
 
 		@Test
 		public void Check_Closing_img() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<img src=\"test.jpg\" alt=\"test\">");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("img tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 		@Test
 		public void Check_Closing_img_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<img src=\"test.jpg\" alt=\"test\" />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));	
 		}
 
 		@Test
 		public void Check_Closing_param() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<object data=\"test.swf\" type=\"application/x-shockwave-flash\">");
 			testingSource.add("<param name=\"test\">");
 			testingSource.add("</object>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("param tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));		
 		}
 
 		@Test
 		public void Check_Closing_param_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<object data=\"test.swf\" type=\"application/x-shockwave-flash\">");
 			testingSource.add("<param name=\"test\" />");
 			testingSource.add("</object>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_Closing_source() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<video>");
 			testingSource.add("<source src=\"test.mov\">");
 			testingSource.add("</video>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("source tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));				
 		}
 
 		@Test
 		public void Check_Closing_source_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<video>");
 			testingSource.add("<source src=\"test.mov\" />");
 			testingSource.add("</video>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));				
 		}
 
 		@Test
 		public void Check_Closing_track() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<video>");
 			testingSource.add("<track src=\"test.srt\">");
 			testingSource.add("</video>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("track tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));			
 		}
 
 		@Test
 		public void Check_Closing_track_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<video>");
 			testingSource.add("<track src=\"test.srt\" />");
 			testingSource.add("</video>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));		
 		}
 
 		@Test
 		public void Check_Closing_input() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form action=\"test.php\">");
 			testingSource.add("<input type=\"text\">");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("input tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));			
 		}
 
 		@Test
 		public void Check_Closing_input_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form action=\"test.php\">");
 			testingSource.add("<input type=\"text\" />");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));			
 		}
 
 		@Test
 		public void Check_Closing_keygen() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form action=\"test.php\">");
 			testingSource.add("<keygen name=\"test\">");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("keygen tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));			
 		}
 
 		@Test
 		public void Check_Closing_keygen_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form action=\"test.php\">");
 			testingSource.add("<keygen name=\"test\" />");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));		
 		}
 
 		@Test
 		public void Check_Closing_menuitem() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<menu label=\"test\">");
 			testingSource.add("<menuitem type=\"command\">");
 			testingSource.add("</menu>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("menuitem tags should be self-closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));			
 		}
 
 		@Test
 		public void Check_Closing_menuitem_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<menu label=\"test\">");
 			testingSource.add("<menuitem type=\"command\" />");
 			testingSource.add("</menu>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));		
 		}
 		
 
 		// Check a couple of elements that are not self closing (i.e. upload file that does not close an element properly, check error is returned)
 		@Test
 		public void Check_Not_Self_Closing() {
 
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<p />");
 			testingSource.add("<span />");
 			testingSource.add("<table />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(3, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<p> is not allowed to self close", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<span> is not allowed to self close", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("3")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<table> is not allowed to self close", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("3")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("3")).get("line"));
 		}
 	}
 
 	public static class Required_Tags {
 
 		// Check that a doctype error is both returned and not returned (i.e when a file with this error and without this error are uploaded)
 		@Test
 		public void Check_Exists_Doctype() {
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("First element should be doctype", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(1, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 
 		}
 
 		// Check that there is an error for not having any html tags
 		@Test
 		public void Check_Exists_html() {
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("You are missing html tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(2, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 
 		}
 
 		// Check that there is an error for not having any head tags
 		@Test
 		public void Check_Exists_head() {
 
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("You are missing head tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(3, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 
 		}
 
 		// Check that there is an error for not having any body tags
 		@Test
 		public void Check_Exists_body() {
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("You are missing body tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(6, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		// Check that there is a warning for not having any title tags
 		@Test
 		public void Check_Exists_title() {
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message is stored
 			Assert.assertEquals("You are missing title tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert error is on correct line
 			Assert.assertEquals(4, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 		}
 
 		// A file with all required tags to ensure no unexpected errors are present.
 		@Test
 		public void Check_Control_required_tags() {
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		// A file with all required tags to ensure no unexpected errors are present.
 		@Test
 		public void Check_multiple_missing_required() {
 
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(2, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for first error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("type"));
 			//assert correct error message for first error is stored
 			Assert.assertEquals("First element should be doctype", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("message"));
 			//assert first error is on correct line
 			Assert.assertEquals(1, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("0")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("You are missing title tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(3, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 	}
 
 	public static class Nonexistent_Tags {
 
 		// Test the error associated with using nonexistent tags
 		@Test
 		public void Check_Non_Existing_Tags1() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<nonexistent> </nonexistent>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("You are using a nonexistent tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		// Test the error associated with using nonexistent tags
 		@Test
 		public void Check_Non_Existing_Tags2() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<anotherone> </anotherone>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("You are using a nonexistent tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		// Test the error associated with using nonexistent tags
 		@Test
 		public void Check_Non_Existing_Tags3() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<athirdtag> </athirdtag>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("You are using a nonexistent tag", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 	}
 
 	// Test the warning/error associated with deprecated tags
 	public static class Deprecated_Tags {
 
 		@Test
 		public void Check_Deprecated_acronym() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<acronym> </acronym>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<acronym> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_applet() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<applet> </applet>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<applet> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_basefont() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<basefont> </basefont>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<basefont> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_big() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<big> </big>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<big> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_blackface() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<blackface> </blackface>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<blackface> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_center() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<center> </center>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<center> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_dir() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<dir> </dir>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<dir> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_font() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<font> </font>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<font> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_frame() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<frame> </frame>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<frame> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_frameset() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<frameset> </frameset>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<frameset> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_isindex() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<isindex> </isindex>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<isindex> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_noframe() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<noframe> </noframe>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<noframe> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_strike() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<strike> </strike>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<strike> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_tt() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tt> </tt>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tt> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Deprecated_xmp() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<xmp> </xmp>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<xmp> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 	}
 
 	// Check that statements in comments are not checked for errors (by passing in errors within comment tags)
 	public static class Check_Comments {
 
 
 		@Test
 		public void Check_Deprecated_inComments() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<xmp> </xmp>");
 			testingSource.add("<!--");
 			testingSource.add("<xmp> </xmp>");
 			testingSource.add("-->");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<xmp> is deprecated", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Singular_inComments() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<p>Test</p>");
 			testingSource.add("<!--");
 			testingSource.add("<html> </html>");
 			testingSource.add("-->");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 		
 
 
 		@Test
 		public void Check_Self_Closing_inComments() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<p>Test</p>");
 			testingSource.add("<!--");
 			testingSource.add("<br>");
 			testingSource.add("-->");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 		
 
 
 		@Test
 		public void Check_Closing_inComments() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<p>Test</p>");
 			testingSource.add("<!--");
 			testingSource.add("</body>");
 			testingSource.add("-->");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<body> is not closed", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(11, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 	}
 
 	public static class Form_Elements {
 
 		@Test
 		public void Check_button_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<button type=\"button\">test</button>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<button> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_button_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<button type=\"button\">test</button>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_datalist_option_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<datalist>");
 			testingSource.add("<option value=\"test\">test</option>");
 			testingSource.add("</datalist>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(2, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<datalist> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<option> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 		}
 
 		@Test
 		public void Check_datalist_option_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<datalist>");
 			testingSource.add("<option value=\"test\">test</option>");
 			testingSource.add("</datalist>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_fieldset_legend_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<fieldset>");
 			testingSource.add("<legend>Test</legend>");
 			testingSource.add("</fieldset>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(2, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<fieldset> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<legend> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 		}
 
 		@Test
 		public void Check_fieldset_legend_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<fieldset>");
 			testingSource.add("<legend>Test</legend>");
 			testingSource.add("</fieldset>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_label_input_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<label for=\"test\">Test:</label>");
 			testingSource.add("<input type=\"text\" id=\"test\" />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(2, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<label> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<input> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 		}
 
 		@Test
 		public void Check_label_input_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<label for=\"test\">Test:</label>");
 			testingSource.add("<input type=\"text\" id=\"test\" />");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_keygen_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<keygen name=\"test\" />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<keygen> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_keygen_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<keygen name=\"test\" />");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_meter_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<meter value=\"1\" />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<meter> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_meter_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<meter value=\"1\" />");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_optgroup_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<optgroup label=\"test\" />");
 			testingSource.add("<option value=\"testval\">test</option>");
 			testingSource.add("</optgroup>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(2, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<optgroup> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<option> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 		}
 
 		@Test
 		public void Check_optgroup_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<optgroup label=\"test\" />");
 			testingSource.add("<option value=\"testval\">test</option>");
 			testingSource.add("</optgroup>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_output_input_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<input type=\"range\" id=\"a\" value=\"50\" />100");
 			testingSource.add("+<input type=\"number\" id=\"b\" value=\"50\" />");
 			testingSource.add("=<output name=\"x\" for=\"a b\"></output>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(3, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<input> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<input> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("3")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<output> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("3")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("3")).get("line"));
 		}
 
 		@Test
 		public void Check_output_input_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<input type=\"range\" id=\"a\" value=\"50\" />100");
 			testingSource.add("+<input type=\"number\" id=\"b\" value=\"50\" />");
 			testingSource.add("=<output name=\"x\" for=\"a b\"></output>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_progress_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<progress value=\"1\" max=\"20\"></progress>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<progress> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_progress_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<progress value=\"1\" max=\"20\"></progress>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_select_option_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<select>");
 			testingSource.add("<option value=\"test\">test</option>");
 			testingSource.add("</select>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(2, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<select> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<option> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("2")).get("line"));
 		}
 
 		@Test
 		public void Check_select_option_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<select>");
 			testingSource.add("<option value=\"test\">test</option>");
 			testingSource.add("</select>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 		@Test
 		public void Check_textarea_in_Form() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<textarea rows=\"4\" cols=\"10\">");
 			testingSource.add("Some testing text");
 			testingSource.add("</textarea>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<textarea> should be inside a <form>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_textarea_in_Form_control() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<form>");
 			testingSource.add("<textarea rows=\"4\" cols=\"10\">");
 			testingSource.add("Some testing text");
 			testingSource.add("</textarea>");
 			testingSource.add("</form>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 
 	}
 
 	// Check various properties of table elements
 	public static class Table_Elements {
 		
 		@Test
 		public void Check_Caption_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<caption>Test caption</caption>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<caption> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Colgroup_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<colgroup></colgroup>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<colgroup> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Col_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<col />");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("warning", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<col> should be inside a <colgroup> element, within a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Tbody_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tbody></tbody>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tbody> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Thead_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<thead></thead>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<thead> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Tfoot_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tfoot></tfoot>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tfoot> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Tr_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tr></tr>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tr> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Td_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<td></td>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<td> should be inside a <tr> element, within a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Th_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<th></th>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<th> should be inside a <tr> element, within a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 
 		@Test
 		public void Check_Col_In_Colgroup() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<col />");
 			testingSource.add("<table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<col> should be inside a <colgroup> element, within a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Td_In_Tr() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<td></td>");
 			testingSource.add("<table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<td> should be inside a <tr> element, within a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}		
 		
 		@Test
 		public void Check_Th_In_Tr() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<th></th>");
 			testingSource.add("<table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<th> should be inside a <tr> element, within a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}	
 		
 		@Test
 		public void Check_Colgroup_With_Content_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<colgroup>");
 			testingSource.add("<col />");
 			testingSource.add("</colgroup>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<colgroup> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Tbody_With_Content_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tbody>");
 			testingSource.add("<tr></tr>");
 			testingSource.add("</tbody>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tbody> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Thead_With_Content_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<thead>");
 			testingSource.add("<tr></tr>");
 			testingSource.add("</thead>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<thead> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Tfoot_With_Content_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tbody>");
 			testingSource.add("<tr></tr>");
 			testingSource.add("</tbody>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tfoot> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Tr_With_Content_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<tr>");
 			testingSource.add("<th></th>");
 			testingSource.add("</tr>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<tr> should be inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Caption_Position_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<thead>");
 			testingSource.add("</thead>");
 			testingSource.add("<caption>Test caption</caption>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<caption> should be the first element inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(10, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Colgroup_Position_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<thead>");
 			testingSource.add("</thead>");
 			testingSource.add("<colgroup></colgroup>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<colgroup> should be before any <thead>, <tbody>, <tfoot>, <tr> or <th> element inside a <table>", 
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(10, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Thead_Position_In_Table() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<tbody>");
 			testingSource.add("</tbody>");
 			testingSource.add("<thead>");
 			testingSource.add("</thead>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<thead> should be before any <tbody>, <tfoot>, <tr> or <th> element inside a <table>", 
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(10, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Singular_Caption() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<caption>Test caption 1</caption>");
 			testingSource.add("<caption>Test caption 2</caption>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("There should only be one <caption> element inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Singular_Colgroup() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<colgroup></colgroup>");
 			testingSource.add("<colgroup></colgroup>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("There should only be one <colgroup> element inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Singular_Tbody() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<tbody></tbody>");
 			testingSource.add("<tbody></tbody>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("There should only be one <tbody> element inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Singular_Thead() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<thead></thead>");
 			testingSource.add("<thead></thead>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("There should only be one <thead> element inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Singular_Tfoot() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<tfoot></tfoot>");
 			testingSource.add("<tfoot></tfoot>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("There should only be one <tfoot> element inside a <table>", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(9, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Multiple_Tables_With_Singular_Tags() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<table>");
 			testingSource.add("<caption>Test Table 1</caption>");
 			testingSource.add("<colgroup></colgroup>");
 			testingSource.add("<thead></thead>");
 			testingSource.add("<tbody></tbody>");
 			testingSource.add("<tfoot></tfoot>");
 			testingSource.add("</table>");
 			testingSource.add("<table>");
 			testingSource.add("<caption>Test Table 2</caption>");
 			testingSource.add("<colgroup></colgroup>");
 			testingSource.add("<thead></thead>");
 			testingSource.add("<tbody></tbody>");
 			testingSource.add("<tfoot></tfoot>");
 			testingSource.add("</table>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(0, ((JSONObject) testingResult.get("errors")).get("count"));
 		}
 	}
 	
 	// This class includes all sub-element tag checks
 	public static class Nested_SubElements {
 		
 		@Test
 		public void Check_Option_In_Select() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<option>");
 			testingSource.add("</option>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<option> should be nested inside <select> element", 
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		@Test
 		public void Check_Li_In_Ul() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<li>");
 			testingSource.add("</li>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<li> should be nested inside <ul> element", 
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		@Test
 		public void Check_Li_in_Ol() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<li>");
 			testingSource.add("</li>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<li> should be nested inside <ol> element",  
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		//Descriptor Tags
 		@Test
 		public void Check_Dt_in_Dl() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<dt>Coffee</dt>");
 			testingSource.add("<dd>Black hot drink</dd>");
 			testingSource.add("<dt>Milk</dt>");
 			testingSource.add("<dd>White cold drink</dd>");
 			testingSource.add("</body>");
 			testingSource.add("</html>");
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<dt> should be nested inside <dl> element",  
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(7, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 		//Descriptor tag check
 		@Test
 		public void Check_Dd_in_Dt() {
 			List<String> testingSource = new ArrayList<String>();
 
 			//create html input to error check
 			testingSource.add("<!DOCTYPE html>");
 			testingSource.add("<html>");
 			testingSource.add("<head>");
 			testingSource.add("<title>Just a test</title>");
 			testingSource.add("</head>");
 			testingSource.add("<body>");
 			testingSource.add("<dl>");
 			testingSource.add("<dd>Black hot drink</dd>");
 			testingSource.add("<dd>White cold drink</dd>");
 			testingSource.add("</dl>");
 			testingSource.add("</body>");
			testingSource.add("</html>")
 
 			JSONObject testingResult = Check.findErrors(testingSource);
 
 			//assert correct number of lines are stored
 			Assert.assertEquals(testingSource.size(), ((JSONObject) testingResult.get("source")).get("length"));
 			//assert correct number of errors are stored
 			Assert.assertEquals(1, ((JSONObject) testingResult.get("errors")).get("count"));
 
 			//assert correct error type for second error is stored
 			Assert.assertEquals("syntax", ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("type"));
 			//assert correct error message for second error is stored
 			Assert.assertEquals("<dd> should be nested inside <dt> sub - element",  
 					((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("message"));
 			//assert second error is on correct line
 			Assert.assertEquals(8, ((JSONObject) ((JSONObject) testingResult.get("errors")).get("1")).get("line"));
 		}
 		
 	}
 }
