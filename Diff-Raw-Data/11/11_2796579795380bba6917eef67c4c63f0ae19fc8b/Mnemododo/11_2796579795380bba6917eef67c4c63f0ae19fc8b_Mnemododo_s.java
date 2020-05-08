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
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class Mnemododo
     extends Activity
     implements OnClickListener
 {       
     enum Mode { SHOW_QUESTION, SHOW_ANSWER, NO_CARDS, NO_NEW_CARDS }
     Mode mode = Mode.SHOW_QUESTION;
     
     static final int DIALOG_ABOUT = 0;
     static final int DIALOG_STATS = 1;
     static final int DIALOG_SCHEDULE = 2;
 
     protected static final int MENU_SKIP = 0;
     protected static final int MENU_STATISTICS = 1;
     protected static final int MENU_SCHEDULE = 2;
     protected static final int MENU_SETTINGS = 3;
     protected static final int MENU_ABOUT = 4;
 
     protected static final int REQUEST_SETTINGS = 100;
 
     protected static final int KEY_GRADE0 = 0;
     protected static final int KEY_GRADE1 = 1;
     protected static final int KEY_GRADE2 = 2;
     protected static final int KEY_GRADE3 = 3;
     protected static final int KEY_GRADE4 = 4;
     protected static final int KEY_GRADE5 = 5;
     protected static final int KEY_SHOW_ANSWER = 6;
     
     protected String html_pre = "<html><body>";
     protected static final String html_post = "</body></html>";
         
     /* data */      
     protected HexCsvAndroid carddb;
     boolean carddb_dirty = false;
 
     protected Card cur_card;
     private Date thinking_from;
     private long thinking_msecs = 0;
     
     /* UI */
     Runtime rt = Runtime.getRuntime();
     WebView webview;
     
     int[] grade_buttons = {R.id.grade0, R.id.grade1, R.id.grade2,
                            R.id.grade3, R.id.grade4, R.id.grade5};
     int[] other_buttons = {R.id.show};
 
     View grading_buttons;
     View show_buttons;
     View ok_buttons;
     View hidden_view = null;
 
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
                 
     /* Tasks */
         
 	private class LoadStatsTask
 		extends ProgressTask<String, Boolean>
 	{
 		protected HexCsvAndroid loaddb;
 		protected String error_msg;
 
 		protected String getMessage()
 		{
 			return getString(R.string.loading_card_dir);
 		}
 
 		protected Context getContext()
 		{
 			return Mnemododo.this;
 		}
 
 		public Boolean doInBackground(String... path)
 		{
 			try {
 				loaddb = new HexCsvAndroid(path[0], LoadStatsTask.this);
 				loaddb.cards_to_load = cards_to_load;
 
 			} catch (Exception e) {
 				error_msg = getString(R.string.corrupt_card_dir) + "\n\n("
 						+ e.toString() + ")";
 				stopOperation();
 				return false;
 
 			} catch (OutOfMemoryError e) {
 				error_msg = getString(R.string.not_enough_memory_to_load);
 				stopOperation();
 				return false;
 			}
 
 			stopOperation();
 			return true;
 		}
 
 		public void onPostExecute(Boolean result)
 		{
 			if (result) {
 				carddb = loaddb;
 				carddb_dirty = false;
                 try {
                     carddb.backupCards(new StringBuffer(cards_path), null);
                 } catch (IOException e) { }
 				nextQuestion();
 			} else {
 				carddb = null;
 				setMode(Mode.NO_CARDS);
 				showFatal(error_msg, false);
 			}
 		}
 	}
         
     private class LoadCardTask extends ProgressTask<Boolean, String>
     {
         Card card;
         boolean is_question;
         boolean start_thinking;
 
         protected String getMessage()
         {
             return getString(R.string.loading_cards);
         }
 
         protected Context getContext(){
             return Mnemododo.this;
         }
 
         public void onPreExecute()
         {
             style = ProgressDialog.STYLE_HORIZONTAL;
             carddb.setProgress(LoadCardTask.this);
             card = cur_card;
         }
 
         public String doInBackground(Boolean... options)
         {
             is_question = !options[0];
             if (options.length > 1) {
                 start_thinking = options[1];
             } else {
                 start_thinking = is_question;
             }
 
             String html = makeCardHtml(card, !is_question);
             stopOperation();
             return html;
         }
 
         public void onPostExecute(String html)
         {
             setCategory(cur_card.categoryName());
             webview.loadDataWithBaseURL("file://" + cards_path, html,
                     "text/html", "UTF-8", "");
 
             if (start_thinking && (cur_card != null)) {
                 startThinking();
             }
 
             if (hidden_view != null && cur_card != null) {
                 handler.removeCallbacks(makeViewVisible);
                 handler.postDelayed(makeViewVisible, make_visible_delay);
             }
         }
     }
 
     /* Configuration */
     
     String cards_path = null;
     int cards_to_load = 50;
     boolean center = true;
     boolean touch_buttons = true;
     String card_font_size = "normal";
     String card_font = "";
     int[] key;
     
     int make_visible_delay = 400;
     int make_visible_fade_delay = 400;
 
     /** Called when the activity is first created. */
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         // Setup UI specifics
         webview = (WebView) findViewById(R.id.card_webview);
         grading_buttons = findViewById(R.id.grading_buttons);
         show_buttons = findViewById(R.id.show_buttons);
 
         for (int butid : grade_buttons) {
             Button button = (Button) findViewById(butid);
             button.setOnClickListener(this);
         }
 
         for (int butid : other_buttons) {
             Button button = (Button) findViewById(butid);
             button.setOnClickListener(this);
         }
         
         findViewById(R.id.cards_left).setOnClickListener(this);
         findViewById(R.id.category).setOnClickListener(this);
 
         // Get settings and load cards if necessary
         loadPrefs();
 
         // Animation
         buttonAnimation = new AlphaAnimation(0.0f, 1.0f);
         buttonAnimation.setDuration(make_visible_fade_delay);
     }
 
     public void loadPrefs()
     {
         SharedPreferences settings = PreferenceManager
                 .getDefaultSharedPreferences(this);
 
         // store default prefs if necessary
         if (!settings.contains("center")) {
             SharedPreferences.Editor editor = settings.edit();
             editor.putBoolean("center", center);
             editor.putBoolean("touch_buttons", touch_buttons);
             editor.putString("card_font_size", card_font_size);
             editor.putString("cards_to_load", Integer.toString(cards_to_load));
             editor.commit();
         }
 
         // load prefs
         cards_to_load = Integer.parseInt(settings.getString("cards_to_load", "50"));
         touch_buttons = settings.getBoolean("touch_buttons", true);
 
         boolean ncenter = settings.getBoolean("center", true);
         String ncard_font = settings.getString("card_font", "");
         String ncard_font_size = settings.getString("card_font_size", "normal");
 
         boolean reload = (center != ncenter) || (!card_font.equals(ncard_font))
                 || (!card_font_size.equals(ncard_font_size));
         center = ncenter;
         card_font = ncard_font;
         card_font_size = ncard_font_size;
         html_pre = getCardHeader();
         
         key = new int[KEY_SHOW_ANSWER + 1];
         key[KEY_GRADE0] = settings.getInt("key_grade0", KeyEvent.KEYCODE_0);
         key[KEY_GRADE1] = settings.getInt("key_grade1", KeyEvent.KEYCODE_1);
         key[KEY_GRADE2] = settings.getInt("key_grade2", KeyEvent.KEYCODE_2);
         key[KEY_GRADE3] = settings.getInt("key_grade3", KeyEvent.KEYCODE_3);
         key[KEY_GRADE4] = settings.getInt("key_grade4", KeyEvent.KEYCODE_4);
         key[KEY_GRADE5] = settings.getInt("key_grade5", KeyEvent.KEYCODE_5);
         key[KEY_SHOW_ANSWER] = settings.getInt("key_show_answer", KeyEvent.KEYCODE_9);
 
         String settings_cards_path = settings.getString("cards_path", null);
         if (settings_cards_path != null
                 && !settings_cards_path.endsWith(File.separator)) {
             settings_cards_path = settings_cards_path + File.separator;
         }
         
         boolean will_load_cards =
             (cards_path == null && settings_cards_path != null)
             || (cards_path != null
                 && settings_cards_path != null
                 && !cards_path.equals(settings_cards_path));
                 
         if (touch_buttons && !will_load_cards) {
             show_buttons
                     .setVisibility(mode == Mode.SHOW_QUESTION ? View.VISIBLE
                             : View.GONE);
             grading_buttons
                     .setVisibility(mode == Mode.SHOW_ANSWER ? View.VISIBLE
                             : View.GONE);
         } else {
             show_buttons.setVisibility(View.GONE);
             grading_buttons.setVisibility(View.GONE);
         }
 
         if (will_load_cards) {
             setCardDir(settings_cards_path);
             reload = false;
         } else if (cards_path == null) {
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
                 new LoadCardTask().execute(mode == Mode.SHOW_ANSWER, false);
             }
         }
     }
 
     public boolean onKeyUp(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_MENU
                 || keyCode == KeyEvent.KEYCODE_BACK) {
             return false;
         }
 
         if (keyCode == key[KEY_SHOW_ANSWER]) {
             onClick(findViewById(R.id.show));
 
         } else if (keyCode >= key[KEY_GRADE0]) {
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
 
         } else {
             return false;
         }
 
         return true;
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);
 
         if ((requestCode == REQUEST_SETTINGS) && (resultCode == RESULT_OK)) {
             loadPrefs();
         }
     }
 
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem skip = menu.findItem(MENU_SKIP);
         MenuItem stats = menu.findItem(MENU_STATISTICS);
         MenuItem sched = menu.findItem(MENU_SCHEDULE);
 
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
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     public boolean onCreateOptionsMenu(Menu menu)
     {
         menu.add(0, MENU_SKIP, 0, getString(R.string.skip_card)).setIcon(
                 R.drawable.icon_skip);
 
         menu.add(0, MENU_STATISTICS, 0, getString(R.string.statistics))
                 .setIcon(R.drawable.icon_stats);
 
         menu.add(0, MENU_SCHEDULE, 0, getString(R.string.future_schedule))
                 .setIcon(R.drawable.icon_schedule);
 
         menu.add(0, MENU_SETTINGS, 0, getString(R.string.settings)).setIcon(
                 android.R.drawable.ic_menu_preferences);
 
         menu.add(0, MENU_ABOUT, 0, getString(R.string.info)).setIcon(
                 android.R.drawable.ic_menu_info_details);
 
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
 
         case MENU_SETTINGS:
             Intent settings_intent = new Intent();
             settings_intent.setClass(this, Settings.class);
             startActivityForResult(settings_intent, REQUEST_SETTINGS);
             return true;
 
         case MENU_ABOUT:
             showDialog(DIALOG_ABOUT);
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     public void onResume()
     {
         super.onResume();
        pauseThinking();
     }
 
     public void onPause()
     {
         super.onPause();
         pauseThinking();
         saveCards();
     }
 
     public void onDestroy()
     {
         super.onDestroy();
         if (carddb != null) {
             carddb.close();
         }
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
                             Mnemododo.this.finish();
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
             if (cur_card == null) {
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
                     .daysSinceLastRep(carddb.days_since_start)));
 
             text = (TextView) dialog
                     .findViewById(R.id.days_until_next_repetition);
             text.setText(Integer.toString(cur_card
                     .daysUntilNextRep(carddb.days_since_start)));
 
             break;
         }
     }
     
     protected Dialog onCreateDialog(int id)
     {
         Dialog dialog = null;
 
         // Context mContext = getApplicationContext();
         Context mContext = Mnemododo.this;
 
         switch (id) {
         case DIALOG_ABOUT:
             dialog = new Dialog(mContext);
             dialog.setContentView(R.layout.about);
             dialog.setTitle(getString(R.string.app_name) + " "
                     + getString(R.string.app_version));
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
             int daysLeft = carddb.daysLeft();
             if (daysLeft < 0) {
                 dialog = showInfo(R.string.update_overdue_text, false);
 
             } else if (daysLeft == 0) {
                 dialog = showInfo(R.string.update_today_text, false);
 
             } else {
                 int[] indays = carddb.getFutureSchedule();
                 if (indays != null) {
                     dialog = new Dialog(mContext);
                     dialog.setTitle(getString(R.string.for_days_text));
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
         }
 
         return dialog;
     }
 
     public void setCardDir(String path)
     {
         if (!path.endsWith(File.separator)) {
             path = path + File.separator;
         }
 
         if (!FindCardDirAndroid.isCardDir(new File(path))) {
             cards_path = null;
             cur_card = null;
             setMode(Mode.NO_CARDS);
             return;
         }
 
         cards_path = path;
 
         saveCards();
         if (carddb != null) {
             carddb.close();
         }
 
         new LoadStatsTask().execute(path);
     }
 
     public void setCategory(String category)
     {
         TextView cat_title = (TextView) findViewById(R.id.category);
         cat_title.setText(category);
     }
 
     public void setNumLeft(int cards_left)
     {
         TextView cardsl_title = (TextView) findViewById(R.id.cards_left);
         cardsl_title.setText(Integer.toString(cards_left));
         
         cardsl_title.setBackgroundColor(android.graphics.Color.BLACK);
         if (carddb != null) {
             int daysLeft = carddb.daysLeft();
 
             if (daysLeft < 0) {
                 cardsl_title.setBackgroundColor(android.graphics.Color.RED);
                 cardsl_title.setTextColor(android.graphics.Color.BLACK);
             } else if (daysLeft == 0) {
                 cardsl_title.setBackgroundColor(android.graphics.Color.YELLOW);
                 cardsl_title.setTextColor(android.graphics.Color.BLACK);
             }
         }
     }
 
     public void setMode(Mode m)
     {
         StringBuffer html;
 
         if (m == Mode.NO_CARDS || m == Mode.NO_NEW_CARDS || !touch_buttons) {
             show_buttons.setVisibility(View.GONE);
             grading_buttons.setVisibility(View.GONE);
         } else {
             show_buttons.setVisibility(m == Mode.SHOW_QUESTION ? View.INVISIBLE
                     : View.GONE);
             grading_buttons
                     .setVisibility(m == Mode.SHOW_ANSWER ? View.INVISIBLE
                             : View.GONE);
         }
 
         mode = m;
         hidden_view = null;
 
         switch (m) {
         case SHOW_QUESTION:
             if (cur_card != null) {
                 if (touch_buttons) {
                     hidden_view = show_buttons;
                 }
                 new LoadCardTask().execute(false);
             }
             break;
 
         case SHOW_ANSWER:
             if (cur_card != null) {
                 if (touch_buttons) {
                     hidden_view = grading_buttons;
                 }
                 new LoadCardTask().execute(true);
             }
             break;
 
         case NO_CARDS:
             html = new StringBuffer(html_pre);
 
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
             html.append("<div style=\"padding: 1ex; text-align: center;\"><p>");
             html.append(getString(R.string.no_cards_left));
             html.append("</p></div>");
             html.append(html_post);
 
             setCategory(getString(R.string.no_new_cards_title));
             webview.loadDataWithBaseURL("", html.toString(), "text/html",
                     "UTF-8", "");
             break;
         }
     }
 
     public void onClick(View v)
     {
         int click_id = v.getId();
         
         switch (mode) {
 
         case SHOW_QUESTION:
             if (click_id == R.id.show) {
                 setMode(Mode.SHOW_ANSWER);
 
             } else if (click_id == R.id.cards_left) {
                 showDialog(DIALOG_SCHEDULE);
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
             } else if (click_id == R.id.cards_left) {
                 showDialog(DIALOG_SCHEDULE);
             } else if (click_id == R.id.category) {
                 showDialog(DIALOG_STATS);
             }
             break;
 
         default:
             break;
         }
     }
 
     protected boolean doGrade(int grade)
     {
         if (cur_card == null) {
             return false;
         }
 
         try {
             cur_card.gradeCard(carddb.days_since_start, grade, thinking_msecs,
                     carddb.logfile);
             carddb.updateFutureSchedule(cur_card);
             carddb_dirty = true;
             return true;
 
         } catch (IOException e) {
             showFatal(e.toString(), false);
             return false;
         }
     }
 
     protected void saveCards()
     {
         if ((carddb != null) && (cards_path != null) && carddb_dirty) {
             try {
                 carddb.writeCards(new StringBuffer(cards_path), null);
                 carddb_dirty = false;
             } catch (IOException e) {
                 showFatal(e.toString(), true);
             }
         }
     }
 
     protected boolean nextQuestion()
     {
         cur_card = carddb.getCard();
 
         if (cur_card == null) {
             setMode(Mode.NO_NEW_CARDS);
             return false;
         }
 
         try {
             setNumLeft(carddb.numScheduled());
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
 
    protected long stopThinking()
    {
        pauseThinking();
        return thinking_msecs;
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
         return
             "<html><head>"
             + "<style>"
             + "body { margin: 0px; padding: 0px; margin-top: 5px; "
             + "font-size: " + card_font_size + "; "
             + "}"
             + "div.q, div.a { padding-left: 5px; padding-right: 5px }"
             + "hr { width: 100%; height: 1px;"
             + "     background-color: black; border: 0px }"
             + "h3 { margin: 0px; padding: 0px; padding-top: 1.5ex;"
             + "     font-size: normal; }"
             + "</style>"
             + "<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\">"
             + "</head><body>";
     }
 
     protected String makeCardHtml(Card c, boolean show_answer)
     {
         StringBuffer html = new StringBuffer(html_pre);
 
         String question = c.getQuestion();
         String answer = c.getAnswer();
 
         if (center) {
             html.append("<div style=\"text-align: center;\">");
         }
 
         if (question == null || answer == null) {
             html.append(getString(R.string.no_card_loaded_text));
 
         } else if (show_answer) {
             if (!cur_card.getOverlay()) {
                 html.append("<div class=\"q\">");
                 html.append(question);
                 html.append("</div><hr/>");
             }
             html.append("<div class=\"a\">");
             html.append(answer);
             html.append("</div>");
 
         } else {
             html.append("<div class=\"q\">");
             html.append(question);
             html.append("</div>");
         }
 
         if (center) {
             html.append("</div>");
         }
 
         html.append(html_post);
 
         return html.toString();
     }
 
 }
