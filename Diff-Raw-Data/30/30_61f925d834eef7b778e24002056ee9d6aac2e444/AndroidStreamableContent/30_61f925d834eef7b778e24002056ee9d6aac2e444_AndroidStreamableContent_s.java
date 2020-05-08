 /**
  * Copyright (C) 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  *
  * These coded instructions, statements, and computer programs contain
 * proprietary information of Linccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Android-API".
  * 
  * Linccer Android-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Android-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Android-API. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.hoccer.api.android;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.ContentResolver;
 import android.net.Uri;
 import android.util.Log;
 
 import com.hoccer.data.GenericStreamableContent;
 
 public abstract class AndroidStreamableContent extends GenericStreamableContent {
 
     private static final String LOG_TAG = "AndroidStreamableContent";
     ContentResolver             mContentResolver;
     private Uri                 mDataUri;
     protected String            mContentType;
 
     public AndroidStreamableContent(ContentResolver pContentResolver) {
         mContentResolver = pContentResolver;
     }
 
     public Uri getDataUri() {
         return mDataUri;
     }
 
     protected void setDataUri(Uri dataLocation) throws BadContentResolverUriException {
         if (dataLocation == null) {
             throw new BadContentResolverUriException("Could not retrieve content");
         }
 
         mDataUri = dataLocation;
     }
 
     // override this in subclass, if you dont set a contentresolver uri
     @Override
     public OutputStream openOutputStream() throws IOException {
         return mContentResolver.openOutputStream(getDataUri());
     }
 
     // override this in subclass, if you dont set a contentresolver uri
     @Override
     public InputStream openInputStream() throws IOException {
         return mContentResolver.openInputStream(getDataUri());
     }
 
     @Override
     public String getContentType() {
 
         if (getDataUri() != null) {
             String contentType = mContentResolver.getType(getDataUri());
             if (contentType != null) {
                 return contentType;
             }
         }
 
         return mContentType;
     }
 
     // override this in subclass, if you dont set a contentresolver uri
     @Override
     public long getStreamLength() throws IOException {
         if (mDataUri == null) {
             Log.e(LOG_TAG, "no valid content resolver uri!");
         }
 
         return mContentResolver.openAssetFileDescriptor(getDataUri(), "r").getLength();
     }
 
     protected boolean isFileSchemeUri() {
         return "file".equals(mDataUri.getScheme());
     }
 }
