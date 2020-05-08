 /**
  * 
  */
 package ecologylab.xml;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
 import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.xml.types.scalar.ScalarType;
 import ecologylab.xml.types.scalar.TypeRegistry;
 
 /**
  * Used to provide convenient access for setting and getting values, using the ecologylab.xml type
  * system. Provides marshalling and unmarshalling from Strings.
  * 
  * @author andruid
  */
 public class FieldDescriptor extends ElementState implements FieldTypes
 {
 	public static final String			NULL						= ScalarType.DEFAULT_VALUE_STRING;
 
 	@xml_attribute
 	protected Field						field;
 
 	/**
 	 * The tag name that this field is translated to XML with.
 	 * For polymorphic fields, the value of this field is meaningless, except for wrapped collections and maps.
 	 */
 	@xml_attribute
 	private String										tagName;
 	
 	/**
 	 * Used to specify old translations, for backwards compatability. Never written.
 	 */
 	@xml_nowrap
 	@xml_collection("other_tag") private
 	ArrayList<String>								otherTags;
 
 	/**
 	 * Descriptor for the class that this field is declared in.
 	 */
 	protected ClassDescriptor	declaringClassDescriptor;
 
 	@xml_attribute
 	private int												type;
 
 	/**
 	 * This slot makes sense only for attributes and leaf nodes
 	 */
 	@xml_attribute
 	private ScalarType<?>							scalarType;
 
 	/**
 	 * An option for scalar formatting.
 	 */
 	private String[]								format;
 
 	@xml_attribute
 	private boolean									isCDATA;
 
 	@xml_attribute
 	private boolean									needsEscaping;
 	
 	/**
 	 * The FieldDescriptor for the field in a wrap.
 	 */
 	private FieldDescriptor									wrappedFD;
 
 	/**
 	 * Null if the tag for this field is derived from its field declaration. For most fields, tag is
 	 * derived from the field declaration (using field name or @xml_tag).
 	 * <p/>
 	 * However, for some fields, such as those declared using @xml_class, @xml_classes, or @xml_scope,
 	 * the tag is derived from the class declaration (using class name or @xml_tag). This is, for
 	 * example, required for polymorphic nested and collection fields. For these fields, this slot
 	 * contains an array of the legal classes, which will be bound to this field during
 	 * translateFromXML().
 	 */
 	private HashMapArrayList<String, ClassDescriptor>			tagClassDescriptors;
 	private String	unresolvedScopeAnnotation = null;
 	
 	@xml_map("tagClasses")
 	private HashMap<String, Class>						tagClasses;
 
 /**
  * 
  */ @xml_attribute
 	private String													collectionOrMapTagName;
 
 	/**
 	 * Used for Collection and Map fields. Tells if the XML should be wrapped by an intermediate
 	 * element.
 	 */
  	@xml_attribute
 	private boolean									wrapped;
 	
 
 	private Method													setValueMethod;
 
 	public static final Class[]			SET_METHOD_ARG	= { String.class };
 
 	/**
 	 * For nested elements, and collections or maps of nested elements.
 	 * The class descriptor 
 	 */
 	
 	private ClassDescriptor					elementClassDescriptor;
 	
 	@xml_attribute 
 	private Class 							elementClass;
 
 	
 	/**
 	 * Default constructor only for use by translateFromXML().
 	 */
 	public FieldDescriptor()
 	{
 		super();
 		
 	}
 	
 	/**
 	 * Constructor for the pseudo-FieldDescriptor associated with each ClassDesctiptor, for
 	 * translateToXML of fields that deriveTagFromClass.
 	 * 
 	 * @param baseClassDescriptor
 	 */
 	public FieldDescriptor(ClassDescriptor baseClassDescriptor)
 	{
 		this.declaringClassDescriptor = baseClassDescriptor;
 		this.field = null;
 		this.tagName = baseClassDescriptor.getTagName();
 		this.type = PSEUDO_FIELD_DESCRIPTOR;
 		this.scalarType = null;
 	}
 
 	public FieldDescriptor(ClassDescriptor baseClassDescriptor, FieldDescriptor wrappedFD, String wrapperTag)
 	{
 		this.declaringClassDescriptor = baseClassDescriptor;
 		this.wrappedFD	= wrappedFD;
 		this.type		= WRAPPER;
 		this.tagName	= wrapperTag;
 	}
 	/**
 	 * This is the normal constructor.
 	 * 
 	 * @param declaringClassDescriptor
 	 * @param field
 	 * @param annotationType 		Coarse pre-evaluation of the field's annotation type.
 	 * 													Does not differentiate scalars from elements, or check for semantic consistency.
 	 */
 	public FieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, int annotationType) // String nameSpacePrefix
 	{
 		this.declaringClassDescriptor = declaringClassDescriptor;
 		this.field = field;
 		this.field.setAccessible(true);
 
 		deriveTagClassDescriptors(field);
 		
 //		if (!isPolymorphic())
 			this.tagName = XMLTools.getXmlTagName(field);	// uses field name or @xml_tag declaration
 
 		// TODO XmlNs
 		// if (nameSpacePrefix != null)
 		// {
 		// tagName = nameSpacePrefix + tagName;
 		// }
 		type	= UNSET_TYPE;	// for debugging!
 		type	= deriveTypeFromField(field, annotationType);
 		
 		switch (type)
 		{
 		case ATTRIBUTE:
 		case LEAF:
 		case TEXT_ELEMENT:
 		case ENUMERATED_ATTRIBUTE:
 		case ENUMERATED_LEAF:
 			scalarType = deriveScalar(field);
 		}
 		// looks old: -- implement this next???
 		// if (XMLTools.isNested(field))
 		// setupXmlText(ClassDescriptor.getClassDescriptor((Class<ElementState>) field.getType()));
 
 		setValueMethod = ReflectionTools.getMethod(field.getType(), "setValue", SET_METHOD_ARG);
 	}
 	
 	
 /**
  * Process annotations that use meta-language to map tags for translate from based on classes (instead of field names).
  * 
  * @param field
  * @return
  */
 	private boolean deriveTagClassDescriptors(Field field)
 	{
 		// @xml_scope
 		final ElementState.xml_scope scopeAnnotationObj = field
 				.getAnnotation(ElementState.xml_scope.class);
 		final String scopeAnnotation = (scopeAnnotationObj == null) ? null : scopeAnnotationObj.value();
 
 		if (scopeAnnotation != null && scopeAnnotation.length() > 0)
 		{
 			if (!resolveScopeAnnotation(scopeAnnotation))
 			{
 				unresolvedScopeAnnotation	= scopeAnnotation;
 				declaringClassDescriptor.registerUnresolvedScopeAnnotationFD(this);
 			}
 		}
 		// @xml_classes
 		final ElementState.xml_classes classesAnnotationObj = field
 		.getAnnotation(ElementState.xml_classes.class);
 		final Class[] classesAnnotation = (classesAnnotationObj == null) ? null : classesAnnotationObj.value();
 		if ((classesAnnotation != null) && (classesAnnotation.length > 0))
 		{
 			initTagClassDescriptorsArrayList(classesAnnotation.length);
 			for (Class thatClass : classesAnnotation)
 				if (ElementState.class.isAssignableFrom(thatClass))
 				{
 					ClassDescriptor classDescriptor = ClassDescriptor.getClassDescriptor(thatClass);
 					putTagClassDescriptor(classDescriptor);
 					tagClasses.put(classDescriptor.getTagName(), classDescriptor.describedClass());
 				}
 		}
 		// @xml_class
 		final ElementState.xml_class classAnnotationObj = field
 		.getAnnotation(ElementState.xml_class.class);
 		final Class classAnnotation = (classAnnotationObj == null) ? null : classAnnotationObj.value();
 		if (classAnnotation != null)
 		{
 			initTagClassDescriptorsArrayList(1);
 			ClassDescriptor classDescriptor = ClassDescriptor.getClassDescriptor(classAnnotation);
 			putTagClassDescriptor(classDescriptor);
 			tagClasses.put(classDescriptor.getTagName(), classDescriptor.describedClass());
 		}
 		return tagClassDescriptors != null;
 	}
 
 	private void putTagClassDescriptor(ClassDescriptor classDescriptor) 
 	{
 		tagClassDescriptors.put(classDescriptor.getTagName(), classDescriptor);
 		String[] otherTags = classDescriptor.otherTags();
 		if (otherTags != null)
 			for (String otherTag : otherTags)
 			{
 				if ((otherTag != null) && (otherTag.length() > 0))
 				{
 					tagClassDescriptors.put(otherTag, classDescriptor);
 				}
 			}
 	}
 	
 	/**
 	 * Generate tag -> class mappings for a @serial_scope declaration.
 	 * 
 	 * @param scopeAnnotation	Name of the scope to lookup in the global space. Must be non-null.
 	 * 
 	 * @return	true if the scope annotation is successfully resolved to a TranslationScope.
 	 */
 	private boolean resolveScopeAnnotation(final String scopeAnnotation) 
 	{
 		TranslationScope scope = TranslationScope.get(scopeAnnotation);
 		if (scope != null)
 		{
 			Collection<ClassDescriptor> scopeClassDescriptors = scope.getClassDescriptors();
 			initTagClassDescriptorsArrayList(scopeClassDescriptors.size());
 			for (ClassDescriptor classDescriptor : scopeClassDescriptors)
 			{
 				String tagName = classDescriptor.getTagName();
 				tagClassDescriptors.put(tagName, classDescriptor);
 				tagClasses.put(tagName, classDescriptor.describedClass());
 			}
 		}
 		return scope != null;
 	}
 
 	/**
 	 * If there is an unresolvedScopeAnnotation, because a scope had not yet been declared when a ClassDescriptor
 	 * that uses it was constructed, try again.
 	 * 
 	 * @return
 	 */
 	boolean resolveUnresolvedScopeAnnotation()
 	{
 		if (unresolvedScopeAnnotation == null)
 			return true;
 		
 		boolean result	= resolveScopeAnnotation(unresolvedScopeAnnotation);
 		if (result)
 		{
 			unresolvedScopeAnnotation	= null;
 			declaringClassDescriptor.mapTagClassDescriptors(this);
 		}
 		return result;
 	}
 
 	private void initTagClassDescriptorsArrayList(int initialSize)
 	{
 		if (tagClassDescriptors == null)
 			tagClassDescriptors = new HashMapArrayList<String, ClassDescriptor>(initialSize);
 		if( tagClasses == null)
 			tagClasses = new HashMap<String, Class>(initialSize);
 	}
 
 	/**
 	 * Bind the ScalarType for a scalar typed field (attribute, leaf node, text).
 	 * As appropriate, derive other context for scalar fields (is leaf, format).
 	 * <p/>
 	 * This method should only be called when you already know the field has a scalar annotation.
 	 * 
 	 * @param field
 	 * @return			The ScalarType for the field, or null, if none can be found.
 	 */
 	private ScalarType deriveScalar(Field field)
 	{
 		ScalarType result = TypeRegistry.getType(field);
 		if (result != null)
 		{
 				if (type == LEAF || type == TEXT_ELEMENT)
 				{
 					isCDATA 			= XMLTools.leafIsCDATA(field);
 					needsEscaping = result.needsEscaping();
 				}
 				format = XMLTools.getFormatAnnotation(field);
 		}
 		return result;
 	}
 	private ScalarType deriveCollectionScalar(Class collectionScalarClass, Field field)
 	{
 		ScalarType result = TypeRegistry.getType(collectionScalarClass);
 		if (result != null)
 		{
 			needsEscaping 	= result.needsEscaping();
 			format = XMLTools.getFormatAnnotation(field);
 		}
 		return result;
 	}
 	/**
 	 * Figure out the type of field. Build associated data structures, such as
 	 * collection or element class & tag.
 	 * Process @xml_other_tags.
 	 * 
 	 * @param field
 	 * @param annotationType Partial type information from the field declaration annotations, which are required.
 	 */
 	//FIXME -- not complete!!!! return to finish other cases!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	@SuppressWarnings("unchecked")
 	private int deriveTypeFromField(Field field, int annotationType)
 	{
 		int result 				= annotationType;
 		Class fieldClass	= field.getType();
 		switch (annotationType)
 		{
 		case ATTRIBUTE:
 			scalarType			= deriveScalar(field);
 			if (scalarType == null)
 				result				= IGNORED_ATTRIBUTE;
 			break;
 		case LEAF:
 			scalarType			= deriveScalar(field);
 			if (scalarType == null)
 				result				= IGNORED_ELEMENT;
 			break;
 		case TEXT_ELEMENT:
 			scalarType			= deriveScalar(field);
 			if (scalarType == null)
 				result				= IGNORED_ELEMENT;
 			break;
 		case NESTED_ELEMENT:
 			if (!checkAssignableFrom(ElementState.class, field, fieldClass, "@xml_nested"))
 				result				= IGNORED_ELEMENT;
 			else if (!isPolymorphic())
 			{
 				elementClassDescriptor	= ClassDescriptor.getClassDescriptor(fieldClass);
 				elementClass = elementClassDescriptor.describedClass();
 				tagName = XMLTools.getXmlTagName(field);
 			}
 			break;
 		case COLLECTION_ELEMENT:
 				final String collectionTag = field.getAnnotation(ElementState.xml_collection.class).value();
 				if (!checkAssignableFrom(Collection.class, field, fieldClass, "@xml_collection"))
 					return IGNORED_ELEMENT;
 
 				if (!isPolymorphic())
 				{					
 					Class collectionElementClass = getTypeArgClass(field, 0); // 0th type arg for Collection<FooState>
 
 					if (collectionTag == null || collectionTag.isEmpty())
 					{	
 						warning("In " + declaringClassDescriptor.getDescribedClass()
 								+ "\n\tCan't translate  @xml_collection() " + field.getName()
 								+ " because its tag argument is missing.");
 						return IGNORED_ELEMENT;
 					}
 					if (collectionElementClass == null)
 					{
 						warning("In " + declaringClassDescriptor.getDescribedClass()
 								+ "\n\tCan't translate  @xml_collection() " + field.getName()
 								+ " because the parameterized type argument for the Collection is missing.");
 						return IGNORED_ELEMENT;
 					}
 					if (ElementState.class.isAssignableFrom(collectionElementClass))
 					{
 						elementClassDescriptor	= ClassDescriptor.getClassDescriptor(collectionElementClass);
 						elementClass = elementClassDescriptor.describedClass();
 					}
 					else
 					{
 						result = COLLECTION_SCALAR;
 						scalarType	= deriveCollectionScalar(collectionElementClass, field);
 					}
 				}
 				else
 				{
 					if (collectionTag != null && !collectionTag.isEmpty())
 					{
 						warning("In " + declaringClassDescriptor.getDescribedClass()
 								+ "\n\tIgnoring argument to  @xml_collection() " + field.getName()
 								+ " because it is declared polymorphic with @xml_classes.");
 					}
 				}
 				collectionOrMapTagName = collectionTag;
 				break;
 		case MAP_ELEMENT:
 			String mapTag = field.getAnnotation(ElementState.xml_map.class).value();
 			if (!checkAssignableFrom(Map.class, field, fieldClass, "@xml_map"))
 					return IGNORED_ELEMENT;
 
 				if (!isPolymorphic())
 				{					
 					Class mapElementClass = getTypeArgClass(field, 1); // "1st" type arg for Map<FooState>
 					
 					if (mapTag == null || mapTag.isEmpty())
 					{
 						warning("In " + declaringClassDescriptor.getDescribedClass()
 								+ "\n\tCan't translate  @xml_map() " + field.getName()
 								+ " because its tag argument is missing.");
 						return IGNORED_ELEMENT;
 					}
 					if (mapElementClass == null)
 					{
 						warning("In " + declaringClassDescriptor.getDescribedClass()
 								+ "\n\tCan't translate  @xml_map() " + field.getName()
 								+ " because the parameterized type argument for the Collection is missing.");
 						return IGNORED_ELEMENT;
 					}					
 
 					if (ElementState.class.isAssignableFrom(mapElementClass))
 					{
 						elementClassDescriptor	= ClassDescriptor.getClassDescriptor(mapElementClass);
 						elementClass = elementClassDescriptor.describedClass();
 					}
 					else
 					{
 						result = MAP_SCALAR;		//TODO -- do we really support this case??
 						scalarType	= deriveCollectionScalar(mapElementClass, field);
 					}
 				}
 				else
 				{
 					if (mapTag != null && !mapTag.isEmpty())
 					{
 						warning("In " + declaringClassDescriptor.getDescribedClass()
 								+ "\n\tIgnoring argument to  @xml_map() " + field.getName()
 								+ " because it is declared polymorphic with @xml_classes.");
 					}
 				}
 				collectionOrMapTagName = mapTag;
 			break;
 		default:
 			break;
 		}
 		if (annotationType == COLLECTION_ELEMENT || annotationType == MAP_ELEMENT)
 		{
 			if (!field.isAnnotationPresent(ElementState.xml_nowrap.class))
 				wrapped			= true;
 		}
 /*
 			else
 			{ // deriveTagFromClasses
 				// TODO Monday
 			}
 			*/
 		if (result == UNSET_TYPE)
 		{
 			warning("Programmer error -- can't derive type.");
 			result	= IGNORED_ELEMENT;
 		}
 		
 		return result;
 	}
 
 	private boolean checkAssignableFrom(Class targetClass, Field field, Class fieldClass, String annotationDescription)
 	{
 		boolean result = targetClass.isAssignableFrom(fieldClass);
 		if (!result)
 		{
 			warning("In " + declaringClassDescriptor.getDescribedClass()
 					+ "\n\tCan't translate  " + annotationDescription + "() " + field.getName()
 					+ " because the annotated field is not an instance of " + targetClass.getSimpleName() + ".");
 		}
 		return result;
 	}
 
 		
 
 	/**
 	 * Get the value of the ith declared type argument from a field declaration. Only works when the
 	 * type variable is directly instantiated in the declaration.
 	 * <p/>
 	 * DOES NOT WORK when the type variable is instantiated outside the declaration, and passed in.
 	 * This is because in Java, generic type variables are (lamely!) erased after compile time. They
 	 * do not exist at runtime :-( :-( :-(
 	 * 
 	 * @param field
 	 * @param i
 	 *          Index of the type variable in the field declaration.
 	 * 
 	 * @return The class of the type variable, if it exists.
 	 */
 	@SuppressWarnings("unchecked")
 	public Class<?> getTypeArgClass(Field field, int i)
 	{
 		Class result = null;
 
 		java.lang.reflect.Type[] typeArgs = ReflectionTools.getParameterizedTypeTokens(field);
 		if (typeArgs != null)
 		{
 			final int max	= typeArgs.length - 1;
 			if (i > max)
 				i						= max;
 			final Type typeArg0 = typeArgs[i];
 			if (typeArg0 instanceof Class)
 			{
 				result = (Class) typeArg0;
 			}
 			else if (typeArg0 instanceof ParameterizedTypeImpl)
 			{	// nested parameterized type
 				ParameterizedTypeImpl pti	= (ParameterizedTypeImpl) typeArg0;
 				result	= pti.getRawType();
 			}
 			else if (typeArg0 instanceof TypeVariableImpl)
 			{
 				TypeVariableImpl tvi	= (TypeVariableImpl) typeArg0;
 				Type[] tviBounds			= tvi.getBounds();
 				result								= (Class) tviBounds[0];
 				debug("yo! " + result);
 			}
 
 			else
 			{
 				error("getTypeArgClass(" + field + ", " + i + " yucky! Consult s.im.mp serialization developers.");
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return true if this field represents a ScalarType, not a nested element or collection thereof.
 	 */
 	public boolean isScalar()
 	{
 		return scalarType != null;
 	}
 
 	public boolean isCollection()
 	{
 		switch (type)
 		{
 		case MAP_ELEMENT:
 		case MAP_SCALAR:
 		case COLLECTION_ELEMENT:
 		case COLLECTION_SCALAR:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean isNested()
 	{
 		return type == NESTED_ELEMENT;
 	}
 
 	public boolean set(ElementState context, String valueString)
 	{
 		return set(context, valueString, null);
 	}
   /**
    * For noting that the object of this root or @xml_nested field has, within it, a field declared with @xml_text.
    */
   private		Field		xmlTextScalarField;
 
 	/**
 	 * In the supplied context object, set the *typed* value of the field, using the valueString
 	 * passed in. Unmarshalling is performed automatically, by the ScalarType already stored in this.
 	 * <p/>
 	 * Use a set method, if one is defined.
 	 * 
 	 * @param context
 	 *          ElementState object to set the Field in this.
 	 * 
 	 * @param valueString
 	 *          The value to set, which this method will use with the ScalarType, to create the value
 	 *          that will be set.
 	 */
 	// FIXME -- pass in ScalarUnmarshallingContext, and use it!
 	public boolean set(ElementState context, String valueString,
 			ScalarUnmarshallingContext scalarUnMarshallingContext)
 	{
 		boolean result = false;
 		// if ((valueString != null) && (context != null)) andruid & andrew 4/14/09 -- why not allow set
 		// to null?!
 		if ((context != null))
 		{
 			//FIXME -- clean up this mess!
 			if (xmlTextScalarField != null) // this is for MetadataScalars, to set the value in the nested
 																			// object, instead of operating directly on the value
 			{
 				try
 				{
 					ElementState nestedES = (ElementState) field.get(context);
 					if (nestedES == null)
 					{
 						// The field is not initialized...
 						this.setField(context, field.getType().newInstance());
 						nestedES = (ElementState) field.get(context);
 					}
 					if (setValueMethod != null)
 					{
 						ReflectionTools.invoke(setValueMethod, nestedES, valueString);
 						result = true;
 					}
 					else
 						scalarType.setField(nestedES, xmlTextScalarField, valueString, null,
 								scalarUnMarshallingContext);
 					result = true;
 
 				}
 				catch (IllegalArgumentException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				catch (IllegalAccessException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				catch (InstantiationException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			else if (isScalar())
 			{
 				scalarType.setField(context, field, valueString, null,
 						scalarUnMarshallingContext);
 				result = true;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * In the supplied context object, set the non-scalar field to a non-scalar value.
 	 * 
 	 * @param context
 	 * 
 	 * @param value
 	 *          An ElementState, or a Collection, or a Map.
 	 */
 	public void set(ElementState context, Object value)
 	{
 		if (!isScalar())
 		{
 			setField(context, value);
 		}
 	}
 
 	public void setField(ElementState context, Object value)
 	{
 		try
 		{
 			field.set(context, value);
 		}
 		catch (IllegalArgumentException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get the String representation of the value of the field, in the context object, using the
 	 * ScalarType.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public String getValueString(Object context)
 	{
 		String result = NULL;
 		if (context != null)
 		{
 			Field operativeField = xmlTextScalarField;
 			if (operativeField != null)
 			{
 				try
 				{
 					ElementState nestedES = (ElementState) field.get(context);
 
 					// If nestedES is null...then the field is not initialized.
 					if (nestedES != null)
 					{
 						result = scalarType.toString(operativeField, nestedES);
 					}
 
 				}
 				catch (IllegalArgumentException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				catch (IllegalAccessException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			else if (isScalar())
 			{
 				result = scalarType.toString(field, context);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return The Java name of the field.
 	 */
 	public String getFieldName()
 	{
		return (field != null) ? field.getName() : "NULL";
 	}
 
 	/**
 	 * NB: For polymorphic fields, the value of this field is meaningless, 
 	 * except for wrapped collections and maps.
 	 *
 	 * @return 	 The tag name that this field is translated to XML with.
 	 */
 	public String getTagName()
 	{
 		return tagName;
 	}
 
 	/**
 	 * @return the scalarType of the field
 	 */
 	public ScalarType<?> getScalarType()
 	{
 		return scalarType;
 	}
 
 	/**
 	 * @return the field
 	 */
 	public Field getField()
 	{
 		return field;
 	}
 
 	/**
 	 * @return the class of the field
 	 */
 	public Class<?> getFieldType()
 	{
 		return field.getType();
 	}
 
 	/**
 	 * 
 	 * @return The OptimizationTypes type of the field.
 	 */
 	public int getType()
 	{
 		return type;
 	}
 
 	/**
 	 * @return the xmlTextScalarField
 	 */
 	public Field getXmlTextScalarField()
 	{
 		return xmlTextScalarField;
 	}
 
 	public ElementState getNested(ElementState context)
 	{
 		return (ElementState) ReflectionTools.getFieldValue(context, field);
 	}
 
 	public Map getMap(ElementState context)
 	{
 		return (Map) ReflectionTools.getFieldValue(context, field);
 	}
 
 	public Collection getCollection(ElementState context)
 	{
 		return (Collection) ReflectionTools.getFieldValue(context, field);
 	}
 
 	public boolean isPseudoScalar()
 	{
 		return false;
 	}
 
 	public boolean isMixin()
 	{
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param context
 	 *          Object that the field is in.
 	 * 
 	 * @return true if the field is not a scalar or a psuedo-scalar, and it has a non null value.
 	 */
 	public boolean isNonNullReference(ElementState context)
 	{
 		boolean result = false;
 		try
 		{
 			result = (scalarType == null) && !isPseudoScalar() && (field.get(context) != null);
 		}
 		catch (IllegalArgumentException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public ElementState getAndPerhapsCreateNested(ElementState context)
 	{
 		ElementState result = getNested(context);
 
 		if (result == null)
 		{
 			result = (ElementState) ReflectionTools.getInstance(field.getType());
 			ReflectionTools.setFieldValue(context, field, result);
 		}
 		return result;
 	}
 
 	public boolean isWrapped()
 	{
 		return wrapped;
 	}
 
 	/**
 	 * Use this and the context to append an attribute / value pair to the StringBuilder passed in.
 	 * 
 	 * @param buffy
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	public void appendValueAsAttribute(StringBuilder buffy, Object context)
 			throws IllegalArgumentException, IllegalAccessException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			Field field = this.field;
 
 			if (scalarType == null)
 			{
 				weird("scalarType = null!");
 			}
 			else if (!scalarType.isDefaultValue(field, context))
 			{
 				// for this field, generate tags and attach name value pair
 
 				// TODO if type.isFloatingPoint() -- deal with floatValuePrecision here!
 				// (which is an instance variable of this) !!!
 
 				buffy.append(' ');
 				buffy.append(this.tagName);
 				buffy.append('=');
 				buffy.append('"');
 
 				scalarType.appendValue(buffy, this, context);
 				buffy.append('"');
 			}
 		}
 	}
 
 	/**
 	 * Use this and the context to set an attribute (name, value) on the Element DOM Node passed in.
 	 * 
 	 * @param element
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	public void setAttribute(Element element, Object context) throws IllegalArgumentException,
 			IllegalAccessException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			Field field = this.field;
 
 			if (scalarType == null)
 				weird("YO! setAttribute() scalarType == null!!!");
 			else if (!scalarType.isDefaultValue(field, context))
 			{
 				// for this field, generate tags and attach name value pair
 
 				// TODO if type.isFloatingPoint() -- deal with floatValuePrecision here!
 				// (which is an instance variable of this) !!!
 
 				String value = scalarType.toString(field, context);
 
 				element.setAttribute(tagName, value);
 			}
 		}
 	}
 
 	/**
 	 * Use this and the context to set an attribute (name, value) on the Element DOM Node passed in.
 	 * 
 	 * @param element
 	 * @param instance
 	 * @param isAtXMLText
 	 *          TODO
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	public void appendLeaf(Element element, Object instance) throws IllegalArgumentException,
 			IllegalAccessException
 	{
 		if (instance != null)
 		{
 			ScalarType scalarType = this.scalarType;
 
 			Document document = element.getOwnerDocument();
 
 			Object fieldInstance = field.get(instance);
 			if (fieldInstance != null)
 			{
 
 				String fieldValueString = fieldInstance.toString();
 
 				Text textNode = isCDATA ? document.createCDATASection(fieldValueString) : document
 						.createTextNode(fieldValueString);
 
 				Element leafNode = document.createElement(tagName);
 				leafNode.appendChild(textNode);
 
 				element.appendChild(leafNode);
 			}
 		}
 	}
 
 	public void appendXmlText(Element element, Object instance) throws IllegalArgumentException,
 			IllegalAccessException
 	{
 		if (instance != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			if (!scalarType.isDefaultValue(xmlTextScalarField, instance))
 			{
 				Document document = element.getOwnerDocument();
 	
 				Object fieldInstance = xmlTextScalarField.get(instance);
 				if (fieldInstance != null)
 				{
 					String fieldValueString = fieldInstance.toString();
 	
 					Text textNode = isCDATA ? document.createCDATASection(fieldValueString) : document
 							.createTextNode(fieldValueString);
 	
 					element.appendChild(textNode);
 				}
 			}
 		}
 	}
 
 	public void appendCollectionLeaf(Element element, Object instance)
 			throws IllegalArgumentException, IllegalAccessException
 	{
 		if (instance != null)
 		{
 			Document document = element.getOwnerDocument();
 
 			String instanceString = instance.toString();
 
 			// Object fieldInstance = field.get(instance);
 			// String fieldValueString= fieldInstance.toString();
 
 			Text textNode = isCDATA ? document.createCDATASection(instanceString) : document
 					.createTextNode(instanceString);
 
 			Element leafNode = document.createElement(tagName);
 			leafNode.appendChild(textNode);
 
 			element.appendChild(leafNode);
 		}
 	}
 
 	/**
 	 * Use this and the context to append an attribute / value pair to the Appendable passed in.
 	 * 
 	 * @param appendable
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws IOException
 	 */
 	public void appendValueAsAttribute(Appendable appendable, Object context)
 			throws IllegalArgumentException, IllegalAccessException, IOException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			Field field = this.field;
 
 			if (!scalarType.isDefaultValue(field, context))
 			{
 				// for this field, generate tags and attach name value pair
 
 				// TODO if type.isFloatingPoint() -- deal with floatValuePrecision here!
 				// (which is an instance variable of this) !!!
 
 				appendable.append(' ');
 				appendable.append(tagName);
 				appendable.append('=');
 				appendable.append('"');
 
 				scalarType.appendValue(appendable, this, context);
 				appendable.append('"');
 			}
 		}
 	}
 
 	static final String	START_CDATA	= "<![CDATA[";
 
 	static final String	END_CDATA		= "]]>";
 
 	/**
 	 * Use this and the context to append a leaf node with value to the StringBuilder passed in,
 	 * unless it turns out that the value is the default.
 	 * 
 	 * @param buffy
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	void appendLeaf(StringBuilder buffy, Object context) throws IllegalArgumentException,
 			IllegalAccessException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			Field field = this.field;
 			if (!scalarType.isDefaultValue(field, context))
 			{
 				// for this field, generate <tag>value</tag>
 
 				// TODO if type.isFloatingPoint() -- deal with floatValuePrecision here!
 				// (which is an instance variable of this) !!!
 				writeOpenTag(buffy);
 
 				if (isCDATA)
 					buffy.append(START_CDATA);
 				scalarType.appendValue(buffy, this, context); // escape if not CDATA! :-)
 				if (isCDATA)
 					buffy.append(END_CDATA);
 
 				writeCloseTag(buffy);
 			}
 		}
 	}
 
 	/**
 	 * Use this and the context to append a text node value to the StringBuilder passed in, unless it
 	 * turns out that the value is the default.
 	 * 
 	 * @param buffy
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	void appendXmlText(StringBuilder buffy, Object context) throws IllegalArgumentException,
 			IllegalAccessException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			if (!scalarType.isDefaultValue(xmlTextScalarField, context))
 			{
 				// for this field, generate <tag>value</tag>
 
 				if (isCDATA)
 					buffy.append(START_CDATA);
 				// TODO lkkljhkhj
 				scalarType.appendValue(buffy, this, context); // escape if not CDATA! :-)
 				if (isCDATA)
 					buffy.append(END_CDATA);
 			}
 		}
 	}
 
 	/**
 	 * Use this and the context to append a leaf node with value to the StringBuilder passed in.
 	 * Consideration of default values is not evaluated.
 	 * 
 	 * @param buffy
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	void appendCollectionLeaf(StringBuilder buffy, Object instance) throws IllegalArgumentException,
 			IllegalAccessException
 	{
 		if (instance != null)
 		{
 			ScalarType scalarType = this.scalarType;
 
 			writeOpenTag(buffy);
 			if (isCDATA)
 				buffy.append(START_CDATA);
 			scalarType.appendValue(instance, buffy, !isCDATA); // escape if not CDATA! :-)
 			if (isCDATA)
 				buffy.append(END_CDATA);
 
 			writeCloseTag(buffy);
 		}
 	}
 
 	/**
 	 * Use this and the context to append a leaf node with value to the Appendable passed in.
 	 * Consideration of default values is not evaluated.
 	 * 
 	 * @param appendable
 	 * @param context
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws IOException
 	 */
 	void appendCollectionLeaf(Appendable appendable, Object instance)
 			throws IllegalArgumentException, IllegalAccessException, IOException
 	{
 		if (instance != null)
 		{
 			ScalarType scalarType = this.scalarType;
 
 			writeOpenTag(appendable);
 			if (isCDATA)
 				appendable.append(START_CDATA);
 			scalarType.appendValue(instance, appendable, !isCDATA); // escape if not CDATA! :-)
 			if (isCDATA)
 				appendable.append(END_CDATA);
 
 			writeCloseTag(appendable);
 		}
 	}
 
 	/**
 	 * Use this and the context to append a leaf node with value to the Appendable passed in.
 	 * 
 	 * @param buffy
 	 * @param context
 	 * @param isAtXMLText
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	void appendLeaf(Appendable appendable, Object context) throws IllegalArgumentException,
 			IllegalAccessException, IOException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			Field field = this.field;
 			if (!scalarType.isDefaultValue(field, context))
 			{
 				// for this field, generate <tag>value</tag>
 
 				// TODO if type.isFloatingPoint() -- deal with floatValuePrecision here!
 				// (which is an instance variable of this) !!!
 
 				writeOpenTag(appendable);
 
 				if (isCDATA)
 					appendable.append(START_CDATA);
 				scalarType.appendValue(appendable, this, context); // escape if not CDATA! :-)
 				if (isCDATA)
 					appendable.append(END_CDATA);
 
 				writeCloseTag(appendable);
 			}
 		}
 	}
 
 	void appendXmlText(Appendable appendable, Object context) throws IllegalArgumentException,
 			IllegalAccessException, IOException
 	{
 		if (context != null)
 		{
 			ScalarType scalarType = this.scalarType;
 			if (!scalarType.isDefaultValue(xmlTextScalarField, context))
 			{
 				// for this field, generate <tag>value</tag>
 
 				if (isCDATA)
 					appendable.append(START_CDATA);
 
 				scalarType.appendValue(appendable, this, context); // escape if not CDATA! :-)
 				if (isCDATA)
 					appendable.append(END_CDATA);
 			}
 		}
 	}
 
 	public boolean isCDATA()
 	{
 		return isCDATA;
 	}
 
 	public boolean isNeedsEscaping()
 	{
 		return needsEscaping;
 	}
 
 	public String[] getFormat()
 	{
 		return format;
 	}
 
 	public String toString()
 	{
 		String name = (field != null) ? field.getName() : "NO_FIELD";
 		return this.getClassName() + "[" + name + " < " + declaringClassDescriptor.getDescribedClass() + " type=0x" + Integer.toHexString(type) + "]";
 	}
 
 	public HashMapArrayList<String, ClassDescriptor> getTagClassDescriptors()
 	{
 		if(tagClassDescriptors == null) 
 			return null;
 		else
 		return tagClassDescriptors;
 	}
 	
 	public HashMap<String, Class> getTagClasses()
 	{
 		if(tagClasses == null) 
 			return null;
 		else
 		return tagClasses;
 	}
 
 	public void writeElementStart(StringBuilder buffy) 
 	{
 		buffy.append('<').append(elementStart());
 	}
 
 	public void writeElementStart(Appendable appendable) 
 	throws IOException
 	{
 		appendable.append('<').append(elementStart());
 	}
 
 	void writeOpenTag(StringBuilder buffy)
 	{
 		buffy.append('<').append(elementStart()).append('>');
 	}
 
 	void writeCloseTag(StringBuilder buffy)
 	{
 		buffy.append('<').append('/').append(elementStart()).append('>');
 	}
 
 	void writeOpenTag(Appendable buffy) throws IOException
 	{
 		buffy.append('<').append(elementStart()).append('>');
 	}
 
 	void writeCloseTag(Appendable buffy) throws IOException
 	{
 		buffy.append('<').append('/').append(elementStart()).append('>');
 	}
 
 	/**
 	 * Write the tags for opening and closing a wrapped collection.
 	 * 
 	 * @param buffy
 	 * @param close
 	 */
 	public void writeWrap(StringBuilder buffy, boolean close) 
 	{
 		buffy.append('<');
 		if (close)
 			buffy.append('/');
 		buffy.append(tagName).append('>');
 	}
 
 	/**
 	 * Write the tags for opening and closing a wrapped collection.
 	 * 
 	 * @param appendable
 	 * @param close
 	 * @throws IOException
 	 */
 	public void writeWrap(Appendable appendable, boolean close) 
 	throws IOException
 	{
 		appendable.append('<');
 		if (close)
 			appendable.append('/');
 		appendable.append(tagName).append('>');
 	}
 
 	//----------------------------- methods from TagDescriptor ---------------------------------------//
 	/**
 	 * Use a set method or the type system to set our field in the context to the value.
 	 * 
 	 * @param context
 	 * @param value
 	 * @param scalarUnmarshallingContext TODO
 	 */
 	protected void setFieldToScalar(Object context, String value, ScalarUnmarshallingContext scalarUnmarshallingContext)
 	{
 		if ((value == null) /*|| (value.length() == 0) removed by Alex to allow empty delims*/)
 		{
 //			error("Can't set scalar field with empty String");
 			return;
 		}
 		if (setValueMethod != null && !isPseudoScalar())
 		{
 			// if the method is found, invoke the method
 			// fill the String value with the value of the attr node
 			// args is the array of objects containing arguments to the method to be invoked
 			// in our case, methods have only one arg: the value String
 			Object[] args = new Object[1];
 			args[0]		  = value;
 			try
 			{
 				setValueMethod.invoke(context, args); // run set method!
 			}
 			catch (InvocationTargetException e)
 			{
 				weird("couldnt run set method for " + tagName + " even though we found it");
 				e.printStackTrace();
 			}
 			catch (IllegalAccessException e)
 			{
 				weird("couldnt run set method for " + tagName + " even though we found it");
 				e.printStackTrace();
 			}	
 			catch (IllegalArgumentException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		else if (scalarType != null && !scalarType.isMarshallOnly())
 		{
 			scalarType.setField(context, field, value, format, null);
 		}
 	}
 
 	/**
 	 * Assume the first child of the leaf node is a text node.
 	 * Pull the text of out that text node. Trim it, and if necessary, unescape it.
 	 * 
 	 * @param leafNode	The leaf node with the text element value.
 	 * @return			Null if there's not really any text, or the useful text from the Node, if there is some.
 	 */
 	String getLeafNodeValue(Node leafNode)
 	{
 		String result	= null;
 		Node textElementChild			= leafNode.getFirstChild();
 		if (textElementChild != null)
 		{
 			if (textElementChild != null)
 			{
 				String textNodeValue	= textElementChild.getNodeValue();
 				if (textNodeValue != null)
 				{
 					textNodeValue		= textNodeValue.trim();
 					if (!isCDATA && (scalarType != null) && scalarType.needsEscaping())
 						textNodeValue	= XMLTools.unescapeXML(textNodeValue);
 					//debug("setting special text node " +childFieldName +"="+textNodeValue);
 					if (textNodeValue.length() > 0)
 					{
 						result			= textNodeValue;
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Add element derived from the Node to a Collection.
 	 * 
 	 * @param activeES		Contextualizing object that has the Collection slot we're adding to.
 	 * @param childLeafNode	XML leafNode that has the value we need to add, after type conversion.
 	 * 
 	 * @throws XMLTranslationException
 	 */
 	void addLeafNodeToCollection(ElementState activeES, Node childLeafNode)
 	throws XMLTranslationException
 	{
 		addLeafNodeToCollection(activeES, getLeafNodeValue(childLeafNode), null);
 	}
 	/**
 	 * Add element derived from the Node to a Collection.
 	 * 
 	 * @param activeES		Contextualizing object that has the Collection slot we're adding to.
 	 * @param scalarUnmarshallingContext TODO
 	 * @param childLeafNode	XML leafNode that has the value we need to add, after type conversion.
 	 * @throws XMLTranslationException
 	 */
 	void addLeafNodeToCollection(ElementState activeES, String leafNodeValue, ScalarUnmarshallingContext scalarUnmarshallingContext)
 	throws XMLTranslationException
 	{
 		if  (leafNodeValue != null)
 		{
 			// silently ignore null leaf node values
 		}
 		if (scalarType != null)
 		{
 			//TODO -- for performance reasons, should we call without format if format is null, and
 			// let the ScalarTypes that don't use format implement the 1 argument signature?!
 			Object typeConvertedValue		= scalarType.getInstance(leafNodeValue, format, scalarUnmarshallingContext);
 			try
 			{
 				//TODO -- should we be doing this check for null here??
 				if (typeConvertedValue != null)
 				{
 //					Collection collection	= (Collection) field.get(activeES);
 //					if (collection == null)
 //					{
 //						// well, why not create the collection object for them?!
 //						Collection thatCollection	= 
 //							ReflectionTools.getInstance((Class<Collection>) field.getType());
 //
 //					}
 					Collection<Object> collection	= (Collection<Object>) automaticLazyGetCollectionOrMap(activeES);
 					collection.add(typeConvertedValue);
 				}
 			} catch (Exception e)
 			{
 				throw fieldAccessException(typeConvertedValue, e);
 			}
 		}
 		else
 		{
 			reportFieldTypeError(leafNodeValue);
 		}
 	}
 	
 	private void reportFieldTypeError(String textNodeValue)
 	{
 		error("Can't set to " + textNodeValue + " because fieldType is unknown.");
 	}
 
 
 	/**
 	 * Generate an exception about problems accessing a field.
 	 * 
 	 * @param nestedElementState
 	 * @param e
 	 * @return
 	 */
 	private XMLTranslationException fieldAccessException(Object nestedElementState, Exception e)
 	{
 		return new XMLTranslationException(
 					"Unexpected Object / Field set problem. \n\t"+
 					"Field = " + field +"\n\ttrying to set to " + nestedElementState.getClass(), e);
 	}
 
 	/**
 	 * Use the Field of this to seek a Collection or Map object in the activeES.
 	 * If non-null, great -- return it.
 	 * <p/>
 	 * Otherwise, lazy evaluation.
 	 * Since the value of the field is null, use the Type of the Field to instantiate a newInstance.
 	 * Set the instance of the Field in activeES to this newInstance, and return it.
 	 * 
 	 * @param activeES
 	 * @return
 	 */
 	Object automaticLazyGetCollectionOrMap(ElementState activeES)
 	{
 		Object collection	= null;
 		try
 		{
 			collection		= field.get(activeES);
 			if (collection == null)
 			{
 				// initialize the collection for the caller! automatic lazy evaluation :-)
 				Class collectionType	= field.getType();
 				try
 				{
 					collection	= collectionType.newInstance();
 					// set the field to the new collection
 					field.set(activeES, collection);
 				} catch (InstantiationException e)
 				{
 					warning("Can't instantiate collection of type" + collectionType + " for field " + field.getName() + " in " + activeES);
 					e.printStackTrace();
 					// return
 				}
 			}
 		} catch (IllegalArgumentException e)
 		{
 			weird("Trying to addElementToCollection(). Can't access collection field " + field.getType() + " in " + activeES);
 			e.printStackTrace();
 			//return;
 		} catch (IllegalAccessException e)
 		{
 			weird("Trying to addElementToCollection(). Can't access collection field " + field.getType() + " in " + activeES);
 			e.printStackTrace();
 			//return;
 		}
 		return collection;
 	}
 		
 	/**
 	 * Based on the classOp in this, form a child element.
 	 * Set it's parent field and elementByIdMap.
 	 * Look-up Optimizations for it, using the parent's Optimizations as the scope.
 	 *
 	 * @param parent
 	 * @param tagName TODO
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 		ElementState constructChildElementState(ElementState parent, String tagName)
 		throws XMLTranslationException
 		{
 			ClassDescriptor childClassDescriptor= !isPolymorphic() ?
 				elementClassDescriptor : tagClassDescriptors.get(tagName);
 			ElementState result			= null;
 			if (childClassDescriptor != null)
 			{
 				result								= childClassDescriptor.getInstance();
 				if (result != null)
 					parent.setupChildElementState(result);
 			}
 			return result;
 		}
 		
 		void setFieldToNestedObject(ElementState context, Object nestedObject) 
 		throws XMLTranslationException
 		{
 			try
 			{
 				field.set(context, nestedObject);
 			}
 			catch (Exception e)
 			{
 				throw fieldAccessException(nestedObject, e);
 			}
 		}
 
 
 	
 	//----------------------------- constant instances ---------------------------------------//
 	FieldDescriptor(String tag)
 	{
 		this.tagName									= tag;
 		this.type											= IGNORED_ELEMENT;
 		this.field										= null;
 		this.declaringClassDescriptor	= null;
 	}
 	static final FieldDescriptor IGNORED_ELEMENT_FIELD_DESCRIPTOR;
 	static final FieldDescriptor ROOT_ELEMENT_FIELD_DESCRIPTOR;
 	static
 	{
 		IGNORED_ELEMENT_FIELD_DESCRIPTOR		= new FieldDescriptor("IGNORED");
 		
 		ROOT_ELEMENT_FIELD_DESCRIPTOR			= new FieldDescriptor("ROOT");
 		ROOT_ELEMENT_FIELD_DESCRIPTOR.type		= ROOT;
 	}
 
 	
 	//----------------------------- convenience methods ---------------------------------------//
 	
 	public String elementStart()
 	{
 		return isCollection() ? collectionOrMapTagName : tagName;
 	}
 	/**
 	 * Most fields derive their tag from Field name for marshaling. However, some, such as those
 	 * annotated with @xml_class, @xml_classes, @xml_scope, derive their tag from the class of an
 	 * instance. This includes all polymorphic fields.
 	 * 
 	 * @return	true if the tag name name is derived from the class name (
 	 * not the usual case, but needed for polymorphism).
 	 * 
 	 * else if the tag name is derived from the class name for @xml_nested
 	 * or, for @xml_collection and @xml_map), the tag name is derived from the annotation's value
 	 */
 	public boolean isPolymorphic()
 	{	
 		return (tagClassDescriptors != null) || (unresolvedScopeAnnotation != null);
 //		else return true;
 		//return tagClassDescriptors != null;
 	}
 	public String getCollectionOrMapTagName()
 	{
 		return collectionOrMapTagName;
 	}
 	
 	//FIXME -- these are temporary bullshit declarations which need to be turned into something real
 	public boolean hasXmlText()
 	{
 		return false;
 	}
 	public boolean isXmlNsDecl()
 	{
 		return false;
 	}
 	
 	/**
 	 * Used to describe scalar types used for serializing the type system, itself.
 	 * They cannot be unmarshalled in Java, only marshalled.
 	 * Code may be written to access their String representations in other languages.
 	 * 
 	 * @return	false for almost all ScalarTypes and for all element fields
 	 */
 	public boolean isMarshallOnly()
 	{
 		return scalarType != null && scalarType.isMarshallOnly();
 	}
 	
 
 	public FieldDescriptor getWrappedFD() 
 	{
 		return wrappedFD;
 	}
 	
 	public boolean belongsTo(ClassDescriptor c)
 	{
 		return this.field.getDeclaringClass() == c.getDescribedClass();
 	}
 	
 	String[] otherTags()
 	{
 		final ElementState.xml_other_tags otherTagsAnnotation = this.getField().getAnnotation(xml_other_tags.class);
 
 		//commented out since getAnnotation also includes inherited annotations 
 		//ElementState.xml_other_tags otherTagsAnnotation 	= thisClass.getAnnotation(ElementState.xml_other_tags.class);
 		if (otherTagsAnnotation == null)
 			return null;
 		String[] result	= otherTagsAnnotation.value();
 		return  result == null ? null : result.length == 0 ? null : result;
 	}
 	
 }
