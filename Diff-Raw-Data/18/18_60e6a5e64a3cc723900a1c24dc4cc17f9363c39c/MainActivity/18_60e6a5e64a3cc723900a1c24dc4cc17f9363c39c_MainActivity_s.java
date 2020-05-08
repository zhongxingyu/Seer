 package de.gymbuetz.gsgbapp;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Locale;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import de.gymbuetz.gsgbapp.classes.Constants;
 import de.gymbuetz.gsgbapp.classes.DownloadXmlTask;
 
 public class MainActivity extends FragmentActivity implements OnItemSelectedListener {
 
 	private final static String updateUrl = Constants.UPDATE_URL;
 	private final static String updateSaveFile = Constants.UPDATE_SAVE_FILE;
 
 	SectionsPagerAdapter mSectionsPagerAdapter;
 	ProgressDialog mProgressDialog;
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		getOverflowMenu();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.action_refresh:
 				refresh();
 				break;
 			case R.id.action_settings:
 				Intent i = new Intent(this, SettingsActivity.class);
 				startActivity(i);
 		}
 
 		return true;
 	}
 
 	private void refresh() {
 		ProgressDialog pd = new ProgressDialog(this);
 		pd.setProgress(0);
 		pd.setTitle(R.string.loading);
 		pd.show();
 		new DownloadXmlTask(this, Constants.REPPLAN_URL, Constants.REPPALN_FILE_NAME, pd).execute();
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
 	 */
 
 	private void getOverflowMenu() {
 
 		try {
 			ViewConfiguration config = ViewConfiguration.get(this);
 			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
 			if (menuKeyField != null) {
 				menuKeyField.setAccessible(true);
 				menuKeyField.setBoolean(config, false);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment frag = new Fragment();
 
 			switch (position) {
 				case 0:
 					frag = new PlanFragment();
 					break;
 				case 1:
 					frag = new SheduleFragment();
 					break;
 				case 2:
 					frag = new AboutFragment();
 					break;
 			}
 			return frag;
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 				case 0:
 					return getString(R.string.title_repplan).toUpperCase(l);
 				case 1:
 					return getString(R.string.title_sheduler).toUpperCase(l);
 				case 2:
 					return getString(R.string.title_about).toUpperCase(l);
 			}
 			return null;
 		}
 
 	}
 
 	public void updateApp() {
 		// Progress dialog for the download
 		mProgressDialog = new ProgressDialog(MainActivity.this);
 		mProgressDialog.setMessage("Downloading an Update...");
 		mProgressDialog.setIndeterminate(false);
 		mProgressDialog.setMax(100);
 		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 
 		// Execution code for the download
 		new DownloadFile(updateUrl, updateSaveFile).execute();
 	}
 
 	private class DownloadFile extends AsyncTask<String, Integer, String> {
 
 		String url;
 		File saveFile;
 
 		public DownloadFile(String url, String saveFile) {
 			this.url = url;
 			this.saveFile = new File(getFilesDir(), saveFile);
 		}
 
 		@Override
 		protected String doInBackground(String... args) {
 
 			try {
 				URL url = new URL(this.url);
 				URLConnection connection = url.openConnection();
 				connection.connect();
 				// this will be useful so that you can show a typical 0-100% progress bar
 				int fileLength = connection.getContentLength();
 
 				// download the file
 				InputStream input = new BufferedInputStream(url.openStream());
 
 				OutputStream output = new FileOutputStream(saveFile);
 
 				byte data[] = new byte[1024];
 				long total = 0;
 				int count;
 				while ((count = input.read(data)) != -1) {
 					total += count;
 					// publishing the progress....
 					publishProgress((int) (total * 100 / fileLength));
 					output.write(data, 0, count);
 				}
 
 				output.flush();
 				output.close();
 				input.close();
 			} catch (Exception e) {
 				Log.e("Download failed!", e.getMessage());
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			mProgressDialog.show();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			super.onProgressUpdate(progress);
 			mProgressDialog.setProgress(progress[0]);
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 
 			Log.i("Intent", Uri.fromFile(saveFile).toString());
 
 			try {
 				Runtime.getRuntime().exec("chmod a+x " + getExternalFilesDir(DOWNLOAD_SERVICE) + saveFile);
 				Runtime.getRuntime().exec("chmod a+r " + getExternalFilesDir(DOWNLOAD_SERVICE) + saveFile);
 				Runtime.getRuntime().exec("chmod a+w " + getExternalFilesDir(DOWNLOAD_SERVICE) + saveFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// Intent for the Instalation
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setDataAndType(Uri.fromFile(saveFile), "application/vnd.android.package-archive");
 			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 			// Execution code for the installation
 			startActivity(intent);
 
 			mProgressDialog.cancel();
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 
 	}
 }
