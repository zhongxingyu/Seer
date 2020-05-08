 /*
  * ###
  * Phresco Commons
  *
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  * 
  */
 
 package com.photon.phresco.commons.model;
 
 import java.io.Serializable;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
public class RepoInfo implements Serializable  {
     
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 5150170487041090747L;
 
 	private String customerId;
     private String repoName;
     private String baseRepoURL;
     private String releaseRepoURL;
     private String snapshotRepoURL;
     private String groupRepoURL;
     private String repoUserName;
     private String repoPassword;
     
     /**
      * 
      */
     public RepoInfo() {
         
     }
 
     /**
      * @return
      */
     public String getCustomerId() {
         return customerId;
     }
 
     /**
      * @param customerId
      */
     public void setCustomerId(String customerId) {
         this.customerId = customerId;
     }
 
     /**
      * @return
      */
     public String getReleaseRepoURL() {
         return releaseRepoURL;
     }
 
     /**
      * @param repoURL
      */
     public void setReleaseRepoURL(String repoURL) {
         this.releaseRepoURL = repoURL;
     }
 
     /**
      * @return
      */
     public String getRepoUserName() {
         return repoUserName;
     }
 
     /**
      * @param repoUserName
      */
     public void setRepoUserName(String repoUserName) {
         this.repoUserName = repoUserName;
     }
 
     /**
      * @return
      */
     public String getRepoPassword() {
         return repoPassword;
     }
 
     /**
      * @param repoPassword
      */
     public void setRepoPassword(String repoPassword) {
         this.repoPassword = repoPassword;
     }
     
     /**
      * @return
      */
     public String getSnapshotRepoURL() {
         return snapshotRepoURL;
     }
 
     /**
      * @param snapshotRepoURL
      */
     public void setSnapshotRepoURL(String snapshotRepoURL) {
         this.snapshotRepoURL = snapshotRepoURL;
     }
 
     /**
      * @return
      */
     public String getGroupRepoURL() {
         return groupRepoURL;
     }
 
     /**
      * @param groupRepoURL
      */
     public void setGroupRepoURL(String groupRepoURL) {
         this.groupRepoURL = groupRepoURL;
     }
 
     public String getBaseRepoURL() {
         return baseRepoURL;
     }
 
     public void setBaseRepoURL(String baseRepoURL) {
         this.baseRepoURL = baseRepoURL;
     }
     
     public void setRepoName(String repoName) {
 		this.repoName = repoName;
 	}
 
 	public String getRepoName() {
 		return repoName;
 	}
 	
     public String toString() {
         return new ToStringBuilder(this,
                 ToStringStyle.DEFAULT_STYLE)
                 .append("customerId", customerId)
                 .append("repoURL", releaseRepoURL)
                 .append("repoUserName", repoUserName)
                 .append("repoPassword", repoPassword)
                 .append("snapshotRepoURL", snapshotRepoURL)
                 .append("groupRepoURL", groupRepoURL)
                 .toString();
     }
 
 }
