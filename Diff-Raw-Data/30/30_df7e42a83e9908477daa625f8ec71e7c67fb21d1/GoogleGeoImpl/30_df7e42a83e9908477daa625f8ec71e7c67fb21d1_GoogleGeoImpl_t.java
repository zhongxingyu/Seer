 package com.flaptor.util.apis;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.rmi.Remote;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.log4j.Logger;
 import org.apache.tools.ant.filters.StringInputStream;
 import org.xml.sax.SAXException;
 
 import com.flaptor.util.CollectionsUtil;
 import com.flaptor.util.Execute;
 import com.flaptor.util.IOUtil;
 import com.flaptor.util.Pair;
 import com.flaptor.util.SaxUtil;
 import com.flaptor.util.StringUtil;
 import com.flaptor.util.ThreadUtil;
 import com.flaptor.util.cache.MemFileCache;
 import com.flaptor.util.xml.SaxStackHandler;
 
 /**
  * 
  * Cache for google geocoding api, that enforces the request limits
  * 
  * @author Martin Massera
  *
  */
 public class GoogleGeoImpl implements GoogleGeo {
     
     private static final Logger logger = Logger.getLogger(Execute.whoAmI());
 
 	private long lastCall = System.currentTimeMillis();
 	
 	private MemFileCache<String> geoCache;
 	private String key;
 
 	public GoogleGeoImpl(String cacheDir, String key) {
 		geoCache = new MemFileCache<String>(50000, cacheDir);
		this.key = key;
 	}
 	
 	public MemFileCache<String> getCache() {
 		return geoCache;
 	}
 	
     public Geocode getGeocodeCache(String place) {
         String xml = getGeocodingXmlCache(place);        
         if (xml != null) return parse(xml);
         else return null; 
     }
     public String getGeocodingXmlCache(String place) {
        return geoCache.get(place.toLowerCase());
     }
     public Geocode getGeocode(String place) {
 	    String xml = getGeocodingXml(place);
 	    if (xml != null) return parse(xml);
 	    else return null; 
 	}
	public String getGeocodingXml(String place) {
	    place = place.toLowerCase();
 	    String ret = getGeocodingXmlCache(place);
 		if (ret != null) return ret;
 		
 		synchronized (this) {
 			HttpClient client = new HttpClient();
			String url = "http://maps.google.com/maps/geo?q="+StringUtil.urlEncode(place) + "&output=xml&key=" + "ABQIAAAANsiYczUO4OKBi4ERs1tMGxS3wM4UOGmx-u_A5H5NKh1NS56MdRTDtJ0TDy_CC1KY2AhY0D8eC9IjzQ";
			
 			GetMethod get = new GetMethod(url);
			logger.debug("retrieving " + place);
 
 			//enforce the 1 request/1.750 sec limit
             while (System.currentTimeMillis() - lastCall < 1750) {
                 ThreadUtil.sleep(50);
             }
             try {
 				int status = client.executeMethod(get);
 				if (status !=  200) throw new Exception("http request failed - status " + status);
 				ret = IOUtil.readAll(get.getResponseBodyAsStream());
 				get.releaseConnection();
 			} catch (Exception e) {
 				logger.warn(e,e);
 				return null;
 			} finally {
                 lastCall = System.currentTimeMillis();
 			}
 
 	        if (checkGoodStatus(place, ret)) {
                 geoCache.put(place, ret);
 	            return ret; 
 	        } else {
 	            return null;
 	        }
 		}
 	}
 	
 	/**
 	 * removes all entries with bad status codes
 	 */
 	public void clean() {
        for (Pair<String,String> e : geoCache) {
           if (!checkGoodStatus(e.first(), e.last())) continue;
         }
 	}
 	
     public static int getStatusCode(String xml) {
         if (xml == null) return -1;
         final int[] code = new int[]{-1};
         try {
             SaxUtil.parse(xml, new SaxStackHandler() {
                 public void endElement(String uri, String localName, String name, String textContent) throws SAXException {
                     if (name.equals("code")) code[0] = Integer.parseInt(textContent);
                 }
             });
         } catch (Exception e) {
             logger.error(e,e);
         }
         return code[0];
     }
 
     private boolean checkGoodStatus(String place, String xml) {
         if (place == null) return false;
         int code = getStatusCode(xml);
         switch (code) {
             case -1: 
             case 400:
             case 500:
             case 601:
             case 610:
             case 620:
                logger.debug("removing " + place + " - code " + code); 
                 geoCache.remove(place);
                 return false;
         }
         return true;
     }
 
 	/**
 	 * parses XML and gets the locality and country
 	 * works only when only one place matches the query
 	 * 
 	 * @param xml
 	 * @return
 	 */
 	static public Geocode parse(String xml) {
 	    final boolean[] valid = new boolean[] {true};
         final Geocode placeInfo = new Geocode();
         try {
             SAXParserFactory.newInstance().newSAXParser().parse(new StringInputStream(xml), new SaxStackHandler() {
                 public void endElement(String uri, String localName, String name, String textContent) throws SAXException {
                     if (name.equals("code")){
                         if (!textContent.equals("200")) valid[0] = false;
                     }
                     if (name.equals("CountryNameCode")) {
                         if (placeInfo.getCountry() != null) valid[0] = false;
                         placeInfo.setCountry(textContent);
                     }
                     if (name.equals("LocalityName")) placeInfo.setLocality(textContent);
                     if (name.equals("coordinates")) {
                         String[] coords = textContent.split(",");
                         placeInfo.setLatLong(new double[] {Double.parseDouble(coords[1]),Double.parseDouble(coords[0])});
                     }
                 }
             });
         } catch (Exception e) {
             logger.error(e);
             return null;
         }
         if (valid[0]) return placeInfo;
         else return null; 
 	}
 	
 	public static void main(String[] args) {
        new GoogleGeoImpl("/home/marto/geocache", "hola").clean();
     }
 }
