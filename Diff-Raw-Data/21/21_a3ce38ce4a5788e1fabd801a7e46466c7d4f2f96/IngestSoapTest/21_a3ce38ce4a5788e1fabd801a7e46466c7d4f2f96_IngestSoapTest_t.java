 package de.escidoc.core.test.om.ingest.soap;
 
 import org.junit.Test;
 
import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.om.ingest.IngestAbstractTest;
 
 public class IngestSoapTest extends IngestAbstractTest {
 
     public IngestSoapTest() {
         super(Constants.TRANSPORT_SOAP);
     }
 
     /**
     * Test unexpected parser exception instead of XmlCorruptedException during create (see issue INFR-911).
      *
      * @throws Exception Thrown if behavior is not as expected.
      */
    @Test(expected = XmlCorruptedException.class)
     public void testInvalidXml() throws Exception {
 
         ingest("laber-rababer");
     }
 
 }
