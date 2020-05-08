 package com.moac.android.soundmap.api;
 
 import com.google.gson.reflect.TypeToken;
 import com.moac.android.soundmap.model.Track;
 
 import java.util.Collection;
 
 /**
  * Refer - http://developers.soundcloud.com/docs/api/reference#tracks
  */
 public class TracksEndPoint {
 
     public static final String GENRE_FILTER = "genres";
 
     public static final String TAGS_FILTER = "tags";
     public static final String QUERY_FILTER = "q";
     public static final String GEO_TAG = "geo:";
     public static final String GEO_LAT_TAG = "geo:lat";
 
    /**
     * SEEMS TO BE A PROBLEM USING THE TAGS FILTER WITH GEO_TAG STRING - GET HTTP 509 RESPONSE
     *
     * UNTIL I WORK IT OUT - USING A QUERY THAT GENERATES SOME GEO HITS - NO GENRE INFO.
     */
 
     public static ApiRequest<Collection<Track>> getGeoTracks(String _genre) {
         ApiRequest<Collection<Track>> request = new ApiRequest<Collection<Track>>(new TypeToken<Collection<Track>>() {}, EndPoints.TRACKS);
        // See note above.
        //request.withQuery(GENRE_FILTER, _genre);
        //request.withQuery(TAGS_FILTER, GEO_LAT_TAG);
        request.withQuery(QUERY_FILTER, GEO_LAT_TAG);  // query is very broad, get many non geo: hits too.
         return request;
     }
 }
