 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.bart4223.PlayerManagement;
 
 import com.mahn42.framework.PlayerManager;
 
 /**
  *
  * @author Nils
  */
 public class SocialPointHistoryRecord implements PlayerManager.SocialPointHistory{
 
    public SocialPointHistoryRecord(String aPointType, int aPoints, String aReason, String aDistributor, long aTimestamp){
         fPointType = aPointType;
         fPoints = aPoints;
         fReason = aReason;
        fDistributor = aDistributor;
         fTimestamp = aTimestamp;
     }
 
     @Override
     public String getName() {
         return fPointType;
     }
 
     @Override
     public int getAmount() {
         return fPoints;
     }
 
     @Override
     public String getReason() {
         return fReason;
     }
 
     @Override
     public String getChargePlayerName() {
        return fDistributor;
     }
     
     @Override
     public long getTimestamp() {
         return fTimestamp;
     }
 
     protected String fPointType;
     protected int fPoints;
     protected String fReason;
    protected String fDistributor;
     protected long fTimestamp;
 
 }
