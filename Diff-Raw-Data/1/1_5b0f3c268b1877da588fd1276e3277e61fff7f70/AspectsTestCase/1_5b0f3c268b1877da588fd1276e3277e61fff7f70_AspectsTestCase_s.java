 package org.xbrlapi.aspects.alt.tests;
 
 import java.util.List;
 
 import org.xbrlapi.Concept;
 import org.xbrlapi.DOMLoadingTestCase;
 import org.xbrlapi.Fact;
 import org.xbrlapi.aspects.alt.Aspect;
 import org.xbrlapi.aspects.alt.AspectModel;
 import org.xbrlapi.aspects.alt.ConceptAspect;
 import org.xbrlapi.aspects.alt.ConceptAspectValue;
 import org.xbrlapi.aspects.alt.ConceptDomain;
 import org.xbrlapi.aspects.alt.FactSet;
 import org.xbrlapi.aspects.alt.FactSetImpl;
 import org.xbrlapi.aspects.alt.Filter;
 import org.xbrlapi.aspects.alt.FilterImpl;
 import org.xbrlapi.aspects.alt.LocationAspect;
 import org.xbrlapi.aspects.alt.LocationAspectValue;
 import org.xbrlapi.aspects.alt.PeriodAspect;
 import org.xbrlapi.aspects.alt.StandardAspectModel;
 import org.xbrlapi.impl.ConceptImpl;
 
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class AspectsTestCase extends DOMLoadingTestCase {
 	private final String FIRST_SMALL_INSTANCE = "test.data.small.instance";
     private final String SECOND_SMALL_INSTANCE = "test.data.small.instance.2";
    
     private final String TUPLE_INSTANCE = "test.data.local.xbrl.instance.tuples.with.units";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public AspectsTestCase(String arg0) {
 		super(arg0);
 	}
 
 	public void testAspectModel() {
 		try {
 
 		    // Load and retrieve the facts
 		    loader.discover(this.getURI(FIRST_SMALL_INSTANCE));       
 	        loader.discover(this.getURI(SECOND_SMALL_INSTANCE));
 			List<Fact> facts = store.getAllFacts();
 			assertEquals(2,facts.size());
 			
 			// Set up the aspect model
             AspectModel model = new StandardAspectModel(store);
             model.initialise();
             ConceptAspect conceptAspect = (ConceptAspect) model.getAspect(ConceptAspect.ID);
             ConceptDomain conceptDomain = (ConceptDomain) conceptAspect.getDomain();
             assertEquals(store.<Concept>getXMLResources(ConceptImpl.class).size(),conceptDomain.getSize());
             assertEquals(conceptDomain.getSize(), conceptDomain.getAllAspectValues().size());
             model.addAspect("row", conceptAspect);
             assertEquals(7,model.getAspects().size());
             assertTrue(model.hasAxis("row"));
             assertFalse(model.hasAxis("col"));
             assertEquals(1,model.getAspects("row").size());
             assertEquals(2,model.getAxes().size());
 
             // Add in the location aspect
             LocationAspect locationAspect = (LocationAspect) model.getAspect(LocationAspect.ID);
             model.addAspect("row", locationAspect);
             assertEquals(2,model.getAspects("row").size());
             model.addAspect("col", locationAspect);
             assertEquals(1,model.getAspects("row").size());
             assertEquals(3,model.getAxes().size());
             assertTrue(model.hasAxis("col"));
 
             // Set up the filtration system
             Filter filter = new FilterImpl();
             Fact firstFact = facts.get(0);
             ConceptAspectValue conceptAspectValue = new ConceptAspectValue(firstFact.getNamespace(), firstFact.getLocalname());
             filter.addCriterion(conceptAspectValue);
             assertTrue(filter.filtersOn(ConceptAspect.ID));
             assertFalse(filter.filtersOn(LocationAspect.ID));
             LocationAspectValue locationAspectValue = new LocationAspectValue(firstFact.getIndex());
             filter.addCriterion(locationAspectValue);
             assertTrue(filter.filtersOn(LocationAspect.ID));
             filter.removeCriterion(LocationAspect.ID);
             assertFalse(filter.filtersOn(LocationAspect.ID));
 
             // Create a fact set
             FactSet factSet = new FactSetImpl(model);
             factSet.addFacts(facts);
 
             assertEquals(3, model.getAxes().size());
             assertTrue(model.hasAxis("orphan"));
             assertTrue(model.hasAspect(PeriodAspect.ID));
 
             for (String axis: model.getAxes()) {
                 logger.info(axis);
                 for (Aspect aspect: model.getAspects(axis)) {
                     logger.info(aspect.getId());
                 }
             }
             
 /*            List<List<AspectValue>> rowMatrix = model.getAspectValueCombinationsForAxis("row");
             
             assertEquals(2,rowMatrix.size());
             assertEquals(1,rowMatrix.get(0).size());
 
             for (List<AspectValue> rowCombination: rowMatrix) {
                 for (AspectValue rValue: rowCombination) {
                     logger.debug("R: " + rValue.getAspect().getType() + " = " + rValue.getLabel());
                 }
                 Set<Fact> matchingFacts = filter.getMatchingFacts();
                 for (Fact matchingFact: matchingFacts) {
                     logger.debug(matchingFact.getIndex());
                 }
             }
 */ 
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	
 	
 	
 	
 }
