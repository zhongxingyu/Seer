 package com.ouchadam.fang.audio;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.Uri;
 
 import com.ouchadam.fang.domain.PodcastPosition;
 import com.ouchadam.fang.persistance.PositionPersister;
 
 class PlayingItemStateManager {
 
     private final ContentResolver contentResolver;
     private final SharedPreferences preferences;
 
     public static PlayingItemStateManager from(Context context) {
         SharedPreferences preferences = context.getSharedPreferences(AudioService.class.getSimpleName(), Context.MODE_PRIVATE);
         ContentResolver contentResolver = context.getContentResolver();
         return new PlayingItemStateManager(contentResolver, preferences);
     }
 
     PlayingItemStateManager(ContentResolver contentResolver, SharedPreferences preferences) {
         this.contentResolver = contentResolver;
         this.preferences = preferences;
     }
 
     public void persist(long itemId, PodcastPosition position, Uri source) {
         if (itemId != PlayerHandler.MISSING_ID) {
             persistPosition(itemId, position);
             persistId(itemId);
             persistSource(source);
         }
     }
 
     private void persistPosition(long itemId, PodcastPosition position) {
         new PositionPersister(itemId, contentResolver).persist(position);
     }
 
     private void persistSource(Uri uri) {
         preferences.edit().putString("source", uri.toString()).apply();
     }
 
     public Uri getSource() {
         String source = preferences.getString("source", "");
         if (!source.isEmpty()) {
             return Uri.parse(source);
         }
         return null;
     }
 
     private void persistId(long itemId) {
         preferences.edit().putLong("id", itemId).apply();
     }
 
     public long getStoredPlayingId() {
         return preferences.getLong("id", PlayerHandler.MISSING_ID);
     }
 }
