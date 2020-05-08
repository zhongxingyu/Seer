 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import logs.Log;
 import serviceLayer.Services;
 import tests.Inventory;
 import tests.SingleTest;
 import annotations.Test;
 
 public class ClassTester {
 
 	static int intPassed = 0;
 	static int intFailed = 0;
 	static Log myLog;
 	static Inventory testInventory = new Inventory();
 	static boolean boolXML = false;
 
 	public static void main(String[] args) throws Exception {
 
		// Enable assertions for application
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

 		ValidateArguments(args);
 
 		String[] testsToRun = args[0].split(";");
 
 		boolXML = Boolean.parseBoolean(args[2].toString());
 		myLog = Services.getOutputMethod(testsToRun[0].toString(), args[1]
 				.toString());
 
 		for (int x = 0; x < testsToRun.length; x++)
 			loadTests(testsToRun[0].toString());
 
 		lauchTests();
 	}
 
 	private static void ValidateArguments(String[] args) {
 		if (args.length != 3) {
 			System.err
 					.println("Usage: java RunTest [string]ClassName [string]OutputFile [boolean]Encoding");
 			System.exit(1);
 		}
 
 		if (!"true".equals(args[2].toString())
 				&& !"false".equals(args[2].toString())) {
 			System.err.println("Usage: Parameter Encoding must be boolean");
 			System.exit(1);
 		}
 	}
 
 	private static void lauchTests() throws IllegalArgumentException,
 			SecurityException, InvocationTargetException,
 			NoSuchMethodException, ClassNotFoundException {
 
 		for (SingleTest s : testInventory) {
 
 			try {
 
 				Services.ExecuteMethod(s.getClassName(), Class.forName(
 						s.getClassName()).getMethod("setup"));
 				Services.ExecuteMethod(s.getClassName(), s.getMethod());
 
 				s.setTestResultMessage("Success");
 				s.setTestExecutionResult(true);
 				intPassed++;
 
 			} catch (Throwable ex) {
 
 				s.setTestResultMessage(ex.getCause().toString());
 				s.setTestExecutionResult(false);
 				intFailed++;
 
 			} finally {
 
 				Services.ExecuteMethod(s.getClassName(), Class.forName(
 						s.getClassName()).getMethod("tearDown"));
 
 			}
 
 		}
 
 		displayResults();
 
 	}
 
 	private static void loadTests(String className) {
 
 		try {
 
 			for (Method methodToTest : Class.forName(className).getMethods()) {
 
 				if (methodToTest.isAnnotationPresent(Test.class)
 						&& methodToTest.getName() != "setup"
 						&& methodToTest.getName() != "tearDown") {
 
 					SingleTest aTest = new SingleTest(Class.forName(className)
 							.toString().substring(6), methodToTest);
 
 					testInventory.add(aTest);
 				}
 
 			}
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void displayResults() {
 
 		if (boolXML) {
 			myLog.log("<ListOfTests>");
 		}
 
 		for (SingleTest s : testInventory) {
 
 			if (boolXML) {
 
 				myLog.log("<classname>" + s.getClassName() + "</classname>");
 				myLog.log("<method>" + s.getMethodName() + "</method>");
 				myLog.log("<result>" + s.getTestResultMessage() + "</result>");
 
 			} else {
 
 				myLog.log(s.getClassName() + "." + s.getMethodName());
 				myLog.log("Result:" + s.getTestResultMessage());
 			}
 
 		}
 
 		if (boolXML) {
 
 			myLog.log("<GlobalResults>");
 			myLog.log("<Passed>" + intPassed + "</Passed>");
 			myLog.log("<Failed>" + intFailed + "</Failed>");
 			myLog.log("</GlobalResults>");
 			myLog.log("</ListOfTests>");
 
 		} else {
 
 			myLog.skipline();
 			myLog.log("**** Passed:" + intPassed + " Failed " + intFailed);
 		}
 
 		myLog.close();
 
 	}
 
 }
