 package models;
 
 import models.domain.Speaker;
 import models.domain.Talk;
 import models.domain.external.IncogitoSession;
 import models.domain.external.VimeoVideo;
 import org.junit.Ignore;
 import org.junit.Test;
 import play.test.FunctionalTest;
 
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com> 2011-09-24
  *
  * This is a functional test written to easily fetch videos from video, session info from incogito and
  * merge them and store in the database. The test should be ignore when not running it since
  * running time is long.
  */
 public class VideoFetcher extends FunctionalTest {
 
     @Test
     @Ignore
     public void fetchAndSaveVideos() {
         List<VimeoVideo> videos = new VimeoClient().getVideosByYear("2011", null, null);
         List<IncogitoSession> sessions = new IncogitoClient().getSessionsForYear(2011);
 
         List<Talk> finishedTalks = new VideoInformationMerger().mergeVideoAndSessionInfo(videos, sessions);
 
         videos = new VimeoClient().getVideosByYear("2010", null, null);
         sessions = new IncogitoClient().getSessionsForYear(2010);
 
         finishedTalks.addAll(new VideoInformationMerger().mergeVideoAndSessionInfo(videos, sessions));
 
         for(Talk talk : finishedTalks) {
             for(Speaker speaker : talk.speakers()) { //must save reference types first
                 Speaker found = Speaker.find("bySlug", speaker.slug()).first();
 
                 if(null != found) {
                     speaker = found;
                 }
 
                 handleImages(talk, speaker);
                 speaker.save();
             }
 
             talk.save();
         }
     }
 
     private void handleImages(Talk talk, Speaker speaker) {
         HashMap<String, Integer> sizes = new HashMap<String, Integer>(){{
                 put("large", 200);
                 put("small", 80);
         }};
 
         ImageHandler handler = new ImageHandler("public/images/speakers");
         HashMap<String, ImageInfo> versions = handler.handleImage(speaker.slug(), talk.year(),
                                                                speaker.photoURL(), sizes);
 
        if((speaker.images() == null || speaker.images().isEmpty()) || isNewerVersions(versions, speaker.images()) ) {
             speaker.images(versions);
         }
     }
 
     private boolean isNewerVersions(HashMap<String, ImageInfo> newImages, HashMap<String, ImageInfo> oldImages) {
         return !newImages.isEmpty() && newImages.get("large").year() > oldImages.get("large").year();
     }
 
 }
