 import models.IncogitoClient;
 import models.VideoInformationMerger;
 import models.VimeoClient;
 import models.domain.IncogitoSession;
 import models.domain.Video;
 import models.domain.VimeoVideo;
 import org.junit.Test;
 import play.test.FunctionalTest;
 
 import java.util.List;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com>
  * 2011-09-24
  */
 public class VideoFetcher extends FunctionalTest {
 
     @Test
     public void fetchAndSaveVideos() {
         List<VimeoVideo> videos = new VimeoClient().getVideosByYear("2011", null, null);
         List<IncogitoSession> sessions = new IncogitoClient().getSessionsForYear(2011);
 
         List<Video> finishedVideos = new VideoInformationMerger().mergeVideoAndSessionInfo(videos, sessions);
 
         for(Video video : finishedVideos) {
             video.save();
         }
     }
 
 }
