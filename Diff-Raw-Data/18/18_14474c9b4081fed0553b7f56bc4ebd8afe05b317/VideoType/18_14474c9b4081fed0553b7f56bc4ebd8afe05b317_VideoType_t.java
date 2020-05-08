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
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 
 @XmlRootElement
 public class VideoType extends Element {
     
	private String videoInfoId;
 	private String url;
 	private ArtifactGroup artifactGroup;
 	
 	public VideoType() {
 	    super();
 	}
 
	public String getVideoInfoId() {
		return videoInfoId;
	}

	public void setVideoInfoId(String videoInfoId) {
		this.videoInfoId = videoInfoId;
	}

 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
     public ArtifactGroup getArtifactGroup() {
 		return artifactGroup;
 	}
 
 	public void setArtifactGroup(ArtifactGroup artifactGroup) {
 		this.artifactGroup = artifactGroup;
 	}
 
 	@Override
     public String toString() {
         return new ToStringBuilder(this,
                 ToStringStyle.DEFAULT_STYLE)
                 .append(super.toString())
                 .append("url", getUrl())
                 .append("artifactGroup", getArtifactGroup())
                 .toString();
     }
 }
