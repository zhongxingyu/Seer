 package gov.nih.nci.iso21090;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * Represents iso data type IVL.
  * @author lpower
  * @param <T>
  */
 public final class Ivl<T extends Qty> extends QSet<T> implements Cloneable {
 
     private static final long serialVersionUID = 1L;
 
     private T any;
     private T high;
     private Boolean highClosed;
     private T low;
     private Boolean lowClosed;
     private Qty width;
 
     /**
      * @return the any
      */
     public T getAny() {
         return any;
     }
 
     /**
      * @param any the any to set
      */
     public void setAny(T any) {
         this.any = any;
     }
 
     /**
      * @return the high
      */
     public T getHigh() {
         return high;
     }
 
     /**
      * @param high the high to set
      */
     public void setHigh(T high) {
         this.high = high;
     }
 
     /**
      * @return the highClosed
      */
     public Boolean getHighClosed() {
         return highClosed;
     }
 
     /**
      * @param highClosed the highClosed to set
      */
     public void setHighClosed(Boolean highClosed) {
         this.highClosed = highClosed;
     }
 
     /**
      * @return the low
      */
     public T getLow() {
         return low;
     }
 
     /**
      * @param low the low to set
      */
     public void setLow(T low) {
         this.low = low;
     }
 
     /**
      * @return the lowClosed
      */
     public Boolean getLowClosed() {
         return lowClosed;
     }
 
     /**
      * @param lowClosed the lowClosed to set
      */
     public void setLowClosed(Boolean lowClosed) {
         this.lowClosed = lowClosed;
     }
 
     /**
      * @return the width
      */
     public Qty getWidth() {
         return width;
     }
 
     /**
      * @param width the width to set
      */
     public void setWidth(Qty width) {
         this.width = width;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object o) {
         if (o == null) {
             return false;
         }
 
         if (this == o) {
             return true;
         }
 
         if (!(o instanceof Ivl<?>)) {
             return false;
         }
 
         Ivl<?> x = (Ivl<?>) o;
 
         return new EqualsBuilder()
             .appendSuper(super.equals(o))
             .append(this.getHigh(), x.getHigh())
             .append(this.getAny(), x.getAny())
             .append(this.getHighClosed(), x.getHighClosed())
             .append(this.getLow(), x.getLow())
             .append(this.getLowClosed(), x.getLowClosed())
             .append(this.getWidth(), x.getWidth())
             .isEquals();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
 
         return new HashCodeBuilder(HASH_CODE_SEED_1, HASH_CODE_SEED_2)
             .append(this.getHigh())
             .append(this.getAny())
             .append(this.getHighClosed())
             .append(this.getLow())
             .append(this.getLowClosed())
             .append(this.getWidth())
             .toHashCode();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Ivl<T> clone() {
         return (Ivl<T>) super.clone();
     }
 
     /**
      * Returns true if high is not null and low is null.
      * @return boolean
      */
     public boolean isLowMissing() {
         return (this.getHigh() != null && this.getLow() == null) 
            || ((this.getHigh() != null && this.getHigh().getNullFlavor() == null) 
            && (this.getLow() != null && this.getLow().getNullFlavor() != null));
     }
 
     /**
      * Returns true if high is null and low is not null.
      * @return boolean
      */
     public boolean isHighMissing() {
         return (this.getLow() != null && this.getHigh() == null) 
            || ((this.getLow() != null && this.getLow().getNullFlavor() == null) 
            && (this.getHigh() != null && this.getHigh().getNullFlavor() != null));
     }
 
     /**
      * Return true if both high and low are not null and equal to each other.
      * @return boolean
      */
     public boolean isHighEqualLow() {
         return this.getHigh() != null && this.getLow() != null
         && this.getHigh().equals(this.getLow());
     }
 
 
 }
