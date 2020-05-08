 package net.kokkeli;
 
 /**
  * Class for settings
  * @author Hekku2
  *
  */
 public class Settings implements ISettings {
     
     /**
      * Location of database.
      * @return Location of database.
      */
     public String getDatabaseLocation(){
         return "db/database.db";
     }
     
     /**
      * Returns location of library
      * @return Location of library
      */
     public String getLibLocation(){
         return "lib";
     }
 
     /**
      * Returns location of templates
      * @return Location of templates
      */
     public String getTemplatesLocation() {
        return "target\\classes\\net\\kokkeli\\resources\\views";
     }
     
     /**
      * Returns location of tracks
      * @return location of tracks.
      */
     public String getTracksFolder() {
         return "tracks";
     }
 
     /**
      * Returns password salt
      * @return password salt
      */
     public String getPasswordSalt() {
         return "hgdfskh4341%";
     }
     
     public String getVlcLocation() {
         return "D:\\vlc-2.0.5";
     }
 }
