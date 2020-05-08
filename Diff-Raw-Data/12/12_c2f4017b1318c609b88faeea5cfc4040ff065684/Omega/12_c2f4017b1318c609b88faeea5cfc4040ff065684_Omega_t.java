 package ca.uqam.mgl7361.a2011.omega.framework;
 
 import jargs.gnu.CmdLineParser;
 import ca.uqam.mgl7361.a2011.omega.framework.core.*;
 import ca.uqam.mgl7361.a2011.omega.framework.results.*;
 import ca.uqam.mgl7361.a2011.omega.framework.results.formats.*;
 import ca.uqam.mgl7361.a2011.omega.framework.results.writers.*;
 
 public class Omega {
 	static CmdLineParser commandLineParser = new CmdLineParser();
 	static String outputFileName;
 	static Boolean consoleOutputEnabled;
 	static Boolean xmlOutputEnabled;
 	static String[] testClassesNames;
 	static TestSuite testSuite;
 	static Result testSuiteResult;
 	
 	public static void main(String[] args) throws Exception {
 		parseCommandLineArguments(args);
 		testClassesNames = commandLineParser.getRemainingArgs();
 		buildTestSuite();
 		runTestSuite();
 		outputTestSuiteResult();
 	}
 
 	private static void buildTestSuite() {
 		testSuite = new TestSuite(outputFileName);
 		TestCaseFactory factory = TestCaseFactory.getInstance();
 		for (String className : testClassesNames) {
 			try {
 				testSuite.add(factory.makeTestCase(className));
 			} catch (ClassNotFoundException e) {
				System.out.print("Unable to find a class named '" + className + "'");
 			}
 		}
 	}
 
 	private static void runTestSuite() {
 		TestRunner testRunner = TestRunner.getInstance();
 		testSuiteResult = testRunner.execute(testSuite);
 	}
 	
 	private static void outputTestSuiteResult() {
 		if (consoleOutputEnabled) {
 			outputTestSuiteResultToConsole();
 		}
 		outputTestSuiteResultToFile();
 	}
 
 	private static void outputTestSuiteResultToFile() {
		Writer resultWriter = new TextFileWriter();
		resultWriter.write(testSuiteResult, getOutputFormat());	
 	}
 	
 	private static Format getOutputFormat() {
 		Format format = new TextFormat();
 			if(consoleOutputEnabled)
 				format = new XMLFormat();
 		return format;
 	}
 
 	private static void outputTestSuiteResultToConsole() {
 		Writer resultWriter = new ConsoleWriter();
 		resultWriter.write(testSuiteResult, getOutputFormat());
 	}
 
 	private static void parseCommandLineArguments(String[] args) {
 		CmdLineParser.Option outputFileNameOption;
 		CmdLineParser.Option consoleOutputEnabledOption;
 		CmdLineParser.Option xmlOutputEnabledOption;
 		try {
 			consoleOutputEnabledOption = commandLineParser.addBooleanOption('c', "consoleOutput");
 			xmlOutputEnabledOption = commandLineParser.addBooleanOption('x', "xml");
 			outputFileNameOption = commandLineParser.addStringOption('f', "file");
 			commandLineParser.parse(args);
 			outputFileName = (String)commandLineParser.getOptionValue(outputFileNameOption, "OmegaResult");
 			consoleOutputEnabled = (Boolean)commandLineParser.getOptionValue(consoleOutputEnabledOption, Boolean.FALSE);
 			xmlOutputEnabled = (Boolean)commandLineParser.getOptionValue(xmlOutputEnabledOption, Boolean.FALSE);
 		}
         catch ( CmdLineParser.OptionException e ) {
             System.err.println(e.getMessage());
             printUsage();
             System.exit(2);
         }
 	}
 
 	private static void printUsage() {
         System.err.println(
 			"Usage: omega.jar [-f,--file] [-c,--consoleOutput] [-x, --xml]");
     }
 }
