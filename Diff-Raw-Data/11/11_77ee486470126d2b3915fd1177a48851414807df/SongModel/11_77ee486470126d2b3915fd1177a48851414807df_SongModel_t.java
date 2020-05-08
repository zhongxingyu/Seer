 package crescendo.base.song;
 
 import java.util.List;
 
 /**
  * A SongModel represents one entire piece of piece, including all of its tracks and metadata.
  * 
  * @author forana
  */
 public class SongModel
 {
 	/**
 	 * The tracks stored by this model.
 	 */
 	private List<Track> tracks;
 	
 	/** Artists who worked on this particular song */
 	private List<Creator> creators;
 	
 	/** time signature of this song */
 	private TimeSignature timeSignature;
 	private String title;
 	private String email;
 	private String website;
 	private String license;
 	private int bpm;
	private int keySignature;
 	
 	/**
 	 * Creates a SongModel.
 	 * 
 	 * @param tracks
 	 * @param title
 	 * @param creators
 	 * @param email
 	 * @param website
 	 * @param license
 	 * @param beatsPerMinute
 	 * @param timeSignature
 	 * TODO Finish this comment.
 	 */
	public SongModel(List<Track> tracks,String title,List<Creator> creators,String email,String website,String license,int beatsPerMinute, TimeSignature timeSignature, int keySignature) {
 		this.tracks=tracks;
 		this.creators=creators;
 		this.timeSignature = timeSignature;
 		this.title=title;
 		this.email=email;
 		this.website=website;
 		this.license=license;
 		this.bpm=beatsPerMinute;
		this.keySignature = keySignature;
 	}
 	
 	/**
 	 * The tracks in the model.
 	 * 
 	 * @return The tracks.
 	 */
 	public List<Track> getTracks()
 	{
 		return this.tracks;
 	}
 	
 	/**
 	 * getter for the list of creators
 	 * @return list of creators of the song
 	 */
 	public List<Creator> getCreators()
 	{
 		return this.creators;
 	}
 	
 	public String getTitle()
 	{
 		return this.title;
 	}
 	
 	public String getEmail()
 	{
 		return this.email;
 	}
 	
 	public String getWebsite()
 	{
 		return this.website;
 	}
 	
 	public String getLicense()
 	{
 		return this.license;
 	}
 	
 	public int getBPM()
 	{
 		return this.bpm;
 	}
 	
 	public TimeSignature getTimeSignature()
 	{
 		return this.timeSignature;
 	}
	
	public int getKeySignature(){
		return this.keySignature;
	}
 }
