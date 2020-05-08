 package com.hyperactivity.android_app.activities;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.ContextMenu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.AdminActionCallback;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.core.SimpleAdapterEx;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class ThreadListFragment extends ListFragment {
 
     private static final String THREAD_HEADLINE = "thread_headline";
     private static final String THREAD_TEXT = "thread_text";
     private static final String THREAD_IMAGE = "thread_image";
 
     private String[] from = new String[]{THREAD_HEADLINE, THREAD_TEXT, THREAD_IMAGE};
     private int[] to = new int[]{R.id.thread_headline, R.id.thread_text, R.id.thread_image};
 
     private List<Thread> currentThreads;
     private List<HashMap<String, Object>> data;
     private AdminActionCallback callback;
     private boolean showCategoryImages = true;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         //TODO: debug only
        if (true || ((Engine) getActivity().getApplication()).getClientInfo().getAccount().isAdmin()) {
             registerForContextMenu(getListView());
         }
     }
 
     public void updateThreadList(List<Thread> threadList) {
         data = new ArrayList<HashMap<String, Object>>();
 
         for (int i = 0; i < threadList.size(); i++) {
             Thread thread = threadList.get(i);
             data.add(threadToMap(thread));
         }
 
         if (getActivity() != null) {
             final SimpleAdapterEx adapter = new SimpleAdapterEx(getActivity().getBaseContext(), data, R.layout.thread_list_item, from, to);
             data = null;
 
             setListAdapter(adapter);
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
             SimpleAdapterEx adapter = new SimpleAdapterEx(getActivity().getBaseContext(), data, R.layout.thread_list_item, from, to);
             setListAdapter(adapter);
             data = null;
         }
     }
 
     private HashMap<String, Object> threadToMap(Thread thread) {
         String headline = thread.getHeadLine();
         String text = thread.getText();
         HashMap<String, Object> row = new HashMap<String, Object>();
         row.put(THREAD_HEADLINE, headline);
         row.put(THREAD_TEXT, text);
         if (showCategoryImages) {
             row.put(THREAD_IMAGE, thread.getParentCategory().getImage(getActivity()));
         } else {
             row.put(THREAD_IMAGE, thread.getAccount().getProfilePicture());
         }
         return row;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         MenuInflater inflater = this.getActivity().getMenuInflater();
         inflater.inflate(R.menu.admin_edit_delete, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 
         if(callback == null) {
             return super.onContextItemSelected(item);
         }
 
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         Thread thread = currentThreads.get(info.position);
 
         switch (item.getItemId()) {
 
             /*
             case R.id.admin_edit:
                 callback.editThread(thread);
                 return true;
             */
 
             case R.id.admin_delete:
                 callback.deleteThread(thread);
                 return true;
 
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     public void setCallback(AdminActionCallback callback) {
         this.callback = callback;
     }
 
     public void setShowCategoryImages() {
         showCategoryImages = true;
     }
 
     public void setShowProfileImages() {
         showCategoryImages = false;
     }
 }
