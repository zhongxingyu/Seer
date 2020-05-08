 package com.corsework.notepad.activity;
 
 
 import java.util.ArrayList;
 
 import com.corsework.notepad.adapter.ListAdapter;
 import com.corsework.notepad.application.NotePadApplication;
 import com.corsework.notepad.entities.dao.Backuper;
 import com.corsework.notepad.entities.dao.TagInfo;
 import com.corsework.notepad.entities.dao.TagDao;
 import com.corsework.notepad.entities.dao.NoteDao;
 import com.corsework.notepad.entities.dao.ReminderDao;
 import com.corsework.notepad.entities.program.Note;
 import com.corsework.notepad.entities.program.Reminder;
 import com.corsework.notepad.view.NoteListItem;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class listViewClass extends ListActivity {
 	
 	private static final int MENU_ITEM_DELETE = Menu.FIRST;
 	private static final int MENU_ITEM_INSERT_NOTE = Menu.FIRST + 1;
 	private static final int MENU_ITEM_INSERT_REMI = Menu.FIRST + 2;
 	private static final int MENU_ITEM_SHARE = Menu.FIRST + 3;
 	private static final int MENU_SEARCH = Menu.FIRST + 4;
 	private static final int MENU_SETTINGS = Menu.FIRST + 6;
 	private static final int MENU_PASSWORD = Menu.FIRST + 7;
 	private static final int MENU_LOAD_SAVE = Menu.FIRST + 8;
 	private static final int MENU_BACKUP = Menu.FIRST + 9;
 	private static final int MENU_RESTORE = Menu.FIRST + 10;
 	private static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // MUST BE LAST
 	
 
 	private static final int ADD_TEG_ACT = 2346;
 
 	static final int PASSWORD_DIALOG_ID=1;
 	static final int PASSWORD_DELETE_DIALOG_ID=2;
 	final int DIALOG_TEGS = 10;
 
 	private NotePadApplication app;
 	private NoteDao noteD;
 	private ReminderDao remD;
 	private ListAdapter adapter;
 	boolean lookNote;
 	Cursor cursor;
 	TagDao tegD;
 	SharedPreferences mNoteSettings;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.listview_);
 
         mNoteSettings=getSharedPreferences(NotePadApplication.NOTE_PREFERENCES, Context.MODE_PRIVATE);
         app=(NotePadApplication)getApplication();
         noteD= app.getNoteD();
         remD = app.getReminderD();
         tegD = app.getDbc();
         
         lookNote=app.isLookNote();
         adapter = new ListAdapter(noteD.getAll(),remD.getAll(),this);
         setListAdapter(adapter);
 
    	app.startNotify(-1);
         refreshCursor();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		lookNote = app.isLookNote();
 		fillData();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		if (lookNote)
 			menu.add(0, MENU_ITEM_SHARE, 0, R.string.show_reminder).setShortcut('5', 'r')
 		.setIcon(R.drawable.clocks_menu);//(android.R.drawable.ic_menu_my_calendar);
 		else 
 			menu.add(0, MENU_ITEM_SHARE, 0, R.string.show_note).setShortcut('5', 'r')
 			.setIcon(R.drawable.apply_tags_menu);//(android.R.drawable.ic_menu_my_calendar);
 		menu.add(0, MENU_ITEM_INSERT_NOTE, 0, R.string.add_note_button).setShortcut('1', 'i')
 			.setIcon(R.drawable.add_rem_tags_menu);//(android.R.drawable.ic_menu_add);
 		menu.add(0, MENU_ITEM_INSERT_REMI, 0, R.string.add_reminder).setShortcut('2', 'u')
 		.setIcon(R.drawable.clock_menu);//alarm);
 		menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete).setShortcut('5', 'd')
 		.setIcon(android.R.drawable.ic_menu_delete);
 		menu.add(0, MENU_SEARCH, 0, R.string.menu_search_by_teg).setShortcut('3','s')
 		.setIcon(android.R.drawable.ic_menu_search);
 
 		if (mNoteSettings.contains(NotePadApplication.NOTE_PREFERENCES_PASSWORD)){
         	menu.add(0, MENU_PASSWORD, 0, R.string.delete_passwod).setIcon(android.R.drawable.ic_secure);
 		}else
 			menu.add(0, MENU_PASSWORD, 0, R.string.add_passwod).setIcon(android.R.drawable.ic_secure);
 		menu.add(0, MENU_RESTORE, 0, R.string.restore).setIcon(android.R.drawable.ic_popup_sync);
 		menu.add(0, MENU_BACKUP, 0, R.string.backup).setIcon(
 				android.R.drawable.ic_menu_preferences).setShortcut('9', 's');
 			
 		return true;
 	}
 	 @Override
 	    public boolean onPrepareOptionsMenu(Menu menu) {
 	      // TODO Auto-generated method stub
 		 MenuItem mi =  menu.findItem(MENU_ITEM_SHARE);
 		 if (lookNote)
 			 mi.setTitle(R.string.show_reminder).setIcon(R.drawable.clocks_menu);
 		 else 
 			 mi.setTitle(R.string.show_note).setIcon(R.drawable.apply_tags_menu);
 		 mi = menu.findItem(MENU_PASSWORD);
 		 if (mNoteSettings.contains(NotePadApplication.NOTE_PREFERENCES_PASSWORD)){
 	        	mi.setTitle(R.string.delete_passwod);
 			}else
 				mi.setTitle(R.string.add_passwod);
 		
 	      return super.onPrepareOptionsMenu(menu);
 	    }
 	 @Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 	        switch(item.getItemId()) {
 	        	case MENU_ITEM_SHARE:
 	        		lookNote = !lookNote;
 	        		fillData();
 	        		return true;
 	            case MENU_ITEM_INSERT_NOTE:
 	            	app.setLookNote(lookNote);
 	                createNote();
 	                return true;
 	            case MENU_ITEM_DELETE:
 	            	app.setLookNote(lookNote);
 	            	Intent i = new Intent(this, DeleteNoteActivity.class);
 	     	        startActivity(i);
 	            	return true;
 	            case MENU_ITEM_INSERT_REMI:
 	            	app.setLookNote(lookNote);
 	                createReminder();
 	                return true;
 	            case MENU_SEARCH:
 	            	app.setLookNote(lookNote);
 	        		tegD.unChAll();
 	        		refreshCursor();
 	        		showDialog(DIALOG_TEGS);
 	                return true;
 	            case MENU_PASSWORD:
 	            	if (mNoteSettings.contains(NotePadApplication.NOTE_PREFERENCES_PASSWORD)){
 	            	  	passwD();
 	            	}else  	showDialog(PASSWORD_DIALOG_ID);
 	            	return true;
 	            case MENU_BACKUP:
 	            	Backuper.getInstance().backup();
 	            	//lookNote = app.isLookNote();
 	            	//app.updateDao();
 	        		//fillData();
 	            	//app.restart();
 	            	//finish();
 	            	return true;
 	            case MENU_RESTORE:
 	            	Backuper.getInstance().restore();
 	            	lookNote = app.isLookNote();
 	            	app.updateDao();
 	        		//fillData();
 	            	Intent mainIntent = new Intent(this, ViewNotePadActivity.class);  
 	        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  );  
 	        		startActivity(mainIntent); 
 	            	finish();
 	            	return true;
 	        }
 
 	        return super.onMenuItemSelected(featureId, item);
 	    }
 	  private void passwD() {
 			// TODO Auto-generated method stub
 	    	 LayoutInflater infl = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	         final View passwordDialogLayout = infl.inflate(R.layout.enter_password, (ViewGroup) findViewById(R.id.root1));
 
 	         AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(this);
 	         passwordDialogBuilder.setView(passwordDialogLayout);
 	         final TextView passwText = (TextView) passwordDialogLayout.findViewById(R.id.EditText_Password);
 	         
 	    	 final AlertDialog dialog;
 	    	 passwordDialogBuilder.setTitle(R.string.delete_passwod);
 	         passwordDialogBuilder.setPositiveButton(android.R.string.ok, onClickListener_DialogResetPin);
 	         passwordDialogBuilder.setNeutralButton(android.R.string.cancel, onClickListener_DialogResetPin);
 	         dialog = passwordDialogBuilder.create();
 	         dialog.show();
 	         Button btnOK = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
 	         btnOK.setOnClickListener( new View.OnClickListener() {
 	        	 
 	             public void onClick(View arg0) {
 	            	 String passw = mNoteSettings.getString(NotePadApplication.NOTE_PREFERENCES_PASSWORD, "");
 	                 String userPassword = passwText.getText().toString();
 	                 if (userPassword != null && userPassword.equals(passw)) {
 	                	Editor editor =mNoteSettings.edit();
 						editor.remove(NotePadApplication.NOTE_PREFERENCES_PASSWORD);
 						editor.commit();
 						Toast.makeText(listViewClass.this,R.string.delete_passwod, Toast.LENGTH_LONG).show();
 	                 	dialog.dismiss();
 	                 }
 	                 else {
 	                	 Toast.makeText(listViewClass.this,R.string.settings_pwd_not_equal, Toast.LENGTH_LONG).show();
 	                	 //ViewNotePadActivity.this.finish();
 	                 }
 	             }
 	         });
 		}
 	    DialogInterface.OnClickListener onClickListener_DialogResetPin =
 	            new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) {
 	        	dialog.dismiss();
 	        }
 	    };
 
 	private void createReminder() {
 		Intent i = new Intent(this, AddReminderActivity.class);
         startActivity(i);
 	}
 
 	private void createNote() {
 	        Intent i = new Intent(this, AddNoteActivity.class);
 	        startActivity(i);
 	  }
 	 protected Dialog onCreateDialog(int id) {
 		    switch (id) {
 		    case DIALOG_TEGS:
 			    AlertDialog.Builder adb = new AlertDialog.Builder(this);
 			    adb.setTitle(R.string.menu_search_by_teg);
 			    adb.setMultiChoiceItems(cursor, TagInfo.COLUMN_CHK, TagInfo.COLUMN_TEXT, myCursorMultiClickListener);
 			    adb.setPositiveButton(android.R.string.ok, myClickListener);
 			    adb.setNegativeButton(android.R.string.cancel,myClickListener);
 			    return adb.create();
 		  
 		    case PASSWORD_DIALOG_ID:
 			    LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				final View layout = inflater.inflate(R.layout.password_dialog, (ViewGroup)findViewById(R.id.root));
 				final EditText p1=(EditText)layout.findViewById(R.id.EditText_Pwd1);
 				final EditText p2=(EditText)layout.findViewById(R.id.EditText_Pwd2);
 				final TextView error=(TextView)layout.findViewById(R.id.TextView_PwdProblem);
 				p2.addTextChangedListener(new TextWatcher() {
 					public void onTextChanged(CharSequence s, int start, int before, int count) {
 					}
 					public void beforeTextChanged(CharSequence s, int start, int count,
 							int after) {}
 					public void afterTextChanged(Editable s) {
 						String strP1 =p1.getText().toString();
 						String strP2 =p2.getText().toString();
 						if (strP1.equals(strP2)){
 							error.setText(R.string.settings_pwd_equal);
 						} else error.setText(R.string.settings_pwd_not_equal);
 					}
 				});
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setView(layout);
 				builder.setTitle(R.string.settings_button_pwd);
 				builder.setPositiveButton(android.R.string.ok, 
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								String spasw1 = p1.getText().toString();
 								String spasw2 = p2.getText().toString();
 								if (spasw1.equals(spasw2)){
 									Editor editor =mNoteSettings.edit();
 									editor.putString(NotePadApplication.NOTE_PREFERENCES_PASSWORD, spasw1);
 									editor.commit();
 									Toast.makeText(listViewClass.this,getResources().getString(R.string.settings_pwd_set), Toast.LENGTH_LONG).show();
 								} else {
 									Toast.makeText(listViewClass.this,R.string.settings_pwd_not_set, Toast.LENGTH_LONG).show();
 								}
 								listViewClass.this.removeDialog(PASSWORD_DIALOG_ID);
 							}
 						});
 				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 						listViewClass.this.removeDialog(PASSWORD_DIALOG_ID);
 					}
 				});
 				AlertDialog passD = builder.create();
 				return passD;
 		    }
 			  return super.onCreateDialog(id);
 			  }
 	 @Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		 switch (id) {
 		      case DIALOG_TEGS:
 		    	  CursorAdapter lAdapter = (CursorAdapter) ((AlertDialog) dialog).getListView().getAdapter();
 		    	   CursorAdapter cAdapter = (CursorAdapter) lAdapter;
 		           cAdapter.changeCursor(cursor);
 		      
 		      break;
 
 			case PASSWORD_DIALOG_ID:
 					return;
 		    }
 		super.onPrepareDialog(id, dialog);
 	}
 	android.content.DialogInterface.OnClickListener myClickListener = new  android.content.DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog,int which) {
 		     
 		      if (which == Dialog.BUTTON_POSITIVE) {
 		    	  Intent i = new Intent(listViewClass.this, DayListViewClass.class);
 		    	  i.putExtra(NotePadApplication.KEY_FIND, "2");
 		          startActivity(i);
 		      }
 		      
 		    }
 
 
 	};
 	android.content.DialogInterface.OnMultiChoiceClickListener myCursorMultiClickListener = new android.content.DialogInterface.OnMultiChoiceClickListener() {
 		    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 		      ListView lv = ((AlertDialog) dialog).getListView();
 		      Log.d("log tag", "which = " + which + ", isChecked = " + isChecked);
 		      tegD.changeRec(which, isChecked);
 		        refreshCursor();
 		        CursorAdapter cAdapter = (CursorAdapter) lv.getAdapter();
 		        cAdapter.changeCursor(cursor);
 		    }
 		  };
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
     	app.setLookNote(lookNote);
 		if (lookNote){
 		Intent i = new Intent(this,SeeNoteActivity.class);
 		i.putExtra(NotePadApplication.KEY_ROWID,((NoteListItem)v).getRecord().getId());
 		startActivity(i);
 		}
 		else{
 			Intent i = new Intent(this,SeeReminderActivity.class);
 			i.putExtra(NotePadApplication.KEY_ROWID,((NoteListItem)v).getRecord().getId());
 			startActivity(i);
 		}
 	}
 		
 	private void fillData() {
 		if (lookNote)
 			adapter.forceReload(noteD.getAll(),new ArrayList<Reminder>(),lookNote);
 		else adapter.forceReload(new ArrayList<Note>(),remD.getAll(),lookNote);
 	}
 	void refreshCursor() {
 	    stopManagingCursor(cursor);
 	    cursor = tegD.getAllData();
 	    startManagingCursor(cursor);
 	  }
 }
