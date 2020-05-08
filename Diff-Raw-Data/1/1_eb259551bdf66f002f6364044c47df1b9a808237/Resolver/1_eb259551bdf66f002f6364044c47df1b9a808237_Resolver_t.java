 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.internal;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 
 import org.w3c.dom.Document;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import org.znerd.logdoc.Library;
 import org.znerd.util.Preconditions;
 import org.znerd.util.log.LogLevel;
 
 import static org.znerd.util.log.Limb.log;
 
 /**
  * URI resolver that can be used during XSLT transformations to resolve input files and XSLT files.
  */
 public class Resolver implements URIResolver {
 
     private final File inputDir;
     private final String xsltBaseDir;
 
     public Resolver(File inputDir, String xsltBaseDir) throws IllegalArgumentException {
         Preconditions.checkArgument(inputDir == null, "inputDir == null");
         Preconditions.checkArgument(xsltBaseDir == null, "xsltBaseDir == null");
         this.inputDir = inputDir;
         this.xsltBaseDir = xsltBaseDir;
         log(LogLevel.DEBUG, "Created Resolver for input directory \"" + inputDir.getAbsolutePath() + "\" with XSLT base directory \"" + xsltBaseDir + "\".");
     }
 
     public Document loadInputDocument(String fileName) throws IllegalArgumentException, IOException {
         Preconditions.checkArgument(fileName == null, "fileName == null");
 
         log(LogLevel.DEBUG, "Loading input document \"" + fileName + "\".");
         File file = createFileObject(fileName);
         return loadInputDocumentImpl(fileName, file);
     }
 
     private final File createFileObject(String fileName) {
         File file = new File(fileName);
         if (!file.isAbsolute()) {
             file = new File(inputDir, fileName);
         }
         return file;
     }
 
     private Document loadInputDocumentImpl(String fileName, File file) throws FactoryConfigurationError, IOException {
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
             factory.setValidating(false);
 
             DocumentBuilder domBuilder = factory.newDocumentBuilder();
             domBuilder.setErrorHandler(new ErrorHandler());
 
             return domBuilder.parse(file);
         } catch (ParserConfigurationException cause) {
             throw new IOException("Failed to parse \"" + fileName + "\" file.", cause);
         } catch (SAXException cause) {
             throw new IOException("Failed to parse \"" + fileName + "\" file.", cause);
         }
     }
 
     public Source resolve(String href, String base) throws TransformerException {
         Preconditions.checkArgument(href == null, "href == null");
         log(LogLevel.INFO, "Resolving href \"" + href + "\" (with base \"" + base + "\") during XSLT transformation.");
 
         if (href.endsWith(".xslt")) {
             return resolveXsltFileForTransformation(href);
         } else if (href.endsWith(".xml")) {
             return resolveInputFile(href);
         } else {
             throw new TransformerException("File with href \"" + href + "\" is not recognized.");
         }
     }
 
     private Source resolveXsltFileForTransformation(String href) throws TransformerException {
         try {
             return resolveXsltFile(href);
         } catch (IOException cause) {
             throw new TransformerException("Failed to open meta resource \"" + href + "\".", cause);
         }
     }
 
     public Source resolveXsltFile(String href) throws IOException {
         String resultUrl = "xslt/" + xsltBaseDir + href;
         return new StreamSource(Library.getMetaResourceAsStream(resultUrl));
     }
 
     private Source resolveInputFile(String href) {
         return new StreamSource(createFileObject(href));
     }
 
     private static class ErrorHandler implements org.xml.sax.ErrorHandler {
         @Override
         public void warning(SAXParseException exception) throws SAXException {
             log(LogLevel.WARNING, "Warning during XML parsing.", exception);
         }
 
         @Override
         public void error(SAXParseException exception) throws SAXException {
             log(LogLevel.ERROR, "Error during XML parsing.", exception);
             throw new SAXException(exception);
         }
 
         @Override
         public void fatalError(SAXParseException exception) throws SAXException {
             log(LogLevel.ERROR, "Fatal error during XML parsing.", exception);
             throw new SAXException(exception);
         }
     }
 }
