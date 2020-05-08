 package uk.ac.ebi.ricordo.rdfstore.service;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import uk.ac.ebi.ricordo.rdfstore.bean.ResourceList;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Created by IntelliJ IDEA.
  * User: sarala
  * Date: 08/03/12
  * Time: 11:57
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(SpringJUnit4ClassRunner.class )
 @ContextConfiguration(locations={"classpath:ricordo-rdfstore-config.xml"})
 public class RdfStoreServiceTest {
 
     @Autowired
     RdfStoreService rdfStoreService;
 
     @Test
     public void testResources() throws Exception {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("","getResources",resourceList);
         assertEquals(421,resourceList.getCount());  //number of sbml models
     }
 
     @Test
     public void testQueryGetResourcesByType() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#SBMLModel","getResourcesByType",resourceList);
         assertEquals(421,resourceList.getCount());  //number of sbml models
     }
 
 
     @Test
     public void testQueryGetResourceForAnnotationOfElement() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("http://identifiers.org/obo.go/GO:0031594","getResourceForAnnotationOfElement",resourceList);
         assertEquals("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000001", resourceList.getResources().get(0).getValue().get(0));
         assertEquals("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000002",  resourceList.getResources().get(1).getValue().get(0));
     }
 
     @Test
     public void testQueryGetResourceForAnnotation() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("http://identifiers.org/obo.go/GO:0031594","getResourceForAnnotation",resourceList);
         assertEquals("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000001_comp1", resourceList.getResources().get(0).getValue().get(0));
         assertEquals("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000002_comp1",  resourceList.getResources().get(1).getValue().get(0));
     }
 
     @Test
     public void testQueryGetElementOfResource() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000001","getElementOfResource",resourceList);
         assertEquals(65,resourceList.getCount());
     }
 
     @Test
     public void testQueryAnnotationOfResource() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000001","getAnnotationOfResource",resourceList);
         assertEquals("http://biomodels.net/biology-qualifiers#isVersionOf", resourceList.getResources().get(0).getValue().get(0));
         assertEquals("http://identifiers.org/obo.go/GO:0007166", resourceList.getResources().get(0).getValue().get(1));
         assertEquals("http://biomodels.net/biology-qualifiers#isVersionOf",  resourceList.getResources().get(1).getValue().get(0));
         assertEquals("http://identifiers.org/obo.go/GO:0007274",  resourceList.getResources().get(1).getValue().get(1));
 
 
     }
 
     @Test
     public void testQueryAnnotationOfElementOfResource() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000001","getAnnotationOfElementOfResource",resourceList);
         assertEquals(39,resourceList.getCount());
      }
 
     @Test
     public void testQueryGetTypesOfResource() {
         ResourceList resourceList = new ResourceList();
         rdfStoreService.search("","getTypesOfResource",resourceList);
         assertEquals("http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#SBMLModel", resourceList.getResources().get(0).getValue().get(0));
         assertEquals("http://www.ebi.ac.uk/ricordo/toolbox/cellmlo#CellMLModel",resourceList.getResources().get(1).getValue().get(0));
 
     }
 
     @Test
     public void testInsert() {
        String query = "http://www.ebi.ac.uk/ricordo/toolbox/sbmlo#BIOMD0000000081_M,http://biomodels.net/biology-qualifiers/#isVersionOf,http://identifiers.org/obo.fma/FMA:63841";
         rdfStoreService.insert(query,"insertStatement");
 
     }
 
 }
