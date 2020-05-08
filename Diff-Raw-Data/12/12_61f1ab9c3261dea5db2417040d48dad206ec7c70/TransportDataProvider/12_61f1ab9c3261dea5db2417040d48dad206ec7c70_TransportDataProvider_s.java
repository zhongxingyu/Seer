 package uk.ac.cam.cl.dtg.android.time.data;
 
 /**
  * Provides functionality for querying the data feeds for Cambridgeshire's bus system
  * 
  * @author dt316, drt24
  * 
  */
 
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import uk.ac.cam.cl.dtg.android.time.buses.BusArrivalData;
 import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
 import uk.ac.cam.cl.dtg.android.time.buses.StopGroup;
 import uk.ac.cam.cl.dtg.android.time.data.handlers.ArrivalsSAXHandler;
 import uk.ac.cam.cl.dtg.android.time.data.handlers.GetStopsSAXHandler;
 import uk.ac.cam.cl.dtg.android.time.data.handlers.SAXDataHandler;
 import uk.ac.cam.cl.dtg.android.time.data.handlers.StopGroupsSAXHandler;
 import uk.ac.cam.cl.dtg.android.time.data.handlers.StopSAXHandler;
 import uk.ac.cam.cl.dtg.android.time.data.handlers.StopsSAXHandler;
 
 public class TransportDataProvider {
 
   private String feedURL;
   private String apiKey;
 
   public TransportDataProvider(String apiKey, String feedURL) {
 
     this.feedURL = feedURL;
     this.apiKey = apiKey;
   }
 
   /**
    * Returns the list of BusStops visible at a certain zoom level.
    * 
    * @param level Zoom level, 1, 2 or 3. 1 is farthest, 3 is nearest.
    * @return
    * @throws TransportDataException
    */
   public List<BusStop> getBusStops(int level) throws TransportDataException {
     return makeRequest("GetStops", "&level=" + level, new GetStopsSAXHandler());
   }
 
   public List<StopGroup> getStopGroupsNear(double latitude, double longitude)
       throws TransportDataException {
     return makeRequest("FindStopGroups",
         "&method=near" + "&lat=" + latitude + "&long=" + longitude, new StopGroupsSAXHandler());
   }
 
   public List<StopGroup> getStopGroupsWithin(double left, double top, double right, double bottom)
       throws TransportDataException {
     return makeRequest("FindStopGroups", "&method=within" + "&left=" + left + "&top=" + top
         + "&right=" + right + "&bottom=" + bottom, new StopGroupsSAXHandler());
   }
 
   public List<BusStop> getBusStopsInGroup(String group) throws TransportDataException {
     return makeRequest("ListStopPoints", "&groupref=" + group, new StopsSAXHandler());
   }
 
   /**
    * Gets the next n arrivals at a certain bus stop.
    * 
    * @param Stop The stop in question.
    * @param NumberToFetch Maximum number of arrivals to fetch. First 3 are of the format 'x
    *          minutes', after that timetabled information is given.
    * @return
    * @throws TransportDataException
    */
   public BusArrivalData getBusArrivalData(String stopRef, int numberToFetch)
       throws TransportDataException {
     return makeRequest("GetArrivals", "&atco=" + stopRef + "&numarrivals=" + numberToFetch,
         new ArrivalsSAXHandler());
   }
 
   /**
    * Gets the next n arrivals at a certain bus stop.
    * 
    * @param smsCode SMS code of the stop to lookup
    * @return Requested bus stop
    * @throws TransportDataException
    * 
    * @see {@link #getStopByNaptan(String)}
    */
   @Deprecated
   public BusStop getStopBySMS(String smsCode) throws TransportDataException {
     return getStopByNaptan(smsCode);
 
   }
 
   public BusStop getStopByNaptan(String naptanCode) throws TransportDataException {
     return getStop(naptanCode, false);
   }
 
   public BusStop getStopByAtco(String atcoCode) throws TransportDataException {
     return getStop(atcoCode, true);
   }
 
   private BusStop getStop(String code, boolean atco) throws TransportDataException {
     return makeRequest("LookupStop", ((atco) ? "&atco=" : "&naptan=") + code, new StopSAXHandler());
   }
 
   private <T> T makeRequest(String service, String query, SAXDataHandler<T> handler)
       throws TransportDataException {
     try {
 
       /* Create a URL we want to load some xml-data from. */
       URL url = new URL(feedURL + service + "?key=" + apiKey + query);
 
       /* Get a SAXParser from the SAXPArserFactory. */
       SAXParserFactory spf = SAXParserFactory.newInstance();
       SAXParser sp = spf.newSAXParser();
 
       /* Get the XMLReader of the SAXParser we created. */
       XMLReader xr = sp.getXMLReader();
 
       /* Create a new ContentHandler and apply it to the XML-Reader */
       xr.setContentHandler(handler);
 
       /* Parse the xml-data from our URL. */
       xr.parse(new InputSource(url.openStream()));
 
       /* Parsing has finished. */
       return handler.getData();
 
     } catch (Exception e) {
 
      throw new TransportDataException(e);
 
     }
   }
 }
