 package uk.ac.ebi.fgpt.sampletab;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.EuroPMCUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.europmc.ws.QueryException_Exception;
 import junit.framework.TestCase;
 
 
 public class TestEuroPMCUtils extends TestCase {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void testBioSD() {
        //commented out until bamboo proxying works
//        String title = null;
//        try {
//            title = EuroPMCUtils.getTitleByPUBMEDid(22096232);
//        } catch (QueryException_Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            fail();
//        }
//        assertEquals("The BioSample Database (BioSD) at the European Bioinformatics Institute.", title);
     }
 
 }
