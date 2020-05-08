 package com.google.code.geocoder;
 
 import com.google.code.geocoder.model.GeocodeResponse;
 import com.google.code.geocoder.model.GeocoderRequest;
 import com.google.code.geocoder.model.LatLng;
 import com.google.code.geocoder.model.LatLngBounds;
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 /**
  * @author <a href="mailto:panchmp@gmail.com">Michael Panchenko</a>
  */
 public class Geocoder {
     private Log log = LogFactory.getLog(Geocoder.class);
 
     private static final String GEOCODE_REQUEST_URL = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false";
     private static HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
 
     public Geocoder() {
     }
 
     public static synchronized void setConnectionManager(HttpConnectionManager manager) {
         httpClient = new HttpClient(manager);
     }
 
     public synchronized HttpClient getHttpClient() {
         return httpClient;
     }
 
     public GeocodeResponse geocode(final GeocoderRequest geocoderRequest) {
         try {
             final String urlString = getURL(geocoderRequest);
 
             final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
 
             final GetMethod getMethod = new GetMethod(urlString);
             try {
                 httpClient.executeMethod(getMethod);
                 final Reader reader = new InputStreamReader(getMethod.getResponseBodyAsStream(), getMethod.getResponseCharSet());
 
                 return gson.fromJson(reader, GeocodeResponse.class);
             } finally {
                 getMethod.releaseConnection();
             }
         } catch (Exception e) {
            log.error(e);
             return null;
         }
     }
 
     protected String getURL(final GeocoderRequest geocoderRequest) throws UnsupportedEncodingException {
         if (log.isTraceEnabled()) {
             log.trace(geocoderRequest);
         }
         final String address = geocoderRequest.getAddress();
         final LatLngBounds bounds = geocoderRequest.getBounds();
         final String language = geocoderRequest.getLanguage();
         final String region = geocoderRequest.getRegion();
         final LatLng location = geocoderRequest.getLocation();
 
         String urlString = GEOCODE_REQUEST_URL;
         if (StringUtils.isNotBlank(address)) {
             urlString += "&address=" + URLEncoder.encode(address, "UTF-8");
         } else if (location != null) {
             urlString += "&latlng=" + URLEncoder.encode(location.toUrlValue(), "UTF-8");
         } else {
             throw new IllegalArgumentException("Address or location not defined");
         }
         if (StringUtils.isNotBlank(language)) {
             urlString += "&language=" + URLEncoder.encode(language, "UTF-8");
         }
         if (StringUtils.isNotBlank(region)) {
             urlString += "&region=" + URLEncoder.encode(region, "UTF-8");
         }
         if (bounds != null) {
             urlString += "&bounds=" + URLEncoder.encode(bounds.getSouthwest().toUrlValue() + "|" + bounds.getNortheast().toUrlValue(), "UTF-8");
         }
 
         if (log.isTraceEnabled()) {
             log.trace("url: " + urlString);
         }
         return urlString;
     }
 }
