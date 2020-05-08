 package ikeagold.zimniy.giolight;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GioLightUpdActivity extends Activity {
 
 	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
 	private ProgressDialog mProgressDialog;
 	public String fn;
 	public String fnt;
 	public String furlt;
 	public String furl;
 	public String glv; // for <> in future use
 	public boolean Test;
 	public boolean Upd;
 	public String str1;
 	public String str2;
 	SharedPreferences prefs;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		// Strings clean
 		str1 = "\n";
 		str2 = "\n";
 
 		// Get version installed
 		TextView tv9 = (TextView) findViewById(R.id.textView9);
 		String cleanstr3 = "";
 
 		try {
 			Process ifc = Runtime.getRuntime().exec("getprop ro.light.version");
 			BufferedReader bis = new BufferedReader(new InputStreamReader(
 					ifc.getInputStream()));
 			cleanstr3 = bis.readLine();
 			ifc.destroy();
 		} catch (java.io.IOException e) {
 		}
 		glv = " : " + cleanstr3 + "\n";
 		tv9.setText(glv);
 
 		// off button
 		Button button5 = (Button) findViewById(R.id.button5);
 		button5.setEnabled(false);
 
 		// Load saved caption
 		if (Test == false) {
 			TextView tv1 = (TextView) findViewById(R.id.textView1);
 			tv1.setText(" : -\n");
 		} else {
 			TextView tv1 = (TextView) findViewById(R.id.textView1);
 			tv1.setText(" : -\n");
 		}
 
 	}
 
 	// Get Preferences
 	@Override
 	public void onResume() {
 		super.onResume();
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		Upd = prefs.getBoolean("updkey", false);
 		Test = prefs.getBoolean("testkey", false);
		Button button5 = (Button) findViewById(R.id.button5);
		button5.setEnabled(false);
 		
 		if (Test == false) {
 			TextView tv1 = (TextView) findViewById(R.id.textView1);
			tv1.setText(" : -" + str1);
 		} else {
 			TextView tv1 = (TextView) findViewById(R.id.textView1);
			tv1.setText(" : -" + str2);
 		}
 
 		if (Upd == false) {
 		} else {
 			if (isInternetOn()) {
 				update();
 			} else {
 				Toast.makeText(this, "  :(", Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 	}
 
 	// Download buttons ZONE
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_DOWNLOAD_PROGRESS:
 			mProgressDialog = new ProgressDialog(this);
 			mProgressDialog.setMessage("  /sd-card/Light/...");
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgressDialog.setCancelable(false);
 			mProgressDialog.show();
 			return mProgressDialog;
 		default:
 			return null;
 		}
 	}
 
 	private class DownloadFile extends AsyncTask<String, String, String> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showDialog(DIALOG_DOWNLOAD_PROGRESS);
 		}
 
 		@Override
 		protected String doInBackground(String... url) {
 			int count;
 			try {
 				URL url1 = new URL(url[0]);
 				URLConnection conexion = url1.openConnection();
 				conexion.connect();
 				// progress bar 0-100%
 				int lenghtOfFile = conexion.getContentLength();
 				// download the file
 				InputStream input = new BufferedInputStream(url1.openStream());
 				OutputStream output = new FileOutputStream(
 						"/sdcard/Light/1.zip");
 				byte data[] = new byte[1024];
 				long total = 0;
 				while ((count = input.read(data)) != -1) {
 					total += count;
 					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
 					output.write(data, 0, count);
 				}
 				output.flush();
 				output.close();
 				input.close();
 			} catch (Exception e) {
 			}
 			return null;
 		}
 
 		protected void onProgressUpdate(String... progress) {
 			Log.d("ANDRO_ASYNC", progress[0]);
 			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
 		}
 
 		@Override
 		protected void onPostExecute(String unused) {
 			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
 		}
 
 	}
 
 	// Download button
 	public boolean button5_Click(View v) {
 		if (isInternetOn()) {
 			if (Test == false) {
 				DownloadFile downloadFile = new DownloadFile();
 				downloadFile.execute(furlt);
 				String RootDir = Environment.getExternalStorageDirectory()
 						+ File.separator + "Light";
 				File from = new File(RootDir, ("1.zip"));
 				File to = new File(RootDir, fnt);
 				from.renameTo(to);
 			} else {
 				DownloadFile downloadFile = new DownloadFile();
 				downloadFile.execute(furl);
 				String RootDir = Environment.getExternalStorageDirectory()
 						+ File.separator + "Light";
 				File from = new File(RootDir, ("1.zip"));
 				File to = new File(RootDir, fn);
 				from.renameTo(to);
 			}
 			return true;
 		} else {
 			Toast.makeText(this, "  :(", Toast.LENGTH_SHORT).show();
 			return false;
 		}
 	}
 
 	// Checking function separate
 	public void update() {
 		furlt = DownloadText("http://gio-light.googlecode.com/hg/url.txt");
 		String cleanstr1 = furlt.substring(38, 70);
 		fnt = cleanstr1;
 
 		furl = DownloadText("http://gio-light.googlecode.com/hg/url.testing.txt");
 		String cleanstr2 = furl.substring(38, 70);
 		fn = cleanstr2;
 
 		if (Test == false) {
 			str1 = DownloadText("http://gio-light.googlecode.com/hg/version.txt");
 			TextView tv1 = (TextView) findViewById(R.id.textView1);
 			tv1.setText(" : " + str1);
 		} else {
 			str2 = DownloadText("http://gio-light.googlecode.com/hg/version.testing.txt");
 			TextView tv2 = (TextView) findViewById(R.id.textView1);
 			tv2.setText(" : " + str2);
 		}
 
 		// Enable buttons
 		Button button5 = (Button) findViewById(R.id.button5);
 		button5.setEnabled(true);
 	}
 
 	// Check new
 	public boolean button4_Click(View v) {
 		if (isInternetOn()) {
 			update();
 			return true;
 		} else {
 			Toast.makeText(this, "  :(", Toast.LENGTH_SHORT).show();
 			return false;
 		}
 
 	}
 
 	private InputStream OpenHttpConnection(String urlString) throws IOException {
 		InputStream in = null;
 		int response = -1;
 		URL url = new URL(urlString);
 		URLConnection conn = url.openConnection();
 		if (!(conn instanceof HttpURLConnection))
 			throw new IOException("Not an HTTP connection");
 		try {
 			HttpURLConnection httpConn = (HttpURLConnection) conn;
 			httpConn.setAllowUserInteraction(false);
 			httpConn.setInstanceFollowRedirects(true);
 			httpConn.setRequestMethod("GET");
 			httpConn.connect();
 			response = httpConn.getResponseCode();
 			if (response == HttpURLConnection.HTTP_OK) {
 				in = httpConn.getInputStream();
 			}
 		} catch (Exception ex) {
 			throw new IOException("Error connecting");
 		}
 		return in;
 	}
 
 	private String DownloadText(String URL) {
 		int BUFFER_SIZE = 2000;
 		InputStream in = null;
 		try {
 			in = OpenHttpConnection(URL);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return "";
 		}
 		InputStreamReader isr = new InputStreamReader(in);
 		int charRead;
 		String str = "";
 		char[] inputBuffer = new char[BUFFER_SIZE];
 		try {
 			while ((charRead = isr.read(inputBuffer)) > 0) {
 				// convert the chars to a String
 				String readString = String
 						.copyValueOf(inputBuffer, 0, charRead);
 				str += readString;
 				inputBuffer = new char[BUFFER_SIZE];
 			}
 			in.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 		return str;
 	}
 
 	// Check isInternetOn
 	public final boolean isInternetOn() {
 		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
 				|| connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
 			return true;
 		} else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
 			return false;
 		}
 		return false;
 	}
 
 	// Menu
 	Menu myMenu = null;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		this.myMenu = menu;
 		MenuItem item1 = menu.add(0, 1, 0, R.string.pref_label);
 		item1.setIcon(R.drawable.ic_menu_pref);
 		MenuItem item2 = menu.add(0, 2, 0, R.string.alert_label);
 		item2.setIcon(R.drawable.ic_menu_alert);
 		MenuItem item3 = menu.add(0, 3, 0, R.string.install_label);
 		item3.setIcon(R.drawable.ic_menu_install);
 		MenuItem item4 = menu.add(0, 4, 0, R.string.info_label);
 		item4.setIcon(R.drawable.ic_menu_info);
 		MenuItem item5 = menu.add(0, 5, 0, R.string.vote);
 		item5.setIcon(R.drawable.ic_menu_vote);
 		MenuItem item6 = menu.add(0, 6, 0, R.string.about);
 		item6.setIcon(R.drawable.ic_menu_about);
 		return true;
 	}
 
 	private void AboutDialog() {
 		AlertDialog aboutDialog = new AlertDialog.Builder(this).create();
 		aboutDialog.setTitle(R.string.about);
 		aboutDialog.setMessage(getResources().getText(R.string.alasecond));
 		aboutDialog.setButton(getResources().getText(R.string.close),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 		aboutDialog.setIcon(R.drawable.ic_menu_about);
 		aboutDialog.show();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1:
 			Intent Prefs = new Intent(this, Prefs.class);
 			startActivity(Prefs);
 			break;
 		case 2:
 			Intent alert = new Intent(this, Alert.class);
 			startActivity(alert);
 			break;
 		case 3:
 			Intent install = new Intent(this, Install.class);
 			startActivity(install);
 			break;
 		case 4:
 			Intent info = new Intent(this, Info.class);
 			startActivity(info);
 			break;
 		case 5:
 			Intent marketIntent2 = new Intent(Intent.ACTION_VIEW,
 					Uri.parse("http://market.android.com/details?id="
 							+ getPackageName()));
 			startActivity(marketIntent2);
 			break;
 		case 6:
 			AboutDialog();
 			return true;
 		}
 		return false;
 	}
 }
