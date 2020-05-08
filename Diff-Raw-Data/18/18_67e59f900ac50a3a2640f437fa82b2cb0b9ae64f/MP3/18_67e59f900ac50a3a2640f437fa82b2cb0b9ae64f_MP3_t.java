 package com.evervoid.client.sound;
 
 import static com.evervoid.utils.ResourceUtils.getResourceDir;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 
 import javazoom.jl.player.Player;
 
 public class MP3 implements Cloneable
 {
 	private final int duration;
 	private final String filename;
 	private Player player;
 	private Thread playerThread;
 
 	public MP3(final String filename)
 	{
 		this(filename, 0);
 	}
 
 	public MP3(final String filename, final int duration)
 	{
		this.filename = filename;
 		this.duration = duration;
 	}
 
 	@Override
 	public MP3 clone()
 	{
 		return new MP3(filename, duration);
 	}
 
 	public void close()
 	{
 		if (player != null) {
 			player.close();
 		}
 	}
 
 	/**
 	 * returns the length of this mp3 in seconds, 0 if unspecified.
 	 */
 	public int getDuration()
 	{
 		return duration;
 	}
 
	public String getSoundName()
 	{
		return "snd" + File.separator + filename;
 	}
 
 	/**
 	 * Tell whether a MP3 has finished playing or not.
 	 */
 	public boolean isComplete()
 	{
 		return player.isComplete();
 	}
 
 	public void play() throws Exception
 	{
 		playerThread = new Thread()
 		{
 			@Override
 			public void run()
 			{
 				try {
					final FileInputStream ins = new FileInputStream(getResourceDir() + getSoundName());
 					final BufferedInputStream bins = new BufferedInputStream(ins);
 					player = new Player(bins);
 					player.play();
 				}
 				catch (final Exception e) {
 					System.out.println(e);
 				}
 			}
 		};
 		playerThread.setName("Music");
 		playerThread.setDaemon(true);
 		playerThread.start();
 	}
 }
