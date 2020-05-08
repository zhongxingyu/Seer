 package org.atlasapi.media.entity.simple;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalDate;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Ordering;
 
 @XmlRootElement(namespace=PLAY_SIMPLE_XML.NS)
 @XmlType(name="Channel", namespace=PLAY_SIMPLE_XML.NS)
 public class Channel extends Aliased {
 
     private static final Ordering<HistoricalChannelEntry> HISTORY_ORDERING = Ordering.natural();
     private static final Ordering<ChannelNumbering> NUMBERING_ORDERING = new ChannelNumberingOrdering();
 
     private PublisherDetails publisher;
     private String title;
     private String image;
     private String mediaType;
     private Boolean highDefinition;        
     private Boolean regional;
     private Long timeshift;
     private List<ChannelNumbering> channelGroups;
     private PublisherDetails broadcaster;
     private Set<PublisherDetails> availableFrom;
     private Channel parent;
     private Set<Channel> variations;
     private List<HistoricalChannelEntry> history;
     private Date startDate;
     private Date endDate;
 
     public void setPublisherDetails(PublisherDetails publisherDetails) {
         this.publisher = publisherDetails;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setImage(String image) {
         this.image = image;
     }
     
     public void setMediaType(String mediaType) {
         this.mediaType = mediaType;
     }
     
     public void setHighDefinition(Boolean highDefinition) {
         this.highDefinition = highDefinition;
     }
     
     public void setRegional(Boolean regional) {
         this.regional = regional;
     }
     
     public void setTimeshift(Long timeshift) {
         this.timeshift = timeshift;
     }
 
     @XmlElement(name = "publisher")
     public PublisherDetails getPublisherDetails() {
         return this.publisher;
     }
 
     public String getTitle() {
         return this.title;
     }
 
     public String getImage() {
         return this.image;
     }
     
     public String getMediaType() {
         return this.mediaType;
     }
     
     public Boolean getHighDefinition() {
         return highDefinition;
     }
     
     public Boolean getRegional() {
         return regional;
     }
     
     public Long getTimeshift() {
         return timeshift;
     }
 
    public void setChannelGroups(Iterable<ChannelNumbering> channelNumbering) {
         this.channelGroups = NUMBERING_ORDERING.immutableSortedCopy(channelNumbering);
     }
 
     @XmlElementWrapper(name = "channelGroups")
     @XmlElement(name = "channelNumbering")
     public List<ChannelNumbering> getChannelGroups() {
         return channelGroups;
     }
 
     public PublisherDetails getBroadcaster() {
         return broadcaster;
     }
 
     public void setBroadcaster(PublisherDetails broadcaster) {
         this.broadcaster = broadcaster;
     }
     
    public void setAvailableFrom(Iterable<PublisherDetails> availableFrom) {
         this.availableFrom = ImmutableSet.copyOf(availableFrom);
     }
     
     @XmlElementWrapper(name = "availableFrom")
     @XmlElement(name = "publisher")
     public Set<PublisherDetails> getAvailableFrom() {
         return availableFrom;
     }
 
     public Channel getParent() {
         return parent;
     }
 
     public void setParent(Channel parent) {
         this.parent = parent;
     }
 
     @XmlElementWrapper(name = "variations")
     @XmlElement(name = "variation")
     public Set<Channel> getVariations() {
         return variations;
     }
 
    public void setVariations(Iterable<Channel> variations) {
         this.variations = ImmutableSet.copyOf(variations);
     }
 
     @XmlElementWrapper(name = "history")
     @XmlElement(name = "historyEntry")
     public List<HistoricalChannelEntry> getHistory() {
         return history;
     }
 
     public void setHistory(List<HistoricalChannelEntry> history) {
         this.history = HISTORY_ORDERING.immutableSortedCopy(history);
     }
 
     @XmlElement(name = "startDate")    
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(LocalDate startDate) {
         if (startDate != null) {
             this.startDate = startDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
         }
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public void setEndDate(LocalDate endDate) {
         if (endDate != null) {
             this.startDate = endDate.toDate();
         }
     }
 }
