 package ro.finsiel.eunis.jrfTables.species.references;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import eionet.eunis.test.DbHelper;
 import ro.finsiel.eunis.search.AbstractSearchCriteria;
 import ro.finsiel.eunis.search.AbstractSortCriteria;
 import ro.finsiel.eunis.search.species.references.ReferencesSearchCriteria;
 import ro.finsiel.eunis.search.species.references.ReferencesSortCriteria;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.utilities.SQLUtilities;
 
 
 /**
  * Make a search for a species.
  */
 public class SpeciesBookTest {
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         DbHelper.handleSetUpOperation("seed-four-species.xml");
         SQLUtilities sqlUtils = DbHelper.getSqlUtilities();
         // The created date isn't set in the seed. We set it manually.
         sqlUtils.UpdateSQL("UPDATE DC_INDEX SET CREATED='2004' WHERE ID_DC = 1835");
     }
 
     @Test
     public void searchNameStartsWithCerchy() throws Exception {
         ReferencesSearchCriteria criteria1 = new ReferencesSearchCriteria("Cerchy", Utilities.OPERATOR_STARTS);
         ReferencesSearchCriteria[] searchCriteria = { criteria1 };
         AbstractSortCriteria[] sortCriteria = new AbstractSortCriteria[0];
 
         SpeciesBooksDomain instance = new SpeciesBooksDomain(searchCriteria, true);
         assertNotNull("Instantiation failed", instance);
         Long result = instance.countResults();
        // results are grouped by DC_INDEX.* and both species in seed-four-species.xml are linked to the same DC_INDEX
        assertEquals(Long.valueOf(1), result);
     }
 
 }
