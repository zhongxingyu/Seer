 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class Util {
 
     //TODO: Rewrite this function. (PHP deserialize)
     public String forumCache(String cache, String player, int userid, String nummember, String activemembers, String newusername, String newuserid, String extrausername, String lastvalue) {
         StringTokenizer st = new StringTokenizer(cache, ":");
         int i = 0;
         List<String> array = new ArrayList<String>();
         while (st.hasMoreTokens()) { array.add(st.nextToken() + ":"); }
         StringBuffer newcache = new StringBuffer();
         while (array.size() > i) {
             if (array.get(i).equals("\"" + nummember + "\";i:") && nummember != null) {
                 String temp = array.get(i + 1);
                 temp = removeChar(temp, '"');
                 temp = removeChar(temp, ':');
                 temp = removeChar(temp, 's');
                 temp = removeChar(temp, ';');
                 temp = temp.trim();
                 int tempnum = Integer.parseInt(temp) + 1;
                 if (lastvalue.equalsIgnoreCase(nummember)) {
                     temp = tempnum + ";}";
                 } else {
                     temp = tempnum + ";s:";
                 }
                 array.set(i + 1, temp);
             } else if (array.get(i).equals("\"" + newusername + "\";s:") && newusername != null) {
                 array.set(i + 1, player.length() + ":");
                 if (lastvalue.equalsIgnoreCase(newusername)) {
                     array.set(i + 2, "\"" + player + "\"" + ";}");
                 } else {
                     array.set(i + 2, "\"" + player + "\"" + ";s" + ":");
                 }
             } else if (array.get(i).equals("\"" + extrausername + "\";s:") && extrausername != null) {
                 array.set(i + 1, player.length() + ":");
                 if (lastvalue.equalsIgnoreCase(extrausername)) {
                     array.set(i + 2, "\"" + player + "\"" + ";}");
                 } else {
                     array.set(i + 2, "\"" + player + "\"" + ";s" + ":");
                 }
             } else if (array.get(i).equals("\"" + activemembers + "\";s:") && activemembers != null) {
                 String temp = array.get(i + 2);
                 temp = removeChar(temp, '"');
                 temp = removeChar(temp, ':');
                 temp = removeChar(temp, 's');
                 temp = removeChar(temp, ';');
                 temp = temp.trim();
                 int tempnum = Integer.parseInt(temp) + 1;
                 String templength = "" + tempnum;
                 if (lastvalue.equalsIgnoreCase(activemembers)) {
                     temp = "\"" + tempnum + "\"" + ";}";
                 } else {
                     temp = "\"" + tempnum + "\"" + ";s:";
                 }
                 array.set(i + 1, templength.length() + ":");
                 array.set(i + 2, temp);
             } else if (array.get(i).equals("\"" + newuserid + "\";s:") && newuserid != null) {
                 String dupe = "" + userid;
                 array.set(i + 1, dupe.length() + ":");
                 if (lastvalue.equalsIgnoreCase(newuserid)) {
                     array.set(i + 2, "\"" + userid + "\"" + ";}");
                 } else {
                     array.set(i + 2, "\"" + userid + "\"" + ";s:");
                 }
             }
             newcache.append(array.get(i));
             i++;
         }
         return newcache.toString();
     }
 
     public String forumCacheValue(String cache, String value) {
         StringTokenizer st = new StringTokenizer(cache, ":");
         int i = 0;
         List<String> array = new ArrayList<String>();
         while (st.hasMoreTokens()) { array.add(st.nextToken() + ":"); }
         while (array.size() > i) {
             if (array.get(i).equals("\"" + value + "\";s:") && value != null) {
                 String temp = array.get(i + 2);
                 temp = removeChar(temp, '"');
                 temp = removeChar(temp, ':');
                 temp = removeChar(temp, 's');
                 temp = removeChar(temp, ';');
                 temp = temp.trim();
                 return temp;
             }
             i++;
         }
         return null;
     }
 
     public String removeChar(String s, char c) {
        StringBuffer r = new StringBuffer(s.length());
         r.setLength(s.length());
         int current = 0;
         for (int i = 0; i < s.length(); i++) {
             char cur = s.charAt(i);
             if (cur != c) r.setCharAt(current++, cur);
         }
         return r.toString();
     }
 
     public HashMap<String, Object> loadYaml(InputStream yamlStream) throws IOException {
         HashMap<String, Object> yaml = new HashMap<String, Object>();
         if (yamlStream != null) {
             try {
                 InputStreamReader yamlStreamReader = new InputStreamReader (yamlStream);
                 BufferedReader buffer = new BufferedReader (yamlStreamReader);
                 String line, node = "";
                 int lastBlank = 0;
                 boolean isNode = false;
                 while  ((line = buffer.readLine()) != null) {
                     int blank = 0;
                     if (line.length() > 0 && line.charAt(line.length() - 1) == ':') {
                         for (int i=0; i < line.length(); i++) {
                             if (Character.isWhitespace(line.charAt(i))) {
                                 blank++;
                             }
                         }
                         line = line.replaceAll("\\s+", "");
                         line = line.replaceAll(":", "");
                         if (blank == 0) {
                             node = line + ".";
                         } else if (blank > lastBlank) {
                             node += line + ".";
                         } else if (blank <= lastBlank) {
                             String[] split = node.split("\\.");
                             if ((lastBlank - blank) > 0) {
                                 for (int i = 1; ((lastBlank - blank) / 4) >= i; i++) {
                                     node = node.replace("." + split[split.length - i], "");
                                 }
                             } else {
                                 node = node.replace("." + split[split.length - 1], "");
                             }
                             node += line + ".";
                         }
                         lastBlank = blank;
                         isNode = true;
                     } else if (line.length() > 0) {
                         boolean set = false;
                         for (int i=0; i < line.length() && ! set; i++) {
                             if (Character.isWhitespace(line.charAt(i))) {
                                 blank++;
                             } else {
                                 set = true;
                             }
                         }
 
                        String[] split = line.split("\\:");
                         String finalNode = split[0].replaceAll("\\s+", "");
                         if (finalNode.startsWith("#")) {
                             continue;
                         }
                         if (! isNode && blank > lastBlank) {
                             node += finalNode + ".";
                         } else if (! isNode && blank < lastBlank) {
                             String[] spl = node.split("\\.");
                             node = node.replace("." + spl[spl.length - 1], "");
                         }
                         lastBlank = blank;
                         String temp = split[1].substring(1);
                         if (split.length > 1) {
                             for(int i=2; split.length > i; i++){
                                 temp += ":" + split[i];
                             }
                         }
                         int index = temp.lastIndexOf("#");
                         if (index != -1 && Character.isWhitespace(temp.charAt(index - 1))) {
                             temp = temp.substring(0, index - 1);
                         }
                         String value = "";
                         char last = 0;
                         for (int i=0; i < temp.length(); i++) {
                             if (Character.isWhitespace(temp.charAt(i)) && Character.isWhitespace(last)) {
                                 continue;
                             }
                             value += temp.charAt(i);
                             last = temp.charAt(i);
                         }
                         if (Character.isWhitespace(value.charAt(value.length() - 1))) {
                             value = value.substring(0, value.length() - 1);
                         }
                         //System.out.println(node + finalNode + " = " + value);
                         if (value.equalsIgnoreCase("true")) {
                             yaml.put(node + finalNode, true);
                         } else if (value.equalsIgnoreCase("false")) {
                             yaml.put(node + finalNode, false);
                         } else if (CraftCommons.isInteger(value)) {
                             yaml.put(node + finalNode, Integer.parseInt(value));
                         } else if (CraftCommons.isLong(value)) {
                             yaml.put(node + finalNode, Long.parseLong(value));
                         } else {
                             yaml.put(node + finalNode, value);
                         }
                         isNode = false;
                     }
                 }
             } finally  {
                 yamlStream.close();
             }
         }
         return yaml;
     }
 }
