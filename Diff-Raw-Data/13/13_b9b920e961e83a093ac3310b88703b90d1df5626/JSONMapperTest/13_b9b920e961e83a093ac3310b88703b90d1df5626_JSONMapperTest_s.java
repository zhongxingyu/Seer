 package models;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 import models.domain.vimeo.VimeoVideo;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import play.test.UnitTest;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com>
  * 2011-09-11
  */
 public class JSONMapperTest extends UnitTest {
 
 
     private static VimeoVideo video;;
 
    @BeforeClass
    public static void init() throws IOException {
         video = getVideoElement();
     }
 
 
     @Test
     public void mapVideoJSONToListOfVideos() throws Exception {
         JSONMapper mapper = new JSONMapper();
        String videosJson = Files.toString(new File("src/test/resources/twoVideos.json"), Charsets.UTF_8);
         List<VimeoVideo> videos = mapper.videosToObjects(videosJson);
 
         assertEquals(2, videos.size());
     }
 
     @Test
     public void wrapperForTitleInVideoElement() throws Exception {
         assertEquals("Deploying Apps on Heroku", video.title());
     }
 
     @Test
     public void wrapperForId() {
         assertEquals(28803302, video.id());
     }
 
     @Test
     public void wrapperForDescription() {
         assertLargerThan(10, video.description().length());
     }
 
     private void assertLargerThan(int i, int length) {
         if(length < i) {
             throw new AssertionError("expected " + length + " > " + i + " but was " + length + " < " + i);
         }
     }
 
     private static VimeoVideo getVideoElement() throws IOException {
         JSONMapper mapper = new JSONMapper();
        String videosJson = Files.toString(new File("src/test/resources/twoVideos.json"), Charsets.UTF_8);
         return mapper.videosToObjects(videosJson).get(0);
     }
 
 }
