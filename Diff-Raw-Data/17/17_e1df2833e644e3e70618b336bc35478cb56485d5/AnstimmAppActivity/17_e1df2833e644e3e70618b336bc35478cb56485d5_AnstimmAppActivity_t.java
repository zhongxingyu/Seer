 package com.nerdinand.anstimm.activities;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AlphabetIndexer;
 import android.widget.ListView;
 import android.widget.SectionIndexer;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 import com.nerdinand.anstimm.R;
 import com.nerdinand.anstimm.db.SongDB;
 import com.nerdinand.anstimm.db.SongDBUpdater;
 import com.nerdinand.anstimm.db.SongDBUpdaterException;
 
 public class AnstimmAppActivity extends Activity implements OnItemClickListener {
 	private ListView songListView;
 	private SongDBUpdater songDBUpdater;
 	private MyCursorAdapter cursorAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_layout);
 
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		songDBUpdater = new SongDBUpdater(this);
 
 		songListView = (ListView) this.findViewById(R.id.songListView);
 		songListView.setFastScrollEnabled(true);
 		songListView.setOnItemClickListener(this);
 		
		Cursor songCursor = getSongCursor();
 		
 		cursorAdapter = new MyCursorAdapter(
                         getApplicationContext(),
                         R.layout.song_row,
                         songCursor,
                         new String[]{SongDB.SongTable.COLUMN_TITLE, SongDB.SongTable.COLUMN_COMPOSER},//from
                         new int[]{R.id.title, R.id.composer});//to)
 		
 		songListView.setAdapter(cursorAdapter); 
 		songListView.setFastScrollEnabled(true);
 	}
 
	private Cursor getSongCursor() {
		return SongDB.getInstance(this).getSongs();
	}

 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		if (cursorAdapter.getCount() == 0){
 			updateSongs();
 		}
 
 		reloadSongList();
 	}
 
 	private void reloadSongList() {
		cursorAdapter.changeCursor(getSongCursor());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.information:
 			Intent intent = new Intent(this, InformationActivity.class);
 			this.startActivity(intent);
 
 			break;
 		case R.id.update_songs:
 			updateSongs();
 
 			reloadSongList();
 
 			break;
 
 		default:
 			break;
 		}
 		return true;
 	}
 
 	private void updateSongs() {
 		try {
 			songDBUpdater.update();
 		} catch (SongDBUpdaterException e) {
 			Toast.makeText(this, this.getString(R.string.downloading_error),
 					Toast.LENGTH_LONG);
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		if (parent == songListView) {
 			long songId = cursorAdapter.getItemId(position);
 			startSongActivity(songId);
 		}
 	}
 
 	private void startSongActivity(long songId) {
 		Intent intent = new Intent(this, SongActivity.class);
 		intent.putExtra(SongDB.SongTable.TABLE+"."+SongDB.SongTable.COLUMN_ID, songId);
 		this.startActivity(intent);
 	}
 
 	class MyCursorAdapter extends SimpleCursorAdapter implements SectionIndexer {
 
 		AlphabetIndexer alphaIndexer;
 
 		public MyCursorAdapter(Context context, int layout, Cursor cursor,
 				String[] from, int[] to) {
 
 			super(context, layout, cursor, from, to);
 			alphaIndexer = new AlphabetIndexer(cursor,
 					cursor.getColumnIndex(SongDB.SongTable.COLUMN_TITLE),
 					" ABCDEFGHIJKLMNOPQRSTUVWXYZ");
 
 			// you have just to instanciate the indexer class like this
 
 			// cursor,index of the sorted colum,a string representing the
 			// alphabeth (pay attention on the blank char at the beginning of
 			// the sequence)
 
 		}
 
 		@Override
 		public int getPositionForSection(int section) {
 			return alphaIndexer.getPositionForSection(section); // use the
 																// indexer
 		}
 
 		@Override
 		public int getSectionForPosition(int position) {
 			return alphaIndexer.getSectionForPosition(position); // use the
 																	// indexer
 		}
 
 		@Override
 		public Object[] getSections() {
 			return alphaIndexer.getSections(); // use the indexer
 		}
 
 	}
 }
