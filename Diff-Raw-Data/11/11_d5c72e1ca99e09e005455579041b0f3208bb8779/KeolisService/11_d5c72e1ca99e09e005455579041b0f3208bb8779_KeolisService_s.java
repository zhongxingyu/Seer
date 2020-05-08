 package fr.itinerennes.business.http.keolis;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.impl.ItinerennesLoggerFactory;
 
 import fr.itinerennes.ErrorCodeConstants;
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.business.http.GenericHttpService;
 import fr.itinerennes.exceptions.GenericException;
 import fr.itinerennes.model.BikeStation;
 import fr.itinerennes.model.LineIcon;
 import fr.itinerennes.model.SubwayStation;
 import fr.itinerennes.utils.FileUtils;
 
 /**
  * Manage calls to the Keolis API.
  * 
  * @author Jérémie Huchet
  */
 public class KeolisService {
 
     /** The event logger. */
     private static final Logger LOGGER = ItinerennesLoggerFactory.getLogger(KeolisService.class);
 
     /** The HTTP client. */
     private final GenericHttpService httpService = new GenericHttpService();
 
     /** The handler to receive bike stations. */
     private final BikeStationHttpResponseHandler bikeStationHandler = new BikeStationHttpResponseHandler();
 
     /** The handler to receive subway stations. */
     private final SubwayStationHttpResponseHandler subwayStationHandler = new SubwayStationHttpResponseHandler();
 
     /** The handler to receive line icons. */
     private final LineIconsHttpResponseHandler iconsHandler = new LineIconsHttpResponseHandler();
 
     /**
      * Creates a generic request to the Keolis API. This method will set the request headers, the
      * version and the API key needed to send a request to Keolis.
      * 
      * @param parameters
      *            the request parameters
      * @return an {@link HttpPost} to send to execute the request
      * @throws GenericException
      *             unable to encode request parameters
      */
     private HttpPost createKeolisRequest(final List<NameValuePair> parameters)
             throws GenericException {
 
         final HttpPost req = new HttpPost(ItineRennesConstants.KEOLIS_API_URL);
         req.addHeader("Accept", "text/json");
         req.addHeader("Accept", "application/json");
         parameters.add(new BasicNameValuePair(Keo.API_VERSION,
                 ItineRennesConstants.KEOLIS_API_VERSION));
         parameters.add(new BasicNameValuePair(Keo.API_KEY, ItineRennesConstants.KEOLIS_API_KEY));
 
         try {
             req.setEntity(new UrlEncodedFormEntity(parameters));
         } catch (final UnsupportedEncodingException e) {
             throw new GenericException(ErrorCodeConstants.API_CALL_FAILED,
                     "unable to encode request parameters", e);
         }
 
         if (LOGGER.isDebugEnabled()) {
             final StringBuilder msg = new StringBuilder();
             msg.append(req.getURI().toString()).append("?");
             for (final NameValuePair param : parameters) {
                 msg.append(param.getName()).append("=").append(param.getValue()).append("&");
             }
             msg.deleteCharAt(msg.length() - 1);
             LOGGER.debug(msg.toString());
         }
 
         return req;
     }
 
     /**
      * Makes a call to the Keolis API to get all bike stations.
      * 
      * @return a {@link JSONArray} containing all bike stations as {@link JSONObject}s
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final List<BikeStation> getAllBikeStations() throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(5);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_LE_VELO_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_BIKE_STATIONS));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_STATION,
                 Keo.GetBikeStations.VALUE_STATION_ALL));
 
         final List<BikeStation> data = httpService.execute(createKeolisRequest(params),
                 bikeStationHandler);
 
         return data;
     }
 
     /**
      * Makes a call to the Keolis API to get the 3 first nearest bike stations.
      * 
      * @param latitude
      *            the latitude
      * @param longitude
      *            the longitude
      * @return a {@link JSONArray} containing the 3 bike stations as {@link JSONObject}s
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final List<BikeStation> getBikeStationsNearFrom(final int latitude, final int longitude)
             throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(8);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_LE_VELO_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_BIKE_STATIONS));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_STATION,
                 Keo.GetBikeStations.VALUE_STATION_PROXIMITY));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_MODE,
                 Keo.GetBikeStations.VALUE_MODE_COORDINATES));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_LATITUDE, String
                 .valueOf(latitude)));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_LONGITUDE, String
                 .valueOf(longitude)));
 
         final List<BikeStation> data = httpService.execute(createKeolisRequest(params),
                 bikeStationHandler);
 
         return data;
     }
 
     /**
      * Makes a call to the Keolis API to get the bike station related to the given identifier.
      * 
      * @param id
      *            the identifier of the bike station
      * @return a bike station
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final BikeStation getBikeStation(final String id) throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(6);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_LE_VELO_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_BIKE_STATIONS));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_STATION,
                 Keo.GetBikeStations.VALUE_STATION_IDENTIFIER));
         params.add(new BasicNameValuePair(Keo.GetBikeStations.PARAM_VALUE, id));
 
         final List<BikeStation> data = httpService.execute(createKeolisRequest(params),
                 bikeStationHandler);
 
         if (data != null && !data.isEmpty()) {
             return data.get(0);
         } else {
             return null;
         }
     }
 
     /**
      * Makes a call to the Keolis API to get the list of URLs to fetch the icons of the transport
      * lines.
      * 
      * @return a bean containing the base URL and the file names
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final List<LineIcon> getAllLineIcons() throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(4);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_LINES_ICONS));
         params.add(new BasicNameValuePair(Keo.GetLinesIcons.PARAM_MODE,
                 Keo.GetLinesIcons.VALUE_MODE_ALL));
 
         final List<LineIcon> data = httpService.execute(createKeolisRequest(params), iconsHandler);
 
         return data;
     }
 
     /**
      * Fetch an image icon for a transport line.
      * 
      * @param icon
      *            the icon to fetch
      * @return the bytes of the image
      */
     public final byte[] fetchIcon(final LineIcon icon) {
 
         InputStream iconStream = null;
         try {
             final URL url = new URL(icon.getIconUrl());
             iconStream = url.openStream();
             icon.setIconBytes(FileUtils.readBytes(iconStream));
         } catch (final MalformedURLException e) {
             LOGGER.error("Unable to fetch icon {} : {}", icon, e.getMessage());
         } catch (final IOException e) {
            LOGGER.error("Unable to fetch icon {} : {}", icon, e.getMessage());
         } finally {
             if (null != iconStream) {
                 try {
                     iconStream.close();
                 } catch (final IOException e) {
                     LOGGER.error("Stream already closed : {}", e.getMessage());
                 }
             }
         }
         return icon.getIconBytes();
     }
 
     /**
      * Makes a call to the Keolis API to get all subway stations.
      * 
      * @return a list of subway stations
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final List<SubwayStation> getAllSubwayStations() throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(5);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_METRO_STATIONS));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_MODE,
                 Keo.GetSubwayStations.VALUE_MODE_ALL));
 
         final List<SubwayStation> data = httpService.execute(createKeolisRequest(params),
                 subwayStationHandler);
 
         return data;
     }
 
     /**
      * Makes a call to the Keolis API to get the 3 first nearest subway stations.
      * 
      * @param latitude
      *            the latitude
      * @param longitude
      *            the longitude
      * @return a list containing the 3 subway stations
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final List<SubwayStation> getSubwayStationsNearFrom(final int latitude,
             final int longitude) throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(8);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_METRO_STATIONS));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_MODE,
                 Keo.GetSubwayStations.VALUE_MODE_PROXIMITY));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_PROXIMITY_TYPE,
                 Keo.GetSubwayStations.VALUE_PROXIMITY_TYPE_COORDINATES));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_LATITUDE, String
                 .valueOf(latitude)));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_LONGITUDE, String
                 .valueOf(longitude)));
 
         final List<SubwayStation> data = httpService.execute(createKeolisRequest(params),
                 subwayStationHandler);
 
         return data;
     }
 
     /**
      * Makes a call to the Keolis API to get the subway station related to the given identifier.
      * 
      * @param id
      *            the identifier of the subway station
      * @return a subway station
      * @throws GenericException
      *             unable to get a result from the server
      */
     public final SubwayStation getSubwayStation(final String id) throws GenericException {
 
         final List<NameValuePair> params = new ArrayList<NameValuePair>(6);
         params.add(new BasicNameValuePair(Keo.Network.PARAM_NAME, Keo.Network.VALUE_STAR));
         params.add(new BasicNameValuePair(Keo.Command.PARAM_NAME, Keo.Command.GET_METRO_STATIONS));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_MODE,
                 Keo.GetSubwayStations.VALUE_MODE_STATION));
         params.add(new BasicNameValuePair(Keo.GetSubwayStations.PARAM_STATION_IDENTIFIER, id));
 
         final List<SubwayStation> data = httpService.execute(createKeolisRequest(params),
                 subwayStationHandler);
 
         if (data != null && !data.isEmpty()) {
             return data.get(0);
         } else {
             return null;
         }
     }
 }
