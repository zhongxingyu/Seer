 package br.com.nozella.musicxml.to;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Album {
 
 	private String name;
 	private Map<Integer, Music> musics;
 	
 	public Album(String name) {
 		this.musics = new HashMap<Integer, Music>();
 		this.setName(name);
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public void setName(String name) {
 		this.name = name.toUpperCase();
 	}
 	
 	public Collection<Music> getMusics() {
 		return this.musics.values();
 	}
 	
 	public void setMusics(Collection<Music> musics) {
 		for (Music music : musics) {
 			this.putMusic(music);
 		}
 	}
 	
 	public void putMusic(Music music) {
		this.musics.put(music.getTiltle().hashCode(), music);
 	}
 	
 	public Music getMusic(String tiltleMusic) {
		return this.musics.get(tiltleMusic.toUpperCase().hashCode());
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Album other = (Album) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Album [name=%s, musics=%s]", this.getName(), this.getMusics());
 	}
 	
 }
