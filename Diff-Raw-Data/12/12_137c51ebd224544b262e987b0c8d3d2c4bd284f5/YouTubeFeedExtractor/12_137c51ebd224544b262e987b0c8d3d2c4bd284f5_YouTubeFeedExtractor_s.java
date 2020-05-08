 package org.atlasapi.remotesite.youtube;
 
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.youtube.YouTubeModel.VideoEntry;
 import org.atlasapi.remotesite.youtube.YouTubeModel.VideoFeed;
 
 public class YouTubeFeedExtractor implements ContentExtractor<YouTubeFeedSource, Playlist> {
     
     private final ContentExtractor<YouTubeSource, Item> itemExtractor;
     
     public YouTubeFeedExtractor() {
         this(new YouTubeGraphExtractor());
     }
 
     public YouTubeFeedExtractor(ContentExtractor<YouTubeSource, Item> itemExtractor) {
         this.itemExtractor = itemExtractor;
     }
 
     @Override
     public Playlist extract(YouTubeFeedSource source) {
         VideoFeed feed = source.getVideoFeed();
         
         Playlist playlist = new Playlist(source.getUri(), YouTubeFeedCanonicaliser.curieFor(source.getUri()), Publisher.YOUTUBE);
         playlist.setMediaType(MediaType.VIDEO);
         
        for (VideoEntry video: feed.items) {
            Item item = itemExtractor.extract(new YouTubeSource(video, new YoutubeUriCanonicaliser().canonicalUriFor(video.id)));
            playlist.addItem(item);
         }
         
         return playlist;
     }
 }
