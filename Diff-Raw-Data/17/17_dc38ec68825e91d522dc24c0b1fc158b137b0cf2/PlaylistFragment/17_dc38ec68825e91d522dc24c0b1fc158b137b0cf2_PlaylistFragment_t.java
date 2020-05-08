 package com.lastcrusade.fanclub.components;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.lastcrusade.fanclub.CustomApp;
 import com.lastcrusade.fanclub.R;
 import com.lastcrusade.fanclub.model.SongMetadata;
 import com.lastcrusade.fanclub.util.ITitleable;
 import com.lastcrusade.fanclub.util.MusicListAdapter;
 
 public class PlaylistFragment extends SherlockListFragment implements ITitleable{
     //for testing purposes so we have songs to show
     static List<SongMetadata> metadataList = new ArrayList<SongMetadata>(
             Arrays.asList(new SongMetadata(), new SongMetadata(), new SongMetadata()));
     
     public PlaylistFragment(){
         for(SongMetadata s : metadataList){
             s.setTitle("This is a really really really looooooooooooooooooooooooooooooooooooooooooooooong Title");
             s.setAlbum("Album");
             s.setArtist("Artist");
          }
          metadataList.get(0).setUsername("Reid");
          metadataList.get(1).setUsername("Lizziemom");
          metadataList.get(2).setUsername("Greenie");
         
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         CustomApp curApp = (CustomApp) this.getActivity().getApplication();
         setListAdapter(new MusicListAdapter(this.getActivity(), metadataList, curApp.getUserList().getUsers()));
     }    
 
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View v = inflater.inflate(R.layout.list, container, false);
         return v;
     }
     
     @Override
     public void onListItemClick(ListView lv, View v, int position, long id){
         toggleViewSize(v);
     }
     
     private void toggleViewSize(View v){
         TextView title = (TextView)v.findViewById(R.id.title); 
        
         if(v.getHeight()>getResources().getDimension(R.dimen.song_height)){
             title.setMaxLines(1);
            v.findViewById(R.id.user_color).setMinimumHeight((int) getResources().getDimension(R.dimen.song_height));
            
         }
         else{
             title.setMaxLines(10);
            v.findViewById(R.id.user_color).setMinimumHeight((int) getResources().getDimension(R.dimen.song_height));
         }
        
        
        
     }
 
     @Override
     public String getTitle() {
         return getString(R.string.playlist);
     }
     
     @Override
     public void onResume(){
         super.onResume();
         getActivity().setTitle(getTitle());
     }
     
     
 }
