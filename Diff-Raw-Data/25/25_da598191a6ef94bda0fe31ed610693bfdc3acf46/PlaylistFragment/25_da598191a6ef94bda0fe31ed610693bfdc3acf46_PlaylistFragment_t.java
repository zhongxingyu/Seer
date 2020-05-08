 package com.ouchadam.fang.presentation.item;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.novoda.notils.caster.Classes;
 import com.novoda.notils.caster.Views;
 import com.ouchadam.bookkeeper.Downloader;
 import com.ouchadam.bookkeeper.watcher.ListItemWatcher;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.FullItem;
 import com.ouchadam.fang.persistance.FangProvider;
 import com.ouchadam.fang.persistance.Query;
 import com.ouchadam.fang.persistance.database.Tables;
 import com.ouchadam.fang.persistance.database.Uris;
 import com.ouchadam.fang.presentation.FullItemMarshaller;
 import com.ouchadam.fang.presentation.panel.SlidingPanelExposer;
 
 import novoda.android.typewriter.cursor.CursorMarshaller;
 
 import java.util.List;
 
 public class PlaylistFragment extends CursorBackedListFragment<FullItem> implements OnItemClickListener<FullItem>, CursorBackedListFragment.OnLongClickListener<FullItem>, PlaylistActionMode.OnPlaylistActionMode {
 
     private final ActionBarTitleSetter actionBarTitleSetter;
 
     private Downloader downloader;
     private boolean hasRestored;
     private DetailsDisplayManager detailsDisplayManager;
     private TextView spaceUsageText;
     private PlaylistActionMode playlistActionMode;
 
     public PlaylistFragment() {
         this.hasRestored = false;
         this.actionBarTitleSetter = new ActionBarTitleSetter();
     }
 
     @Override
     protected View getRootLayout(LayoutInflater inflater, ViewGroup container) {
         return inflater.inflate(R.layout.fragment_playlist_item_list, container, false);
     }
 
     @Override
     protected TypedListAdapter<FullItem> createAdapter() {
         return new PlaylistAdapter(LayoutInflater.from(getActivity()), getActivity());
     }
 
     @Override
     protected Query getQueryValues() {
         return new Query.Builder()
                 .withUri(FangProvider.getUri(Uris.FULL_ITEM))
                 .withSelection(Tables.Playlist.DOWNLOAD_ID + "!=?")
                 .withSelectionArgs(new String[]{"0"})
                 .withSorter(" CAST (" + Tables.Playlist.LIST_POSITION + " AS DECIMAL)" + " ASC")
                 .build();
     }
 
     @Override
     protected CursorMarshaller<FullItem> getMarshaller() {
         return new FullItemMarshaller();
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         downloader = Classes.from(activity);
         actionBarTitleSetter.onAttach(activity);
         SlidingPanelExposer panelController = Classes.from(activity);
         detailsDisplayManager = new DetailsDisplayManager(panelController, new NavigatorForResult(activity));
         playlistActionMode = new PlaylistActionMode(activity, this);
     }
 
     @Override
     protected void onCreateViewExtra(View root) {
         super.onCreateViewExtra(root);
         spaceUsageText = Views.findById(root, R.id.space_usage);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.playlist, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.ab_delete_all_downloads:
                 onRemoveAll();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void onRemoveAll() {
         List<FullItem> items = getAdapter().getAll();
         DownloadDeleter.from(getActivity()).deleteAll(items);
         // TODO remove current playing item
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         setOnItemClickListener(this);
         setOnItemLongClickListener(this);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         setHasOptionsMenu(true);
         actionBarTitleSetter.set("Playlist");
         spaceUsageText.setText(new AvailableSpaceFetcher().formatted());
     }
 
 
     @Override
     public void onDataUpdated(List<FullItem> data) {
         super.onDataUpdated(data);
         if (!data.isEmpty() && !hasRestored) {
             downloader.restore(new LazyListItemWatcher((ListItemWatcher.ItemWatcher) getAdapter()));
             hasRestored = true;
         }
     }
 
     @Override
     public void onItemClick(TypedListAdapter<FullItem> adapter, int position, long itemId) {
         if (!playlistActionMode.isInActionMode()) {
             detailsDisplayManager.displayItem(itemId);
         }
     }
 
     @Override
     public boolean onItemLongClick(TypedListAdapter<FullItem> adapterView, View view, int position, long itemId) {
         if (playlistActionMode.isInActionMode()) {
             return false;
         } else {
             playlistActionMode.onStart();
             allowChecking();
            setLongPressChecked(position);
             return true;
         }
     }
 
     @Override
     public void onDelete() {
         List<FullItem> selectedItems = getCheckedItems();
         if (selectedItems != null && !selectedItems.isEmpty()) {
             DownloadDeleter.from(getActivity()).deleteItems(selectedItems);
         } else {
             Toast.makeText(getActivity(), "Failed to delete, couldn't find and podcasts", Toast.LENGTH_SHORT).show();
         }
     }
 
     private List<FullItem> getCheckedItems() {
         return getAllCheckedPositions();
     }
 
     @Override
     public void onActionModeFinish() {
         deselectAll();
         disallowChecking();
     }
 
 }
