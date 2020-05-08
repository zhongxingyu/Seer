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
 
     private static TrainType determineType(final String name) {
         final char[] chars = name.toCharArray();
         switch (chars[0]) {
             case 'A':
                 return TrainType.A;
             case 'B':
                 return TrainType.B;
             case 'C':
                 return TrainType.C;
             case 'D':
                 return TrainType.D;
             case 'E':
                 return TrainType.E;
             case 'F':
                 return TrainType.F;
             default:
                 throw new IllegalArgumentException("Invalid train type: " + chars[0]);
         }
     }
 
     private final String                             name;
 
     private final BigDecimal                         length;
     private final BigDecimal                         speedMultiplier;
     private final TrainType                          type;
     private final int                                tob;
     private final Node                               origin;
     private final Node                               destination;
    private final long                               entryTime;
     private final int                                wantTime;
     private final int                                originalDelay;
     private final List<ScheduleAdherenceRequirement> scheduleAdherenceRequirements;
     private final boolean                            carriesHazardousMaterials;
 
     private final boolean                            isWestbound;
 
     public Train(final String name, final BigDecimal length, final BigDecimal speedMultiplier,
            final int tob, final Node origin, final Node destination, final long entryTime,
             final int wantTime, final int originalScheduleAdherence,
             final List<ScheduleAdherenceRequirement> sars, final boolean hazmat,
             final boolean isWestbound) {
         if (name == null) {
             throw new IllegalArgumentException("Train name must be a non-empty String.");
         }
         this.type = Train.determineType(name);
         try {
             if (Integer.valueOf(name.substring(1)) < 0) {
                 throw new NumberFormatException("Negative numbers not allowed!");
             }
         } catch (final NumberFormatException ex) {
             throw new IllegalArgumentException("Train names must be in format of [A-F][0-9]+: "
                     + name);
         }
         this.name = name;
         if (length == null || length.compareTo(BigDecimal.ZERO) != 1) {
             throw new IllegalArgumentException("Train must have a length greater than 0.");
         }
         this.length = length;
         if (speedMultiplier == null || speedMultiplier.compareTo(BigDecimal.ZERO) != 1) {
             throw new IllegalArgumentException("Train must have a speed multiplier greater than 0.");
         }
         this.speedMultiplier = speedMultiplier;
         if (tob < 1) {
             throw new IllegalArgumentException("Train must have TOB > 0.");
         }
         this.tob = tob;
         if (origin == null || destination == null || origin.equals(destination)) {
             throw new IllegalArgumentException(
                     "Train origin and destination must be two different valid nodes.");
         }
         this.origin = origin;
         this.destination = destination;
         if (entryTime < 0) {
             throw new IllegalArgumentException("Train entry time may not be negative.");
         }
         this.entryTime = entryTime;
         if (wantTime < 0) {
             throw new IllegalArgumentException("Train want time may not be negative.");
         }
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
      * Trains are compared by their first letter as strings, then by their number as integers. For example, A2 < B1 and A2 <
      * A11.
      * 
      * @return String.compareTo(String)
      */
     @Override
     public int compareTo(final Train arg0) {
         if (this.getName().charAt(0) == arg0.getName().charAt(0)) {
             final Integer thisId = Integer.valueOf(this.getName().substring(1));
             final Integer otherId = Integer.valueOf(arg0.getName().substring(1));
             return thisId.compareTo(otherId);
         } else {
             return this.getName().compareTo(arg0.getName());
         }
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
         return this.name.equals(other.name);
     }
 
     public long getArcTravellingTimeInMilliseconds(final Arc a) {
         final BigDecimal milesPerHour = BigDecimal.valueOf(this.getMaximumSpeed(a.getTrackType()));
         final BigDecimal hours = a.getLengthInMiles().divide(milesPerHour, 10,
                 BigDecimal.ROUND_HALF_DOWN);
         final BigDecimal sixty = BigDecimal.valueOf(60);
         return hours.multiply(sixty).multiply(sixty).multiply(BigDecimal.valueOf(1000)).longValue();
     }
 
     public Node getDestination() {
         return this.destination;
     }
 
    public long getEntryTime() {
         return this.entryTime;
     }
 
     public BigDecimal getLength() {
         return this.length;
     }
 
     public Integer getMaximumSpeed() {
         return this.getMaximumSpeed(TrackType.MAIN_0);
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
 
     protected BigDecimal getSpeedMultiplier() {
         return this.speedMultiplier;
     }
 
     public TrainType getType() {
         return this.type;
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
