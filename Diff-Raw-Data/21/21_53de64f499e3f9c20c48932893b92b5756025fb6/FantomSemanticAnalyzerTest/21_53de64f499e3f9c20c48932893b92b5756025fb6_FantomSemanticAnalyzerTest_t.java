 /*
  * Thibaut Colar Mar 11, 2010
  */
 
 package net.colar.netbeans.fan.test;
 
 import java.io.File;
 import java.util.List;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.structure.FanSemanticAnalyzer;
 import net.jot.testing.JOTTester;
 
 /**
  *
  * @author thibautc
  */
 public class FantomSemanticAnalyzerTest extends FantomCSLTest
 {
 	public void cslTest() throws Throwable
 	{
 		testAllFanFilesUnder(FantomParserTest.FAN_HOME + "/examples/");
 		testAllFanFilesUnder(FantomParserTest.FAN_HOME + "/src/");
 	}
 
 	private static void testAllFanFilesUnder(String folderPath) throws Exception
 	{
 		List<File> files = NBTestUtilities.listAllFanFilesUnder(folderPath);
 		for (File f : files)
 		{
 			Exception e = null;
 			try
 			{
 				FanParserTask task = FantomStructureAnalyzerTest.analyzeFile(f);
 				analyze(task);
 
 			} catch (Exception ex)
 			{
 				e = ex;
 				e.printStackTrace();
 			}
 			JOTTester.checkIf("SemanticAnalysis of: " + f.getAbsolutePath(), e == null);
 		}
 	}
 
 	public static void analyze(FanParserTask task)
 	{
 		FanSemanticAnalyzer analyzer = new FanSemanticAnalyzer();
 		//SchedulerEvent evt = new SchedulerEvent(task);
 		analyzer.run(task, null);
 		//TODO: check for errors.
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
 }
