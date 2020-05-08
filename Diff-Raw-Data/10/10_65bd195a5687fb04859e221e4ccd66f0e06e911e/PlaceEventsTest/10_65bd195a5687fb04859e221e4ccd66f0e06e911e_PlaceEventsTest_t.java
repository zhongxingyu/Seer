 package com.cloudbees.gasp.location;
 
 import android.os.AsyncTask;
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 import com.cloudbees.gasp.model.EventRequest;
 import com.cloudbees.gasp.model.EventResponse;
 import com.cloudbees.gasp.model.PlaceDetails;
 import com.cloudbees.gasp.model.Places;
 import com.cloudbees.gasp.model.Query;
 import com.google.gson.Gson;
 
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
 
     private CountDownLatch signal, signal2, signal3, signal4, signal5;
 
     private final double lat = 37.3750274;
     private final double lng = -122.1142916;
     private final int radius = 500;
     private final String token = "";
 
     private final int duration = 86400;
     private final String language = "EN-US";
     private final String summary = "New Gasp! Review";
     private final String url = "http://gasp.partnerdemo.cloudbees.net/reviews";
 
     private static String reference = "";
     private static String eventId = "";
 
     private String jsonOutput;
 
     protected void setUp() throws Exception {
         super.setUp();
         signal = new CountDownLatch(1);
         signal2 = new CountDownLatch(1);
         signal3 = new CountDownLatch(1);
         signal4 = new CountDownLatch(1);
         signal5 = new CountDownLatch(1);
     }
 
     public void placesSearch(Query query) {
         final Query searchQuery = query;
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String search = GooglePlacesClient.getQueryStringNearbySearch(searchQuery);
                     Log.d(TAG, search);
                     jsonOutput = GooglePlacesClient.doGet(new URL(search));
 
                 } catch (Exception e) {
                     Log.e(TAG, "Exception: ", e);
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     Places places = new Gson().fromJson(jsonOutput, Places.class);
 
                     if (places.getStatus().equalsIgnoreCase("OK")) {
                         assertNotNull(places);
                         assertTrue(places.getResults().length > 0);
                         reference = places.getResults()[0].getReference();
                         Log.d(TAG, reference);
                         assertFalse(places.getResults()[0].getReference().isEmpty());
                     }
                     else {
                         fail();
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 // Call countDown() on the latch so that the test completes immediately
                 signal.countDown();
             }
         }.execute();
     }
 
     public void eventAdd(EventRequest request) {
         final EventRequest eventRequest = request;
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String requestString = GooglePlacesClient.getQueryStringAddEvent();
                     Log.d(TAG, requestString);
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
 
                     if (eventResponse.getStatus().equalsIgnoreCase("OK")) {
                         assertNotNull(eventResponse);
                         assertFalse(eventResponse.getEvent_id().isEmpty());
                         Log.d(TAG, eventResponse.getEvent_id());
                         eventId = eventResponse.getEvent_id();
 
                     }
                     else {
                         fail();
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 // Call countDown() on the latch so that the test completes immediately
                 signal2.countDown();
             }
         }.execute();
     }
 
     public void eventDelete(EventRequest request) {
         final EventRequest eventRequest = request;
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String requestString = GooglePlacesClient.getQueryStringDeleteEvent();
                     Log.d(TAG, requestString);
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
 
                     if (eventResponse.getStatus().equalsIgnoreCase("OK")) {
                         Log.d(TAG, jsonOutput);
                         assertNotNull(eventResponse);
                     }
                     else {
                         fail();
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 // Call countDown() on the latch so that the test completes immediately
                 signal3.countDown();
             }
         }.execute();
     }
 
     public void placeDetailsEventAdded() {
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String search = GooglePlacesClient.getQueryStringPlaceDetails(reference);
                     Log.d(TAG, search);
                     jsonOutput = GooglePlacesClient.doGet(new URL(search));
 
                 } catch (Exception e) {
                     Log.e(TAG, "Exception: ", e);
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     PlaceDetails details = new Gson().fromJson(jsonOutput, PlaceDetails.class);
 
                     if (details.getStatus().equalsIgnoreCase("OK")) {
                         assertNotNull(details);
                     }
                     else {
                         fail();
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 // Call countDown() on the latch so that the test completes immediately
                 signal4.countDown();
             }
         }.execute();
     }
 
     public void placeDetailsEventDeleted() {
 
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     String search = GooglePlacesClient.getQueryStringPlaceDetails(reference);
                     Log.d(TAG, search);
                     jsonOutput = GooglePlacesClient.doGet(new URL(search));
 
                 } catch (Exception e) {
                     Log.e(TAG, "Exception: ", e);
                 }
                 return jsonOutput;
             }
 
             @Override
             protected void onPostExecute(String jsonOutput) {
                 super.onPostExecute(jsonOutput);
                 try {
                     PlaceDetails details = new Gson().fromJson(jsonOutput, PlaceDetails.class);
 
                     if (details.getStatus().equalsIgnoreCase("OK")) {
                         assertNotNull(details);
                     }
                     else {
                         fail();
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 // Call countDown() on the latch so that the test completes immediately
                 signal5.countDown();
             }
         }.execute();
     }
 
     public void testAddEvent() {
         try {
             placesSearch(new Query(lat, lng, radius, token));
 
             // Allow 30 secs for Google Places API call to complete
             signal.await(30, TimeUnit.SECONDS);
 
             EventRequest eventRequest = new EventRequest();
             eventRequest.setReference(reference);
             eventRequest.setDuration(duration);
             eventRequest.setLanguage(language);
             eventRequest.setSummary(summary);
             eventRequest.setUrl(url);
 
             eventAdd(eventRequest);
 
             // Allow 30 secs for Google Places API call to complete
             signal2.await(30, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             fail();
         }
     }
 
    public void testAddedEvent() {
         try {
             placeDetailsEventAdded();
 
             // Allow 30 secs for Google Places API call to complete
             signal4.await(30, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             fail();
         }
     }
 
     public void testDeleteEvent() {
         try {
             EventRequest eventRequest = new EventRequest();
             eventRequest.setReference(reference);
             eventRequest.setEvent_id(eventId);
 
             eventDelete(eventRequest);
 
             // Allow 30 secs for Google Places API call to complete
             signal3.await(30, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             fail();
         }
     }
 
    public void testDeletedEvent() {
         try {
             placeDetailsEventDeleted();
 
             // Allow 30 secs for Google Places API call to complete
             signal5.await(30, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             fail();
         }
     }
 }
