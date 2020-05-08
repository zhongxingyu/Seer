 package com.lastcrusade.soundstream.util;
 
 import android.support.v4.app.Fragment;
 
 import com.lastcrusade.soundstream.CoreActivity;
 import com.lastcrusade.soundstream.R;
 import com.lastcrusade.soundstream.components.ConnectFragment;
 import com.lastcrusade.soundstream.components.MusicLibraryFragment;
 import com.lastcrusade.soundstream.components.NetworkFragment;
 import com.lastcrusade.soundstream.components.PlaylistFragment;
 
 /**
  * Singleton class to manage transitions of the active content
  * displayed on the app. 
  */
 public class Transitions {
     
     private final static int PLAYLIST = R.string.playlist;
     private final static int MUSIC_LIBRARY = R.string.music_library;
     private final static int NETWORK  = R.string.network;
     private final static int CONNECT = R.string.connect;
     
     private final static PlaylistFragment playlistFragment = new PlaylistFragment();
     private final static MusicLibraryFragment musicLibraryFragment = new MusicLibraryFragment();
     private final static NetworkFragment networkFragment = new NetworkFragment();
     private final static ConnectFragment connectFragment = new ConnectFragment();
 
     //Home is where you get sent after connecting to the network - for now
     //this is the playlist
     private final static int HOME = PLAYLIST;
     
     public static void transitionToHome(CoreActivity activity){
         switchFragment(HOME, activity);
     }
     
     public static void transitionToPlaylist(CoreActivity activity){
         switchFragment(PLAYLIST,activity);
     }
     
     public static void transitionToMusicLibrary(CoreActivity activity){
         switchFragment(MUSIC_LIBRARY, activity);
     }
     
     public static void transitionToNetwork(CoreActivity activity){
         switchFragment(NETWORK, activity);
     }
     
     public static void transitionToConnect(CoreActivity activity){
         switchFragment(CONNECT, activity);
     }
     
     private static void switchFragment(int fragmentName, CoreActivity activity){
         Fragment fragment = getFragment(fragmentName);
         
         activity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.content, fragment)
             .addToBackStack(null).commit();
         activity.showContent();
         String title = activity.getResources().getString(((ITitleable)fragment).getTitle());
         activity.setTitle(title);
     }
     
     private static Fragment getFragment(int fragmentInx) {
         Fragment newFragment = null;
         
         switch (fragmentInx) {
         case PLAYLIST:
             newFragment = playlistFragment;
             break;
         case MUSIC_LIBRARY:
             newFragment = musicLibraryFragment;
             break;
         case NETWORK:
             newFragment = networkFragment;
             break;
         case CONNECT:
             newFragment = connectFragment;
             break;
         }
         return newFragment;
     }
 }
