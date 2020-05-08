 /*
  * Copyright 2011 Zouhin Ro
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
 package magicware.scm.redmine.tools.config;
 
 public class SyncItem {
 
     private String filePath;
 
     private String sheetName;
 
     private String jsonTemplate;
 
     private int keyColumnIdx;
 
     private int keyRowBeginIdx;
 
     private String keyFiledId;
 
     private String projectId;
 
     public String getFilePath() {
         return filePath;
     }
 
     public void setFilePath(String filePath) {
         this.filePath = filePath;
     }
 
     public String getSheetName() {
         return sheetName;
     }
 
     public void setSheetName(String sheetName) {
         this.sheetName = sheetName;
     }
 
     public String getJsonTemplate() {
         return jsonTemplate;
     }
 
     public void setJsonTemplate(String jsonTemplate) {
         this.jsonTemplate = jsonTemplate;
     }
 
     public int getKeyColumnIdx() {
         return keyColumnIdx;
     }
 
     public void setKeyColumnIdx(int keyColumnIdx) {
         this.keyColumnIdx = keyColumnIdx;
     }
 
     public int getKeyRowBeginIdx() {
         return keyRowBeginIdx;
     }
 
     public void setKeyRowBeginIdx(int keyRowBeginIdx) {
         this.keyRowBeginIdx = keyRowBeginIdx;
     }
 
     public String getKeyFiledId() {
         return keyFiledId;
     }
 
     public void setKeyFiledId(String keyFiledId) {
         this.keyFiledId = keyFiledId;
     }
 
     public String getProjectId() {
         return projectId;
     }
 
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
 
 }
