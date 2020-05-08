 package de.saschahlusiak.hrw.dienststatus.dienste;
 
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.model.Dienststatus;
 import de.saschahlusiak.hrw.dienststatus.model.HRWNode;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 
 public class DienststatusFragment extends ListFragment implements OnItemClickListener {
 	static final String tag = DienststatusFragment.class.getSimpleName();
 	DienststatusAdapter adapter;
 	static RefreshTask refreshTask;
 	String level;
 	public static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/dienststatus";
 	OnNodeClicked mListener;
 	HRWNode node = null;
 	Menu optionsMenu;
 	View mRefreshIndeterminateProgressView;
 	
 	public interface OnNodeClicked {
 		public void onNodeDetails(DienststatusFragment fragment, HRWNode node);
 		public void onNodeClicked(DienststatusFragment fragment, HRWNode node);
 	}
 
 	public DienststatusFragment() {
 		
 	}
 
 	public DienststatusFragment(String level) {
 		this.level = level;
 	}
 	
 	public DienststatusFragment(HRWNode node) {
 		this(node == null ? "all" : node.id);
 		this.node = node;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		adapter =  new DienststatusAdapter(getActivity(), level == null);
 		setListAdapter(adapter);
 		setHasOptionsMenu(true);
 		
 		if (savedInstanceState != null) {
 			this.level = savedInstanceState.getString("level");
 			this.node = (HRWNode) savedInstanceState.getSerializable("node");
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {		
 		super.onSaveInstanceState(outState);
 		outState.putString("level", level);
 		outState.putSerializable("node", node);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		this.mListener = (OnNodeClicked) getActivity();
 
 		getListView().setOnItemClickListener(this);
 		registerForContextMenu(getListView());
 		
 		if (node != null) {
 			if (node.getParent() == null) {
 				getActivity().getActionBar().setTitle(node.name);
 				getActivity().getActionBar().setSubtitle(null);
 			} else {
 				getActivity().getActionBar().setTitle(node.getParent().name);
 				getActivity().getActionBar().setSubtitle(node.name);
 			}
 		} else {
			getActivity().getActionBar().setTitle(level == null ? R.string.tab_warnings : R.string.main_name);
 			getActivity().getActionBar().setSubtitle(null);
 		}
 		
 		getActivity().getActionBar().setDisplayHomeAsUpEnabled(node != null);
 		getActivity().getActionBar().setHomeButtonEnabled(node != null);
 	}
 	
 	public void refresh() {
 /*		if (refreshTask != null) {
 			refreshTask.cancel(true);
 			try {
 				refreshTask.get();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} */
 		refreshTask = new RefreshTask(refreshTask);
 		refreshTask.execute();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		adapter.fillLevel(level);
 		if (refreshTask != null || Dienststatus.needsFetch())
 			refresh();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 	}
 	
 	@Override
 	public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 			android.view.ContextMenu.ContextMenuInfo menuInfo) {
 		MenuInflater inflater = getActivity().getMenuInflater();
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
 			mListener.onNodeDetails(this, node);
 			return true;
 			
 		case R.id.sendemail:
 			try {
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("plain/text");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
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
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.optionsmenu_dienststatus, menu);
 		optionsMenu = menu;
 		if (refreshTask != null && refreshTask.getStatus() == Status.RUNNING)
 			setProgressActionView(true);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			refresh();
 			break;
 		case R.id.gotowebsite:
 			intent = new Intent(
 					"android.intent.action.VIEW",
 					Uri.parse(WEBSITE));
 			startActivity(intent);
 			break;
 		case R.id.sendemail:
 			try {
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/plain");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: <br>" + WEBSITE + " ,<br>" + node.getFullPath() + "<br><br>");
 				startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			break;
 			
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 		HRWNode node;
 
 		node = (HRWNode) adapter.getItem(position);
 		if (node.hasSubItems == false)
 			mListener.onNodeDetails(this, node);
 		else
 			mListener.onNodeClicked(this, node);
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
 	
 	
 	private class RefreshTask extends AsyncTask<Void, Integer, String> {
 		RefreshTask old;
 		
 		RefreshTask(RefreshTask old) {
 			this.old = old;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			setProgressActionView(true);
 			super.onPreExecute();
 		}
 
 		@Override
 		protected String doInBackground(Void... arg0) {
 			try {
 				if (old != null) {
 					return old.get();
 				}
 				Log.d("RefreshTask", "fetching begin");
 				String s = Dienststatus.fetch(getActivity());
 				Log.d("RefreshTask", "fetching finished: " + s);
 				
 				return s;
 			} catch (Exception e) {
 				Log.d("RefreshTask", "fetching finished: " + e.getMessage());
 
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		@Override
 		protected void onCancelled() {
 			refreshTask = null;
 			if (getActivity() != null) {
 				setProgressActionView(false);
 				Toast.makeText(getActivity(), getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
 			}
 
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			refreshTask = null;
 			setProgressActionView(false);
 			Log.d(tag, "onPostExecute(" + result + ")");
 			if (result == null)
 				adapter.fillLevel(level);
 			else if (getActivity() != null) {
 				Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
 			}
 			super.onPostExecute(result);
 		}
 	}	
 }
