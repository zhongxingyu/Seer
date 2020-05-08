 package com.ipbox.fragments;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.*;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import com.ipbox.Const;
 import com.ipbox.IpBoxApp;
 import com.ipbox.R;
 import com.ipbox.activites.BaseActivity;
 import com.ipbox.activites.DetailsActivity;
 import com.ipbox.playlist.Playlist;
 
 /**
  * User: gsd
  * Date: 5/23/12
  * Time: 8:37 PM
  */
 public class ChannelsFragment extends Fragment {
 	protected static final String CUR_CHO = "curChoice";
 	protected static final String CUR_VIZ = "curViz";
 
 	public static ChannelsFragment newInstance(int index) {
 		ChannelsFragment f = new ChannelsFragment();
 
 		// Supply index input as an argument.
 		Bundle args = new Bundle();
 		args.putInt(Const.ARGUMENT_INDEX, index);
 		f.setArguments(args);
 
 		return f;
 	}
 
 	public int getShownIndex() {
 		if (getArguments() == null)
 			return 0;
 		return getArguments().getInt(Const.ARGUMENT_INDEX, 0);
 	}
 
 	protected boolean mDualPane;
 	protected int _curCheckPosition = -1;
 	protected Playlist _list;
 	protected ChannelsAdapter _adapter;
 	protected GridView _gridView;
 	protected int _firstVisible;
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		setupGrid(savedState);
 
 		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		//	setHasOptionsMenu(true);
 	}
 
 	protected void setupGrid(Bundle savedState) {
 		_gridView = (GridView) getView().findViewById(R.id.channels_view);
 		//_gridView.setAdapter(_adapter);
 
 		_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				((BaseActivity) getActivity()).loadChannel(_list.getChannel(position));
 
 			}
 		});
 
 		_gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
 				//showDetails(position);
 				return true;
 			}
 		});
 
 		// Check to see if we have a frame in which to embed the details
 		// fragment directly in the containing UI.
 		View detailsFrame = getActivity().findViewById(R.id.details);
 		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
 
 		if (savedState != null) {
 			// Restore last state for checked position.
 			_curCheckPosition = savedState.getInt(CUR_CHO, 0);
 		}
 
 		if (mDualPane) {
 			// In dual-pane mode, list view highlights selected item.
 			//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			// Make sure our UI is in the correct state.
 			showDetails(_curCheckPosition);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.channels, container, false);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		_list = IpBoxApp.getPlayListsHolder().getPlayList(getShownIndex());
 		if (_list == null)
 			return;
 		if (savedInstance != null)
 			_firstVisible = savedInstance.getInt(CUR_VIZ, 0);
 		else
 			_firstVisible = 0;
 		new LoadTask().execute(_list);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(CUR_CHO, _curCheckPosition);
 		outState.putInt(CUR_VIZ, _gridView.getFirstVisiblePosition());
 	}
 
 	/**
 	 * Helper function to show the details of a selected item, either by
 	 * displaying a fragment in-place in the current UI, or starting a
 	 * whole new activity in which it is displayed.
 	 */
 	void showDetails(int index) {
 		_curCheckPosition = index;
 
 		if (mDualPane) {
 			// We can display everything in-place with fragments.
 			// Have the list highlight this item and show the data.
 			//_gridView.setItemChecked(index, true); //TODO
 
 			// Check what fragment is shown, replace if needed.
 			DetailsFragment details = (DetailsFragment) getFragmentManager().findFragmentById(R.id.details);
 			if (details == null || details.getShownIndex() != index) {
 				// Make new fragment to show this selection.
 				details = DetailsFragment.newInstance(index);
 
 				// Execute a transaction, replacing any existing
 				// fragment with this one inside the frame.
 				FragmentTransaction ft = getFragmentManager().beginTransaction();
 				ft.replace(R.id.details, details);
 				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 				ft.commit();
 			}
 
 		} else {
 			// Otherwise we need to launch a new activity to display
 			// the dialog fragment with selected text.
 			Intent intent = new Intent();
 			intent.setClass(getActivity(), DetailsActivity.class);
 			intent.putExtra("index", index);
 			startActivity(intent);
 		}
 	}
 
 	public void setQuery(String newText) {
 		if (_adapter == null)
 			return;
 		if (newText == null || newText.equals(""))
 			_adapter.getFilter().filter("");
 		else
 			_adapter.getFilter().filter(newText.toLowerCase());
 	}
 
 	class LoadTask extends AsyncTask<Playlist, Void, Playlist> {
 
 		@Override
 		protected Playlist doInBackground(Playlist... params) {
 			Playlist playlist = params[0];
 
 			if (playlist.load())
 				return playlist;
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Playlist result) {
 			if (result == null)
 				return;
 			_adapter = new ChannelsAdapter(getActivity(), result);
 			_gridView.setAdapter(_adapter);
 			//_gridView.smoothScrollToPosition(_firstVisible);
 			_gridView.setSelection(_firstVisible);
 		}
 	}
 }
