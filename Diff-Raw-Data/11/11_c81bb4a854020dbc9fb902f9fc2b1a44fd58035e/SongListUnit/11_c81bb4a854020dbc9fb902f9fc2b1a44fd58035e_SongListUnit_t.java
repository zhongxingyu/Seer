 package ar.com.henchy.common;
 
import java.util.List;
 
 public class SongListUnit extends GenericTransportUnit {
 
	private List<SongTransportUnit> songs;
 
 	public SongListUnit() {
 	}
 
	public List<SongTransportUnit> getSongs() {
 		return songs;
 	}
 
	public void setSongs(List<SongTransportUnit> songs) {
 		this.songs = songs;
 	}
 
 }
