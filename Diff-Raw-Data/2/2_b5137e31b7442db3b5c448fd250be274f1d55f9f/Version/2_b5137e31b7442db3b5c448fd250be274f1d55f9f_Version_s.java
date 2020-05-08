 package com.arcusys.liferay.vaadinplugin.util;
 
 /*
  * #%L
  * Liferay Vaadin Plugin
  * %%
  * Copyright (C) 2013 Arcusys Ltd.
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mminin
  * Date: 20/08/13
  * Time: 1:51 PM
 
  */
 public class Version {
     private final String rawVersion;
     private final Integer[] numericVersion;
 
     public Version(String version) {
         rawVersion = version;
         this.numericVersion = parseNumericPart(rawVersion);
     }
 
     public static Integer[] parseNumericPart(String version){
         String[] versionParts = version.split(" ")[0].split("\\.");
         List<Integer> numbersList = new ArrayList<Integer>();
         for (String versionPart : versionParts) {
             try {
                 numbersList.add(Integer.parseInt(versionPart));
             } catch (NumberFormatException exception) {
                 break;
             }
         }
         return numbersList.toArray(new Integer[numbersList.size()]);
     }
 
     public Integer[] getNumericVersion(){
         return numericVersion;
     }
 
     @Override
     public String toString(){
         return rawVersion;
     }
 
     public int compareTo(Version that) {
         return this.compareTo(that.numericVersion);
     }
 
     public int compareTo(Integer[] versionToCompare) {
         if (versionToCompare == null || versionToCompare.length == 0) return 1;
 
         int length = Math.min(numericVersion.length, versionToCompare.length);
         for (int i = 0; i < length; i++) {
             int compareResult = numericVersion[i].compareTo(versionToCompare[i]);
             if (compareResult != 0) return compareResult;
         }
        return Integer.compare(numericVersion.length, versionToCompare.length);
     }
 }
