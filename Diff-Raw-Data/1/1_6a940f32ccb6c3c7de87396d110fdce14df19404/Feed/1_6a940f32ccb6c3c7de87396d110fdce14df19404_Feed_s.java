 package com.radioreference.model;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.ElementList;
 import org.simpleframework.xml.Root;
 
 import java.util.List;
 
 @Root
 public class Feed {
     @Attribute(name = "id")
     private long id;
     @Attribute(name = "status")
     private int status;
     @Attribute(name = "listeners")
     private int listeners;
     @Attribute(name = "descr")
     private String description;
     @Attribute(name = "genre")
     private String genre;
     @Attribute(name = "mount")
     private String mount;
     @Attribute(name = "bitrate")
     private int bitRate;
 
     @ElementList(name = "counties", inline = false, entry = "county", type = County.class, required = false)
     private List<County> counties;
     @ElementList(name = "relays", inline = false, entry = "relay", type = Relay.class)
     private List<Relay> relays;
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public int getStatus() {
         return status;
     }
 
     public void setStatus(int status) {
         this.status = status;
     }
 
     public int getListeners() {
         return listeners;
     }
 
     public void setListeners(int listeners) {
         this.listeners = listeners;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getGenre() {
         return genre;
     }
 
     public void setGenre(String genre) {
         this.genre = genre;
     }
 
     public String getMount() {
         return mount;
     }
 
     public void setMount(String mount) {
         this.mount = mount;
     }
 
     public int getBitRate() {
         return bitRate;
     }
 
     public void setBitRate(int bitRate) {
         this.bitRate = bitRate;
     }
 
     public List<County> getCounties() {
         return counties;
     }
 
     public void setCounties(List<County> counties) {
         this.counties = counties;
     }
 
     public List<Relay> getRelays() {
         return relays;
     }
 
     public void setRelays(List<Relay> relays) {
         this.relays = relays;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Feed)) return false;
 
         Feed feed = (Feed) o;
 
         if (bitRate != feed.bitRate) return false;
         if (id != feed.id) return false;
         if (listeners != feed.listeners) return false;
         if (status != feed.status) return false;
         if (counties != null ? !counties.equals(feed.counties) : feed.counties != null) return false;
         if (description != null ? !description.equals(feed.description) : feed.description != null) return false;
         if (genre != null ? !genre.equals(feed.genre) : feed.genre != null) return false;
         if (mount != null ? !mount.equals(feed.mount) : feed.mount != null) return false;
         if (relays != null ? !relays.equals(feed.relays) : feed.relays != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = (int) (id ^ (id >>> 32));
         result = 31 * result + status;
         result = 31 * result + listeners;
         result = 31 * result + (description != null ? description.hashCode() : 0);
         result = 31 * result + (genre != null ? genre.hashCode() : 0);
         result = 31 * result + (mount != null ? mount.hashCode() : 0);
         result = 31 * result + bitRate;
         result = 31 * result + (counties != null ? counties.hashCode() : 0);
         result = 31 * result + (relays != null ? relays.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "Feed{" +
                 "id=" + id +
                 ", status=" + status +
                 ", listeners=" + listeners +
                 ", description=" + description +
                 ", genre=" + genre +
                 ", mount='" + mount + '\'' +
                 ", bitRate='" + bitRate + '\'' +
                ", counties=" + counties +
                 ", relays=" + relays +
                 '}';
     }
 }
