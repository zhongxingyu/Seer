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
 
 package com.google.code.geobeagle.xmlimport;
 
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
 import android.util.Log;
 
 import java.util.regex.Pattern;
 
 @Singleton
 public class EmotifierPatternProvider implements Provider<Pattern> {
     static Pattern createEmotifierPattern(String[] emoticons) {
         StringBuffer keysBuffer = new StringBuffer();
        String escapeChars = "()|?}^";
         for (String emoticon : emoticons) {
             String key = new String(emoticon);
             for (int i = 0; i < escapeChars.length(); i++) {
                 char c = escapeChars.charAt(i);
                 key = key.replaceAll("\\" + String.valueOf(c), "\\\\" + c);
             }
             keysBuffer.append("|" + key);
         }
         keysBuffer.deleteCharAt(0);
         final String keys = "\\[(" + keysBuffer.toString() + ")\\]";
         try {
             return Pattern.compile(keys);
         } catch (Exception e) {
             Log.d("GeoBeagle", e.getLocalizedMessage());
         }
         return null;
     }
 
     private Pattern pattern;
 
     @Override
     public Pattern get() {
         if (pattern != null)
             return pattern;
         String emoticons[] = {
                 ":(", ":o)", ":)", ":D", "8D", ":I", ":P", "}:)", ":)", ":D", "8D", ":I", ":P",
                 "}:)", ";)", "B)", "8", ":)", "8)", ":O", ":(!", "xx(", "|)", ":X", "V", "?",
                 "^"
         };
         pattern = createEmotifierPattern(emoticons);
         return pattern;
     }
 }
