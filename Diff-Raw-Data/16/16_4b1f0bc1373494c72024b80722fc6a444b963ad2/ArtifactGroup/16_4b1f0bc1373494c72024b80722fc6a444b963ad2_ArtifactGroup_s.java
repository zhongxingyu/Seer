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
  */
 package com.photon.phresco.commons.model;
 
 import java.util.List;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 public class ArtifactGroup extends CustomerBasedElement {
 	
 	public enum Type {
 		ARCHETYPE, FEATURE, JAVASCRIPT, COMPONENT
 	}
     
 	private String groupId;
     private String artifactId;
     //jar, zip, js
     private String packaging;
     private String classifier;
     private List<ArtifactInfo> versions;
     //module, component, js, archetype
     private Type type;
     private String imageURL;
     private List<CoreOption> appliesTo;
 
     public ArtifactGroup() {
         super();
     }
 
     public ArtifactGroup(String id) {
         super(id);
     }

     public String getGroupId() {
         return groupId;
     }
 
     public void setGroupId(String groupId) {
         this.groupId = groupId;
     }
 
     public String getArtifactId() {
         return artifactId;
     }
 
     public void setArtifactId(String artifactId) {
         this.artifactId = artifactId;
     }
 
 	public String getPackaging() {
         return packaging;
     }
 
     public void setPackaging(String packaging) {
         this.packaging = packaging;
     }
 
     public List<ArtifactInfo> getVersions() {
         return versions;
     }
 
     public void setVersions(List<ArtifactInfo> artifactInfos) {
         this.versions = artifactInfos;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public String getImageURL() {
         return imageURL;
     }
 
     public void setImageURL(String imageURL) {
         this.imageURL = imageURL;
     }
 
     public String getClassifier() {
         return classifier;
     }
 
     public void setClassifier(String classifier) {
         this.classifier = classifier;
     }
     
     public List<CoreOption> getAppliesTo() {
         return appliesTo;
     }
 
     public void setAppliesTo(List<CoreOption> appliesTo) {
         this.appliesTo = appliesTo;
     }
     
     @Override
     public String toString() {
         return new ToStringBuilder(this,
                 ToStringStyle.DEFAULT_STYLE)
                 .append(super.toString())
                 .append("groupId", getGroupId())
                 .append("artifactId", getArtifactId())
                 .append("packaging", getPackaging())
                 .append("artifactInfos", getVersions())
                 .append("type", getType())
                 .append("imageURL", getImageURL())
                 .append("appliesTo", getAppliesTo())
                 .toString();
     }
     
 }
