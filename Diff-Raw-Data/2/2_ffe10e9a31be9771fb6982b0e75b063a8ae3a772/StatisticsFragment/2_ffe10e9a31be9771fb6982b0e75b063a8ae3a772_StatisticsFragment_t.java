 package de.saschahlusiak.hrw.dienststatus.statistic;
 
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.AsyncTask.Status;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ShareActionProvider;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.model.Statistic;
 
 public class StatisticsFragment extends ListFragment implements OnItemClickListener {
 	private StatisticsAdapter adapter;
 	int category;
 	Menu optionsMenu;
 	View mRefreshIndeterminateProgressView;
 
 	
 	static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/zahlen-und-fakten";
 	
 	public interface OnStatisticClicked {
 		public void onStatisticClicked(StatisticsFragment fragment, int category);
 	}
 	
 	OnStatisticClicked mListener;
 
 	
 	private class RefreshTask extends AsyncTask<String, Statistic, String> {
 		boolean force;
 		
 		public RefreshTask(boolean force) {
 			this.force = force;
 		}
 		
 		@Override
 		protected void onPreExecute() {			
 			setProgressActionView(true);
 			super.onPreExecute();			
 		}
 
 		@Override
 		protected String doInBackground(String... urls) {
 			publishProgress();
 
 			for (int i=0; i < urls.length; i++) {
 				Statistic s = (Statistic)adapter.getItem(i);
 				if (s.getValid())
 					continue;
 				try {
 					s.setBitmap(s.fetch(getActivity(), force));
 					publishProgress(s);
 					if (isCancelled())
 						break;
 				} catch (Exception e) {
 					e.printStackTrace();
 					publishProgress(s);
 //					return getString(R.string.connection_error);
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Statistic... values) {
 			if (values != null && values.length > 0) {
 				adapter.update(values[0].getIndex(), values[0].getBitmap());
 			}
 
 			adapter.notifyDataSetChanged();
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			setProgressActionView(false);
 			adapter.notifyDataSetChanged();
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (result != null && getActivity() != null)
 				Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
 			setProgressActionView(false);
 			adapter.notifyDataSetChanged();
 			super.onPostExecute(result);
 		}
 	}
 
 	RefreshTask task = null;
 
 	
 	private void refresh(boolean force) {
 		if (task != null) {
 			task.cancel(false);
 			try {
 				task.get();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		task = new RefreshTask(force);
 		task.execute(StatisticsActivity.STATISTICS[category]);
 	}
 	
 	public StatisticsFragment() {
 		this.category = 0;
 	}
 	
 	public StatisticsFragment(int categoty) {
 		this.category = categoty;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if (savedInstanceState != null)
 			category = savedInstanceState.getInt("category");
 
 		setHasOptionsMenu(true);
 		adapter = new StatisticsAdapter(getActivity(), StatisticsActivity.STATISTICS[category]);
 		setListAdapter(adapter);
 		adapter.invalidate();
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putInt("category", category);
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		this.mListener = (OnStatisticClicked) getActivity();
 		getListView().setOnItemClickListener(this);
 		registerForContextMenu(getListView());
 		
 		if (category != 0) {
 			getActivity().getActionBar().setTitle(R.string.tab_statistics);
 			getActivity().getActionBar().setSubtitle(StatisticsActivity.STATISTIC_TITLES[category]);
 		} else {
 			getActivity().getActionBar().setTitle(R.string.tab_statistics);
 			getActivity().getActionBar().setSubtitle(null);
 		}
 		
 		getActivity().getActionBar().setDisplayHomeAsUpEnabled(category > 0);
 		getActivity().getActionBar().setHomeButtonEnabled(category > 0);	
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		/* first fragment, refresh to fetch images */
 		if (task == null)
 			refresh(false);
 
		if (optionsMenu != null) {
 			ShareActionProvider share = (ShareActionProvider) optionsMenu.findItem(R.id.menu_item_share).getActionProvider();
 
 			Statistic s = (Statistic) adapter.getItem(0);
 			
 			Intent intent = new Intent(Intent.ACTION_SEND);
 			if (category != 0) {
 				Uri uri = Uri.fromFile(s.getCacheFile(getActivity()));
 				intent.setType("image/png");
 				intent.putExtra(Intent.EXTRA_STREAM, uri);
 			} else {
 				intent.setType("text/plain");
 				intent.putExtra(Intent.EXTRA_TEXT, WEBSITE);
 			}
 			share.setShareIntent(intent);
 		}
 		
 	}
 
 	@Override
 	public void onStop() {
 		if (task != null) {
 			task.cancel(false);
 			task = null;
 		}
 		super.onStop();
 	}
 	
 	@Override
 	public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 			android.view.ContextMenu.ContextMenuInfo menuInfo) {
 		MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.contextmenu_statistic, menu);
 		
 		String title = StatisticsActivity.STATISTIC_TITLES[category];
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		
 		if (category == 0)
 			title = StatisticsActivity.STATISTIC_TITLES[(int)info.id + 1];
 		menu.setHeaderTitle(title);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		Statistic s;
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		Intent intent;
 		s = (Statistic) adapter.getItem((int) info.id);
 		if (s == null)
 			return false;
 
 		switch (item.getItemId()) {
 		case R.id.menu_item_share:
 			if (s.getBitmap() == null)
 				return false;
 			try {
 				Uri uri = Uri.fromFile(s.getCacheFile(getActivity()));
 				
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("image/png");
 				intent.putExtra(Intent.EXTRA_STREAM, uri);
 				startActivity(Intent.createChooser(intent, getString(R.string.share)));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		case R.id.sendemail:
 			try {
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/plain");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, s.getWebURL());
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
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.optionsmenu_statistics, menu);
 		optionsMenu = menu;
 		if (task != null && task.getStatus() == Status.RUNNING)
 			setProgressActionView(true);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.refresh) {
 			adapter.invalidate();			
 			refresh(true);
 			return true;
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
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
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
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 		if (category != 0)
 			return;
 		
 		mListener.onStatisticClicked(this, (int)id + 1);
 	}
 	
 	void setProgressActionView(boolean refreshing) {
 		if (optionsMenu == null)
 			return;
         final MenuItem refreshItem = optionsMenu.findItem(R.id.refresh);
         if (refreshItem != null) {
             if (refreshing) {
                 if (mRefreshIndeterminateProgressView == null) {
                     LayoutInflater inflater = (LayoutInflater)
                             getActivity().getSystemService(
                                     Context.LAYOUT_INFLATER_SERVICE);
                     mRefreshIndeterminateProgressView = inflater.inflate(
                             R.layout.actionbar_indeterminate_progress, null);
                 }
 
                 refreshItem.setActionView(mRefreshIndeterminateProgressView);
             } else {
                 refreshItem.setActionView(null);
             }
         }
 	}	
 
 }
