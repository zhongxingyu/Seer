 package br.usp.ime.arranger.tests.functional;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.activation.DataHandler;
 
import org.junit.After;
 import org.junit.Test;
 
 import br.usp.ime.arranger.behaviors.BehaviorException;
 import br.usp.ime.arranger.service.Performer;
 import br.usp.ime.arranger.utils.CommUtils;
 import br.usp.ime.arranger.utils.FileCreator;
 
 import com.sun.xml.ws.developer.StreamingDataHandler;
 
 @SuppressWarnings("PMD.AvoidFinalLocalVariable")
 public class PerformerTest {
 
    @After
    public void destroy() {
        CommUtils.destroy();
    }

     @Test
     public void shouldSaveFileOfRequestSize() throws BehaviorException,
             IOException {
         final long size = 1024;
 
         final PerformerPublisher publisher = new PerformerPublisher();
         final String wsdl = publisher.publish();
         final CommUtils comm = new CommUtils();
         final Performer performer = comm.getPerformer(wsdl);
         final DataHandler dataHandler = performer.msgStringReqDataRes("dummy",
                 size);
 
         final StreamingDataHandler stream = (StreamingDataHandler) dataHandler;
         final File file = FileCreator.createTempFile();
         stream.moveTo(file);
 
         final long fileSize = file.length();
 
         stream.close();
         publisher.stopAll();
 
         assertEquals(size, fileSize);
     }
 }
