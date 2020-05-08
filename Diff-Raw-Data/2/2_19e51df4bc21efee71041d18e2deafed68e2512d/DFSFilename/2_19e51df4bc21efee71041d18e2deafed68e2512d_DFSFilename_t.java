 
 public class DFSFilename {
 
   public static final String kPrefixString = "/dfs/";
 
   private int node;
   private String path;
 
   public DFSFilename(String fileName) {
     if (!fileName.startsWith(kPrefixString))
       throw new IllegalArgumentException("Invalid DFS filename");
 
 
     int nodeIdEnd = fileName.indexOf('/', kPrefixString.length());
     if (nodeIdEnd == -1 || fileName.length() == nodeIdEnd)
       throw new IllegalArgumentException("No path beyond the node id!");
 
     try {
       node = Integer.parseInt(
         fileName.substring(kPrefixString.length(), nodeIdEnd));
     } catch (NumberFormatException e) {
       throw new IllegalArgumentException("Node ID is nonnumeric!");
     }
 
     path = fileName.substring(nodeIdEnd + 1);
   }
 
   public int getOwningServer() {
     return node;
   }
 
   public String getPath() {
     return path;
   }
 
   @Override
   public String toString() {
    return kPrefixString + Integer.toString(node) + "/" + path;
   }
 
   @Override
   public int hashCode() {
     return toString().hashCode();
   }
 
   @Override
   public boolean equals(Object other) {
     if (!(other instanceof DFSFilename)) 
       return false;
 
     return ((DFSFilename) other).node == node && 
       ((DFSFilename) other).path.equals(path);
   }
 }
