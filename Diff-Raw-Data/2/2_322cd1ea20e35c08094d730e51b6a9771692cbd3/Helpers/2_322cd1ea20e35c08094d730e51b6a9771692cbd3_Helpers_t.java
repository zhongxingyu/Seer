 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package helpers;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.*;
 import javax.xml.xpath.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import models.Asset;
 import models.TextReference;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XsltCompiler;
 import net.sf.saxon.s9api.XsltExecutable;
 import net.sf.saxon.s9api.XsltTransformer;
 import org.jsoup.Jsoup;
 
 /**
  *
  * @author pe
  */
 public class Helpers {
 
     public static String readFile(String path) throws IOException {
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             /* Instead of using default, pass in a decoder. */
            return Charset.forName("utf-8").decode(bb).toString();
         } finally {
             stream.close();
         }
     }
 
     public static String getReferencesFromXml(String xml, String fileName) {
         StringBuilder refs = new StringBuilder(xml);
         try {
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(true);
             DocumentBuilder builder = domFactory.newDocumentBuilder();
             InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
             Document doc = builder.parse(in);
 
             XPath xpath = XPathFactory.newInstance().newXPath();
             XPathExpression expr = xpath.compile("//*");
             Object result = expr.evaluate(doc, XPathConstants.NODESET);
             NodeList nodes = (NodeList) result;
             System.out.println("------- Number of references found: " + nodes.getLength());
             for (int i = 0; i < nodes.getLength(); i++) {
                 Node n = nodes.item(i);
                 if (n.getNodeName() == "seg") {
                     String ref = n.getAttributes().getNamedItem("type").getNodeValue();
                     if (!ref.equals("com")) {
                         String id = fileName.replaceFirst(".xml", "_") + ref;
                         TextReference comment = TextReference.find("textId = ? and type = ?", id, Asset.commentType).first();
                         System.out.println("id: " + id + " comment: " + comment.showName);
                         if (comment != null) {
                             refs.append(comment.showName);
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         // System.out.println("Refs: " + refs.toString());
         return refs.toString();
 
     }
 
     public static String nodeToString(Node node) {
         StringWriter sw = new StringWriter();
         try {
             Transformer t = TransformerFactory.newInstance().newTransformer();
             t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
             t.transform(new DOMSource(node), new StreamResult(sw));
         } catch (TransformerException te) {
             System.out.println("nodeToString Transformer Exception");
         }
         return sw.toString();
     }
 
     public static Document stringToNode(String str) {
         Document doc = null;
         try {
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(true);
             DocumentBuilder builder = domFactory.newDocumentBuilder();
             InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
             doc = builder.parse(in);
             doc.getDocumentElement().normalize();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return doc;
     }
 
 
     public static List<Node> getChildrenOfType(Node parent, String tagName) {
         ArrayList<Node> nodes = new ArrayList<Node>();
         NodeList children = parent.getFirstChild().getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             if (children.item(i).getNodeName().equals(tagName)) {
                 nodes.add(children.item(i));
             }
         }
         return nodes;
     }
 
     public static Node getChildOfType(Node parent, String tagName) {
         NodeList children = parent.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             if (children.item(i).getNodeName().equals(tagName)) {
                 return children.item(i);
             }
         }
         return null;
     }
 
     public static String xmlToHtml(String xml, String filePath) {
         try {
             // File xmlIn = new File(fileName);
             StreamSource source = new StreamSource(new ByteArrayInputStream(xml.getBytes("utf-8")));
             Processor proc = new Processor(false);
             XsltCompiler comp = proc.newXsltCompiler();
             XsltExecutable exp = comp.compile(new StreamSource(new File(filePath)));
             Serializer out = new Serializer();
             ByteArrayOutputStream buf = new ByteArrayOutputStream();
             out.setOutputProperty(Serializer.Property.METHOD, "html");
             out.setOutputProperty(Serializer.Property.INDENT, "yes");
             out.setOutputStream(buf);
             // out.setOutputFile(new File("tour.html"));
             XsltTransformer trans = exp.load();
             // trans.setInitialTemplate(new QName("main"));
             trans.setSource(source);
             trans.setDestination(out);
             trans.transform();
             // System.err.println("Output generated: " + buf.toString());
             return buf.toString();
         } catch (Exception e) {
             e.printStackTrace();
             return ("Error: " + e.toString());
         }
 
     }
 
     public static void copyfile(String srFile, String dtFile) {
         try {
             File f1 = new File(srFile);
             File f2 = new File(dtFile);
             InputStream in = new FileInputStream(f1);
 
             //For Overwrite the file.
             OutputStream out = new FileOutputStream(f2);
 
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             in.close();
             out.close();
             System.out.println("File copied.");
         } catch (FileNotFoundException ex) {
             System.out.println(ex.getMessage() + " in the specified directory.");
             System.exit(0);
         } catch (IOException e) {
             System.out.println(e.getMessage());
         }
     }
 
     public static String stripHtml(String html) {
         if (html == null) return ""; // in case of pictures :-)
         return Jsoup.parse(html).text();
     }
 
     public static String createTeaser(String str, String lookfor, int len) {
         int lookforStart = str.indexOf(lookfor);
         int lookforEnd = lookforStart + lookfor.length();
         int start = lookforStart;
         int stop = lookforEnd;
 
         // find stop
         while (stop > str.length() && ((stop - lookforEnd) < len)) stop++;
         while (start > 0 && ((lookforStart - start) < len)) start--;
 
 
         return str.substring(start, stop);
     }
 
 }
