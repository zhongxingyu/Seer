 /*
 Copyright 2012 Get AS
 
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
 package no.get.cms.plugin.resourcecompressor.content;
 
 import no.get.cms.plugin.resourcecompressor.ContentLoader;
 import org.apache.commons.codec.digest.DigestUtils;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /*
  * Replace multiple <script src=""></script> tags with
  * one script tag which includes all Javascript files combined into one file.
  *
  * Does also support inlined Javascript like
  * <script type="text/javascript">var test = "HelloWorld";</script>
  */
 public class JsCompressedContent implements CompressedContent {
 
     // Does not support </script> tags inside inlined Javascript or nested tags.
    final Pattern pattern = Pattern.compile("<script\\s.*?src=(['\"])(.*?)\\1(?:.*?)>|<script.*?>(.*?)</script>",
            Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
 
     final private String path;
     final private String content;
     final private String baseUrl;
     final private String filename;
 
     private String compressedContent;
 
     private ContentLoader contentLoader = new ContentLoader();
 
     public JsCompressedContent(String baseUrl, String path, String content) {
         this.baseUrl = baseUrl;
         this.path = path;
         this.content = content;
         this.filename = DigestUtils.md5Hex(content) + ".js";
     }
 
     @Override
     public String getContentReplacement() {
         return "<!-- get.compressor js -->" +
                 "<script src=\"" + baseUrl + path + getFilename() + "\" type=\"text/javascript\"></script>" +
                 "<!-- get.compressor end -->";
     }
 
     @Override
     public String getContentType() {
         return "application/javascript;charset=UTF-8";
     }
 
     @Override
     public String getFilename() {
         return filename;
     }
 
     @Override
     public String getCompressedContent() {
         if (compressedContent == null) {
             loadCompressedContent();
         }
         return compressedContent;
     }
 
     /**
      * Synchronized to allow multiple client requests, but load content only for the first client.
      */
     private synchronized void loadCompressedContent() {
         if (compressedContent == null) {
             compressedContent = parseResourceUrls();
         }
     }
 
     String parseResourceUrls() {
         final StringBuilder combinedResources = new StringBuilder();
         final Matcher matcher = pattern.matcher(content);
         while (matcher.find()) {
             final String scriptSrc = matcher.group(2);
             final String inlinedJavascript = matcher.group(3);
            if (inlinedJavascript != null) {
                combinedResources.append(inlinedJavascript);
            } else {
                 final String src = getAbsoluteUrl(scriptSrc);
                 final String body = contentLoader.load(src);
                 combinedResources.append(body);
             }
             combinedResources.append("\n");
         }
         return combinedResources.toString().trim();
     }
 
     private String getAbsoluteUrl(String url) {
         if (!url.startsWith("http")) {
             return baseUrl + url;
         }
         return url;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         JsCompressedContent that = (JsCompressedContent) o;
 
         if (!content.equals(that.content)) return false;
         if (!filename.equals(that.filename)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return filename.hashCode();
     }
 
     public void setContentLoader(ContentLoader contentLoader) {
         this.contentLoader = contentLoader;
     }
 }
