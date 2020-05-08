 package com.example.tomatroid;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.example.tomatroid.sql.SQHelper;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ThemeList extends Activity {
 
 	SQHelper sqHelper = new SQHelper(this);
 	ListView shown, hide;
 	LayoutInflater mInflater;
 
 	ThemeCursorAdapter showAdapter, hideAdapter;
 	SimpleCursorAdapter themeListAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_theme_list);
 		mInflater = LayoutInflater.from(this);
 
 		Cursor c1 = sqHelper.getThemeCursor(0);
 		Cursor c2 = sqHelper.getThemeCursor(1);
 
 		shown = (ListView) findViewById(R.id.shown);
 		hide = (ListView) findViewById(R.id.hide);
 
 		showAdapter = new ThemeCursorAdapter(this, c1, 0, 0);
 		hideAdapter = new ThemeCursorAdapter(this, c2, 0, 1);
 
 		shown.setAdapter(showAdapter);
 		hide.setAdapter(hideAdapter);
 		themeListAdapter = new SimpleCursorAdapter(this,
 				R.layout.choose_theme_row, sqHelper.getThemeCursor(),
 				new String[] { SQHelper.KEY_NAME }, new int[] { R.id.name }, 0);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_theme_list, menu);
 		return true;
 	}
 
 	class ThemeCursorAdapter extends CursorAdapter {
 
 		int rowid;
 		int name;
 		int itemof;
 		int hide;
 
 		public ThemeCursorAdapter(Context context, Cursor c, int flags, int hide) {
 			super(context, c, flags);
 			rowid = c.getColumnIndex(SQHelper.KEY_ROWID);
 			name = c.getColumnIndex(SQHelper.KEY_NAME);
 			itemof = c.getColumnIndex(SQHelper.KEY_ITEMOF);
 			this.hide = hide;
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor c) {
 
 			final int id = c.getInt(this.rowid);
 			final String name = c.getString(this.name);
 			final int parentid = c.getInt(itemof);
 			final String parentName = sqHelper.getTheme(parentid);
 
 			view.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					showChangeThemeDialog(id, name, parentid);
 				}
 			});
 
 			TextView tv1 = (TextView) view.findViewById(R.id.parent);
 			TextView tv2 = (TextView) view.findViewById(R.id.name);
 			ImageButton change = (ImageButton) view.findViewById(R.id.button);
 			change.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					if (hide == 0) {
 						sqHelper.changeThemeStatus(id, 1);
 						Toast.makeText(getApplicationContext(),
 								"Hide " + name + " from theme lists.",
 								Toast.LENGTH_LONG).show();
 					} else {
 						sqHelper.changeThemeStatus(id, 0);
 						Toast.makeText(getApplicationContext(),
 								name + " is now shown on theme lists.",
 								Toast.LENGTH_LONG).show();
 					}
 					showAdapter.getCursor().requery();
 					showAdapter.notifyDataSetChanged();
 					hideAdapter.getCursor().requery();
 					hideAdapter.notifyDataSetChanged();
 				}
 			});
 
 			tv1.setText(parentName);
 			tv2.setText(name);
 
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			return mInflater.inflate(R.layout.theme_list_row, parent, false);
 		}
 
 	}
 
 	private void showChangeThemeDialog(final int id, final String name, final int parent) {
 		View dialogView = mInflater.inflate(R.layout.dialog_newtheme, null);
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setView(dialogView);
 		alertDialogBuilder.setTitle("Thema ndern");
 
 		final EditText userInput = (EditText) dialogView
 				.findViewById(R.id.editTextDialogUserInput);
 		userInput.setText(name);
 		
 		final Spinner parentSpinner = (Spinner) dialogView
 				.findViewById(R.id.parentspinner);
 		parentSpinner.setAdapter(themeListAdapter);
 		parentSpinner.setSelection(getIndex(parentSpinner, parent));
 		
 		// set dialog message
 		alertDialogBuilder.setPositiveButton("Verndern",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Cursor cc = (Cursor) parentSpinner.getSelectedItem();
 						int parentId = cc.getInt(cc
 								.getColumnIndex(SQHelper.KEY_ROWID));
 						if(parentId == 1) 
 							parentId = -1;
 						sqHelper.changeTheme(id, userInput.getText().toString(), parentId);
 						showAdapter.getCursor().requery();
 						showAdapter.notifyDataSetChanged();
 					}
 				}).setNegativeButton("Abbrechen",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				}).setNeutralButton("Lschen", 
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 						showDeleteThemeDialog(id, name, parent);
 					}
 				});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	
 	private void showDeleteThemeDialog(final int id, final String name, final int parent) {
 		View dialogView = mInflater.inflate(R.layout.dialog_deletetheme, null);
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setView(dialogView);
 		alertDialogBuilder.setTitle("Thema lschen");
 		
 		final Spinner parentSpinner = (Spinner) dialogView
 				.findViewById(R.id.parentspinner);
 		parentSpinner.setAdapter(themeListAdapter);
 		parentSpinner.setSelection(getIndex(parentSpinner, parent));
 		
 		// set dialog message
 		alertDialogBuilder.setPositiveButton("Lschen",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Cursor cc = (Cursor) parentSpinner.getSelectedItem();
 						int parentId = cc.getInt(cc
 								.getColumnIndex(SQHelper.KEY_ROWID));
						if(id != 1){
 							sqHelper.deleteTheme(id, name, parentId);
 							showAdapter.getCursor().requery();
 							showAdapter.notifyDataSetChanged();
 							hideAdapter.getCursor().requery();
 							hideAdapter.notifyDataSetChanged();
 						}
 					}
 				}).setNegativeButton("Abbrechen",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	
 	private int getIndex(Spinner spinner, int parentId){
 		if(parentId == -1){
 			return 0;
 		}
 		Cursor cc = sqHelper.getThemeCursor();
 		int column = cc.getColumnIndex(SQHelper.KEY_ROWID);
 		if(cc.moveToFirst()){
 			while(cc.moveToNext()){
 				if(cc.getInt(column) == parentId)
 					return cc.getPosition();
 			}
 		}
         return 0;
 	}
 }
