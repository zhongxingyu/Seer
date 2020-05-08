 package org.atlasapi.media.channel;
 
 import java.util.Set;
 
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Image;
 import org.atlasapi.media.entity.ImageTheme;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.RelatedLink;
 import org.joda.time.Duration;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 public class Channel extends Identified {
     
     public static final Predicate<Image> IS_PRIMARY_IMAGE = new Predicate<Image>() {
         @Override
         public boolean apply(Image input) {
             return input.getTheme().equals(ImageTheme.LIGHT_OPAQUE);
         }
     };
     
     public static final Builder builder() {
         return new Builder();
     }
     
     public static class Builder {
         
         private Publisher source;
         private String uri;
         private String key;
         private Publisher broadcaster;
         private Set<TemporalField<Image>> images = Sets.newHashSet();
         private Set<TemporalField<String>> titles = Sets.newHashSet();
         private Set<RelatedLink> relatedLinks = Sets.newHashSet();
         private MediaType mediaType;
         private Boolean regional;
         private Boolean highDefinition;
         private Duration timeshift;
         private Set<Publisher> availableFrom = ImmutableSet.of();
         private Set<Long> variations = Sets.newHashSet();
         private Long parent;
         private Set<ChannelNumbering> channelNumbers = Sets.newHashSet();
         private LocalDate startDate;
         private LocalDate endDate;
         
         public Builder withSource(Publisher source) {
             this.source = source;
             return this;
         };
         
         public Builder withUri(String uri) {
             this.uri = uri;
             return this;
         }
         
         public Builder withTitle(String title) {
             return withTitle(title, null, null);
         };
         
         public Builder withTitle(String title, LocalDate startDate) {
             return withTitle(title, startDate, null);
         };
         
         public Builder withTitle(String title, LocalDate startDate, LocalDate endDate) {
             this.titles.add(new TemporalField<String>(title, startDate, endDate));
             return this;
         };
         
         public Builder withImage(Image image) {
             return withImage(image, null, null);
         };
         
         public Builder withImage(Image image, LocalDate startDate) {
             return withImage(image, startDate, endDate);
         };
         
         public Builder withImage(Image image, LocalDate startDate, LocalDate endDate) {
             this.images.add(new TemporalField<Image>(image, startDate, endDate));
             return this;
         };
         
         public Builder withRelatedLink(RelatedLink relatedLink) {
             this.relatedLinks.add(relatedLink);
             return this;
         }
         
         public Builder withMediaType(MediaType mediaType) {
             this.mediaType = mediaType;
             return this;
         };
         
         @Deprecated
         public Builder withKey(String key) {
             this.key = key;
             return this;
         };
         
         public Builder withHighDefinition(Boolean highDefinition) {
             this.highDefinition = highDefinition;
             return this;
         };
         
         public Builder withRegional(Boolean regional) {
             this.regional = regional;
             return this;
         };
         
         public Builder withTimeshift(Duration timeshift) {
             this.timeshift = timeshift;
             return this;
         };
         
         public Builder withBroadcaster(Publisher broadcaster) {
             this.broadcaster = broadcaster;
             return this;
         };
         
         public Builder withAvailableFrom(Iterable<Publisher> availableFrom) {
             this.availableFrom = ImmutableSet.copyOf(availableFrom);
             return this;
         };
         
         public Builder withVariationIds(Iterable<Long> variationIds) {
             this.variations = Sets.newHashSet(variationIds);
             return this;
         };
         
         public Builder withVariations(Iterable<Channel> variations) {
             this.variations.clear();
             for (Channel variation : variations) {
                 withVariation(variation);
             }
             return this;
         };
         
         public Builder withVariation(Long variationId) {
             this.variations.add(variationId);
             return this;
         };
         
         public Builder withVariation(Channel variation) {
             this.variations.add(variation.getId());
             return this;
         };
         
         public Builder withParent(Channel parent) {
             this.parent = parent.getId();
             return this;
         }
         
         public Builder withParent(Long parentId) {
             this.parent = parentId;
             return this;
         }
         
         public Builder withChannelNumbers(Iterable<ChannelNumbering> channelNumbers) {
             this.channelNumbers = Sets.newHashSet(channelNumbers);
             return this;
         }
         
         public Builder withChannelNumber(ChannelNumbering channelNumber) {
             this.channelNumbers.add(channelNumber);
             return this;
         }
         
         public Builder withStartDate(LocalDate startDate) {
             this.startDate = startDate;
             return this;
         }
         
         public Builder withEndDate(LocalDate endDate) {
             this.endDate = endDate;
             return this;
         }
         
         public Channel build() {
             return new Channel(source, titles, images, relatedLinks, key, highDefinition, regional, timeshift, mediaType, uri, broadcaster, availableFrom, variations, parent, channelNumbers, startDate, endDate);
         }
     }
     
     
     private Publisher source;
     private Set<TemporalField<String>> titles = Sets.newHashSet();
     private Set<TemporalField<Image>> images = Sets.newHashSet();
     private Set<RelatedLink> relatedLinks = Sets.newHashSet();
     private MediaType mediaType;
     private String key;
     private Boolean highDefinition;
     private Boolean regional;
     private Duration timeshift;
     private Publisher broadcaster;
     private Set<Publisher> availableFrom;
     private Set<Long> variations = Sets.newHashSet();
     private Long parent;
     private Set<ChannelNumbering> channelNumbers = Sets.newHashSet();
     private LocalDate startDate;
     private LocalDate endDate;
     
     @Deprecated
     public Channel(Publisher publisher, String title, String key, Boolean highDefinition, MediaType mediaType, String uri) {
         this(publisher, ImmutableSet.of(new TemporalField<String>(title, null, null)), ImmutableSet.<TemporalField<Image>>of(), ImmutableSet.<RelatedLink>of(), key, highDefinition, null, null, mediaType, uri, null, ImmutableSet.<Publisher>of(), ImmutableSet.<Long>of(), null, ImmutableSet.<ChannelNumbering>of(), null, null);
     }
     
     @Deprecated //Required for OldChannel
     protected Channel() { }
     
     private Channel(Publisher publisher, Set<TemporalField<String>> titles, Set<TemporalField<Image>> images, Set<RelatedLink> relatedLinks, String key, Boolean highDefinition, Boolean regional, Duration timeshift, MediaType mediaType, String uri, Publisher broadcaster, Iterable<Publisher> availableFrom, Iterable<Long> variations, Long parent, Iterable<ChannelNumbering> channelNumbers, LocalDate startDate, LocalDate endDate) {
         super(uri);
         this.source = publisher;
         this.regional = regional;
         this.timeshift = timeshift;
         this.titles = Sets.newHashSet(titles);
         this.images = Sets.newCopyOnWriteArraySet(images);
         this.relatedLinks = Sets.newHashSet(relatedLinks);
         this.parent = parent;
         this.key = key;
         this.highDefinition = highDefinition;
         this.mediaType = mediaType;
         this.broadcaster = broadcaster;
         this.startDate = startDate;
         this.endDate = endDate;
         this.availableFrom = ImmutableSet.copyOf(availableFrom);
         this.variations = Sets.newHashSet(variations);
         this.channelNumbers = Sets.newHashSet(channelNumbers);
     }
     
     public String getUri() {
         return getCanonicalUri();
     }
     
     /**
      * Gets the current or next title
      * @return the current title, if one exists, otherwise the 
      * first future title
      */
     public String getTitle() {
         return TemporalField.currentOrFutureValue(titles);
     }
     
     public String getTitleForDate(LocalDate date) {
        return Iterables.getOnlyElement(TemporalField.valuesForDate(titles, date), null);
     }
     
     public Iterable<TemporalField<String>> getAllTitles() {
         return ImmutableSet.copyOf(titles);
     }
     
     public Boolean getHighDefinition() {
         return highDefinition;
     }
     
     public Boolean getRegional() {
         return regional;
     }
     
     public Duration getTimeshift() {
         return timeshift;
     }
     
     public MediaType getMediaType() {
         return mediaType;
     }
     
     public Publisher getSource() {
         return source;
     }
     
     public Publisher getBroadcaster() {
         return broadcaster;
     }
     
     public Set<Publisher> getAvailableFrom() {
         return availableFrom;
     }
     
     public Set<Long> getVariations() {
         return variations;
     }
     
     public Long getParent() {
         return parent;
     }
     
     public Set<ChannelNumbering> getChannelNumbers() {
         return ImmutableSet.copyOf(channelNumbers);
     }
     
     @Deprecated
     public String getKey() {
         return key;
     }
     
     /**
      * @return primary channel image, or first future primary image if 
      * no current image
      */
     public Image getImage() {
         Iterable<TemporalField<Image>> primaryImages = Iterables.filter(
             images, 
             new Predicate<TemporalField<Image>>() {
                 @Override
                 public boolean apply(TemporalField<Image> input) {
                     Image image = input.getValue();
                     return IS_PRIMARY_IMAGE.apply(image);
                 }
             }
         );
         return TemporalField.currentOrFutureValue(primaryImages);
     }
     
     public Set<Image> getImages() {
         return TemporalField.currentValues(images);
     }
     
     public Set<Image> getImagesForDate(LocalDate date) {
         return TemporalField.valuesForDate(images, date);
     }
     
     public Iterable<TemporalField<Image>> getAllImages() {
         return ImmutableSet.copyOf(images);
     }
     
     public LocalDate getStartDate() {
         return startDate;
     }
     
     public Set<RelatedLink> getRelatedLinks() {
         return relatedLinks;
     }
     
     public LocalDate getEndDate() {
         return endDate;
     }
     
     public void setSource(Publisher source) {
         this.source = source;
     }
     
     public void addTitle(String title) {
         addTitle(title, null);
     }
     
     public void addTitle(String title, LocalDate startDate) {
         addTitle(title, startDate, null);
     }
     
     public void addTitle(String title, LocalDate startDate, LocalDate endDate) {
         this.titles.add(new TemporalField<String>(title, startDate, endDate));
     }
     
     public void addTitle(TemporalField<String> title) {
         this.titles.add(title);
     }
     
     public void setTitles(Iterable<TemporalField<String>> titles) {
         this.titles = Sets.newHashSet(titles);
     }
     
     public void setMediaType(MediaType mediaType) {
         this.mediaType = mediaType;
     }
     
     public void setKey(String key) {
         this.key = key;
     }
     
     public void setHighDefinition(Boolean highDefinition) {
         this.highDefinition = highDefinition;
     }
 
     public void setRegional(Boolean regional) {
         this.regional = regional;
     }
 
     public void setTimeshift(Duration timeshift) {
         this.timeshift = timeshift;
     }
     
     public void setBroadcaster(Publisher broadcaster) {
         this.broadcaster = broadcaster;
     }
     
     public void setAvailableFrom(Iterable<Publisher> availableOn) {
         this.availableFrom = ImmutableSet.copyOf(availableOn);
     }
     
     public void setVariations(Iterable<Channel> variations) {
         this.variations.clear();
         for (Channel variation : variations) {
             addVariation(variation);
         }
     }
     
     public void setVariationIds(Iterable<Long> variationIds) {
         this.variations = Sets.newHashSet(variationIds);
     }
     
     public void addVariation(Channel variation) {
         this.variations.add(variation.getId());
     }
     
     public void addVariation(Long variationId) {
         this.variations.add(variationId);
     }
     
     public void setParent(Channel parent) {
         this.parent = parent.getId();
     }
     
     public void setParent(Long parentId) {
         this.parent = parentId;
     }
     
     public void setChannelNumbers(Iterable<ChannelNumbering> channelNumbers) {
         this.channelNumbers = Sets.newHashSet(channelNumbers);
     }
     
     public void addChannelNumber(ChannelNumbering channelNumber) {
         this.channelNumbers.add(channelNumber);
     }
 
     public void setStartDate(LocalDate startDate) {
         this.startDate = startDate;
     }
 
     public void setEndDate(LocalDate endDate) {
         this.endDate = endDate;
     }
     
     public void addChannelNumber(ChannelGroup channelGroup, String channelNumber, LocalDate startDate, LocalDate endDate) {
         ChannelNumbering channelNumbering = ChannelNumbering.builder()
             .withChannelGroup(channelGroup)
             .withChannelNumber(channelNumber)
             .withStartDate(startDate)
             .withEndDate(endDate)
             .build();
         this.channelNumbers.add(channelNumbering);
     };
     
     public void addImage(Image image) {
         addImage(image, null, null);
     }
     
     public void addImage(Image image, LocalDate startDate) {
         addImage(image, startDate, null);
     }
     
     public void addImage(Image image, LocalDate startDate, LocalDate endDate) {
         this.images.add(new TemporalField<Image>(image, startDate, endDate));
     }
     
     public void addImage(TemporalField<Image> image) {
         this.images.add(image);
     }
     
     public void setImages(Iterable<TemporalField<Image>> images) {
         this.images = Sets.newHashSet(images);
     }
     
     public void addRelatedLink(RelatedLink relatedLink) {
         this.relatedLinks.add(relatedLink);
     }
     
     public void setRelatedLinks(Iterable<RelatedLink> relatedLinks) {
         this.relatedLinks = Sets.newHashSet(relatedLinks);
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj instanceof Channel) {
             Channel target = (Channel) obj;
             return getId() != null ? Objects.equal(getId(), target.getId()) 
                                    : Objects.equal(getUri(), target.getUri());
         }
         return false;
     }
     
     @Override
     public int hashCode() {
         return getId() != null ? getId().hashCode() : getUri().hashCode();
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                 .addValue(getId())
                 .addValue(getCanonicalUri())
                 .toString();
     }
     
     public static final Function<Channel, String> TO_KEY = new Function<Channel, String>() {
         @Override
         public String apply(Channel input) {
             return input.getKey();
         }
     };
 
 }
