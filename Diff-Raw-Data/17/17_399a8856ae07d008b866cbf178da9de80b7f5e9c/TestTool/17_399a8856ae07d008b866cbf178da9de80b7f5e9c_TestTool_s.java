 package org.libraryofthings;
 
 import java.io.IOException;
 
 import javax.script.ScriptException;
 
 import org.libraryofthings.model.LOTScript;
 import org.libraryofthings.model.LOTTool;
 import org.xml.sax.SAXException;
 
 public final class TestTool extends LOTTestCase {
 
 	public void testSaveAndLoad() throws IOException, SAXException,
 			NoSuchMethodException, ScriptException {
 		LOTEnvironment env = getNewEnv();
 		assertNotNull(env);
 		//
 		LOTTool t = env.getObjectFactory().getTool();
 		t.setName("testing changing name");
 		assertTrue(t.getServiceObject().save());
 		//
 		LOTScript lotScript = new LOTScript(env);
 		t.addScript("test", lotScript);
 		lotScript
 				.setScript("function info() { return \"testing tool script\"; }");
 		//
 		t.newModel();
 		String testbinarydatastring = "TESTIBINARYDATA";
 		t.getModel().getBinary()
 				.add(new String(testbinarydatastring).getBytes());
 		//
 		t.save();
 		t.publish();
 		//
 		LOTEnvironment benv = getNewEnv();
 		assertNotNull(benv);
		LOTTool btool = env.getObjectFactory().getTool(
 				t.getServiceObject().getID().getStringID());
 		assertEquals(btool.getName(), t.getName());
 		waitObject(btool);
 		//
 		LOTScript bscript = btool.getScript("test");
 		assertNotNull(bscript);
 		assertEquals(lotScript.getScript(), bscript.getScript());
 		//
 		String sdata = new String(btool.getModel().getBinary().asByteBuffer());
 		assertEquals(testbinarydatastring, sdata);

 	}
 
 }
