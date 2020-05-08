 /*
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
 package com.trickl.crawler.protocol.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import org.apache.droids.api.ManagedContentEntity;
 import org.apache.droids.api.Parse;
 import org.apache.droids.api.Protocol;
 
 public class ResourceProtocol implements Protocol {
 
    private ClassLoader classLoader = ResourceProtocol.class.getClassLoader();
 
    public ResourceProtocol() {
    }
 
    public void setClassLoader(ClassLoader classLoader) {
       this.classLoader = classLoader;
    }
 
    @Override
    public boolean isAllowed(URI uri) {
       return true;
    }
 
    @Override
    public ManagedContentEntity load(URI uri) throws IOException {
       String location = extractLocation(uri);
       URL url = classLoader.getResource(location);
       if (url == null) {
          throw new IOException("Unable to find resource at '" + location + "'");
       }
       return new ResourceContentEntity(url);
    }
 
    private String extractLocation(URI uri) {
       String location = uri.toString();
      if (location.startsWith("classpath:/"))
      {
         location = location.substring("classpath:/".length());
       }
       return location;
    }
 
    static class ResourceContentEntity implements ManagedContentEntity {
 
       private final URL url;
       private final String mimeType;
       private final String charset;
       private Parse parse = null;
 
       public ResourceContentEntity(URL url) throws IOException {
          super();
          this.url = url;
          String s = url.getFile().toLowerCase();
          if (s.endsWith(".html") || s.endsWith(".htm")) {
             this.mimeType = "text/html";
             this.charset = "ISO-8859-1";
          } else if (s.endsWith(".txt")) {
             this.mimeType = "text/plain";
             this.charset = "ISO-8859-1";
          } else {
             this.mimeType = "binary/octet-stream";
             this.charset = null;
          }
       }
 
       @Override
       public InputStream obtainContent() throws IOException {
          return url.openStream();
       }
 
       @Override
       public void finish() {
       }
 
       @Override
       public String getMimeType() {
          return mimeType;
       }
 
       @Override
       public String getCharset() {
          return charset;
       }
 
       @Override
       public Parse getParse() {
          return this.parse;
       }
 
       @Override
       public void setParse(Parse parse) {
          this.parse = parse;
       }
    }
 }
