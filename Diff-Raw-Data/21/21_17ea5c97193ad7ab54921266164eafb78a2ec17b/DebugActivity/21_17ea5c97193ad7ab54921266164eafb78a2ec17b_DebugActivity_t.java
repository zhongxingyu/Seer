 package com.strollimo.android.view;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceFragment;
 import android.util.Log;
 import android.widget.Toast;
 import com.strollimo.android.R;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.controller.AccomplishableController;
 import com.strollimo.android.controller.EnvSyncManager;
 import com.strollimo.android.model.*;
 import com.strollimo.android.network.StrollimoApi;
 import com.strollimo.android.network.response.*;
 import retrofit.Callback;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 
 import java.util.Map;
 
 
 public class DebugActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Display the fragment as the main content.
         getFragmentManager().beginTransaction().replace(android.R.id.content,
                new DebugFragment()).commit();
     }
 
    public static class DebugFragment extends PreferenceFragment {
         public static final String PREF_CURRENT_ENV = "pref_current_env";
         private StrollimoPreferences mPrefs;
         private AccomplishableController mAccomplishableController;
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             mPrefs = StrollimoApplication.getService(StrollimoPreferences.class);
             mAccomplishableController = StrollimoApplication.getService(AccomplishableController.class);
 
             // Load the preferences from an XML resource
             addPreferencesFromResource(R.xml.debug_prefs);
             final Preference changeEnvPref = findPreference(PREF_CURRENT_ENV);
             changeEnvPref.setSummary(mPrefs.getEnvTag());
             changeEnvPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference preference, Object newValue) {
                     changeEnvPref.setSummary((String)newValue);
                     mPrefs.setEnvTag((String)newValue);
                     syncData();
                     return true;
                 }
             });
 
             final Preference debugModePref = findPreference(StrollimoPreferences.DEBUG_MODE_ON);
             debugModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference preference, Object newValue) {
                     mPrefs.setDebugModeOn((Boolean) newValue);
                     return true;
                 }
             });
 
             findPreference("nopref_crash_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                 @Override
                 public boolean onPreferenceClick(Preference preference) {
                     throw new NullPointerException("Test crash");
                 }
             });
 
             findPreference("nopref_test_something").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                 @Override
                 public boolean onPreferenceClick(Preference preference) {
                     testSomething();
                     return true;
                 }
             });
         }
 
         private void syncData() {
             String env = StrollimoApplication.getService(StrollimoPreferences.class).getEnvTag();
             EnvSyncManager preloader = new EnvSyncManager(getActivity(), env, new AccomplishableController.OperationCallback() {
                 @Override
                 public void onSuccess() {
                     StrollimoApplication.getService(StrollimoPreferences.class).saveSyncTime();
                 }
 
                 @Override
                 public void onError(String errorMsg) {
                     Toast.makeText(getActivity(), "Sync error", Toast.LENGTH_SHORT).show();
                 }
             });
             preloader.start();
         }
 
         private void testSomething() {
 //        testGetMysteries();
 //        testUpdateSecretCall();
 //        testUpdateMysteryCall();
 //        testGetSecrets();
 //        testPickup();
             testGetPickupStates();
         }
 
         private void testGetPickupStates() {
             StrollimoApplication.getService(StrollimoApi.class).getPickupStatus(null, new Callback<GetPickupStatusResponse>() {
 
                 @Override
                 public void success(GetPickupStatusResponse getPickupStatusResponse, Response response) {
                     Map<String, BaseAccomplishable.PickupState> statuses = getPickupStatusResponse.getSecretStatuses();
                     for (String stringId : statuses.keySet()) {
                         Log.i("BB", "id: " + stringId + ", value: " + statuses.get(stringId));
                     }
                 }
 
                 @Override
                 public void failure(RetrofitError retrofitError) {
                     Log.i("BB", "Error: " + retrofitError.toString());
                 }
             });
         }
 
         private void testPickup() {
             StrollimoApplication.getService(StrollimoApi.class).pickupSecret(new Secret("11marco", "test"), "http//", new Callback<PickupSecretResponse>() {
                 @Override
                 public void success(PickupSecretResponse pickupSecretResponse, Response response) {
                     Log.i("BB", "success");
                 }
 
                 @Override
                 public void failure(RetrofitError retrofitError) {
                     Log.i("BB", "failure");
 
                 }
             });
         }
 
         private void testGetMysteries() {
             StrollimoApplication.getService(StrollimoApi.class).getMysteries("default", new Callback<GetMysteriesResponse>() {
                 @Override
                 public void success(GetMysteriesResponse getMysteriesResponse, Response response) {
                     Log.i("BB", "success");
                 }
 
                 @Override
                 public void failure(RetrofitError retrofitError) {
                     Log.i("BB", "failure");
 
                 }
             });
         }
 
         private void testGetSecrets() {
             String mysteryId = "feb1eb63-fca6-4291-99c1-7a6fee31ee05";
             StrollimoApplication.getService(StrollimoApi.class).getSecrets(mysteryId, new Callback<GetSecretsResponse>() {
                 @Override
                 public void success(GetSecretsResponse getSecretsResponse, Response response) {
                     Log.i("BB", "success");
                 }
 
                 @Override
                 public void failure(RetrofitError retrofitError) {
                     Log.i("BB", "failure");
 
                 }
             });
         }
 
         private void testUpdateSecretCall() {
             Secret secret = new Secret("1", "bb test secret");
             secret.setShortDesc("short desc");
             secret.setImgUrl("test imag url");
             secret.setLocation(new Location(0.4, 0.5, 0.1));
             ImageComparisonPickupMode mode = new ImageComparisonPickupMode();
             mode.addUrl("test url 1");
             secret.addPickupMode(mode);
             StrollimoApplication.getService(StrollimoApi.class).updateSecret(secret, new Callback<UpdateSecretResponse>() {
                 @Override
                 public void success(UpdateSecretResponse updateSecretResponse, Response response) {
                     Log.i("BB", "success");
                 }
 
                 @Override
                 public void failure(RetrofitError retrofitError) {
                     Log.i("BB", "failure");
 
                 }
             });
         }
 
         private void testUpdateMysteryCall() {
             Mystery mystery = new Mystery("56", "BBTestNew", 0.4, 0.5, "image URL");
             mystery.setShortDesc("something");
             StrollimoApplication.getService(StrollimoApi.class).updateMystery(mystery, new Callback<UpdateMysteryResponse>() {
                 @Override
                 public void success(UpdateMysteryResponse updateMysteryResponse, Response response) {
                     Log.i("BB", "success");
                 }
 
                 @Override
                 public void failure(RetrofitError retrofitError) {
                     Log.i("BB", "failure");
 
                 }
             });
         }
     }
 
 }
 
