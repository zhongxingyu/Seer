 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.internal;
 
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
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import org.znerd.logdoc.Library;
 import org.znerd.logdoc.LogLevel;
 import static org.znerd.logdoc.internal.ExceptionUtils.newIOException;
 import static org.znerd.logdoc.internal.InternalLogging.log;
 
 /**
  * URI resolver that can be used during XSLT transformations.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class Resolver implements URIResolver {
 
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
    public Resolver(File dir) throws IllegalArgumentException {
 
       // Check preconditions
       if (dir == null) {
          throw new IllegalArgumentException("dir == null");
       }
 
       // Initialize object
       _inputDir = dir;
       log(LogLevel.DEBUG, "Created Resolver for input directory \"" + dir.getAbsolutePath() + "\".");
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
 
       log(LogLevel.DEBUG, "Loading input document \"" + fileName + "\".");
 
      File file = new File(_inputDir, fileName);
 
       try {
 
          // Create a non-validating DOM/XML parser
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setValidating(false);
 
          DocumentBuilder domBuilder = factory.newDocumentBuilder();
          domBuilder.setErrorHandler(new ErrorHandler());
 
          // Parse the file to produce a DOM/XML object
          return domBuilder.parse(file);
 
       } catch (ParserConfigurationException cause) {
          throw newIOException("Failed to parse \"" + fileName + "\" file.", cause);
       } catch (SAXException cause) {
          throw newIOException("Failed to parse \"" + fileName + "\" file.", cause);
       }
    }
 
    public Source resolve(String href, String base) throws TransformerException {
 
       log(LogLevel.INFO, "Resolving href \"" + href + "\" (with base \"" + base + "\") during XSLT transformation.");
 
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
 
    /**
     * Error handler for the <code>Resolver</code>.
     *
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     */
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
 
       public void warning(SAXParseException exception) throws SAXException {
          log(LogLevel.WARNING, "Warning during XML parsing.", exception);
       }
 
       public void error(SAXParseException exception) throws SAXException {
          log(LogLevel.ERROR, "Error during XML parsing.", exception);
          throw new SAXException(exception);
       }
 
       public void fatalError(SAXParseException exception) throws SAXException {
          log(LogLevel.ERROR, "Fatal error during XML parsing.", exception);
          throw new SAXException(exception);
       }
    }
 }
