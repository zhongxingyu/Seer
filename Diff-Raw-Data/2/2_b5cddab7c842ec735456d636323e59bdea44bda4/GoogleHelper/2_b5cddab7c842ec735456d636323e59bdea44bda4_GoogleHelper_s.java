 package com.guide.city.crawler;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.guide.city.entities.Location;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 
 public class GoogleHelper {
 
     public final static String GOOGLE_APPLICATION_KEY = "AIzaSyCd5bBuiljMgprbV2NbHZwVT4V-A2_nKE8";
 
     public final static String GOOGLE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
     public final static String GOOGLE_PLACE_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json";
     public final static String GOOGLE_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
 
 
     public static String encodeParams(final Map<String, String> params) {
         final String paramsUrl = Joiner.on('&').join(// получаем значение вида key1=value1&key2=value2...
                 Iterables.transform(params.entrySet(), new Function<Map.Entry<String, String>, String>() {
 
                     @Override
                     public String apply(final Map.Entry<String, String> input) {
                         try {
                             final StringBuffer buffer = new StringBuffer();
                             buffer.append(input.getKey());// получаем значение вида key=value
                             buffer.append('=');
                             buffer.append(URLEncoder.encode(input.getValue(),
                                     "utf-8"));// кодируем строку в соответствии со стандартом HTML 4.01
                             return buffer.toString();
                         }
                         catch (final UnsupportedEncodingException e) {
                             throw new RuntimeException(e);
                         }
                     }
                 }));
         return paramsUrl;
     }
 
     private static String readAll(final Reader rd) throws IOException {
         final StringBuilder sb = new StringBuilder();
         int cp;
         while ((cp = rd.read()) != -1) {
             sb.append((char) cp);
         }
         return sb.toString();
     }
 
     public static JSONObject read(final String url) throws IOException, JSONException {
         final InputStream is = new URL(url).openStream();
         try {
             final BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
             final String jsonText = readAll(rd);
             final JSONObject json = new JSONObject(jsonText);
             return json;
         }
         finally {
             is.close();
         }
     }
 
     public static String getStreetName(Location location) throws Exception {
         Map<String, String> params = Maps.newHashMap();
        params.put("language", "ru");// язык данных, на котором мы хотим получить
         params.put("sensor", "false");// исходит ли запрос на геокодирование от устройства с датчиком местоположения
         // текстовое значение широты/долготы, для которого следует получить ближайший понятный человеку адрес, долгота и
         // широта разделяется запятой, берем из предыдущего примера
         params.put("latlng", location.toGoogleStringFormat());
         String streetName = GoogleResponseCreator.getStreet(params);
         return streetName.substring(0, streetName.indexOf(","));
     }
 }
