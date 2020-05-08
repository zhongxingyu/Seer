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
 
 import java.util.List;
 
 import org.dbartists.api.Artist;
 import org.dbartists.api.ArtistFactory;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ArtistsListAdapter extends ArrayAdapter<Artist> {
 	private static final String LOG_TAG = ArtistsListAdapter.class.getName();
 	private LayoutInflater inflater;
	private static Typeface headlineTypeface = null;
 
 	private final static int MSG_ARTISTS_LOADED = 0;
 
 	private ImageLoader dm;
 
 	private List<Artist> moreArtists;
 
 	private boolean endReached = false;
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_ARTISTS_LOADED:
 				if (moreArtists != null) {
 					remove(null);
 					for (Artist t : moreArtists) {
 						if (getPosition(t) < 0) {
 							add(t);
 						}
 					}
 					if (!endReached) {
 						add(null);
 					}
 				}
 				break;
 			}
 		}
 	};
 
 	public ArtistsListAdapter(Context context) {
 		super(context, R.layout.artist_item);
 		inflater = LayoutInflater.from(getContext());
 		dm = new ImageLoader(context);
 	}
 
 	public void addMoreArtists(final String url, final int startId) {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				getMoreArtists(url, startId);
 				handler.sendEmptyMessage(MSG_ARTISTS_LOADED);
 			}
 		}).start();
 	}
 
 	private void getMoreArtists(String url, int startId) {
		moreArtists = ArtistFactory.downloadArtists(url, startId);
		if (moreArtists == null || moreArtists.size() < 20) {
 			endReached = true;
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = inflater.inflate(R.layout.artist_item, parent, false);
 		}
 
 		final Artist artist = getItem(position);
 
 		final ImageView image = (ImageView) convertView
 				.findViewById(R.id.artistItemImage);
 		final TextView name = (TextView) convertView
 				.findViewById(R.id.artimstItemName);
 		final ImageView arrow = (ImageView) convertView
 				.findViewById(R.id.arrow);
 
 		ProgressBar titleProgressBar;
 		titleProgressBar = (ProgressBar) parent.getRootView().findViewById(
 				R.id.leadProgressBar);
 		// hide the progress bar if it is not needed
 		titleProgressBar.setVisibility(View.GONE);
 
 		if (artist != null) {
 
 			image.setTag(artist.getImg());
 			dm.DisplayImage(artist.getImg(),
 					(Activity) convertView.getContext(), image);
 
 			name.setText(artist.getName());
 
 		} else {
 			// null marker means it's the end of the list.
			image.setVisibility(View.GONE);
 			name.setText(R.string.msg_load_more);
 			arrow.setVisibility(View.GONE);
 		}
 		return convertView;
 	}
 }
