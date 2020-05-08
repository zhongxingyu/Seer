 package com.jacobobryant.scripturemastery;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteException;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScriptureActivity extends Activity {
     public static final String PASSAGE_BUNDLE = "passageBundle";
     public static final int RESULT_MEMORIZED = RESULT_FIRST_USER;
     public static final int RESULT_PARTIALLY_MEMORIZED =
                                             RESULT_FIRST_USER + 1;
     public static final int RESULT_MASTERED = RESULT_FIRST_USER + 2;
     private static final int ROUTINE_DIALOG = 0;
     private static final int PROGRESS_DIALOG = 1;
     private static final int CONTEXT_DIALOG = 2;
     private static final int DOCTRINE_DIALOG = 3;
     private static final int APPLICATION_DIALOG = 4;
     private static final int HELP_DIALOG = 5;
     private static final String PREF_HELP_SHOWN = "pref_help_shown";
     private String routine;
     private Passage passage;
     private int progress;
     private int scripId;
     private Scripture scripture;
 
     public class DoubleClickListener implements OnTouchListener {
         private long touchTime;
         private Context context;
 
         public DoubleClickListener(Context context) {
             this.context = context;
         }
 
         public boolean onTouch(View v, MotionEvent event) {
             switch (event.getAction()) {
                 case MotionEvent.ACTION_DOWN:
                     handlePress();
                     break;
                     /*
                 case MotionEvent.ACTION_UP:
                     handleRelease();
                     break;
                     */
             }
             return false;
         }
 
         public void handlePress() {
             final int DOUBLE_TAP_WINDOW = 300;
             long time = SystemClock.uptimeMillis();
             int messageId;
 
             if (touchTime + DOUBLE_TAP_WINDOW > time) {
                 messageId = passage.toggleHint()
                     ? R.string.hint_active : R.string.hint_inactive;
                 Toast.makeText(context, messageId, Toast.LENGTH_SHORT)
                     .show();
                 setText();
             }
             touchTime = time;
         }
 
         /*
         public void handleRelease() {
             if (passage.hintActive()) {
                 passage.setHintActive(false);
                 setText();
             }
         }
         */
     }
 
     @SuppressWarnings("deprecation")
     @Override
     public void onCreate(Bundle state) {
         super.onCreate(state);
         Log.d(SMApp.TAG, "ScriptureActivity.onCreate()");
         setContentView(R.layout.scripture_activity);
         LayoutInflater inflater = LayoutInflater.from(this);
         ViewGroup layout = (ViewGroup) findViewById(R.id.layout);
         TextView lblVerse;
         Paint defaultPaint = ((TextView)
                 inflater.inflate(R.layout.verse, null)).getPaint();
         View scrollView = findViewById(R.id.scroll);
         Intent intent = getIntent();
         Context a = getApplication();
         boolean inRoutine;
         SharedPreferences prefs =
                 PreferenceManager.getDefaultSharedPreferences(a);
         boolean helpShown = prefs.getBoolean(PREF_HELP_SHOWN, false);
 
         if (!helpShown) {
             showDialog(HELP_DIALOG);
             prefs.edit().putBoolean(PREF_HELP_SHOWN, true).apply();
         }
         //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         inRoutine = intent.getBooleanExtra(
                 ScriptureListActivity.EXTRA_IN_ROUTINE, false);
         scripId = intent.getIntExtra(
                 ScriptureListActivity.EXTRA_SCRIP_ID, -1);
         try {
             // There have been weird crash reports for this line
             scripture = Scripture.objects(a).get(scripId);
         } catch (SQLiteException e) {
             Log.w(SMApp.TAG, "SQLiteException was caught. " +
                     "Syncing DB and trying again...");
             SyncDB.syncDB(a);
             scripture = Scripture.objects(a).get(scripId);
         }
         // there appears to be a bug in the Bundle.get*() methods. They
         // shouldn't throw NullPointerExceptions, but they do.
         try {
             Bundle passageBundle = state.getBundle(PASSAGE_BUNDLE);
             passage = new Passage(a, scripture, defaultPaint,
                     passageBundle);
         } catch (NullPointerException e) {
             passage = new Passage(a, scripture, defaultPaint);
         }
         if (inRoutine) {
             routine = scripture.getBook(a).getRoutine(a);
         }
         setTitle(scripture.getReference());
         for (int i = 0; i < passage.getParagraphs().length; i++) {
             lblVerse = (TextView)
                     inflater.inflate(R.layout.verse, null);
             layout.addView(lblVerse);
         }
         scrollView.setOnTouchListener(new DoubleClickListener(this));
         setText();
         progress = RESULT_MEMORIZED;
     }
 
     @Override
     public void onSaveInstanceState(Bundle state) {
         state.putBundle(PASSAGE_BUNDLE, passage.getBundle());
         super.onSaveInstanceState(state);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.scripture_activity_options, menu);
         L.log("scripture.getContext().length(): "
                 + scripture.getContext().length());
         if (routine == null) {
             menu.findItem(R.id.mnuRoutine).setVisible(false);
         }
         if (scripture.getContext().length() == 0) {
             menu.findItem(R.id.mnu_context).setVisible(false);
             menu.findItem(R.id.mnu_doctrine).setVisible(false);
             menu.findItem(R.id.mnu_application).setVisible(false);
             menu.findItem(R.id.mnu_open).setVisible(false);
         }
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         if (!passage.hasMoreLevels(getApplication())) {
             menu.findItem(R.id.mnuIncreaseLevel).setVisible(false);
         }
         return true;
     }
 
     // todo: get rid of deprecated showDialog calls.
     @SuppressWarnings("deprecation")
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             /*
             case android.R.id.home:
                 finish();
                 return true;
                 */
             case R.id.mnuIncreaseLevel:
                 passage.setHint(false);
                 passage.increaseLevel(getApplication());
                 setText();
                 return true;
             case R.id.mnuDone:
                 showDialog(PROGRESS_DIALOG);
                 return true;
             case R.id.mnu_context:
                 showDialog(CONTEXT_DIALOG);
                 return true;
             case R.id.mnu_doctrine:
                 showDialog(DOCTRINE_DIALOG);
                 return true;
             case R.id.mnu_application:
                 showDialog(APPLICATION_DIALOG);
                 return true;
             case R.id.mnuRoutine:
                 showDialog(ROUTINE_DIALOG);
                 return true;
             case R.id.mnu_open:
                 openWebScripture();
                 return true;
             case R.id.mnu_settings:
                 Intent intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         switch (id) {
             case ROUTINE_DIALOG:
                 builder.setTitle(R.string.routineDialog)
                         .setMessage(routine)
                         .setPositiveButton(android.R.string.ok, null);
                 break;
             case CONTEXT_DIALOG:
                 builder.setTitle(R.string.mnu_context)
                         .setMessage(scripture.getContext())
                         .setPositiveButton(android.R.string.ok, null);
                 break;
             case DOCTRINE_DIALOG:
                 builder.setTitle(R.string.mnu_doctrine)
                         .setMessage(scripture.getDoctrine())
                         .setPositiveButton(android.R.string.ok, null);
                 break;
             case APPLICATION_DIALOG:
                 builder.setTitle(R.string.mnu_application)
                         .setMessage(scripture.getApplication())
                         .setPositiveButton(android.R.string.ok, null);
                 break;
             case PROGRESS_DIALOG:
                 buildProgressDialog(builder);
                 break;
             case HELP_DIALOG:
                 builder.setTitle(R.string.help_title)
                         .setMessage(R.string.toggle_help_message)
                         .setPositiveButton(android.R.string.ok, null);
                 break;
         }
         return builder.create();
     }
 
     private void openWebScripture() {
         String url = "http://www.lds.org/search?collection=scriptures&query="
                + scripture.getReference();
         Intent browserIntent =
                 new Intent(Intent.ACTION_VIEW, Uri.parse(url));
         startActivity(browserIntent);
     }
 
     private void buildProgressDialog(AlertDialog.Builder builder) {
         builder.setTitle(R.string.progressDialog)
                 .setSingleChoiceItems(R.array.progress, 0, 
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog,
                             int which) {
                         // the cases correspond to the items in
                         // R.array.progress
                         switch (which) {
                             case 0:
                                 progress = RESULT_MEMORIZED;
                                 break;
                             case 1:
                                 progress = RESULT_PARTIALLY_MEMORIZED;
                                 break;
                             case 2:
                                 progress = RESULT_MASTERED;
                         }
                     }
                 })
                 .setPositiveButton(android.R.string.ok,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         setResult(progress);
                         finish();
                     }
                 })
                 .setNegativeButton(android.R.string.cancel, null);
     }
 
     public void setText() {
         ViewGroup layout = (ViewGroup) findViewById(R.id.layout);
         String[] verses = passage.getParagraphs();
         for (int i = 0; i < verses.length; i++) {
             ((TextView) layout.getChildAt(i)).setText(verses[i]);
         }
     }
 }
