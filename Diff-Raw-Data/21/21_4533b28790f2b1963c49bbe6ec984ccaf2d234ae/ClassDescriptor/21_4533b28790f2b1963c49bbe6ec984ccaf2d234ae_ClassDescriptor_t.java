 package ecologylab.serialization;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.serialization.annotations.Hint;
 import ecologylab.serialization.annotations.bibtex_key;
 import ecologylab.serialization.annotations.bibtex_type;
 import ecologylab.serialization.annotations.simpl_collection;
 import ecologylab.serialization.annotations.simpl_composite;
 import ecologylab.serialization.annotations.simpl_descriptor_classes;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.annotations.simpl_map;
 import ecologylab.serialization.annotations.simpl_map_key_field;
 import ecologylab.serialization.annotations.simpl_nowrap;
 import ecologylab.serialization.annotations.simpl_other_tags;
 import ecologylab.serialization.annotations.simpl_scalar;
 import ecologylab.serialization.annotations.simpl_tag;
 import ecologylab.serialization.annotations.simpl_use_equals_equals;
 import ecologylab.serialization.formatenums.StringFormat;
 import ecologylab.serialization.types.CollectionType;
 import ecologylab.serialization.types.ScalarType;
 import ecologylab.serialization.types.TypeRegistry;
 import ecologylab.serialization.types.element.IMappable;
 
 /**
  * Cached object that holds all of the structures needed to optimize translations to and from XML
  * for a single subclass of ElementState. A rootOptimizationsMap keeps track of these, using the XML
  * tag as the key.
  * <p/>
  * This structure itself, as well as the structure within it, are created just in time, by lazy
  * evaluation.
  * 
  * @author andruid
  */
 @simpl_inherit
 public class ClassDescriptor<FD extends FieldDescriptor> extends DescriptorBase implements
 		FieldTypes, IMappable<String>, Iterable<FD>
 {
 
 	private static final String																												PACKAGE_CLASS_SEP										= ".";
 
 	/**
 	 * Class object that we are describing.
 	 */
 	@simpl_scalar
 	private Class<?>																																	describedClass;
 
 	@simpl_scalar
 	private String																																		describedClassSimpleName;
 
 	@simpl_scalar
 	private String																																		describedClassPackageName;
 
 	@simpl_composite
 	private ClassDescriptor<? extends FieldDescriptor>																superClass;
 
 	@simpl_collection("interface")
 	@simpl_other_tags("inerface")
 	// handle spelling error that was here
 	private ArrayList<String>																													interfaces;
 
 	/**
 	 * Class object that we are describing.
 	 */
 	@simpl_scalar
 	private Class<? extends ClassDescriptor>																					classDescriptorClass;
 
 	/**
 	 * Class object that we are describing.
 	 */
 	@simpl_scalar
 	private Class<? extends FieldDescriptor>																					fieldDescriptorClass;
 
 	/**
 	 * This is a pseudo FieldDescriptor object, defined for the class, for cases in which the tag for
 	 * the root element or a field is determined by class name, not by field name.
 	 */
 	private FieldDescriptor																														pseudoFieldDescriptor;
 
 	/**
 	 * This flag prevents loops when creating descriptors for type graphs.
 	 */
 	private boolean																																		isGetAndOrganizeComplete;
 
 	/**
 	 * Map of FieldToXMLOptimizations, with field names as keys.
 	 * 
 	 * Used to optimize translateToXML(). Also handy for providing functionality like associative
 	 * arrays in Perl, JavaScript, PHP, ..., but with less overhead, because the hashtable is only
 	 * maintained per class, not per instance.
 	 */
 	private HashMapArrayList<String, FD>																							fieldDescriptorsByFieldName					= new HashMapArrayList<String, FD>();
 
 	@simpl_nowrap
 	@simpl_map("field_descriptor")
 	@simpl_map_key_field("name")
 	private HashMapArrayList<String, FD>																							declaredFieldDescriptorsByFieldName	= new HashMapArrayList<String, FD>();
 
 	/**
 	 * This data structure is handy for translateFromXML(). There can be multiple tags (keys in this
 	 * map) for a single FieldDescriptor if @simpl_other_tags is used.
 	 */
 	private HashMap<String, FD>																												allFieldDescriptorsByTagNames				= new HashMap<String, FD>();
 
 	private HashMap<Integer, FD>																											allFieldDescriptorsByTLVIds					= new HashMap<Integer, FD>();
 
 	private FD																																				fieldDescriptorForBibTeXKey					= null;
 
 	private HashMap<String, FD>																												allFieldDescriptorsByBibTeXTag			= new HashMap<String, FD>();
 
 	private ArrayList<FD>																															attributeFieldDescriptors						= new ArrayList<FD>();
 
 	private ArrayList<FD>																															elementFieldDescriptors							= new ArrayList<FD>();																								;
 
 	private FD																																				scalarValueFieldDescripotor					= null;
 
 	/**
 	 * Global map of all ClassDescriptors. Key is the full, qualified name of the class ==
 	 * describedClassName.
 	 */
 	private static final HashMap<String, ClassDescriptor<? extends FieldDescriptor>>	globalClassDescriptorsMap						= new HashMap<String, ClassDescriptor<? extends FieldDescriptor>>();
 
 	private ArrayList<FD>																															unresolvedScopeAnnotationFDs;
 
 	private ArrayList<FD>																															unresolvedClassesAnnotationFDs;
 
 	private String																																		bibtexType													= "";
 
 	@simpl_collection("generic_type_variable")
 	private ArrayList<String>																													genericTypeVariables								= new ArrayList<String>();
 
 	/**
 	 * true if the class was annotated with @simpl_use_equals_equals, and thus that test will be used
 	 * during de/serialization to detect equivalent objects
 	 */
 	@simpl_scalar
 	private boolean																																		strictObjectGraphRequired						= false;
 
 	public Class<?>																																		fdClass;
 
 	@simpl_collection("generic_type_var")
 	private ArrayList<GenericTypeVar>																									genericTypeVars											= null;
 
 	@simpl_collection("super_class_generic_type_var")
 	private ArrayList<GenericTypeVar>																									superClassGenericTypeVars						= null;
 
 	static
 	{
 		TypeRegistry.init();
 	}
 
 	/**
 	 * Default constructor only for use by translateFromXML().
 	 */
 	public ClassDescriptor()
 	{
 		super();
 	}
 
 	/**
 	 * Constructor typically used by this class for creating a ClassDescriptor given its Java Class.
 	 * 
 	 * @param thatClass
 	 */
 	protected ClassDescriptor(Class<?> thatClass)
 	{
 		super(XMLTools.getXmlTagName(thatClass, SimplTypesScope.STATE), thatClass.getName());
 
 		this.describedClass = thatClass;
 		this.describedClassSimpleName = thatClass.getSimpleName();
 		this.describedClassPackageName = thatClass.getPackage().getName();
 
 		final simpl_descriptor_classes descriptorsClassesAnnotation = thatClass
 				.getAnnotation(simpl_descriptor_classes.class);
 		if (descriptorsClassesAnnotation != null)
 		{
 			classDescriptorClass = (Class<? extends ClassDescriptor>) descriptorsClassesAnnotation
 					.value()[0];
 			fieldDescriptorClass = (Class<? extends FieldDescriptor>) descriptorsClassesAnnotation
 					.value()[1];
 		}
 
 		if (thatClass.isAnnotationPresent(simpl_inherit.class))
 			this.superClass = getClassDescriptor(thatClass.getSuperclass());
 
 		addGenericTypeVariables();
 		if (javaParser != null)
 		{
 			comment = javaParser.getJavaDocComment(thatClass);
 		}
 		if (thatClass.isAnnotationPresent(simpl_use_equals_equals.class))
 		{
 			this.strictObjectGraphRequired = true;
 		}
 	}
 
 	/**
 	 * Constructor used by Meta-Metadata Compiler.
 	 * 
 	 * @param tagName
 	 * @param comment
 	 * @param describedClassPackageName
 	 * @param describedClassSimpleName
 	 * @param superClass
 	 * @param interfaces
 	 */
 	protected ClassDescriptor(String tagName, String comment, String describedClassPackageName,
 			String describedClassSimpleName, ClassDescriptor<FD> superClass, ArrayList<String> interfaces)
 	{
 		super(tagName, describedClassPackageName + PACKAGE_CLASS_SEP + describedClassSimpleName,
 				comment);
 
 		this.describedClassPackageName = describedClassPackageName;
 		this.describedClassSimpleName = describedClassSimpleName;
 		this.superClass = superClass;
 		this.interfaces = interfaces;
 	}
 
 	/**
 	 * Handles a text node.
 	 */
 	private FieldDescriptor	scalarTextFD;
 
	private Object SCOPE_ANNOTATION_LOCK = new Object();

 	public FieldDescriptor getScalarTextFD()
 	{
 		return scalarTextFD;
 	}
 
 	void setScalarTextFD(FieldDescriptor scalarTextFD)
 	{
 		this.scalarTextFD = scalarTextFD;
 	}
 
 	public boolean hasScalarFD()
 	{
 		return scalarTextFD != null;
 	}
 
 	public ArrayList<String> getInterfaceList()
 	{
 		return interfaces;
 	}
 
 	private void addGenericTypeVariables()
 	{
 		TypeVariable<?>[] typeVariables = describedClass.getTypeParameters();
 		if (typeVariables != null && typeVariables.length > 0)
 		{
 			for (TypeVariable<?> typeVariable : typeVariables)
 			{
 				String typeClassName = typeVariable.getName();
 				genericTypeVariables.add(typeClassName);
 			}
 		}
 	}
 
 	/**
 	 * lazy-evaluation method.
 	 * 
 	 * @return
 	 */
 	public ArrayList<GenericTypeVar> getGenericTypeVars()
 	{
 		if (genericTypeVars == null)
 		{
 			synchronized (this)
 			{
 				if (genericTypeVars == null)
 				{
 					genericTypeVars = new ArrayList<GenericTypeVar>();
 					deriveGenericTypeVariables();
 				}
 			}
 		}
 
 		return genericTypeVars;
 	}
 
 	/**
 	 * lazy-evaluation method.
 	 * 
 	 * @return
 	 */
 	public ArrayList<GenericTypeVar> getSuperClassGenericTypeVars()
 	{
 		if (superClassGenericTypeVars == null)
 		{
 			synchronized (this)
 			{
 				if (superClassGenericTypeVars == null)
 				{
 					superClassGenericTypeVars = new ArrayList<GenericTypeVar>();
 					deriveSuperGenericTypeVariables();
 				}
 			}
 		}
 
 		return superClassGenericTypeVars;
 	}
 
 	private void deriveSuperGenericTypeVariables()
 	{
 
 		Type superClassType = describedClass.getGenericSuperclass();
 
 		if (superClassType instanceof ParameterizedTypeImpl)
 		{
 			ParameterizedTypeImpl superClassParameterizedType = (ParameterizedTypeImpl) superClassType;
 			superClassGenericTypeVars = GenericTypeVar.getGenericTypeVars(superClassParameterizedType);
 		}
 	}
 
 	private void deriveGenericTypeVariables()
 	{
 		if (describedClass != null) // for generated descriptors, describedClass == null
 		{
 			TypeVariable<?>[] typeVariables = describedClass.getTypeParameters();
 			if (typeVariables != null && typeVariables.length > 0)
 			{
 				for (TypeVariable<?> typeVariable : typeVariables)
 				{
 					GenericTypeVar g = GenericTypeVar.getGenericTypeVar(typeVariable);
 					this.genericTypeVars.add(g);
 				}
 			}
 		}
 	}
 
 	@Deprecated
 	public ArrayList<String> getGenericTypeVariables()
 	{
 		return genericTypeVariables;
 	}
 
 	public String getTagName()
 	{
 		return tagName;
 	}
 
 	public String getBibtexType()
 	{
 		if (this.bibtexType == null || this.bibtexType.equals(""))
 		{
 			return tagName;
 		}
 		return bibtexType;
 	}
 
 	/**
 	 * Obtain Optimizations object in the global scope of root Optimizations. Uses just-in-time / lazy
 	 * evaluation. The first time this is called for a given ElementState class, it constructs a new
 	 * Optimizations saves it in our rootOptimizationsMap, and returns it.
 	 * <p/>
 	 * Subsequent calls merely pass back the already created object from the rootOptimizationsMap.
 	 * 
 	 * @param elementState
 	 *          An ElementState object that we're looking up Optimizations for.
 	 * @return
 	 */
 	public static ClassDescriptor<? extends FieldDescriptor> getClassDescriptor(Object object)
 	{
 		Class<? extends Object> thatClass = object.getClass();
 
 		return getClassDescriptor(thatClass);
 	}
 
 	static final Class<?>[]	CONSTRUCTOR_ARGS	=
 																						{ Class.class };
 
 	/**
 	 * Obtain Optimizations object in the global scope of root Optimizations. Uses just-in-time / lazy
 	 * evaluation. The first time this is called for a given ElementState class, it constructs a new
 	 * Optimizations saves it in our rootOptimizationsMap, and returns it.
 	 * <p/>
 	 * Subsequent calls merely pass back the already created object from the rootOptimizationsMap.
 	 * 
 	 * @param thatClass
 	 * @return
 	 */
 	public static ClassDescriptor<? extends FieldDescriptor> getClassDescriptor(Class<?> thatClass)
 	{
 		String className = thatClass.getName();
 		// stay out of the synchronized block most of the time
 		ClassDescriptor<? extends FieldDescriptor> result = globalClassDescriptorsMap.get(className);
 		if (result == null || !result.isGetAndOrganizeComplete)
 		{
 			// but still be thread safe!
 			synchronized (globalClassDescriptorsMap)
 			{
 				result = globalClassDescriptorsMap.get(className);
 				if (result == null)
 				{
 					final simpl_descriptor_classes descriptorsClassesAnnotation = thatClass
 							.getAnnotation(simpl_descriptor_classes.class);
 					if (descriptorsClassesAnnotation == null)
 						result = new ClassDescriptor<FieldDescriptor>(thatClass);
 					else
 					{
 						Class<?> aClass = descriptorsClassesAnnotation.value()[0];
 						Object[] args = new Object[1];
 						args[0] = thatClass;
 
 						result = (ClassDescriptor<? extends FieldDescriptor>) ReflectionTools.getInstance(
 								aClass, CONSTRUCTOR_ARGS, args);
 					}
 					globalClassDescriptorsMap.put(className, result);
 
 					// NB: this call was moved out of the constructor to avoid recursion problems
 					result.deriveAndOrganizeFieldsRecursive(thatClass);
 					result.isGetAndOrganizeComplete = true;
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Form a pseudo-FieldDescriptor-object for a root element. We say pseudo, because there is no
 	 * Field corresponding to this element. The pseudo-FieldDescriptor-object still guides the
 	 * translation process.
 	 * 
 	 * @return
 	 */
 	public FieldDescriptor pseudoFieldDescriptor()
 	{
 		FieldDescriptor result = pseudoFieldDescriptor;
 		if (result == null)
 		{
 			synchronized (this)
 			{
 				result = pseudoFieldDescriptor;
 				if (result == null)
 				{
 					result = new FieldDescriptor(this);
 					pseudoFieldDescriptor = result;
 				}
 			}
 		}
 		return result;
 	}
 
 	public ArrayList<FD> allFieldDescriptors()
 	{
 		ArrayList<FD> allFieldDescriptors = new ArrayList<FD>();
 		if (attributeFieldDescriptors != null)
 			allFieldDescriptors.addAll(attributeFieldDescriptors);
 		if (elementFieldDescriptors != null)
 			allFieldDescriptors.addAll(elementFieldDescriptors);
 		return allFieldDescriptors;
 	}
 
 	public ArrayList<FD> attributeFieldDescriptors()
 	{
 		return attributeFieldDescriptors;
 	}
 
 	public ArrayList<FD> elementFieldDescriptors()
 	{
 		return elementFieldDescriptors;
 	}
 
 	public FD getFieldDescriptorByTag(String tag, SimplTypesScope tScope, Object context)
 	{
 		if (unresolvedScopeAnnotationFDs != null)
 			resolveUnresolvedScopeAnnotationFDs();
 
 		if (unresolvedClassesAnnotationFDs != null)
 			resolveUnresolvedClassesAnnotationFDs();
 
 		return allFieldDescriptorsByTagNames.get(tag);
 	}
 
 	public FD getFieldDescriptorByTag(String tag, SimplTypesScope tScope)
 	{
 		return getFieldDescriptorByTag(tag, tScope, null);
 	}
 
 	public FD getFieldDescriptorByTLVId(int tlvId)
 	{
 		if (unresolvedScopeAnnotationFDs != null)
 			resolveUnresolvedScopeAnnotationFDs();
 
 		if (unresolvedClassesAnnotationFDs != null)
 			resolveUnresolvedClassesAnnotationFDs();
 
 		return allFieldDescriptorsByTLVIds.get(tlvId);
 	}
 
 	public FD getFieldDescriptorForBibTeXKey()
 	{
 		return fieldDescriptorForBibTeXKey;
 	}
 
 	public FD getFieldDescriptorByBibTeXTag(String bibTeXTag)
 	{
 		return allFieldDescriptorsByBibTeXTag.get(bibTeXTag);
 	}
 
 	public FieldDescriptor getFieldDescriptorByFieldName(String fieldName)
 	{
 		return fieldDescriptorsByFieldName.get(fieldName);
 	}
 
 	public Iterator<FD> iterator()
 	{
 		return fieldDescriptorsByFieldName.iterator();
 	}
 
 	/**
 	 * Build and return an ArrayList with Field objects for all the annotated fields in this class.
 	 * 
 	 * @param fieldDescriptorClass
 	 *          The Class to use for instantiating each FieldDescriptor. The default is
 	 *          FieldDescriptor, but class objects may be passed in that extend that class.
 	 * 
 	 * @return HashMapArrayList of Field objects, using the XML tag name for each field (not its Java
 	 *         field name!) as the keys. Could be empty. Never null.
 	 */
 
 	private Class<FieldDescriptor> fieldDescriptorAnnotationValue(Class<? extends Object> thatClass)
 	{
 		final simpl_descriptor_classes fieldDescriptorsClassAnnotation = thatClass
 				.getAnnotation(simpl_descriptor_classes.class);
 		Class<FieldDescriptor> result = null;
 		if (fieldDescriptorsClassAnnotation != null)
 		{
 			Class<?> annotatedFieldDescriptorClass = fieldDescriptorsClassAnnotation.value()[1];
 			if (annotatedFieldDescriptorClass != null
 					&& FieldDescriptor.class.isAssignableFrom(annotatedFieldDescriptorClass))
 				result = (Class<FieldDescriptor>) annotatedFieldDescriptorClass;
 		}
 		return result;
 	}
 
 	/**
 	 * Recursive method to create optimized data structures needed for translation to and from XML,
 	 * and also for efficient reflection-based access to field (descriptors) at run-time, with field
 	 * name as a variable.
 	 * <p/>
 	 * Recurses up the chain of inherited Java classes, when @xml_inherit is specified.
 	 * 
 	 * @param fdc
 	 * @return
 	 */
 	private synchronized void deriveAndOrganizeFieldsRecursive(Class<? extends Object> classWithFields)
 	{
 
 		if (classWithFields.isAnnotationPresent(simpl_inherit.class))
 		{
 			ClassDescriptor<FD> superClassDescriptor = (ClassDescriptor<FD>) ClassDescriptor
 					.getClassDescriptor(classWithFields.getSuperclass());
 
 			referFieldDescriptorsFrom(superClassDescriptor);
 		}
 
 		// if (classWithFields.isAnnotationPresent(simpl_inherit.class))
 		// { // recurse on super class first, so subclass declarations shadow those in superclasses,
 		//
 		// // there are field name conflicts
 		// Class<?> superClass = classWithFields.getSuperclass();
 		//
 		// if (superClass != null)
 		// {
 		// deriveAndOrganizeFieldsRecursive(superClass);
 		//
 		// }
 		// }
 
 		if (classWithFields.isAnnotationPresent(bibtex_type.class))
 		{
 			bibtex_type bibtexTypeAnnotation = classWithFields.getAnnotation(bibtex_type.class);
 			bibtexType = bibtexTypeAnnotation.value();
 		}
 
 		Field[] fields = classWithFields.getDeclaredFields();
 
 		for (int i = 0; i < fields.length; i++)
 		{
 			Field thatField = fields[i];
 
 			// skip static fields, since we're saving instances,
 			// and inclusion w each instance would be redundant.
 			if ((thatField.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
 			{
 				// debug("Skipping " + thatField + " because its static!");
 				continue;
 			}
 			int fieldType = UNSET_TYPE;
 
 			if (XMLTools.isScalar(thatField))
 			{
 				fieldType = SCALAR;
 			}
 			else if (XMLTools.representAsComposite(thatField))
 			{
 				fieldType = COMPOSITE_ELEMENT;
 			}
 			else if (XMLTools.representAsCollection(thatField))
 			{
 				fieldType = COLLECTION_ELEMENT;
 			}
 			else if (XMLTools.representAsMap(thatField))
 			{
 				fieldType = MAP_ELEMENT;
 			}
 			if (fieldType == UNSET_TYPE)
 				continue; // not a simpl serialization annotated field
 
 			FD fieldDescriptor = newFieldDescriptor(thatField, fieldType,
 					(Class<FD>) fieldDescriptorClass);
 			// create indexes for serialize
 			if (fieldDescriptor.getType() == SCALAR)
 			{
 				Hint xmlHint = fieldDescriptor.getXmlHint();
 				switch (xmlHint)
 				{
 				case XML_ATTRIBUTE:
 					attributeFieldDescriptors.add(fieldDescriptor);
 					break;
 				case XML_TEXT:
 				case XML_TEXT_CDATA:
 					break;
 				case XML_LEAF:
 				case XML_LEAF_CDATA:
 					elementFieldDescriptors.add(fieldDescriptor);
 					break;
 				}
 			}
 			else
 				elementFieldDescriptors.add(fieldDescriptor);
 
 			if (XMLTools.isCompositeAsScalarvalue(thatField))
 			{
 				scalarValueFieldDescripotor = fieldDescriptor;
 			}
 
 			// generate a warning message if a mapping is being overridden
 			fieldDescriptorsByFieldName.put(thatField.getName(), fieldDescriptor);
 			if (classWithFields == describedClass)
 			{
 				declaredFieldDescriptorsByFieldName.put(thatField.getName(), fieldDescriptor);
 			}
 
 			if (fieldDescriptor.isMarshallOnly())
 				continue; // not translated from XML, so don't add those mappings
 
 			// find the field descriptor for bibtex citation key
 			bibtex_key keyAnnotation = thatField.getAnnotation(bibtex_key.class);
 			if (keyAnnotation != null)
 				fieldDescriptorForBibTeXKey = fieldDescriptor;
 
 			// create mappings for translateFromBibTeX() --> allFieldDescriptorsByBibTeXTag
 			final String bibTeXTag = fieldDescriptor.getBibtexTagName();
 			allFieldDescriptorsByBibTeXTag.put(bibTeXTag, fieldDescriptor);
 
 			// create mappings for translateFromXML() --> allFieldDescriptorsByTagNames
 			final String fieldTagName = fieldDescriptor.getTagName();
 			if (fieldDescriptor.isWrapped())
 			{
 				FD wrapper = newFieldDescriptor(fieldDescriptor, fieldTagName,
 						(Class<FD>) fieldDescriptorClass);
 				mapTagToFdForDeserialize(fieldTagName, wrapper);
 				mapOtherTagsToFdForDeserialize(wrapper, fieldDescriptor.otherTags());
 			}
 			else if (!fieldDescriptor.isPolymorphic()) // tag(s) from field, not from class :-)
 			{
 				String tag = fieldDescriptor.isCollection() ? fieldDescriptor.getCollectionOrMapTagName()
 						: fieldTagName;
 				mapTagToFdForDeserialize(tag, fieldDescriptor);
 				mapOtherTagsToFdForDeserialize(fieldDescriptor, fieldDescriptor.otherTags());
 			}
 			else
 			{
 				mapPolymorphicClassDescriptors(fieldDescriptor);
 			}
 			thatField.setAccessible(true); // else -- ignore non-annotated fields
 		} // end for all fields
 
 	}
 
 	private void referFieldDescriptorsFrom(ClassDescriptor<FD> superClassDescriptor)
 	{
 		for (Entry<String, FD> fieldDescriptorEntry : superClassDescriptor
 				.getFieldDescriptorsByFieldName().entrySet())
 		{
 			fieldDescriptorsByFieldName.put(fieldDescriptorEntry.getKey(), fieldDescriptorEntry
 					.getValue());
 		}
 
 		for (Entry<String, FD> fieldDescriptorEntry : superClassDescriptor
 				.getDeclaredFieldDescriptorsByFieldName().entrySet())
 		{
 			declaredFieldDescriptorsByFieldName.put(fieldDescriptorEntry.getKey(), fieldDescriptorEntry
 					.getValue());
 		}
 
 		for (Entry<String, FD> fieldDescriptorEntry : superClassDescriptor
 				.getAllFieldDescriptorsByTagNames().entrySet())
 		{
 			allFieldDescriptorsByTagNames.put(fieldDescriptorEntry.getKey(), fieldDescriptorEntry
 					.getValue());
 		}
 
 		for (Entry<Integer, FD> fieldDescriptorEntry : superClassDescriptor
 				.getAllFieldDescriptorsByTLVIds().entrySet())
 		{
 			allFieldDescriptorsByTLVIds.put(fieldDescriptorEntry.getKey(), fieldDescriptorEntry
 					.getValue());
 		}
 
 		for (Entry<String, FD> fieldDescriptorEntry : superClassDescriptor
 				.getAllFieldDescriptorsByBibTeXTag().entrySet())
 		{
 			allFieldDescriptorsByBibTeXTag.put(fieldDescriptorEntry.getKey(), fieldDescriptorEntry
 					.getValue());
 		}
 
 		for (FD fieldDescriptor : superClassDescriptor.attributeFieldDescriptors())
 		{
 			attributeFieldDescriptors.add(fieldDescriptor);
 		}
 
 		for (FD fieldDescriptor : superClassDescriptor.elementFieldDescriptors())
 		{
 			elementFieldDescriptors.add(fieldDescriptor);
 		}
 
 		if (superClassDescriptor.getUnresolvedScopeAnnotationFDs() != null)
 		{
 			for (FD fd : superClassDescriptor.getUnresolvedScopeAnnotationFDs())
 			{
 				this.registerUnresolvedScopeAnnotationFD(fd);
 			}
 		}
 
 		if (superClassDescriptor.getUnresolvedClassesAnnotationFDs() != null)
 		{
 			for (FD fd : superClassDescriptor.getUnresolvedClassesAnnotationFDs())
 			{
 				this.registerUnresolvedClassesAnnotationFD(fd);
 			}
 		}
 	}
 
 	protected void mapOtherTagsToFdForDeserialize(FD fieldDescriptor, ArrayList<String> otherTags)
 	{
 		if (otherTags != null)
 		{
 			for (String otherTag : otherTags)
 				mapTagToFdForDeserialize(otherTag, fieldDescriptor);
 		}
 	}
 
 	/**
 	 * @param fieldDescriptor
 	 */
 	void mapPolymorphicClassDescriptors(FD fieldDescriptor)
 	{
 		Collection<String> tagClassDescriptors = fieldDescriptor.getPolymorphicTags();
 
 		if (tagClassDescriptors != null)
 			for (String tagName : tagClassDescriptors)
 			{
 				mapTagToFdForDeserialize(tagName, fieldDescriptor);
 			}
 
 		mapTagToFdForDeserialize(fieldDescriptor.getTagName(), fieldDescriptor);
 	}
 
 	static final Class[]	FIELD_DESCRIPTOR_ARGS	=
 																							{ ClassDescriptor.class, Field.class, int.class };
 
 	/**
 	 * @param thatField
 	 * @param fieldDescriptorClass
 	 * @return
 	 */
 	private FD newFieldDescriptor(Field thatField, int annotationType, Class<FD> fieldDescriptorClass)
 	{
 		if (fieldDescriptorClass == null)
 			return (FD) new FieldDescriptor(this, thatField, annotationType);
 
 		Object args[] = new Object[3];
 		args[0] = this;
 		args[1] = thatField;
 		args[2] = annotationType;
 
 		return (FD) ReflectionTools.getInstance(fieldDescriptorClass, FIELD_DESCRIPTOR_ARGS, args);
 	}
 
 	static final Class[]	WRAPPER_FIELD_DESCRIPTOR_ARGS	=
 																											{ ClassDescriptor.class,
 			FieldDescriptor.class, String.class						};
 
 	private FD newFieldDescriptor(FD wrappedFD, String wrapperTag, Class<FD> fieldDescriptorClass)
 	{
 		if (fieldDescriptorClass == null)
 			return (FD) new FieldDescriptor(this, wrappedFD, wrapperTag);
 
 		Object args[] = new Object[3];
 		args[0] = this;
 		args[1] = wrappedFD;
 		args[2] = wrapperTag;
 
 		return (FD) ReflectionTools.getInstance(fieldDescriptorClass, WRAPPER_FIELD_DESCRIPTOR_ARGS,
 				args);
 	}
 
 	/**
 	 * Map the tag to the FieldDescriptor for use in translateFromXML() for elements of this class
 	 * type.
 	 * 
 	 * @param tagName
 	 * @param fdToMap
 	 */
 	private void mapTagToFdForDeserialize(String tagName, FD fdToMap)
 	{
 		if (!fdToMap.isWrapped())
 		{
 			FD previousMapping = allFieldDescriptorsByTagNames.put(tagName, fdToMap);
 			allFieldDescriptorsByTLVIds.put(tagName.hashCode(), fdToMap);
 			if (previousMapping != null && previousMapping != fdToMap)
 				warning(" tag <" + tagName + ">:\tfield[" + fdToMap.getName() + "] overrides field["
 						+ previousMapping.getName() + "]");
 		}
 	}
 
 	/**
 	 * Add an entry to our map of Field objects, using the field's name as the key. Used, for example,
 	 * for ignored fields.
 	 * 
 	 * @param fieldDescriptor
 	 */
 	void addFieldDescriptorMapping(FD fieldDescriptor)
 	{
 		String tagName = fieldDescriptor.getTagName();
 		if (tagName != null)
 			mapTagToFdForDeserialize(tagName, fieldDescriptor);
 	}
 
 	/**
 	 * (used by the compiler)
 	 * 
 	 * @param fieldDescriptor
 	 */
 	protected void addFieldDescriptor(FD fieldDescriptor)
 	{
 		declaredFieldDescriptorsByFieldName.put(fieldDescriptor.getName(), fieldDescriptor);
 	}
 
 	public String toString()
 	{
 		return getClassSimpleName() + "[" + this.name + "]";
 	}
 
 	public Class<?> getDescribedClass()
 	{
 		return describedClass;
 	}
 
 	/**
 	 * 
 	 * @return true if this is an empty entry, for a tag that we do not parse. No class is associated
 	 *         with such an entry.
 	 */
 	public boolean isEmpty()
 	{
 		return describedClass == null;
 	}
 
 	public String getDescribedClassSimpleName()
 	{
 		return describedClassSimpleName;
 	}
 
 	public String getDescribedClassPackageName()
 	{
 		return describedClassPackageName;
 	}
 
 	/**
 	 * Get the full name of the class that this describes. Use the Class to get this, if there is one;
 	 * else use de/serialize fields that describe this.
 	 * 
 	 * @return
 	 */
 	public String getDescribedClassName()
 	{
 		return describedClass != null ? describedClass.getName() : describedClassPackageName + "."
 				+ describedClassSimpleName;
 	}
 
 	/**
 	 * @return The full, qualified name of the class that this describes.
 	 */
 	@Override
 	public String getJavaTypeName()
 	{
 		return getDescribedClassName();
 	}
 
 	@Override
 	public String getCSharpTypeName()
 	{
 		return getDescribedClassName();
 	}
 
 	@Override
 	public String getCSharpNamespace()
 	{
 		String csTypeName = this.getCSharpTypeName();
 		if (csTypeName != null)
 		{
 			int pos = csTypeName.lastIndexOf('.');
 			return pos > 0 ? csTypeName.substring(0, pos) : CSHARP_PRIMITIVE_NAMESPACE;
 		}
 		else
 			return null;
 	}
 
 	@Override
 	public String getObjectiveCTypeName()
 	{
 		return this.getDescribedClassSimpleName();
 	}
 
 	@Override
 	public String getDbTypeName()
 	{
 		return null;
 	}
 
 	public Object getInstance() throws SIMPLTranslationException
 	{
 		return XMLTools.getInstance(describedClass);
 	}
 
 	public int numFields()
 	{
 		return allFieldDescriptorsByTagNames.size();
 	}
 
 	/**
 	 * The tagName.
 	 */
 	public String key()
 	{
 		return tagName;
 	}
 
 	public HashMapArrayList<String, FD> getFieldDescriptorsByFieldName()
 	{
 		return fieldDescriptorsByFieldName;
 	}
 
 	public HashMapArrayList<String, FD> getDeclaredFieldDescriptorsByFieldName()
 	{
 		return declaredFieldDescriptorsByFieldName;
 	}
 
 	public HashMap<String, FD> getAllFieldDescriptorsByTagNames()
 	{
 		return allFieldDescriptorsByTagNames;
 	}
 
 	public HashMap<Integer, FD> getAllFieldDescriptorsByTLVIds()
 	{
 		return allFieldDescriptorsByTLVIds;
 	}
 
 	public HashMap<String, FD> getAllFieldDescriptorsByBibTeXTag()
 	{
 		return allFieldDescriptorsByBibTeXTag;
 	}
 
 	public ArrayList<FD> getUnresolvedScopeAnnotationFDs()
 	{
 		return this.unresolvedScopeAnnotationFDs;
 	}
 
 	public ArrayList<FD> getUnresolvedClassesAnnotationFDs()
 	{
 		return this.unresolvedClassesAnnotationFDs;
 	}
 
 	public String getSuperClassName()
 	{
 		return XMLTools.getClassSimpleName(describedClass.getSuperclass());
 	}
 
 	public static void main(String[] s)
 	{
 		SimplTypesScope mostBasicTranslations = SimplTypesScope.get("most_basic",
 				ClassDescriptor.class, FieldDescriptor.class, SimplTypesScope.class);
 
 		try
 		{
 			SimplTypesScope.serialize(mostBasicTranslations, System.out, StringFormat.XML);
 		}
 		catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Keep track of any FieldDescriptors with unresolved @serial_scope declarations so we can try to
 	 * resolve them later when there is use.
 	 * 
 	 * @param fd
 	 */
 	void registerUnresolvedScopeAnnotationFD(FD fd)
 	{
 		if (unresolvedScopeAnnotationFDs == null)
 		{
 			synchronized (this)
 			{
 				if (unresolvedScopeAnnotationFDs == null)
 					unresolvedScopeAnnotationFDs = new ArrayList<FD>();
 			}
 		}
 		unresolvedScopeAnnotationFDs.add(fd);
 	}
 
 	void registerUnresolvedClassesAnnotationFD(FD fd)
 	{
 		if (unresolvedClassesAnnotationFDs == null)
 		{
 			synchronized (this)
 			{
 				if (unresolvedClassesAnnotationFDs == null)
 					unresolvedClassesAnnotationFDs = new ArrayList<FD>();
 			}
 		}
 		unresolvedClassesAnnotationFDs.add(fd);
 	}
 
 	/**
 	 * Late evaluation of @serial_scope, if it failed the first time around.
 	 */
 	public void resolvePolymorphicAnnotations()
 	{
 		resolveUnresolvedScopeAnnotationFDs();
 		resolveUnresolvedClassesAnnotationFDs();
 	}
 
 	public void resolveUnresolvedScopeAnnotationFDs()
 	{
 		if (unresolvedScopeAnnotationFDs != null)
 		{
			synchronized (SCOPE_ANNOTATION_LOCK )
 			{
				if (unresolvedScopeAnnotationFDs != null)
				{
					for (int i = unresolvedScopeAnnotationFDs.size() - 1; i >= 0; i--)
					{
						FieldDescriptor fd = unresolvedScopeAnnotationFDs.remove(i);
						fd.resolveUnresolvedScopeAnnotation();
						this.mapPolymorphicClassDescriptors((FD) fd);
					}
					unresolvedScopeAnnotationFDs = null;
				}
 			}
 		}
 	}
 
 	/**
 	 * Late evaluation of @serial_scope, if it failed the first time around.
 	 */
 	public void resolveUnresolvedClassesAnnotationFDs()
 	{
 		if (unresolvedClassesAnnotationFDs != null)
 		{
 			for (int i = unresolvedClassesAnnotationFDs.size() - 1; i >= 0; i--)
 			{
 				FieldDescriptor fd = unresolvedClassesAnnotationFDs.remove(i);
 				fd.resolveUnresolvedClassesAnnotation();
 				this.mapPolymorphicClassDescriptors((FD) fd);
 			}
 		}
 		unresolvedClassesAnnotationFDs = null;
 	}
 
 	/**
 	 * Use the @simpl_other_tags annotation to obtain an array of alternative (old) tags for this
 	 * class.
 	 * 
 	 * @return The array of old tags, or null, if there is no @simpl_other_tags annotation.
 	 */
 	public ArrayList<String> otherTags()
 	{
 		ArrayList<String> result = this.otherTags;
 		if (result == null)
 		{
 			result = new ArrayList<String>();
 
 			Class<?> thisClass = getDescribedClass();
 			if (thisClass != null)
 			{
 				final simpl_other_tags otherTagsAnnotation = thisClass
 						.getAnnotation(simpl_other_tags.class);
 
 				// commented out since getAnnotation also includes inherited annotations
 				// ElementState.xml_other_tags otherTagsAnnotation =
 				// thisClass.getAnnotation(ElementState.xml_other_tags.class);
 				if (otherTagsAnnotation != null)
 					for (String otherTag : otherTagsAnnotation.value())
 						result.add(otherTag);
 			}
 
 			this.otherTags = result;
 		}
 		return result;
 	}
 
 	public FD getScalarValueFieldDescripotor()
 	{
 		return scalarValueFieldDescripotor;
 	}
 
 	public ClassDescriptor<? extends FieldDescriptor> getSuperClass()
 	{
 		return superClass;
 	}
 
 	/**
 	 * method returns whether a strict object graph is required
 	 * 
 	 * @return true if the class was annotated with @simpl_use_equals_equals, and thus that test will
 	 *         be used during de/serialization to detect equivalent objects
 	 */
 	public boolean getStrictObjectGraphRequired()
 	{
 		return this.strictObjectGraphRequired;
 	}
 
 	/**
 	 * Find all the Collection fields in this. Assemble a Set of them, in order to generate import
 	 * statements.
 	 * 
 	 * @return
 	 */
 	public Set<CollectionType> deriveCollectionDependencies()
 	{
 		HashSet<CollectionType> result = new HashSet<CollectionType>();
 		for (FieldDescriptor fd : declaredFieldDescriptorsByFieldName)
 		{
 			if (fd.isCollection())
 				result.add(fd.getCollectionType());
 		}
 		return result;
 	}
 
 	/**
 	 * Find all the Composite fields in this. Assemble a Set of them, in order to generate import
 	 * statements.
 	 * 
 	 * @return
 	 */
 	public Set<ClassDescriptor> deriveCompositeDependencies()
 	{
 		HashSet<ClassDescriptor> result = new HashSet<ClassDescriptor>();
 		for (FieldDescriptor fd : declaredFieldDescriptorsByFieldName)
 		{
 			if (fd.isNested() || (fd.isCollection()))
 			{
 				ClassDescriptor elementClassDescriptor = fd.getElementClassDescriptor();
 				if (elementClassDescriptor != null
 						&& TypeRegistry.getScalarTypeByName(elementClassDescriptor.getDescribedClassName()) == null)
 					result.add(elementClassDescriptor);
 
 				Collection<ClassDescriptor> polyClassDescriptors = fd.getPolymorphicClassDescriptors();
 				if (polyClassDescriptors != null)
 					for (ClassDescriptor polyCd : polyClassDescriptors)
 						result.add(polyCd);
 			}
 		}
 		if (superClass != null)
 		{
 			result.add(superClass);
 		}
 		return result;
 	}
 
 	/**
 	 * Find all the Scalar fields in this. Assemble a Set of them, in order to generate import
 	 * statements.
 	 * 
 	 * @return
 	 */
 	public Set<ScalarType> deriveScalarDependencies()
 	{
 		HashSet<ScalarType> result = new HashSet<ScalarType>();
 		for (FieldDescriptor fd : declaredFieldDescriptorsByFieldName)
 		{
 			if (fd.isScalar())
 			{
 				ScalarType<?> scalarType = fd.getScalarType();
 				if (!scalarType.isPrimitive())
 				{
 					result.add(scalarType);
 					ScalarType<?> operativeScalarType = scalarType.operativeScalarType();
 					if (!scalarType.equals(operativeScalarType))
 						result.add(operativeScalarType);
 				}
 			}
 		}
 		/*
 		 * for (String genericTypeName: genericTypeVariables) { ScalarType scalarType =
 		 * TypeRegistry.getType(genericTypeName); if (scalarType != null) result.add(scalarType); }
 		 */
 		return result;
 	}
 
 	@Override
 	public void deserializationPreHook(TranslationContext translationContext)
 	{
 		synchronized (globalClassDescriptorsMap)
 		{
 			String name = this.getName();
 			if (name != null)
 			{
 				if (globalClassDescriptorsMap.containsKey(name))
 					error("Already a ClassDescriptor for " + name);
 				else
 				{
 					globalClassDescriptorsMap.put(name, this);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return The list of meta-information (annotations, attributes, etc.) for this class.
 	 */
 	public List<MetaInformation> getMetaInformation()
 	{
 		if (metaInfo == null)
 		{
 			metaInfo = new ArrayList<MetaInformation>();
 
 			// @simpl_inherit
 			if (superClass != null)
 				metaInfo.add(new MetaInformation(simpl_inherit.class));
 
 			// @simpl_tag
 			String autoTagName = XMLTools.getXmlTagName(getDescribedClassSimpleName(), null);
 			if (tagName != null && !tagName.equals("") && !tagName.equals(autoTagName))
 				metaInfo.add(new MetaInformation(simpl_tag.class, false, tagName));
 
 			// @simpl_other_tags
 			ArrayList<String> otherTags = otherTags();
 			if (otherTags != null && otherTags.size() > 0)
 				metaInfo.add(new MetaInformation(simpl_other_tags.class, true, otherTags.toArray()));
 		}
 		return metaInfo;
 	}
 
 }
