 package de.saschahlusiak.hrw.dienststatus.dienste;
 
 import de.saschahlusiak.hrw.dienststatus.AboutActivity;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.dienstdetails.DetailActivity;
 import de.saschahlusiak.hrw.dienststatus.model.Dienststatus;
 import de.saschahlusiak.hrw.dienststatus.model.HRWNode;
 
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
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class DienststatusActivity extends Activity implements
 		OnItemClickListener {
 	
 	ListView list;
 	DienststatusAdapter adapter;
 	
 	private final String tag = DienststatusActivity.class.getSimpleName();
 	private String level = "all";
 	private static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/dienststatus";
 
 	private class RefreshTask extends AsyncTask<Void, Integer, String> {
 		@Override
 		protected void onPreExecute() {
 			setProgressBarIndeterminateVisibility(true);
 			super.onPreExecute();
 		}
 
 		@Override
 		protected String doInBackground(Void... arg0) {
 			publishProgress(0, 0);
 			return Dienststatus.fetch(DienststatusActivity.this);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			setProgressBarIndeterminateVisibility(false);
 			Toast.makeText(DienststatusActivity.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
 
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			setProgressBarIndeterminateVisibility(false);
 			if (result == null)
 				adapter.fillLevel(level);
 			else
 				Toast.makeText(DienststatusActivity.this, result, Toast.LENGTH_SHORT).show();
 			super.onPostExecute(result);
 		}
 	}
 	
 	RefreshTask refreshTask = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Intent intent;
 		Bundle extras;
 
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		setContentView(R.layout.main_activity);
 		
 		list = (ListView) findViewById(R.id.list);
 
 		intent = getIntent();
 
 		level = "all";
 		// level = null;
 		if (intent != null) {
 			extras = intent.getExtras();
 			if (extras != null) {
 				level = extras.getString("level");
 				setTitle(extras.getString("name"));
 			}
 		} else
 			setTitle(R.string.app_name);
 
 		adapter = new DienststatusAdapter(this, level == null);
 		list.setAdapter(adapter);
 
 		list.setOnItemClickListener(this);
 		registerForContextMenu(list);
 		setProgressBarIndeterminateVisibility(false);
 		
 		/* only refresh if this is a new activity and the root activity */
 		if ((savedInstanceState == null) && (level == null || level.equals("all"))) {
 			if (refreshTask != null) {
 				refreshTask.cancel(true);
 				try {
 					refreshTask.get();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			refreshTask = new RefreshTask();
 			refreshTask.execute();
 		}
 	}
 	
 	@Override
 	protected void onStart() {
 		adapter.fillLevel(level);
 		super.onStart();
 	}
 
 	public void showDetails(HRWNode node) {
 		Intent intent = new Intent(this, DetailActivity.class);
 		intent.putExtra("name", node.name);
 		intent.putExtra("duration", node.duration);
 		intent.putExtra("url", node.url);
 		intent.putExtra("label", node.title);
 		intent.putExtra("status", node.status);
 		intent.putExtra("acknowledged", node.acknowledged);
 		intent.putExtra("name", node.name);
 		if (node.getParent() != null)
 			intent.putExtra("path", node.getParent().getPath(true));
 		if (node.output != null)
 			intent.putExtra("output", node.output);
 		startActivity(intent);
 	}
 
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		HRWNode node;
 		String newtitle;
 
 		node = (HRWNode) adapter.getItem(position);
 		if (node.hasSubItems == false) {
 			showDetails(node);
 			return;
 		}
 
 		Intent viewIntent = new Intent(this, DienststatusActivity.class);
 
 		newtitle = node.getPath(true);
 		viewIntent.putExtra("level", node.id);
 		viewIntent.putExtra("name", newtitle);
 		startActivity(viewIntent);
 	}
 
 	@Override
 	public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 			android.view.ContextMenu.ContextMenuInfo menuInfo) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.contextmenu, menu);
 
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		HRWNode node = (HRWNode) adapter.getItem((int) info.id);
 
 		menu.setHeaderTitle(node.name);
 
 		if (node.url == null)
 			menu.findItem(R.id.openinbrowser).setEnabled(false);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		HRWNode node;
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		node = (HRWNode) adapter.getItem((int) info.id);
 		if (node == null)
 			return false;
 
 		switch (item.getItemId()) {
 		case R.id.openinbrowser:
 			if (node.url == null)
 				return false;
 			Intent intent = new Intent("android.intent.action.VIEW", Uri
 					.parse(node.url));
 			startActivity(intent);
 			return true;
 
 		case R.id.details:
 			showDetails(node);
 
 			return true;
 			
 		case R.id.sendemail:
 			try {
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("plain/text");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "rz-service@hs-weingarten.de" });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: <br>" + WEBSITE + " ,<br>" + node.getFullPath() + "<br><br>");
 				startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 			
 			
 		default:
 			return super.onContextItemSelected(item);
 		}
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
 			if (refreshTask != null) {
 				refreshTask.cancel(true);
 				try {
 					refreshTask.get();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			refreshTask = new RefreshTask();
 			refreshTask.execute();
 			
 			return true;
 		}
 		/*
 		 * if (item.getItemId() == R.id.preferences) { Intent intent = new
 		 * Intent(this, HRWPreferences.class); startActivity(intent); }
 		 */
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
 }
