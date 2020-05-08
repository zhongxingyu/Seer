 package com.thoughtworks.twu.domain;
 
 import com.thoughtworks.twu.utils.ApplicationClock;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.joda.time.DateTime;
 
 public class Talk {
     public Talk(Presentation presentation, String venue, DateTime dateTime, DateTime lastModifiedAt) {
         this.presentation = presentation;
         this.dateTime = dateTime;
         this.venue = venue;
         this.lastModifiedAt = lastModifiedAt;
     }
 
     private DateTime dateTime;
 
 
 
     public void setTalkId(int talkId) {
         this.talkId = talkId;
     }
 
     Presentation presentation;
     int talkId;
     String venue;
 
     DateTime lastModifiedAt;
 
     public int getTalkId() {
         return talkId;
     }
 
 
     public DateTime getLastModifiedAt() {
         return lastModifiedAt;
     }
 
     public Presentation getPresentation() {
         return presentation;
     }
 
     public String getVenue() {
 
         return venue;
     }
 
     public Talk() {
     }
 
     public int getId() {
         return talkId;
     }
 
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 
 
 
     public DateTime getDateTime() {
         return dateTime;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Talk talk = (Talk) o;
         if (dateTime != null ? !dateTime.equals(talk.dateTime) : talk.dateTime != null) return false;
         if (presentation != null ? !presentation.equals(talk.presentation) : talk.presentation != null) return false;
         if (venue != null ? !venue.equals(talk.venue) : talk.venue != null) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = dateTime != null ? dateTime.hashCode() : 0;
         result = 31 * result + (presentation != null ? presentation.hashCode() : 0);
         result = 31 * result + (venue != null ? venue.hashCode() : 0);
         return result;
     }
 
     public Boolean isUpcoming() {
        return dateTime.isAfter(new ApplicationClock().now());
     }
 
 
 }
