 /*
  *
  *  Copyright (C) 2010 JFrog Ltd.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * /
  */
 
 package org.jfrog.wharf.ivy.resource;
 
 import org.apache.ivy.plugins.repository.Resource;
 import org.apache.ivy.plugins.repository.file.FileResource;
 import org.apache.ivy.plugins.repository.url.URLResource;
 import org.apache.ivy.util.url.URLHandlerRegistry;
 import org.jfrog.wharf.ivy.handler.WharfUrlHandler;
 import org.jfrog.wharf.ivy.util.WharfUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * @author Tomer Cohen
  */
 public class WharfUrlResource implements Resource {
     private URL url;
 
     private boolean init = false;
 
     private long lastModified;
 
     private long contentLength;
 
     private boolean exists;
 
     private String sha1;
 
     private String md5;
 
     public WharfUrlResource(URL url) {
         this.url = url;
     }
 
     public WharfUrlResource(Resource resource) {
         if (resource instanceof FileResource) {
             try {
                 url = ((FileResource) resource).getFile().toURI().toURL();
             } catch (MalformedURLException e) {
                 throw new IllegalArgumentException("Malformed File URL", e);
             }
         } else if (resource instanceof URLResource) {
             url = ((URLResource) resource).getURL();
         } else if (resource instanceof WharfUrlResource) {
            WharfUrlResource wharfUrlResource = (WharfUrlResource) resource;
            this.url = wharfUrlResource.url;
            this.init = wharfUrlResource.init;
            this.lastModified = wharfUrlResource.lastModified;
            this.contentLength = wharfUrlResource.contentLength;
            this.exists = wharfUrlResource.exists;
            this.sha1 = wharfUrlResource.sha1;
            this.md5 = wharfUrlResource.md5;
         } else {
             throw new IllegalArgumentException("Wharf Downloader manage only URL and Files");
         }
     }
 
     @Override
     public String getName() {
         return url.toExternalForm();
     }
 
     @Override
     public Resource clone(String cloneName) {
         try {
             return new WharfUrlResource(new URL(cloneName));
         } catch (MalformedURLException e) {
             try {
                 return new WharfUrlResource(new File(cloneName).toURI().toURL());
             } catch (MalformedURLException e1) {
                 throw new IllegalArgumentException(
                         "bad clone name provided: not suitable for an URLResource: " + cloneName);
             }
         }
     }
 
     @Override
     public long getLastModified() {
         if (!init) {
             init();
         }
         return lastModified;
     }
 
     private void init() {
         WharfUrlHandler.WharfUrlInfo info = new WharfUrlHandler().getURLInfo(url);
         contentLength = info.getContentLength();
         lastModified = info.getLastModified();
         exists = info.isReachable();
         sha1 = WharfUtils.getCleanChecksum(info.getSha1());
         md5 = WharfUtils.getCleanChecksum(info.getMd5());
         init = true;
     }
 
     @Override
     public long getContentLength() {
         if (!init) {
             init();
         }
         return contentLength;
     }
 
     public String getSha1() {
         if (!init) {
             init();
         }
         return sha1;
     }
 
     public String getMd5() {
         if (!init) {
             init();
         }
         return md5;
     }
 
     @Override
     public boolean exists() {
         if (!init) {
             init();
         }
         return exists;
     }
 
     public String toString() {
         return getName();
     }
 
     @Override
     public boolean isLocal() {
         return false;
     }
 
     @Override
     public InputStream openStream() throws IOException {
         return URLHandlerRegistry.getDefault().openStream(url);
     }
 }
