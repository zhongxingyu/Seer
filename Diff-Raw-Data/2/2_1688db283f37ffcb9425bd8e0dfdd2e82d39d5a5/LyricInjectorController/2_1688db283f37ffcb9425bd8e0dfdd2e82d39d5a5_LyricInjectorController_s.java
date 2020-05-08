 package com.clete2.LyricInjector;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JLabel;
 
 /**
  * Class to control the LyricInjectorView.
  * @author clete2
  *
  */
 public class LyricInjectorController {
 	private JLabel threadStatusLabel;
 	private ExecutorService lyricThreadPool;
 	private MusicScanner musicScanner;
 
 	public LyricInjectorController() {
 		this.initalize();
 	}
 
 	private void initalize() {
 		this.lyricThreadPool = null;
 		this.musicScanner = new MusicScanner();
 
 		this.threadStatusLabel = new JLabel(this.getThreadStatusLabelText());
 		new LyricInjectorView(this, threadStatusLabel);
 	}
 
 	/**
 	 * Injects lyrics for a given directory.
 	 * @param path A directory path to scan & inject lyrics.
 	 */
 	public void injectLyrics(final String path) {
 		Runnable lyricInjection = new Runnable() {
 			public void run() {
 
 				injectLyricsForPath(path);
 			}
 		};
 		new Thread(lyricInjection).start();
 	}
 
 	public String getThreadStatusLabelText() {
 		String status;
 
 		if(this.lyricThreadPool == null
 				|| this.lyricThreadPool.isShutdown() 
 				|| this.lyricThreadPool.isTerminated()) {
 			status = "Idle";
 		} else {
 			status = "Active";
 		}
 
 		return status;
 	}
 
 	/**
 	 * Guesses the initial file path to the user's music directory.
 	 * @return A guess of the file path to the user's music directory.
 	 */
 	public String getInitialFilePath() {
 		StringBuilder initialFilePath = new StringBuilder();
 
 		String userHome = System.getProperty("user.home");
 
 		if(userHome != null) {
 			initialFilePath.append(userHome);
 		}
 		
 		String osName = System.getProperty("os.name").toUpperCase();
 		String fileSeparator = System.getProperty("file.separator");
 
 		// Currently not appending anything after the user home
 		// If it is Linux or some other OS.
 		// With so many Linux distributions it is hard to guess
 		// where the pictures folder might be.
 		// TODO: Search for a pictures folder using a 'smart' fashion.
 		if(osName != null) {
 			if(osName.contains("WIN") && (osName.contains("7") || osName.contains("8"))) {
 				initialFilePath.append(fileSeparator +"Music"+ fileSeparator);
 			} else if(osName.contains("WIN") && osName.contains("XP")) {
 				initialFilePath.append(fileSeparator +"My Music"+ fileSeparator);
 			} else if(osName.contains("MAC")) {
 				initialFilePath.append(fileSeparator +"Music"+ fileSeparator);
 			}
 		}
 
 		return initialFilePath.toString();
 	}
 
 	/**
 	 * Multi-threaded injection of lyrics. Sets up and executes the injection of lyrics.
 	 * @param path Directory to scan & inject lyrics.
 	 */
 	private void injectLyricsForPath(String path) {
 		// Create a thread pool that will inject lyrics into audio files
 		// Start up 3 times as many threads as logical processors
 		this.lyricThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
 
 		// Get all of the audio files from the given path and store each path in an ArrayList
 		//ArrayList<Path> audioPaths = musicScanner.getAudioListFromPath("/Users/Clete2/Desktop/Music/");
 		ArrayList<Path> audioPaths = musicScanner.getAudioListFromPath(path);
 		// Store lyric injectors for each Path
 		ArrayList<LyricInjector> lyricInjectors = new ArrayList<LyricInjector>();
 
 		// Create and store all lyric injectors
 		for(Path audioPath : audioPaths) {
 			lyricInjectors.add(new LyricInjector(audioPath));
 		}
 
 		try {
 			// 30 second timeout
			this.lyricThreadPool.invokeAll(lyricInjectors, 30, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		try {
 			this.lyricThreadPool.awaitTermination(30, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		this.lyricThreadPool.shutdown();
 		this.lyricThreadPool = null;
 	}
 }
