 package org.mule.module.geonames;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 public class GeoNamesClientImpl implements GeoNamesClient {
 
     private RequestExecutor requestExecutor;
 
     public GeoNamesClientImpl(String username) {
         requestExecutor = new RequestExecutor(username);
     }
 
     @Override
     public String astergdem(Double[] latitudes, Double[] longitudes) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LATS, commaSeparatedString(latitudes));
         params.put(GeoNameParameter.LNGS, commaSeparatedString(longitudes));
         return executeGetRequest("/astergdem", params);
     }
 
     @Override
     public String children(Integer geonameId, Integer maxRows, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(3);
         params.put(GeoNameParameter.GEONAME_ID, geonameId);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/children", params);
     }
 
     @Override
     public String cities(Double north, Double south, Double east, Double west, String callback, String lang, Integer maxRows, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(8);
         params.put(GeoNameParameter.NORTH, north);
         params.put(GeoNameParameter.SOUTH, south);
         params.put(GeoNameParameter.EAST, east);
         params.put(GeoNameParameter.WEST, west);
         params.put(GeoNameParameter.CALLBACK, callback);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/cities", params);
     }
 
     @Override
     public String countryCode(Double latitude, Double longitude, Integer radius, String lang) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(4);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.LANG, lang);
         return executeGetRequest("/countryCode", params);
     }
 
     @Override
     public String countryInfo(String country, String lang) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.LANG, lang);
         return executeGetRequest("/countryInfo", params);
     }
 
     @Override
     public String countrySubdivision(Double latitude, Double longitude, String lang, Integer radius, Integer level, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(6);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.LEVEL, level);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/countrySubdivision", params);
     }
 
     @Override
     public String earthquakes(Double north, Double south, Double east, Double west, String callback, String date, Double minMagnitude, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(9);
         params.put(GeoNameParameter.NORTH, north);
         params.put(GeoNameParameter.SOUTH, south);
         params.put(GeoNameParameter.EAST, east);
         params.put(GeoNameParameter.WEST, west);
         params.put(GeoNameParameter.CALLBACK, callback);
         params.put(GeoNameParameter.DATE, date);
         params.put(GeoNameParameter.MIN_MAGNITUDE, minMagnitude);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/earthquakes", params);
     }
 
     @Override
     public String extendedFindNearby(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/extendedFindNearby", params);
     }
 
     @Override
     public String findNearby(Double latitude, Double longitude, FeatureClass featureClass, String featureCode, Integer radius, Integer maxRows, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(7);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.FEATURE_CLASS, featureClass);
         params.put(GeoNameParameter.FEATURE_CODE, featureCode);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/findNearby", params);
     }
 
     @Override
     public String findNearbyPlaceName(Double latitude, Double longitude, String lang, Integer radius, Integer maxRows, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(6);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/findNearbyPlaceName", params);
     }
 
     @Override
     public String findNearbyPostalCodesByLatLong(Double latitude, Double longitude, Integer radius, Integer maxRows, Style style, String country, Boolean localCountry) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(7);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.LOCAL_COUNTRY, localCountry);
         return executeGetRequest("/findNearbyPostalCodes", params);
     }
 
     @Override
     public String findNearbyPostalCodesByPostalCode(String postalCode, String country, Integer radius, Integer maxRows, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(5);
         params.put(GeoNameParameter.POSTAL_CODE, postalCode);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/findNearbyPostalCodes", params);
     }
 
     @Override
     public String findNearbyPostalCodesByPlaceName(String placeName, String country, Integer radius, Integer maxRows, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(5);
         params.put(GeoNameParameter.PLACE_NAME, placeName);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/findNearbyPostalCodes", params);
     }
 
     @Override
     public String findNearbyStreets(Double latitude, Double longitude, Integer maxRows, Integer radius) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(4);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/findNearbyPostalCodes", params);
     }
 
     @Override
     public String findNearbyStreetsOSM(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/findNearbyStreetsOSM", params);
     }
 
     @Override
     public String findNearByWeather(Double latitude, Double longitude, String callback) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(3);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.CALLBACK, callback);
         return executeGetRequest("/findNearByWeatherXML", params);
     }
 
     @Override
     public String findNearbyWikipediaByLatLong(Double latitude, Double longitude, String lang, Integer radius, Integer maxRows, String country) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(6);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.COUNTRY, country);
         return executeGetRequest("/findNearbyWikipedia", params);
     }
 
     @Override
     public String findNearbyWikipediaByPostalCode(String postalCode, String country, String lang, Integer radius, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(5);
         params.put(GeoNameParameter.POSTAL_CODE, postalCode);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/findNearbyWikipedia", params);
     }
 
     @Override
     public String findNearbyWikipediaByPlaceName(String placeName, String country, String lang, Integer radius, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(5);
         params.put(GeoNameParameter.PLACE_NAME, placeName);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/findNearbyWikipedia", params);
     }
 
     @Override
     public String findNearestAddress(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/findNearestAddress", params);
     }
 
     @Override
     public String findNearestIntersection(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/findNearestIntersection", params);
     }
 
     @Override
     public String findNearestIntersectionOSM(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/findNearestIntersectionOSM", params);
     }
 
     @Override
     public String findNearbyPOIsOSM(Double latitude, Double longitude, String type, String callback) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.TYPE, type);
         params.put(GeoNameParameter.CALLBACK, callback);
         return executeGetRequest("/findNearbyPOIsOSM", params);
     }
 
     @Override
     public String gtopo30(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/gtopo30", params);
     }
 
     @Override
     public String hierarchy(Integer geonameId, Style style) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.GEONAME_ID, geonameId);
         params.put(GeoNameParameter.STYLE, style);
         return executeGetRequest("/hierarchy", params);
     }
 
     @Override
     public String neighbourhood(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/neighbourhood", params);
     }
 
     @Override
     public String neighboursByGeonameId(Integer geonameId) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(1);
         params.put(GeoNameParameter.GEONAME_ID, geonameId);
         return executeGetRequest("/neighbours", params);
     }
 
     @Override
     public String neighboursByCountry(String country) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(1);
         params.put(GeoNameParameter.COUNTRY, country);
         return executeGetRequest("/neighbours", params);
     }
 
     @Override
     public String ocean(Double latitude, Double longitude) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         return executeGetRequest("/ocean", params);
     }
 
     @Override
     public String postalCodeCountryInfo() {
         return executeGetRequest("/postalCodeCountryInfo", Collections.<GeoNameParameter, Object>emptyMap());
     }
 
     @Override
     public String postalCodeLookup(String postalCode, String country, Integer maxRows, String callback, String charset) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(5);
         params.put(GeoNameParameter.POSTAL_CODE, postalCode);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.CALLBACK, callback);
         params.put(GeoNameParameter.CHARSET, charset);
         return executeGetRequest("/postalCodeLookupJSON", params);
     }
 
     @Override
     public String postalCodeSearchByPostalCode(String postalCode, String postalCodeStartsWith, String country, String countryBias, Integer maxRows, Style style,
                                                String operator, String charset, Boolean reduced) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(9);
         params.put(GeoNameParameter.POSTAL_CODE, postalCode);
        params.put(GeoNameParameter.POSTAL_CODE_STARTS_WITH, postalCode);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.COUNTRY_BIAS, countryBias);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         params.put(GeoNameParameter.OPERATOR, operator);
         params.put(GeoNameParameter.CHARSET, charset);
         params.put(GeoNameParameter.REDUCED, reduced);
         return executeGetRequest("/postalCodeSearch", params);
     }
 
     @Override
     public String postalCodeSearchByPlaceName(String placeName, String placeNameStartsWith, String country, String countryBias, Integer maxRows, Style style,
                                               String operator, String charset, Boolean reduced) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(9);
         params.put(GeoNameParameter.PLACE_NAME, placeName);
         params.put(GeoNameParameter.PLACE_NAME_STARTS_WITH, placeNameStartsWith);
         params.put(GeoNameParameter.COUNTRY, country);
         params.put(GeoNameParameter.COUNTRY_BIAS, countryBias);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.STYLE, style);
         params.put(GeoNameParameter.OPERATOR, operator);
         params.put(GeoNameParameter.CHARSET, charset);
         params.put(GeoNameParameter.REDUCED, reduced);
         return executeGetRequest("/postalCodeSearch", params);
     }
 
     @Override
     public String rssToGeo(String feedUrl, String feedLanguage, String type, String geoRSS, Boolean addUngeocodedItems, String country) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(6);
         params.put(GeoNameParameter.FEED_URL, feedUrl);
         params.put(GeoNameParameter.FEED_LANGUAGUE, feedLanguage);
         params.put(GeoNameParameter.TYPE, type);
         params.put(GeoNameParameter.GEO_RSS, geoRSS);
         params.put(GeoNameParameter.ADD_UNGEOCODED_ITEMS, addUngeocodedItems);
         params.put(GeoNameParameter.COUNTRY, country);
         return executeGetRequest("/rssToGeoRSS", params);
     }
 
     @Override
     public String search(String q, String name, String nameEquals, String nameStartsWith, Integer maxRows, Integer startRow, String[] countries, String countryBias,
                          String continentCode, String adminCode1, String adminCode2, String adminCode3, FeatureClass[] featureClasses, String[] featureCodes, String lang,
                          String type, Style style, Boolean nameRequired, String tag, String operator, String charset, Float fuzzy) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(22);
         params.put(GeoNameParameter.Q, q);
         params.put(GeoNameParameter.NAME, name);
         params.put(GeoNameParameter.NAME_EQUALS, nameEquals);
         params.put(GeoNameParameter.NAME_STARTS_WITH, nameStartsWith);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         params.put(GeoNameParameter.START_ROW, startRow);
         for (String country : countries) {
             params.put(GeoNameParameter.COUNTRY, country);
         }
         params.put(GeoNameParameter.COUNTRY_BIAS, countryBias);
         params.put(GeoNameParameter.CONTINENT_CODE, continentCode);
         params.put(GeoNameParameter.ADMIN_CODE_1, adminCode1);
         params.put(GeoNameParameter.ADMIN_CODE_2, adminCode2);
         params.put(GeoNameParameter.ADMIN_CODE_3, adminCode3);
         for (FeatureClass featureClass : featureClasses) {
             params.put(GeoNameParameter.FEATURE_CLASS, featureClass);
         }
         for (String featureCode : featureCodes) {
             params.put(GeoNameParameter.FEATURE_CODE, featureCode);
         }
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.TYPE, type);
         params.put(GeoNameParameter.STYLE, style);
         params.put(GeoNameParameter.NAME_REQUIRED, nameRequired);
         params.put(GeoNameParameter.TAG, tag);
         params.put(GeoNameParameter.OPERATOR, operator);
         params.put(GeoNameParameter.CHARSET, charset);
         params.put(GeoNameParameter.FUZZY, fuzzy);
         return executeGetRequest("/search", params);
     }
 
     @Override
     public String siblings(Integer geonameId) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(1);
         params.put(GeoNameParameter.GEONAME_ID, geonameId);
         return executeGetRequest("/siblings", params);
     }
 
     @Override
     public String srtm3(Double[] latitudes, Double[] longitudes) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.LATS, commaSeparatedString(latitudes));
         params.put(GeoNameParameter.LNGS, commaSeparatedString(longitudes));
         return executeGetRequest("/srtm3", params);
     }
 
     @Override
     public String timezone(Double latitude, Double longitude, Integer radius, String date) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(4);
         params.put(GeoNameParameter.LAT, latitude);
         params.put(GeoNameParameter.LNG, longitude);
         params.put(GeoNameParameter.RADIUS, radius);
         params.put(GeoNameParameter.DATE, date);
         return executeGetRequest("/timezone", params);
     }
 
     @Override
     public String weather(Double north, Double south, Double east, Double west, String callback, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(6);
         params.put(GeoNameParameter.NORTH, north);
         params.put(GeoNameParameter.SOUTH, south);
         params.put(GeoNameParameter.EAST, east);
         params.put(GeoNameParameter.WEST, west);
         params.put(GeoNameParameter.CALLBACK, callback);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/weather", params);
     }
 
     @Override
     public String weatherIcao(String icaoCode, String callback) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(2);
         params.put(GeoNameParameter.ICAO_CODE, icaoCode);
         params.put(GeoNameParameter.CALLBACK, callback);
         return executeGetRequest("/weatherIcaoXML", params);
     }
 
     @Override
     public String wikipediaBoundingBox(Double north, Double south, Double east, Double west, String lang, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(6);
         params.put(GeoNameParameter.NORTH, north);
         params.put(GeoNameParameter.SOUTH, south);
         params.put(GeoNameParameter.EAST, east);
         params.put(GeoNameParameter.WEST, west);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/wikipediaBoundingBox", params);
     }
 
     @Override
     public String wikipediaSearch(String q, String title, String lang, Integer maxRows) {
         Map<GeoNameParameter, Object> params = new HashMap<GeoNameParameter, Object>(4);
         params.put(GeoNameParameter.Q, q);
         params.put(GeoNameParameter.TITLE, title);
         params.put(GeoNameParameter.LANG, lang);
         params.put(GeoNameParameter.MAX_ROWS, maxRows);
         return executeGetRequest("/wikipediaSearch", params);
     }
 
     private String commaSeparatedString(Object[] objects) {
         StringBuilder stringBuilder = new StringBuilder();
         for (Object object : objects) {
             stringBuilder.append(object).append(',');
         }
         return stringBuilder.substring(0, stringBuilder.length() - 1);
     }
 
     private String executeGetRequest(String url, Map<GeoNameParameter, Object> params) {
         try {
             String response = requestExecutor.executeGetRequest(url, params);
             checkError(response);
             return response;
         } catch (Exception e) {
             throw new GeoNamesConnectorException(e);
         }
     }
 
     private void checkError(String response) {
         if (response.contains("status")) {
             Document document = buildDoc(response);
             Element message = document.getRootElement().getChild("status");
             if (message != null) {
                 String code = message.getAttributeValue("value");
                 throw new GeoNamesConnectorException("Error code: " + code + " / Error message: " + message.getAttributeValue("message"));
             }
         }
     }
 
     private Document buildDoc(String response) {
         SAXBuilder parser = new SAXBuilder();
         try {
             return parser.build(new StringReader(response));
         } catch (JDOMException e) {
             throw new GeoNamesConnectorException(e);
         } catch (IOException e) {
             throw new GeoNamesConnectorException(e);
         }
     }
 }
