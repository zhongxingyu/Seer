 import java.io.IOException;
 
 import org.junit.Test;
 
 import suite.fp.eval.FunRbTreeTest;
import suite.instructionexecutor.InstructionTranslatorTest;
 
 public class FailedTests {
 
 	// Type check take 11 seconds
 	@Test
 	public void test0() throws IOException {
 		new FunRbTreeTest().test();
 	}
 
	// NullPointerException
 	@Test
	public void test1() throws IOException {
		new InstructionTranslatorTest().testStandardLibrary();
 	}
 
 }
