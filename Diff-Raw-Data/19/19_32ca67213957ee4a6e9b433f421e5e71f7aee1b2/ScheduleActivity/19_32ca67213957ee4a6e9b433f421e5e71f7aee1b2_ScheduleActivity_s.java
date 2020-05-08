 package com.alexismorin.iksu;
 
 import java.io.File;
 //import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 //import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import com.alexismorin.iksu.IKSUHelper.ApiException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScheduleActivity extends Activity implements OnClickListener{
 	//views from the XML file
 	public LayoutInflater m_inflater;
 	public Button m_p, m_n;
 	public ListView m_class_list;
 	public TextView m_date;
 	
 	public ArrayList<String> alSchedule;
 	public Object[] wholeSchedule;
 	public IKSUSchedule iksuSchedule;
 	
 	int dayIndex = 0;
 	
 	private ProgressDialog dialog;
 	ScheduleAdapter scheduleAdapter = null;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         m_inflater = LayoutInflater.from(this);
         m_class_list = (ListView) findViewById(R.id.activities_view);
         m_date = (TextView) findViewById(R.id.title_header);
         m_p = (Button) findViewById(R.id.btnPrev);
         m_n = (Button) findViewById(R.id.btnNext);
         
         m_p.setOnClickListener(this);
         m_n.setOnClickListener(this);
         
         iksuSchedule = (IKSUSchedule) getLastNonConfigurationInstance();
         
         if( iksuSchedule == null){//if the app is freshly started, need to initialize again.
         	m_p.setVisibility(View.INVISIBLE);
         	m_n.setVisibility(View.INVISIBLE);
         	iksuSchedule = new IKSUSchedule();
         }else{//this happens when the app is rotated
         	m_date.setText(iksuSchedule.dates.get(iksuSchedule.currentDateIndex));
         	if(iksuSchedule.currentDateIndex == 0){
         		m_p.setVisibility(View.INVISIBLE);
             	m_n.setVisibility(View.VISIBLE);
         	}
         	if(iksuSchedule.currentDateIndex == iksuSchedule.dates.size()-1){
         		m_p.setVisibility(View.VISIBLE);
             	m_n.setVisibility(View.INVISIBLE);
         	}
         }
         
         scheduleAdapter = new ScheduleAdapter(getApplicationContext());
         m_class_list.setAdapter(scheduleAdapter);
         
         if(isOnline(getApplicationContext())){
 	        if(iksuSchedule.activities.size() == 0){
 	        	/*if(scheduleIsCached()){
 	        		Log.i("Cache", "Schedule was found to be cached");
 	        		try{
 		        		iksuSchedule = loadScheduleFromCache();
 		        	}catch (Exception e) {
 						Log.i("Cache", "No schedule found in cache");
 		        	}
 		        }else{*/
 					loadData();
 				//}
 	        }
         }else{
         	Toast.makeText(getApplicationContext(),R.string.no_connection, Toast.LENGTH_SHORT).show();
         }
     }
     
     public void onClick(View v) {
         // do something when the button is clicked  	
     	if(v == m_n){
     		loadDay(1);
     	}
     	if(v == m_p){
     		loadDay(-1);
     	}
       }
     
     /*@Override
     public void onResume(){
     	if(iksuSchedule.activities.size() == 0){
     		loadData();
     	}
     }*/
     
     private void loadPrefs(){
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     	iksuSchedule.typeFilter = prefs.getString("iksuActType", "");
     	//Log.i("Prefs",iksuSchedule.typeFilter);
     }
     
     public void loadData(){
     	loadPrefs();
     	dialog = ProgressDialog.show(this, "", 
                 this.getString(R.string.fetching_schedule), true);
     	new IksuScheduleTask().execute();
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {//makes sure that the data is saved when an instance of the app is destroyed
         final IKSUSchedule data = iksuSchedule;
         return data;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.schedule_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.reload_menu_btn:
             loadData();
             return true;
         case R.id.today_menu_btn:
         	loadDayToView(0);
         	return true;
         case R.id.prefs_menu_btn:
         	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
         	startActivity(settingsActivity);
         }
         return super.onOptionsItemSelected(item);
     }
     
     private class IksuScheduleTask extends AsyncTask<String, Integer, Document>{
     	
     	Exception bgException;
     	
 		@Override
 		protected Document doInBackground(String... arg0) {
 			//android.os.Debug.waitForDebugger();
 				//Log.i("IksuScheduleTask", "Fetching Web Page");
 			
 				try {
 					String thePage = IKSUHelper.getXml();
 					//Log.i("IksuScheduleTask", thePage);
 					try {
 						//@REMINDER your return is here. So many potential errors. WTF
 						return IKSUHelper.parseTheXml(thePage);
 					} catch (SAXException e) {
 						handleError(e);
 						return null;
 					} catch (IOException e) {
 						handleError(e);
 						return null;
 					} catch (ParserConfigurationException e) {
 						handleError(e);
 						return null;
 					} catch (Exception e){
 						handleError(e);
 						return null;
 					}
 				} catch (ApiException e) {
 					handleError(e);
 					return null;
 				} catch (SocketTimeoutException e) {
 					handleError(e);
 					return null;
 				}
 		}
 		
 		private void handleError(Exception e){
 			//Log.i("IksuScheduleTask", "Handling the exception, like a boss.");
 			bgException = e;
 			bgException.printStackTrace();
 			if(dialog.isShowing())
 				dialog.dismiss();
 			cancel(true);
 		}
 		
 		@Override
 		protected void onPostExecute(Document result) {
 			//Log.i("IksuScheduleTask", "PostExecuting");
 			if(!isCancelled()){
 				iksuSchedule.thePage = result;
 				iksuSchedule.dates = IKSUHelper.getDatesArray(result);
 				
 				//Log.i("IksuScheduleTask", "first date:"+iksuSchedule.dates.get(0));
 				
 				//Log.i("IksuScheduleTask", "Got the dates");
 				dialog.dismiss();
 				iksuSchedule.lastRefreshed = new Date();
 				loadDayToView(dayIndex);
 				
 				if(iksuSchedule.getNumActivities() == 0){					
 					loadDayToView(++dayIndex);
 				}
 				
 				saveScheduleToCache();
 				
 				//if()
 					//Toast.makeText(getApplicationContext(), "Saved Object successfully", Toast.LENGTH_SHORT);
 				//else
 					//Toast.makeText(getApplicationContext(), "Failed to save object", Toast.LENGTH_SHORT);
 			}else{
 				Log.i("IksuScheduleTask","Task Cancelled due to caught exception.(PostExecute)");
 				cancel(true);
 				Toast.makeText(getApplicationContext(), R.string.casError, Toast.LENGTH_LONG).show();
 				
 				Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://netlogon.umu.se/index.cgi?referer=www.google.com"));
 				startActivity(viewIntent);
 			}
 		}
 		
 		@Override
 		protected void onCancelled(){
 			//kill the dialog window so it doesn't leak ALL OVER THE PLACE!
 			if(dialog.isShowing())
 				dialog.dismiss();
 			
 			Log.i("IksuScheduleTask","Task Cancelled due to caught exception.");
 			
 			Toast.makeText(getApplicationContext(), 
 					R.string.casError, Toast.LENGTH_LONG);
 		}
     	
     }
     
     private void loadDay(int direction){
     	if(iksuSchedule.dates.size() > 0){
     		if(direction == 1){
     			if(iksuSchedule.currentDateIndex < iksuSchedule.dates.size()-1){
     				loadDayToView(iksuSchedule.currentDateIndex + direction);
     			}
     		}
     		if(direction == -1){
     			if(iksuSchedule.currentDateIndex > 0){
     				loadDayToView(iksuSchedule.currentDateIndex + direction);
     			}
     		}
     	}
     }
     
     private void loadDayToView(int newDayIndex){
     	dayIndex = newDayIndex;
     	iksuSchedule.activities = IKSUHelper.getScheduleFromPageForDay(iksuSchedule.thePage, dayIndex, iksuSchedule.typeFilter);
     	m_date.setText(iksuSchedule.dates.get(dayIndex).trim());
     	iksuSchedule.currentDateIndex = dayIndex;
     	
     	if(dayIndex == 0 && iksuSchedule.dates.size() > 0){
 			m_p.setVisibility(View.INVISIBLE);
 			m_n.setVisibility(View.VISIBLE);
 		}else if(dayIndex == iksuSchedule.dates.size() -1){
     		m_p.setVisibility(View.VISIBLE);
 			m_n.setVisibility(View.INVISIBLE);
     	}else{
     		m_p.setVisibility(View.VISIBLE);
     		m_n.setVisibility(View.VISIBLE);
     	}
     	
     	//Log.i("IksuScheduleTask", "Day is" + iksuSchedule.dates.get(dayIndex));
     	
     	scheduleAdapter.notifyDataSetChanged();
     }
     
    /* private boolean scheduleIsCached(){
     	return new File(getIKSUCacheDir(), "schedule").exists();
     }
     
     private IKSUSchedule loadScheduleFromCache(){
     	Log.i("Cache", "Loading Object");
     	
     	IKSUSchedule cachedSched = null;
     	FileInputStream fis = null;
     	ObjectInputStream ois = null;
     	
     	final File scheduleFile = new File(getIKSUCacheDir(), "schedule");
     	
     	try {
 			fis = new FileInputStream(scheduleFile);
 			ois = new ObjectInputStream(fis);
 			cachedSched = (IKSUSchedule) ois.readObject();
 		} catch (Exception e) {
 			Log.e("Cache", e.getMessage());
 		}finally{
 			try{
 				if(fis != null) fis.close();
 				if(ois != null) ois.close();
 			}catch (Exception e) {}
 		}
     	
     	return cachedSched;
     }*/
     
     private boolean saveScheduleToCache(){
     	//Log.i("Cache", "Saving Object");
     	
     	FileOutputStream fos = null;
     	ObjectOutputStream oos = null;
     	boolean keep = true;
     	
     	final File scheduleFile = new File(getIKSUCacheDir(), "schedule");
     	
     	try{
     		fos = new FileOutputStream(scheduleFile);
     		oos = new ObjectOutputStream(fos);
     		oos.writeObject(iksuSchedule);
     	}catch (Exception e) {
 			// TODO: handle exception
 		}finally{
 			try{
 				if(oos != null) oos.close();
 				if(fos != null) fos.close();
 				if(keep == false) scheduleFile.delete();
 			}catch (Exception e) {
 				/* do nothing */
 			}
 		}
 		
 		//if(keep)
 			//Log.i("Cache", "Object successfully saved");
 		
 		return keep;
     }
     
     private File getIKSUCacheDir(){
     	File cacheDir;
     	
     	if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
     		cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "rawIksuSchedule");
     	else
     		cacheDir = getCacheDir();
     	
     	if(!cacheDir.exists())
     		cacheDir.mkdirs();
     	
     	return cacheDir;
     }
     
     public boolean isOnline(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context
         .getSystemService(Context.CONNECTIVITY_SERVICE);
         if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting())
             return true;
         return false;
 	}
     
     static class ActivityHolder{
     	TextView class_name, class_time, class_room, class_instructor;
     }
     
     private class ScheduleAdapter extends BaseAdapter{
     	
     	//private Context mCtx;
     	
     	public ScheduleAdapter (Context c){
     		//mCtx = c;
     	}
     	
     	@Override
         public int getCount() {
             return iksuSchedule.getNumActivities();
         }
 
         @Override
         public Object getItem(int arg0) {
             return null;
         }
 
         @Override
         public long getItemId(int pos) {
             return pos;
         }
         
         public View getView(int pos, View convertView, ViewGroup parent){
         	ActivityHolder actHolder;
         	
         	if(convertView == null){
         		actHolder = new ActivityHolder();
         		convertView = m_inflater.inflate(R.layout.main_list_item, parent, false);
         		actHolder.class_name = (TextView) convertView.findViewById(R.id.activity_name);
         		actHolder.class_time = (TextView) convertView.findViewById(R.id.activity_time);
         		actHolder.class_room = (TextView) convertView.findViewById(R.id.activity_room);
         		actHolder.class_instructor = (TextView) convertView.findViewById(R.id.activity_instructor);
         		
         		convertView.setTag(actHolder);
         	}else{
         		actHolder = (ActivityHolder) convertView.getTag();
         	}
         	        	
         	actHolder.class_name.setText(iksuSchedule.getActivityAtPosition(pos).name);
         	actHolder.class_time.setText(iksuSchedule.getActivityAtPosition(pos).time);
         	actHolder.class_room.setText(iksuSchedule.getActivityAtPosition(pos).room);
         	actHolder.class_instructor.setText(iksuSchedule.getActivityAtPosition(pos).instructor);
         	
         	return convertView;
         }
     }
 }
 
