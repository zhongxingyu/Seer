 package ar.com.henchy.common;
 
import java.util.ArrayList;
 
 public class SongListUnit extends GenericTransportUnit {
 
	private ArrayList<SongTransportUnit> songs;
 
 	public SongListUnit() {
 	}
 
	public ArrayList<SongTransportUnit> getSongs() {
 		return songs;
 	}
 
	public void setSongs(ArrayList<SongTransportUnit> songs) {
 		this.songs = songs;
 	}
 
 }
