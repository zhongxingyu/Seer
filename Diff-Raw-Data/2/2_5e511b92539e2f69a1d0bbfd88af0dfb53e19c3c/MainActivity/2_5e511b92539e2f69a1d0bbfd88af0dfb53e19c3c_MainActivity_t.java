 package benny.bach.spotihifi;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.LoaderManager;
 import android.content.ComponentName;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements Callback {
     private static final String TAG = "MainActivity";
 
     //private TrackListFragment mTrackListFragment;
     //private ArtistListFragment mArtistListFragment;
     //private PlaylistListFragment mPlaylistListFragment;
     //private ViewPager mViewPager;
     private SpotiHifiService mSpotiHifiService;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         //mTrackListFragment = new TrackListFragment();
         //mArtistListFragment = new ArtistListFragment();
         //mPlaylistListFragment = new PlaylistListFragment();
 
         if ( savedInstanceState == null ) {
             getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new TrackListFragment(), "tracks")
                 .commit();
         }
 
         Log.i(TAG, "created");
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.i(TAG, "pausing");
         if ( mSpotiHifiService != null ) {
             unbindService(mConnection);
             mSpotiHifiService = null;
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.i(TAG, "resuming");
 
         bindService(new Intent(this, SpotiHifiService.class), mConnection, Context.BIND_AUTO_CREATE);
 
         // Hmmm - For now remove any content as sync will load everything. Really need
         // to show a loading message.
 
         //Toast.makeText(getApplicationContext(), "synchronizing...", Toast.LENGTH_SHORT).show();
 
         //clearBackStack();
 
         //getFragmentManager().beginTransaction()
         //  .remove(mTrackListFragment)
         //  .commit();
 
         //getFragmentManager().beginTransaction()
         //  .remove(mArtistListFragment)
         //  .commit();
 
         //getFragmentManager().beginTransaction()
         //  .remove(mPlaylistListFragment)
         //  .commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             Log.i(TAG, "go home");
             return true;
         case R.id.action_play:
             playerPlay(null);
             return true;
         case R.id.action_play_all:
             playerPlay("");
             return true;
         case R.id.action_pause:
             playerPause();
             return true;
         case R.id.action_skip:
             playerSkip();
             return true;
         case R.id.action_stop:
             playerStop();
             return true;
         case R.id.action_songs:
             //if ( !mTrackListFragment.isVisible() )
             //{
             //  Log.i(TAG, "Show songs");
             //  clearBackStack();
             //    getFragmentManager().beginTransaction()
             //            .replace(android.R.id.content, mTrackListFragment)
             //            .commit();
             //}
             getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new TrackListFragment(), "tracks")
                 .commit();
             return true;
         case R.id.action_artists:
             //if ( !mArtistListFragment.isVisible() )
             //{
             //  Log.i(TAG, "Show artists");
             //  clearBackStack();
             //    getFragmentManager().beginTransaction()
             //            .replace(android.R.id.content, mArtistListFragment)
             //            .commit();
             //}
 
             getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new ArtistListFragment(), "artists")
                 .commit();
 
             return true;
         case R.id.action_playlists:
             //if ( !mPlaylistListFragment.isVisible() )
             //{
             //  Log.i(TAG, "Show playlists");
             //  clearBackStack();
             //    getFragmentManager().beginTransaction()
             //            .replace(android.R.id.content, mPlaylistListFragment)
             //            .commit();
             //}
 
             getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new PlaylistListFragment(), "playlists")
                 .commit();
 
             return true;
         case R.id.action_info:
             getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new PlayerFragment(), "info")
                 .commit();
             return true;
         case R.id.action_settings:
             Intent settings = new Intent(this, SettingsActivity.class);
             startActivity(settings);
             return true;
         default:
             Log.i(TAG, "go " + item.getItemId());
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean handleMessage(Message msg) {
         switch ( msg.what )
         {
         //case SpotifyService.SYNC_COMPLETE_RELOAD_MSG_ID:
         //  // Hmmm - For now create a new track list fragment and show it.
         //  mTrackListFragment = new TrackListFragment();
         //  // FALL THROUGH INTENDED
         //case SpotifyService.SYNC_COMPLETE_NO_CHANGE_MSG_ID:
         //  Log.i(TAG, "Show songs");
         //    getFragmentManager().beginTransaction()
         //            .replace(android.R.id.content, mTrackListFragment)
         //            .commit();
         //  break;
         case SpotiHifi.RESULT_MSG_ID:
             Bundle bundle = msg.getData();
             String result = bundle.getString("result");
             Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
             break;
         default:
             Log.i(TAG, "main activity got unknown message");
             break;
         }
         return false;
     }
 
     private void showSongsForArtist(String artist)
     {
         TrackListFragment f = new TrackListFragment();
 
         Bundle args = new Bundle();
         args.putString("where", "artist=\""+artist+"\"");
         f.setArguments(args);
 
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, f)
                 .addToBackStack("artist.tracklist")
                 .commit();
     }
 
     private void showSongsForPlaylist(String playlist)
     {
         TrackListFragment f = new TrackListFragment();
 
         Bundle args = new Bundle();
         // TODO: This probably doesn't handle all escaping, have to study the specs.
         playlist = playlist.replaceAll("/", "\\\\/");
         playlist = playlist.replaceAll("'", "''");
         args.putString("where", "playlists LIKE '%\""+playlist+"\"%'");
         f.setArguments(args);
 
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, f)
                 .addToBackStack("playlist.tracklist")
                 .commit();
     }
 
     private void playerPlay(String playlist)
     {
         if ( mSpotiHifiService != null ) {
             mSpotiHifiService.playerPlay(playlist);
         }
         else {
             Log.e(TAG, "not bound to spotihifi service");
         }
     }
 
     private void queueTrack(String trackId)
     {
         if ( mSpotiHifiService != null ) {
             mSpotiHifiService.queue(trackId);
         }
         else {
             Log.e(TAG, "not bound to spotihifi service");
         }
     }
 
     private void playerStop()
     {
         if ( mSpotiHifiService != null ) {
             mSpotiHifiService.playerStop();
         }
         else {
             Log.e(TAG, "not bound to spotihifi service");
         }
     }
 
     private void playerPause()
     {
         if ( mSpotiHifiService != null ) {
             mSpotiHifiService.playerPause();
         }
         else {
             Log.e(TAG, "not bound to spotihifi service");
         }
     }
 
     private void playerSkip()
     {
         if ( mSpotiHifiService != null ) {
             mSpotiHifiService.playerSkip();
         }
         else {
             Log.e(TAG, "not bound to spotihifi service");
         }
     }
 
     //private void clearBackStack()
     //{
     //  for ( int i=0; i<getFragmentManager().getBackStackEntryCount(); ++i) {
     //      getFragmentManager().popBackStack();
     //  }
     //}
 
 
 
     /////
     // SpotiHifiService connection
     //
 
     private ServiceConnection mConnection = new ServiceConnection()
     {
         public void onServiceConnected(ComponentName className, IBinder service) {
             mSpotiHifiService = ((SpotiHifiService.SpotiHifiBinder)service).getService();
             mSpotiHifiService.setResultHandler(new Handler(MainActivity.this));
         }
 
         public void onServiceDisconnected(ComponentName className) {
             mSpotiHifiService = null;
         }
     };
 
     ///////////////////////////////////////////////////////////////////////////
     // TrackListCursorAdapter
     //
 
     static public class TrackListCursorAdapter extends CursorAdapter {
 
         public TrackListCursorAdapter(Context context, Cursor c) {
             super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             TextView title = (TextView) view.findViewById(R.id.song_title);
             title.setText(cursor.getString(cursor.getColumnIndex(SpotiHifi.Tracks.COLUMN_NAME_TITLE)));
 
             TextView artist_n_album = (TextView) view.findViewById(R.id.song_artist);
 
             String artist = cursor.getString(cursor.getColumnIndex(SpotiHifi.Tracks.COLUMN_NAME_ARTIST));
             String album = cursor.getString(cursor.getColumnIndex(SpotiHifi.Tracks.COLUMN_NAME_ALBUM));
 
             artist_n_album.setText(artist + "   \u2022   " + album);
         }
 
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             // Inflate your view here.
             LayoutInflater inflater = LayoutInflater.from(parent.getContext());
             return inflater.inflate(R.layout.song, parent, false);
         }
     }
 
     ///////////////////////////////////////////////////////////////////////////
     // TrackListFragment
     //
 
     public static class TrackListFragment extends Fragment
             implements LoaderManager.LoaderCallbacks<Cursor>
     {
         private static final String TAG = "TrackListFragment";
 
         private static final String[] PROJECTION = new String[] {
             SpotiHifi.Tracks.COLUMN_NAME_ID, // 0
             SpotiHifi.Tracks.COLUMN_NAME_TITLE, // 1
             SpotiHifi.Tracks.COLUMN_NAME_ARTIST, // 2
             SpotiHifi.Tracks.COLUMN_NAME_ALBUM // 3
         };
 
         private CursorAdapter mAdapter;
         private String mWhere;
 
         public TrackListFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
         {
             View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
 
             Bundle args = getArguments();
             mWhere = (args == null ? null : args.getString("where"));
 
             return rootView;
         }
 
         @Override
         public void onActivityCreated (Bundle savedInstanceState)
         {
             super.onActivityCreated(savedInstanceState);
             Log.i(TAG, "TrackListFragment: activity created");
 
             mAdapter = new TrackListCursorAdapter(getActivity(), null);
 
             ListView lv = (ListView) getActivity().findViewById(R.id.tracklist);
 
             lv.setOnItemClickListener(new OnItemClickListener() {
                 public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
                 {
                     Uri uri = ContentUris.withAppendedId(SpotiHifi.Tracks.CONTENT_URI_BASE, id);
 
                     Cursor c = getActivity().getContentResolver().query(uri, SpotiHifi.Tracks.TRACK_PROJECTION, null, null, null);
 
                     if (c == null || !c.moveToFirst()) {
                         Log.i(TAG, "tracklist fragment - song not found " + id);
                     }
                     else {
                         int trackIdIndex = c.getColumnIndex(SpotiHifi.Tracks.COLUMN_NAME_TRACK_ID);
                         String trackId = c.getString(trackIdIndex);
 
                         ((MainActivity)getActivity()).queueTrack(trackId);
                     }
                 }
             });
 
            //lv.setFastScrollAlwaysVisible(true);
             lv.setAdapter(mAdapter);
 
             getLoaderManager().initLoader(0, null, this);
         }
 
         @Override
         public void onAttach(Activity activity)
         {
             super.onAttach(activity);
             Log.i(TAG, "TrackListFragment: attach");
         }
 
         @Override
         public void onDetach()
         {
             super.onDetach();
             Log.i(TAG, "TrackListFragment: detach");
         }
 
         @Override
         public void onResume()
         {
             super.onResume();
         }
 
         @Override
         public void onPause()
         {
             super.onPause();
             Log.i(TAG, "TrackListFragment: pause");
         }
 
         @Override
         public void onDestroyView()
         {
             super.onDestroyView();
             Log.i(TAG, "TrackListFragment: destroy view");
         }
 
         @Override
         public void onDestroy()
         {
             super.onDestroy();
             Log.i(TAG, "TrackListFragment: destroy");
         }
 
         public Loader<Cursor> onCreateLoader(int id, Bundle args)
         {
              return new CursorLoader(
                      getActivity(),
                      SpotiHifi.Tracks.CONTENT_URI,
                      PROJECTION,
                      mWhere,
                      null,
                      null);
         }
 
         public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
             mAdapter.swapCursor(data);
         }
 
         public void onLoaderReset(Loader<Cursor> loader) {
             mAdapter.swapCursor(null);
         }
     }
 
 
 
     ///////////////////////////////////////////////////////////////////////////
     // ArtistListFragment
     //
 
     public static class ArtistListFragment extends Fragment
             implements LoaderManager.LoaderCallbacks<Cursor>
     {
         private static final String TAG = "ArtistListFragment";
 
         private CursorAdapter mAdapter;
         private MainActivity mActivity;
 
         public ArtistListFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
         {
             View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
             return rootView;
         }
 
         @Override
         public void onActivityCreated (Bundle savedInstanceState)
         {
             super.onActivityCreated(savedInstanceState);
             Log.i(TAG, "ArtistListFragment: activity created");
 
             mActivity = (MainActivity)getActivity();
 
             String[] from = { SpotiHifi.Artists.COLUMN_NAME_ARTIST };
             int[] to = { R.id.song_title };
 
             mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.song, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 
             ListView lv = (ListView) getActivity().findViewById(R.id.tracklist);
 
             lv.setOnItemClickListener(new OnItemClickListener() {
                 public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                     mActivity.showSongsForArtist(getValueAtPosition(position));
                 }
             });
 
             registerForContextMenu(lv);
 
             lv.setAdapter(mAdapter);
 
             getLoaderManager().initLoader(2, null, this);
         }
 
         @Override
         public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
         {
             super.onCreateContextMenu(menu, v, menuInfo);
             MenuInflater inflater = mActivity.getMenuInflater();
             inflater.inflate(R.menu.playlist_ctx_menu, menu);
         }
 
         @Override
         public boolean onContextItemSelected(MenuItem item)
         {
             AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 
             switch (item.getItemId())
             {
             case R.id.play_item:
                 //mActivity.playerPlay(getValueAtPosition(info.position));
                 return true;
             }
             return false;
         }
 
 
         @Override
         public void onAttach(Activity activity)
         {
             super.onAttach(activity);
             Log.i(TAG, "ArtistListFragment: attach");
         }
 
         @Override
         public void onDetach()
         {
             super.onDetach();
             Log.i(TAG, "ArtistListFragment: detach");
         }
 
         @Override
         public void onResume()
         {
             super.onResume();
         }
 
         @Override
         public void onPause()
         {
             super.onPause();
             Log.i(TAG, "ArtistListFragment: pause");
         }
 
         @Override
         public void onDestroyView()
         {
             super.onDestroyView();
             Log.i(TAG, "ArtistListFragment: destroy view");
         }
 
         @Override
         public void onDestroy()
         {
             super.onDestroy();
             Log.i(TAG, "ArtistListFragment: destroy");
         }
 
         public Loader<Cursor> onCreateLoader(int id, Bundle args)
         {
              return new CursorLoader(
                      getActivity(),
                      SpotiHifi.Artists.CONTENT_URI,
                      SpotiHifi.Artists.ARTIST_PROJECTION,
                      null,
                      null,
                      null);
         }
 
         public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
             mAdapter.swapCursor(data);
         }
 
         public void onLoaderReset(Loader<Cursor> loader) {
             mAdapter.swapCursor(null);
         }
 
         private String getValueAtPosition(int position)
         {
             Cursor cursor =  mAdapter.getCursor();
 
             cursor.moveToPosition(position);
             String value = cursor.getString(cursor.getColumnIndex(SpotiHifi.Artists.COLUMN_NAME_ARTIST));
 
             return value;
         }
     }
 
 
 
     ///////////////////////////////////////////////////////////////////////////
     // PlaylistListFragment
     //
 
     public static class PlaylistListFragment extends Fragment
             implements LoaderManager.LoaderCallbacks<Cursor>
     {
         private static final String TAG = "PlaylistListFragment";
 
         private CursorAdapter mAdapter;
         private MainActivity mActivity;
 
         public PlaylistListFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
         {
             View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
             return rootView;
         }
 
         @Override
         public void onActivityCreated (Bundle savedInstanceState)
         {
             super.onActivityCreated(savedInstanceState);
             Log.i(TAG, "PlaylistListFragment: activity created");
 
             mActivity = (MainActivity)getActivity();
 
             String[] from = { SpotiHifi.Playlists.COLUMN_NAME_TITLE };
             int[] to = { R.id.song_title };
 
             mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.song, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 
             ListView lv = (ListView) getActivity().findViewById(R.id.tracklist);
 
             lv.setOnItemClickListener(new OnItemClickListener() {
                 public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                     mActivity.showSongsForPlaylist(getValueAtPosition(position));
                 }
             });
 
             registerForContextMenu(lv);
 
             lv.setAdapter(mAdapter);
 
             getLoaderManager().initLoader(1, null, this);
         }
 
         @Override
         public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
         {
             super.onCreateContextMenu(menu, v, menuInfo);
             MenuInflater inflater = mActivity.getMenuInflater();
             inflater.inflate(R.menu.playlist_ctx_menu, menu);
         }
 
         @Override
         public boolean onContextItemSelected(MenuItem item)
         {
             AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 
             switch (item.getItemId())
             {
             case R.id.play_item:
                 mActivity.playerPlay(getValueAtPosition(info.position));
                 return true;
             }
             return false;
         }
 
 
         @Override
         public void onAttach(Activity activity)
         {
             super.onAttach(activity);
             Log.i(TAG, "PlaylistListFragment: attach");
         }
 
         @Override
         public void onDetach()
         {
             super.onDetach();
             Log.i(TAG, "PlaylistListFragment: detach");
         }
 
         @Override
         public void onResume()
         {
             super.onResume();
         }
 
         @Override
         public void onPause()
         {
             super.onPause();
             Log.i(TAG, "PlaylistListFragment: pause");
         }
 
         @Override
         public void onDestroyView()
         {
             super.onDestroyView();
             Log.i(TAG, "PlaylistListFragment: destroy view");
         }
 
         @Override
         public void onDestroy()
         {
             super.onDestroy();
             Log.i(TAG, "PlaylistListFragment: destroy");
         }
 
         public Loader<Cursor> onCreateLoader(int id, Bundle args)
         {
              return new CursorLoader(
                      getActivity(),
                      SpotiHifi.Playlists.CONTENT_URI,
                      SpotiHifi.Playlists.PLAYLIST_PROJECTION,
                      null,
                      null,
                      null);
         }
 
         public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
             mAdapter.swapCursor(data);
         }
 
         public void onLoaderReset(Loader<Cursor> loader) {
             mAdapter.swapCursor(null);
         }
 
         private String getValueAtPosition(int position)
         {
             Cursor cursor =  mAdapter.getCursor();
 
             cursor.moveToPosition(position);
             String value = cursor.getString(cursor.getColumnIndex(SpotiHifi.Playlists.COLUMN_NAME_TITLE));
 
             return value;
         }
     }
 
 
 
     ///////////////////////////////////////////////////////////////////////////
     // PlayerFragment
     //
 
     public static class PlayerFragment extends Fragment
             implements LoaderManager.LoaderCallbacks<Cursor>
     {
         private static final String TAG = "PlayerFragment";
 
         private MainActivity mActivity;
 
         public PlayerFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
         {
             View rootView = inflater.inflate(R.layout.fragment_player, container, false);
             return rootView;
         }
 
         @Override
         public void onActivityCreated (Bundle savedInstanceState)
         {
             super.onActivityCreated(savedInstanceState);
             Log.i(TAG, "PlayerFragment: activity created");
 
             mActivity = (MainActivity)getActivity();
 
             getLoaderManager().initLoader(3, null, this);
         }
 
 
         //@Override
         //public void onAttach(Activity activity)
         //{
         //  super.onAttach(activity);
         //  Log.i(TAG, "PlayerFragment: attach");
         //}
 
         //@Override
         //public void onDetach()
         //{
         //  super.onDetach();
         //  Log.i(TAG, "PlayerFragment: detach");
         //}
 
         //@Override
         //public void onResume()
         //{
         //  super.onResume();
         //}
 
         //@Override
         //public void onPause()
         //{
         //  super.onPause();
         //  Log.i(TAG, "PlayerFragment: pause");
         //}
 
         //@Override
         //public void onDestroyView()
         //{
         //  super.onDestroyView();
         //  Log.i(TAG, "PlayerFragment: destroy view");
         //}
 
         //@Override
         //public void onDestroy()
         //{
         //  super.onDestroy();
         //  Log.i(TAG, "PlayerFragment: destroy");
         //}
 
         public Loader<Cursor> onCreateLoader(int id, Bundle args)
         {
              return new CursorLoader(
                      getActivity(),
                      SpotiHifi.PlayerState.CONTENT_URI,
                      SpotiHifi.PlayerState.PLAYER_STATE_PROJECTION,
                      null,
                      null,
                      null);
         }
 
         public void onLoadFinished(Loader<Cursor> loader, Cursor data)
         {
             if ( data == null || !data.moveToFirst() ) {
                 Log.e(TAG, "player state table appears to be empty!");
             }
             else
             {
                 TextView tv1 = (TextView) getActivity().findViewById(R.id.state1);
                 TextView tv2 = (TextView) getActivity().findViewById(R.id.state2);
                 ImageView iv1 = (ImageView) getActivity().findViewById(R.id.cover_art);
                 //TextView tv3 = (TextView) getActivity().findViewById(R.id.state3);
 
                 String title = data.getString(data.getColumnIndex(SpotiHifi.PlayerState.COLUMN_NAME_TITLE));
                 String artist = data.getString(data.getColumnIndex(SpotiHifi.PlayerState.COLUMN_NAME_ARTIST));
                 String album = data.getString(data.getColumnIndex(SpotiHifi.PlayerState.COLUMN_NAME_ALBUM));
                 //String state = data.getString(data.getColumnIndex(SpotiHifi.PlayerState.COLUMN_NAME_STATE));
                 byte[] image = data.getBlob(data.getColumnIndex(SpotiHifi.PlayerState.COLUMN_NAME_COVER_ART));
 
                 Log.i(TAG, "image blob length=" + image.length );
 
                 Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
 
                 tv1.setText(title);
                 tv2.setText(artist + "   \u2022   " + album);
                 iv1.setImageBitmap(bitmap);
                 //tv3.setText(state);
             }
         }
 
         public void onLoaderReset(Loader<Cursor> loader)
         {
             // TODO: Clear view.
         }
     }
 
 }
