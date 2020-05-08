 package musicplayer;
 
 import database.Database;
 
 public abstract class PlayingSong implements IPlayingSong
 {
 	private Database DB;
 	public PlayingSong(Database DB_)
 	{
 		DB = DB_;
 	}
 	
 	
 	// throws an Error if can't play the song
 	public void play() throws Error
 	{
 		if (cant_play())
 			throw new Error("Cannot Play");
 		
 		play_song();
 		
 		start_time = get_time();
 		
 		paused = false;
 		
 		put_volume();
 	}
 
 
 	public void pause()
 	{
 		update_position(); 
 
 		pause_song();
 
 		paused = true;
 	}
 
 	// prints second count to the file
 	protected void update_position()
 	{
 		long cur_time = get_time();
 
 		// don't update if it's paused, there is no song, 
 		// or it hasn't been a second yet
 		// this logic should be changed to the oposite way! 381!
 		if (!paused && song != "" && cur_time - start_time > 1000)
 		{
 			long start_sec = position;
 			position += cur_time - start_time;
 			long end_sec = position;
 			
 			DB.insert_sec_count(song, start_sec, end_sec);
 		}
 	}
 
 	// This should be used to get the position, but not update count
 	protected long get_position()
 	{
 		long cur_time = get_time();
 		
 		return position + cur_time - start_time;
 	}
 	
 	public void replace_song(String filename)
 	{
 		if (song_exists())
 		{
 			update_position();
 			remove_song();
 		}
 		
 		add_song(filename);
 	}
 		
 	private boolean song_exists()
 	{
 		return song != "";
 	}
 
 
 	private void add_song(String filename)
 	{
 		song = filename;
 		
 		set_song(filename);
 		
 		totalDuration = get_duration();
 
 		position = 0;
 		paused = true;
 	}
 	
 
 	// throws error if past total duration
 	public void change_position(int new_pos) throws Error
 	{
 		if (new_pos > totalDuration)
 			throw new Error("Song not long enough");
 
 		update_position();
 		
 		move_to(new_pos);
 		
 
 		position = new_pos;
 	}
 	
 	public void change_pos_relative(int relative_pos)
 	{
 		// watch this!!! We might want to insert longs and change those..
 		change_position((int)(get_position()*1000) + relative_pos);
 	}
 
 	public void set_hs()
 	{
 //		hs_map.add_hotspot(song, get_position());
 	}
 	public void next_hs()
 	{
 		//change_position(hs_map.get_hotspots(song).get_next(get_position()+1));
 
 	}
 	public void prev_hs()
 	{
 //		change_position(hs_map.get_hotspots(song).get_prev(get_position()-1));
 	
 	}
 	public void remove_hs()
 	{
 //		Hotspots &hotspots = hs_map.get_hotspots(song);
 //		hotspots.remove(hotspots.get_next(get_position()));	
 	}
 
 	public boolean is_paused() // true if paused
 		{return paused;}
 
 	public boolean is_finished()	// return true if the song ended
 		{return totalDuration < get_position();}
 
 	public void vol_up()
 	{
 		set_volume_percent(get_volume_percent() + 5);		
 	}
 	
 	public void vol_down()
 	{
 		set_volume_percent(get_volume_percent() - 5);
 	}
 	
 	// sets the volume.  
 	public void set_volume_percent(int percent)
 	{	
 		if (percent > 100)
 			percent = 100;
 		
 		else if (percent < 0)
 			percent = 0;
 		
 		change_volume(percent);
 	}
 	
 	public String get_cur_song()
 	{
 		return song;		
 	}
 	
 // ABSTRACT FUNCTIONS	
 	// gets how many seconds are in the song
 	abstract protected float get_duration();
 	
 	// starts playing the song at the current position
 	abstract protected void play_song();
 
 	// sets the volume of the current song to current volume
 	// volume should be handled completely by derived classes
 	abstract protected void put_volume();
 	
 	// returns true if the current song cant be played
 	abstract protected boolean cant_play();
 	
 	// pauses the current playing song
 	abstract protected void pause_song();
 	
 	// sets the song we will play to filename
 	abstract protected void set_song(String filename);
 	
 	// stops playing the current song and prepares it to be replaced
 	abstract protected void remove_song();
 
 	// seeks to new_pos in the cur playing song
 	// throw new Error("Can't seek to that position");
 	// don't check if past total length of song
 	abstract protected void move_to(int new_pos);
 	
 	// changes the volume, needs to be recorded in derived class
 	abstract protected void change_volume(int percent);
 	
 	// gets how loud the player is playing at (0-100)
 	//	0 = no sound, 100 = max loudness
 	abstract public int get_volume_percent();
 	
 	public long get_time()
 	{
 		return System.currentTimeMillis();
 		
 	}
 	
 // PRIVATE DATA	
 	private String song = ""; // filename
 	private boolean paused = true;	// true if the song is paused
 	private float totalDuration = 0.0f; // total length of the song in secs
 	private int position = 0;	// position in the song
 	long start_time; // time the song started playing last
 	
 	
 	
 }
