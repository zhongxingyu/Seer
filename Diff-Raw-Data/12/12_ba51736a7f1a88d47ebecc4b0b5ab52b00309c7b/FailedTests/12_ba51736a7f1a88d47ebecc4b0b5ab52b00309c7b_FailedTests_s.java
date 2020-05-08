 import java.io.IOException;
 
 import org.instructionexecutor.FunRbTreeTest;
 import org.junit.Test;
 import org.suite.Suite;
 
 public class FailedTests {
 
 	// Type check take 11 seconds
 	@Test
 	public void test0() throws IOException {
 		new FunRbTreeTest().test();
 	}
 
 	// Strange error message "Unknown expression if b"
 	@Test
 	public void test1() throws IOException {
 		Suite.evaluateFun("if a then b", false);
 	}
 
 }
