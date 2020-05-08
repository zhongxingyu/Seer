 package uk.ac.ebi.ae15.components;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import uk.ac.ebi.ae15.app.Application;
 import uk.ac.ebi.ae15.app.ApplicationComponent;
 import uk.ac.ebi.ae15.utils.AppXalanExtension;
 
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
import java.net.URL;
 import java.util.Map;
 
 public class XsltHelper extends ApplicationComponent implements URIResolver
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     private TransformerFactory tFactory;
 
     public XsltHelper( Application app )
     {
         super(app, "XsltHelper");
     }
 
     public void initialize()
     {
         AppXalanExtension.setApplication(getApplication());
     }
 
     public void terminate()
     {
         AppXalanExtension.setApplication(null);
     }
 
     public Source resolve( String href, String base ) throws TransformerException
     {
         Source src;
         try {
            URL resource = getApplication().getServletContext().getResource("/WEB-INF/server-assets/stylesheets/" + href);
            if ( null == resource ) {
                throw new TransformerException("Unable to locate stylesheet resource [" + href + "]");
            }
            InputStream input = resource.openStream();
            if ( null == input ) {
                throw new TransformerException("Unable to open stream for resource [" + resource + "]");
            }
            src = new StreamSource(input);
        } catch ( TransformerException x ) {
            throw x;
         } catch ( Exception x ) {
             log.error("Caught an exception:", x);
             throw new TransformerException(x.getMessage());
         }
 
         return src;
     }
 
     public boolean transformDocumentToFile( Document srcDocument, String stylesheet, Map params, File dstFile )
     {
         try {
             return transform(new DOMSource(srcDocument), stylesheet, params, new StreamResult(new FileOutputStream(dstFile)));
         } catch ( Throwable x ) {
             log.error("Caught an exceptiom:", x);
         }
 
         return false;
     }
 
     public String transformDocumentToString( Document srcDocument, String stylesheet, Map params )
     {
         try {
             StringWriter sw = new StringWriter();
             if (transform(new DOMSource(srcDocument), stylesheet, params, new StreamResult(sw))) {
                 return sw.toString();
             }
         } catch ( Throwable x ) {
             log.error("Caught an exceptiom:", x);
         }
 
         return null;
     }
 
     public Document transformStringToDocument( String srcXmlString, String stylesheet, Map params )
     {
         try {
             InputStream inStream = new ByteArrayInputStream(srcXmlString.getBytes("ISO-8859-1"));
             DOMResult dst = new DOMResult();
             if (transform(new StreamSource(inStream), stylesheet, params, dst)) {
                 if (null != dst.getNode()) {
                     return (Document) dst.getNode();
                 }
             }
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
         return null;
     }
 
     public boolean transformDocumentToPrintWriter( Document srcDocument, String stylesheet, Map params, PrintWriter dstWriter )
     {
         return transform(new DOMSource(srcDocument), stylesheet, params, new StreamResult(dstWriter));
     }
 
     private boolean transform( Source src, String stylesheet, Map params, Result dst )
     {
         boolean result = false;
         try {
             // create a transformer factory if null
             if (null == tFactory) {
                 tFactory = TransformerFactory.newInstance();
                 tFactory.setURIResolver(this);
             }
 
             // Open the stylesheet
             Source xslSource = resolve(stylesheet, null);
 
             // Generate the transformer.
             Transformer transformer = tFactory.newTransformer(xslSource);
 
             // assign the parameters (if not null)
             if (null != params) {
                 for ( Object param : params.entrySet() ) {
                     Map.Entry p = (Map.Entry) param;
                     transformer.setParameter((String) p.getKey(), arrayToString((String[]) p.getValue()));
                 }
             }
 
             // Perform the transformation, sending the output to the response.
             log.debug("about to start transformer.transform() with stylesheet [" + stylesheet + "]");
             transformer.transform(src, dst);
             log.debug("transformer.transform() completed");
 
             result = true;
         } catch ( Throwable x ) {
             log.error("Caught an exception transforming [" + stylesheet + "]:", x);
         }
         return result;
     }
 
     private static String arrayToString( String[] array )
     {
         StringBuilder sb = new StringBuilder();
         for ( String item : array ) {
             sb.append(item).append(' ');
         }
         return sb.toString().trim();
     }
 }
