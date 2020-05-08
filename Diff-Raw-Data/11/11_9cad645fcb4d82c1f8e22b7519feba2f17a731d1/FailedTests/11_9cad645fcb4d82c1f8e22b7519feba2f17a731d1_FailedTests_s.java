 import java.io.IOException;
 
 import org.junit.Test;
 
import suite.Suite;
 import suite.fp.eval.FunRbTreeTest;
 
 public class FailedTests {
 
 	// Type check take 11 seconds
 	@Test
 	public void test0() throws IOException {
 		new FunRbTreeTest().test();
 	}
 
	// Strange error message "Unknown expression (temp$$0 => temp$$0 {})"
 	@Test
	public void test2() throws IOException {
		Suite.evaluateFun("`{}`", false);
 	}
 
 }
