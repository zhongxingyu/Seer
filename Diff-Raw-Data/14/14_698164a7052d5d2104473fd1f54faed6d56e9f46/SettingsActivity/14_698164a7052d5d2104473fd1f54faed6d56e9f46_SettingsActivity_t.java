 
 package se.slide.timy;
 
 import android.accounts.AccountManager;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.preference.RingtonePreference;
 import android.support.v4.app.NavUtils;
 import android.text.TextUtils;
 import android.view.MenuItem;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
 import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.calendar.CalendarScopes;
 import com.google.api.services.calendar.model.CalendarList;
 import com.google.api.services.calendar.model.CalendarListEntry;
 import com.google.api.services.calendar.model.ColorDefinition;
 import com.google.api.services.calendar.model.Colors;
 
 import se.slide.timy.db.DatabaseManager;
 import se.slide.timy.model.Color;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * A {@link PreferenceActivity} that presents a set of application settings. On
  * handset devices, settings are presented as a single list. On tablets,
  * settings are split by category, with category headers shown to the left of
  * the list of settings.
  * <p>
  * See <a href="http://developer.android.com/design/patterns/settings.html">
  * Android Design: Settings</a> for design guidelines and the <a
  * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
  * API Guide</a> for more information on developing a Settings UI.
  */
 public class SettingsActivity extends PreferenceActivity {
     /**
      * Determines whether to always show the simplified settings UI, where
      * settings are presented in a single list. When false, settings are shown
      * as a master/detail two-pane view on tablets. When true, a single pane is
      * shown on tablets.
      */
     private static final boolean ALWAYS_SIMPLE_PREFS = false;
     
     static final int REQUEST_ACCOUNT_PICKER = 2;
     
     ProgressDialog mProgress;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setupActionBar();
     }
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 // TODO: If Settings has multiple levels, Up should navigate up
                 // that hierarchy.
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
         setupSimplePreferencesScreen();
     }
 
     /**
      * Shows the simplified settings UI if the device configuration if the
      * device configuration dictates that a simplified, single-pane UI should be
      * shown.
      */
     @SuppressWarnings("deprecation")
     private void setupSimplePreferencesScreen() {
         if (!isSimplePreferences(this)) {
             return;
         }
 
         // In the simplified UI, fragments are not used at all and we instead
         // use the older PreferenceActivity APIs.
 
         PreferenceCategory fakeHeader;
         
         // Add 'general' preferences.
         addPreferencesFromResource(R.xml.pref_general);
         
         fakeHeader = new PreferenceCategory(this);
         fakeHeader.setTitle(R.string.pref_header_sync);
         getPreferenceScreen().addPreference(fakeHeader);
         addPreferencesFromResource(R.xml.pref_data_sync);
         
         fakeHeader = new PreferenceCategory(this);
         fakeHeader.setTitle(R.string.pref_header_about);
         getPreferenceScreen().addPreference(fakeHeader);
         addPreferencesFromResource(R.xml.pref_about);
         
         
         // Reminder
         Preference remindMe = findPreference("remind_me");
         remindMe.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
             
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 boolean checked = (Boolean) newValue;
                 
                 Context context = preference.getContext();
                 
                 if (checked)
                     sendBroadcast(new Intent(context, BootReceiver.class));
                 else {
                     AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                     Intent i = new Intent(context, AlarmReceiver.class);
                     PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
                     mgr.cancel(pi);
                 }
                 
                 return true;
             }
         });
         
         // Remind me at
         Preference remindMeAt = findPreference("remind_me_at");
         remindMeAt.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
             
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 String stringValue = getFormattedTime(newValue.toString());
                 preference.setSummary(stringValue);
                 
                 sendBroadcast(new Intent(preference.getContext(), BootReceiver.class));
                 
                 return true;
             }
         });
         
         String remindMeAtSummary = PreferenceManager
                 .getDefaultSharedPreferences(this)
                 .getString(remindMeAt.getKey(), "");
         
         if (remindMeAtSummary != null && remindMeAtSummary.length() > 0) {
             remindMeAt.setSummary(getFormattedTime(remindMeAtSummary));
         }
         
         // Remind me at
         Preference remindMeWhen = findPreference("remind_me_when");
         remindMeWhen.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
             
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 @SuppressWarnings("unchecked")
                 String value = getFormattedDays((Set<String>) newValue);
                 preference.setSummary(value);
                 
                 return true;
             }
         });
         
         Set<String> values = PreferenceManager
                 .getDefaultSharedPreferences(this)
                 .getStringSet(remindMeWhen.getKey(), new HashSet<String>());
         
         String remindMeWhenSummary = getFormattedDays(values);
         remindMeWhen.setSummary(remindMeWhenSummary);
         
         // Account
         Preference syncGoogleAccountPref = findPreference("sync_google_calendar_account");
         syncGoogleAccountPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 String accountName = PreferenceManager
                         .getDefaultSharedPreferences(preference.getContext())
                         .getString(preference.getKey(), "");
                 
                 GoogleAccountCredential credential;
                 credential = GoogleAccountCredential.usingOAuth2(preference.getContext(), CalendarScopes.CALENDAR);
                 credential.setSelectedAccountName(accountName);
                 startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                 return false;
             }
         });
         
         
         String accountName = PreferenceManager
             .getDefaultSharedPreferences(this)
             .getString(syncGoogleAccountPref.getKey(), "");
         syncGoogleAccountPref.setSummary(accountName);
         
         // Google Calendar
         Preference syncGoogleCalendarPref = findPreference("sync_google_calendar_calendar");
         syncGoogleCalendarPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 String accountName = PreferenceManager
                         .getDefaultSharedPreferences(preference.getContext())
                         .getString("sync_google_calendar_account", "");
                 
                 if (accountName == null || accountName.length() < 1) {
                     final Preference accountPref = findPreference("sync_google_calendar_account");
                     accountPref.getOnPreferenceClickListener().onPreferenceClick(accountPref);
                     
                     return true;
                 }
                 
                 getCalendarData(accountName);
                 
                 return true;
             }
         });
         
         String calendarName = PreferenceManager
                 .getDefaultSharedPreferences(this)
                 .getString("sync_google_calendar_calendar_name", "");
         syncGoogleCalendarPref.setSummary(calendarName);
     }
     
     @SuppressLint("SimpleDateFormat")
     private String getFormattedTime(String prefTime) {
         String[] pieces=prefTime.split(":");
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(pieces[0]));
         cal.add(Calendar.MINUTE, Integer.parseInt(pieces[1]));
 
         SimpleDateFormat format = new SimpleDateFormat("HH:mm");
         try {
             Date time = format.parse(prefTime);
            return android.text.format.DateFormat.getTimeFormat(this).format(time);
         } catch (ParseException e) {
             e.printStackTrace();
         }
         
         return "";
     }
     
     private String getFormattedDays(Set<String> values) {
         
         StringBuilder builder = new StringBuilder();
         if (values.contains("1"))
             builder.append("Mon");
         
         if (values.contains("2")) {
             if (builder.length() > 0)
                 builder.append(", ");
             builder.append("Tue");
         }
         if (values.contains("3")) {
             if (builder.length() > 0)
                 builder.append(", ");
             builder.append("Wed");
         }
         if (values.contains("4")) {
             if (builder.length() > 0)
                 builder.append(", ");
             builder.append("Thu");
         }
         if (values.contains("5")) {
             if (builder.length() > 0)
                 builder.append(", ");
             builder.append("Fri");
         }
         if (values.contains("6")) {
             if (builder.length() > 0)
                 builder.append(", ");
             builder.append("Sat");
         }
         if (values.contains("7")) {
             if (builder.length() > 0)
                 builder.append(", ");
             builder.append("Sun");
         }
         
         return builder.toString();
     }
     
     private class GetColorsAsyncTask extends AsyncTask<Void, Void, List<Color>> {
 
         private String accountName;
         private WeakReference<Activity> weakActivity;
         
         public GetColorsAsyncTask(String accountName, Activity activity) {
             this.accountName = accountName;
             weakActivity = new WeakReference<Activity>(activity);
         }
         
         /* (non-Javadoc)
          * @see android.os.AsyncTask#onPreExecute()
          */
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             
             Activity activity = weakActivity.get();
             if (activity != null) {
                 ((SettingsActivity) activity).showProgressDialog(true);    
             }
         }
         
         /* (non-Javadoc)
          * @see android.os.AsyncTask#doInBackground(Params[])
          */
         @Override
         protected List<Color> doInBackground(Void... params) {
             try {
                 final com.google.api.services.calendar.Calendar client;
                 final HttpTransport transport = AndroidHttp.newCompatibleTransport();
                 final JsonFactory jsonFactory = new GsonFactory();
                 
                 GoogleAccountCredential credential;
                 credential = GoogleAccountCredential.usingOAuth2(weakActivity.get(), CalendarScopes.CALENDAR);
                 credential.setSelectedAccountName(accountName);
                 // Calendar client
                 client = new com.google.api.services.calendar.Calendar.Builder(
                     transport, jsonFactory, credential).setApplicationName("Timy/1.0")
                     .build();
                 
                 // Fire the request
                 Colors eventColors = client.colors().get().execute();
                 Set<Entry<String, ColorDefinition>> sets = eventColors.getEvent().entrySet();
                 
                 List<Color> colors = new ArrayList<Color>();
                 for (Entry<String, ColorDefinition> entry : sets) {
                     Color color = new Color();
                     color.setId(entry.getKey());
                     
                     ColorDefinition colorDefinitions = entry.getValue();
                     color.setBackgroundColor(colorDefinitions.getBackground());
                     color.setForegroundColor(colorDefinitions.getForeground());
                     
                     colors.add(color);
                 }
                                 
                 return colors;
             } catch (UserRecoverableAuthIOException e) {
                 startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
             
             return null;
         }
 
         /* (non-Javadoc)
          * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
          */
         @Override
         protected void onPostExecute(List<Color> result) {
             super.onPostExecute(result);
             
             Activity activity = weakActivity.get();
             if (activity != null && result != null) {
                 ((SettingsActivity) activity).saveColors(result);
                 ((SettingsActivity) activity).getCalendars(accountName);
             }
         }
         
     }
     
     public void showProgressDialog(boolean show) {
         if (show) {
             mProgress = ProgressDialog.show(this, getString(R.string.please_wait), getString(R.string.getting_calendar_info), true);
         }
         else {
             if (mProgress != null && mProgress.isShowing())
                 mProgress.dismiss();
         }
     }
     
     public void saveColors(List<Color> colors) {
         DatabaseManager.getInstance().saveColors(colors);
     }
     
     private class GetCalendarsAsyncTask extends AsyncTask<Void, Void, CalendarList> {
         
         private String accountName;
         private WeakReference<Activity> weakActivity;
         
         public GetCalendarsAsyncTask(String accountName, Activity activity) {
             this.accountName = accountName;
             weakActivity = new WeakReference<Activity>(activity);
         }
         
         @Override
         protected CalendarList doInBackground(Void... urls) {
             
             try {
                 final com.google.api.services.calendar.Calendar client;
                 final HttpTransport transport = AndroidHttp.newCompatibleTransport();
                 final JsonFactory jsonFactory = new GsonFactory();
                 
                 GoogleAccountCredential credential;
                 credential = GoogleAccountCredential.usingOAuth2(weakActivity.get(), CalendarScopes.CALENDAR);
                 credential.setSelectedAccountName(accountName);
                 // Calendar client
                 client = new com.google.api.services.calendar.Calendar.Builder(
                     transport, jsonFactory, credential).setApplicationName("Timy/1.0")
                     .build();
                 
                 // Fire the request
                 String FIELDS = "id,summary";
                 final String FEED_FIELDS = "items(" + FIELDS + ")";
                 CalendarList feed = client.calendarList().list().setFields(FEED_FIELDS).execute();
                 
                 
                 
                 
                 return feed;
             } catch (UserRecoverableAuthIOException e) {
                 startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
             
             return null;
         }
 
         /* (non-Javadoc)
          * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
          */
         @Override
         protected void onPostExecute(CalendarList result) {
             super.onPostExecute(result);
             
             Activity activity = weakActivity.get();
             if (activity != null && result != null) {
                 ((SettingsActivity) activity).showProgressDialog(false);
                 ((SettingsActivity) activity).displayCalendarList(result);
             }
         }
         
     }
     
     public void getCalendarData(String accountName) {
         GetColorsAsyncTask getColors = new GetColorsAsyncTask(accountName, SettingsActivity.this);
         getColors.execute();
         
     }
     
     public void getCalendars(String accountName) {
         GetCalendarsAsyncTask getCalendars = new GetCalendarsAsyncTask(accountName, SettingsActivity.this);
         getCalendars.execute();
     }
     
     public void displayCalendarList(CalendarList feed) {
         List<CalendarListEntry> list = feed.getItems();
         
         final CharSequence[] entries = new CharSequence[list.size()];
         final String[] calendarIds = new String[list.size()];
         for (int i = 0; i < list.size(); i++) {
             entries[i] = list.get(i).getSummary();
             calendarIds[i] = list.get(i).getId();
         }
 
         int calendarId = PreferenceManager
                 .getDefaultSharedPreferences(this)
                 .getInt("sync_google_calendar_calendar", 0);
         
         if (calendarId > entries.length)
             calendarId = 0;
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Choose a calendar")
             .setSingleChoiceItems(entries, calendarId, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialogInterface, int item) {
                     PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putString("sync_google_calendar_calendar_name", entries[item].toString()).commit();
                     PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putString("sync_google_calendar_calendar_id", calendarIds[item]).commit();
                     PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putInt("sync_google_calendar_calendar", item).commit();
                     Preference syncGoogleCalendarPref = findPreference("sync_google_calendar_calendar");
                     syncGoogleCalendarPref.setSummary(entries[item]);
                     dialogInterface.dismiss();
                 }
             });
 
         builder.create().show();
         
     }
     
 
     /* (non-Javadoc)
      * @see android.preference.PreferenceActivity#onActivityResult(int, int, android.content.Intent)
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
         if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
             String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
             
             Preference syncGoogleAccountPref = findPreference("sync_google_calendar_account");
             syncGoogleAccountPref.getEditor().putString("sync_google_calendar_account", accountName).commit();
             syncGoogleAccountPref.setSummary(accountName);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onIsMultiPane() {
         return isXLargeTablet(this) && !isSimplePreferences(this);
     }
 
     /**
      * Helper method to determine if the device has an extra-large screen. For
      * example, 10" tablets are extra-large.
      */
     private static boolean isXLargeTablet(Context context) {
         return (context.getResources().getConfiguration().screenLayout
         & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
     }
 
     /**
      * Determines whether the simplified settings UI should be shown. This is
      * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
      * doesn't have newer APIs like {@link PreferenceFragment}, or the device
      * doesn't have an extra-large screen. In these cases, a single-pane
      * "simplified" settings UI should be shown.
      */
     private static boolean isSimplePreferences(Context context) {
         return ALWAYS_SIMPLE_PREFS
                 || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                 || !isXLargeTablet(context);
     }
 
     /** {@inheritDoc} */
     @Override
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public void onBuildHeaders(List<Header> target) {
         if (!isSimplePreferences(this)) {
             loadHeadersFromResource(R.xml.pref_headers, target);
         }
     }
 
     /**
      * A preference value change listener that updates the preference's summary
      * to reflect its new value.
      */
     private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object value) {
             String stringValue = value.toString();
 
             if (preference instanceof ListPreference) {
                 // For list preferences, look up the correct display value in
                 // the preference's 'entries' list.
                 ListPreference listPreference = (ListPreference) preference;
                 int index = listPreference.findIndexOfValue(stringValue);
 
                 // Set the summary to reflect the new value.
                 preference.setSummary(
                         index >= 0
                                 ? listPreference.getEntries()[index]
                                 : null);
 
             } else if (preference instanceof RingtonePreference) {
                 // For ringtone preferences, look up the correct display value
                 // using RingtoneManager.
                 if (TextUtils.isEmpty(stringValue)) {
                     // Empty values correspond to 'silent' (no ringtone).
                     //preference.setSummary(R.string.pref_ringtone_silent);
 
                 } else {
                     Ringtone ringtone = RingtoneManager.getRingtone(
                             preference.getContext(), Uri.parse(stringValue));
 
                     if (ringtone == null) {
                         // Clear the summary if there was a lookup error.
                         preference.setSummary(null);
                     } else {
                         // Set the summary to reflect the new ringtone display
                         // name.
                         String name = ringtone.getTitle(preference.getContext());
                         preference.setSummary(name);
                     }
                 }
 
             } else {
                 // For all other preferences, set the summary to the value's
                 // simple string representation.
                 preference.setSummary(stringValue);
             }
             return true;
         }
     };
 
     /**
      * Binds a preference's summary to its value. More specifically, when the
      * preference's value is changed, its summary (line of text below the
      * preference title) is updated to reflect the value. The summary is also
      * immediately updated upon calling this method. The exact display format is
      * dependent on the type of preference.
      * 
      * @see #sBindPreferenceSummaryToValueListener
      */
     private static void bindPreferenceSummaryToValue(Preference preference) {
         // Set the listener to watch for value changes.
         preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 
         // Trigger the listener immediately with the preference's
         // current value.
         sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                 PreferenceManager
                         .getDefaultSharedPreferences(preference.getContext())
                         .getString(preference.getKey(), ""));
     }
 
     /**
      * This fragment shows general preferences only. It is used when the
      * activity is showing a two-pane settings UI.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public static class GeneralPreferenceFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.pref_general);
 
             // Bind the summaries of EditText/List/Dialog/Ringtone preferences
             // to their values. When their values change, their summaries are
             // updated to reflect the new value, per the Android Design
             // guidelines.
             
             //bindPreferenceSummaryToValue(findPreference("example_text"));
             //bindPreferenceSummaryToValue(findPreference("example_list"));
         }
     }
 
     /**
      * This fragment shows notification preferences only. It is used when the
      * activity is showing a two-pane settings UI.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public static class NotificationPreferenceFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             //addPreferencesFromResource(R.xml.pref_notification);
 
             // Bind the summaries of EditText/List/Dialog/Ringtone preferences
             // to their values. When their values change, their summaries are
             // updated to reflect the new value, per the Android Design
             // guidelines.
             
             //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
         }
     }
 
     /**
      * This fragment shows data and sync preferences only. It is used when the
      * activity is showing a two-pane settings UI.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public static class DataSyncPreferenceFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.pref_data_sync);
 
             // Bind the summaries of EditText/List/Dialog/Ringtone preferences
             // to their values. When their values change, their summaries are
             // updated to reflect the new value, per the Android Design
             // guidelines.
             
             //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
         }
     }
 }
