 package com.km2team.syriush;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 import com.km2team.syriush.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 import com.km2team.syriush.database.DatabaseException;
 import com.km2team.syriush.preference.SettingsActivity;
 import com.km2team.syriush.preference.SyriushPereference;
 import com.km2team.syriush.service.GPSService;
 import com.km2team.syriush.service.GPSServiceBinder;
 import com.km2team.syriush.service.LoggingLocationListener;
 
 
 public class MainActivity extends Activity implements ServiceConnection{
 	Button configButton;
 	ListView listView;
 	ArrayList<SyriushButton> file; //g��wna lista - menu
 	ArrayList<SyriushButton> dataList; //lista zawsze podpi�ta pod mainListView
 	ArrayAdapter<SyriushButton> adapter;
 	boolean inFolderState=false;
 	ArrayAdapter<SyriushButton> adapterBufor;
 	GPSServiceBinder binder = null;
 	static LoggingLocationListener listener=null;
 	public static Context context;
 	
 	private void binderStop(){
 		binder.stop();
 	}
 	
 	private void binderStart(){
		binder.start();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		context=getApplicationContext();
 		SyriushPereference.setDefaultValuesIfNecessary();
 		listener = new LoggingLocationListener(this);
 		setContentView(R.layout.activity_main);
 		configButton=(Button) findViewById(R.id.configButton);
 		configButton.setOnClickListener(ConfigButtoneListener);
 		ButtonFactory.setContext(this.getApplicationContext());
 			ButtonFactory.load();
 		file = ButtonFactory.getButtonList();
 		dataList=file;
         listView = (ListView)findViewById(R.id.routesListView1);
         adapter = new ArrayAdapter<SyriushButton>(this,android.R.layout.simple_list_item_1, file);
             listView.setAdapter(adapter);
             adapter.notifyDataSetChanged();
             listView.setOnItemClickListener(clickOnList);
 	}
 	
 	private void toast(String s){
 		Toast.makeText(MainActivity.this,s, Toast.LENGTH_SHORT).show();
 	}
 	
 	@SuppressWarnings("unchecked")
 	void upadteListView(){
 		  Collections.sort(dataList);
 		  adapter.notifyDataSetChanged();
 	}
 	
 	
 	SyriushButton clickedButton;
 	OnItemClickListener clickOnList = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
 				long arg3) {
 			clickedButton=dataList.get(pos);
 			if (clickedButton.itIsFolder) {openFolder(); return;}
 			if (clickedButton.getId()==4 && binder!=null) {binder.getCurrentLocation(); return;}
 			if (clickedButton.getId()==7 && binder!=null) {showAddPointAlert(); return;}
 			if (clickedButton.getId()==8 && binder!=null) {Intent startNewActivityOpen = new Intent(MainActivity.this, NewRouteActivity.class);startActivityForResult(startNewActivityOpen, 0);return;}
 			if (clickedButton.getId()==9 && binder!=null) {Intent startNewActivityOpen = new Intent(MainActivity.this, RoutesManagerActivity.class);startActivityForResult(startNewActivityOpen, 0);return;}
 		}
 	};
 	
 	boolean binderIsStarted=false;
 	private OnClickListener NaviButtoneListener=new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			if (binderIsStarted) binder.stop(); else binderStart();
 		}
 	};
 	
 	
 	private OnClickListener ConfigButtoneListener=new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			Intent startNewActivityOpen = new Intent(MainActivity.this, ConfigActivity.class);
 			startActivityForResult(startNewActivityOpen, 0);
 		}
 	};
 
 	private void openFolder() {
 		adapterBufor = adapter;
 		dataList=clickedButton.list;
 		adapter = new ArrayAdapter<SyriushButton>(this,android.R.layout.simple_list_item_1, dataList);
 		listView.setAdapter(adapter);
 		inFolderState=true;
 		upadteListView();
 	}
 
 	@Override
 	public void onStart(){
 		super.onStart();
 		bindService( new Intent(this, GPSService.class), this, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		 file=ButtonFactory.getButtonList();
 		 dataList=file;
 		adapter = new ArrayAdapter<SyriushButton>(this,android.R.layout.simple_list_item_1, file);
          listView.setAdapter(adapter);
          upadteListView();
 		 settingsUpdate();
          
 	}
 	
 	private void settingsUpdate() {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		//toast("powiadomienia gps: "+settings.getString(getString(R.string.messageTimeKey), "-1"));
 		//settings.getBoolean(getString(R.string.gpsMessageCheckboxKey), true));
 		
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK ) {
         		if (inFolderState){
                 	inFolderState=false;
                 	adapter=adapterBufor;
                 	listView.setAdapter(adapter);
                 	dataList=file;
                 	upadteListView();}
         		else showExitAlert();
                 return false;
         }
         if (keyCode == KeyEvent.KEYCODE_MENU ) {
         	Intent startNewActivityOpen = new Intent(MainActivity.this, SettingsActivity.class);startActivityForResult(startNewActivityOpen, 0);
             return false;
     }
     return super.onKeyDown(keyCode, event);
 }
 	@Override
 	public void onPause(){
 		try {
 			ButtonFactory.save();
 		} catch (DatabaseException e) {
 			toast(e.toString());
 		}
 		super.onStop();
 		
 		
 	}
 	
 	@Override
 	public void onDestroy(){
 	    binder.stop();
 		super.onDestroy();
 		
 		
 	}
 	
 	private void showExitAlert() {
 		final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
         alert.setTitle("Wyjść z aplikacji Syriush?");
         alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             	MainActivity.this.finish();
             }
             });
         alert.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) { }
             });
         alert.show();
 		
 	}
 
 	private void showAddPointAlert() {
 		final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
         alert.setTitle("Dodawanie punktu");
         final EditText text = new EditText(this);
         alert.setView(text);
         alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             	//binder.stop();
             	
             	
             	try {
 					listener.savePoint(text.getText().toString());
 				} catch (DatabaseException e) {toast("Błąd: "+e.toString());
 				}
             }
             });
         alert.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) { }
             });
         alert.show();
 		
 	}
 	
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		binder = ((GPSServiceBinder)service);
 		binderStart();
 		binderIsStarted=true;
 		toast("Aplikacja gotowa do pracy");
 		
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
