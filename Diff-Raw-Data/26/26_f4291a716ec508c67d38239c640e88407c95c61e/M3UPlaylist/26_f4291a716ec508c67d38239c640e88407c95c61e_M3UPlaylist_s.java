 package com.mamewo.malarm;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import android.util.Log;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  */
 
 public class M3UPlaylist implements Playlist {
 	private int mNextIndex = 0;
 	private final String mBasepath;
 	private List<String> mPlaylist;
 
 	/**
 	 * 
 	 * @param playlist_basepath path to directory which contains play list
 	 * @param playlist_filename filename of playlist (not absolute path)
 	 */
	public M3UPlaylist(String playlist_basepath, String playlist_filename) {
 		mBasepath = playlist_basepath;
 		final String playlist_abs_path = (new File(playlist_basepath, playlist_filename)).getAbsolutePath();
 		try {
 			mPlaylist = new ArrayList<String>();
 			loadPlaylist(playlist_abs_path);
		} catch (FileNotFoundException e) {
			Log.i("M3UPlaylist", "cannot find playlist " + playlist_filename);
 		} catch (IOException e) {
 			Log.i("M3UPlaylist", "cannot read playlist " + playlist_filename);
 		}
 	}
 	
 	@Override
 	final public boolean isEmpty() {
 		return mPlaylist.isEmpty();
 	}
 	
 	@Override
 	final public String next() {
 		if (mPlaylist.size() <= mNextIndex) {
 			mNextIndex = 0;
 		}
 		String result = mPlaylist.get(mNextIndex);
 		//TODO: test
 		if (! (new File(result)).isAbsolute()) {
 			result = (new File(mBasepath, result)).getAbsolutePath();
 		}
 		mNextIndex++;
 		return result;
 	}
 
 	@Override
 	final public void reset() {
 		mNextIndex = 0;
 	}
 
 	final protected void loadPlaylist(final String filename) throws FileNotFoundException, IOException {
 		final BufferedReader br = new BufferedReader(new FileReader (filename));
 		String music_filename;
 		while ((music_filename = br.readLine()) != null) {
 			if (music_filename.charAt(0) != '#') {
 				mPlaylist.add(music_filename);
 			}
 		}
 		br.close();
 	}
 }
