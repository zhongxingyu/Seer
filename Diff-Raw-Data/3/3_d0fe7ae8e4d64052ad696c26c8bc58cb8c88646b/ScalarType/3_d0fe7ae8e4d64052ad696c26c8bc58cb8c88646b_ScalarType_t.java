 /*
  * Created on Dec 31, 2004
  */
 package ecologylab.serialization.types.scalar;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.regex.Pattern;
 
 import ecologylab.generic.Debug;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.ScalarUnmarshallingContext;
 
 /**
  * Basic unit of the scalar type system. Manages marshalling from a Java class that represents a
  * scalar, to a String, and from a String to that Java class.
  * <p/>
  * The ScalarType object is a means for associating a type name with a type index. It also knows how
  * to create an instance of the type, given a String representation. If the ScalarType is a
  * reference type, this is done with getInstance(String); if the ScalarType is a primitive, this is
  * done with getValue(String), which cannot appear as a method in this, the base class, because it
  * will return a different primitive type for each such Type.
  * <p/>
  * Note: unlike with ElementState subtypes, translation of these is controlled entirely by the name
  * of the underlying Java class that gets translated, and not by the class name of subclasses of
  * this.
  * 
  * @author andruid
  */
 public abstract class ScalarType<T> extends Debug
 {
 	Class<T>										thatClass;
 
 	Class<T>										alternativeClass;
 	
 	// int index;
 	boolean											isPrimitive;
 
 	public static final Object	DEFAULT_VALUE					= null;
 
 	public static final String	DEFAULT_VALUE_STRING	= "null";
 
 	/**
 	 * Constructor is protected because there should only be 1 instance that gets re-used, for each
 	 * type. To get the instance of this type object for use in translations, call
 	 * <code>TypeRegistry.get("type-string")</code>.
 	 * 
 	 */
 	protected ScalarType(Class<T> thatClass)
 	{
 		this.thatClass = thatClass;
 		// this.index = index;
 		this.isPrimitive = thatClass.isPrimitive();
 	}
 
 	/**
 	 * If <code>this</code> is a reference type, build an appropriate Object, given a String
 	 * representation. If it is a primitive type, return a boxed value.
 	 * 
 	 * @param value
 	 *          String representation of the instance.
 	 * @param formatStrings
 	 *          Array of formatting values.
 	 * @param scalarUnmarshallingContext
 	 *          TODO
 	 */
 	abstract public T getInstance(String value, String[] formatStrings,
 			ScalarUnmarshallingContext scalarUnmarshallingContext);
 
 	/**
 	 * Construct an instance, using the subclass of this for marshalling, with null for the format
 	 * Strings.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public T getInstance(String value)
 	{
 		return getInstance(value, null, null);
 	}
 
 	/**
 	 * Set the field in the context, using the valueString, converting it to the appropriate type
 	 * using a subclass of this.
 	 * <p/>
 	 * Many different types of exceptions may be thrown. These include IllegalAccessException on the
 	 * one hand, which would come from problems with using reflection to access the Field. This is
 	 * very unlikely.
 	 * <p/>
 	 * More likely are problems with conversion of the parameter value into into an object or
 	 * primitive of the proper type.
 	 * 
 	 * @param context
 	 *          The object whose field should be modified.
 	 * @param field
 	 *          The field to be set.
 	 * @param valueString
 	 *          String representation of the value to set the field to. This Type will convert the
 	 *          value to the appropriate type, using getInstance(String) for reference types, and type
 	 *          specific getValue(String) methods for primitive types.
 	 * @param scalarUnmarshallingContext
 	 *          TODO
 	 * @return true if the field is set properly, or if the parameter value that is passed in is null.
 	 *         false if the field cannot be accessed, or if value cannot be converted to the
 	 *         appropriate type.
 	 */
 	public boolean setField(Object context, Field field, String valueString, String[] format,
 			ScalarUnmarshallingContext scalarUnmarshallingContext)
 	{
 		if (valueString == null)
 			return true;
 
 		boolean result = false;
 		T referenceObject;
 
 		try
 		{
 			referenceObject = getInstance(valueString, format, scalarUnmarshallingContext);
 			if (referenceObject != null)
 			{
 				field.set(context, referenceObject);
 				result = true;
 			}
 		}
 		catch (Exception e)
 		{
 			setFieldError(field, valueString, e);
 		}
 		return result;
 	}
 
 	/**
 	 * Set the field in the context, using the valueString, converting it to the appropriate type
 	 * using a subclass of this.
 	 * <p/>
 	 * The format annotations passed through to the ScalarType subclass will be null.
 	 * <p/>
 	 * Many different types of exceptions may be thrown. These include IllegalAccessException on the
 	 * one hand, which would come from problems with using reflection to access the Field. This is
 	 * very unlikely.
 	 * <p/>
 	 * More likely are problems with conversion of the parameter value into into an object or
 	 * primitive of the proper type.
 	 * 
 	 * @param context
 	 *          The object whose field should be modified.
 	 * @param field
 	 *          The field to be set.
 	 * @param valueString
 	 *          String representation of the value to set the field to. This Type will convert the
 	 *          value to the appropriate type, using getInstance(String) for reference types, and type
 	 *          specific getValue(String) methods for primitive types.
 	 * 
 	 * @return true if the field is set properly, or if the parameter value that is passed in is null.
 	 *         false if the field cannot be accessed, or if value cannot be converted to the
 	 *         appropriate type.
 	 */
 	public boolean setField(Object object, Field field, String value)
 	{
 		return setField(object, field, value, null, null);
 	}
 
 	/**
 	 * Display an error message that arose while setting field to value.
 	 * 
 	 * @param field
 	 * @param value
 	 * @param e
 	 */
 	protected void setFieldError(Field field, String value, Exception e)
 	{
 		error("Got " + e + " while trying to set field " + field + " to " + value);
 	}
 
 	/**
 	 * @return Returns the simple className (unqualified) for this type.
 	 */
 	@Override
 	public String getClassName()
 	{
 		return thatClass.getSimpleName();
 	}
 
 	/**
 	 * @return Returns the integer index associated with this type.
 	 */
 	/*
 	 * public int getIndex() { return index; }
 	 */
 	/**
 	 * Find out if this is a reference type or a primitive types.
 	 * 
 	 * @return true for a primitive type. false for a reference type.
 	 */
 	public boolean isPrimitive()
 	{
 		return isPrimitive;
 	}
 
 	/**
 	 * Return true if this type may need escaping when emitted as XML.
 	 * 
 	 * @return true, by default, for all reference types (includes Strings, PURLs, ...); false
 	 *         otherwise.
 	 */
 	public boolean needsEscaping()
 	{
 		return isReference();
 	}
 
 	/**
 	 * Find out if this is a reference type or a primitive types.
 	 * 
 	 * @return true for a reference type. false for a primitive type.
 	 */
 	public boolean isReference()
 	{
 		return !isPrimitive;
 	}
 
 	/**
 	 * The string representation for a Field of this type. Reference scalar types should NOT override
 	 * this. They should simply override marshall(instance), which this method calls.
 	 * <p/>
 	 * Primitive types cannot create such an instance, from the value of a field, and so must
 	 * override.
 	 */
 	public String toString(Field field, Object context)
 	{
 		String result = "COULDNT CONVERT!";
 		try
 		{
 			T instance = (T) field.get(context);
 			if (instance == null)
 				result = DEFAULT_VALUE_STRING;
 			else
 				result = marshall(instance);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/**
 	 * Get the value from the Field, in the context. Append its value to the buffy.
 	 * <p/>
 	 * Should only be called *after* checking !isDefault() yourself.
 	 * 
 	 * @param buffy
 	 * @param field
 	 * @param context
 	 * @param needsEscaping
 	 *          TODO
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 */
 	public void appendValue(Appendable buffy, FieldDescriptor fieldDescriptor, Object context)
 			throws IllegalArgumentException, IllegalAccessException, IOException
 	{
 		Object instance = fieldDescriptor.getField().get(context);
 
 		appendValue((T) instance, buffy, !fieldDescriptor.isCDATA());
 	}
 
 	/**
 	 * Get a String representation of the instance, using this. The default just calls the toString()
 	 * method on the instance.
 	 * 
 	 * @param instance
 	 * @return
 	 */
 	public String marshall(T instance)
 	{
 		return instance.toString();
 	}
 
 	/**
 	 * Get the value from the Field, in the context. Append its value to the buffy.
 	 * <p/>
 	 * Should only be called *after* checking !isDefault() yourself.
 	 * 
 	 * @param buffy
 	 * @param field
 	 * @param context
 	 * @param needsEscaping
 	 *          TODO
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 */
 	public void appendValue(StringBuilder buffy, FieldDescriptor fieldDescriptor, Object context)
 			throws IllegalArgumentException, IllegalAccessException
 	{
 		try
 		{
 			Object instance = fieldDescriptor.getField().get(context);
 
 			appendValue((T) instance, buffy, !fieldDescriptor.isCDATA());
 		}
 		catch (IllegalArgumentException e)
 		{
 			throw e;
 		}
 	}
 
 	public void appendValue(T instance, StringBuilder buffy, boolean needsEscaping)
 	{
 		buffy.append(marshall(instance));
 	}
 
 	public void appendValue(T instance, Appendable appendable, boolean needsEscaping)
 			throws IOException
 	{
 		appendable.append(marshall(instance));
 	}
 
 	/**
 	 * The default value for this type, as a String. This value is the one that translateToXML(...)
 	 * wont bother emitting.
 	 * 
 	 * In this case, "null".
 	 */
 	protected String defaultValueString()
 	{
 		return DEFAULT_VALUE_STRING;
 	}
 
 	/**
 	 * The default value for this, in its own type. Not meaningful for primitive types.
 	 * 
 	 * @return
 	 */
 	protected T defaultValue()
 	{
 		return null;
 	}
 
 	public final int defaultValueLength()
 	{
 		return defaultValueString().length();
 	}
 
 	public boolean isDefaultValue(String value)
 	{
 		String defaultValue = defaultValueString();
 		return (defaultValue.length() == value.length()) && defaultValue.equals(value);
 	}
 
 	public boolean isDefaultValue(Field field, Object context) throws IllegalArgumentException,
 			IllegalAccessException
 	{
		Object fieldValue = field.get(context);
		return fieldValue == null || DEFAULT_VALUE_STRING.equals(fieldValue.toString());
 	}
 
 	/**
 	 * Returns whether or not this is a floating point value of some sort; Types that are floating
 	 * point values should override this method to return true.
 	 * 
 	 * The implication of returning true is that the precision of this can be controlled when it is
 	 * emitted as XML.
 	 * 
 	 * @return false
 	 */
 	public boolean isFloatingPoint()
 	{
 		return false;
 	}
 
 	public boolean allowNewLines()
 	{
 		return true;
 	}
 
 	/**
 	 * Get the class object for the Type for which this manages conversion.
 	 * 
 	 * @return Class associated with this Type.
 	 */
 	public Class getTypeClass()
 	{
 		return thatClass;
 	}
 
 	public static final String	DEFAULT_DELIMS						= " \n\t";
 
 	public static final Pattern	DEFAULT_DELIMS_TOKENIZER	= Pattern.compile("([" + DEFAULT_DELIMS
 																														+ "]*)([^" + DEFAULT_DELIMS + "]+)");
 
 	public static final String	MINIMAL_DELIM							= " ";
 
 	/**
 	 * For editing: these are the valid delimiters for separating tokens that make up a field of this
 	 * type.
 	 * 
 	 * @return
 	 */
 	public Pattern delimitersTokenizer()
 	{
 		return DEFAULT_DELIMS_TOKENIZER;
 	}
 
 	public String delimeters()
 	{
 		return DEFAULT_DELIMS;
 	}
 
 	/**
 	 * The most basic and fundamental delimiter to use between characters.
 	 * 
 	 * @return The base implementation, here, returns a space.
 	 */
 	public String primaryDelimiter()
 	{
 		return MINIMAL_DELIM;
 	}
 
 	/**
 	 * When editing, determines whether delimiters can be included in token strings.
 	 * 
 	 * @return
 	 */
 	// FIXME -- Add String delimitersAfter to TextChunk -- interleaved with TextTokens, and
 	// get rid of this!!!
 	public boolean allowDelimitersInTokens()
 	{
 		return false;
 	}
 
 	/**
 	 * When editing, do not allow the user to include these characters in the resulting value String.
 	 * 
 	 * @return
 	 */
 	public String illegalChars()
 	{
 		return "";
 	}
 
 	/**
 	 * When editing, is the field one that should be part of the Term model?
 	 * 
 	 * @return true for Strings
 	 */
 	public boolean composedOfTerms()
 	{
 		return true;
 	}
 
 	/**
 	 * True if the user should be able to express interest in fields of this type.
 	 * 
 	 * @return true for Strings
 	 */
 	public boolean affordsInterestExpression()
 	{
 		return true;
 	}
 
 	String	fieldTypeName;
 
 	public String fieldTypeName()
 	{
 		String result = fieldTypeName;
 		if (result == null)
 		{
 			result = this.getClassName();
 			int index = result.indexOf("Type");
 			if (index != -1)
 			{
 				result = result.substring(0, index);
 			}
 			// if (isPrimitive())
 			// {
 			// char first = Character.toLowerCase(result.charAt(0));
 			// if (result.length() > 1)
 			// result = first + result.substring(1);
 			// else
 			// result = Character.toString(first);
 			// }
 			fieldTypeName = result;
 		}
 		return result;
 	}
 
 	/**
 	 * Used to describe scalar types used for serializing the type system, itself. They cannot be
 	 * unmarshalled in Java, only marshalled. Code may be written to access their String
 	 * representations in other languages.
 	 * 
 	 * @return false for almost all ScalarTypes
 	 */
 	public boolean isMarshallOnly()
 	{
 		return false;
 	}
 
 	public ScalarType operativeScalarType()
 	{
 		return this;
 	}
 
 	/**
 	 * Used to fill seams between direct scalar types, and those with a nested field that actually
 	 * stores the value.
 	 * 
 	 * @param externalField
 	 * @return Depending on the type, either the external field, or one within the type.
 	 */
 	public Field operativeField(Field externalField)
 	{
 		return externalField;
 	}
 
 	/**
 	 * Used to fill seams between direct scalar types, and those with a nested field that actually
 	 * stores the value.
 	 * 
 	 * @param largerContext
 	 * @param field
 	 * 
 	 * @return Depending on the type, the larger context passed in, or the object value of the field
 	 *         within the context.
 	 */
 	public T unpackContext(Object largerContext, Field field)
 	{
 		return (T) largerContext;
 	}
 
 	abstract public String getObjectiveCType();
 
 	abstract public String getCSharptType();
 
 	abstract public String getDbType();
 }
