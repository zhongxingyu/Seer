 /*
  * Ninja Trials is an old school style Android Game developed for OUYA & using
  * AndEngine. It features several minigames with simple gameplay.
  * Copyright 2013 Mad Gear Games <madgeargames@gmail.com>
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
 
 package com.madgear.ninjatrials.records;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import com.madgear.ninjatrials.R;
 import com.madgear.ninjatrials.managers.ResourceManager;
 
 public class RecordsTable implements Serializable {
 
     //  Serial version:
     private static final long serialVersionUID = 1L;
 
     public Record[] recordsTable;
     public Date updateDate;
     
     // Const:
     public RecordsTable(int size) {
         recordsTable = new Record[size];
         for(int i = 0; i < recordsTable.length; i++)
             recordsTable[i] = new Record();
         updateDate = new Date();
     }
     
     public int getMin() {
         int min = recordsTable[0].score;
         for (int i = 0; i < recordsTable.length; i++) {
             if (recordsTable[i].score < min) {
                 min = recordsTable[i].score;
             }
         }
         return min;
     }
     
     /**
      * Bubble insert method.
      * @param record
      */
     public void insert(Record record) {
         if(record.score > getMin()) {
             updateDate = new Date();  // updateDate = now
             recordsTable[0] = record;
             for(int i = 1; i < recordsTable.length; i++) {
                 if (record.score > recordsTable[i].score) {
                     recordsTable[i-1] = recordsTable[i];
                     recordsTable[i] = record;
                 }
             }
         }
     }
     
     /**
      * 
      * @return False if any record name equals MADGEAR. Else true.
      */
     public boolean allDefaultRecordsReplaced() {
         boolean allReplaced = true;
         for (int i = 0; i < recordsTable.length; i++) {
            if (recordsTable[i].profileName == ResourceManager.getInstance().loadAndroidRes().
                    getString(R.string.record_default_name)) {
                 allReplaced = false;
             }
         }
         
         return allReplaced;
     }
 }
