 package org.cagrid.i2b2.test.utils;
 
 import gov.nih.nci.cagrid.metadata.MetadataUtils;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.cagrid.i2b2.ontomapper.utils.AttributeNotFoundInModelException;
 import org.cagrid.i2b2.ontomapper.utils.ClassNotFoundInModelException;
 import org.cagrid.i2b2.ontomapper.utils.DomainModelCdeIdMapper;
 
 /**
  * DomainModelConceptCodeMapperTestCase
  * Tests the DomainModelConceptCodeMapper
  * 
  * @author David
  */
 public class DomainModelCdeIdMapperTestCase extends TestCase {
     
    public static final String MODEL_LOCATION = "/resources/models/caArray_2-1_DomainModel.xml";
     
     private DomainModelCdeIdMapper mapper = null;
     
     public DomainModelCdeIdMapperTestCase() {
         super("Domain Model Cde Id Mapper Test Case");
     }
     
     
     public void setUp() {
         try {
             InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(MODEL_LOCATION));
             DomainModel model = MetadataUtils.deserializeDomainModel(reader);
             reader.close();
             mapper = new DomainModelCdeIdMapper(model);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error setting up domain model cde id mapper: " + ex.getMessage());
         }
     }
     
     
     public void testClassFound() {
         String className = "edu.georgetown.pir.Organism";
         try {
             mapper.getCdeIdsForClass(className);
         } catch (ClassNotFoundInModelException ex) {
             ex.printStackTrace();
             fail("Class " + className + " was expected to be in the model: " + ex.getMessage());
         }
     }
     
     
     public void testAttributesAndCdes() {
         String className = "edu.georgetown.pir.Organism";
         Map<String, Long> attributeCdeIds = null;
         try {
             attributeCdeIds = mapper.getCdeIdsForClass(className);
         } catch (ClassNotFoundInModelException ex) {
             ex.printStackTrace();
             fail("Class " + className + " was expected to be in the model: " + ex.getMessage());
         }
         // check the size
         assertEquals("Unexpected number of attributes found for class " + className, 6, attributeCdeIds.size());
         List<String> expectedAttributeNames = Arrays.asList(new String[] {
             "commonName", "ethnicityStrain", "id", "ncbiTaxonomyId", "scientificName", "taxonomyRank"
         });
         List<Long> expectedCdeIds = Arrays.asList(new Long[] {
             Long.valueOf(2223787), Long.valueOf(2590794), Long.valueOf(2223783),
             Long.valueOf(2342465), Long.valueOf(2223784), Long.valueOf(2590793)
         });
         
         // verify CDEids per attribute
         for (int i = 0; i < expectedAttributeNames.size(); i++) {
             String expectName = expectedAttributeNames.get(i);
             Long expectCde = expectedCdeIds.get(i);
             assertTrue("Attribute " + expectName + " not found", 
                 attributeCdeIds.containsKey(expectName));
             Long foundCde = attributeCdeIds.get(expectName);
             assertEquals("CDE id was not what we expected for " + expectName, expectCde, foundCde);
         }
     }
     
     
     public void testInvalidClass() {
         String className = "non.existant.package.AndClass";
         try {
             mapper.getCdeIdsForClass(className);
         } catch (ClassNotFoundInModelException ex) {
             // expected
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Unexpected exception thrown: " + ex.getMessage());
         }
     }
     
     
     public void testValidClassInvalidAttribute() {
         String className = "edu.georgetown.pir.Organism";
         String attributeName = "nonExistantAttribute";
         try {
             mapper.getCdeIdForAttribute(className, attributeName);
         } catch (AttributeNotFoundInModelException ex) {
             // expected
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Unexpected exception thrown: " + ex.getMessage());
         }
     }
     
     
     public void testInvalidClassInvalidAttribute() {
         String className = "non.existant.package.AndClass";
         String attributeName = "nonExistantAttribute";
         try {
             mapper.getCdeIdForAttribute(className, attributeName);
         } catch (ClassNotFoundInModelException ex) {
             // expected... should throw this before attribute not found
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Unexpected exception thrown: " + ex.getMessage());
         }
     }
     
 
     public static void main(String[] args) {
         TestRunner runner = new TestRunner();
         TestResult result = runner.doRun(new TestSuite(DomainModelCdeIdMapperTestCase.class));
         System.exit(result.errorCount() + result.failureCount());
     }
 }
