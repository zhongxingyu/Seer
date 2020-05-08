 package RefScraper.data;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
 * Model of science festival page that contains lists of items to be processed
  * @author al
  */
 public class EdSciEventListPage {
 
     private final URL theURL;
     private final Document theDocument;
     private final Logger theLogger;
    private static String theBaseURL = "http://www.sciencefestival.co.uk";
 
     /**
      * Constructs model of edinburgh science festival list page.
      * @param newURL 
      * @param logger
      * @throws IOException
      * @throws ParserConfigurationException
      * @throws SAXException  
      */
     public EdSciEventListPage(URL newURL,
             Logger logger) throws IOException, ParserConfigurationException, SAXException {
         theURL = newURL;
         theLogger = logger;
         HTMLPageParser theParser = new HTMLPageParser(theLogger);
         theDocument = theParser.getParsedPage(theURL);
     }
 
     /**
      * @return -the base url for this page
      */
     public static String getBaseURL() {
         return theBaseURL;
     }
 
     /**
      * @return -the page url
      */
     public URL getURL() {
         return theURL;
     }
 
     /**
      * Finds candidate links by looking various sections of the page.
      * @return - the candidate links
      */
     public List<HTMLLink> getCandidates() {
         List<HTMLLink> theCandidates = new ArrayList<HTMLLink>();
         getMainListCandidates(theCandidates);
 
         return theCandidates;
     }
 
     /**
      * Finds candidate links by looking up top level links in the page
      * @param theCandidates - the links to populate
      */
     private void getMainListCandidates(List<HTMLLink> theCandidates) {
         NodeList linkNodeList = null;
 
         try {
             String searchString = "/html//div[@id='event-listing']/div[@class='element']/a";
             XPath linkXpath = XPathFactory.newInstance().newXPath();
             linkNodeList = (NodeList) linkXpath.evaluate(searchString, theDocument, XPathConstants.NODESET);
 
             int listLength = linkNodeList.getLength();
 
             for (int i = 0; i < listLength; ++i) {
                 Node childNode = (Node) linkNodeList.item(i);
                 short nodeType = childNode.getNodeType();
 
                 Element theElement = (Element) childNode;
                 String theTitle = theElement.getAttribute("title");
                 String theHREF = theElement.getAttribute("href");
                
                if(!theHREF.startsWith("http://")){
                    theHREF = getBaseURL() + theHREF;
                }
                
                 String theText = theElement.getTextContent();
 
                 if (!theHREF.isEmpty()) {
                     theLogger.log(Level.INFO, "Found candidate :{0}", theTitle);
                     HTMLLink theCandidate = new HTMLLink(theTitle, theHREF);
                     
                     DateFormat theDateFormat = new SimpleDateFormat("EEE dd MMM");
                     theDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                     String theDatePeriodString = getDatePeriodString(childNode, theHREF);
 
                     try {
                         Date startDate = theDateFormat.parse(theDatePeriodString);
                     } catch (ParseException ex) {
                         theCandidate.setPeriodString(theDatePeriodString.replace("From ", ""));
                         theLogger.log(Level.WARNING, "Parse failure on date for :{0}, setting it as period", theHREF);
                     }
                     
                     theCandidates.add(theCandidate);
                 }
             }
         } catch (Exception e) {
             theLogger.log(Level.SEVERE, "Exception on XPath: ", e);
         }
     }
 
     private String getDatePeriodString(Node childNode,
             String theHREF) {
         String datePeriodString = "";
         
         try {
             //                String detailsSearchString = "../div[@class='details']/table/tr";  
             String detailsSearchString = "../div[@class='details']/table";
             XPath detailsXpath = XPathFactory.newInstance().newXPath();
             NodeList detailsNodeList = (NodeList) detailsXpath.evaluate(detailsSearchString, childNode, XPathConstants.NODESET);
 
             if (detailsNodeList != null) {
                 boolean dateFound = false;
                 int detailsLength = detailsNodeList.getLength();
 
                 if (detailsLength > 0) {
                     Node theTable = detailsNodeList.item(0);
                     String theContent = theTable.getTextContent().trim();
                     String[] theContentBits = theContent.split("\n");
 
                     if (theContentBits.length > 1) {
                         String theHeaderBit = theContentBits[0].trim();
                         String theDetailBit = theContentBits[1].trim();
 
                        if (theHeaderBit.equalsIgnoreCase("Date:") ||
                            theHeaderBit.equalsIgnoreCase("Dates:")) {
                             datePeriodString = EdSciEventListPage.getAsciiText(theDetailBit);
                         }
                     }
                 }
             }
         } catch (XPathExpressionException ex) {
             Logger.getLogger(EdSciEventListPage.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return datePeriodString;
     }
         
     static String getAsciiText(String theText) {
         StringBuilder theBuilder = new StringBuilder();
         int lengthInChars = theText.length();
         int noOfCodePoints = theText.codePointCount(0, lengthInChars - 1);
 
         try {
             if (lengthInChars > 0
                     && lengthInChars > noOfCodePoints) {
                 for (int offset = 0; offset < lengthInChars;) {
                     final int codePoint = theText.codePointAt(offset);
                     char theCharAt = theText.charAt(offset);
 
                     if (codePoint >= 0 && codePoint < 128) {
                         theBuilder.append(theCharAt);
                     } else {
                         theBuilder.append(EdSciEventListPage.asciiFromUTF(codePoint));
                     }
 
                     offset += Character.charCount(codePoint);
                 }
             } else {
                 theBuilder.append(theText);
             }
         } catch (IndexOutOfBoundsException exc) {
             System.out.println("out of bounds");
         }
 
         return theBuilder.toString();
     }
     
     static char asciiFromUTF(int codePoint) {
         char retVal = ' ';
         switch (codePoint) {
             case 8211:
                 retVal = '-';
                 break;
         }
 
         return retVal;
     }
 }
