 package com.example.educationalapp;
 
import com.google.android.youtube.player.YouTubeIntents;

import android.content.Intent;
 import android.os.Bundle;
import android.view.View;

 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 
 /**
  * A simple {@link android.support.v4.app.Fragment} subclass.
  * 
  */
 public class VideosTab extends Fragment {
	private static final String PLAYLIST_ID = "PLF3DFB800F05F551A";
 
 	public VideosTab() {
 		// Required empty public constructor
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
		Intent intent = YouTubeIntents.createOpenPlaylistIntent(getActivity(), PLAYLIST_ID);
        startActivity(intent);
		
 		// Inflate the layout for this fragment
 		return inflater.inflate(R.layout.videos_tab_fragment, container, false);
 		
 		
 	}
 
 }
