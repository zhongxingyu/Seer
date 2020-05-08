 package ecologylab.xml;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.Collection;
 import java.util.Map;
 
 import org.w3c.dom.Node;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.xml.ElementState.xml_tag;
 import ecologylab.xml.types.element.Mappable;
 import ecologylab.xml.types.scalar.ScalarType;
 import ecologylab.xml.types.scalar.TypeRegistry;
 
 /**
  * Holds optimizations for translating from XML a single tag or attribute
  * within a contextualizing ElementState subclass.
  *
  * @author andruid
  */
 class ParseTableEntry extends Debug
 implements ParseTableEntryTypes
 {
 	private final String		tag;
 	
 	private boolean				isID;
 	
 	private int					type;
 	
 	private String				nameSpaceName;
 	
 	/**
 	 * The Field object that we should set. 
 	 * Usually this is defined in the context of the original ElementState object passed to
 	 * the constructor. It corresponds to the tag originally passed in.
 	 * However, in the case of an XML Namespace, it may be defined in the context of a nested field.
 	 */
 	private Field				field;
 	
 	private Method				setMethod;
 	
 	private ScalarType			scalarType;
 	
 	/**
 	 * true for LEAF_NODE_VALUE entries. Value not used for other types.
 	 */
 //	private	boolean				isCDATA;
 	
 	/**
 	 * Usually the class of the field corresponding to the tag in the context of the original ElementState 
 	 * object passed to the constructor. However, n the case of an XML Namespace, it may be the class
 	 * of a nested Namespace object.
 	 */
 	private Class				classOp;
 	
 	private TranslationSpace	translationSpace;
 	
 	private ParseTableEntry		nestedPTE;
 	
 	/**
 	 * 
 	 * @param translationSpace
 	 * @param optimizations
 	 * @param context
 	 * @param tag		Tag corresponding to the value in the XML that we're translating.
 	 * @param isAttribute TODO
 	 */
 	ParseTableEntry(TranslationSpace translationSpace, Optimizations optimizations, ElementState context, String tag, boolean isAttribute)
 	{
 		super();
 		this.tag				= tag;
 		this.translationSpace	= translationSpace;
 		
 		Class contextClass 		= context.getClass();
 		int colonIndex			= tag.indexOf(':');
 		
 		if (isAttribute)
 		{
 			if ("id".equals(tag))
 				isID			= true;
 			
 			if (colonIndex > -1)
 			{	//TODO -- make this do the right thing
 				this.type		= IGNORED_ATTRIBUTE;
 				return;
 			}
 			
 			setupScalarValue(tag, optimizations, contextClass, true);
 			return;
 		}
 		
 		// element, not attribute
 		if (colonIndex > 0)
 		{	// there is an XML namespace specified in the XML!
 			nameSpaceName		= tag.substring(0, colonIndex);
 			translationSpace	= TranslationSpace.get(nameSpaceName);
 			String subTag		= tag.substring(colonIndex+1);
 			// is there a field called nameSpaceName?
 			Field nameSpaceField= optimizations.getField(nameSpaceName);
 			if (nameSpaceField != null)
 			{	// o.k. we know we're working in the object of namespace fields
 				// create a dummy object to get its ParseTableEntry
 				Class nsFieldClass			= nameSpaceField.getType();
 				Object dummy				= ReflectionTools.getInstance(nsFieldClass);
 				if (dummy instanceof ElementState)
 				{
 					this.field				= nameSpaceField;
 					ElementState dummyES 	= (ElementState) dummy;
 					Optimizations nsOptimizations	= Optimizations.lookup(dummyES);
 					TranslationSpace nameSpaceTranslations	= TranslationSpace.get(nameSpaceName);
 					ParseTableEntry	 nsPTE	= nsOptimizations.parseTableEntry(nameSpaceTranslations, dummyES, subTag);
 					this.classOp			= nsFieldClass;
 					fillValues(nsPTE);
 					this.nestedPTE			= nsPTE;
 				}
 				else
 				{
 					println("ERROR: there is a field called " + nameSpaceName + " in " + contextClass +
 							" but it is not of type ElementState!");
 					this.type		= BAD_FIELD;
 					return;
 				}
 			}
 			
 		}
 		else
 		{	// no XML namespace; life is simpler.
 			
 			// try as leaf node
 			int diganosedType		= setupScalarValue(tag, optimizations, contextClass, false);
 			switch (diganosedType)
 			{
 			case LEAF_NODE_VALUE:
 				this.classOp		= contextClass;
 /*				ElementState.xml_leaf leafAnnotation		= field.getAnnotation(ElementState.xml_leaf.class);
 				if ((leafAnnotation != null) && (leafAnnotation.value() == ElementState.CDATA))
 						this.isCDATA= true;
  */				return;
 			case IGNORED_ELEMENT:    // this may be a temporary label -- not a leaf node or an attribute
 				if (this.field != null)
 				{
 					// this is actually a regular nested element
 					this.type	= REGULAR_NESTED_ELEMENT;
 //					this.classOp	= field.getType();
 					// changed 12/2/06 by andruid -- use the type in the TranslationSpace!
 					// ah, but that was wrong to do. you must get the class from the Field object,
 					// because the Field object, and not the class is supposed to be a source.
 					// but we need to support having 2 classes with the same simple name, in which one overrides the other,
 					// for internal vs external versions of APIs.
 					//
 					// thus, what we do is:
 					// 1) get the class name from the field
 					// 2) use it as a key into the TranslationSpace (seeking an override)
 					// 3) if that fails, then just use the Class from the field.
 					Class fieldClass			= field.getType();
 					Class classFromTS			= translationSpace.getClassBySimpleNameOfClass(fieldClass);
 					if (classFromTS == null)
 					{
 						this.classOp	= fieldClass;
 						//TODO - if warnings mode, warn user that class is not in the TranslationSpace
 					}
 					else// the way it should be :-)
 						this.classOp	= classFromTS;
 					return;
 				}
 				// no field object, so we must continue to check stuff out!
 				break;
 			default:
 				error("Unknown case in element type assignment switch " + diganosedType + ".");
 				return;
 			}
 
 			// else there is no Field to resolve. but there may be a class!
 			
 			// was collection declared explicitly?
 			Field collectionField	= optimizations.getCollectionFieldByTag(tag);
 			if (collectionField != null)
 			{
 				java.lang.reflect.Type[] typeArgs	= ReflectionTools.getParameterizedTypeTokens(collectionField);
 				if (typeArgs != null)
 				{
 					Class	collectionElementsType		= (Class) typeArgs[0];
 					debug("!!!collection elements are of type: " + collectionElementsType.getName());
 					this.classOp			= collectionElementsType;
 					this.field				= collectionField;
 					// is collectionElementsType a scalar or a nested element
 					if (ElementState.class.isAssignableFrom(collectionElementsType))
 					{	// nested element
 						this.type				= COLLECTION_ELEMENT;						
 					}
 					else
 					{	// scalar
 						this.type				= COLLECTION_SCALAR;
 						this.scalarType			= translationSpace.getType(collectionElementsType);
 					}
 				}
 				else
 				{
 					warning("Ignoring declaration @xml_collection(\"" + tag + 
 							  	 "\") because it is not annotating a parameterized generic Collection defined with a <type> token.");
 					this.type	= IGNORED_ELEMENT;
 					//!! remove entry from map !!
 				}
 			}
 			else
 			{
 				Field mapField	= optimizations.getMapFieldByTag(tag); 
 				if (mapField != null)
 				{
 					java.lang.reflect.Type[] typeArgs	= ReflectionTools.getParameterizedTypeTokens(mapField);
 					if (typeArgs != null)
 					{
 						Class	mapElementsType		= (Class) typeArgs[1];
 						debug("!!!map elements are of type: " + mapElementsType.getName());
 						this.classOp			= mapElementsType;
 						this.field				= mapField;
 						// is mapElementsType a scalar or a nested element
 						if (ElementState.class.isAssignableFrom(mapElementsType))
 						{	// nested element
 							this.type				= MAP_ELEMENT;						
 						}
 						else
 						{	// scalar
 							this.type				= MAP_SCALAR;
 							this.scalarType			= translationSpace.getType(mapElementsType);
 						}
 					}
 					else
 					{
 						warning("Ignoring declaration @xml_map(\"" + tag + 
 								  	 "\") because it is not annotating a parameterized generic Map defined with a <type> token.");
 						this.type	= IGNORED_ELEMENT;
 						//!! remove entry from map !!
 					}
 				}
 				else
 				{
 					Class classOp	= translationSpace.xmlTagToClass(tag);
 					if (classOp != null)
 					{
 						Map map					= context.getMap(classOp);
 						if (map != null)
 							this.type			= MAP_ELEMENT;
 						else
 						{
 							Collection collection = context.getCollection(classOp);
 							this.type	= (collection != null) ?
 									COLLECTION_ELEMENT : OTHER_NESTED_ELEMENT;
 						}
 						this.classOp= classOp;
 					}
 					else
 					{
 						context.debugA("WARNING - ignoring <" + tag() +"/>");
 						this.type	= IGNORED_ELEMENT;
 					}
 				}
 			}
 		}
 	}
 
 /**
  * Set-up PTE for scalar valued field (attribute or leaf node).
  * First look for a set method.
  * Then, look in the type registry.
  * Finally, if all else fails, get the list of @xml_name tags in the context class, and see if any match
  * Else, we must ignore the field.
  * 
  * @param tag
  * @param optimizations TODO
  * @param contextClass
  * @param isAttribute		true for attribute; false for leaf node.
  */
 	private int setupScalarValue(String tag, Optimizations optimizations, Class contextClass, boolean isAttribute)
 	{
 		int type			= UNSET_TYPE;
 		String methodName	= XmlTools.methodNameFromTagName(tag);
 		Method setMethod	= 
 			ReflectionTools.getMethod(contextClass, methodName, ElementState.MARSHALLING_PARAMS);
 		if (setMethod != null)
 		{
 			this.setMethod	= setMethod;
 			type			= isAttribute ? REGULAR_ATTRIBUTE : LEAF_NODE_VALUE;
 			// set method is custom code on a per field basis, and so doesnt need field object
 		}
 		else
 		{
 			String fieldName= XmlTools.fieldNameFromElementName(tag);
 			Field field		= optimizations.getField(fieldName);
             
             // TODO this might need to be done somewhere else...maybe another method
             if (field == null)
             { // we still haven't found the right one; have to check @xml_name in the context
                 for (Field contextField : contextClass.getDeclaredFields())
                 { // iterate through all the fields, and look for ones with xml_name's
                     if (contextField.isAnnotationPresent(xml_tag.class))
                     {
                         if (tag.equals(contextField.getAnnotation(xml_tag.class).value()))
                         { // if we found a matching xml_name, then that's our field!
                             field = contextField;
                             field.setAccessible(true);
                             break;
                         }
                     }
                 }
             }
             
 			if (field != null)
 			{
 				ScalarType fieldType		= TypeRegistry.getType(field);
 				if (fieldType != null)
 				{
 					this.scalarType	= fieldType;
 					type			= isAttribute ? REGULAR_ATTRIBUTE : LEAF_NODE_VALUE;
 					this.field		= field;
 				}
 				else if (!isAttribute)
 					this.field	= field; // for leaf node seekers that can be nested elements
 			}
 		}
 		if (type == UNSET_TYPE)
 		{
 			type					= isAttribute ? IGNORED_ATTRIBUTE : IGNORED_ELEMENT;
 			if (isAttribute)
 				error("no set method or type to set value for this tag in " + 
 						contextClass.getName() + ".");
 		}
 		this.type				= type;
 		return type;
 	}
 	
 	/**
 	 * Use this as the spec for obtaining a new ElementState (subclass),
 	 * springing forth from the parent, and based on the XML Node.
 	 * 
 	 * @param parent
 	 * @param node
 	 * @param useExistingTree TODO
 	 * @return
 	 * @throws XmlTranslationException
 	 */
 	ElementState createChildElement(ElementState parent, Node node, boolean useExistingTree)
 	throws XmlTranslationException
 	{
 		ElementState childElementState	= null;
 		boolean finished			= false;
 		if (useExistingTree)
 		{
 			try
 			{
 				childElementState	= (ElementState) field.get(parent);
 			} catch (Exception e)
 			{
 				throw fieldAccessException(parent, e);
 			}
 			if (childElementState != null)
 			{
 				childElementState.translateFromXML(node, classOp, translationSpace, true);
 				finished			= true;
 			}
 		}
 		if (!finished)
 		{
 			childElementState	= parent.getChildElementState(node, classOp, translationSpace);
 			parent.createChildHook(childElementState);
 		}
         childElementState.postTranslationProcessingHook();
 		return childElementState;
 	}
 	
 	/**
 	 * Use a set method or the type system to set our field in the context to the value.
 	 * 
 	 * @param context
 	 * @param value
 	 */
 	void setFieldToScalar(Object context, String value)
 	{
		if ((value == null) /*|| (value.length() == 0) removed by Alex to allow empty delims*/)
 		{
 			error("Can't set scalar field with empty String");
 			return;
 		}
 		if (setMethod != null)
 		{
 			// if the method is found, invoke the method
 			// fill the String value with the value of the attr node
 			// args is the array of objects containing arguments to the method to be invoked
 			// in our case, methods have only one arg: the value String
 			Object[] args = new Object[1];
 			args[0]		  = value;
 			try
 			{
 				setMethod.invoke(context, args); // run set method!
 			}
 			catch (InvocationTargetException e)
 			{
 				weird("couldnt run set method for " + tag +
 						  " even though we found it");
 				e.printStackTrace();
 			}
 			catch (IllegalAccessException e)
 			{
 				weird("couldnt run set method for " + tag +
 						  " even though we found it");
 				e.printStackTrace();
 			}	  
 			
 		}
 		else if (scalarType != null)
 		{
 			scalarType.setField(context, field, value);
 		}
 	}
 		
 	public String toString()
 	{
 		String tagString	= (tag == null) ? "NO_TAG?" : tag;
 		return super.toString() + "[" + tagString + "]";
 	}
 	/**
 	 * Set a scalar value using the textElementChild Node as the source,
 	 * the stateClass as the template for where the field is located, 
 	 * the childFieldName as the name of the field to select in the template,
 	 * and this as the object to do the set in.
 	 * 
 	 * @param context	The object in which we are setting the field's value.
 	 * @param leafNode	The leaf node with the text element value.
 	 */
 	void setScalarFieldWithLeafNode(Object context, Node leafNode)
 	{
 		String textNodeValue	= getLeafNodeValue(leafNode);
 		setFieldToScalar(context, textNodeValue);
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
 					if ((scalarType != null) && scalarType.needsEscaping())
 						textNodeValue	= XmlTools.unescapeXML(textNodeValue);
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
 	 * Used to set a field in this to a nested ElementState object.
 	 * 
 	 * his method is called during translateFromXML(...).
 	 * @param nestedElementState	the nested state-object to be added
 	 * @param childNode				XML doc subtree to use as the source of translation
 	 * @param useExistingTree		if true, re-fill in existing objects, instead of creating new ones.
 	 */
 	protected void setFieldToNestedElement(ElementState context, Node childNode, boolean useExistingTree)
 		throws XmlTranslationException
 	{
 		Object nestedElementState	= createChildElement(context, childNode, false);
 		if (!useExistingTree)
 		{
 			try
 			{
 				field.set(context, nestedElementState);
 			}
 			catch (Exception e)
 			{
 			   throw fieldAccessException(nestedElementState, e);
 			}
 		}
 	}
 
 	/**
 	 * Generate an exception about problems accessing a field.
 	 * 
 	 * @param nestedElementState
 	 * @param e
 	 * @return
 	 */
 	private XmlTranslationException fieldAccessException(Object nestedElementState, Exception e)
 	{
 		return new XmlTranslationException(
 					"Unexpected Object / Field set problem. \n\t"+
 					"Field = " + field +"\n\ttrying to set to " + nestedElementState.getClass(), e);
 	}
 	
 	/**
 	 * Add element derived from the Node to a Collection.
 	 * 
 	 * @param activeES
 	 * @param childNode
 	 * @throws XmlTranslationException
 	 */
 	void addElementToCollection(ElementState activeES, Node childNode)
 	throws XmlTranslationException
 	{
 		Collection collection	= null;
 		if (field != null)
 		{
 			try
 			{
 				collection		= (Collection) field.get(activeES);
 			} catch (Exception e)
 			{
 				weird("Trying to addElementToCollection(). Can't access collection field " + field.getType() + " in " + activeES);
 				e.printStackTrace();
 			}
 		}
 		else
 			collection		= activeES.getCollection(classOp());
 			
 		if (collection != null)
 		{
 			ElementState childElement = createChildElement(activeES, childNode, false);
 			collection.add(childElement);
 		}
 	}
 		
 	/**
 	 * Add element derived from the Node to a Collection.
 	 * 
 	 * @param activeES
 	 * @param childNode
 	 * @throws XmlTranslationException
 	 */
 	void addElementToMap(ElementState activeES, Node childNode)
 	throws XmlTranslationException
 	{
 		Map map		= null;
 		if (field != null)
 		{
 			try
 			{
 				map		= (Map) field.get(activeES);
 			} catch (Exception e)
 			{
 				weird("Trying to addElementToMap(). Can't access map field " + field.getType() + " in " + activeES);
 			}
 		}
 		else
 			map		= activeES.getMap(classOp());
 			
 		if (map != null)
 		{
 			Mappable mappable	= (Mappable) createChildElement(activeES, childNode, false);
 			map.put(mappable.key(), mappable);
 		}
 	}
 		
 	/**
 	 * Add element derived from the Node to a Collection.
 	 * 
 	 * @param activeES		Contextualizing object that has the Collection slot we're adding to.
 	 * @param childLeafNode	XML leafNode that has the value we need to add, after type conversion.
 	 * 
 	 * @throws XmlTranslationException
 	 */
 	void addLeafNodeToCollection(ElementState activeES, Node childLeafNode)
 	throws XmlTranslationException
 	{
 		if (scalarType != null)
 		{
 			String textNodeValue			= getLeafNodeValue(childLeafNode);
 			
 			Object typeConvertedValue		= scalarType.getInstance(textNodeValue);
 			try
 			{
 				//TODO -- should we be doing this check for null here??
 				if (typeConvertedValue != null)
 				{
 					Collection collection	= (Collection) field.get(activeES);
 					if (collection == null)
 					{
 						// well, why not create the collection object for them?!
 						Collection thatCollection	= 
 							ReflectionTools.getInstance((Class<Collection>) field.getType());
 
 					}
 					collection.add(typeConvertedValue);
 				}
 			} catch (Exception e)
 			{
 				throw fieldAccessException(typeConvertedValue, e);
 			}
 		}
 		else
 		{
 			Node textChild	= childLeafNode.getFirstChild();
 			Object desiredValue	= (textChild == null) ? childLeafNode : textChild.getNodeValue();
 			error("Can't set to " + desiredValue + " because fieldType is unknown.");
 		}
 	}
 			
 	private void fillValues(ParseTableEntry other)
 	{
 		//this.classOp			= other.classOp;
 		this.type				= other.type;
 		// i think this field could be gotten from here (the child) or be propagated from us
 		// no difference, cause the current Field has to appear in both TranslationSpaces!
 		this.translationSpace	= other.translationSpace;
 	}
 
 	/**
 	 * @return the Class operand that we need to work with in translation from XML.
 	 */
 	Class classOp()
 	{
 		return classOp;
 	}
 
 	/**
 	 * @return the field
 	 */
 	Field field()
 	{
 		return field;
 	}
 
 	/**
 	 * @return the nameSpaceName
 	 */
 	String nameSpaceName()
 	{
 		return nameSpaceName;
 	}
 
 	/**
 	 * @return the nestedNameSpaceParseTableEntry
 	 */
 	ParseTableEntry nestedPTE()
 	{
 		return nestedPTE;
 	}
 
 	/**
 	 * @return the type
 	 */
 	int type()
 	{
 		return type;
 	}
 
 	/**
 	 * @return the translationSpace
 	 */
 	TranslationSpace translationSpace()
 	{
 		return translationSpace;
 	}
 
 	/**
 	 * @return the tag
 	 */
 	String tag()
 	{
 		return tag;
 	}
 
 	/**
 	 * @return the isID
 	 */
 	boolean isID()
 	{
 		return isID;
 	}
 
 	/**
 	 * 
 	 * @return true if this entry is a leaf node.
 	 */
 	boolean isLeafNode()
 	{
 		return this.type == LEAF_NODE_VALUE;
 	}
 	/**
 	 * 
 	 * @return true if this entry is a collection of scalar valued leaf nodes.
 	 */
 	boolean isCollectionScalar()
 	{
 		return this.type == COLLECTION_SCALAR;
 	}
 	
 	/**
 	 * 
 	 * @return true if this entry is a collection of scalar valued leaf nodes.
 	 */
 	boolean isMapScalar()
 	{
 		return this.type == MAP_SCALAR;
 	}
 	
 	ScalarType scalarType()
 	{
 		return scalarType;
 	}
 }
