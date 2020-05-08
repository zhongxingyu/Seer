 package fnug.servlet;
 
 import java.io.UnsupportedEncodingException;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import fnug.ResourceServlet;
 import fnug.resource.Bundle;
 import fnug.resource.ResourceResolver;
 
 /*
  Copyright 2010 Martin Algesten
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 public class Bootstrap implements ToServe {
 
     private static final String BOOTSTRAP_ADD = "fnug.loadBundle(\"/***bundles***/\");";
 
     private static final String TOKEN_BASE_URL = "/***baseUrl***/";
     private static final String TOKEN_BUNDLES = "\"/***bundles***/\"";
 
     private static final int LINE_LENGTH = 1000;
 
     private byte[] bytes;
     private long lastModified;
 
     public Bootstrap(ObjectMapper mapper, String baseUrl, Bundle bundle, boolean add) {
 
         if (baseUrl.endsWith("/")) {
             baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
         }
 
         Bundle fnug = ResourceResolver.getInstance().getBundle("fnug");
 
         // when developing bootstrap, turn on checkModified in /fnug/bundles.js.
         fnug.checkModified();
 
         String bootstrapJs;
 
         if (add) {
             bootstrapJs = BOOTSTRAP_ADD;
         } else {
             // no strange utf-8 chars in bootstrap
             bootstrapJs = new String(fnug.getResourceCollections()[0].getCompressedJs().getBytes());
             bootstrapJs = new String(fnug.getResourceCollections()[0].getJs());
         }
 
         this.lastModified = Math.max(bundle.getLastModified(), fnug.getLastModified());
 
         JsonBundle jb = new JsonBundle(bundle);
         String jbs;
 
         try {
             jbs = mapper.writeValueAsString(jb);
         } catch (Exception e) {
             throw new RuntimeException("Failed to generate json", e);
         }
 
         // Spliced in as string and parsed with JSON.parse().
         jbs = escape(jbs, LINE_LENGTH);
 
         String result = bootstrapJs;
 
         result = result.replace(TOKEN_BASE_URL, baseUrl);
         result = result.replace(TOKEN_BUNDLES, jbs);
 
         try {
             bytes = result.getBytes(ResourceServlet.UTF_8);
         } catch (UnsupportedEncodingException e) {
             // nope
         }
 
     }
 
     protected static String escape(String s, int length) {
 
         s = s.replace("\\", "\\\\");
         s = s.replace("\"", "\\\"");
 
         String result = "";
         int start = 0;
         int end = 0;
 
         while (end + length < s.length()) {
             start = end;
             end = start + length;
            while (s.charAt(end) == '\\')
                 end--;
             result += s.substring(start, end) + "\"+\n\"";
         }
 
         result += s.substring(end, s.length());
 
         result = "\"" + result + "\"";
 
         return result;
 
     }
 
     public byte[] getBytes() {
         return bytes;
     }
 
     public long getLastModified() {
         return lastModified;
     }
 
     @Override
     public boolean futureExpires() {
         return false;
     }
 
     @Override
     public String getContentType() {
         return ResourceServlet.CONTENT_TYPE_JS;
     }
 
 }
