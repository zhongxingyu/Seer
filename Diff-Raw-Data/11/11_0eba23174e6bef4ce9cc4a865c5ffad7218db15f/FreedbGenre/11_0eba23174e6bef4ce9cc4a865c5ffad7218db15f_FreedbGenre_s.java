 package my.triviagame.xmcd;
 
 /**
  * FreeDB genres.
  * Names correspond to folder name in FreeDB archives).
  * Ordinals correspond to the ordinals used in our database.
  */
 public enum FreedbGenre {
     BLUES,
     CLASSICAL,
     COUNTRY,
     DATA,
     FOLK,
     JAZZ,
     MISC,
     NEWAGE,
     REGGAE,
     ROCK,
     SOUNDTRACK;
 
     /**
      * Initializes from a FreeDB directory name.
      */
    public static FreedbGenre fromDirectoryName(String dirName) throws IllegalArgumentException {
        return FreedbGenre.valueOf(dirName.toUpperCase());
     }
 
     /**
      * Returns the FreeDB directory name that corresponds to this genre.
      */
     public String toDirName() {
         return name().toLowerCase();
     }
 }
