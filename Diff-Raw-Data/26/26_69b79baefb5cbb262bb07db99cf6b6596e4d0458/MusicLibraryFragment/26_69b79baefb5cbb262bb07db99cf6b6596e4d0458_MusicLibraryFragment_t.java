 package com.lastcrusade.fanclub.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 
 import com.lastcrusade.fanclub.CustomApp;
 import com.lastcrusade.fanclub.R;
 import com.lastcrusade.fanclub.model.SongMetadata;
 import com.lastcrusade.fanclub.model.UserList;
 import com.lastcrusade.fanclub.service.MusicLibraryService;
 import com.lastcrusade.fanclub.service.MusicLibraryService.MusicLibraryServiceBinder;
 import com.lastcrusade.fanclub.service.PlaylistService;
 import com.lastcrusade.fanclub.service.PlaylistService.PlaylistServiceBinder;
 import com.lastcrusade.fanclub.service.ServiceLocator;
 import com.lastcrusade.fanclub.service.ServiceNotBoundException;
 import com.lastcrusade.fanclub.util.BroadcastRegistrar;
 import com.lastcrusade.fanclub.util.IBroadcastActionHandler;
 import com.lastcrusade.fanclub.util.MusicListAdapter;
 
 public class MusicLibraryFragment extends MusicListFragment {
     private final String TAG = MusicLibraryFragment.class.getName();
     private BroadcastRegistrar registrar;
 
     private ServiceLocator<PlaylistService> playlistServiceLocator;
 
     private MusicLibraryService mMusicLibraryService;
     private boolean boundToService = false; //Since you cannot instantly bind, set a boolean
                                     // after its safe to call methods
     
    private MusicAdapter mMusicAdapter;
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection musicLibraryConn = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             MusicLibraryServiceBinder binder = (MusicLibraryServiceBinder) service;
             
             mMusicLibraryService = binder.getService();
             boundToService = true;
             
             //update displayed music
            mMusicAdapter.updateMusicFromLibrary();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundToService = false;
         }
     };
 
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         playlistServiceLocator = new ServiceLocator<PlaylistService>(MusicLibraryFragment.this.getActivity(),
                 PlaylistService.class, PlaylistServiceBinder.class);
 
         if(boundToService == false){
             Intent intentML = new Intent(this.getActivity(), MusicLibraryService.class);
             this.getActivity().bindService(intentML, musicLibraryConn, Context.BIND_AUTO_CREATE);
         }
         
         
         UserList users = ((CustomApp) this.getActivity().getApplication()).getUserList();
         //make a new music list adapter and give it an empty list of songs to use until the service is connected
        mMusicAdapter = new MusicAdapter(this.getActivity(), new ArrayList<SongMetadata>() , users);
        setListAdapter(mMusicAdapter);
         
         registerReceivers();
     }
     
         
     @Override
     public void onDestroy() {
         super.onDestroy();
         // Unbind from the MusicLibrary service
         if (boundToService) {
             this.getActivity().unbindService(musicLibraryConn);
             boundToService = false;
         }
         unregisterReceivers();
     }
 
     @Override
     public int getTitle() {
         return R.string.music_library;
     }
     
     /**
      * Register intent receivers to control this service
      *
      */
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar.addAction(MusicLibraryService.ACTION_LIBRARY_UPDATED, new IBroadcastActionHandler() {
 
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 //Update library shown when the library service gets an update
                mMusicAdapter.updateMusicFromLibrary();
             }
         }).register(this.getActivity());
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
     
 
     protected PlaylistService getPlaylistService() {
         PlaylistService playlistService = null;
 
         try {
             playlistService = this.playlistServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return playlistService;
     }
 
     /**
      * Inner class extends MusicListAdapter to add the add to playlist image button, and click listener
      * 
      */
     private class MusicAdapter extends MusicListAdapter {
         public MusicAdapter(
                 Context mContext,
                 List<SongMetadata> metadataList,
                 UserList users
                 ) {
             super(mContext, metadataList, users);
         }
         
         public View getView(int position, View convertView, ViewGroup parent){
             View v = super.getView(position, convertView, parent);
             ImageButton imageButton = (ImageButton) v.findViewById(R.id.add_to_playlist);
             imageButton.setVisibility(View.VISIBLE);
 
             imageButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     SongMetadata meta = getItem((Integer) v.getTag()); //mMusicLibraryService.getLibrary().get((Integer) v.getTag());
                     getPlaylistService().addSong(meta);
                 }
             });
 
             return v;
         }
 
         /**
          * Update the list with music from the library
          */
         private void updateMusicFromLibrary() {
             this.updateMusic(new ArrayList<SongMetadata>(mMusicLibraryService.getLibrary()));
         }
     }
 }
