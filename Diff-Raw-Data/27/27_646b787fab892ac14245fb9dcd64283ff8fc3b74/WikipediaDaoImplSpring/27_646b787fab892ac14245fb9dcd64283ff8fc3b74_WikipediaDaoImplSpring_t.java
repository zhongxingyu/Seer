 /*
  * Copyright (c) 2012-2013 Nahuel Barrios <barrios.nahuel@gmail.com>.
  * No se reconocerá ningún tipo de garantía.
  */
 
 /**
  * Created by: Nahuel Barrios.
  * On: 12/17/12 at 1:48 PMhs.
  */
 package org.nbempire.android.tourguide.dao.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import android.util.Log;
 import org.nbempire.android.tourguide.dao.WikipediaDao;
 import org.nbempire.android.tourguide.domain.wikipedia.WikipediaPlace;
 import org.nbempire.android.tourguide.domain.wikipedia.WikipediaResponse;
 import org.nbempire.android.tourguide.util.wikipedia.WikipediaConstants;
 import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 /**
  * Implementation of the {@link WikipediaDao} interface that uses Spring Android REST Template to retrieve places.
  *
  * @author Nahuel Barrios.
  * @since 1
  */
 public class WikipediaDaoImplSpring implements WikipediaDao {
 
     /**
      * Tag for class' log.
      */
     private static final String TAG = "WikipediaDaoImplSpring";
 
     @Override
     public WikipediaPlace[] queryByGeosearch(double latitude, double longitude) {
         RestTemplate restTemplate = new RestTemplate();
 
         // Add a JSON converter (use GSON instead of Jackson because is a smaller library)
         restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
 
         String urlString = "http://en.wikipedia.org/w/api.php?" +
                                    "action=query" +
                                    "&list=geosearch" +
                                    "&format=json" +
                                    "&gscoord=" + latitude + "%7C" + longitude +
                                    "&gsradius=" + WikipediaConstants.MAXIMUM_SEARCH_RADIUS +
                                    "&gslimit=10";

         URI url = null;
        WikipediaPlace[] wikipediaPlaces = new WikipediaPlace[0];
         try {
             url = new URI(urlString);

            WikipediaResponse response = restTemplate.getForObject(url, WikipediaResponse.class);

            if (response != null && response.getQuery() != null) {
                wikipediaPlaces = response.getQuery().getGeosearch();
                Log.i(TAG, "Found: " + wikipediaPlaces.length + " wikipedia places for (lat, lon): (" + latitude + ", " + longitude + ").");
            }
         } catch (URISyntaxException e) {
             // TODO : Log this exception or do something cool.
             Log.e(TAG, "There was an error creating the URI: " + urlString);
 
        } catch (RestClientException restClientException) {
            Log.e(TAG, "There was an error getting Wikipedia places from URL: " + url);
            Log.e(TAG, restClientException.getMessage());
        }
 
         return wikipediaPlaces;
     }
 }
