 /**
  * Part of aSQLiteManager (http://sourceforge.net/projects/asqlitemanager/)
  * a a SQLite Manager by andsen (http://sourceforge.net/users/andsen)
  *
  * The mail class of the aSQLiteManager
  *
  * @author andsen
  *
  */
 package dk.andsen.asqlitemanager;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import dk.andsen.utils.NewFilePicker;
 import dk.andsen.utils.Utils;
 
 public class aSQLiteManager extends Activity implements OnClickListener {
 	/**
 	 * True to enable functions under test
 	 */
 	private static final int MENU_OPT = 1;
 	private static final int MENU_HLP = 2;
 	private static final int MENU_RESET = 3;
 	final String WelcomeId = "ShowWelcome2.6";
 	// change this to delete preferences 
 	final String vers = "2.6";
 	private Context _cont;
 	private String _recentFiles;
 	private boolean testing = false;
 	private boolean logging = false;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         logging = Prefs.getLogging(this);
         setContentView(R.layout.main);
         Button open = (Button) this.findViewById(R.id.Open);
         open.setOnClickListener(this);
         Button about = (Button) this.findViewById(R.id.About);
         about.setOnClickListener(this);
         Button newDatabase = (Button) this.findViewById(R.id.NewDB);
         newDatabase.setOnClickListener(this);
         Button recently = (Button) this.findViewById(R.id.Recently);
         recently.setOnClickListener(this);
         TextView tv = (TextView) this.findViewById(R.id.Version);
         tv.setText(getText(R.string.Version) + " " + getText(R.string.VersionNo));
         _cont = this;
     		final SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
     		// Show welcome screen if not disabled
     		// Perhaps change how the welcome screen is displayed. Store version no in
     		// in "VersionNo" and show welcome if versionNo has changed
     		SharedPreferences prefs = getSharedPreferences("dk.andsen.asqlitemanager_preferences", MODE_PRIVATE);
     		if (!settings.getString(vers, "").equals(vers)) {
     			Editor editor = prefs.edit();
     			editor.clear();
     			editor.commit();
     		}
     		Editor ed = settings.edit();
     		ed.putString(vers, vers);
     		ed.commit();
     		if(settings.getBoolean(WelcomeId, true)) {
     			final Dialog dial = new Dialog(this);
     			dial.setContentView(R.layout.new_welcome);
     			dial.setTitle(R.string.Welcome);
     			Button _btOK = (Button)dial.findViewById(R.id.OK);
     			_btOK.setOnClickListener(new OnClickListener() {
     				public void onClick(View v) {
     					CheckBox _remember = (CheckBox) dial.findViewById(R.id.ShowAtStartUp);
     					android.content.SharedPreferences.Editor edt = settings.edit();
     					edt.putBoolean(WelcomeId, _remember.isChecked()); 
     					edt.commit();
    					dial.dismiss();
     				} });
     			dial.show();
     		}
     		AppRater.app_launched(_cont);
     		if (testing )
     			AppRater.showRateDialog(_cont, null);
   			Bundle extras = getIntent().getExtras();
   			if(extras !=null) {
   				String _dbPath = extras.getString("Database");
 					Intent i = new Intent(_cont, DBViewer.class);
 					i.putExtra("db", _dbPath);
 					startActivity(i);
   			} else {
   			}
     }
 
 		/* (non-Javadoc)
 		 * @see android.view.View.OnClickListener#onClick(android.view.View)
 		 */
 		public void onClick(View v) {
 			int key = v.getId();
 			if (key == R.id.Open) {
 				Intent i = new Intent(this, NewFilePicker.class);
 				Utils.logD("Calling NewFilepicker", logging);
 //				Utils.logD("Calling NewFilepicker for result");
 //				startActivityForResult(i, 1);
 				startActivity(i);
 			} else if (key == R.id.About) {
 				showAboutDialog();
 			}  else if (key == R.id.NewDB) {
 				Utils.logD("Create new database", logging);
 				newDatabase();
 			} else if (key == R.id.Recently) {
 				// Retrieve recently opened files
 				SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
 				_recentFiles = settings.getString("Recently", null);
 				if (_recentFiles == null) {
 					Utils.showMessage("Recently files: ", _recentFiles, _cont);
 				} else {
 					String[] resently = _recentFiles.split(";");
 					Utils.logD(_recentFiles, logging);
 					AlertDialog dial = new AlertDialog.Builder(this)
 					.setTitle(getString(R.string.Recently)) 
 					.setSingleChoiceItems(resently, 0, new ResentFileOnClickHandler() )
 					.create();
 					dial.show();
 				}
 			}
 		}
 		
 		/**
 		 * Open a the database clicked on from the recently opened file menu
 		 */
 		public class ResentFileOnClickHandler implements DialogInterface.OnClickListener {
 			public void onClick(DialogInterface dialog, int which) {
 				String[] files = _recentFiles.split(";");
 				String database = files[which];
 				//Utils.toastMsg(_cont, database);
 				dialog.dismiss();
 				Intent i = new Intent(_cont, DBViewer.class);
 				i.putExtra("db", database);
 				startActivity(i);
 			}
 		}
 
 		
 		/**
 		 * Display the about dialog
 		 */
 		private void showAboutDialog() {
 			Dialog dial = new Dialog(this);
 			dial.setTitle(getText(R.string.About) + " " + getText(R.string.hello));
 			dial.setContentView(R.layout.about);
 			dial.show();
 			//Intent i = new Intent(this, ProgressBarTest.class);
 			//Utils.logD("Calling ProgressBar");
 			//startActivity(i);
 		}
 
 		protected void onActivityResult(int requestCode, int resultCode, Intent data)
 		{
 			Utils.logD("Main-activity got result from sub-activity", logging);
 			if (resultCode == Activity.RESULT_CANCELED) {
 				Utils.logD("WidgetActivity was cancelled or encountered an error. resultcode == result_cancelled", logging);
 				Utils.logD("WidgetActivity was cancelled - data =" + data, logging);
 			} else
 				switch (requestCode) {
 				case 1:
 					 String msg = data.getStringExtra("returnedData");
 					 Utils.showMessage("Returned file", msg, _cont);
 					break;
 				}
 			Utils.logD("Main-activity got result from sub-activity", logging);
 		}		
 		
 		/**
 		 * Create a new empty database
 		 */
 		private void newDatabase() {
 			final Dialog newDatabaseDialog = new Dialog(this);
 			newDatabaseDialog.setContentView(R.layout.new_database);
 			newDatabaseDialog.setTitle(getText(R.string.NewDBSDCard));
 			final EditText edNewDB = (EditText)newDatabaseDialog.findViewById(R.id.newCode);
 			edNewDB.setHint(getText(R.string.NewDBPath));
 			TextView tvMessage = (TextView) newDatabaseDialog.findViewById(R.id.newMessage);
 			tvMessage.setText(getText(R.string.Database));
 			newDatabaseDialog.show();
 			final Button btnMOK = (Button) newDatabaseDialog.findViewById(R.id.btnMOK);
 			btnMOK.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					boolean error = false;
 					String path;
 					if (v == btnMOK) {
 						if (Utils.isSDAvailable()) {
 							String fileName = edNewDB.getEditableText().toString();
 							path = Environment.getExternalStorageDirectory().getAbsolutePath();
 							path += "/" + fileName;
 							if (fileName.trim().equals("")) {
 								Utils.showMessage((String)getText(R.string.Error), (String)getText(R.string.NoFileName), _cont);
 							} else {
 								if (!path.endsWith(".sqlite"))
 									path += ".sqlite";
 								try {
 									//check to see if it exists
 									File f = new File(path);
 									if (f.exists()) {
 										error = true;
 										Utils.showMessage(getString(R.string.Error), path + " "
 												+ getString(R.string.DatabaseExists), _cont);
 									} else {
 										SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
 										db.close();
 									}
 								} catch (Exception e) {
 									error = true;
 									Utils.showMessage(getString(R.string.Error),
 											getString(R.string.CouldNotCreate) +" " + path, _cont);
 									e.printStackTrace();
 								}
 								// Ask before??
 								if (!error) {
 									Intent i = new Intent(_cont, DBViewer.class);
 									i.putExtra("db", path);
 									newDatabaseDialog.dismiss();
 									startActivity(i);
 								}
 							}
 						}
 						Utils.logD("Path: " + edNewDB.getText().toString(), logging);
 					}
 				}
 			});
 		}
 
 		/*
 		 *  Creates the menu items
 		 */
 		public boolean onCreateOptionsMenu(Menu menu) {
 			menu.add(0, MENU_OPT, 0, R.string.Option).setIcon(R.drawable.ic_menu_preferences);
 			menu.add(0, MENU_HLP, 0, R.string.Help).setIcon(R.drawable.ic_menu_help);
 			menu.add(0, MENU_RESET, 0, R.string.Reset).setIcon(R.drawable.ic_menu_close_clear_cancel);
 			return true;
 		}
 		
 		/* (non-Javadoc) Handles item selections
 		 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 		 */
 		public boolean onOptionsItemSelected(MenuItem item) {
 			switch (item.getItemId()) {
 	    case MENU_OPT:
 	      startActivity(new Intent(this, Prefs.class));
 	      return true;
 	    case MENU_HLP:
 				Intent i = new Intent(this, Help.class);
 				startActivity(i);
 	      return true;
 	    case MENU_RESET:
 	    	// Reset all settings to default
 	    	SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.clear();
 				editor.commit();
 				settings = getSharedPreferences("dk.andsen.asqlitemanager_preferences", MODE_PRIVATE);
 				editor = settings.edit();
 				editor.clear();
 				editor.commit();
 				return false;
 			}
 			return false;
 		}
 }
