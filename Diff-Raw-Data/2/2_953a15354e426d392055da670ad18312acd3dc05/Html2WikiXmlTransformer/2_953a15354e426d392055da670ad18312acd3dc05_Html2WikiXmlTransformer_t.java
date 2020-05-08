 package org.wyona.jspwiki;
 
 import java.io.ByteArrayInputStream;
 import java.util.Vector;
 import org.apache.log4j.Category;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class Html2WikiXmlTransformer extends DefaultHandler {
     
     private final String NAME_SPACE = "http://www.wyona.org/yanel/1.0";
     
     private static Category log = Category.getInstance(Html2WikiXmlTransformer.class);
     private ByteArrayInputStream byteArrayInputStream = null;
     private StringBuffer html2xml = null;
     private Vector htmlElements = new Vector();
     
     private String listType = "";
     private String tagName = null;
     private boolean startTag = false;
     private String plain = "";
     
     /**
      * this method is called at the begging of the document
      */
     public void startDocument() throws SAXException {
         html2xml = new StringBuffer();
         html2xml.append("<wiki xmlns:wiki=\""+NAME_SPACE+"\" xmlns=\""+NAME_SPACE+"\">");
     }
 
     /**
      * this method is called when the document end has been reached
      */
     public void endDocument() throws SAXException {
         iterateVector();
         html2xml.append("</wiki>");
         log.debug("\n\n--------------\n" + html2xml.toString());
         setResultInputStream();
     }
     
     /**
      * this method is for debugging only 
      * to show which elements have been added
      *
      */
     public void showVectorElements() {
         for(int i=0; i<htmlElements.size(); i++) {
             System.out.println((i+1) + ". " + (String) htmlElements.elementAt(i));
         }
     }
     
     /**
      * this method walks through all the Vector elements and calls 
      * iterateVectorElements
      *
      */
     private void iterateVector() {
         for(int i=0; i<htmlElements.size(); i++) {
             iterateVectorElements(i, (String) htmlElements.elementAt(i));
         }
     }
     
     /**
      * this method iterated through the Vectorelements
      * and appends the according WikiTags so that the page can be 
      * edited with YULUP   
      * @param position of Vector htmlElements
      * @param elementName the tag name of the returning jspWikiPage 
      */
     private void iterateVectorElements(int position, String elementName) {
         
         if(elementName.startsWith("START_")) {
             tagName = elementName.substring(6);
             startTag = true;
         } else 
         if(elementName.startsWith("END_")) {
             tagName = elementName.substring(4);
             startTag = false;
         } else {
             tagName = elementName;
             handleText(tagName);  
         }
 
         if(tagName.equals("ol")) handleOl();
         if(tagName.equals("ul")) handleUl();
         if(tagName.equals("li")) handleLi(position);
         if(tagName.equals("br")) handleBr();
         if(tagName.equals("hr")) handleHr();
         if(tagName.equals("a")) handleA(position);
         if(tagName.equals("span")) handleSpan();
         if(tagName.equals("h4")) handleH4();
         if(tagName.equals("h3")) handleH3();
         if(tagName.equals("h2")) handleH2();
         if(tagName.equals("b")) handleB();
         if(tagName.equals("i")) handleI();
         if(tagName.equals("p")) handleP();
         if(tagName.equals("u")) handleU();
         if(tagName.equals("tt")) handleTt();
         if(tagName.equals("dl")) handleDl();
         if(tagName.equals("dt")) handleDt();
         if(tagName.equals("dd")) handleDd();
         if(tagName.equals("table")) handleTable();
         //if(tagName.equals("th")) handleTh();
         if(tagName.equals("tr")) handleTr();
         if(tagName.equals("td")) handleTd();
         if(tagName.equals("p")) handleP();
         if(tagName.equals("u")) handleU();
     }
     
     /**
      * this method handles the tag BR
      *
      */
     private void handleBr() {
         if(startTag) html2xml.append("<ForceNewline/>");
     }
     
     /**
      * this method handles the tag OL
      *
      */
     private void handleOl() {
         if(startTag) {
             listType = "N";
             String tag = "<" + listType + "List>";
             html2xml.append(tag);
         } else {
             String tag = "</" + listType + "List>";
             html2xml.append(tag);
             listType = "";
         }
     }
     
     /**
      * this method handles the tag Ul
      *
      */
     private void handleUl() {
         if(startTag) {
             listType = "B";
             String tag = "<" + listType + "List>";
             html2xml.append(tag);
         } else {
             String tag = "</" + listType + "List>";
             html2xml.append(tag);
             listType = "";
         }
     }
     
     /**
      * this method handles the tag LI
      * @param position of the Vector htmlElements
      */
     private void handleLi(int position) {
         if(startTag) {
             String depth = getNextElementAsString(position + 1);
             //remove this element 
             htmlElements.remove(position+1);
             String tag = "<" + listType + "ListItem depth=\"" + depth + "\">";
             html2xml.append(tag); 
         } else {
             String tag = "</" + listType + "ListItem>";
             html2xml.append(tag); 
         }
     }
     
     /**
      * this method handles the tag HR
      *
      */
     private void handleHr() {
         if(startTag) html2xml.append("<Hrule/>");
     }
     
     /**
      * this method handles the tag a 
      * @param position of Vector htmlElements
      */
     private void handleA(int position) {
         if(startTag) {
             String href = getNextElementAsString(position + 1);
            String defaultNotExistingLink = "Edit.jsp?page="; 
            if(href.indexOf(defaultNotExistingLink) != -1) href = href.substring(defaultNotExistingLink.length());
             String linkLabel = ""; 
             String label = getNextElementAsString(position + 2);
             if(!label.startsWith("END_") && !label.equals(href)) {
                 linkLabel = " label=\"" + label + "\"";
             }
             html2xml.append("<Link href=\"" + href + "\"" + linkLabel + ">");
         } else {
             html2xml.append("</Link>");
         }
     }
     
     /**
      * this method will return the href or the label of an link 
      *
      */
     private String getNextElementAsString(int position) {
         return (String) htmlElements.elementAt(position);
     }
     
     /**
      * this method handles the tag SPAN which is indication 
      * something went wrong
      *
      */
     private void handleSpan() {
         if(startTag) html2xml.append("<Error>");
         else html2xml.append("</Error>");
     }
     
     /**
      * this method handles the tag H4
      *
      */
     private void handleH4() {
         if(startTag) html2xml.append("<MainMainTitle>");
         else html2xml.append("</MainMainTitle>");
     }
     
     /**
      * this method handles the tag H3
      *
      */
     private void handleH3() {
         if(startTag) html2xml.append("<MainTitle>");
         else html2xml.append("</MainTitle>");
     }
     
     /**
      * this method handles the tag H2
      *
      */
     private void handleH2() {
         if(startTag) html2xml.append("<Title>");
         else html2xml.append("</Title>");
     }
     
     /**
      * this method handles the tag B
      *
      */
     private void handleB() {
         if(startTag) html2xml.append("<Bold>");
         else html2xml.append("</Bold>");
     }
     
     /**
      * this method handles the tag I
      *
      */
     private void handleI() {
         if(startTag) html2xml.append("<Italic>");
         else html2xml.append("</Italic>");
     }
     
     /**
      * this method handles the tag P
      *
      */
     private void handleP() {
         if(startTag) html2xml.append("<Paragraph>");
         else html2xml.append("</Paragraph>");
     }
     
     /**
      * this method handles the tag U
      *
      */
     private void handleU() {
         if(startTag) html2xml.append("<Underline>");
         else html2xml.append("</Underline>");
     }
     
     
     /**
      * this method handles the tag TT
      *
      */
     private void handleTt() {
         if(startTag) {
             html2xml.append("<Plain>");
             plain = "Plain";
         } else {
             html2xml.append("</Plain>");
             plain = "";
         }
     }
     
     /**
      * this method handles the tag DL
      *
      */
     private void handleDl() {
         if(startTag) html2xml.append("<DefinitionList>"); 
         else html2xml.append("</DefinitionList>");
     }
     
     /**
      * this method handles the tag DT
      *
      */
     private void handleDt() {
         if(startTag) html2xml.append("<Term>");
         else html2xml.append("</Term>");
     }
     
     /**
      * this method handles the tag DD
      *
      */
     private void handleDd() {
         if(startTag) html2xml.append("<Definition>"); 
         else html2xml.append("</Definition>");
     }
     
     /**
      * this method handles the tag TABLE
      *
      */
     private void handleTable() {
         if(startTag) html2xml.append("<Table>"); 
         else html2xml.append("</Table>");
     }
     
     /**
      * this method handles the tag TR
      *
      */
     private void handleTr() {
         if(startTag) html2xml.append("<TableRow>");
         else html2xml.append("</TableRow>");
     }
     
     /**
      * this method handles the tag TD
      *
      */
     private void handleTd() {
         if(startTag) html2xml.append("<TableCol>");
         else html2xml.append("</TableCol>");
     }
     
     /**
      * this method is called whenever the value of a tag is being processed
      * @param elementName 
      */
     private void handleText(String elementName) {
         for(int i = 0; i < elementName.length(); i++) {
             if(elementName.charAt(i) == '\n') {} else
             if(elementName.charAt(i) == '"') { html2xml.append("<" + plain + "Text value=\"&#34;\"/>"); } else
             if(elementName.charAt(i) == '<') { html2xml.append("<" + plain + "Text value=\"&#60;\"/>"); } else
             if(elementName.charAt(i) == '>') { html2xml.append("<" + plain + "Text value=\"&#62;\"/>"); }
             else html2xml.append("<" + plain + "Text value=\"" + elementName.charAt(i) + "\"/>");
         }
     }
     
     /**
      * this method will be called whenever a start tag is processed
      */
     public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
         String eName = ("".equals(localName)) ? qName : localName;
         if(eName.equals("html") || eName.equals("body")) {//ignore html and body
         } else {
             htmlElements.add("START_" + eName);
             for(int i=0; i<attrs.getLength(); i++) {
                 if(attrs.getQName(i).equals("href")) {
                     String href = attrs.getValue(i);
                     htmlElements.add(href);
                 }
                 if(attrs.getQName(i).equals("depth")) {
                     String depth = attrs.getValue(i);
                     htmlElements.add(depth);
                 }
             }
         }
     }
     
     /**
      * this method will be called whenever a Tag is closed
      */
     public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
         String eName = ("".equals(localName)) ? qName : localName;
         if(eName.equals("html") || eName.equals("body")) {//ignore html and body
         } else {
             htmlElements.add("END_" + eName);
         }
     }
 
     /**
      * this methods handles all the tag values and converts them to TextTags
      * where every character will be wrapped in a TextTag e.g. Text will be
      * transformed to: 
      * <Text value="T"/> 
      * <Text value="e"/> 
      * <Text value="x"/> 
      * <Text value="t"/> 
      */
     public void characters(char[] buf, int offset, int len) throws SAXException {
         String value = new String(buf, offset, len);
         if(!value.equals("\n")) htmlElements.add(value);
     }
 
     /**
      * this method set the result InputStream 
      *
      */
     private void setResultInputStream() {
         this.byteArrayInputStream = new ByteArrayInputStream(html2xml.toString().getBytes());
     }
  
     /**
      * this method returns the result as InputStream
      * @return InputStream
      */
     public ByteArrayInputStream getInputStream() {
         return this.byteArrayInputStream;
     }
 
     /**
      * this method shows the transformed xml as String
      * @return transformed xml as String 
      */
     public String showTransformedXmlAsString() {
         return html2xml.toString();
     }
 }
