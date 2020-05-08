 package com.mulesoft.module.dropbox.model;
 
 import java.io.Serializable;
 
 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 public class AccountInformation implements Serializable {
    private static final long serialVersionUID = -1L;
 
     /*  This is the response to /account/info for V1 of Dropbox API
         See dropbox docs at https://www.dropbox.com/developers/core/docs#account-info
     {
         "referral_link": "https://www.dropbox.com/referrals/r1a2n3d4m5s6t7",
         "display_name": "John P. User",
         "uid": 12345678,
         "country": "US",
         "quota_info": {
             "shared": 253738410565,
             "quota": 107374182400000,
             "normal": 680031877871
         },
         "email": "foo@bar.not"
     }
     */
 
     /**
      * The user's referral link.
      */
     private String referalLink;
 
     /**
      * The user's display name.
      */
     private String displayName;
 
     /**
      * The user's unique Dropbox ID.
      */
     private String uid;
 
     /**
      * The user's two-letter country code, if available.
      */
     private String country;
 
     /**
      * The user's quota information
      */
     private QuotaInformation quotaInfo;
 
     /**
      * The user's email
      */
     private String email;
 
     public String getReferalLink() {
         return referalLink;
     }
 
     public void setReferalLink(String referalLink) {
         this.referalLink = referalLink;
     }
 
     public String getUid() {
         return uid;
     }
 
     public void setUid(String uid) {
         this.uid = uid;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public QuotaInformation getQuotaInfo() {
         return quotaInfo;
     }
 
     public void setQuotaInfo(QuotaInformation quotaInfo) {
         this.quotaInfo = quotaInfo;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 }
