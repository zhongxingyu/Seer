 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.wazari.dao.jpa.entity.xml;
 
 import java.util.List;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Tag;
 import net.wazari.dao.entity.TagPhoto;
 import net.wazari.dao.entity.TagTheme;
 import net.wazari.dao.entity.Theme;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.dao.jpa.entity.*;
 
 /**
  *
  * @author kevinpouget
  */
 @XmlRootElement
 public class WebAlbumsXML {
     public static final Class<?>[] clazzez = new Class<?>[]
     {WebAlbumsXML.class, JPATheme.class, JPATheme.class, JPAUtilisateur.class,
     JPAAlbum.class, JPAPhoto.class, JPATag.class, JPATagTheme.class,
      JPATagPhoto.class} ;
 
     @XmlElement private Themes Themes ;
     @XmlElement private Utilisateurs Utilisateurs ;
     @XmlElement private Albums Albums ;
     @XmlElement private Photos Photos ;
     @XmlElement private Tags Tags ;
     @XmlElement private TagThemes TagThemes ;
     @XmlElement private TagPhotos TagPhoto ;
 
     private WebAlbumsXML(){}
     
     public WebAlbumsXML(List<Theme> themes, List<Utilisateur> utilisateurs, List<Album> albums, List<Photo> photos, 
             List<Tag> tags, List<TagTheme> tagThemes, List<TagPhoto> tagPhoto)
     {
         this.Themes = new Themes((List)themes);
         this.Utilisateurs = new Utilisateurs((List)utilisateurs);
         this.Albums = new Albums((List)albums);
         this.Photos = new Photos((List)photos);
         this.Tags = new Tags((List)tags);
         this.TagThemes = new TagThemes((List)tagThemes);
         this.TagPhoto = new TagPhotos((List)tagPhoto);
     }
 
     public List<JPAAlbum> getAlbums() {
         return (List) Albums.Album;
     }
 
     public List<JPAPhoto> getPhotos() {
         return (List) Photos.Photo;
     }
 
     public List<JPATagPhoto> getTagPhoto() {
         return (List) TagPhoto.TagPhoto;
     }
 
     public List<JPATagTheme> getTagThemes() {
         return (List) TagThemes.TagTheme;
     }
 
     public List<JPATag> getTags() {
         return (List) Tags.Tag;
     }
 
     public List<JPATheme> getThemes() {
         return (List) Themes.Theme;
     }
 
     public List<JPAUtilisateur> getUtilisateurs() {
         return (List) Utilisateurs.Utilisateur ;
     }
 
     public static class Themes {
         public List<JPATheme> Theme ;
 
         public Themes() {}
         public Themes(List<JPATheme> Theme) {
             this.Theme = Theme;
         }
     }
     public static class Albums {
         public List<JPAAlbum> Album ;
 
         public Albums() {}
         public Albums(List<JPAAlbum> Album) {
             this.Album = Album;
         }
     }
     public static class Photos {
         public List<JPAPhoto> Photo ;
 
         public Photos() {}
         public Photos(List<JPAPhoto> Photo) {
             this.Photo = Photo;
         }
     }
     public static class Tags {
         public List<JPATag> Tag ;
 
         public Tags() {}
         public Tags(List<JPATag> Tag) {
             this.Tag = Tag;
         }
     }
     public static class TagPhotos {
         public List<JPATagPhoto> TagPhoto ;
 
         public TagPhotos() {}
         public TagPhotos(List<JPATagPhoto> TagPhoto) {
             this.TagPhoto = TagPhoto;
         }
     }
     public static class TagThemes {
         public List<JPATagTheme> TagTheme ;
 
         public TagThemes() {}
         public TagThemes(List<JPATagTheme> TagTheme) {
             this.TagTheme = TagTheme;
         }
     }
     public static class Utilisateurs {
         public List<JPAUtilisateur> Utilisateur ;
 
         public Utilisateurs() {}
         public Utilisateurs(List<JPAUtilisateur> Utilisateur) {
             this.Utilisateur = Utilisateur ;
         }
     }
 }
