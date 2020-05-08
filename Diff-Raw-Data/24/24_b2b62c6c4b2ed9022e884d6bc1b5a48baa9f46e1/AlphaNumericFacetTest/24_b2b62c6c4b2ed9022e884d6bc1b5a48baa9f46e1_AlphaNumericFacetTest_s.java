 package org.ilrt.wf.facets.impl;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.vocabulary.RDF;
 import org.ilrt.wf.facets.Facet;
 import org.ilrt.wf.facets.FacetEnvironment;
 import org.ilrt.wf.facets.FacetException;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.RegexpConstraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author Mike Jones (mike.a.jones@bristol.ac.uk)
  */
 @RunWith(JMock.class)
 public class AlphaNumericFacetTest extends AbstractFacetTest {
 
     @Before
     public void setUp() {
         mockFacetQueryService = context.mock(FacetQueryService.class);
         facetFactory = new FacetFactoryImpl(mockFacetQueryService, getPrefixMap());
     }
 
     /**
      * Is label for a character in the correct format?
      */
     @Test
     public void alphaNumericLabel() {
 
         assertEquals("Unexpected label", label, facetFactory.alphaNumericLabel(c));
     }
 
     /**
      * Is the constraint for a character in the correct format?
      */
     @Test
     public void alphaNumericConstraint() {
 
         assertEquals("Unexpected constraint", constraint,
                 facetFactory.alphaNumericConstraint(ResourceFactory.
                         createProperty(linkProperty), c).getRegexp());
     }
 
     /**
      * Is the constraint for a character in the correct format?
      */
     @Test
     public void alphaNumericConstraintFromLabel() {
 
         assertEquals("Unexpected constraint", constraint,
                 facetFactory.alphaNumericConstraint(ResourceFactory.
                         createProperty(linkProperty), label).getRegexp());
     }
 
     /**
      * Is the alpha-numeric array is correct?
      */
     @Test
     public void expectedAlphaNumericArray() {
 
         assertEquals("Unexpected array size", MAX_ALPHANUMERIC_ITEMS,
                 facetFactory.alphaNumericArray().length);
     }
 
     /**
      * Test the refinements for an alpha-numeric facet are correct.
      */
     @Test
     public void alphaNumericRefinements() {
 
         // pseudo root state
         final FacetStateImpl rootState = new FacetStateImpl();
         rootState.setRoot(true);
 
         // property used in each state
         Property p = ResourceFactory.createProperty(linkProperty);
 
         // get the states
         List<FacetState> states =
                 facetFactory.alphaNumericRefinements(new ValueConstraint(RDF.type,
                         ResourceFactory.createProperty(typeProperty)), p, rootState);
 
         // check we have the expected number of elements
         assertEquals("Unexpected array size", MAX_ALPHANUMERIC_ITEMS,
                 states.size());
 
         // check one of the items ...
         FacetStateImpl state = (FacetStateImpl) states.get(10);
         assertEquals("Unexpected label", label, state.getName());
         assertEquals("Unexpected number of constraints", 2, state.getConstraints().size());
         assertFalse("The state should not assert that it is a parent", state.isRoot());
         assertTrue("The parent should assert it is the root", state.getParent().isRoot());
         assertEquals("Unexpected param value", label, state.getParamValue());
     }
 
     @Test
     public void calculateCountAlphaNumericRefinements() throws FacetException {
 
         // create mock states
         final FacetStateImpl mockRootState = new FacetStateImpl(null, null, null, null);
         final FacetState mockRefinementState =
                 new FacetStateImpl(label, mockRootState, label, new ArrayList<Constraint>());
         mockRootState.getRefinements().add(mockRefinementState);
 
         // service expects a list of current (parent states)
         final List<FacetState> rootStateList = new ArrayList<FacetState>();
         rootStateList.add(mockRootState);
 
         // mock the results returned by the service
         final Map<FacetState, Integer> results = getCounts(rootStateList);
 
         // set the expectations for the service
         context.checking(new Expectations() {{
             oneOf(mockFacetQueryService).getCounts(rootStateList);
             will(returnValue(results));
         }});
 
         // mock the facet and add it to the refinementStateList
         Facet mockFacet = new FacetImpl(alphaNumericTestName, mockRootState,
                 alphaNumericTestParamName);
         final List<Facet> facets = new ArrayList<Facet>();
         facets.add(mockFacet);
 
         // sanity check we have constructed the mock objects
         assertEquals("Unexpected facet refinementStateList size", 1, facets.size());
         assertEquals("Unexpected facet", mockFacet, facets.get(0));
         assertEquals("Unexpected facet state", mockRootState, facets.get(0).getState());
         assertEquals("Unexpected refinement state", mockRefinementState,
                 facets.get(0).getState().getRefinements().get(0));
 
         // update the count
         facetFactory.calculateCount(currentStates(facets));
 
         assertEquals("Unexpected count", 5,
                 facets.get(0).getState().getRefinements().get(0).getCount());
     }
 
     @Test
     public void createAlphaNumericFacetWithoutParameter() throws FacetException {
 
 
         FacetEnvironment environment = new FacetEnvironmentImpl(createAlphaNumericConfig(),
                 new HashMap<String, String[]>(), new HashMap<String, String>());
 
         // testing the facet values
 
         Facet facet = facetFactory.create(environment);
 
         assertNotNull("The facet should not be null", facet);
         assertEquals("Unexpected Name", alphaNumericTestName, facet.getName());
         assertEquals("Unexpected parameter name", alphaNumericTestParamName, facet.getParam());
 
         // testing the state of the facet
 
         FacetState facetState = facet.getState();
 
         assertNotNull("The facet state should not be null", facetState);
         assertTrue("The facet state should be root", facetState.isRoot());
         assertEquals("Unexpected number of refinements", MAX_ALPHANUMERIC_ITEMS,
                 facetState.getRefinements().size());
         assertEquals("Unexpected count value", 0, facetState.getCount());
        assertEquals("There should be no constraints", 0, facetState.getConstraints().size());
 
 
         // testing the refinements of the state
 
         for (FacetState state : facetState.getRefinements()) {
             assertFalse("The facet state should not be root", state.isRoot());
             assertEquals("Unexpected parent", facetState, state.getParent());
             assertNotNull("The name should not be null", state.getName());
             assertNotNull("The parameter value should not be null", state.getParamValue());
             assertEquals("Unexpected count value", 0, state.getCount());
             assertEquals("There should be no refinements", 0, state.getRefinements().size());
             assertEquals("Unexpected number of constraints", 2, state.getConstraints().size());
         }
 
         // check actual values of a specific state
         FacetState specificState = facetState.getRefinements().get(10);
         assertEquals("Unexpected name", label, specificState.getName());
         assertEquals("Unexpected parameter value", label, specificState.getParamValue());
 
         // check we have the expected constraint types
         for (Constraint constraint : specificState.getConstraints()) {
             if (!(constraint instanceof ValueConstraint) && !(constraint instanceof RegexpConstraint)) {
                 fail("Unexpected constraint type: " + constraint.getClass().getName());
             }
         }
     }
 
     @Test
     public void createAlphaNumericFacetWithParameter() throws FacetException {
 
         // create a parameter
 
         String[] paramValue = {label};
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(alphaNumericTestParamName, paramValue);
 
         // create the constraint
 
         FacetEnvironmentImpl facetConstraint = new FacetEnvironmentImpl(createAlphaNumericConfig(),
                 parameters, new HashMap<String, String>());
 
         // test the facet
 
         Facet facet = facetFactory.create(facetConstraint);
 
         assertNotNull("The facet should not be null", facet);
         assertEquals("Unexpected Name", alphaNumericTestName, facet.getName());
         assertEquals("Unexpected parameter name", alphaNumericTestParamName, facet.getParam());
 
         // test the facet state
 
         FacetState facetState = facet.getState();
         assertNotNull("The facet state should not be null", facetState);
         assertFalse("The facet state should not be root", facetState.isRoot());
         assertEquals("Unexpected number of refinements", 0,
                 facetState.getRefinements().size());
         assertEquals("Unexpected count value", 0, facetState.getCount());
         assertEquals("There should be no constraints", 2, facetState.getConstraints().size());
         assertEquals("Unexpected name", label, facetState.getName());
         assertEquals("Unexpected parameter value", label, facetState.getParamValue());
 
 
     }
 
     // ---------- private helper methods
 
     /**
      * @return a map of configuration details needed for an alphanumeric facet
      */
     private Map<String, String> createAlphaNumericConfig() {
 
         Map<String, String> config = new HashMap<String, String>();
         config.put(Facet.FACET_TYPE, Facet.ALPHA_NUMERIC_FACET_TYPE);
         config.put(Facet.FACET_TITLE, alphaNumericTestName);
         config.put(Facet.LINK_PROPERTY, alphaNumericTestLinkProperty);
         config.put(Facet.CONSTRAINT_TYPE, alphaNumericTestConstraintType);
         config.put(Facet.PARAM_NAME, alphaNumericTestParamName);
         return config;
     }
 
     public Map<FacetState, Integer> getCounts(List<? extends FacetState> states) {
 
         Map<FacetState, Integer> results = new HashMap<FacetState, Integer>();
 
         // go through root states to get refinements
         for (FacetState facetState : states) {
             for (FacetState refinementState : facetState.getRefinements()) {
                 // set everything with a count of 5
                 results.put(refinementState, 5);
             }
         }
 
         return results;
     }
 
     private List<FacetState> currentStates(List<Facet> facetList) {
 
         List<FacetState> states = new ArrayList<FacetState>();
 
         for (Facet facet : facetList) {
             states.add(facet.getState());
         }
 
         return states;
     }
 
     // ---------- private variables
 
     private final char c = 'A';
     private final String label = "A*";
     private final String constraint = "^A";
     private final int MAX_ALPHANUMERIC_ITEMS = 36;
     private final String linkProperty = "http://http://purl.org/dc/elements/1.1/title";
     private final String typeProperty = "http://example.org/vocab/#Thingy";
 
     private final String alphaNumericTestName = "Family Names";
     private final String alphaNumericTestLinkProperty = "http://xmlns.com/foaf/0.1/family_name";
     private final String alphaNumericTestConstraintType = "http://xmlns.com/foaf/0.1/Person";
     private final String alphaNumericTestParamName = "personName";
 
     private final Mockery context = new JUnit4Mockery();
 
     private FacetQueryService mockFacetQueryService;
     private FacetFactoryImpl facetFactory;
 }
