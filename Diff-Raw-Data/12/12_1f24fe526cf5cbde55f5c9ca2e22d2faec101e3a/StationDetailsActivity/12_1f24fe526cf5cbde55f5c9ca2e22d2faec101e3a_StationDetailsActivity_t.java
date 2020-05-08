 // Copyright 2009 Google Inc.
 // Copyright 2011 NPR
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
 
 package org.npr.android.news;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import org.npr.android.util.DisplayUtils;
 import org.npr.android.util.FavoriteStationsProvider;
 import org.npr.android.util.StationCache;
 import org.npr.android.util.Tracker;
 import org.npr.android.util.Tracker.StationDetailsMeasurement;
 import org.npr.api.Station;
 import org.npr.api.Station.AudioStream;
 import org.npr.api.Station.Podcast;
 
 import java.util.ArrayList;
 
 public class StationDetailsActivity extends RootActivity implements
     AdapterView.OnItemClickListener {
   private static final String LOG_TAG = StationDetailsActivity.class.getName();
 
   private ArrayAdapter<ListItem> listAdapter;
 
   private String stationId;
   private Station station;
   private boolean isFavorite;
   
   private Handler m_uiHandler;
 
   private class ListItem {
     private final AudioStream stream;
     private final Podcast podcast;
     private final String header;
 
     private ListItem(AudioStream stream) {
       this.stream = stream;
       this.podcast = null;
       this.header = null;
     }
 
     private ListItem(Podcast podcast) {
       this.stream = null;
       this.podcast = podcast;
       this.header = null;
     }
 
     private ListItem(String header) {
       this.stream = null;
       this.podcast = null;
       this.header = header;
     }
 
     public boolean isStream() {
       return stream != null;
     }
 
     public boolean isPodcast() {
       return podcast != null;
     }
 
     public boolean isHeader() {
       return header != null;
     }
   }
 
   private class ListItemAdapter extends ArrayAdapter<ListItem> {
     public ListItemAdapter(Context context, ArrayList<ListItem> listItems) {
       super(context, R.layout.station_details_item, R.id.station_details_title,
           listItems);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       convertView = super.getView(position, convertView, parent);
       ListItem listItem = this.getItem(position);
       if (listItem == null) {
         return convertView;
       }
 
       ImageView favorite = (ImageView) convertView.findViewById(R.id
           .station_details_favorite);
       TextView title = (TextView) convertView.findViewById(R.id
           .station_details_title);
       TextView subtitle = (TextView) convertView.findViewById(R.id
           .station_details_subtitle);
       TextView tagline = (TextView) convertView.findViewById(R.id
           .station_details_tagline);
 
       if (position == 0) {
         convertView.setEnabled(false);
         convertView.setBackgroundDrawable(getResources().getDrawable(
             R.drawable.station_name_background));
         convertView.getLayoutParams().height = AbsListView.LayoutParams
             .WRAP_CONTENT;
         convertView.setPadding(10, 8, 10, 8);
         favorite.setVisibility(View.GONE);
         title.setText(Html.fromHtml(station.getName()));
         title.setTextColor(getResources().getColor(R.color.black));
         title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
         subtitle.setVisibility(View.VISIBLE);
         subtitle.setText(Html.fromHtml(
             new StringBuilder()
                 .append(station.getFrequency())
                 .append(" ")
                 .append(station.getBand())
                 .append(", ")
                 .append(station.getMarketCity())
                 .toString()
         ));
         subtitle.setTextColor(getResources().getColor(R.color.black));
         subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
         tagline.setVisibility(View.VISIBLE);
         tagline.setText(station.getTagline());
         tagline.setTextColor(getResources().getColor(R.color.black));
         tagline.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
       } else if (position == 1) {
         convertView.setEnabled(true);
         convertView.setBackgroundDrawable(getResources().getDrawable(
             R.drawable.station_favorite_background));
         convertView.getLayoutParams().height = AbsListView.LayoutParams
             .WRAP_CONTENT;
         convertView.setPadding(10, 8, 10, 8);
         favorite.setVisibility(View.VISIBLE);
         if (isFavorite) {
           favorite.setImageResource(R.drawable.heart_normal_selected);
           title.setText(getString(R.string.msg_station_remove_favorite));
         } else {
           favorite.setImageResource(R.drawable.heart_normal);
           title.setText(getString(R.string.msg_station_add_favorite));
         }
         title.setTextColor(getResources().getColor(R.color.black));
         title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
         subtitle.setVisibility(View.GONE);
         tagline.setVisibility(View.GONE);
       } else if (listItem.isHeader()) {
         convertView.setEnabled(false);
         convertView.setBackgroundDrawable(getResources().getDrawable(
             R.drawable.top_stories_title_background));
         convertView.getLayoutParams().height =
             DisplayUtils.convertToDIP(getContext(), 20);
         convertView.setPadding(10, 0, 10, 0);
         favorite.setVisibility(View.GONE);
         title.setText(listItem.header);
         title.setTextColor(getResources().getColor(R.color.news_title_text));
         title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
         subtitle.setVisibility(View.GONE);
         tagline.setVisibility(View.GONE);
       } else {
         convertView.setEnabled(true);
         convertView.setBackgroundDrawable(null);
         convertView.getLayoutParams().height = AbsListView.LayoutParams
             .WRAP_CONTENT;
         convertView.setPadding(10, 18, 10, 18);
         favorite.setVisibility(View.GONE);
         if (listItem.isStream()) {
           title.setText(Html.fromHtml(listItem.stream.getTitle()));
         } else if (listItem.isPodcast()) {
           title.setText(Html.fromHtml(listItem.podcast.getTitle()));
         }
         title.setTextColor(getResources().getColor(R.color.black));
         title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
         subtitle.setVisibility(View.GONE);
         tagline.setVisibility(View.GONE);
       }
       return convertView;
     }
   }
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     if (getIntent().hasExtra(Constants.EXTRA_STATION_ID)) {
       stationId = getIntent().getStringExtra(Constants.EXTRA_STATION_ID);
     } else if (getIntent().hasExtra(Constants.EXTRA_ACTIVITY_DATA)) {
       stationId = getIntent().getStringExtra(Constants.EXTRA_ACTIVITY_DATA);
     }
 
     m_uiHandler = new Handler();
     
     new Thread() {
       @Override
       public void run()
       {
         setupStation();
       }
     }.start();
   }
   
   private void setupStation()
   {
     station = StationCache.getStation(stationId);
 
     String selection = FavoriteStationsProvider.Items.STATION_ID + " = ?";
     String[] selectionArgs = new String[1];
     selectionArgs[0] = stationId;
     Cursor c = getContentResolver().query(
         FavoriteStationsProvider.CONTENT_URI,
         null, selection, selectionArgs, null);
 
     isFavorite = c.moveToFirst();
     c.close();
 
     if (station == null) {
       Log.d(LOG_TAG, "Couldn't get station. Notifying user.");
       Toast.makeText(this, R.string.msg_station_not_found, Toast.LENGTH_SHORT);
       if (isFavorite) {
         getContentResolver().delete(
             FavoriteStationsProvider.CONTENT_URI,
             selection,
             selectionArgs
         );
       }
       finish();
       return;
     }
     
     m_uiHandler.post( new Runnable() {
       @Override
       public void run() {
         ViewGroup container = (ViewGroup) findViewById(R.id.TitleContent);
         ViewGroup.inflate(StationDetailsActivity.this, R.layout.station_details, container);
 
         ArrayList<ListItem> listItems = new ArrayList<ListItem>();
         listItems.add(new ListItem(station.getName()));
         listItems.add(new ListItem("Favorites")); // string is a placeholder
         if (station.getAudioStreams().size() > 0) {
           listItems.add(new ListItem(getString(R.string.msg_station_streams) +
               " (" + station.getAudioStreams().size() + ")"));
           for (AudioStream stream : station.getAudioStreams()) {
             if (stream.getTitle() == null) {
               listItems.add(new ListItem(new AudioStream(
                   stream.getUrl(),
                   String.format(getString(R.string.format_default_station_name),
                       station.getName()
                       )
               )));
             } else {
               listItems.add(new ListItem(stream));
             }
           }
         }
         if (station.getPodcasts().size() > 0) {
           listItems.add(new ListItem(getString(R.string.msg_station_podcasts) +
               " (" + station.getPodcasts().size() + ")"));
           for (Podcast podcast : station.getPodcasts()) {
             listItems.add(new ListItem(podcast));
           }
         }
 
         ListView streamList = (ListView) findViewById(R.id.station_details_list);
         streamList.setOnItemClickListener(StationDetailsActivity.this);
 
         listAdapter = new ListItemAdapter(StationDetailsActivity.this, listItems);
         streamList.setAdapter(listAdapter);
        
        bringPlayerNavToFront();
       }
     });
   }
 
   @Override
   public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
     ListItem li = (ListItem) adapterView.getItemAtPosition(i);
     if (i == 1) {
       isFavorite = !isFavorite;
       if (isFavorite) {
         ContentValues values = new ContentValues();
         values.put(FavoriteStationsProvider.Items.NAME, station.getName());
         values.put(FavoriteStationsProvider.Items.MARKET,
             station.getMarketCity());
         values.put(FavoriteStationsProvider.Items.FREQUENCY,
             station.getFrequency());
         values.put(FavoriteStationsProvider.Items.BAND, station.getBand());
         values.put(FavoriteStationsProvider.Items.STATION_ID, stationId);
         getContentResolver().insert(
             FavoriteStationsProvider.CONTENT_URI,
             values
         );
       } else {
         String selection = FavoriteStationsProvider.Items.STATION_ID + " = ?";
         String[] selectionArgs = new String[1];
         selectionArgs[0] = stationId;
         getContentResolver().delete(
             FavoriteStationsProvider.CONTENT_URI,
             selection,
             selectionArgs
         );
       }
       listAdapter.notifyDataSetChanged();
     } else if (li.isStream()) {
       playSingleNow(Playable.PlayableFactory.fromStationStream(station.getId(),
           li.stream));
     } else if (li.isPodcast()) {
       Intent intent = new Intent(this, PodcastActivity.class).putExtra(
           PodcastActivity.EXTRA_PODCAST_TITLE, li.podcast.getTitle()).putExtra(
           PodcastActivity.EXTRA_PODCAST_URL, li.podcast.getUrl());
       startActivity(intent);
     }
   }
 
   @Override
   public void trackNow() {
     StringBuilder pageName = new StringBuilder("Stations").append(Tracker.PAGE_NAME_SEPARATOR);
     pageName.append(station == null ? "" : station.getName());
     Tracker.instance(getApplication()).trackPage(
         new StationDetailsMeasurement(pageName.toString(), "Stations",
             stationId));
   }
 }
