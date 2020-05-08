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
  
 package org.dazeend.harmonium.music;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.dazeend.harmonium.Harmonium;
 
 
 public class HPLFile extends EditablePlaylist implements PlaylistFile {
 	
 	public static final String	DESCRIPTION_KEYWORD = "#HarmoniumDescription:";
 	public static final String	SHUFFLE_KEYWORD = "#HarmoniumShuffle:";
 	public static final String	REPEAT_KEYWORD = "#HarmoniumRepeat:";
 	public static final String	BOOLEAN_TRUE_KEYWORD = "ON";
 	public static final String	BOOLEAN_FALSE_KEYWORD = "OFF";
 	
 	private boolean					shuffleMode;
 	private boolean					repeatMode;
 	private String					description = "";
 	private File file;
 	
 	/**
 	 * Creates a new HPLFile and adds a single new member.
 	 * 
 	 * @param app
 	 * @param file
 	 * @param shuffleMode
 	 * @param repeatMode
 	 * @param description
 	 * @param member
 	 */
 	public HPLFile(	Harmonium			app,
 					File 				file, 
 					boolean 			shuffleMode, 
 					boolean 			repeatMode, 
 					String 				description,
 					PlaylistEligible	member
 	) throws IOException {
 		
 		_tracks = new ArrayList<Playable>();
 		this._tracks.addAll(member.listMemberTracks(app));
 		this.common(app, file, shuffleMode, repeatMode, description);
 	}
 	
 	/**
 	 * Creates a new HPLFile and adds a list of new members.
 	 * 
 	 * @param app
 	 * @param file
 	 * @param shuffleMode
 	 * @param repeatMode
 	 * @param description
 	 * @param members
 	 * @throws IOException
 	 */
 	public HPLFile(	Harmonium				app,
 					File 					file, 
 					boolean 				shuffleMode, 
 					boolean 				repeatMode, 
 					String 					description,
 					List<PlaylistEligible>	members
 	) throws IOException {
 		
 		_tracks = new ArrayList<Playable>();
 		for(PlaylistEligible musicItem : members) {
 			this._tracks.addAll(musicItem.listMemberTracks(app));
 		}
 		this.common(app, file, shuffleMode, repeatMode, description);
 	}
 	
 	/**
 	 * Creates a new HPLFile using data stored in a file.
 	 * 
 	 * @param hFactory
 	 * @param file
 	 */
 	public HPLFile(MusicCollection musicCollection, File file) throws IOException, FileNotFoundException {
 		
 		_tracks = new ArrayList<Playable>();
 		this.file = file;
 		
 		// open file for reading
 		FileReader fin = new FileReader(this.file);
 		BufferedReader bin = new BufferedReader(fin);
 		try {
 			// Read the file line by line
 			while(bin.ready()) {
 				String line = bin.readLine();
 		
 				// Look for keywords
 				if(line.startsWith(HPLFile.DESCRIPTION_KEYWORD)) {
 					// Found the description of this HPL playlist
 					if(line.substring( HPLFile.DESCRIPTION_KEYWORD.length() ) != null) {
 						this.description = line.substring( HPLFile.DESCRIPTION_KEYWORD.length() );
 					}
 				}
 				else if(line.startsWith(HPLFile.SHUFFLE_KEYWORD)) {
 					// Found the default shuffle mode for this playlist
     				if(line.substring(HPLFile.SHUFFLE_KEYWORD.length()).equals(HPLFile.BOOLEAN_TRUE_KEYWORD)) {
 						this.shuffleMode = true;
 					}
 					else if(line.substring(HPLFile.SHUFFLE_KEYWORD.length()).equals(HPLFile.BOOLEAN_FALSE_KEYWORD)) {
 						this.shuffleMode = false;
 					}
 				}
 				else if(line.startsWith(HPLFile.REPEAT_KEYWORD)) {
 					// found the default repeat mode for this playlist
 					if(line.substring(HPLFile.REPEAT_KEYWORD.length()).equals(HPLFile.BOOLEAN_TRUE_KEYWORD)) {
 						this.repeatMode = true;
 					}
 					else if(line.substring(HPLFile.REPEAT_KEYWORD.length()).equals(HPLFile.BOOLEAN_FALSE_KEYWORD)) {
 						this.repeatMode = false;
 					}
 				}
 				else if( ! line.startsWith("#") ) {
 					// Ignoring all comments (have pound as first character).
 					// This line contains the path to a music track.
 					// All paths in HPL playlists are relative to the music root.
 					File trackFile = new File(musicCollection.getMusicRoot(), line);
 					Playable track = musicCollection.lookupTrackByFile(trackFile);
 					
 					if( track != null ) {
 						// The track has been found. Add it to the list of members.
 						this._tracks.add(track);
 					}
 				}
 			}
 		}
 		finally {
 			// Close streams
 			bin.close();
 			fin.close();
 		}
     	
     	
 	}
 	
 	/**
 	 * Common code used to initialize newly created HPLFiles
 	 * 
 	 * @param app
 	 * @param file
 	 * @param shuffleMode
 	 * @param repeatMode
 	 * @param description
 	 * @throws IOException
 	 */
 	private void common(	Harmonium			app,
 							File 				file, 
 							boolean 			shuffleMode, 
 							boolean 			repeatMode, 
 							String 				description
 	) throws IOException {
 		
 		this.file = file;
 		this.shuffleMode = shuffleMode;
 		this.repeatMode = repeatMode;
 		
 		if(description != null) {
 			this.description = description;
 		}
 		else {
 			this.description = "";
 		}
 		
 		// Write the data to disk
 		this.syncToDisk();
 	}
 	
 	public String getDescription() {
 		if(this.description != null) {
 			return this.description;
 		}
 		else { 
 			return "";
 		}
 	}
 	
 	public List<PlaylistEligible> getMembers() {
     	List<PlaylistEligible> list = new ArrayList<PlaylistEligible>();
     	list.addAll(this._tracks);
     	return list;
 	}
 	
 	public List<Playable> listMemberTracks(Harmonium app) {
 		List<Playable> list = new ArrayList<Playable>();
 		list.addAll(this._tracks);
 		return list;
 	}
 
 
 	/**
 	 * Adds a music track to this playlist
 	 * 
 	 * @param app
 	 * @param musicItem
 	 */
 	public void add(Harmonium app, PlaylistEligible musicItem) throws IOException {
 		this._tracks.addAll(musicItem.listMemberTracks(app));
 		this.syncToDisk();
 	}
 	
 	public boolean getRepeatMode(Harmonium app) {
 		return this.repeatMode;
 	}
 
 	public boolean getShuffleMode(Harmonium app) {
 		return this.shuffleMode;
 	}
 
 	/**
 	 * @param shuffleMode the shuffleMode to set
 	 */
 	public void setShuffleMode(boolean shuffleMode) throws IOException {
 		this.shuffleMode = shuffleMode;
 		
 		this.syncToDisk();
 	}
 
 	/**
 	 * @param repeatMode the repeatMode to set
 	 */
 	public void setRepeatMode(boolean repeatMode) throws IOException {
 		this.repeatMode = repeatMode;
 		this.syncToDisk();
 	}
 	
 	/**
 	 * Sets the repeat mode and shuffle mode of this playlist.
 	 * 
 	 * @param repeatMode
 	 * @param shuffleMode
 	 * @throws IOException
 	 */
 	public void setOptions(boolean repeatMode, boolean shuffleMode) throws IOException {
 		this.repeatMode = repeatMode;
 		this.shuffleMode = shuffleMode;
 		this.syncToDisk();
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) throws IOException {
 		
 		if(description != null) {
 			this.description = description;
 		}
 		else {
 			this.description = "";
 		}
 		
 		this.syncToDisk();
 		
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.music.PlaylistFile#toString()
 	 */
 	@Override
 	public String toString() {
 		if(this.description != null && ( ! this.description.equals("") ) ) {
 			return this.description;
 		}
 		else {
 			return this.file.getName();
 		}
 	}
 	
 	/**
 	 * Sets the value of an element of the playlist
 	 * 
 	 * @param index
 	 * @param musicItem
 	 */
 	public synchronized void setMember(int index, Playable musicItem) {
 		this._tracks.set(index, musicItem);
 	}
 	
 	public void save() throws IOException {
 		syncToDisk();
 	}
 	
 	public synchronized void syncToDisk() throws IOException {
 		
 		FileWriter fout = new FileWriter(this.file);
 		BufferedWriter bout = new BufferedWriter(fout);
 		
 		try {
 			// Write playlist description to file
 			bout.write(HPLFile.DESCRIPTION_KEYWORD + this.description);
 			bout.newLine();
 			
 			// Write shuffle mode to file
 			if(this.shuffleMode) {
 				bout.write(HPLFile.SHUFFLE_KEYWORD + HPLFile.BOOLEAN_TRUE_KEYWORD);
 			}
 			else{
 				bout.write(HPLFile.SHUFFLE_KEYWORD + HPLFile.BOOLEAN_FALSE_KEYWORD);
 			}
 			bout.newLine();
 			
 			// Write repeat mode to file
 			if(this.repeatMode) {
 				bout.write(HPLFile.REPEAT_KEYWORD + HPLFile.BOOLEAN_TRUE_KEYWORD);
 			}
 			else{
 				bout.write(HPLFile.REPEAT_KEYWORD + HPLFile.BOOLEAN_FALSE_KEYWORD);
 			}
 			bout.newLine();
 			
 			// Write the member tracks to file if any exist
 			if(! this._tracks.isEmpty()) {
 				for(Playable track : this._tracks) {
 					bout.write( track.getPath() );
 					bout.newLine();
 				}			
 			}
 			
 			// Flush the stream
 			bout.flush();
 		}
 		finally {
 			// Close streams
 			bout.close();
 			fout.close();
 		}
 	}
 
 	public String toStringTitleSortForm() {
 		return getDescription();
 	}
 	
 	public long getLastModified() {
 		return this.file.lastModified();
 	}
 	
 	public static class DateDescendingComparator implements Comparator<HPLFile> {
 
 		public int compare(HPLFile x, HPLFile y)
 		{
 			long l = x.getLastModified() - y.getLastModified();
 			if (l < 0)
 				return 1;
 			else if (l > 0)
 				return -1;
 			else return 0;
 		}
 		
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public int compareTo(PlaylistFile that)
 	{
 		// NullPointerException if we are trying to compare to a null object
 		if(that == null){
 			throw new NullPointerException();
 		}
 		
 		return this.toString().compareToIgnoreCase(that.toString());
 	}
 	
 }
