 package org.drools.planner.examples.ras2012.model;
 
 public class ScheduleAdherenceRequirement {
 
     private final Node destination;
 
     private final int  timeSinceStartOfWorld;
 
     public ScheduleAdherenceRequirement(final Node where, final int when) {
         if (where == null) {
             throw new IllegalArgumentException("Node cannot be null.");
         }
         this.destination = where;
         this.timeSinceStartOfWorld = when;
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
         final ScheduleAdherenceRequirement other = (ScheduleAdherenceRequirement) obj;
        if (!this.destination.equals(other.destination)) {
             return false;
         }
         if (this.timeSinceStartOfWorld != other.timeSinceStartOfWorld) {
             return false;
         }
         return true;
     }
 
     public Node getDestination() {
         return this.destination;
     }
 
     public int getTimeSinceStartOfWorld() {
         return this.timeSinceStartOfWorld;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.destination == null ? 0 : this.destination.hashCode());
         result = prime * result + this.timeSinceStartOfWorld;
         return result;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("ScheduleAdherenceRequirement [destination=").append(this.destination)
                 .append(", timeSinceStartOfWorld=").append(this.timeSinceStartOfWorld).append("]");
         return builder.toString();
     }
 
 }
