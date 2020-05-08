 // See the COPYRIGHT file for copyright and license information
 package org.xins.logdoc;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 
 import org.w3c.dom.Document;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * Entity/URI resolver that can be used during XML parsing and during XSLT
  * transformation.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 class Resolver implements EntityResolver, URIResolver {
    
    // TODO: Cache the DTD information internally
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The URL to the local <code>log_3_0</code> DTD.
     * Initialized by the class initializer.
     */
    private static final URL LOG_DTD_URL;
    
    /**
     * The URL to the local <code>translation-bundle_3_0</code> DTD.
     * Initialized by the class initializer.
     */
    private static final URL TRANSLATION_BUNDLE_DTD_URL;
 
   
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
    * Class initializer that reads the DTDs into memory.
     */
    static {
       // TODO: Move this to the Library class
 
       LOG_DTD_URL = Library.getMetaResource("dtd/log_3_0.dtd");
       if (LOG_DTD_URL == null) {
          throw new Error("Failed to load log_3_0.dtd file.");
       }
       
       TRANSLATION_BUNDLE_DTD_URL = Library.getMetaResource("dtd/translation-bundle_3_0.dtd");
       if (TRANSLATION_BUNDLE_DTD_URL == null) {
          throw new Error("Failed to load translation-bundle_3_0.dtd file.");
       }
    }
 
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Resolver</code> for the specified input
     * directory.
     * 
     * @param dir
     *    the directory containing the input files,
     *    cannot be <code>null</code>.
     *    
     * @throws IllegalArgumentException
     *    if <code>dir == null</code>.
     */
    Resolver(File dir) throws IllegalArgumentException {
       if (dir == null) {
          throw new IllegalArgumentException("dir == null");
       }
       _inputDir = dir;
    }
 
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The input directory. Never <code>null</code>.
     */
    private final File _inputDir;
    
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public Document loadInputDocument(String fileName) throws IllegalArgumentException, IOException {
       
       // Check preconditions
       if (fileName == null) {
          throw new IllegalArgumentException("fileName == null");
       }
       
       File file = new File(_inputDir, fileName);
       
       try {
  
          // Create a validating DOM/XML parser
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setValidating(true);
  
          DocumentBuilder domBuilder = factory.newDocumentBuilder();
          domBuilder.setEntityResolver(this);
          domBuilder.setErrorHandler(new ErrorHandler());
  
          // Parse the file to produce a DOM/XML object
          return domBuilder.parse(file);
  
       } catch (ParserConfigurationException cause) {
          IOException e = new IOException("Failed to parse \"log.xml\" file.");
          e.initCause(cause);
          throw e;
       } catch (SAXException cause) {
          IOException e = new IOException("Failed to parse \"log.xml\" file.");
          e.initCause(cause);
          throw e;
       }
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
 	throws SAXException, IOException {
       //System.err.println("Resolver.resolveEntity called with publicId=\"" + publicId + "\" and systemId=\"" + systemId + "\".");
       if ("-//Logdoc//DTD Logdoc 3.0//EN".equals(publicId)) {
          return new InputSource(LOG_DTD_URL.openStream());
       } else if ("-//Logdoc//DTD Translation Bundle 3.0//EN".equals(publicId)) {
          return new InputSource(TRANSLATION_BUNDLE_DTD_URL.openStream());
       }
 		return null;
 	}
 
 	public Source resolve(String href, String base) throws TransformerException {
 
      System.err.println("Resolver.resolve called with href=\"" + href + "\" and base=\"" + base + "\".");
 
 	   // Check preconditions
 	   if (href == null) {
 	      throw new TransformerException("href == null");
 	      
 	   // XSLT file
 	   } else if (href.endsWith(".xslt")) {
 	      String resultURL = "xslt/" + href;
          try {
    		   return new StreamSource(Library.getMetaResourceAsStream(resultURL));
          } catch (IOException cause) {
             throw new TransformerException("Failed to open meta resource \"" + resultURL + "\".", cause);
          }
          
       // Input file
 	   } else if (href.endsWith(".xml")) {
          return new StreamSource(new File(_inputDir, href));
          
       // Unknown file
 	   } else {
 	      throw new TransformerException("File with href \"" + href + "\" is not recognized.");
 	   }
 	}
 	
	
    //-------------------------------------------------------------------------
 	// Inner classes
    //-------------------------------------------------------------------------
 	
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
 
       public void error(SAXParseException exception) throws SAXException {
          exception.printStackTrace();
          throw new SAXException(exception);
       }
 
       public void fatalError(SAXParseException exception) throws SAXException {
          exception.printStackTrace();
          throw new SAXException(exception);
       }
 
       public void warning(SAXParseException exception) throws SAXException {
          // empty
       }
    }
 }
