 package org.atlasapi.remotesite.youtube;
 
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 import org.atlasapi.remotesite.youtube.YouTubeModel.VideoFeed;
 
import com.metabroadcast.common.http.HttpStatusCodeException;

 public class YouTubeFeedAdapter implements SiteSpecificAdapter<Playlist> {
     
     private final RemoteSiteClient<VideoFeed> gdataClient;
     private final ContentExtractor<YouTubeFeedSource, Playlist> feedExtractor;
     
     public YouTubeFeedAdapter() {
         this(new YouTubeFeedClient(), new YouTubeFeedExtractor());
     }
     
     public YouTubeFeedAdapter(RemoteSiteClient<VideoFeed> gdataClient, ContentExtractor<YouTubeFeedSource, Playlist> feedExtractor) {
         this.gdataClient = gdataClient;
         this.feedExtractor = feedExtractor;
     }
 
     @Override
     public boolean canFetch(String uri) {
         return new YouTubeFeedCanonicaliser().canonicalise(uri) != null;
     }
 
     @Override
     public Playlist fetch(String uri) {
         try {
             VideoFeed feed = gdataClient.get(uri);
             
             return feedExtractor.extract(new YouTubeFeedSource(feed, uri));
        } catch (HttpStatusCodeException e) {
            return null;
         } catch (Exception e) {
             throw new FetchException("Failed to fetch: " + uri, e);
         }
     }
 }
