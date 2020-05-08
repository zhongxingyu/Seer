 /*   
  * Copyright 2013 Karsten Patzwaldt
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.muckebox.android.net;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
import javax.net.ssl.SSLException;

 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.muckebox.android.Muckebox;
 import org.muckebox.android.R;
 import org.muckebox.android.db.MuckeboxProvider;
 import org.muckebox.android.db.MuckeboxContract.AlbumEntry;
 import org.muckebox.android.db.MuckeboxContract.ArtistEntry;
 import org.muckebox.android.db.MuckeboxContract.TrackEntry;
 
 import android.content.ContentProviderOperation;
 import android.content.ContentValues;
 import android.content.OperationApplicationException;
 import android.net.Uri;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class RefreshHelper {
     private final static String LOG_TAG = "RefreshHelper";
     
     public static Integer refreshTracks(long albumId) {
         try {
             ArrayList<ContentProviderOperation> operations =
                     new ArrayList<ContentProviderOperation>(1);
 
             JSONArray json = NetHelper.callApi("tracks", null,
                     new String[] { "album" },
                     new String[] { Long.toString(albumId) });
             operations.ensureCapacity(operations.size() + json.length() + 1);
             
             operations.add(ContentProviderOperation.newDelete(
                     Uri.withAppendedPath(MuckeboxProvider.URI_TRACKS_ALBUM,
                         Long.toString(albumId))).build());
 
             for (int j = 0; j < json.length(); ++j) {
                 JSONObject o = json.getJSONObject(j);
                 ContentValues values = new ContentValues();
                 
                 values.put(TrackEntry.SHORT_ID, o.getInt("id"));
                 values.put(TrackEntry.SHORT_ARTIST_ID,  o.getInt("artist_id"));
                 values.put(TrackEntry.SHORT_ALBUM_ID, o.getInt("album_id"));
                 
                 values.put(TrackEntry.SHORT_TITLE, o.getString("title"));
                 
                 if (! o.isNull("tracknumber"))
                     values.put(TrackEntry.SHORT_TRACKNUMBER, o.getInt("tracknumber"));
                 
                 if (! o.isNull("discnumber"))
                     values.put(TrackEntry.SHORT_DISCNUMBER, o.getInt("discnumber"));
                 
                 values.put(TrackEntry.SHORT_LABEL, o.getString("label"));
                 values.put(TrackEntry.SHORT_CATALOGNUMBER, o.getString("catalognumber"));
                 
                 values.put(TrackEntry.SHORT_LENGTH, o.getInt("length"));
                 values.put(TrackEntry.SHORT_DATE, o.getString("date"));
                 
                 operations.add(ContentProviderOperation.newDelete(
                     Uri.withAppendedPath(MuckeboxProvider.URI_TRACKS,
                         Integer.toString(o.getInt("id")))).build());
                 operations.add(ContentProviderOperation.newInsert(MuckeboxProvider.URI_TRACKS).
                         withValues(values).build());
             }
 
             Log.d(LOG_TAG, "Got " + json.length() + " Tracks");
             
             Muckebox.getAppContext().getContentResolver().applyBatch(
                     MuckeboxProvider.AUTHORITY, operations);
        } catch (SSLException e) {
            return R.string.error_ssl;
         } catch (IOException e) {
             Log.d(LOG_TAG, "IOException: " + e.getMessage());
             return R.string.error_reload_tracks;
         } catch (JSONException e) {
             return R.string.error_json;
         } catch (RemoteException e) {
             e.printStackTrace();
             return R.string.error_reload_tracks;
         } catch (OperationApplicationException e) {
             e.printStackTrace();
             return R.string.error_reload_tracks;
         } 
         
         return null;
     }
     
     public static Integer refreshAlbums() {
         try {
             JSONArray json = NetHelper.callApi("albums");
             ArrayList<ContentProviderOperation> operations = 
                     new ArrayList<ContentProviderOperation>(json.length() + 1);
             
             operations.add(
                     ContentProviderOperation.newDelete(MuckeboxProvider.URI_ALBUMS).build());
             
             for (int i = 0; i < json.length(); ++i) {
                 JSONObject o = json.getJSONObject(i);
 
                 operations.add(
                         ContentProviderOperation.newInsert(MuckeboxProvider.URI_ALBUMS).
                             withValue(AlbumEntry.SHORT_ID, o.getInt("id")).
                             withValue(AlbumEntry.SHORT_TITLE, o.getString("title")).
                             withValue(AlbumEntry.SHORT_ARTIST_ID, o.getInt("artist_id")).
                             withValue(AlbumEntry.SHORT_CREATED, o.getInt("created")).build());
             }
             
             Muckebox.getAppContext().getContentResolver().applyBatch(
                     MuckeboxProvider.AUTHORITY, operations);
     
             Log.d(LOG_TAG, "Got " + json.length() + " albums");
        } catch (SSLException e) {
            return R.string.error_ssl;
         } catch (IOException e) {
             Log.d(LOG_TAG, "IOException: " + e.getMessage());
             return R.string.error_reload_albums;
         } catch (JSONException e) {
             return R.string.error_json;
         } catch (RemoteException e) {
             e.printStackTrace();
             return R.string.error_reload_albums;
         } catch (OperationApplicationException e) {
             e.printStackTrace();
             return R.string.error_reload_albums;
         }
         
         return null;
     }
     
     public static Integer refreshArtists() {
         try {
             JSONArray json = NetHelper.callApi("artists");
             ArrayList<ContentProviderOperation> operations =
                     new ArrayList<ContentProviderOperation>(json.length() + 1);
             
             operations.add(ContentProviderOperation.newDelete(MuckeboxProvider.URI_ARTISTS).build());
             
             for (int i = 0; i < json.length(); ++i) {
                 JSONObject o = json.getJSONObject(i);
                 operations.add(ContentProviderOperation.newInsert(MuckeboxProvider.URI_ARTISTS).
                         withValue(ArtistEntry.SHORT_ID, o.getInt("id")).
                         withValue(ArtistEntry.SHORT_NAME, o.getString("name")).build());
             }
             
             Muckebox.getAppContext().getContentResolver().applyBatch(
                     MuckeboxProvider.AUTHORITY, operations);
        } catch (SSLException e) {
            return R.string.error_ssl;
         } catch (IOException e) {
             Log.d(LOG_TAG, "IOException: " + e.getMessage());
             return R.string.error_reload_artists;
         } catch (JSONException e) {
             return R.string.error_json;
         } catch (RemoteException e) {
             e.printStackTrace();
             return R.string.error_reload_artists;
         } catch (OperationApplicationException e) {
             e.printStackTrace();
             return R.string.error_reload_artists;
         }
         
         return null;
     }
 }
