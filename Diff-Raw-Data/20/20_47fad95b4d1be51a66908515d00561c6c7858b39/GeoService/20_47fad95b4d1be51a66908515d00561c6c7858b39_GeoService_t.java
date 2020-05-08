 package ru.exorg.processing;
 
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
import org.springframework.beans.factory.InitializingBean;
 import ru.exorg.core.model.Location;
 
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

 final public class GeoService implements InitializingBean {
     final private static int    httpPort = 80;
     final private static String GAPI_PROTO = "http://";
     final private static String GAPI_SERVER = "maps.googleapis.com";
     final private static String GAPI_PATH = "/maps/api/geocode/json?";
     final private static String GAPI_Q_FOOTER = "&sensor=true";
 
 
     final private static String YAPI_PROTO = "http://";
    final private static String YAPI_SERVER = "geocode-maps.yandex.ru";
   final private static String YAPI_PATH = "/1.x/?";
     final private static String YAPI_Q_FOOTER = "&key=ANvK1k0BAAAAdOSwLAIAN1YUetQwuIkOfKscfc9OprS8ed8AAAAAAAAAAABpeUFMBr_TjUPQr01cqWYXU6dxUA==&format=json";
 
     final public static int API_GOOGLE = 1;
     final public static int API_YANDEX = 2;
 
     private int timeout = 1000;
 
     private String proxyHost;
     private int proxyPort;
 
     private GoogleApi googleApi;
     private YandexApi yandexApi;
 
     private static JSONObject get(final JSONObject jobj, final String field) {
         return (JSONObject)jobj.get(field);
     }
 
     private interface GeoApi {
         public List<Location> queryLocation(final String address, final String guess) throws Exception;
     }
 
     private class YandexApi implements GeoApi {
         private JSONObject query(final String q) throws Exception {
             String res = queryHttp(YAPI_SERVER, YAPI_PATH, String.format("geocode=%s", URLEncoder.encode(q, "UTF-8")), YAPI_Q_FOOTER);
 
             //System.out.println(res);
 
             return (JSONObject)JSONValue.parse(res);
         }
 
         private List<Location> parse(final JSONObject gi) {
             if (gi == null) {
                 return null;
             }
 
             JSONObject coll = get(get(gi, "response"), "GeoObjectCollection");
             JSONObject response = get(get(coll, "metaDataProperty"), "GeocoderResponseMetaData");
 
             if (!((String)response.get("found")).equals("0")) {
                 JSONArray geoObjects = (JSONArray)coll.get("featureMember");
                 List<Location> r = new ArrayList<Location>();
 
                 for (int i = 0; i < geoObjects.size(); ++i) {
                     JSONObject geoObject = get((JSONObject)geoObjects.get(i), "GeoObject");
 
                     //System.out.println(geoObject);
 
                     String address = (String)get(get(geoObject, "metaDataProperty"), "GeocoderMetaData").get("text");
                     String[] pos = ((String)get(geoObject, "Point").get("pos")).split(" ");
 
                     double lat = Double.parseDouble(pos[1]);
                     double lng = Double.parseDouble(pos[0]);
 
                     r.add(new Location(0, address, lat, lng));
                 }
                 return r;
             } else {
                 return null;
             }
         }
 
         public List<Location> queryLocation(final String address, final String guess) throws Exception {
             JSONObject r;
 
             if (address != null) {
                 r = this.query(address);
             } else if (guess != null) {
                 r = this.query(guess);
             } else {
                 return null;
             }
 
             List<Location> locs = this.parse(r);
             if (locs == null && guess != null) {
                 r = this.query(guess);
                 locs = this.parse(r);
             }
 
             return locs;
         }
     }
 
     private class GoogleApi implements GeoApi {
         private JSONObject query(final String q) throws Exception {
             String res = queryHttp(GAPI_SERVER, GAPI_PATH, String.format("address=%s", URLEncoder.encode(q, "UTF-8")), GAPI_Q_FOOTER);
             return (JSONObject)JSONValue.parse(res);
         }
 
         private List<Location> parse(final JSONObject gi) {
             if (gi == null || gi.get("status") == null) {
                 return null;
             }
 
             if (((String)gi.get("status")).equals("OK")) {
                 List<Location> r = new ArrayList<Location>();
                 JSONArray results = (JSONArray)gi.get("results");
 
                 for (int i = 0; i < results.size(); ++i) {
                     JSONObject first_res = (JSONObject)results.get(i);
                     String address = (String)first_res.get("formatted_address");
                     JSONObject loc = (JSONObject)((JSONObject)first_res.get("geometry")).get("location");
                     double lat = ((Double)loc.get("lat")).doubleValue();
                     double lng = ((Double)loc.get("lng")).doubleValue();
 
                     r.add(new Location(0, address, lat, lng));
                 }
                 return r;
             } else {
                 return null;
             }
         }
 
         public List<Location> queryLocation(final String address, final String guess) throws Exception {
             JSONObject r;
 
             if (address != null) {
                 r = this.query(address);
             } else if (guess != null) {
                 r = this.query(guess);
             } else {
                 return null;
             }
 
             List<Location> locs = parse(r);
             if (locs == null && guess != null) {
                 r = this.query(guess);
                 locs = parse(r);
             }
 
             return locs;
         }
     }
 
     private String queryHttp(final String server, final String servicePath, final String query, final String footer) throws Exception {
         HttpConnection conn = null;
         String hostName = server;
         String path = servicePath + query + footer;
 
         //System.out.println(path);
 
         HttpState state = new HttpState();
 
         if (doesUseProxy()) {
             conn = new HttpConnection(proxyHost, proxyPort, hostName, httpPort);
         } else {
             conn = new HttpConnection(hostName, httpPort);
         }
         conn.open();
 
         HttpMethod getMeth = new GetMethod();
         getMeth.setPath(path);
         //getMeth.addRequestHeader("Host", "maps.googleapis.com");
         getMeth.addRequestHeader("Accept", "application/json");
         getMeth.addRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US)");
         //getMeth.addRequestHeader("Accept-Language", "ru");
         getMeth.addRequestHeader("Accept-Language", "ru;en");
         getMeth.addRequestHeader("Accept-Charset", "utf-8");
 
         if (!doesUseProxy()) {
             getMeth.addRequestHeader("Connection", "close");
         } else {
             getMeth.addRequestHeader("Proxy-Connection", "keep-alive");
         }
 
         try {
             getMeth.execute(state, conn);
         } catch (Exception e) {
             throw e;
         }
 
         return new String(getMeth.getResponseBody());
     }
 
 
     public void setProxy(final String proxy) {
         String[] proxyConf = proxy.split(":");
         this.proxyHost = proxyConf[0];
         this.proxyPort = Integer.parseInt(proxyConf[1]);
     }
 
     private boolean doesUseProxy() {
         return this.proxyHost != null;
     }
 
     public List<Location> lookupLocation(final Location loc, final String guess) throws Exception {
         List<Location> r;
         String address = loc.getAddress();
 
         r = this.googleApi.queryLocation(address, guess);
         if (r == null) {
             r = this.yandexApi.queryLocation(address, guess);
         }
 
         return r;
     }
 
     public List<Location> lookupLocation(final Location loc, final String guess, int api) throws Exception {
         String address = loc.getAddress();
 
         switch (api) {
             case API_GOOGLE:
                 return this.googleApi.queryLocation(address, guess);
 
             case API_YANDEX:
                 return this.yandexApi.queryLocation(address, guess);
 
             default:
                 return null;
         }
     }
 
     public void afterPropertiesSet() {
         this.googleApi = new GoogleApi();
         this.yandexApi = new YandexApi();
     }
 }
