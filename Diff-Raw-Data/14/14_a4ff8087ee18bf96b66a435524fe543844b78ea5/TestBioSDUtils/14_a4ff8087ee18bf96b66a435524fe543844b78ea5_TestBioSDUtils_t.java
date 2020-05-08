 package uk.ac.ebi.fgpt.sampletab;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.dom4j.DocumentException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.BioSDUtils;
 import junit.framework.TestCase;
 
 
 public class TestBioSDUtils extends TestCase {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void testENA() {
         Set<String> accessions = new HashSet<String>();
        //accessions.add("SAMEA958157");
        accessions.add("SAMEA2417299");
         try {
             assertEquals(accessions, BioSDUtils.getBioSDAccessionOf("ERS011203"));
         } catch (DocumentException e) {
             e.printStackTrace();
             fail();
         }
     }
 
 }
