 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.detailviews;
 
 import java.util.*;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import org.joda.time.DateTime;
 import org.junit.*;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.AbstractLookup;
 import org.openide.util.lookup.InstanceContent;
 import org.openide.util.lookup.ServiceProvider;
 import ovation.*;
 import ovation.test.TestManager;
 import us.physion.ovation.interfaces.*;
 import org.junit.AfterClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 
 @ServiceProvider(service = Lookup.Provider.class)
 /**
  *
  * @author huecotanks
  */
 public class ParameterViewTest extends OvationTestCase implements Lookup.Provider, ConnectionProvider{
         
     private Lookup l;
     InstanceContent ic;
     private TestEntityWrapper e1;
     private TestEntityWrapper e2;
     private ParametersTopComponent t;
     
     static TestManager mgr = new SelectionViewTestManager();
     public ParameterViewTest() {
 	setTestManager(mgr); //this is because there are static and non-static methods that need to use the test manager
         ic = new InstanceContent();
         l = new AbstractLookup(ic);
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
         Project p = c.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
         Source s = c.insertSource("source");
         HashMap params = new HashMap();
         params.put("color", "yellow");
         params.put("size", 10.5);
         e1 = new TestEntityWrapper(dsc, p.insertExperiment("purpose", UNUSED_START).insertEpochGroup(s, "label", UNUSED_START).insertEpoch(UNUSED_START, UNUSED_START, "protocolID", params));
         params = new HashMap();
         params.put("id", 4);
         params.put("birthday", "6/23/1988");
         e2 = new TestEntityWrapper(dsc, p.insertExperiment("purpose", UNUSED_START).insertEpochGroup(s, "label", UNUSED_START).insertEpoch(UNUSED_START, UNUSED_START, "protocolID", params));
 
         t = new ParametersTopComponent();
         t.setTableTree(new DummyTableTree());
         ic.add(this);
 
         Lookup.getDefault().lookup(ConnectionProvider.class);
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
 
     /*TODO: test this somewhere
      * @Test
     public void testGetsProperTreeNodeStructure() throws InterruptedException {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
 
         entitySet.add(e1);
         entitySet.add(e2);
         assertTrue(t.getEntities() == null || t.getEntities().isEmpty());
         t.setEntities(entitySet);
 
         JTree tree = t.getTableTree().getTree();
         DefaultMutableTreeNode n = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel()).getRoot();
         assertEquals(n.getChildCount(), 1);
 
         DefaultMutableTreeNode currentUserNode = (DefaultMutableTreeNode) n.getChildAt(0);
 
         assertTrue(((DefaultMutableTreeNode) currentUserNode.getChildAt(0)) instanceof TableNode);
         assertEquals(currentUserNode.getChildCount(), 1);
     }*/
     
     @Test
     public void testGetsParametersAppropriatelyForEpochs()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
        
         entitySet.add(e1);
         entitySet.add(e2);
         List<TableTreeKey> params = t.setEntities(entitySet);
         
         assertEquals(params.size(), 1);
         assertTrue(params.get(0) instanceof ParameterSet);
         ParameterSet protocolParams = (ParameterSet)params.get(0);
         assertEquals(protocolParams.getDisplayName(), "Protocol Parameters");
         assertEquals(protocolParams.isEditable(), false);
         Set<TestTuple> databaseParams = new HashSet<TestTuple>();
         for (IEntityWrapper ew : entitySet)
         {
             Map<String, Object> ps = ((Epoch)ew.getEntity()).getProtocolParameters();
             aggregateDatabaseParams(databaseParams, ps);
         }
         assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(protocolParams), databaseParams));
     }
     
     @Test
     public void testGetsParametersAppropriatelyForResponses()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         Epoch e = ((Epoch)e1.getEntity());
         Map<String, Object> deviceParameters = new HashMap();
         deviceParameters.put("one", 1);
         deviceParameters.put("two", "fish");
         Response r1 = e.insertResponse(e.getEpochGroup().getExperiment().externalDevice("name", "manufacturer"), deviceParameters, new NumericData(new double[]{1, 2, 3}), "units", "label", 10, "hz", Response.NUMERIC_DATA_UTI);
         e = ((Epoch)e2.getEntity());
         deviceParameters.put("one", 2);
         Response r2 = e.insertResponse(e.getEpochGroup().getExperiment().externalDevice("name", "manufacturer"), deviceParameters, new NumericData(new double[]{1, 2, 3}), "units", "label", 10, "hz", Response.NUMERIC_DATA_UTI);
         entitySet.add(new TestEntityWrapper(dsc, r1));
         entitySet.add(new TestEntityWrapper(dsc, r2));
 
         List<TableTreeKey> params = t.setEntities(entitySet);
         
         assertEquals(params.size(), 1);
         assertTrue(params.get(0) instanceof ParameterSet);
         ParameterSet protocolParams = (ParameterSet)params.get(0);
         assertEquals(protocolParams.getDisplayName(), "Device Parameters");
         assertEquals(protocolParams.isEditable(), false);
         Set<TestTuple> databaseParams = new HashSet<TestTuple>();
         for (IEntityWrapper ew : entitySet)
         {
             Map<String, Object> ps = ((Response)ew.getEntity()).getDeviceParameters();
             aggregateDatabaseParams(databaseParams, ps);
 
         }
         assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(protocolParams), databaseParams));
     }
     
     @Test
     public void testGetsParametersAppropriatelyForAnalysisRecords()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         Epoch e = ((Epoch)e1.getEntity());
         Map<String, Object> deviceParameters = new HashMap();
         deviceParameters.put("one", 1);
         deviceParameters.put("two", "fish");
         AnalysisRecord a1 = e.getEpochGroup().getExperiment().getProjects()[0].insertAnalysisRecord("name", new Epoch[]{e}, "function name", deviceParameters, "file:///URL", "revision");
         e = ((Epoch)e2.getEntity());
         deviceParameters.put("one", 2);
         AnalysisRecord a2 = e.getEpochGroup().getExperiment().getProjects()[0].insertAnalysisRecord("name2", new Epoch[]{e}, "function name", deviceParameters, "file:///URL", "revision");
         entitySet.add(new TestEntityWrapper(dsc, a1));
         entitySet.add(new TestEntityWrapper(dsc, a2));
        
         List<TableTreeKey> params = t.setEntities(entitySet);
         
         assertEquals(params.size(), 1);
         assertTrue(params.get(0) instanceof ParameterSet);
         ParameterSet protocolParams = (ParameterSet)params.get(0);
         assertEquals(protocolParams.getDisplayName(), "Analysis Parameters");
         assertEquals(protocolParams.isEditable(), false);
         Set<TestTuple> databaseParams = new HashSet<TestTuple>();
         for (IEntityWrapper ew : entitySet)
         {
             Map<String, Object> ps = ((AnalysisRecord)ew.getEntity()).getAnalysisParameters();
             aggregateDatabaseParams(databaseParams, ps);
             
         }
         assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(protocolParams), databaseParams));
     }
     
     @Test
     public void testGetsParametersAppropriatelyForStimulus()
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();
         Epoch e = ((Epoch)e1.getEntity());
         Map<String, Object> deviceParameters = new HashMap();
         deviceParameters.put("one", 1);
         deviceParameters.put("two", "fish");
         Map<String, Object> parameters = new HashMap();
         parameters.put("one", "fish");
         parameters.put("another", "fish");
         Stimulus r1 = e.insertStimulus(e.getEpochGroup().getExperiment().externalDevice("name", "manufacturer"), deviceParameters, "pluginID", parameters, "units", new String[]{"thing1"});
         e = ((Epoch)e2.getEntity());
         deviceParameters.put("one", 2);
         parameters.put("red", "fish");
         parameters.put("blue", "fish");
         Stimulus r2 = e.insertStimulus(e.getEpochGroup().getExperiment().externalDevice("name", "manufacturer"), deviceParameters, "pluginID", parameters, "units", new String[]{"thing1"});
         entitySet.add(new TestEntityWrapper(dsc, r1));
         entitySet.add(new TestEntityWrapper(dsc, r2));
         List<TableTreeKey> params = t.setEntities(entitySet);
         
         assertEquals(params.size(), 2);
         assertTrue(params.get(0) instanceof ParameterSet);
         assertTrue(params.get(1) instanceof ParameterSet);
        
         //device params
         ParameterSet deviceParams = (ParameterSet)params.get(0);
         assertEquals(deviceParams.getDisplayName(), "Device Parameters");
         assertEquals(deviceParams.isEditable(), false);
         Set<TestTuple> databaseParams = new HashSet<TestTuple>();
         for (IEntityWrapper ew : entitySet)
         {
             Map<String, Object> ps = ((Stimulus)ew.getEntity()).getDeviceParameters();
             aggregateDatabaseParams(databaseParams, ps);
         }
         assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(deviceParams), databaseParams));
         
         // stimulus params
         ParameterSet stimulusParams = (ParameterSet)params.get(1);
         assertEquals(stimulusParams.getDisplayName(), "Stimulus Parameters");
         assertEquals(stimulusParams.isEditable(), false);
         databaseParams = new HashSet<TestTuple>();
         for (IEntityWrapper ew : entitySet)
         {
             Map<String, Object> ps = ((Stimulus)ew.getEntity()).getStimulusParameters();
             aggregateDatabaseParams(databaseParams, ps);
         }
         assertTrue(TableTreeUtils.setsEqual(TableTreeUtils.getTuples(stimulusParams), databaseParams));
     }
 
     @Override
     public Lookup getLookup() {
         return l;
     }
 
     @Override
     public IAuthenticatedDataStoreCoordinator getConnection() {
         return dsc;
     }
 
     @Override
     public void addConnectionListener(ConnectionListener cl) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void removeConnectionListener(ConnectionListener cl) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
         static Set<TestTuple> getAggregateUserProperties(User u, Set<IEntityWrapper> entities) {
         
         Set<TestTuple> databaseProps = new HashSet<TestTuple>();
         for (IEntityWrapper ew : entities) {
             Map<String, Object> props = ew.getEntity().getUserProperties(u);
             for (String key : props.keySet())
             {
                 databaseProps.add(new TestTuple(key, props.get(key)));
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
     
     Set<TestTuple> getPropertiesByKey(String key, Set<TestTuple> props)
     {
         Set<TestTuple> result = new HashSet<TestTuple>();
         for (TestTuple p : props)
         {
             if (p.getKey().equals(key))
             {
                 result.add(p);
             }
         }
         return result;
     }
     
     private Set<TestTuple> getProperties(ScrollableTableTree t, String userURI) {
         Set<TestTuple> properties = new HashSet<TestTuple>();
         TableTreeKey k = t.getTableKey(userURI);
         if (k == null)
         {
             return properties;
         }    
          
         Object[][] data = k.getData();
         //DefaultTableModel m = ((DefaultTableModel) ((TableInTreeCellRenderer) t.getTree().getCellRenderer()).getTableModel(k));
         for (int i  = 0; i < data.length; ++i)
         {
             properties.add(new TestTuple((String) data[i][0], data[i][1]));
         }
         return properties;
     }
 
     @Override
     public void resetConnection() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private void aggregateDatabaseParams(Set<TestTuple> databaseParams, Map<String, Object> ps) {
         for (String key : ps.keySet())
             {
                 if (!TableTreeUtils.getTuplesByKey(key, databaseParams).isEmpty())
                 {
                     TestTuple tuple = TableTreeUtils.getTuplesByKey(key, databaseParams).iterator().next();
                     MultiUserParameter p = new MultiUserParameter(tuple.getValue());
                     p.add(ps.get(tuple.getKey()));
                     databaseParams.remove(tuple);
                     databaseParams.add(new TestTuple(tuple.getKey(), p));
                 }
                 else{
                     databaseParams.add(new TestTuple(key, ps.get(key)));
                 }
             }
     }
 }
