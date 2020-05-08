 package br.com.nozella.musicxml.to;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Library {
 	private Map<Integer, Artist> artists;
 
 	public Library() {
 		this.artists = new HashMap<Integer, Artist>();
 	}
 	public Collection<Artist> getArtists() {
 		return artists.values();
 	}
 
 	public void setArtists(Collection<Artist> artists) {
 		for (Artist artist : artists) {
 			this.putArtist(artist);
 		}
 	}
 	
 	public void putArtist(Artist artist) {
		this.artists.put(artist.hashCode(), artist);
 	}
 	
 	public Artist getArtist(String artistName) {
		return this.artists.get(artistName);
 	}
 }
