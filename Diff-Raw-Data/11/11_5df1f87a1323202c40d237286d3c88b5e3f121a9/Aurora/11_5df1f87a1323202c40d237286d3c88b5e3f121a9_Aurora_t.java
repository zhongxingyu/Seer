 package aurora.project;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TabHost;
 import android.widget.Toast;
 import android.widget.TabHost.OnTabChangeListener;
 
 public class Aurora extends TabActivity {
 	
 	public static final String KEY_USER = "username";
 	public static int USER_ID;
 	public static int GROUP_ID;
 	public static String USERNAME;
 	private TabHost tabHost;
 	private RelativeLayout signinscreen; 
 	private ImageView auroraLogo;
 	private Button signin;
 	private EditText username;
 	private EditText password;
 	private CheckBox remember;
 	private SharedPreferences prefs;
 	
 	//TODO
 	private static NetworkConnectivityListener connectivityListener;
 	private final int CONNECTIVITY_MSG = 0;
 	private ProgressDialog dialog;
 	private static ArrayList<AsyncTask> tasks;
 	public static void addTask(AsyncTask task) {
 		tasks.add(task);
 	}
 	public static void killTasks() {
 		for(AsyncTask task : tasks) {
     		task.cancel(true);
     	}
     	tasks.clear();
 	}
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Resources res = getResources();
         tabHost = getTabHost();
         TabHost.TabSpec spec;
         Intent intent;
         
         signinscreen = (RelativeLayout) findViewById(R.id.signinscreen);
         auroraLogo = (ImageView) findViewById(R.id.auroraLogo);
         signin = (Button) findViewById(R.id.signin);
         username = (EditText) findViewById(R.id.username);
         password = (EditText) findViewById(R.id.password);
         remember = (CheckBox) findViewById(R.id.remember);
         prefs = getPreferences(MODE_PRIVATE);
         
        auroraLogo.setImageResource(R.drawable.final_aurora_logo_290w);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) 
        	auroraLogo.setVisibility(4);
         tabHost.setVisibility(4);
         
         try{
         username.setText(prefs.getString("username", ""));
         password.setText(prefs.getString("password", ""));
         if(!username.getText().toString().equals(""))
         	remember.setChecked(true);
         } catch(Exception e) {
         	Log.e("AURORA", "ERROR RETRIEVING SAVED PREFERENCES");
         }
         
         //TODO
       //manage network connection
     	Handler mHandler = new Handler() {
     		public void handleMessage(Message msg) {
     			switch(msg.what) {
     			case CONNECTIVITY_MSG:
     				if(!isOnline())
     					Toast.makeText(Aurora.this, "No internet connection", Toast.LENGTH_LONG).show();
     				break;
     			}
     		}
     	};
         connectivityListener = new NetworkConnectivityListener();
         connectivityListener.registerHandler(mHandler, CONNECTIVITY_MSG);	 
     	connectivityListener.startListening(this);
     	tasks = new ArrayList<AsyncTask>();
         
         signin.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		//				check username and password from EditText's username and password
         		dialog = ProgressDialog.show(Aurora.this, "", "Loading. Please wait...", true);
         		new GetUserIdTask().execute();	
         	}    		
         });
         
         intent = new Intent().setClass(this,PostActivity.class);
         spec = tabHost.newTabSpec("Post").setIndicator("Post",
                 res.getDrawable(R.drawable.ic_tab_post))
             .setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, FriendsActivity.class);
         spec = tabHost.newTabSpec("Friends").setIndicator("Friends",
                 res.getDrawable(R.drawable.ic_tab_friends))
             .setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, GamesActivity.class);
         spec = tabHost.newTabSpec("Games").setIndicator("Games",
                 res.getDrawable(R.drawable.ic_tab_games))
             .setContent(intent);
         tabHost.addTab(spec);
         
         intent = new Intent().setClass(this, TalkActivity.class);
         spec = tabHost.newTabSpec("Talk").setIndicator("Talk",
                 res.getDrawable(R.drawable.ic_tab_talk))
             .setContent(intent);
         tabHost.addTab(spec);
 
         tabHost.setCurrentTab(0);
         
         tabHost.setOnTabChangedListener(new OnTabChangeListener(){
         	@Override
         	public void onTabChanged(String tabId) {
         		if("Post".equals(tabId)) { 
         			PostActivity activity = (PostActivity) getLocalActivityManager().getActivity(tabId); 
         			activity.myOnResume();  
         		}
         		else if("Friends".equals(tabId)) { 
         			FriendsActivity activity = (FriendsActivity) getLocalActivityManager().getActivity(tabId); 
         			activity.myOnResume();  
         		}
         		else if("Games".equals(tabId)) { 
         			GamesActivity activity = (GamesActivity) getLocalActivityManager().getActivity(tabId); 
         			activity.myOnResume();  
         		}
         		else if("Talk".equals(tabId)) { 
         			TalkActivity activity = (TalkActivity) getLocalActivityManager().getActivity(tabId); 
         			activity.myOnResume();  
         		}
         	}
         });
     }
 	
 	//TODO
 	public static boolean isOnline() {
 		NetworkInfo networkInfo = connectivityListener.getNetworkInfo();
 		return networkInfo.isConnected();
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	    super.onConfigurationChanged(newConfig);
 	    if (newConfig.orientation == (Configuration.ORIENTATION_LANDSCAPE))
 	    	auroraLogo.setVisibility(4);
 	    if (newConfig.orientation == (Configuration.ORIENTATION_PORTRAIT))
 	    	auroraLogo.setVisibility(0);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    menu.add("Sign Out").setIcon(R.drawable.undo);
 	    return true;
 	}
 
 	/* Handles item selections */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		tabHost.setVisibility(4);
 		signinscreen.setVisibility(0);
 		return true;
 	}
 	
 	private class GetUserIdTask extends AsyncTask<Void, Void, Integer> {
 		@Override
 		protected Integer doInBackground(Void... params) {
 			String result = "";
 			//the login data to send
 			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("username", username.getText().toString()));
 			nameValuePairs.add(new BasicNameValuePair("password", password.getText().toString()));
 			InputStream is = null;
 			
 			//http post
 			try{
 			        HttpClient httpclient = new DefaultHttpClient();
 			        HttpPost httppost = new HttpPost("http://auroralabs.cornellhci.org/android/login.php");
 			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			        HttpResponse response = httpclient.execute(httppost);
 			        
 			        HttpEntity entity = response.getEntity();
 			        is = entity.getContent();
 			}catch(Exception e){
 			        Log.e("log_tag", "Error in http connection "+e.toString());
 			}
 			
 			//convert response to string
 			try{
 			        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
 			        StringBuilder sb = new StringBuilder();
 			        String line = null;
 			        while ((line = reader.readLine()) != null) {
 			                sb.append(line);
 			        }
 			        is.close();
 			 
 			        result=sb.toString();
 			}catch(Exception e){
 			        Log.e("AURORA", "Error converting result "+e.toString());
 			}
 			
 			//TODO
 			if(result.equals("") || result.equals("-1"))
 				return -1;
 			else {
 				try{
 					SharedPreferences.Editor ed = prefs.edit();
 					if (remember.isChecked()){
 						ed.putString("username", username.getText().toString());
 						ed.putString("password", password.getText().toString());
 						ed.putInt("userId", Integer.valueOf(result));
 					} else {  		
 						ed.clear();              	
 					}
 					ed.commit();
 				} catch(Exception e) {
 					Log.e("AURORA", e.toString());
 					e.printStackTrace();
 				}
 
 				return Integer.parseInt(result);
 			}
 		}
 		
 		//TODO
 		@Override
     	protected void onPostExecute(Integer userId){
     		if (userId >=0){	
     			Aurora.USER_ID = userId;
     			Aurora.USERNAME = username.getText().toString();
     			try{				
     				tabHost.setVisibility(0);
     				signinscreen.setVisibility(8);  				
     			} catch(Exception e) {
     				Log.e("AURORA", "ERROR SAVING PREFERENCES");
     			}
     		} else{
     			if(!isOnline())
 					Toast.makeText(Aurora.this, "No internet connection", Toast.LENGTH_LONG).show();
 				else {
 					Toast.makeText(Aurora.this, "Incorrect Username & Password Combination", Toast.LENGTH_LONG).show();
 					password.setText("");
 				}
     		}
     		dialog.dismiss();
     		((PostActivity)getLocalActivityManager().getActivity("Post")).myOnResume();
     	}
     	
     }
 }
