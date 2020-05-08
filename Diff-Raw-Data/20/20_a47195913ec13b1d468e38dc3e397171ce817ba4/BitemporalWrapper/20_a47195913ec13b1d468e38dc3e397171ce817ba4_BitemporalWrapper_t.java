 /*
  * (c) Copyright Ervacon 2007.
  * All Rights Reserved.
  */
 
 package com.anasoft.os.daofusion.bitemporal;
 
 import javax.persistence.Column;
 import javax.persistence.MappedSuperclass;
 
 import org.hibernate.annotations.Columns;
 import org.hibernate.annotations.Type;
 import org.joda.time.Interval;
 
 import com.anasoft.os.daofusion.entity.MutablePersistentEntity;
 
 /**
  * Decorates a value with bitemporal information, making it possible to
  * bitemporally track the value in a {@link BitemporalTrace}. A {@link BitemporalWrapper}
  * allows you to bitemporally track existing value classes, for instance strings. This
  * class implements the {@link Bitemporal} interface for use with JPA / Hibernate.
  * 
  * <p>
  * 
  * Due to the nature of bitemporality, the wrapped value should be immutable.
  * The value itself will never change, instead new values will be added to the
  * {@link BitemporalTrace} to represent changes of the value. A {@link BitemporalWrapper}
  * itself is not immutable, its record interval can be {@link #end() ended}.
  * 
  * <p>
  * 
  * Instances of this class are serializable if the wrapped value is serializable.
  * 
  * <p>
  * 
  * Objects of this class are not thread-safe.
  * 
  * @param <V> Value tracked by {@link BitemporalTrace}.
  * 
  * @see Bitemporal
  * @see BitemporalTrace
  * 
  * @author Erwin Vervaet
  * @author Christophe Vanfleteren
  * @author igor.mihalik
  */
 @MappedSuperclass
 public abstract class BitemporalWrapper<V> extends MutablePersistentEntity implements Bitemporal {
 
     public static final String _VALID_FROM = "validityInterval.start";
     public static final String _VALID_TO = "validityInterval.end";
 
     public static final String _RECORD_FROM = "recordInterval.start";
     public static final String _RECORD_TO = "recordInterval.end";
 
     @Columns(columns = { @Column(name = "validFrom"), @Column(name = "validTo") })
     @Type(type = PersistentInterval.TYPE)
     private Interval validityInterval;
 
     @Columns(columns = { @Column(name = "recordFrom"), @Column(name = "recordTo") })
     @Type(type = PersistentInterval.TYPE)
     private Interval recordInterval;
 
     protected BitemporalWrapper() {
         // default constructor required by Hibernate
     }
 
     /**
      * Bitemporally wrap the given value. Validity will be as specified, and the
      * recording interval will be {@link TimeUtils#fromNow() from now on}.
      * 
      * @param value The value to wrap (can be <tt>null</tt>).
      * @param validityInterval Validity of the value.
      */
     public BitemporalWrapper(V value, Interval validityInterval) {
         if (validityInterval == null) {
             throw new IllegalArgumentException("The validity interval is required");
         }
         this.validityInterval = validityInterval;
         this.recordInterval = TimeUtils.fromNow();
         setValue(value);
     }
 
     /**
      * Set the wrapped value, possibly <tt>null</tt>.
      */
     protected abstract void setValue(V value);
 
     /**
      * Returns the wrapped value, possibly <tt>null</tt>.
      */
     public abstract V getValue();
 
     /**
      * @see com.anasoft.os.daofusion.bitemporal.Bitemporal#getValidityInterval()
      */
     public Interval getValidityInterval() {
         return validityInterval;
     }
     
     /**
      * Set the validity interval for the wrapped value.
      */
     protected void setValidityInterval(Interval validityInterval) {
         this.validityInterval = validityInterval;
     }
    
    /**
    * Set the validity interval for the wrapped value.
    */
   protected void setRecordInterval(Interval recordInterval) {
       this.recordInterval = recordInterval;
   }
 
     /**
      * @see com.anasoft.os.daofusion.bitemporal.Bitemporal#getRecordInterval()
      */
     public Interval getRecordInterval() {
         return recordInterval;
     }
 
     /**
      * @see com.anasoft.os.daofusion.bitemporal.Bitemporal#end()
      */
     public void end() {
         this.recordInterval = TimeUtils.interval(getRecordInterval().getStart(), TimeUtils.now());
     }
 
     @Override
     public String toString() {
         return getValidityInterval() + "  ~  " + getRecordInterval() + "  ~  "
                 + String.valueOf(getValue());
     }
 
 }
