 /*
  * Copyright 2008 Charles Perry
  *
  * This file is part of Harmonium, the TiVo music player.
  *
  * Harmonium is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as 
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * Harmonium is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public
  * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
  
 package org.dazeend.harmonium.screens;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.dazeend.harmonium.Harmonium;
 import org.dazeend.harmonium.music.HPLFile;
 import org.dazeend.harmonium.music.MusicCollection;
 import org.dazeend.harmonium.music.PlaylistEligible;
 
 import com.tivo.hme.bananas.BView;
 
 /**
  * @author Charles Perry (harmonium@DazeEnd.org)
  *
  */
 public class AddToPlaylistScreen extends HListScreen {
 
 	private PlaylistEligible musicItem;
 	private static final String NEW_PLAYLIST_LABEL = "Add to New Playlist";
 	private static final String NP_PLAYLIST_LABEL = "Add to \"Now Playing\" Playlist";
 	
 	public AddToPlaylistScreen(Harmonium app, PlaylistEligible musicItem) {
 		super(app, "Add " + musicItem.toString() + " to Playlist");
 		
 		this.app = app;
 		this.musicItem = musicItem;
 		
 		// if there's music playing, put "Now Playing" at the top
		if (this.app.getDiscJockey().hasCurrentPlaylist()) {
 			this.list.add(NP_PLAYLIST_LABEL);
 		}
 		
 		// sort existing HPL playlists by date last modified, add them to the list
 		List<HPLFile> sortedHPLPlaylists = MusicCollection.getMusicCollection(this.app.getHFactory()).getHPLPlaylists();
 		HPLFile.DateDescendingComparator c = new HPLFile.DateDescendingComparator();
 		Collections.sort(sortedHPLPlaylists, c);
 		this.list.add( sortedHPLPlaylists.toArray() );
 
 		// Put "Add to new playlist" at the bottom of the list. 
 		this.list.add(NEW_PLAYLIST_LABEL);
 }
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
 	 */
 	@Override
 	public boolean handleEnter(Object screenArgument, boolean isReturning) {
 		
 		if(isReturning) {
 			// pops screen if we have just finished creating a new playlist
 			this.app.pop();
 		}
 		
 		return super.handleEnter(screenArgument, isReturning);
 	}
 
 
 
 	@Override
 	public boolean handleAction(BView view, Object action) {
         if(action.equals("right") || action.equals("select")) {
         	if( list.get( list.getFocus() ).equals(NEW_PLAYLIST_LABEL) ) {
         		// Create a new playlist and add this music item to it
         		this.app.push(new CreatePlaylistScreen(this.app, this.musicItem), TRANSITION_LEFT);
         	}
         	else if (  list.get( list.getFocus() ).equals(NP_PLAYLIST_LABEL)  ) {
         		// Add music item to now playing playlist
         		this.app.getDiscJockey().enqueueAtEnd(this.musicItem);
         	}
         	else {
         		HPLFile hplFile = (HPLFile)list.get( list.getFocus() );
         		
         		// Add this music item's tracks to the HPL playlist
         		try {
         			hplFile.add(this.app, this.musicItem);
         			this.app.pop();
         		}
         		catch(Exception e) {
         			this.app.play("bonk.snd");
         			this.app.push(new ErrorScreen(this.app, "Could not write to playlist."), TRANSITION_LEFT);
         		}
         	}
         
             
         }  
         return super.handleAction(view, action);
     }
 }
