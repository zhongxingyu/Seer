 package what.test;
 
 
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Result;
 
public class TestRunner {
	
 	Result result = JUnitCore.runClasses(ParserTest.class);
	
 }
