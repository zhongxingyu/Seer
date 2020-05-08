 package swp_compiler_ss13.fuc.test.m2;
 
 import org.junit.*;
 import swp_compiler_ss13.fuc.lexer.LexerImpl;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import swp_compiler_ss13.fuc.backend.LLVMBackend;
 import swp_compiler_ss13.fuc.ir.IntermediateCodeGeneratorImpl;
 import swp_compiler_ss13.fuc.parser.ParserImpl;
 import swp_compiler_ss13.fuc.semantic_analyser.SemanticAnalyser;
 import swp_compiler_ss13.fuc.test.ExampleProgs;
 import swp_compiler_ss13.fuc.test.TestBase;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 
 /**
  * <p>
  * Runtime tests for the M2 examples.
  * </p>
  * <p>
  * The runtime tests check for results (return values and output) of the
  * execution of the translated examples. The tests require a LLVM installation
  * for executing the LLVM IR. All tests are ignored if no <code>lli</code> is
  * found.
  * </p>
  * <p>
  * All example progs can be found in {@link ExampleProgs}.
  * </p>
  * 
  * @author Jens V. Fischer
  */
 public class M2RuntimeTest extends TestBase {
 
 	private static Logger logger = Logger.getLogger(M2RuntimeTest.class);
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 		Logger.getRootLogger().setLevel(Level.ERROR);
 
 		/* only run tests if lli (dynamic compiler from LLVM) is found */
 		Assume.assumeTrue(checkForLLIInstallation());
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		lexer = new LexerImpl();
 		parser = new ParserImpl();
 		analyser = new SemanticAnalyser();
 		irgen = new IntermediateCodeGeneratorImpl();
 		backend = new LLVMBackend();
 		errlog = new ReportLogImpl();
 	}
 
 	@Test
 	public void testAssignmentProg() throws Exception {
 		testProgRuntime(ExampleProgs.assignmentProg());
 	}
 
 	@Test
 	public void testCondProg() throws Exception {
 		testProgRuntime(ExampleProgs.condProg());
 	}
 
 	@Test
 	public void testPrintProg() throws Exception {
 		testProgRuntime(ExampleProgs.printProg());
 	}
 
 	/* regression test against return bug */
 	@Test
 	public void testReturnProg() throws Exception {
 		testProgRuntime(ExampleProgs.returnProg());
 	}
 
 	@Test
 	@Ignore("fails in Semnatic Analyser")
 	public void testArrayProg1() throws Exception {
 		testProgCompilation(ExampleProgs.arrayProg1());
 	}
 
 	@Test
 	public void testArrayProg2() throws Exception {
 		testProgRuntime(ExampleProgs.arrayProg2());
 	}
 
 	@Test
 	@Ignore("not yet implemented")
 	public void testArrayProg3() throws Exception {
 		testProgRuntime(ExampleProgs.arrayProg3());
 	}
 
 	@Test
 	@Ignore("not yet implemented")
 	public void testReturnBooleProg() throws Exception {
 		testProgRuntime(ExampleProgs.returnBool());
 	}
 
 }
