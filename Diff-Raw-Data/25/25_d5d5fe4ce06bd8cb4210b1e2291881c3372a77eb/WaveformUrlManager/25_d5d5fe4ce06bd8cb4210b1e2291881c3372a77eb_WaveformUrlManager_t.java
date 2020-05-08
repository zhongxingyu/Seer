 package com.github.kaeppler.sawtoothtapestry;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.github.ignition.core.tasks.IgnitedAsyncTaskHandler;
 import com.github.kaeppler.sawtoothtapestry.api.GetTracksTask;
 import com.github.kaeppler.sawtoothtapestry.api.SoundCloudApi;
 import com.github.kaeppler.sawtoothtapestry.model.Track;
 import com.github.kaeppler.sawtoothtapestry.settings.SettingsKeys;
 
 /**
  * Responsible for dealing with waveform image URLs, such as writing and reading them from the
  * shared preferences or returning a random one to the caller.
  */
 public class WaveformUrlManager implements IgnitedAsyncTaskHandler<Context, Void, List<Track>> {
 
    public static final String DEFAULT_WAVEFORM_URL = "https://w1.sndcdn.com/Wh3yMggQ8r3l_m.png";
 
     private static final String TAG = WaveformUrlManager.class.getSimpleName();
     private static final String URL_DELIMITER = ";";
 
     private Context context;
     private SoundCloudApi api;
     private Handler handler;
     private SharedPreferences preferences;
     private ArrayList<String> waveformUrls;
 
     public WaveformUrlManager(Context context, SoundCloudApi api, Handler handler) {
         this.context = context;
         this.api = api;
         this.handler = handler;
 
         preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String waveformUrlsCSV = preferences.getString(SettingsKeys.SETTINGS_KEY_WAVEFORM_URLS,
                null);
        if (waveformUrlsCSV == null) {
            waveformUrls = new ArrayList<String>();
        } else {
            String[] urlArray = waveformUrlsCSV.split(URL_DELIMITER);
            waveformUrls = new ArrayList<String>(Arrays.asList(urlArray));
        }
     }
 
     public boolean areWaveformsAvailable() {
         return !waveformUrls.isEmpty();
     }
 
     public void fetchWaveformUrls() {
         GetTracksTask task = api.getFavoriteTracks();
         task.connect(this);
         task.execute();
 
     }
 
     public void updateWaveformUrls(List<Track> tracks) {
         waveformUrls = new ArrayList<String>(tracks.size());
         for (Track track : tracks) {
             waveformUrls.add(track.getWaveformUrl());
         }
 
         // unfortunately, putStringSet is not available below API level 11
         String waveformUrlsCSV = TextUtils.join(URL_DELIMITER, waveformUrls);
 
         Editor editor = preferences.edit();
         editor.putString(SettingsKeys.SETTINGS_KEY_WAVEFORM_URLS, waveformUrlsCSV);
         if (editor.commit()) {
             Log.d(TAG, "Stored " + waveformUrls.size() + " new waveform URLs");
         } else {
             Log.e(TAG, "Error storing waveform URLs");
         }
     }
 
     public String getRandomWaveformUrl() {
        int numWaveforms = waveformUrls.size();
        String url = null;
        if (numWaveforms == 1) {
            url = waveformUrls.get(0);
        } else if (numWaveforms > 1) {
            int index = new Random(System.currentTimeMillis()).nextInt(numWaveforms - 1);
            url = waveformUrls.get(index);
        }
 
         if (url == null) {
             url = DEFAULT_WAVEFORM_URL;
         }
 
         return url;
     }
 
     @Override
     public Context getContext() {
         return context;
     }
 
     @Override
     public void setContext(Context context) {
         this.context = context;
     }
 
     @Override
     public boolean onTaskStarted(Context context) {
         return true;
     }
     
     @Override
     public boolean onTaskCompleted(Context context, List<Track> tracks) {
         return true;
     }
 
     @Override
     public boolean onTaskFailed(Context context, Exception error) {
         SuperToast.error(context, error);
         return false;
     }
 
     @Override
     public boolean onTaskProgress(Context context, Void... progress) {
         return true;
     }
 
     @Override
     public boolean onTaskSuccess(Context context, List<Track> tracks) {
         Log.d(TAG, "New favorites fetched from API: " + tracks.size());
 
         updateWaveformUrls(tracks);
 
         handler.sendEmptyMessage(R.id.message_waveforms_available);
 
         return true;
     }
 
 }
