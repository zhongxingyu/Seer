 /**
  * Mule Yammer Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.yammer;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class User {
 
     @JsonProperty("email")
     private String email;
 
     @JsonProperty("full_name")
     private String fullName;
 
     @JsonProperty("job_title")
     private String jobTitle;
 
     @JsonProperty("network_id")
     private long networkId;
 
     @JsonProperty("mugshot_url")
    private String mugshotUrl;
 
     @JsonProperty("web_url")
     private String webUrl;
 
     @JsonProperty("type")
     private String type;
 
     private long id;
 
     private String url;
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getFullName() {
         return fullName;
     }
 
     public void setFullName(String fullName) {
         this.fullName = fullName;
     }
 
     public String getJobTitle() {
         return jobTitle;
     }
 
     public void setJobTitle(String jobTitle) {
         this.jobTitle = jobTitle;
     }
 
     public long getNetworkId() {
         return networkId;
     }
 
     public void setNetworkId(long networkId) {
         this.networkId = networkId;
     }
 
    public String getMugshotUrl() {
         return mugshotUrl;
     }
 
    public void setMugshotUrl(String mugshotUrl) {
         this.mugshotUrl = mugshotUrl;
     }
 
     public String getWebUrl() {
         return webUrl;
     }
 
     public void setWebUrl(String webUrl) {
         this.webUrl = webUrl;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     @Override
     public String toString() {
         return "User{" +
                 "email='" + email + '\'' +
                 ", fullName='" + fullName + '\'' +
                 ", jobTitle='" + jobTitle + '\'' +
                 ", networkId=" + networkId +
                 ", mugshotUrl=" + mugshotUrl +
                 ", webUrl='" + webUrl + '\'' +
                 ", id=" + id +
                 ", url='" + url + '\'' +
                 ", type='" + type + '\'' +
                 '}';
     }
 }
