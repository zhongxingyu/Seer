 package com.quiltplayer.model.jotify;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.NotImplementedException;
 
 import com.quiltplayer.core.comparators.YearComparator;
 import com.quiltplayer.core.factory.SpotifyObjectFactory;
 import com.quiltplayer.core.repo.spotify.JotifyRepository;
 import com.quiltplayer.model.Album;
 import com.quiltplayer.model.Artist;
 import com.quiltplayer.model.ArtistName;
 import com.quiltplayer.model.StringId;
 
 public class JotifyArtist implements Artist {
 
     private de.felixbruns.jotify.media.Artist spotifyArtist;
 
    private JotifyRepository jotifyRepository;
 
     private List<Album> albums;
 
     private StringId id;
 
     private ArtistName name;
 
    /**
     * @param jotifyRepository
     *            the jotifyRepository to set
     */
    public final void setJotifyRepository(JotifyRepository jotifyRepository) {
        this.jotifyRepository = jotifyRepository;
    }

     public JotifyArtist(de.felixbruns.jotify.media.Artist spotifyArtist) {
         this.spotifyArtist = spotifyArtist;
 
         name = new ArtistName(spotifyArtist.getName());
         id = new StringId(spotifyArtist.getId());
     }
 
     @Override
     public void addAlbum(Album album) {
     }
 
     @Override
     public ArtistName getArtistName() {
         return name;
     }
 
     @Override
     public StringId getStringId() {
         return id;
     }
 
     @Override
     public boolean hasAlbum(Album album) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean hasAlbums() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean isThe() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void removeAlbum(Album album) {
     }
 
     @Override
     public void setArtistName(ArtistName artistName) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setId(StringId id) {
     }
 
     @Override
     public void setThe(boolean the) {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * @return the spotifyArtist
      */
     public final de.felixbruns.jotify.media.Artist getSpotifyArtist() {
         return spotifyArtist;
     }
 
     /**
      * @return the albums
      */
     public final List<Album> getAlbums() {
         if (albums == null) {
             albums = new ArrayList<Album>();
 
            spotifyArtist = jotifyRepository.getInstance().browse(spotifyArtist);
 
             for (de.felixbruns.jotify.media.Album album : spotifyArtist.getAlbums()) {
                 albums.add(SpotifyObjectFactory.getAlbum(album));
             }
 
             Collections.sort(albums, new YearComparator());
         }
 
         return albums;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.quiltplayer.model.Artist#getSpotifyId()
      */
     @Override
     public String getSpotifyId() {
         return spotifyArtist.getId();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.quiltplayer.model.Artist#setSpotifyId(java.lang.String)
      */
     @Override
     public void setSpotifyId(String spotifyId) {
         throw new NotImplementedException("");
     }
 }
