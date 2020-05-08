 package de.cosmicsand.webtools.path;
 
 import static org.apache.commons.lang.StringUtils.isEmpty;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 
public class Path implements Comparable<Path> {
     public static final Path EMPTY_PATH = new Path();
     private final String rawPath;
 
     public Path(String rawPath) {
         this.rawPath = isEmpty(rawPath) ? "" : rawPath;
     }
 
     public Path() {
         this("");
     }
 
     @Override
     public String toString() {
         return rawPath;
     }
 
     @Override
     public boolean equals(Object obj) {
        if (obj instanceof Path)
             return rawPath.equals(((Path) obj).rawPath);
         return false;
     }
 
     @Override
     public int hashCode() {
         return rawPath.hashCode();
     }
 
     @Override
    public int compareTo(Path other) {
        // Path other = (Path) arg0;
         if (isEmpty(rawPath) && isEmpty(other.rawPath))
             return 0;
         else if (isNotEmpty(rawPath) && isEmpty(other.rawPath))
             return 1;
         else if (isEmpty(rawPath) && isNotEmpty(other.rawPath))
             return -1;
         return rawPath.compareTo(other.rawPath);
     }
 }
