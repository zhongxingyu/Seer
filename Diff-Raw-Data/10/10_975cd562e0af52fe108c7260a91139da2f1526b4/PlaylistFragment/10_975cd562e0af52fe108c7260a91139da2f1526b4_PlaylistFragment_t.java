 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.thelastcrusade.soundstream.components;
 
 import java.util.Collections;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.GestureDetectorCompat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.TranslateAnimation;
 import android.widget.ListView;
 
 import com.thelastcrusade.soundstream.CoreActivity;
 import com.thelastcrusade.soundstream.R;
 import com.thelastcrusade.soundstream.model.PlaylistEntry;
 import com.thelastcrusade.soundstream.model.SongMetadata;
 import com.thelastcrusade.soundstream.model.UserList;
 import com.thelastcrusade.soundstream.service.PlaylistService;
 import com.thelastcrusade.soundstream.service.ServiceLocator;
 import com.thelastcrusade.soundstream.service.ServiceLocator.IOnBindListener;
 import com.thelastcrusade.soundstream.service.ServiceNotBoundException;
 import com.thelastcrusade.soundstream.service.UserListService;
 import com.thelastcrusade.soundstream.util.BroadcastRegistrar;
 import com.thelastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.thelastcrusade.soundstream.util.MusicListAdapter;
 import com.thelastcrusade.soundstream.util.SongGestureListener;
 import com.thelastcrusade.soundstream.util.Toaster;
 
 public class PlaylistFragment extends MusicListFragment{
     //for testing purposes so we have songs to show
     private final String TAG = PlaylistFragment.class.getSimpleName();
 
     private BroadcastRegistrar registrar;
 
     private ServiceLocator<PlaylistService> playlistServiceServiceLocator;
     private ServiceLocator<UserListService> userListServiceLocator;
 
     private PlayListAdapter mPlayListAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         mPlayListAdapter = new PlayListAdapter(
                 this.getActivity(),
                 Collections.<PlaylistEntry> emptyList(),
                 new UserList()
         );
 
         playlistServiceServiceLocator = new ServiceLocator<PlaylistService>(
                 this.getActivity(),
                 PlaylistService.class,
                 PlaylistService.PlaylistServiceBinder.class
         );
 
         playlistServiceServiceLocator.setOnBindListener(new ServiceLocator.IOnBindListener() {
             @Override
             public void onServiceBound() {
                 updatePlaylist();
             }
         });
 
         userListServiceLocator = new ServiceLocator<UserListService>(
                 this.getActivity(), UserListService.class, UserListService.UserListServiceBinder.class);
         userListServiceLocator.setOnBindListener(new IOnBindListener() {
             @Override
             public void onServiceBound() {
                 mPlayListAdapter.updateUsers(getUserListFromService());
             }
         });
 
         registerReceivers();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View v = inflater.inflate(R.layout.list, container, false);
         setListAdapter(mPlayListAdapter);  
         return v;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         ((CoreActivity)getActivity()).getTracker().sendView(TAG);
     }
 
     @Override
     public void onResume(){
         super.onResume();
         getActivity().setTitle(getTitle());
         
         final GestureDetectorCompat songGesture = new GestureDetectorCompat(getActivity(), 
                 new PlaylistSongGestureListener(getListView()));
         
         getListView().setOnTouchListener(new View.OnTouchListener() {       
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if(songGesture.onTouchEvent(event)){
                     if(event.getAction() != MotionEvent.ACTION_DOWN){
                         MotionEvent cancelEvent = MotionEvent.obtain(event);
                         cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                         v.onTouchEvent(cancelEvent);
                     }
                     return true;
                 }
                 return false;
             }
         });
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         playlistServiceServiceLocator.unbind();
         userListServiceLocator.unbind();
         unregisterReceivers();
     }
 
     @Override
     public int getTitle() {
         return R.string.playlist;
     }
 
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar.addLocalAction(PlaylistService.ACTION_PLAYLIST_UPDATED, new IBroadcastActionHandler() {
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 updatePlaylist();
             }
         })
         .addLocalAction(UserList.ACTION_USER_LIST_UPDATE, new IBroadcastActionHandler() {
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 updatePlaylist();
             }
         })
         .addLocalAction(PlaylistService.ACTION_PLAYING_AUDIO, new IBroadcastActionHandler() {
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 //when the playlist starts playing a song, we want to make sure that we are
                 // showing the correct song being played, so we tell the adapter to update
                 // the playlist to force a redraw of the views
                 updatePlaylist();
             }
         })
         .addLocalAction(PlaylistService.ACTION_SONG_REMOVED, new IBroadcastActionHandler() {
             
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 SongMetadata entry = intent.getParcelableExtra(PlaylistService.EXTRA_SONG);
                 //for now we are just toasting, but eventually this might change to something that 
                 //allows the user to undo the action
                 //Toaster.iToast(getActivity(), getString(R.string.removed_label) + "\"" + entry.getTitle() + "\"");
                 
                 //commenting out the toast, but leaving this here for now so that if we want to 
                 //go back in and add some kind of pop up menu to undo the removal we have a 
                 //place to do so
             }
         })
         .addLocalAction(PlaylistService.ACTION_CURRENT_SONG, new IBroadcastActionHandler() {
 
 			@Override
 			public void onReceiveAction(Context context, Intent intent) {
 				//the current song has changed...notify the adapter that we must
 				// rerender the rows
 				mPlayListAdapter.notifyDataSetChanged();
 			}
         	
         })
         .register(this.getActivity());
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
 
     protected PlaylistService getPlaylistService() {
         PlaylistService playlistService = null;
 
         try {
             playlistService = this.playlistServiceServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return playlistService;
     }
 
     private void updatePlaylist() {
         mPlayListAdapter.updateMusic(getPlaylistService().getPlaylistEntries());
     }
 
     private UserList getUserListFromService(){
         UserList activeUsers = new UserList();
         try {
             activeUsers = userListServiceLocator.getService().getUserList();
         } catch (ServiceNotBoundException e) {
             Log.w(TAG, "UserListService not bound");
         }
         return activeUsers;
     }
 
     private class PlayListAdapter extends MusicListAdapter<PlaylistEntry> {
         public PlayListAdapter(
                 Context mContext,
                 List<PlaylistEntry> playlistEntries,
                 UserList users
                 ) {
             super(mContext, playlistEntries, users);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View element = super.getView(position, convertView, parent);
             
             //This depends on played music being above unplayed music
             PlaylistEntry entry = super.getItem(position);
             
             if (!entry.isLoaded()) {
                 //TODO: style the unloaded elements here
                 element.setBackgroundColor(getResources().getColor(R.color.bright_foreground_disabled_holo_light));
                 element.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                 element.findViewById(R.id.now_playing).setVisibility(View.INVISIBLE);
             } 
             else if (entry.isPlayed()) {
                 element.setBackgroundColor(getResources().getColor(R.color.used));
                 element.findViewById(R.id.now_playing).setVisibility(View.INVISIBLE);
             } 
             else {
                element.setBackgroundColor(getResources().getColor(R.color.holo_light));
                 element.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                 if(entry.equals(getPlaylistService().getCurrentEntry())){
                     element.findViewById(R.id.now_playing).setVisibility(View.VISIBLE);
                 }
                 else{
                     element.findViewById(R.id.now_playing).setVisibility(View.INVISIBLE);
                 }
             }
             
             return element;
         }
         
         /* (non-Javadoc)
          * @see com.thelastcrusade.soundstream.util.MusicListAdapter#updateMusic(java.util.List)
          */
         @Override
         public void updateMusic(List<PlaylistEntry> metadataList) {
             super.updateMusic(metadataList);
             if(metadataList.size() == 0){
                 Toaster.iToast(getActivity(),R.string.no_songs_in_playlist);
             }
         }
     }
     
   //detect gestures 
     private class PlaylistSongGestureListener extends SongGestureListener{
         private final int SWIPE_MIN_DISTANCE = 200;
         
         public PlaylistSongGestureListener(ListView view){
             super(view);   
         }
         
         //fling a song to the right to remove it
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                 float velocityY) {
             boolean swipe = false;
             // Fling is what the gesture detector detects
             // Swipe is our internal vocabulary for a 
             // horizontal left to right fling
             if(selectedIndex != -1 && isSwipe(e1, e2, velocityX, velocityY)){
                 animateDragging((int)e2.getX());
 
                 getPlaylistService().removeSong(getSelectedEntry());
                 selectedIndex = -1;
                 swipe=true;
             }
                 
             return swipe;
         }
         
         //allows the view to be moved horizontally
         @Override
         public boolean onScroll(MotionEvent e1, MotionEvent e2,
             float distanceX, float distanceY) {
             float dx = e2.getX() - e1.getX();
             if(selectedIndex != -1 && Math.abs(distanceX) > Math.abs(distanceY)){
                 
                 animateDragging(dx);
                 
                 if( (e2.getX() > ((View)getSelectedView().getParent()).getWidth()-100 ||
                         e2.getX() < 100) && Math.abs(dx) > 100){
                     getPlaylistService().removeSong(getSelectedEntry());
                     selectedIndex = -1;
                 }
                 return true;
             }
             return false;
         }
         
         //bump the song to the top when double tapped
         @Override
         public boolean onDoubleTap(MotionEvent e) {
             if(selectedIndex != -1){
                 getPlaylistService().bumpSong(getSelectedEntry());
                 selectedIndex = -1;
             }
             return true;
         } 
         
         /**
          * Animates the current view by moving it to the right by the given 
          * amount
          * 
          * @param amount
          */
         private void animateDragging(float amount){
                 TranslateAnimation trans = new TranslateAnimation(amount, amount, 0,0);
                 trans.setDuration(100);
                 View view = getSelectedView();
                 if(view != null){
                     trans.initialize(view.getWidth(), view.getHeight(), 
                             ((View)view.getParent()).getWidth(), ((View)view.getParent()).getHeight());
                     
                     view.startAnimation(trans);
                 }
         }
         
         
         /**
          * Checks to see if the fling motion described by these inputs matches
          * our definition of a left to right swipe
          * 
          * @param e1
          * @param e2
          * @param velocityX
          * @param velocityY
          * @return
          */
         private boolean isSwipe(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
             float dx = e2.getX() - e1.getX();
             
             if(dx > SWIPE_MIN_DISTANCE && velocityX > velocityY){
                 return true;
             }
             return false;  
         }
         
         private PlaylistEntry getSelectedEntry(){
             return (PlaylistEntry)musicView.getAdapter().getItem(selectedIndex);
         }
     } 
 
 }
