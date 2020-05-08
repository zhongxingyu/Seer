 package org.drools.planner.examples.ras2012.model;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.List;
 
 import org.drools.planner.examples.ras2012.model.Arc.TrackType;
 
 public class Train implements Comparable<Train> {
 
     public static enum TrainType {
         A(600), B(500), C(400), D(300), E(150, false), F(100, false);
 
         private final boolean adhereToSchedule;
         private final int     delayPenalty;
 
         TrainType(final int delayPenalty) {
             this(delayPenalty, true);
         }
 
         TrainType(final int delayPenalty, final boolean adhereToSchedule) {
             this.adhereToSchedule = adhereToSchedule;
             this.delayPenalty = delayPenalty;
         }
 
         public boolean adhereToSchedule() {
             return this.adhereToSchedule;
         }
 
         public int getDelayPenalty() {
             return this.delayPenalty;
         }
     }
 
     private final String                             name;
 
     private final BigDecimal                         length;
 
     private final BigDecimal                         speedMultiplier;
 
     private final int                                tob;
     private final Node                               origin;
     private final Node                               destination;
     private final int                                entryTime;
     private final int                                wantTime;
     private final int                                originalDelay;
     private final List<ScheduleAdherenceRequirement> scheduleAdherenceRequirements;
     private final boolean                            carriesHazardousMaterials;
     private final boolean                            isWestbound;
 
     public Train(final String name, final BigDecimal length, final BigDecimal speedMultiplier,
             final int tob, final Node origin, final Node destination, final int entryTime,
             final int wantTime, final int originalScheduleAdherence,
             final List<ScheduleAdherenceRequirement> sars, final boolean hazmat,
             final boolean isWestbound) {
         // FIXME validate train name of [A-F][num]
         if (name == null || name.trim().length() == 0) {
             throw new IllegalArgumentException("Train name must be a non-empty String.");
         }
         if (length == null || length.compareTo(BigDecimal.ZERO) != 1) {
             throw new IllegalArgumentException("Train must have a length greater than 0.");
         }
         if (speedMultiplier == null || speedMultiplier.compareTo(BigDecimal.ZERO) != 1) {
             throw new IllegalArgumentException("Train must have a speed multiplier greater than 0.");
         }
         if (tob < 1) {
             throw new IllegalArgumentException("Train must have TOB > 0.");
         }
         if (origin == null || destination == null || origin.equals(destination)) {
             throw new IllegalArgumentException(
                     "Train origin and destination must be two different valid nodes.");
         }
         if (entryTime < 0) {
             throw new IllegalArgumentException("Train entry time may not be negative.");
         }
         if (wantTime < 0) {
             throw new IllegalArgumentException("Train want time may not be negative.");
         }
         this.name = name;
         this.length = length;
         this.speedMultiplier = speedMultiplier;
         this.tob = tob;
         this.origin = origin;
         this.destination = destination;
         this.entryTime = entryTime;
         this.wantTime = wantTime;
         this.originalDelay = originalScheduleAdherence;
         this.scheduleAdherenceRequirements = sars == null ? Collections
                 .<ScheduleAdherenceRequirement> emptyList() : Collections.unmodifiableList(sars);
         this.carriesHazardousMaterials = hazmat;
         this.isWestbound = isWestbound;
     }
 
     public boolean carriesHazardousMaterials() {
         return this.carriesHazardousMaterials;
     }
 
     /**
      * Trains are compared by their names as strings.
      * 
      * @return String.compareTo(String)
      */
     @Override
     public int compareTo(final Train arg0) {
         return this.getName().compareTo(arg0.getName());
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (this.getClass() != obj.getClass()) {
             return false;
         }
         final Train other = (Train) obj;
         if (this.name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     public Node getDestination() {
         return this.destination;
     }
 
     public int getEntryTime() {
         return this.entryTime;
     }
 
     public BigDecimal getLength() {
         return this.length;
     }
 
     public Integer getMaximumSpeed(final TrackType t) {
         final int coreSpeed = this.isWestbound() ? t.getSpeedWestbound() : t.getSpeedEastbound();
         if (t.isMainTrack()) {
             return this.speedMultiplier.multiply(new BigDecimal(coreSpeed)).intValue();
         } else {
             return coreSpeed;
         }
     }
 
     public String getName() {
         return this.name;
     }
 
     public Node getOrigin() {
         return this.origin;
     }
 
     public int getOriginalDelay() {
         return this.originalDelay;
     }
 
     public List<ScheduleAdherenceRequirement> getScheduleAdherenceRequirements() {
         return this.scheduleAdherenceRequirements;
     }
 
     public TrainType getType() {
        final String firstCharOfName = this.name.substring(0, 1);
        switch (firstCharOfName) {
            case "A":
                 return TrainType.A;
            case "B":
                 return TrainType.B;
            case "C":
                 return TrainType.C;
            case "D":
                 return TrainType.D;
            case "E":
                 return TrainType.E;
            case "F":
                 return TrainType.F;
             default:
                throw new IllegalArgumentException("Invalid train type: " + firstCharOfName);
         }
     }
 
     public int getWantTime() {
         return this.wantTime;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.name == null ? 0 : this.name.hashCode());
         return result;
     }
 
     public boolean isEastbound() {
         return !this.isWestbound();
     }
 
     public boolean isHeavy() {
         return this.tob > 100;
     }
 
     public boolean isWestbound() {
         return this.isWestbound;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("Train [name=").append(this.name).append(", length=").append(this.length)
                 .append(", origin=").append(this.origin).append(", destination=")
                 .append(this.destination).append(", entryTime=").append(this.entryTime)
                 .append(", wantTime=").append(this.wantTime).append(", originalDelay=")
                 .append(this.originalDelay).append(", scheduleAdherenceRequirements=")
                 .append(this.scheduleAdherenceRequirements).append(", carriesHazardousMaterials=")
                 .append(this.carriesHazardousMaterials).append(", isWestbound=")
                 .append(this.isWestbound).append(", getType()=").append(this.getType())
                 .append(", isHeavy()=").append(this.isHeavy()).append("]");
         return builder.toString();
     }
 
 }
