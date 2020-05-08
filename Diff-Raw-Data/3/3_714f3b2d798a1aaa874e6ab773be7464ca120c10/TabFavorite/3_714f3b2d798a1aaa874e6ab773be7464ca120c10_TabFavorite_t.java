 package com.underdusken.kulturekalendar.ui.fragments;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.underdusken.kulturekalendar.R;
 import com.underdusken.kulturekalendar.data.EventItem;
 import com.underdusken.kulturekalendar.data.db.DatabaseManager;
 import com.underdusken.kulturekalendar.ui.activities.EventsDescription;
 import com.underdusken.kulturekalendar.ui.adapters.AdapterEventsItem;
 import com.underdusken.kulturekalendar.ui.receivers.NotificationUpdateReceiver;
 import com.underdusken.kulturekalendar.utils.ToDo;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TabFavorite extends Fragment {
     private static final String TAG = "TabFavorite";
     private NotificationUpdateReceiver notificationUpdateReceiver = null;
 
     private AdapterEventsItem adapterEventsItem = null;
     private List<EventItem> eventItemList = new ArrayList<EventItem>();
 
     private ListView lvEvents = null;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.tab_favorites, container, false);
     }
 
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         lvEvents = (ListView) getActivity().findViewById(R.id.list_events_favorites);
 
         notificationUpdateReceiver = new NotificationUpdateReceiver(new Handler(), new ToDo() {
             @Override
             public void doSomething() {
                 loadEventsFromDb();
                 updateView();
             }
         });
 
         adapterEventsItem = new AdapterEventsItem(this.getActivity(), 0, eventItemList);
         lvEvents.setAdapter(adapterEventsItem);
 
         lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent = new Intent(TabFavorite.this.getActivity(), EventsDescription.class);
                 intent.putExtra("events_id", eventItemList.get(i).getId());
                 startActivityForResult(intent, 1);
             }
         });
     }
 
     @Override
     public void onStart() {
         super.onStart();
        View emptyView = getActivity().findViewById(R.id.list_empty_view);
         lvEvents.setEmptyView(emptyView);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         loadEventsFromDb();
         updateView();
     }
 
     private void loadEventsFromDb() {
         DatabaseManager databaseManager = new DatabaseManager(getActivity());
         try {
             databaseManager.open();
             List<EventItem> newEventItemList = databaseManager.getAllEventsFavorites();
             eventItemList.clear();
             eventItemList.addAll(newEventItemList);
             eventItemList = DatabaseManager.sortEventsByDate(eventItemList);
             databaseManager.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     private void updateView() {
         adapterEventsItem.notifyDataSetChanged();
     }
 
 }
