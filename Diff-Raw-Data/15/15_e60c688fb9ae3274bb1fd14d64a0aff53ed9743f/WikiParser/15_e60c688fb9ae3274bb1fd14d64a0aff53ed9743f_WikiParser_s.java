 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.jdom.Content;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.wyona.jspwiki.Html2WikiXmlTransformer;
 
 import com.ecyrd.jspwiki.WikiContext;
 import com.ecyrd.jspwiki.WikiEngine;
 import com.ecyrd.jspwiki.WikiException;
 import com.ecyrd.jspwiki.WikiPage;
 
 
public class WikiParser {
     
     private Properties properties = null;
     private String pathToWikiFile = "examples/test.txt";
     private String pathToProperties = "jar/parser.properties";
     
     private Element bodyElement = null;
     
     public static void main(String[] args) {
         new TestJspWikiParser();
     }
     
     public TestJspWikiParser() {
         loadProperties();
         testParser();
     }
     
     public void loadProperties() {
         //loading properties
         properties = new Properties();
         try {
             properties.load(new FileInputStream(pathToProperties));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public void testParser() {
         InputStream inputStream = convertFileToInputStream();
         
         
         WikiEngine engine = null;
         try {
             engine = new WikiEngine(properties);
         } catch (WikiException e) {
             e.printStackTrace();
         }
         WikiPage page = new WikiPage(engine, "PAGE");
         WikiContext context = new WikiContext(engine, page);
         
         BufferedReader bufferedReader;
         try {
             bufferedReader = new BufferedReader(new FileReader(new File(pathToWikiFile)));
             String line = null;
             StringBuffer stringBuffer = new StringBuffer();
             while ((line = bufferedReader.readLine()) != null) {
                 stringBuffer.append(line + "\n");                
             }
             
             String test = engine.textToHTML(context, stringBuffer.toString());
             //System.out.println(test + "\n\n");
             
             StringBuffer html = new StringBuffer();
             html.append("<html><body>");
             html.append(test);
             html.append("</body></html>");
             System.out.println(html.toString());
             
             File temp = File.createTempFile("pattern", ".suffix");
             // Delete temp file when program exits.
             temp.deleteOnExit();
         
             // Write to temp file
             BufferedWriter out = new BufferedWriter(new FileWriter(temp));
             out.write(html.toString());
             out.close();
             
             SAXBuilder builder = new SAXBuilder();
             Document doc = builder.build(temp);
             Element root = doc.getRootElement();
             bodyElement = root.getChild("body");
             List listRootElements = bodyElement.getChildren();
             for(int i=0; i<listRootElements.size(); i++) {
                 Element element  = (Element)(listRootElements.get(i));
                 if(element.getName().equalsIgnoreCase("ul") || element.getName().equalsIgnoreCase("ol")) {
                     treeWalker(element, 1);
                 }
             }
             
             XMLOutputter outputter = new XMLOutputter();
             
             String modifiedHtml = outputter.outputString(doc);
             System.out.println("#####################################\n" + modifiedHtml + "\n\n");
             Html2WikiXmlTransformer html2WikiXml = new Html2WikiXmlTransformer();
             SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
             //saxParser.parse(new java.io.ByteArrayInputStream(createdHtml.toString().getBytes()), html2WikiXml);
             saxParser.parse(new java.io.ByteArrayInputStream(modifiedHtml.getBytes()), html2WikiXml);
             
             //setResultAsInputStream(html2WikiXml.getInputStream());
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
     
     public void treeWalker(Content node, int counter) {
         if(node instanceof Element) {
             Element element = (Element) node;
             if(!("true").equals(element.getAttributeValue("moved"))) {
                 if(element.getName().equals("li")) {
                     element.setAttribute("depth", "" + counter);
                     counter++;
                 }    
                 List listChildren = element.getChildren();
                 for(int i=0; i<listChildren.size(); i++) {
                     treeWalker((Element) listChildren.get(i), counter);
                 }
                 if(element.getName().equals("ul")) {
                     moveElement(element, "ul");
                 }
                 if(element.getName().equals("ol")) {
                     moveElement(element, "ol");
                 }
                 element.setAttribute("moved", "true");
             }
         }
     }
     
     public void moveElement(Element element, String name) {
         int position = 0;
         Vector keepThisKids = new Vector();
         Element grandParentElement = element.getParentElement().getParentElement();
         if(grandParentElement.getName().equals(name)) {
             Element temp = element.getParentElement();
             List movingChildren = element.getChildren();
             for(int i=0; i<movingChildren.size(); i++) {
                 keepThisKids.add(movingChildren.get(i));
                 ((Element)(movingChildren.get(i))).detach();
             }
             element.detach();
             position = grandParentElement.indexOf(temp) + 1;
             grandParentElement.addContent(position, keepThisKids);
         } else 
         if(name.equals("ol") && grandParentElement.getName().equals("ul") || name.equals("ul") && grandParentElement.getName().equals("ol")) {
             Element subRoot = getActualRootsChild(element);
             List movingChildren = element.getChildren();
             for(int i=0; i<movingChildren.size(); i++) {
                 keepThisKids.add(movingChildren.get(i));
                 ((Element)(movingChildren.get(i))).detach();
             }
             element.detach();
             position = bodyElement.indexOf(subRoot) + 1;
             bodyElement.addContent(position, keepThisKids);
         }
         //TODO handle this bug DONT know where to attach this 
     }
     
     public Element getActualRootsChild(Element element) {
         if(element.getParentElement().equals(bodyElement)) return element;
         else return getActualRootsChild(element.getParentElement());
     }
     
     public void debugInputStream(InputStream inputStream) {
         try {
             InputStreamReader inR = new InputStreamReader(inputStream);
             BufferedReader buf = new BufferedReader(inR);
             String line;
             while ((line = buf.readLine()) != null) {
                 System.out.println(line);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     private InputStream convertFileToInputStream() {
         String record = null;
         StringBuffer fileData = new StringBuffer();
         try {
             BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(pathToWikiFile)));
             while ((record = bufferedReader.readLine()) != null) {
                 fileData.append(record + "\n");
                 //System.out.println(record);
             }
         } catch (IOException e) {
             System.out.println("Uh oh, got an IOException error!" + e.getMessage());
         }
         return new ByteArrayInputStream(fileData.toString().getBytes());
     }
 }
