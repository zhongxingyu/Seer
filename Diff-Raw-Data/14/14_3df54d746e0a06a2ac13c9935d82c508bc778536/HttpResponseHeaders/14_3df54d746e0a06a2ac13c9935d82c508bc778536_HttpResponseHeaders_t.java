 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2010, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
  File:   HttpResponseHeaders.java
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 
 package com.janrain.android.engage.net.async;
 
 import android.util.Config;
 import android.util.Log;
 import com.janrain.android.engage.types.JRDictionary;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 
 /**
  * TODO: javadoc
  */
 public class HttpResponseHeaders {
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
     // Tag used for logging
     private static final String TAG = HttpResponseHeaders.class.getSimpleName();
 
     // HTTP response header for last modified (UTC) value
     public static final String HEADER_LAST_MODIFIED = "Last-Modified";
 
     // HTTP response header for ETag value
     public static final String HEADER_ETAG = "ETag";
 
     // Invalid response code (initial state of object)
     public static final int RESPONSE_CODE_INVALID = -1;
 

     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
     public static HttpResponseHeaders fromConnection(HttpURLConnection connection)
             throws IOException {
 
         if (Config.LOGD) {
             Log.d(TAG, "[fromConnection] BEGIN");
         }
         
         HttpResponseHeaders headers = new HttpResponseHeaders();
         
         headers.setResponseCode(connection.getResponseCode());
         headers.setContentEncoding(connection.getContentEncoding());
         headers.setContentLength(connection.getContentLength());
         headers.setContentType(connection.getContentType());
         headers.setDate(connection.getDate());
         headers.setLastModified(connection.getLastModified());
         headers.setLastModifiedUtc(connection.getHeaderField(HEADER_LAST_MODIFIED));
         headers.setETag(connection.getHeaderField(HEADER_ETAG));
        headers.setConnection(connection);
         
         return headers;
     }
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     private int mResponseCode;
     private String mContentEncoding;
     private int mContentLength;
     private String mContentType;
     private long mDate;
     private long mLastModified;
     private String mLastModifiedUtc;
     private String mETag;
 
    public void setConnection(HttpURLConnection connection) {
        this.mConnection = connection;
    }

    public String getHeaderField(String headerFieldName) {
        return mConnection.getHeaderField(headerFieldName);
    }

    private HttpURLConnection mConnection;

     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     public HttpResponseHeaders() {
         mResponseCode = RESPONSE_CODE_INVALID;
     }
     
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public int getResponseCode() {
         return mResponseCode;
     }
 
     public void setResponseCode(int responseCode) {
         mResponseCode = responseCode;
     }
 
     public String getContentEncoding() {
         return mContentEncoding;
     }
 
     public void setContentEncoding(String contentEncoding) {
         mContentEncoding = contentEncoding;
     }
 
     public int getContentLength() {
         return mContentLength;
     }
 
     public void setContentLength(int contentLength) {
         mContentLength = contentLength;
     }
 
     public String getContentType() {
         return mContentType;
     }
 
     public void setContentType(String contentType) {
         mContentType = contentType;
     }
 
     public long getDate() {
         return mDate;
     }
 
     public void setDate(long date) {
         mDate = date;
     }
 
     public long getLastModified() {
         return mLastModified;
     }
 
     public void setLastModified(long lastModified) {
         mLastModified = lastModified;
     }
 
     public String getLastModifiedUtc() {
         return mLastModifiedUtc;
     }
 
     public void setLastModifiedUtc(String lastModifiedUtc) {
         mLastModifiedUtc = lastModifiedUtc;
     }
 
     public String getETag() {
         return mETag;
     }
 
     public void setETag(String eTag) {
         mETag = eTag;
     }
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("HttpResponseHeaders [");
         sb.append(" Response Code: ").append(mResponseCode);
         sb.append(" | Content Encoding: ").append(mContentEncoding);
         sb.append(" | Content Length: ").append(mContentLength);
         sb.append(" | Content Type: ").append(mContentType);
         sb.append(" | Content Date: ").append(mDate);
         sb.append(" | Content Last Modified: ").append(mLastModified);
         sb.append(" | Content Last Modified UTC: ").append(mLastModifiedUtc);
         sb.append(" | ETag: ").append(mETag);
         sb.append(" ]");
         return sb.toString();
     }
 }
