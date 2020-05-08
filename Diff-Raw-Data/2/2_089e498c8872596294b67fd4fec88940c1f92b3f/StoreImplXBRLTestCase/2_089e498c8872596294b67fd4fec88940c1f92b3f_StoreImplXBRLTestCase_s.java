 package org.xbrlapi.data.dom.tests;
 
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 
 import org.xbrlapi.Arc;
 import org.xbrlapi.ArcroleType;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.RoleType;
 import org.xbrlapi.utilities.Constants;
 
 /**
  * Test the XBRL Store implementation of the XBRL specific store functions.
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
 */
 
 public class StoreImplXBRLTestCase extends BaseTestCase {
 	
     private final String STARTING_POINTA = "test.data.custom.link.role";
 	private final String STARTING_POINTB = "test.data.custom.link.arcrole";
 	private final String STARTING_POINTC = "test.data.custom.resource.role";
     private final String STARTING_POINTD = "real.data.xbrl.2.1.roles";	
 
     private final String STARTING_POINT_INSTANCE_1 = "test.data.small.instance.1";
     private final String STARTING_POINT_INSTANCE_2 = "test.data.small.instance.2";
 		
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURI(STARTING_POINTA));
 		loader.discover(this.getURI(STARTING_POINTB));	
 		loader.discover(this.getURI(STARTING_POINTC));	
         loader.discover(this.getURI(STARTING_POINTD));
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public StoreImplXBRLTestCase(String arg0) {
 		super(arg0);
 	}
 
 	public void testGetLinkRoles() throws Exception {
 		try {
 			Set<URI> roles = store.getLinkRoles();
 			assertEquals(2, roles.size());
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 	public void testGetArcRoles() throws Exception {
 		try {
 			Set<URI> roles = store.getArcroles();
 			assertTrue(roles.size() > 7);
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testGetResourceRoles() throws Exception {
 		try {
 			List<URI> roles = store.getResourceRoles();
 			assertEquals(3, roles.size());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testGetRoleTypes() throws Exception {
 		try {
 			List<RoleType> roleTypes = store.getRoleTypes();
             logger.info(roleTypes.size());
 			assertTrue(roleTypes.size() > 40);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 
 	public void testGetArcroleTypes() throws Exception {
 		try {
 			List<ArcroleType> arcroleTypes = store.getArcroleTypes();
             for (ArcroleType at: arcroleTypes) {
                 logger.debug(at.getCustomURI());
             }
 			assertEquals(21, arcroleTypes.size());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testGetLinkrolesForAGivenArcrole() throws Exception {
 		try {
 			Set<URI> arcroles = store.getArcroles();
 			assertTrue(arcroles.size() > 0);
 			for (URI arcrole: arcroles) {
 				Set<URI> linkroles = store.getLinkRoles(arcrole);
 				logger.info(arcrole + " " + linkroles.size());
 			}
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}	
 	
 	public void testGetSpecificArcroleTypes() throws Exception {
 		try {
 			List<ArcroleType> arcroleTypes = store.getArcroleTypes(Constants.PresentationArcrole);
 			assertEquals(1, arcroleTypes.size());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testGetSpecificRoleTypes() throws Exception {
 		try {
 			List<RoleType> roleTypes = store.getRoleTypes(Constants.VerboseLabelRole);
 			assertEquals(1, roleTypes.size());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testCustomArcroleIsUsedCorrectly() throws Exception {
 		try {
 			List<ArcroleType> roleTypes = store.getArcroleTypes(Constants.PresentationArcrole);
 			ArcroleType type = roleTypes.get(0);
 			assertTrue(type.isUsedOn(Constants.XBRL21LinkNamespace,"presentationArc"));
 			assertFalse(type.isUsedOn(Constants.XBRL21LinkNamespace,"calculationArc"));
 			List<Arc> arcs = store.getXMLResources("Arc");
 			assertTrue(arcs.size() > 0);
 			for (Arc arc: arcs) {
 			    if (arc.getLocalname().equals("presentationArc"))
 			        assertTrue(type.isUsedCorrectly(arc));
 			    else
                     assertFalse(type.isUsedCorrectly(arc));
 			}
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}	
 
     public void testFilteredQuerying() throws Exception {
 
         URI uri1 = this.getURI(this.STARTING_POINT_INSTANCE_1);
         loader.discover(uri1);
 
         URI uri2 = this.getURI(this.STARTING_POINT_INSTANCE_2);
         loader.discover(uri2);
 
         logger.info("Done with loading the data.");
         
         List<Fragment> allFragments = store.queryForXMLResources("#roots#");
 
         logger.info("Total number of fragments: " + allFragments.size());
         
         Set<URI> allURIs = store.getDocumentURIs();
         logger.info("# URIs in store = " + allURIs.size());
        List<URI> uris = store.getMinimumDocumentSet(uri1);
         assertTrue(uris.size() > 1);
 
         assertTrue(allURIs.size() > uris.size());
         
         logger.info("Number of URIs in minimum set = " + uris.size());
         
         store.setFilteringURIs(uris);
         
         List<Fragment> filteredFragments = store.queryForXMLResources("#roots#");
 
         assertTrue(allFragments.size() > filteredFragments.size());        
         
         logger.info(allFragments.size());
         logger.info(filteredFragments.size());
         
     }
 	
 }
