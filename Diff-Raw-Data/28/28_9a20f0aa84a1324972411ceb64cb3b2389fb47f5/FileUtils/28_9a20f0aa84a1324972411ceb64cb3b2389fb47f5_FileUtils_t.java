 package com.atlassian.plugin.util;
 
 import com.atlassian.plugin.util.zip.UrlUnzipper;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 public class FileUtils
 {
     private static final Log log = LogFactory.getLog(FileUtils.class);
 
     /**
      * Extract the zip from the URL into the destination directory, but only if the contents haven't already been
      * unzipped.  If the directory contains different contents than the zip, the directory is cleaned out
      * and the files are unzipped.
      *
      * @param zipUrl The zip url
      * @param destDir The destination directory for the zip contents
      */
     public static void conditionallyExtractZipFile(URL zipUrl, File destDir)
     {
         try
         {
             UrlUnzipper unzipper = new UrlUnzipper(zipUrl, destDir);
             unzipper.conditionalUnzip();
         }
         catch (IOException e)
         {
             log.error("Found " + zipUrl + ", but failed to read file", e);
         }
     }
 
    /**
     * @deprecated Since 2.0.0
     */
    public static void deleteDir(File directory)
    {
        try
        {
            org.apache.commons.io.FileUtils.deleteDirectory(directory);
        } catch (IOException e)
        {
            log.error("Unable to delete directory: "+directory.getAbsolutePath(), e);
        }
    }

 }
