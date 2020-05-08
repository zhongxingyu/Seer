 package de.lmu.ios.geomelody.service;
 
import java.util.List;

 import de.lmu.ios.geomelody.dom.Filters;
 import de.lmu.ios.geomelody.dom.Location;
 import de.lmu.ios.geomelody.dom.Song;
 
 
 public interface SongMappingService {
 	public void saveSong(Song song);
 	
	public List<Song> getkNearestSongs(Location location, Filters filters, int k);
 }
