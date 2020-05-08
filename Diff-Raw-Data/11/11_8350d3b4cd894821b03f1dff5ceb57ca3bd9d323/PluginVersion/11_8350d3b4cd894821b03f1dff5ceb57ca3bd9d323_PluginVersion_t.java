 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2011 Pentaho Corporation.  All rights reserved.
  *
  * Created Jan 24th, 2012
  * @author pedrovale
  */
 package org.pentaho.marketplace;
 
 import java.io.Serializable;
 
 public class PluginVersion implements Serializable {
 
     private static final long serialVersionUID = 8252279235434152153L;
     private String branch;
     private String name;
     private String version;
     private String downloadUrl;
     private String samplesDownloadUrl;
     private String description;
     private String changelog;
     
     
     /** new props **/
     private String buildId;
     private String releaseDate;    
     private String minParentVersion;
     private String maxParentVersion;
 
    public PluginVersion(String branch, String name, String version, String downloadUrl, String samplesDownloadUrl, String description, String changelog, String buildId, String releaseDate, String minParentVersion, String maxParentVersion) {
 
         this.branch = branch;
         this.name = name;
         this.version = version;
         this.downloadUrl = downloadUrl;
         this.samplesDownloadUrl = samplesDownloadUrl;
         this.description = description;
         this.changelog = changelog;
         this.buildId = buildId;
         this.releaseDate = releaseDate;
 
         this.minParentVersion = minParentVersion;
         this.maxParentVersion = maxParentVersion;
 
     }
 
     public String getBranch() {
         return branch;
     }
 
     public void setBranch(String branch) {
         this.branch = branch;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     public String getVersion() {
         return version;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     /**
      * @return the downloadUrl
      */
     public String getDownloadUrl() {
         return downloadUrl;
     }
 
     /**
      * @param downloadUrl the downloadUrl to set
      */
     public void setDownloadUrl(String downloadUrl) {
         this.downloadUrl = downloadUrl;
     }
 
     /**
      * @return the samplesDownloadUrl
      */
     public String getSamplesDownloadUrl() {
         return samplesDownloadUrl;
     }
 
     /**
      * @param samplesDownloadUrl the samplesDownloadUrl to set
      */
     public void setSamplesDownloadUrl(String samplesDownloadUrl) {
         this.samplesDownloadUrl = samplesDownloadUrl;
     }
     
     /**
      * @param date the date to set
      */
     public void setReleaseDate(String releaseDate) {
         this.releaseDate = releaseDate;
     }
 
     /**
      * @return the description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @param description the description to set
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * @return the changelog
      */
     public String getChangelog() {
         return changelog;
     }
 
     /**
      * @return the buildId
      */
     public String getBuildId() {
         return buildId;
     }
 
 
     /**
      * @return the date
      */
     public String getReleaseDate() {
         return releaseDate;
     }
     
     
     /**
      * @return the minParentVersion
      */
     public String getMinParentVersion() {
       return minParentVersion;
     }
     
     /**
      * @return the maxParentVersion
      */
     public String getMaxParentVersion() {
       return maxParentVersion;
     }
 
 }
