 package de.saschahlusiak.hrw.dienststatus.statistic;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import java.io.InputStream;
 import java.net.URL;
 import de.saschahlusiak.hrw.dienststatus.AboutActivity;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.model.StatisticsProvider;
 
 public class StatisticsActivity extends ListActivity implements OnItemClickListener {
 	private StatisticsAdapter adapter;
 	int category;
 	
 	static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/zahlen-und-fakten";
 
 	public static final String STATISTIC_TITLES[] = {
 			null, "Internet", "IPv6", "E-Mail", "Wireless-LAN", "LSF", "Moodle", "VPN", "Monitoring",
 			};
 	public static final String STATISTICS[][] = { 
 			/* 0 */	{ "internet", "internet-ipv6", "email", "wlan-hswgt2", "lsf", "moodle-week", "vpn", "nagvis-day" },
 			/* 1 */ { "internet", "internet-month", "internet-year" },
 			/* 2 */ { "internet-ipv6", "internet-ipv6-month", "internet-ipv6-year", "internet-ipv6-percent", "internet-ipv6-percent-year" },
 			/* 3 */ { "email", "email-week", "email-month", "email-year" },
 			/* 4 */ { "wlan-hrw", "wlan-hrw-week", "wlan-hrw-month", "wlan-hrw-year", 
 				"wlan-hswgt2", "wlan-hswgt2-week", "wlan-hswgt2-month", "wlan-hswgt2-year",
 				"wlan-eduroam", "wlan-eduroam-week", "wlan-eduroam-month", "wlan-eduroam-year" },
 				/* 5 */ { "lsf", "lsf-week", "lsf-month", "lsf-year" },
 				/* 6 */ { "moodle-week", "moodle-month", "moodle-year" },
 				/* 7 */ { "vpn", "vpn-week", "vpn-month", "vpn-year" },
 				/* 8 */ { "nagvis-day", "nagvis-week", "nagvis-month", "nagvis-year" }
 	};
 	
 	private class PictureBundle {
 		int index;
 		BitmapDrawable d;
 	};
 	
 	private class RefreshTask extends AsyncTask<String, PictureBundle, String> {
 		boolean force;
 		
 		public RefreshTask(boolean force) {
 			this.force = force;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected String doInBackground(String... urls) {
 			publishProgress();
 		
 			for (int i=0; i < urls.length; i++) {
 				try {
 					StatisticsAdapter.Statistic s = (StatisticsAdapter.Statistic)adapter.getItem(i);
 					if (s.valid)
 						continue;
 					
 					PictureBundle b = new PictureBundle();
 					b.index = i;
 					b.d = StatisticsProvider.getImage(StatisticsActivity.this, urls[i], force);
 					if (isCancelled())
 						break;
 					publishProgress(b);
 				} catch (Exception e) {
 					e.printStackTrace();
 					PictureBundle b = new PictureBundle();
 					b.index = i;
 					b.d = null;
 					publishProgress(b);
 //					return getString(R.string.connection_error);
 				}
 			}
 
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(PictureBundle... values) {
 			if (values != null && values.length > 0) {
 				adapter.add(values[0].d, values[0].index);
 				adapter.setLoading(values[0].index + 1);
 			} else {
 				adapter.setLoading(0);
 			}
 
 			adapter.notifyDataSetChanged();
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			adapter.setLoading(-1);
 			adapter.notifyDataSetChanged();
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (result != null)
 				Toast.makeText(StatisticsActivity.this, result, Toast.LENGTH_SHORT).show();
 			adapter.setLoading(-1);
 			adapter.notifyDataSetChanged();
 			super.onPostExecute(result);
 		}
 	}
 	
 	RefreshTask task = null;
 
 	
 	private void refresh(boolean force) {
 		if (task != null)
 			task.cancel(false);
 		task = new RefreshTask(force);
 		task.execute(STATISTICS[category]);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		adapter = new StatisticsAdapter(this);
 		getListView().setAdapter(adapter);
 		getListView().setOnItemClickListener(this);
 		
 		category = 0;
 		if (getIntent() != null) {
 			if (getIntent().getExtras() != null) {
 				category = (int) getIntent().getExtras().getLong("category");
 				setTitle(getTitle() + " - " + STATISTIC_TITLES[category]);
 			}
 		}
 		adapter.invalidate(STATISTICS[category].length);
 		refresh(false);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.optionsmenu, menu);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.refresh) {
 			adapter.invalidate(STATISTICS[category].length);			
 			refresh(true);
 			return true;
 		}
 		if (item.getItemId() == R.id.about) {
 			Intent intent = new Intent(this, AboutActivity.class);
 			startActivity(intent);
 		}
 		if (item.getItemId() == R.id.gotowebsite) {
 			Intent intent = new Intent(
 					"android.intent.action.VIEW",
 					Uri.parse(WEBSITE));
 			startActivity(intent);
 			return true;
 		}
 		if (item.getItemId() == R.id.sendemail) {
 			try {
 				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("plain/text");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "rz-service@hs-weingarten.de" });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: " + WEBSITE);
 				startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if (category != 0)
 			return;
 		
 		Intent intent = new Intent(this, StatisticsActivity.class);
 		intent.putExtra("category", arg3 + 1);
 		startActivity(intent);
 	}
 
 }
