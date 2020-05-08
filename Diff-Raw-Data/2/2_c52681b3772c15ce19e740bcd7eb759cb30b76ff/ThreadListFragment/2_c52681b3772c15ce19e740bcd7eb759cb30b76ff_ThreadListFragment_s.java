 package com.hyperactivity.android_app.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 public class ThreadListFragment extends ListFragment {
 
     private String[] from = new String[]{"thread_headline", "thread_text"};
     private int[] to = new int[]{R.id.thread_headline, R.id.thread_text};
 
     private List<Thread> currentThreads;
     private List<HashMap<String, String>> data;
 
     public void updateThreadList(List<Thread> threadList) {


         data = new ArrayList<HashMap<String, String>>();
 
         for (int i = 0; i < threadList.size(); i++) {
             Thread thread = threadList.get(i);
             data.add(threadToMap(thread));
         }
 
         if (getActivity() != null) {
             SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), data, R.layout.thread_list_item, from, to);
             setListAdapter(adapter);
             data = null;
             getListView().setDivider(null);
         }
         currentThreads = threadList;
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         if (position >= 0 && position < currentThreads.size()) {
             if (getActivity() != null) {
                 ((MainActivity) getActivity()).visitThread(currentThreads.get(position));
             }
         }
 
     }
 
 
     @Override
     public void onResume() {
         super.onResume();
         if (data != null) {
             SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), data, R.layout.thread_list_item, from, to);
             setListAdapter(adapter);
             data = null;
         }
     }
 
     private HashMap<String, String> threadToMap(Thread thread) {
         String headline = thread.getHeadLine();
         String text = thread.getText();
         HashMap<String, String> row = new HashMap<String, String>();
         row.put("thread_headline", headline);
         row.put("thread_text", text);
         return row;
     }
 }
