 // See the COPYRIGHT file for copyright and license information
 package org.xins.logdoc.def;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * Log definition. Typically read from a <code>log.xml</code> file with one or
  * more associated translation bundles, see {@link TranslationBundleDef}.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since Logdoc 3.0
  */
 public final class LogDef {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Loads a log definition from a specified directory.
     *
     * @param dir
     *    the directory to load the log definition from,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>dir == null</code>, or if it is not a directory.
     *
     * @throws IOException
     *    if the definition could not be loaded.
     */
    public static final LogDef loadFromDirectory(File dir)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       if (dir == null) {
          throw new IllegalArgumentException("dir == null");
       } else if (! dir.isDirectory()) {
          throw new IllegalArgumentException("Path (\"" + dir.getPath() + "\") is not a directory.");
       }
 
       // Define the location of the log.xml file
       File file = new File(dir, "log.xml");
 
       Document xml;
       try {
 
          // Create a validating DOM/XML parser
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setValidating(true);
 
          DocumentBuilder domBuilder = factory.newDocumentBuilder();
          LogdocResolver    resolver = new LogdocResolver();
          domBuilder.setEntityResolver(resolver);
 
          // Parse the file to produce a DOM/XML object
          xml = domBuilder.parse(file);
 
       } catch (ParserConfigurationException cause) {
          IOException e = new IOException("Failed to parse \"log.xml\" file.");
          e.initCause(cause);
          throw e;
       } catch (SAXException cause) {
          IOException e = new IOException("Failed to parse \"log.xml\" file.");
          e.initCause(cause);
          throw e;
       }
 
       return new LogDef(xml);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>LogDef</code>.
     *
     * @param xml
     *    the XML {@link Document} to construct this object from,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>.
     */
    private LogDef(Document xml) throws IllegalArgumentException {
 
       // Check preconditions
       if (xml == null) {
          throw new IllegalArgumentException("xml == null");
       }
 
       // Initialize fields
       _xml = xml;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The source file as a DOM document. Never <code>null</code>.
     */
    private final Document _xml;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Generates the Java code for this log definition.
     *
     * @param targetDir
     *    the target directory to create the Java source files in,
     *    cannot be <code>null</code>, and must be an existent writable
     *    directory.
     *
     * @param packageName
     *    the name of the package for the Java code,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>targetDir == null || packageName == null || accessLevel == null</code>
     *    or if <code>packageName</code> is not a valid Java package name.
     *
     * @throws IOException
     *    if the Java code could not be generated.
     */
    public void generateJavaCode(File targetDir, String packageName)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       if (targetDir == null) {
          throw new IllegalArgumentException("targetDir == null");
       } else if (packageName == null) {
          throw new IllegalArgumentException("packageName == null");
       } else if (! packageName.matches("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)")) {
          throw new IllegalArgumentException("Invalid package name \"" + packageName + "\".");
       }
 
       // Perform transformations
       transform(targetDir, packageName, "Log");
       transform(targetDir, packageName, "TranslationBundle");
    }
 
    private Source getSource() {
       return new DOMSource(_xml);
    }
 
    private IOException newIOException(String detail, Throwable cause) {
       IOException e = new IOException(detail);
       e.initCause(cause);
       return e;
    }
 
    private void transform(File baseDir, String packageName, String className)
    throws IOException {
 
       try {
 
          // Create an XSLT Transforer
          String                   xsltPath = "/xslt/log_to_" + className + "_java.xslt";
          InputStream            xsltStream = getClass().getResourceAsStream(xsltPath);
          StreamSource     xsltStreamSource = new StreamSource(xsltStream);
          TransformerFactory xformerFactory = TransformerFactory.newInstance();
          Transformer               xformer = xformerFactory.newTransformer(xsltStreamSource);
 
          // TODO: Set the parameters for the template
 
          // Define where the output should go
          File               outDir = new File(baseDir, packageName.replace("\\.", "/"));
          File              outFile = new File(outDir, className + ".java");
          StreamResult streamResult = new StreamResult(outFile);
 
          // Perform the transformation
          xformer.transform(getSource(), streamResult);
 
       // Transformer configuration error
       } catch (TransformerConfigurationException cause) {
          throw newIOException("Failed to perform XSLT transformation.", cause);
 
       // Transformer error
       } catch (TransformerException cause) {
          throw newIOException("Failed to perform XSLT transformation.", cause);
       }
    }
 }
