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
  * Enumerated type of a category.
  *
  * Instances of this class are immutable.
  *
  * The following categorys are defined:
  * <ul>
  *    <li>Category.AUDIT = 'Audit'</li>
  *    <li>Category.BUSINESS = 'Business'</li>
  *    <li>Category.FLOW = 'Flow'</li>
  *    <li>Category.SECURITY = 'Security'</li>
  *    <li>Category.TECHNICAL = 'Technical'</li>
  *    <li>Category.UNDEFINED = 'Undefined'</li>
  * </ul>
  *
  * 
  * The values of this enum have a internal
  * sequential integer representation starting with '0'.
  *
  * @author generated
  */
 public final class Category
         implements Serializable
 {
    /**
     * The name of this type.
     */
    public static final String TYPE_NAME = "Category";
 
    /** Ordinal of next category to be created. */
    private static int sNextOrdinal = 0;
 
    /** Maps a string representation to an enumerated value. */
   private static final Map<String, Category>
      FROM_STRING = new HashMap<String, Category>();
 
    /** The Category Audit. */
    public static final Category AUDIT
       = new Category("Audit");
 
    /** The Category Business. */
    public static final Category BUSINESS
       = new Category("Business");
 
    /** The Category Flow. */
    public static final Category FLOW
       = new Category("Flow");
 
    /** The Category Security. */
    public static final Category SECURITY
       = new Category("Security");
 
    /** The Category Technical. */
    public static final Category TECHNICAL
       = new Category("Technical");
 
    /** The Category Undefined. */
    public static final Category UNDEFINED
       = new Category("Undefined");
 
 
    /** The serialVersionUID used for serialization. */
    static final long serialVersionUID = 1;
 
    /** Internal list of all available Categorys */
    private static final Category[] PRIVATE_VALUES
          =
             {
                Category.AUDIT,
                Category.BUSINESS,
                Category.FLOW,
                Category.SECURITY,
                Category.TECHNICAL,
                Category.UNDEFINED
             };
 
    /** Immutable list of the Categorys. */
   public static final List<Category> VALUES
          = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
 
    /**
     * Immutable map using the name string as key holding the
     * Categorys as values.
     */
   public static final Map<String, Category> VALUE_MAP
          = Collections.unmodifiableMap(FROM_STRING);
 
    /** Assign a ordinal to this category */
    private final int mOrdinal = sNextOrdinal++;
 
    /** The name of the category */
    private final transient String mName;
 
    /** Private Constructor */
    private Category (String name)
    {
       mName = name;
       FROM_STRING.put(mName, this);
    }
 
 
    /**
     * Creates a Category object from its int representation.
     *
     * @param i the integer representation of the category.
     * @return the Category object represented by this int.
     * @throws ArgumentMalformedException If the assigned int value isn't
     *       listed in the internal category table.
     */
    public static Category fromInt (int i)
          throws ArgumentMalformedException
    {
       try
       {
          return PRIVATE_VALUES[i];
       }
       catch (ArrayIndexOutOfBoundsException e)
       {
          throw new ArgumentMalformedException(
                "Category",
                "" + i,
                "Illegal int representation of Category.");
       }
    }
 
 
    /**
     * Creates a Category object from its String representation.
     *
     * @param str the string representation of the
     *       category.
     * @return the Category object represented by this str.
     * @throws ArgumentMalformedException If the given str value isn't
     *       listed in the internal category table.
     */
    public static Category fromString (String str)
          throws ArgumentMalformedException
    {
       final Category result
             = (Category) FROM_STRING.get(str);
       if (result == null)
       {
          throw new ArgumentMalformedException(
                "Category",
                str,
                "Illegal string representation of Category, only "
                   + VALUES + " are allowed.");
       }
       return result;
    }
 
    /**
     * Returns the int representation of this category.
     *
     * @return the int representation of this category.
     */
    public int toInt ()
    {
         return mOrdinal;
    }
 
    /**
     * Returns the String representation of this category.
     *
     * @return the String representation of this category.
     */
    public String toString ()
    {
       return mName;
    }
 
    /**
     * Resolves instances being deserialized to a single instance
     * per category.
     */
    private Object readResolve ()
    {
       return PRIVATE_VALUES[mOrdinal];
    }
 }
