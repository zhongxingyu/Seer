 package dk.illution.computer.info;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ComputerListFragment extends ListFragment {
 
     private static final String STATE_ACTIVATED_POSITION = "activated_position";
 
     private Callbacks mCallbacks = sDummyCallbacks;
     private int mActivatedPosition = ListView.INVALID_POSITION;
 
     public interface Callbacks {
 
         public void onItemSelected(String id);
     }
 
     private static Callbacks sDummyCallbacks = new Callbacks() {
 
         public void onItemSelected(String id) {
             // TODO Auto-generated method stub
 
         }
     };
 
     public ComputerListFragment() {
     }
 
     public List<String> computers = new ArrayList<String>();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setRetainInstance(true);
 
         JSONObject computers_data = ComputerInfo.loadComputers();
         Log.d("ComputerInfo", "Loaded computers.");
 
         try {
             Log.d("count", computers_data.getString("count"));
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         try {
             JSONArray computers_array = computers_data
                     .getJSONArray("Computers");
             for (int i = 0; i <= computers_array.length() - 1; i++) {
                 JSONObject computer = computers_array.getJSONObject(i);
                 computers.add(computer.getString("identifier"));
                 ComputerList.addItem(new ComputerList.Computer(computer
                         .getString("id"), computer));
             }
         } catch (JSONException e) {
            Log.e("ComputerInfo", "Error loading one or more computers");
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                 android.R.layout.simple_list_item_1, computers);
 
         this.setListAdapter(adapter);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         if (savedInstanceState != null
                 && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
             setActivatedPosition(savedInstanceState
                     .getInt(STATE_ACTIVATED_POSITION));
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         if (!(activity instanceof Callbacks)) {
             throw new IllegalStateException(
                     "Activity must implement fragment's callbacks.");
         }
 
         mCallbacks = (Callbacks) activity;
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         mCallbacks = sDummyCallbacks;
     }
 
     @Override
     public void onListItemClick(ListView listView, View view, int position,
                                 long id) {
         super.onListItemClick(listView, view, position, id);
         mCallbacks.onItemSelected(ComputerList.ITEMS.get(position).id);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (mActivatedPosition != ListView.INVALID_POSITION) {
             outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
         }
     }
 
     public void setActivateOnItemClick(boolean activateOnItemClick) {
         getListView().setChoiceMode(
                 activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
                         : ListView.CHOICE_MODE_NONE);
     }
 
     public void setActivatedPosition(int position) {
         if (position == ListView.INVALID_POSITION) {
             getListView().setItemChecked(mActivatedPosition, false);
         } else {
             getListView().setItemChecked(position, true);
         }
 
         mActivatedPosition = position;
     }
 }
