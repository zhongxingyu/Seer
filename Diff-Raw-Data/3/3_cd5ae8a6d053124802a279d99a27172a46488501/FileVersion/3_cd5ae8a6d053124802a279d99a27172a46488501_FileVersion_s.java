 
 /**
  * Version object for transactioned cache entries.
  */
 public class FileVersion implements Comparable<FileVersion> {
   
   public static FileVersion parse(byte[] arr) {
     return parse(new String(arr));
   }
 
   public static FileVersion parse(String s) {
     String[] parts = s.split("-");
     if (parts.length != 3)
       throw new NumberFormatException("Not enough versions!");
     
     return new FileOwner(Integer.parseInt(parts[0]),
                          Integer.parseInt(parts[1]),
                          Integer.parseInt(parts[2]));
   }
 
   private int owner;
   private int version;
   private int revision;
 
   /**
    * Construct a new FileVersion for a file owned by the given
    * owner at the given version.
    */
   public FileVersion(int owner, int version, int revision) {
     this.owner = owner;
     this.version = version;
     this.revision = revision;
   }
 
   public void setVersion(int newVersion) {
     version = newVersion;
   }
 
   public int getOwner() {
     return owner;
   }
 
   public int getVersion() {
     return version;
   }
 
   public int getRevision() {
     return revision;
   }
 
   public void incrementRevision() {
     revision++;
   }
 
   @Override
   public int compareTo(FileVersion other) {
     return other.version - version;
   }
 
   @Override
   public int hashCode() {
     return Integer.hashCode(version ^ owner);
   }
 
   @Override
   public boolean equals(Object o) {
     if (o instanceof FileVersion) {
       return ((FileVersion) o).owner == owner &&
        ((FileVersion) o).version = version;
     }
     return false;
   }
 
   @Override
   public String toString() {
     return "" + owner + "-" + version + "-" + revision;
   }
 }
