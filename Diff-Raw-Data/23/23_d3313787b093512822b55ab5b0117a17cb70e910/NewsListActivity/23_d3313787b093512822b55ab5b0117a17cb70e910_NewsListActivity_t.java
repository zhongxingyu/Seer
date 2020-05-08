 package de.saschahlusiak.hrw.dienststatus.news;
 
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Stack;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import de.saschahlusiak.hrw.dienststatus.AboutActivity;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.model.NewsItem;
 import de.saschahlusiak.hrw.dienststatus.model.NewsProvider;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class NewsListActivity extends Activity implements OnItemClickListener {
 	NewsAdapter adapter;
 	ListView list;
 	ProgressBar progress;
 	
 	private static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/aktuelles";
 	private static final String tag = NewsListActivity.class.getSimpleName();
 
 	
 	public class RefreshTask extends AsyncTask<Void, NewsItem, String> implements NewsProvider.OnNewNewsItem {
 		ArrayList<NewsItem> mynodes = new ArrayList<NewsItem>();
 		
 		@Override
 		protected void onPreExecute() {
 			progress.setVisibility(View.VISIBLE);
 			adapter.setNodes(mynodes);
 			adapter.notifyDataSetChanged();
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected String doInBackground(Void... args) {
 			return NewsProvider.fetchNews(NewsListActivity.this, this);
 		}
 		
 		@Override
 		protected void onProgressUpdate(NewsItem... values) {
 			mynodes.add(values[0]);
 			adapter.notifyDataSetChanged();
 			progress.setVisibility(View.GONE);
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			Toast.makeText(NewsListActivity.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
 			progress.setVisibility(View.GONE);
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (result != null)
 				Toast.makeText(NewsListActivity.this, result, Toast.LENGTH_SHORT).show();
 			progress.setVisibility(View.GONE);
 			super.onPostExecute(result);
 		}
 
 		@Override
 		public void onNewNewsItem(NewsItem item) {
 			publishProgress(item);
 		}
 	}
 	
 	RefreshTask task = null;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.news_list_activity);
 		
 		list = (ListView) findViewById(R.id.list);
 		progress = (ProgressBar) findViewById(R.id.progress);
 		
 		adapter = new NewsAdapter(this);
 		list.setAdapter(adapter);
 
 		list.setOnItemClickListener(this);
 		
 		task = new RefreshTask();
 		task.execute();
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		NewsItem news;
 
 		news = (NewsItem) adapter.getItem(position);
 		Intent intent = new Intent(this, NewsItemActivity.class);
 		intent.putExtra("newsItem", news);
 		startActivity(intent);		
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
 			if (task != null) {
 				task.cancel(false);
 				try {
 					task.get();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			task = new RefreshTask();
 			task.execute();
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
				intent.setType("text/plain");
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
 
 }
