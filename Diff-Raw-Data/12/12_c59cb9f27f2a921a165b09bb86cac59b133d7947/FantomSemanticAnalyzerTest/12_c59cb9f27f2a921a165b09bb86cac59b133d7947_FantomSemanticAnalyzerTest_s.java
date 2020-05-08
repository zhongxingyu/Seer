 /*
  * Thibaut Colar Mar 11, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.List;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.structure.FanSemanticAnalyzer;
 import net.jot.testing.JOTTester;
 import org.netbeans.modules.parsing.api.Snapshot;
 
 /**
  *
  * @author thibautc
  */
 public class FantomSemanticAnalyzerTest extends FantomCSLTest
 {
 
 	File failedListFile = null;
 	final String[] badFiles =
 	{
 		"/src/compiler/fan/steps/CheckErrors.fan", // ternary value assignment
 		"/src/compilerJs/fan/runner/Dump.fan", // ?? closure formal indexing ?
 		"/src/flux/fluxText/fan/TextEditor.fan", // it/this ?
 		"/src/testSys/fan/ProcessTest.fan", // closure implicit "it" var
 		"/src/testSys/fan/RangeTest.fan", // Range like 0..1 
 		"/src/testSys/fan/ClosureTest.fan", // closure 'it' on local function call
 	};
 	
 	final String[] ignoredFiles =
 	{
 		// skip those files (known errors)
 		"/src/compilerJs/fan/runner/TestRunner.fan", // javax.script with java 1.5
 		"/src/compilerJs/fan/runner/Runner.fan" // javax.script with java 1.5
 	};
 
 	public void cslTest() throws Throwable
 	{
 		String fanHome = prefs.getString("fantom.home");
		// Outputs the names of the failed files into a text file, ina  format that makes it easy to use in a java string array
 		failedListFile = new File(prefs.getString("test.home") + File.separator + "failed.txt");
 		failedListFile.delete();
 
 		testAllFanFilesUnder(fanHome + "/src/");
 
 		/*for (String file : badFiles)
 		{
 			testFile(new File(fanHome, file));
 		}*/
 	}
 
 	private void testAllFanFilesUnder(String folderPath) throws Exception
 	{
 		List<File> files = NBTestUtilities.listAllFanFilesUnder(folderPath);
 		for (File f : files)
 		{
 			boolean skip = false;
 			for (String p : ignoredFiles)
 			{
 				if (f.getPath().endsWith(p))
 				{
 					skip = true;
 				}
 			}
 			if (!skip)
 			{
 				testFile(f);
 			}
 		}
 	}
 
 	private void testFile(File f) throws Exception
 	{
 		FanParserTask task = null;
 		try
 		{
 			Snapshot snap = NBTestUtilities.fileToSnapshot(f);
 			task = new FanParserTask(snap);
 			boolean hasErr = analyze(task);
 			if (hasErr)
 			{
 				documentFailedFile(task.getSourceFile().getPath());
 			}
 
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			JOTTester.checkIf("Exception during semantic analysis " + f.getPath(), false);
 			if (task != null)
 			{
 				documentFailedFile(task.getSourceFile().getPath());
 			}
 		}
 	}
 
 	public static boolean analyze(FanParserTask task) throws Exception
 	{
 		task.parse();
 		task.parseGlobalScope();
 		task.parseLocalScopes();
 		FanSemanticAnalyzer analyzer = new FanSemanticAnalyzer();
 		analyzer.run(task, null);
 		boolean hasErr = false;
 		if (task.getDiagnostics().size() > 0)
 		{
 			for (org.netbeans.modules.csl.api.Error err : task.getDiagnostics())
 			{
 				String desc = err.getDisplayName();
 				// Avoid known false positives (we don't index java.awt & javax.swing by default)
 				if (!desc.contains("java.awt") && !desc.contains("javax.swing") && !desc.contains("javax.script") && !desc.contains("ScriptEngine"))
 				{
 					hasErr = true;
 					System.err.println("Error: " + err);
 				}
 			}
 		}
 		JOTTester.checkIf("Semantic errors in " + task.getSourceFile().getPath(), !hasErr);
 		return hasErr;
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomSemanticAnalyzerTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 
 	private void documentFailedFile(String path) throws Exception
 	{
 		FileOutputStream fis = new FileOutputStream(failedListFile, true);
 		String txt = "\t\t\"" + path + "\",\n";
 		fis.write(txt.getBytes());
 		fis.close();
 	}
 }
