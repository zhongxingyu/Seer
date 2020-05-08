 package supergenestest;
 
 import com.undeadscythes.genebase.record.*;
 import com.undeadscythes.supergenes.*;
 import static org.junit.Assert.*;
 import org.junit.*;
 import supergenestest.implementation.*;
 
 /**
  * @author UndeadScythes
  */
 public class HeaderTest {
     @Test
     public void testCustomHeader() {
         assertEquals("getName", "SuperGenes", SuperGenesHeader.getStandard().getFirstFromPath("SOUR.NAME").getValue());
        final Header header = new SuperGenesHeader(new SuperTGC55C().getGeneBase("TGC55C"));
         assertEquals("getName", "GEDitCOM", header.getFirstFromPath("SOUR.DATA").getValue());
     }
 }
