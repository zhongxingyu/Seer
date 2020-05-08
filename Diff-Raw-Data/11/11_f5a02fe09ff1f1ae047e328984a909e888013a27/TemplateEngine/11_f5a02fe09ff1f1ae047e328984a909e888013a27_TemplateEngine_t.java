 package ir.xweb.module;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Map;
 
 /**
  * This feature will add to XWeb ResourceModule soon!
 * @Deprecated Please Use ResourceModule instead
  */
@Deprecated
 class TemplateEngine {
 
     private final static Logger logger = LoggerFactory.getLogger("TemplateEngine");
 
     private final File dir;
 
     public TemplateEngine(final File dir) {
         if(dir == null) {
             throw new IllegalArgumentException("Null template directory");
         }
         this.dir = dir;
     }
 
     public String apply(final String template, final Map<String, String> params) {
         final File xsltFile = new File(dir, template + ".xsl");
         if(xsltFile.exists()) {
             try {
                 final String xml = paramToXml(params);
                 final String html = applyXslt(xsltFile, xml);
 
                 return html;
             } catch (Exception ex) {
                 logger.error("Error to apply XSLT template: " + xsltFile);
             }
         }
 
         return null;
     }
 
     private String applyXslt(final File template, final String xml) throws Exception {
         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
         final DocumentBuilder builder = factory.newDocumentBuilder();
         final InputSource is = new InputSource();
         is.setCharacterStream(new StringReader(xml));
 
         // Use a Transformer for output
         final TransformerFactory tFactory = TransformerFactory.newInstance();
         final StreamSource stylesource = new StreamSource(template);
         final Transformer transformer = tFactory.newTransformer(stylesource);
 
         final StringWriter w = new StringWriter();
         final DOMSource source = new DOMSource(builder.parse(is));
         final StreamResult result = new StreamResult(w);
         transformer.transform(source, result);
 
         return w.toString();
     }
 
     private String paramToXml(final Map<String, String> params) throws IOException {
         final Element root = new Element("params");
 
         for(Map.Entry<String, String> e:params.entrySet()) {
             final Element param = new Element(e.getKey());
             param.setText(e.getValue());
             root.addContent(param);
         }
 
         final XMLOutputter xmlOutput = new XMLOutputter();
 
         final StringWriter w = new StringWriter();
         xmlOutput.setFormat(Format.getRawFormat());
         xmlOutput.output(new Document(root), w);
 
         return w.toString();
     }
 
 }
