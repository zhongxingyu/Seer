 /**
  *
  */
 package aeglos.saytune.services;
 
 import static android.speech.tts.TextToSpeech.QUEUE_ADD;
 
 import java.util.Locale;
 
 import aeglos.android.common.Logger;
 import aeglos.android.common.ServiceMan;
 import aeglos.android.common.ServiceMan.Closure;
 import aeglos.saytune.R;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.ConditionVariable;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 
 import com.android.music.IMediaPlaybackService;
 
 /**
  * @author plalloni
  */
 public class SpeakerService extends Service {
 
     private static final Logger log = Logger.get(SpeakerService.class);
 
     private boolean enabled;
 
     private TextToSpeech textToSpeech;
 
     private ConditionVariable textToSpeechInitialized;
 
     private OnSharedPreferenceChangeListener preferencesListener;
 
     private ServiceMan<IMediaPlaybackService> mediaPlaybackServiceManager;
 
     private SharedPreferences preferences;
 
     private boolean defaultLanguage;
 
     private String language;
 
     private String lastTune;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null; // not exported
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         log.info("onCreate");
 
         mediaPlaybackServiceManager = new ServiceMan<IMediaPlaybackService>(this, new Intent().setClassName(
             "com.android.music", "com.android.music.MediaPlaybackService")) {};
 
         final String SPEAKING_KEY = getString(R.string.Speaking);
         final String DEFAULT_LANGUAGE_KEY = getString(R.string.DefaultLanguage);
         final String LANGUAGE_KEY = getString(R.string.SpeakingLanguage);
 
         preferencesListener = new OnSharedPreferenceChangeListener() {
             public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                 log.info("onSharedPreferenceChanged Key: %s", key);
                 if (SPEAKING_KEY.equals(key)) {
                     enabled = preferences.getBoolean(SPEAKING_KEY, true);
                 } else if (DEFAULT_LANGUAGE_KEY.equals(key)) {
                     defaultLanguage = preferences.getBoolean(DEFAULT_LANGUAGE_KEY, true);
                 } else if (LANGUAGE_KEY.equals(key)) {
                     language = preferences.getString(LANGUAGE_KEY, "eng");
                 }
             }
         };
 
         preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         preferences.registerOnSharedPreferenceChangeListener(preferencesListener);
 
         enabled = preferences.getBoolean(SPEAKING_KEY, true);
         defaultLanguage = preferences.getBoolean(DEFAULT_LANGUAGE_KEY, true);
        language = preferences.getString(LANGUAGE_KEY, "eng");
 
         textToSpeechInitialized = new ConditionVariable(false);
         textToSpeech = new TextToSpeech(getBaseContext(), new OnInitListener() {
             @Override
             public void onInit(int status) {
                 log.info("TTS initialization code: %s", status);
                 textToSpeechInitialized.open();
             }
         });
     }
 
     @Override
     public void onStart(final Intent intent, int startId) {
         super.onStart(intent, startId);
         log.info("onStart intent: %s startId: %s", intent, startId);
         if (enabled) {
             if (!mediaPlaybackServiceManager.call(new Closure<IMediaPlaybackService>() {
                 public void with(Context context, IMediaPlaybackService mediaPlayback) throws RemoteException {
                     if (lastTune == null || !lastTune.equals(mediaPlayback.getPath())) {
                         if (textToSpeechInitialized.block(10000)) {
                             if (defaultLanguage) {
                                 if (!Locale.getDefault().equals(textToSpeech.getLanguage())) {
                                     textToSpeech.setLanguage(Locale.getDefault());
                                 }
                             } else {
                                 Locale custom = new Locale(language);
                                 if (!custom.equals(textToSpeech.getLanguage())) {
                                     textToSpeech.setLanguage(custom);
                                 }
                             }
                             if (mediaPlayback.isPlaying()) {
                                 textToSpeech.speak(mediaPlayback.getTrackName(), QUEUE_ADD, null);
                                 lastTune = mediaPlayback.getPath();
                             } else {
                                 log.info("Music player not playing");
                             }
                         } else {
                             log.info("Timed out waiting for TTS initialization");
                         }
                     } else {
                         log.info("Not repeating tune");
                     }
                 }
             })) {
                 log.error("Unable to bind media playback service");
             }
         } else {
             log.info("Disabled");
         }
     }
 
     @Override
     public void onDestroy() {
         log.info("onDestroy");
         textToSpeech.shutdown();
         mediaPlaybackServiceManager.unbindService();
         preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener);
         super.onDestroy();
     }
 
 }
