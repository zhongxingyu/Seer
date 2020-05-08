 package jieehd.villain.updater;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 
 
 
 public class VillainUpdater extends Activity {
 	
 	TextView tvDevice;
 	Button btnDown;
 	TextView tvUpdate_info;
 	TextView tvROM;
 	TextView tvBuild;
 	TextView tvUpdate;
 	TextView tvVer;
 	TextView tvNewVer;
 	HttpClient client;
 	JSONObject json;
 	JSONObject device;
 	String rom;
 	JSONObject device_id;
 	Button btnShow;
 	
 	
 	 final static String URL = "http://dl.dropbox.com/u/44265003/update.json";
 	 File sdDir = (Environment.getExternalStorageDirectory());
  	 public String PATH = sdDir + "/VillainROM/ROMs/VillainROM";
 	 protected static final String Display = null;
 	 
 		
 		//initialize progress bar
 		
 	    private ProgressDialog mProgressDialog;
 	    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         client = new DefaultHttpClient();
         tvDevice = (TextView) findViewById(R.id.tvDevice);
         tvBuild = (TextView) findViewById(R.id.tvBuild);
         tvROM = (TextView) findViewById(R.id.tvROM);
         tvVer = (TextView) findViewById(R.id.tvVer);
         tvUpdate = (TextView) findViewById(R.id.tvUpdate);
         tvNewVer = (TextView) findViewById(R.id.tvNewVer);
         tvUpdate_info = (TextView) findViewById(R.id.tvUpdate_info);
         btnDown = (Button) findViewById(R.id.btnDown);
         btnDown.setVisibility(4);
 
 
         
         
         String buildDevice = android.os.Build.DEVICE.toUpperCase();
         String buildVersion = android.os.Build.ID;
         String buildRom = android.os.Build.DISPLAY;
         String buildPrint = android.os.Build.FINGERPRINT;
         
         tvDevice.setText("Device: " + buildDevice);
         tvROM.setText("ROM: " + buildRom);
         tvVer.setText("Version: " + buildVersion);
         tvBuild.setText("Build-fingerprint: " + buildPrint);
         
        
         
         new Read().execute();
         
        
         
     }
     
     
     
     
     
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         	case R.id.view:			Intent intent = new Intent();
 									intent.setClass(getApplicationContext(), getfiles.class);
 									startActivity(intent);
         						break;
             case R.id.settings:     Intent settings = new Intent();
             						settings.setClass(getApplicationContext(), villainsettings.class);
             						startActivity(settings);
                                 break;
             case R.id.about:		Intent about = new Intent();
             						about.setClass(getApplicationContext(), about.class);
             						startActivity(about);
             					break;
             case R.id.exit: 		finish();
             						System.exit(0);
                                 break;
         }
         return true;
     }
     
     
     
     public JSONObject getVersion() 
     		throws ClientProtocolException, IOException, JSONException{
     	StringBuilder url = new StringBuilder(URL);
     	HttpGet get = new HttpGet(url.toString());
     	HttpResponse r = client.execute(get);
     	int status = r.getStatusLine().getStatusCode();
     	if (status == 200){
     		HttpEntity e = r.getEntity();
     		String data = EntityUtils.toString(e);
     		JSONObject stream = new JSONObject(data);
     		JSONObject rom = stream.getJSONObject("villain-roms");
     		return rom;
     	}else{
     		return null;
     	}
     }
     
     public class Display {
     	public String mRom;
     	public String mChange;
     	public String mUrl;
     	public String mBuild;
     	
     	public Display (String rom, String changelog, String downurl, String build) {
     		mRom = rom;
     		mChange = changelog;
     		mUrl = downurl;
     		mBuild = build;
     	
     	
     	}
     }
     
     
     
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
 		case DIALOG_DOWNLOAD_PROGRESS:
 			mProgressDialog = new ProgressDialog(this);
 			mProgressDialog.setMessage("Downloading File...");
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgressDialog.setCancelable(true);
 			mProgressDialog.setProgress(0);
 			mProgressDialog.show();
 			return mProgressDialog;
 		default:
 			return null;
         }
     }
 
     	
     public class Read extends AsyncTask<String, Integer, Display> {
     	
 
 		@Override
 		protected Display doInBackground(String... params) {
 			String buildDevice = android.os.Build.DEVICE.toUpperCase();
 			// TODO Auto-generated method stub
 			try {
 				if (buildDevice.equals("PYRAMID")) {
 				json = getVersion();
 				String rom = json.getJSONObject("device").getJSONArray("PYRAMID").getJSONObject(0).getString("version");
 				String changelog = json.getJSONObject("device").getJSONArray("PYRAMID").getJSONObject(0).getString("changelog");
 				String downurl = json.getJSONObject("device").getJSONArray("PYRAMID").getJSONObject(0).getString("url");
 				String build = json.getJSONObject("device").getJSONArray("PYRAMID").getJSONObject(0).getString("rom");
 				return new Display(rom, changelog, downurl, build);
 				} else if (buildDevice.equals("MAGURO")) {
 					json = getVersion();
 					String rom = json.getJSONObject("device").getJSONArray("MAGURO").getJSONObject(0).getString("version");
 					String changelog = json.getJSONObject("device").getJSONArray("MAGURO").getJSONObject(0).getString("changelog");
 					String downurl = json.getJSONObject("device").getJSONArray("MAGURO").getJSONObject(0).getString("url");
 					String build = json.getJSONObject("device").getJSONArray("MAGURO").getJSONObject(0).getString("rom");
 					return new Display(rom, changelog, downurl, build);
 				} else if (buildDevice.equals("I9100")) {
 					json = getVersion();
 					String rom = json.getJSONObject("device").getJSONArray("I9100").getJSONObject(0).getString("version");
 					String changelog = json.getJSONObject("device").getJSONArray("I9100").getJSONObject(0).getString("changelog");
 					String downurl = json.getJSONObject("device").getJSONArray("I9100").getJSONObject(0).getString("url");
 					String build = json.getJSONObject("device").getJSONArray("I9100").getJSONObject(0).getString("rom");
 					return new Display(rom, changelog, downurl, build);
 				} else if (buildDevice.equals("PASSION")) {
 					json = getVersion();
 					String rom = json.getJSONObject("device").getJSONArray("PASSION").getJSONObject(0).getString("version");
 					String changelog = json.getJSONObject("device").getJSONArray("PASSION").getJSONObject(0).getString("changelog");
 					String downurl = json.getJSONObject("device").getJSONArray("PASSION").getJSONObject(0).getString("url");
 					String build = json.getJSONObject("device").getJSONArray("PASSION").getJSONObject(0).getString("rom");
 					return new Display(rom, changelog, downurl, build);
 				} else if (buildDevice.equals("GENERIC")) {
 					json = getVersion();
 					String rom = json.getJSONObject("device").getJSONArray("GENERIC").getJSONObject(0).getString("version");
 					String changelog = json.getJSONObject("device").getJSONArray("GENERIC").getJSONObject(0).getString("changelog");
 					String downurl = json.getJSONObject("device").getJSONArray("GENERIC").getJSONObject(0).getString("url");
 					String build = json.getJSONObject("device").getJSONArray("GENERIC").getJSONObject(0).getString("rom");
 					return new Display(rom, changelog, downurl, build);
 				} else if (buildDevice.equals(null)) {
 					String rom = "Could not retrieve device information";
 					String changelog = "Could not retrieve information from server";
 					String downurl = "Could not retrieve information from server";
 					String build = "Could not retrieve information from server";
 					return new Display(rom, changelog, downurl, build);}
 						
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			return null;
 			}
 		
 		@Override
 		public void onPostExecute(final Display result) {
 			
 		    
 		    
         	boolean file = new File(sdDir + "/VillainROM/ROMs").exists(); {
         	if (file == true) {
         		
         	}else if (file == false){
         		new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/VillainROM/ROMs").mkdirs();
         	}}
         	
         	boolean file2 = new File(sdDir + "/VillainROM/Tweaks").exists(); {
         	if (file2 == true) {
         		
         	}else if (file2 == false){
         		new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/VillainROM/Tweaks").mkdirs();
         	}}
         	
 
 			String buildVersion = android.os.Build.ID;
 					if (buildVersion.equals(result.mRom)) {
 						tvUpdate.setText("No new updates available!");
 						tvNewVer.setText(":(");
 					}else{
 						tvUpdate.setText("New updates available!");
 						tvNewVer.setText("New version: " + result.mRom);
 						tvUpdate_info.setText("Changelog: " + result.mChange);
 						btnDown.setVisibility(0);
 						
 						
 				        btnDown.setOnClickListener(new OnClickListener()  {
 
 
 							public void onClick(View v) {
 								showDialog(DIALOG_DOWNLOAD_PROGRESS);
 								new Thread(new Runnable() {
 
 								public void run(){
 									try {
 										URL getUrl = new URL (result.mUrl);
 										final File file = new File(PATH + "_" + result.mBuild + "_"  + result.mRom + ".zip");
 										long startTime = System.currentTimeMillis();
 										Log.d("Download Manager", "download beginning: " + startTime);
 										Log.d("Download Manager", "download url: " + getUrl);
 										Log.d("Download Manager", "file name: " + file);
 																	
 										
 										URLConnection ucon = getUrl.openConnection();
 										final int lengthOfFile = ucon.getContentLength();
 										ucon.connect();
 										InputStream input = new BufferedInputStream(getUrl.openStream());
 										OutputStream output = new FileOutputStream(PATH + "_" + result.mBuild + "_" + result.mRom + ".zip");
 										
 										
 										byte data[] = new byte[1024];
 																		
 										int current;
 										long total = 0;
 										while ((current = input.read(data)) != -1) {
 								       		output.write(data, 0, current);
 											total += current;
 											updateProgress(total, lengthOfFile);
 											}
 										mProgressDialog.dismiss();
 										output.flush();
 										output.close();
 										input.close();
 										
 									
 										
 																											
 										long finishTime = System.currentTimeMillis();
 										Log.d("Download Manager", "download finished: " + finishTime);
 									}catch (ClientProtocolException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 									} catch (IOException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 									}
 								    }
 								
 								public void updateProgress(final long total, final int lengthOfFile) {
 									runOnUiThread(new Runnable() {
 
 										public void run() {
 									    mProgressDialog.setMax(lengthOfFile);
 									    mProgressDialog.setProgress((int) total);
 									    
 									    tvUpdate.setText("Download Successful!");
 									    tvNewVer.setText("Your file can be found at" + PATH);
 									    tvUpdate_info.setText("");
 									    btnDown.setVisibility(4);
 									}});
 									}
 								
 							    	
 								}).start();
 								
 
 								
 								
 
 							}
 
 						
 
 					}
 		);}
 
 		}
 		
 }
 }
