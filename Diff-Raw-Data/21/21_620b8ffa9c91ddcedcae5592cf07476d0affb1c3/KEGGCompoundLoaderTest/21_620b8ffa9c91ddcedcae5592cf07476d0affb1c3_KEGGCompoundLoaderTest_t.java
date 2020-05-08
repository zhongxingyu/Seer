 package uk.ac.ebi.chemet.service.loader.multiple;
 
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import uk.ac.ebi.chemet.service.index.data.KEGGCompoundDataIndex;
 import uk.ac.ebi.chemet.service.index.name.KEGGCompoundNameIndex;
 import uk.ac.ebi.chemet.service.loader.LoaderTestUtil;
 import uk.ac.ebi.chemet.service.loader.LuceneIndexInspector;
 import uk.ac.ebi.service.MultiIndexResourceLoader;
 import uk.ac.ebi.service.exception.MissingLocationException;
 import uk.ac.ebi.service.index.LuceneIndex;
 import uk.ac.ebi.service.query.QueryService;
 import uk.ac.ebi.service.query.data.MolecularFormulaService;
 import uk.ac.ebi.service.query.name.PreferredNameService;
 import uk.ac.ebi.service.query.name.SynonymService;
 
 import java.io.IOException;
 
 /**
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public class KEGGCompoundLoaderTest {
 
     private static final Logger LOGGER = Logger.getLogger(KEGGCompoundLoaderTest.class);
 
     private static LuceneIndex name;
     private static LuceneIndex data;
 
     private static LuceneIndexInspector nameInspector;
     private static LuceneIndexInspector dataInspector;
 
     @BeforeClass
     public static void createIndexes() throws IOException, MissingLocationException {
 
         name = new KEGGCompoundNameIndex(LoaderTestUtil.createTemporaryDirectory("kegg.names"));
         data = new KEGGCompoundDataIndex(LoaderTestUtil.createTemporaryDirectory("kegg.data"));
 
         MultiIndexResourceLoader loader = new KEGGCompoundLoader();
 
         // set test-indexes
         loader.addIndex("kegg.names", name);
         loader.addIndex("kegg.data", data);
 
         // added location
         loader.addLocation("KEGG Compound", LoaderTestUtil.getLocation("data/kegg/compound"));
 
         loader.update();
 
     }
 
     @AfterClass
     public static void tearDownClass() throws IOException {
         if (nameInspector != null) nameInspector.close();
         if (dataInspector != null) dataInspector.close();
     }
 
 
     @Test
     public void testUpdate_created() {
         Assert.assertTrue("Name index was not created/found", LoaderTestUtil.testInspector(name));
         Assert.assertTrue("Data index was not created/found", LoaderTestUtil.testInspector(data));
     }
 
 
     @Test
     public void testUpdate_name_fields() throws IOException {
 
         nameInspector = nameInspector == null ? LoaderTestUtil.getIndexInspector(name) : nameInspector;
 
 
         // test fields are in place
         Assert.assertTrue(nameInspector.hasField(QueryService.IDENTIFIER.field()));
         Assert.assertTrue(nameInspector.hasField(PreferredNameService.PREFERRED_NAME.field()));
         Assert.assertTrue(nameInspector.hasField(SynonymService.SYNONYM.field()));
 
     }
 
     @Test
     public void testUpdate_name_identifiers() throws IOException {
 
         nameInspector = nameInspector == null ? LoaderTestUtil.getIndexInspector(name) : nameInspector;
 
         String field = QueryService.IDENTIFIER.field();
 
         // test identifiers are in place
         Assert.assertTrue("C15303 was not loaded", nameInspector.hasValue(field, "C15303"));
         Assert.assertTrue("C15304 was not loaded", nameInspector.hasValue(field, "C15304"));
         Assert.assertTrue("C15305 was not loaded", nameInspector.hasValue(field, "C15305"));
         Assert.assertTrue("C15306 was not loaded", nameInspector.hasValue(field, "C15306"));
 
     }
 
 
     @Test
     public void testUpdate_name() throws IOException {
 
         nameInspector = nameInspector == null ? LoaderTestUtil.getIndexInspector(name) : nameInspector;
 
         String field = PreferredNameService.PREFERRED_NAME.field();
 
         // preferred names
         Assert.assertTrue(nameInspector.hasValue(field, "(Z)-11beta,21-Dihydroxypregna-1,4,17(20)-trien-3-one"));
         Assert.assertTrue(nameInspector.hasValue(field, "3beta,19-Dihydroxyandrost-5-en-17-one 3-acetate"));
         Assert.assertTrue(nameInspector.hasValue(field, "9-Chloro-11beta,17,21-trihydroxypregn-4-ene-3,20-dione 21-acetate"));
         Assert.assertTrue(nameInspector.hasValue(field, "11alpha,17beta-Dihydroxyandrost-4-en-3-one"));
 
     }
 
     @Test
     public void testUpdate_data_fields() throws IOException {
 
        dataInspector = dataInspector == null ? LoaderTestUtil.getIndexInspector(data) : dataInspector;
 
         // test fields are in place
         Assert.assertTrue(dataInspector.hasField(QueryService.IDENTIFIER.field()));
         Assert.assertTrue(dataInspector.hasField(MolecularFormulaService.MOLECULAR_FORMULA.field()));
 
     }
 
     @Test
     public void testUpdate_data_identifiers() throws IOException {
 
        dataInspector = dataInspector == null ? LoaderTestUtil.getIndexInspector(data) : dataInspector;
         
         String field = QueryService.IDENTIFIER.field();
         
         // test identifiers are in place
         Assert.assertTrue("C15303 was not loaded", nameInspector.hasValue(field, "C15303"));
         Assert.assertTrue("C15304 was not loaded", nameInspector.hasValue(field, "C15304"));
         Assert.assertTrue("C15305 was not loaded", nameInspector.hasValue(field, "C15305"));
         Assert.assertTrue("C15306 was not loaded", nameInspector.hasValue(field, "C15306"));
 
     }
 
 
     @Test
     public void testUpdate_data() throws IOException {
 
        dataInspector = dataInspector == null ? LoaderTestUtil.getIndexInspector(data) : dataInspector;
 
        String field = MolecularFormulaService.MOLECULAR_FORMULA.field();
 
         // preferred names
        Assert.assertTrue(dataInspector.hasValue(field, "C19H28O3"));
        Assert.assertTrue(dataInspector.hasValue(field, "C21H28O3"));
        Assert.assertTrue(dataInspector.hasValue(field, "C21H30O4"));
        Assert.assertTrue(dataInspector.hasValue(field, "C23H31ClO6"));
 
     }
 
     @Test
     public void testGetName() throws Exception {
         Assert.assertNotNull(new KEGGCompoundLoader().getName());
     }
 
 
 }
