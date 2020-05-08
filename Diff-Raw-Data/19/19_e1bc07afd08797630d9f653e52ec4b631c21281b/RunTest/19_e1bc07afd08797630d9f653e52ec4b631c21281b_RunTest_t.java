 package testutils;
 
 import java.util.List;
import testutils.javaparser.PTJavaParser;
 import AST.JavaParser;
 
 /**
  * Has ugly proof of concept code to test all files ending with .ptjava in the
  * same folder together.
  * 
  * 
  * @author eivindgl
  * 
  */
 public class RunTest extends PTFrontend {
 
 	private boolean shouldBeOk, hasRun, ranOk;
 	private String[] filenames;
 	private String testname;
 
 	public RunTest(String filename) {
 		this(asArray(filename));
 	}
 	
 	/** Use default JavaParser */
 	public RunTest(String foldername, List<String> filenames) {
 		this(foldername, asArray(filenames), new PTJavaParser());
 	}
 
 	private static String[] asArray(List<String> seq) {
 		return seq.toArray(new String[seq.size()]);
 	}
 
 	/** Use default JavaParser */
 	public RunTest(String[] filenames) {
 		this(filenames[0], filenames, new PTJavaParser());
 	}
 
 	public RunTest(String testname, String[] filenames, JavaParser parser) {
 		super(filenames, parser);
 		this.filenames = filenames;
 		this.testname = testname; 
 		this.shouldBeOk = !testname.contains("_fail");
 	}
 
 	private static String[] asArray(String filename) {
 		String[] x = { filename };
 		return x;
 	}
 
 	public boolean run() {
 		if (!hasRun) {
 			ranOk = process();
 			hasRun = true;
 		}
 		return ranOk;
 	}
 
 	public boolean didNotRunAsExpected() {
 		return shouldBeOk != run();
 	}
 
 	public String getReport() {
 		boolean ok = run(); // cached
 		String desiredResult = shouldBeOk ? "passed" : "failed";
 		String result = ok ? "passed" : "failed";
 		return "Test for filename '" + testname + ") should have "
 				+ desiredResult + ", but " + result;
 	}
 
 	public String getTestname() {
 		return testname;
 	}
 }
