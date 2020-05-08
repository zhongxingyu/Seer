 package com.wsu_codemichigan.parcelables;
 
 /**
  * Created by Mrgadgetz
  * Date: 8/14/13
  * Time: 2:17 PM
  */
 public class CustomList {
 
    public String management_company, name,
            city, human_address, id, zipcode;
 
     public boolean needs_recording;
     public double longitude, latitude;
 
     @Override
     public String toString() {
         return "Company: " + management_company + "Address: " + human_address;
     }
 }
 
 ;
