 /*
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
 
 package com.infine.android.devoxx.ui;
 
 import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.infine.android.devoxx.R;
 import com.infine.android.devoxx.provider.ScheduleContract;
 import com.infine.android.devoxx.ui.phone.MapActivity;
 import com.infine.android.devoxx.ui.phone.ScheduleActivity;
 
 public class DashboardFragment extends Fragment {
 
     public void fireTrackerEvent(String label) {
 //        AnalyticsUtils.getInstance(getActivity()).trackEvent("Home Screen Dashboard", "Click", label, 0);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View root = inflater.inflate(R.layout.fragment_dashboard, container);
 
         // Attach event handlers
         root.findViewById(R.id.home_btn_schedule).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 fireTrackerEvent("Schedule");
 //                if (UIUtils.isHoneycombTablet(getActivity())) {
 //                    startActivity(new Intent(getActivity(), ScheduleMultiPaneActivity.class));
 //                } else {
                     startActivity(new Intent(getActivity(), ScheduleActivity.class));
 //                }
                 
             }
             
         });
 
         // bouton des session
         root.findViewById(R.id.home_btn_sessions).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 fireTrackerEvent("Sessions");
                 // Launch sessions list
 //                if (UIUtils.isHoneycombTablet(getActivity())) {
 //                    startActivity(new Intent(getActivity(), SessionsMultiPaneActivity.class));
 //                } else {
                     final Intent intent2 = new Intent(Intent.ACTION_VIEW);
                     intent2.setData(ScheduleContract.Sessions.CONTENT_URI);
                     startActivity(intent2);
 //                }
 
             }
         });
 
         // bouton favoris
         root.findViewById(R.id.home_btn_starred).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 fireTrackerEvent("Starred");
                 // Launch list of sessions and vendors the user has starred
                 startActivity(new Intent(getActivity(), StarredActivity.class));                
             }
         });
 
         // speaker button
         root.findViewById(R.id.home_btn_speakers).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 fireTrackerEvent("Speakers");
                 // Launch speaker list
 //                if (UIUtils.isHoneycombTablet(getActivity())) {
 //                    startActivity(new Intent(getActivity(), VendorsMultiPaneActivity.class));
 //                } else {
                     final Intent intent = new Intent(Intent.ACTION_VIEW,
                             ScheduleContract.Speakers.CONTENT_URI);
 //                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_speakers));
 //                    intent.putExtra(TracksFragment.EXTRA_NEXT_TYPE,
 //                            TracksFragment.NEXT_TYPE_VENDORS);
                     startActivity(intent);
 //                    startActivity(new Intent(getActivity(), SpeakersActivity.class));
 //                }
             }
         });
 
         root.findViewById(R.id.home_btn_map).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 // Launch map of conference venue
                 fireTrackerEvent("Map");
 //                startActivity(new Intent(getActivity(),
 //                        UIUtils.getMapActivityClass(getActivity())));
                 startActivity(new Intent(getActivity(),  MapActivity.class));
             }
         });
         
         root.findViewById(R.id.home_btn_twitter).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 // Launch map of conference venue
                 fireTrackerEvent("Twitter");
                 // Launch twitter activity

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=DevoxxFR")));
                }catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/DevoxxFR")));
                }
                //startActivity(new Intent(getActivity(), TwitterActivity.class));
             }
         });
 
         return root;
     }
 }
