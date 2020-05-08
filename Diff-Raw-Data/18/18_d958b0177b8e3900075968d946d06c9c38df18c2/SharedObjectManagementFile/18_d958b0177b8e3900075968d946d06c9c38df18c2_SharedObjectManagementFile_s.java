 package org.jenkinsci.plugins.sharedobjects.service;
 
 import hudson.model.Hudson;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectException;
 
import java.io.File;
 
 /**
  * @author Gregory Boissinot
  */
 public class SharedObjectManagementFile {
 
     public String getTemporaryFilePath(String sharedObjectName) throws SharedObjectException {
         if (sharedObjectName == null) {
             throw new SharedObjectException("A shared object name is required.");
         }
 
        File tmpFile = new File(Hudson.getInstance().getRootPath().child("userContent/sharedObjects/").getRemote() + "_TMP_FETCHED_" + sharedObjectName);
        tmpFile.mkdirs();
        return tmpFile.getAbsolutePath();
     }
 }
