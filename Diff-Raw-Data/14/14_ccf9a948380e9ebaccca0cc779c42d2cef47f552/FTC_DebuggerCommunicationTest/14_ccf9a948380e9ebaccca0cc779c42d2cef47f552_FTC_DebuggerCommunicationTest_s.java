 package org.rubypeople.rdt.debug.core.tests;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.Socket;
 
 import junit.framework.TestCase;
 
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.debug.core.ExceptionSuspensionPoint;
 import org.rubypeople.rdt.internal.debug.core.StepSuspensionPoint;
 import org.rubypeople.rdt.internal.debug.core.SuspensionPoint;
 import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;
 import org.rubypeople.rdt.internal.debug.core.model.RubyStackFrame;
 import org.rubypeople.rdt.internal.debug.core.model.RubyThread;
 import org.rubypeople.rdt.internal.debug.core.model.RubyVariable;
 import org.rubypeople.rdt.internal.debug.core.model.ThreadInfo;
 import org.rubypeople.rdt.internal.debug.core.parsing.FramesReader;
 import org.rubypeople.rdt.internal.debug.core.parsing.LoadResultReader;
 import org.rubypeople.rdt.internal.debug.core.parsing.MultiReaderStrategy;
 import org.rubypeople.rdt.internal.debug.core.parsing.SuspensionReader;
 import org.rubypeople.rdt.internal.debug.core.parsing.ThreadInfoReader;
 import org.rubypeople.rdt.internal.debug.core.parsing.VariableReader;
 import org.rubypeople.rdt.internal.launching.RdtLaunchingPlugin;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 public class FTC_DebuggerCommunicationTest extends TestCase {
 
 /*
 	public static junit.framework.TestSuite suite() {
 
 		junit.framework.TestSuite suite = new junit.framework.TestSuite();
 		//suite.addTest(new TC_DebuggerCommunicationTest("testConstants"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testConstantDefinedInBothClassAndSuperclass"));
 		
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariablesInFrames"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testBreakpoint"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testFramesWhenThreadSpawned"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testThreadIdsAndResume"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testThreadsAndFrames"));		
 		//suite.addTest(new TC_DebuggerCommunicationTest("testStepOver"));		
 		//suite.addTest(new TC_DebuggerCommunicationTest("testThreadFramesAndVariables"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableNil"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableInstanceNested"));				
 		//suite.addTest(new TC_DebuggerCommunicationTest("testStaticVariableInstanceNested"));			
 		
 		//suite.addTest(new TC_DebuggerCommunicationTest("testNameError"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariablesInObject"));	
 		//suite.addTest(new TC_DebuggerCommunicationTest("testStaticVariables"));		
 		//suite.addTest(new TC_DebuggerCommunicationTest("testSingletonStaticVariables"));							
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableString"));	
 		// suite.addTest(new TC_DebuggerCommunicationTest("testInspect"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testInspectError"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableArray"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableArrayEmpty"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableHash"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableHashWithObjectKeys"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableHashWithStringKeys"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableWithXmlContent"));
 		//  suite.addTest(new TC_DebuggerCommunicationTest("testVariableLocal"));
 		//suite.addTest(new TC_DebuggerCommunicationTest("testReloadAndInspect")) ;
 		//suite.addTest(new TC_DebuggerCommunicationTest("testReloadWithException")) ;
 		//suite.addTest(new TC_DebuggerCommunicationTest("testReloadAndStep")) ;
 		//suite.addTest(new TC_DebuggerCommunicationTest("testReloadInRequire")) ;
 		//suite.addTest(new TC_DebuggerCommunicationTest("testReloadInStackFrame")) ;
 		suite.addTest(new TC_DebuggerCommunicationTest("testIgnoreException"));
 		suite.addTest(new TC_DebuggerCommunicationTest("testExceptionHierarchy"));
 		suite.addTest(new TC_DebuggerCommunicationTest("testException"));
         
 		return suite;
 	}
 	*/
 
 
 
 	private static String tmpDir;
 	private static String getTmpDir() {
 		if (tmpDir == null) {
 			tmpDir = System.getProperty("java.io.tmpdir");
 			if (tmpDir.charAt(tmpDir.length() - 1) != File.separatorChar) {
 				tmpDir = tmpDir + File.separator;
 			}
 		}
 		return tmpDir;
 	}
 	public static String RUBY_INTERPRETER;
 	static {
 		RUBY_INTERPRETER = System.getProperty("rdt.rubyInterpreter");
 		if (RUBY_INTERPRETER == null) {
 			RUBY_INTERPRETER = "ruby";
 		}
 	}
 	private static long TIMEOUT_MS = 60000 ;
 	private Process process;
 	private OutputRedirectorThread rubyStdoutRedirectorThread;
 	private OutputRedirectorThread rubyStderrRedirectorThread;
 	private Socket socket;
 	private PrintWriter out;
 	private MultiReaderStrategy multiReaderStrategy;
 	
 	// for timeout handling
 	private Thread mainThread ;
 	private Thread timeoutThread ;
 
 	public FTC_DebuggerCommunicationTest(String arg0) {
 		super(arg0);
 	}
 
 	public static void main(String[] args) {
 		junit.textui.TestRunner.run(FTC_DebuggerCommunicationTest.class);
 	}
 
 	private String getTestFilename() {
 		return getTmpDir() + "test.rb";
 	}
 
 	private String getRubyTestFilename() {
 		return getTestFilename().replace('\\', '/');
 	}
 
 	protected XmlPullParser getXpp(Socket socket) throws Exception {
 
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance("org.kxml2.io.KXmlParser,org.kxml2.io.KXmlSerializer", null);
 		XmlPullParser xpp = factory.newPullParser();
 		xpp.setInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));
 		return xpp;
 	}
 
 	protected SuspensionReader getSuspensionReader() throws Exception {
 		return new SuspensionReader(multiReaderStrategy);
 	}
 
 	protected VariableReader getVariableReader() throws Exception {
 		return new VariableReader(multiReaderStrategy);
 	}
 
 	protected FramesReader getFramesReader() throws Exception {
 		return new FramesReader(multiReaderStrategy);
 	}
 
 	protected ThreadInfoReader getThreadInfoReader() throws Exception {
 		return new ThreadInfoReader(multiReaderStrategy);
 	}
 	
 	protected LoadResultReader getLoadResultReader() throws Exception {
 		return new LoadResultReader(multiReaderStrategy) ;
 	}
 
 	public void startRubyProcess() throws Exception {
 		String cmd = FTC_DebuggerCommunicationTest.RUBY_INTERPRETER + " -I" + createIncludeDir() +  " -I" + getTmpDir().replace('\\', '/') + " -reclipseDebugVerbose.rb " + getRubyTestFilename();
 		System.out.println("Starting: " + cmd);
 		process = Runtime.getRuntime().exec(cmd);
 		rubyStderrRedirectorThread = new OutputRedirectorThread(process.getErrorStream());
 		rubyStderrRedirectorThread.start();
 		rubyStdoutRedirectorThread = new OutputRedirectorThread(process.getInputStream());
 		rubyStdoutRedirectorThread.start();
 
 	}
 
 	private String createIncludeDir() {
 		String includeDir;
 		if (RdtLaunchingPlugin.getDefault() != null) {
 			// being run as JUnit Plug-in Test, Eclipse is running			
 			includeDir = RubyCore.getOSDirectory(RdtLaunchingPlugin.getDefault()) + "ruby" ;
 		}
 		else {
 		    // being run as "pure" JUnit Test without Eclipse running 
 			// getResource delivers a URL, so we get slashes as Fileseparator
 			includeDir = getClass().getResource("/").getFile();
 			includeDir += "../../org.rubypeople.rdt.launching/ruby" ;
 			// if on windows, remove a leading slash
 			if (includeDir.startsWith("/") && File.separatorChar == '\\') {
 				includeDir = includeDir.substring(1);
 			}
 		}
 		// the ruby interpreter on linux does not like quotes, so we use them only if really necessary
 		if (includeDir.indexOf(" ") == -1) {
 			return includeDir ;
 		}
 		else {
 			return '"' + includeDir + '"';
 		}
 	}
 
 	protected String getOSIndependent(String path) {
 		return path.replace('\\', '/');
 	}
 
 	public void setUp() throws Exception {
 		if (!new File(getTmpDir()).exists() || !new File(getTmpDir()).isDirectory()) {
 			throw new RuntimeException("Temp directory does not exist: " + getTmpDir());
 		}
 		// if a reader hangs, because the expected data from the ruby process
 		// does not arrive, it gets interrupted from the timeout watchdog.
 		mainThread = Thread.currentThread() ;
 		timeoutThread = new Thread() {
 			public void run() {
 				try {
 					while (true) {
 						System.out.println("Starting timeout watchdog.");
 						Thread.sleep(TIMEOUT_MS);
 						System.out.println("Timeout reached.");
 						mainThread.interrupt();
 					}
 				} catch (InterruptedException e) {
 					System.out.println("Watchdog deactivated.");
 				} 
 			}
 		} ;
 		timeoutThread.start() ;		
 	}
 
 	public void tearDown() throws Exception {
 		timeoutThread.interrupt() ;
 		if (process == null || socket == null) {
 			// here we go it there was an error in the creation of the process (process == null)
 			// or there was an error creating the socket, e.g. ruby process has died early
 			return ;
 		}
 		Thread.sleep(1000);
 		socket.close();
 		try {
 			if (process.exitValue() != 0) {
 				System.out.println("Ruby finished with exit value: " + process.exitValue());
 			}
 		} catch (IllegalThreadStateException ex) {
 			process.destroy();
 			System.out.println("Ruby process had to be destroyed.");
 			// wait so that the debugger port will be availabel for the next test			
 			// There seems to be a delay after the destroying of a process and
 			// freeing the server port
 			Thread.sleep(5000) ;
 		}
 
 		System.out.println("Waiting for stdout redirector thread..");
 		rubyStdoutRedirectorThread.join();
 		System.out.println("..done");
 		System.out.println("Waiting for stderr redirector thread..");
 		rubyStderrRedirectorThread.join();
 		System.out.println("..done");
 	}
 
 	private void writeFile(String name, String[] content) throws Exception {
 		PrintWriter writer = new PrintWriter(new FileOutputStream(getTmpDir() + name));
 		for (int i = 0; i < content.length; i++) {
 			writer.println(content[i]);
 		}
 		writer.close();
 	}
 
 	private void createSocket(String[] lines) throws Exception {
 		writeFile("test.rb", lines);
 		startRubyProcess();
 		Thread.sleep(500) ;
 		try {
 			socket = new Socket("localhost", 1098);
 		} catch (ConnectException cex) {
 			throw new RuntimeException("Ruby process finished prematurely. Last line in stderr: " + rubyStderrRedirectorThread.getLastLine(), cex) ;
 		}
 		multiReaderStrategy = new MultiReaderStrategy(getXpp(socket));
 		out = new PrintWriter(socket.getOutputStream(), true);
 	}
 	
 	private void sendRuby(String debuggerCommand) {
 		try {
 			process.exitValue() ;
 			throw new RuntimeException("Ruby debugger has finished prematurely.") ;
 		} catch (IllegalThreadStateException ex) {
 			// not yet finished, normal behaviour
 			// why does process does not have a function like isRunning() ?
 			out.println(debuggerCommand);
 		}
 	}
 	
 	public void testNameError() throws Exception {
 		createSocket(new String[] { "puts 'x'" });
 		sendRuby("cont");
 		SuspensionPoint hit = getSuspensionReader().readSuspension();
 		// TODO: assertion is wrong, I want the debugger to suspend here
 		// but there must be several catchpoints available
 		assertNull(hit);
 	}
 
 	public void testBreakpoint() throws Exception {
 		// Breakpoint in line 1 does not work yet.
 		createSocket(new String[] { "puts 'a'", "def add", "puts 'b'", "end", "add()", "add()" });
 		sendRuby("b test.rb:1");
 		sendRuby("b add test.rb:3");
 		sendRuby("cont");
 		System.out.println("Waiting for breakpoint..");
 		SuspensionPoint hit = getSuspensionReader().readSuspension();
 		assertNotNull(hit);
 		assertTrue(hit.isBreakpoint());
 		assertEquals(1, hit.getLine());
 		assertEquals("test.rb", hit.getFile());
 		sendRuby("cont");
 		hit = getSuspensionReader().readSuspension();
 		assertNotNull(hit);
 		assertTrue(hit.isBreakpoint());
 		assertEquals(3, hit.getLine());
 		assertEquals("test.rb", hit.getFile());
 		sendRuby("b remove test.rb:3");
 		sendRuby("cont");
 		hit = getSuspensionReader().readSuspension();
 		assertNull(hit);
 
 	}
 
 	public void testException() throws Exception {
 		// per default catch is set to StandardError, i.e. every raise of a subclass of StandardError
 		// will suspend
 		createSocket(new String[] { "puts 'a'", "raise 'message \\dir\\file: <xml/>\n<8>'", "puts 'c'" });
 		sendRuby("cont");
 		System.out.println("Waiting for exception");
 		SuspensionPoint hit = getSuspensionReader().readSuspension();
 		assertNotNull(hit);
 		assertEquals(3, hit.getLine());
 		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), hit.getFile());
 		assertTrue(hit.isException());
 		assertEquals("message \\dir\\file: <xml/> <8>", ((ExceptionSuspensionPoint) hit).getExceptionMessage());
 		assertEquals("RuntimeError", ((ExceptionSuspensionPoint) hit).getExceptionType());
		sendRuby("cont");
 	}
 
 	public void testIgnoreException() throws Exception {
 		createSocket(new String[] { "puts 'a'", "raise 'dont stop'" });
 		sendRuby("catch off");
 		sendRuby("cont");
 		System.out.println("Waiting for the program to finish without suspending at the raise command");
 		SuspensionPoint hit = getSuspensionReader().readSuspension();
 		assertNull(hit);
 	}	
 	
 	public void testExceptionHierarchy() throws Exception {
 		createSocket(new String[] { "class MyError < StandardError", "end", "begin",  "raise StandardError.new", "rescue", "end", "raise MyError.new"});
 		sendRuby("catch MyError");
 		sendRuby("cont");
 		System.out.println("Waiting for the program to finish without suspending at the raise command");
 		SuspensionPoint hit = getSuspensionReader().readSuspension();
 		assertNotNull(hit);
 		assertEquals(7, hit.getLine());
 		assertEquals("MyError", ((ExceptionSuspensionPoint) hit).getExceptionType());
 		sendRuby("cont");
 		hit = getSuspensionReader().readSuspension();
 		assertNull(hit);
 	}
 	
 	public void testBreakpointNeverReached() throws Exception {
 		createSocket(new String[] { "puts 'a'", "puts 'b'", "puts 'c'" });
 		sendRuby("b test.rb:10");
 		sendRuby("cont");
 		System.out.println("Waiting for breakpoint..");
 		SuspensionPoint hit = getSuspensionReader().readSuspension();
 		assertNull(hit);
 	}
 
 	public void testStepOver() throws Exception {
 		createSocket(new String[] { "puts 'a'", "puts 'b'", "puts 'c'" });
 		sendRuby("b test.rb:2");
 		sendRuby("cont");
 		getSuspensionReader().readSuspension();
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(3, info.getLine());
 		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), info.getFile());
 		assertTrue(info.isStep());
 		assertEquals(1, ((StepSuspensionPoint) info).getFramesNumber());
 		sendRuby("next");
 		info = getSuspensionReader().readSuspension();
 		assertNull(info);
 	}
 
 	public void testStepOverFrames() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "puts 'a'", "Test2.new.print()" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "end", "end" });
 		sendRuby("b test.rb:3");
 		sendRuby("cont");
 		getSuspensionReader().readSuspension();
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertNull(info);
 	}
 
 	public void testStepOverFramesValue2() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "puts 'a'", "Test2.new.print()" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
 		runTo("test2.rb", 3);
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(4, info.getLine());
 		assertEquals(getOSIndependent(getTmpDir() + "test2.rb"), info.getFile());
 		assertTrue(info.isStep());
 		assertEquals(2, ((StepSuspensionPoint) info).getFramesNumber());
 		sendRuby("next");
 		info = getSuspensionReader().readSuspension();
 		assertNull(info);
 	}
 
 	public void testStepOverInDifferentFrame() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "Test2.new.print()", "puts 'a'" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
 		runTo("test2.rb", 4);
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(3, info.getLine());
 		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), info.getFile());
 		assertTrue(info.isStep());
 		assertEquals(1, ((StepSuspensionPoint) info).getFramesNumber());
 		sendRuby("cont");
 	}
 
 	public void testStepReturn() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "Test2.new.print()", "puts 'a'" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
 		runTo("test2.rb", 4);
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(3, info.getLine());
 		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), info.getFile());
 		assertTrue(info.isStep());
 		assertEquals(1, ((StepSuspensionPoint) info).getFramesNumber());
 		sendRuby("cont");
 	}
 
 	public void testHitBreakpointWhileSteppingOver() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "Test2.new.print()", "puts 'a'" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
 		sendRuby("b test2.rb:4");
 		runTo("test.rb", 2);
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(4, info.getLine());
 		assertEquals("test2.rb", info.getFile());
 		assertTrue(info.isBreakpoint());
 		sendRuby("cont");
 	}
 
 	public void testStepInto() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "puts 'a'", "Test2.new.print()" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
 		runTo("test.rb", 3);
 		sendRuby("step");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(3, info.getLine());
 		assertEquals(getOSIndependent(getTmpDir() + "test2.rb"), info.getFile());
 		assertTrue(info.isStep());
 		assertEquals(2, ((StepSuspensionPoint) info).getFramesNumber());
 		sendRuby("cont");
 	}
 
 	private void runToLine(int lineNumber) throws Exception {
 		runTo("test.rb", lineNumber);
 	}
 
 	private void runTo(String filename, int lineNumber) throws Exception {
 		sendRuby("b " + filename + ":" + lineNumber);
 		sendRuby("cont");
 		SuspensionPoint suspension = getSuspensionReader().readSuspension();
 		if (suspension == null) {
 			throw new RuntimeException("Expected suspension, but program exited.");
 		}
 	}
 
 	protected RubyStackFrame createStackFrame() throws Exception {
 		RubyStackFrame stackFrame = new RubyStackFrame(null, "", 5, 1); //RubyThread thread = new RubyThread(null) ;
 		//thread.addStackFrame(stackFrame) ;
 		return stackFrame;
 	}
 
 	public void testVariableNil() throws Exception {
 		createSocket(new String[] { "puts 'a'", "puts 'b'", "stringA='XX'" });
 		runToLine(2);
 		sendRuby("v l");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("stringA", variables[0].getName());
 		assertEquals("nil", variables[0].getValue().getValueString());
 		assertEquals(null, variables[0].getValue().getReferenceTypeName());
 		assertTrue(!variables[0].getValue().hasVariables());
 	}
 
 	public void testVariableWithXmlContent() throws Exception {
 		createSocket(new String[] { "stringA='<start test=\"&\"/>'", "testHashValue=Hash[ '$&' => nil]",  "puts 'b'" });
 		runToLine(3);
 		sendRuby("v l");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(2, variables.length);
 		assertEquals("stringA", variables[0].getName());
 		assertEquals("<start test=\"&\"/>", variables[0].getValue().getValueString());
 		assertTrue(variables[0].isLocal()) ;
 		// the testHashValue contains an example, where the name consists of special characters
 		sendRuby("v i testHashValue");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("'$&'", variables[0].getName());
 		
 						
 	}
 
 	public void testVariablesInObject() throws Exception {
 		createSocket(new String[] { "class Test", "def initialize", "@y=5", "puts @y", "end", "def to_s", "'test'", "end", "end", "Test.new()" });
 		runTo("test.rb", 4);
 		// Read numerical variable
 		sendRuby("v l");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("self", variables[0].getName());
 		assertEquals("test", variables[0].getValue().getValueString());
 		assertEquals("Test", variables[0].getValue().getReferenceTypeName());
 		assertTrue(variables[0].getValue().hasVariables());
 		sendRuby("v i self");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("@y", variables[0].getName());
 		assertEquals("5", variables[0].getValue().getValueString());
 		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
 		assertTrue(!variables[0].isStatic()) ;
 		assertTrue(!variables[0].isLocal()) ;
 		assertTrue(variables[0].isInstance()) ;				
 		assertTrue(!variables[0].getValue().hasVariables());
 		sendRuby("cont");
 	}
 
 	public void testStaticVariables() throws Exception {
 		createSocket(new String[] { "class Test", "@@staticVar=55", "def method", "puts 'a'", "end", "end", "test=Test.new()", "test.method()" });
 		runTo("test.rb", 4);
 		sendRuby("v l") ;
 	    RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("self", variables[0].getName());	
 		assertTrue(variables[0].getValue().hasVariables());			    
 		sendRuby("v i self");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("@@staticVar", variables[0].getName());
 		assertEquals("55", variables[0].getValue().getValueString());
 		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
 		assertTrue(variables[0].isStatic()) ;
 		assertTrue(!variables[0].isLocal()) ;
 		assertTrue(!variables[0].isInstance()) ;						
 		assertTrue(!variables[0].getValue().hasVariables());
 		sendRuby("cont");
 	}
 
 	public void testSingletonStaticVariables() throws Exception {
 		createSocket(new String[] { "class Test",  "def method", "puts 'a'", "end", "class << Test", "@@staticVar=55", "end", "end", "Test.new().method()" });
 		runTo("test.rb", 3);
 		sendRuby("v i self");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("@@staticVar", variables[0].getName());
 		assertEquals("55", variables[0].getValue().getValueString());
 		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
 		assertTrue(variables[0].isStatic()) ;
 		assertTrue(!variables[0].isLocal()) ;
 		assertTrue(!variables[0].isInstance()) ;				
 		assertTrue(!variables[0].getValue().hasVariables());
 		sendRuby("cont");
 	}
 
 
 	public void testConstants() throws Exception {
 		createSocket(new String[] { "class Test", "TestConstant=5", "end", "test=Test.new()", "puts 'a'" });
 		runTo("test.rb", 5);
 		sendRuby("v i test");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("TestConstant", variables[0].getName());
 		assertEquals("5", variables[0].getValue().getValueString());
 		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
 		assertTrue(variables[0].isConstant()) ;
 		assertTrue(!variables[0].isStatic()) ;
 		assertTrue(!variables[0].isLocal()) ;
 		assertTrue(!variables[0].isInstance()) ;						
 		assertTrue(!variables[0].getValue().hasVariables());
 		sendRuby("cont");
 	}
 
 
 	public void testConstantDefinedInBothClassAndSuperclass() throws Exception {
 		createSocket(new String[] { "class A", "TestConstant=5", "TestConstant2=2", "end", "class B < A", "TestConstant=6", "end", "b=B.new()", "puts 'a'" });
 		runTo("test.rb", 9);
 		sendRuby("v i b");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("TestConstant", variables[0].getName());
 		assertEquals("6", variables[0].getValue().getValueString());
 		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
 		assertTrue(variables[0].isConstant()) ;
 		assertTrue(!variables[0].isStatic()) ;
 		assertTrue(!variables[0].isLocal()) ;
 		assertTrue(!variables[0].isInstance()) ;						
 		assertTrue(!variables[0].getValue().hasVariables());
 		sendRuby("cont");
 	}
 
 
 	public void testVariableString() throws Exception {
 		createSocket(new String[] { "stringA='XX'", "puts stringA" });
 		runToLine(2);
 		// Read numerical variable
 		sendRuby("v l");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("stringA", variables[0].getName());
 		assertEquals("XX", variables[0].getValue().getValueString());
 		assertEquals("String", variables[0].getValue().getReferenceTypeName());
 		assertTrue(!variables[0].getValue().hasVariables());
 		sendRuby("cont");
 	}
 
 	public void testVariableLocal() throws Exception {
 		createSocket(new String[] { "class User", "def initialize(id)", "@id=id", "end", "end", 
 				"class CallClass", "def method(user)", 	"puts user", "end", "end", 
 				"CallClass.new.method(User.new(22))" }) ;
 		runTo("test.rb", 8);
 		sendRuby("v local") ;
 		RubyVariable[] localVariables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(2, localVariables.length);
 		RubyVariable userVariable = localVariables[1] ;
 		sendRuby("v i 1 " + userVariable.getObjectId());
 		RubyVariable[] userVariables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, userVariables.length);
 		assertEquals("@id", userVariables[0].getName());
 		assertEquals("22", userVariables[0].getValue().getValueString());
 		assertEquals("Fixnum", userVariables[0].getValue().getReferenceTypeName());
 		assertTrue(!userVariables[0].getValue().hasVariables());
 	}		
 	
 	public void testVariableInstance() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "customObject=Test2.new", "puts customObject" });
 		writeFile("test2.rb", new String[] { "class Test2", "def initialize", "@y=5", "end", "def to_s", "'test'", "end", "end" });
 		runTo("test2.rb", 6);
 		sendRuby("v i 2 customObject");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("@y", variables[0].getName());
 		assertEquals("5", variables[0].getValue().getValueString());
 		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
 		assertTrue(!variables[0].getValue().hasVariables());
 	}
 
 	public void testVariableArray() throws Exception {
 		createSocket(new String[] { "array = []", "array << 1", "array << 2", "puts 'a'" });		
 		runTo("test.rb", 4);
 		sendRuby("v local") ;
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("array", variables[0].getName());
 		assertTrue("array has children", variables[0].getValue().hasVariables());
 		sendRuby("v i array");
 		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
 		assertEquals(2, elements.length);
 		assertEquals("[0]", elements[0].getName());
 		assertEquals("1", elements[0].getValue().getValueString());
 		assertEquals("Fixnum", elements[0].getValue().getReferenceTypeName());
 		assertEquals("array[0]", elements[0].getQualifiedName()) ;
 	}
 
 	public void testVariableHashWithStringKeys() throws Exception {
 		createSocket(new String[] { "hash = Hash['a' => 'z', 'b' => 'y']", "puts 'a'" });		
 		runTo("test.rb", 2);
 		sendRuby("v local") ;
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("hash", variables[0].getName());
 		assertTrue("hash has children", variables[0].getValue().hasVariables());
 		sendRuby("v i hash");
 		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
 		assertEquals(2, elements.length);
 		assertEquals("'a'", elements[0].getName());
 		assertEquals("z", elements[0].getValue().getValueString());
 		assertEquals("String", elements[0].getValue().getReferenceTypeName());
 		assertEquals("hash['a']", elements[0].getQualifiedName()) ;
 	}
 
 	public void testVariableHashWithObjectKeys() throws Exception {
 		createSocket(new String[] { "class KeyAndValue", "def initialize(v)", "@a=v", "end", "def to_s", "return @a.to_s", "end", "end", "hash = Hash[KeyAndValue.new(55) => KeyAndValue.new(66)]", "puts 'a'" });		
 		runTo("test.rb", 10);
 		sendRuby("v local") ;
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("hash", variables[0].getName());
 		assertTrue("hash has children", variables[0].getValue().hasVariables());
 		sendRuby("v i 1 " + variables[0].getObjectId());
 		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
 		assertEquals(1, elements.length); 
 		assertEquals("55", elements[0].getName());
 		//assertEquals("z", elements[0].getValue().getValueString());
 		assertEquals("KeyAndValue", elements[0].getValue().getReferenceTypeName());
 		// get the value
 		sendRuby("v i 1 " + elements[0].getObjectId()) ;
 		RubyVariable[] values = getVariableReader().readVariables(variables[0]);
 		assertEquals(1, values.length);
 		assertEquals("@a", values[0].getName());
 		assertEquals("Fixnum", values[0].getValue().getReferenceTypeName());
 		assertEquals("66", values[0].getValue().getValueString());
 					
 	}
 
 
 	public void testVariableArrayEmpty() throws Exception {
 		createSocket(new String[] { "emptyArray = []", "puts 'a'" });		
 		runTo("test.rb", 2);
 		sendRuby("v local") ;
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("emptyArray", variables[0].getName());
 		assertTrue("array does not have children", !variables[0].getValue().hasVariables());
 	}
 
 
 	public void testVariableInstanceNested() throws Exception {
 		createSocket(new String[] { "class Test", "def initialize(test)", "@privateTest = test", "end", "end", "test2 = Test.new(Test.new(nil))", "puts test2" });
 		runToLine(7);
 		sendRuby("v l");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		RubyVariable test2Variable = variables[0];
 		assertEquals("test2", test2Variable.getName());
 		assertEquals("test2", test2Variable.getQualifiedName());
 		sendRuby("v i " + test2Variable.getQualifiedName());
 		variables = getVariableReader().readVariables(test2Variable);
 		assertEquals(1, variables.length);
 		RubyVariable privateTestVariable = variables[0];
 		assertEquals("@privateTest", privateTestVariable.getName());
 		assertEquals("test2.@privateTest", privateTestVariable.getQualifiedName());
 		assertTrue(privateTestVariable.getValue().hasVariables());
 		sendRuby("v i " + privateTestVariable.getQualifiedName());
 		variables = getVariableReader().readVariables(privateTestVariable);
 		assertEquals(1, variables.length);
 		RubyVariable privateTestprivateTestVariable = variables[0];
 		assertEquals("@privateTest", privateTestprivateTestVariable.getName());
 		assertEquals("test2.@privateTest.@privateTest", privateTestprivateTestVariable.getQualifiedName());
 		assertEquals("nil", privateTestprivateTestVariable.getValue().getValueString());
 		assertTrue(!privateTestprivateTestVariable.getValue().hasVariables());
 		sendRuby("cont");
 	}
 	
 	public void testInspect() throws Exception {
 		createSocket(new String[] { "class Test", "def calc(a)", "a = a*2", "return a",  "end", "end", "test=Test.new()", "a=3", "test.calc(a)"  });
 		runToLine(4);
 		// test variable value in stack 1 (top stack frame)
 		sendRuby("v inspect 1 a*2");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("There is one variable returned.", 1, variables.length) ;
 		assertEquals("Result is 12", "12", variables[0].getValue().getValueString()) ;
 		// test variable value in stack 2 (caller stack)
 		sendRuby("v inspect 2 a*4");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("There is one variable returned.", 1, variables.length) ;
 		assertEquals("Result is 12", "12", variables[0].getValue().getValueString()) ;
 		// test more complex expression
 		sendRuby("v inspect 1 Test.new().calc(5)");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("There is one variable returned.", 1, variables.length) ;
 		assertEquals("Result is 10", "10", variables[0].getValue().getValueString()) ;
 	}
 
 	public void testInspectError() throws Exception {
 		createSocket(new String[] { "puts 'test'"  });
 		runToLine(1);
 		sendRuby("v inspect a*2");
         try {
             getVariableReader().readVariables(createStackFrame());
         } catch (RubyProcessingException e) {
         	assertNotNull(e.getMessage()) ;
         	return ;
         }
         fail("RubyProcessingException not thrown.") ;
 	}
 
 	public void testStaticVariableInstanceNested() throws Exception {
 		createSocket(new String[] { "class TestStatic", "def initialize(no)", "@no = no", "end", "@@staticVar=TestStatic.new(2)",  "end", "test = TestStatic.new(1)", "puts test" });
 		runToLine(8);
 		sendRuby("v i test.@@staticVar");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(2, variables.length) ; 
 		assertEquals("@no", variables[0].getName()) ;
 		assertEquals("2", variables[0].getValue().getValueString()) ;
 		assertEquals("@@staticVar", variables[1].getName()) ;
 		assertTrue("2", variables[1].getValue().hasVariables()) ;
 		
 		sendRuby("cont");
 	}
 
 
 	public void testVariablesInFrames() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "y=5", "Test2.new().test()" });
 		writeFile("test2.rb", new String[] { "class Test2", "def test", "y=6", "puts y", "end", "end" });
 		runTo("test2.rb", 4);
 		sendRuby("v l");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		// there are 2 variables self and y
 		assertEquals(2, variables.length);
 		// the variables are sorted: self = variables[0], y = variables[1]
 		assertEquals("y", variables[1].getName());
 		assertEquals("6", variables[1].getValue().getValueString());
 		sendRuby("v l 1");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(2, variables.length);
 		assertEquals("y", variables[1].getName());
 		assertEquals("6", variables[1].getValue().getValueString());
 		sendRuby("v l 2");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(1, variables.length);
 		assertEquals("y", variables[0].getName());
 		assertEquals("5", variables[0].getValue().getValueString());
 		// 20 is out of range, then the default frame is used, which is 1
 		sendRuby("v l 20");
 		variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals(2, variables.length);
 		assertEquals("y", variables[1].getName());
 		assertEquals("6", variables[1].getValue().getValueString());
 		sendRuby("cont");
 	}
 
 	public void testFrames() throws Exception {
 		createSocket(new String[] { "require 'test2.rb'", "test = Test2.new()", "test.print()", "test.print()" });
 		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "end", "end" });
 		runTo("test2.rb", 3);
 		sendRuby("b test.rb:4");
 		sendRuby("w");
 		RubyThread thread = new RubyThread(null, 0);
 		getFramesReader().readFrames(thread);
 		assertEquals(2, thread.getStackFrames().length);
 		RubyStackFrame frame1 = (RubyStackFrame) thread.getStackFrames()[0];
 		assertEquals(getOSIndependent(getTmpDir() + "test2.rb"), frame1.getFileName());
 		assertEquals(1, frame1.getIndex());
 		assertEquals(3, frame1.getLineNumber());
 		RubyStackFrame frame2 = (RubyStackFrame) thread.getStackFrames()[1];
 		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), frame2.getFileName());
 		assertEquals(2, frame2.getIndex());
 		assertEquals(3, frame2.getLineNumber());
 		sendRuby("cont");
 		getSuspensionReader().readSuspension();
 		sendRuby("w");
 		getFramesReader().readFrames(thread);
 		assertEquals(1, thread.getStackFramesSize());
 
 	}
 
 	public void testFramesWhenThreadSpawned() throws Exception {
 		createSocket(new String[] { "def startThread", "Thread.new() {  a = 5  }", "end", "def calc", "5 + 5", "end", "startThread()", "calc()" });
 		runTo("test.rb", 5);
 		RubyThread thread = new RubyThread(null, 0);
 		sendRuby("w");
 		getFramesReader().readFrames(thread);
 		assertEquals(2, thread.getStackFramesSize());
 	}
 
 	public void testThreads() throws Exception {
 		createSocket(new String[] { "Thread.new {", "puts 'a'", "}", "Thread.pass", "puts 'b'" });
 		sendRuby("b test.rb:2");
 		sendRuby("b test.rb:5");
 		sendRuby("cont");
 		SuspensionPoint point = getSuspensionReader().readSuspension();
 		if (point.getThreadId() == 2) {
 			assertEquals(2, point.getLine());
 		} else {
 			assertEquals(5, point.getLine());
 		}
 		sendRuby("cont");
 		point = getSuspensionReader().readSuspension();
 		if (point.getThreadId() == 2) {
 			assertEquals(2, point.getLine());
 		} else {
 			assertEquals(5, point.getLine());
 		}
 		sendRuby("cont");
 	}
 
 	public void testThreadIdsAndResume() throws Exception {
 		createSocket(new String[] { "threads=[]", "threads << Thread.new {", "puts 'a'", "}", "threads << Thread.new{", "puts 'b'", "}", "puts 'c'", "threads.each{|t| t.join()}" });
 		sendRuby("b test.rb:3");
 		sendRuby("b test.rb:6");
 		sendRuby("b test.rb:8");
 		sendRuby("cont");
 		getSuspensionReader().readSuspension();
 		getSuspensionReader().readSuspension();
 		getSuspensionReader().readSuspension();
 
 		sendRuby("th l");
 		ThreadInfo[] threads = getThreadInfoReader().readThreads();
 		assertEquals(3, threads.length);
 		int threadId1 = threads[0].getId();
 		int threadId2 = threads[1].getId();
 		int threadId3 = threads[2].getId();
 		sendRuby("th " + threadId2 + " ; cont");
 
 		sendRuby("th l");
 		threads = getThreadInfoReader().readThreads();
 		assertEquals(2, threads.length);
 		assertEquals(threadId1, threads[0].getId());
 		assertEquals(threadId3, threads[1].getId());
 		sendRuby("th " + threadId3 + " ; cont");
 
 		sendRuby("th l");
 		threads = getThreadInfoReader().readThreads();
 		assertEquals(1, threads.length);
 		assertEquals(threadId1, threads[0].getId());
 		sendRuby("cont");
 	}
 
 	public void testThreadFramesAndVariables() throws Exception {
 		createSocket(new String[] { "Thread.new {", "a=5", "x=6", "puts 'x'", "}", "b=10", "b=11" });
 		sendRuby("b test.rb:3");
 		sendRuby("b test.rb:7");
 		sendRuby("cont");
 		getSuspensionReader().readSuspension();
 		getSuspensionReader().readSuspension();
 		// the main thread and the "puts 'a'" - thread are active
 		sendRuby("th l");
 		ThreadInfo[] threads = getThreadInfoReader().readThreads();
 		assertEquals(2, threads.length);
 		assertEquals(1, threads[0].getId());
 		assertEquals(2, threads[1].getId());
 		sendRuby("th 1 ; f ");
 		RubyStackFrame[] stackFrames = getFramesReader().readFrames(new RubyThread(null, 1));
 		assertEquals(1, stackFrames.length);
 		assertEquals(7, stackFrames[0].getLineNumber());
 		sendRuby("th 1 ; v l");
 		RubyVariable[] variables = getVariableReader().readVariables(stackFrames[0]);
 		assertEquals(1, variables.length);
 		assertEquals("b", variables[0].getName());
 		sendRuby("th 2 ; f");
 		stackFrames = getFramesReader().readFrames(new RubyThread(null, 1));
 		assertEquals(1, stackFrames.length);
 		assertEquals(3, stackFrames[0].getLineNumber());
 		sendRuby("th 2 ; v l");
 		variables = getVariableReader().readVariables(stackFrames[0]);		
 		assertEquals("a", variables[0].getName()) ;
 		assertEquals("b", variables[1].getName()) ;
 		// there is a third variable 'x' for ruby 1.8.0
 		sendRuby("th 2 ; next");
 		getSuspensionReader().readSuspension();
 		sendRuby("th 2 ; v l");		
 		variables = getVariableReader().readVariables(stackFrames[0]);
 		assertEquals(3, variables.length) ;
 		assertEquals("a", variables[0].getName()) ;
 		assertEquals("b", variables[1].getName()) ;
 		assertEquals("x", variables[2].getName()) ;		
 		
 	}
 	
 	public void testReloadAndInspect() throws Exception {
 		String[] lines = new String[] { "class Test", "def calc(a)", "a = a*2", "return a",  "end", "end", "test=Test.new()" } ;
 		createSocket( lines );
 		runToLine(7);
 		// test variable value in stack 1 (top stack frame)
 		lines[2] = "a=a*4" ;
 		writeFile( "test.rb", lines);
 		sendRuby("load " + getTmpDir() + "test.rb") ;
 		LoadResultReader.LoadResult loadResult = this.getLoadResultReader().readLoadResult() ;
 		assertTrue("No Exception from load", loadResult.isOk()) ;
 		sendRuby("v inspect Test.new.calc(2)");
 		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("There is one variable returned.", 1, variables.length) ;
 		assertEquals("Result is 8", "8", variables[0].getValue().getValueString()) ;
 	}
 	
 	public void testReloadAndStep() throws Exception {
 		String[] lines = new String[] { "puts 'a'", "puts 'b'", "puts 'c'" } ;
 		createSocket( lines );
         runToLine(2) ;
 		lines = new String[] { "puts 'd'", "puts 'e'", "puts 'f'" } ;
         writeFile("test.rb", lines) ;
 		sendRuby("load " + getTmpDir() + "test.rb") ;
 		this.getLoadResultReader().readLoadResult() ;
 		sendRuby("next");
 		SuspensionPoint info = getSuspensionReader().readSuspension();
 		assertEquals(3, info.getLine());		
 	}	
 
 	public void testReloadWithException() throws Exception {
 		createSocket(new String[] { "puts 'a'" }) ;
 		runToLine(1);
 		// test variable value in stack 1 (top stack frame)
 		String[] lines = new String[] { "classs A;end" } ;
 		writeFile( "test.rb", lines);
 		
 		sendRuby("load " + getTmpDir() + "test.rb") ;
 		LoadResultReader.LoadResult loadResult = this.getLoadResultReader().readLoadResult() ;
 		assertFalse("Exception from load", loadResult.isOk()) ;
 		assertEquals(loadResult.getExceptionType(), "SyntaxError") ;
 	}
 	
 	public void testReloadInRequire() throws Exception {
 		// Deadlock
 		String[] lines = new String[] { "def endless", "sleep 0.1", "end" } ;
 		writeFile( "content file.rb", lines);
 		createSocket(new String[] { "require 'content file'", "while true", "endless()", "end"} );
 		sendRuby("cont") ;		
 		// test variable value in stack 1 (top stack frame)
 		lines[1] = "exit 0" ;
 		writeFile( "content file.rb", lines);
 		sendRuby("load " + getTmpDir() + "content file.rb") ;
 		LoadResultReader.LoadResult loadResult = this.getLoadResultReader().readLoadResult() ;
 		assertTrue("No Exception from load", loadResult.isOk()) ;
 	}
 	
 	
 	public void testReloadInStackFrame() throws Exception {
 		String[] lines = new String[] { "class Test", "def calc(a)", "a = a*2", "return a",  "end", "end", "result = Test.new.calc(2)", "result = Test.new.calc(2)", "puts result" } ;
 		createSocket( lines );
 		runToLine(3);
 		// a has not yet been calculated ...
 		sendRuby("v local") ;
 		RubyVariable[] localVariables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("2", localVariables[0].getValue().getValueString());
 		// now change the code ...
 		lines[2] = "a=a*4" ;
 		writeFile( "test.rb", lines);
 		sendRuby("load " + getTmpDir() + "test.rb") ;
 		LoadResultReader.LoadResult loadResult = this.getLoadResultReader().readLoadResult() ;
 		assertTrue("No Exception from load", loadResult.isOk()) ;
 		runToLine(4);
 		// now a is calculated and the result is 4. That means that ruby does not change the code which 
 	    // currently being executed in a stack frame, Java would have reset the instruction pointer and the
 		// result would be 8
 		sendRuby("v local") ;
 		localVariables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("4", localVariables[0].getValue().getValueString());
 		
 		// Now check that the new code is executed with the next call to calc
 		runToLine(3) ;
 		runToLine(4) ;
 		sendRuby("v local") ;
 		localVariables = getVariableReader().readVariables(createStackFrame());
 		assertEquals("8", localVariables[0].getValue().getValueString());
 	}
 	
 	
 	
 }
 
