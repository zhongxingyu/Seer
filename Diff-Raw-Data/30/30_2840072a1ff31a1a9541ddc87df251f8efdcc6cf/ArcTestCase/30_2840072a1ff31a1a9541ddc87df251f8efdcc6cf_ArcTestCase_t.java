 package org.xbrlapi.fragment.tests;
 
 import java.util.List;
 
 import org.xbrlapi.Arc;
 import org.xbrlapi.DOMLoadingTestCase;
 
 /**
  * Tests the implementation of the org.xbrlapi.Arc interface.
  * Uses the DOM-based data store to ensure rapid testing.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class ArcTestCase extends DOMLoadingTestCase {
 	private final String STARTING_POINT = "test.data.multi.concept.schema";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURI(STARTING_POINT));		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public ArcTestCase(String arg0) {
 		super(arg0);
 	}
 	
 	/**
 	 * Test getting the xlink:to attribute value.
 	 */
 	public void testGetTo() {	
 
 		try {
 			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertEquals("contributingItem", fragment.getTo());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}	
 
 	/**
 	 * Test getting xlink from value.
 	 */
 	public void testGetFrom() {	
 
 		try {
			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertEquals("summationItem", fragment.getFrom());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test getting xlink show value.
 	 */
 	public void testGetShow() {	
 
 		try {
 			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertNull(fragment.getShow());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}		
 	
 	/**
 	 * Test getting xlink actuate value.
 	 */
 	public void testGetActuate() {	
 
 		try {
 			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertNull(fragment.getActuate());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 	/**
 	 * Test getting xbrl order attribute value.
 	 */
 	public void testGetOrder() {	
 
 		try {
 			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertEquals(new Double(1.0), fragment.getOrder());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 	/**
 	 * Test getting list of fragments that the arc runs from.
 	 */
 	public void testGetSourceFragments() {	
 
 		try {
 			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertEquals(1, fragment.getSourceFragments().size());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test getting list of fragments that the arc runs to.
 	 */
 	public void testGetTargets() {	
 
 		try {
 			List<Arc> fragments = store.<Arc>queryForXMLResources("#roots#[*/*/@xlink:type='arc']");
 			Arc fragment = fragments.get(0);
 			assertEquals(2, fragment.getTargetFragments().size());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}	
 }
