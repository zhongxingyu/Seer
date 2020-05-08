 package org.apache.hadoop.hive.metastore.model;
 
 public class MEquipRoom {
   private String eqRoomName;
   private int status;
   private String geoLocName;
   private String comment;
   private MGeoLocation geolocation;
 
   /**
    * @author cry
    */
   public MEquipRoom() {}
   /**
    * @author cry
    * @param eqRoomName
    * @param status
    * @param
    * @param comment
    * @param geolocation
    */
   public MEquipRoom(String eqRoomName, int status, String geoLocName, String comment, MGeoLocation geolocation) {
     super();
     this.eqRoomName = eqRoomName;
     this.status = MetaStoreConst.MEquipRoomStatus.SUSPECT;
     this.geoLocName = geoLocName;
     this.comment = comment;
     this.geolocation = geolocation;
   }
 
   public String getComment() {
     return comment;
   }
   public void setComment(String comment) {
     this.comment = comment;
   }
   public String getEqRoomName() {
     return eqRoomName;
   }
   public void setEqRoomName(String eqRoomName) {
     this.eqRoomName = eqRoomName;
   }
   public int getStatus() {
     return status;
   }
   public void setStatus(int status) {
     this.status = status;
   }
   public String getGeoLocName() {
     return geoLocName;
   }
   public void setGeoLocName(String geoLocName) {
     this.geoLocName = geoLocName;
   }
   public MGeoLocation getGeolocation() {
     return geolocation;
   }
   public void setGeolocation(MGeoLocation geolocation) {
     this.geolocation = geolocation;
   }
 
 }
