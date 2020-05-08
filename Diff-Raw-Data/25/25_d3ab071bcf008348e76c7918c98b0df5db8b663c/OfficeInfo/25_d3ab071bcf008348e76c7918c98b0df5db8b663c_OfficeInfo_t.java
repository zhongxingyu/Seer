 package com.thoughtworks.viewmodels;
 
 import android.database.Cursor;
 import com.thoughtworks.models.Office;
 
 public class OfficeInfo {
 
     private int officeId;
     private String officeName;
     private String companyName;
 
     public OfficeInfo(Cursor cursor) {
         this.officeId = cursor.getInt(cursor.getColumnIndex(Office.ID));
        this.officeName = cursor.getString(cursor.getColumnIndex(Office.OFFICE_NAME_ALIAS));
        this.companyName = cursor.getString(cursor.getColumnIndex(Office.COMPANY_NAME_ALIAS));
     }
 
     public int getOfficeId() {
         return officeId;
     }
 
     public String getOfficeName() {
         return officeName;
     }
 
     public String getCompanyName() {
         return companyName;
     }
 }
