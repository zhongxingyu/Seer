 package de.skuzzle.polly.sdk.constraints;
 
 /**
  * Provides constant instances for default constraints like integer, double, boolean
  * and mail addresses.
  * 
  * @author Simon
  * @since 0.9
  */
 public interface Constraints {
 
     /**
      * Constraints a value to <code>true</code> or <code>false</code>
      * @see BooleanConstraint
      */
     public final static AttributeConstraint BOOLEAN = new BooleanConstraint();
     
     /**
      * Constraints a value to a double.
     * @see {@link DoubleConstraint}
      */
     public final static AttributeConstraint DOUBLE = new DoubleConstraint();
     
     /**
      * Constraints a value to an integer.
      * @see IntegerConstraint
      */
     public final static AttributeConstraint INTEGER = new IntegerConstraint();
     
     /**
      * Constraints a value to a valid email address.
      * @see MailAddressConstraint
      */
     public final static AttributeConstraint MAILADDRESS = new MailAddressConstraint();
 }
