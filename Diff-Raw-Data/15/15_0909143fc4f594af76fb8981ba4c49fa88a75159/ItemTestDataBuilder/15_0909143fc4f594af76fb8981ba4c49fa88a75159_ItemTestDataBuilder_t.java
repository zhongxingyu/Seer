 package org.atlasapi.media.entity.testing;
 
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 
 import org.atlasapi.media.entity.simple.BrandSummary;
 import org.atlasapi.media.entity.simple.Broadcast;
 import org.atlasapi.media.entity.simple.Item;
 import org.atlasapi.media.entity.simple.Location;
 import org.atlasapi.media.entity.simple.PublisherDetails;
 import org.atlasapi.media.entity.simple.SeriesSummary;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSortedSet;
 
 public class ItemTestDataBuilder {
     private static Integer uniqueId = 1;
     
     private String uri;
     private String curie;
     
     private Set<String> aliases;
     private BrandSummary brand;
     private SortedSet<Broadcast> broadcasts;
     private List<Item> clips;
 
     private String mediaType;
 
     private String description;
     private Integer episodeNumber;
     private Set<String> genres;
     private String image;
     private Set<Location> locations;
     private PublisherDetails publisher;
     private Set<String> sameAs;
     private Integer seriesNumber;
     private SeriesSummary seriesSummary;
     private Set<String> tags;
     private String thumbnail;
     private String title;
     private String id;
     
     public static ItemTestDataBuilder item() {
     	return new ItemTestDataBuilder();
     }
     
     private ItemTestDataBuilder() {
         resetAttributes();
     }
     
     private void resetAttributes() {
     	
         uri = "http://test.metabroadcast.com/item/default";
         curie = "mbtest:i-default";
         
         id = String.valueOf(uniqueId++);
         uri = "http://test.metabroadcast.com/unique/items/" + id;
         curie = "mbtest:i-" + id;
         
         aliases = ImmutableSet.of();
         brand = defaultBrand();
         broadcasts = ImmutableSortedSet.of();
         clips = ImmutableList.of();
         mediaType = null;
         description = "Default test item created by ItemTestDataBuilder";
         episodeNumber = 1;
         genres = ImmutableSet.of("http://test.metabroadcast.com/genres/default");
         image = "http://test.metabroadcast.com/images/default";
         locations = ImmutableSet.of();
         publisher = defaultPublisher();
         sameAs = ImmutableSet.of();
         seriesNumber = 1;
         seriesSummary = defaultSeriesSummary();
         tags = ImmutableSet.of();
         thumbnail = "http://test.metabroadcast.com/thumbnails/default";
         title = "Default Test Item";
     }
     
     private SeriesSummary defaultSeriesSummary() {
         SeriesSummary series = new SeriesSummary("http://test.metabroadcast.com/series/default");
         series.setCurie("mbtest:s-default");
         series.setDescription("Default test series created by ItemTestDataBuilder");
         series.setSeriesNumber(1);
         series.setTitle("Series 1");
         return series;
     }
     
     private BrandSummary defaultBrand() {
         BrandSummary brand = new BrandSummary("http://test.metabroadcast.com/brand/default");
         brand.setCurie("mbtest:b-default");
         brand.setDescription("The default brand created by the ItemTestDataBuilder");
         brand.setTitle("Metabroadcast Test Default Brand");
         return brand;
     }
     
     private PublisherDetails defaultPublisher() {
         PublisherDetails publisher = new PublisherDetails("http://test.metabroadcast.com/publishers/default");
         publisher.setName("Metabroadcast Test");
         publisher.setCountry("UK");
         return publisher;
     }
     
     public Item build() {
         return buildItemAttributes(new Item());
     }
     
     private Item buildItemAttributes(Item item) {
         item.setId(id);
         item.setAliases(aliases);
         item.setBrandSummary(brand);
         item.setBroadcasts(broadcasts);
         item.setClips(clips);
         item.setMediaType(mediaType);
         item.setDescription(description);
         item.setEpisodeNumber(episodeNumber);
         item.setGenres(genres);
         item.setImage(image);
         item.setLocations(locations);
         item.setPublisher(publisher);
         item.setSameAs(sameAs);
         item.setSeriesNumber(seriesNumber);
         item.setSeriesSummary(seriesSummary);
         item.setTags(tags);
         item.setThumbnail(thumbnail);
         item.setTitle(title);
         item.setUri(uri);
         item.setCurie(curie);
         return item;
     }
     
     public ItemTestDataBuilder withBroadcasts(Broadcast... broadcasts) {
     	this.broadcasts = ImmutableSortedSet.copyOf(broadcasts);
     	return this;
     }
     
     public ItemTestDataBuilder withSameAs(String... sameAs) {
         this.sameAs = ImmutableSortedSet.copyOf(sameAs);
         return this;
     }
     
     public ItemTestDataBuilder withLocations(Location... locations) {
         this.locations = ImmutableSet.copyOf(locations);
         return this;
     }
     
     public ItemTestDataBuilder withClips(Item... clips) {
         this.clips = ImmutableList.copyOf(clips);
         return this;
     }
     
     public ItemTestDataBuilder withTitle(String title) {
         this.title = title;
         return this;
     }
     
     public ItemTestDataBuilder withUri(String uri) {
         this.uri = uri;
         return this;
     }
     
     public ItemTestDataBuilder withId(String id) {
         this.id = id;
         return this;
     }
     
     public ItemTestDataBuilder withTags(String... tags) {
         this.tags = ImmutableSet.copyOf(tags);
         return this;
     }
     
     public ItemTestDataBuilder withAliases(String... aliases) {
         this.aliases = ImmutableSet.copyOf(aliases);
         return this;
     }
 }
