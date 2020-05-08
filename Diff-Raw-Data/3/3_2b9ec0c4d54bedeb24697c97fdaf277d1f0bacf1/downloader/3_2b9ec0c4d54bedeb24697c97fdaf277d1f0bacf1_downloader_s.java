 package eu.cyanogen.downloader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.security.acl.LastOwnerException;
 import java.util.HashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.RemoteViews;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 public class downloader extends Activity implements OnSharedPreferenceChangeListener {
     /** Called when the activity is first created. */
 	
 	ListView displayData;
 	private static String URL="http://download.cyanogenmod.com";
 	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 	private String url;
 	private SharedPreferences prefs;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         displayData=(ListView)findViewById(R.id.displayData);
         /*Set click*/
         displayData.setOnItemClickListener(new OnItemClickListener(){
 			@Override
         	@SuppressWarnings("unchecked")
          	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
         		final HashMap<String, String> map = (HashMap<String, String>) displayData.getItemAtPosition(position);
         		AlertDialog.Builder adb = new AlertDialog.Builder(downloader.this);
         		adb.setTitle("Download this version ?");
         		adb.setMessage("Your choice : "+map.get("name")+"\n"+map.get("size")+"\n"+map.get("date"));
         		adb.setPositiveButton("Ok", new OnClickListener() {
         			@Override
         			public void onClick(DialogInterface arg0, int arg1)
         			{
         				download(map.get("link"));
         			}
         		});
         		adb.setNegativeButton("Cancel", null);
         		adb.show();
         	}
          });
         
         /*Get preferences and display data*/
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
         prefs.registerOnSharedPreferenceChangeListener(this);
         url=prefs.getString("downloadUrl",URL)+prefs.getString("phoneType","/");
         
         /*Start processes*/
         retrieveInformation();
         if(prefs.contains("useUpdater")&&prefs.getBoolean("useUpdater", false))
         	startChecker();
     }
     /**
      * By default check update every hours ... should not be used if not enabled ....
      */
     private void startChecker()
     {
     	if(prefs.contains("intervalUpdater"))
     		scheduler.schedule(new Runnable(){public void run(){checker();}},60*Integer.parseInt(prefs.getString("intervalUpdater", "60")) ,TimeUnit.SECONDS);
     }
     private void checker()
     {
     	/*TODO: Finish the checker*/
     	/*Notification ... need to find out a checking method ;)*/
         NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         Notification notification = new Notification(R.drawable.icon, "A new version of cyanogen is available", System.currentTimeMillis());
         Intent notificationIntent = new Intent(this, downloader.class);
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         notification.setLatestEventInfo(getApplicationContext(), "Cyanogen Update", "Version VERSION is now out for PHONE and NIGHTLY", contentIntent);
         mNotificationManager.notify(1, notification);
     }
     private void download(String url)
     {
//    	final String urlD = PreferenceManager.getDefaultSharedPreferences(this).getString("downloadUrl",URL)+url;
    	final String urlD = "http://192.168.1.12/"+url;
     	if(urlD.startsWith("http"))
     	{//use the browser to download ... so no need to have right to write data
     		//unable to DL zip ... too bad startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlD)));
     		
     		//Most uglier code ever !!!!!!!!! WTF 
     		final File fo = new File(getFilesDir(),urlD.substring(urlD.lastIndexOf("/")+1));
 			final ThreadChecker checker = new ThreadChecker(fo);
 			this.displayToast("File will be downloaded into : "+fo.getAbsolutePath());
 			Thread dl = new Thread()
 			{
 				@Override
 	            public void run()
 				{
 					try
 					{
 						HttpEntity he = new DefaultHttpClient().execute(new HttpGet(urlD)).getEntity();
 						checker.setLength((int) he.getContentLength());
 						checker.start();
 						he.writeTo(new FileOutputStream(fo));
     				}catch(Exception e){checker.stop();}
 				}
 			};
 			dl.start();
     	}else
     	{
     		this.displayToast("Problem with URL : download aborted ");
     	}
     }
     private void retrieveInformation()
     {
         ProgressDialog pdialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
     	ParseData pd = ParseData.getInstance(this, url,true);
         SimpleAdapter mSchedule = new SimpleAdapter (downloader.this.getBaseContext(), pd.retrieveDownloadsList(), R.layout.list, new String[] {"name", "date", "size","type"}, new int[] {R.id.name,R.id.date,R.id.size,R.id.type});
         displayData.setAdapter(mSchedule);
         pdialog.dismiss();
     }
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	menu.add(Menu.NONE,0,Menu.NONE,"Reload").setAlphabeticShortcut('r');
     	menu.add(Menu.NONE,1,Menu.NONE,"Preferences").setAlphabeticShortcut('p');
     	return(super.onCreateOptionsMenu(menu));
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	switch(item.getItemId())
     	{
 	    	case 0:
 	    		retrieveInformation();
 	    		return true;
 	    	case 1:
 	    		startActivity(new Intent(getBaseContext(),EditPreferences.class));
 	    		return true;
     	}
 		return super.onOptionsItemSelected(item);
     }
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
 		if(arg1.equals("downloadUrl"))
 		{
 			url=arg0.getString("downloadUrl",URL);
 		}
 		if(arg1.equals("phoneType"))
 		{
 			url=arg0.getString("downloadUrl",URL)+arg0.getString("phoneType","/");
 		}
 		if(arg1.equals("useUpdater"))
 		{
 			startChecker();
 		}
 	}
 	public void displayToast(String message)
 	{
 		Toast toast=Toast.makeText(this, message, Toast.LENGTH_LONG);  
 		toast.show();
 	}
 	class ThreadChecker extends Thread
 	{
 		int length;
 		File fo;
 		public ThreadChecker(File fo) {
 			this.fo = fo;
 		}
 		
 		@Override
         public void run()
 		{
 			Intent intent = new Intent(downloader.this, downloader.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
         // configure the ongoing event
             final Notification notification = new Notification(R.drawable.icon, "Downloading a ROM", System.currentTimeMillis());
             notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
             notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress);
             notification.contentIntent = pendingIntent;
             notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.icon);
             notification.contentView.setTextViewText(R.id.status_text, "Downloading "+fo.getName());
             notification.contentView.setProgressBar(R.id.status_progress, length, 0, false);
             NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
             notificationManager.notify(42, notification);
             
             try {
 	            while ( length>fo.length() ) {
 	    	         notification.contentView.setProgressBar(R.id.status_progress, length, (int) fo.length(), false);
 	    	         notificationManager.notify(42, notification);
 	    	         this.sleep(2000);//wait 2 sec between 
 	    	    }
 	            notificationManager.cancel(42);//remove notification once the download finished
 	        //display notification saying that application is downloaded
 //	            PendingIntent eventIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
 	            
 	            Notification notDL = new Notification(R.drawable.icon, "Finished ROM Download", System.currentTimeMillis());
 	            notDL.setLatestEventInfo(getApplicationContext(), "CyanogenMod Downloader", "File "+fo.getAbsolutePath()+" has been downloaded", pendingIntent);
 	            notificationManager.notify(2, notDL);
             }catch(InterruptedException e){return;}
 		}
 		
 		public void setLength(int length)
 		{
 			this.length=length;
 		}
 	};
 	
 }
