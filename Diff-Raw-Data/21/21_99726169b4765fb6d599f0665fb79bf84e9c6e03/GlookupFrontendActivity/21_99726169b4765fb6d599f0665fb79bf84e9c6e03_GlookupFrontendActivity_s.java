 package zipcodeman.glookup;
 
 import zipcodeman.glookup.R;
 import zipcodeman.glookup.authentication.AddUserActivity;
 import zipcodeman.glookup.authentication.AddUserAsyncTask;
 import zipcodeman.glookup.authentication.DSAKeys;
 import zipcodeman.glookup.maingrades.MainGradesActivity;
 import zipcodeman.glookup.models.LoginDataHelper;
 import zipcodeman.glookup.notification.GlookupAlarmReceiver;
 import zipcodeman.glookup.util.Constants;
 import zipcodeman.glookup.util.SettingsActivity;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import android.os.Bundle;
 
 import android.preference.PreferenceManager;
 
 import android.view.ContextMenu;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ContextMenu.ContextMenuInfo;
 
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 
 // TODO: Move to ssh keys. Perhaps more secure.
 public class GlookupFrontendActivity extends ListActivity {
 
 	public static final int ADD_USER_RESULT = 0;
 	public static final int EDIT_USER_RESULT = 1;
 	public static final int PREFERENCES_RESULT = 2;
 	public static final int NOT_A_USER_ID = -1;
 	public static final int INVALID_USER_ID = -2;
 	public static final String ADD_USER_USERNAME = "username";
 	public static final String ADD_USER_PASSWORD = "password";
 	public static final String ADD_USER_SERVER = "server";
 	public static final String GRADES_UNAME = "username";
 	public static final String GRADES_PASS = "password";
 	public static final String GRADES_SERVER = "server";
 	public static final String PREFS_NAME = "glookupFrontendData";
 	public static final String PREFS_USER = "glookupFrontendUser";
 	public static final String[] items = { "Open Menu and Click Add New User" };
 	public static final String ADD_USER_ID = "uid";
 	
 	private String[] unames, passwords, servers;
 	
 	private LoginDataHelper dataDB;
 	private SQLiteDatabase readOnly;
 	private SQLiteDatabase writeOnly;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        
         setContentView(R.layout.main);
         
         dataDB = new LoginDataHelper(this);
         readOnly = dataDB.getReadableDatabase();
         writeOnly = dataDB.getWritableDatabase();
         
         scheduleAlarmReceiver();
         
         loadList();
 
         this.registerForContextMenu(getListView());
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
     }
     
     public void loadList(){
     	SQLiteDatabase read = readOnly;
     	Cursor rows = read.query("Users", null, null, null, null, null, null);
     	if(rows != null)
     		rows.moveToFirst();
     	int count = rows.getCount();
     	if(count > 0){
     		int i = 0;
     		String[] users = new String[count];
     		this.unames = new String[count];
     		this.passwords = new String[count];
     		this.servers = new String[count];
     		do{
     			if(rows.getColumnCount() >= 3){
     				String uname = rows.getString(1);
     				String pass = rows.getString(2);
     				String server = rows.getString(3);
     				unames[i] = uname;
     				passwords[i] = pass;
     				servers[i] = server;
     				users[i] = uname + "@" + server;
     			}
     			i++;
     		}while(rows.moveToNext());
     		rows.close();
     		
 			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , users));
     	}else{
 			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , items));
 		}
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
     	SQLiteDatabase sld = readOnly;
     	Cursor rows = sld.rawQuery("SELECT * FROM Users LIMIT " + position + ", 1", null);
     	if(rows != null)
     		rows.moveToFirst();
     	if(rows.getCount() > 0){
     		Intent mainGrades = new Intent(this, MainGradesActivity.class);
 
     		String uname = rows.getString(1);
     		String pass = rows.getString(2);
     		String server = rows.getString(3);
     		
     		rows.close();
     		mainGrades.putExtra(GRADES_UNAME, uname);
         	mainGrades.putExtra(GRADES_PASS, pass);
         	mainGrades.putExtra(GRADES_SERVER, server);
         	this.startActivity(mainGrades);
     	}
     	super.onListItemClick(l, v, position, id);
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
     	if(view.getId() == this.getListView().getId()){
     		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
     		TextView tv = (TextView)info.targetView.findViewById(android.R.id.text1);
     		menu.setHeaderTitle(tv.getText());
     		menu.add(Menu.NONE, 0, 0, R.string.delete_connection);
     		menu.add(Menu.NONE, 1, 1, R.string.edit_connection);
     	}
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item){	    	
     	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
     	SQLiteDatabase sld = dataDB.getWritableDatabase();
     	Cursor row = sld.rawQuery("SELECT * FROM Users LIMIT " + info.position + ", 1", null);
     	switch(item.getItemId()){
     	case 0:
 	    	if(row != null){
 	    		row.moveToFirst();
	    		DSAKeys.removeKeys(row.getString(1));
 	    		deleteFile(row.getInt(0) + "-data");
 		    	if(row.getCount() >= 1){
 		    		sld.delete("Users",  "user_id=" + row.getInt(0), null);
 		    	}
 	    	}
 	    	loadList();
 	    	break;
     	case 1:
 	    	if(row != null){
 	    		row.moveToFirst();
 	    		if(row.getCount() >= 1){
 	    			this.editAccount(row.getInt(0), row.getString(1), row.getString(2), row.getString(3));
 	    		}
 	    	}
 	    	loadList();
     		break;
     	}
     	return false;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.addAccount:
         	addAccount();
             return true;
         case R.id.settings:
         	updatePreferences();
         	return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if(requestCode == ADD_USER_RESULT){
     		if(resultCode == Activity.RESULT_OK){
     			String uname = data.getExtras().getString(ADD_USER_USERNAME);
     			String pass = data.getExtras().getString(ADD_USER_PASSWORD);
     			String server = data.getExtras().getString(ADD_USER_SERVER);
     			
     			new AddUserAsyncTask(this, true, writeOnly).execute(uname, pass, server, "");
     		}
     	}else if(requestCode == EDIT_USER_RESULT){
     		if(resultCode == Activity.RESULT_OK){
     			String uname = data.getExtras().getString(ADD_USER_USERNAME);
     			String pass = data.getExtras().getString(ADD_USER_PASSWORD);
     			String server = data.getExtras().getString(ADD_USER_SERVER);
     			Integer uid = data.getIntExtra(ADD_USER_ID, -1);
     			
     			new AddUserAsyncTask(this, false, writeOnly).execute(uname, pass, server, uid.toString());
     		}
     	} else if(requestCode == PREFERENCES_RESULT) {
     		scheduleAlarmReceiver();
     	}
     }
     
     public void onMenuAddClick(View v) {
     	addAccount();
     }
 
     public void onPreferencesClick(View v) {
     	updatePreferences();
     }
     
     private void updatePreferences() {
     	Intent updatePref = new Intent(this, SettingsActivity.class);
     	GlookupFrontendActivity.this.startActivityForResult(updatePref, PREFERENCES_RESULT);
     }
     
     private void addAccount(){
     	Intent addUser = new Intent(this, AddUserActivity.class);
     	GlookupFrontendActivity.this.startActivityForResult(addUser, ADD_USER_RESULT);
     }
 
     public void editAccount(String uname, String pass, String server) {
     	editAccount(INVALID_USER_ID, uname, pass, server);
     }
     
     public void editAccount(int id, String uname, String pass, String server){
     	Intent editUser = new Intent(this, AddUserActivity.class);
     	editUser.putExtra(ADD_USER_ID, id);
     	editUser.putExtra(ADD_USER_USERNAME, uname);
     	editUser.putExtra(ADD_USER_PASSWORD, pass);
     	editUser.putExtra(ADD_USER_SERVER, server);
     	GlookupFrontendActivity.this.startActivityForResult(editUser, 
     			                     (id >= 0) ? EDIT_USER_RESULT : ADD_USER_RESULT);
     }
     
     private void scheduleAlarmReceiver() {
     	scheduleAlarmReceiver(this);
     }
     
     public static void scheduleAlarmReceiver(Context context) {
     	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
     	
 
         AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         PendingIntent pendingIntent =
                  PendingIntent.getBroadcast(context, 0, new Intent(context, GlookupAlarmReceiver.class),
                           PendingIntent.FLAG_CANCEL_CURRENT);
         
         if (pref.getBoolean("check-for-updates", true)) {
         	String refresh = pref.getString("refresh-frequency", "3h");
         	long update_freq = Constants.UPDATE_FREQUENCY;
         	switch (refresh.charAt(refresh.length() - 1)) {
         	case 'm':
         		update_freq = Integer.parseInt(refresh.substring(0, refresh.length() - 1)) * 60000;
         		break;
         	case 'h':
         		update_freq = Integer.parseInt(refresh.substring(0, refresh.length() - 1)) * AlarmManager.INTERVAL_HOUR;
         		break;
         	} 
         	
 	        // Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
 	        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, Constants.UPDATE_STARTUP_TIME,
 	                 				     update_freq, pendingIntent);
         } else {
         	alarmMgr.cancel(pendingIntent);
         }
      }
 }
