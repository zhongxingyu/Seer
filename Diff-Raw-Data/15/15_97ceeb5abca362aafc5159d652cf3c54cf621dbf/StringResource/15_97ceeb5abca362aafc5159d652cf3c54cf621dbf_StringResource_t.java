 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.resourceloaders;
 
 import ru.histone.utils.IOUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.UnsupportedEncodingException;
 
 /**
  * @author sazonovkirill@gmail.com
  */
 public class StringResource implements Resource {
     private final String content;
     private final String baseHref;
     private final String contentType;
     private final InputStream inputStream;
 
     public StringResource(String content, String baseHref, String contentType) {
         this.content = content;
         this.baseHref = baseHref;
         this.contentType = contentType;
        try {
            this.inputStream = new ByteArrayInputStream(content.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding error",e);
        }
     }
 
     @Override
     public InputStream getInputStream() throws IOException {
         return inputStream;
     }
 
     @Override
     public String getBaseHref() {
         return baseHref;
     }
 
     @Override
     public String getContentType() {
         return contentType;
     }
 
     @Override
     public void close() throws IOException {
         IOUtils.closeQuietly(inputStream);
     }
 }
