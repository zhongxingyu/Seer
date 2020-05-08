 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.detailviews;
 
 import java.util.*;
 import javax.swing.JPanel;
 import javax.swing.JTree;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import org.joda.time.DateTime;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import ovation.*;
 import ovation.test.TestManager;
 import us.physion.ovation.detailviews.TreeWithTableRenderer.TableInTreeCellRenderer;
 import us.physion.ovation.interfaces.*;
 
 /**
  *
  * @author huecotanks
  */
 public class PropertyViewTest extends OvationTestCase{
     
     private TestEntityWrapper project;
     private TestEntityWrapper source;
     private TestEntityWrapper user1;
     private TestEntityWrapper user2;
     private Set<String> userURIs;
     
     static TestManager mgr = new SelectionViewTestManager();
     public PropertyViewTest() {
 	setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
     }
     
     @BeforeClass
     public static void setUpClass()
     {
         OvationTestCase.setUpDatabase(mgr, 5);
     }
     
     @Before
     public void setUp() throws UserAuthenticationException {
         dsc = setUpTest();
         Ovation.enableLogging(LogLevel.DEBUG);
 
         String UNUSED_NAME = "name";
         String UNUSED_PURPOSE = "purpose";
         DateTime UNUSED_START = new DateTime(0);
         byte[] data = {1, 2, 3, 4, 5};
         String uti = "unknown-uti";
         
         DataContext c = dsc.getContext();
         User newUser = c.addUser("newUser", "password");
         project = new TestEntityWrapper(dsc, c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START));
         source = new TestEntityWrapper(dsc, c.insertSource("source"));
         Project p = (Project)project.getEntity();
         p.addProperty("color", "yellow");
         p.addProperty("size", 10.5);
         Source s = (Source)source.getEntity();
         s.addProperty("id", 4);
         s.addProperty("birthday", "6/23/1988");
         
         user1 = new TestEntityWrapper(dsc, c.currentAuthenticatedUser());
         user2 = new TestEntityWrapper(dsc, newUser);
         userURIs = new HashSet();
         userURIs.add(user1.getURI());
         userURIs.add(user2.getURI());
         
         c.authenticateUser("newUser", "password");
         p.addProperty("color", "chartreuse");
         p.addProperty("interesting", true);
     }
     
     
     @After
     public void tearDown()
     {
         tearDownTest();
     }
     
     @AfterClass
     public static void tearDownClass() throws Exception {
         OvationTestCase.tearDownDatabase(mgr);
     }
 
 
     
     @Test
     public void testGetsProperTreeNodeStructure()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent t = new PropertiesViewTopComponent();
         assertTrue( t.getEntities() == null ||t.getEntities().isEmpty());
         t.update(entitySet, dsc.getContext());
         
         TableTree tt = new TableTree(t.getTreeRenderer(), userURIs);
         JTree tree = t.getTreeRenderer().getTree();
         DefaultMutableTreeNode n = (DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot();
         assertEquals(n.getChildCount(), 2);
 
         DefaultMutableTreeNode currentUserNode = (DefaultMutableTreeNode)n.getChildAt(0);
         DefaultMutableTreeNode otherUserNode = (DefaultMutableTreeNode)n.getChildAt(1);
         
         assertTrue(((DefaultMutableTreeNode)currentUserNode.getChildAt(0)) instanceof TableNode);
         assertEquals(currentUserNode.getChildCount(), 1);
         assertTrue(((DefaultMutableTreeNode)otherUserNode.getChildAt(0)) instanceof TableNode);
         assertEquals(otherUserNode.getChildCount(), 1);
     }
     
     @Test
     public void testGetsPropertiesAppropriatelyForEachUser()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         //user1 properties
         Set<TestProperty> props = t.getProperties(user1.getURI());
         Set<TestProperty> databaseProps = getAggregateUserProperties(((User)user1.getEntity()), entitySet);
         assertSetsEqual(props, databaseProps);
         
         //user2 properties
         props = t.getProperties(user1.getURI());
         databaseProps = getAggregateUserProperties(((User)user1.getEntity()), entitySet);
         assertSetsEqual(props, databaseProps);
         
     }
     
     @Test
     public void testSetsPropertiesByDifferentUsers()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         
         String userURI = c.currentAuthenticatedUser().getURIString();
         Set<TestProperty> props = t.getProperties(userURI);
         String key = props.iterator().next().getKey();
         
         String newValue = "something else";
         t.editProperty(userURI, key, newValue);        
         Set<TestProperty> databaseProps = getAggregateUserProperties(c.currentAuthenticatedUser(), entitySet);
         Set<TestProperty> matching = getPropertiesByKey(key, databaseProps);
         assertEquals(matching.size(), 1);
         assertEquals(matching.iterator().next().getValue(), newValue);
         
         matching = getPropertiesByKey(key, t.getProperties(userURI));
         assertEquals(matching.size(), 1);
         assertEquals(matching.iterator().next().getValue(), newValue);
     }  
     
     @Test
     public void testCantEditOtherUsersProperty()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = user2.getURI();
         //get the table for user2 and check that it's a NonEditableTable
         UserPropertySet s = t.getUserPropertySet(userURI);
         
         TableInTreeCellRenderer r = (TableInTreeCellRenderer)t.renderer.getTree().getCellRenderer();
         JPanel p = r.getPanelFromPropertySet(s, (TableNode)t.getUserNode(userURI).getChildAt(0), dsc);
         
         assertTrue(p instanceof NonEditableTable);
     }
    
     @Test
     public void testAddPropertyToEntity()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         assertTrue( tc.getEntities() ==null || tc.getEntities().isEmpty());
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = user1.getURI();
         Set<TestProperty> props = t.getProperties(userURI);
         String key = "a brand new key";
         Set<TestProperty> matchingKey = getPropertiesByKey(key, props);
         assertTrue(matchingKey.isEmpty());
         
         String newValue = "something else";
         t.addProperty(userURI, key, newValue);
         
         
         Set<TestProperty> databaseProps = getAggregateUserProperties(c.currentAuthenticatedUser(), entitySet);
         matchingKey = getPropertiesByKey(key, databaseProps);
         assertEquals(matchingKey.iterator().next().getValue(), newValue);
         
         matchingKey = getPropertiesByKey(key, t.getProperties(userURI));
         assertEquals(matchingKey.iterator().next().getValue(), newValue);
     } 
     
     @Test
     public void testAddPropertiesToMultipleEntities()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         assertTrue( tc.getEntities() == null || tc.getEntities().isEmpty());
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = user1.getURI();
         Set<TestProperty> props = t.getProperties(userURI);
         String key = "a brand new key";
         Set<TestProperty> matchingKey = getPropertiesByKey(key, props);
         assertTrue(matchingKey.isEmpty());
                 
         String newValue = "something else";
         t.addProperty(userURI, key, newValue);        
         
         assertEquals(project.getEntity().getMyProperties().get(key), newValue);
         assertEquals(source.getEntity().getMyProperties().get(key), newValue);
     } 
     
     @Test
     public void testAddPropertyToMultipleEntitiesEvenWhenOneEntityAlreadyHasProperty()
     {
          
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         assertTrue( tc.getEntities() == null || tc.getEntities().isEmpty());
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = user1.getURI();
         Set<TestProperty> props = t.getProperties(userURI);
         String key = "a brand new key";
         Set<TestProperty> matchingKey = getPropertiesByKey(key, props);
         assertTrue(matchingKey.isEmpty());
         
         project.getEntity().addProperty(key, 25.7); //now project contains a property with key, but source does not
         
         String newValue = "something else";
         t.addProperty(userURI, key, newValue);        
         
         assertEquals(project.getEntity().getMyProperties().get(key), newValue);
         assertEquals(source.getEntity().getMyProperties().get(key), newValue);
 
     } 
     
     @Test
     public void testRemovePropertyFromEntity()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = c.currentAuthenticatedUser().getURIString();
         Set<TestProperty> props = t.getProperties(userURI);
         String key = props.iterator().next().getKey();
         
         t.removeProperty(userURI, key);
         
         Set<TestProperty> databaseProps = getAggregateUserProperties(c.currentAuthenticatedUser(), entitySet);
         Set<TestProperty> matchingKey = getPropertiesByKey(key, databaseProps);
         assertTrue(matchingKey.isEmpty());
         
         matchingKey = getPropertiesByKey(key, t.getProperties(userURI));
         assertTrue(matchingKey.isEmpty());
     } 
     
     @Test
     public void testRemovePropertiesFromMultipleEntities()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = c.currentAuthenticatedUser().getURIString();
         Set<TestProperty> props = t.getProperties(userURI);
         String key = "a brand new key";
         Set<TestProperty> matchingKey = getPropertiesByKey(key, props);
         assertTrue(matchingKey.isEmpty());
         
         project.getEntity().addProperty(key, 27.8);
         source.getEntity().addProperty(key, 27.8);
         ((TableNode)t.getUserNode(userURI).getChildAt(0)).resetProperties(dsc);
         
         assertTrue(project.getEntity().getMyProperties().containsKey(key));
         assertTrue(source.getEntity().getMyProperties().containsKey(key));
         
         t.removeProperty(userURI, key);
         
         assertFalse(project.getEntity().getMyProperties().containsKey(key));
         assertFalse(source.getEntity().getMyProperties().containsKey(key));
     } 
     
     @Test
     public void testRemovePropertyFromMutlipleEntitesIfPropertyDidNotExistOnOneEntity()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = c.currentAuthenticatedUser().getURIString();
         Set<TestProperty> props = t.getProperties(userURI);
         
         String key = "a brand new key";
         Set<TestProperty> matchingKey = getPropertiesByKey(key, props);
         assertTrue(matchingKey.isEmpty());
 
         project.getEntity().addProperty(key, 27.8);
         ((TableNode)t.getUserNode(userURI).getChildAt(0)).resetProperties(dsc);
         
         
         t.removeProperty(userURI, key);
 
         assertFalse(project.getEntity().getMyProperties().containsKey(key));
         assertFalse(source.getEntity().getMyProperties().containsKey(key));
     }
 
     @Test
     public void testRemoveOnlySelectedKeyValuePairIfKeyExistsOnAnotherObject() {
         
         //this is the case where one object has a property key, value1
         // and another object has a property key, value2
         // and the user chooses to delete one of these pairs
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(project);
         entitySet.add(source);
         PropertiesViewTopComponent tc = new PropertiesViewTopComponent();
         tc.update(entitySet, dsc.getContext());
         
         DataContext c = dsc.getContext();
         TableTree t = new TableTree(tc.getTreeRenderer(), userURIs);
         
         String userURI = c.currentAuthenticatedUser().getURIString();
         Set<TestProperty> props = t.getProperties(userURI);
         
         String key = "a brand new key";
         Set<TestProperty> matchingKey = getPropertiesByKey(key, props);
         assertTrue(matchingKey.isEmpty());
 
         project.getEntity().addProperty(key, "thing1");
         source.getEntity().addProperty(key, "thing2");
         ((TableNode)t.getUserNode(userURI).getChildAt(0)).resetProperties(dsc);
 
         t.removeProperty(userURI, key, "thing2");
 
         assertTrue(project.getEntity().getMyProperties().containsKey(key));
         assertFalse(source.getEntity().getMyProperties().containsKey(key));
     }
 
     static Set<TestProperty> getAggregateUserProperties(User u, Set<IEntityWrapper> entities) {
         
         Set<TestProperty> databaseProps = new HashSet<TestProperty>();
         for (IEntityWrapper ew : entities) {
             Map<String, Object> props = ew.getEntity().getUserProperties(u);
             for (String key : props.keySet())
             {
                 databaseProps.add(new TestProperty(key, props.get(key)));
             }
         }
         return databaseProps;
     }
 
     void assertSetsEqual(Set s1, Set s2) {
         assertEquals(s1.size(), s2.size());
         for (Object t1 : s1)
         {
             for (Object t2 : s2)
             {
                 if (t1.equals(t2))
                 {
                     s2.remove(t2);
                     break;
                 }
             }    
                 
         }
         assertTrue(s2.isEmpty());
         //assertTrue(s1.containsAll(s2));
     }
     
     Set<TestProperty> getPropertiesByKey(String key, Set<TestProperty> props)
     {
         Set<TestProperty> result = new HashSet<TestProperty>();
         for (TestProperty p : props)
         {
             if (p.getKey().equals(key))
             {
                 result.add(p);
             }
         }
         return result;
     }
 
     class TableTree {
 
         TreeWithTableRenderer renderer;
 
         TableTree(TreeWithTableRenderer t, Set<String> userURIs) {
             renderer = t;
             int i = 1;
             for (String userURI : userURIs) {
                 TableNode node = ((TableNode) ((DefaultMutableTreeNode) getUserNode(userURI)).getChildAt(0));
                 ((TableInTreeCellRenderer) renderer.getTree().getCellRenderer()).getPanelFromPropertySet(getUserPropertySet(userURI), node, dsc);
             }
         }
 
         DefaultMutableTreeNode getUserNode(String userURI) {
             JTree tree = renderer.getTree();
             DefaultMutableTreeNode n = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel()).getRoot();
 
             if (userURI == null) {
                 return n;
             }
             for (int i = 0; i < n.getChildCount(); i++) {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) n.getChildAt(i);
                 UserPropertySet s = ((UserPropertySet) ((DefaultMutableTreeNode) node.getChildAt(0)).getUserObject());
                 if (s.getURI().equals(userURI)) {
                     return node;
                 }
             }
             return null;
         }
 
         UserPropertySet getUserPropertySet(String userURI) {
             JTree tree = renderer.getTree();
             DefaultMutableTreeNode n = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel()).getRoot();
 
             if (userURI == null) {
                 return null;
             }
             for (int i = 0; i < n.getChildCount(); i++) {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) n.getChildAt(i);
                 UserPropertySet s = ((UserPropertySet) ((DefaultMutableTreeNode) node.getChildAt(0)).getUserObject());
                 if (s.getURI().equals(userURI)) {
                     return s;
                 }
             }
             return null;
         }
 
         public Set<TestProperty> getProperties(String userURI) {
             JTree tree = renderer.getTree();
             DefaultMutableTreeNode n = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel()).getRoot();
 
             for (int i = 0; i < n.getChildCount(); i++) {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) n.getChildAt(i);
                 UserPropertySet s = ((UserPropertySet) ((DefaultMutableTreeNode) node.getChildAt(0)).getUserObject());
                 
                 if (s.getURI().equals(userURI)) {
                     Set<TestProperty> properties = new HashSet<TestProperty>();
                     DefaultTableModel m = ((DefaultTableModel) ((TableInTreeCellRenderer) tree.getCellRenderer()).getTableModel(s));
                     for (int j=0; j< m.getRowCount(); j++)
                     {
                         properties.add(new TestProperty((String)m.getValueAt(j, 0), m.getValueAt(j, 1)) );
                     }
                     return properties;
                 }
             }
             return null;
         }
 
         public void editProperty(String userURI, final String key, final Object value)  {
             JTree tree = renderer.getTree();
             UserPropertySet s = getUserPropertySet(userURI);
             final DefaultTableModel m = ((DefaultTableModel) ((TableInTreeCellRenderer) tree.getCellRenderer()).getTableModel(s));
                 EventQueueUtilities.runOffEDT(new Runnable() {
 
                     @Override
                     public void run() {
                         int firstRow = -1;
                         for (int i = 0; i < m.getRowCount(); i++) {
                             if (m.getValueAt(i, 0).equals(key)) {
                                 firstRow = i;
                                 m.setValueAt(value, i, 1);
                             }
                         }
                         if (firstRow < 0) {
                             Ovation.getLogger().debug("Property to edit doesn't exist, call 'addProperty' instead");
                             //throw new RuntimeException("Property to edit doesn't exist, call 'addProperty' instead");
                         }
 
                         boolean noListener = true;
                         for (TableModelListener l : m.getListeners(TableModelListener.class)) {
                             if (l instanceof PropertyTableModelListener) {
                                 noListener = false;
 
                                 TableModelEvent t = new TableModelEvent(m, firstRow, firstRow, 1, TableModelEvent.UPDATE);
                                 l.tableChanged(t);
                                 break;
                             }
                         }
                         if (noListener) {
                             Ovation.getLogger().debug("Property to edit doesn't exist, call 'addProperty' instead");
                             //throw new RuntimeException("No listener available for the TableModel");
                         }
                     }
                 });
         }
         
         public void addProperty(String userURI, final String key, final Object value) 
         {
             JTree tree = renderer.getTree();
             UserPropertySet s = getUserPropertySet(userURI);
             final DefaultTableModel m = ((DefaultTableModel)((TableInTreeCellRenderer)tree.getCellRenderer()).getTableModel(s));
                 EventQueueUtilities.runOffEDT(new Runnable(){
 
                     @Override
                     public void run() {
                         m.addRow(new Object[]{"", ""});
                         int row = m.getRowCount() - 1;
                         m.setValueAt(key, row, 0);
                         m.setValueAt(value, row, 1);
 
                         boolean noListener = true;
                         for (TableModelListener l : m.getListeners(TableModelListener.class)) {
                             if (l instanceof PropertyTableModelListener) {
                                 noListener = false;
 
                                 TableModelEvent t1 = new TableModelEvent(m, row, row, 0, TableModelEvent.UPDATE);
                                 TableModelEvent t2 = new TableModelEvent(m, row, row, 1, TableModelEvent.UPDATE);
                                 l.tableChanged(t1);
                                 l.tableChanged(t2);
                                 break;
                             }
                         }
                         if (noListener) {
                             Ovation.getLogger().debug("No listener available for the TableModel");
                             //throw new RuntimeException("No listener available for the TableModel");
                         }
                     }
                 });
         }
         
         public void removeProperty(String userURI, String key)
         {
             removeProperty(userURI, key, null);
         }
         
         public void removeProperty(String userURI, final String key, final Object value)
         {
             JTree tree = renderer.getTree();
             UserPropertySet s = getUserPropertySet(userURI);
             
             TableNode node = (TableNode)getUserNode(s.getURI()).getChildAt(0);
             node.resetProperties(dsc);
             ((TableInTreeCellRenderer)tree.getCellRenderer()).getPanelFromPropertySet(s, node, dsc);
             final DefaultTableModel m = ((DefaultTableModel)((TableInTreeCellRenderer)tree.getCellRenderer()).getTableModel(s));
                 EventQueueUtilities.runOffEDT(new Runnable(){
 
                     @Override
                     public void run() {
                         int row = -1;
                         for (int i = 0; i < m.getRowCount(); i++) {
                             if (m.getValueAt(i, 0).equals(key)) {
                                 if (value != null)
                                 {
                                     if (m.getValueAt(i, 1).equals(value) )
                                     {
                                         row = i;
                                         break;
                                     }
                                 }
                                 else{
                                     row = i;
                                     break;
                                 }
                             }
                         }
                         if (row <0)
                             return;//no row to delete 
                         
                         boolean noListener = true;
                         for (TableModelListener l : m.getListeners(TableModelListener.class)) {
                             if (l instanceof PropertyTableModelListener) {
                                 noListener = false;
 
                                ((PropertyTableModelListener) l).deleteProperty(m, row);
                                 break;
                             }
                         }
                         if (noListener) {
                              Ovation.getLogger().debug("No listener available for the TableModel");
                             //throw new RuntimeException("No listener available for the TableModel");
                         }
                     }
                 });
            
         }
     }
 }
