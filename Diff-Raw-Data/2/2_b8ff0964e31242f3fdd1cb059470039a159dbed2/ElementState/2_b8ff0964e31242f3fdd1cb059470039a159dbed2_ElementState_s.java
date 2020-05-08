 package ecologylab.xml;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.generic.StringInputStream;
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.types.scalar.ScalarType;
 import ecologylab.xml.types.scalar.TypeRegistry;
 
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
  * @version     2.9
  */
 public class ElementState extends Debug
 implements OptimizationTypes, XMLTranslationExceptionTypes
 {
 	/**
 	 * Link for a DOM tree.
 	 */
 	ElementState				parent;
 /**
  * Enables storage of a single text node child.
  * This facility is meager and rarely used, since
  * the leaf nodes facility does the same thing but better.
  * <p/>
  * We might want to implement the ability to store multiple text nodes
  * here some time in the future.
  */	
 	private StringBuilder		textNodeBuffy;
 	
 	/**
 	 * Just-in time look-up tables to make translation be efficient.
 	 * Allocated on a per class basis.
 	 */
 	Optimizations				optimizations;
 	
 	/**
 	 * Use for resolving getElementById()
 	 */
 	HashMap<String, ElementState>						elementByIdMap;
 	
 	HashMap<String, ElementState>						nestedNameSpaces;
 
 	
 	public static final int 	UTF16_LE	= 0;
 	public static final int 	UTF16		= 1;
 	public static final int 	UTF8		= 2;
 	
 	/**
 	 * These are the styles for declaring fields as translated to XML.
 	 *
 	 * @author andruid
 	 */
 	public enum DeclarationStyle { ANNOTATION, TRANSIENT, PUBLIC};
 	
 	private static DeclarationStyle	declarationStyle	= DeclarationStyle.ANNOTATION;
 	
 	/**
 	 * xml header
 	 */
 	static protected final String XML_FILE_HEADER = "<?xml version=" + "\"1.0\"" + " encoding=" + "\"UTF-8\"" + "?>\n";
 //	static protected final String XML_FILE_HEADER = "<?xml version=" + "\"1.0\"" + " encoding=" + "\"US-ASCII\"" + "?>";
 	
 	static protected final int	ESTIMATE_CHARS_PER_FIELD	= 80;
 
 	static final int 			TOP_LEVEL_NODE		= 1;
 	
 /**
  * Used for argument marshalling with reflection to access 
  * a set method that takes a String as an argument.
  */
 	protected static Class[] 	MARSHALLING_PARAMS	= {String.class};
 
     /**
      * Constant indicating that floating precision cutoff is disabled. If floatingPrecision is set
      * to this value, then all available decimal places will be emitted.
      */
     public static final short             FLOATING_PRECISION_OFF   = -1;
 
     /**
      * Indicates how many digits after the decimal will be emitted on all floating values (floats
      * and doubles). If set to FLOATING_PRECISION_OFF (the default value), nothing will be done.
      */
     private short                         floatingPrecision = FLOATING_PRECISION_OFF;
     
     private static boolean				useDOMForTranslateTo;
     
     /**
      * Construct. Create a link to a root optimizations object.
      */
 	public ElementState()
 	{
 		Optimizations parentOptimizations	= (parent == null) ? null : parent.optimizations;
 		
 		optimizations						= Optimizations.lookupRootOptimizations(this);
 		optimizations.setParent(parentOptimizations);	// andruid 2/8/08
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
 	 * @return 							the generated xml string, in a Reusable SBtringBuilder
 	 * 
 	 * @throws XMLTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	public StringBuilder translateToXML() throws XMLTranslationException
 	{
 		return translateToXML((StringBuilder)null);
 	}
 	
 /**
  * Allocated a StringBuilder for translateToXML(), based on a rough guess of how many fields there are to translate.
  * @return
  */
 	private StringBuilder allocStringBuilder()
 	{
 		ArrayList<Field> attributeFields	= optimizations.attributeFields();
 		int numAttributes 					= attributeFields.size();
 		int	numFields						= numAttributes + optimizations.quickNumElements();
 
 		return new StringBuilder(numFields * ESTIMATE_CHARS_PER_FIELD);
 	}
 	/**
 	 * Translates a tree of ElementState objects into equivalent XML in a StringBuilder.
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
 	 * @param buffy 				StringBuilder to translate into, or null if you want one created for you.
 	 * 
 	 * @return 						the generated xml string
 	 * 
 	 * @throws XMLTranslationException if a problem arises during translation.
 	 * Problems with Field access are possible, but very unlikely.
 	 */	
 	public StringBuilder translateToXML(StringBuilder buffy) 
 	throws XMLTranslationException
 	{
 		if (buffy == null)
 	        buffy = allocStringBuilder();
 		Class rootClass	= optimizations.thatClass;
 
 		translateToXMLBuilder(rootClass, optimizations.rootFieldToXMLOptimizations(rootClass), buffy);
 		
 		return buffy;
 	}
 	
 	/**
 	 * Translates a tree of ElementState objects, and writes the output to the File passed in.
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * <p/>
 	 * Makes directories if necessary.
 	 * 
 	 * @param outputFile		File to write the XML to.
 	 * 
 	 * @throws XMLTranslationException if a problem arises during translation. 
 	 * Problems with Field access are possible, but very unlikely.
 	 * @throws IOException		If there are problems with the file.
 	 */
 	public void translateToXML(File outputFile)
 	throws XMLTranslationException, IOException
 	{
 		if (outputFile.isDirectory())
 			throw new XMLTranslationException("Output path is already a directory, so it can't be a file: " + outputFile.getAbsolutePath());
 		
 		String outputDirName	= outputFile.getParent();
 		File outputDir			= new File(outputDirName);
 		outputDir.mkdirs();
 		
 		BufferedWriter bufferedWriter	= new BufferedWriter(new FileWriter(outputFile));
 		translateToXML(bufferedWriter);
 		bufferedWriter.close();
 	}
 
 	/**
 	 * Translates a tree of ElementState objects, and writes the output to the Appendable passed in.
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param appendable		Appendable to translate into. Must be non-null. Can be a Writer, OutputStream, ...
 	 * 
 	 * @throws XMLTranslationException if a problem arises during translation.
 	 * The most likely cause is an IOException.
 
 	 * <p/>
 	 * Problems with Field access are possible, but very unlikely.
 	 */
 	public void translateToXML(Appendable appendable) 
 	throws XMLTranslationException
 	{
 		if (appendable == null)
 	        throw new XMLTranslationException("Appendable is null");
 	
 		try
 		{
 			Class rootClass = optimizations.thatClass;
 			translateToXMLAppendable(rootClass, optimizations.rootFieldToXMLOptimizations(rootClass), appendable);
 		} catch (IOException e)
 		{
 			throw new XMLTranslationException("IO", e);
 		}
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
 	 * @return 						the generated xml string
 	 * 
 	 * @throws XMLTranslationException if a problem arises during translation.
 	 * Problems with Field access are possible, but very unlikely.
 	 */
 	private void translateToXMLBuilder(Class thatClass, FieldToXMLOptimizations fieldToXMLOptimizations, StringBuilder buffy)
 	throws XMLTranslationException
 	{
         this.preTranslationProcessingHook();
 		
 		final String startOpenTag = fieldToXMLOptimizations.startOpenTag();
 		buffy.append(startOpenTag);
 
 		ArrayList<FieldToXMLOptimizations> xmlnsF2XOs	= optimizations.xmlnsAttributeOptimizations();
 		int numXmlnsAttributes 			= (xmlnsF2XOs == null) ? 0 : xmlnsF2XOs.size();
 		if (numXmlnsAttributes > 0)
 		{
 			for (int i=0; i<numXmlnsAttributes; i++)
 			{
 				FieldToXMLOptimizations xmlnsF2Xo	= xmlnsF2XOs.get(i);
 				xmlnsF2Xo.xmlnsAttr(buffy);
 			}			
 		}
         ArrayList<FieldToXMLOptimizations> attributeF2XOs	= optimizations.attributeFieldOptimizations();
 		int numAttributes 			= attributeF2XOs.size();
 		
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i=0; i<numAttributes; i++)
 				{
 					// iterate through fields
 					FieldToXMLOptimizations childF2Xo	= attributeF2XOs.get(i);
 					childF2Xo.appendValueAsAttribute(buffy, this);
 				}
 			} catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new XMLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 
 		ArrayList<FieldToXMLOptimizations> elementF2XOs	= optimizations.elementFieldOptimizations();
 		int numElements						= elementF2XOs.size();
 
 		StringBuilder textNode				= this.textNodeBuffy;
 		//TODO or if there is a special @xml_text field!
 		
 		if ((numElements == 0) && ((textNode == null) || (textNode.length() == 0)))
 		{
 			buffy.append('/').append('>');	// done! completely close element behind attributes				
 		}
 		else
 		{
 			if (startOpenTag.length() > 0)
 				buffy.append('>');	// close open tag behind attributes
 			if (textNode != null) 
 			{	
 				XMLTools.escapeXML(buffy, textNode);
 			}
 			for (int i=0; i<numElements; i++)
 			{
 				FieldToXMLOptimizations childF2Xo	= elementF2XOs.get(i);
 				final int childOptimizationsType 	= childF2Xo.type();
 				if (childOptimizationsType == LEAF_NODE_VALUE)
 				{
 					try
 					{
 						childF2Xo.appendLeaf(buffy, this);
 					} catch (Exception e)
 					{
 						throw new XMLTranslationException("TranslateToXML for leaf node " + this, e);
 					}				}
 				else
 				{
 					Object thatReferenceObject	= null;
 					Field childField			= childF2Xo.field();
 					try
 					{
 						thatReferenceObject		= childField.get(this);
 					}
 					catch (IllegalAccessException e)
 					{
 						debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 						childField.setAccessible(true);
 						try
 						{
 							thatReferenceObject	= childField.get(this);
 						} catch (IllegalAccessException e1)
 						{
 							error("Can't access " + childField.getName());
 							e1.printStackTrace();
 						}
 					}
 					// ignore null reference objects
 					if (thatReferenceObject == null)
 						continue;
 
 					final boolean isScalar		= (childOptimizationsType == COLLECTION_SCALAR) || (childOptimizationsType == MAP_SCALAR);
 					// gets Collection object directly or through Map.values()
 					Collection thatCollection;
 					switch (childOptimizationsType)
 					{
 					case COLLECTION_ELEMENT:
 					case COLLECTION_SCALAR:
 					case MAP_ELEMENT:
 					case MAP_SCALAR:
 						thatCollection			= XMLTools.getCollection(thatReferenceObject);
 						break;
 					default:
 						thatCollection			= null;
 					break;
 					}
 
 					if (thatCollection != null)
 					{
 						//if the object is a collection, 
 						//basically iterate thru the collection and emit XML from each element
 						final Iterator iterator			= thatCollection.iterator();
 						while (iterator.hasNext())
 						{
 							Object next = iterator.next();
 							if (isScalar)	// leaf node!
 							{
 								try
 								{
 									childF2Xo.appendCollectionLeaf(buffy, next);
 								} catch (IllegalArgumentException e)
 								{
 									throw new XMLTranslationException("TranslateToXML for collection leaf " + this, e);
 								} catch (IllegalAccessException e)
 								{
 									throw new XMLTranslationException("TranslateToXML for collection leaf " + this, e);
 								}
 							}
 							else if (next instanceof ElementState)
 							{
 								ElementState collectionSubElementState = (ElementState) next;
 								//collectionSubElementState.translateToXML(collectionSubElementState.getClass(), true, nodeNumber, buffy, REGULAR_NESTED_ELEMENT);
 								final Class<? extends ElementState> collectionElementClass = collectionSubElementState.getClass();
 								FieldToXMLOptimizations collectionElementEntry		= optimizations.fieldToJavaOptimizations(childF2Xo, collectionElementClass);
 								collectionSubElementState.translateToXMLBuilder(collectionElementClass, collectionElementEntry, buffy);
 							}
 							else
 								throw collectionElementTypeException(thatReferenceObject);
 
 						}
 					}
 					else if (thatReferenceObject instanceof ElementState)
 					{	// one of our nested elements, so recurse
 						ElementState thatElementState	= (ElementState) thatReferenceObject;
 						// if the field type is the same type of the instance (that is, if no subclassing),
 						// then use the field name to determine the XML tag name.
 						// if the field object is an instance of a subclass that extends the declared type of the
 						// field, use the instance's type to determine the XML tag name.
 						Class thatNewClass			= thatElementState.getClass();
 						// debug("checking: " + thatReferenceObject+" w " + thatNewClass+", " + thatField.getType());
 						FieldToXMLOptimizations nestedF2Xo = thatNewClass.equals(childField.getType()) ?
 								childF2Xo : fieldToXMLOptimizations(childField, thatNewClass);
 
 						thatElementState.translateToXMLBuilder(thatNewClass, nestedF2Xo, buffy);
 						//buffy.append('\n');						
 					}
 				}
 			} //end of for each element child
 			HashMap<String, ElementState> nestedNameSpaces = this.nestedNameSpaces;
 			if (nestedNameSpaces != null)
 			{
 				for (ElementState nestedNSE : nestedNameSpaces.values())
 				{
 					//TODO -- where do we get optimizations for nested namespace elements?
 					Class<? extends ElementState> nestedNSClass = nestedNSE.getClass();
 					FieldToXMLOptimizations nestedNsF2XO	=
 						nestedNSE.optimizations.rootFieldToXMLOptimizations(nestedNSClass);
 					nestedNSE.translateToXMLBuilder(nestedNSClass, nestedNsF2XO, buffy);
 				}
 			}
 			// end the element
 			buffy.append(fieldToXMLOptimizations.closeTag())/* .append('\n') */;
 
 		} // end if no nested elements or text node
 	}
 	/**
 	 * Generate an exception during translateToXML()
 	 * when a collection contains elements that are not ElementState subclasses, or ScalarType leafs.
 	 * 
 	 * @param thatReferenceObject
 	 * @return
 	 */
 	private XMLTranslationException collectionElementTypeException(
 			Object thatReferenceObject)
 	{
 		return new XMLTranslationException("Collections MUST contain " +
 				"objects of class derived from ElementState or Scalars, but " +
 				thatReferenceObject +" contains some that aren't.");
 	}
     
 	/**
 	/**
 	 * Translates a tree of ElementState objects, and writes the output to the Appendable passed in
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param thatClass
 	 * @param fieldToXMLOptimizations
 	 * @param appendable		Appendable to translate into. Must be non-null. Can be a Writer, OutputStream, ...
 	 * 
 	 * @throws XMLTranslationException if a problem arises during translation.
 	 * Problems with Field access are possible, but very unlikely.
 	 * @throws IOException
 	 */
 	private void translateToXMLAppendable(Class thatClass, FieldToXMLOptimizations fieldToXMLOptimizations, Appendable appendable)
 	throws XMLTranslationException, IOException
 	{
 		this.preTranslationProcessingHook();
 
 		final String startOpenTag = fieldToXMLOptimizations.startOpenTag();
 		appendable.append(startOpenTag);
 
 		ArrayList<FieldToXMLOptimizations> xmlnsF2XOs	= optimizations.xmlnsAttributeOptimizations();
 		int numXmlnsAttributes 			= (xmlnsF2XOs == null) ? 0 : xmlnsF2XOs.size();
 		if (numXmlnsAttributes > 0)
 		{
 			for (int i=0; i<numXmlnsAttributes; i++)
 			{
 				FieldToXMLOptimizations xmlnsF2Xo	= xmlnsF2XOs.get(i);
 				xmlnsF2Xo.xmlnsAttr(appendable);
 			}			
 		}
 		ArrayList<FieldToXMLOptimizations> attributeF2XOs	= optimizations.attributeFieldOptimizations();
 		int numAttributes 			= attributeF2XOs.size();
 
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i=0; i<numAttributes; i++)
 				{
 					// iterate through fields
 					FieldToXMLOptimizations childF2Xo	= attributeF2XOs.get(i);
 					childF2Xo.appendValueAsAttribute(appendable, this);
 				}
 			} catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new XMLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 		//ArrayList<Field> elementFields		= optimizations.elementFields();
 		ArrayList<FieldToXMLOptimizations> elementF2XOs	= optimizations.elementFieldOptimizations();
 		int numElements						= elementF2XOs.size();
 
 		StringBuilder textNode = this.textNodeBuffy;
 		//TODO -- fix textNode == null -- should be size() == 0 or some such
 		if ((numElements == 0) && (textNode == null))
 		{
 			appendable.append('/').append('>');	// done! completely close element behind attributes				
 		}
 		else
 		{
 			if (startOpenTag.length() > 0)
 				appendable.append('>');	// close open tag behind attributes unless in a nested namespace root
 			if (textNode != null) 
 			{	
 				//TODO -- might need to trim the buffy here!
 				//if (textNode.length() > 0 -- not needed with current impl, which doesnt do append to text node if trim -> empty string
 				//if (textNode.length() > 0)
 				XMLTools.escapeXML(appendable, textNode);
 			}
 			for (int i=0; i<numElements; i++)
 			{
 //				NodeToJavaOptimizations pte		= optimizations.getPTEByFieldName(thatFieldName);
 				FieldToXMLOptimizations childF2XO	= elementF2XOs.get(i);
 				//if (XmlTools.representAsLeafNode(thatField))
 				final int childOptimizationsType 	= childF2XO.type();
 				if (childOptimizationsType == LEAF_NODE_VALUE)
 				{
 					try
 					{
 						childF2XO.appendLeaf(appendable, this);
 					} catch (Exception e)
 					{
 						throw new XMLTranslationException("TranslateToXML for leaf node " + this, e);
 					}				
 				}
 				else
 				{
 					Object thatReferenceObject	= null;
 					Field childField			= childF2XO.field();
 					try
 					{
 						thatReferenceObject		= childField.get(this);
 					}
 					catch (IllegalAccessException e)
 					{
 						debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 						childField.setAccessible(true);
 						try
 						{
 							thatReferenceObject	= childField.get(this);
 						} catch (IllegalAccessException e1)
 						{
 							error("Can't access " + childField.getName());
 							e1.printStackTrace();
 						}
 					}
 					// ignore null reference objects
 					if (thatReferenceObject == null)
 						continue;
 
 					final boolean isScalar		= (childOptimizationsType == COLLECTION_SCALAR) || (childOptimizationsType == MAP_SCALAR);
 					// gets Collection object directly or through Map.values()
 					Collection thatCollection;
 					switch (childOptimizationsType)
 					{
 					case COLLECTION_ELEMENT:
 					case COLLECTION_SCALAR:
 					case MAP_ELEMENT:
 					case MAP_SCALAR:
 						thatCollection			= XMLTools.getCollection(thatReferenceObject);
 						break;
 					default:
 						thatCollection			= null;
 					break;
 					}
 
 					if (thatCollection != null)
 					{
 						//if the object is a collection, 
 						//basically iterate thru the collection and emit XML from each element
 						final Iterator iterator			= thatCollection.iterator();
 //						Class childClass				= iterator.hasNext() ? iterator.
 
 						while (iterator.hasNext())
 						{
 							Object next = iterator.next();
 							if (isScalar)	// leaf node!
 							{
 								try
 								{
 									childF2XO.appendCollectionLeaf(appendable, next);
 								} catch (IllegalArgumentException e)
 								{
 									throw new XMLTranslationException("TranslateToXML for collection leaf " + this, e);
 								} catch (IllegalAccessException e)
 								{
 									throw new XMLTranslationException("TranslateToXML for collection leaf " + this, e);
 								}
 							}
 							else if (next instanceof ElementState)
 							{
 								ElementState collectionSubElementState = (ElementState) next;
 								//collectionSubElementState.translateToXML(collectionSubElementState.getClass(), true, nodeNumber, buffy, REGULAR_NESTED_ELEMENT);
 								final Class<? extends ElementState> collectionElementClass = collectionSubElementState.getClass();
 								FieldToXMLOptimizations collectionElementEntry		= optimizations.fieldToJavaOptimizations(childF2XO, collectionElementClass);
 								collectionSubElementState.translateToXMLAppendable(collectionElementClass, collectionElementEntry, appendable);
 							}
 							else
 								throw collectionElementTypeException(thatReferenceObject);
 						}
 					}
 					else if (thatReferenceObject instanceof ElementState)
 					{	// one of our nested elements, so recurse
 						ElementState thatElementState	= (ElementState) thatReferenceObject;
 						// if the field type is the same type of the instance (that is, if no subclassing),
 						// then use the field name to determine the XML tag name.
 						// if the field object is an instance of a subclass that extends the declared type of the
 						// field, use the instance's type to determine the XML tag name.
 						Class thatNewClass			= thatElementState.getClass();
 						// debug("checking: " + thatReferenceObject+" w " + thatNewClass+", " + thatField.getType());
 						FieldToXMLOptimizations nestedF2XO = thatNewClass.equals(childField.getType()) ?
 								childF2XO : fieldToXMLOptimizations(childField, thatNewClass);
 
 						thatElementState.translateToXMLAppendable(thatNewClass, nestedF2XO, appendable);
 					}
 				}
 			} //end of for each element child
 			HashMap<String, ElementState> nestedNameSpaces = this.nestedNameSpaces;
 			if (nestedNameSpaces != null)
 			{
 				for (ElementState nestedNSE : nestedNameSpaces.values())
 				{
 					Class<? extends ElementState> nestedNSClass = nestedNSE.getClass();
 					// translate nested namespace root
 					FieldToXMLOptimizations nestedNsF2XO	=
 						nestedNSE.optimizations.rootFieldToXMLOptimizations(nestedNSClass);
 					nestedNSE.translateToXMLAppendable(nestedNSClass, nestedNsF2XO, appendable);
 				}
 
 			}
 			// end the element
 			appendable.append(fieldToXMLOptimizations.closeTag())/* .append('\n') */;
 
 		} // end if no nested elements or text node
 	}
 	
 	/**
 	 * Create a W3C Document object from this. 
 	 * That is, go back, from our nice, strongly typed tree, to an untyped one.
 	 * 
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public Document translateToDOM() 
 	throws XMLTranslationException
 	{
 		DocumentBuilderFactory factory	= DocumentBuilderFactory.newInstance();
 		try
 		{
 			DocumentBuilder docBuilder 	= factory.newDocumentBuilder();
 			Document dom				= docBuilder.newDocument();
 /*			
 			String nsURN				= "http://rssnamespace.org/feedburner/ext/1.0";
 //			Element root 				= dom.createElementNS(nsURN, "rss");
 			Element root 				= dom.createElement("rss");
 			dom.appendChild(root);
 			
 			Attr attr 					= dom.createAttribute("xmlns:feedburner");
 			attr.setValue(nsURN);
 			root.setAttributeNode(attr);
 			attr 					= dom.createAttribute("xmlns:media");
 			attr.setValue(nsURN);
 			root.setAttributeNode(attr);
 			println("yo!");
 */
 			Class rootClass				= optimizations.thatClass;
 
 			translateToDOM(rootClass, optimizations.rootFieldToXMLOptimizations(rootClass), dom, dom);
 		
 			return dom;
 		} catch (ParserConfigurationException e)
 		{
 			throw new XMLTranslationException("Couldn't acquire empty Document.", e);
 		}
 	}
 
 	/**
 	/**
 	 * Translates a tree of ElementState objects, and writes the output to the Appendable passed in
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param thatClass
 	 * @param fieldToXMLOptimizations
 	 * @param dom TODO
 	 * @param appendable		Appendable to translate into. Must be non-null. Can be a Writer, OutputStream, ...
 	 * @throws XMLTranslationException if a problem arises during translation.
 	 * Problems with Field access are possible, but very unlikely.
 	 * @throws IOException
 	 */
 	private void translateToDOM(Class thatClass, FieldToXMLOptimizations fieldToXMLOptimizations, Node parentNode, Document dom)
 	throws XMLTranslationException
 	{
 		this.preTranslationProcessingHook();
 		
 		Element elementNode;
 		ArrayList<FieldToXMLOptimizations> xmlnsF2XOs	= optimizations.xmlnsAttributeOptimizations();
 		if (fieldToXMLOptimizations.startOpenTag().length() > 0)
 		{
 			String tagName 							= fieldToXMLOptimizations.tagName();
 			elementNode								= dom.createElement(tagName);		
 			parentNode.appendChild(elementNode);
 			if ((xmlnsF2XOs != null) && (xmlnsF2XOs.size() > 0))
 			{
 				int numXmlnsAttributes 					= xmlnsF2XOs.size();
 				for (int i=0; i<numXmlnsAttributes; i++)
 				{
 					FieldToXMLOptimizations xmlnsF2Xo	= xmlnsF2XOs.get(i);
 					xmlnsF2Xo.xmlnsAttr(elementNode, dom);
 				}
 			}
 		}
 		else	// nested namespace
 			elementNode								= (Element) parentNode;
 
 		ArrayList<FieldToXMLOptimizations> attributeF2XOs	= optimizations.attributeFieldOptimizations();
 		int numAttributes 			= attributeF2XOs.size();
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i=0; i<numAttributes; i++)
 				{
 					// iterate through fields
 					FieldToXMLOptimizations childF2Xo	= attributeF2XOs.get(i);
 					childF2Xo.setAttribute(elementNode, this);
 				}
 			} catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new XMLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 		
 		//TODO -- deal with text node child
 		
 		ArrayList<FieldToXMLOptimizations> elementF2XOs	= optimizations.elementFieldOptimizations();
 		int numElements						= elementF2XOs.size();
 
 		for (int i=0; i<numElements; i++)
 		{
 			FieldToXMLOptimizations childF2Xo	= elementF2XOs.get(i);
 
 			final int childOptimizationsType 	= childF2Xo.type();
 			if (childOptimizationsType == LEAF_NODE_VALUE)
 			{
 				try
 				{
 					childF2Xo.appendLeaf(elementNode, this);
 				} catch (Exception e)
 				{
 					throw new XMLTranslationException("TranslateToXML for leaf node " + this, e);
 				}				
 			}
 			else
 			{
 				Object thatReferenceObject	= null;
 				Field childField			= childF2Xo.field();
 				try
 				{
 					thatReferenceObject		= childField.get(this);
 				}
 				catch (IllegalAccessException e)
 				{
 					throw new XMLTranslationException("Couldn't access " + childF2Xo.tagName());
 				}
 				// ignore null reference objects
 				if (thatReferenceObject == null)
 					continue;
 
 				final boolean isScalar		= (childOptimizationsType == COLLECTION_SCALAR) || (childOptimizationsType == MAP_SCALAR);
 				// gets Collection object directly or through Map.values()
 				Collection thatCollection;
 				switch (childOptimizationsType)
 				{
 				case COLLECTION_ELEMENT:
 				case COLLECTION_SCALAR:
 				case MAP_ELEMENT:
 				case MAP_SCALAR:
 					thatCollection			= XMLTools.getCollection(thatReferenceObject);
 					break;
 				default:
 					thatCollection			= null;
 				break;
 				}
 
 				if (thatCollection != null)
 				{
 					//if the object is a collection, 
 					//basically iterate thru the collection and emit XML from each element
 					final Iterator iterator			= thatCollection.iterator();
 //					Class childClass				= iterator.hasNext() ? iterator.
 
 					while (iterator.hasNext())
 					{
 						Object next = iterator.next();
 						if (isScalar)	// leaf node!
 						{
 							try
 							{
								childF2Xo.appendLeaf(elementNode, next);
 							} catch (IllegalArgumentException e)
 							{
 								throw new XMLTranslationException("TranslateToXML for collection leaf " + this, e);
 							} catch (IllegalAccessException e)
 							{
 								throw new XMLTranslationException("TranslateToXML for collection leaf " + this, e);
 							}
 						}
 						else if (next instanceof ElementState)
 						{
 							ElementState collectionSubElementState = (ElementState) next;
 							//collectionSubElementState.translateToXML(collectionSubElementState.getClass(), true, nodeNumber, buffy, REGULAR_NESTED_ELEMENT);
 							final Class<? extends ElementState> collectionElementClass = collectionSubElementState.getClass();
 							FieldToXMLOptimizations collectionElementF2XO		= optimizations.fieldToJavaOptimizations(childF2Xo, collectionElementClass);
 							collectionSubElementState.translateToDOM(collectionElementClass, collectionElementF2XO, elementNode, dom);
 						}
 						else
 							throw new XMLTranslationException("Collections MUST contain " +
 									"objects of class derived from ElementState or XML Strings, but " +
 									thatReferenceObject +" contains some that aren't.");
 
 					}
 				}
 				else if (thatReferenceObject instanceof ElementState)
 				{	// one of our nested elements, so recurse
 					ElementState thatElementState	= (ElementState) thatReferenceObject;
 					// if the field type is the same type of the instance (that is, if no subclassing),
 					// then use the field name to determine the XML tag name.
 					// if the field object is an instance of a subclass that extends the declared type of the
 					// field, use the instance's type to determine the XML tag name.
 					Class thatNewClass			= thatElementState.getClass();
 					// debug("checking: " + thatReferenceObject+" w " + thatNewClass+", " + thatField.getType());
 					FieldToXMLOptimizations nestedTagMapEntry = thatNewClass.equals(childField.getType()) ?
 							childF2Xo : fieldToXMLOptimizations(childField, thatNewClass);
 
 					thatElementState.translateToDOM(thatNewClass, nestedTagMapEntry, elementNode, dom);
 				}
 			}
 		} //end of for each element child
 		if (nestedNameSpaces != null)
 		{
 			for (ElementState nestedNSE : nestedNameSpaces.values())
 			{
 				//TODO -- where do we get optimizations for nested namespace elements?
 				Class<? extends ElementState> nestedNSClass = nestedNSE.getClass();
 				FieldToXMLOptimizations nestedNsF2XO	=
 					nestedNSE.optimizations.rootFieldToXMLOptimizations(nestedNSClass);
 				nestedNSE.translateToDOM(nestedNSClass, nestedNsF2XO, elementNode, dom);
 			}
 		}
 	}
 
     /**
      * Returns the precision of floating point numbers associated with this
      * instance of ElementState.
      * 
      * Subclasses may override this method, which is particularly useful if a
      * class should have a certain floating point precision associated with it.
      * 
      * @return the floating point precision to be used when translating this to
      *         XML.
      */
     protected short floatingPrecision()
     {
         return this.floatingPrecision;
     }
     
     /**
 	 * Translate our representation of a leaf node to XML.
 	 * 
 	 * @param buffy
 	 * @param leafElementName
 	 * @param leafValue
 	 * @param type
 	 * @param isCDATA
 	 */
 	void appendLeafXML(StringBuilder buffy, FieldToXMLOptimizations fieldToXMLOptimizations, String leafValue)
 	{
 		appendLeafXML(buffy, fieldToXMLOptimizations.tagName(), leafValue, fieldToXMLOptimizations.isNeedsEscaping(), fieldToXMLOptimizations.isCDATA());
 	}
 	/**
 	 * Translate our representation of a leaf node to XML.
 	 * 
 	 * @param buffy
 	 * @param leafElementName
 	 * @param leafValue
 	 * @param type
 	 * @param isCDATA
 	 */
 	void appendLeafXML(StringBuilder buffy, String leafElementName, String leafValue, boolean needsEscaping, boolean isCDATA)
 	{
 		if (!ecologylab.xml.types.scalar.ScalarType.DEFAULT_VALUE_STRING.equals(leafValue))
 		{
 			buffy.append('<').append(leafElementName).append('>');
 			
 			if (isCDATA)
 			{
 				buffy.append("<![CDATA[");
 				buffy.append(leafValue);
 				buffy.append("]]>");
 			}
 			else
 			{
 				if (needsEscaping)
 					XMLTools.escapeXML(buffy, leafValue);
 				else
 					buffy.append(leafValue);
 			}
 			buffy.append("</").append(leafElementName).append('>').append('\n');
 		}
 	}
 
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param purl	ParsedURL for the XML document that needs to be translated.
 	 * @param translationSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 	   Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLDOM(ParsedURL purl,
 												TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		return translateFromXMLDOM(buildDOM(purl), translationSpace);
 	}
 
 	/**
 	 * Translate data from a ParseURL from XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param xmlStream	An InputStream to the XML that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return						Strongly typed tree of ElementState objects.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXML(ParsedURL purl, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		if (purl == null)
 			throw new XMLTranslationException("Null PURL", NULL_PURL);
 
 		if (!purl.isNotFileOrExists())
 			throw new XMLTranslationException("Can't find " + purl.toString(), FILE_NOT_FOUND);
 		
 		return useDOMForTranslateTo ? translateFromXMLDOM(purl, translationSpace) : translateFromXMLSAX(purl, translationSpace);	
 	}
 /**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * <p/>
 	 * Uses the default globalNameSpace as the basis for translation.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlDocumentURL	URL for the XML document that needs to be translated.
 	 * 
 	 * @param translationSpace		NameSpace that provides basis for translation.
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 */
 
 	public static ElementState translateFromXMLDOM(URL xmlDocumentURL,
 												TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 	   Document document	= buildDOM(xmlDocumentURL);
 	   return (document == null) ? 
 		  null : translateFromXMLDOM(document, translationSpace);
 	}
 	
 	public static ElementState translateFromXML(URL url,
 			TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		return useDOMForTranslateTo ? translateFromXMLDOM(url, translationSpace) : translateFromXMLSAX(url, translationSpace);
 	}
 	/**
 	 * Translate a file from XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use DOM parsing -- builds an intermediate DOM object.
 	 * 
 	 * @param xmlFile				XML source material.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return						Strongly typed tree of ElementState objects.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLDOM(File xmlFile, 
 												TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 	   Document document	= buildDOM(xmlFile);
 	   ElementState result	= null;
 	   if (document != null)
 		  result			= translateFromXMLDOM(document, translationSpace);
 	   return result;
 	}
 
 	/**
 	 * Translate a file from XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param xmlFile				XML source material.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return						Strongly typed tree of ElementState objects.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXML(File xmlFile, 
 			TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		return useDOMForTranslateTo ? translateFromXMLDOM(xmlFile, translationSpace) : translateFromXMLSAX(xmlFile, translationSpace);
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
 	 * @param fileName	the name of the XML file that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLDOM(String fileName,
 												TranslationScope translationSpace)
 		throws XMLTranslationException
 	{
 		Document document	= buildDOM(fileName);
 		return (document == null) ? null : translateFromXMLDOM(document, translationSpace);
 	}
 
 	/**
 	 * Translate a file XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param fileName	the name of the XML file that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return						Strongly typed tree of ElementState objects.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXML(String fileName,
 			TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		File xmlFile	= new File(fileName);
 		if (!xmlFile.exists() && !xmlFile.canRead())
 			throw new XMLTranslationException("Can't access " + xmlFile.getAbsolutePath(), FILE_NOT_FOUND);
 
 		return useDOMForTranslateTo ? translateFromXMLDOM(xmlFile, translationSpace) : translateFromXMLSAX(xmlFile, translationSpace);	
 	}
 
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Build a DOM first. Then, recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * @param xmlStream	An InputStream to the XML that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLDOM(InputStream xmlStream, TranslationScope nameSpace)
 	throws XMLTranslationException
 	{
 		Document document	= buildDOM(xmlStream);
 		return (document == null) ? null : translateFromXMLDOM(document, nameSpace);
 	}	
 	
 	/**
 	 * Translate an InputStream from XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param xmlStream	An InputStream to the XML that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return						Strongly typed tree of ElementState objects.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXML(InputStream xmlStream, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		return useDOMForTranslateTo ? translateFromXMLDOM(xmlStream, translationSpace) : translateFromXMLSAX(xmlStream, translationSpace);	
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
 	 * @param charSequence	the actual XML that needs to be translated.
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLDOMCharSequence(CharSequence charSequence, 
 													  int charsetType,
 													  TranslationScope translationSpace)
 		throws XMLTranslationException
 	{
 		Document dom		= buildDOMFromXMLCharSequence(charSequence, charsetType);
 		return (dom == null) ? null : translateFromXMLRootNode(dom.getDocumentElement(), translationSpace);
 	}
 
 	/**
 	 * Given an XML-formatted String, uses charset type UTF-8 to create
 	 * a stream, and build a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the
 	 * structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into
 	 * a tree of state-objects. Uses the default UTF8 charset.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param charSequence	the actual XML that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLDOMCharSequence(CharSequence charSequence,
 														 TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		   return translateFromXMLDOMCharSequence(charSequence, StringInputStream.UTF8, translationSpace);
 	}
 	
 	/**
 	 * Translate a String of XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param xmlString				the actual XML that needs to be translated.
 	 * @param translationSpace		Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return						Strongly typed tree of ElementState objects.
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLCharSequence(CharSequence xmlString,
 													  		TranslationScope translationSpace)
 		throws XMLTranslationException
 	{
 		return useDOMForTranslateTo ? translateFromXMLDOMCharSequence(xmlString, translationSpace) : translateFromXMLSAX(xmlString, translationSpace);	
 	}
 	
 	/**
 	 * Given the Document object for an XML DOM, builds a tree of equivalent
 	 * ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the 
 	 * structure of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param doc	Document object for DOM tree that needs to be translated.
 	 * @param translationSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 		Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLDOM(Document doc, 
 												TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		return translateFromXMLRootNode(doc.getDocumentElement(), translationSpace);
 	}
 
 	/**
 	 * A recursive DOM-based translation translateFromXML(...).
 	 * Entry point for the old DOM-based parsing.
 	 * <p/>
 	 * Typically, this method is initially passed the root Node of an XML DOM,
 	 * from which it builds a tree of equivalent ElementState objects.
 	 * It does this by recursively calling itself for each node/subtree of 
 	 * ElementState objects.
 	 * 
 	 * The method translates any tree of DOM into a tree of Java objects, each
 	 * of which is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of 
 	 * classes derived from ElementState, which corresponds to the structure 
 	 * of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file, and access the 
 	 * root Node. S/he passes it to this method to create a Java hierarchy 
 	 * equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlRootNode		Root node of the DOM tree that needs to be translated.
 	 * @param translationSpace		NameSpace that provides basis for translation.
 	 * @return 				Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLRootNode(Node xmlRootNode,
 												TranslationScope translationSpace)
 	   throws XMLTranslationException
 	{
 	   // find the class for the new object derived from ElementState
 		Class<? extends ElementState> stateClass		= null;
 		String tagName						= xmlRootNode.getNodeName();
 		try
 		{
 			//TODO -- use class-level @xml_tag if it was declared?!
 			stateClass						= translationSpace.xmlTagToClass(tagName);
 			if (stateClass == null)
 			{
 				int colonIndex				= tagName.indexOf(':');
 				if (colonIndex > 1)
 				{   // we are dealing with an XML Namespace
 					String nameSpacePrefix	= tagName.substring(0, colonIndex);
 					tagName					= tagName.substring(colonIndex + 1);
 					//TODO -- handle namespace properly!
 				}
 			}
 			if (stateClass != null)
 			{
 				ElementState rootState		= XMLTools.getInstance(stateClass);
 				if (rootState != null)
 				{
 					rootState.setupRoot();
 					rootState.translateFromXMLNode(xmlRootNode, translationSpace);
 
 					rootState.postTranslationProcessingHook();
 
 					return rootState;
 				}
 			}
 			else
 			{
 				throw new RootElementException(tagName, translationSpace);
 			}
 		}
 		catch (Exception e)
 		{
 		   StackTraceElement stackTrace[] = e.getStackTrace();
 		   println("XML Translation WARNING: Exception while trying to translate XML element <" 
 				   + tagName+ "> class="+stateClass + ". Ignored.\nThe exception was " 
 				   + e.getMessage() + " from " +stackTrace[0] +" " + stackTrace[1]);
 		   //e.printStackTrace();
 //		   throw new XmlTranslationException("All ElementState subclasses"
 //							       + "MUST contain an empty constructor, but "+
 //								   stateClass+" doesn't seem to.");
 		}
 		return null;
 	 }	
 	
 	/**
 	 * Link new born root element to its Optimizations and create an elementByIdMap for it.
 	 */
 	void setupRoot()
 	{
 		elementByIdMap		= new HashMap<String, ElementState>();
 		optimizations		= Optimizations.lookupRootOptimizations(this);	
 	}
 /**
      * A recursive method -- the core of the old DOM-Based translateFromXML(...).
      * <p/>
      * The method translates any tree of DOM into a tree of Java objects, each
      * of which is an instance of a subclass of ElementState.
      * The operation of the method is predicated on the existence of a tree of
      * classes derived from ElementState, which corresponds to the structure 
 	 * of the XML DOM that needs to be parsed.
      * 
      * Before calling the version of this method with this signature, the
 	 *  programmer needs to create a DOM from the XML file, and access the root
      * Node. S/he passes it to this method to create a Java hierarchy 
 	 * equivalent to the DOM.
      * 
      * Recursively parses the XML nodes in DFS order and translates them into
 	 * a tree of state-objects.
      * 
      * @param xmlNode	Root node of the DOM tree that needs to be translated.
      * @param translationSpace		NameSpace that provides basis for translation.
      * @return 			Parent ElementState object of the corresponding Java tree.
      */
 	void translateFromXMLNode(Node xmlNode, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		// translate attribtues
 		if (xmlNode.hasAttributes())
 		{
 			NamedNodeMap xmlNodeAttributes = xmlNode.getAttributes();
 			
 			int numAttributes = xmlNodeAttributes.getLength();
 			for (int i = 0; i < numAttributes; i++) 
 			{
 				final Node attrNode 	= xmlNodeAttributes.item(i);
 				final String tag		= attrNode.getNodeName();
 				final String value		= attrNode.getNodeValue();
                
 				if (value != null)
 				{
 					NodeToJavaOptimizations njo	=
 						optimizations.nodeToJavaOptimizations(translationSpace, this, tag, true);
 					switch (njo.type())
 					{
 					case REGULAR_ATTRIBUTE:
 						njo.setFieldToScalar(this, value);
 						// the value can become a unique id for looking up this
 						if ("id".equals(njo.tag()))
 							this.elementByIdMap.put(value, this);
 						break;
 					case XMLNS_ATTRIBUTE:
 						njo.registerXMLNS(this, value);
 						break;
 					default:
 						break;	
 					}
 				}
 			}
 		}
 		
 		// translate nested elements (aka children):
 		// loop through them, recursively build them, and add them to ourself
 		NodeList childNodes	= xmlNode.getChildNodes();
 		int numChilds		= childNodes.getLength();
 	
 		for (int i = 0; i < numChilds; i++)
 		{
 			Node childNode		= childNodes.item(i);
 			short childNodeType	= childNode.getNodeType();
 			if ((childNodeType == Node.TEXT_NODE) || (childNodeType == Node.CDATA_SECTION_NODE))
 			{
 				appendTextNodeString(childNode.getNodeValue());
 			}
 			else
 			{
 				NodeToJavaOptimizations njo		= optimizations.elementNodeToJavaOptimizations(translationSpace, this, childNode);
 				NodeToJavaOptimizations nsNJO	= njo.nestedPTE();
 				NodeToJavaOptimizations	activeNJO;
 				ElementState	activeES;
 				if (nsNJO != null)
 				{
 					activeNJO				= nsNJO;
 					// get (create if necessary) the ElementState object corresponding to the XML Namespace
 					activeES				= (ElementState) ReflectionTools.getFieldValue(this, njo.field());
 					if (activeES == null)
 					{	// first time using the Namespace element, so we gotta create it
 						activeES			= (ElementState) njo.domFormChildElement(this, null, false);
 						ReflectionTools.setFieldValue(this, njo.field(), activeES);
 					}
 				}
 				else
 				{
 					activeNJO				= njo;
 					activeES				= this;
 				}
 				switch (njo.type())
 				{
 				case REGULAR_NESTED_ELEMENT:
 					activeNJO.domFormNestedElementAndSetField(activeES, childNode);
 					break;
 				case LEAF_NODE_VALUE:
 					activeNJO.setScalarFieldWithLeafNode(activeES, childNode);
 					break;
 				case COLLECTION_ELEMENT:
 					activeNJO.domFormElementAndAddToCollection(activeES, childNode);
 					break;
 				case NAME_SPACE_NESTED_ELEMENT:
 //					debug("WOW!!! got NAME_SPACE_NESTED_ELEMENT: " + childNode.getNodeName());
 					ElementState nsContext			= getNestedNameSpace(activeNJO.nameSpaceID());
 					activeNJO.domFormNestedElementAndSetField(nsContext, childNode);
 					break;
 				case COLLECTION_SCALAR:
 					activeNJO.addLeafNodeToCollection(activeES, childNode);
 					break;
 //				case MAP_SCALAR:
 //					activeNJO.addLeafNodeToMap(activeES, childNode);
 //					break;
 				case MAP_ELEMENT:
 					activeNJO.domFormElementAndToMap(activeES, childNode);
 					break;
 				case OTHER_NESTED_ELEMENT:
 					activeES.addNestedElement(activeNJO, childNode);
 					break;
 				case IGNORED_ELEMENT:
 				case BAD_FIELD:
 				default:
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param charSequence
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLSAX(CharSequence charSequence, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		ElementStateSAXHandler saxHandler	= new ElementStateSAXHandler(translationSpace);
 		return saxHandler.parse(charSequence);
 	}
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param purl
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLSAX(ParsedURL purl, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		ElementStateSAXHandler saxHandler	= new ElementStateSAXHandler(translationSpace);
 		return saxHandler.parse(purl);
 	}
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param url
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLSAX(URL url, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		ElementStateSAXHandler saxHandler	= new ElementStateSAXHandler(translationSpace);
 		return saxHandler.parse(url);
 	}
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param file
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLSAX(File file, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		ElementStateSAXHandler saxHandler	= new ElementStateSAXHandler(translationSpace);
 		return saxHandler.parse(file);
 	}
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param inputStream
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static ElementState translateFromXMLSAX(InputStream inputStream, TranslationScope translationSpace)
 	throws XMLTranslationException
 	{
 		ElementStateSAXHandler saxHandler	= new ElementStateSAXHandler(translationSpace);
 		return saxHandler.parse(inputStream);
 	}
 	/**
 	 * Used in SAX parsing to unmarshall attributes into fields.
 	 * 
 	 * @param translationSpace
 	 * @param attributes
 	 */
 	void translateAttributes(TranslationScope translationSpace, Attributes attributes)
 	{
 		int numAttributes	= attributes.getLength();
 		for (int i=0; i<numAttributes; i++)
 		{
 			//TODO -- figure out what we're doing if there's a colon and a namespace
 			final String tag		= attributes.getQName(i);
 			final String value	= attributes.getValue(i);
 			//TODO String attrType = getType()?!
 			if (value != null)
 			{
 				NodeToJavaOptimizations njo	= 
 					optimizations.nodeToJavaOptimizations(translationSpace, this, tag, true);
 				switch (njo.type())
 				{
 				case REGULAR_ATTRIBUTE:
 					njo.setFieldToScalar(this, value);
 					// the value can become a unique id for looking up this
 					//TODO -- could support the ID type for the node here!
 					if ("id".equals(njo.tag()))
 						this.elementByIdMap.put(value, this);
 					break;
 				case XMLNS_ATTRIBUTE:
 					njo.registerXMLNS(this, value);
 					break;
 				default:
 					break;	
 				}
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
 	
 	static public Document buildDOM(ParsedURL purl)
 	{
 		return purl.isFile() ? buildDOM(purl.file()) : buildDOM(purl.url());
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
 		Document document	= null;
 		try
 		{
     	  DocumentBuilder builder = XMLTools.getDocumentBuilder();
     	  createErrorHandler(builder);
      	  document = builder.parse(file);
 		} 
 		
 		catch (SAXParseException spe) 
 		{
 			// Error generated by the parser
 		    reportException(spe, file.getAbsolutePath());
 	  	}
 	  	catch (SAXException sxe) 
 	  	{
 		    // Error generated during parsing
 		    reportException(sxe);
 	   	}
 	  	catch(Exception e)
 	  	{
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
 	static public Document buildDOM(String xmlFileOrURLName)
 	{		       
 		Document document	= null;
 		try
 		{
     	  DocumentBuilder builder = XMLTools.getDocumentBuilder();
     	  createErrorHandler(builder);
     	  if( !xmlFileOrURLName.contains("://") )
     		  xmlFileOrURLName = "file:///" + xmlFileOrURLName;
     	  document = builder.parse(xmlFileOrURLName);
 		} 
 		
 		catch (SAXParseException spe) 
 		{
 			// Error generated by the parser
 		    reportException(spe, xmlFileOrURLName);
 	  	}
 	  	catch (SAXException sxe) 
 	  	{
 		    // Error generated during parsing
 		    reportException(sxe);
 	   	}
         catch(IOException e)
         {
             e.printStackTrace();
         }
 	  	catch(Exception e)
 	  	{
 	  		e.printStackTrace();
 	  	}
 		return document;
 	}
 	/**
 	 * Report exception during DOM parsing.
 	 * 
 	 * @param sxe
 	 */
 	private static void reportException(SAXException sxe) 
 	{
 		Exception  x = sxe;
 		if (sxe.getException() != null)
 		  x = sxe.getException();
 		x.printStackTrace();
 	}
 	/**
 	 * Report exception during DOM parsing.
 	 * 
 	 * @param spe
 	 * @param xmlFileOrURLName
 	 */
 	private static void reportException(SAXParseException spe, String xmlFileOrURLName)
 	{
 		println(xmlFileOrURLName + ":\n** Parsing error" + ", line " + spe.getLineNumber() + ", urn " + spe.getSystemId());
 		println("   " + spe.getMessage());
   
 		// Use the contained exception, if any
 		Exception  x = spe;
 		if (spe.getException() != null)
 		   x = spe.getException();
 		x.printStackTrace();
 	}
 
 	/**
 	 * This method creates a DOM Document from the XML file at a given URI,
 	 * which could be a local file or a URL.
 	 *
 	 * @param inStream	InputStream from which the DOM is to be created
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
 		catch (SAXParseException spe)
 		{
 			// Error generated by the parser
 		    println("ERROR parsing DOM in" + inStream + ":\n\t** Parsing error on line " + spe.getLineNumber() + ", urn=" + spe.getSystemId());
 		    println("   " + spe.getMessage());
 	  	}
 	  	catch (SAXException sxe)
 	  	{   // Error generated during parsing
 		    Exception  x = sxe;
 		    if (sxe.getException() != null)
 		      x = sxe.getException();
 		    x.printStackTrace();
 	   	}
 	   	catch (ParserConfigurationException pce)
 	   	{
 		    // Parser with specified options can't be built
 		    pce.printStackTrace();
 	   	}
 	   	catch (IOException ioe)
 	   	{
 		    // I/O error
 		    ioe.printStackTrace();
 	  	}
 	  	catch(FactoryConfigurationError fce)
 	  	{
 	  		fce.printStackTrace();
 	  	}
 	  	catch(Exception e)
 	  	{
 	  		e.printStackTrace();
 	  	}
 		return document;
 	}
 	/**
 	 * This method creates a DOM Document from an XML-formatted String.
 	 *
 	 * @param charSequence	the XML-formatted String from which the DOM is to be created
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOMFromXMLCharSequence(CharSequence charSequence,
 												 int charsetType)
     {
 	   InputStream xmlStream =
 		  new StringInputStream(charSequence, charsetType);
 
 	   return buildDOM(xmlStream);
 	}
 
 	/**
 	 * This method creates a DOM Document from an XML-formatted String,
 	 * encoded as UTF8.
 	 *
 	 * @param charSequence		the XML-formatted String from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOMFromXMLString(CharSequence charSequence)
     {
 	   return buildDOMFromXMLCharSequence(charSequence, UTF8);
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
 		        + ", urn " + err.getSystemId());
 		      println("   " + err.getMessage());
 		    }
 	    
 	  	}  
 		); 
   	}
 
 	//////////////// methods to generate XML, and write to a file /////////////
 
 	/**
 	 * 	Translate to XML, then write the result to a file, while formatting nicely.
 	 */
 	public void writePrettyXML(String xmlFileName)
 	throws XMLTranslationException
 	{
 		if(!xmlFileName.endsWith(".xml") && !xmlFileName.endsWith(".XML"))
 		{
 			xmlFileName	= xmlFileName + ".xml";
 		}
 		writePrettyXML(new File(xmlFileName));
 	}
 	
 /**
  * 	Translate to XML, then write the result to a file.
  * 
  * 	@param xmlFile		the file in which the xml needs to be saved
  */	
 	public void writePrettyXML(File xmlFile)
 		throws XMLTranslationException
 	{
 	 	XMLTools.writePrettyXML(translateToDOM(), xmlFile);
 	}
 	
 	/**
 	 * 	Translate to XML, then write the result to a file.
 	 * 
 	 * 	@param xmlFile		the file in which the xml needs to be saved
 	 */	
 	public void writePrettyXML(OutputStream outputStream)
 	throws XMLTranslationException
 	{
 		XMLTools.writePrettyXML(translateToDOM(), outputStream);
 	}
 		
 	//////////////// helper methods used by translateToXML() //////////////////
 
 /**
  * Get a tag translation object that corresponds to the fieldName,
  * with this class. If necessary, form that tag translation object,
  * and cache it.
  */
 	protected FieldToXMLOptimizations fieldToXMLOptimizations(Field field, Class<? extends ElementState> thatClass)
 	{
 		return optimizations.fieldToXMLOptimizations(field, thatClass);
 	}
 
 	//////////////// helper methods used by translateFromXML() ////////////////
 	/**
 	 * Set a field that is an extended primitive -- a non ElementState --
 	 * using the type registry.
 	 * 
 	 * @param field
 	 * @param fieldValue
 	 * @return	true if the Field is set successfully.
 	 */
 	protected boolean setFieldUsingTypeRegistry(Field field, String fieldValue)
 	{
 		boolean result		= false;
 		ScalarType fieldType		= TypeRegistry.getType(field);
 		if (fieldType != null)
 			result			= fieldType.setField(this, field, fieldValue);
 		else
 			debug("Can't find type for " + field + " with value=" + fieldValue);
 		return result;
 	}
 
 	static final int HAVENT_TRIED_ADDING	= 0;
 	static final int DONT_NEED_WARNING		= 1;
 	static final int NEED_WARNING			= -1;
 	
 	private int considerWarning				= HAVENT_TRIED_ADDING;
 	
 	/**
 	 * Old-school DOM approach.
 	 * 
 	 * This base implementation provides a warning.
 	 * @param pte
 	 * @param childNode
 	 * @throws XMLTranslationException
 	 */
 	protected void addNestedElement(NodeToJavaOptimizations pte, Node childNode)
 	throws XMLTranslationException
 	{
 		addNestedElement((ElementState) pte.domFormChildElement(this, childNode, false));
 		if (considerWarning == NEED_WARNING)
 		{
 			warning("Ignoring nested elements with tag <" + pte.tag() + ">");
 			considerWarning					= DONT_NEED_WARNING;
 		}
 	}
 	/**
 	 * This is the hook that enables programmers to do something special
 	 * when handling a nested XML element and its associate ElementState (subclass),
 	 * by overriding this method and providing a custom implementation.
 	 * <p/>
 	 * The default implementation is a no-op.
 	 * fields that get here are ignored.
 	 * 
 	 * @param elementState
 	 * @throws XMLTranslationException 
 	 */
 	protected void addNestedElement(ElementState elementState)
 	{
 		if (considerWarning == HAVENT_TRIED_ADDING)
 			considerWarning	= NEED_WARNING;
 	}
 
 	/**
 	 * Called during translateFromXML().
 	 * If the textNodeString is currently null, assign to.
 	 * Otherwise, append to it.
 	 * 
 	 * @param newText	Text Node value just found parsing the XML.
 	 */
 	protected void appendTextNodeString(String newText)
 	{
 	   if ((newText != null) && (newText.length() > 0))
 	   {
 		   //TODO -- hopefully we can get away with this speed up
 		   String trimmed	=	newText.trim();
 		   if (trimmed.length() > 0)
 		   {
 			   String unescapedString = XMLTools.unescapeXML(newText);
 			   if (this.textNodeBuffy == null)
 				   textNodeBuffy	= new StringBuilder(unescapedString);
 			   else
 				   textNodeBuffy.append(unescapedString);
 		   }
 	   }
 	}
 	/**
 	 * @deprecated should use @xml_text or @xml_leaf to specify text child
 	 * @return
 	 */
 	@Deprecated public String getTextNodeString()
 	{
 		return (textNodeBuffy == null) ? null : textNodeBuffy.toString();
 //		return (textNodeString == null) ? null : XmlTools.unescapeXML(textNodeString);
 	}
 	/////////////////////////// other methods //////////////////////////
 
 	/**
 	 * The DOM classic accessor method.
 	 * 
 	 * @return element in the tree rooted from this, whose id attrribute is as in the parameter.
 	 * 
 	 */
 	public ElementState getElementStateById(String id)
 	{
 		return this.elementByIdMap.get(id);
 	}
 
 	/**
 	 * When translating from XML, if a tag is encountered with no matching field, perhaps
 	 * it belongs in a Collection.
 	 * This method tells us which collection object that would be.
 	 * 
 	 * @param thatClass		The class of the ElementState superclass that could be stored in a Collection.
 	 * @return
 	 */
 	protected Collection<? extends ElementState> getCollection(Class thatClass)
 	{
 		return null;
 	}
 	
 	/**
 	 * When translating from XML, if a tag is encountered with no matching field, perhaps
 	 * it belongs in a Collection.
 	 * This method tells us which collection object that would be.
 	 * 
 	 * @param thatClass		The class of the ElementState superclass that could be stored in a Collection.
 	 * @return
 	 */
 	protected Map getMap(Class thatClass)
 	{
 		return null;
 	}
 	
 	/**
 	 * An array of Strings with the names of the leaf elements.
 	 * Must be overridden to provide leaf elements as direct, typed field values.
 	 * 
 	 * @return		null in the default implementation.
 	 */
 	protected String[] leafElementFieldNames()
 	{
 		return null;
 	}
 	
 	
 	/**
 	 * Convenience for specifying what collection to put objects of a given
 	 * type into, where there is a clear mapping based on type (class).
 	 *
 	 * @author andruid
 	 */
 	protected class ClassToCollectionMap
 	extends HashMap<String, Collection>
 	{
 		public ClassToCollectionMap(Object[][] mappings)
 		{
 			int numMappings	= mappings.length;
 			for (int i=0; i<numMappings; i++)
 			{
 				Object[] thatMapping			= mappings[i];
 				try
 				{
 					Class thatClass				= (Class) thatMapping[0];
 					Collection thatCollection	= (Collection) thatMapping[1];
 //					put(thatClass.getSimpleName(), thatCollection);
 					put(Debug.classSimpleName(thatClass), thatCollection);
 				} catch (ClassCastException e)
 				{
 					debug("ERROR in ClassToCollectionMap initializer("+i+" has wrong type:\n\t"+
 						  thatMapping[0] +", " + thatMapping[i]);
 				}
 			}
 		}
 		public Collection lookup(String className)
 		{
 			return (Collection) get(className);
 		}
 		public Collection lookup(Class thatClass)
 		{
 			return lookup(classSimpleName(thatClass));
 		}
 	}
 
 	/**
 	 * Specifies automatic conversion from XML style names (e.g. composition_space) to
 	 * Java style class names (e.g. CompositionSpace) or instance variable names (e.g. compositionSpace).
 	 * 
 	 * @return	The default implementation returns true.
 	 */
 	protected boolean convertNameStyles()
 	{
 		return true;
 	}
 
 	/**
 	 * @return the parent
 	 */
 	public ElementState parent()
 	{
 		return parent;
 	}
 	/**
 	 * Set the parent of this, to create the tree structure.
 	 * 
 	 * @param parent
 	 */
 	public void setParent(ElementState parent)
 	{
 		this.parent		= parent;
 	}
     public void setFloatingPrecision(short floatingPrecision)
     {
         this.floatingPrecision = floatingPrecision;
     }
     
     public static void setDeclarationStyle(DeclarationStyle ds)
     {
     	declarationStyle	= ds;
     }
     
     static DeclarationStyle declarationStyle()
     {
     	return declarationStyle;
     }
     static boolean isPublicDeclarationStyle()
     {
     	return declarationStyle() == DeclarationStyle.PUBLIC;
     }
     /**
      * Metalanguage declaration that tells ecologylab.xml translators that each Field it is applied to as an annotation
      * is a scalar-value,
      * which should be represented in XML as an attribute.
      * <p/>
      * The attribute name will be derived from the field name, using camel case conversion, unless @xml_tag is used.
      *
      * @author andruid
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_attribute
     {
 
     }
 
     /**
      * Value for the leaf annotation that specifies translation to XML as CDATA.
      */
     public static final int		CDATA	= 1;
     /**
      * Value for the leaf annotation that specifies translation to XML without CDATA.
      */
     public static final int		NORMAL	= 0;
     
     static final 		String	EMPTY	= "";
     /**
      * Metalanguage declaration that tells ecologylab.xml translators that each Field it is applied to as an annotation
      * is a scalar-value,
      * which should be represented in XML as a leaf node:
      * an XML element with a single text node child, which represents the value.
      * <p/>
      * Can be passed the optional argument CDATA (a constant defined here), to make a CDATA declaration in the XML for the leaf node.
      * <p/>
      * The node name will be derived from the field name, using camel case conversion, unless @xml_tag is used.
      *
      * @author andruid
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_leaf
     {
     	int value() default NORMAL;
     }
 
     /**
      * Optional metalanguage declaration.
      * Enables specificaition of one or more formatting strings.
      * Only affects ScalarTyped Fields (ignored otherwise).
      * The format string will be passed to the ScalarType for type-specific interpretation.
      * <p/>
      * An example of use is to pass DateFormat info to the DateType.
      *
      * @author andruid
      * @author toupsz
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_format
     {
     	String[] value();
     }
     
 /**
  * Meta-language declaration for a single text node child, in the case where the parent
  * also has attributes, so @xml_leaf is insufficient.
  * 
  * @author andruid
  * @author toupsz
  */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_text
     {
     	int value() default NORMAL;
     }
    
     /**
      * Metalanguage declaration that tells ecologylab.xml translators that each Field it is applied to as an annotation
      * is represented in XML by a (non-leaf) nested child element.
      * The field must be a subclass of ElementState.
      * <p/>
      * The nested child element name will be derived from the field name, using camel case conversion, unless @xml_tag is used.
      *
      * @author andruid
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_nested
     {
 
     }
     static final String NULL_TAG	= "";
     
     static final Class[] NO_CLASSES	= 
     {
     	
     };
     /**
      * Metalanguage declaration that tells ecologylab.xml translators that each Field it is applied to as an annotation
      * is of type Collection. 
      * An argument may be passed to declare the tag name of the child elements.
      * The XML may define any number of child elements with this tag.
      * In this case, the class of the elements will be dervied from the instantiated generic type declaration of the children.
      * For example,		<code>@xml_collection("item")    ArrayList&lt;Item&gt;	items;</code>
      * <p/>
      * For that formulation, the type of the children may be a subclass of ElementState, for full nested elements, 
      * or it may be a ScalarType, for leaf nodes.
      * <p/>
      * Without the tag name declaration, the tag name will be derived from the class name of the children, and
      * in translate from XML, the class name will be derived from the tag name, and then resolved in the TranslationSpace.
      * <p/>
      * Alternatively, to achieve polymorphism, for children subclassed from ElementState  only,
      * this declaration can be combined with @xml_classes.
      * In such cases, items of the various classes will be collected together in the declared Collection.
      * Then, the tag names for these elements will be derived from their class declarations.
      * 
      * @author andruid
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_collection
     {
        	String value() default NULL_TAG;
     }
     
 
     /**
      * Metalanguage declaration that tells ecologylab.xml translators that each Field it is applied to as an annotation
      * is of type Map. 
      * An argument may be passed to declare the tag name of the child elements.
      * The XML may define any number of child elements with this tag.
      * In this case, the class of the elements will be dervied from the instantiated generic type declaration of the children.
      * <p/>
      * For example,		<code>@xml_map("foo")    HashMap&lt;String, FooFoo&gt;	items;</code><br/>
      * The values of the Map must implement the Mappable interface, to supply a key which matches the key declaration
      * in the Map's instantiated generic types.
      * <p/>
      * Without the tag name declaration, the tag name will be derived from the class name of the children, and
      * in translate from XML, the class name will be derived from the tag name, and then resolved in the TranslationSpace.
      * 
      * @author andruid
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     @Inherited
     public @interface xml_map
     {
        	String value() default NULL_TAG;
     }
     
     /**
      * Metalanguage declaration that can be applied either to field or to class declarations.
      * 
      * Annotation that tells ecologylab.xml translators that instead of
      * generating a name for XML elements corresponding to the field or class using camel case conversion, 
      * one is specified explicitly.
      * This name is specified by the value of this annotation.
      * <p/>
      * Note that programmers should be careful when specifying an xml_tag, to ensure that there are no collisions with
      * other names. Note that when an xml_tag is specified for a field or class, it will ALWAYS EMIT AND TRANSLATE FROM USING
      * THAT NAME.
      * 
      * xml_tag's should typically be something that cannot be represented using camel case name conversion, such as
      * utilizing characters that are not normally allowed in field names, but that are allowed in XML names. This can be
      * particularly useful for building ElementState objects out of XML from the wild.
      * <p/>
      * You cannot use XML-forbidden characters or constructs in an xml_tag!
      * 
      * When using @xml_tag, you MUST create your corresponding TranslationSpace entry using a Class object,
      * instead of using a default package name.
      * 
      * @author Zachary O. Toups (toupsz@cs.tamu.edu)
      */
     @Retention(RetentionPolicy.RUNTIME) 
     @Inherited 
     public @interface xml_tag
     {
         String value();
     }
     
     /**
      * This optional metalanguage declaration is used to add extra tags to a field or class,
      * in order to enable backwards compatability with a previous dialect of XML.
      * It affects only translate from XML; translateToXML() never uses these entries.
      * 
      * @author andruid
      */
     @Retention(RetentionPolicy.RUNTIME) 
     @Inherited 
     public @interface xml_other_tags
     {
         String[] value();
     }
     
     /**
      * Supplementary metalanguage declaration that can be applied only to a field.
      * The argument is a single Class object.
      * <p/>
      * Annotation forms a tag name from the class name, using camel case conversion.
      * It then creates a mapping from the tag and class name to the field it is applied to, 
      * so that translateFromXML(...) will set a value based on an element with the tag, 
      * if field is also declared with @xml_nested,
      * or collect values when elements have the tag, if the field is declared with @xml_collection.
      */
     @Retention(RetentionPolicy.RUNTIME) 
     @Inherited 
     public @interface xml_class
     {
         Class value();
     }
     
     /**
      * Supplementary metalanguage declaration that can be applied only to a field.
      * The argument is an array of Class objects.
      * <p/>
      * Annotation forms tag names from each of the class names, using camel case conversion.
      * It then creates a mapping from the tag and class names to the field it is applied to, 
      * so that translateFromXML(...) will set a value based on an element with the tags, 
      * if field is also declared with @xml_nested,
      * or collect values when elements have the tags, if the field is declared with @xml_collection.
      */
     @Retention(RetentionPolicy.RUNTIME) 
     @Inherited 
     public @interface xml_classes
     {
         Class<? extends ElementState>[] value();
     }
     
 	public void checkAnnotation() throws NoSuchFieldException
 	{
 		System.out.println(" isValidatable = " + this.getClass().isAnnotationPresent(xml_inherit.class));
 		Field f		= this.getClass().getField("foo");
 		System.out.println(" is leaf = " + XMLTools.representAsLeafNode(f));
 	}
 	/**
 	 * @return Returns the optimizations.
 	 */
 	protected Optimizations optimizations()
 	{
 		return optimizations;
 	}
 
 	/**
 	 * Perform custom processing on the newly created child node,
 	 * just before it is added to this.
 	 * <p/>
 	 * This is part of depth-first traversal during translateFromXML().
 	 * <p/>
 	 * This, the default implementation, does nothing.
 	 * Sub-classes may wish to override.
 	 * 
 	 * @param child
 	 */
 	protected void createChildHook(ElementState child)
 	{
 		
 	}
     
     /**
      * Perform custom processing immediately before translating this to XML. 
      * <p/>
      * This, the default implementation, does nothing. Sub-classes may wish to override.
      *
      */
     protected void preTranslationProcessingHook()
     {
 
     }
     
     /**
      * Perform custom processing immediately after all translation from XML is
      * completed. This allows a newly-created ElementState object to perform any
      * post processing with all the data it will have from XML.
      * <p/>
      * This method is called by NodeToJavaOptimizations.createChildElement() or
      * translateToXML depending on whether the element in question is a child or
      * the top-level parent.
      * <p/>
      * This, the default implementation, does nothing. Sub-classes may wish to
      * override.
      * 
      */
     protected void postTranslationProcessingHook()
     {
 
     }
     
     /**
      * Clear data structures and references to enable garbage collecting of resources associated with this.
      */
     public void recycle()
     {
     	if (parent == null)
     	{	// root state!
     		if (elementByIdMap != null)
     		{
     			elementByIdMap.clear();
     			elementByIdMap	= null;
     		}
     	}
     	else
     		parent		= null;
     	
     	elementByIdMap	= null;
     	textNodeBuffy	= null;
     	optimizations	= null;  
     	if (nestedNameSpaces != null)
     	{
     		for (ElementState nns : nestedNameSpaces.values())
     		{
     			if (nns != null)
     				nns.recycle();
     		}
     		nestedNameSpaces.clear();
     		nestedNameSpaces	= null;
     	}
     }
     
     /**
      * Add a NestedNameSpace object to this.
      * 
      * @param urn
      * @param nns
      */
     private void nestNameSpace(String urn, ElementState nns)
     {
     	if (nestedNameSpaces == null)
     		nestedNameSpaces	= new HashMap<String, ElementState>(2);
     	
     	nestedNameSpaces.put(urn, nns);
     }
     
     /**
      * Set-up referential chains for a newly born child of this.
      * 
      * @param newChildES
      */
     void setupChildElementState(ElementState newChildES)
 	{
 		newChildES.elementByIdMap			= elementByIdMap;
 		newChildES.parent					= this;
 		Optimizations parentOptimizations	= optimizations;
 		Optimizations childOptimizations 	= parentOptimizations.lookupChildOptimizations(newChildES);
 		newChildES.optimizations			= childOptimizations;
 		childOptimizations.setParent(parentOptimizations);
 	}
 
     
     /**
      * Either lookup an existing Nested Namespace object, 
      * or form a new one, map it, and return it. 
      * This lazy evaluation type call is invoked either in translateFromXML(), or,
      * when procedurally building an element with Namespace children.
      * 
      * @param id
      * @param esClass
      * @return		Namespace ElementState object associated with urn.
      */
     public ElementState getNestedNameSpace(String id)
     {
     	ElementState result	= (nestedNameSpaces == null) ? null : nestedNameSpaces.get(id);
     	if (result == null)
     	{
     		Class<? extends ElementState> esClass	= optimizations.lookupNameSpaceClassById(id);
     		if (esClass != null)
     		{
 	    		try
 				{
 					result			= XMLTools.getInstance(esClass);
 					this.setupChildElementState(result);
 					result.optimizations.setNameSpaceID(id);
 //					result.parent	= this;
 		    		nestNameSpace(id, result);
 //		    		debug("WOW! Created nested Namespace xmlns:"+id+'\n');
 				} catch (XMLTranslationException e)
 				{
 					e.printStackTrace();
 				}
     		}
     	}
     	return result;
     }
     
     /**
      * Lookup an ElementState subclass representing the scope of the nested XML Namespace in this.
      * 
      * @param 	id
      * @return	The ElementState subclass associated with xmlns:id, if there is one.
      * 			Otherwise, null.
      */
     public ElementState lookupNestedNameSpace(String id)
     {
     	return(nestedNameSpaces == null) ? null : nestedNameSpaces.get(id);
     }
     /**
      * Set to true to use the DOM parser by default for translateToXML().
      * Otherwise, the SAX parser will be used.
      * 
      * @param value
      */
     public static void setUseDOMForTranslateTo(boolean value)
     {
     	useDOMForTranslateTo	= value;
     }
 
  	/**
  	 * If the element associated with this is annotated with a field for @xml_text, make that available here.
  	 * @return
  	 */
  	NodeToJavaOptimizations scalarTextChildN2jo()
  	{
  		return optimizations.scalarTextN2jo();
  	}
 }
