 /* Copyright (C) 2013 David Li <li.davidm96@gmail.com>
 
    This file is part of Fiction Music.
 
    Fiction Music is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version.
 
    Fiction Music is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.
 
    You should have received a copy of the GNU General Public License along with
    Fiction Music.  If not, see <http://www.gnu.org/licenses/>. */
 
 
 package com.lithiumli.fiction;
 
 import android.app.Activity;
 import android.app.ActivityOptions;
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.PorterDuff;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.util.Log;
 
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.Request.Method;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.Volley;
 
 import com.lithiumli.fiction.ui.AlbumSwiper;
 import com.lithiumli.fiction.util.BitmapLruCache;
 
 public class NowPlayingActivity
     extends FictionActivity
 {
     static final String ECHO_NEST_URL = "http://developer.echonest.com/api/v4/artist/images?api_key=ETDSSZR6RAMYOU4SI&results=1&name=";
     TextView mSongName;
     TextView mSongAlbum;
     TextView mSongArtist;
     AlbumSwiper mCoverPager;
     RequestQueue mRequestQueue;
     ImageLoader mImageLoader;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.now_playing);
         initializeDrawer(false);
 
         mCoverPager = (AlbumSwiper) findViewById(R.id.cover_pager);
         mCoverPager.setListener(this);
 
         mSongName = (TextView) findViewById(R.id.np_song_name);
         mSongAlbum = (TextView) findViewById(R.id.np_song_album);
         mSongArtist = (TextView) findViewById(R.id.np_song_artist);
         mSongArtist.setSelected(true);
 
         ActionBar ab = getActionBar();
         ab.setDisplayHomeAsUpEnabled(true);
         ab.setTitle("Now Playing");
         ab.setSubtitle("Fiction Music");
 
         mRequestQueue = Volley.newRequestQueue(this);
         mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache());
 
         setQueueMargin();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             Intent parentIntent = new Intent(this, LibraryActivity.class);
             parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             ActivityOptions options =
                 ActivityOptions.makeCustomAnimation(this,
                                                     R.anim.activity_slide_down,
                                                     R.anim.activity_slide_up);
             startActivity(parentIntent, options.toBundle());
             finish();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         overridePendingTransition(R.anim.activity_slide_down, R.anim.activity_slide_up);
     }
 
     @Override
     public void onServiceConnected(PlaybackService service) {
         mCoverPager.setQueue(service.getQueue());
         mCoverPager.updateCovers();
     }
 
     @Override
     public void onSongChange(Song song) {
         mSongName.setText(song.getTitle());
         mSongAlbum.setText(song.getAlbum());
         mSongArtist.setText(song.getArtist());
 
         mCoverPager.updateCovers();
        mCoverPager.postInvalidate();
 
         if (song.getArtist().equals("<unknown>")) {
             ((ImageView) findViewById(R.id.background_image)).setImageDrawable(new ColorDrawable(0xFF000000));
             return;
         };
         String artist;
         try {
             artist = java.net.URLEncoder.encode(song.getArtist(), "UTF-8");
         }
         catch (java.io.UnsupportedEncodingException e) {
             return;
         }
 
         JsonObjectRequest req = new JsonObjectRequest(
             Method.GET,
             ECHO_NEST_URL + artist,
             null,
             new VolleyListener(),
             new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError error) {
                     Log.d("fiction", "error");
                 }
             });
         mRequestQueue.add(req);
     }
 
     @Override
     public void onPlayStateChange(PlaybackService.PlayState state) {
         ImageButton button = (ImageButton) findViewById(R.id.np_play_pause);
 
         switch (state) {
         case PLAYING:
             button.setImageResource(R.drawable.ic_menu_pause);
             break;
         case STOPPED:
             finish();
         case PAUSED:
             button.setImageResource(R.drawable.ic_menu_play);
             break;
         default:
             break;
         }
     }
 
     public void shuffleButton(View view) {
         ImageButton button = (ImageButton) view;
 
         if (isServiceBound()) {
             PlaybackService service = getService();
             PlaybackQueue queue = service.getQueue();
 
             if (queue.isShuffling()) {
                 button.setColorFilter(0xFFFFFFFF);
                 queue.restoreShuffle();
             }
             else {
                 button.setColorFilter(0xFF0099CC);
                 queue.shuffle();
             }
 
             mCoverPager.updateCovers();
         }
     }
 
     class VolleyListener implements Response.Listener<JSONObject> {
         @Override
             public void onResponse(JSONObject response) {
             try {
                 response = response.getJSONObject("response");
                 JSONArray images = response.getJSONArray("images");
 
                 if (images.length() > 0) {
                     JSONObject image = images.getJSONObject(0);
                     String url = image.getString("url");
                     mImageLoader.get(url, new VolleyImageListener());
                 }
             }
             catch (JSONException e) {
                 Log.d("fiction", "response error");
             }
         }
     }
 
     class VolleyImageListener implements ImageLoader.ImageListener {
         @Override
         public void onErrorResponse(VolleyError e) {
             // image load error
         }
 
         @Override
         public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
             if (response.getBitmap() != null) {
                 ((ImageView) NowPlayingActivity.this.findViewById(R.id.background_image)).setImageBitmap(response.getBitmap());
             }
             else {
                 ((ImageView)
         NowPlayingActivity.this.findViewById(R.id.background_image)).setImageDrawable(new
                                                                                       ColorDrawable(0xFF000000));
             }
         }
     }
 }
