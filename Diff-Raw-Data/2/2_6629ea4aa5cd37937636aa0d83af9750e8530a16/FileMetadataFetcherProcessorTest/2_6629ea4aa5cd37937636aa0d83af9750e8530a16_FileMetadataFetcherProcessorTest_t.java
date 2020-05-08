 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import junit.framework.TestCase;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: csr
  * Date: 11/22/12
  * Time: 12:39 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FileMetadataFetcherProcessorTest  {
 
     @Before
     public void setUp() {
         try {
             InetAddress.getByName("carme");
         } catch (UnknownHostException e) {
             Assume.assumeNoException(e);
         }
     }
 
     @Test
     public void testProcessIteratively() throws Exception {
         FileMetadataFetcherProcessor processor = new FileMetadataFetcherProcessor();
         TranscodeRequest request = new TranscodeRequest();
         SingleTranscodingContext context = new SingleTranscodingContext();
        request.setObjectPid("uuid:3a1bdfce-497f-4bff-8804-22ac000cca83");
         context.setDomsEndpoint("http://carme:7880/centralWebservice-service/central/");
         context.setDomsUsername("fedoraAdmin");
         context.setDomsPassword("spD68ZJl");
         processor.processIteratively(request,context);
     }
 }
