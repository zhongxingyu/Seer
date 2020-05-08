 package org.libraryofthings;
 
 import java.io.IOException;
 
 import org.libraryofthings.model.LOT3DModel;
 import org.xml.sax.SAXException;
 
 public class Test3DModel extends LOTTestCase {
 
 	public void testSaveAndLoad() throws IOException, SAXException {
 		LOTEnvironment env = getNewEnv();
 		assertNotNull(env);
 		//
 		LOT3DModel s = new LOT3DModel(env);
 		s.setName("TEST");
 		assertTrue(s.getServiceObject().save());
 		//
 		LOTEnvironment benv = getNewEnv();
 		assertNotNull(benv);
		LOT3DModel bs = new LOT3DModel(benv, s.getServiceObject().getID()
				.getStringID());
 		assertEquals(s.getName(), bs.getName());
 	}
 
 	public void testSaveAndLoadBinary() throws IOException, SAXException {
 		LOTEnvironment env = getNewEnv();
 		assertNotNull(env);
 		//
 		LOT3DModel s = new LOT3DModel(env);
 		s.setName("TEST");
 		String testbinarydatastring = "TESTIBINARYDATA";
 		s.getBinary().add(new String(testbinarydatastring).getBytes());
 		s.getBinary().setReady();
 		s.publish();
 		//
 		assertTrue(s.getServiceObject().save());
 		//
 		LOTEnvironment benv = getNewEnv();
 		assertNotNull(benv);
		LOT3DModel bs = new LOT3DModel(benv, s.getServiceObject().getID()
				.getStringID());
 		assertEquals(s.getName(), bs.getName());
 		//
 		waitObject(bs);
 		//
 		String sdata = new String(bs.getBinary().asByteBuffer());
 		assertEquals(testbinarydatastring, sdata);
 	}
 
 }
