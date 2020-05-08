 package com.teamboid.twitterapi.search;
 
 import com.teamboid.twitterapi.client.Paging;
 
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * @author Aidan Follestad
  */
 public class SearchQuery implements Serializable {
 
 	private static final long serialVersionUID = 1386290028343678230L;
 
 	private SearchQuery(String query, Paging paging) {
         if(query != null && query.trim().length() == 0) _query = null;
         _resultType = ResultType.DEFAULT;
         _query = query;
         _paging = paging;
     }
 
     private String _query;
     private GeoCode _geo;
     private Paging _paging;
     private String _lang;
     private String _until;
     private ResultType _resultType;
 
     /**
      * Creates a new search query.
      * @param query The term to match tweets with.
      * @return
      */
     public static SearchQuery create(String query) {
         return new SearchQuery(query, null);
     }
 
     /**
      * Creates a new search query.
      * @param query The term to match tweets with.
      * @return
      */
     public static SearchQuery create(String query, Paging paging) {
         return new SearchQuery(query, paging);
     }
 
     /**
      * Sets the geo code for the query, allowing you to find tweets composed near a
      * location in the world or composed by people that live near a location in the world.
      */
     public SearchQuery setGeoCode(GeoCode geo) {
         _geo = geo;
         return this;
     }
 
     /**
      * Sets the result type of the query, allowing you to get popular and/or recent tweets.
      */
     public SearchQuery setResultType(ResultType type) {
         _resultType = type;
         return this;
     }
 
     /**
      * Sets the locale code of tweets to attempt to limit the search to (e.g., "en" for English).
      */
     public SearchQuery setLang(String lang) {
         _lang = lang;
         return this;
     }
 
     public SearchQuery setUntil(Date until) {
         if(until != null) {
             SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
             _until = sf.format(until);
         }
         return this;
     }
 
     public static enum ResultType {
         /**
          * The result_type parameter will not be included in your search.
          */
         DEFAULT,
         /**
          * Include both popular and real time results in the response.
          */
         MIXED,
         /**
          * Return only the most recent results in the response.
          */
         RECENT,
         /**
          * Return only the most popular results in the response.
          */
         POPULAR
     }
 
     public String getUrl() {
         try {
             String url = null;
             if(_geo != null && _query == null) {
                url = "&q=geocode:" + _geo.getLatitude() + "," + _geo.getLongitude() + "," +
                         _geo.getDistance() + _geo.getUnit().name().toLowerCase();
             } else {
                url = "&q=" + URLEncoder.encode(_query, "UTF8");
                 if(_geo != null) {
                     url += ("&" + _geo.getLatitude() + "," + _geo.getLongitude() + "," +
                             _geo.getDistance() + _geo.getUnit().name().toLowerCase());
                 }
             }
             if(_paging != null) {
                 url += _paging.getUrlString('&', true).replace("count=", "rpp=");
             }
             if(_lang != null) url += "&lang=" + _lang;
             if(_until != null) url += "&until=" + _until;
             if(_resultType != ResultType.DEFAULT) {
                 url += "&result_type=" + _resultType.name().toLowerCase();
             }
             return url;
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
