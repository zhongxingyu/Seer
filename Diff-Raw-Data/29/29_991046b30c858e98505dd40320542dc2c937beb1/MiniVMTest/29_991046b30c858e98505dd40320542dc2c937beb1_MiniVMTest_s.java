 package Test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import MiniRE.AST;
 import MiniRE.RecursiveDescent.MiniParser;
 import MiniRE.VirtualMachine.MiniVM;
 
 public class MiniVMTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void testMiniVM() {
 		MiniVM mini = new MiniVM();
 		MiniParser parser = new MiniParser();
 		
		AST ast = parser.parseFile("test/phase-ii/checkReplaceScript.txt");
 		
 		try {
 			mini.run(ast);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testRun() {
 		fail("Not yet implemented");
 	}
 
 }
