 package ch.x42.terye;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import javax.jcr.ItemExistsException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class NodeTest extends BaseTest {
 
     private Node node1;
 
     @Override
     @Before
     public void setUp() throws RepositoryException {
         super.setUp();
         node1 = session.getNode("/node1");
     }
 
     @Test
     public void testAddNode() throws RepositoryException {
         Node node4 = node1.addNode("node4");
         assertEquals("node4", node4.getName());
         assertEquals("/node1/node4", node4.getPath());
     }
 
     @Test
     public void testAddNodeSave() throws RepositoryException {
         node1.addNode("node4");
         session.save();
 
         // verify existence of node in a new session
         Session session2 = repository.login();
         assertTrue(session2.nodeExists("/node1/node4"));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testAddINodellegalArgument() throws RepositoryException {
         node1.addNode("/not/relative");
     }
 
     @Test(expected = PathNotFoundException.class)
     public void testAddNodePathNotFound() throws RepositoryException {
         node1.addNode("node2/no/where");
     }
 
     @Test(expected = ItemExistsException.class)
     public void testAddNodeItemExists() throws RepositoryException {
         node1.addNode("node2");
     }
 
     @Test
     public void testGetNode() throws RepositoryException {
         Node node2 = node1.getNode("node2");
         assertEquals("node2", node2.getName());
         assertEquals("/node1/node2", node2.getPath());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetNodeIllegalArgument() throws RepositoryException {
         node1.getNode("/not/relative");
     }
 
     @Test(expected = PathNotFoundException.class)
     public void testGetNodePathNotFound() throws RepositoryException {
         node1.getNode("node2/no/where");
     }
 
     @Test
     public void testGetNodes() throws RepositoryException {
         NodeIterator iterator = node1.getNodes();
         assertEquals(2, iterator.getSize());
         Node node2 = iterator.nextNode();
         assertEquals("node2", node2.getName());
         assertEquals("/node1/node2", node2.getPath());
         Node node3 = iterator.nextNode();
         assertEquals("node3", node3.getName());
         assertEquals("/node1/node3", node3.getPath());
         assertFalse(iterator.hasNext());
     }
 
     @Test
     public void testHasNode() throws RepositoryException {
         assertTrue(node1.hasNode("node2"));
         assertTrue(node1.hasNode(".."));
         assertFalse(node1.hasNode("node4"));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testHasNodeIllegalArgument() throws RepositoryException {
         node1.hasNode("/not/relative");
     }
 
     @Test
     public void testHasNodes() throws RepositoryException {
         assertTrue(node1.hasNodes());
         Node node2 = node1.getNode("node2");
         assertFalse(node2.hasNodes());
     }
 
     @Test
     public void testSetProperty() throws RepositoryException {
         Property property = node1.setProperty("property4", "stuvwx");
         assertEquals("property4", property.getName());
         assertEquals("/node1/property4", property.getPath());
     }
 
     @Test
     public void testSetPropertySave() throws RepositoryException {
         node1.setProperty("property4", "stuvwx");
         session.save();
 
         // verify existence of node in a new session
         Session session2 = repository.login();
         assertTrue(session2.propertyExists("/node1/property4"));
     }
 
     @Ignore("to be done")
     @Test
     public void testSetPropertyIllegalArgument() {
 
     }
 
     @Test
     public void testGetProperty() throws RepositoryException {
         Property property2 = node1.getProperty("property2");
         assertEquals("property2", property2.getName());
         assertEquals("/node1/property2", property2.getPath());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetPropertyIllegalArgument() throws RepositoryException {
         node1.getProperty("/not/relative");
     }
 
     @Test(expected = PathNotFoundException.class)
     public void testGetPathNotFound() throws RepositoryException {
         node1.getProperty("node2/to/nowhere");
     }
 
     @Test
     public void testGetProperties() throws RepositoryException {
         PropertyIterator iterator = node1.getProperties();
         assertEquals(2, iterator.getSize());
         Property property2 = iterator.nextProperty();
         assertEquals("property2", property2.getName());
         assertEquals("/node1/property2", property2.getPath());
         Property property3 = iterator.nextProperty();
         assertEquals("property3", property3.getName());
         assertEquals("/node1/property3", property3.getPath());
         assertFalse(iterator.hasNext());
     }
 
     @Ignore("to be done")
     @Test
     public void testOverwriteProperty() throws RepositoryException {
 
     }
 
     @Test
     public void testHasProperty() throws RepositoryException {
         assertTrue(node1.hasProperty("property2"));
         assertTrue(node1.hasProperty("../property1"));
         assertFalse(node1.hasProperty("property4"));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testHasPropertyIllegalArgument() throws RepositoryException {
         node1.hasProperty("/not/relative");
     }
 
     @Test
     public void testHasProperties() throws RepositoryException {
         assertTrue(node1.hasProperties());
         Node node2 = node1.getNode("node2");
         assertFalse(node2.hasProperties());
     }
 
     @Test
     public void testGetParent() throws RepositoryException {
         Node parent = node1.getParent();
         assertEquals(root.getName(), parent.getName());
         assertEquals(root.getPath(), parent.getPath());
     }
 
    @Ignore("fix")
     @Test(expected = PathNotFoundException.class)
     public void testRemove() throws RepositoryException {
         node1.remove();
         assertFalse(session.itemExists("/node1"));
         assertFalse(session.itemExists("/node1/node2"));
         assertFalse(session.itemExists("/node1/node3"));
         assertFalse(session.itemExists("/node1/property2"));
         assertFalse(session.itemExists("/node1/property3"));
         assertFalse(root.hasNodes());
        root.getNode("/node1");
     }
 
     @Test
     public void testRemoveSave() throws RepositoryException {
         node1.remove();
         session.save();
 
         Session session2 = repository.login();
         assertFalse(session2.itemExists("/node1"));
         assertFalse(session2.itemExists("/node1/node2"));
         assertFalse(session2.itemExists("/node1/node3"));
         assertFalse(session2.itemExists("/node1/property2"));
         assertFalse(session2.itemExists("/node1/property3"));
         assertFalse(session2.getRootNode().hasNodes());
         session2.logout();
     }
 
     @Test
     public void testIsNode() throws RepositoryException {
         assertTrue(node1.isNode());
     }
 
 }
