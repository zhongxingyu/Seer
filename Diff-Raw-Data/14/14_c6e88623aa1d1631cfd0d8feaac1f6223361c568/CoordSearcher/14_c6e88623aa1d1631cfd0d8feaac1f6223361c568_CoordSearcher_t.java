 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lp.reminiscens.crawler;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Locale;
 import lp.reminiscens.crawler.entities.City;
 import lp.reminiscens.crawler.entities.Event;
 import lp.reminiscens.crawler.entities.Media;
 import org.apache.commons.io.IOUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author Nicola.Parrello
  */
 public class CoordSearcher {
 
     String geocodingString;
     String address;
     Database db; // used once to add cities to reminiscens.City
     Collection<String> province; // used once to add cities to reminiscens.City
 
     public CoordSearcher() {
 
         geocodingString = "http://maps.googleapis.com/maps/api/geocode/json?address=";
         province = new <String> ArrayList(); // used once to add cities to reminiscens.City
 
     }
 
     // "main" function used once to add cities to reminiscens.City
     public static void main(String[] args) throws MalformedURLException, UnsupportedEncodingException, IOException, JSONException {
 
        /*CoordSearcher coord = new CoordSearcher();
         coord.db = new Database();
         String out;
         String path = "C:\\Users\\Nicola\\Documents\\NetBeansProjects\\Reminiscens\\timeline\\data-collection\\crawler\\files\\comune_cap_pr_regione.csv";
         FileReader fr = new FileReader(path);
         BufferedReader reader = new BufferedReader(fr);
         String line;
         String address;
         City city = null;
         while (reader.ready()) {
             line = reader.readLine();
             coord.province.add(line);
         }
 
         Iterator i = coord.province.iterator();
 
         while (i.hasNext()) {
             address = (String) (i.next());
             System.out.println(address + ": ");
             city = coord.getCityFromLine(address);
             out = coord.getJSONByGoogle(address);
             coord.parseGeoJSON(out, city);
             coord.db.addCity(city);
         }
        **/
 
 
     }
 
     public String getJSONByGoogle(String address) throws MalformedURLException, UnsupportedEncodingException, IOException {
 
         URL url = new URL(geocodingString + URLEncoder.encode(address, "UTF-8") + "&sensor=false");
 
         URLConnection conn = url.openConnection();
         ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
 
         IOUtils.copy(conn.getInputStream(), output);
 
         output.close();
 
         return output.toString();
     }
 
     // used once to add cities to reminiscens.City
     public void parseGeoJSON(String json, City city) throws JSONException {
 
         JSONObject JSONresults = new JSONObject(json);
         JSONArray results = JSONresults.getJSONArray("results");
         JSONObject geometry = results.getJSONObject(0).getJSONObject("geometry");
         JSONObject location = geometry.getJSONObject("location");
         String lat = location.getString("lat");
         String lon = location.getString("lng");
         city.setLat(lat);
         city.setLon(lon);
     }
 
     public void parseGeoJSON(String json, Media photo) throws JSONException {
 
         JSONObject JSONresults = new JSONObject(json);
         JSONArray results = JSONresults.getJSONArray("results");
         JSONObject geometry = results.getJSONObject(0).getJSONObject("geometry");
         JSONObject location = geometry.getJSONObject("location");
         String lat;
         String lon;
         lat = location.getString("lat");
         lon = location.getString("lng");
         JSONArray components = results.getJSONObject(0).getJSONArray("address_components");
         String city = components.getJSONObject(2).getString("long_name");
         String region = components.getJSONObject(3).getString("long_name");
         String country = components.getJSONObject(4).getString("long_name");
         photo.getLocation().setCity(city);
         photo.getLocation().setRegion(region);
         photo.getLocation().setCountry(country);
         photo.getLocation().setLat(lat);
         photo.getLocation().setLon(lon);
         photo.getLocation().setGoogled(true);
     }
 
     public void parseGeoJSON(String json, Event event) throws JSONException {
 
         JSONObject JSONresults = new JSONObject(json);
         JSONArray results = JSONresults.getJSONArray("results");
         JSONObject geometry = results.getJSONObject(0).getJSONObject("geometry");
         JSONObject location = geometry.getJSONObject("location");
         String lat;
         String lon;
         lat = location.getString("lat");
         lon = location.getString("lng");
         JSONArray components = results.getJSONObject(0).getJSONArray("address_components");
         String city = components.getJSONObject(2).getString("long_name");
         String region = components.getJSONObject(3).getString("long_name");
         String country = components.getJSONObject(4).getString("long_name");
         event.getLocation().setCity(city);
         event.getLocation().setRegion(region);
         event.getLocation().setCountry(country);
         event.getLocation().setLat(lat);
         event.getLocation().setLon(lon);
         event.getLocation().setGoogled(true);
     }
 
     // used once to add cities to reminiscens.City
     public City getCityFromLine(String line) {
         City city = new City();
         String name = line.substring(0, line.indexOf(','));
         name=name.toLowerCase(Locale.ITALIAN).substring(0, 1).toUpperCase(Locale.ITALIAN);   
         city.setCity_name(name);
         String region = line.substring(line.indexOf(',') + 1,line.length());
         city.setRegion(region);
         city.setCountry("Italy");
         return city;
     }
 }
