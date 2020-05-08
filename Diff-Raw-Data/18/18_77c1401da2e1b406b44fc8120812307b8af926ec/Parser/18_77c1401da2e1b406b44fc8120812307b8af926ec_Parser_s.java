 package com.benbenedek.parser;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.PrettyXmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * @author Ben Benedek
  *
  *	Main Parsing unit.
  */
 public class Parser {
 	
 	private HtmlCleaner cleaner;
 	private URL urlSrc;
 	private CleanerProperties properties;
 	private Document xmlOutput;
 	private TagNode tagNode;
 	
 	private String charSet = "utf-8";
 	
 	/**
 	 * Create instance of parser from URL.
 	 * @see java.net.URL
 	 * @param URL src
 	 */
 	public Parser(URL src) {
 		initParser();
 		this.urlSrc = src;
 	}
 	
 	
 	/**
 	 * Create instance of parser from String URL source.
 	 * @param String src
 	 */
 	public Parser(String src) {
 		try {
 			this.urlSrc = new URL(src);
 		} catch (MalformedURLException e) {
 			throw new RuntimeException("Invalid URL inserted as source for Parser.");
 		}
 		initParser();
 	}
 		
 	
 	/**
 	 * Public Parse API, parse the source returns no output.
 	 */
 	public void parse() {
 		validateSource();		
 		validateProperties();
 		
 		this.tagNode = run();
 	}
 	
	public TagNode run() {
 		TagNode tagNode = null;
 		try {
 			// Actual parsing.
 			tagNode = cleaner.clean( urlSrc );
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Unexpected error while parsing.");
 		}
 		
 		return tagNode;
 	}
 	
 	private void validateSource() {
 		// Check if we have input source to parse.
 		if ( urlSrc == null ) { 
 			throw new RuntimeException("No input source.");
 		}
 	}
 
 	private void validateProperties() {
 		// Check if we have properties file, if not, create it.
 		if (( properties == null ) && (cleaner != null)) {
 			properties = cleaner.getProperties();
 		}
 		else {
 			throw new RuntimeException("Properties file is null, default parser was not initiaited.");
 		}
 	}
 	
 	private void initParser() {
 		this.cleaner = new HtmlCleaner();
 	}
 	
 	public void createDocumentObject() { 
 		
 		String xmlString = null;
 		try {
 			xmlString = new PrettyXmlSerializer(properties).getAsString(tagNode, charSet);
 		} catch (IOException e) {
 			e.printStackTrace();
 			new RuntimeException("Error while creating Document Object");
 		}
 		
 		if ( xmlString != null ) {
 			try {
 				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 				DocumentBuilder db = dbf.newDocumentBuilder();
 	            InputSource is = new InputSource(new StringReader(xmlString));
 	            this.xmlOutput = db.parse(is);
 			} catch (ParserConfigurationException e) {				
 				e.printStackTrace();
 				throw new RuntimeException();
 			} catch (SAXException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			} catch (IOException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 
 		}
 		
 	}
 	
 	public void outputToXML(String fileName) {
 		try {
 			new PrettyXmlSerializer(properties).writeToFile(tagNode, fileName, charSet);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Error while writing parse results to file.");
 		}
 	}
 	
 	public Document getXmlOutput() {
 		return xmlOutput;
 	}
 
 
 	public void setUrlSrc(URL urlSrc) {
 		this.urlSrc = urlSrc;
 	}
 
 
 	public void setProperties(CleanerProperties properties) {
 		this.properties = properties;
 	}
 
 
 	public void setCharSet(String charSet) {
 		this.charSet = charSet;
 	}
 }
