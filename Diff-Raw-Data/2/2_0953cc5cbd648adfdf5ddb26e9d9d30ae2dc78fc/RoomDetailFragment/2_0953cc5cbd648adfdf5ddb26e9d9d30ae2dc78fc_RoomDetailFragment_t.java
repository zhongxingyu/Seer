 package net.danopia.mobile.laundryview;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.GridView;
 import net.danopia.mobile.laundryview.data.Client;
 import net.danopia.mobile.laundryview.data.MachineArrayAdapter;
 import net.danopia.mobile.laundryview.structs.Room;
 
 /**
  * A fragment representing a single Room detail screen.
  * This fragment is either contained in a {@link RoomListActivity}
  * in two-pane mode (on tablets) or a {@link RoomDetailActivity}
  * on handsets.
  */
public class RoomDetailFragment extends Fragment {
     /**
      * The fragment argument representing the item ID that this fragment
      * represents.
      */
     public static final String ARG_ITEM_ID = "room_id";
 
     /**
      * The room this fragment is presenting.
      */
     private Room mRoom = null;
 
 
     private UserLoginTask mAuthTask = null;
     private UserLoginTask2 mAuthTask2 = null;
 
     /**
      * Mandatory empty constructor for the fragment manager to instantiate the
      * fragment (e.g. upon screen orientation changes).
      */
     public RoomDetailFragment() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (getArguments().containsKey(ARG_ITEM_ID)) {
             // Load the dummy content specified by the fragment
             // arguments. In a real-world scenario, use a Loader
             // to load content from a content provider.
             //mItem = DummyContent.DUMMY.itemMap.get();
 
             if (Cache.provider != null) {
                 mRoom = Cache.provider.getRoom(Integer.parseInt(getArguments().getString(ARG_ITEM_ID)));
 
                 mAuthTask = new UserLoginTask();
                 mAuthTask.execute(mRoom);
             }
         }
     }
     private View rootView=null;
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_room_detail, container, false);
 
         // Show the dummy content as text in a TextView.
         if (mRoom != null) {
             if (getActivity().getTitle() != Cache.provider.name) {
                 getActivity().setTitle(mRoom.name);
                 getActivity().getActionBar().setSubtitle(Util.titleCase(Cache.provider.getRoomLocation(mRoom.id).name));
             }
         }
 
         return rootView;
     }
 
 
     /**
      * Represents an asynchronous login/registration task used to authenticate
      * the user.
      */
     private class UserLoginTask extends AsyncTask<Room, Void, Room> {
         @Override
         protected Room doInBackground(Room... params) {
             Client.getRoom(params[0]);
             return params[0];
         }
 
         @Override
         protected void onPostExecute(final Room data) {
             mAuthTask = null;
             //showProgress(false);
             //mItem = data;
             //if (rootView!=null && mItem !=null)
             //((TextView) rootView.findViewById(R.id.room_detail)).setText(mItem.content);
 
             ///((GridView) rootView.findViewById(R.id.machine_grid)).setAdapter(new MachineArrayAdapter(
             ///        getActivity(),data));
 
             // TODO: make sure we still exist
             ((GridView) rootView.findViewById(R.id.machine_grid)).setAdapter(new MachineArrayAdapter(getActivity(), data.machines));
 
             mAuthTask2 = new UserLoginTask2();
             mAuthTask2.execute(mRoom);
 
             /*if (success) {
                 finish();
             } else {
                 mPasswordView.setError(getString(R.string.error_incorrect_password));
                 mPasswordView.requestFocus();
             }*/
         }
 
         @Override
         protected void onCancelled() {
             mAuthTask = null;
             //showProgress(false);
         }
     }
 
     private class UserLoginTask2 extends AsyncTask<Room, Void, Room> {
         @Override
         protected Room doInBackground(Room... params) {
             Client.updateRoom(params[0]);
             return params[0];
         }
 
         @Override
         protected void onPostExecute(final Room data) {
             mAuthTask2 = null;
             //showProgress(false);
             //mItem = data;
             //if (rootView!=null && mItem !=null)
             //((TextView) rootView.findViewById(R.id.room_detail)).setText(mItem.content);
 
             ///((GridView) rootView.findViewById(R.id.machine_grid)).setAdapter(new MachineArrayAdapter(
             ///        getActivity(),data));
 
             // TODO: make sure we still exist
             ((GridView) rootView.findViewById(R.id.machine_grid)).invalidateViews();//.setAdapter(new MachineArrayAdapter(getActivity(), data.machines));
 
             /*if (success) {
                 finish();
             } else {
                 mPasswordView.setError(getString(R.string.error_incorrect_password));
                 mPasswordView.requestFocus();
             }*/
         }
 
         @Override
         protected void onCancelled() {
             mAuthTask2 = null;
             //showProgress(false);
         }
     }
 }
