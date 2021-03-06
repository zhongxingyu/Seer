 /*
  * Copyright (C) 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.ase.activity;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.DataSetObserver;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.TypedValue;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.ase.ActivityFlinger;
 import com.google.ase.AseAnalytics;
 import com.google.ase.AseLog;
 import com.google.ase.Constants;
 import com.google.ase.IntentBuilders;
 import com.google.ase.R;
 import com.google.ase.ScriptStorageAdapter;
 import com.google.ase.dialog.Help;
 import com.google.ase.dialog.UsageTrackingConfirmation;
 import com.google.ase.interpreter.Interpreter;
 import com.google.ase.interpreter.InterpreterConfiguration;
 
 /**
  * Manages creation, deletion, and execution of stored scripts.
  * 
  * @author Damon Kohler (damonkohler@gmail.com)
  */
 public class ScriptManager extends ListActivity {
 
   private List<File> mScriptList;
   private ScriptManagerAdapter mAdapter;
   private SharedPreferences mPreferences;
   private HashMap<Integer, Interpreter> mAddMenuIds;
 
   private static enum RequestCode {
     INSTALL_INTERPETER, QRCODE_ADD
   }
 
   private static enum MenuId {
     DELETE, EDIT, START_SERVICE, HELP, QRCODE_ADD, INTERPRETER_MANAGER, PREFERENCES, LOGCAT_VIEWER,
     TRIGGER_MANAGER, REFRESH;
     public int getId() {
       return ordinal() + Menu.FIRST;
     }
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     CustomizeWindow.requestCustomTitle(this, "Scripts", R.layout.script_manager);
     mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
     mScriptList =
         ScriptStorageAdapter.listScripts(mPreferences.getBoolean("show_all_files", false));
     mAdapter = new ScriptManagerAdapter();
     mAdapter.registerDataSetObserver(new ScriptListObserver());
     setListAdapter(mAdapter);
     registerForContextMenu(getListView());
     UsageTrackingConfirmation.show(this);
     ActivityFlinger.attachView(getListView(), this);
     ActivityFlinger.attachView(getWindow().getDecorView(), this);
     setResult(RESULT_CANCELED); // Default to canceled if we were started for result.
     AseAnalytics.trackActivity(this);
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     mAdapter.notifyDataSetInvalidated();
   }
 
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     super.onPrepareOptionsMenu(menu);
     menu.clear();
     buildMenuIdMaps();
     buildAddMenu(menu);
     buildSwitchActivityMenu(menu);
     menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences").setIcon(
         android.R.drawable.ic_menu_preferences);
     menu.add(Menu.NONE, MenuId.HELP.getId(), Menu.NONE, "Help").setIcon(
         android.R.drawable.ic_menu_help);
     menu.add(Menu.NONE, MenuId.REFRESH.getId(), Menu.NONE, "Refresh").setIcon(
         R.drawable.ic_menu_refresh);
     return true;
   }
 
   private void buildSwitchActivityMenu(Menu menu) {
     Menu subMenu =
         menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "View").setIcon(
             android.R.drawable.ic_menu_more);
     subMenu.add(Menu.NONE, MenuId.INTERPRETER_MANAGER.getId(), Menu.NONE, "Interpreters");
     subMenu.add(Menu.NONE, MenuId.TRIGGER_MANAGER.getId(), Menu.NONE, "Triggers");
     subMenu.add(Menu.NONE, MenuId.LOGCAT_VIEWER.getId(), Menu.NONE, "Logcat");
   }
 
   private void buildMenuIdMaps() {
     mAddMenuIds = new HashMap<Integer, Interpreter>();
     int i = MenuId.values().length + Menu.FIRST;
     List<Interpreter> installed = InterpreterConfiguration.getInstalledInterpreters();
     for (Interpreter interpreter : installed) {
       mAddMenuIds.put(i, interpreter);
       ++i;
     }
   }
 
   private void buildAddMenu(Menu menu) {
     Menu addMenu =
         menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "Add").setIcon(
             android.R.drawable.ic_menu_add);
     for (Entry<Integer, Interpreter> entry : mAddMenuIds.entrySet()) {
       addMenu.add(Menu.NONE, entry.getKey(), Menu.NONE, entry.getValue().getNiceName());
     }
     addMenu.add(Menu.NONE, MenuId.QRCODE_ADD.getId(), Menu.NONE, "Scan Barcode");
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     int itemId = item.getItemId();
     if (itemId == MenuId.HELP.getId()) {
       Help.show(this);
     } else if (itemId == MenuId.INTERPRETER_MANAGER.getId()) {
       // Show interpreter manger.
       Intent i = new Intent(this, InterpreterManager.class);
       startActivity(i);
     } else if (mAddMenuIds.containsKey(itemId)) {
       // Add a new script.
       Intent intent = new Intent(Constants.ACTION_EDIT_SCRIPT);
       Interpreter interpreter = mAddMenuIds.get(itemId);
       intent.putExtra(Constants.EXTRA_SCRIPT_NAME, interpreter.getExtension());
       intent.putExtra(Constants.EXTRA_SCRIPT_CONTENT, interpreter.getContentTemplate());
       intent.putExtra(Constants.EXTRA_IS_NEW_SCRIPT, true);
       startActivity(intent);
     } else if (itemId == MenuId.QRCODE_ADD.getId()) {
       Intent intent = new Intent("com.google.zxing.client.android.SCAN");
       startActivityForResult(intent, RequestCode.QRCODE_ADD.ordinal());
     } else if (itemId == MenuId.PREFERENCES.getId()) {
       startActivity(new Intent(this, AsePreferences.class));
     } else if (itemId == MenuId.TRIGGER_MANAGER.getId()) {
       startActivity(new Intent(this, TriggerManager.class));
     } else if (itemId == MenuId.LOGCAT_VIEWER.getId()) {
       startActivity(new Intent(this, LogcatViewer.class));
     } else if (itemId == MenuId.REFRESH.getId()) {
       mAdapter.notifyDataSetInvalidated();
     }
     return true;
   }
 
   @Override
   protected void onListItemClick(ListView list, View view, int position, long id) {
     File script = (File) list.getItemAtPosition(position);
     startActivity(IntentBuilders.buildStartInTerminalIntent(script.getName()));
   }
 
   /**
    * Opens the script for editing.
    * 
    * @param scriptName
    *          the name of the script to edit
    */
   private void editScript(String scriptName) {
     Intent i = new Intent(Constants.ACTION_EDIT_SCRIPT);
     i.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
     startActivity(i);
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
     menu.add(Menu.NONE, MenuId.EDIT.getId(), Menu.NONE, "Edit");
     menu.add(Menu.NONE, MenuId.DELETE.getId(), Menu.NONE, "Delete");
     menu.add(Menu.NONE, MenuId.START_SERVICE.getId(), Menu.NONE, "Start in Background");
   }
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     AdapterView.AdapterContextMenuInfo info;
     try {
       info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
     } catch (ClassCastException e) {
       AseLog.e("Bad menuInfo", e);
       return false;
     }
 
     File script = (File) mAdapter.getItem(info.position);
     if (script == null) {
       AseLog.v("No script selected.");
       return false;
     }
 
     int itemId = item.getItemId();
     if (itemId == MenuId.DELETE.getId()) {
       deleteScript(script.getName());
     } else if (itemId == MenuId.EDIT.getId()) {
       editScript(script.getName());
     } else if (itemId == MenuId.START_SERVICE.getId()) {
       Intent intent = new Intent(this, AseService.class);
       intent.setAction(Constants.ACTION_LAUNCH_SCRIPT);
       intent.putExtra(Constants.EXTRA_SCRIPT_NAME, script.getName());
       startService(intent);
     }
     return true;
   }
 
   private void deleteScript(final String scriptName) {
     AlertDialog.Builder alert = new AlertDialog.Builder(this);
     alert.setTitle("Delete Script");
     alert.setMessage("Would you like to delete " + scriptName + "?");
     alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         ScriptStorageAdapter.deleteScript(scriptName);
         mAdapter.notifyDataSetInvalidated();
       }
     });
     alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         // Ignore.
       }
     });
     alert.show();
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     RequestCode request = RequestCode.values()[requestCode];
     if (resultCode == RESULT_OK) {
       switch (request) {
       case QRCODE_ADD:
        String contents[] = data.getStringExtra("SCAN_RESULT").split("\n", 2);
        String title = contents[0];
        String body = contents[1];
        ScriptStorageAdapter.writeScript(title, body);
         break;
       default:
         break;
       }
     } else {
       switch (request) {
       case QRCODE_ADD:
         break;
       default:
         break;
       }
     }
     mAdapter.notifyDataSetInvalidated();
   }
 
   private class ScriptListObserver extends DataSetObserver {
     @Override
     public void onInvalidated() {
       mScriptList =
           ScriptStorageAdapter.listScripts(mPreferences.getBoolean("show_all_files", false));
     }
   }
 
   private class ScriptManagerAdapter extends BaseAdapter {
 
     @Override
     public int getCount() {
       return mScriptList.size();
     }
 
     @Override
     public Object getItem(int position) {
       return mScriptList.get(position);
     }
 
     @Override
     public long getItemId(int position) {
       return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       TextView view = new TextView(ScriptManager.this);
       view.setPadding(2, 2, 2, 2);
       view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
       view.setText(mScriptList.get(position).getName());
       return view;
     }
   }
 }
