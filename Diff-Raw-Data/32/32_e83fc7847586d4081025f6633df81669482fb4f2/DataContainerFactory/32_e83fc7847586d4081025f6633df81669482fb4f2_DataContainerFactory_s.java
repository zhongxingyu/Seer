 package com.artcom.y60.data;
 
 import java.io.IOException;
 
 public abstract class DataContainerFactory {
    
     public abstract StreamableContent createStreamableContent(String pMimeType, String pFilename)
            throws IOException, UnknownContentTypeException;
    
 }
