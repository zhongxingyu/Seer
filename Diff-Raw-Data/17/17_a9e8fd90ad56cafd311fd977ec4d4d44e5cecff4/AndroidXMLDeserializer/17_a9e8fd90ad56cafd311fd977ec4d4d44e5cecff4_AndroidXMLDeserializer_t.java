 package com.ecologylab;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.Map;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.deserializers.pullhandlers.stringformats.StringPullDeserializer;
 import ecologylab.serialization.types.element.IMappable;
 
 public class AndroidXMLDeserializer extends StringPullDeserializer
 {
 
 	private CharSequence	test;
 
 	private XmlPullParser	xmlPullParser;
 
	public AndroidXMLDeserializer(SimplTypesScope translationScope,
 			TranslationContext translationContext)
 	{
 		super(translationScope, translationContext);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public Object parse(CharSequence charSequence) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(charSequence);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	@Override
 	public Object parse(InputStream inputStream, Charset charSet) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(inputStream, charSet);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	@Override
 	public Object parse(InputStream inputStream) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(inputStream);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	private void configure(InputStream inputStream, Charset charSet) throws XmlPullParserException
 	{
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		xmlPullParser = factory.newPullParser();
 		xmlPullParser.setInput(inputStream, charSet.name());
 	}
 
 	private void configure(InputStream inputStream) throws XmlPullParserException
 	{
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		xmlPullParser = factory.newPullParser();
 		xmlPullParser.setInput(inputStream, "UTF-8");
 	}
 
 	private void configure(CharSequence charSequence) throws XmlPullParserException
 	{
 		test = charSequence;
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		xmlPullParser = factory.newPullParser();
 		xmlPullParser.setInput(new StringReader(charSequence.toString()));
 	}
 
 	private Object parse() throws SIMPLTranslationException, IOException, XmlPullParserException
 	{
 		Object root = null;
 
 		nextEvent();
 
 		if (xmlPullParser.getEventType() != XmlPullParser.START_TAG)
 		{
 			throw new SIMPLTranslationException("start of an element expected");
 		}
 
 		String rootTag = getTagName();
 
 		ClassDescriptor<? extends FieldDescriptor> rootClassDescriptor = translationScope
 				.getClassDescriptorByTag(rootTag);
 
 		if (rootClassDescriptor == null)
 		{
 			throw new SIMPLTranslationException("cannot find the class descriptor for root element <"
 					+ rootTag + ">; make sure if translation scope is correct.");
 		}
 
 		root = rootClassDescriptor.getInstance();
 
 		deserializationPreHook(root, translationContext);
 		if (deserializationHookStrategy != null)
 			deserializationHookStrategy.deserializationPreHook(root, null);
 
 		deserializeAttributes(root, rootClassDescriptor);
 
 		createObjectModel(root, rootClassDescriptor, rootTag);
 
 		return root;
 	}
 
 	private void createObjectModel(Object root,
 			ClassDescriptor<? extends FieldDescriptor> rootClassDescriptor, String rootTag)
 			throws IOException, SIMPLTranslationException, XmlPullParserException
 	{
 
 		try
 		{
 
 			int event = 0;
 			event = nextEvent();
 
 			FieldDescriptor currentFieldDescriptor = new FieldDescriptor();
 
 			String xmlText = "";
 
 			while (event != XmlPullParser.END_DOCUMENT
 					&& (event != XmlPullParser.END_TAG || !rootTag.equals(getTagName())))
 			{
 				if (event != XmlPullParser.START_TAG)
 				{
 					if (event == XmlPullParser.TEXT)
 						xmlText += xmlPullParser.getText();
 					event = nextEvent();
 					continue;
 				}
 
 				String tag = getTagName();
 
 				currentFieldDescriptor = currentFieldDescriptor.getType() == WRAPPER ? currentFieldDescriptor
 						.getWrappedFD()
 						: rootClassDescriptor.getFieldDescriptorByTag(tag, translationScope, null);
 
 				if (currentFieldDescriptor == null)
 				{
 					currentFieldDescriptor = FieldDescriptor.makeIgnoredFieldDescriptor(tag);
 				}
 
 				int fieldType = currentFieldDescriptor.getType();
 
 				switch (fieldType)
 				{
 				case SCALAR:
 					event = deserializeScalar(root, currentFieldDescriptor);
 					break;
 				case COLLECTION_SCALAR:
 					event = deserializeScalarCollection(root, currentFieldDescriptor);
 					break;
 				case COMPOSITE_ELEMENT:
 					event = deserializeComposite(root, currentFieldDescriptor);
 					break;
 				case COLLECTION_ELEMENT:
 					event = deserializeCompositeCollection(root, currentFieldDescriptor);
 					break;
 				case MAP_ELEMENT:
 					event = deserializeCompositeMap(root, currentFieldDescriptor);
 					break;
 				case WRAPPER:
 					event = nextEvent();
 					break;
 				case IGNORED_ELEMENT:
 					event = ignoreTag(tag);
 					break;
 				default:
 					event = nextEvent();
 				}
 
 				if (event == XmlPullParser.END_DOCUMENT)
 				{
 					// no more data? but we are expecting so its not correct
 					throw new SIMPLTranslationException(
 							"premature end of file: check XML file for consistency");
 				}
 			}
 
 			if (rootClassDescriptor.hasScalarFD())
 			{
 				rootClassDescriptor.getScalarTextFD().setFieldToScalar(root, xmlText, translationContext);
 			}
 			deserializationPostHook(root, translationContext);
 			if (deserializationHookStrategy != null)
 				deserializationHookStrategy.deserializationPostHook(root, null);
 		}
 
 		catch (Exception ex)
 		{
 			printParse();
 			System.out.println(ex);
 		}
 	}
 
 	private int deserializeScalarCollection(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException, XmlPullParserException, IOException
 	{
 		int event = xmlPullParser.getEventType();
 
 		while (fd.isCollectionTag(getTagName()))
 		{
 			String tag = getTagName();
 			if (event != XmlPullParser.START_TAG)
 			{
 				// end of collection
 				break;
 			}
 
 			event = xmlPullParser.next();
 
 			if (event == XmlPullParser.TEXT && event != XmlPullParser.END_TAG)
 			{
 				StringBuilder text = new StringBuilder();
 				text.append(xmlPullParser.getText());
 				while (xmlPullParser.next() != XmlPullParser.END_TAG)
 				{
 					if (xmlPullParser.getEventType() == XmlPullParser.TEXT)
 						text.append(xmlPullParser.getText());
 				}
 
 				String value = text.toString();
 				fd.addLeafNodeToCollection(root, value, translationContext);
 			}
 
 			event = xmlPullParser.nextTag();
 		}
 
 		return event;
 	}
 
 	private int deserializeComposite(Object root, FieldDescriptor currentFieldDescriptor)
 			throws SIMPLTranslationException, IOException, XmlPullParserException
 	{
 
 		String tagName = getTagName();
 		Object subRoot = getSubRoot(currentFieldDescriptor, tagName, root);
 		currentFieldDescriptor.setFieldToComposite(root, subRoot);
 
 		return nextEvent();
 	}
 
 	private int deserializeCompositeMap(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException, IOException, XmlPullParserException
 	{
 		Object subRoot;
 		int event = xmlPullParser.getEventType();
 
 		while (fd.isCollectionTag(getTagName()))
 		{
 			if (event != XmlPullParser.START_TAG)
 			{
 				// end of collection
 				break;
 			}
 
 			String compositeTagName = getTagName();
 			subRoot = getSubRoot(fd, compositeTagName, root);
 			if (subRoot instanceof IMappable<?>)
 			{
 				final Object key = ((IMappable<?>) subRoot).key();
 				Map map = (Map) fd.automaticLazyGetCollectionOrMap(root);
 				map.put(key, subRoot);
 			}
 
 			event = xmlPullParser.nextTag();
 
 		}
 		return event;
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param fd
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 * @throws XmlPullParserException
 	 * @throws XMLStreamException
 	 */
 	private int deserializeCompositeCollection(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException, IOException, XmlPullParserException
 	{
 		Object subRoot;
 		int event = xmlPullParser.getEventType();
 		while (fd.isCollectionTag(getTagName()))
 		{
 			if (event != XmlPullParser.START_TAG)
 			{
 				// end of collection
 				break;
 			}
 
 			String compositeTagName = getTagName();
 			subRoot = getSubRoot(fd, compositeTagName, root);
 			Collection collection = (Collection) fd.automaticLazyGetCollectionOrMap(root);
 			collection.add(subRoot);
 
 			event = xmlPullParser.nextTag();
 		}
 
 		return event;
 	}
 
 	private int deserializeScalar(Object root, FieldDescriptor currentFieldDescriptor)
 			throws XmlPullParserException, IOException
 	{
 		nextEvent();
 
 		StringBuilder text = new StringBuilder();
 		text.append(xmlPullParser.getText());
 
 		while (nextEvent() != XmlPullParser.END_TAG)
 		{
 			if (xmlPullParser.getEventType() == XmlPullParser.TEXT)
 				text.append(xmlPullParser.getText());
 		}
 
 		String value = text.toString();
 		currentFieldDescriptor.setFieldToScalar(root, value, translationContext);
 
 		return nextEvent();
 
 	}
 
 	private int ignoreTag(String tag) throws XmlPullParserException, IOException
 	{
 		int event = -1;
 		println("ignoring tag: " + tag);
 
 		while (event != XmlPullParser.END_TAG || !getTagName().equals(tag))
 			event = nextEvent();
 
 		return nextEvent();
 	}
 
 	private Object getSubRoot(FieldDescriptor currentFieldDescriptor, String tagName, Object root)
 			throws SIMPLTranslationException, IOException, XmlPullParserException
 	{
 		Object subRoot = null;
 		ClassDescriptor<? extends FieldDescriptor> subRootClassDescriptor = currentFieldDescriptor
 				.getChildClassDescriptor(tagName);
 
 		String simplReference = null;
 
 		if ((simplReference = getSimpleReference()) != null)
 		{
 			subRoot = translationContext.getFromMap(simplReference);
 			xmlPullParser.next();
 		}
 		else
 		{
 			subRoot = subRootClassDescriptor.getInstance();
 
 			deserializationPreHook(subRoot, translationContext);
 			if (deserializationHookStrategy != null)
 				deserializationHookStrategy.deserializationPreHook(subRoot, currentFieldDescriptor);
 
 			if (subRoot != null)
 			{
 				if (subRoot instanceof ElementState && root instanceof ElementState)
 				{
 					((ElementState) subRoot).setupInParent((ElementState) root);
 				}
 			}
 
 			deserializeAttributes(subRoot, subRootClassDescriptor);
 			createObjectModel(subRoot, subRootClassDescriptor, tagName);
 		}
 
 		return subRoot;
 	}
 
 	private String getSimpleReference()
 	{
 		String simplReference = null;
 
 		for (int i = 0; i < xmlPullParser.getAttributeCount(); i++)
 		{
 			String attributePrefix = xmlPullParser.getAttributePrefix(i);
 			String tag = xmlPullParser.getAttributeName(i);
 			String value = xmlPullParser.getAttributeValue(i);
 
 			if (TranslationContext.SIMPL.equals(attributePrefix))
 			{
 				if (tag.equals(TranslationContext.REF))
 				{
 					simplReference = value;
 				}
 			}
 		}
 
 		return simplReference;
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param rootClassDescriptor
 	 * @return
 	 */
 	private boolean deserializeAttributes(Object root,
 			ClassDescriptor<? extends FieldDescriptor> rootClassDescriptor)
 	{
 		for (int i = 0; i < xmlPullParser.getAttributeCount(); i++)
 		{
 			String attributePrefix = xmlPullParser.getAttributePrefix(i);
 			String tag = xmlPullParser.getAttributeName(i);
 			String value = xmlPullParser.getAttributeValue(i);
 
 			if (TranslationContext.SIMPL.equals(attributePrefix))
 			{
 				if (tag.equals(TranslationContext.ID))
 				{
 					translationContext.markAsUnmarshalled(value, root);
 				}
 			}
 			else
 			{
 				FieldDescriptor attributeFieldDescriptor = rootClassDescriptor.getFieldDescriptorByTag(tag,
 						translationScope);
 
 				if (attributeFieldDescriptor != null)
 				{
 					attributeFieldDescriptor.setFieldToScalar(root, value, translationContext);
 				}
 				else
 				{
 					debug("ignoring attribute: " + tag);
 				}
 			}
 		}
 
 		return true;
 	}
 
 	private int nextEvent() throws XmlPullParserException, IOException
 	{
 		int eventType = XmlPullParser.END_DOCUMENT;
 
 		// skip events that we don't handle.
 		while ((eventType = xmlPullParser.next()) != XmlPullParser.END_DOCUMENT)
 		{
 			if (xmlPullParser.getEventType() == XmlPullParser.START_DOCUMENT
 					|| xmlPullParser.getEventType() == XmlPullParser.START_TAG
 					|| xmlPullParser.getEventType() == XmlPullParser.END_TAG
 					|| xmlPullParser.getEventType() == XmlPullParser.END_DOCUMENT
 					|| xmlPullParser.getEventType() == XmlPullParser.TEXT)
 			{
 				break;
 			}
 		}
 
 		return eventType;
 	}
 
 	protected void debug() throws XmlPullParserException
 	{
 		int event = xmlPullParser.getEventType();
 		switch (event)
 		{
 		case XmlPullParser.START_TAG:
 			System.out.println(getTagName());
 			break;
 		case XmlPullParser.END_TAG:
 			System.out.println(getTagName());
 			break;
 		case XmlPullParser.TEXT:
 			System.out.println(xmlPullParser.getText());
 			break;
 		case XmlPullParser.CDSECT:
 			System.out.println("cdata " + xmlPullParser.getText());
 			break;
 		} // end switch
 	}
 
 	private String getTagName()
 	{
 		if (xmlPullParser.getPrefix() != null && xmlPullParser.getPrefix().length() != 0)
 			return xmlPullParser.getPrefix() + ":" + xmlPullParser.getName();
 		else
 			return xmlPullParser.getName();
 	}
 
 	protected void printParse() throws XmlPullParserException, IOException
 	{
 		int event;
 		do
 		{
 			event = xmlPullParser.getEventType();
 			switch (event)
 			{
 			case XmlPullParser.START_TAG:
 				System.out.print("start element: ");
 				System.out.print(xmlPullParser.getEventType());
 				System.out.print(" : ");
 				System.out.print(xmlPullParser.getName().toString());
 				System.out.println();
 				break;
 			case XmlPullParser.END_TAG:
 				System.out.print("end element: ");
 				System.out.print(xmlPullParser.getEventType());
 				System.out.print(" : ");
 				System.out.print(xmlPullParser.getName().toString());
 				System.out.println();
 				break;
 			case XmlPullParser.TEXT:
 				System.out.print("TEXT: ");
 				System.out.print(xmlPullParser.getEventType());
 				System.out.print(" : ");
 				System.out.print(xmlPullParser.getText());
 				System.out.println();
 				break;
 			case XmlPullParser.CDSECT:
 				System.out.println("cdata " + xmlPullParser.getText());
 				break;
 			default:
 				System.out.println(xmlPullParser.getEventType());
 			} // end switch
 
 		}
 		while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT);
 	}
 
 }
