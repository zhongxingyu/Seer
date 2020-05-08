 // Copyright 2009 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package org.dbartists;
 
 import org.dbartists.utils.PlaylistProvider;
 import org.dbartists.utils.PlaylistProvider.Items;
 import org.dbartists.utils.RecentArtistProvider.ArtistItems;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class PlaylistActivity extends Activity implements OnClickListener,
 		OnItemClickListener, OnItemLongClickListener {
 	public class MyDataViewBinder implements SimpleCursorAdapter.ViewBinder {
 		@Override
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 			int nImageIndex = cursor
 					.getColumnIndex(PlaylistProvider.Items.IS_PLAYING);
 
 			if (nImageIndex == columnIndex) {
 				ImageView image = (ImageView) view;
 				int isPlaying = cursor.getInt(columnIndex);
 				if (isPlaying == 0) {
 					image.setImageResource(R.drawable.icon_item);
 				} else {
 					image.setImageResource(R.drawable.icon_listen_main);
 				}
 				return true;
 			}
 
 			return false;
 		}
 	}
 
 	class MySimpleCursorAdapter extends SimpleCursorAdapter {
 
 		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
 				String[] from, int[] to) {
 			super(context, layout, c, from, to);
 			setViewBinder(new MyDataViewBinder());
 		}
 
 	}
 
 	private static final String LOG_TAG = PlaylistActivity.class.getName();
 
 	@Override
 	public void onClick(View arg0) {
 		switch (arg0.getId()) {
 		case R.id.PlaylistClear:
 			getContentResolver().delete(PlaylistProvider.CONTENT_URI, null,
 					null);
 			refreshList();
 			break;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		// Remove title bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.playlist);
 		TextView clearButton = (TextView) findViewById(R.id.PlaylistClear);
 		clearButton.setOnClickListener(this);
		
 
 		refreshList();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Cursor c = (Cursor) parent.getItemAtPosition(position);
 		if (c != null) {
 			c.moveToPosition(position);
 			long playlistId = c.getLong(c.getColumnIndex(BaseColumns._ID));
 			String name = c.getString(c.getColumnIndex(Items.NAME));
 			String url = c.getString(c.getColumnIndex(Items.URL));
 			Intent it = new Intent(Constants.REMOTE_PLAY_ACTION);
 			it.putExtra(Constants.EXTRA_TRACK_ID, playlistId);
 			it.putExtra(Constants.EXTRA_TRACK_NAME, name);
 			it.putExtra(Constants.EXTRA_TRACK_URL, url);
 			sendBroadcast(it);
 			Log.d(LOG_TAG, "clicked on position " + position + ", id "
 					+ playlistId);
 		}
 		c.close();
 		finish();
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view,
 			int position, long id) {
 		Cursor c = (Cursor) parent.getItemAtPosition(position);
 		String selection = Items._ID + " = ?";
 		String[] selectionArgs = new String[1];
 		selectionArgs[0] = Integer.toString(c.getInt(c
 				.getColumnIndex(Items._ID)));
 		getContentResolver().delete(PlaylistProvider.CONTENT_URI, selection,
 				selectionArgs);
 		refreshList();
 		return true;
 	}
 
 	private void refreshList() {
 		String[] cols = new String[] { Items.IS_PLAYING, Items.NAME };
 		Cursor cursor = managedQuery(PlaylistProvider.CONTENT_URI, null, null,
 				null, Items.PLAY_ORDER);
 		Log.d(LOG_TAG, "" + cursor.getCount());
 		startManagingCursor(cursor);
 
 		ListAdapter adapter = new MySimpleCursorAdapter(this,
 				R.layout.playlist_item, cursor, cols, new int[] {
 						R.id.StationItemPlayableImage, R.id.PlaylistItemText });
 
 		ListView listView = (ListView) findViewById(R.id.ListView01);
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
 	}
 }
