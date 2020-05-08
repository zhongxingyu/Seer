 /*
  * Copyright LGPL3
  * YES Technology Association
  * http://yestech.org
  *
  * http://www.opensource.org/licenses/lgpl-3.0.html
  */
 
 /*
  *
  * Author:  Artie Copeland
  * Last Modified Date: $DateTime: $
  */
 package org.yestech.publish.objectmodel;
 
 import org.yestech.lib.util.Pair;
 
 /**
  * @author Artie Copeland
  * @version $Revision: $
  */
 public interface IFileArtifactMetaData<O extends IArtifactOwner, I> extends IArtifactMetaData<O , I> {
 
     String getMimeType();
 
     void setMimeType(String mimeType);
 
     String getFileName();
 
     void setFileName(String fileName);
 
     long getSize();
 
     void setSize(long size);
 
     void setLocation(String location);
 
     String getLocation();
 
     /**
      * Returns the Path, FileName pair
      *
      * @return the pair
      */
     Pair<String, String> getUniqueNames();
 
     /**
      * Sets the Path, FileName pair
      *
      * @param uniqueNames first val - path, second val - filename
      */
    void setUniqueFileName(Pair<String, String> uniqueNames);
 
 }
