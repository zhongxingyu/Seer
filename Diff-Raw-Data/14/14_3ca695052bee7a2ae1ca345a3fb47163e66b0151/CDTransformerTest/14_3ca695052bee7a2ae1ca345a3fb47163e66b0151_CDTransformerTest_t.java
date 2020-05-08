 package gov.nih.nci.iso21090.grid.dto.transform.iso;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import gov.nih.nci.iso21090.Cd;
 import gov.nih.nci.iso21090.NullFlavor;
 import gov.nih.nci.iso21090.grid.dto.transform.AbstractTransformerTestBase;
import gov.nih.nci.iso21090.grid.dto.transform.iso.CDTransformer.CDCoreTransformer;
 
 import org.iso._21090.CD;
 import org.junit.Test;
 
 /**
  *
  * @author gax
  */
public class CDTransformerTest extends AbstractTransformerTestBase<CDCoreTransformer, CD, Cd> {
 
     @Override
     public Cd makeDtoSimple() {
         Cd i = new Cd();
         i.setCode("code");
         i.setCodeSystem("ignored");
         i.setCodeSystemName("ignored");
         i.setCodeSystemVersion("ignored");
         i.setDisplayName(new STTransformerTest().makeDtoSimple());
         i.setOriginalText(null);
         return i;
     }
 
     @Override
     public CD makeXmlSimple() {
         CD x = new CD();
         x.setCode("code");
         x.setCodeSystem("sys");
         x.setCodeSystemName("name");
         x.setCodeSystemVersion("v");
         x.setControlActExtension("cae");
         x.setControlActRoot("r");
         x.setDisplayName(new STTransformerTest().makeXmlSimple());
 
         return x;
     }
 
     @Override
     public void verifyXmlSimple(CD x) {
         assertEquals("code", x.getCode());
         assertEquals("ignored", x.getCodeSystem());
         assertEquals("ignored", x.getCodeSystemName());
         assertEquals("ignored", x.getCodeSystemVersion());
     }
 
 
     @Override
     public void verifyDtoSimple(Cd i) {
         assertEquals("code", i.getCode());
         assertEquals("sys", i.getCodeSystem());
         assertEquals("name", i.getCodeSystemName());
         assertEquals("v", i.getCodeSystemVersion());
     }
 
     @Test
     public void testCdCodeNull() throws Exception {
         Cd cd = new Cd();
         cd.setNullFlavor(NullFlavor.ASKU);
        CD result = CDCoreTransformer.INSTANCE.toXml(cd);
         assertNotNull(result);
         assertNull(result.getCode());
         assertEquals(org.iso._21090.NullFlavor.ASKU, result.getNullFlavor());
 
        cd = CDCoreTransformer.INSTANCE.toDto(result);
         assertNotNull(cd);
         assertNull(cd.getCode());
         assertEquals(NullFlavor.ASKU, cd.getNullFlavor());
     }
 }
