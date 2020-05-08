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
     * 
     * @throws XmcdFormatException on illegal directory name
      */
    public static FreedbGenre fromDirectoryName(String dirName) throws XmcdFormatException {
        try {
            return FreedbGenre.valueOf(dirName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new XmcdFormatException("Illegal directory name " + dirName);
        }
     }
 
     /**
      * Returns the FreeDB directory name that corresponds to this genre.
      */
     public String toDirName() {
         return name().toLowerCase();
     }
 }
