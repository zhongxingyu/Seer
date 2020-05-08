 /*
  * Generated source file, not in CVS/SVN repository
  */
 package org.jcoderz.commons;
 
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 
 
 import org.jcoderz.commons.ArgumentMalformedException;
 
 
 /**
  * Enumerated type of a business impact.
  *
  * Instances of this class are immutable.
  *
  * The following business impacts are defined:
  * <ul>
  *    <li>BusinessImpact.CRITICAL = 'Critical'</li>
  *    <li>BusinessImpact.MAJOR = 'Major'</li>
  *    <li>BusinessImpact.MIDDLE = 'Middle'</li>
  *    <li>BusinessImpact.MINOR = 'Minor'</li>
  *    <li>BusinessImpact.NONE = 'None'</li>
  *    <li>BusinessImpact.UNDEFINED = 'Undefined'</li>
  * </ul>
  *
  * 
  * The values of this enum have a internal
  * sequential integer representation starting with '0'.
  *
  * @author generated
  */
 public final class BusinessImpact
         implements Serializable
 {
    /**
     * The name of this type.
     */
    public static final String TYPE_NAME = "BusinessImpact";
 
    /** Ordinal of next business impact to be created. */
    private static int sNextOrdinal = 0;
 
    /** Maps a string representation to an enumerated value. */
   private static final Map<String, BusinessImpact>
      FROM_STRING = new HashMap<String, BusinessImpact>();
 
    /** The BusinessImpact Critical. */
    public static final BusinessImpact CRITICAL
       = new BusinessImpact("Critical");
 
    /** The BusinessImpact Major. */
    public static final BusinessImpact MAJOR
       = new BusinessImpact("Major");
 
    /** The BusinessImpact Middle. */
    public static final BusinessImpact MIDDLE
       = new BusinessImpact("Middle");
 
    /** The BusinessImpact Minor. */
    public static final BusinessImpact MINOR
       = new BusinessImpact("Minor");
 
    /** The BusinessImpact None. */
    public static final BusinessImpact NONE
       = new BusinessImpact("None");
 
    /** The BusinessImpact Undefined. */
    public static final BusinessImpact UNDEFINED
       = new BusinessImpact("Undefined");
 
 
    /** The serialVersionUID used for serialization. */
    static final long serialVersionUID = 1;
 
    /** Internal list of all available BusinessImpacts */
    private static final BusinessImpact[] PRIVATE_VALUES
          =
             {
                BusinessImpact.CRITICAL,
                BusinessImpact.MAJOR,
                BusinessImpact.MIDDLE,
                BusinessImpact.MINOR,
                BusinessImpact.NONE,
                BusinessImpact.UNDEFINED
             };
 
    /** Immutable list of the BusinessImpacts. */
   public static final List<BusinessImpact> VALUES
          = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
 
    /**
     * Immutable map using the name string as key holding the
     * BusinessImpacts as values.
     */
   public static final Map<String, BusinessImpact> VALUE_MAP
          = Collections.unmodifiableMap(FROM_STRING);
 
    /** Assign a ordinal to this business impact */
    private final int mOrdinal = sNextOrdinal++;
 
    /** The name of the business impact */
    private final transient String mName;
 
    /** Private Constructor */
    private BusinessImpact (String name)
    {
       mName = name;
       FROM_STRING.put(mName, this);
    }
 
 
    /**
     * Creates a BusinessImpact object from its int representation.
     *
     * @param i the integer representation of the business impact.
     * @return the BusinessImpact object represented by this int.
     * @throws ArgumentMalformedException If the assigned int value isn't
     *       listed in the internal business impact table.
     */
    public static BusinessImpact fromInt (int i)
          throws ArgumentMalformedException
    {
       try
       {
          return PRIVATE_VALUES[i];
       }
       catch (ArrayIndexOutOfBoundsException e)
       {
          throw new ArgumentMalformedException(
                "BusinessImpact",
                "" + i,
                "Illegal int representation of BusinessImpact.");
       }
    }
 
 
    /**
     * Creates a BusinessImpact object from its String representation.
     *
     * @param str the string representation of the
     *       business impact.
     * @return the BusinessImpact object represented by this str.
     * @throws ArgumentMalformedException If the given str value isn't
     *       listed in the internal business impact table.
     */
    public static BusinessImpact fromString (String str)
          throws ArgumentMalformedException
    {
       final BusinessImpact result
             = (BusinessImpact) FROM_STRING.get(str);
       if (result == null)
       {
          throw new ArgumentMalformedException(
                "BusinessImpact",
                str,
                "Illegal string representation of BusinessImpact, only "
                   + VALUES + " are allowed.");
       }
       return result;
    }
 
    /**
     * Returns the int representation of this business impact.
     *
     * @return the int representation of this business impact.
     */
    public int toInt ()
    {
         return mOrdinal;
    }
 
    /**
     * Returns the String representation of this business impact.
     *
     * @return the String representation of this business impact.
     */
    public String toString ()
    {
       return mName;
    }
 
    /**
     * Resolves instances being deserialized to a single instance
     * per business impact.
     */
    private Object readResolve ()
    {
       return PRIVATE_VALUES[mOrdinal];
    }
 }
