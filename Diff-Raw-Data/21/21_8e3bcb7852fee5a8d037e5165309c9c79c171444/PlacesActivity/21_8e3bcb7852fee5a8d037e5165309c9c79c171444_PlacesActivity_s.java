 package com.cloudbees.gasp.activity;
 
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.cloudbees.gasp.R;
 import com.cloudbees.gasp.fragment.AddEventFragment;
 import com.cloudbees.gasp.fragment.DeleteEventFragment;
 import com.cloudbees.gasp.fragment.NearbySearchFragment;
 import com.cloudbees.gasp.fragment.PlaceDetailsFragment;
 import com.cloudbees.gasp.model.EventResponse;
 import com.cloudbees.gasp.model.Place;
 import com.cloudbees.gasp.model.PlaceDetails;
 import com.cloudbees.gasp.model.Places;
 import com.cloudbees.gasp.model.Query;
 
 import java.util.ArrayList;
 
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
 
 public class PlacesActivity extends Activity {
     private static final String TAG = PlacesActivity.class.getName();
 
     private ArrayAdapter<String> mAdapter;
     private ListView mListView;
     private final ArrayList<String> mList = new ArrayList<String>();
     private final ArrayList<String> mReferenceList = new ArrayList<String>();
 
     private NearbySearchFragment mSearchFragment;
     private PlaceDetailsFragment mDetailsFragment;
     private AddEventFragment mAddEventFragment;
     private DeleteEventFragment mDeleteEventFragment;
 
     private static final double lat = 37.3750274;
     private static final double lng = -122.1142916;
     private static final int radius = 500;
     private static String token = "";
 
     private void showLocations(Places places) {
         for (Place place : places.getResults()) {
             Log.d(TAG, place.getName() + " " + place.getReference());
             mAdapter.add(place.getName());
             mReferenceList.add(place.getReference());
         }
     }
 
     private void checkToken(Places places) {
         try {
             if (places.getNext_page_token() == null) {
                 token = "";
                 Log.d(TAG, "No page token returned from Places API");
             }
             else {
                 token = places.getNext_page_token();
                 Log.d(TAG, "Next page token: " + token);
                 setButtonText(R.string.places_button_locations_next);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void showDetails(PlaceDetails placeDetails) {
         try {
             Intent intent = new Intent();
             intent.setClass(PlacesActivity.this, PlacesDetailActivity.class);
             intent.putExtra("PlaceDetail", placeDetails.getResult());
             startActivityForResult(intent, 0);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
 
     }
 
     private void addPlacesFragments() {
         mSearchFragment = new NearbySearchFragment() {
             public void onSuccess(Places places) {
                 showLocations(places);
                 checkToken(places);
             }
             public void onFailure(String status) {
                 Log.e(TAG, "Google Places API search failed: status = " + status );
             }
         };
 
         mDetailsFragment = new PlaceDetailsFragment() {
             @Override
             public void onSuccess(PlaceDetails placeDetails) {
                 showDetails(placeDetails);
             }
 
             @Override
             public void onFailure(String status) {
                 Log.e(TAG, "Google Places API search failed: status = " + status );
             }
         };
 
         mAddEventFragment = new AddEventFragment() {
             @Override
             public void onSuccess(EventResponse eventResponse) {
                 Log.d(TAG, "Event Added: " + eventResponse.getEvent_id());
             }
 
             @Override
             public void onFailure(String status) {
                 Log.e(TAG, "Google Places API search failed: status = " + status);
             }
         };
 
         mDeleteEventFragment = new DeleteEventFragment() {
             @Override
             public void onSuccess(EventResponse eventResponse) {
                 Log.d(TAG, "Event Deleted");
             }
 
             @Override
             public void onFailure(String status) {
                 Log.e(TAG, "Google Places API search failed: status = " + status );
             }
         };
 
         FragmentManager fm = getFragmentManager();
         FragmentTransaction ft = fm.beginTransaction();
         ft.add(mSearchFragment, "LocationSearchFragment");
         ft.add(mDetailsFragment, "PlaceDetailsFragment");
         ft.add(mAddEventFragment, "AddEventFragment");
         ft.add(mDeleteEventFragment, "DeleteEventFragment");
         ft.commit();
     }
 
     private void getLocations() {
         try {
             Query query = new Query (lat, lng, radius, token);
             mSearchFragment.nearbySearch(query);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void addButtonListener() {
         Button placesButton = (Button)findViewById(R.id.places_button);
         placesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getLocations();
             }
         });
     }
 
     private void addListViewAdapter() {
         try {
             setContentView(R.layout.gasp_places_layout);
             mListView = (ListView) findViewById(R.id.places_listView);
             mAdapter = new ArrayAdapter<String>(this, R.layout.gasp_list_layout, mList);
             mListView.setAdapter(mAdapter);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void addItemClickListener() {
         mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Log.d(TAG, "Clicked: item #" + id + ", "
                             + mList.get((int) id) + " ["
                             + mReferenceList.get((int)id) + "]");
                 Query query = new Query(mReferenceList.get((int)id));
                 mDetailsFragment.placeDetails(query);
             }
         });
     }
 
     private void setButtonText(int resId) {
         Button placesButton = (Button)findViewById(R.id.places_button);
         placesButton.setText(resId);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         try {
             addPlacesFragments();
             addListViewAdapter();
             addButtonListener();
             addItemClickListener();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 }
