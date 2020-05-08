 package fi.cie.chiru.servicefusionar.serviceApi;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fi.cie.chiru.servicefusionar.groovesharkService.Grooveshark;
 import fi.cie.chiru.servicefusionar.groovesharkService.PlaylistXmlParser;
 
 public class MusicManager
 {
     private final static String LOG_TAG = "GroovesharkMusicService";
     
     private Grooveshark grooveshark = null;
     private ServiceManager serviceManager = null;
 	private InfoBubble infobubble = null;
 	private List<String> musicInfo = null;
 	private boolean musicPlaylistDownloaded;
 	private boolean musicPositionInitialized;
 
 
     public MusicManager(ServiceManager serviceManager)
     {
     	musicPlaylistDownloaded = false;
     	musicPositionInitialized = false;
     	
     	Log.i(LOG_TAG, "Starting MusicManager");
     	this.serviceManager = serviceManager;
     	
     	// URL to Otto K's DjOnline site.
 		String URL = "http://www.djonline.fi/playing/?id=487";
 		
 		new XmlDownloader() { 
 	        protected void onPostExecute(String xmlData) {
 	        	if (xmlData != null)
 	        	{
 	        		PlaylistXmlParser parser = new PlaylistXmlParser();
 	        		musicInfo = parser.parse(xmlData);
 	        		musicPlaylistDownloaded = true;
 	        		
 	        		fillMusicPlaylist();
 	        	}
 	        	else
 	        		Log.e(LOG_TAG, "Couldn't download xml data!");
 	        }
 	    }.execute(URL); 
 
 	    StartGrooveshark();
     }
     
     public void StartGrooveshark()
     {
     	if (grooveshark == null)
     		grooveshark = new Grooveshark();
     }
     
     public void StopPlaying(String serviceApplicationName)
     {
     	if (grooveshark != null)
     		grooveshark.StopPlaying();
     }
     
 	public void showPlaylist(String serviceApplicationName) 
 	{
 		if(!musicPlaylistDownloaded)
 			return;
 		
 		if (infobubble != null)
			infobubble.setvisible(!infobubble.isVisible());
 	}
 	
 	public void playSong(String song)
 	{
 		if (grooveshark != null)
 		{
 			grooveshark.SearchSong(song, 1);
 			Log.d("MusicManager", "start playing song: " + song);
 		}
 	}
 	
 	public InfoBubble getInfoBubble()
 	{
 	    return infobubble;
 	}
 	
 	public void positionInitialized()
 	{
 		musicPositionInitialized = true;
 		fillMusicPlaylist();
 	}
 	
 	public void fillMusicPlaylist(/*List<String> playlist*/)
 	{
 		if (musicInfo == null || !musicPositionInitialized)
 			return;
 		
 		musicInfo.add(0, "Pink Floyd - Comfortably Numb");
 		musicInfo.add(0, "Steven Wilson - Luminol");
 		//playlist.add(0, "Esbjörn Svensson - Seven days of Falling");
 		//playlist.add(0, "New Order - Crystal");
 		musicInfo.add(0, "Rise Against - Make It Stop");
 		int longestTitle = getLongestSongTitleLen(musicInfo);
 		
 		List<String> tempList = new ArrayList<String>();
 
 		// Fill in white space to get all titles in same level
 		for (int i = 0; i < musicInfo.size(); i++)
 		{
 			String song = musicInfo.get(i);
 			
 			// Sometimes description in an rss feed is formatted as: "Current: OFF" instead of "Current: artist - song"  
 			if (song.equalsIgnoreCase("OFF"))
 				continue;
 			
 			song = song + fillWhiteSpace(longestTitle - song.length());
 			tempList.add(song);
 		}
 		
 		infobubble = serviceManager.getInfobubble("MusicInfobubble");
     	if (infobubble != null)
     		infobubble.populateItems(tempList, "MusicManager");
  		
 	}
 	
 	/* 
 	 * Returns string containing only wanted number of white space.
 	 * This is used to level strings in an info bubble.  
 	 */
     private String fillWhiteSpace(int number)
     {
     	String whitespace = new String();
     	
     	for(int j=0; j<number; j++)
     		whitespace += " ";
     	
     	return whitespace;
     }
     
     /*
      * Get length of the longest string in the received list object.
      */
     private int getLongestSongTitleLen(List<String> song)
     {
     	if (song == null)
     	{
     		return 0;
     	}
     	
     	int len = 0;
     	for(int k=0; k<song.size(); k++)
     	{
     	    if(song.get(k).length() > len)
     	    	len = song.get(k).length();
     	}
     	
     	return len;
     }
     
     public void onDestroy()
     {
     	if (grooveshark != null)
     	{
     		grooveshark.StopPlaying();
     	}
     }
 }
