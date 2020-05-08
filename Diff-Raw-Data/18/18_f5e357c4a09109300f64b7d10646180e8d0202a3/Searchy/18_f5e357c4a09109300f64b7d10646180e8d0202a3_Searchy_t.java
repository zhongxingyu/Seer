 package com.bomba.b;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 
 import android.widget.ListView;
 //import android.widget.SearchView;
 
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
 
 import com.bomba.R;
 import com.bomba.database.DbHelper;
 import com.bomba.services.Mplayer;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingActivity;
 
 public class Searchy extends SlidingActivity implements OnQueryTextListener,
 		OnClickListener {
 	ListView tracks;
 	String pls;
 	Bundle m;
 	DbHelper pickTracks;
 	Button bPrev, bStop, bNext;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		pls = getIntent().getExtras().getString("p");
 		getSupportActionBar().setTitle(pls);
 
 		setContentView(R.layout.s_view);
 		setBehindContentView(R.layout.slide);
 		SlidingMenu sm = getSlidingMenu();
 		sm.setMode(SlidingMenu.LEFT);
 		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 		sm.setShadowWidthRes(R.dimen.shadow_width);
 		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
 		sm.setShadowDrawable(R.drawable.shadow);
 		sm.setFadeDegree(0.35f);
 		sm.setMenu(R.layout.slide);
 
 		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		init();
 
 		// set up the side list
 		ListView lv = (ListView) findViewById(R.id.slideList);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
 					long arg3) {
 				switch (pos) {
 				case 0:
 					Intent n = new Intent(Searchy.this, SideSearch.class);
 					n.putExtra("p", "Search:");
 					startActivity(n);
 					break;
 				case 1:
 					startActivity(new Intent(Searchy.this, MainActivity.class));
 					break;
 				case 2:
 					break;
 
 				}
 			}
 		});
 
 	}
 
 	private void init() {
 		bPrev = (Button) findViewById(R.id.Previous);
 		bStop = (Button) findViewById(R.id.Stop);
 		bNext = (Button) findViewById(R.id.Next);
 		bPrev.setOnClickListener(this);
 		bStop.setOnClickListener(this);
 		bPrev.setOnClickListener(this);
 		tracks = (ListView) findViewById(R.id.listofTracks);
 		pickTracks = new DbHelper(Searchy.this);
 		pickTracks.open();
 		initpl();
 
 	}
 
 	private void initpl() {
 		// if (pickTracks.DoesPlaylistExist(pls)) {
 		loadList l = new loadList();
 		l.execute(pls);
 		// } else {
 		Toast.makeText(Searchy.this,
 				"Please search for a track to add to this playlist",
 				Toast.LENGTH_LONG).show();
 		// }
 	}
 
 	public class loadList extends AsyncTask<String, Void, Void> {
 		ArrayList<HashMap<String, String>> what;
 
 		@Override
 		protected Void doInBackground(String... st) {
 			pickTracks = new DbHelper(Searchy.this);
 			pickTracks.open();
 			what = pickTracks.getTracksInList(st[0]);
 			pickTracks.close();
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			if (what != null) {
 				Log.v("SEARCHYLOADER", what.size() + "");
 				SimpleAdapter adp = new SimpleAdapter(Searchy.this, what,
						R.layout.searchrow,
 						new String[] { pickTracks.A_STAGE_NAME,
								pickTracks.TRACK_TITLE, pickTracks.TRACK_file },
						new int[] { R.id.tvs_a_id, R.id.tv_songs_name,
								R.id.tv_songs_link });
 				tracks.setAdapter(adp);
 
 			} else {
 				Toast.makeText(Searchy.this, "Search tracks to add to list",
 						Toast.LENGTH_LONG).show();
 				Log.v("async", "the arraylist is empty");
 			}
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		com.actionbarsherlock.widget.SearchView searchView = new com.actionbarsherlock.widget.SearchView(
 				getSupportActionBar().getThemedContext());
 		searchView.setQueryHint("Search for Track");
 		menu.add(0, 1, 1, "playlist")
 				.setIcon(R.drawable.action_bar_add_playlist)
 				.setActionView(searchView)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_IF_ROOM
 								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 
 		return true;
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		Log.v("SearchItem", item.getItemId() + "");
 
 		switch (item.getItemId()) {
 		case 1:
 
 			SearchView sv = (SearchView) item.getActionView();
 			sv.setOnQueryTextListener(this);
 
 			// onSearchRequested();
 			break;
 		}
 
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 	}
 
 	@Override
 	protected void onDestroy() {
 
 		super.onDestroy();
 		pickTracks.close();
 	}
 
 	@Override
 	protected void onPause() {
 
 		super.onPause();
 		pickTracks.close();
 
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 
 		// pull data from the cursor
 		Log.v("SEARCHY", "loading songs from db");
 		populateSongs p = new populateSongs();
 		p.execute(query);
 
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 
 		return false;
 	}
 
 	public class populateSongs extends AsyncTask<String, Void, Void> {
 		Cursor mCursor;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (mCursor.getCount() <= 0) {
 				Toast.makeText(
 						Searchy.this,
 						"We don't have any tracks with that name but we will look around",
 						Toast.LENGTH_LONG).show();
 			} else {
 				TextView header = new TextView(Searchy.this);
 				header.setText("long press to add to playlist");
 				tracks.invalidate();
 				//tracks.addHeaderView(header);
 				//tracks.addFooterView(header);
 				ListAdapter adp = new SimpleCursorAdapter(
 						Searchy.this,
 						R.layout.searchrow,
 						mCursor,
 						new String[] { pickTracks.A_STAGE_NAME,
 								pickTracks.TRACK_TITLE, pickTracks.TRACK_file },
 						new int[] { R.id.tvs_a_id, R.id.tv_songs_name,
 								R.id.tv_songs_link });
 				tracks.setAdapter(adp);
 
 			}
 
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected Void doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			pickTracks.open();
 			mCursor = pickTracks.getSearched(params[0]);
 			Log.v("SEARCHY", mCursor.getCount() + "");
 			startManagingCursor(mCursor);
 
 			 tracks.setOnItemClickListener(new OnItemClickListener() {
 			
 			 @Override
 			 public void onItemClick(AdapterView<?> arg0, View v, int pos,
 			 long arg3) {
 			
 			 RelativeLayout relly = (RelativeLayout) v;
 			 TextView tv = ((TextView) findViewById(R.id.tv_songs_link));
 			 String me = tv.getText().toString();
 			
 			 String aI = "http://109.74.201.47/content/" + me;
 			 String sName = "your favorite Song";
 			 String UR = "http://109.74.201.47/content/" + me+".mp3";
 			 Intent view = new Intent(Searchy.this, Player_View.class);
 			 view.putExtra("artistImage", aI);
 			 Intent sing = new Intent(Searchy.this, Mplayer.class);
 			 sing.putExtra("songName", sName);
 			 sing.putExtra("url", UR);
 			
 			 // startActivity(view);
 			 startService(sing);
 			
 			 }
 			 });
 
 			tracks.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 				@Override
 				public boolean onItemLongClick(AdapterView<?> v, View arg1,
 						int arg2, long arg3) {
 					// RelativeLayout relly = (RelativeLayout) v;
 					TextView tv = ((TextView) findViewById(R.id.tv_songs_link));
 					String me = tv.getText().toString();
 					pickTracks.open();
 					int pl_id = pickTracks.getPlaylistId(pls);
 					int tr_id = pickTracks.getTrackId(me);
					pickTracks.AddSongToPlaylist(tr_id, pl_id, me);
 					pickTracks.close();
 					Toast.makeText(Searchy.this,
 							me + " has been added to" + pls, Toast.LENGTH_LONG)
 							.show();
 					initpl();
 
 					return false;
 				}
 			});
 			return null;
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.Stop:
 			stopService(new Intent(Searchy.this, Mplayer.class));
 			break;
 		}
 
 	}
 }
