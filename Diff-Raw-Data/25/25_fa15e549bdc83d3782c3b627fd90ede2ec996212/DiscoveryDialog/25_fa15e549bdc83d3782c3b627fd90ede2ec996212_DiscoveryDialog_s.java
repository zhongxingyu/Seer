 package com.github.alexesprit.chatlogs.dialog;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.github.alexesprit.chatlogs.R;
 import com.github.alexesprit.chatlogs.adapter.DiscoveryAdapter;
 import com.github.alexesprit.chatlogs.discovery.BookmarkDiscovery;
 import com.github.alexesprit.chatlogs.item.Bookmark;
 import com.github.alexesprit.chatlogs.item.DiscoveryItem;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class DiscoveryDialog {
     private AlertDialog dialog;
     private ListView discoveryList;
     private BookmarkDiscovery discovery;
     private OnBookmarkListAddListener listener;
 
     public DiscoveryDialog(final Context context) {
         discovery = new BookmarkDiscovery();
 
         View dialogView = LayoutInflater.from(context).inflate(R.layout.discovery_dialog, null);
         discoveryList = (ListView)dialogView.findViewById(R.id.discovery_list);
         discoveryList.setEmptyView(dialogView.findViewById(R.id.discovery_view_wait));
 
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 ArrayList<Bookmark> checkedItems = new ArrayList<Bookmark>();
                 DiscoveryAdapter adapter = (DiscoveryAdapter)discoveryList.getAdapter();
                 for (int i = 0; i < adapter.getCount(); ++i) {
                     DiscoveryItem item = adapter.getItem(i);
                     if (item.checked) {
                         checkedItems.add(new Bookmark(item.address, item.source));
                     }
                 }
                 if (checkedItems.isEmpty()) {
                     Toast.makeText(context, R.string.enter_address, Toast.LENGTH_SHORT).show();
                 } else {
                     try {
                         listener = (OnBookmarkListAddListener)context;
                         listener.onBookmarkListAdded(checkedItems);
                     } catch (ClassCastException ignored) {
                     }
                 }
             }
         });
         builder.setNegativeButton(android.R.string.cancel, null);
         builder.setTitle(R.string.dialog_disovery_title);
         builder.setView(dialogView);
 
         dialog = builder.create();
         new DiscoveryTask().execute();
     }
 
     public void show() {
         dialog.show();
     }
 
     public interface OnBookmarkListAddListener {
         void onBookmarkListAdded(ArrayList<Bookmark> items);
     }
 
     private class DiscoveryTask extends AsyncTask<Void, Void, ArrayList<DiscoveryItem>> {
         @Override
         protected ArrayList<DiscoveryItem> doInBackground(Void... voids) {
             try {
                 return discovery.getBookmarkList();
             } catch (IOException ignored) {
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(ArrayList<DiscoveryItem> bookmarks) {
             Context context = dialog.getContext();
             if (null != bookmarks) {
                 DiscoveryAdapter adapter = new DiscoveryAdapter(context, bookmarks);
                 discoveryList.setAdapter(adapter);
             } else {
                 dialog.dismiss();
                 Toast.makeText(context, R.string.unable_to_discovery, Toast.LENGTH_SHORT).show();
             }
         }
     }
 }
