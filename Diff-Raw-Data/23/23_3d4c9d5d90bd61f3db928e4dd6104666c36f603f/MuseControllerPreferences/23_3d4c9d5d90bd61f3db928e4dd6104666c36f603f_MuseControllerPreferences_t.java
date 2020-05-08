 package com.sleazyweasel.applescriptifier.preferences;
 
 
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 public class MuseControllerPreferences {
     private static final String PIANOBAR_VETO_KEY = "pianobar.veto";
     private static final String SPOTIFY_ENABLE_KEY = "spotify.enable";
     private static final String MUSECONTROL_ENABLE_KEY = "musecontrol.enable";
     private static final String LAST_STREAMER_KEY = "last.streamer";
     private static final String PANDORA_STREAMER_VALUE = "Pandora";
     private static final String SPOTIFY_STREAMER_VALUE = "Spotify";
     private static final String PREVIOUS_PANDORA_STATION_ID_KEY = "PREVIOUS_PANDORA_STATION_ID_KEY";
     private static final String PREVIOUS_PANDORA_VOLUME_KEY = "PREVIOUS_PANDORA_VOLUME_KEY";
     private static final String PREVIOUS_SPOTIFY_VOLUME_KEY = "PREVIOUS_SPOTIFY_VOLUME_KEY";
 
     private final Preferences preferences;
 
     public MuseControllerPreferences(Preferences preferences) {
         this.preferences = preferences;
     }
 
     public boolean isPianoBarEnabled() {
         return !preferences.getBoolean(PIANOBAR_VETO_KEY, false);
     }
 
     public boolean isSpotifyEnabled() {
         return preferences.getBoolean(SPOTIFY_ENABLE_KEY, true);
     }
 
     public void enablePianoBar(boolean enable) {
         preferences.putBoolean(PIANOBAR_VETO_KEY, !enable);
     }
 
     public void enableMuseControl(boolean enable) {
         preferences.putBoolean(MUSECONTROL_ENABLE_KEY, enable);
     }
 
     public boolean isMuseControlEnabled() {
         return preferences.getBoolean(MUSECONTROL_ENABLE_KEY, true);
     }
 
     public void save() throws BackingStoreException {
         preferences.sync();
     }
 
     public boolean wasPandoraTheLastStreamerOpen() {
         String lastStreamer = preferences.get(LAST_STREAMER_KEY, null);
         //at the moment, Pandora is the default to start up with. In future, I should probably pop up a message on first startup of the app, to configure prefs and set this stuff up.
         return lastStreamer == null || lastStreamer.equals(PANDORA_STREAMER_VALUE);
     }
 
     public boolean wasSpotifyTheLastStreamerOpen() {
         String lastStreamer = preferences.get(LAST_STREAMER_KEY, null);
         return SPOTIFY_STREAMER_VALUE.equals(lastStreamer);
     }
 
     public void setPandoraAsStreamer() {
         preferences.put(LAST_STREAMER_KEY, PANDORA_STREAMER_VALUE);
     }
 
     public void enableSpotify(boolean enable) {
         preferences.putBoolean(SPOTIFY_ENABLE_KEY, enable);
     }
 
     public void setSpotifyAsStreamer() {
         preferences.put(LAST_STREAMER_KEY, SPOTIFY_STREAMER_VALUE);
     }
 
     public Long getPreviousPandoraStationId() {
         if (keyExists(PREVIOUS_PANDORA_STATION_ID_KEY)) {
             return preferences.getLong(PREVIOUS_PANDORA_STATION_ID_KEY, 0);
         }
         return null;
     }
 
     public void setPandoraStationId(Long stationId) {
        if (stationId == null) {
            preferences.remove(PREVIOUS_PANDORA_STATION_ID_KEY);
        }
        else {
            preferences.putLong(PREVIOUS_PANDORA_STATION_ID_KEY, stationId);
        }
     }
 
     public Double getPreviousPandoraVolume() {
         if (keyExists(PREVIOUS_PANDORA_VOLUME_KEY)) {
             return preferences.getDouble(PREVIOUS_PANDORA_VOLUME_KEY, 0);
         }
         return null;
     }
 
     public void setPandoraVolume(double volume) {
         preferences.putDouble(PREVIOUS_PANDORA_VOLUME_KEY, volume);
     }
 
     public Float getPreviousSpotifyVolume() {
         return preferences.getFloat(PREVIOUS_SPOTIFY_VOLUME_KEY, 1.0f);
     }
 
     public void setSpotifyVolume(float volume) {
         preferences.putFloat(PREVIOUS_SPOTIFY_VOLUME_KEY, volume);
     }
 
     private boolean keyExists(String keyToCheck) {
         try {
             String[] keys = preferences.keys();
             for (String key : keys) {
                 if (keyToCheck.equals(key)) {
                     return true;
                 }
             }
             return false;
         } catch (BackingStoreException e) {
             e.printStackTrace();
             return false;
         }
     }
 
 }
