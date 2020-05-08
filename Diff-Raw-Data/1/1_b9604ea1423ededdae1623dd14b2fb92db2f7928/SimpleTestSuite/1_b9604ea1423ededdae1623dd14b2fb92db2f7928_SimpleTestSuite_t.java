 public class SimpleTestSuite extends ClassTester {
 	public static void main(String[] args) throws Exception {
 
 		createTestsConfiguration("SimpleTestSuite");
 		addTestsSuite("SimpleTestSuiteSub1");
 		addTestsSuite("SimpleTestSuiteSub2");
 		addTestsSuite("SimpleTestSuiteSub3");
 
 		lauchTests();
 	}
 }
