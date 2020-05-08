 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.bart4223.PlayerManagement;
 
 import com.mahn42.framework.DBRecord;
import java.sql.Timestamp;
import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Nils
  */
 public class SocialPointHistoryDBRecord extends DBRecord{
     
     public String PointType;
     public String Distributor;
     public String Player;
     public int Points;
     public String Reason;
     public long Timestamp;
 
     @Override
     protected void toCSVInternal(ArrayList aCols) {
         super.toCSVInternal(aCols);
         aCols.add(PointType);
         aCols.add(Distributor);
         aCols.add(Player);
         aCols.add(Points);
         aCols.add(Reason);
         SimpleDateFormat lSdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
         aCols.add(lSdf.format(new Date(Timestamp)));
     }
 
     @Override
     protected void fromCSVInternal(DBRecord.DBRecordCSVArray aCols) {
         super.fromCSVInternal(aCols);
         PointType = aCols.pop();
         Distributor = aCols.pop();
         Player = aCols.pop();
         Points = Integer.parseInt(aCols.pop());   
         Reason = aCols.pop();
         SimpleDateFormat lSdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
         Date lDate;
         try {
             lDate = lSdf.parse(aCols.pop());
             Timestamp = lDate.getTime();
         } catch (ParseException ex) {
             Logger.getLogger(SocialPointHistoryDBRecord.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
 }
