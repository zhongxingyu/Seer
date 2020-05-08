 package xbrlcore.junit.linkbase;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import xbrlcore.exception.InstanceValidationException;
 import xbrlcore.exception.XBRLException;
 import xbrlcore.instance.Fact;
 import xbrlcore.instance.Instance;
 import xbrlcore.instance.InstanceFactory;
 import xbrlcore.instance.InstanceValidator;
 import xbrlcore.junit.sax.TestHelper;
 import xbrlcore.linkbase.CalculationLinkbase;
 import xbrlcore.taxonomy.Concept;
 import xbrlcore.taxonomy.DiscoverableTaxonomySet;
 import static org.junit.Assert.*;
 
 /**
  * @TODO: Add comment.
  * @author Daniel Hamm
  */
 public class CalculationLinkbaseTest {
 
     /**
      * Path used in this test case.
      */
    private static String PATH = "xbrl\\test\\linkbase_test\\";
 
     /**
      * DTS used in this test case.
      */
     private static DiscoverableTaxonomySet taxCalcDTS;
 
     /**
      * Instance used in this test case.
      */
     private static Instance instance;
 
     /**
      * This method is executed before all test cases in this JUnit test.
      */
     @BeforeClass
     public static void setUp() {
         try {
             InstanceFactory instanceFactory = InstanceFactory.get();
 
             // DTSFactory taxonomyFactory = DTSFactory.get();
             // taxCalcDTS = taxonomyFactory.createTaxonomy(new File(PATH
             // + "tax_calc.xsd"));
 
             taxCalcDTS = TestHelper
                 .getDTS("xbrl/test/linkbase_test/tax_calc.xsd");
             instance = instanceFactory.createInstance(new File(PATH
                 + "instance.xml"));
         } catch (Exception ex) {
             System.out.println(ex.toString());
             ex.printStackTrace();
             fail("Fehler beim Erstellen der Taxonomy pr: " + ex.getMessage());
         }
     }
 
     /**
      * TODO: Add comment.
      */
     @Test
     public void calcLinkbase() {
         try {
             assertNotNull(taxCalcDTS);
             CalculationLinkbase calcLinkbase = taxCalcDTS
                 .getCalculationLinkbase();
             assertNotNull(calcLinkbase);
 
             Concept primItem = taxCalcDTS.getConceptByID("p0_newItem");
             assertNotNull(primItem);
             Map calcMap = calcLinkbase.getCalculations(primItem,
                 "http://www.xbrl.org/2003/role/link");
             assertEquals(2, calcMap.size());
         } catch (Exception ex) {
             System.out.println(ex.toString());
             ex.printStackTrace();
             fail(ex.getMessage());
         }
 
     }
 
     /**
      * TODO: Add comment.
      */
     @Test
     public void singleFactToCalculate() {
         try {
             assertNotNull(instance);
             Iterator iterator = instance.getDiscoverableTaxonomySet()
                 .iterator();
             CalculationLinkbase calcLinkbase = ((DiscoverableTaxonomySet) iterator
                 .next()).getCalculationLinkbase();
 
             InstanceValidator validator = new InstanceValidator(instance);
 
             Set factSet = instance.getFactSet();
             Iterator factSetIterator = factSet.iterator();
             while (factSetIterator.hasNext()) {
                 Fact currFact = (Fact) factSetIterator.next();
                 if (currFact.getConcept().getId().equals("p0_newItem5")) {
                     try {
                         validator.validateCalculation(currFact);
                     } catch (InstanceValidationException ex) {
                         System.err.println(ex.toString());
                         fail(ex.toString());
                     }
                 }
             }
 
         } catch (Exception ex) {
             System.out.println(ex.toString());
             ex.printStackTrace();
             fail(ex.getMessage());
         }
 
     }
 
     /**
      * TODO: Add comment.
      */
     @Test
     public void calculateInstance() {
         assertNotNull(instance);
         Iterator iterator = instance.getDiscoverableTaxonomySet().iterator();
         CalculationLinkbase calcLinkbase = ((DiscoverableTaxonomySet) iterator
             .next()).getCalculationLinkbase();
 
         InstanceValidator validator = new InstanceValidator(instance);
         try {
             validator.validate();
         } catch (InstanceValidationException ex) {
             System.err.println(ex.toString());
             fail(ex.toString());
         } catch (XBRLException ex) {
             System.err.println(ex.toString());
             fail(ex.toString());
         }
     }
 
 }
