 package com.partyrock.music;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.*;
 
 public class MP3 extends Sound {
 
 	private File file;
 	private boolean isPaused;
 	private boolean isPlaying;
 	private AudioFileFormat fileFormat;
 	private Thread MP3Thread;
 	private SourceDataLine line;
 	private AudioInputStream din;
 	private MP3Player myMP3Player;
 	private double startTime = 0;
 
 	/**
 	 * Constructs an MP3 from a given file.
 	 * @param file The file
 	 */
 	public MP3(File file) {
 		super();
 		this.file = file;
 		isPaused = false;
 
 		if (!file.exists()) {
 			System.err.println("MP3 constructed for non-existent file");
 			return;
 		}
 
 		try {
 			fileFormat = AudioSystem.getAudioFileFormat(file);
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public class MP3Player implements Runnable {
 
 		private double startTime;
 		boolean paused;
 
 		public MP3Player(double var) {
 			this.startTime = var;
 			paused = false;
 		}
 
 		public void run() {
 			din = null;
 			try {
 				AudioInputStream in = AudioSystem.getAudioInputStream(file);
 				AudioFormat baseFormat = in.getFormat();
 				AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
 				din = AudioSystem.getAudioInputStream(decodedFormat, in);
 
 				line = AudioSystem.getSourceDataLine(decodedFormat, null);
 
 				if (line != null) {
 					line.open(decodedFormat);
 					byte[] data = new byte[4096];
 
 					// Start
 					line.start();
 					isPlaying = true;
 
 					// Skip first startTime seconds
 					int bytesToSkip = (int) (startTime
 							* (Integer) fileFormat.properties().get("mp3.bitrate.nominal.bps") / 8);
 					din.skip(bytesToSkip);
 
 					int nBytesRead;
 					while ((data != null) && (din != null) && (nBytesRead = din.read(data, 0, data.length)) != -1
 							&& isPlaying) {
 						// Skip first startTime seconds
 						if (isPaused == true) {
 							while (isPaused) {
 								Thread.sleep(100);
 							}
 						}
 						line.write(data, 0, nBytesRead);
 						// System.out.println(line.getMicrosecondPosition());
 					}
 
 					isPlaying = false;
 					// Stop
 					if (line != null && din != null) {
 						line.drain();
 						line.stop();
 						line.close();
 						din.close();
 					}
 				}
 
 			} catch (Exception e) {
 				System.out.println("Error!");
 				e.printStackTrace();
 			} finally {
 				if (din != null) {
 					try {
 						din.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void play(double startTime) {
 		this.startTime = startTime;
 		myMP3Player = new MP3Player(startTime);
 		MP3Thread = new Thread(myMP3Player);
 		MP3Thread.start();
 
 	}
 
 	public void playthread(double startTime) {
 
 	}
 
 	@Override
 	public double getDuration() {
 		// TODO Auto-generated method stub
 		Long microseconds = (Long) fileFormat.properties().get("duration");
 		double milliseconds = (int) (microseconds / 1000);
 		double seconds = (milliseconds / 1000);
 		return seconds;
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		isPaused = true;
 		// MP3Thread.suspend();
 		myMP3Player.paused = true;
 	}
 
 	public void unpause() {
 		// TODO Auto-generated method stub
 		isPaused = false;
 		myMP3Player.paused = false;
 		// MP3Thread.resume();
 	}
 
 	@Override
 	public double getCurrentTime() {
 		// TODO Auto-generated method stub
 		System.out.println(line.getMicrosecondPosition());
 		return startTime + (line.getMicrosecondPosition() / 1000);
 	}
 
 	/**
 	 * Returns the file associated with this MP3
 	 * @return The mp3 file
 	 */
 	public File getFile() {
 		return file;
 	}
 
 	public void stop() {
 		isPlaying = false;
 
 		pause();
 		MP3Thread = null;
 		myMP3Player = null;
 
 		isPaused = false;
 		isPlaying = false;
 		// fileFormat = null;
 		MP3Thread = null;
 		line = null;
 		din = null;
 		myMP3Player = null;
 		startTime = 0;
 
 		// Stop
 		// line.drain();
 		// line.stop();
 		// line.close();
 	}
 
 }
