 /*
  * Copyright (C) 2010 Timothy Bourke
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 59
  * Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package org.tbrk.mnemododo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import mnemogogo.mobile.hexcsv.Card;
 import mnemogogo.mobile.hexcsv.HexCsvAndroid;
 import mnemogogo.mobile.hexcsv.FindCardDirAndroid;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.SeekBar;
 import android.util.Log;
 
 abstract class MnemododoMain
     extends Activity
     implements OnClickListener, OnKeyListener
 {
     enum Mode { SHOW_QUESTION, SHOW_ANSWER, NO_CARDS, NO_NEW_CARDS }
     
     static final int BUTTON_POS_BOTTOM = 0;
     static final int BUTTON_POS_LEFT = 1;
     static final int BUTTON_POS_RIGHT = 2;
 
     static final int[] grading_button_panel_ids =
         {R.id.grading_buttons_bottom,
          R.id.grading_buttons_left,
          R.id.grading_buttons_right};
     static final int[] show_button_panel_ids =
         {R.id.show_buttons_bottom,
          R.id.show_buttons_left,
          R.id.show_buttons_right};
     
     static final int DIALOG_ABOUT = 0;
     static final int DIALOG_STATS = 1;
     static final int DIALOG_SCHEDULE = 2;
     static final int DIALOG_CATEGORIES = 3;
     static final int DIALOG_STYLES = 4;
 
     protected static final int MENU_SKIP = 0;
     protected static final int MENU_STATISTICS = 1;
     protected static final int MENU_SCHEDULE = 2;
     protected static final int MENU_SETTINGS = 3;
     protected static final int MENU_ABOUT = 4;
     protected static final int MENU_CATEGORIES = 5;
 
     protected static final int REQUEST_SETTINGS = 100;
 
     protected static final int KEY_GRADE0 = 0;
     protected static final int KEY_GRADE1 = 1;
     protected static final int KEY_GRADE2 = 2;
     protected static final int KEY_GRADE3 = 3;
     protected static final int KEY_GRADE4 = 4;
     protected static final int KEY_GRADE5 = 5;
     protected static final int KEY_SHOW_ANSWER = 6;
     protected static final int KEY_REPLAY_SOUNDS = 7;
 
     protected static final int STYLE_DARK   = 0;
     protected static final int STYLE_DIMMED = 1;
     protected static final int STYLE_LIGHT  = 2;
 
     protected static final String html_post = "</body></html>";
 
     /* data (always recalculated) */
     boolean carddb_dirty = false;
     private Date thinking_from;
 
     final int make_visible_delay = 300;
     final int make_visible_fade_delay = 50;
     
     /* data (cache on temporary restart) */
 
     Mode mode = Mode.NO_CARDS;
     protected String html_pre = "<html>";
     protected boolean auto_play = true;
 
     CardStore carddb = new CardStore();
     protected Card cur_card;
     protected long thinking_msecs = 0;
 
     protected LoadCardTask card_task = null;
 
     protected boolean is_demo = false;
     protected String demo_path = "/android_asset/demodeck/";
     protected String demo_imgson_path_override = null;
     protected String package_name = "org.tbrk.mnemododo";
 
     /* Configuration */
     
     int cards_to_load = 50;
     boolean center = true;
     boolean touch_buttons = true;
     boolean two_grading_rows = false;
 
     int style = STYLE_LIGHT;
 
     String card_font_size = "normal";
     String card_text_color = "black";
     String card_back_color = "white";
     String card_font = "";
     int[] key;
 
     /* UI */
     WebView webview;
     
     int[] grade_buttons = {R.id.grade0, R.id.grade1, R.id.grade2,
                            R.id.grade3, R.id.grade4, R.id.grade5};
     int[] other_buttons = {R.id.show};
 
     TableLayout grading_panel;
     ViewGroup show_panel;
     View hidden_view = null;
     int button_pos = BUTTON_POS_BOTTOM;
     boolean is_wide_screen = false;
     
     SoundPlayer sound_player = new SoundPlayer(MnemododoMain.this);
     
     private Handler handler = new Handler();
     private Animation buttonAnimation;
     private Runnable makeViewVisible = new Runnable() {
         public void run() {
             if (hidden_view != null) {
                 hidden_view.setVisibility(View.VISIBLE);
                 if (buttonAnimation != null) {
                     hidden_view.startAnimation(buttonAnimation);
                 }
             }
         }
     };
 
     /* Javascript interface */
     
     private class Javascript
     {
         @SuppressWarnings("unused")
         public void learnAhead()
         {
             final Runnable r = new Runnable() {
                 public void run() {
                     carddb.cards.learnAhead();
                     nextQuestion();
                 }
             };
             
             handler.post(r);
         }
 
         @SuppressWarnings("unused")
         public void replayQuestionSounds()
         {
             queueQuestionSounds();
         }
         
         @SuppressWarnings("unused")
         public void replayAnswerSounds()
         {
             queueAnswerSounds();
         }
     }
     
     abstract protected void configureDemo();
     
     /** Called when the activity is first created. */
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setFullscreenMode();
         setContentView(R.layout.main);
 
         configureDemo();
         
         HexCsvAndroid.context = this;
         
         // Setup UI specifics
         webview = (WebView) findViewById(R.id.card_webview);
         webview.setOnKeyListener(this);
         webview.getSettings().setJavaScriptEnabled(true);
         webview.addJavascriptInterface(new Javascript(), "Mnemododo");
 
         grading_panel = (TableLayout) findViewById(R.id.grading_buttons_bottom);
         show_panel = (ViewGroup) findViewById(R.id.show_buttons_bottom);
         
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         is_wide_screen = metrics.widthPixels > metrics.heightPixels;
 
         for (int butid : grade_buttons) {
             Button button = (Button) findViewById(butid);
             button.setOnClickListener(this);
             button.setOnKeyListener(this);
         }
 
         for (int butid : other_buttons) {
             Button button = (Button) findViewById(butid);
             button.setOnClickListener(this);
             button.setOnKeyListener(this);
         }
         
         findViewById(R.id.category).setOnClickListener(this);
 
         View leftview = findViewById(R.id.cards_left);
         leftview.setOnClickListener(this);
         leftview.setLongClickable(true);
         leftview.setOnLongClickListener(
                 new View.OnLongClickListener () {
                     public boolean onLongClick(View v) {
                         showDialog(DIALOG_STYLES);
                         return true;
                     }
                 }
             );
 
         // Sound
         setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
 
         // Get settings and load cards if necessary
         loadPrefs((MnemododoMain) getLastNonConfigurationInstance());
 
         // Animation
         buttonAnimation = new AlphaAnimation(0.0f, 1.0f);
         buttonAnimation.setDuration(make_visible_fade_delay);
         
         Eula.show(MnemododoMain.this);
     }
 
     protected TaskListener<String> makeCardStoreListener ()
     {
         return new TaskListener<String> () {
             public Context getContext ()
             {
                 return MnemododoMain.this;
             }
 
             public String getString(int resid)
             {
                 return MnemododoMain.this.getString(resid);
             }
 
             public void onFinished(String error_msg)
             {
                 if (error_msg == null) {
                     carddb_dirty = false;
                     nextQuestion();
                 } else {
                     setMode(Mode.NO_CARDS);
                     showFatal(error_msg, false);
                 }
             }
         };
     }
 
     protected TaskListener<Pair <Boolean, String>> makeLoadCardListener()
     {
         return new TaskListener<Pair<Boolean, String>> () {
             public Context getContext ()
             {
                 return MnemododoMain.this;
             }
 
             public String getString(int resid)
             {
                 return MnemododoMain.this.getString(resid);
             }
 
             public void onFinished(Pair<Boolean, String> result)
             {
                 Boolean start_thinking = result.fst;
                 String html = result.snd;
 
                 card_task = null;
 
                 setCategory(cur_card.categoryName());
                 
                 if (demo_imgson_path_override != null) {
                     webview.loadDataWithBaseURL("file://" +
                             demo_imgson_path_override, html,
                             "text/html", "UTF-8", "");
                 } else {
                     webview.loadDataWithBaseURL("file://" + carddb.cards_path,
                             html, "text/html", "UTF-8", "");
                 }
 
                 if (start_thinking && (cur_card != null)) {
                     startThinking();
                 }
 
                 if (hidden_view != null && cur_card != null) {
                     handler.removeCallbacks(makeViewVisible);
                     handler.postDelayed(makeViewVisible, make_visible_delay);
                 }
             }
         };
     }
 
     protected void loadCardDB(String path)
     {
         carddb = new CardStore(path, cards_to_load, makeCardStoreListener());
     }
 
     protected void loadCard(boolean is_question, boolean start_thinking)
     {
         if (card_task == null) {
             card_task = new LoadCardTask(makeLoadCardListener(),
                 cur_card, center, html_pre, html_post);
             carddb.setProgress(card_task);
             card_task.execute(is_question, start_thinking);
         }
     }
 
     public void setFullscreenMode()
     {
         SharedPreferences settings = PreferenceManager
             .getDefaultSharedPreferences(this);
         if (settings.getBoolean("fullscreen_mode", false)) {
             getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
     }
 
     public void loadPrefs(MnemododoMain lastDodo)
     {
         boolean quick_restart = false;
         Mode nmode = mode;
         
         if (lastDodo != null) {
             nmode = lastDodo.mode;
             html_pre = lastDodo.html_pre;
             auto_play = lastDodo.auto_play;
 
             carddb = lastDodo.carddb;
             carddb.updateCallback(makeCardStoreListener());
             carddb.resume();
 
             card_task = lastDodo.card_task;
 
             cur_card = lastDodo.cur_card;
             thinking_msecs = lastDodo.thinking_msecs;
             quick_restart = true;
         }
         
         SharedPreferences settings = PreferenceManager
                 .getDefaultSharedPreferences(this);
 
         // store default prefs if necessary
         if (!settings.contains("center")) {
             SharedPreferences.Editor editor = settings.edit();
             editor.putBoolean("center", center);
             editor.putBoolean("touch_buttons", touch_buttons);
             editor.putString("card_font_size", card_font_size);
             editor.putString("cards_to_load", Integer.toString(cards_to_load));
             editor.putBoolean("two_grading_rows", two_grading_rows);
             editor.putBoolean("fullscreen_mode", false);
             editor.putBoolean("show_titlebar", true);
             editor.putBoolean("auto_play", auto_play);
             editor.putString("button_pos", Integer.toString(button_pos));
             editor.putString("style", Integer.toString(style));
             editor.commit();
         }
 
         // load prefs
         cards_to_load = Integer.parseInt(settings.getString("cards_to_load", "50"));
         touch_buttons = settings.getBoolean("touch_buttons", true);
         auto_play = settings.getBoolean("auto_play", true);
         style = Integer.parseInt(settings.getString("style", "2"));
         boolean two_grading_rows = settings.getBoolean("two_grading_rows", false);
         int nbutton_pos = Integer.parseInt(settings.getString("button_pos",
                 Integer.toString(BUTTON_POS_BOTTOM)));
         boolean show_titlebar = settings.getBoolean("show_titlebar", true);
 
         boolean ncenter = settings.getBoolean("center", true);
         String ncard_font = settings.getString("card_font", "");
         String ncard_font_size = settings.getString("card_font_size", "normal");
 
         boolean reload = (center != ncenter)
             || (!card_font.equals(ncard_font))
             || (!card_font_size.equals(ncard_font_size))
             || quick_restart;
         center = ncenter;
         card_font = ncard_font;
         card_font_size = ncard_font_size;
 
         applyStyle(style);
         html_pre = getCardHeader();
 
         // keys
         key = new int[KEY_REPLAY_SOUNDS + 1];
         key[KEY_GRADE0] = settings.getInt("key_grade0", KeyEvent.KEYCODE_0);
         key[KEY_GRADE1] = settings.getInt("key_grade1", KeyEvent.KEYCODE_1);
         key[KEY_GRADE2] = settings.getInt("key_grade2", KeyEvent.KEYCODE_2);
         key[KEY_GRADE3] = settings.getInt("key_grade3", KeyEvent.KEYCODE_3);
         key[KEY_GRADE4] = settings.getInt("key_grade4", KeyEvent.KEYCODE_4);
         key[KEY_GRADE5] = settings.getInt("key_grade5", KeyEvent.KEYCODE_5);
         key[KEY_SHOW_ANSWER] = settings.getInt("key_show_answer", KeyEvent.KEYCODE_9);
         key[KEY_REPLAY_SOUNDS] = settings.getInt("key_replay_sounds", KeyEvent.KEYCODE_7);
 
         // cards_path
         String settings_cards_path;
         settings_cards_path = settings.getString("cards_path", demo_path);
 
         if (settings_cards_path != null
                 && !settings_cards_path.endsWith(File.separator)) {
             settings_cards_path = settings_cards_path + File.separator;
         }
         
         boolean will_load_cards = carddb.needLoadCards(settings_cards_path);
 
         // update the layout
         reconfigureButtons(nbutton_pos, two_grading_rows);
 
         findViewById(R.id.titlebar)
             .setVisibility(show_titlebar ? View.VISIBLE : View.GONE);
 
         if (touch_buttons && !will_load_cards) {
             show_panel
                     .setVisibility(nmode == Mode.SHOW_QUESTION ? View.VISIBLE
                             : View.GONE);
             grading_panel
                     .setVisibility(nmode == Mode.SHOW_ANSWER ? View.VISIBLE
                             : View.GONE);
         } else {
             show_panel.setVisibility(View.GONE);
             grading_panel.setVisibility(View.GONE);
         }
 
         if (will_load_cards) {
             if (!carddb.loadingCards()) {
                 loadCards(settings_cards_path);
             }
             reload = false;
         } else if (!carddb.active()) {
             setMode(Mode.NO_CARDS);
             reload = false;
         }
         
         if (webview != null) {
             WebSettings websettings = webview.getSettings();
             if (!card_font.equals("")) {
                 websettings.setStandardFontFamily(card_font);
             }
 
             if (reload) {
                 hidden_view = null;
                 setMode(nmode, false);
             }
         }
     }
 
     protected void reconfigureButtons(int nbutton_pos, boolean two_rows)
     {
         int curr_rows = grading_panel.getChildCount();
         int num_rows;
         boolean side_buttons = false;
 
         int curr_button_pos = button_pos;
         button_pos = nbutton_pos;
         
         if (is_wide_screen) {
             side_buttons = (nbutton_pos != BUTTON_POS_BOTTOM);
             num_rows = (side_buttons ? 6 : 1);
         } else {
             num_rows = (two_rows ? 2 : 1);
             nbutton_pos = BUTTON_POS_BOTTOM;
         }
 
         if ((curr_rows == num_rows) && (curr_button_pos == nbutton_pos)) {
             return;
         }
         
         // Get existing buttons
         Button buttons[] = new Button[grade_buttons.length];
         for (int i = 0; i < grade_buttons.length; ++i) {
             buttons[i] = (Button) findViewById(grade_buttons[i]);
         }
         
         RotatedButton show_button = (RotatedButton) findViewById(R.id.show);
         
         // Remove existing rows and buttons
         for (int i = 0; i < curr_rows; ++i) {
             TableRow row = (TableRow) grading_panel.getChildAt(i);
             row.removeAllViews();
         }
         grading_panel.removeAllViews();
         grading_panel.setVisibility(View.GONE);
         show_panel.removeAllViews();
         show_panel.setVisibility(View.GONE);
         
         // Choose the panel
         grading_panel = (TableLayout) findViewById(grading_button_panel_ids[nbutton_pos]);
         show_panel = (ViewGroup) findViewById(show_button_panel_ids[nbutton_pos]);
 
         // Add show button
         switch (nbutton_pos) {
         case BUTTON_POS_BOTTOM:
             show_button.angle = 0;
             break;
         case BUTTON_POS_LEFT:
             show_button.angle = 90;
             break;
         case BUTTON_POS_RIGHT:
             show_button.angle = -90;
             break;
         }
         show_panel.addView(show_button);
         
         // Add new rows
         TableRow rows[] = new TableRow[num_rows];
         
         for (int i = 0; i < rows.length; ++i) {
             rows[i] = new TableRow(MnemododoMain.this);
             rows[i].setLayoutParams(
                     new ViewGroup.LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT
                 ));
             if (side_buttons) {
                 rows[i].setMinimumWidth(60);
             }
             grading_panel.addView(rows[i]);
         }
         
         // Add buttons to new rows
         int num_per_row = grade_buttons.length / num_rows;        
         for (int i = 0; i < grade_buttons.length; ++i) {
             rows[i / num_per_row].addView(buttons[i]);
         }
     }
 
     public boolean onKey(View v, int keyCode, KeyEvent event)
     {
         if (event.getAction() != KeyEvent.ACTION_DOWN) {
             return false;
         }
         
         if (keyCode == KeyEvent.KEYCODE_MENU
                 || keyCode == KeyEvent.KEYCODE_HOME
                 || keyCode == KeyEvent.KEYCODE_BACK) {
             return false;
         }
         
         if (keyCode == key[KEY_SHOW_ANSWER]) {
             onClick(findViewById(R.id.show));
 
         } else if (keyCode == key[KEY_GRADE0]) {
             onClick(findViewById(grade_buttons[0]));
 
         } else if (keyCode == key[KEY_GRADE1]) {
             onClick(findViewById(grade_buttons[1]));
 
         } else if (keyCode == key[KEY_GRADE2]) {
             onClick(findViewById(grade_buttons[2]));
 
         } else if (keyCode == key[KEY_GRADE3]) {
             onClick(findViewById(grade_buttons[3]));
 
         } else if (keyCode == key[KEY_GRADE4]) {
             onClick(findViewById(grade_buttons[4]));
 
         } else if (keyCode == key[KEY_GRADE5]) {
             onClick(findViewById(grade_buttons[5]));
 
         } else if (keyCode == key[KEY_REPLAY_SOUNDS]) {
             if ((cur_card != null) && (!cur_card.getOverlay())) {
                 queueQuestionSounds();
             }
             if (mode == Mode.SHOW_ANSWER) {
                 queueAnswerSounds();
             }
 
         } else {
             return false;
         }
 
         return true;
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);
 
         if ((requestCode == REQUEST_SETTINGS) && (resultCode == RESULT_OK)) {
             loadPrefs(null);
         }
     }
 
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem skip = menu.findItem(MENU_SKIP);
         MenuItem stats = menu.findItem(MENU_STATISTICS);
         MenuItem sched = menu.findItem(MENU_SCHEDULE);
         MenuItem cats = menu.findItem(MENU_CATEGORIES);
 
         boolean show_card_buttons = (mode == Mode.SHOW_ANSWER
                 || mode == Mode.SHOW_QUESTION);
         boolean show_db_buttons = (mode == Mode.SHOW_ANSWER
                 || mode == Mode.SHOW_QUESTION
                 || mode == Mode.NO_NEW_CARDS);
 
         skip.setVisible(show_card_buttons);
         skip.setEnabled(show_card_buttons);
 
         stats.setVisible(show_card_buttons);
         stats.setEnabled(show_card_buttons);
 
         sched.setVisible(show_db_buttons);
         sched.setEnabled(show_db_buttons);
 
         cats.setVisible(show_db_buttons);
         cats.setEnabled(show_db_buttons);
         
         return super.onPrepareOptionsMenu(menu);
     }
 
     public boolean onCreateOptionsMenu(Menu menu)
     {
         menu.add(0, MENU_SKIP, 0, getString(R.string.skip_card))
                 .setIcon(R.drawable.icon_skip);
 
         menu.add(0, MENU_STATISTICS, 0, getString(R.string.statistics))
                 .setIcon(R.drawable.icon_stats);
 
         menu.add(0, MENU_SCHEDULE, 0, getString(R.string.schedule))
                 .setIcon(R.drawable.icon_schedule);
 
         menu.add(0, MENU_CATEGORIES, 0, getString(R.string.categories))
                 .setIcon(R.drawable.icon_categories);
 
         menu.add(0, MENU_SETTINGS, 0, getString(R.string.settings))
                 .setIcon(android.R.drawable.ic_menu_preferences);
 
         menu.add(0, MENU_ABOUT, 0, getString(R.string.info))
                 .setIcon(android.R.drawable.ic_menu_info_details);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId()) {
         case MENU_SKIP:
             cur_card.setSkip();
             nextQuestion();
             return true;
 
         case MENU_STATISTICS:
             showDialog(DIALOG_STATS);
             return true;
 
         case MENU_SCHEDULE:
             showDialog(DIALOG_SCHEDULE);
             return true;
 
         case MENU_CATEGORIES:
             showDialog(DIALOG_CATEGORIES);
             return true;
 
         case MENU_SETTINGS:
             Intent settings_intent = new Intent();
             settings_intent.setClass(this, Settings.class);
             settings_intent.putExtra("is_demo", is_demo);
             startActivityForResult(settings_intent, REQUEST_SETTINGS);
             return true;
 
         case MENU_ABOUT:
             showDialog(DIALOG_ABOUT);
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
     
     public Object onRetainNonConfigurationInstance()
     {
         return this;
     }
 
     public void onResume()
     {
         super.onResume();
 
         if (carddb.needsReload()) {
             loadCardDB(carddb.cards_path);
         } else {
             unpauseThinking();
         }
 
         if (card_task != null) {
             card_task.updateCallback(makeLoadCardListener());
         }
     }
 
     public void onPause()
     {
         super.onPause();
         sound_player.release();
         pauseThinking();
         saveCards();
         carddb.onPause();
 
         if (card_task != null) {
             card_task.pause();
         }
     }
 
     public void onDestroy()
     {
         super.onDestroy();
         carddb.close();
     }
 
     protected Dialog showInfo(int msg_id, boolean terminate)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(msg_id).setCancelable(false);
         if (terminate) {
             builder.setPositiveButton(R.string.ok,
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id)
                         {
                             MnemododoMain.this.finish();
                         }
                     });
         } else {
             builder.setPositiveButton(R.string.ok,
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id)
                         {
                         }
                     });
         }
         return (Dialog) builder.create();
     }
 
     protected void onPrepareDialog(int id, Dialog dialog)
     {
         switch (id) {
         case DIALOG_STATS:
             if (cur_card == null || !carddb.active()) {
                 return;
             }
 
             TextView text;
 
             text = (TextView) dialog.findViewById(R.id.grade);
             text.setText(Integer.toString(cur_card.grade));
 
             text = (TextView) dialog.findViewById(R.id.easiness);
             text.setText(Float.toString(cur_card.feasiness()));
 
             text = (TextView) dialog.findViewById(R.id.repetitions);
             text.setText(Integer.toString(cur_card.repetitions()));
 
             text = (TextView) dialog.findViewById(R.id.lapses);
             text.setText(Integer.toString(cur_card.lapses));
 
             text = (TextView) dialog
                     .findViewById(R.id.days_since_last_repetition);
             text.setText(Integer.toString(cur_card
                     .daysSinceLastRep(carddb.cards.days_since_start)));
 
             text = (TextView) dialog
                     .findViewById(R.id.days_until_next_repetition);
             text.setText(Integer.toString(cur_card
                     .daysUntilNextRep(carddb.cards.days_since_start)));
 
             break;
         }
     }
     
     protected Dialog onCreateDialog(int id)
     {
         Dialog dialog = null;
 
         // Context mContext = getApplicationContext();
         Context mContext = MnemododoMain.this;
 
         switch (id) {
         case DIALOG_ABOUT:
             PackageManager pm = getPackageManager();
             String version_name = "?.?.?";
             int version_code = 0;
             try {
                 PackageInfo pi = pm.getPackageInfo(package_name, 0);
                 version_name = pi.versionName;
                 version_code = pi.versionCode;
             } catch (NameNotFoundException e) { }
 
             dialog = new Dialog(mContext);
             dialog.setContentView(R.layout.about);
             dialog.setTitle(getString(R.string.app_name) + " "
                     + version_name
                     + " (r" + Integer.toString(version_code) + ")");
             dialog.setCanceledOnTouchOutside(true);            
             break;
 
         case DIALOG_STATS:
             if (cur_card == null) {
                 return null;
             }
 
             dialog = new Dialog(mContext);
 
             dialog.setContentView(R.layout.stats);
             dialog.setTitle(getString(R.string.card_statistics));
             dialog.setCanceledOnTouchOutside(true);
             break;
 
         case DIALOG_SCHEDULE:
             int daysLeft = carddb.cards.daysLeft();
             if (daysLeft < 0) {
                 dialog = showInfo(R.string.update_overdue_text, false);
 
             } else if (daysLeft == 0) {
                 dialog = showInfo(R.string.update_today_text, false);
 
             } else {
                 int[] indays = carddb.cards.getFutureSchedule();
                 if (indays != null) {
                     dialog = new Dialog(mContext);
                     dialog.setTitle(getString(R.string.schedule));
                     dialog.setContentView(R.layout.schedule);
                     TableLayout table = (TableLayout) dialog
                             .findViewById(R.id.schedule_table);
                     table.setPadding(10, 0, 10, 10);
 
                     for (int i = 0; i < indays.length; ++i) {
                         TableRow row = new TableRow(mContext);
 
                         TextView label = new TextView(mContext);
                         label.setText(getString(R.string.in_text)
                                 + " "
                                 + Integer.toString(i + 1)
                                 + " "
                                 + getString(i == 0 ? R.string.day_text
                                         : R.string.days_text) + ":");
                         label.setPadding(0, 0, 10, 2);
 
                         TextView value = new TextView(mContext);
                         value.setText(Integer.toString(indays[i]));
                         value.setGravity(android.view.Gravity.RIGHT);
 
                         row.addView(label);
                         row.addView(value);
                         table.addView(row);
                     }
                 }
             }
             dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 public void onDismiss(DialogInterface dialog) {
                     removeDialog(DIALOG_SCHEDULE);
                 }
             });
             dialog.setCanceledOnTouchOutside(true);
             break;
 
         case DIALOG_CATEGORIES:
             int num_categories = carddb.cards.numCategories();
             CharSequence[] items = new CharSequence[num_categories];
             boolean[] checked = new boolean[num_categories];
             
             for (int i=0; i < num_categories; ++i) {
                 items[i] = carddb.cards.getCategory(i);
                 checked[i] = !carddb.cards.skipCategory(i);
             }
 
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder
             .setTitle(getString(R.string.activate_categories))
             .setMultiChoiceItems(items, checked,
                 new DialogInterface.OnMultiChoiceClickListener() {
                     public void onClick(DialogInterface dialog,
                                         int item, boolean value) {
                         carddb.cards.setSkipCategory(item, !value);
                     }
             })
             .setCancelable(false)
             .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     carddb.writeCategorySkips();
                     carddb.cards.rebuildQueue();
                     nextQuestion();
                 }
             });
             
             dialog = (Dialog)builder.create();
             break;
 
         case DIALOG_STYLES:
             dialog = new Dialog(mContext);
             dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
             dialog.setContentView(R.layout.styles);
             dialog.setCanceledOnTouchOutside(true);            
 
             final SeekBar slider = (SeekBar) dialog.findViewById(R.id.slider);
             slider.setProgress(style);
 
             dialog.setOnDismissListener(
                     new DialogInterface.OnDismissListener () {
                         public void onDismiss(DialogInterface dialog) {
                             newStyle(slider.getProgress());
                         }
                     }
                 );
             break;
         }
 
         return dialog;
     }
 
     public void loadCards(String path)
     {
         removeDialog(DIALOG_CATEGORIES);
 
         saveCards();
         carddb.close();
 
         if (!path.endsWith(File.separator)) {
             path = path + File.separator;
         }
 
         if (!FindCardDirAndroid.isCardDir(path)) {
             carddb = new CardStore();
             cur_card = null;
             setMode(Mode.NO_CARDS);
             return;
         }
 
         if (demo_imgson_path_override != null) {
             sound_player.setBasePath(demo_imgson_path_override);
         } else {
             sound_player.setBasePath(path);
         }
 
         loadCardDB(path);
     }
 
     public void setCategory(String category)
     {
         TextView cat_title = (TextView) findViewById(R.id.category);
         cat_title.setText(category);
     }
 
     public void applyStyle(int style)
     {
         View frame = findViewById(R.id.card_webview_frame);
 
         switch (style) {
             case STYLE_DARK:
                 webview.setBackgroundColor(0xff000000);
                 frame.setBackgroundColor(0xff777777);
                 card_text_color = "lightgray";
                 card_back_color = "black";
                 break;
 
             case STYLE_DIMMED:
                 webview.setBackgroundColor(0xff777777);
                 frame.setBackgroundColor(0xff000000);
                 card_text_color = "white";
                 card_back_color = "#777777";
                 break;
 
             case STYLE_LIGHT:
                 webview.setBackgroundColor(0xffffffff);
                 frame.setBackgroundColor(0xff000000);
                 card_text_color = "black";
                 card_back_color = "white";
                 break;
 
         }
     }
 
     public void newStyle(int new_style)
     {
         style = new_style;
 
         SharedPreferences settings = PreferenceManager
             .getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = settings.edit();
         editor.putString("style", Integer.toString(style));
         editor.commit();
 
         applyStyle(style);
         html_pre = getCardHeader();
         refreshMode();
     }
 
     public void setNumLeft(int cards_left)
     {
         TextView cardsl_title = (TextView) findViewById(R.id.cards_left);
         cardsl_title.setText(Integer.toString(cards_left));
 
         if (carddb.active()) {
             int daysLeft = carddb.cards.daysLeft();
 
             if (daysLeft < 0) {
                 cardsl_title.setBackgroundColor(android.graphics.Color.RED);
                 cardsl_title.setTextColor(android.graphics.Color.BLACK);
             } else if (daysLeft == 0) {
                 cardsl_title.setBackgroundColor(android.graphics.Color.YELLOW);
                 cardsl_title.setTextColor(android.graphics.Color.BLACK);
             } else {
                 TextView cat_title = (TextView) findViewById(R.id.category);
                 cardsl_title.setBackgroundColor(
                     cat_title.getDrawingCacheBackgroundColor());
                 cardsl_title.setTextColor(cat_title.getCurrentTextColor());
             }
         }
     }
 
     public void setMode(Mode m)
     {
         setMode(m, true);
     }
 
     public void setMode(Mode m, boolean start_thinking)
     {
         StringBuffer html;
 
         if (m == Mode.NO_CARDS || m == Mode.NO_NEW_CARDS || !touch_buttons) {
             show_panel.setVisibility(View.GONE);
             grading_panel.setVisibility(View.GONE);
         } else {
             show_panel.setVisibility(m == Mode.SHOW_QUESTION ? View.INVISIBLE
                     : View.GONE);
             grading_panel
                     .setVisibility(m == Mode.SHOW_ANSWER ? View.INVISIBLE
                             : View.GONE);
         }
 
         mode = m;
         hidden_view = null;
 
         switch (m) {
         case SHOW_QUESTION:
             setNumLeft(carddb.numScheduled());
             if (cur_card != null) {
                 if (touch_buttons) {
                     hidden_view = show_panel;
                 }
                 loadCard(false, start_thinking);
             }
             break;
 
         case SHOW_ANSWER:
             if (start_thinking) {
                 pauseThinking();
             }
             setNumLeft(carddb.numScheduled());
             if (cur_card != null) {
                 if (touch_buttons) {
                     hidden_view = grading_panel;
                 }
                loadCard(true, start_thinking);
             }
             break;
 
         case NO_CARDS:
             html = new StringBuffer(html_pre);
             html.append("<body>");
 
             html.append("<div style=\"padding: 1ex;\"><p>");
             html.append(getString(R.string.no_cards_main));
             html.append("</p><ol>");
 
             html.append("<li style=\"padding-bottom: 2ex;\">");
             html.append(getString(R.string.no_cards_step1));
             html.append("</li>");
 
             html.append("<li>");
             html.append(getString(R.string.no_cards_step2));
             html.append("</li></ol></div>");
 
             html.append(html_post);
 
             setCategory(getString(R.string.no_cards_title));
             webview.loadDataWithBaseURL("", html.toString(), "text/html",
                     "UTF-8", "");
             break;
 
         case NO_NEW_CARDS:
             html = new StringBuffer(html_pre);
             html.append("<body>");
             html.append("<div style=\"padding: 1ex; text-align: center;\"><p>");
             html.append(getString(R.string.no_cards_left));
             html.append("</p></div>");
             if (carddb.canLearnAhead()) {
                 html.append("<input type=\"button\" value=\"");
                 html.append(getString(R.string.learn_ahead));
                 html.append("\" style=\"width: 100%; margin-top: 1em;\"");
                 html.append(" onclick=\"Mnemododo.learnAhead();\" />");
             }
             html.append(html_post);
 
             setCategory(getString(R.string.no_new_cards_title));
             setNumLeft(carddb.numScheduled());
             webview.loadDataWithBaseURL("", html.toString(), "text/html",
                     "UTF-8", "");
             break;
         }
     }
 
     public void refreshMode()
     {
         setMode(mode, false);
     }
 
     public void onClick(View v)
     {
         int click_id = v.getId();
         
         switch (mode) {
 
         case SHOW_QUESTION:
             if (click_id == R.id.show) {
                 if (auto_play) {
                     queueAnswerSounds();
                 }
                 setMode(Mode.SHOW_ANSWER);
 
             } else if (click_id == R.id.category) {
                 showDialog(DIALOG_STATS);
             }
             break;
 
         case SHOW_ANSWER:
             int grade = 0;
             for (int butid : grade_buttons) {
                 if (click_id == butid) {
                     break;
                 }
                 ++grade;
             }
 
             if (grade < grade_buttons.length) {
                 doGrade(grade);
                 nextQuestion();
             }
 
             if (click_id == R.id.category) {
                 showDialog(DIALOG_STATS);
             }
             break;
 
         default:
             break;
         }
 
         if (click_id == R.id.cards_left) {
             showDialog(DIALOG_SCHEDULE);
         }
 
     }
 
     protected boolean doGrade(int grade)
     {
         if (cur_card == null) {
             return false;
         }
 
         try {
             if (carddb.active()) {
                 carddb.cards.removeFromFutureSchedule(cur_card);
                 cur_card.gradeCard(carddb.cards.days_since_start, grade,
                         thinking_msecs, carddb.cards.logfile);
                 carddb.cards.addToFutureSchedule(cur_card);
                 carddb_dirty = true;
                 return true;
             } else {
                 return false;
             }
 
         } catch (IOException e) {
             showFatal(e.toString(), false);
             return false;
         }
     }
 
     protected void saveCards()
     {
         if (carddb_dirty) {
             try {
                 carddb.saveCards();
                 carddb_dirty = false;
             } catch (IOException e) {
                 showFatal(e.toString(), true);
             }
         }
     }
 
     protected boolean nextQuestion()
     {
         cur_card = carddb.cards.getCard();
 
         if (cur_card == null) {
             setMode(Mode.NO_NEW_CARDS);
             return false;
         }
 
         try {
             sound_player.clear();
             if (auto_play) {
                 queueQuestionSounds();
             }
             setMode(Mode.SHOW_QUESTION);
 
         } catch (Exception e) {
             showFatal(e.toString(), false);
             return false;
         }
 
         return true;
     }
 
     protected void startThinking()
     {
         thinking_from = new Date();
         thinking_msecs = 0;
     }
 
     protected void pauseThinking()
     {
         Date now = new Date();
 
         if (thinking_from != null) {
             thinking_msecs += now.getTime() - thinking_from.getTime();
             thinking_from = null;
         }
     }
 
     protected void unpauseThinking()
     {
         if (thinking_from == null) {
             thinking_from = new Date();
         }
     }
 
     protected void showFatal(String msg, final boolean exit)
     {
         new AlertDialog.Builder(this).setTitle(getString(R.string.fatal_error))
                 .setMessage(msg).setNegativeButton(R.string.ok,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog,
                                     int whichButton)
                             {
                                 if (exit) {
                                     finish();
                                 }
                             }
                         }).show();
     }
 
     protected String getCardHeader()
     {
         String tablecss = "";
         if (center) {
             tablecss = "margin-left: auto; margin-right: auto; ";
         }
         
         return
             "<html><head>"
             + "<style>"
             + "body { margin: 0px; padding: 0px; margin-top: 5px; "
             + "font-size: " + card_font_size + "; "
             + "color: " + card_text_color + "; "
             + "background-color: " + card_back_color + "; "
             + "}"
             + "div.q, div.a { padding-left: 5px; padding-right: 5px }"
             + "hr { width: 100%; height: 1px;"
             + "     background-color: " + card_text_color + " ; border: 0px }"
             + "h3 { margin: 0px; padding: 0px; padding-top: 1.5ex;"
             + "     font-size: normal; }"
             + "table { " + tablecss + "}"
             + "tr { font-size: " + card_font_size + "; }"
             + "</style>"
             + "<link rel=\"stylesheet\" href=\"STYLE.CSS\" type=\"text/css\">"
             + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
             + "</head>";
     }
 
     protected void queueQuestionSounds()
     {
         if (cur_card != null) {
             sound_player.queue(cur_card.getQuestionSounds());
         }
     }
 
     protected void queueAnswerSounds()
     {
         if (cur_card != null) {
             sound_player.queue(cur_card.getAnswerSounds());
         }
     }
 }
 
