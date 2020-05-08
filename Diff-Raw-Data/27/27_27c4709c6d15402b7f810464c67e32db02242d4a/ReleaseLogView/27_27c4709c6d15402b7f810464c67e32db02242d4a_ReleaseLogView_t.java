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
    private int releaseNumber;
 
    public ReleaseLogView(List<PlayerRecord> records, int releaseNumber) {
         this.records = records;
        this.releaseNumber = releaseNumber;
     }
 
     public List<PlayerRecord> getRecords() {
         return records;
     }

    public int getReleaseNumber() {
        return releaseNumber;
    }
 }
