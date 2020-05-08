 package org.ilrt.wf.facets.impl;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import org.ilrt.wf.facets.Facet;
 import org.ilrt.wf.facets.FacetEnvironment;
 import org.ilrt.wf.facets.FacetException;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.FacetQueryService.Tree;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.QNameUtility;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Mike Jones (mike.a.jones@bristol.ac.uk)
  */
 @RunWith(JMock.class)
 public class HierarchicalFacetTest extends AbstractFacetTest {
 
     @Before
     public void setUp() {
         mockFacetQueryService = context.mock(FacetQueryService.class);
         facetFactory = new FacetFactoryImpl(mockFacetQueryService, getPrefixMap());
         qNameUtility = new QNameUtility(getPrefixMap());
     }
 
     @Test
     public void createHierarchicalFacetWithoutParameter() throws FacetException {
 
         // the mock results that the service will return
         final List<Resource> resourceList = createMockRefinementsResourceListFromNodeA();
 
         // create the expectations for the service
         context.checking(new Expectations() {{
             oneOf(mockFacetQueryService)
                     .getRefinements(ResourceFactory.createResource(testFacetBase),
                             ResourceFactory.createProperty(testBroaderProperty), true);
             will(returnValue(resourceList));
         }});
 
         final Tree<Resource> tree = createTestTree();
 
         context.checking(new Expectations() {{
             oneOf(mockFacetQueryService).getHierarchy(ResourceFactory.createResource(testFacetBase),
                     ResourceFactory.createProperty(testBroaderProperty), true);
             will(returnValue(tree));
         }});
 
         // create a mock environment for the facet
         FacetEnvironment environment = new FacetEnvironmentImpl(createHierarchicalConfig(),
                 new HashMap<String, String[]>(), new HashMap<String, String>());
 
         // create the facet
         Facet facet = facetFactory.create(environment);
 
         // test the result
         assertNotNull("The facet should not be null", facet);
         assertEquals("Unexpected name", testName, facet.getName());
         assertEquals("Unexpected parameter name", testParamName, facet.getParam());
 
         // test the current state
         assertTrue(facet.getState().isRoot());
         assertNull(facet.getState().getParent());
         assertEquals(URI_A, qNameUtility.expandQName(facet.getState().getParamValue()));
 
         // test we have refinements
         assertEquals("Unexpected number of refinements", 2,
                 facet.getState().getRefinements().size());
 
         FacetState facetState = facet.getState().getRefinements().get(0);
         assertEquals("Unexpected label", label_B1, facetState.getName());
         assertEquals("unexpected value", qNameUtility.getQName(URI_B1),
                 facetState.getParamValue());
         assertEquals("Unexpected parent state", facet.getState(), facetState.getParent());
         assertFalse("State is not root", facetState.isRoot());
     }
 
     @Test
     public void createHierarchicalFacetWithParameter() throws FacetException {
 
         // the mock results that the service will return
         final List<Resource> resourceList = createMockRefinementsResourceListFromNodeC4();
 
         // create the expectations for the service
         context.checking(new Expectations() {{
             oneOf(mockFacetQueryService)
                     .getRefinements(ResourceFactory.createResource(URI_C4),
                             ResourceFactory.createProperty(testBroaderProperty), true);
             will(returnValue(resourceList));
         }});
 
         final Tree<Resource> tree = createTestTree();
 
         context.checking(new Expectations() {{
             oneOf(mockFacetQueryService).getHierarchy(ResourceFactory.createResource(URI_A),
                     ResourceFactory.createProperty(testBroaderProperty), true);
             will(returnValue(tree));
         }});
 
         // create some parameter values
         String[] paramValues = { qNameUtility.getQName(URI_C4 )};
         Map<String, String[]> params = new HashMap<String, String[]>();
         params.put(testParamName, paramValues);
 
         // create a mock environment for the facet
         FacetEnvironment environment = new FacetEnvironmentImpl(createHierarchicalConfig(),
                 params, new HashMap<String, String>());
 
         // create the facet
         Facet facet = facetFactory.create(environment);
 
         // test the result
         assertNotNull("The facet should not be null", facet);
         assertEquals("Unexpected name", testName, facet.getName());
         assertEquals("Unexpected parameter name", testParamName, facet.getParam());
 
         // test the current state
         FacetState facetState = facet.getState();
         assertFalse(facetState.isRoot());
         assertNotNull(facetState.getParent());
         assertEquals(URI_C4, qNameUtility.expandQName(facetState.getParamValue()));
 
         // test we have refinements
         assertEquals("Unexpected number of refinements", 1,
                 facet.getState().getRefinements().size());
 
         FacetState refinementFacetState = facet.getState().getRefinements().get(0);
         assertEquals("Unexpected label", label_D2, refinementFacetState.getName());
         assertEquals("unexpected value", qNameUtility.getQName(URI_D2),
                 refinementFacetState.getParamValue());
 
         FacetState parent_B2 = facetState.getParent();
         assertFalse(parent_B2.isRoot());
         assertEquals(label_B2, parent_B2.getName());
 
         FacetState parent_A = parent_B2.getParent();
         assertTrue(parent_A.isRoot());
         assertEquals(label_A, parent_A.getName());
 
     }
 
 
     @Test
     public void hierarchicalRefinements() {
 
         // we need a mock parent facet
         FacetState mockParentState = new FacetStateImpl();
 
         // we need to create some mock constraints
         ValueConstraint mockTypeConstraint = new ValueConstraint(RDF.type,
                 ResourceFactory.createProperty("http://example.org/#Thing"));
         List<? extends Constraint> mockConstraints =
                 Arrays.asList(mockTypeConstraint, mockTypeConstraint);
 
         // we need some mock resources that would have been returned from the service
         // *IF* we had called the public interface method
         List<Resource> resources = createMockRefinementsResourceListFromNodeA();
 
         // call the internal "protected" method and test the results
         List<FacetState> refinements = facetFactory.hierarchicalRefinements(resources,
                mockConstraints, mockParentState);
 
         // test the list
         assertEquals("Unexpected number of refinements", 2, refinements.size());
 
         // test the first object in the list
         FacetState state = refinements.get(0);
         assertFalse("Should not be root", state.isRoot());
         assertEquals("Unexpected name", label_B1, state.getName());
         assertEquals("Unexpected parameter value", qNameUtility.getQName(URI_B1),
                 state.getParamValue());
         assertEquals("Unexpected parent", mockParentState, state.getParent());
     }
 
 
     // ---------- private helper methods
 
     private Map<String, String> createHierarchicalConfig() {
 
         Map<String, String> config = new HashMap<String, String>();
         config.put(Facet.FACET_TYPE, Facet.HIERARCHICAL_FACET_TYPE);
         config.put(Facet.FACET_TITLE, testName);
         config.put(Facet.LINK_PROPERTY, testLinkProperty);
         config.put(Facet.BROADER_PROPERTY, testBroaderProperty);
         config.put(Facet.FACET_BASE, testFacetBase);
         config.put(Facet.CONSTRAINT_TYPE, testConstraintType);
         config.put(Facet.PARAM_NAME, testParamName);
         config.put(Facet.PREFIX, testPrefix);
         return config;
     }
 
 
     // ---------- private helper methods
 
     private List<Resource> createMockRefinementsResourceListFromNodeA() {
 
         Model model = ModelFactory.createDefaultModel();
 
         Resource resource_B1 = model.createResource(URI_B1);
         resource_B1.addLiteral(RDFS.label, label_B1);
 
         Resource resource_B2 = model.createResource(URI_B2);
         resource_B2.addLiteral(RDFS.label, label_B2);
 
         List<Resource> resources = new ArrayList<Resource>();
         resources.add(resource_B1);
         resources.add(resource_B2);
 
         return resources;
     }
 
     private List<Resource> createMockRefinementsResourceListFromNodeC4() {
 
         Model model = ModelFactory.createDefaultModel();
 
         Resource resource_D2 = model.createResource(URI_D2);
         resource_D2.addLiteral(RDFS.label, label_D2);
 
         List<Resource> resources = new ArrayList<Resource>();
         resources.add(resource_D2);
 
         return resources;
     }
 
 
     // ---------- private variables
 
     private final String testName = "Subjects";
     private final String testLinkProperty = "http://xmlns.com/foaf/0.1/topic_interest";
     private final String testBroaderProperty = "http://www.w3.org/2004/02/skos/core#broader";
     private final String testFacetBase = URI_A;
     private final String testConstraintType = "http://www.ilrt.bristol.ac.uk/iugo#MainEvent";
     private final String testParamName = "subjects";
     private final String testPrefix = "iugosubs";
 
     private final Mockery context = new JUnit4Mockery();
 
     private FacetQueryService mockFacetQueryService;
     private FacetFactoryImpl facetFactory;
     private QNameUtility qNameUtility;
 }
