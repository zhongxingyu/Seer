 package eo.processing;
 
 import java.util.*;
 import java.util.regex.*;
 import java.net.*;
 import java.io.*;
 
 import java.util.zip.GZIPInputStream;
 
 import org.springframework.beans.factory.InitializingBean;
 
 import org.json.simple.*;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.methods.*;
 
 import eo.common.POI;
 
 // ================================================================================
 
 public class Main implements InitializingBean {
     private POI poi_;
     private String proxy_host_ = "";
     private int proxy_port_ = 0;
     
     private int    http_port = 80;
     private String GAPI_PROTO = "http://";
     private String GAPI_SERVER = "maps.googleapis.com";
     private String GAPI_PATH = "/maps/api/geocode/json?";
     private String GAPI_Q_FOOTER = "&sensor=true";
 
 
     private JSONObject queryGAPI(final Map<String, String> query) throws Exception {
         StringBuilder qs = new StringBuilder();
         for (Map.Entry<String, String> e : query.entrySet()) {
             qs.append(e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8"));
         }
         String host_name = GAPI_SERVER;
         String path = GAPI_PATH + qs.toString() + GAPI_Q_FOOTER;
 
         HttpState state = new HttpState();
         HttpConnection conn;
 
         if (proxy_host_.length() == 0) {
             conn = new HttpConnection(host_name, http_port);
         } else {
             conn = new HttpConnection(proxy_host_, proxy_port_, host_name, http_port);
         }
         HttpMethod get_meth = new GetMethod();
         conn.open();
         get_meth.setPath(path);
         get_meth.addRequestHeader("Host", "maps.googleapis.com");
         get_meth.addRequestHeader("Connection", "close");
        /*get_meth.addRequestHeader("Cache-Control", "max-age=0");*/
         get_meth.addRequestHeader("Accept", "application/json");
         get_meth.addRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US)");
         get_meth.addRequestHeader("Accept-Language", "ru"); 
         get_meth.addRequestHeader("Accept-Charset", "utf-8");
         get_meth.execute(state, conn);
         
         return (JSONObject)JSONValue.parse(new String(get_meth.getResponseBody()));
     }
 
     public Main(POI poi, final String proxy) {
         poi_ = poi;
 
         if (proxy.length() > 0) {
             String[] proxy_conf = proxy.split(":");
             proxy_host_ = proxy_conf[0];
             proxy_port_ = Integer.parseInt(proxy_conf[1]);
         }
     }
 
 
     private void addRawGeo(POI.Entry e) throws Exception {
         if (!e.hasRawGeo()) {
             Map<String, String> p = new TreeMap<String, String>();
             
             if (e.hasAddress()) {            
                 p.put("address", e.getAddress());
             } else {
                 p.put("address", e.getName());
             }
 
             JSONObject r = queryGAPI(p);
 
             if (((String)r.get("status")).equals("OK")) {
                 JSONArray results = (JSONArray)r.get("results");
 
                 for (int i = 0; i < results.size(); ++i) {
                     JSONObject first_res = (JSONObject)results.get(i);
                     String address = (String)first_res.get("formatted_address");
                     JSONObject loc = (JSONObject)((JSONObject)first_res.get("geometry")).get("location");
                     double lat = ((Double)loc.get("lat")).doubleValue();
                     double lng = ((Double)loc.get("lng")).doubleValue();
 
                     if (poi_.isWithinCity(e.getCityId(), new POI.Loc(lat, lng))) {
                         e.addRawGeoInfo(address, lat, lng);
                     }
                 } 
                 
                 Thread.sleep(1500);
             }
         }
     }
 
     private void guessType(POI.Entry e) {
         if (!e.hasType()) {
             e.guessType();
         }
     }
 
     public void afterPropertiesSet() {
         try {
             Iterator it = poi_.poiIterator();
             while (it.hasNext()) {
                 POI.Entry e = (POI.Entry)it.next();
 
                 addRawGeo(e);
                 guessType(e);
             }
         } catch (Exception e) {
             System.out.println(e.toString());
             e.printStackTrace();
         }
     }
 }
