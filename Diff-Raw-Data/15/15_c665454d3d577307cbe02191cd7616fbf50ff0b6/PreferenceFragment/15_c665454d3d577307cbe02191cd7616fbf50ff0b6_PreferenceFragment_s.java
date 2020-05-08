 package me.gods.raintimer;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class PreferenceFragment extends Fragment {
     public static final String FAVORITE_EVENT_LIST = "FavoriteEventList";
     public static final String EVENT_LIST = "EventList";
 
     private ListView eventListView;
     private ArrayAdapter<String> adapter;
     private Button addEventButton;
 
     private SharedPreferences settings;
     private ArrayList<String> eventList;
     JSONArray eventArray = null;
     private ArrayList<String> favoriteList;
     JSONArray favoriteArray = null;
 
     private SQLiteDatabase db;
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_preference, container, false);
 
         db = getActivity().openOrCreateDatabase("raintimer.db", Context.MODE_PRIVATE, null);
 
         settings = this.getActivity().getPreferences(Activity.MODE_PRIVATE);
         eventList = new ArrayList<String>();
        String events = settings.getString("EventList", "[]");
         try {
             eventArray = new JSONArray(events);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         int eventLength = eventArray.length();
         for (int i = 0; i < eventLength; i++) {
             try {
                 eventList.add(eventArray.getString(i));
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
 
         favoriteList = new ArrayList<String>();
        String favorites = settings.getString("EventList", "[]");
         try {
             favoriteArray = new JSONArray(favorites);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         int favoriteLength = favoriteArray.length();
         for (int i = 0; i < favoriteLength; i++) {
             try {
                 favoriteList.add(favoriteArray.getString(i));
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
 
         eventListView = (ListView)v.findViewById(R.id.event_list);
         adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_expandable_list_item_1,eventList);
         eventListView.setAdapter(adapter);
 
         eventListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
             @Override
             public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                 menu.setHeaderIcon(android.R.drawable.ic_delete);
                 menu.setHeaderTitle("This operation will delete all data about this event. Continue?");
                 menu.add(0, 0, 0, "Delete");
                 menu.add(0, 1, 0, "Cancel");  
             }
         });
 
         addEventButton = (Button)v.findViewById(R.id.add_event_button);
         addEventButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                     .setTitle("Please Input Event Name")
                     .setIcon(android.R.drawable.ic_dialog_info);
 
                 final EditText input = new EditText(getActivity());
 
                 dialog.setView(input)
                     .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             String added = input.getText().toString();
 
                             if (eventList.contains(added)) {
                                 return;
                             }
 
                             eventList.add(added);
                             updateStorage();
                         }
                     })
                     .setNegativeButton("Cancel", null)
                     .show();
 
                 
             }
         });
 
         return v;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         db.close();
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         int position = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
 
         if (item.getItemId() == 0) {
             db.delete("history", "event_name = ?", new String[]{eventList.get(position)});
             favoriteList.remove(eventList.get(position));
             eventList.remove(position);
             updateStorage();
         }
 
         return super.onContextItemSelected(item);
     }
 
     private void updateStorage() {
         eventArray = new JSONArray(eventList);
         favoriteArray = new JSONArray(favoriteList);
 
         SharedPreferences.Editor editor = settings.edit();
         editor.putString(EVENT_LIST, eventArray.toString());
        editor.putString(FAVORITE_EVENT_LIST, favoriteList.toString());
         editor.commit();
 
         adapter.notifyDataSetChanged();
     }
 }
