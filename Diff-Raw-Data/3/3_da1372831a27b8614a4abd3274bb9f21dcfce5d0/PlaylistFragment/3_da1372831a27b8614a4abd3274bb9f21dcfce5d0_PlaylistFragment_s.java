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
     
     private final int SHORT_VIEW = 1;
     private final int EXPANDED_VIEW = 10;
     
     public PlaylistFragment(){
         for(SongMetadata s : metadataList){
            s.setTitle("This is a really really really looooooooooooooooooooooooooooooooooooooooooooooong Title");
             s.setAlbum("Album is super freaking long too");
             s.setArtist("Artist is too, but not as much");
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
     
     @Override
     public void onResume(){
         super.onResume();
         getActivity().setTitle(getTitle());
     }
     
     
 }
