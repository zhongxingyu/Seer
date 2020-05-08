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
 
    //Home is where you get sent after connecting to the network
     private final static int HOME = MUSIC_LIBRARY;
     
     public static void transitionToHome(CoreActivity activity){
         switchFragment(HOME, activity);
         activity.showMenu();
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
