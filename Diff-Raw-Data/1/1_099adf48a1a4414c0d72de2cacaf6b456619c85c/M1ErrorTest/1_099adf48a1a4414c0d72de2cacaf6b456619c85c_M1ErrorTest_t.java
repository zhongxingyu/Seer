 package swp_compiler_ss13.fuc.test.m1;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 import swp_compiler_ss13.fuc.lexer.LexerImpl;
 import swp_compiler_ss13.fuc.parser.ParserImpl;
 import swp_compiler_ss13.fuc.test.ExampleProgs;
 import swp_compiler_ss13.fuc.test.TestBase;
import org.junit.Ignore;
 
 /**
  * <p>
  * Test for the M1 examples producing an expected error.
  * </p>
  * <p>
  * All example progs can be found in {@link ExampleProgs}.
  * </p>
  * 
  * @author Jens V. Fischer
  */
 public class M1ErrorTest extends TestBase {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		Logger.getRootLogger().setLevel(Level.INFO);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		lexer = new LexerImpl();
 		parser = new ParserImpl();
 		analyser = new swp_compiler_ss13.fuc.semantic_analyser.SemanticAnalyser();
 		errlog = new ReportLogImpl();
 	}
 
 
 	@Test
 	public void testDoubleDeclaration() throws Exception {
 		testProgForErrorMsg(ExampleProgs.doubleDeclaration());
 	}
 
 	@Test
 	public void testInvalidIds() throws Exception {
 		testProgForErrorMsg(ExampleProgs.invalidIds());
 	}
 
 	@Test
 	public void testMultipleMinusENotation() throws Exception {
 		testProgForErrorMsg(ExampleProgs.multipleMinusENotation());
 	}
 
   @Ignore
 	@Test
 	public void testMultiplePlusesInExp() throws Exception {
 		testProgForErrorMsg(ExampleProgs.multiplePlusesInExp());
 	}
 
 	@Test
 	public void testUndefReturn() throws Exception {
 		testProgForErrorMsg(ExampleProgs.undefReturnProg());
 	}
 
 }
