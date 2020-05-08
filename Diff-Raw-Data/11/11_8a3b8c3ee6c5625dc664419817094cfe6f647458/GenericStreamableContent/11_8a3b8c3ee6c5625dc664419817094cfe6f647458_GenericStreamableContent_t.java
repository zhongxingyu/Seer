 package com.hoccer.data;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class GenericStreamableContent implements StreamableContent {
 
     String                              mFilename     = "filename.unkonwn";
     String                              mContentType;
     private final ByteArrayOutputStream mResultStream = new ByteArrayOutputStream();
 
     public void setContentType(String pContentType) {
         mContentType = pContentType;
     }
 
     @Override
     public String getContentType() {
         return mContentType;
     }
 
     public String getFilename() {
         return mFilename;
     }
 
     public void setFilename(String mFilename) {
         this.mFilename = mFilename;
     }
 
     @Override
     public InputStream openInputStream() throws IOException {
         return new ByteArrayInputStream(mResultStream.toByteArray());
     }
 
     @Override
    public long getStreamLength() throws IOException {
         return mResultStream.size();
     }
 
     @Override
     public OutputStream openOutputStream() throws IOException {
         return mResultStream;
     }
 
     @Override
     public String toString() {
         if (mContentType != null
                 && (mContentType.contains("text") || mContentType.contains("json"))) {
             return mResultStream.toString();
         }
 
         return mFilename + " (" + mContentType + ")";
     }
 }
