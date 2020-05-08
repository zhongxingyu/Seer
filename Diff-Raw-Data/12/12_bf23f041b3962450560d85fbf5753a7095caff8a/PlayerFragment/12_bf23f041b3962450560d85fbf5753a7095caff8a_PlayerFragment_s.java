 package com.example.lurvberry;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Random;
 
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnCompletionListener {
 	
 	private ImageButton playButton;
 	private ImageButton forwardButton;
 	private ImageButton rewindButton;
 	private ImageButton nextButton;
 	private ImageButton previousButton;
 	private ImageButton playlistButton;
 	private ImageButton repeatButton;
 	private ImageButton shuffleButton;
 	private SeekBar songProgressBar;
 	private TextView songTitleLabel;
 	private TextView songCurrentDurationLabel;
 	private TextView songTotalDurationLabel;
 
 	private MediaPlayer player;
 	
 	private Handler playerHandler = new Handler();
 	private SongManager songManager;
 	private Utilities utils;
 	private int seekForwardTime = 5000;
 	private int seekRewindTime = 5000;
 	private int currentSongIndex = 0;
 	private int previousShuffleIndex = 0;
 	private boolean isShuffle = false;
 	private boolean isRepeat = false;
 	private ArrayList<Integer> shuffledList = new ArrayList<Integer>();
 	private ArrayList<LinkedHashMap<String, String>> songsList = new ArrayList<LinkedHashMap<String, String>>();
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.player, container, false);
         
         // All player buttons
      	playButton = (ImageButton) view.findViewById(R.id.btnPlay);
      	forwardButton = (ImageButton) view.findViewById(R.id.btnForward);
      	rewindButton = (ImageButton) view.findViewById(R.id.btnBackward);
      	nextButton = (ImageButton) view.findViewById(R.id.btnNext);
      	previousButton = (ImageButton) view.findViewById(R.id.btnPrevious);
      	playlistButton = (ImageButton) view.findViewById(R.id.btnPlaylist);
      	repeatButton = (ImageButton) view.findViewById(R.id.btnRepeat);
      	shuffleButton = (ImageButton) view.findViewById(R.id.btnShuffle);
      	songProgressBar = (SeekBar) view.findViewById(R.id.songProgressBar);
      	songTitleLabel = (TextView) view.findViewById(R.id.songTitle);
      	songCurrentDurationLabel = (TextView) view.findViewById(R.id.songCurrentDurationLabel);
      	songTotalDurationLabel = (TextView) view.findViewById(R.id.songTotalDurationLabel);
         
      	player = new MediaPlayer();
      	songManager = new SongManager();
 		utils = new Utilities();
 		
 		//Listeners Very Important
 		songProgressBar.setOnSeekBarChangeListener(this); 
 		player.setOnCompletionListener(this);
 		
 		songsList = songManager.getSongs();
 		shuffledList.clear(); 
 		playSong(0);
 		
 		/* Play button click event */
 		playButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(player.isPlaying()){
 					if(player!=null){
 						player.pause();
 						playButton.setImageResource(R.drawable.btn_play);
 					}
 				}
 				else{
 					if(player!=null){
 						player.start();
 						playButton.setImageResource(R.drawable.btn_pause);
 					}
 				}
 			}
 		});
 		
 		/* Forward button click event */
 		forwardButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				int currentPosition = player.getCurrentPosition();
 				if(currentPosition + seekForwardTime <= player.getDuration()) 
 					player.seekTo(currentPosition + seekForwardTime);
 				else player.seekTo(player.getDuration());
 			}
 		});
 		
 		/* Rewind button click event */
 		rewindButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				int currentPosition = player.getCurrentPosition();
 				if(currentPosition - seekRewindTime >= 0)
 					player.seekTo(currentPosition - seekRewindTime);
 				else player.seekTo(0);
 			}
 		});
 		
 		/* Next button click event */
 		nextButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(isShuffle){
 					if(shuffledList.isEmpty()){
 						Random rand = new Random();
 						previousShuffleIndex = 0;
 						shuffledList.add(currentSongIndex);
 						currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
 						playSong(currentSongIndex);
 					}
					else if(previousShuffleIndex < (shuffledList.size() - 1)){
 						previousShuffleIndex = previousShuffleIndex + 1;
						currentSongIndex = shuffledList.get(previousShuffleIndex);
 						playSong(currentSongIndex);
 					}
 					else{
 						Random rand = new Random();
						shuffledList.add(currentSongIndex);
 						previousShuffleIndex = previousShuffleIndex + 1;
 						currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
 						playSong(currentSongIndex);
 					}
 				}
 				else{
 					shuffledList.clear();
 					previousShuffleIndex = 0;
 					
 					if(currentSongIndex < (songsList.size() - 1)){
 						playSong(currentSongIndex + 1);
 						currentSongIndex = currentSongIndex + 1;
 				}
 				
 				else{
 					playSong(0);
 					currentSongIndex = 0;
 				}
 			}
 		}
 		});
 		
 		/* Back button click event */
 		previousButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(isShuffle && !shuffledList.isEmpty() && previousShuffleIndex >= 0){
 					currentSongIndex = shuffledList.get(previousShuffleIndex);
 					playSong(currentSongIndex);
 					previousShuffleIndex = previousShuffleIndex - 1;
 				}
 				else{
 					shuffledList.clear();
 					previousShuffleIndex = 0;
 					
 					if(currentSongIndex > 0){
 						playSong(currentSongIndex - 1);
 						currentSongIndex = currentSongIndex - 1;
 					}
 					else{
 						playSong(songsList.size() - 1);
 						currentSongIndex = songsList.size() - 1;
 					}
 				}
 			}
 		});
 		
 		/* Repeat button click event */
 		repeatButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(isRepeat){
 					isRepeat = false;
 					Toast.makeText(getActivity(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
 					repeatButton.setImageResource(R.drawable.btn_repeat);
 				}
 				else{
 					isRepeat = true;
 					Toast.makeText(getActivity(), "Repeat is ON", Toast.LENGTH_SHORT).show();
 					// make shuffle to false
 					isShuffle = false; 
 					repeatButton.setImageResource(R.drawable.btn_repeat_focused);
 					shuffleButton.setImageResource(R.drawable.btn_shuffle);
 				}	
 			}
 		});
 		
 		/* Shuffle button click event */
 		shuffleButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(isShuffle){
 					isShuffle = false;
 					Toast.makeText(getActivity(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
 					shuffleButton.setImageResource(R.drawable.btn_shuffle);
 				}else{
 					isShuffle= true;
 					Toast.makeText(getActivity(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
 					// make repeat to false
 					isRepeat = false;
 					shuffleButton.setImageResource(R.drawable.btn_shuffle_focused);
 					repeatButton.setImageResource(R.drawable.btn_repeat);
 				}	
 			}
 		});
 		
      	return view;
 	}
 	
 	/* Function to play a song */
 	public void  playSong(int songIndex){
 		try {
         	player.reset();
         	player.setDataSource(songsList.get(songIndex).get("songPath"));
         	player.prepare();
         	player.start();
  
         	String songTitle = songsList.get(songIndex).get("songTitle");
         	songTitleLabel.setText(songTitle);
 			
 			playButton.setImageResource(R.drawable.btn_pause);
 			
 			// set Progress bar values
 			songProgressBar.setProgress(0);
 			songProgressBar.setMax(100);
 			
 			// Updating progress bar
 			updateProgressBar();			
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/* Update timer on seekbar */
 	public void updateProgressBar() {
 		playerHandler.postDelayed(playerUpdateTimeTask, 100);        
     }
 	
 	/* Background Runnable thread */
 	private Runnable playerUpdateTimeTask = new Runnable() {
 	   public void run() {
 		   long totalDuration = player.getDuration();
 		   long currentDuration = player.getCurrentPosition();
 			  
 		   songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
 		   songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
 			   
 		   int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
 		   songProgressBar.setProgress(progress);
 		   // Running this thread after 100 milliseconds
 	       playerHandler.postDelayed(this, 100);
 	   }
 	};
 	
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
 	}
 	
 	/* When user starts moving the progress handler */
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// remove message Handler from updating progress bar
 		playerHandler.removeCallbacks(playerUpdateTimeTask);
     }
 	
 	/* When user stops moving the progress hanlder */
 	@Override
     public void onStopTrackingTouch(SeekBar seekBar) {
 		playerHandler.removeCallbacks(playerUpdateTimeTask);
 		int totalDuration = player.getDuration();
 		int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
 		
 		player.seekTo(currentPosition);
 		
 		updateProgressBar();
     }
 	
 	/* On Song Playing completed */
 	@Override
 	public void onCompletion(MediaPlayer arg0) {
 		if(isRepeat) playSong(currentSongIndex);
 		else if(isShuffle){
 			Random rand = new Random();
 			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
 			playSong(currentSongIndex);
 		} 
 		else{
 			if(currentSongIndex < (songsList.size() - 1)){
 				playSong(currentSongIndex + 1);
 				currentSongIndex = currentSongIndex + 1;
 			}
 			else{
 				playSong(0);
 				currentSongIndex = 0;
 			}
 		}
 	}
 }
