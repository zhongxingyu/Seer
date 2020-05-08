 /**
  * 
  */
 package ecologylab.serialization;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.generic.StringInputStream;
 import ecologylab.net.ConnectionAdapter;
 import ecologylab.net.PURLConnection;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.types.element.Mappable;
 
 /**
  * Use SAX to translate XML into a typed tree of ElementState objects.
  * 
  * @author andruid
  */
 public class ElementStateSAXHandler extends Debug implements ContentHandler, FieldTypes,
 		ScalarUnmarshallingContext
 {
 
 	final TranslationScope			translationScope;
 
 	ElementState								root;
 
 	// private XMLReader parser;
 
 	/**
 	 * Current "DOM" frame state.
 	 */
 	ElementState								currentElementState;
 
 	/**
 	 * Optimizations for current field.
 	 */
 	FieldDescriptor							currentFD;
 
 	SIMPLTranslationException		xmlTranslationException;
 
 	ArrayList<FieldDescriptor>	fdStack				= new ArrayList<FieldDescriptor>();
 
 	static XMLReaderPool				xmlReaderPool	= new XMLReaderPool(1, 1);
 
 	ParsedURL										purlContext;
 
 	File												fileContext;
 
 	DeserializationHookStrategy deserializationHookStrategy;
 	/**
 	 * 
 	 */
 	public ElementStateSAXHandler(TranslationScope translationScope)
 	{
 		this.translationScope = translationScope;
 
 		// try
 		// {
 		// parser = createXMLReader();
 		// parser.setContentHandler(this);
 		// } catch (Exception e)
 		// {
 		// parser = null;
 		// }
 	}
 
 	static final String								SUN_XERCES_PARSER_NAME	= "com.sun.org.apache.xerces.internal.parsers.SAXParser";
 
 	static Class<? extends XMLReader>	parserClass;
 
 	static boolean										triedToFindParserClass;
 
 	public static XMLReader createXMLReader() throws SAXException
 	{
 		if (parserClass != null)
 			return ReflectionTools.getInstance(parserClass);
 		// else
 
 		if (!triedToFindParserClass)
 		{
 			triedToFindParserClass = true;
 			try
 			{
 				parserClass = (Class<? extends XMLReader>) Class.forName(SUN_XERCES_PARSER_NAME);
 				return createXMLReader();
 			}
 			catch (Exception e)
 			{
 
 			}
 		}
 		// stuck doin it the slow way :-(
 		return XMLReaderFactory.createXMLReader();
 	}
 
 	/**
 	 * Parse the CharSequence of XML, using UTF-8 encoding.
 	 * 
 	 * @param charSequence
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState parse(CharSequence charSequence) throws SIMPLTranslationException
 	{
 		return parse(charSequence, StringInputStream.UTF8);
 	}
 
 	/**
 	 * Parse the CharSequence of XML, given the charsetType encoding info.
 	 * 
 	 * @param charSequence
 	 * @param charsetType
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState parse(CharSequence charSequence, int charsetType)
 			throws SIMPLTranslationException
 	{
 		InputStream xmlStream = new StringInputStream(charSequence, charsetType);
 		ElementState result = parse(xmlStream);
 		try
 		{
 			xmlStream.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public ElementState parseString(String xmlString) throws SIMPLTranslationException
 	{
 		StringReader reader = new StringReader(xmlString);
 		ElementState result = parse(reader);
 		reader.close();
 
 		return result;
 	}
 
 	static final ConnectionAdapter	connectionAdapter	= new ConnectionAdapter();
 
 	/**
 	 * Translate an XML document read from a URL to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param purl
 	 *          XML source material.
 	 * @param translationScope
 	 *          Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return Strongly typed tree of ElementState objects.
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState parse(URL url) throws SIMPLTranslationException
 	{
 		return parse(new ParsedURL(url));
 	}
 
 	/**
 	 * Translate an XML document read from a ParsedURL to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param purl
 	 *          XML source material.
 	 * @param translationScope
 	 *          Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return Strongly typed tree of ElementState objects.
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState parse(ParsedURL purl) throws SIMPLTranslationException
 	{
 		if (purl.isFile())
 			return parse(purl.file());
 
 		this.purlContext = purl;
 
 		PURLConnection purlConnection = purl.connect(connectionAdapter);
 		ElementState result = parse(purlConnection.inputStream());
 		purlConnection.recycle();
 		this.purlContext = null;
 		return result;
 	}
 
 	/**
 	 * Translate a file from XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param file
 	 *          XML source material.
 	 * @param translationScope
 	 *          Specifies mapping from XML nodes (elements and attributes) to Java types.
 	 * 
 	 * @return Strongly typed tree of ElementState objects.
 	 * @throws SIMPLTranslationException
 	 */
 
 	public ElementState parse(File file) throws SIMPLTranslationException
 	{
 		try
 		{
 			FileInputStream fileInputStream = new FileInputStream(file);
 			BufferedInputStream bufferedStream = new BufferedInputStream(fileInputStream);
 			this.fileContext = file;
 			ElementState elementState = parse(bufferedStream);
 			this.fileContext = null;
 			bufferedStream.close();
 			return elementState;
 		}
 		catch (FileNotFoundException e)
 		{
 			this.fileContext = null;
 			throw new SIMPLTranslationException("Can't open file " + file.getAbsolutePath(), e);
 		}
 		catch (IOException e)
 		{
 			this.fileContext = null;
 			throw new SIMPLTranslationException("Can't close file " + file.getAbsolutePath(), e);
 		}
 	}
 
 	public ElementState parse(Reader reader) throws SIMPLTranslationException
 	{
 		InputSource inputSource = new InputSource(reader);
 		ElementState result = parse(inputSource);
 		// TODO -- put this into a finally from the parse
 		try
 		{
 			reader.close();
 		}
 		catch (IOException e)
 		{
 			throw new SIMPLTranslationException("Can't close reader: " + reader, e);
 		}
 		return result;
 	}
 
 	public ElementState parse(InputStream inputStream, DeserializationHookStrategy deserializationHookStrategy) throws SIMPLTranslationException
 	{
 		this.deserializationHookStrategy	= deserializationHookStrategy;
 		return parse(new InputSource(inputStream));
 	}
 	
 	public ElementState parse(File file, DeserializationHookStrategy deserializationHookStrategy) throws SIMPLTranslationException
 	{
 		this.deserializationHookStrategy = deserializationHookStrategy;
 		return parse(file);
 	}
 
 	public ElementState parse(InputStream inputStream) throws SIMPLTranslationException
 	{
 		return parse(new InputSource(inputStream));
 	}
 
 	public ElementState parse(InputSource inputSource) throws SIMPLTranslationException
 	{
 		XMLReader parser = null;
 		try
 		{
 			parser = xmlReaderPool.acquire();
 			parser.setContentHandler(this);
 			parser.parse(inputSource);
 		}
 		catch (IOException e)
 		{
 			xmlTranslationException = new SIMPLTranslationException("IOException during parsing", e);
 		}
 		catch (SAXException e)
 		{
 			// (condition trys to ignore weird characters at the end of yahoo's xml on 9/9/08
			if (!(currentFD != null) && (currentFD.getType() == PSEUDO_FIELD_DESCRIPTOR) && (currentElementState != null))
 			{
 				xmlTranslationException = new SIMPLTranslationException("SAXException during parsing", e);
 
 				// print xml
 				StringBuilder builder = root.serialize();
 				System.out.println("Failed XML:");
 				System.out.println(builder.toString());
 			}
 		}
 		finally
 		{
 			if (parser != null)
 				xmlReaderPool.release(parser);
 		}
 		if (xmlTranslationException != null)
 			throw xmlTranslationException;
 		return root;
 	}
 
 	private void setRoot(ElementState root)
 	{
 		this.root = root;
 		this.currentElementState = root;
 	}
 
 	private ClassDescriptor currentClassDescriptor()
 	{
 		if (currentElementState != null)
 			return this.currentElementState.classDescriptor();
 		else
 			return null;
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String,
 	 *      java.lang.String, org.xml.sax.Attributes)
 	 */
 	public void startElement(String uri, String localName, String tagName, Attributes attributes)
 			throws SAXException
 	{
 		if (xmlTranslationException != null)
 			return;
 
 		FieldDescriptor childFD = null;
 		final boolean isRoot = (root == null);
 		if (isRoot)
 		{ // form the root ElementState!
 			ClassDescriptor rootClassDescriptor = translationScope.getClassDescriptorByTag(tagName);
 			if (rootClassDescriptor != null)
 			{
 				try
 				{
 					ElementState root	= null;
 					if (root == null)
 						root = rootClassDescriptor.getInstance();
 					if (root != null)
 					{
 						root.setupRoot();
 						setRoot(root);
 						if (deserializationHookStrategy != null)
 							deserializationHookStrategy.deserializationPreHook(root, null);
 						root.translateAttributes(translationScope, attributes, this, root);
 						childFD = rootClassDescriptor.pseudoFieldDescriptor();
 					}
 					else
 					{
 						this.xmlTranslationException = new RootElementException(tagName, translationScope);
 						return;
 					}
 				}
 				catch (SIMPLTranslationException e)
 				{
 					xmlTranslationException = e;
 				}
 			}
 			else
 			{
 				// else, we dont translate this element; we ignore it.
 				String message = "XML Translation WARNING: Cant find class object for Root XML element <"
 						+ tagName + ">: Ignored. ";
 				println(message);
 				xmlTranslationException = new SIMPLTranslationException(message);
 				return;
 			}
 		}
 		else
 		// not root
 		{
 			final int currentType = currentFD.getType();
 			ElementState currentES = this.currentElementState;
 			// if there is a pending text node, assign it somehow!
 			processPendingScalar(currentType, currentES);
 
 			ClassDescriptor currentClassDescriptor = currentClassDescriptor();
 			childFD = (currentFD != null) && (currentType == IGNORED_ELEMENT) ?
 			// new NodeToJavaOptimizations(tagName) : // (nice for debugging; slows us down)
 			FieldDescriptor.IGNORED_ELEMENT_FIELD_DESCRIPTOR
 					: (currentType == WRAPPER) ? currentFD.getWrappedFD() : currentClassDescriptor
 							.getFieldDescriptorByTag(tagName, translationScope, currentES);
 			if (childFD == null)
 			{
 				childFD = makeIgnoredFieldDescriptor(tagName, currentClassDescriptor);
 			}
 		}
 		this.currentFD = childFD;
 		registerXMLNS();
 		// TODO? -- do we need to avoid this if null from an exception in translating root?
 		pushFD(childFD);
 		// printStack("After push");
 
 		if (isRoot)
 			return;
 
 		ElementState currentElementState = this.currentElementState;
 		ElementState childES = null;
 		try
 		{
 			switch (childFD.getType())
 			{
 			case COMPOSITE_ELEMENT:
 				childES = childFD.constructChildElementState(currentElementState, tagName, attributes);
 
 				if (childES == null)
 				{
 					childFD = makeIgnoredFieldDescriptor(tagName, currentClassDescriptor());
 				}
 				else
 					childFD.setFieldToComposite(currentElementState, childES); // maybe we
 				// should do
 				// this on close
 				// element
 				break;
 			case NAME_SPACE_NESTED_ELEMENT:
 				// TODO Name Space support!
 				// ElementState nsContext =
 				// currentElementState.getNestedNameSpace(activeFieldDescriptor.nameSpaceID());
 				// childES = activeFieldDescriptor.constructChildElementState(nsContext);
 				// activeFieldDescriptor.setFieldToNestedObject(nsContext, childES);
 				break;
 			case NAME_SPACE_SCALAR:
 				// TODO Name Space support!
 				// childES = currentElementState.getNestedNameSpace(activeFieldDescriptor.nameSpaceID());
 				break;
 			case SCALAR:
 				// wait for characters to set scalar field
 				// activeN2JO.setScalarFieldWithLeafNode(activeES, childNode);
 				break;
 			case COLLECTION_ELEMENT:
 				Collection collection = (Collection) childFD
 						.automaticLazyGetCollectionOrMap(currentElementState);
 				if (collection != null)
 				{
 					ElementState childElement = childFD.constructChildElementState(
 							currentElementState, tagName, attributes);
 					childES = childElement;
 
 					if (childES == null)
 					{
 						childFD = makeIgnoredFieldDescriptor(tagName, currentClassDescriptor());
 					}
 
 					collection.add(childES);
 				}
 				// activeNJO.formElementAndAddToCollection(activeES, childNode);
 				break;
 			case COLLECTION_SCALAR:
 				// wait for characters to create scalar reference type and add to collection
 				// activeN2JO.addLeafNodeToCollection(activeES, childNode);
 				break;
 			case MAP_ELEMENT:
 				Map map = (Map) childFD.automaticLazyGetCollectionOrMap(currentElementState);
 				if (map != null)
 				{
 					ElementState childElement = childFD.constructChildElementState(
 							currentElementState, tagName, attributes);
 
 					childES = childElement;
 					if (childES == null)
 					{
 						this.currentFD = makeIgnoredFieldDescriptor(tagName, currentClassDescriptor());
 					}
 				}
 				// Map map = activeFieldDescriptor.getMap(currentElementState);
 				// if (map != null)
 				// {
 				// childES = activeFieldDescriptor.constructChildElementState(currentElementState, tagName);
 				// }
 				break;
 			case IGNORED_ELEMENT:
 				// should get a set of Optimizations for this, to represent its subfields
 			case BAD_FIELD:
 			case WRAPPER:
 			default:
 				break;
 
 			}
 			if (childES != null)
 			{
 				// fill in its attributes
 				if (deserializationHookStrategy != null)
 					deserializationHookStrategy.deserializationPreHook(childES, childFD);
 
 				childES.translateAttributes(translationScope, attributes, this, currentElementState);
 
 				this.currentElementState = childES; // childES.parent = old currentElementState
 				this.currentFD = childFD;
 			}
 		}
 		catch (SIMPLTranslationException e)
 		{
 			this.xmlTranslationException = e;
 		}
 	}
 
 	private FieldDescriptor makeIgnoredFieldDescriptor(String tagName,
 			ClassDescriptor currentClassDescriptor)
 	{
 		FieldDescriptor activeFieldDescriptor;
 		currentClassDescriptor.warning(" Ignoring tag <" + tagName + ">");
 		activeFieldDescriptor = new FieldDescriptor(tagName); // TODO -- should we record
 		// declaringClass in here??!
 		if (activeFieldDescriptor.getTagName() != null)
 			currentClassDescriptor.addFieldDescriptorMapping(activeFieldDescriptor);
 		return activeFieldDescriptor;
 	}
 
 	private void pushFD(FieldDescriptor fd)
 	{
 		this.fdStack.add(fd);
 	}
 
 	private void popAndPeekFD()
 	{
 		ArrayList<FieldDescriptor> stack = this.fdStack;
 		int last = stack.size() - 1;
 		if (last >= 0)
 		{
 			FieldDescriptor result = stack.remove(last--);
 			if (last >= 0)
 				result = stack.get(last);
 			this.currentFD = result;
 			// printStack("After Pop");
 		}
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void endElement(String namespaceURI, String localTagName, String prefixedTagName)
 			throws SAXException
 	{
 		// if (this.currentElementState == null)
 		// {
 		// this.currentFD.warning(" Ignoring tag <" + localTagName + ">");
 		// return;
 		// }
 
 		if (xmlTranslationException != null)
 			return;
 
 		final FieldDescriptor currentFD = this.currentFD;
 		final int curentFdType = currentFD.getType();
 
 		if (curentFdType == NAMESPACE_TRIAL_ELEMENT)
 		{
 			// re-attempt lookup in case we figured out how
 
 			// if not, we will have to set currentFdType = NAMESPACE_IGNORED_ELEMENT
 		}
 
 		ElementState currentES = this.currentElementState;
 		processPendingScalar(curentFdType, currentES);
 
 		final ElementState parentES = currentES.parent;
 
 		switch (curentFdType)
 		// every good push deserves a pop :-) (and othertimes, not!)
 		{
 		case MAP_ELEMENT:
 			if (currentES instanceof Mappable)
 			{
 				final Object key = ((Mappable) currentES).key();
 				Map map = (Map) currentFD.automaticLazyGetCollectionOrMap(parentES);
 				// Map map = currentFD.getMap(parentES);
 				map.put(key, currentES);
 			}
 		case COMPOSITE_ELEMENT:
 		case COLLECTION_ELEMENT:
 		case NAME_SPACE_NESTED_ELEMENT:
 			if (parentES != null)
 				parentES.createChildHook(currentES);
 			else
 				debug("cool - post ns element");
 			currentES.deserializationPostHook();
 			if (deserializationHookStrategy != null)
 				deserializationHookStrategy.deserializationPostHook(currentES, currentFD);
 			this.currentElementState = currentES.parent;
 		case NAME_SPACE_SCALAR:
 			// case WRAPPER:
 			this.currentElementState = parentES; // restore context!
 			break;
 		default:
 			break;
 		}
 		// end of the Namespace object, so we gotta pop it off, too.
 		// if (curentN2JOType == NAME_SPACE_NESTED_ELEMENT)
 		// this.currentElementState = this.currentElementState.parent;
 		popAndPeekFD();
 		// if (this.startElementPushed) // every good push deserves a pop :-) (and othertimes, not!)
 	}
 
 	/**
 	 * Assign pending value to a @simpl_scalar
 	 * 
 	 * @param curentN2JOType
 	 * @param currentES
 	 */
 	private void processPendingScalar(final int curentN2JOType, ElementState currentES)
 	{
 		final int length = currentTextValue.length();
 		if (length > 0)
 		{
 			try
 			{
 				switch (curentN2JOType)
 				{
 				case NAME_SPACE_SCALAR:
 				case SCALAR:
 					// TODO -- unmarshall to set field with scalar type
 					// copy from the StringBuilder
 					String value = stringToDeserializeAsScalar(length);
 					currentFD.setFieldToScalar(currentES, value, this);
 					break;
 				case COLLECTION_SCALAR:
 					value = stringToDeserializeAsScalar(length);
 					currentFD.addLeafNodeToCollection(currentES, value, this);
 					break;
 				case COMPOSITE_ELEMENT:
 				case COLLECTION_ELEMENT:
 				case PSEUDO_FIELD_DESCRIPTOR:
 					// TODO - is this code used????
 					// optimizations in currentN2JO are for its parent (they were in scope when it was
 					// constructed)
 					// so we get the optimizations we need from the currentElementState
 					// FIXME -- implement this!!!
 					FieldDescriptor scalarTextFD = currentElementState.classDescriptor().getScalarTextFD();
 					if (scalarTextFD != null)
 					{
 						value = stringToDeserializeAsScalar(length);
 						scalarTextFD.setFieldToScalar(currentES, value, this);
 					}
 					break;
 				default:
 					break;
 				}
 			}
 			catch (SIMPLTranslationException e)
 			{
 				this.xmlTranslationException = e;
 			}
 
 			currentTextValue.setLength(0);
 		}
 	}
 
 	/**
 	 * Get the String that will be marshalled into the value with a ScalarType, using the
 	 * currentTextValue state variable from the parse, and the length parameter. If appropriate, use
 	 * the currentFD to perform a regex filter on the value before passing it to the appropriate
 	 * scalar marshalling and field or collection element setter.
 	 * 
 	 * @param length
 	 * @return
 	 */
 	private String stringToDeserializeAsScalar(final int length)
 	{
 		String result = new String(currentTextValue.substring(0, length));
 		if (translationScope.isPerformFilters())
 			result = currentFD.filterValue(result);
 		return result;
 	}
 
 	void printStack(String msg)
 	{
 		currentElementState.debug("Stack -- " + msg + "\t[" + this.currentElementState + "]");
 		for (FieldDescriptor thatFD : fdStack)
 		{
 			println(thatFD.getTagName() + " - 0x" + Integer.toHexString(thatFD.getType()));
 		}
 		println("");
 	}
 
 	StringBuilder	currentTextValue	= new StringBuilder(1024);
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
 	 */
 	public void characters(char[] chars, int startIndex, int length) throws SAXException
 	{
 		if (xmlTranslationException != null)
 			return;
 
 		if (currentFD != null)
 		{
 			int n2joType = currentFD.getType();
 			switch (n2joType)
 			{
 			case SCALAR:
 			case COLLECTION_SCALAR:
 				currentTextValue.append(chars, startIndex, length);
 				// TODO -- unmarshall to set field with scalar type
 				break;
 			case COMPOSITE_ELEMENT:
 			case COLLECTION_ELEMENT:
 			case PSEUDO_FIELD_DESCRIPTOR:
 				// optimizations in currentN2JO are for its parent (they were in scope when it was
 				// constructed)
 				// so we get the optimizations we need from the currentElementState
 				if (currentElementState.classDescriptor().hasScalarFD())
 					currentTextValue.append(chars, startIndex, length);
 				break;
 			default:
 				// TODO ?! can we dump characters in this case, or should we append to textNode?!
 				// currentElementState.appendLeafXML(buffy, leafElementName, leafValue, needsEscaping,
 				// isCDATA)
 				break;
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#endDocument()
 	 */
 	public void endDocument() throws SAXException
 	{
 		if ((xmlTranslationException == null) && (root != null))
 			root.deserializationPostHook();
 		
 		ElementState.recycleDeserializationMappings();
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
 	 */
 	public void endPrefixMapping(String prefix) throws SAXException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
 	 */
 	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
 	 */
 	public void processingInstruction(String target, String data) throws SAXException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
 	 */
 	public void setDocumentLocator(Locator locator)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
 	 */
 	public void skippedEntity(String name) throws SAXException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#startDocument()
 	 */
 	public void startDocument() throws SAXException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * ${tags}
 	 * 
 	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
 	 */
 	public void startPrefixMapping(String nsID, String urn) throws SAXException
 	{
 		// debug("Hi: startPrefixMapping(" + nsID +" := " + urn);
 		// this.nameSpacePrefix = prefix;
 		// this.nameSpaceURN = urn;
 		if (nsID.length() > 0) // these days, ignore ns decls without an id (default ones)
 		{
 			// push the urn in first; pop it off 2nd
 			xmlnsStack.add(urn);
 			// push the nsID in 2nd; pop it off 1st
 			xmlnsStack.add(nsID);
 		}
 	}
 
 	// String nameSpacePrefix;
 	//	
 	// String nameSpaceURN; //FIXME -- this should be a stack!
 
 	ArrayList<String>	xmlnsStack	= new ArrayList<String>(2);
 
 	void registerXMLNS()
 	{
 		int size = xmlnsStack.size();
 		while (size >= 2)
 		{
 			String nameSpaceID = xmlnsStack.remove(--size);
 			String urn = xmlnsStack.remove(--size);
 			if ((nameSpaceID != null) && (urn != null))
 			{
 				registerXMLNS(this.currentElementState, nameSpaceID, urn);
 			}
 		}
 		// String urn = nameSpaceURN;
 		// if (urn != null)
 		// {
 		// registerXMLNS(this.currentElementState, nameSpacePrefix, urn);
 		// nameSpaceURN = null;
 		// nameSpacePrefix = null;
 		// }
 	}
 
 	/**
 	 * Create a name space object, nested in the context, using info saved in this.
 	 * 
 	 * @param context
 	 * @param urn
 	 *          The value of the xmlns:id attribute is the URL that is mapped to the class.
 	 */
 	private void registerXMLNS(ElementState context, String prefix, String urn)
 	{
 		if (context != null)
 			context.classDescriptor().mapNamespaceIdToClass(translationScope, prefix, urn);
 		else
 			println("ERROR: Null context. Can't register xmlns:" + prefix + "=" + urn);
 	}
 
 	/**
 	 * @return the root
 	 */
 	public ElementState root()
 	{
 		return root;
 	}
 
 	public File fileContext()
 	{
 		return (fileContext != null) ? fileContext : (purlContext != null) ? purlContext.file() : null;
 	}
 
 	public ParsedURL purlContext()
 	{
 		return purlContext;
 	}
 
 }
