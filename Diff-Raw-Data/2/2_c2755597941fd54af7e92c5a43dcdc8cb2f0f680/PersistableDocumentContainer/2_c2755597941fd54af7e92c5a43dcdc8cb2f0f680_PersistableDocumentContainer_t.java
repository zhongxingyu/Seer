 package uk.ac.ebi.ae15.utils.persistence;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.ByteArrayInputStream;
 import java.io.StringWriter;
 
 
 public class PersistableDocumentContainer implements Persistable
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     // document version (for checking purposes)
    private final String XML_DOCUMENT_VERSION = "1.1.080625";
 
     // document storage
     private Document document;
 
     public PersistableDocumentContainer()
     {
         createDocument();
     }
 
     public PersistableDocumentContainer( Document doc )
     {
         if (null == doc) {
             createDocument();
         } else {
             document = doc;
             versionDocument();
         }
     }
 
     public Document getDocument()
     {
         return document;
     }
 
     public String toPersistence()
     {
         OutputFormat format = new OutputFormat();
 
         format.setLineSeparator(String.valueOf(EOL));
         format.setIndenting(true);
         format.setLineWidth(0);
         format.setPreserveSpace(true);
 
         StringWriter sw = new StringWriter();
 
         try {
             XMLSerializer serializer = new XMLSerializer(sw, format);
             serializer.asDOMSerializer();
             serializer.serialize(document);
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
 
         return sw.toString();
     }
 
     public void fromPersistence( String str )
     {
         if (null == str || 0 == str.length()) {
             document = null;
         } else {
 
             try {
                 //parse using builder to get DOM representation of the XML file
                 DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 document = docBuilder.parse(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
 
                 if (null != document) {
                     String docVer = document.getDocumentElement().getAttribute("version");
                     if (!XML_DOCUMENT_VERSION.equals(docVer)) {
                         log.error("Loaded document version mismatch: loaded [" + docVer + "], expected [" + XML_DOCUMENT_VERSION + "]");
                         document = null;
                     }
                 }
             } catch ( Throwable x ) {
                 log.error("Caught an exception:", x);
             }
         }
         if (null == document) {
             createDocument();
         }
     }
 
     public boolean shouldLoadFromPersistence()
     {
         if (null == document)
             return true;
 
         String total = document.getDocumentElement().getAttribute("total");
 
         return (null == total || total.equals("0"));
     }
 
     private void createDocument()
     {
         try {
             DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             document = docBuilder.newDocument();
 
             Element expElement = document.createElement("experiments");
             expElement.setAttribute("total", "0");
             expElement.setAttribute("version", XML_DOCUMENT_VERSION);
 
             document.appendChild(expElement);
 
         } catch ( Throwable x ) {
             log.debug("Caught an exception:", x);
         }
 
         if (null == document) {
             log.error("The document WAS NOT created, expect problems down the road");
         }
 
     }
 
     private void versionDocument()
     {
         if (null != document) {
             Element docElement = document.getDocumentElement();
             if (null != docElement) {
                 docElement.setAttribute("version", XML_DOCUMENT_VERSION);
             }
         }
     }
 }
