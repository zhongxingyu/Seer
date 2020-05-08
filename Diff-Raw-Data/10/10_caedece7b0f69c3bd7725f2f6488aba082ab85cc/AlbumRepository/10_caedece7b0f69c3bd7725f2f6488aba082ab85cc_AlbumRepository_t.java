 package org.guiceae.main.repositories;
 
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import org.guiceae.main.model.Album;
import org.guiceae.util.UserPrincipalHolder;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: boui
  * Date: 6/26/12
  */
 public class AlbumRepository {
     @Inject
     private Provider<Objectify> ofy;
 
     private static Album DEFAULT_ALBUM = new Album();
    
    @Inject
    UserPrincipalHolder userPrincipalHolder;
 
     static {
         ObjectifyService.register(Album.class);
         DEFAULT_ALBUM.setId(0L);
         DEFAULT_ALBUM.setTitle("(не в альбомі)");
     }
 
     public Album getDefaultAlbum(){
         return DEFAULT_ALBUM;
     }
 
     public List<Album> getAll() {
         List<Album> albums = new ArrayList<Album> ();
        if (userPrincipalHolder.get().contains("cm")){
            albums.add(DEFAULT_ALBUM);
        }
         albums.addAll(ofy.get().query(Album.class).list());
         return albums;
     }
 
     public Album getById(Long id) {
         if (id==null || id.equals(0L)){
             return DEFAULT_ALBUM;
         }else{
             return ofy.get().get(Album.class, id);
         }
     }
 
     public void persistAlbum(Album album) {
         ofy.get().put(album);
     }
 }
