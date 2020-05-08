 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.mira.adastra3.server.network;
 
 import java.util.Date;
 
 /**
  *
  * @author tim
  */
 public abstract class NetworkEntity {
     //
     // Member data
     //
     
     private final String mVendor, mModel;
     private String mAddress;
    private Date mMark;
     
     
     //
     // Construction and destruction
     //
     
     public NetworkEntity(final String iVendor, final String iModel) {
         mVendor = iVendor;
         mModel = iModel;
         updateHeartbeat();
     }
     
     
     //
     // Getters and setters
     //
     
     public final String getVendor() {
         return mVendor;
     }
     
     public final String getModel() {
         return mModel;
     }
     
     public final String getAddress() {
         return mAddress;
     }
     
     public final void setAddress(final String iAddress) {
         mAddress = iAddress;
     }
     
     public final Date getHeartbeat() {
        return mMark;
     }
     
     public final void updateHeartbeat() {
        mMark = new Date();
     }
     
     public final long getHeartbeatDelta() {
        return (new Date().getTime()) - mMark.getTime();
     }
 }
