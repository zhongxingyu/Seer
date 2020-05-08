 package no.runsafe.moosic;
 
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 
 import java.io.File;
 import java.util.HashMap;
 
 public class MusicHandler
 {
 	public MusicHandler(IScheduler scheduler, Plugin moosic, IOutput output)
 	{
 		this.scheduler = scheduler;
 		this.path = String.format("plugins/%s/songs/", moosic.getName());
 		this.moosic = moosic;
 
 		File pathDir = new File(this.path);
 		if (!pathDir.exists())
 			if (!pathDir.mkdirs())
 				output.logWarning("&cUnable to create directories at %s", this.path);
 	}
 
 	public File loadSongFile(String fileName)
 	{
 		return new File(this.path + fileName);
 	}
 
 	public int startSong(MusicTrack musicTrack, RunsafeLocation location, float volume)
 	{
 		TrackPlayer trackPlayer = new TrackPlayer(location, musicTrack, volume);
 		final int newID = this.currentTrackPlayerID + 1;
 
 		double tickDelay = 1.0 / (double) musicTrack.getTempo();
 		tickDelay = tickDelay * 20D;
 		long delay = (long) tickDelay;
 		int timer = this.scheduler.startSyncRepeatingTask(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				progressPlayer(newID);
 			}
 		}, delay, delay);
 
 		trackPlayer.setTimerID(timer);
 		this.trackPlayers.put(newID, trackPlayer);
 
 		this.currentTrackPlayerID = newID;
 		return currentTrackPlayerID;
 	}
 
 	public boolean forceStop(int playerID)
 	{
 		if (this.trackPlayers.containsKey(playerID))
 		{
 			this.stopPlayer(playerID);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean playerExists(int playerID)
 	{
 		return this.trackPlayers.containsKey(playerID);
 	}
 
 	public void progressPlayer(int playerID)
 	{
 		TrackPlayer trackPlayer = this.trackPlayers.get(playerID);
 		if (!trackPlayer.playNextTick())
 			this.stopPlayer(playerID);
 	}
 
 	private void stopPlayer(int playerID)
 	{
 		TrackPlayer trackPlayer = this.trackPlayers.get(playerID);
 		this.scheduler.cancelTask(trackPlayer.getTimerID());
 		this.trackPlayers.remove(playerID);
 		this.moosic.trackStop(playerID);
 	}
 
 	private final IScheduler scheduler;
 	private final HashMap<Integer, TrackPlayer> trackPlayers = new HashMap<Integer, TrackPlayer>();
 	private int currentTrackPlayerID = 0;
 	private final String path;
 	private final Plugin moosic;
 }
