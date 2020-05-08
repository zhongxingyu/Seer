 package com.buildbotwatcher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.buildbotwatcher.worker.Build;
 import com.buildbotwatcher.worker.Builder;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class BuilderActivity extends ListActivity {
 	private Builder			_builder;
 	private BuildsAdapter	_adapter;
 	private int				_displayed;
 	private boolean			_loadingMore;
 	private List<Build>		_newBuilds;
 	private Menu			_menu;
 
 	static final int		LOAD_STEP = 15;
 	static final Class<?>	PARENT_ACTIVITY = BuildersActivity.class;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.builder);
 		
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 		
 		_displayed = 0;
 		_loadingMore = false;
 		Bundle bundle = getIntent().getExtras();
 		// if no builder, go back to the parent activity
 		if (!bundle.containsKey("builder") || bundle.get("builder") == null) {
 			Intent intent = new Intent(this, PARENT_ACTIVITY);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		} else {
 			_builder = (Builder) bundle.get("builder");
 			setTitle(_builder.getName());
 			TextView state = (TextView) findViewById(R.id.state);
 			state.setText(String.format(getResources().getString(R.string.builder_state), _builder.getState()));
 			TextView header = (TextView) getLayoutInflater().inflate(R.layout.builder_list_header, null);
 			int count = _builder.getBuildCount();
 			header.setText(getResources().getQuantityString(R.plurals.builder_build_number, count, count));
 			ListView listView = getListView();
 			listView.addHeaderView(header);
 	
 			_adapter = new BuildsAdapter(this);
 			
 			@SuppressWarnings("unchecked")
 			final List<Build> data = (List<Build>) getLastNonConfigurationInstance();
 			if (data == null) {
 				if (_builder.getBuildCount() > _displayed) {
 					Thread thread =  new Thread(null, loadBuilds);
 					thread.start();
 				}
 			} else {
 				for (Build b: data) {
 					_adapter.addBuild(b);
 				}
 			}
 	
 			listView.setOnScrollListener(new OnScrollListener() {
 				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 					int lastInScreen = firstVisibleItem + visibleItemCount;
 					if((lastInScreen == totalItemCount) && !(_loadingMore) && _builder.getBuildCount() > _displayed) {
 						Thread thread =  new Thread(null, loadBuilds);
 						thread.start();
 					}
 				}
 	
 				public void onScrollStateChanged(AbsListView view, int scrollState) {}
 			});
 			
 			setListAdapter(_adapter);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		_menu = menu;
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.builder, menu);
 		return true;
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		final List<Build> data = _adapter.getBuilds();
 		return data;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent i = new Intent();
 		i.setClass(BuilderActivity.this, BuildActivity.class);
 		//TODO: use android.os.Parcelable instead of java.io.Serializable
 		i.putExtra("build", _adapter.getBuilds().get(position - 1));
 		i.putExtra("builder", _builder);
 		startActivity(i);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, PARENT_ACTIVITY);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 			
 		case R.id.menu_refresh:
 			refresh();
 			return true;
 			
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void refresh() {
 		_menu.findItem(R.id.menu_refresh).setEnabled(false);
 		_builder.clearCache();
 		_adapter.clearBuilds();
 		if (_builder.getBuildCount() > _displayed) {
 			Thread thread =  new Thread(null, loadBuilds);
 			thread.start();
 		}
 	}
 
 	private Runnable loadBuilds = new Runnable() {
 		public void run() {
 			if (_loadingMore)
 				return;
 			_loadingMore = true;
 			_newBuilds = new ArrayList<Build>();
 
 			int count = LOAD_STEP;
 			int start = _builder.getBuildCount() - _displayed;
 			int stop = _builder.getBuildCount() - _displayed - count;
 			if (stop < 0)
 				stop = 0;
 			if (count > _builder.getBuildCount() - _displayed)
 				count = _builder.getBuildCount() - _displayed;
 
 			for (int i = start - 1; i >= stop; i--)
 				_newBuilds.add(_builder.getBuild(i));
 
 			runOnUiThread(returnRes);
 		}
 	};
 
 	private Runnable returnRes = new Runnable() {
 		public void run() {
 			if (_newBuilds != null && _newBuilds.size() > 0){
 				for (int i = 0; i < _newBuilds.size(); i++)
 					_adapter.addBuild(_newBuilds.get(i));
 			}
 			_adapter.notifyDataSetChanged();
 			_loadingMore = false;
			if (_menu != null)
				_menu.findItem(R.id.menu_refresh).setEnabled(true);
 		}
 	};
 
 	private class BuildsAdapter extends ArrayAdapter<Build> {
 		private final Activity	_context;
 		private List<Build>		_builds;
 
 		public BuildsAdapter(Activity context) {
 			super(context, R.layout.builders_row);
 			_context = context;
 			_builds = new ArrayList<Build>();
 		}
 
 		public void addBuild(Build b) {
 			_displayed++;
 			_builds.add(b);
 			add(b);
 		}
 		
 		public List<Build> getBuilds() {
 			return _builds;
 		}
 		
 		public void clearBuilds() {
 			_displayed = 0;
 			_builds.clear();
 			clear();
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater inflater = _context.getLayoutInflater();
 				v = inflater.inflate(R.layout.builders_row, null, true);
 			}
 			TextView textView = (TextView) v.findViewById(R.id.label);
 			String s = String.valueOf(_builds.get(position).getNumber());
 			textView.setText(s);
 			if (_builds.get(position).isSuccessful())
 				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.success, 0, 0, 0);
 			else
 				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.failure, 0, 0, 0);
 
 			return v;
 		}
 	}
 }
