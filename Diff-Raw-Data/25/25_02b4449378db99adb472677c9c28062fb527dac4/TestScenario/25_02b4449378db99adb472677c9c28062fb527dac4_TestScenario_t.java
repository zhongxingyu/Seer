 package testutils;
 
 import jargs.gnu.CmdLineParser;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 
 import testutils.exceptions.FatalErrorException;
 import testutils.tester.CompileSuite;
 import testutils.tester.FileInFolderIsSingleUnitSuite;
 import testutils.tester.FilesInFolderIsUnitSuite;
 import testutils.tester.ReportManager;
 import testutils.tester.SemanticReport;
 import testutils.tester.SimpleReport;
 import testutils.tester.SingleFileNamesSuite;
 import testutils.tester.TestSuiteView;
 
 /**
  * Runs testfiles in the pt-compiler/PTFrontend/test folder.
  * 
  * A test instance is either a file (located in test/single_file) 
  * or a folder containing multiple interdependent files (located in test/multiple_files).
  * 
  * A TestCase is a semantic check or a semantic check and compilation attempt of an instance.
  * Compilation output is placed in PTFrontend/debug
  * 
  * It's possible to run a single TestCase or all TestCases in the test/ folder.
  * 
  * See the following build.xml targets: testall, testcompile, testsingle
  *
  */
 public class TestScenario {
 
 	private boolean verbose;
 	private String singleFileFolder;
 	private String multipleFileFolder;
 	private List<TestSuiteView> suites;
 	private ReportManager testReports;
 	private String[] singleTestNames;
 	private String compileTestsOutputFolder;
 	private boolean compileFlag;
 
 	public static void main(String[] args) {
 		try {
 			TestScenario scenario = parseArgsAndInstantiate(args);
 			scenario.createTestSuites();
 			scenario.runSuites();
 			if (scenario.compileFlag)
 				scenario.testCompileSelected();
 			System.out.println(scenario.presentResults());
 		} catch (FatalErrorException e) {
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	private void testCompileSelected() {
 		CompileSuite suite = new CompileSuite(compileTestsOutputFolder,
 				testReports.getCompilableTests());
 		suite.runSuite(testReports);
 	}
 
 	private String presentResults() {
 		StringBuffer sb = new StringBuffer();
 		if (testReports.allPassed()) {
 			sb.append("*** All " + testReports.getNumberOfTestsTotal()
 					+ " tests passed.\n");
 		} else {
 			int passed = testReports.getNumberOfPassedTests();
 			int total = testReports.getNumberOfTestsTotal();
 			int failed = testReports.getNumberOfFailedTests();
			
			for (SemanticReport failBlog : testReports.getFailedReports()) {
				sb.append(failBlog.errorMsgs + "\n");
			}
			
 			sb.append(String.format(
 					"\n*** %d of %d tests passed, %d failed.\n", passed, total,
 					failed));
 			sb.append("The following files did not pass as expected:\n");
 			for (SimpleReport report : testReports.getFailedCompilations()) {
 				String output = String.format("  compilation failed for %s\n",
 						report.getPath());
 				sb.append(output);
 			}
			
 			for (SemanticReport failBlog : testReports.getFailedReports()) {
 				sb.append("    ant testsingle -Dname=" + failBlog.getPath()
 						+ "\n");
 			}
			
 		}
 		return sb.toString();
 	}
 
 	private void createTestSuites() {
 		suites = new LinkedList<TestSuiteView>();
 		if (multipleFileFolder != null) {
 			suites.add(new FilesInFolderIsUnitSuite(multipleFileFolder));
 		}
 		if (singleFileFolder != null) {
 			suites.add(new FileInFolderIsSingleUnitSuite(singleFileFolder));
 		}
 		for (int i = 0; i < singleTestNames.length; i++) {
 			String name = singleTestNames[i];
 			File f = new File(name);
 			if (f.isDirectory())
 				suites.add(new FilesInFolderIsUnitSuite(name));
 			else
 				suites.add(new SingleFileNamesSuite(name));
 		}
 	}
 
 	private void runSuites() {
 		testReports = new ReportManager();
 		for (TestSuiteView suite : suites)
 			suite.runSuite(testReports);
 	}
 
 	public TestScenario(boolean isVerbose, String singleFileFolder,
 			String multipleFileFolder, String[] singleTestNames,
 			String compileTestsOutputFolder) {
 		verbose = isVerbose;
 		this.singleFileFolder = singleFileFolder;
 		this.multipleFileFolder = multipleFileFolder;
 		this.singleTestNames = singleTestNames;
 		this.compileTestsOutputFolder = compileTestsOutputFolder;
 		if (compileTestsOutputFolder != null)
 			compileFlag = true;
 	}
 
 	private static TestScenario parseArgsAndInstantiate(String[] args) {
 		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verbose = parser.addBooleanOption('v',"verbose");
 		CmdLineParser.Option testSingleDirOption = parser
 				.addStringOption("testSingleFiles");
 		CmdLineParser.Option testMultipleDirOption = parser
 				.addStringOption("testFolderAsUnit");
 		CmdLineParser.Option compileTestsOutputFolderOption = parser
 				.addStringOption("compileTestsFolder");
 
 		try {
 			parser.parse(args);
 		} catch (Exception e) {
 			System.err.println("Unknown args");
 			System.exit(1);
 		}
 		boolean isVerbose = (Boolean) parser.getOptionValue(verbose,
 				Boolean.FALSE);
 
 		String singleFileFolder = (String) parser
 				.getOptionValue(testSingleDirOption);
 		String multipleFilesFolder = (String) parser
 				.getOptionValue(testMultipleDirOption);
 		String compileTestsOutputFolder = (String) parser
 				.getOptionValue(compileTestsOutputFolderOption);
 		String[] singleTestNames = parser.getRemainingArgs();
 		return new TestScenario(isVerbose, singleFileFolder,
 				multipleFilesFolder, singleTestNames, compileTestsOutputFolder);
 	}
 }
