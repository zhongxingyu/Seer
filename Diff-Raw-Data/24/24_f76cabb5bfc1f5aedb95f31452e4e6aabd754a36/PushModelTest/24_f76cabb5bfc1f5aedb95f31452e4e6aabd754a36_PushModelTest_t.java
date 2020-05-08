 package edu.uci.lighthouse.core.controller.test;
 
 import java.io.IOException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.dom4j.DocumentException;
 
 import edu.uci.lighthouse.model.LighthouseDelta;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.io.LighthouseModelXMLPersistence;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.LHEventDAO;
 import edu.uci.lighthouse.test.util.LHTestDataFiles;
 import edu.uci.lighthouse.test.util.LighthouseModelTest;
 
 public class PushModelTest extends TestCase {
 
 	public void testUpdateModelFromDelta() throws DocumentException, IOException, JPAException {
 		LighthouseModelTest xmlModel = new LighthouseModelTest();
 		new LighthouseModelXMLPersistence(xmlModel).load(LHTestDataFiles.XML_LH_MODEL);
 		
 		// Save LH Model Into the database in order to help the
 		// BuilderRelationship to handle the External classes
 //		new LighthouseModelManager(xmlModel).saveEventsIntoDatabase(xmlModel.getListEvents());
 		
 		// Load delta from xml file
 		LighthouseDelta xmlDelta = new LighthouseDelta();
		//new LHDeltaXMLPersistence(xmlDelta).load(LHTestDataFiles.XML_DELTA_ATM_CLASS);
 		
 		// Update Model
		//new PushModel(xmlModel).updateModelFromDelta(xmlDelta);
 		
 		List<LighthouseEvent> listEvents = new LHEventDAO().list();
 		LighthouseModelTest databaseModel = new LighthouseModelTest();
 		for (LighthouseEvent event : listEvents) {
 			new LighthouseModelManager(databaseModel).addEvent(event);
 		}
 		
 		// Load LH Updated Model in XML file
 		LighthouseModelTest xmlUpdatedModel = new LighthouseModelTest();
 		new LighthouseModelXMLPersistence(xmlUpdatedModel).load(LHTestDataFiles.XML_UPDATED_MODEL);
 		
 		assertEquals(true, databaseModel.getListEvents().containsAll(xmlUpdatedModel.getListEvents()));
 	}
 	
 	public void testUpdateCommittedEvents() throws DocumentException, JPAException {
 //		LighthouseModelTest xmlModel = new LighthouseModelTest();
 //		new LighthouseModelXMLPersistence(xmlModel).load(LHTestDataFiles.XML_LH_MODEL);
 //		
 //		// Save LH Model Into the database in order to help the
 //		// BuilderRelationship to handle the External classes
 //		new LighthouseModelManager(xmlModel).saveEventsIntoDatabase(xmlModel.getListEvents());
 //		
 //		List<String> listClazzFqn = new ArrayList<String>();
 //		listClazzFqn.add("edu.prenticehall.deitel.Screen");
 //		listClazzFqn.add("edu.prenticehall.deitel.Transaction");
 //		new PushModel(xmlModel).updateCommittedEvents(listClazzFqn, "Max");
 //		
 //		String command = "SELECT e FROM LighthouseEvent e WHERE e.isCommitted=1";
 //		List<LighthouseEvent> listEvents = new LHEventDAO().executeDynamicQuery(command);
 //		assertEquals(true, listEvents.size()==12);
 	}
 
 }
