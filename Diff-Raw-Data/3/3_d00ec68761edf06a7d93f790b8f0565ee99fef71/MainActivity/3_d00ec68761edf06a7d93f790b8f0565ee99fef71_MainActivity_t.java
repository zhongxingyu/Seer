 /* Copyright (C) 2012  Aracs LLC
  * 
  * This file is part of Holy Quran Android Application.
  * 
  * Holy Quran Android Application is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Holy Quran Android Application is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.aracssoftware.quran;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.aracssoftware.quran.lib.DatabaseHelper;
 
 public class MainActivity extends Activity {
 	Context context;
 	DatabaseHelper dbH;
 	Cursor cTrans;
 	ListView listTrans;
 	TextView txtAppName;
 	CheckBox checkSaveLang;
 	SharedPreferences settings;
 	SharedPreferences.Editor editor;
 	Typeface fontEvo;
 	int lang, trans=14;
 	//String lang, trans;
 	boolean isFirstRun, saveLang, saveTrans, askLang;
 	String[][] strings = { { "Qurani Kərim", "Yadda saxla" },
 			{ "Священный Коран", "Запомни" } };
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		context = this;
 
 		settings = getSharedPreferences("QuraniKerim", 0);
 		settings = PreferenceManager.getDefaultSharedPreferences(context);
 		
 		editor = settings.edit();
 		isFirstRun = settings.getBoolean("isFirstRun", true);
 		if (isFirstRun) { 
 			editor.putBoolean("save_lang", true);
 		}
 		saveLang = settings.getBoolean("save_lang", true); 
 		askLang = isFirstRun || !saveLang;
 		//lang = settings.getInt("lang", -1);
 		//trans = settings.getInt("trans", -1);
 		lang = Integer.valueOf(settings.getString("lang", "0"));
		//trans = settings.getInt("trans", 14);
		trans = Integer.valueOf(settings.getString("trans", "14"));
 
 		fontEvo = Typeface.createFromAsset(getAssets(), "Evo.otf");
 
 		checkSaveLang = (CheckBox) findViewById(R.id.checkSaveLang);
 		txtAppName = (TextView) findViewById(R.id.txtAppName);
 		listTrans = (ListView) findViewById(R.id.listTrans);
 		checkSaveLang.setTypeface(fontEvo);
 
 		if (isFirstRun) {
 			//Toast.makeText(context, "first run", 1).show();
 			Intent InitIntent = new Intent(context, InitActivity.class);
 			startActivityForResult(InitIntent, 1);
 
 		} else {
 			askLangIfNotChosen();
 		}
 
 		listTrans.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int pos,
 					long id) {
 
 				if (checkSaveLang.isChecked()) {
 					editor.putBoolean("save_trans", true);
 					//editor.putInt("trans", (int) id);
 					editor.putString("string", String.valueOf((int) id));
 					editor.commit();
 				}
 
 				Intent MainMenuIntent = new Intent(context,
 						MainMenuActivity.class);
 				MainMenuIntent.putExtra("trans", trans); // TODO un-fix 14
 				MainMenuIntent.putExtra("lang", lang); // TODO un-fix 0
 				startActivity(MainMenuIntent);
 				finish();
 			}
 		});
 
 	}
 
 	public void doNotFirstRun() {
 
 		dbH = new DatabaseHelper(context);
 
 		saveTrans = settings.getBoolean("save_trans", false);
 
 		if (saveTrans) {
 			Intent MainMenuIntent = new Intent(this, MainMenuActivity.class);
 			MainMenuIntent.putExtra("lang", lang);
 			MainMenuIntent.putExtra("trans", trans);
 			startActivity(MainMenuIntent);
 			finish();
 		} else {
 
 			prepareTransList();
 
 		}
 
 		if (txtAppName != null) {
 			txtAppName.setTypeface(fontEvo);
 			txtAppName.setTextSize(40);
 
 		}
 	}
 
 	public void askLangIfNotChosen() {
 		if (askLang) {
 			final CharSequence[] langs = { "Azərbaycanca", "На русском" };
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Pick a language");
 			builder.setItems(langs, new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					lang = which;
 					//editor.putInt("lang", which);
 					editor.putString("lang", String.valueOf(which));
 					editor.commit();
 					txtAppName.setText(strings[lang][0]);
 					checkSaveLang.setText(strings[lang][1]);
 					doNotFirstRun();
 				}
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		} else {
 			doNotFirstRun();
 		}
 	}
 
 	public void prepareTransList() {
 		cTrans = dbH.getTranslations();
 		startManagingCursor(cTrans);
 		String[] columns = new String[] { "name", "author" };
 		int[] to = new int[] { R.id.txtLang, R.id.txtAuthor };
 		SimpleCursorAdapter transAdapter = new SimpleCursorAdapter(context,
 				R.layout.trans_item, cTrans, columns, to);
 		listTrans.setAdapter(transAdapter);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		// TODO if
 		editor.putBoolean("isFirstRun", false);
 		editor.commit();
 		isFirstRun = false;
 		askLangIfNotChosen();
 		// Toast.makeText(context, "initialized - notFistRun", 1).show();
 
 	}
 }
