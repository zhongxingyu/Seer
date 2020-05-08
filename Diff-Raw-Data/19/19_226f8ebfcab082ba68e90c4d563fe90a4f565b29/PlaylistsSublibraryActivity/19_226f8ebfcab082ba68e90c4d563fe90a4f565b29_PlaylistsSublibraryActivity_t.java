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
 
 import android.app.ActionBar;
 import android.app.LoaderManager;
 import android.os.Bundle;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.lithiumli.fiction.fragments.*;
 
 public class PlaylistsSublibraryActivity
     extends SublibraryActivity
     implements LoaderManager.LoaderCallbacks<Cursor>,
                AdapterView.OnItemClickListener
 {
     public static final String DATA_URI = "com.lithiumli.fiction.PLAYLIST_URI";
 
     static final String[] PROJECTION = new String[] {
             MediaStore.Audio.Playlists.Members._ID,
             MediaStore.Audio.Playlists.Members.TITLE,
             MediaStore.Audio.Playlists.Members.ARTIST,
             MediaStore.Audio.Playlists.Members.ALBUM,
             MediaStore.Audio.Playlists.Members.ALBUM_ID,
             MediaStore.Audio.Playlists.Members.AUDIO_ID,
             MediaStore.Audio.Playlists.Members.DURATION,
             MediaStore.Audio.Playlists.Members.PLAY_ORDER
     };
 
     ListView mListView;
     View mControls;
     FictionCursorAdapter mAdapter;
     Uri mUri;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.playlists);
         initializeDrawer(false);
         initializeBottomActionBar();
 
         mUri = getIntent().getParcelableExtra(DATA_URI);
 
         ActionBar ab = getActionBar();
         ab.setDisplayHomeAsUpEnabled(true);
         ab.setTitle("Playlist");
 
         LayoutInflater inflater = (LayoutInflater)
             getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mListView = (ListView) findViewById(R.id.playlist_list);
         mListView.setOnItemClickListener(this);
 
         getLoaderManager().initLoader(0, null, this);
 
         if (mListView.getAdapter() == null) {
             mAdapter = new FictionCursorAdapter(this, null, 0) {
                 @Override
                 public void bindView(View view, Context context, Cursor cursor) {
                     TextView title = (TextView) view.findViewById(R.id.title_text);
                     TextView sub = (TextView) view.findViewById(R.id.sub_text);
 
                     String songTitle =
                             cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                     String songArtist =
                             cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                     String songAlbum =
                             cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM));
 
                     title.setText(songTitle);
 
                     sub.setText(songArtist + " — " + songAlbum);
                 }
             };
             mListView.setAdapter(mAdapter);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.playlist, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean drawerOpen = isDrawerOpen();
         menu.findItem(R.id.play_all).setVisible(!drawerOpen);
         menu.findItem(R.id.enqueue_all).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.play_all:
             if (isServiceBound()) {
                 PlaybackService service = getService();
                 PlaybackQueue queue = service.getQueue();
 
                 queue.setContext(PlaybackQueue.QueueContext.PLAYLIST,
                                  mAdapter.getCursor());
                 service.play(0);
             }
             return true;
         case R.id.enqueue_all:
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     public void onItemClick(AdapterView<?> parent, View view, int position, long
                             id) {
         position -= 1; // compensate for header view
         if (isServiceBound()) {
             PlaybackService service = getService();
             PlaybackQueue queue = service.getQueue();
 
             // TODO also set context if different playlist
             // QueueContext needs to hold context info about which playlist,
             // etc, not just the type of queue
             if (queue.getContext() != PlaybackQueue.QueueContext.PLAYLIST) {
                 queue.setContext(PlaybackQueue.QueueContext.PLAYLIST,
                                  mAdapter.getCursor());
             }

            // TODO if shuffling, unshuffle, play, then reshuffle
             service.play(position);
         }
     }
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new CursorLoader(this, mUri,
                 PROJECTION,
                 "", null,
                 MediaStore.Audio.Playlists.Members.PLAY_ORDER);
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         mAdapter.swapCursor(data);
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.swapCursor(null);
     }
 }
