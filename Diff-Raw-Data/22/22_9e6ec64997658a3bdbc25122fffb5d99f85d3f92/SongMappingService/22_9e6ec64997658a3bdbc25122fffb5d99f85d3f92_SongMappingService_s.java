 package de.lmu.ios.geomelody.service;
 
 import de.lmu.ios.geomelody.dom.Filters;
 import de.lmu.ios.geomelody.dom.Location;
 import de.lmu.ios.geomelody.dom.Song;
import de.lmu.ios.geomelody.dom.Songs;
 
 
 public interface SongMappingService {
 	public void saveSong(Song song);
 	
	public Songs getkNearestSongs(Location location, Filters filters, int k);
 }
