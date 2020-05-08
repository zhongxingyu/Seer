 /*
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.heneryh.aquanotes.ui;
 
 import com.heneryh.aquanotes.R;
 import com.heneryh.aquanotes.configure.ConfigurePrefs;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract;
 import com.heneryh.aquanotes.ui.controllers.ControllersActivity;
 import com.heneryh.aquanotes.ui.feed.FeedActivity;
 import com.heneryh.aquanotes.ui.livestock.LivestockActivity;
 import com.heneryh.aquanotes.util.AnalyticsUtils;
 import com.heneryh.aquanotes.util.UIUtils;
 
 import android.appwidget.AppWidgetManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class DashboardFragment extends Fragment {
 
     public void fireTrackerEvent(String label) {
         AnalyticsUtils.getInstance(getActivity()).trackEvent(
                 "Home Screen Dashboard", "Click", label, 0);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View root = inflater.inflate(R.layout.fragment_dashboard, container);
 
         // Attach event handlers
 
         root.findViewById(R.id.home_btn_controllers).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 fireTrackerEvent("Controllers");
                 // Launch list of sessions and vendors the user has starred
                 startActivity(new Intent(getActivity(), ControllersActivity.class));                
             }
         });
 
//        root.findViewById(R.id.home_btn_livestock).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                fireTrackerEvent("Livestock");
//                // Launch list of sessions and vendors the user has starred
//                startActivity(new Intent(getActivity(), LivestockActivity.class));                
//            }
//        });
 
         root.findViewById(R.id.home_btn_feed).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 fireTrackerEvent("Feed");
                 // Launch list of sessions and vendors the user has starred
                 startActivity(new Intent(getActivity(), FeedActivity.class));                
             }
         });
 
 
 
         root.findViewById(R.id.home_btn_prefs).setOnClickListener(
                 new View.OnClickListener() {
                     public void onClick(View view) {
         				Intent i = new Intent(getActivity(), ConfigurePrefs.class);
         				int controllerId = 999; // use the special case of widget=999 , this is not a good solution <-- no longer used!
         				i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, controllerId);
         				startActivity(i);
                     }
                 });
 
         return root;
     }
 }
