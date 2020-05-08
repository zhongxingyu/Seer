 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2010, Centre Nationale de la Recherche Scientifique
 
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
 
 package eu.stratuslab.authn;
 
 import java.io.BufferedReader;
import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class AuthnData {
 
     private static final Pattern COMMENT_LINE = Pattern.compile("^\\s*#.*");
     private static final Pattern DATA_LINE = Pattern
             .compile("^\\s*\"(.*)\"[\\s,]*(.*)");
 
     // Authorized users with associated groups.
     private Map<String, List<String>> data = new HashMap<String, List<String>>();
 
     // Empty list for nonexistent users.
     private static List<String> noGroups = createGroupList(null);
 
     public AuthnData(Object filename) {
         readAuthnFile(filename);
     }
 
     public boolean isValidUser(String username) {
         return data.containsKey(username);
     }
 
     public List<String> groups(String username) {
         return data.containsKey(username) ? data.get(username) : noGroups;
     }
 
     private void readAuthnFile(Object filename) {
 
         if (filename != null) {
 
            Reader reader = null;
             try {
                reader = new InputStreamReader(new FileInputStream(
                        filename.toString()), Charset.defaultCharset());
                 BufferedReader br = new BufferedReader(reader);
 
                 processByLines(br);
             } catch (IOException consumed) {
 
             } finally {
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (IOException consumed) {
                     }
                 }
             }
         }
     }
 
     private void processByLines(BufferedReader br) throws IOException {
 
         String line = br.readLine();
         while (line != null) {
             if (!COMMENT_LINE.matcher(line).matches()) {
                 Matcher m = DATA_LINE.matcher(line);
                 if (m.matches()) {
                     processLine(m.group(1), m.group(2));
                 }
             }
             line = br.readLine();
         }
 
     }
 
     private void processLine(String username, String groupInfo) {
 
         List<String> groupList = createGroupList(groupInfo);
         data.put(username, groupList);
 
     }
 
     private static List<String> createGroupList(String groupInfo) {
 
         List<String> groupList = new ArrayList<String>();
 
         if (groupInfo != null) {
             String[] groups = groupInfo.split("[\\s,]+");
             for (String group : groups) {
                 groupList.add(group);
             }
         }
 
         return Collections.unmodifiableList(groupList);
     }
 
 }
