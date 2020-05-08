 package com.jtbdevelopment.e_eye_o.entities.impl;
 
 import com.jtbdevelopment.e_eye_o.entities.AppUser;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.entities.Observation;
 import com.jtbdevelopment.e_eye_o.entities.ObservationCategory;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Date: 11/4/12
  * Time: 7:26 PM
  */
 public class ObservationImpl extends AppUserOwnedObjectImpl implements Observation {
     private String comment = "";
     private LocalDateTime observationTimestamp = new LocalDateTime();
     private AppUserOwnedObject observationSubject;
     private boolean significant = false;
 
     private Set<ObservationCategory> categories = new HashSet<>();
 
     private boolean followUpNeeded = false;
     private LocalDate followUpReminder;
     private Observation followUpObservation;
 
     ObservationImpl(final AppUser appUser) {
         super(appUser);
     }
 
     @Override
     public AppUserOwnedObject getObservationSubject() {
         return observationSubject;
     }
 
     @Override
     public void setObservationSubject(final AppUserOwnedObject observationSubject) {
         this.observationSubject = observationSubject;
     }
 
     @Override
     public LocalDateTime getObservationTimestamp() {
         return observationTimestamp;
     }
 
     @Override
     public void setObservationTimestamp(final LocalDateTime observationDate) {
         this.observationTimestamp = observationDate;
     }
 
     @Override
     public boolean isSignificant() {
         return significant;
     }
 
     @Override
     public void setSignificant(final boolean significant) {
         this.significant = significant;
     }
 
     @Override
     public boolean isFollowUpNeeded() {
         return followUpNeeded;
     }
 
     @Override
     public void setFollowUpNeeded(final boolean followUpNeeded) {
         this.followUpNeeded = followUpNeeded;
     }
 
     @Override
     public LocalDate getFollowUpReminder() {
         return followUpReminder;
     }
 
     @Override
     public void setFollowUpReminder(final LocalDate followUpReminder) {
         this.followUpReminder = followUpReminder;
     }
 
     @Override
     public Observation getFollowUpObservation() {
         return followUpObservation;
     }
 
     @Override
     public void setFollowUpObservation(final Observation followUpObservation) {
         this.followUpObservation = followUpObservation;
     }
 
     @Override
     public Set<ObservationCategory> getCategories() {
         return Collections.unmodifiableSet(categories);
     }
 
     @Override
     public void setCategories(final Set<ObservationCategory> categories) {
         this.categories.clear();
         this.categories.addAll(categories);
     }
 
     @Override
     public void addCategory(final ObservationCategory observationCategory) {
         categories.add(observationCategory);
     }
 
     @Override
     public void addCategories(final Collection<ObservationCategory> observationCategories) {
         categories.addAll(observationCategories);
     }
 
     @Override
     public void removeCategory(final ObservationCategory observationCategory) {
         categories.remove(observationCategory);
     }
 
     @Override
     public String getComment() {
         return comment;
     }
 
     @Override
     public void setComment(final String comment) {
         this.comment = comment;
     }
 
     @Override
     public String getSummaryDescription() {
        return (observationSubject != null ? observationSubject.getSummaryDescription() : "?"
                 + " "
                 + observationTimestamp.toString("MMM dd")).trim();
     }
 }
