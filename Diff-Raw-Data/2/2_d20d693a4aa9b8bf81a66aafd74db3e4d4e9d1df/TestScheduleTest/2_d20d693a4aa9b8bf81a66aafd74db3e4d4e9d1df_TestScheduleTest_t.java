 /**
  * 
  */
 package com.testmonkey.app.test;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.UUID;
 
 import javax.xml.xpath.XPathExpressionException;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Test;
 import org.xml.sax.InputSource;
 
 import com.testmonkey.app.GlobalConfig;
 import com.testmonkey.app.TestSchedule;
 import com.testmonkey.exceptions.VarNotFoundException;
 import com.testmonkey.model.TestModule;
 import com.testmonkey.util.HashMethodFactory;
 import com.testmonkey.util.IHashMethod;
 import com.testmonkey.util.InputSourceFactory;
 
 /**
  * @author phil
  *
  */
 public class TestScheduleTest {
 	
 	Mockery context = new Mockery();
 
 	/**
 	 * Test TestSchedule.getTestModuleListFromSchedule
 	 * @throws IOException 
 	 * @throws VarNotFoundException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws XPathExpressionException 
 	 * @throws IllegalArgumentException 
 	 */
 	@Test
 	public void getTestModuleListFromScheduleTest() throws IllegalArgumentException, XPathExpressionException, NoSuchAlgorithmException, VarNotFoundException, IOException {
 		// create mock hash object
 		final IHashMethod mockHash = context.mock(IHashMethod.class);
 				
 		// register mock object factory
 		HashMethodFactory.registerHashMethodProvider(new HashMethodFactory() {
 			@Override
 			protected IHashMethod createHashMethod() {
 				return mockHash;
 			}
     	});
 		
 		// expectations
 		context.checking(new Expectations() {{
 			oneOf (mockHash).getFileHash(with(any(String.class))); will(returnValue(UUID.randomUUID().toString()));
 		}});
 		
 		// add var to global config object
 		GlobalConfig config = GlobalConfig.getConfig();
 		config.processCommandLine(new String[] {"/test/fakeGTestApp", "TestVariableReplacement=replaced" });
 		
 		// define an InputSourceFactory that returns a stubbed InputSource (can't use a mock in this instance
 		// as InputSource isn't an interface, but a stub will do)
 		InputSourceFactory.registerInputSourceProvider(new InputSourceFactory() {
 			// define some gtest format output xml that has a test failure
 			private static final String testXmlOutput = 
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
 				"<UnitTestSchedule runallfrom=\"/test/directory/$(TestVariableReplacement)/ping\">\n" +
 				"   <UnitTestModule name=\"fakeApp\" testfilter=\"*\" enabled=\"true\" description=\"A fake unit test module\">fakeGTestApp</UnitTestModule>\n" +
 				"   <UnitTestModule name=\"ignored\" testfilter=\"*\" enabled=\"false\">AnIgnoredModule</UnitTestModule>\n" +
 				"</UnitTestSchedule>";
 			
 			@Override
 			protected InputSource createInputSource(String sourceIdentifier) {
 				return new InputSource(new StringReader(testXmlOutput));
 			}
     	});
 		
 		TestSchedule testSchedule = new TestSchedule("dummyFile");
 		
 		// execute
 		List<TestModule> testModules = testSchedule.getTestModuleListFromSchedule();
 		
 		// verify
         context.assertIsSatisfied(); // were the mocks called with the right params in the right order?
         
 		assertEquals(1, testModules.size());
		assertEquals("/test/directory/replaced/ping" + System.getProperty("file.separator") + "fakeGTestApp", testModules.get(0).getModuleFilePath());
 	}
 
 }
