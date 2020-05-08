 /*
  * Copyright 2011-2013 the original author or authors.
  *
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
  */
 
 package griffon.portal.values;
 
 import grails.util.GrailsNameUtils;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Andres Almiray
  */
 public enum ArtifactTab {
    INSTALLATION,
     DESCRIPTION,
     RELEASES,
     // FAQ,
     // SCREENSHOTS,
     COMMENTS,
     STATISTICS;
 
     public String getName() {
         return name().toLowerCase();
     }
 
     public String getCapitalizedName() {
         return GrailsNameUtils.getNaturalName(getName());
     }
 
     private static String[] NAMES;
     private static String[] CAPITALIZED_NAMES;
 
     static {
         ArtifactTab[] values = ArtifactTab.values();
         NAMES = new String[values.length];
         CAPITALIZED_NAMES = new String[values.length];
 
         int i = 0;
         for (ArtifactTab tab : ArtifactTab.values()) {
             NAMES[i] = tab.getName();
             CAPITALIZED_NAMES[i++] = tab.getCapitalizedName();
         }
     }
 
     public static List<String> getNamesAsList() {
         return Arrays.asList(NAMES);
     }
 
     public static List<String> getCapitalizedNamesAsList() {
         return Arrays.asList(CAPITALIZED_NAMES);
     }
 
     public static List<ArtifactTab> asList() {
         return Arrays.asList(values());
     }
 }
