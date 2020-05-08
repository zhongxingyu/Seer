 package ro.finsiel.eunis.jrfTables.species.names;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import org.junit.Test;
 import org.junit.BeforeClass;
 
 import eionet.eunis.test.DbHelper;
 
 import ro.finsiel.eunis.search.AbstractSearchCriteria;
 import ro.finsiel.eunis.search.AbstractSortCriteria;
 import ro.finsiel.eunis.search.species.names.NameSearchCriteria;
 import ro.finsiel.eunis.search.Utilities;
 
 /**
  * Call the getResults()
  */
 // OPERATOR_IS = 2
 // OPERATOR_STARTS = 3
 // OPERATOR_CONTAINS = 4
 
 public class SimilarNameDomainTest {
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         DbHelper.handleSetUpOperation("seed-species-lyn.xml");
     }
 
     /**
      * Search on name Contains "lynx linx". There is no such exact name in the database,
      * but plenty very similar.
      */
     @Test
     public void searchNameContainsLinxLynx() throws Exception {
         NameSearchCriteria criteria1 = new NameSearchCriteria("lynx linx", Utilities.OPERATOR_CONTAINS);
         assertTrue(criteria1.isMainCriteria());
         NameSearchCriteria[] searchCriteria = { criteria1 };
         AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0];
         boolean searchSynonyms = true;
         boolean showEUNISInvalidatedSpecies = true;
         Boolean searchVernacular = Boolean.valueOf(true);
 
         SimilarNameDomain instance = new SimilarNameDomain(searchCriteria, sortCriteria,
                 searchSynonyms, showEUNISInvalidatedSpecies, searchVernacular);
         assertNotNull("Instantiation failed", instance);
         List<ScientificNamePersist> result = instance.getResults(0, 1000, sortCriteria);
 
         //System.out.println(result.size());
         //for (ScientificNamePersist listItem : result) {
         //    System.out.println(listItem.getScientificName() + ":" + listItem.getCommonName());
         //}
 
         assertEquals(16, result.size());
     }
 
     /**
      * Search on name starts with "lynx" - any group.
      */
     @Test
     public void searchNameStartsWithLynx() throws Exception {
         NameSearchCriteria criteria1 = new NameSearchCriteria("Lynx", Utilities.OPERATOR_STARTS);
         NameSearchCriteria[] searchCriteria = { criteria1 };
         AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0];
         boolean searchSynonymsT = true;
         boolean showEUNISInvalidatedSpeciesT = true;
         Boolean searchVernacularT = Boolean.valueOf(true);
 
         SimilarNameDomain instance = new SimilarNameDomain(searchCriteria, sortCriteria,
                 searchSynonymsT, showEUNISInvalidatedSpeciesT, searchVernacularT);
         assertNotNull("Instantiation failed", instance);
         List result = instance.getResults(0, 1000, sortCriteria);
         assertEquals(16, result.size());
     }
 
     /**
      * Search on name Contains "Lynx" and species group is "Fishes".
      * We go through the results and verify that all hits are fishes.
      * You can do the same through the UI.
      */
    //@Test
     public void searchNameContainsLynAndFishesGroup() throws Exception {
         NameSearchCriteria criteria1 = new NameSearchCriteria("Lynx", Utilities.OPERATOR_CONTAINS);
         NameSearchCriteria criteria2 = new NameSearchCriteria("Fishes",
                 NameSearchCriteria.CRITERIA_GROUP, Utilities.OPERATOR_IS);
         NameSearchCriteria[] searchCriteria = { criteria1, criteria2 };
         AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0];
         boolean searchSynonyms = true;
         boolean showEUNISInvalidatedSpecies = true;
         Boolean searchVernacular = Boolean.valueOf(true);
 
         SimilarNameDomain instance = new SimilarNameDomain(searchCriteria, sortCriteria,
                 searchSynonyms, showEUNISInvalidatedSpecies, searchVernacular);
         assertNotNull("Instantiation failed", instance);
         List<ScientificNamePersist> result = instance.getResults(0, 1000, sortCriteria);
 
         for (ScientificNamePersist listItem : result) {
             assertEquals("Fishes", listItem.getCommonName());
         }
 
         assertEquals(1, result.size());
     }
 }
