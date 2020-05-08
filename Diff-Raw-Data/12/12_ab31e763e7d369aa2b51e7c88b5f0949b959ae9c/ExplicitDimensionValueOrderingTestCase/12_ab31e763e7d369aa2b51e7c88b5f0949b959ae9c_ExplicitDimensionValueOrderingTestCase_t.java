 package org.xbrlapi.xdt.values.tests;
 
import java.util.List;
 import java.util.TreeSet;
 
 import org.xbrlapi.Concept;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.Item;
 import org.xbrlapi.data.XBRLStore;
 import org.xbrlapi.networks.Network;
 import org.xbrlapi.networks.Networks;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.xdt.ExplicitDimension;
 import org.xbrlapi.xdt.XDTConstants;
 import org.xbrlapi.xdt.tests.BaseTestCase;
 import org.xbrlapi.xdt.values.DimensionValue;
 import org.xbrlapi.xdt.values.DimensionValueAccessor;
 import org.xbrlapi.xdt.values.DimensionValueAccessorImpl;
 import org.xbrlapi.xdt.values.DimensionValueComparator;
 import org.xbrlapi.xdt.values.ExplicitDimensionValueLabelComparator;
 import org.xbrlapi.xdt.values.ExplicitDimensionValueTreeComparator;
 
 /**
  * Tests the identification of XDT fragments.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class ExplicitDimensionValueOrderingTestCase extends BaseTestCase {
 
     private final String STARTING_POINT = "test.data.local.xdt.several.explicit.dimension.values";
     
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 	}	
 	
 	public ExplicitDimensionValueOrderingTestCase(String arg0) {
 		super(arg0);
 	}
 
 	public void testExplicitDimensionValueOrdering() {
     
     	try {
     
             DimensionValueAccessor dva = new DimensionValueAccessorImpl();
     
             String uri = this.getURI(STARTING_POINT);
 
             loader.discover(uri);
             
             FragmentList<ExplicitDimension> dimensions = store.<ExplicitDimension>getFragments("org.xbrlapi.xdt.ExplicitDimensionImpl");
             assertEquals(2,dimensions.getLength());
             FragmentList<Item> items = ((XBRLStore) store).getItems();
             assertEquals(3,items.getLength());
 
             TreeSet <DimensionValue> values = new TreeSet<DimensionValue>(new DimensionValueComparator());
 
             for (Item item: items) {
                 for (ExplicitDimension dimension: dimensions) {
                     DimensionValue value = dva.getValue(item,dimension);
                     if (value != null) {
                         assertTrue(value.isExplicitDimensionValue());
                         assertTrue(value.getValue() != null);
                         values.add(value);
                     }
                 }
             }
             
             assertEquals(6, values.size());
             
             for (DimensionValue value: values) {
                 store.serialize(value.getItem().getDataRootElement());
                 store.serialize(value.getDimension().getDataRootElement());
                 store.serialize(((Concept) value.getValue()).getDataRootElement());
             }
             
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail(e.getMessage());
     	}
     }
 
     public void testExplicitDimensionValueOrderingByLabel() {
         
         try {
     
             DimensionValueAccessor dva = new DimensionValueAccessorImpl();
     
             String uri = this.getURI(STARTING_POINT);
 
             loader.discover(uri);
             
             FragmentList<ExplicitDimension> dimensions = store.<ExplicitDimension>getFragments("org.xbrlapi.xdt.ExplicitDimensionImpl");
             assertEquals(2,dimensions.getLength());
             FragmentList<Item> items = ((XBRLStore) store).getItems();
             assertEquals(3,items.getLength());
 
             TreeSet <DimensionValue> values = new TreeSet<DimensionValue>(new ExplicitDimensionValueLabelComparator("en",Constants.StandardLabelRole));
 
             for (Item item: items) {
                 for (ExplicitDimension dimension: dimensions) {
                     DimensionValue value = dva.getValue(item,dimension);
                     if (value != null) {
                         assertTrue(value.isExplicitDimensionValue());
                         assertTrue(value.getValue() != null);
                         values.add(value);
                     }
                 }
             }
             
             assertEquals(6, values.size());
             
             for (DimensionValue value: values) {
 //                store.serialize(value.getItem().getDataRootElement());
 //                store.serialize(value.getDimension().getDataRootElement());
                 store.serialize(((Concept) value.getValue()).getDataRootElement());
             }
             
         } catch (Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }    
 	
 
     public void testExplicitDimensionValueOrderingByTree() {
         
         try {
     
             DimensionValueAccessor dva = new DimensionValueAccessorImpl();
 
             String uri = this.getURI(STARTING_POINT);
 
             loader.discover(uri);

            List<String> uris = store.getStoredURIs();
            for (String myURI: uris) logger.info(myURI);

            FragmentList<Concept> concepts = store.<Concept>getFragments("Concept");
            for (Concept c: concepts) store.serialize(c.getDataRootElement());
            
             
             FragmentList<ExplicitDimension> dimensions = store.<ExplicitDimension>getFragments("org.xbrlapi.xdt.ExplicitDimensionImpl");
             assertEquals(2,dimensions.getLength());
             FragmentList<Item> items = ((XBRLStore) store).getItems();
             assertEquals(3,items.getLength());
 
             Networks networks = store.getNetworks(Constants.StandardLinkRole,XDTConstants.domainMemberArcrole);
             assertEquals(1,networks.getSize());
             Network network = networks.getNetwork(XDTConstants.domainMemberArcrole,Constants.StandardLinkRole);
             assertNotNull(network);
            Concept root = ((XBRLStore) store).getConcept("http://xbrlapi.org/test/xdt/001","dom1");
 
             TreeSet <DimensionValue> values = new TreeSet<DimensionValue>(new ExplicitDimensionValueTreeComparator(network, root));
 
             for (Item item: items) {
                 for (ExplicitDimension dimension: dimensions) {
                     DimensionValue value = dva.getValue(item,dimension);
                     if (value != null) {
                         assertTrue(value.isExplicitDimensionValue());
                         assertTrue(value.getValue() != null);
                         values.add(value);
                     }
                 }
             }
             
             assertEquals(6, values.size());
             
             for (DimensionValue value: values) {
 //                store.serialize(value.getItem().getDataRootElement());
 //                store.serialize(value.getDimension().getDataRootElement());
                 store.serialize(((Concept) value.getValue()).getDataRootElement());
             }
             
         } catch (Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }    
         
 	
 
 }
