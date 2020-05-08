 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.cachedetails;
 
 import com.google.code.geobeagle.xmlimport.EmotifierPatternProvider;
 import com.google.inject.Inject;
 
 import java.util.regex.Matcher;
 
 public class Emotifier {
     public static final String ICON_PREFIX = "<img src='file:///android_asset/";
    public static final String ICON_SUFFIX = ".gif' border=0 align=bottom>";
     public static final String EMOTICON_PREFIX = ICON_PREFIX + "icon_smile_";
     private final EmotifierPatternProvider patternProvider;
 
     @Inject
     public Emotifier(EmotifierPatternProvider patternProvider) {
         this.patternProvider = patternProvider;
     }
 
     String emotify(String text) {
         Matcher matcher = patternProvider.get().matcher(text);
         StringBuffer sb = new StringBuffer();
         while (matcher.find()) {
             String group = matcher.group(1);
             String replacement = group.replace(":", "");
             matcher.appendReplacement(sb, EMOTICON_PREFIX + replacement + ICON_SUFFIX);
         }
         matcher.appendTail(sb);
         return sb.toString();
     }
 }
