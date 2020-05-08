 package uk.ac.ebi.arrayexpress.components;
 
 import net.sf.saxon.om.AxisIterator;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.s9api.*;
 import net.sf.saxon.type.Type;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
 
 import javax.xml.transform.ErrorListener;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.net.URL;
import java.util.HashMap;
 import java.util.Map;
 
 public class SaxonEngine extends ApplicationComponent implements URIResolver, ErrorListener
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     private Processor processor;
 
     private DocumentBuilder docBuilder;
     private XsltCompiler xsltCompiler;
     private XPathCompiler xpathCompiler;
 
    private Map<String,XsltExecutable> xsltExecCache = new HashMap<String,XsltExecutable>();

     private final String XML_STRING_ENCODING = "ISO-8859-1";
 
     public SaxonEngine()
     {
         super("SaxonEngine");
     }
 
     public void initialize()
     {
         processor = new Processor(false);
 
         docBuilder = processor.newDocumentBuilder();
 
         xsltCompiler = processor.newXsltCompiler();
         xsltCompiler.setErrorListener(this);
         xsltCompiler.setURIResolver(this);
 
         xpathCompiler = processor.newXPathCompiler();
     }
 
     public void terminate()
     {
     }
 
     // implements URIResolver.resolve
     public Source resolve( String href, String base ) throws TransformerException
     {
         Source src;
         try {
             URL resource = Application.getInstance().getResource("/WEB-INF/server-assets/stylesheets/" + href);
             if (null == resource) {
                 throw new TransformerException("Unable to locate stylesheet resource [" + href + "]");
             }
             InputStream input = resource.openStream();
             if (null == input) {
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
 
     // implements ErrorListener.error
     public void error( TransformerException x ) throws TransformerException
     {
         throw x;
     }
 
     // implements ErrorListener.fatalError
     public void fatalError( TransformerException x ) throws TransformerException
     {
         throw x;
     }
 
     // implements ErrorListenet.warning
     public void	warning( TransformerException x )
     {
         log.warn("There was a warning while transforming:", x);
     }
 
     public String serializeDocument( XdmNode document )
     {
         String string = null;
         Serializer out = new Serializer();
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 
         out.setOutputProperty(Serializer.Property.METHOD, "xml");
         out.setOutputProperty(Serializer.Property.INDENT, "no");
         out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "no");
         out.setOutputStream(outStream);
 
         try {
             processor.writeXdmValue(document, out);
             string = outStream.toString(XML_STRING_ENCODING);
         } catch (Throwable x) {
             log.error("Caught an exception:", x);
         }
 
         return string;
     }
 
     public XdmNode buildDocument( String xml )
     {
         StringReader reader = new StringReader(xml);
         XdmNode document = null;
         try {
             document = docBuilder.build(new StreamSource(reader));
         } catch (Throwable x) {
             log.error("Caught an exception:", x);
         }
 
         return document;
     }
 
     public String evaluateXPathSingle( XdmNode node, String xpath )
     {
         try {
             XPathSelector selector = xpathCompiler.compile(xpath).load();
             selector.setContextItem(node);
             XdmItem result = selector.evaluateSingle();
 
             if (null != result) {
                 if (result.isAtomicValue()) {
                     return result.getStringValue();
                 } else {
                     return ((XdmNode)result).getUnderlyingNode().getStringValue();
                 }
             }
         } catch (Throwable x) {
             log.error("Caught an exception:", x);
         }
 
         return null;
     }
     
     public XdmValue evaluateXPath( XdmNode node, String xpath )
     {
         try {
             XPathSelector selector = xpathCompiler.compile(xpath).load();
             selector.setContextItem(node);
             return selector.evaluate();
         } catch (Throwable x) {
             log.error("Caught an exception:", x);
         }
 
         return null;
     }
 
     public String concatAllText( XdmValue nodes )
     {
         StringBuilder buf = new StringBuilder();
         if (null != nodes) {
             for (XdmItem node : nodes) {
                 buf.append(concatAllText((XdmNode)node)).append(' ');
             }
         }
         return buf.toString();
     }
 
     public String concatAllText( XdmNode node )
     {
         StringBuilder buf = new StringBuilder();
 
         if (null != node) {
             AxisIterator nodesItor = node.getUnderlyingNode().iterateAxis(net.sf.saxon.om.Axis.DESCENDANT_OR_SELF);
             do {
                 NodeInfo next = (NodeInfo)nodesItor.next();
 
                 // if null there is no next
                 if (null == next) {
                     break;
                 }
 
                 if ( Type.TEXT == next.getNodeKind()) {
                     if (null != next.getStringValue() && 0 != next.getStringValue().length()) {
                         buf.append(next.getStringValue()).append(' ');
                     }
                 } else if (Type.ELEMENT == next.getNodeKind()) {
                     // iterate over attributes and collect values from there
                     AxisIterator attributesItor = next.iterateAxis(net.sf.saxon.om.Axis.ATTRIBUTE);
                     do {
                         NodeInfo nextAttr = (NodeInfo)attributesItor.next();
 
                         // if null there is no next
                         if (null == nextAttr) {
                             break;
                         }
 
                         // append attribute value (if any)
                         if (null == nextAttr.getStringValue() || 0 != nextAttr.getStringValue().length()) {
                             buf.append(nextAttr.getStringValue()).append(' ');
                         }
 
                     } while (true);
                 }
 
             } while (true);
         }
 
         return buf.toString();
     }
 
     public boolean transformToWriter( XdmNode srcDocument, String stylesheet, Map<String,String> params, Writer dstWriter )
     {
         Serializer out = new Serializer();
         out.setOutputWriter(dstWriter);
 
         return transform(null, srcDocument, stylesheet, params, out);
     }
 
     public boolean transformToFile( XdmNode srcDocument, String stylesheet, Map<String,String> params, File dstFile )
     {
         Serializer out = new Serializer();
         out.setOutputFile(dstFile);
 
         return transform(null, srcDocument, stylesheet, params, out);
     }
 
     public String transformToString( XdmNode srcDocument, String stylesheet, Map<String,String> params )
     {
         String str = null;
         Serializer out = new Serializer();
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         out.setOutputStream(outStream);
 
         if (transform(null, srcDocument, stylesheet, params, out)) {
             try {
                 str = outStream.toString(XML_STRING_ENCODING);
             } catch (Throwable x) {
                 log.error("Caught an exception:", x);
             }
             return str;
         } else {
             return null;
         }
     }
 
     public XdmNode transform( String srcXmlString, String stylesheet, Map<String,String> params )
     {
         try {
             Source src = new StreamSource(new StringReader(srcXmlString));
             XdmDestination dst = new XdmDestination();
             if (transform(src, null, stylesheet, params, dst)) {
                 return dst.getXdmNode();
             }
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
         return null;
     }
 
     public XdmNode transform( XdmNode srcDocument, String stylesheet, Map<String,String> params )
     {
         try {
             XdmDestination dst = new XdmDestination();
             if (transform(null, srcDocument, stylesheet, params, dst)) {
                 return dst.getXdmNode();
             }
         } catch ( Throwable x ) {
             log.error("Caught an exceptiom:", x);
         }
 
         return null;
     }
 
     private boolean transform( Source src, XdmNode srcDoc, String stylesheet, Map<String,String> params, Destination dst )
     {
         boolean result = false;
         try {
            XsltExecutable xsltExec;
            if (!xsltExecCache.containsKey(stylesheet)) {
                log.debug("Caching XSLT Executable for stylesheet [" + stylesheet + "]");
                // Open the stylesheet
                Source xslSource = resolve(stylesheet, null);

                xsltExec = xsltCompiler.compile(xslSource);
                xsltExecCache.put(stylesheet, xsltExec);
            } else {
                log.debug("Getting XSLT Executable for stylesheet [" + stylesheet + "] from the cache");
                xsltExec = xsltExecCache.get(stylesheet);
            }
             XsltTransformer xslt = xsltExec.load();
 
             // assign the parameters (if not null)
             if (null != params) {
                 for ( Map.Entry<String, String> param : params.entrySet() ) {
                     xslt.setParameter(new QName(param.getKey()), new XdmAtomicValue(param.getValue()));
                 }
             }
 
             if (null != srcDoc) {
                 xslt.setInitialContextNode(srcDoc);
             } else {
                 xslt.setSource(src);
             }
             
             xslt.setDestination(dst);
             // Perform the transformation, sending the output to the response.
             log.debug("about to start transformer.transform() with stylesheet [" + stylesheet + "]");
             xslt.transform();
             log.debug("transformer.transform() completed");
 
             result = true;
         } catch ( Throwable x ) {
             if (x.getMessage().contains("java.lang.InterruptedException")) {
                 log.error("Transformation has been interruped");
 
             } else {
                 log.error("Caught an exception transforming [" + stylesheet + "]:", x);
             }
         }
         return result;
     }
 }
