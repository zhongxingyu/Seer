 package RefScraper.data;
 
 import RefScraper.HTMLPageParser;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Class to hold information for placing a reference that has position and date
  * or duration
  * @author al
  */
 public class RefThree implements Comparable {
 
     String theName;
     private String theLatitude;
     private String theLongitude;
     private URL theLocationRef;
     private URL thePlace;
     private Date theStartDate;
     private Date theEndDate;
     private URL theURL;
     private static PeriodMap thePeriodMap = new PeriodMap();
     private static PositionMap thePositionMap = new PositionMap();
     private String theBaseURL = "http://en.wikipedia.org";
     private final Logger theLogger;
 
     /**
      * 
      * @param theTitle 
      * @param theHREF 
      * @param logger 
      */
     public RefThree(String theTitle,
             String theHREF,
             Logger logger) {
         String thePageHREF = theHREF;
 
         if (thePageHREF.indexOf("http://") != 0) {
             thePageHREF = theBaseURL + thePageHREF;
         }
 
         try {
             theURL = new URL(thePageHREF);
         } catch (MalformedURLException ex) {
             Logger.getLogger(RefThree.class.getName()).log(Level.SEVERE, null, ex);
         }
         theName = theTitle;
         theLogger = logger;
     }
 
     /**
      * a copy constructor so that the object is not shred in threads
      * @param theOther 
      */
     public RefThree(RefThree theOther) {
         theName = theOther.theName;
         theLatitude = theOther.theLatitude;
         theLongitude = theOther.theLongitude;
         theLocationRef = theOther.theLocationRef;
         thePlace = theOther.thePlace;
         theStartDate = theOther.theStartDate;
         theEndDate = theOther.theEndDate;
         theURL = theOther.theURL;
         theLogger = theOther.theLogger;
     }
 
     /**
      * 
      * @param ps - the stream to where the data is written
      */
     public void outputAsKML(PrintStream ps) {
         ps.print("<event ");
         ps.print("start=\"");
         ps.print(theStartDate.toString());
         ps.print("\" ");
 
         if (theEndDate != null
                 && theEndDate.after(theStartDate)) {
             ps.print("end=\"");
             ps.print(theEndDate.toString());
             ps.print("\" ");
         }
 
         ps.print("title=\"");
         ps.print(theName);
         ps.print("\">");
         ps.println();
         ps.print("<description>");
         ps.println();
 
         if (theURL != null) {
             ps.print("&lt;p&gt;");
             ps.println();
             ps.print("&lt;a href=\"");
             ps.print(theURL);
             ps.print("\"&gt; more info&gt;&gt;&gt;");
             ps.print("&lt;/a&gt;");
             ps.println();
             ps.print("&lt;/p&gt;");
             ps.println();
         }
 
         ps.print("</description>");
         ps.println();
         ps.print("<Point>");
         ps.println();
         ps.print("<coordinates>");
         ps.print(theLongitude);
         ps.print(",");
         ps.print(theLatitude);
         ps.print("</coordinates>");
         ps.println();
         ps.print("</Point>");
         ps.println();
         ps.print("</event>");
         ps.println();
     }
 
     /**
      * 
      * @return - a unique id for the placemark
      */
     public String getId() {
         String strId = theName.replaceAll(" ", "");
         return strId;
     }
 
     /**
      * 
      * @return - a unique id for the placemark
      */
     public boolean complete() {
         HTMLPageParser theParser = new HTMLPageParser(theLogger);
         Document theDocument = theParser.getParsedPage(theURL);
 
         populateCoordsFromPage(theDocument);
         NodeList summaryFromPage = getSummaryFromPage(theDocument);
 
         if (summaryFromPage == null) {
             Node theFirstPara = getFirstParaFromPage(theDocument);
 
             if (theFirstPara != null) {
                 populateDateFromFirstPara(theFirstPara);
             }
         } else {
             populateDateFromSummaryData(summaryFromPage);
 
             if (!isPositionSet()) {
                 populateLocationFromSummaryData(summaryFromPage);
             }
         }
         
         // try to recover if data is only partially set
         if((isPeriodSet() || isPositionSet()) && !(isPeriodSet() && isPositionSet())){
             if(!isPeriodSet()){
                 theStartDate = thePeriodMap.getStartDate(getId());
                 theEndDate = thePeriodMap.getEndDate(getId());
             }
 
             if(!isPositionSet()){
                 theLatitude = thePositionMap.getLatitude(getId());
                 theLongitude = thePositionMap.getLongitude(getId());                  
             }
         }
 
         if (isComplete()) {
             return true;
         } else {
             theLogger.log(Level.WARNING, "Unable to complete {0}", getId());
             return false;                
         }
     }
 
     /*
      * Get the position from the coordinates field on the top left of the page
      *
      */
     private void populateCoordsFromPage(Document theDocument) {
         try {
             XPath latitudeXpath = XPathFactory.newInstance().newXPath();
             Node theLatitudeNode = (Node) latitudeXpath.evaluate("html/body//span[@class='latitude']", theDocument, XPathConstants.NODE);
 
             if (theLatitudeNode != null) {
                 theLatitude = theLatitudeNode.getTextContent();
             }
 
             XPath longitudeXpath = XPathFactory.newInstance().newXPath();
             Node theLongitudeNode = (Node) longitudeXpath.evaluate("html/body//span[@class='longitude']", theDocument, XPathConstants.NODE);
 
             if (theLongitudeNode != null) {
                 theLongitude = theLongitudeNode.getTextContent();
             }
         } catch (XPathExpressionException ex) {
             theLogger.log(Level.SEVERE, null, ex);
         }
     }
 
     /*
      * Get the summary data from the page
      * todo go straight to the summary and then go to parent
      *
      */
     private NodeList getSummaryFromPage(Document theDocument) {
         NodeList retVal = null;
 
         try {
             XPath infoboxTableXpath = XPathFactory.newInstance().newXPath();
             NodeList theData = (NodeList) infoboxTableXpath.evaluate("html//table[@class='infobox vevent']/tr", theDocument, XPathConstants.NODESET);
             int theLength = theData.getLength();
 
             for (int i = 0; i < theLength && retVal == null; ++i) {
                 XPath summaryXpath = XPathFactory.newInstance().newXPath();
                 NodeList theSummary = (NodeList) summaryXpath.evaluate("./th[@class='summary']", theData.item(i), XPathConstants.NODESET);
 
                 if (theSummary != null
                         && theSummary.getLength() > 0) {
                     String theText = theSummary.item(0).getTextContent();
                     retVal = theData;
                 }
 
             }
         } catch (XPathExpressionException ex) {
             theLogger.log(Level.SEVERE, null, ex);
         }
 
         return retVal;
     }
 
     /*
      * Get the summary data from the page
      * todo go straight to the summary and the go to parent
      *
      */
     private Node getFirstParaFromPage(Document theDocument) {
         Node retVal = null;
 
         try {
             XPath firstParaXpath = XPathFactory.newInstance().newXPath();
             NodeList theData = (NodeList) firstParaXpath.evaluate("html/body//div[@id='bodyContent']/p", theDocument, XPathConstants.NODESET);
             int listLength = theData.getLength();
 
             for(int i = 0; i < listLength && retVal == null; ++i) {
                 XPath coordsXpath = XPathFactory.newInstance().newXPath();
                 Node theCoords = (Node) coordsXpath.evaluate(".//span[@id='coordinates']", theData.item(i), XPathConstants.NODE);
                 
                 if(theCoords == null){
                     retVal = theData.item(i);
                 }
             }
         } catch (XPathExpressionException ex) {
             theLogger.log(Level.SEVERE, null, ex);
         }
 
         return retVal;
     }
 
     /*
      * Get the summary from the top left of the page
      *
      */
     private void populateDateFromFirstPara(Node theFirstPara) {
         String testString = theFirstPara.getTextContent();
         Date testDate = getOccurenceDate(testString);
 
         if (testDate != null) {
             theStartDate = testDate;
             theEndDate = testDate;
         }
     }
 
     /*
      * Get the summary from the top left of the page
      *
      */
     private void populateDateFromSummaryData(NodeList summaryData) {
         try {
             int theLength = summaryData.getLength();
             boolean dateFound = false;
 
             for (int i = 0; i < theLength && !dateFound; ++i) {
                 XPath rowXpath = XPathFactory.newInstance().newXPath();
                 NodeList theRows = (NodeList) rowXpath.evaluate("./td/table/tr", summaryData.item(i), XPathConstants.NODESET);
 
                 if (theRows != null) {
                     int theRowsLength = theRows.getLength();
 
                     for (int j = 0; j < theRowsLength; ++j) {
                         Node theRowNode = theRows.item(j);
 
                         XPath headerXpath = XPathFactory.newInstance().newXPath();
                         Node theHeaderNode = (Node) rowXpath.evaluate("./th", theRowNode, XPathConstants.NODE);
 
                         String theText = theHeaderNode.getTextContent();
 
                         if (theText != null
                                 && theText.equalsIgnoreCase("Date")) {
                             XPath detailXpath = XPathFactory.newInstance().newXPath();
                             Node theDetail = (Node) headerXpath.evaluate("./td", theRowNode, XPathConstants.NODE);
 
                             if (theDetail != null) {
                                 String detailText = theDetail.getTextContent();
 
                                 boolean periodDateSet = setPeriod(detailText);
 
                                 if (!periodDateSet) {
                                     Date theDate = getDate(detailText);
 
                                     if (theDate != null) {
                                         theStartDate = theDate;
                                         theEndDate = theDate;
                                     }
                                 }
                             }
 
                             dateFound = true;
                         }
                     }
                 }
             }
         } catch (XPathExpressionException ex) {
             theLogger.log(Level.SEVERE, null, ex);
         }
     }
 
     /*
      * populate the position from the summary data
      * either use the summary ref or the summary place
      *
      */
     private void populateLocationFromSummaryData(NodeList summaryData) {
         int theLength = summaryData.getLength();
         boolean locationFound = false;
 
         try {
             for (int i = 0; i < theLength && !locationFound; ++i) {
                 XPath rowXpath = XPathFactory.newInstance().newXPath();
                 NodeList theRows = (NodeList) rowXpath.evaluate("./td/table/tr", summaryData.item(i), XPathConstants.NODESET);
 
                 if (theRows != null) {
                     int theRowsLength = theRows.getLength();
 
                     for (int j = 0; j < theRowsLength; ++j) {
                         Node theRowNode = theRows.item(j);
 
                         XPath headerXpath = XPathFactory.newInstance().newXPath();
                         Node theHeaderNode = (Node) rowXpath.evaluate("./th", theRowNode, XPathConstants.NODE);
 
                         String theText = theHeaderNode.getTextContent();
 
                         if (theText != null
                                 && theText.equalsIgnoreCase("Location")) {
                             XPath detailXpath = XPathFactory.newInstance().newXPath();
                             Node theDetail = (Node) headerXpath.evaluate("./td", theRowNode, XPathConstants.NODE);
 
                             if (theDetail != null) {
                                 XPath anchorsXpath = XPathFactory.newInstance().newXPath();
                                 NodeList theAnchors = (NodeList) headerXpath.evaluate("./span/a", theDetail, XPathConstants.NODESET);
 
                                 if (theAnchors != null) {
                                     int theAnchorsLength = theAnchors.getLength();
 
                                     if (theAnchorsLength > 0) {
                                         Element thePlaceElement = (Element) theAnchors.item(0);
                                         String thePlaceHREF = thePlaceElement.getAttribute("href");
                                         if (thePlaceHREF.indexOf("http://") != 0) {
                                             thePlaceHREF = theBaseURL + thePlaceHREF;
                                         }
 
                                         try {
                                             theLocationRef = new URL(thePlaceHREF);
                                             populateLatLong();
                                         } catch (MalformedURLException ex) {
                                             theLogger.log(Level.SEVERE, "Unable to format place URL", ex);
                                         }
                                     }
                                 }
                             }
 
                             locationFound = true;
                         }
                     }
                 }
             }
         } catch (XPathExpressionException ex) {
             theLogger.log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Try and get the latitude and longitude from either the place url
      * @return
      */
     private boolean populateLatLong() {
         HTMLPageParser theParser = new HTMLPageParser(theLogger);
         Document document = theParser.getParsedPage(theLocationRef);
 
         try {
             XPath lonXpath = XPathFactory.newInstance().newXPath();
             NodeList lonNodeList = (NodeList) lonXpath.evaluate("html//span[@class='longitude']", document, XPathConstants.NODESET);
             int lonLength = lonNodeList.getLength();
             if (lonLength > 0) {
                 Element element = (Element) lonNodeList.item(0);
                 theLongitude = element.getTextContent();
             }
 
             XPath latXpath = XPathFactory.newInstance().newXPath();
             NodeList latNodeList = (NodeList) latXpath.evaluate("html//span[@class='latitude']", document, XPathConstants.NODESET);
             int latLength = latNodeList.getLength();
             if (latLength > 0) {
                 Element element = (Element) latNodeList.item(0);
                 theLatitude = element.getTextContent();
             }
         } catch (XPathExpressionException theException) {
             theLogger.log(Level.SEVERE, "Placemark populateLatLongFromURL:" + getId(), theException);
         }
 
         if (theLatitude == null
                 || theLongitude == null) {
             return false;
         } else {
             return true;
         }
     }
 
     private boolean isPositionSet() {
         boolean latValid = (theLatitude != null && !theLatitude.isEmpty());
         boolean lonValid = (theLongitude != null && !theLongitude.isEmpty());
 
         boolean retVal = latValid && lonValid;
 
         return retVal;
     }
 
     private boolean isPeriodSet() {
         boolean startDateValid = (theStartDate != null);
         boolean endDateValid = (theEndDate != null);
 
         boolean retVal = startDateValid && endDateValid;
 
         return retVal;
     }
 
     private boolean isComplete() {
         boolean retVal = isPositionSet() && isPeriodSet();
 
         return retVal;
     }
 
     private boolean setPeriod(String dateString) {
         Pattern thePattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d\\d\\d");
         Matcher theMatcher = thePattern.matcher(dateString);
         boolean retVal = theMatcher.matches();
 
         if (retVal) {
             Scanner s = new Scanner(dateString).useDelimiter("-");
             Integer theStartYear = new Integer(s.nextInt());
             Integer theEndYear = new Integer(s.nextInt());
 
             DateFormat theYearFormat = new SimpleDateFormat("yyyy");
 
             try {
                 theStartDate = theYearFormat.parse(theStartYear.toString());
                 theEndDate = theYearFormat.parse(theEndYear.toString());
             } catch (ParseException ex) {
                 Logger.getLogger(RefThree.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         return retVal;
     }
 
     // todo - formatters list should be set up once (if needed) only
     // deal with confusion between dd/mm/yyyy and mm/dd/yyyy
     private Date getDate(String dateString) {
         Date retVal = null;
         List<DateFormat> theFormatters = new ArrayList<DateFormat>();
         theFormatters.add(new SimpleDateFormat("dd MMMM yyyy"));
         theFormatters.add(new SimpleDateFormat("MMMM dd, yyyy"));
         theFormatters.add(new SimpleDateFormat("MMMM, yyyy"));
         theFormatters.add(new SimpleDateFormat("MMMM yyyy"));
         theFormatters.add(new SimpleDateFormat("yyyy"));
         theFormatters.add(new SimpleDateFormat("dd/MM/yyyy"));
         theFormatters.add(new SimpleDateFormat("MM/dd/yyyy"));
 
         for (int i = 0; i < theFormatters.size() && retVal == null; ++i) {
             try {
                 Date theDate = theFormatters.get(i).parse(dateString);
                 retVal = theDate;
             } catch (ParseException ex) {
                 // not really an exceptional case
             }
         }
 
         if (retVal == null) {
             theLogger.log(Level.WARNING, "Cannot get date");
         }
 
         return retVal;
     }
 
     // todo cut this into as few patterns as possible
     private Date getOccurenceDate(String paragraphText) {
         Date retVal = null;
         List<Pattern> theDatePatterns = new ArrayList<Pattern>();
         theDatePatterns.add(Pattern.compile(" \\d\\d January \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d February \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d March \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d April \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d May \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d June \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d July \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d August \\d\\d\\d\\d"));
        theDatePatterns.add(Pattern.compile(" \\d\\d September \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d October \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d November \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d\\d December \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d January \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d February \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d March \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d April \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d May \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d June \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d July \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d August \\d\\d\\d\\d"));
        theDatePatterns.add(Pattern.compile(" \\d September \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d October \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d November \\d\\d\\d\\d"));
         theDatePatterns.add(Pattern.compile(" \\d December \\d\\d\\d\\d"));
 
         for (int i = 0; i < theDatePatterns.size() && retVal == null; ++i) {
             Matcher theMatcher = theDatePatterns.get(i).matcher(paragraphText);
             boolean matchFound = theMatcher.find();
 
             if (matchFound) {
                 String matchingString = theMatcher.group();
                 retVal = getDate(matchingString);
             }
         }
 
         return retVal;
     }
 
     public int compareTo(Object anotherPlacemark) throws ClassCastException {
         if (!(anotherPlacemark instanceof RefThree)) {
             throw new ClassCastException("A Placemark object expected.");
         }
         String anotherPlacemarkName = ((RefThree) anotherPlacemark).getId();
         return this.getId().compareTo(anotherPlacemarkName);
     }
 }
