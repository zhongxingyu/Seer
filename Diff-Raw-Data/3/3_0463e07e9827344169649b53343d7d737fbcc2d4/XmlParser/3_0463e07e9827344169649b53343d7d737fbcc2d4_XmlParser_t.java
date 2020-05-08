 /**
  * 
  */
 package edu.wustl.cab2b.client.ui.searchDataWizard;
 
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
/**This Class as name Suggests is for parsing XML using SAX Parser. It is for converting System generated 
 * DCQL XML into user friendly DCQL XML format
  * @author gaurav_mehta
  * 
  */
 public class XmlParser extends DefaultHandler {
 
     // StringBUffer in which entire formatted XML is stored and then returned to display it on GUI
     private StringBuffer xmlText = new StringBuffer("<HTML><BODY>\n");
 
     // Count is used for indenting the XML as to show the hierarchy in XML 
     private int count = 0;
 
     /**
       * Default Constructor
       */
     public XmlParser() {
 
     }
 
     /**
      * This function accepts the dcqlQuery as string and then parses it using SAX Parser to return the formatted XML
      * @param dcqlQuery
      * @return xmlText is a string of formatted DCQL in XML format
      * @throws RuntimeException
      */
     public String parseXml(String dcqlQuery) throws RuntimeException {
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser saxParser = factory.newSAXParser();
             saxParser.parse(new InputSource(new StringReader(dcqlQuery)), this);
 
             xmlText.append("</BODY></HTML>");
         } catch (SAXException saxException) {
             throw new RuntimeException(saxException.getMessage());
         } catch (ParserConfigurationException pce) {
             throw new RuntimeException(pce.getMessage());
         } catch (IOException ioException) {
             throw new RuntimeException(ioException.getMessage());
         }
 
         return xmlText.toString();
     }
 
     @Override
     public void startElement(String uri, String localName, String qName, Attributes attributes)
             throws SAXException {
         for (int i = 0; i < count; i++) {
             xmlText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
         }
 
         xmlText.append(setColor("BLACK", "&lt"));
         xmlText.append(setColor("PURPLE", qName + " "));
         if (attributes != null) {
             for (int i = 0; i < attributes.getLength(); i++) {
                 if (i != 0 && (i % 2) == 0) {
                     xmlText.append("<br>");
                     for (int j = 0; j < count + 1; j++)
                         xmlText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                 }
 
                 xmlText.append(setColor("OLIVE", attributes.getQName(i)));
                 xmlText.append(setColor("BLACK", "="));
                 xmlText.append(setColor("BLACK", String.valueOf('"')));
                 xmlText.append(setColor("BLUE", attributes.getValue(i)));
 
                 if (i == (attributes.getLength() - 1)) {
                     xmlText.append(setColor("BLACK", String.valueOf('"')));
                 } else {
                     xmlText.append(setColor("BLACK", String.valueOf('"') + " "));
                 }
             }
         }
         xmlText.append(setColor("BLACK", ">"));
         xmlText.append("<br>");
 
         count++;
     }
 
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         count--;
         for (int i = 0; i < count; i++) {
             xmlText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
         }
         xmlText.append(setColor("BLACK", "&lt/"));
         xmlText.append(setColor("PURPLE", qName));
         xmlText.append(setColor("BLACK", ">"));
         xmlText.append("<br>");
     }
 
     @Override
     public void characters(char[] character, int start, int length) throws SAXException {
         if (length > 10) {
             for (int i = 0; i < count; i++) {
                 xmlText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
             }
 
             for (int i = start; i < (start + length); i++) {
                 xmlText.append(setColor("BLUE", String.valueOf(character[i])));
             }
         }
     }
 
     /**
      * This function is used to represent different tags in different colors
      * The tags colors is done HTML way.
      * @param color 
      * @param tag
      * @return formatted XML Tag in string form
      */
     String setColor(String color, String tag) {
         StringBuffer htmlStart = new StringBuffer("<span style='font-size:14pt;"
                 + "font-family:Courier New;font-weight:normal;color:");
 
         StringBuffer formattedText = new StringBuffer();
         String htmlEnd = "</span>";
 
         int length = htmlStart.length();
         if ("BLACK".equals(color)) {
             htmlStart = htmlStart.append("black'>");
             formattedText = formattedText.append(htmlStart).append(tag).append(htmlEnd);
             htmlStart.setLength(length);
         } else if ("PURPLE".equals(color)) {
             htmlStart = htmlStart.append("purple'>");
             formattedText = formattedText.append(htmlStart).append(tag).append(htmlEnd);
             htmlStart.setLength(length);
         } else if ("BLUE".equals(color)) {
             htmlStart = htmlStart.append("blue'>");
             formattedText = formattedText.append(htmlStart).append(tag).append(htmlEnd);
             htmlStart.setLength(length);
         } else if ("OLIVE".equals(color)) {
             htmlStart = htmlStart.append("olive'>");
             formattedText = formattedText.append(htmlStart).append(tag).append(htmlEnd);
             htmlStart.setLength(length);
         }
 
         return formattedText.toString();
     }
 
 }
