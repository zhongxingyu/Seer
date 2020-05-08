 package dk.statsbiblioteket.doms.common;
 
 import org.w3c.dom.Document;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import java.io.ByteArrayInputStream;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathConstants;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  */
 public class FFProbeParser {
 
 	private final String allowedFormatName;
 	private final String formaturi;
 	
 	public FFProbeParser(String allowedFormatName, String formaturi) {
 		this.allowedFormatName = allowedFormatName;
 		this.formaturi = formaturi;
 	}
 	
     public String getFormatURIFromFFProbeOutput(String FFProbeOutput)
             throws XPathExpressionException, ParserConfigurationException,
             IOException, SAXException {
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(false);
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(new ByteArrayInputStream(
                 FFProbeOutput.getBytes()));
         XPathFactory xPathfactory = XPathFactory.newInstance();
 
         XPath xpath;
         XPathExpression expr;
 
         // Get format name
         String format_name;
         xpath = xPathfactory.newXPath();
         expr = xpath.compile("ffprobe/format/@format_name");
         format_name = expr.evaluate(doc);
         if (format_name.trim().isEmpty()){
             throw new RuntimeException("Invalid ffprobe file, no format_name");
         }
 
         // Get codec name
         List<String> codecs = new ArrayList<String>();
         xpath = xPathfactory.newXPath();
         expr = xpath.compile("ffprobe/streams/stream/@codec_name");
         NodeList codecsNodeList
                 = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
         Set<String> codecSet = new HashSet<String>();
         // Convert nodelist to a set of strings, to remove duplicates
         for(int i = 0; i < codecsNodeList.getLength(); i++) {
             codecSet.add(codecsNodeList.item(i).getNodeValue());
         }
         // Convert set to a list of strings
         codecs = new ArrayList<String>(codecSet);
         // Sort it
         Collections.sort(codecs);
 
         String format_uri;
         if (format_name.equals(allowedFormatName)){
             format_uri = formaturi;
         } else {
            throw new RuntimeException("Invalid ffprobe file, bad format name");
         }
 
         if (codecs.size() > 0){
             format_uri= format_uri + ";codecs=\"";
             for (int i = 0; i < codecs.size(); i++){
                 String codec = codecs.get(i);
                 format_uri = format_uri + codec;
                 if (i+1 != codecs.size()){
                     format_uri = format_uri + ",";
                 }
             }
             format_uri = format_uri + "\"";
         }
 
         return format_uri;
     }
 }
