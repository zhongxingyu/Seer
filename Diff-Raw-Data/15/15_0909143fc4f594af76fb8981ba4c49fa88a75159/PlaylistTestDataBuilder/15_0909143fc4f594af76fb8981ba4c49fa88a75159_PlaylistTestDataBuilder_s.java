 package org.atlasapi.media.entity.testing;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.media.entity.simple.Description;
 import org.atlasapi.media.entity.simple.Item;
 import org.atlasapi.media.entity.simple.Playlist;
 import org.atlasapi.media.entity.simple.PublisherDetails;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSortedSet;
 
 public class PlaylistTestDataBuilder {
     private static Integer uniqueId = 1;
     
     private String uri;
     private String curie;
     
     private Set<String> aliases;
     private List<Item> clips;
     
     private String mediaType;
     private String description;
     private Set<String> genres;
     private String image;
     private PublisherDetails publisher;
     private Set<String> sameAs;
     private Set<String> tags;
     private String thumbnail;
     private String title;
     private String id;
     
     private List<Description> contents;
     
     public static PlaylistTestDataBuilder playlist() {
         return new PlaylistTestDataBuilder();
     }
     
     private PlaylistTestDataBuilder() {
         resetAttributes();
     }
     
     private void resetAttributes() {
         
         uri = "http://test.metabroadcast.com/item/default";
         curie = "mbtest:i-default";
         
         id = String.valueOf(uniqueId++);
         uri = "http://test.metabroadcast.com/unique/items/" + id;
         curie = "mbtest:i-" + id;
         
         aliases = ImmutableSet.of();
         clips = ImmutableList.of();
         mediaType = null;
         description = "Default test item created by PlaylistTestDataBuilder";
         genres = ImmutableSet.of("http://test.metabroadcast.com/genres/default");
         image = "http://test.metabroadcast.com/images/default";
         publisher = defaultPublisher();
         sameAs = ImmutableSet.of();
         tags = ImmutableSet.of();
         thumbnail = "http://test.metabroadcast.com/thumbnails/default";
         title = "Default Test Item";
         contents = ImmutableList.of();
     }
     
     private PublisherDetails defaultPublisher() {
         PublisherDetails publisher = new PublisherDetails("http://test.metabroadcast.com/publishers/default");
         publisher.setName("Metabroadcast Test");
         publisher.setCountry("UK");
         return publisher;
     }
     
     public Playlist build() {
         return buildPlaylistAttributes(new Playlist());
     }
     
     private Playlist buildPlaylistAttributes(Playlist playlist) {
         playlist.setId(id);
         playlist.setAliases(aliases);
         playlist.setClips(clips);
         playlist.setMediaType(mediaType);
         playlist.setDescription(description);
         playlist.setGenres(genres);
         playlist.setImage(image);
         playlist.setPublisher(publisher);
         playlist.setSameAs(sameAs);
         playlist.setTags(tags);
         playlist.setThumbnail(thumbnail);
         playlist.setTitle(title);
         playlist.setUri(uri);
         playlist.setCurie(curie);
         playlist.setContent(contents);
         return playlist;
     }
     
     public PlaylistTestDataBuilder withSameAs(String... sameAs) {
         this.sameAs = ImmutableSortedSet.copyOf(sameAs);
         return this;
     }
     
     public PlaylistTestDataBuilder withTitle(String title) {
         this.title = title;
         return this;
     }
     
     public PlaylistTestDataBuilder withUri(String uri) {
         this.uri = uri;
         return this;
     }
     
     public PlaylistTestDataBuilder withContent(Description... content) {
         this.contents = ImmutableList.copyOf(content);
         return this;
     }
    
    public PlaylistTestDataBuilder withPlaylists(Playlist... playlists) {
        this.playlists = ImmutableList.copyOf(playlists);
        return this;
    }
 }
