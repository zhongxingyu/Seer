 package org.jenkinsci.plugins.sharedobjects.service;
 
import hudson.FilePath;
 import hudson.model.Hudson;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectException;
 
import java.io.IOException;
 
 /**
  * @author Gregory Boissinot
  */
 public class SharedObjectManagementFile {
 
     public String getTemporaryFilePath(String sharedObjectName) throws SharedObjectException {
         if (sharedObjectName == null) {
             throw new SharedObjectException("A shared object name is required.");
         }
 
        FilePath tmpDir = Hudson.getInstance().getRootPath().child("userContent/sharedObjects");
        try {
            tmpDir.mkdirs();
        } catch (IOException e) {
            throw new SharedObjectException(e);
        } catch (InterruptedException e) {
            throw new SharedObjectException(e);
        }
        FilePath tmpFile = tmpDir.child("_TMP_FETCHED_" + sharedObjectName);
        return tmpFile.getRemote();
     }
 }
