package xml;
 
 import java.lang.reflect.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.*;
 import org.xml.sax.*;
 
 import ecologylab.types.*;
 
 /**
  * This class is the heart of the <code>ecologylab.xml</code>
  * translation framework.
  * 
  * <p/>
  * To use the framework, the programmer must define a tree of objects derived
  * from this class. The public fields in each of these derived objects 
  * correspond to the XML DOM. The declarations of attribute fields  must 
  * preceed thos for nested XML elements. Attributes are built directly from
  * Strings, using classes derived from
  * @link ecologylab.types.Type ecologylab.types.Type}.
  *
  * <p/>
  * The framework proceeds automatically through the application of rules.
  * In the standard case, the rules are based on the automatic mapping of
  * XML element names (aka tags), to ElementState class names.
  * An mechanism for supplying additional translations may also be provided.
  * 
  * <p/>
  * <code>ElementState</code> is based on 2 methods, each of which employs 
  * Java reflection and recursive descent.
  * 
  * <li><code>translateToXML(...)</code> translates a tree of these 
  * <code>ElementState</code> objects into XML.</li>
  *
  * <li><code>translateFromXML(...)</code> translates an XML DOM into a tree of these
  * <code>ElementState</code> objects</li>
  *  
  * @author      Andruid Kerne
  * @author      Madhur Khandelwal
  * @version     0.9
  */
 public class ElementState extends IO
 {
 	/**
 	 * xml header
 	 */
 	static protected final String XML_FILE_HEADER = "<?xml version=" + "\"1.0\"" + " encoding=" + "\"US-ASCII\"" + "?>";
 	
 	/**
 	 * whether the generated XML should be in compressed form or not
 	 */
 	protected static boolean compressed = false;
 
 	static final int TOP_LEVEL_NODE		= 1;
 	
 	/**
 	 * package name of the class
 	 */ 
 	protected String	packageName;
 	
 	/**
 	 * controls if the public fields of a parent class (= super class)
 	 * will be emitted or not, during translation from XML.
 	 */
 	protected boolean emitParentFields	 = false;
 	
 	protected static final NameSpace globalNameSpace	= new NameSpace("global");
 	
 /**
  * This instance of the String Class is used for argument marshalling
  * when using reflection to access a set method that takes a String
  * as an argument.
  */
 	protected static final Class STRING_CLASS;
 	static
 	{
 		Class stringClass	= null;
 		try
 		{
 			stringClass		= Class.forName("java.lang.String");
 		} catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		STRING_CLASS	 = stringClass;
 	}
 
 /**
  * For each class, for each field name of type ElementState, store the
  * open tag. Also do the same for this class.
  */
 	static HashMap		eStateToFieldNameOrClassToOpenTagMapMap = new HashMap();
 
 	HashMap				fieldNameOrClassToTagMap;
 	
 	/**
 	 * Use for resolving getElementById()
 	 */
 	HashMap				elementByIdMap;
 	
 	public ElementState()
 	{
 	   fieldNameOrClassToTagMap	= getFieldNamesToOpenTagsMap();
 	}
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements.
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression				if the emitted xml needs to be compressed
 	 * @param nodeNumber				counts the depth of recursive descent.
 	 * 
 	 * @return 							the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	public String translateToXML(boolean compression) throws XmlTranslationException
 	{
 		//nodeNumber is just to indicate which node number(#1 is the root node of the DOM)
 		//is being processed. compression attr is emitted only for node number 1
 		return translateToXML(compression, true, TOP_LEVEL_NODE);
 	}
 	
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements
 	 * -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression				if the emitted xml needs to be compressed
 	 * @param doRecursiveDescent		true for recursive descent parsing.
 	 * 									false to parse just one level of attributes.
 	 * 										In this case, only the open tag w attributes is generated.
 	 * 										There is no close.
 	 * 
 	 * @return 							the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	public String translateToXML(boolean compression, boolean doRecursiveDescent) throws XmlTranslationException
 	{
 		return translateToXML(compression, doRecursiveDescent, TOP_LEVEL_NODE);
 	}
 	
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements
 	 * -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression			true to compress the xml while emitting.
 	
 	 * @param doRecursiveDescent	true for recursive descent parsing.
 	 * 								false to parse just 1 level of attributes.
 	 * 										In this case, only the open tag w attributes is generated.
 	 * 										There is no close.
 	 * @param nodeNumber			counts the depth of recursive descent.
 	 * 
 	 * @return 						the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	protected String translateToXML(boolean compression, boolean doRecursiveDescent, int nodeNumber)
 		throws XmlTranslationException
 	{
 	   
 	   return translateToXML(compression, doRecursiveDescent, nodeNumber,
 							 getTagMapEntry(getClass(), compression));
 	}
 	
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements
 	 * -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression			true to compress the xml while emitting.
 	
 	 * @param doRecursiveDescent	true for recursive descent parsing.
 	 * 								false to parse just 1 level of attributes.
 	 * 										In this case, only the open tag w attributes is generated.
 	 * 										There is no close.
 	 * @param nodeNumber			counts the depth of recursive descent.
 	 * 
 	 * @return 						the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	protected String translateToXML(boolean compression, 
 									boolean doRecursiveDescent, 
 									int nodeNumber, TagMapEntry tagMapEntry)
 		throws XmlTranslationException
 	{
 		compressed = compression;
 		String result	= "";
 		result += tagMapEntry.startOpenTag;
 		
 		//emit compresseion = true only for the top node, so this dirty hack
 		//so if the nodeNumber is 1 (top node) then emit the compression attribute
 		if (compression && (nodeNumber == TOP_LEVEL_NODE))
 		{
 			String compressionAttr = " " + "compression" + " = " + "\"" + compression + "\"" + " ";
 			result += compressionAttr;
 		}
 		nodeNumber++;
 		
 		try
 		{
 			Field[] fields	= getClass().getFields();
 			//arrange the fields such that all primitive types occur before the reference types
 			arrangeFields(fields);
 			boolean	processingNestedElements= false;
 			
 			String className			= getClass().getName();
 			for (int i=0; i<fields.length; i++)
 			{
 				// iterate through fields
 				Field thatField			= fields[i];
 				int fieldModifiers		= thatField.getModifiers();
 
 				// skip static fields, since we're saving instances,
 				// and inclusion w each instance would be redundant.
 				if ((fieldModifiers & Modifier.STATIC) == Modifier.STATIC)
 				{
 //					debug("Skipping " + thatField + " because its static!");
 					continue;
 				 }
 				if (XmlTools.emitFieldAsAttribute(thatField))
 				{
 					// controls comes here if a primitive type is found 
 					// AFTER a reference type, which is not allowed in our framework
 					String declaringClassName			= 
 						thatField.getDeclaringClass().getName();
 					boolean fieldIsFromDeclaringClass= // false if from a parent
 						declaringClassName.equals(className); // (parent=super class)
 
 					if (fieldIsFromDeclaringClass && processingNestedElements)
 						throw new XmlTranslationException("Primitive type " + thatField + 
 				   					" found after Reference type " + fields[i-1].getType().getName());
 					
 					// emit only if the field is present in this classs
 					// parent class fields should not be emitted,
 					// coz thats confusing
 					if (fieldIsFromDeclaringClass || emitParentFields)
 						result	+= XmlTools.generateNameVal(thatField, this);
 				}
 				else if (doRecursiveDescent)	// recursive descent
 				{	
 					if (!processingNestedElements)
 					{	// found *first* recursive element
 						result	+= ">";	// close element tag behind attributes
 						processingNestedElements	= true;
 					}
 					Object thatReferenceObject = null;
 					try
 					{
 						thatReferenceObject	= thatField.get(this);
 					}
 					catch (IllegalAccessException e)
 					{
 						e.printStackTrace();
 					}
 					// ignore null reference objects
 					if (thatReferenceObject == null)
 					   continue;
 					
 					Collection thatCollection = XmlTools.getCollection(thatReferenceObject);
 					
 					if (thatCollection != null)
 					{
 						//if the object is a collection, 
 						//basically iterate thru the collection and emit Xml from each element
 						Iterator elementIterator = thatCollection.iterator();
 					
 						while (elementIterator.hasNext())
 						{
 							ElementState element;
 							try{
 								element = (ElementState) elementIterator.next();
 							}catch(ClassCastException e)
 							{
 								throw new XmlTranslationException("Collections MUST contain " +
 										"objects of class derived from ElementState but " +
 										thatReferenceObject +" contains some that aren't.");
 							}
 							result += element.translateToXML(compression, true, nodeNumber);		
 						}
 					}
 					else if (thatReferenceObject instanceof ElementState)
 					{	// one of our nested elements, so recurse
 						ElementState thatElementState	= (ElementState) thatReferenceObject;
 						String fieldName		= thatField.getName();
 						// if the field type is the same type of the instance (that is, if no subclassing),
 						// then use the field name to determine the XML tag name.
 						// if the field object is an instance of a subclass that extends the declared type of the
 						// field, use the instance's type to determine the XML tag name.
 						Class thatClass			= thatElementState.getClass();
 //						debug("checking: " + thatReferenceObject+" w " + thatClass+", " + thatField.getType());
 						if (thatClass == thatField.getType())
 						   result += 
 							  thatElementState.translateToXML(compression, true, nodeNumber,
 									  getTagMapEntry(fieldName, compression));
 						else
 						{
 //						   debug("derived class -- using class name for " + thatClass);
 						   result +=
 							  thatElementState.translateToXML(compression, true, nodeNumber,
 									  getTagMapEntry(thatClass, compression));
 						}
 					}
 				} //end of doRecursiveDescent
 			} //end of for loop
 			
 			// end the element (or, at least, our contribution to it)
 			if (!doRecursiveDescent)
 				result += ">"; // dont close it
 			else if (processingNestedElements)
 			{
 				//TODO emit text node
 				String textNode = this.getTextNodeString();
 				if ( textNode != null)
 				{
 					result += textNode;
 				}
 				result	+= tagMapEntry.closeTag;
 			}
 			else
 			{
 				String textNode = this.getTextNodeString();
 				if ( textNode != null)
 				{	
 					result += ">";
 					result += textNode;
 					result	+= tagMapEntry.closeTag;
 				}
 				else
 				{
 					result	+= "/>";	// simple element w attrs but no embedded elements and no text node
 				}
 			}
 				
 		} catch (SecurityException e)
 		{
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlDocumentURL	the url for the XML document that needs to be translated.
 	 * @return 					the parent ElementState object of the corresponding Java tree.
 	 */
 
 	public static ElementState translateFromXML(URL xmlDocumentURL)
 		throws XmlTranslationException
 	{
 		Document document	= buildDOM(xmlDocumentURL);
 		ElementState result	= null;
 		if (document != null)
 			result			= translateFromXML(document);
 		return result;
 	}
 	/**
 	 * Given the name of a valid XML file,
 	 * reads the file and builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param fileName	the name of the XML file that needs to be translated.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(String fileName)
 		throws XmlTranslationException
 	{
 		Document document	= buildDOM(fileName);
 		ElementState result	= null;
 		if (document != null)
 			result			= translateFromXML(document);
 		return result;
 	}
 	
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param fileName	the name of the XML file that needs to be translated.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString)
 		throws XmlTranslationException
 	{
 		Document document	= buildDOMFromXMLString(xmlString);
 		ElementState result	= null;
 		if (document != null)
 			result			= translateFromXML(document);
 		return result;
 	}	
 	
 	/**
 	 * Given the Document object for an XML DOM, builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param doc	the Document object for the DOM tree that needs to be translated.
 	 * @return 		the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Document doc) throws XmlTranslationException
 	{
 		Node rootNode				= (Node) doc.getDocumentElement();
 		return translateFromXML(rootNode);
 	}
 	
 	/**
 	 * A recursive method.
 	 * Typically, this method is initially passed the root Node of an XML DOM,
 	 * from which it builds a tree of equivalent ElementState objects.
 	 * It does this by recursively calling itself for each node/subtree of ElementState objects.
 	 * 
 	 * The method translates any tree of DOM into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file, and access the root Node.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlNode	the root node of the DOM tree that needs to be translated.
 	 * @return 				the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Node xmlNode)
 	   throws XmlTranslationException
 	{
 	   // find the class for the new object derived from ElementState
 		Class stateClass				= null;
 		String tagName		= xmlNode.getNodeName();
 		try
 		{			  
 		   stateClass= globalNameSpace.xmlTagToClass(tagName);
 		   if (stateClass != null)
 		   {
 		   	  ElementState rootState = getElementState(stateClass);
 		   	  if (rootState != null)
 		   	  {
 		   	  	 rootState.elementByIdMap		= new HashMap();
 		   	  	 rootState.translateFromXML(xmlNode, stateClass);
 		   	  	 return rootState;
 		   	  }
 		   }
 		   // else, we dont translate this field; we ignore it.
 		}
 		catch (Exception e)
 		{
 		   println("XML Translation WARNING: Cant find class object for" + tagName
 			   + "\nIgnored.");
 //		   e.printStackTrace();
 //		   throw new XmlTranslationException("All ElementState subclasses"
 //							       + "MUST contain an empty constructor, but "+
 //								   stateClass+" doesn't seem to.");
 		}
 		return null;
 	 }		
 /**
  * Get an instance of an ElementState based Class object.
  * 
  * @param stateClass		Must be derived from ElementState. The type of the object to translate in to.
  * 
  * @return				The ElementState subclassed object.
  * 
  * @throws XmlTranslationException	If its not an ElementState Class object, or
  *  if that class lacks a constructor that takes no paramebers.
  */
 	public static ElementState getElementState(Class stateClass)
 	   throws XmlTranslationException
 	{
 		   // form the new object derived from ElementState
 		ElementState elementState		= null;
 		try
 		{			  
 			elementState	=	(ElementState) stateClass.newInstance();
 		}
 		catch (Exception e)
 		{
 		   throw new XmlTranslationException("All ElementState subclasses"
 							       + "MUST contain an empty constructor, but "+
 								   stateClass+" doesn't seem to.");
 		}
 		return elementState;
 	}
     /**
      * A recursive method.
      * Typically, this method is initially passed the root Node of an XML DOM,
      * from which it builds a tree of equivalent ElementState objects.
      * It does this by recursively calling itself for each node/subtree of ElementState objects.
      * 
      * The method translates any tree of DOM into a tree of Java objects, each of which is 
      * an instance of a subclass of ElementState.
      * The operation of the method is predicated on the existence of a tree of classes derived
      * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
      * 
      * Before calling the version of this method with this signature,
      * the programmer needs to create a DOM from the XML file, and access the root Node.
      * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
      * 
      * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
      * 
      * This method used to be called builtStateObject(...).
      * 
      * @param stateClass		Must be derived from ElementState. The type of the object to translate in to.
      * @param xmlNode	the root node of the DOM tree that needs to be translated.
      * @return 				the parent ElementState object of the corresponding Java tree.
      */
 	public void translateFromXML(Node xmlNode, Class stateClass)
 	   throws XmlTranslationException
 	{
 		// translate attribtues
 		if (xmlNode.hasAttributes())
 		{
 			NamedNodeMap xmlNodeAttributes = xmlNode.getAttributes();
 			
 			for (int i = 0; i < xmlNodeAttributes.getLength(); i++) 
 			{
 				Node xmlAttr = xmlNodeAttributes.item(i);
                
 				if (xmlAttr.getNodeValue() != null)
 				{
 					String xmlAttrName	= xmlAttr.getNodeName();
 					//create the method name from the tag name
 					//for example, for the attr bias, methodName = setBias
 					String methodName	= XmlTools.methodNameFromTagName(xmlAttrName);
 					//search for the method with the name created above 
 					//for this u have to create an array of class indicating the parameters to the method
 					//in our case, all the methods have a single parameter, String
 					//which holds the value of the attribute and then that object is responsible
 					//for converting it to appropriate type from the string
 					String value		= xmlAttr.getNodeValue();
 						
 						if (xmlAttrName.equals("id"))
 						{
 							this.elementByIdMap.put(value, this);
 						}
 					try
 					{
 						Class[] parameters	= new Class[1];
 						parameters[0]		= STRING_CLASS;
 						Method attrMethod	= stateClass.getMethod(methodName, parameters);
 						// if the method is found, invoke the method
 						// fill the String value with the value of the attr node
 						// args is the array of objects containing arguments to the method to be invoked
 						// in our case, methods have only one arg: the value String
 						Object[] args = new Object[1];
 						args[0]		  = value;
 						try
 						{
 							attrMethod.invoke(this,args); // run set method!
 						}
 						catch (InvocationTargetException e)
 						{
 							println("WEIRD: couldnt run set method for " + xmlAttrName +
 									  " even though we found it");
 							e.printStackTrace();
 						}
 						catch (IllegalAccessException e)
 						{
 							println("WEIRD: couldnt run set method for " + xmlAttrName +
 									  " even though we found it");
 							e.printStackTrace();
 						}	 
 
 					}
 					catch (NoSuchMethodException e)
 					{
 						String fieldName = XmlTools.fieldNameFromElementName(xmlAttr.getNodeName());
 //						debug("buildFromXML(-> setPrimitive("+fieldName,value);
 						setField(fieldName, value);
 //						if (!elementState.setField(fieldName, value))
 //							throw new
 //							   XmlTranslationException("Set method missing and automatic set failing for variable " + xmlAttr.getNodeName() + " in "+stateClass+
 // ", please create a method that takes a String as parameter and sets the value of " + xmlAttr.getNodeName());
 					}
 				} // end if non-null attribute
 			} // end of for attribute processing loop
 		}// end of if hasAttributes
 
 		// translate nested elements (aka children):
 		// loop through them, recursively build them, and add them to ourself
 		NodeList childNodes	= xmlNode.getChildNodes();
 		int numChilds		= childNodes.getLength();
 	
 		// TODO -- why this block??
 		if (numChilds == 1)
 		{
 		   // is it a single text node?
 			Node firstNode	= childNodes.item(0);
 			if (firstNode.getNodeType() == Node.TEXT_NODE)
 			{
 				// create ElementStateTextNode here
 				this.setTextNodeString(firstNode.getNodeValue());
 			}
 		}
 		for (int i = 0; i < numChilds; i++)
 		{
 			Node childNode		= childNodes.item(i);
 			short childNodeType		= childNode.getNodeType();
 			if ((childNodeType != Node.TEXT_NODE) && (childNodeType != Node.CDATA_SECTION_NODE))
 			{
 			   // look for instance variable name corresponding to
 			   // childNode's tag in this. Get the class of that.
 			   String childTag			= childNode.getNodeName();
 			   String childFieldName	= 
 				  XmlTools.fieldNameFromElementName(childTag);
 //			   println("childFieldName="+childFieldName +" in "+
 //				   stateClass);
 			   try
 			   {
 				  Field childField		= stateClass.getField(childFieldName);
 //				  println("childField="+childField);
 				  Class childClass		= childField.getType();
 //				  println("childClass="+childClass);
 				  ElementState childElementState = getElementState(childClass);
 				  childElementState.elementByIdMap	= this.elementByIdMap;
 				  
 				  childElementState.translateFromXML(childNode, childClass);
 				  addNestedElement(childField, childElementState);
 				  
 			   } catch (NoSuchFieldException e)
 			   {
 				  // must be part of a collection, or a field we dont know about
 			   	  // anyway, its not not a named field
 			   	  
 
 				  String tagName		= childNode.getNodeName();
 			  	  Class childStateClass= globalNameSpace.xmlTagToClass(tagName);				  
 				  
 				  ElementState childElementState = getElementState(childStateClass);
 				  childElementState.elementByIdMap	= this.elementByIdMap;
 				  
 				  childElementState.translateFromXML(childNode, childStateClass);
 		
 				  if (childElementState != null)
 				  	addNestedElement(childElementState);
 				  // else we couldnt find an appropriate class for this tag, so we're ignoring it
 			   }
 			}
 			else if (numChilds == 1) // we could get rid of this to be even more general!
 			{
 				String text	= childNode.getNodeValue();;
 				if (text != null)
 					setTextNodeString(text);
 			}
 		}
 	}
 	
 
 	//////////////// methods to generate DOM objects ///////////////////////
 	/**
 	 * This method creates a DOM Document from the XML file at a given URL.
 	 *
 	 * @param url	the URL to the XML from which the DOM is to be created
 	 * 
 	 * @return			the Document object
 	 */
 	static public Document buildDOM(URL url)
 	{
 		return buildDOM(url.toString());
 	}
 	/**
 	 * This method creates a DOM Document from the local XML file.
 	 *
 	 * @param file		the XML file from which the DOM is to be created
 	 * 
 	 * @return			the Document object
 	 */
 	static public Document buildDOM(File file)
 	{
 		return buildDOM(file.toString());
 	}
 	/**
 	 * This method creates a DOM Document from the XML file at a given URI,
 	 * which could be a local file or a URL.
 	 *
 	 * @param xmlFileOrURLName	the path to the XML from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOM(String xmlFileOrURLName)
 	{		       
 		Document document	= null;
 		try
 		{
     	  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     	  DocumentBuilder builder = factory.newDocumentBuilder();
     	  createErrorHandler(builder);
     	  
   		  document = builder.parse(xmlFileOrURLName);
 		} 
 		
 		catch (SAXParseException spe) {
 			// Error generated by the parser
 		    println(xmlFileOrURLName + ":\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
 		    println("   " + spe.getMessage());
 		  
 		    // Use the contained exception, if any
 		    Exception  x = spe;
 		    if (spe.getException() != null)
 		   	   x = spe.getException();
 		    x.printStackTrace();
 	  	}
 	  	
 	  	catch (SAXException sxe) {
 		    // Error generated during parsing
 		    Exception  x = sxe;
 		    if (sxe.getException() != null)
 		      x = sxe.getException();
 		    x.printStackTrace();
 	   	}
 	   	
 	   	catch (ParserConfigurationException pce) {
 		    // Parser with specified options can't be built
 		    pce.printStackTrace();
 	   	}
 	   	
 	   	catch (IOException ioe) {
 		    // I/O error
 		    ioe.printStackTrace();
 	  	}
 	  	
 	  	catch(FactoryConfigurationError fce){
 	  		fce.printStackTrace();
 	  	}
 	  	catch(Exception e){
 	  		e.printStackTrace();
 	  	}
 		return document;
 	}
 
 	/**
 	 * This method creates a DOM Document from the XML file at a given URI,
 	 * which could be a local file or a URL.
 	 *
 	 * @param xmlFileOrURLName	the path to the XML from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOM(InputStream inStream)
 	{		       
 		Document document	= null;
 		try
 		{
     	  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     	  DocumentBuilder builder = factory.newDocumentBuilder();
     	  createErrorHandler(builder);
     	  
   		  document = builder.parse(inStream);
 		} 
 		
 		catch (SAXParseException spe) {
 			// Error generated by the parser
 		    println(inStream + ":\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
 		    println("   " + spe.getMessage());
 		  
 		    // Use the contained exception, if any
 		    Exception  x = spe;
 		    if (spe.getException() != null)
 		   	   x = spe.getException();
 		    x.printStackTrace();
 	  	}
 	  	
 	  	catch (SAXException sxe) {
 		    // Error generated during parsing
 		    Exception  x = sxe;
 		    if (sxe.getException() != null)
 		      x = sxe.getException();
 		    x.printStackTrace();
 	   	}
 	   	
 	   	catch (ParserConfigurationException pce) {
 		    // Parser with specified options can't be built
 		    pce.printStackTrace();
 	   	}
 	   	
 	   	catch (IOException ioe) {
 		    // I/O error
 		    ioe.printStackTrace();
 	  	}
 	  	
 	  	catch(FactoryConfigurationError fce){
 	  		fce.printStackTrace();
 	  	}
 	  	catch(Exception e){
 	  		e.printStackTrace();
 	  	}
 		return document;
 	}
 	/**
 	 * This method creates a DOM Document from an XML-formatted String.
 	 *
 	 * @param xmlString	the XML-formatted String from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOMFromXMLString(String xmlString) {
         Document document = null;
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory
                     .newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             createErrorHandler(builder);
 
             builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
         }
 
         catch (SAXParseException spe) {
             // Error generated by the parser
             println(xmlString + ":\n** Parsing error" + ", line "
                     + spe.getLineNumber() + ", uri " + spe.getSystemId());
             println("   " + spe.getMessage());
 
             // Use the contained exception, if any
             Exception x = spe;
             if (spe.getException() != null)
                 x = spe.getException();
             x.printStackTrace();
         }
 
         catch (SAXException sxe) {
             // Error generated during parsing
             Exception x = sxe;
             if (sxe.getException() != null)
                 x = sxe.getException();
             x.printStackTrace();
         }
 
         catch (ParserConfigurationException pce) {
             // Parser with specified options can't be built
             pce.printStackTrace();
         }
 
         catch (IOException ioe) {
             // I/O error
             ioe.printStackTrace();
         }
 
         catch (FactoryConfigurationError fce) {
             fce.printStackTrace();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return document;
     }
 
   	static private void createErrorHandler(final DocumentBuilder builder){
   		
   		builder.setErrorHandler(
 	  	new org.xml.sax.ErrorHandler() {
 	    	// ignore fatal errors (an exception is guaranteed)
 		    public void fatalError(SAXParseException exception)
 		    throws SAXException {
 		    }
 		    // treat validation errors as fatal
 		    public void error(SAXParseException e)
 		    throws SAXParseException
 		    {
 		      throw e;
 		    }
 		
 		     // dump warnings too
 		    public void warning(SAXParseException err)
 		    throws SAXParseException
 		    {
 		      println(builder + "** Warning"
 		        + ", line " + err.getLineNumber()
 		        + ", uri " + err.getSystemId());
 		      println("   " + err.getMessage());
 		    }
 	    
 	  	}  
 		); 
   	}
 
 	//////////////// methods to generate XML, and write to a file /////////////
 /**
  * 	Translate to XML, then write the result to a file.
  * 
  * 	@param filePath		the file in which the xml needs to be saved
  * 	@param prettyXml	whether the xml should be written in an indented fashion
  *  @param compression	whether the xml should be compressed while being emitted
  */	
 	public void saveXmlFile(String filePath, boolean prettyXml, boolean compression)
 		throws XmlTranslationException
 	{
 		final String xml = XML_FILE_HEADER + translateToXML(compression);
 
 		//write the Xml in the file		
 		try
 		{
 			String xmlFileName = filePath;
 			if(!filePath.endsWith(".xml") && !filePath.endsWith(".XML"))
 			{
 				xmlFileName = filePath + ".xml";
 			}
 			else
 			{
 				filePath	=	filePath.substring(0,filePath.indexOf(".xml"));
 			}
 		 if (prettyXml)
 		 {
 		 	XmlTools.writePrettyXml(xml, new StreamResult(new File(xmlFileName)));
 		 }
 		 
 			else
 			{
 				FileOutputStream out = new FileOutputStream(new File(xmlFileName));
 				PrintStream p = new PrintStream(out);
 				p.println(xml);
 				p.close();
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}		
 	}
 	
 /**
  * Andruid [1/2/05]: this method is supposed to do the following, but it doesn't seem
  * that its being used, nor does it really seem to do this.
  * <p/>
  * 
  * Translate to XML, then appends the result to a file. 
  * This method is used when XML should be emitted and written to a file
  * incrementally. In other words, it does not wait for the complete XML to be
  * emitted before saving it to a file. This is useful in cases such as logging.
  * 
  * @see  <code>saveXmlFile</code>
 
  * 	@param filePath		the file in which the xml needs to be saved
  * 	@param prettyXml	whether the xml should be written in an indented fashion
  *  @param compression	whether the xml should be compressed while being emitted
  */	
 	public void appendXmlFile(String xmlToWrite, String filePath, 
 							  boolean prettyXml, boolean compression)
 	{
 		try
 		{
 			String xmlFileName = filePath;
 			if(!filePath.endsWith(".xml") && !filePath.endsWith(".XML"))
 			{
 				xmlFileName = filePath + ".xml";
 			}
 			else
 			{
 				filePath	=	filePath.substring(0,filePath.indexOf(".xml"));
 			}
 			if (prettyXml)
 				XmlTools.writePrettyXml(xmlToWrite, new StreamResult(new File(xmlFileName)));
 			else
 			{
 				BufferedWriter writer = IO.openWriter(xmlFileName, true);
 				File temp = new File(xmlFileName);
 				if(!temp.exists())
 				IO.writeLine(writer,XML_FILE_HEADER);								
 				IO.writeLine(writer,translateToXML(compression));
 				IO.closeWriter(writer);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	//////////////// helper methods used by translateToXML() //////////////////
 
 /**
  * Get the map for translating field names to startOpenTags for this.
  * We have to create a HashMap to do this, instead of using a static,
  * because all relevant objects are subclassed from <code>ElementState</code>,
  * so a static declaration wouldn't actually be class wide.
  */
 	private HashMap getFieldNamesToOpenTagsMap()
 	{
 	   Class thisClass= getClass();
 	   HashMap result = (HashMap) eStateToFieldNameOrClassToOpenTagMapMap.get(thisClass);
 	   // stay out of the synchronized block most of the time
 	   if (result == null)
 	   {
 		  synchronized (eStateToFieldNameOrClassToOpenTagMapMap)
 		  {
 			 result = (HashMap) eStateToFieldNameOrClassToOpenTagMapMap.get(thisClass);
 			 if (result == null)
 			 {
 				result	= new HashMap();
 				eStateToFieldNameOrClassToOpenTagMapMap.put(thisClass, result);
 			 }
 		  }
 	   }
 	   return result;
 	}
 	/**
 	 * @return	the XML element name, or <i>tag</i>, that maps to this ElementState derived class.
 	 */
 	public String tagName()
 	{
 	   return globalNameSpace.objectToXmlTag(this);
 	}
 
 	/**
 	 * Translate the name of this ElementState derived class into an
 	 * appropriate tag name. Use the tag name to form most of an open
 	 * tag for an XML element.
 	 * </p>
 	 * However, the final close > is not emitted here,
 	 * so attributes can be appended.
 	 *
 	 * @return	Most of an open tag for the XML element.
 	 */
 	public String startOpenTag()
 	{	
 	   return "<" + this.tagName();
 	}
 	/**
 	 * Translate the name of this ElementState derived class into an
 	 * appropriate tag name. Use the tag name to form a close tag for 
 	 * an XML element.
 	 *
 	 * @return	Most of an open tag for the XML element.
 	 */
 	public String closeTag()
 	{
 		return "</" + this.tagName() + ">";
 	}
 
 	/**
 	 * Translate a supplied field name into an appropriate tag name. 
 	 * Use the tag name to form most of an open tag for an XML element.
 	 * </p>
 	 * However, the final close > is not emitted here,
 	 * so attributes can be appended.
 	 *
 	 * @return	Most of an open tag for the XML element.
 	 */
 	public String startOpenTag(String fieldName, boolean compression)
 	{	
 	   return getTagMapEntry(fieldName, compression).startOpenTag;
 	}
 	/**
 	 * Translate a supplied field name into an appropriate tag name. 
 	 * Use the tag name to form a close tag for an XML element.
 	 * </p>
 	 * However, the final close > is not emitted here,
 	 * so attributes can be appended.
 	 *
 	 * @return	Most of an open tag for the XML element.
 	 */
 	public String closeTag(String fieldName, boolean compression)
 	{	
 	   return getTagMapEntry(fieldName, compression).closeTag;
 	}
 
 /**
  * Get a tag translation object that corresponds to the fieldName,
  * with this class. If necessary, form that tag translation object,
  * and cache it.
  */
 	private TagMapEntry getTagMapEntry(String fieldName, boolean compression)
 	{
 	   TagMapEntry result= (TagMapEntry)fieldNameOrClassToTagMap.get(fieldName);
 	   if (result == null)
 	   {
 		  synchronized (fieldNameOrClassToTagMap)
 		  {
 			 result		= (TagMapEntry) fieldNameOrClassToTagMap.get(fieldName);
 			 if (result == null)
 			 {
 				String tagName	= XmlTools.getXmlTagName(fieldName, "State", compression);
 				result	= new TagMapEntry(tagName);
 				debug(tagName.toString());
 				fieldNameOrClassToTagMap.put(fieldName, result);
 			 }
 		  }
 	   }
 	   return result;
 	}
 /**
  * Get a tag translation object that corresponds to the fieldName,
  * with this class. If necessary, form that tag translation object,
  * and cache it.
  */
 	private TagMapEntry getTagMapEntry(Class thatClass, boolean compression)
 	{
 	   TagMapEntry result= (TagMapEntry)fieldNameOrClassToTagMap.get(thatClass);
 	   if (result == null)
 	   {
 		  synchronized (fieldNameOrClassToTagMap)
 		  {
 			 result		= (TagMapEntry) fieldNameOrClassToTagMap.get(thatClass);
 			 if (result == null)
 			 {
 				String tagName	= XmlTools.getXmlTagName(thatClass, "State", compression);
 				result	= new TagMapEntry(tagName);
 				fieldNameOrClassToTagMap.put(thatClass, result);
 				debug(tagName.toString());
 			 }
 		  }
 	   }
 	   return result;
 	}
 	/**
 	 * Reorders fields so that all primitive types occur first and then the reference types.
 	 * 
 	 * @param fields
 	 */
 	private void arrangeFields(Field[] fields)
 	{
 		int primitivePos = 0;
 		Vector refTypes = new Vector();
 		
 		for (int i = 0; i < fields.length; i++)
 		{
 			Field thatField	= fields[i];
 			if (XmlTools.emitFieldAsAttribute(thatField))
 			{
 				if(i > primitivePos)
 				{
 					fields[primitivePos] = thatField;
 				}
 				primitivePos++;
 			}
 			else
 			{
 				refTypes.add(thatField);
 			}
 		}
 		
 		//copy the ref types at the end of the primitive types
 		int j = 0;
 		for(int i = fields.length - refTypes.size(); i < fields.length; i++)
 		{
 			fields[i]	=	(Field)refTypes.elementAt(j++);			
 		}
 	}
 
 
 	//////////////// helper methods used by translateFromXML() ////////////////
 	/**
 	 * Set the specified extended primitive field in this, if possible.
 	 * 
 	 * @param fieldName		name of the field to set.
 	 * @param fieldValue	String representation of the value.
 	 * 
 	 * @return true if the field is set successfully. false if it seems to not exist.
 	 */
 	protected boolean setField(String fieldName, String fieldValue)
 	
 	{
 		boolean result	= false;
 		try
 		{
 		   Field field			= getClass().getField(fieldName);
 		   Type fieldType		= TypeRegistry.getType(field);
 		   if (fieldType != null)
 			  result			= fieldType.setField(this, field, fieldValue);
 		}
 		catch (NoSuchFieldException e)
 		{
 			debug("ERROR no such field to set "+fieldName+" = "+
 			      fieldValue);
 		}
 		return result;
 	}
 
 	/**
 	 * Used to add a nested object to <code>this ElementState</code> object.
 	 * 
 	 * This method MUST be overridden by all derived ElementState super classes
 	 * that function as collections (e.g., via Vector, Hashtable etc.) of other
 	 * ElementState derivatives.
 	 * In those cases, it is used to add the nested derived elements inside
 	 * to the collection. This method is called during translateFromXML(...).
 	 *
 	 * @param elementState	the nested state-object to be added
 	 */
 	protected void addNestedElement(ElementState elementState)
 		throws XmlTranslationException
 	{
 		String fieldName = XmlTools.fieldNameFromObject(elementState);
 //		debug("<<<<<<<<<<<<<<<<<<<<<<<<fieldName is: " + fieldName);
 		try
 		{
 			Field field = getClass().getField(fieldName);
 			addNestedElement(field, elementState);
 		}
 		catch (Exception e)
 		{
 		   debug("ERROR: Can't find a field called " + fieldName);
 		}
 	}
 
 	/**
 	 * Used to add a nested object to <code>this ElementState</code> object.
 	 * 
 	 * This method MUST be overridden by all derived ElementState super classes
 	 * that function as collections (e.g., via Vector, Hashtable etc.) of other
 	 * ElementState derivatives.
 	 * In those cases, it is used to add the nested derived elements inside
 	 * to the collection. This method is called during translateFromXML(...).
 	 *
 	 * @param elementState	the nested state-object to be added
 	 */
 	protected void addNestedElement(Field field, ElementState elementState)
 		throws XmlTranslationException
 	{
 //		debug("<<<<<<<<<<<<<<<<<<<<<<<<fieldName is: " + fieldName);
 		try
 		{
 			field.set(this,elementState);
 		}
 		catch (Exception e)
 		{
 		   throw new XmlTranslationException(
 					"Object / Field set mismatch -- unexpected. This should never happen.\n"+
 					field +" , " + this);
 		}
 	}
 	
 	String textNodeString;
 	
 	public void setTextNodeString(String textNodeString)
 	{
 	   this.textNodeString		= XmlTools.unescapeXML(textNodeString);
 	}
 	public String getTextNodeString()
 	{
 		return textNodeString;
 //		return (textNodeString == null) ? null : XmlTools.unescapeXML(textNodeString);
 	}
 	/////////////////////////// other methods //////////////////////////
 
 	/**
 	 * Call this method if the object should be translated using a compression table to
 	 * mininmize space (and legibility :-).
 	 * 
 	 * @param value		String version of a boolean. Use "true" to turn it on.
 	 */
 	public void setCompressed(String value)
 	{
 		if ("true".equals(value))
 			compressed	=	true;
 	}
 	
 	/**
 	 * Add a package name to className mapping to the translation table in the NameSpace.
 	 * 
 	 * @param packageName
 	 * @param className
 	 */
 	public static void addTranslation(String packageName, String className)
 	{
 		globalNameSpace.addTranslation(packageName, className);
 	}
 
 	class TagMapEntry
 	{
 	   final String startOpenTag;
 	   final String closeTag;
 	   
 	   TagMapEntry(String tagName)
 	   {
 		  startOpenTag	= "<" + tagName;
 		  closeTag		= "</" + tagName + ">";
 	   }
 	   public String toString()
 	   {
 	   		return "TagMapEntry" + closeTag;
 	   }
 	}
 	
 	public ElementState getElementStateById(String id)
 	{
 		return (ElementState) this.elementByIdMap.get(id);
 	}
 	/*	
 	void fillValues(Vector vector)
 	{
 	   int n = vector.size();
 	   for (int i=0; i<n; i++)
 	      fillValues((Attr) vector.elementAt(i));
 	}
 	
 	protected void fillValues(Attr attr)
 	{	  
 	  setField(attr.getName(), attr.getValue());
 	}
 	
 	void fillValues(String fieldName, String value)
 	{	   
 	   setField(fieldName, value);
 	}
 */	
 }
