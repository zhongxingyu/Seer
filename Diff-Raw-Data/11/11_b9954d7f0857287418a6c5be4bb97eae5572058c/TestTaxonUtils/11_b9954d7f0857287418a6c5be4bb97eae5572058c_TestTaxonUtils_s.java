 package uk.ac.ebi.fgpt.sampletab;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonException;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonUtils;
 import junit.framework.TestCase;
 
 
 public class TestTaxonUtils extends TestCase {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void testHuman() {
        /*
         try {
             assertEquals("Homo sapiens", TaxonUtils.getSpeciesOfID(9606));
         } catch (TaxonException e) {
             log.error("Problem getting species of ID 9606", e);
             fail();
         }
        */
     }
     
     public void testXenopus() {
        /*
         try {
             assertEquals(new Integer(8364), TaxonUtils.findTaxon("Xenopus (Silurana) tropicalis"));
         } catch (TaxonException e) {
             log.error("Problem getting tax id of 'Xenopus (Silurana) tropicalis'", e);
             fail();
         }
        */
     }
 
 }
