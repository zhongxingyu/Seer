 /*
    Copyright 2011-2012 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android;
 
 import java.io.IOException;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxieq.FFXICharacter;
 import com.github.kanata3249.ffxieq.R;
 import com.github.kanata3249.ffxieq.android.db.FFXIDatabase;
 import com.github.kanata3249.ffxieq.android.db.FFXIEQSettings;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager.LayoutParams;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class FFXIEQActivity extends TabActivity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setContentView(R.layout.ffxieqactivity);
 
 		if (getSettings().useExternalDB()) {
 			// check SD card...
 			String status = Environment.getExternalStorageState();
 			if (status.equals(Environment.MEDIA_MOUNTED)) {
 				// valid status for read
 			} else {
 				showDialog(R.string.SDNotMounted);
 				return;
 			}
 		}
 		setupSubViews();
 	}
 	
 	void setupSubViews() {
         Resources res = getResources();
 		TabHost tabHost = getTabHost();
 		TabHost.TabSpec spec;
 		Intent intent;
 		
 		/* make sure that Database is exist */
 		getDAO();
 		
 		intent = new Intent().setClass(this, EquipmentSetEditActivity.class);
 		spec = tabHost.newTabSpec("equipment");
 		spec.setIndicator("Equipment", res.getDrawable(android.R.drawable.ic_menu_edit));
 		spec.setContent(intent);
 		tabHost.addTab(spec);
 		
 		if (!EquipmentSetEditActivity.hasAllView(tabHost.getCurrentView())) {
 			intent = new Intent().setClass(this, BasicEditActivity.class);
 			spec = tabHost.newTabSpec("basic");
 			spec.setIndicator("Basic", res.getDrawable(android.R.drawable.ic_menu_edit));
 			spec.setContent(intent);
 			tabHost.addTab(spec);
 			
 			intent = new Intent().setClass(this, MagicSetEditActivity.class);
 			spec = tabHost.newTabSpec("magic");
 			spec.setIndicator("Magic", res.getDrawable(android.R.drawable.ic_menu_edit));
 			spec.setContent(intent);
 			tabHost.addTab(spec);
 
 			intent = new Intent().setClass(this, CharacterStatusActivity.class);
 			spec = tabHost.newTabSpec("status");
 			spec.setIndicator("Status", res.getDrawable(android.R.drawable.ic_menu_view));
 			spec.setContent(intent);
 			tabHost.addTab(spec);
 			
 			tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
 				public void onTabChanged(String tabId) {
 					InputMethodManager imm;
 					
 					imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 					if (imm != null && getCurrentFocus() != null) {
 						imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
 					}
 					if (tabId.equals("equipment")) {
 						EquipmentSetEditActivity activity;
 
 						activity = (EquipmentSetEditActivity)getCurrentActivity();
 						activity.notifyDatasetChanged();
 					} else if (tabId.equals("basic")) {
 						BasicEditActivity activity;
 
 						activity = (BasicEditActivity)getCurrentActivity();
 						activity.notifyDatasetChanged();
 					} else if (tabId.equals("magic")) {
 						MagicSetEditActivity activity;
 
 						activity = (MagicSetEditActivity)getCurrentActivity();
 						activity.notifyDatasetChanged();
 					} else if (tabId.equals("status")) {
 						CharacterStatusActivity activity;
 
 						activity = (CharacterStatusActivity)getCurrentActivity();
 		//				activity.setDisplayParam(mDisplayParam, mShowSkillValue);
 						activity.notifyDatasetChanged();
 					}
 				}
 			});
 		} else {
 			/* disable tab */
 			getTabWidget().setVisibility(View.GONE);
 		}
 		updateValues();
 	}
 	
     @Override
     public void onStart() {
     	super.onStart();
 
         {
         	Button btn;
         	
         	btn = (Button)findViewById(R.id.Save);
         	if (btn != null) {
         		btn.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View arg0) {
 						((FFXIEQBaseActivity)getCurrentActivity()).saveValues();
 						showDialog(R.layout.querysavecharacter);
 					}
         		});
         	}
         }
         
         {
         	CheckBox cb;
         	
         	cb = (CheckBox)findViewById(R.id.Compare);
         	if (cb != null) {
         		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 						if (arg1) {
 							CharacterSelectorView cs = (CharacterSelectorView)findViewById(R.id.CharacterSelectorToCompare);
 							long id;
 							
 							id = cs.getSelectedItemId();
 			    			setCharacterIDToCompare(id);
 			    			setFFXICharacterToCompare(getSettings().loadCharInfo(id));						
 			    			updateSubViewValues();
 						} else {
 			    			setCharacterIDToCompare(-1);
 			    			setFFXICharacterToCompare(null);
 			    			updateSubViewValues();
 						}
 					}
         		});
        		cb.setChecked(getCharacterIDToCompare() != -1);
         	}
         }
         
         {
         	CharacterSelectorView cs;
         	
         	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
         	if (cs != null) {
     	        AdapterView.OnItemSelectedListener listener;
 
     	    	listener = new AdapterView.OnItemSelectedListener() {
     				public void onItemSelected(AdapterView<?> arg0, View arg1,
     						int arg2, long arg3) {
     					if (arg3 != getCharacterID()) {
 	    					if (getFFXICharacter().isModified()) {
 	    						showDialog(R.string.QueryLoadCharacter);
 	    					} else {
 	    						loadFFXICharacter(arg3);
 	    						updateValues();
 	    					}
     					}
     				}
     				public void onNothingSelected(AdapterView<?> arg0) {
     				}
     			};
    				cs.setOnItemSelectedListener(listener);
         	}
 
         	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelectorToCompare);
         	if (cs != null) {
     	        AdapterView.OnItemSelectedListener listener;
 
     	    	listener = new AdapterView.OnItemSelectedListener() {
     				public void onItemSelected(AdapterView<?> arg0, View arg1,
     						int arg2, long arg3) {
     					if (arg3 != getCharacterIDToCompare()) {
     						CheckBox cb = (CheckBox)findViewById(R.id.Compare);
     						if (cb.isChecked()) {
     							setCharacterIDToCompare(arg3);
     							setFFXICharacterToCompare(getSettings().loadCharInfo(arg3));
     							updateSubViewValues();
     	        		   	}
     					}
     				}
     				public void onNothingSelected(AdapterView<?> arg0) {
 						CheckBox cb;
 		    			setCharacterIDToCompare(-1);
 		    			setFFXICharacterToCompare(null);
 		            	cb = (CheckBox)findViewById(R.id.Compare);
 		            	cb.setChecked(false);
     				}
     			};
    				cs.setOnItemSelectedListener(listener);
         	}
         }
     }
     @Override
 	public void onStop() {
         // reset listeners
         {
         	Button btn;
         	
         	btn = (Button)findViewById(R.id.Save);
         	if (btn != null) {
         		btn.setOnClickListener(null);
         	}
         }
         
         {
         	CheckBox cb;
         	
         	cb = (CheckBox)findViewById(R.id.Compare);
         	if (cb != null) {
         		cb.setOnCheckedChangeListener(null);
         	}
         }
         
         {
         	CharacterSelectorView cs;
         	
         	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
         	if (cs != null) {
    				cs.setOnItemSelectedListener(null);
         	}
 
         	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelectorToCompare);
         	if (cs != null) {
    				cs.setOnItemSelectedListener(null);
         	}
         }
 		super.onStop();
 	}
     
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
 	        updateSubViewValues();
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainactivity, menu);
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		FFXIEQSettings settings = getSettings();
 		boolean useExternalDB;
 		MenuItem useExternalDBItem, installDBItem;
 		boolean writable, readable;
 
 		useExternalDB = settings.useExternalDB();
 		useExternalDBItem = menu.findItem(R.id.useExternalDB);
 		installDBItem = menu.findItem(R.id.InstallDB);
 	
 		// check SD card...
 		String status = Environment.getExternalStorageState();
 
 		writable = false;
 		readable = false;
 		if (status.equals(Environment.MEDIA_MOUNTED)) {
 			// valid status for write
 			writable = true;
 			readable = true;
 		} else if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
 			readable = true;
 		}
 
 		useExternalDBItem.setChecked(useExternalDB);
 		if (useExternalDB) {
 			useExternalDBItem.setEnabled(true);
 			installDBItem.setEnabled(writable);
 		} else {
 			useExternalDBItem.setEnabled(writable);
 			installDBItem.setEnabled(true);
 		}
 		if (!FFXIDatabase.supportsDBonSD()) {
 			useExternalDBItem.setEnabled(false);
 		}
 		
 		MenuItem item;
 		item = menu.findItem(R.id.BackupToSD);
 		if (item != null)
 			item.setEnabled(writable);
 		item = menu.findItem(R.id.RestoreFromSD);
 		if (item != null)
 			item.setEnabled(readable);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.EditMerits:
 			MeritPointEditActivity.startActivity(this, 0);
 			return true;
 		case R.id.EditSkill:
 			SkillEditActivity.startActivity(this, 0);
 			return true;
 		case R.id.EditBlueMagic:
 			BlueMagicSetEditActivity.startActivity(this, 0);
 			return true;
 
 		case R.id.Delete:
 			showDialog(R.layout.querydeletecharacter);
 			return true;
 		case R.id.DiscardChanges:
 			if (getFFXICharacter().isModified()) {
 				showDialog(R.string.QueryDiscardChanges);
 			}
 			return true;
 		case R.id.NewCharacter:
 			if (getFFXICharacter().isModified()) {
 				showDialog(R.string.QueryNewCharacter);
 			} else {
 				long id = getSettings().saveCharInfo(-1, getString(R.string.NewCharacterName), new FFXICharacter());
 				loadFFXICharacter(id);
 				updateValues();
 			}
 			return true;
 
 		case R.id.useExternalDB:
 			{
 				final FFXIDatabase db = (FFXIDatabase)getDAO();
 				final FFXIEQSettings settings = getSettings();
 				boolean useExternalDB;
 				final MenuItem menuItem = item;
 
 				useExternalDB = settings.useExternalDB();
 				if (useExternalDB)
 					useExternalDB = false;
 				else
 					useExternalDB = true;
 
 				final ProgressDialog dlg = new ProgressDialog(this);
 				dlg.show();
 				AsyncTask<Boolean, Void, Boolean> task = new AsyncTask<Boolean, Void, Boolean>() {
 					boolean mNewSetting;
 					@Override
 					protected Boolean doInBackground(Boolean... params) {
 						mNewSetting = params[0];
 						return db.setUseExternalDB(mNewSetting);
 					}
 
 					@Override
 					protected void onPostExecute(Boolean result) {
 						if (result) {
 							settings.setUseExternalDB(mNewSetting);
 							menuItem.setChecked(mNewSetting);
 							showDialog(R.string.UseExternalDBSucceeded);
 						} else {
 							showDialog(R.string.UseExternalDBFailed);
 						}
 						dlg.dismiss();
 					}
 				};
 				task.execute(useExternalDB);
 
 				return true;
 			}
 
 		case R.id.InstallDB:
 			{
 				final ProgressDialog dlg = new ProgressDialog(this);
 				dlg.show();
 				AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
 					@Override
 					protected Boolean doInBackground(Void... params) {
 						try {
 							((FFXIDatabase)getDAO()).copyDatabaseFromAssets(null);
 							return true;
 						} catch (IOException e) {
 							return false;
 						}
 					}
 	
 					@Override
 					protected void onPostExecute(Boolean result) {
 						if (result) {
 							showDialog(R.string.InstallDBSucceeded);
 						} else {
 							showDialog(R.string.InstallDBFailed);
 						}
 						dlg.dismiss();
 					}
 				};
 				task.execute();
 	
 				return true;
 			}
 		case R.id.BackupToSD:
 			showDialog(R.string.QueryBackupToSD);
 			return true;
 
 		case R.id.RestoreFromSD:
 			showDialog(R.string.QueryRestoreFromSD);
 			return true;
 			
 		case R.id.showCredit:
 			showDialog(R.id.showCredit);
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		AlertDialog.Builder builder;
 
 		switch (id) {
 		case R.layout.querysavecharacter:
 	    	dialog = new Dialog(this);
 			dialog.setContentView(R.layout.querysavecharacter);
 			dialog.setTitle(getString(R.string.QuerySaveCharacterTitle));
 
 			return dialog;
 		case R.layout.querydeletecharacter:
 	    	dialog = new Dialog(this);
 			dialog.setContentView(R.layout.querydeletecharacter);
 			dialog.setTitle(getString(R.string.QueryDeleteCharacterTitle));
 			return dialog;
 		case R.string.QueryDiscardChanges:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(true);
 	    	builder.setMessage(getString(R.string.QueryDiscardChanges));
 	    	builder.setTitle(getString(R.string.QueryDiscardChangesTitle));
 	    	builder.setPositiveButton(R.string.DiscardOK, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 		        	CharacterSelectorView cs;
 		        	long id;
 
 		        	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
 		        	id = cs.getSelectedItemId();
 		   			setCharacterID(id);
 	    			setFFXICharacter(getSettings().loadCharInfo(id));
 		    		updateSubViewValues();
 					dismissDialog(R.string.QueryDiscardChanges);
 				}
 			});
 	    	builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 		        	CharacterSelectorView cs;
 
 		        	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
 		        	cs.setSelectionById(getCharacterID());
 					dismissDialog(R.string.QueryDiscardChanges);
 				}
 			});
 			dialog = builder.create();
 			return dialog;
 		case R.string.QueryLoadCharacter:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(true);
 	    	builder.setMessage(getString(R.string.QueryLoadCharacter));
 	    	builder.setTitle(getString(R.string.QueryLoadCharacterTitle));
 	    	builder.setNeutralButton(R.string.DiscardAndLoad, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 		        	CharacterSelectorView cs;
 		        	long id;
 
 		        	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
 		        	id = cs.getSelectedItemId();
 		   			setCharacterID(id);
 	    			setFFXICharacter(getSettings().loadCharInfo(id));
 		    		updateSubViewValues();
 					dismissDialog(R.string.QueryLoadCharacter);
 				}
 			});
 	    	builder.setPositiveButton(R.string.SaveAndLoad, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					/* save */
 					getSettings().saveCharInfo(getCharacterID(), getName(), getFFXICharacter());
 					/* load character */
 					CharacterSelectorView cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
 		        	long id = cs.getSelectedItemId();
 		   			setCharacterID(id);
 	    			setFFXICharacter(getSettings().loadCharInfo(id));
 		    		updateSubViewValues();
 					dismissDialog(R.string.QueryLoadCharacter);
 				}
 			});
 	    	builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 		        	CharacterSelectorView cs;
 
 		        	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
 		        	cs.setSelectionById(getCharacterID());
 					dismissDialog(R.string.QueryLoadCharacter);
 				}
 			});
 			dialog = builder.create();
 			return dialog;
 		case R.string.QueryNewCharacter:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(true);
 	    	builder.setMessage(getString(R.string.QueryNewCharacter));
 	    	builder.setTitle(getString(R.string.QueryNewCharacterTitle));
 	    	builder.setPositiveButton(R.string.DiscardAndNewCharacterOK, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					long id = getSettings().saveCharInfo(-1, getString(R.string.NewCharacterName), new FFXICharacter());
 					loadFFXICharacter(id);
 					updateValues();
 					dismissDialog(R.string.QueryNewCharacter);
 				}
 			});
 	    	builder.setNeutralButton(R.string.SaveAndNewCharacterOK, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					/* save */
 					getSettings().saveCharInfo(getCharacterID(), getName(), getFFXICharacter());
 					/* create new character */
 					long id = getSettings().saveCharInfo(-1, getString(R.string.NewCharacterName), new FFXICharacter());
 					loadFFXICharacter(id);
 					updateValues();
 					dismissDialog(R.string.QueryNewCharacter);
 				}
 			});
 	    	builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dismissDialog(R.string.QueryNewCharacter);
 				}
 			});
 			dialog = builder.create();
 			return dialog;
 		case R.string.InstallDBSucceeded:
 		case R.string.InstallDBFailed:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 	    	builder.setMessage(getString(id));
 	    	builder.setTitle(getString(R.string.InstallDB));
 	    	builder.setPositiveButton(R.string.OK, null);
 			dialog = builder.create();
 			return dialog;
 
 		case R.string.UseExternalDBSucceeded:
 		case R.string.UseExternalDBFailed:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 	    	builder.setMessage(getString(id));
 	    	builder.setTitle(getString(R.string.app_name));
 	    	builder.setPositiveButton(R.string.OK, null);
 			dialog = builder.create();
 			return dialog;
 		case R.string.SDNotMounted:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 	    	builder.setMessage(getString(R.string.SDNotMounted));
 	    	builder.setTitle(getString(R.string.app_name));
 	    	builder.setPositiveButton(R.string.YesAndRestart, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					getSettings().setUseExternalDB(false);
 					dismissDialog(R.string.SDNotMounted);
 					setupSubViews();
 				}
 	    	});
 	    	builder.setNegativeButton(R.string.NoAndDismiss, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dismissDialog(R.string.SDNotMounted);
 					finish();
 				}
 	    	});
 			dialog = builder.create();
 			return dialog;
 			
 		case R.string.BackupToSDSucceeded:
 		case R.string.BackupToSDFailed:
 		case R.string.RestoreFromSDSucceeded:
 		case R.string.RestoreFromSDFailed:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 	    	builder.setMessage(getString(id));
 	    	builder.setTitle(getString(R.string.app_name));
 	    	builder.setPositiveButton(R.string.OK, null);
 			dialog = builder.create();
 			return dialog;
 		case R.string.QueryBackupToSD:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 	    	builder.setMessage(getString(R.string.QueryBackupToSD));
 	    	builder.setTitle(getString(R.string.app_name));
 	    	builder.setPositiveButton(R.string.Yes, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					final FFXIEQSettings settings = getSettings();
 		
 					dismissDialog(R.string.QueryBackupToSD);
 
 					final ProgressDialog dlg = new ProgressDialog(FFXIEQActivity.this);
 					dlg.show();
 					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
 						@Override
 						protected Boolean doInBackground(Void... params) {
 							boolean result;
 		
 							try {
 								try {
 									settings.copyDatabaseToSD(FFXIEQSettings.EXTERNAL_SD_PATH);
 								} catch (IOException e) {
 									settings.copyDatabaseToSD(FFXIEQSettings.SD_PATH);
 								}
 								result = true;
 							} catch (IOException e) {
 								result = false;
 							}
 							return result;
 						}
 		
 						@Override
 						protected void onPostExecute(Boolean result) {
 							if (result) {
 								showDialog(R.string.BackupToSDSucceeded);
 							} else {
 								showDialog(R.string.BackupToSDFailed);
 							}
 							dlg.dismiss();
 						}
 					};
 					task.execute();
 				}
 	    	});
 	    	builder.setNegativeButton(R.string.Cancel, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dismissDialog(R.string.QueryBackupToSD);
 				}
 	    	});
 			dialog = builder.create();
 			return dialog;
 		case R.string.QueryRestoreFromSD:
 			builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 	    	builder.setMessage(getString(R.string.QueryRestoreFromSD));
 	    	builder.setTitle(getString(R.string.app_name));
 	    	builder.setPositiveButton(R.string.Yes, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					final FFXIEQSettings settings = getSettings();
 		
 					dismissDialog(R.string.QueryRestoreFromSD);
 
 					final ProgressDialog dlg = new ProgressDialog(FFXIEQActivity.this);
 					dlg.show();
 					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
 						@Override
 						protected Boolean doInBackground(Void... params) {
 							boolean result;
 		
 							try {
 								try {
 									settings.copyDatabaseFromSD(FFXIEQSettings.EXTERNAL_SD_PATH);
 								} catch (IOException e) {
 									settings.copyDatabaseFromSD(FFXIEQSettings.SD_PATH);
 								}
 								result = true;
 							} catch (IOException e) {
 								result = false;
 							}
 							return result;
 						}
 		
 						@Override
 						protected void onPostExecute(Boolean result) {
 							if (result) {
 								showDialog(R.string.RestoreFromSDSucceeded);
 								loadFFXICharacter(getSettings().getFirstCharacterId());
 								updateValues();
 								updateSubViewValues();
 							} else {
 								showDialog(R.string.RestoreFromSDFailed);
 							}
 							dlg.dismiss();
 						}
 					};
 					task.execute();
 				}
 	    	});
 	    	builder.setNegativeButton(R.string.Cancel, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dismissDialog(R.string.QueryRestoreFromSD);
 				}
 	    	});
 			dialog = builder.create();
 			return dialog;
 			
 		case R.id.showCredit:
 			dialog = new WebViewDialog(this);
 			((WebViewDialog)dialog).loadURL("file:///android_asset/about/about.html");
 			return dialog;
 		}
 		return super.onCreateDialog(id);
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
     	TextView tv;
     	Button btn;
 
 		switch (id) {
 		case R.layout.querysavecharacter:
 			tv = (TextView)dialog.findViewById(R.id.CharacterName);
 			tv.setText(getName());
 			CheckBox cb = (CheckBox)(dialog.findViewById(R.id.SaveNew));
 			cb.setChecked(false);
 			btn = (Button)dialog.findViewById(R.id.SaveOK);
 			
 			final long charid = FFXIEQActivity.this.getCharacterID();
 			btn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					TextView tv = (TextView)(v.getRootView().findViewById(R.id.CharacterName));
 					String name = tv.getText().toString();
 					CheckBox cb = (CheckBox)(v.getRootView().findViewById(R.id.SaveNew));
 					long newid;
 					String old_name;
 					
 					newid = charid;
 					if (cb.isChecked())
 						newid = -1;
 					old_name = getName();
 					newid = getSettings().saveCharInfo(newid, name, getFFXICharacter());
 					dismissDialog(R.layout.querysavecharacter);
 					if (newid != charid || !name.equals(old_name)) {
 						loadFFXICharacter(newid);
 						updateValues();
 					} else {
 						getFFXICharacter().setNotModified();
 					}
 				}
 			});
 			return;
 		case R.layout.querydeletecharacter:
 			tv = (TextView)dialog.findViewById(R.id.CharacterName);
 			tv.setText(getName());
 			btn = (Button)dialog.findViewById(R.id.DeleteOK);
 			btn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					getSettings().deleteCharInfo(getCharacterID(), getFFXICharacter().getMeritPointId());
 					dismissDialog(R.layout.querydeletecharacter);
 					loadFFXICharacter(getSettings().getFirstCharacterId());
 					updateValues();
 				}
 			});
 			btn = (Button)dialog.findViewById(R.id.Cancel);
 			btn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					dismissDialog(R.layout.querydeletecharacter);
 				}
 			});
 			return;
 		}
 		super.onPrepareDialog(id, dialog);
 	}
 	
     protected void updateValues() {
     	CharacterSelectorView cs;
     	
     	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelector);
     	if (cs != null) {
 	        cs.setParam(getSettings(), getDAO(), getCharacterID());
     	}
     	cs = (CharacterSelectorView)findViewById(R.id.CharacterSelectorToCompare);
     	if (cs != null) {
 	        cs.setParam(getSettings(), getDAO(), getCharacterIDToCompare());
     	}
     }
 
     protected void updateSubViewValues() {
         ((FFXIEQBaseActivity)getCurrentActivity()).notifyDatasetChanged();
     }
 
     public String getName() {
 		return getSettings().getCharacterName(getCharacterID());
 	}
 
     public void loadFFXICharacter(long id) {
 		setCharacterID(id);
 		setFFXICharacter(getSettings().loadCharInfo(id));
 		
 		updateSubViewValues();
 	}
 	protected FFXIDAO getDAO() {
 		return ((FFXIEQApplication)getApplication()).getFFXIDatabase();
 	}
 	protected FFXIEQSettings getSettings() {
 		return ((FFXIEQApplication)getApplication()).getFFXIEQSettings();
 	}
 
 	public long getCharacterID() {
 		return ((FFXIEQApplication)getApplication()).getCharacterID();
 	}
 
 	public void setCharacterID(long characterID) {
 		((FFXIEQApplication)getApplication()).setCharacterID(characterID);
 	}
 
 	public FFXICharacter getFFXICharacter() {
 		return ((FFXIEQApplication)getApplication()).getFFXICharacter();
 	}
 
 	public void setFFXICharacter(FFXICharacter charInfo) {
 		((FFXIEQApplication)getApplication()).setFFXICharacter(charInfo);
 	}
 
 	public long getCharacterIDToCompare() {
 		return ((FFXIEQApplication)getApplication()).getCharacterIDToCompare();
 	}
 
 	public void setCharacterIDToCompare(long characterID) {
 		((FFXIEQApplication)getApplication()).setCharacterIDToCompare(characterID);
 	}
 	public FFXICharacter getFFXICharacterToCompare() {
 		return ((FFXIEQApplication)getApplication()).getFFXICharacterToCompare();
 	}
 
 	public void setFFXICharacterToCompare(FFXICharacter charInfo) {
 		((FFXIEQApplication)getApplication()).setFFXICharacterToCompare(charInfo);
 	}
 
 	public ControlBindableValue[] getTemporaryValues() {
 		return ((FFXIEQApplication)getApplication()).getTemporaryValues();
 	}
 
 	public void setTemporaryValues(ControlBindableValue []values) {
 		((FFXIEQApplication)getApplication()).setTemporaryValues(values);
 	}
 }
