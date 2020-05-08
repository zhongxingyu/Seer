 package org.xbrlapi.fragment.tests;
 
 import org.xbrlapi.DOMLoadingTestCase;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.ReferencePart;
 import org.xbrlapi.ReferencePartDeclaration;
 import org.xbrlapi.ReferenceResource;
 
 /**
  * Tests the implementation of the org.xbrlapi.ReferencePart interface.
  * Uses the DOM-based data store to ensure rapid testing.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class ReferencePartTestCase extends DOMLoadingTestCase {
 	private final String STARTING_POINT = "test.data.reference.links";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURI(STARTING_POINT));		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public ReferencePartTestCase(String arg0) {
 		super(arg0);
 	}
 	
 	public void testGetValue() {	
 
 		try {
 			FragmentList<ReferenceResource> fragments = store.<ReferenceResource>getFragments("ReferenceResource");
 			assertTrue(fragments.getLength() > 0);
 			ReferenceResource fragment = fragments.getFragment(0);
 			
 			FragmentList<ReferencePart> parts = fragment.getReferenceParts();
 			assertTrue(parts.getLength() > 0);
 			
 			ReferencePart part = parts.getFragment(0);
			assertEquals("New Fixed Assets", part.getValue());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testGetReferencePartDeclaration() {	
 		try {
 			FragmentList<ReferenceResource> fragments = store.<ReferenceResource>getFragments("ReferenceResource");
 			ReferenceResource fragment = fragments.getFragment(0);			
 			ReferencePart part = fragment.getReferenceParts().getFragment(0);
 			store.serialize(part.getDataRootElement());
 			ReferencePartDeclaration declaration = part.getDeclaration();
 			assertEquals(part.getLocalname(),declaration.getName());
 			assertEquals(part.getNamespaceURI(),declaration.getTargetNamespaceURI());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}		
 }
