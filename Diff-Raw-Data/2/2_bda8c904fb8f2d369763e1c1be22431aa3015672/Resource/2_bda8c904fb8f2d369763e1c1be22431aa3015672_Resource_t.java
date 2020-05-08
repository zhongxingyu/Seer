 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.lavender.modules;
 
 import net.oneandone.lavender.index.Hex;
 import net.oneandone.lavender.index.Label;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public abstract class Resource {
     public abstract String getPath();
     public abstract long getLastModified() throws IOException;
 
     /** for logging purpose */
     public abstract String getOrigin();
 
     public abstract long getSize();
 
     public abstract byte[] getMd5() throws IOException;
 
     public abstract byte[] getData() throws IOException;
 
     public abstract boolean isOutdated();
     
     public Label labelNormal(String targetPathPrefix) throws IOException {
         String path;
 
         path = getPath();
         return new Label(path, targetPathPrefix + path, getMd5());
     }
 
     public Label labelLavendelized(String targetPathPrefix, String folder) throws IOException {
         String path;
         String filename;
         String md5str;
 
         path = getPath();
         filename = path.substring(path.lastIndexOf('/') + 1); // ok when not found
         md5str = Hex.encodeString(getMd5());
         if (md5str.length() < 3) {
             throw new IllegalArgumentException(md5str);
         }
         return new Label(path, targetPathPrefix + md5str.substring(0, 3) + "/" + md5str.substring(3) + "/" + folder + "/" + filename, getMd5());
     }
 
     @Override
     public String toString() {
         return getPath() + "[" + getSize() + "] ->" + getOrigin();
     }
 
     //--
 
     private static final MessageDigest DIGEST;
 
     static {
         try {
             DIGEST = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e.getMessage());
         }
     }
 
    public static synchronized byte[] md5(byte ... data) {
         DIGEST.update(data, 0, data.length);
         return DIGEST.digest();
     }
 
 }
