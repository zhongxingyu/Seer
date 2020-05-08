 package edu.mines.csci598.recycler.bettyCrocker;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * The Song class represents a collection of Tracks that are played 
  * synchronously to provide a final mix that is the song that is played.
  * 
  * In order to create a song, you have two options:
  * 1.) You can define a List of Tracks and initialize the Song with the List
  * 2.) You can create a Song with the default constructor and add Tracks later
  * 
  * Once a Song is created, the 'startPlaying' method needs to be called to
  * start the Song, and the 'stopPlaying' method is called to stop it.
  * 
  * @author John
  *
  */
 public class Song {
 	
 	private final CountDownLatch startLatch = new CountDownLatch(1); //Used for making sure the Tracks start at the same time
 	
 	private List<Track> tracks;
 	private boolean isPlaying = false;
 	private boolean isLooping = false;
 	
 	public Song() {
 		tracks = new ArrayList<Track>();
 	}
 	
 	public Song(List<Track> tracks) {
 		for (Track track : tracks) {
 			addTrack(track);
 		}
 	}
 
 	public void startPlaying() {
		startPlaying(false);
 	}
 	
 	public void startPlaying(boolean loop) {
 		isLooping = loop;
 		isPlaying = true;
 		new PlayThread().start();
 	}
 	
 	public void stopPlaying() {
 		isPlaying = false;
 		isLooping = false;
 		
 		for(Track track : tracks) {
 			track.stopPlaying();
 		}
 	}
 	
 	public void addTrack(Track track) {
 		insertTrack(tracks.size(), track);
 	}
 	
 	public void insertTrack(int index, Track track) {
 		track.setStartLatch(startLatch);
 		tracks.add(index, track);
 	}
 	
 	/*
 	 * This method starts all of the Tracks assigned to this
 	 * song at the same time.
 	 */
 	private void playTracks() {
 		for(Track track : tracks) {
 			track.startPlaying();
 		}
 		
 		//Sleep for a short amount of time to give all of the tracks a chance to start playing
 		try {
 			Thread.sleep(200);
 		} catch (InterruptedException e) {
 			System.out.println("Problem sleeping before starting track playback.");
 			e.printStackTrace();
 		}
 		
 		//Count down the start latch to have all tracks begin playing at the same time
 		startLatch.countDown();
 	}
 	
 	private boolean allTracksFinished() {
 		boolean allTracksFinished = true;
 		
 		for (Track track : tracks) {
 			if (track.isPlaying()) {
 				allTracksFinished = false;
 				break;
 			}
 		}
 		
 		return allTracksFinished;
 	}
 	
 	public void setLooping(boolean isLooping) {
 		this.isLooping = isLooping;
 	}
 	
 	public List<Track> getTracks() {
 		return tracks;
 	}
 	
 	
 	/*
 	 * This class is the thread that is used for playing the Tracks.
 	 * It is needed so that main flow of the application is not
 	 * interrupted while the Song plays.
 	 */
 	private class PlayThread extends Thread {
 		
 		public void run() {
 			playTracks();
 			
 			while (isPlaying && isLooping) {
 				if (allTracksFinished()) {
 					playTracks();
 				}
 			}
 		}
 		
 	}
 
 }
