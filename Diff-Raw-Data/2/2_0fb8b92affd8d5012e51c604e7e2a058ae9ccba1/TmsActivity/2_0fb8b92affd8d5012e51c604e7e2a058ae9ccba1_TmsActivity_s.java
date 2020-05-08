 
 package org.inftel.tms.mobile.ui;
 
 import static java.text.DateFormat.SHORT;
 import static org.inftel.tms.mobile.pasos.PasosMessage.buildUserAlarm;
 
 import java.text.DateFormat;
 import java.util.Locale;
 
 import org.inftel.tms.mobile.R;
 import org.inftel.tms.mobile.TmsConstants;
 import org.inftel.tms.mobile.pasos.PasosMessage;
 import org.inftel.tms.mobile.pasos.PasosMessage.Builder;
 import org.inftel.tms.mobile.services.SendPasosMessageIntentService;
 import org.inftel.tms.mobile.util.PlatformSpecificImplementationFactory;
 import org.inftel.tms.mobile.util.base.ILastLocationFinder;
 import org.inftel.tms.mobile.util.base.SharedPreferenceSaver;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class TmsActivity extends Activity {
 
     private static final String TAG = "TmsActivity";
     public static final String CUSTOM_INTENT = "org.inftel.tms.mobile.intent.action.RUN_ONCE";
 
     static final int PICK_CONTACT_REQUEST = 0;
 
     protected PendingIntent singleUpatePI;
     protected LocationManager locationManager;
     protected LocationListener locationListener;
     protected Criteria criteria;
     protected DateFormat dateFormat = java.text.DateFormat.getDateInstance(SHORT, Locale.FRANCE);
 
     private TextView text;
     private ScrollView scroll;
 
     /* Preferences */
     private SharedPreferences prefs;
     private Editor prefsEditor;
     private SharedPreferenceSaver preferenceSaver;
 
     private ILastLocationFinder lastLocationFinder;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // Requerido para coger las preferencias por defecto definidas en
         // /xml/preferences.xml
         PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
 
         // Get a reference to the Shared Preferences and a Shared Preference
         // Editor.
         prefs = getSharedPreferences(TmsConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
         prefsEditor = prefs.edit();
         // Instantiate a SharedPreferenceSaver class based on the available
         // platform version.
         preferenceSaver = PlatformSpecificImplementationFactory.getSharedPreferenceSaver(this);
 
         // Save that we've been run once.
         if (!prefs.getBoolean(TmsConstants.SP_KEY_RUN_ONCE, false)) {
             prefsEditor.putBoolean(TmsConstants.SP_KEY_RUN_ONCE, true);
             preferenceSaver.savePreferences(prefsEditor, false);
             Intent i = new Intent();
             i.setAction(CUSTOM_INTENT);
             getApplicationContext().sendBroadcast(i);
         }
 
         // Configura location finder
         lastLocationFinder = PlatformSpecificImplementationFactory.getLastLocationFinder(this);
 
         // Hook up button presses to the appropriate event handler.
         ((ImageView) findViewById(R.id.buttonUserAlarm)).setOnClickListener(mUserAlarm);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.mainMenuFences:
                 startActivity(new Intent(TmsActivity.this, FencesActivity.class));
                 return true;
             case R.id.mainMenuPreferences:
                 startActivity(new Intent(TmsActivity.this, PreferencesActivity.class));
                 return true;
             case R.id.mainMenuAbout:
                 startActivity(new Intent(TmsActivity.this, AboutActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public void append(CharSequence line) {
         text.append(line);
         text.append("\n");
         scroll.post(new Runnable() {
             public void run() {
                 scroll.smoothScrollTo(0, text.getBottom());
             }
         });
     }
 
     OnClickListener mUserAlarm = new OnClickListener() {
         public void onClick(View view) {
             // Obtener ultima posicion y construir PaSOS message
             Location location = lastLocationFinder.getLastBestLocation(
                    TmsConstants.MAX_DISTANCE, TmsConstants.MAX_TIME);
             Builder messageBuilder = buildUserAlarm().cause("click: solicitada asistencia");
 
             if (location != null) {
                 messageBuilder.location(location.getLatitude(), location.getLongitude());
             } else {
                 Log.w(TAG, "No se ha podido obtener localizacion al enviar una alerta de usuario");
             }
             PasosMessage message = messageBuilder.build();
 
             // Configurar y Lanzar servicio de envio de mensajes PaSOS
             Intent sendService = new Intent(view.getContext(), SendPasosMessageIntentService.class);
             sendService.putExtra(TmsConstants.EXTRA_KEY_MESSAGE_CONTENT, message.toString());
             startService(sendService);
         }
     };
 
 }
