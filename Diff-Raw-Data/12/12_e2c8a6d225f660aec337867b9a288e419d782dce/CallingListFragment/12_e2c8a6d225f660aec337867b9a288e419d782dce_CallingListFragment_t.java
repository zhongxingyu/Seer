 package org.lds.community.CallingWorkFlow.activity;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
 import org.lds.community.CallingWorkFlow.Adapter.CallingViewItemAdapter;
 import org.lds.community.CallingWorkFlow.R;
 import org.lds.community.CallingWorkFlow.api.CwfNetworkUtil;
 import org.lds.community.CallingWorkFlow.domain.CallingViewItem;
 import org.lds.community.CallingWorkFlow.domain.WorkFlowDB;
 import roboguice.fragment.RoboListFragment;
 
 import javax.inject.Inject;
 import java.io.Serializable;
 import java.util.List;
 
 public class CallingListFragment extends RoboListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
     @Inject
     WorkFlowDB db;
 
     @Inject
     CwfNetworkUtil networkUtil;
 
 	private CallingViewItemAdapter callingViewItemAdapter;
 	private List<CallingViewItem> callingViewItems;
 
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
     }
 
     private int currentPositionInList = 0;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		callingViewItems = db.getCallings(false);
 		this.callingViewItemAdapter = new CallingViewItemAdapter(getActivity(), android.R.layout.simple_list_item_1, callingViewItems);
 		setListAdapter(callingViewItemAdapter);
 		ListView listView = getListView();
 
 		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
 				Toast.makeText(getActivity(), "long click", Toast.LENGTH_SHORT).show();
 				//displayStatusPopup();
 				return true;
 			}
 		});
 
 	}
 
 	private void displayStatusPopup() {
		/*
 		Spinner statusSpinner = (Spinner) getListView().findViewById(11);
         List<WorkFlowStatus> statusList = db.getWorkFlowStatuses();
         List<CharSequence> statusOptions = new ArrayList<CharSequence>();
         for(WorkFlowStatus s: statusList) { statusOptions.add(s.getStatusName()); }
         ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(),android.R.layout.simple_spinner_item,statusOptions);
         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         statusSpinner.setAdapter(spinnerAdapter);
        */
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		CallingViewItem callingViewItem = callingViewItems.get(position);
 		Intent intent = new Intent(getActivity(), DetailActivity.class);
 		intent.putExtra("callingViewItems", (Serializable)callingViewItem);
 
 		startActivity(intent);
 	}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
 	    return getLayoutInflater( savedInstanceState ).inflate( R.layout.callingworkflow_list, container );
     }
 	/*
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 		//outState.putInt("LIST_POS", getListView().getFirstVisiblePosition());
     }
 
     public void selectPosition(int position, long id) {
         currentPositionInList = position;
     }
     */
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
 		return null;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> cursorLoader) {
 	}
 }
