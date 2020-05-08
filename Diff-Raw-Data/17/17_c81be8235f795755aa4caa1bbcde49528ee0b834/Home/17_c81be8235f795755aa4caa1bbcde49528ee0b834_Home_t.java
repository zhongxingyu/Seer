 /*
  * ###
  * Framework Web Archive
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
 package com.photon.phresco.framework.actions.home;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.VideoInfo;
 import com.photon.phresco.commons.model.VideoType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.commons.FrameworkActions;
 import com.sun.jersey.api.client.ClientHandlerException;
 
 public class Home extends FrameworkBaseAction implements FrameworkActions {
 	private static final long serialVersionUID = -9002492813622189809L;
 	
 	private static final Logger S_LOGGER   = Logger.getLogger(Home.class);
 	private static Boolean DebugEnabled = S_LOGGER.isDebugEnabled();
	
 	public String welcome() {
         return HOME_WELCOME;
     }
 	
 	public String view() {
 		if (DebugEnabled) {
 			S_LOGGER.debug("Entering Method Home.view()");
 		}
 
 	    try {
 			List<VideoInfo> videoInfos = getServiceManager().getVideoInfos();
 			FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
 			setReqAttribute(REQ_SERVER_URL, configuration.getVideoServicePath());
 			setReqAttribute(REQ_VIDEO_INFOS, videoInfos);
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 		}
 	    
 		return HOME_VIEW;
 	}
 
 	public String video() {
 		if (DebugEnabled) {
 			S_LOGGER.debug("Entering Method Home.video()");
 		}
 
 
 		try {
 			String videoName = getReqParameter(REQ_VIDEO);
 			List<VideoType> videoTypes = getVideoTypes(videoName);
 			FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
 			setReqAttribute(REQ_SERVER_URL, configuration.getVideoServicePath());
 			setReqAttribute(REQ_VIDEO_TYPES, videoTypes);
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 		}
 
 		return HOME_VIDEO;
 	}
 	
 	 public List<VideoInfo> getVideoInfos() throws PhrescoException {
 		if (DebugEnabled) {
 			S_LOGGER.debug("Entering Method Home.getVideoTypeInfos()");
 		}
 
 		try {
 			return getServiceManager().getVideoInfos();
 		} catch (ClientHandlerException ex) {
 			if (DebugEnabled) {
 				S_LOGGER.error(ex.getLocalizedMessage());
 			}
 			throw new PhrescoException(ex);
 		}
 	 }
 	 
 	 public List<VideoType> getVideoTypes(String name) throws PhrescoException {
 		if (DebugEnabled) {
 			S_LOGGER.debug("Entering Method Home.getVideoTypes(String name)");
 		}
 		try {
 			List<VideoInfo> videoInfos = getVideoInfos();
 			for (VideoInfo videoInfo : videoInfos) {
 				if(videoInfo.getName().equals(name)) {
 					return videoInfo.getVideoList();
 				}	
 			}
 		} catch (Exception e) {
 			if (DebugEnabled) {
 				S_LOGGER.debug("Video information is not available");
 			}
 		}
 		
 		return null;
 	 }
 }
