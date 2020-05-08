 package net.acuttone.reddimg.prefs;
 
 import java.util.Collections;
 import java.util.List;
 
 import net.acuttone.reddimg.R;
 import net.acuttone.reddimg.core.ReddimgApp;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class UserSubredditsActivity extends Activity {
 	private ListView listView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.usersubreddits);
 
 		listView = (ListView) findViewById(R.id.usersubreddits_listView);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				SubredditArrayAdapter adapter = (SubredditArrayAdapter) listView.getAdapter();
 				String item = adapter.getItem((int) id);
 				List<String> subreddits = SubredditsPickerActivity.getSubredditsFromPref();
 				if(subreddits.contains(item) == false) {
 					subreddits.add(item);
 					SubredditsPickerActivity.saveSubreddits(subreddits);
 					((SubredditArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();
 				}
 			}
 		});
 		
 		Button btnRefresh = (Button) findViewById(R.id.usersubreddits_btnrefresh);
 		btnRefresh.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				findViewById(R.id.usersubreddits_login_text).setVisibility(View.GONE);
 				refreshSubreddits();
 			}
 		});
 		
 		refreshSubreddits();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(listView != null && listView.getAdapter() != null) {
 			((SubredditArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();
 		}
 	}
 
 	private void refreshSubreddits() {
 		AsyncTask<Void, Void, List<String>> loadTask = new AsyncTask<Void, Void, List<String>>() {
 
 			private ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute() {
 				progressDialog = ProgressDialog.show(UserSubredditsActivity.this, "Reddimg", "Loading subreddits...");
 			}
 			
 			@Override
 			protected List<String> doInBackground(Void... params) {
 				List<String> names = ReddimgApp.instance().getRedditClient().getMySubreddits();
 				Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
 				return names;
 			}
 
 			@Override
 			protected void onPostExecute(List<String> result) {
 				SubredditArrayAdapter adapter = new SubredditArrayAdapter(UserSubredditsActivity.this, result, R.drawable.plus, true);
 				listView.setAdapter(adapter);
 				progressDialog.dismiss();
 				if(result.size() == 0) {
 					findViewById(R.id.usersubreddits_login_text).setVisibility(View.VISIBLE);
 				}
 				super.onPostExecute(result);
 			}
 			
 		};
		loadTask.execute((Void [])null);
 	}
 
 }
