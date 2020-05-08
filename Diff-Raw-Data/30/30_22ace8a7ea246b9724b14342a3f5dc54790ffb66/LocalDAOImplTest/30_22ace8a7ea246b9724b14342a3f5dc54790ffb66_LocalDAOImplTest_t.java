 package com.szas.server.gwt.client;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class LocalDAOImplTest extends UniversalDAOImplTest {
 	private LocalDAO<MockElement> localDAO;
 	@Before
 	public void setUp() {
 		localDAO = new LocalDAOImpl<MockElement>();
 		universalDAO = localDAO;
 	}
 	@Test
	public void testSyncInfoInsered() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		localDAO.insert(element);
 		ArrayList<LocalTuple<MockElement>> elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 		
 		LocalTuple<MockElement> elementToSync = elementsToSync.get(0);
 		element.testEuality(elementToSync.getElement());
 		assertEquals(LocalTuple.Status.INSERTING,elementToSync.getStatus());
 	}
 	
 	@Test
	public void testSyncInfoUpdated() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		universalDAO.insert(element);
 		ArrayList<LocalTuple<MockElement>> elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 
 		LocalTuple<MockElement> elementToSync = elementsToSync.get(0);
 		
 		
 		elementToSync.setStatus(LocalTuple.Status.SYNCED);
		// TODO inform LocalDAO about changes state to SYNCED		
 		
 		element.data = NEW_EXAMPLE_DATA;
 		universalDAO.update(element);
 		
 		elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 		
 		elementToSync = elementsToSync.get(0);
 		element.testEuality(elementToSync.getElement());
 		assertEquals(LocalTuple.Status.UPDATING,elementToSync.getStatus());
 	}
 	
 	@Test
	public void testSyncInfoDeleted() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		universalDAO.insert(element);
 		ArrayList<LocalTuple<MockElement>> elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 
 		LocalTuple<MockElement> elementToSync = elementsToSync.get(0);
 		
 		
 		elementToSync.setStatus(LocalTuple.Status.SYNCED);
 		// TODO inform LocalDAO about changes state to SYNCED
 		
 		
 		universalDAO.delete(element);
 		
 		elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 		
 		elementToSync = elementsToSync.get(0);
 		element.testEuality(elementToSync.getElement());
 		assertEquals(LocalTuple.Status.DELETING,elementToSync.getStatus());
 	}
 }
