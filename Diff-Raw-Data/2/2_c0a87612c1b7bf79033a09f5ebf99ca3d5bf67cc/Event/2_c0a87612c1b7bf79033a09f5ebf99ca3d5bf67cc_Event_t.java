 package reevent.domain;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import reevent.domain.media.MediaBase;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 @Entity
 public class Event extends EntityBase {
     @Column(nullable = false)
     String name;
 
     /**
      * Date and time when the event starts
      */
     @Column(nullable = false)
     Date start;
 
     String genre;
 
     String performer;
 
     @Lob
     String description;
 
     @Embedded
     Location location = new Location();
 
     @ManyToOne(optional = false)
     User createdBy;
 
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
     Set<Feedback> feedbacks = new LinkedHashSet<Feedback>();
 
     @ManyToOne
     MediaBase mainPicture;
 
     @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
     @JoinTable
     Set<MediaBase> media = new LinkedHashSet<MediaBase>();
 
     public Event() {
     }
 
     public Event(String name, Date start) {
         this.name = name;
         this.start = start;
     }
     
     public Event(String name, Date start, String locationName) {
         this.name = name;
         this.start = start;
         this.location = new Location(locationName);
     }
     
     public Event(String name, Date start, String locationName, String locationAddress) {
         this.name = name;
         this.start = start;
         this.location = new Location(locationName, locationAddress);
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this).
                 append("performer", performer).
                 append("name", name).
                 append("start", start).
                 append("genre", genre).
                 append("location", location).
                 //append("createdBy", createdBy.getUsername()).
                 appendSuper(super.toString()).
                 toString();
     }
 
     public void setFeedbacks(Set<Feedback> feedbacks) {
 		this.feedbacks = feedbacks;
 	}
 
 	public void setMedia(Set<MediaBase> media) {
 		this.media = media;
 	}
 
 	public String getGenre() {
         return genre;
     }
 
     public void setGenre(String genre) {
         this.genre = genre;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Date getStart() {
         return start;
     }
 
     public void setStart(Date start) {
         this.start = start;
     }
 
     public User getCreatedBy() {
         return createdBy;
     }
 
     public void setCreatedBy(User createdBy) {
         this.createdBy = createdBy;
     }
 
     public Set<Feedback> getFeedbacks() {
         return feedbacks;
     }
 
     public Set<MediaBase> getMedia() {
         return media;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getPerformer() {
         return performer;
     }
 
     public void setPerformer(String performer) {
         this.performer = performer;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
     
     public String getLocationAddress() {
         return this.location.getLocationAddress();
     }
 
     public Double getLatitude() {
         return this.location.getLatitude();
     }
 
     public String getLocationName() {
         return this.location.getLocationName();
     }
     
 
     public void setLocationAddress(String address) {
     	this.location.setLocationAddress(address);
     }
 
     public void setLatitude(Double latitude) {
     	this.location.setLatitude(latitude);
     }
 
     public void setLocationName(String locationName) {
     	this.location.setLocationName(locationName);
     }
 
     public void setLongitude(Double longitude) {
     	this.location.setLongitude(longitude);
     }
 
     public Double getLongitude() {
         return this.location.getLongitude();
     }
 
     public boolean isLocationEmpty() {
         return this.location.isEmpty();
     }
 
     public MediaBase getMainPicture() {
         return mainPicture;
     }
 
     public void setMainPicture(MediaBase mainPicture) {
         this.mainPicture = mainPicture;
     }
 }
