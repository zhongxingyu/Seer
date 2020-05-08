 package com.github.alexesprit.chatlogs.fragment;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ListView;
 import com.actionbarsherlock.internal.widget.IcsToast;
 import com.github.alexesprit.chatlogs.R;
 import com.github.alexesprit.chatlogs.adapter.DiscoveryAdapter;
 import com.github.alexesprit.chatlogs.discovery.BookmarkDiscovery;
 import com.github.alexesprit.chatlogs.discovery.BookmarkDiscoveryLoader;
 import com.github.alexesprit.chatlogs.item.Bookmark;
 import com.github.alexesprit.chatlogs.item.DiscoveryItem;
 import com.github.alexesprit.chatlogs.util.ThemeManager;
 
 import java.util.ArrayList;
 
 public final class DiscoveryDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<DiscoveryItem>> {
     private BookmarkDiscovery discovery;
     private DiscoveryAdapter discoveryAdapter;
     private OnBookmarkListAddListener listener;
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final Context context = getActivity();
 
         discovery = new BookmarkDiscovery();
         View dialogView = LayoutInflater.from(context).inflate(R.layout.discovery_dialog, null);
         discoveryAdapter = new DiscoveryAdapter(context);
         final ListView discoveryList = (ListView)dialogView.findViewById(R.id.discovery_list);
         discoveryList.setAdapter(discoveryAdapter);
         discoveryList.setEmptyView(dialogView.findViewById(R.id.discovery_view_wait));
 
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 ArrayList<Bookmark> checkedItems = new ArrayList<Bookmark>();
                 for (int i = 0; i < discoveryAdapter.getCount(); ++i) {
                     DiscoveryItem item = discoveryAdapter.getItem(i);
                     if (item.checked) {
                         checkedItems.add(new Bookmark(item.address, item.source));
                     }
                 }
                 if (checkedItems.isEmpty()) {
                     IcsToast.makeText(context, R.string.select_item, IcsToast.LENGTH_SHORT).show();
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
         builder.setInverseBackgroundForced(ThemeManager.isNeedToInverseDialogBackground());
         builder.setTitle(R.string.dialog_disovery_title);
         builder.setView(dialogView);
 
         return builder.create();
     }
 
     @Override
     public Loader<ArrayList<DiscoveryItem>> onCreateLoader(int i, Bundle bundle) {
         BookmarkDiscoveryLoader loader = new BookmarkDiscoveryLoader(getActivity());
         loader.setBookmarkDiscovery(discovery);
         loader.forceLoad();
         return loader;
     }
 
     @Override
     public void onLoadFinished(Loader<ArrayList<DiscoveryItem>> arrayListLoader, ArrayList<DiscoveryItem> discoveryItems) {
         if (null != discoveryItems) {
             discoveryAdapter.setItems(discoveryItems);
         } else {
            dismiss();
             IcsToast.makeText(getActivity(), R.string.unable_to_discovery, IcsToast.LENGTH_SHORT).show();
         }
     }
 
     @Override
     public void onLoaderReset(Loader<ArrayList<DiscoveryItem>> arrayListLoader) {
     }
 
     @Override
     public void onStart() {
         super.onStart();
         getLoaderManager().initLoader(BookmarkDiscoveryLoader.LOADER_ID, null, this);
     }
 
     public interface OnBookmarkListAddListener {
         void onBookmarkListAdded(ArrayList<Bookmark> items);
     }
 }
