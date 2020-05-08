 package org.automation.dojo.web.controllers;
 
 import com.google.inject.internal.Errors;
 import org.automation.dojo.PlayerRecord;
 
 import java.util.List;
 
 /**
  * User: serhiy.zelenin
  * Date: 5/22/12
  * Time: 6:36 PM
  */
 public class ReleaseLogView {
     private List<PlayerRecord> records;
 
    public ReleaseLogView(List<PlayerRecord> records) {
         this.records = records;
     }
 
     public List<PlayerRecord> getRecords() {
         return records;
     }
 }
