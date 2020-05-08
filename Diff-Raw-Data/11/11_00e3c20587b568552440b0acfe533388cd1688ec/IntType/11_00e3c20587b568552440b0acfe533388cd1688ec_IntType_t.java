 /*
  * Created on Dec 31, 2004 at the Interface Ecology Lab.
  */
 package ecologylab.types;
 
 import java.lang.reflect.Field;
 
 /**
  * Type system entry for int, a built-in primitive.
  * 
  * @author andruid
  */
 public class IntType extends Type 
 {
 /**
  * This constructor should only be called once per session, through
  * a static initializer, typically in TypeRegistry.
  * <p>
  * To get the instance of this type object for use in translations, call
  * <code>TypeRegistry.get("int")</code>.
  * 
  */
 	protected IntType()
 	{
 		super("int", true);
 	}
 
 	/**
 	 * Convert the parameter to int.
 	 */
 	public int getValue(String valueString)
 	{
 		return Integer.parseInt(valueString);
 	}
 	
 	/**
 	 * This is a primitive type, so we set it specially.
 	 * 
 	 * @see ecologylab.types.Type#setField(java.lang.reflect.Field, java.lang.String)
 	 */
 	public boolean setField(Object object, Field field, String value) 
 	{
 		boolean result	= false;
 		int converted	= Integer.MIN_VALUE;
 		try
 		{
 		   converted	= getValue(value);
 		   field.setInt(object, converted);
 		   result		= true;
 		} catch (Exception e)
 		{
 		   debug("Got " + e + " while setting field " +
				 field + " to " + value+"->"+converted + " in " + object);
		   e.printStackTrace();
 		}
 		return result;
 	}
 /**
  * The string representation for a Field of this type
  */
 	public String toString(Object object, Field field)
 	{
 	   String result	= "COULDN'T CONVERT!";
 	   try
 	   {
 		  result		= Integer.toString(field.getInt(object));
 	   } catch (Exception e)
 	   {
 		  e.printStackTrace();
 	   }
 	   return result;
 	}
 
 /**
  * The default value for this type, as a String.
  * This value is the one that translateToXML(...) wont bother emitting.
  * 
  * In this case, "0".
  */
 	public String defaultValue()
 	{
 	   return "0";
 	}
 }
