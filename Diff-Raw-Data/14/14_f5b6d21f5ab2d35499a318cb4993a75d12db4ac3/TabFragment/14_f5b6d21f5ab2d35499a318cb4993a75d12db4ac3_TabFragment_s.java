 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
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
 package com.meiste.greg.ptw;
 
import com.meiste.greg.ptw.TabFragmentAdapter.FragmentListener;

 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 
 public class TabFragment extends Fragment {
     private static final String KEY_TITLE = "TabFragment:Title";
     private String mTitle = "???";
     private FragmentListener mFragmentListener;
 
     public void setTitle(String title) {
         mTitle = title;
     }
 
     public String getTitle() {
         return mTitle;
     }
 
     public void setFragmentListener(FragmentListener fl) {
         mFragmentListener = fl;
     }
 
     protected void notifyChanged() {
         try {
             if (mFragmentListener != null)
                 mFragmentListener.onChangedFragment();
         } catch (Exception e) {
             Util.log("Failed to notifyChanged! - " + e);
         }
     }
 
     public boolean isChanged() {
         return false;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_TITLE)) {
             setTitle(savedInstanceState.getString(KEY_TITLE));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString(KEY_TITLE, getTitle());
     }
 }
