 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import models.Activity;
 import models.Photo;
 import models.User;
 import play.Play;
 import play.libs.Codec;
 import play.libs.Images;
 import play.libs.MimeTypes;
 import play.mvc.Controller;
 import play.mvc.With;
 import service.ShareService;
 
 /**
  *
  * @author Pluce
  */
 @Check("user")
 @With(Secure.class)
 public class Photos extends Controller{
     
     public static void index(){
         
         render();
     }
     
     public static void upload(File image) throws IOException{
        if(image == null){
            index();
        }
         ShareService.sharePhoto(Security.connectedUser(), image);
         
         index();
     }
     
     public static void remove(Long id){
         Photo p = Photo.findById(id);
         if(p != null){
             ShareService.unsharePhoto(Security.connectedUser(),p);
         }
         index();
     }
     
     public static void avatar(Long id){
         Photo photo = Photo.findById(id);
         if(photo==null){
             notFound();
         }
         render(photo);
     }
     
     public static void makeAvatar(Long id, Integer x1, Integer y1, Integer x2, Integer y2) throws IOException{
         Photo photoBase = Photo.findById(id);
         User current = Security.connectedUser();
         File photoFile = Play.getFile(Photo.PHOTOS_PATH+photoBase.fileName);
         File avatarFile = Play.getFile(Photo.AVATAR_PATH+current.id+".jpg");
         avatarFile.createNewFile();
         Images.crop(photoFile, avatarFile, x1, y1, x2, y2);
         Profil.index();
     }
 }
