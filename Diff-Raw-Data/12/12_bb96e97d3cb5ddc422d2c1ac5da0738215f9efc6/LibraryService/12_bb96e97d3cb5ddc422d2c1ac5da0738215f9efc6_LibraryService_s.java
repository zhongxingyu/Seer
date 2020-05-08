 package ch.epfl.unison;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.util.Log;
 import ch.epfl.unison.api.JsonStruct;
 import ch.epfl.unison.api.Request;
 import ch.epfl.unison.api.UnisonAPI;
 
 public class LibraryService extends Service {
 
     private static final String TAG = "ch.epfl.unison.LibraryService";
     private static final int MIN_UPDATE_INTERVAL = 60 * 60 * 10;  // In seconds.
 
     public static final String ACTION_UPDATE = "ch.epfl.unison.action.UPDATE";
     public static final String ACTION_TRUNCATE = "ch.epfl.unison.action.TRUNCATE";
 
     private boolean isUpdating;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         String action = intent.getAction();
         if (action.equals(ACTION_UPDATE)) {
             this.update();
         } else if (action.equals(ACTION_TRUNCATE)){
             this.truncate();
         }
         return START_NOT_STICKY;
     }
 
     private void truncate() {
         LibraryHelper helper = new LibraryHelper(this);
         helper.truncate();
         helper.close();
     }
 
     private void update() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         // How many seconds elapsed since the last successful update ?
        int interval = ((int) System.currentTimeMillis() / 1000) - prefs.getInt("lastupdate", -1);
 
         if (!this.isUpdating && interval > MIN_UPDATE_INTERVAL) {
             this.isUpdating = true;
             LibraryHelper helper = new LibraryHelper(this);
             if (helper.isEmpty()) {
                 // If the DB is empty, just PUT all the tracks.
                 Log.d(TAG, "uploading all the music");
                 new Uploader().execute();
             } else {
                 Log.d(TAG, "updating the library");
                 new Updater().execute();
             }
         }
     }
 
     private abstract class LibraryTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected void onPostExecute(Boolean isSuccessful) {
             LibraryService.this.isUpdating = false;
 
             if (isSuccessful) {
                 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LibraryService.this);
                 SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("lastupdate", (int) System.currentTimeMillis() / 1000);
                 editor.commit();
             }
         }
 
         public Set<MusicItem> getRealMusic() {
             Set<MusicItem> set = new HashSet<MusicItem>();
             String[] columns = {
                     MediaStore.Audio.Media._ID,
                     MediaStore.Audio.Media.ARTIST,
                     MediaStore.Audio.Media.TITLE,
             };
             Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
             Cursor cur = LibraryService.this.getContentResolver().query(uri, columns,
                     MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
 
             if (cur != null && cur.moveToFirst()) {
                 int colId = cur.getColumnIndex(MediaStore.Audio.Media._ID);
                 int colArtist = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                 int colTitle = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                 do {
                     set.add(new MusicItem(cur.getInt(colId),
                             cur.getString(colArtist), cur.getString(colTitle)));
                 } while (cur.moveToNext());
             }
 
             cur.close();
             return set;
         }
     }
 
     private class Updater extends LibraryTask {
 
         @Override
         protected Boolean doInBackground(Void... arg0) {
             LibraryHelper helper = new LibraryHelper(LibraryService.this);
 
             // Setting up the expectations.
             Set<MusicItem> expectation = helper.getEntries();
             Log.d(TAG, "number of OUR entries: " + expectation.size());
 
             // Take a hard look at the reality.
             Set<MusicItem> reality = this.getRealMusic();
             Log.d(TAG, "number of TRUE music entries: " + reality.size());
 
             // Trying to reconcile everyone.
             List<JsonStruct.Delta> deltas = new ArrayList<JsonStruct.Delta>();
             for (MusicItem item : reality) {
                 if (!expectation.contains(item)) {
                     Log.d(TAG, "Adding track: " + item.title);
                     deltas.add(new JsonStruct.Delta(JsonStruct.Delta.TYPE_PUT,
                             item.localId, item.artist, item.title));  // Add item.
                 }
             }
             for (MusicItem item : expectation) {
                 if (!reality.contains(item)) {
                     Log.d(TAG, "Removing track: " + item.title);
                     deltas.add(new JsonStruct.Delta(JsonStruct.Delta.TYPE_DELETE,
                             item.localId, item.artist, item.title));  // Delete item.
                 }
             }
             Log.d(TAG, "number of deltas: " + deltas.size());
 
             // Sending the updates to the server.
             UnisonAPI api = AppData.getInstance(LibraryService.this).getAPI();
             long uid = AppData.getInstance(LibraryService.this).getUid();
 
             Request.Result<JsonStruct.Success> res = api.updateLibrarySync(uid, deltas);
             if (res.result == null) {
                 if (res.error.hasJsonError()) {
                     Log.w(TAG, "couldn't send deltas to server: " + res.error.jsonError.message);
                 } else {
                     Log.w(TAG, "couldn't send deltas to server.", res.error.error);
                 }
                 return false;
             }
 
             // "Commiting" the changes locally.
             for (JsonStruct.Delta delta : deltas) {
                 MusicItem item = new MusicItem(
                         delta.entry.localId, delta.entry.artist, delta.entry.title);
                 if (delta.type.equals(JsonStruct.Delta.TYPE_PUT)) {
                     helper.insert(item);
                 } else {  // TYPE_DELETE.
                     helper.delete(item);
                 }
             }
 
             helper.close();
             return true;
         }
     }
 
     private class Uploader extends LibraryTask {
 
         @Override
         protected Boolean doInBackground(Void... params) {
             List<JsonStruct.Track> tracks = new ArrayList<JsonStruct.Track>();
             Iterable<MusicItem> music = this.getRealMusic();
 
             for (MusicItem item : music) {
                 tracks.add(new JsonStruct.Track(item.localId, item.artist, item.title));
             }
 
             // Sending the updates to the server.
             UnisonAPI api = AppData.getInstance(LibraryService.this).getAPI();
             long uid = AppData.getInstance(LibraryService.this).getUid();
 
             Request.Result<JsonStruct.Success> res = api.uploadLibrarySync(uid, tracks);
             if (res.result == null) {
                 if (res.error.hasJsonError()) {
                     Log.w(TAG, "couldn't send tracks to server: " + res.error.jsonError.message);
                 } else {
                     Log.w(TAG, "couldn't send tracks to server.", res.error.error);
                 }
                 return false;
             }
 
             // Store the music in the library.
             LibraryHelper helper = new LibraryHelper(LibraryService.this);
             for (MusicItem item : music) {
                 helper.insert(item);
             }
 
             helper.close();
             return true;
         }
 
     }
 
 }
