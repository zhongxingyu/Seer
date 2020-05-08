 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BE;
 
 /**
  *
  * @author Mak
  */
 public class PlayList
 {
 
     private final int playlistId;
     private String playlistName;
     private String created;
 
     public PlayList(int playlistId, String playlistName, String created)
     {
         this.playlistId = playlistId;
         this.playlistName = playlistName;
         this.created = created;
     }
 
     /**
      * @return the id
      */
     public int getId()
     {
         return playlistId;
     }
 
     /**
      * @return the name
      */
     public String getName()
     {
         return playlistName;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name)
     {
         this.playlistName = name;
     }
 
     /**
      * @return the created
      */
     public String getCreated()
     {
         return created;
     }
 
     /**
      * @param created the created to set
      */
     public void setCreated(String created)
     {
         this.created = created;
     }
     
      @Override
     public String toString()
     {
        return String.format("%-5d %-30s %-30s", playlistId, playlistName, created);
     }
 }
