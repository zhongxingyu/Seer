 package testRunner;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.time.StopWatch;
 
 import testRunner.tests.BigMessage;
 import testRunner.tests.DummyTest;
 import testRunner.tests.SendAndPopMessages;
 import testRunner.tests.SendMessages;
 import testRunner.tests.SendMessagesTime;
 
 public class Runner {
 	List<Class> tests = new ArrayList<Class>();
 	
 	String testArgs[];
 	
 	public Runner(){
 		tests.add(DummyTest.class);
 		tests.add(SendMessages.class);
 		tests.add(SendMessagesTime.class);
 		tests.add(BigMessage.class);
 		tests.add(SendAndPopMessages.class);
 	}
 	
 	public List<Class> getTests(){
 		return tests;
 	}
 	
 	public void setArgs(String args[]) {
 		this.testArgs = args;
 	}
 
 	public Test getTestFromIdentifier(String input) throws InstantiationException, IllegalAccessException {
 		for(Class t : tests)
 		{
 			Test test = (Test)t.newInstance();
 			if(test.getIdentifier().toLowerCase().equals(input.toLowerCase()))
 				return test;
 		}
 		return null;
 	}
 	
 	
 	
 	public void runTest(String testName, Settings settings, MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception{
 		System.out.println("OK. Initing test...");
 		Test test = getTestFromIdentifier(testName);
 		
 		System.out.println("---");
 		test.setConnectionInfo(settings.host, settings.port);
 		test.init(this.testArgs);
 		System.out.println("---");
 			
 		System.out.println("OK. Running test...");
 		
 		System.out.println("---");
 		StopWatch w = new StopWatch();
 		w.start();
 		test.run(applicationLogger, testLogger);
 		w.stop();
 		System.out.println("---");
 			
 		System.out.println("OK. Finished");
 		System.out.println("Total Running time: " + w.getNanoTime()/1000/(float)1000 + " ms");
 	}
 }
