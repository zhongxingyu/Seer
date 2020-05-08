 package de.eahapp.gui;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.holoeverywhere.widget.ProgressBar;
 import org.holoeverywhere.widget.TextView;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import de.eahapp.OnlineHelper;
 import de.eahapp.Project;
 import de.eahapp.SharedPreferencesHelper;
 import de.eahapp.Utils;
 import de.eahapp.gui.navigator.DataBase;
 import vu.alfonsodiecko.stundenplanfhjena.R;
 public class SplashScreen extends Activity {
 	private ProgressBar progress;
 	private TextView text;
 	private int state = 1;
 	private Context context;
 	// used to know if the back button was pressed in the splash screen activity
 	// and avoid opening the next activity
 	private boolean mIsBackButtonPressed;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle b = getIntent().getExtras();
 		setContentView(R.layout.splash_screen);
 		context = this.getApplicationContext();
 		progress = (ProgressBar) findViewById(R.id.progressBarLoad);
 		text = (TextView) findViewById(R.id.textViewLoader);
 		String hash = SharedPreferencesHelper.getSharedPreferences(context, Project.Strings.dbHash);
 		boolean update = hash.equals("") || SharedPreferencesHelper.getSharedPreferences(context, Project.Settings.autoUpdate).equals("true") || b != null && b.getString("update").equals("update");
     	if(!new File(Utils.getAppFilespath(context)+DataBase.DATABASE_NAME).exists()){
     		try {
 				Utils.copyDataBase(Utils.getAppFilespath(context), DataBase.DATABASE_NAME, context);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
 
 		if (!isOnline() && hash.equals("")) {
 			text.setText(this.getResources().getString(R.string.keineInet));
 			sleepForSomeTime(3000, false);
 		} else {
 			if (update) {
 
 				update(hash);
 
 			} else {
 				sleepForSomeTime(3000, true);
 			}
 
 		}
 
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	private void update(String oldHash) {
 
 		DownloadFileAsync task = new DownloadFileAsync();
 		task.execute(Project.Websites.pathToDBHash, Project.Websites.pathToDB, oldHash);
 
 	}
 
 	private void sleepForSomeTime(int time, final boolean toTermin) {
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				// make sure we close the splash screen so the user won't come
 				// back when it presses back key
 				if (toTermin) {
 					finish();
 					if (!mIsBackButtonPressed) {
 						// start the home screen if the back button wasn't
 						// pressed
 						// already
 
 							SplashScreen.this.startActivity(new Intent(SplashScreen.this, Homescreen.class));
 						
 
 					}
 				}
 			}
 		}, time);
 	}
 
 	@Override
 	public void onBackPressed() {
 
 		// set the flag to true so the next activity won't start up
 		mIsBackButtonPressed = true;
 		super.onBackPressed();
 
 	}
 
 	class DownloadFileAsync extends AsyncTask<String, String, String> {
 		@Override
 		protected void onPreExecute() {
 			text.setText("Suche nach Updates...");
 			super.onPreExecute();
 		}
 		@Override
 		protected String doInBackground(String... aurl) {
 			int count;
 			final String path = Utils.getAppFilespath(context);
 			final String zipFile = path + "studiengang.zip";
 			try {
 				String newhash = OnlineHelper.getWebsite(new URL(aurl[0]), "UTF-8");
 				Thread.sleep(1000);
 				if (newhash.equals(aurl[2])) {
 					publishProgress("Keine Updates gefunden...", "");
 					Thread.sleep(1000);
 					return null;
 				}
 				publishProgress("Update gefunden...", "");
 				Thread.sleep(1000);
 				URL url = new URL(aurl[1]);
 				URLConnection conexion = url.openConnection();
 				conexion.connect();
 				int lenghtOfFile = conexion.getContentLength();
 				InputStream input = new BufferedInputStream(url.openStream());
 				// create if not exist
 				File appDir = new File(path);
 				if (!appDir.exists()) {
 					appDir.mkdir();
 				}
 				OutputStream output = new FileOutputStream(zipFile);
 
 				byte data[] = new byte[1024];
 
 				long total = 0;
 
 				while ((count = input.read(data)) != -1) {
 					total += count;
					String[] progress = { "" + (int) ((total * 100) / lenghtOfFile) + "%", "Download " + lenghtOfFile / (1024 * 1000) + "mb " };
 					publishProgress(progress);
 					output.write(data, 0, count);
 				}
 
 				output.flush();
 				output.close();
 				input.close();
 				try {
 					publishProgress("Entpacke Datenbank", "");
 					Utils.unpackZip(path, "studiengang.zip");
 					File zip = new File(zipFile);
 					zip.delete();
 					publishProgress("Kopiere Datenbank", "");
 
 				} finally {
 					SharedPreferencesHelper.setSharedPreferences(context, Project.Strings.dbHash, newhash);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onProgressUpdate(String... progress) {
 			text.setText(String.valueOf(progress[1] + progress[0]));
 		}
 
 		@Override
 		protected void onPostExecute(String unused) {
 
 			sleepForSomeTime(2000, true);
 		}
 	}
 
 }
