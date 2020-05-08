 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.bart4223.PlayerManagement;
 
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.PlayerManager;
 import com.mahn42.framework.SocialPointEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Nils
  */
 public class SocialPointManager implements PlayerManager {
       
     public SocialPointManager(PlayerManagement aPlugin){
         Plugin = aPlugin;
         fLog = Plugin.getLogger();
         fSocialPointTypes = new ArrayList<SocialPointType>();
         InitSPDB();
         InitSPHDB();
         Framework.plugin.registerSaver(fSPDB);
         Framework.plugin.registerSaver(fSPHDB);
     } 
         
     protected PlayerManagement Plugin; 
     public int MaxSPHistoryItems;
     public int MaxShownSPListItems;
     
     @Override
     public void increaseSocialPoint(String aPlayerName, String aName, int aAmount, String aReason, String aChargePlayerName) {
         AccordSocialPoint(aName, aPlayerName, aChargePlayerName, aAmount, aReason);
     }
 
     @Override
     public List<SocialPoint> getSocialPoints(String aPlayerName) {
         return getSocialPointsBy(aPlayerName);
     }
 
     @Override
     public List<SocialPointHistory> getSocialPointHistory(String aPlayerName, String aName) {
         return getSocialPointHistoryByType(aPlayerName, aName);
     }
 
     @Override
     public SocialPoint getSocialPoint(String aPlayerName, String aName) {
         return getSocialPointByType(aPlayerName,aName);
     }
      
     public List<SocialPoint> getSocialPointsBy(String aPlayer) {
         ArrayList<SocialPoint> lResult;
         lResult = new ArrayList<SocialPoint>();
         List<SocialPointDBRecord> lSPDBRecords = fSPDB.getSocialPoints(aPlayer);
         for (Iterator<SocialPointDBRecord> it = lSPDBRecords.iterator(); it.hasNext();) {
             SocialPointDBRecord lSPDBRecord = it.next();
             lResult.add(new SocialPointRecord(lSPDBRecord.SocialPointType, lSPDBRecord.TotalPoints));
         }
         return lResult;
     }       
 
     public List<SocialPointHistory> getSocialPointHistoryByType(String aPlayer, String aPointType) {
         ArrayList<SocialPointHistory> lResult;
         lResult = new ArrayList<SocialPointHistory>();
         List<SocialPointHistoryDBRecord> lSPHDBRecords = fSPHDB.getSocialPointsByType(aPlayer, aPointType);
         for (Iterator<SocialPointHistoryDBRecord> it = lSPHDBRecords.iterator(); it.hasNext();) {
             SocialPointHistoryDBRecord lSPHDBRecord = it.next();
            lResult.add(new SocialPointHistoryRecord(lSPHDBRecord.PointType, lSPHDBRecord.Points, lSPHDBRecord.Reason, lSPHDBRecord.Distributor, lSPHDBRecord.Timestamp));
         }
         return lResult;
     }       
   
     public SocialPoint getSocialPointByType(String aPlayer, String aPointType) {
         SocialPointDBRecord lSPDBRecord = fSPDB.getSocialPointByType(aPlayer, aPointType, false);
         return new SocialPointRecord(lSPDBRecord.SocialPointType, lSPDBRecord.TotalPoints);
     }    
 
     public SocialPointType getSocialPointType(String aName) {
         Iterator<SocialPointType> lItr = fSocialPointTypes.iterator();
         while (lItr.hasNext()) {
             SocialPointType lSPT = lItr.next();
             if (lSPT.Name.equals(aName)) {
                 return lSPT;
             }
         }
         return null;
     }
 
     public SocialPointProperty getSocialPointTypeProp(String aType, String aName) {
         SocialPointType lSPT = getSocialPointType(aType);
         if (lSPT != null ) {
             return lSPT.GetProp(aName);
         }
         else {
             return null;
         }
     }
 
     public Object getSocialPointTypePropValue(String aType, String aName) {
         SocialPointType lSPT = getSocialPointType(aType);
         if (lSPT != null ) {
             return lSPT.GetPropValue(aName);
         }
         else {
             return null;
         }
     }
     
     public void AddSocialPoinType(SocialPointType aType) {
         fSocialPointTypes.add(aType);
     }
     
     protected Logger fLog; 
     protected SocialPointDBSet fSPDB;
     protected SocialPointHistoryDBSet fSPHDB;
     protected ArrayList<SocialPointType> fSocialPointTypes;
 
     protected boolean AccordSocialPoint(String aPointType, String aDistributor, String aPlayer, int aPoints, String aReason) {
         boolean lOK = true;
         SocialPointHistoryDBRecord lHRec = new SocialPointHistoryDBRecord();
         lHRec.Distributor = aDistributor;
         lHRec.Player = aPlayer;
         lHRec.PointType = aPointType;
         lHRec.Points = aPoints;
         lHRec.Reason = aReason;
         lHRec.Timestamp = new Date().getTime(); 
         while (fSPHDB.size() >= MaxSPHistoryItems) {
             fSPHDB.remove(0);
         }
         fSPHDB.addRecord(lHRec);
         SocialPointDBRecord lSPDBRecord = fSPDB.getSocialPointByType(aPlayer, aPointType, true);
         lSPDBRecord.TotalPoints = lSPDBRecord.TotalPoints + lHRec.Points;
         SocialPoint lSP = getSocialPoint(aPlayer, aPointType);
        SocialPointHistory lSPH = new SocialPointHistoryRecord(lHRec.PointType, lHRec.Points, lHRec.Reason, lHRec.Distributor, lHRec.Timestamp);
         SocialPointEvent lEvent = new SocialPointEvent(this, lSP, lSPH);    
         lEvent.raise();       
         return lOK;
     }   
     
     protected void InitSPDB() {
         if (fSPDB == null) {
             File lFolder = Plugin.getDataFolder();
             if (!lFolder.exists()) {
                 lFolder.mkdirs();
             }
             String lPath = lFolder.getPath();
             lPath = lPath + File.separatorChar + "socialpoint.csv";
             File lFile = new File(lPath);
             fSPDB = new SocialPointDBSet(lFile);
             fSPDB.load();
             fLog.info("Datafile " + lFile.toString() + " loaded. (Records:" + new Integer(fSPDB.size()).toString() + ")");
         }
     }   
 
     protected void InitSPHDB() {
         if (fSPHDB == null) {
             File lFolder = Plugin.getDataFolder();
             if (!lFolder.exists()) {
                 lFolder.mkdirs();
             }
             String lPath = lFolder.getPath();
             lPath = lPath + File.separatorChar + "socialpointhistory.csv";
             File lFile = new File(lPath);
             fSPHDB = new SocialPointHistoryDBSet(lFile);
             fSPHDB.load();
             fLog.info("Datafile " + lFile.toString() + " loaded. (Records:" + new Integer(fSPHDB.size()).toString() + ")");
         }
     }   
     
 }
