 /* Topic.java - created on Mar 18, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.subject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.theeuropeanlibrary.model.common.FieldId;
 import org.theeuropeanlibrary.model.common.Identifier;
 import org.theeuropeanlibrary.model.common.spatial.SpatialEntity;
 import org.theeuropeanlibrary.model.common.time.Temporal;
 
 /**
  * The topic class holding information about the actual topic name and an optional description.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @author Nuno Freire <nfreire@gmail.com>
  * @since Mar 18, 2011
  */
 public class Topic extends Subject {
     /**
      * actual name of the topic
      */
     @FieldId(5)
     private String             topicName;
     /**
      * description of the topic
      */
     @FieldId(6)
     private String             topicDescription;
 
     /**
      * Topical term following the name entry element
      */
     @FieldId(7)
     private String             secondTopicTerm;
     /**
      * Location of event
      */
     @FieldId(8)
     private SpatialEntity      locationOfEvent;
     /**
      * Time period during which an event occurred
      */
     @FieldId(9)
     private Temporal           activeDates;
 
 
     /** Edition/version of the kos system */
     @FieldId(10)
     private String             edition;
     
     /**
      * Identifiers of the topic in external data sets
      */
     @FieldId(11)
     protected List<Identifier> identifiers;
 
     /**
      * Creates a new instance of this class.
      */
     public Topic() {
         super();
         this.identifiers = new ArrayList<Identifier>(0);
     }
 
 
     /**
      * Creates a new instance of this class.
      * 
      * @param topicName
      *            actual name of the topic
      */
     public Topic(String topicName) {
         this(topicName, null, null);
     }
 
     /**
      * Creates a new instance of this class.
      * 
      * @param topicName
      *            actual name of the topic
      * @param topicDescription
      *            description of the topic
      * @param identifiers
      *            Identifiers of the topic in external data sets
      */
     public Topic(String topicName, String topicDescription, List<Identifier> identifiers) {
         if (topicName == null) { throw new IllegalArgumentException(
                 "Argument 'topicName' should not be null!"); }
         this.topicName = topicName;
         this.topicDescription = topicDescription;
         if (identifiers == null) {
             this.identifiers = new ArrayList<Identifier>(0);
         } else {
             this.identifiers = identifiers;
         }
     }
 
     /**
      * @return actual name of the topic
      */
     public String getTopicName() {
         return topicName;
     }
 
     /**
      * @return description of the topic
      */
     public String getTopicDescription() {
         return topicDescription;
     }
 
     /**
      * @param topicName
      *            actual name of the topic
      */
     public void setTopicName(String topicName) {
         this.topicName = topicName;
     }
 
     /**
      * @param topicDescription
      *            description of the topic
      */
     public void setTopicDescription(String topicDescription) {
         this.topicDescription = topicDescription;
     }
 
     /**
      * @return Topical term following the name entry element
      */
     public String getSecondTopicTerm() {
         return secondTopicTerm;
     }
 
     /**
      * @param secondTopicTerm
      *            Topical term following the name entry element
      */
     public void setSecondTopicTerm(String secondTopicTerm) {
         this.secondTopicTerm = secondTopicTerm;
     }
 
     /**
      * @return Location of event
      */
     public SpatialEntity getLocationOfEvent() {
         return locationOfEvent;
     }
 
     /**
      * @param locationOfEvent
      *            Location of event
      */
     public void setLocationOfEvent(SpatialEntity locationOfEvent) {
         this.locationOfEvent = locationOfEvent;
     }
 
     /**
      * @return Time period during which an event occurred
      */
     public Temporal getActiveDates() {
         return activeDates;
     }
 
     /**
      * @param activeDates
      *            Time period during which an event occurred
      */
     public void setActiveDates(Temporal activeDates) {
         this.activeDates = activeDates;
     }
 
     
 
     /**
      * Sets the edition
      * @param edition the edition to set
      */
     public void setEdition(String edition) {
         this.edition = edition;
     }
 
 
     /**
      * Returns the edition.
      * @return the edition
      */
     public String getEdition() {
         return edition;
     }
     
     /**
      * @return identifiers of topics like TEL, MACS, LCSH, ...
      */
     public List<Identifier> getIdentifiers() {
         return identifiers;
     }
 
     /**
      * @param identifiers
      *            identifiers of topics like TEL, MACS, LCSH, ...
      */
     public void setIdentifiers(List<Identifier> identifiers) {
         this.identifiers = identifiers;
     }
 
     
 
 
     /**
      * @return displayable representation of the topic object
      */
     public String display() {
         StringBuilder builder = new StringBuilder();
         builder.append(topicName);
 
        if (secondTopicTerm != null) {
            builder.append(" secondTopicTerm=" + secondTopicTerm);
        }
        if (locationOfEvent != null) {
            builder.append(" locationOfEvent=" + locationOfEvent);
        }
        if (activeDates != null) {
            builder.append(" activeDates=" + activeDates);
        }
         if (formSubdivision != null) {
            builder.append(" formSubdivision=" + formSubdivision);
         }
         if (generalSubdivision != null) {
            builder.append(" generalSubdivision=" + generalSubdivision);
         }
         if (chronologicalSubdivision != null) {
            builder.append(" chronologicalSubdivision=" + chronologicalSubdivision);
         }
         if (geographicSubdivision != null) {
            builder.append(" geographicSubdivision=" + geographicSubdivision);
         }
 
         return builder.toString();
     }
 
 
     
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((activeDates == null) ? 0 : activeDates.hashCode());
         result = prime * result + ((edition == null) ? 0 : edition.hashCode());
         result = prime * result + ((identifiers == null) ? 0 : identifiers.hashCode());
         result = prime * result + ((locationOfEvent == null) ? 0 : locationOfEvent.hashCode());
         result = prime * result + ((secondTopicTerm == null) ? 0 : secondTopicTerm.hashCode());
         result = prime * result + ((topicDescription == null) ? 0 : topicDescription.hashCode());
         result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
         return result;
     }
 
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!super.equals(obj)) return false;
         if (getClass() != obj.getClass()) return false;
         Topic other = (Topic)obj;
         if (activeDates == null) {
             if (other.activeDates != null) return false;
         } else if (!activeDates.equals(other.activeDates)) return false;
         if (edition == null) {
             if (other.edition != null) return false;
         } else if (!edition.equals(other.edition)) return false;
         if (identifiers == null) {
             if (other.identifiers != null) return false;
         } else if (!identifiers.equals(other.identifiers)) return false;
         if (locationOfEvent == null) {
             if (other.locationOfEvent != null) return false;
         } else if (!locationOfEvent.equals(other.locationOfEvent)) return false;
         if (secondTopicTerm == null) {
             if (other.secondTopicTerm != null) return false;
         } else if (!secondTopicTerm.equals(other.secondTopicTerm)) return false;
         if (topicDescription == null) {
             if (other.topicDescription != null) return false;
         } else if (!topicDescription.equals(other.topicDescription)) return false;
         if (topicName == null) {
             if (other.topicName != null) return false;
         } else if (!topicName.equals(other.topicName)) return false;
         return true;
     }
 
 
     @Override
     public String toString() {
         return "Topic [topicName=" + topicName + ", topicDescription=" + topicDescription +
                ", secondTopicTerm=" + secondTopicTerm + ", locationOfEvent=" + locationOfEvent +
                ", activeDates=" + activeDates + ", edition=" + edition + ", identifiers=" +
                identifiers + ", formSubdivision=" + formSubdivision + ", generalSubdivision=" +
                generalSubdivision + ", chronologicalSubdivision=" + chronologicalSubdivision +
                ", geographicSubdivision=" + geographicSubdivision + "]";
     }
 
 
 
 
     
 
 
 }
