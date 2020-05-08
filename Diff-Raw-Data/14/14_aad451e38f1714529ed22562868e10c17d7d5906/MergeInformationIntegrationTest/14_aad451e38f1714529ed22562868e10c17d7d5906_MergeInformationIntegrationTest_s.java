 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 import models.JSONMapper;
 import models.domain.VimeoVideo;
 
 import org.junit.Test;
 
 import java.io.File;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com>
  * 2011-09-11
  */
public class MergeInformationIntegrationTest {
 
     @Test
     public void allTitlesAreMerged() throws Exception {
        String videosString = readFile("src/test/resources/videos.json");
        String sessionsString = readFile("src/test/resources/sessions.json");
        JSONMapper mapper = new JSONMapper();
 
        List<VimeoVideo> videos = mapper.videosToObjects(videosString);
         //List<IncogitoSession> sessions = mapper.sessionsTotoObjects(sessionsString);
         assertEquals(50, videos.size());
 
     }
 
     private String readFile(String fileName) throws Exception {
         return Files.toString(new File(fileName), Charsets.UTF_8);
     }
 }
