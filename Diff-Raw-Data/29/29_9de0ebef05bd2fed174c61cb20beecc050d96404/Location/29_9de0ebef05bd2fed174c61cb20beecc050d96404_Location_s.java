 package com.ksoft.android.model;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Catalin Samarghitan EMail : catalin.samarghitan@gmail.com
  * Date: 4/17/13
  * Time: 10:42 AM
  */
 public class Location {
 
     public static final String LOCATION_TABLE_NAME = "LOCATION";
    public static final String _ID = "ID";
     public static final String COLUMN_NAME = "NAME";
     public static final String COLUMN_WIFI_NAME = "WIFI_NAME";
 
     private Long id;
     private String name;
     private String wifiName;
     // GPS coordinates maybe
 
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getWifiName() {
         return wifiName;
     }
 
     public void setWifiName(String wifiName) {
         this.wifiName = wifiName;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Location location = (Location) o;
 
         if (!id.equals(location.id)) return false;
         if (!name.equals(location.name)) return false;
         if (wifiName != null ? !wifiName.equals(location.wifiName) : location.wifiName != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id.hashCode();
         result = 31 * result + name.hashCode();
         result = 31 * result + (wifiName != null ? wifiName.hashCode() : 0);
         return result;
     }
 }
