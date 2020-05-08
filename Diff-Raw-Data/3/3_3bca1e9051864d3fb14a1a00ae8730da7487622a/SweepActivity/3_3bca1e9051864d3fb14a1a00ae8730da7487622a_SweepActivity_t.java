 /*
  * Copyright 2010 Roger Kapsi
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package org.ardverk.sweeper;
 
 import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
 import static android.provider.MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 
 public class SweepActivity extends ListActivity {
 
     private static final String TAG 
         = SweepActivity.class.getName();
 
     private static final String[] ALL = { "*" };
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Bundle extras = getIntent().getExtras();
         
         boolean external = extras.getBoolean("external");
         boolean internal = extras.getBoolean("internal");
         
         List<String> messages = new ArrayList<String>();
         
         ProgressDialog dialog = ProgressDialog.show(this, null, 
                 getString(R.string.sweeping), true);
         try {
             if (external) {
                 purge(EXTERNAL_CONTENT_URI, messages);
             }
             
             if (internal) {
                 purge(INTERNAL_CONTENT_URI, messages);
             }
         } finally {
             dialog.dismiss();
         }
         
         if (messages.isEmpty()) {
             messages.add(getString(R.string.nothing));
         }
         
         ListAdapter adapter = new ArrayAdapter<String>(this, 
                 android.R.layout.simple_list_item_1, messages);
         setListAdapter(adapter);
     }
     
     private void purge(Uri contentUri, List<String> messages) {
         try {
            List<String> list = purge(contentUri);
             messages.addAll(list);
         } catch (SQLiteException err) {
             Log.e(TAG, "SQLiteException", err);
         }
     }
     
     private List<String> purge(Uri contentUri) throws SQLiteException {
         List<String> list = new ArrayList<String>();
         Cursor cursor = managedQuery(contentUri, ALL, null, null, null);
         try {
             if (cursor != null && cursor.moveToFirst()) {
                 ContentResolver contentResolver = getContentResolver();
                 do {
                     int playlistId = cursor.getInt(0);
                     String playlistPath = cursor.getString(1);
                     String playlistName = cursor.getString(2);
                     
                     if (StringUtils.isEmpty(playlistPath)) {
                         list.add(playlistName + " (" + playlistId + ")");
                         
                         Uri uri = ContentUris.withAppendedId(contentUri, playlistId);
                         contentResolver.delete(uri, null, null);
                     }
                 } while (cursor.moveToNext());
             }
         } finally {
             CursorUtils.close(cursor);
         }
         
         return list;
     }
 }
