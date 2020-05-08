 package com.wsu_codemichigan.parcelables;
 
 /**
  * Created by Mrgadgetz
  * Date: 8/14/13
  * Time: 2:17 PM
  */
 public class CustomList {
    public final String TAG_ID = "id";
    public final String TAG_Company = "management_company";
    public final String TAG_ZIPCODE = "zipcode";
    public final String TAG_NAME = "name";
    public final String TAG_CITY = "Harrisville";
    public final String TAG_LOCATION1 = "location_1";
    public final String TAG_LOC_NEEDSREC = "needs_recoding";
    public final String TAG_LOC_LONGITUDE = "longitude";
    public final String TAG_LOC_LATITUDE = "latitude";
    public final String TAG_LOC_ADDRESS = "human_address";
 
 
    public String management_company, name, city, human_address, id, zipcode;
     public boolean needs_recording;
     public double longitude, latitude;
 
     @Override
     public String toString() {
         return "Company: " + management_company + "Address: " + human_address;
     }
 }
 
 ;
