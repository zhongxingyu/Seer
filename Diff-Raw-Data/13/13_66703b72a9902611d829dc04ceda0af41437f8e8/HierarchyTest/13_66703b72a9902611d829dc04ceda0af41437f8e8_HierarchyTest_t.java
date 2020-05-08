 package org.sakaiproject.hierarchy.test;
 
 import org.sakaiproject.hierarchy.api.HierarchyServiceException;
 import org.sakaiproject.hierarchy.api.model.Hierarchy;
 
 
 abstract public class HierarchyTest extends ServiceTest {
 
 	private Hierarchy root;
 	
 	public void setUp() throws Exception {
 		super.setUp();
 		createRoot();
 		service.begin();
 	}
 	
 	public void tearDown() throws Exception {
 		service.end();
		super.tearDown();
 	}
 
 	private void createRoot() throws HierarchyServiceException {
 		try {
 			service.begin();
 			root = service.newHierarchy("/root");
 			service.save(root);
 		} finally {
 			service.end();
 		}
 	}
 	
 	private Hierarchy createChild(Hierarchy root){
 		Hierarchy node = null;
 		try {
 			service.begin();
 			node = service.newHierarchy("/root/child");
 			root.addTochildren(node);
 			service.save(node);
 		} catch(HierarchyServiceException hse) {
 			fail("Could not create child node.");
 		} finally {
 			service.end();
 		}
 		return node;
 	}
 	
 	private Hierarchy loadRoot() {
 		return service.getNode("/root");
 	}
 	
 	public void testGetPath() {
 		assertEquals("/root", loadRoot().getPath());
 	}
 	
 	public void testGetParent(){
 		assertNull(loadRoot().getParent());
 		Hierarchy child = createChild(root);
 		assertEquals(root.getId(), child.getParent().getId());
 	}
 	
 	public void testGetChildren() {
 		assertNotNull(root.getChildren());
 		assertNotNull(loadRoot().getChildren());
 		assertEquals(0,root.getChildren().size());
 		createChild(root);
		root = loadRoot();
 		assertNotNull(root.getChildren());
 		assertNotNull(loadRoot().getChildren());
 		assertEquals(1,root.getChildren().size());
 	}
 	
 	public void testAddToProperties() {
 		assertNull(root.getProperty("prop"));
 		root.addToproperties("prop", "value");
 		service.save(root);
		root = loadRoot();
 		assertEquals("value", root.getProperty("prop").getPropvalue());
 		root.addToproperties("prop", "different");
 		service.save(root);
		root = loadRoot();
 		assertEquals("different", root.getProperty("prop").getPropvalue());
 	}
 	
 	public void testGetChild() {
 		Hierarchy child = createChild(root);
		root = loadRoot();
 		assertEquals(child.getId(), root.getChild("/root/child").getId());
 		assertNull(root.getChild("/root/nothere"));
 	}
 	
 	
 }
