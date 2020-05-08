 package gov.usgs.cida.watersmart.parse;
 
 import com.ctc.wstx.stax.WstxInputFactory;
 import com.google.common.collect.Maps;
 import gov.usgs.cida.netcdf.dsg.Station;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import javax.xml.stream.XMLInputFactory;
 import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
 import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Build a list of stations for a model run / netcdf file Have a way to look up
  * the index of stations
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class StationLookup {
 
     private static final Logger LOG = LoggerFactory.getLogger(
             StationLookup.class);
     private final LinkedHashMap<String, Station> stationLookupTable = Maps.newLinkedHashMap();
 
     public StationLookup(RunMetadata meta) throws IOException, XMLStreamException {
         this(meta.getWfsUrl(), meta.getLayerName(), meta.getCommonAttribute());
     }
     
     public StationLookup(String wfsUrl, String typeName, String nameAttr) throws
             IOException, XMLStreamException {
         // has to be better way to do this call
         String url = wfsUrl + "?service=WFS&version=1.1.0&request=GetFeature&typeName=" + typeName + "&outputFormat=text/xml; subtype=gml/3.2";
 
         HttpClient client = new HttpClient();
         GetMethod get = new GetMethod();
         URI uri = new URI(url, false);
         get.setURI(uri);
         int statusCode = client.executeMethod(get);
         if (statusCode != HttpStatus.SC_OK) {
             LOG.error("Bad request to wfs: " + wfsUrl);
         }
         InputStream response = get.getResponseBodyAsStream();
 
         XMLInputFactory factory = WstxInputFactory.newFactory();
         XMLStreamReader reader = factory.createXMLStreamReader(response);
 
         float lat = Float.NaN;
         float lon = Float.NaN;
         String stationId = null;
 
         // TODO not doing namespacing for now, might be important at some point
         int index = 0;
         while (reader.hasNext()) {
             int code = reader.next();
             if (code == START_ELEMENT
                 && reader.getName().getLocalPart().equals("member")) {
                 lat = Float.NaN;
                 lon = Float.NaN;
                 stationId = null;
             }
             else if (code == END_ELEMENT
                      && reader.getName().getLocalPart().equals("member")
                      && lat != Float.NaN
                      && lon != Float.NaN
                      && stationId != null) {
                 stationLookupTable.put(stationId, new Station(lat, lon,
                                                               stationId, index));
                 index++;
             }
             else if (code == START_ELEMENT
                      && reader.getName().getLocalPart().equals("pos")) {
                 String pos = reader.getElementText();
                 String[] split = pos.split(" ");
                 lat = Float.parseFloat(split[0]);
                 lon = Float.parseFloat(split[1]);
             }
             else if (code == START_ELEMENT
                      && reader.getName().getLocalPart().equals(nameAttr)) {
                 stationId = reader.getElementText();
             }
         }
     }
 
     // Call to get the list of all stations, create 
     public Collection<Station> getStations() {
         return stationLookupTable.values();
     }
 
     /**
      * Looks up the station id by station name
      *
      * @param stationName name or number of station
      * @return index of station for referencing in NetCDF file, -1 if not found
      */
     public int lookup(String stationName) {
         Station station = this.get(stationName);
         if (null != station) {
             return station.index;
         }
         else {
            return -1;
         }
     }
 
     /**
      * Returns station object
      *
      * @param stationName station name or number
      * @return Station or null if not found
      */
     public Station get(String stationName) {
         Station station = stationLookupTable.get(stationName);
         if (null != station) {
             return station;
         }
         // sometimes leading zero may be dropped
         station = stationLookupTable.get("0" + stationName);
         return station;
     }
 }
