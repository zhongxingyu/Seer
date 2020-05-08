 package dk.statsbiblioteket.broadcasttranscoder.reklamefilm;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.InfrastructureContext;
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import org.junit.Test;
 
 import java.io.File;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: csr
  * Date: 29/01/13
  * Time: 14:33
  * To change this template use File | Settings | File Templates.
  */
 public class ReklamefilmFileResolverImplTest {
 
     @Test
     public void testGetFile() {
         SingleTranscodingContext context = new SingleTranscodingContext();
         context.setReklamefileRootDirectories(new String[]{"broadcast-transcoder/src/test/java/dk/statsbiblioteket/broadcasttranscoder/reklamefilm/data"});
         ReklamefilmFileResolverImpl resolver = new ReklamefilmFileResolverImpl(context);
         File foundFile = resolver.getFile("foobar", "file1");
         assertTrue(foundFile.exists());
     }
 
 }
