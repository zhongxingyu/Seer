 package uk.ac.kcl.inf.provoking.model;
 
 import java.util.Date;
 import uk.ac.kcl.inf.provoking.model.util.AttributeHolder;
 import uk.ac.kcl.inf.provoking.model.util.Influenceable;
 import uk.ac.kcl.inf.provoking.model.util.Term;
 
 public class Activity extends AttributeHolder implements Description, Influenceable {
     private static Term[] CLASS_TERMS = terms (Term.Activity);
     private Date _startedAt;
     private Date _endedAt;
     private Location _location;
     
     public Activity (Object identifier) {
         this (identifier, null, null);
     }
 
     public Activity (Object identifier, Date startedAt, Date endedAt, Location location) {
         super (identifier);
         _startedAt = startedAt;
         _endedAt = endedAt;
        _location = location;
     }
 
     public Activity (Object identifier, Date startedAt, Date endedAt) {
         this (identifier, startedAt, endedAt, null);
     }
 
     public Activity (Object identifier, Location location) {
         this (identifier, null, null, location);
     }
     
     public Location getLocation () {
         return _location;
     }
     
     public void setLocation (Location location) {
         _location = location;
     }
 
     public Date getStartedAt () {
         return _startedAt;
     }
 
     public void setStartedAt (Date startedAt) {
         if (startedAt != null) {
             _startedAt = startedAt;
         }
     }
 
     public Date getEndedAt () {
         return _endedAt;
     }
 
     public void setEndedAt (Date endedAt) {
         if (endedAt != null) {
             _endedAt = endedAt;
         }
     }
     
     public static Activity reference (Object identifier) {
         Activity reference = new Activity (identifier);
         reference.setIsReference (true);
         return reference;
     }
 
     @Override
     public Term[] getClassTerms () {
         return CLASS_TERMS;
     }
 }
