 /*
  * Copyright (C) 2012 The Android Open Source Project
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
 package com.android.dreams.phototable;
 
 import android.content.SharedPreferences;
 import android.database.DataSetObserver;
 import android.app.ListActivity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListAdapter;
 
 import java.util.LinkedList;
 
 /**
  * Settings panel for photo flipping dream.
  */
 public class FlipperDreamSettings extends ListActivity {
     private static final String TAG = "FlipperDreamSettings";
     public static final String PREFS_NAME = FlipperDream.TAG;
 
     protected SharedPreferences mSettings;
 
     private PhotoSourcePlexor mPhotoSource;
     private SectionedAlbumDataAdapter mAdapter;
     private MenuItem mSelectAll;
 
     @Override
     protected void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         mSettings = getSharedPreferences(PREFS_NAME, 0);
         init();
     }
 
     protected void init() {
         mPhotoSource = new PhotoSourcePlexor(this, mSettings);
         setContentView(R.layout.settingslist);
 
         new AsyncTask<Void, Void, Void>() {
             @Override
             public Void doInBackground(Void... unused) {
                 mAdapter = new SectionedAlbumDataAdapter(FlipperDreamSettings.this,
                         mSettings,
                         R.layout.header,
                         R.layout.album,
                         new LinkedList<PhotoSource.AlbumData>(mPhotoSource.findAlbums()));
                 return null;
             }
 
            @Override
            public void onPostExecute(Void unused) {
                mAdapter.registerDataSetObserver(new DataSetObserver () {
                        @Override
                        public void onChanged() {
                            updateActionItem();
                        }
                        @Override
                        public void onInvalidated() {
                            updateActionItem();
                        }
                    });
                setListAdapter(mAdapter);
               getListView().setItemsCanFocus(true);
                updateActionItem();
                if (mAdapter.getCount() == 0) {
                    findViewById(android.R.id.empty).setVisibility(View.GONE);
                }
            }
         }.execute();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.photodream_settings_menu, menu);
         mSelectAll = menu.findItem(R.id.photodream_menu_all);
         updateActionItem();
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.photodream_menu_all:
             mAdapter.selectAll(!mAdapter.areAllSelected());
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void updateActionItem() {
         if (mAdapter != null && mSelectAll != null) {
             if (mAdapter.areAllSelected()) {
                 mSelectAll.setTitle(R.string.photodream_select_none);
             } else {
                 mSelectAll.setTitle(R.string.photodream_select_all);
             }
         }
     }
 }
