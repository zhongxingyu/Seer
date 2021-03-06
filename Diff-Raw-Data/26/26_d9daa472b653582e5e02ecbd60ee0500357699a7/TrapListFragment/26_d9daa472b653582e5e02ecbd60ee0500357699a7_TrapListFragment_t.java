 package io.coldstart.android;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import io.coldstart.android.dummy.DummyContent;
 
 /**
  * A list fragment representing a list of Traps. This fragment also supports
  * tablet devices by allowing list items to be given an 'activated' state upon
  * selection. This helps indicate which item is currently being viewed in a
  * {@link TrapDetailFragment}.
  * <p>
  * Activities containing this fragment MUST implement the {@link Callbacks}
  * interface.
  */
 public class TrapListFragment extends ListFragment 
 {
 	TrapListAdapter adapter = null;
 	TrapsDataSource datasource = null;
 	public List<Trap> listOfTraps = null;
 
     private BroadcastReceiver receiver = new BroadcastReceiver()
     {
         @Override
         public void onReceive(Context context, Intent intent)
         {
             Log.e("onRecieve","Got a broadcast");
             //Toast.makeText(getApplicationContext(), "received", Toast.LENGTH_SHORT).show();
             getData();
         }
     };
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * activated item position. Only used on tablets.
 	 */
 	private static final String STATE_ACTIVATED_POSITION = "activated_position";
 
 	/**
 	 * The fragment's current callback object, which is notified of list item
 	 * clicks.
 	 */
 	private Callbacks mCallbacks = sDummyCallbacks;
 
 	/**
 	 * The current activated item position. Only used on tablets.
 	 */
 	private int mActivatedPosition = ListView.INVALID_POSITION;
 
    private String mSelectedHost = null;
 	/**
 	 * A callback interface that all activities containing this fragment must
 	 * implement. This mechanism allows activities to be notified of item
 	 * selections.
 	 */
 	public interface Callbacks
 	{
 		/**
 		 * Callback for when an item has been selected.
 		 */
 		//public void onItemSelected(String id);
 		public void onItemSelected(Trap trap);
 	}
 
 	/**
 	 * A dummy implementation of the {@link Callbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static Callbacks sDummyCallbacks = new Callbacks()
 	{
 		/*@Override
 		public void onItemSelected(String id) 
 		{
 		}*/
 		@Override
 		public void onItemSelected(Trap trap) 
 		{
 		}
 	};
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public TrapListFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
         getData();
 
 	}
 
     private void getData()
     {
         Log.i("getData","getting data");
         datasource = new TrapsDataSource(getActivity());
         datasource.open();
 
         listOfTraps = datasource.getRecentTraps();
 
 		/*int trapSize = listOfTraps.size();
 
 		for(int i = 0; i < trapSize; i++)
 		{
 			Trap test = listOfTraps.get(i);
 
 			Log.e("trap","Hostname: " + test.Hostname + " / IP: " +  test.IP
 					+ " Date: " +  test.date
 					+ " ID: " +  test.trapID
 					+ " Uptime: " +  test.uptime
 					+ " Read: " +  test.read
 					+ " Payload: " +  test.trap);
 		}*/
 
         //Not sure why this is commented out
         datasource.close();
 
         adapter = new TrapListAdapter(getActivity(),listOfTraps);
 
         setListAdapter(adapter);
 
        /*if(mActivatedPosition != ListView.INVALID_POSITION)
         {
             Log.e("mActivatedPosition","Not invalid: " + Integer.toString(mActivatedPosition));
             getListView().setItemChecked(mActivatedPosition, true);
        }*/

        if(null != mSelectedHost)
        {
            int hostCount = listOfTraps.size();
            for(int i = 0; i < hostCount; i++)
            {
                if(listOfTraps.get(i).Hostname.equals(mSelectedHost))
                {
                    getListView().setItemChecked(i, true);
                }
            }
         }
     }
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) 
 	{
 		super.onViewCreated(view, savedInstanceState);
 
 		// Restore the previously serialized activated item position.
 		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) 
 		{
 			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) 
 	{
 		super.onAttach(activity);
 
 		// Activities containing this fragment must implement its callbacks.
 		if (!(activity instanceof Callbacks)) 
 		{
 			throw new IllegalStateException("Activity must implement fragment's callbacks.");
 		}
 
 		mCallbacks = (Callbacks) activity;
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 
         //getActivity().unregisterReceiver(receiver);
 	}
 
     @Override
     public void onResume()
     {
         super.onResume();
         Log.e("onResume","Registering broadcast");
         IntentFilter filter = new IntentFilter();
         filter.addAction(API.BROADCAST_ACTION);
         getActivity().registerReceiver(receiver, filter);
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
         Log.e("onResume","UNregistering broadcast");
         getActivity().unregisterReceiver(receiver);
     }
 	@Override
 	public void onListItemClick(ListView listView, View view, int position, long id) 
 	{
 		super.onListItemClick(listView, view, position, id);
 
 		// Notify the active callbacks interface (the activity, if the
 		// fragment is attached to one) that an item has been selected.
 		//mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
 		mCallbacks.onItemSelected(listOfTraps.get(position));

        //We want to keep track of this for our own purposes
        //mActivatedPosition = position;
        mSelectedHost = listOfTraps.get(position).Hostname;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) 
 	{
 		View view = inflater.inflate(R.layout.list_fragment, null);
 		return view;
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) 
 	{
 		super.onSaveInstanceState(outState);
 		
 		if (mActivatedPosition != ListView.INVALID_POSITION) 
 		{
 			// Serialize and persist the activated item position.
 			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
 		}
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
 	 */
 	public void setActivateOnItemClick(boolean activateOnItemClick) 
 	{
 		// When setting CHOICE_MODE_SINGLE, ListView will automatically
 		// give items the 'activated' state when touched.
 		getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
 	}
 
 	private void setActivatedPosition(int position) 
 	{
 		if (position == ListView.INVALID_POSITION) 
 		{
 			getListView().setItemChecked(mActivatedPosition, false);
 		} 
 		else 
 		{
 			getListView().setItemChecked(position, true);
 		}
 
 		mActivatedPosition = position;
 	}
 }
