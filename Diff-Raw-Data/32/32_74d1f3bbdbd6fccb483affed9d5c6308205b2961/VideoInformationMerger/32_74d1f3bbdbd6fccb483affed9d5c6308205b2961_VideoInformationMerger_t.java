 package models;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import models.domain.IncogitoSession;
 import models.domain.Video;
 import models.domain.VimeoVideo;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class VideoInformationMerger {
 
     public List<Video> mergeVideoAndSessionInfo(List<VimeoVideo> vimeoVideos, List<IncogitoSession> sessions) {
         List<Video> videos = new ArrayList<Video>();
 
         for(VimeoVideo vVideo : vimeoVideos) {
             Video video = new Video();
             int index = titleMatches(vVideo, sessions);
 
             if(index >= 0) {
                 IncogitoSession session = sessions.get(index);
                 video.talkAbstract(session.talkAbstract());
                 video.id(vVideo.id());
                 video.title(session.title());
                 videos.add(video);
             } else {
                 videos.add(new Video(vVideo));
             }
 
         }
 
         return videos;
     }
 
     private int titleMatches(final VimeoVideo vVideo, List<IncogitoSession> sessions) {
 
         Predicate<IncogitoSession> exactTitleMatcher = new Predicate<IncogitoSession>() {
             @Override
             public boolean apply(IncogitoSession session) {
                 if(StringUtils.isBlank(vVideo.title()) || StringUtils.isBlank(session.title())) {
                     return false;
                 }
                 return vVideo.title().equals(session.title());
             }
         };
 
         Predicate<IncogitoSession> levenshteinTitleMatcher = new Predicate<IncogitoSession>() {
             @Override
             public boolean apply(IncogitoSession session) {
                 return withInEditDistance(vVideo.title(), session.title());
             }
 
             private boolean withInEditDistance(String videoTitle, String sessionTitle) {
                 int distance = StringUtils.getLevenshteinDistance(videoTitle, sessionTitle);
                 return distance <= 2;
             }
         };
 
 
         int index = Iterables.indexOf(sessions, exactTitleMatcher);
 
         if(index < 0) {
             return Iterables.indexOf(sessions, levenshteinTitleMatcher);
         }
 
         return index;
     }
 
 }
