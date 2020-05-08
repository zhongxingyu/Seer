 package org.geoserver.wms.responses.map.kml;
 
 import static org.custommonkey.xmlunit.XMLAssert.*;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.xml.namespace.QName;
 
 import org.geoserver.data.test.MockData;
 import org.geoserver.wms.WMSTestSupport;
 import org.geotools.util.Converters;
 import org.geotools.xs.bindings.XSDateTimeBinding;
 import org.vfny.geoserver.wms.responses.map.kml.OWS5MapProducerFactory;
 import org.w3c.dom.Document;
 
 public class ExtendedTextSymbolizerTest extends WMSTestSupport {
     public static String OWS5_PREFIX = "ows5";
 
     public static String OWS5_URI = "http://www.opengis.net/ows5";
 
     public static QName DATES = new QName(OWS5_URI, "Dates", OWS5_PREFIX);
 
     @Override
     protected void populateDataDirectory(MockData dataDirectory) throws Exception {
         super.populateDataDirectory(dataDirectory);
         dataDirectory.addStyle("bridgeDescription", ExtendedDataTest.class
                 .getResource("BridgeDescription.sld"));
         dataDirectory.addStyle("bridgeDescriptionHTML", ExtendedDataTest.class
                 .getResource("BridgeDescriptionHTML.sld"));
         dataDirectory.addStyle("KmlTimestamp", ExtendedDataTest.class
                 .getResource("KmlTimestamp.sld"));
         dataDirectory
                 .addStyle("KmlTimespan", ExtendedDataTest.class.getResource("KmlTimespan.sld"));
 
         dataDirectory.addPropertiesType(DATES, ExtendedTextSymbolizerTest.class
                .getResource("Dates.properties"), "KmlTimestamp", null);
     }
 
     protected String getDefaultLogConfiguration() {
         return "/DEFAULT_LOGGING.properties";
     }
 
     public void testDefaultOutput() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + layerId(MockData.BRIDGES)
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&styles=bridgeDescription");
 
         print(doc);
 
         // we should have just one feature
         assertXpathEvaluatesTo("1", "count(//Placemark)", doc);
         // see if the description in sld has been used
         assertXpathEvaluatesTo("The bridge name is: Cam Bridge", "//description", doc);
         assertXpathEvaluatesTo("This is bridge 110", "//Snippet", doc);
     }
 
     public void testFreemarkerOutput() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + layerId(MockData.BRIDGES)
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&styles=bridgeDescriptionHTML");
 
         print(doc);
 
         // we should have just one feature
         assertXpathEvaluatesTo("1", "count(//Placemark)", doc);
         // see if the description in sld has been used
         String expected = "<html><body>\n" + //
                 "This is bridge <b>110</b> whose name is <i>Cam Bridge</i>\n" + "</body></html>";
         assertXpathEvaluatesTo(expected, "//Snippet", doc);
     }
 
     public void testTimestamp() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + layerId(DATES)
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&styles=KmlTimestamp");
         print(doc);
         // check we have timestamps, no timespans
         assertXpathEvaluatesTo("3", "count(//TimeStamp/when)", doc);
         assertXpathEvaluatesTo("0", "count(//TimeSpan)", doc);
 
         // check the date has been encoded into an iso date
         String isoDate = convertToISODate("2007-12-18");
         assertXpathEvaluatesTo(isoDate, "/kml/Document/Placemark[@id='Dates.3']/TimeStamp/when",
                 doc);
     }
 
     public void testTimeSpan() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + layerId(DATES)
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&styles=KmlTimespan");
         print(doc);
         // check we have timespans, no timestamps
         assertXpathEvaluatesTo("0", "count(//TimeStamp)", doc);
         assertXpathEvaluatesTo("3", "count(//TimeSpan)", doc);
         // we should have one timestamp with both childs and two having just one
         // child
         assertXpathEvaluatesTo("2", "count(//TimeSpan/begin)", doc);
         assertXpathEvaluatesTo("2", "count(//TimeSpan/end)", doc);
 
         String beginDate = convertToISODate("2002-12-02");
         String endDate = convertToISODate("2003-12-01");
         assertXpathEvaluatesTo(beginDate, "/kml/Document/Placemark[@id='Dates.1']/TimeSpan/begin",
                 doc);
         assertXpathEvaluatesTo(endDate, "/kml/Document/Placemark[@id='Dates.1']/TimeSpan/end", doc);
     }
 
     public void testFreemarkerTemplate() throws Exception {
 
     }
 
     private String convertToISODate(String date) {
         Date dateValue = (Date) Converters.convert(date, Date.class);
         Calendar c = Calendar.getInstance();
         c.setTime(dateValue);
         String isoDate = new XSDateTimeBinding().encode(c, null);
         return isoDate;
     }
 }
