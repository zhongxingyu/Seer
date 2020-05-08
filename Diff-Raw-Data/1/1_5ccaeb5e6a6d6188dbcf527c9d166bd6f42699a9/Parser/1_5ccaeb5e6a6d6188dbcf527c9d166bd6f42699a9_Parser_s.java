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
 package net.oneandone.jasmin.model;
 
 import net.oneandone.sushi.util.Separator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public final class Parser {
    /** @return variant */
     public static void parseComment(String str, List<String> depends, List<String> calls)  throws IOException {
         final String prefix = "/* jasmin";
         int idx;
         String key;
         String value;
 
         if (!str.startsWith(prefix)) {
             throw new IOException("missing jasmin comment");
         }
         idx = str.indexOf("*/");
         if (idx == -1) {
             throw new IOException("jasmin comment is not closed");
         }
         for (String[] line : parse(str.substring(prefix.length(), idx))) {
             key = line[0];
             value = line[1];
             if ("depend".equals(key)) {
                 depends.add(value);
             } else if ("call".equals(key)) {
                 calls.add(value);
             } else {
                 throw new IOException("unknown key: " + key);
             }
         }
     }
 
     private static final Separator NL = Separator.on('\n').trim().skipEmpty();
 
     public static List<String[]> parse(String str) throws IOException {
         int idx;
         List<String[]> result;
 
         result = new ArrayList<String[]>();
         for (String line : NL.split(str)) {
             idx = line.indexOf('=');
             if (idx == -1) {
                 throw new IOException("malformed jasmin comment: missing '=' in line '" + line + "'");
             }
             result.add(new String[] { line.substring(0, idx).trim(), line.substring(idx + 1).trim() });
         }
         return result;
     }
 
     private Parser() {
     }
 }
