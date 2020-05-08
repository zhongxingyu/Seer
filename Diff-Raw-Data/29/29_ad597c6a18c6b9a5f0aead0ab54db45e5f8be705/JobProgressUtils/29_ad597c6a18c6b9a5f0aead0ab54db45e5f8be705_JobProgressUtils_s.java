 package com.norconex.jef.progress;
 
 import java.io.IOException;
import java.util.Properties;
 
import com.norconex.commons.lang.map.TypedProperties;
 import com.norconex.jef.JobException;
 
 /**
  * Utility methods for facilitating common job progress operations.
  * @author Pascal Essiembre (pascal.essiembre&#x40;norconex.com)
  * @since 1.1
  */
 @SuppressWarnings("nls")
 public final class JobProgressUtils {
 
     private JobProgressUtils() {
         super();
     }
 
     /**
     * Gets meta data as a {@link TypedProperties} instance.  This is a 
      * convenience method for treating the meta data string as properties.
      * It calls {@link JobProgress#getMetadata()} and parses it assuming it
      * follows {@link Properties} syntax.
      * @return configuration properties
      */
    public static TypedProperties getMetaDataProperties(JobProgress progress) {
         if (progress == null || progress.getMetadata() == null) {
             return null;
         }
        TypedProperties props = new TypedProperties();
         try {
             props.loadFromString(progress.getMetadata());
         } catch (IOException e) {
             throw new JobException(
                     "Cannot convert progress meta data to properties.", e);
         }
         return props;
     }
 
     /**
      * Sets meta data as a string from a {@link ConfigProperties} instance.
      * This is a convenience method for treating the meta data string as 
      * properties.
      * It calls {@link JobProgress#setMetadata()} after converting the
      * properties to a string as per {@link Properties} syntax.
      * @param progress job progress on which to store meta data
      * @param props properties to store in job progress
      */
     public static void setMetaDataProperties(
            JobProgress progress, TypedProperties props) {
         if (progress == null || props == null) {
             return;
         }
         try {
             progress.setMetadata(props.storeToString(null));
         } catch (IOException e) {
             throw new JobException(
                     "Cannot convert properties to progress meta data.", e);
         }
     }
 }
