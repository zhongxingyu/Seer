 package com.lastcrusade.fanclub.components;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.lastcrusade.fanclub.CustomApp;
 import com.lastcrusade.fanclub.R;
 import com.lastcrusade.fanclub.model.Playlist;
 import com.lastcrusade.fanclub.model.SongMetadata;
 import com.lastcrusade.fanclub.service.PlaylistService;
 import com.lastcrusade.fanclub.service.ServiceLocator;
 import com.lastcrusade.fanclub.service.ServiceNotBoundException;
 import com.lastcrusade.fanclub.util.BroadcastRegistrar;
 import com.lastcrusade.fanclub.util.IBroadcastActionHandler;
 import com.lastcrusade.fanclub.util.ITitleable;
 import com.lastcrusade.fanclub.util.MusicListAdapter;
 
 public class PlaylistFragment extends SherlockListFragment implements ITitleable{
     //for testing purposes so we have songs to show
     private final String TAG = PlaylistFragment.class.getName();
 
     private BroadcastRegistrar registrar;
     private Playlist metadataList;
 
     private ServiceLocator<PlaylistService> playlistServiceServiceLocator;
 
     private final int SHORT_VIEW = 1;
     private final int EXPANDED_VIEW = 10;
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         playlistServiceServiceLocator = new ServiceLocator<PlaylistService>(
                 this.getActivity(), PlaylistService.class, PlaylistService.PlaylistServiceBinder.class);
 
         //TODO: get the userlist better
         final CustomApp curApp = (CustomApp) this.getActivity().getApplication();
 
         playlistServiceServiceLocator.setOnBindListener(new ServiceLocator.IOnBindListener() {
             @Override
             public void onServiceBound() {
                 metadataList = getPlaylistService().getPlaylist();
                 setListAdapter(new MusicListAdapter(PlaylistFragment.this.getActivity(), metadataList.getList(), curApp.getUserList().getUsers()));
             }
         });
 
         registerReceivers();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View v = inflater.inflate(R.layout.list, container, false);
         return v;
     }
 
     @Override
     public void onResume(){
         super.onResume();
         getActivity().setTitle(getTitle());
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         unregisterReceivers();
     }
 
     @Override
     public void onListItemClick(ListView lv, View v, int position, long id){
         toggleViewSize(v);
     }
     
     private void toggleViewSize(View v){
         TextView title = (TextView)v.findViewById(R.id.title); 
         TextView album = (TextView)v.findViewById(R.id.album);
         TextView artist = (TextView)v.findViewById(R.id.artist);
         
         //if the view height is larger than the standard element, set it back to the standard
         if(v.getHeight()>getResources().getDimension(R.dimen.song_height)){
             title.setMaxLines(SHORT_VIEW);
             album.setMaxLines(SHORT_VIEW);
             artist.setMaxLines(SHORT_VIEW);
             
             //set the height of the color bar to the standard song element height
             v.findViewById(R.id.user_color).setMinimumHeight((int) getResources().getDimension(R.dimen.song_height));
         }
         //otherwise, expand the view
         else{
             title.setMaxLines(EXPANDED_VIEW);
             album.setMaxLines(EXPANDED_VIEW);
             artist.setMaxLines(EXPANDED_VIEW);
             
             //get the additional height taken up by the expanded words
             int titleHeight = (title.getLineCount()-1)*title.getLineHeight();
             int artistHeight =  (artist.getLineCount()-1)*artist.getLineHeight();
             int albumHeight = (album.getLineCount()-1)*album.getLineHeight();
             
             //calculate the total height of the expanded view
             int viewHeight = (int) getResources().getDimension(R.dimen.song_height)
                     + titleHeight + artistHeight + albumHeight;
             
             //set the height of the color bar to the new view height
             v.findViewById(R.id.user_color).setMinimumHeight(viewHeight);
         }  
     }
 
     @Override
     public String getTitle() {
         return getString(R.string.playlist);
     }
 
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar.addAction(PlaylistService.ACTION_PLAYLIST_UPDATED, new IBroadcastActionHandler() {
             @Override
             public void onReceiveAction(Context context, Intent intent) {
                 Log.i(PlaylistFragment.class.getName(), "action playlist updated");
                 setListAdapter(new MusicListAdapter(context, getPlaylist().getList(), null));
             }
         }).register(this.getActivity());
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
 
     private Playlist getPlaylist() {
        Playlist playlist = null;

         try {
            playlist = playlistServiceServiceLocator.getService().getPlaylist();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }

        return playlist;
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
 }
