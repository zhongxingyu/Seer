 package edu.uidaho.engr.seniordesign.umax;
 
 import android.app.*;
 import android.content.Intent;
 import android.os.*;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 
 public class MainMenu extends ListFragment {
 	
 	public class DefaultVariableClass {
 		public DefaultVariableClass() { }
 		
 		private boolean _connected = false;
 		private NetConnectionFragment _ncf;
 		public void SetNetConnectionFragment(NetConnectionFragment ncf) { _ncf = ncf; }
 		public boolean getConnected() { return _connected; }
 		public void setConnected(boolean connected) {
 			_connected = connected;
 			if (_ncf == null) {
				Log.w("Defaults", "NetConnectionFragment was null.");
 				return;
 			}
 			_ncf.enableButton(!_connected, _connected);
 		}
 	}
 	
 	public DefaultVariableClass defaults = new DefaultVariableClass();
 	
 	
 	
 	boolean mDualPane;
 	int mCurCheckPosition = 0;
 
 	private static final String[] MenuItems = new String[] {
 		"Network", "Device", "Controls", "Output"
 	};
 	
 	@Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         // Populate list with our static array of titles.
         setListAdapter(new ArrayAdapter<String>(getActivity(),
                 android.R.layout.simple_list_item_activated_1, MenuItems));
 
         // Check to see if we have a frame in which to embed the details
         // fragment directly in the containing UI.
         View detailsFrame = getActivity().findViewById(R.id.mainFragment);
         mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
 
         if (savedInstanceState != null) {
             // Restore last state for checked position.
             mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
         }
 
         if (mDualPane) {
             // In dual-pane mode, the list view highlights the selected item.
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             // Make sure our UI is in the correct state.
             showDetails(mCurCheckPosition);
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("curChoice", mCurCheckPosition);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         showDetails(position);
     }
 
     /**
      * Helper function to show the details of a selected item, either by
      * displaying a fragment in-place in the current UI, or starting a
      * whole new activity in which it is displayed.
      */
     void showDetails(int index) {
         mCurCheckPosition = index;
 
         if (mDualPane) {
             getListView().setItemChecked(index, true);
             defaults.SetNetConnectionFragment(null);
             
             if (index == 0) {
             	// NETWORK TAB
             	NetConnectionFragment ncf = (NetConnectionFragment)
                 getFragmentManager().findFragmentById(R.id.netFragment);
 		        if (ncf == null) {
 		            ncf = NetConnectionFragment.newInstance();
 		            defaults.SetNetConnectionFragment(ncf);
 		            defaults.setConnected(defaults.getConnected());
 		            FragmentTransaction ft = getFragmentManager().beginTransaction();
 		            ft.replace(R.id.extras, ncf);
 		            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		            ft.commit();
 		        } else {
 		            defaults.setConnected(defaults.getConnected());
 		        }
             } else {
             	Fragment f = new Fragment();
             	FragmentTransaction ft = getFragmentManager().beginTransaction();
             	ft.replace(R.id.extras, f);
             	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
             	ft.commit();
             }
 
         } else {
             // Otherwise we need to launch a new activity to display
             // the dialog fragment with selected text.
             /*Intent intent = new Intent();
             intent.setClass(getActivity(), DetailsActivity.class);
             intent.putExtra("index", index);
             startActivity(intent);*/
         	Log.w("Main Menu", "DUAL PANE FALSE :(");
         }
     }
 }
