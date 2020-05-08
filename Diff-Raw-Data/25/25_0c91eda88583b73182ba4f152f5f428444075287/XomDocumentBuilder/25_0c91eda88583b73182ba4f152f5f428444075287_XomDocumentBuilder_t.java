 package org.caudexorigo.xom;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.NodeFactory;
 import nu.xom.Nodes;
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 import nu.xom.XMLException;
 
 import org.caudexorigo.jpt.JptConfiguration;
 import org.caudexorigo.jpt.JptNotFoundException;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 public final class XomDocumentBuilder
 {
 	private static final Logger log = LoggerFactory.getLogger(XomDocumentBuilder.class);
 
 	// These are stored in the order of preference.
 	private static String[] parsers = { "org.apache.xerces.parsers.SAXParser", "com.sun.org.apache.xerces.internal.parsers.SAXParser", "gnu.xml.aelfred2.XmlReader", "org.apache.crimson.parser.XMLReaderImpl", "com.bluecast.xml.Piccolo", "oracle.xml.parser.v2.SAXParser", "com.jclark.xml.sax.SAX2Driver", "net.sf.saxon.aelfred.SAXDriver", "com.icl.saxon.aelfred.SAXDriver", "org.dom4j.io.aelfred2.SAXDriver", "org.dom4j.io.aelfred.SAXDriver" };
 
 	private static final XMLReader reader;
 
 	static
 	{
 		if (StringUtils.isBlank(JptConfiguration.xmlReaderClass()))
 		{
 			reader = findParser();
 		}
 		else
 		{
 			reader = createParser(JptConfiguration.xmlReaderClass());
 		}
 
 		if (reader == null)
 		{
 			throw new RuntimeException("Unable to create XMLReader");
 		}
 		else
 		{
 			reader.setEntityResolver(new XhtmlEntityResolver());
 		}
 	}
 
 	static XMLReader createParser(String parserClass)
 	{
 		XMLReader parser;
 		try
 		{
 			parser = XMLReaderFactory.createXMLReader(parserClass);
 
 			return parser;
 		}
 		catch (Exception ex)
 		{
 			throw new XMLException(parserClass + " not found or not a a suitable SAX2 parser", ex);
 		}
 	}
 
 	static XMLReader findParser()
 	{
 		// first look for Xerces; we only trust Xerces if
 		// we set it up; and we need to configure it specially
 		// so we can't load it with the XMLReaderFactory
 		XMLReader parser;
 
 		// XMLReaderFactory.createXMLReader never returns
 		// null. If it can't locate the parser, it throws
 		// a SAXException.
 		for (int i = 0; i < parsers.length; i++)
 		{
 			try
 			{
 				parser = XMLReaderFactory.createXMLReader(parsers[i]);
 
 				try
 				{
 					parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
 					parser.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
 					parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
 					parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
 				}
 				catch (Exception e)
 				{
 					// ignore, we can live without this.
 				}
 
 				return parser;
 			}
 			catch (SAXException ex)
 			{
 				// try the next one
 			}
 			catch (NoClassDefFoundError err)
 			{
 				// try the next one
 			}
 		}
 
 		try
 		{ // default
 			parser = XMLReaderFactory.createXMLReader();
 
 			return parser;
 		}
 		catch (SAXException ex)
 		{
 			throw new XMLException("Could not find a suitable SAX2 parser", ex);
 		}
 	}
 
 	public static Document getDocument(InputStream in) throws ValidityException, ParsingException, IOException
 	{
		synchronized (reader)
 		{
			if (reader == null)
			{
				XMLReader singularity_reader = findParser();
				Builder builder = new Builder(singularity_reader, false);
				return builder.build(in);
			}

			Builder builder = new Builder(reader, false);
 			return builder.build(in);
 		}
 	}
 
 	public static Document getDocument(URI templateUri) throws ValidityException, ParsingException, IOException
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("building document: {}", templateUri.toString());
 		}
 
 		// File ftemplate = new File(JptUtil.resolvePath(templatePath));
 
 		File ftemplate = new File(templateUri);
 
 		FileInputStream fis;
 		try
 		{
 			fis = new FileInputStream(ftemplate);
 		}
 		catch (FileNotFoundException e)
 		{
 			throw new JptNotFoundException(e);
 		}
 
		synchronized (reader)
		{
			Builder builder = new Builder(reader, false);
			return builder.build(fis);
		}
 	}
 
 	private XomDocumentBuilder()
 	{
 	}
 }
