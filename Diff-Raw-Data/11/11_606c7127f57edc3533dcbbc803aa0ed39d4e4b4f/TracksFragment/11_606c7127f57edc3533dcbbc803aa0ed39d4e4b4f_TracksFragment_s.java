 package ru.noisefm.orders.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.widget.SearchView;
 import ru.noisefm.orders.R;
 import ru.noisefm.orders.activity.MainActivity;
 import ru.noisefm.orders.adapter.TrackAdapter;
 import ru.noisefm.orders.database.FavouritesDatabase;
 import ru.noisefm.orders.helper.InfiniteScrolling;
 import ru.noisefm.orders.helper.NavigationHelper;
 import ru.noisefm.orders.helper.Util;
 import ru.noisefm.orders.loader.TracksLoader;
 import ru.noisefm.orders.order.OrdersTable;
 import ru.noisefm.orders.order.Track;
 
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 
 public class TracksFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<ArrayList<Track>> {
     public static final String ARG_QUERY = "query";
     private TrackAdapter trackAdapter;
     private NavigationHelper navHelper;
     private InfiniteScrolling infiniteScrolling;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.track_list_view, null);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         final MainActivity activity = (MainActivity)getSherlockActivity();
         navHelper = new NavigationHelper();
         trackAdapter = new TrackAdapter(activity);
         ListView trackListView = (ListView)activity.findViewById(R.id.track_list);
         trackListView.setAdapter(trackAdapter);
         trackListView.setEmptyView(activity.findViewById(R.id.track_list_empty_view));
         trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                 Track track = trackAdapter.getItem(pos);
                 activity.orderTrack(track);
             }
         });
         infiniteScrolling = new InfiniteScrolling();
         infiniteScrolling.setOnReachEndListener(new InfiniteScrolling.OnReachEndListener() {
             @Override
             public void onEndReached() {
                 navHelper.setNextPage();
                 startTracksLoader();
             }
         });
         trackListView.setOnScrollListener(infiniteScrolling);
         registerForContextMenu(trackListView);
 
         String query = null;
         Bundle arguments = getArguments();
         if (null != arguments) {
             query = arguments.getString(ARG_QUERY);
         }
         updateView(query);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.track_list_context_menu, menu);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
         inflater.inflate(R.menu.track_list_view_menu, menu);
         super.onCreateOptionsMenu(menu, inflater);
 
         final com.actionbarsherlock.view.MenuItem searchMenuItem = menu.findItem(R.id.menu_search_music);
         SearchView searchView = (SearchView)searchMenuItem.getActionView();
         searchView.setQueryHint(getText(R.string.search_hint));
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 searchMenuItem.collapseActionView();
                 navHelper.pushCurrentQuery();
                 updateView(query);
                 return true;
             }
 
             @Override
             public boolean onQueryTextChange(String s) {
                 return false;
             }
         });
     }
 
     @Override
     public boolean onContextItemSelected(android.view.MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         Track track = trackAdapter.getItem(info.position);
         switch (item.getItemId()) {
             case R.id.track_list_menu_add_to_fav:
                 addTrackToFavList(track);
                 return true;
             case R.id.track_list_menu_search_by_artist:
                 navHelper.pushCurrentQuery();
                 updateView(track.getArtist());
                 return true;
             case R.id.track_list_menu_copy_track_data:
                 Util.copyTrackData(getActivity(), track);
                 return true;
         }
         return false;
     }
 
     @Override
     public void updateActivityTitle() {
         ActionBar actionBar = getSherlockActivity().getSupportActionBar();
         actionBar.setTitle(R.string.search);
         actionBar.setSubtitle(navHelper.getQuery());
     }
 
     @Override
     public Loader<ArrayList<Track>> onCreateLoader(int i, Bundle bundle) {
         TracksLoader tracksLoader = new TracksLoader(getActivity());
         tracksLoader.setPage(navHelper.getPage());
         tracksLoader.setQuery(navHelper.getQuery());
        tracksLoader.forceLoad();
         return tracksLoader;
     }
 
     @Override
     public void onLoadFinished(Loader<ArrayList<Track>> arrayListLoader, ArrayList<Track> tracks) {
         if (null != tracks) {
             trackAdapter.extendItems(tracks);
             infiniteScrolling.itemsCountChanged();
             infiniteScrolling.setDataAvalable(tracks.size() == OrdersTable.ITEMS_PER_PAGE);
         } else {
             getMainActivity().showToast(R.string.unable_to_load);
         }
         getMainActivity().setLoadingState(false);
     }
 
     @Override
     public void onLoaderReset(Loader<ArrayList<Track>> arrayListLoader) {
     }
 
     @Override
     public boolean onBackPressed() {
         try {
             updateView(navHelper.popQuery());
             return true;
         } catch (EmptyStackException ignored) {
             return super.onBackPressed();
         }
     }
 
     private void updateView(String query) {
         infiniteScrolling.setDataAvalable(true);
         trackAdapter.clear();
         navHelper.setQuery(query);
         updateActivityTitle();
         startTracksLoader();
     }
 
     private void startTracksLoader() {
         getMainActivity().setLoadingState(true);
         getLoaderManager().restartLoader(TracksLoader.LOADER_ID, null, this);
     }
 
     private void addTrackToFavList(Track track) {
         FavouritesDatabase favDatabase = getMainActivity().getFavouriteDatabase();
         if (favDatabase.isFavourite(track)) {
             getMainActivity().showToast(R.string.already_in_fav);
         } else {
             favDatabase.addTrack(track);
             getMainActivity().showToast(R.string.added_to_fav);
         }
     }
 }
