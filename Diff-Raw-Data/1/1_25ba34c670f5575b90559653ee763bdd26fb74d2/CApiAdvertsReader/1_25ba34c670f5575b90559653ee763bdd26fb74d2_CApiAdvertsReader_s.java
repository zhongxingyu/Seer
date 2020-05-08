 package com.gumtree.api.service.impl;
 
 import com.gumtree.api.entity.Advert;
 import com.gumtree.api.parser.xml.advert.CAPIAdvertsXMLParserException;
 import com.gumtree.api.parser.xml.advert.CApiAdvertsXMLParser;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: markkelly
  * Date: 31/08/2011
  * Time: 15:12
  * To change this template use File | Settings | File Templates.
  */
 @Service
 public class CApiAdvertsReader extends CApiReaderImpl {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(CApiAdvertsReader.class);
 
     @Value("${gumtree.capi.minutes_time_offset:0}")
     private Integer minutesTimeOffset;
 
     @Value("${gumtree.capi.hours_time_offset:0}")
     private Integer hoursTimeOffset;
 
     @Autowired
     private String path;
 
     @Autowired
     private String afterQuery;
 
     @Autowired
     private String statusQuery;
 
     @Autowired
     private String dateFormatQuery;
 
     private CApiAdvertsXMLParser xmlParser;
 
     DateTime currentDateTime;
 
     public List<Advert> getMostRecentAdverts() {
 
         List<Advert> ads = new LinkedList<Advert>();
 
         URI mostRecentURI = null;
 
         try {
             mostRecentURI = constructCApiMostRecentActiveAdvertsQueryURL();
 
             String content = getResponse(mostRecentURI);
 
             ads = convertToAdverts( getXMLDataFromContent(content) );
         } catch (CApiReaderConnectionException e) {
             LOGGER.error("Cannot Connect to CAPI");
         }
 
         return ads;
     }
 
     //Construct the URI to retrieve specific data
     private URI constructCApiMostRecentActiveAdvertsQueryURL() {
 
         URI mostRecentURI = null;
 
         //Create the Query where we want ACTIVE ads X minutes and Y Hours ago
         currentDateTime = new DateTime();
 
         if(minutesTimeOffset != null && minutesTimeOffset != 0)
             currentDateTime = currentDateTime.minusMinutes(minutesTimeOffset);
 
         if(hoursTimeOffset != null && hoursTimeOffset != 0)
             currentDateTime = currentDateTime.minusHours(hoursTimeOffset);
 
         //CAPI does not support the timezone offset e.g. ".123+01:00" at the end
         LOGGER.info("Getting ads from CAPI above " + currentDateTime.toString(dateFormatQuery));
 
         try {
             mostRecentURI = new URI("http://" +
                                       host  +
                                       path  + "?" +
                                       afterQuery +
                                       currentDateTime.toString(dateFormatQuery) + "&" +
                                       statusQuery + "ACTIVE");
         } catch (URISyntaxException e) {
             LOGGER.error("Cannot Construct the URI due to URI Exception");
         }
 
         return mostRecentURI;
     }
 
     //Parse the retrieved XML content
     private List<Map<String, Object>> getXMLDataFromContent(String content) {
         //Parse the retrieved XML content
         xmlParser = new CApiAdvertsXMLParser(content);
         List<Map<String, Object>> XMLAds = new LinkedList<Map<String, Object>>();
 
         try {
             XMLAds = xmlParser.Parse();
         }catch (CAPIAdvertsXMLParserException e) {
             LOGGER.error("Exception Thrown Parsing the XML contents");
         }
 
         return XMLAds;
     }
 
     //Convert to Advert Object
     private List<Advert> convertToAdverts( List<Map<String, Object>> XMLAds) {
 
         List<Advert> ads = new LinkedList<Advert>();
 
         //Create each advert for parsed content
         for(int i = 0; i < XMLAds.size(); i++) {
 
             try {
                 ads.add(new Advert(XMLAds.get(i)));
             }
             catch(NumberFormatException nfe) {
                 LOGGER.warn("ID is in wrong format in XML");
             }
             catch(IllegalArgumentException iae) {
                 LOGGER.warn("Not a valid currency in XML");
             }
             catch(ClassCastException cce) {
                 LOGGER.warn("cannot cast for a number value in XML");
             }
 
             if(ads.get(i).canDisplay())
                 System.out.println(ads.get(i));
         }
 
         return ads;
     }
 }
