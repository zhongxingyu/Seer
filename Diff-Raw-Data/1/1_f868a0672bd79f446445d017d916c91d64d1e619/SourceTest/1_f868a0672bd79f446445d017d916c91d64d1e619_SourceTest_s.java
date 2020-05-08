 package tests;
 
 import java.io.BufferedReader;
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import junit.framework.TestCase;
 import sparser.SparseSourceRunner;
 
 public class SourceTest extends TestCase {
 
 	public void testBasic() throws Exception {
 		run("basic.sp");
 	}
 
 	public void testDefun() throws Exception {
 		run("defunTest.sp");
 	}
 	
 	public void testDefSpecial() throws Exception {
 		run("defspecialTest.sp");
 	}
 
 	public void testScope() throws Exception {
 		run("scopeTests.sp");
 	}
 
 	public void testArithmetic() throws Exception {
 		run("arithmetic.sp");
 	}
 
 	public void testIf() throws Exception {
 		run("iftest.sp");
 	}
 	
 	private void run(String string) throws FileNotFoundException {
 		String filePath = "testSources/" + string;
 		
 		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 		SparseSourceRunner runner = new SparseSourceRunner(reader);
 		runner.run();
 	}
 }
