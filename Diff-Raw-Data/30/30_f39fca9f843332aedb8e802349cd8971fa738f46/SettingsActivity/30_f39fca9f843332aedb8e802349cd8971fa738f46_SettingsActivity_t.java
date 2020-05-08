 package fi.iki.dezgeg.matkakorttiwidget.gui;
 
import android.app.AlertDialog;
 import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceGroup;
 import android.text.Html;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageButton;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import fi.iki.dezgeg.matkakorttiwidget.R;
 import fi.iki.dezgeg.matkakorttiwidget.matkakortti.Card;
 import fi.iki.dezgeg.matkakorttiwidget.matkakortti.MatkakorttiException;
 
 import static android.util.Log.d;
 
 public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
     private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 
     private volatile List<Card> fetchedCards;
     private FetchCardListTask fetchCardListTask;
 
     private static final String[] PREF_KEYS = new String[]{"username", "password"};
 
     private static final String PER_WIDGET_PREF_STRING_PREFIX = "settings_widgetPrefs_";
     private static final String[][] PER_WIDGET_PREFS = new String[][]{
             {"showName", "true"},
             {"autoHidePeriod", "false"},
     };
 
     private PreferenceGroup getCardListPrefGroup() {
         return (PreferenceGroup) findPreference("cardList");
     }
 
     private boolean loginDetailsFilledIn() {
         SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
         return !prefs.getString("username", "").equals("") && !prefs.getString("password", "").equals("");
     }
 
     private void updateOkButtonEnabledState() {
         boolean enable = loginDetailsFilledIn() && fetchedCards != null && !fetchedCards.isEmpty();
         findViewById(R.id.settings_ok_button).setEnabled(enable);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         requestWindowFeature(Window.FEATURE_PROGRESS);
         super.onCreate(savedInstanceState);
 
         appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
         if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
             appWidgetId = getIntent().getIntExtra("EXTRA_APPWIDGET_ID", AppWidgetManager.INVALID_APPWIDGET_ID);
 
         d("SettingsActivity", "Launched for AppWidget " + appWidgetId);
         setResult(RESULT_CANCELED);
 
         addPreferencesFromResource(R.xml.main_menu);
         PreferenceGroup perWidgetPrefs = (PreferenceGroup) findPreference("widgetPrefs");
         for (String[] pair : PER_WIDGET_PREFS) {
             String key = pair[0];
             boolean defaultValue = Boolean.valueOf(pair[1]);
 
             CheckBoxPreference pref = new CheckBoxPreference(this);
             int resId = getResources().getIdentifier(PER_WIDGET_PREF_STRING_PREFIX + key,
                     "string", "fi.iki.dezgeg.matkakorttiwidget");
             pref.setTitle(localize(resId));
             pref.setDefaultValue(defaultValue);
             pref.setKey(Utils.prefKeyForWidgetId(appWidgetId, key));
 
             perWidgetPrefs.addPreference(pref);
         }
         setContentView(R.layout.settings_menu);
 
         findPreference("registerLink").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse("https://omamatkakortti.hsl.fi"));
                 startActivity(intent);
                 return true;
             }
         });
         ((ImageButton)findViewById(R.id.settings_ok_button)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent resultValue = new Intent();
                 resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                 SettingsActivity.this.setResult(RESULT_OK, resultValue);
                 SettingsActivity.this.finish();
             }
         });
     }
 
     private CharSequence localize(int resId, Object... args) {
         return getResources().getString(resId, args);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
         for (String prefKey : PREF_KEYS)
             updatePrefTitle(getPreferenceScreen().getSharedPreferences(), prefKey);
         updateCardList();
         updateOkButtonEnabledState();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
         updatePrefTitle(prefs, key);
 
         if (key.startsWith("widget_") && fetchedCards != null) {
             WidgetUpdaterService.updateWidgets(getApplicationContext(), fetchedCards);
         } else if (key.equals("username") || key.equals("password")) {
                 updateCardList();
         }
 
         updateOkButtonEnabledState();
     }
 
     private synchronized void updateCardList() {
         if (loginDetailsFilledIn()) {
             if (fetchCardListTask != null && fetchCardListTask.getStatus() != AsyncTask.Status.FINISHED)
                 fetchCardListTask.cancel(true);
             fetchCardListTask = new FetchCardListTask();
             fetchCardListTask.execute();
         }
     }
 
     private void updatePrefTitle(SharedPreferences prefs, String key) {
 
         if (key.equals("password")) {
             String password = prefs.getString(key, "");
             String masked = "";
             for (int i = 0; i < password.length(); i++)
                 masked += "*";
 
             findPreference(key).setSummary(masked);
         } else if (findPreference(key) instanceof EditTextPreference) {
             findPreference(key).setSummary(prefs.getString(key, ""));
         }
     }
 
     // Helper classes
     private class FetchCardListTask extends AsyncTask<Void, Void, MatkakorttiApiResult> {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             getCardListPrefGroup().removeAll();
             SettingsActivity.this.setProgressBarIndeterminate(true);
             SettingsActivity.this.setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected MatkakorttiApiResult doInBackground(Void... unused) {
             try {
                 return new MatkakorttiApiResult(MatkakorttiWidgetApp.getCardList());
             } catch (Exception e) {
                 return new MatkakorttiApiResult(e);
             }
         }
 
         @Override
         protected void onPostExecute(MatkakorttiApiResult result) {
             super.onPostExecute(result);
             Exception exc = result.getException();
 
             if (exc == null) {
                 fetchedCards = result.getCardList();
                 WidgetUpdaterService.updateWidgets(getApplicationContext(), fetchedCards);
                 populateCardListPrefGroup();
             } else {
                 MatkakorttiException apiExc = exc instanceof MatkakorttiException ? (MatkakorttiException) exc : null;
 
                 fetchedCards = null;
                Preference text = new Preference(SettingsActivity.this);
 
                 CharSequence escaped;
                 if (Utils.isConnectionProblemRelatedException(exc))
                     escaped = localize(R.string.settings_errors_connectionError);
                 else if (apiExc != null && !apiExc.isInternalError())
                     escaped = localize(R.string.settings_errors_apiErrorPrefix) + " " +
                             exc.getMessage(); // TODO: escape
                 else {
                     escaped = localize(R.string.settings_errors_unexpectedError);
                     Utils.reportException("SettingsActivity", exc);
                 }
                 text.setTitle(Html.fromHtml("<font color='#FF0000'>" + escaped + "</font>"));

                final CharSequence finalMsg = escaped;
                text.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setCancelable(true)
                                .setTitle(R.string.settings_errors_errorDialog_title)
                                .setMessage(finalMsg)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        dialog.cancel();
                                    }
                                })
                                .create().show();
                        return true;
                    }
                });

                 getCardListPrefGroup().addPreference(text);
             }
             SettingsActivity.this.setProgressBarIndeterminateVisibility(false);
             updateOkButtonEnabledState();
         }
 
         private void populateCardListPrefGroup() {
             final PreferenceGroup cardListPrefGroup = getCardListPrefGroup();
 
             // Uh oh. Simulate radio buttons with checkboxes since there is no RadioButtonPreference.
             final String thisWidgetKey = Utils.prefKeyForWidgetId(appWidgetId, "cardSelected");
             String selectedCardId = getPreferenceScreen().getSharedPreferences().getString(thisWidgetKey, "");
             boolean foundSelected = false;
             for (final Card card : fetchedCards) {
                 final CheckBoxPreference pref = new CheckBoxPreference(SettingsActivity.this);
                 pref.setWidgetLayoutResource(R.layout.radiobutton_preference);
                 pref.setTitle(card.getName());
                 pref.setSummary(radioButtonSummaryForCard(card));
                 if (!foundSelected && card.getId().equals(selectedCardId)) {
                     pref.setChecked(true);
                     foundSelected = true;
                 }
                 pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                     // !!!!! When we are called, the box has already changed state.
                     @Override
                     public boolean onPreferenceClick(Preference unused) {
                         // Trivial case: Un-checking the selected box, undo it.
                         if (!pref.isChecked()) {
                             pref.setChecked(true);
                             return true;
                         }
                         // Clear all others...
                         for (int i = 0; i < cardListPrefGroup.getPreferenceCount(); i++) {
                             CheckBoxPreference cp = ((CheckBoxPreference) cardListPrefGroup.getPreference(i));
                             cp.setChecked(false);
                         }
                         pref.setChecked(true); // ...but not this one.
 
                         setSelectedCardForThisWidget(card.getId(), thisWidgetKey);
                         return true;
                     }
                 });
                 cardListPrefGroup.addPreference(pref);
             }
 
             if (!foundSelected && cardListPrefGroup.getPreferenceCount() > 0) {
                 setSelectedCardForThisWidget(fetchedCards.get(0).getId(), thisWidgetKey);
                 ((CheckBoxPreference) cardListPrefGroup.getPreference(0)).setChecked(true);
             }
         }
 
         private CharSequence radioButtonSummaryForCard(Card card) {
             CharSequence period;
             if (card.getPeriodExpiryDate() == null)
                 period = localize(R.string.settings_cardList_card_noPeriod);
             else
                 period = localize(R.string.settings_cardList_card_hasPeriod,
                         new SimpleDateFormat("dd.MM.yyyy").format(card.getPeriodExpiryDate()));
             CharSequence money = localize(R.string.settings_cardList_card_hasMoney,
                     WidgetUpdaterService.FORMAT_TWO_DIGITS_AFTER_POINT.format(card.getMoney()));
             return money.toString() + " " + period;
         }
 
         private void setSelectedCardForThisWidget(String cardId, String thisWidgetKey) {
             SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
             editor.putString(thisWidgetKey, cardId).commit();
             d("SettingsActivity", thisWidgetKey + " setting changing to " + cardId);
         }
     }
 
     static class MatkakorttiApiResult {
         private List<Card> cardList;
         private Exception exception;
 
         MatkakorttiApiResult(List<Card> cardList) {
             this.cardList = cardList;
         }
 
         MatkakorttiApiResult(Exception exception) {
             this.exception = exception;
         }
 
         List<Card> getCardList() {
             return cardList;
         }
 
         Exception getException() {
             return exception;
         }
     }
 }
