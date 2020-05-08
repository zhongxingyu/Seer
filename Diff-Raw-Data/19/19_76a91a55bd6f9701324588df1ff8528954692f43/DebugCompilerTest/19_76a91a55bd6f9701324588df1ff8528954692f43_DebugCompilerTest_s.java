 package org.strategoxt.debug.core;
 
 import java.io.IOException;
 
 import org.StrategoFileManager;
 import org.strategoxt.debug.core.util.DebugCompiler;
 
 public class DebugCompilerTest {
 
	public static final String WORKING_DIR = StrategoFileManager.BASE + "/working";
 	
 	public static void main(String[] args) {
 		//String inputBase = StrategoFileManager.BASE;
 		String inputFilePath = "src/stratego/localvar/localvar.str";
 		//String strategoFilePath = inputBase + "/" + inputFilename;
 		String projectName = "localvar";
		DebugCompiler debugCompiler = new DebugCompiler(WORKING_DIR);
 		// mkdir localvar/stratego in workingdir
 		// mkdir localvar/java
 		// mkdir localvar/class
 		String binBase = null;
 		boolean compileSucces = false;
 		try {
 			binBase = debugCompiler.debugCompile(StrategoFileManager.BASE, inputFilePath, projectName);
 			compileSucces = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		boolean runjava = true;
 		// run .class
 		if (runjava && compileSucces)
 		{
 			String input = StrategoFileManager.BASE + "/src/strategies/terms/test.prog";
 			String argsForMainClass = "-i " + input;
 			String mainClass = "localvar.localvar";
 			String mainArgs = mainClass + " " + argsForMainClass;
 			String cp = strategoxtjar + ":" + libstrategodebuglib + ":" + strjdebugruntime + ":" + binBase;
 			String classpath = cp;
 			org.strategoxt.debug.core.util.Runner.run(projectName, mainArgs, classpath);
 		}
 	}
 	
 	private static String strj_dbg_core = "/home/rlindeman/workspace/strj-dbg-core";
 
 	private static String strategoxtjar = strj_dbg_core + "/lib/strategoxt.jar";
 	private static String libstrategodebuglib = strj_dbg_core + "/lib/strj_dbg_runtime_lib.jar";
 	private static String strjdebugruntime = strj_dbg_core + "/lib/strj_dbg_runtime_lib-java.jar";
 }
