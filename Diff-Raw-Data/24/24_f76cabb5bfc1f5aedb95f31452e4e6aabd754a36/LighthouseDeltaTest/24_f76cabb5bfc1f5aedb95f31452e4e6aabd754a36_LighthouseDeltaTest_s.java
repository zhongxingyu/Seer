 package edu.uci.lighthouse.model.test;
 
 import junit.framework.TestCase;
 
 import org.dom4j.DocumentException;
 
 import edu.uci.lighthouse.model.LighthouseDelta;
 import edu.uci.lighthouse.model.LighthouseFile;
import edu.uci.lighthouse.model.io.LHDeltaXMLPersistence;
 import edu.uci.lighthouse.model.io.LighthouseFileXMLPersistence;
 import edu.uci.lighthouse.model.util.LHPreference;
 import edu.uci.lighthouse.test.util.LHTestDataFiles;
 
 public class LighthouseDeltaTest extends TestCase {
 
 	public void testAtmClassModification() throws DocumentException {
 		LighthouseFile atmClass = new LighthouseFile();
 		new LighthouseFileXMLPersistence(atmClass).load(LHTestDataFiles.XML_ATM_JAVA);
 		
 		LighthouseFile atmClassModified = new LighthouseFile();
 		new LighthouseFileXMLPersistence(atmClassModified).load(LHTestDataFiles.XML_ATM_JAVA_MODIFIED);
 		
 		// Generate delta
 		LighthouseDelta currentDelta = new LighthouseDelta(LHPreference.author, atmClass, atmClassModified);
 		
 		// Load delta from xml file
 		LighthouseDelta xmlDelta = new LighthouseDelta();
		new LHDeltaXMLPersistence(xmlDelta).load(LHTestDataFiles.XML_DELTA_ATM_CLASS);
 		
 		assertEquals(true,(currentDelta.getEvents().containsAll(xmlDelta.getEvents())));
 	}
 
 	public void testAtmAddEvents() throws DocumentException {
 		LighthouseFile atmClass = new LighthouseFile();
 		new LighthouseFileXMLPersistence(atmClass).load(LHTestDataFiles.XML_ATM_JAVA);
 		
 		// Generate delta
 		LighthouseDelta currentDelta = new LighthouseDelta(LHPreference.author, null, atmClass);
 		
 		// Load delta from xml file
 		LighthouseDelta xmlDelta = new LighthouseDelta();
		new LHDeltaXMLPersistence(xmlDelta).load(LHTestDataFiles.XML_DELTA_ATM_CLASS_JUST_ADD);
 		
 		assertEquals(true,(currentDelta.getEvents().containsAll(xmlDelta.getEvents())));
 	}
 	
 	public void testAtmRemoveEvents() throws DocumentException {
 		LighthouseFile atmClass = new LighthouseFile();
 		new LighthouseFileXMLPersistence(atmClass).load(LHTestDataFiles.XML_ATM_JAVA);
 		
 		// Generate delta
 		LighthouseDelta currentDelta = new LighthouseDelta (LHPreference.author, atmClass, null);
 		
 		// Load delta from xml file
 		LighthouseDelta xmlDelta = new LighthouseDelta();
		new LHDeltaXMLPersistence(xmlDelta).load(LHTestDataFiles.XML_DELTA_ATM_CLASS_JUST_REMOVE);
 		
 		assertEquals(true,(currentDelta.getEvents().containsAll(xmlDelta.getEvents())));
 	}
 	
 }
