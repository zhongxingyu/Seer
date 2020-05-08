 // Copyright 2009 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package org.dbartists;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dbartists.api.Artist;
 import org.dbartists.api.ArtistInfo;
 import org.dbartists.api.ArtistInfoFactory;
 import org.dbartists.api.Track;
 import org.dbartists.utils.PlaylistEntry;
 import org.dbartists.utils.PlaylistProvider;
 import org.dbartists.utils.PlaylistProvider.Items;
 import org.dbartists.utils.RecentArtistProvider;
 import org.dbartists.utils.RecentArtistProvider.ArtistItems;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class TrackListActivity extends PlayerActivity implements
 		OnItemClickListener {
 
 	private final static String TAG = "TrackListActivity";
 	private String apiUrl = Constants.ARTIST_MP3_API_URL;
 	private String artistUrl;
 	private String artistName;
 	private String artistImg;
 	private ArtistInfo artistInfo;
 
 	private ImageLoader dm;
 
 	protected TrackListAdapter listAdapter;
 
 	private void addTracks() {
 		String trackUrl = apiUrl + "?url=" + artistUrl;
 		listAdapter.addMoreTracks(trackUrl, 0);
 	}
 
 	private boolean existInPlaylist(String name, boolean next) {
 		String selection = PlaylistProvider.Items.NAME + " = ?";
 		String[] selectionArgs = new String[1];
 		selectionArgs[0] = name;
 		String sort = PlaylistProvider.Items.PLAY_ORDER
 				+ (next ? " asc" : " desc");
 		return retrievePlaylistItem(selection, selectionArgs, sort);
 	}
 
 	@Override
 	public CharSequence getMainTitle() {
 		return artistName;
 	}
 
 	@Override
 	public boolean isRefreshable() {
 		return true;
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 
 			TextView name = (TextView) findViewById(R.id.artistName);
 			TextView genre = (TextView) findViewById(R.id.artistGnere);
 			TextView member = (TextView) findViewById(R.id.artistMember);
 			TextView company = (TextView) findViewById(R.id.artistCompany);
 
 			if (artistInfo.getName() == null || artistInfo.getName().equals(""))
 				name.setVisibility(View.GONE);
 			else
 				name.setText(getString(R.string.msg_label_name)
 						+ artistInfo.getName());
			
 			if (artistInfo.getGenre() == null
 					|| artistInfo.getGenre().equals(""))
 				genre.setVisibility(View.GONE);
 			else
 				genre.setText(getString(R.string.msg_label_genre)
 						+ artistInfo.getGenre());
			
 			if (artistInfo.getMember() == null
 					|| artistInfo.getMember().equals(""))
 				member.setVisibility(View.GONE);
 			else
 				member.setText(getString(R.string.msg_label_member)
 						+ artistInfo.getMember());
			
 			if (artistInfo.getCompany() == null
 					|| artistInfo.getCompany().equals(""))
 				company.setVisibility(View.GONE);
 			else
 				company.setText(getString(R.string.msg_label_company)
 						+ artistInfo.getCompany());
 
 		}
 	};
 
 	private void addRecentArtistItem() {
 		ContentValues values = new ContentValues();
 		values.put(ArtistItems.NAME, artistName);
 		values.put(ArtistItems.URL, artistUrl);
 		values.put(ArtistItems.IMAGE, artistImg);
 		values.put(ArtistItems.PLAY_ORDER,
 				RecentArtistProvider.getMax(this) + 1);
 		Log.d(TAG, "Adding artist item to db");
 		getContentResolver().insert(RecentArtistProvider.CONTENT_URI, values);
 	}
 
 	private void deleteOldArtistItem() {
 		String selection = ArtistItems.URL + " = ?";
 		String[] selectionArgs = new String[1];
 		selectionArgs[0] = artistUrl;
 		Log.d(TAG, "Deleting artist item to db");
 		getContentResolver().delete(RecentArtistProvider.CONTENT_URI,
 				selection, selectionArgs);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		artistName = getIntent().getStringExtra(Constants.EXTRA_ARTIST_NAME);
 		artistUrl = getIntent().getStringExtra(Constants.EXTRA_ARTIST_URL);
 		artistImg = getIntent().getStringExtra(Constants.EXTRA_ARTIST_IMG);
 
 		deleteOldArtistItem();
 		addRecentArtistItem();
 
 		super.onCreate(savedInstanceState);
 
 		dm = new ImageLoader(this);
 
 		ViewGroup container = (ViewGroup) findViewById(R.id.Content);
 		View.inflate(this, R.layout.items, container);
 
 		ListView listView = (ListView) findViewById(R.id.ListView01);
 		listView.setOnItemClickListener(this);
 		listAdapter = new TrackListAdapter(TrackListActivity.this);
 		listView.setAdapter(listAdapter);
 
 		ImageView image = (ImageView) findViewById(R.id.artistImage);
 		image.setTag(artistImg);
 		dm.DisplayImage(artistImg, this, image);
 
 		addArtistInfo();
 
 		addTracks();
 	}
 
 	private void addArtistInfo() {
 		new Thread() {
 			@Override
 			public void run() {
 				artistInfo = ArtistInfoFactory
 						.downloadArtist(Constants.ARTIST_INFO_API_URL + "?url="
 								+ artistUrl);
				handler.sendEmptyMessage(0);
 			}
 		}.start();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Track s = (Track) parent.getAdapter().getItem(position);
 		if (s != null)
 			playTrack(s, true);
 		else
 			refresh();
 	}
 
 	private void playTrack(Track track, boolean playNow) {
 		Log.d(TAG, "play now: " + track.getUrl());
 		PlaylistEntry entry = new PlaylistEntry(-1, track.getUrl(),
 				track.getName(), true, -1, new Artist(artistName, artistImg,
 						artistUrl));
 		if (playNow) {
 			this.listen(entry);
 		}
 
 		boolean start = false;
 		List<PlaylistEntry> entries = new ArrayList<PlaylistEntry>();
 		for (Track t : listAdapter.getTracklist()) {
 			if (t == track) {
 				start = true;
 				continue;
 			}
 			if (start && !existInPlaylist(t.getName(), true)) {
 				PlaylistEntry e = new PlaylistEntry(-1, t.getUrl(),
 						t.getName(), true, -1, new Artist(artistName,
 								artistImg, artistUrl));
 				entries.add(e);
 			}
 		}
 
 		if (entries.size() > 0)
 			this.addToPlayList(entries);
 
 		listAdapter.refresh();
 
 	}
 
 	@Override
 	public void refresh() {
 		listAdapter.clear();
 		addTracks();
 		addArtistInfo();
 	}
 
 	private boolean retrievePlaylistItem(String selection,
 			String[] selectionArgs, String sort) {
 		Cursor cursor = getContentResolver().query(
 				PlaylistProvider.CONTENT_URI, null, selection, selectionArgs,
 				sort);
 		if (cursor.moveToFirst()) {
 			cursor.close();
 			return true;
 		} else {
 			cursor.close();
 			return false;
 		}
 
 	}
 }
