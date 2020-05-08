 package swp_compiler_ss13.fuc.test.base;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import junit.extensions.PA;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.backend.Backend;
 import swp_compiler_ss13.common.backend.BackendException;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.parser.Parser;
 import swp_compiler_ss13.fuc.backend.TACExecutor;
import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 
 /**
  * Runtime tests base class. The tests runtime tests require a LLVM installation
  * for executing the LLVM IR. All tests are ignored if no <code>lli</code> is
  * found.
  * 
  * @author Jens V. Fischer
  */
 public abstract class RuntimeTestBase {
 
 	protected static Lexer lexer;
 	protected static Parser parser;
 	protected static IntermediateCodeGenerator irgen;
 	protected static Backend backend;
 	protected static ReportLogImpl errlog;
 	protected static Logger logger = Logger.getLogger(RuntimeTestBase.class);
 
 
 	/*
 	 * Check if lli is correctly installed.
 	 */
 	protected static boolean checkForLLIInstallation() {
 
 		Level level = Logger.getRootLogger().getLevel();
 
 		Logger.getRootLogger().setLevel(Level.FATAL);
 		boolean hasLLI;
 		try {
 			PA.invokeMethod(TACExecutor.class, "tryToStartLLI()");
 			hasLLI = true;
 		} catch (Exception e) {
 			hasLLI = false;
 		}
 
 		Logger.getRootLogger().setLevel(level);
 
 		if (!hasLLI) {
 			logger.warn("Runtime tests are ignored, because of missing LLVM lli installation.");
 			String infoMsg = "If you have LLVM installed you might need to check your $PATH: "
 					+ "Intellij IDEA: Run -> Edit Configurations -> Environment variables; "
 					+ "Eclipse: Run Configurations -> Environment; " + "Shell: Check $PATH";
 			logger.info(infoMsg);
 		}
 		return hasLLI;
 	}
 
 	protected TACExecutor.ExecutionResult compileAndExecute(String prog) throws BackendException,
 			IntermediateCodeGeneratorException, IOException, InterruptedException {
 		lexer.setSourceStream(new ByteArrayInputStream(prog.getBytes("UTF-8")));
 		parser.setLexer(lexer);
 		parser.setReportLog(errlog);
 		AST ast = parser.getParsedAST();
 		List<Quadruple> tac = irgen.generateIntermediateCode(ast);
 		Map<String, InputStream> targets = backend.generateTargetCode("", tac);
 		InputStream irCode = targets.get(".ll");
 		return TACExecutor.runIR(irCode);
 	}
 
 }
