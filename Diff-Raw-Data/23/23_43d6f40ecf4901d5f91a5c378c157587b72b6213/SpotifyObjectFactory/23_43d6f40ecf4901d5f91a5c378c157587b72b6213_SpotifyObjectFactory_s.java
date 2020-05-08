 package com.quiltplayer.core.factory;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.quiltplayer.core.repo.spotify.JotifyRepository;
 import com.quiltplayer.model.Album;
 import com.quiltplayer.model.Artist;
 import com.quiltplayer.model.Song;
 import com.quiltplayer.model.jotify.JotifyAlbum;
 import com.quiltplayer.model.jotify.JotifyArtist;
 import com.quiltplayer.model.jotify.JotifySong;
 
 import de.felixbruns.jotify.media.Track;
 
 /**
  * 
  * @author Vlado Palczynski
  * 
  */
 @Component
 public class SpotifyObjectFactory {
 
     private static JotifyRepository jotifyRepository;
 
     @Autowired
     private JotifyRepository jr;
 
     @PostConstruct
     public void setProperty() {
         SpotifyObjectFactory.jotifyRepository = jr;
     }
 
     public static Artist getArtist(de.felixbruns.jotify.media.Artist jotifyArtist) {
         JotifyArtist artist = new JotifyArtist(jotifyArtist);
 
         return artist;
     }
 
     public static Album getAlbum(de.felixbruns.jotify.media.Album spotifyAlbum) {
         JotifyAlbum album = new JotifyAlbum(spotifyAlbum);
         album.setJotifyRepository(jotifyRepository);
 
         return album;
     }
 
     public static Song getTrack(Track track) {
         JotifySong song = new JotifySong(track);
 
         return song;
     }
 
     public static Song getTrack(Track track, Album album) {
         JotifySong song = new JotifySong(track, album);
 
         return song;
     }
 
 }
