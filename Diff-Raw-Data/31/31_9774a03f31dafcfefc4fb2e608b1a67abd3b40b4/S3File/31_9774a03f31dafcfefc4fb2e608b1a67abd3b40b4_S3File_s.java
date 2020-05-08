 package models;
 
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import play.Logger;
 import play.db.ebean.Model;
 import plugins.S3Plugin;
 
 import javax.persistence.*;
 import javax.persistence.Transient;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.UUID;
 
 /**
  * Uploads files to AWS S3 and stores file metadata.
  * Taken from https://devcenter.heroku.com/articles/using-amazon-s3-for-file-uploads-with-java-and-play-2.
  *
  * Date: 30/10/13
  * Time: 10:38
  *
  * @author      Sav Balac
  * @version     %I%, %G%
  * @since       1.0
  */
 @Entity
 @Table(name="s3file")
 public class S3File extends Model {
 
     @Id
     public UUID     id;
 
     private String  bucket;
 
     public String   name;
 
     @Transient
     public File     file;
 
 
     /**
      * Generic query helper for entity S3File.
      */
     public static Finder<UUID,S3File> find = new Finder<UUID,S3File>(UUID.class, S3File.class);
 
 
 
     /**
      * Gets the URL.
      * @return URL
      * @throws MalformedURLException
      */
     public URL getUrl() throws MalformedURLException {
         return new URL("https://s3.amazonaws.com/" + bucket + "/" + getActualFileName());
     }
 
 
     /**
      * Gets the file name.
      * @return String
      */
     private String getActualFileName() {
         return id + "/" + name;
     }
 
 
     /**
      * Saves the file.
      */
     @Override
     public void save() {
         if (S3Plugin.amazonS3 == null) {
             Logger.error("Could not save because amazonS3 was null");
             throw new RuntimeException("Could not save");
         }
         else {
             this.bucket = S3Plugin.s3Bucket;
 
             super.save(); // assigns an id
 
             PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
             putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
             S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
         }
     }
 
 
     /**
      * Deletes the file.
      */
     @Override
     public void delete() {
         if (S3Plugin.amazonS3 == null) {
             Logger.error("Could not delete because amazonS3 was null");
             throw new RuntimeException("Could not delete");
         }
         else {
             S3Plugin.amazonS3.deleteObject(bucket, getActualFileName());
             super.delete();
         }
     }
 
 
 }
