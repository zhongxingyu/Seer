 package org.drools.planner.examples.ras2012.model;
 
 import java.math.BigDecimal;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Arc extends Section {
 
     private static final AtomicInteger idGenerator = new AtomicInteger();
 
     private final int                  id          = Arc.idGenerator.incrementAndGet();
 
     private final Track                track;
 
     private final BigDecimal           lengthInMiles;
 
     private final String               asString;
 
     public Arc(final Track t, final BigDecimal lengthInMiles, final Node westNode,
             final Node eastNode) {
         super(westNode, eastNode);
         if (t == null || lengthInMiles == null) {
             throw new IllegalArgumentException("Neither of the arguments can be null.");
         }
         if (lengthInMiles.signum() <= 0) {
             throw new IllegalArgumentException("Arc length must be greater than zero.");
         }
         this.track = t;
         this.lengthInMiles = lengthInMiles;
         this.asString = this.toStringInternal();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Arc)) {
             return false;
         }
         final Arc other = (Arc) obj;
         return this.id == other.id;
     }
 
    public int getId() {
        return this.id;
    }

     public BigDecimal getLength() {
         return this.lengthInMiles;
     }
 
     public Track getTrack() {
         return this.track;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + this.id;
         return result;
     }
 
     @Override
     public String toString() {
         return this.asString;
     }
 
     private String toStringInternal() {
         final StringBuilder builder = new StringBuilder();
         builder.append("Arc (").append(this.track.getSymbol()).append(") [lengthInMiles=")
                 .append(this.lengthInMiles).append(", section=").append(super.toString())
                 .append("]");
         return builder.toString();
     }
 }
