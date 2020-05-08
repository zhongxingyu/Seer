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
   * {@inheritDoc}
    */
  @Override
  public boolean accept(File path, String fileName) {
     return pattern.matcher(fileName).matches();
   }
 
 }
