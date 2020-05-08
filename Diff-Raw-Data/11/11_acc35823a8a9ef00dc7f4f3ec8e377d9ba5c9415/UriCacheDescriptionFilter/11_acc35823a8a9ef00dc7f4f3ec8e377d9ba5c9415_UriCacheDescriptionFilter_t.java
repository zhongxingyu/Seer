 package org.hackystat.sensorbase.uricache;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.regex.Pattern;
 
 /**
  * Filters UriCache description file names.
  * 
  * @author Pavel Senin.
  * 
  */
 public class UriCacheDescriptionFilter implements FilenameFilter {
 
   /** Pattern for the .desc files. */
   private Pattern pattern = Pattern.compile(".*\\.desc");
 
   /**
   * Filters out only cache description file names.
   * 
   * @param dir the folder name.
   * @param fileName the file name.
   * 
   * @return true if the file name is cache description.
    */
  public boolean accept(File dir, String fileName) {
     return pattern.matcher(fileName).matches();
   }
 
 }
