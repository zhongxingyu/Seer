 package com.cloudbees.demo.gasp.location;
 
 import android.os.AsyncTask;
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 import com.cloudbees.demo.gasp.model.EventRequest;
 import com.cloudbees.demo.gasp.model.EventResponse;
 import com.cloudbees.demo.gasp.model.PlaceDetails;
 import com.cloudbees.demo.gasp.model.PlaceEvent;
 import com.cloudbees.demo.gasp.model.Places;
 import com.cloudbees.demo.gasp.model.Query;
 import com.google.gson.Gson;
 
import java.lang.NullPointerException;
 import java.lang.String;
 import java.net.URL;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Copyright (c) 2013 Mark Prichard, CloudBees
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 public class PlaceEventsTest extends AndroidTestCase {
     private static final String TAG = PlaceEventsTest.class.getName();
 
     // Latch used to signal completion of async Google Places API calls
     private CountDownLatch signal;
 
     private String jsonOutput;
 
     protected void setUp() throws Exception {
         super.setUp();
         signal = new CountDownLatch(1);
     }
 
     /**
      * Initial Google Places API Search
      * {@link} https://developers.google.com/places/documentation/search#PlaceSearchRequests
      * @param query com.cloudbees.demo.gasp.model.Query
      */
     public void placesSearch(final Query query) {
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String search = GooglePlacesClient.getQueryStringNearbySearch(query);
                     Log.d(TAG, "Places API search: " + search);
                     jsonOutput = GooglePlacesClient.doGet(new URL(search));
 
                 } catch (Exception e) {
                     fail();
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     String reference = new String();
                     Places places = new Gson().fromJson(jsonOutput, Places.class);
 
                     if (places.getStatus().equalsIgnoreCase("OK")) {
                         assertNotNull(places);
                         assertTrue(places.getResults().length > 0);
                         reference = places.getResults()[0].getReference();
                         Log.d(TAG, "Places API returns reference: " + reference);
                         assertFalse(places.getResults()[0].getReference().isEmpty());
                     }
                     else {
                         fail();
                     }
 
                     EventRequest eventRequest = new EventRequest();
                     eventRequest.setReference(reference);
                     int duration = 86400;
                     eventRequest.setDuration(duration);
                     String language = "EN-US";
                     eventRequest.setLanguage(language);
                     String summary = "New Gasp! Review";
                     eventRequest.setSummary(summary);
                     String url = "http://gasp.partnerdemo.cloudbees.net/reviews";
                     eventRequest.setUrl(url);
 
                     eventAdd(eventRequest);
 
                 } catch (Exception e) {
                     fail();
                 }
             }
         }.execute();
     }
 
     /**
      * Google Places API Add Event: calls placeDetailsEventAdded()
      * {@link} https://developers.google.com/places/documentation/actions#event_intro
      * @param request com.cloudbees.demo.gasp.model.EventRequest
      */
     public void eventAdd(final EventRequest request) {
         final EventRequest eventRequest = request;
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String requestString = GooglePlacesClient.getQueryStringAddEvent();
                     Log.d(TAG, "Places API Add Event URL: " + requestString);
                     Log.d(TAG, "Places API Add Event Body: " + new Gson().toJson(eventRequest, EventRequest.class));
                     jsonOutput = GooglePlacesClient.doPost(
                             new Gson().toJson(eventRequest, EventRequest.class), new URL(requestString));
 
                 } catch (Exception e) {
                     Log.e(TAG, "Exception: ", e);
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     EventResponse eventResponse = new Gson().fromJson(jsonOutput, EventResponse.class);
                     assertNotNull(eventResponse);
                     if (eventResponse.getStatus().equalsIgnoreCase("OK")) {
                         assertFalse(eventResponse.getEvent_id().isEmpty());
                         Log.d(TAG, "Places API Event Response: " + eventResponse.getEvent_id());
                     }
                     else {
                         fail();
                     }
 
                     placeDetailsEventAdded(eventRequest.getReference(),
                                            eventResponse.getEvent_id());
 
                 } catch (Exception e) {
                     fail();
                 }
             }
         }.execute();
     }
 
     /**
      * Google Places API Delete Event: calls PlaceDetailsEventDeleted()
      * {@link} https://developers.google.com/places/documentation/actions#event_intro
      * @param request com.cloudbees.demo.gasp.model.EventRequest
      */
     public void eventDelete(final EventRequest request) {
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String requestString = GooglePlacesClient.getQueryStringDeleteEvent();
                     Log.d(TAG, "Places API Delete Event URL: " + requestString);
                     Log.d(TAG, "Places API Delete Event Body: " + new Gson().toJson(request, EventRequest.class));
                     jsonOutput = GooglePlacesClient.doPost(
                             new Gson().toJson(request, EventRequest.class), new URL(requestString));
 
                 } catch (Exception e) {
                     fail();
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     EventResponse eventResponse = new Gson().fromJson(jsonOutput, EventResponse.class);
                     assertNotNull(eventResponse);
                     if (eventResponse.getStatus().equalsIgnoreCase("OK")) {
                         Log.d(TAG, "Places API Delete Event Response: " + eventResponse.getStatus());
                     }
                     else {
                         fail();
                     }
                     placeDetailsEventDeleted(request.getReference(), request.getEvent_id());
 
                 } catch (Exception e) {
                     fail();
                 }
             }
         }.execute();
     }
 
     /**
      * Google Places API Search (to verify event added): calls eventDelete()
      * {@link} https://developers.google.com/places/documentation/search#PlaceSearchRequests
      * @param reference
      * @param eventId
      */
     public void placeDetailsEventAdded(final String reference, final String eventId) {
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String search = GooglePlacesClient.getQueryStringPlaceDetails(reference);
                     Log.d(TAG, "Places API Search: " + search);
                     jsonOutput = GooglePlacesClient.doGet(new URL(search));
 
                 } catch (Exception e) {
                     fail();
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     PlaceDetails details = new Gson().fromJson(jsonOutput, PlaceDetails.class);
                     assertNotNull(details);
                     if (details.getStatus().equalsIgnoreCase("OK")) {
                         Log.d(TAG, "Places API Add Event Response: " + details.getStatus());
                         boolean found = false;
                         for (PlaceEvent event: details.getResult().getEvents()) {
                             if (event.getEvent_id().compareTo(eventId) == 0) {
                                 found = true;
                                 assertEquals(event.getEvent_id(), eventId);
                                 Log.d(TAG, "Places API Event: " + event.getEvent_id());
                             }
                         }
                         assertTrue(found == true);
                     }
                     else {
                         fail();
                     }
 
                     EventRequest eventRequest = new EventRequest();
                     eventRequest.setReference(reference);
                     eventRequest.setEvent_id(eventId);
 
                     eventDelete(eventRequest);
 
                 } catch (Exception e) {
                     fail();
                 }
             }
         }.execute();
     }
 
     /**
      * Google Places API Search (to verify event deleted)
      * {@link} https://developers.google.com/places/documentation/search#PlaceSearchRequests
      * @param reference
      * @param eventId
      */
     public void placeDetailsEventDeleted(final String reference, final String eventId) {
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String search = GooglePlacesClient.getQueryStringPlaceDetails(reference);
                     Log.d(TAG, "Places API Search: " + search);
                     jsonOutput = GooglePlacesClient.doGet(new URL(search));
 
                 } catch (Exception e) {
                     fail();
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     PlaceDetails details = new Gson().fromJson(jsonOutput, PlaceDetails.class);
                     assertNotNull(details);
 
                     if (details.getStatus().equalsIgnoreCase("OK")) {
                         boolean found = false;

                        if (details.getResult().getEvents() != null) {
                            for (PlaceEvent event : details.getResult().getEvents()) {
                                if (event.getEvent_id().compareTo(eventId) == 0) {
                                    found = true;
                                }
                             }
                         }
                         assertTrue(found == false);
                     }
                     else {
                         fail();
                     }
 
                 } catch (Exception e) {
                    e.printStackTrace();
                     fail();
                 }
                 // Signal completion of async test processes
                 signal.countDown();
             }
         }.execute();
     }
 
     public void testAddEvent() {
         try {
             double lat = 37.3750274;
             double lng = -122.1142916;
             int radius = 500;
             String token = "";
 
             placesSearch(new Query(lat, lng, radius, token));
 
             // Wait for async test processes to complete
             signal.await(120, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             fail();
         }
     }
 }
