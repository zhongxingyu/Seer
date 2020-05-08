 package com.artcom.y60.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.ContentResolver;
 import android.net.Uri;
 
 public abstract class AndroidStreamableContent implements StreamableContent {
 
     ContentResolver mContentResolver;
     private Uri     mContentResolverUri;
     String          mContentType;
 
     public AndroidStreamableContent(ContentResolver pContentResolver) {
         mContentResolver = pContentResolver;
     }
 
     public Uri getContentResolverUri() {
         return mContentResolverUri;
     }
 
    protected void setContentResolverUri(Uri dataLocation) throws BadContentResolverUriException {
        if (dataLocation == null) {
            throw new BadContentResolverUriException();
        }

         mContentResolverUri = dataLocation;
     }
 
     public OutputStream openOutputStream() throws IOException {
         return mContentResolver.openOutputStream(getContentResolverUri());
     }
 
     public InputStream openInputStream() throws IOException {
         return mContentResolver.openInputStream(getContentResolverUri());
     }
 
     public String getContentType() {
         return mContentResolver.getType(getContentResolverUri());
     }
 
     @Override
     public long getStreamLength() throws IOException {
         return mContentResolver.openAssetFileDescriptor(getContentResolverUri(), "r").getLength();
     }
 }
