 /**
  * Framework Web Archive
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.framework.actions.applications;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.service.client.api.ServiceManager;
 
 public class Download extends FrameworkBaseAction {
 
     private static final long serialVersionUID = -4735573440570585624L;
     
     private static final Logger S_LOGGER = Logger.getLogger(Applications.class);
     private static Boolean s_debugEnabled  = S_LOGGER.isDebugEnabled();
     
     public String list() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Download.list()");
     	}
     	
     	try {
     	    ServiceManager serviceManager = getServiceManager();
 			String platform = FrameworkUtil.findPlatform();
 			
			List<DownloadInfo> serverDownloadInfo = serviceManager.getDownloads(getCustomerId(), "", DownloadInfo.Category.SERVER.name(), platform);
 			if (CollectionUtils.isNotEmpty(serverDownloadInfo)) {
 				Collections.sort(serverDownloadInfo, sortDownloadsInAlphaOrder());
 			}
 			setReqAttribute(REQ_SERVER_DOWNLOAD_INFO, serverDownloadInfo);
 			
			List<DownloadInfo> dbDownloadInfo = serviceManager.getDownloads(getCustomerId(), "", DownloadInfo.Category.DATABASE.name(), platform);
 			if (CollectionUtils.isNotEmpty(dbDownloadInfo)) {
 				Collections.sort(dbDownloadInfo, sortDownloadsInAlphaOrder());
 			}
 			
 			setReqAttribute(REQ_DB_DOWNLOAD_INFO, dbDownloadInfo);
 			
			List<DownloadInfo> editorDownloadInfo = serviceManager.getDownloads(getCustomerId(), "", DownloadInfo.Category.EDITOR.name(), platform);
 			if (CollectionUtils.isNotEmpty(editorDownloadInfo)) {
 				Collections.sort(editorDownloadInfo, sortDownloadsInAlphaOrder());
 			}
 			setReqAttribute(REQ_EDITOR_DOWNLOAD_INFO, editorDownloadInfo);
 			
			List<DownloadInfo> toolsDownloadInfo = serviceManager.getDownloads(getCustomerId(), "", DownloadInfo.Category.TOOLS.name(), platform);
 			if (CollectionUtils.isNotEmpty(toolsDownloadInfo)) {
 				Collections.sort(toolsDownloadInfo, sortDownloadsInAlphaOrder());
 			}
 			setReqAttribute(REQ_TOOLS_DOWNLOAD_INFO, toolsDownloadInfo);
 			
			List<DownloadInfo> othersDownloadInfo = serviceManager.getDownloads(getCustomerId(), "", DownloadInfo.Category.OTHERS.name(), platform);
 			if (CollectionUtils.isNotEmpty(othersDownloadInfo)) {
 				Collections.sort(othersDownloadInfo, sortDownloadsInAlphaOrder());
 			}
 			setReqAttribute(REQ_OTHERS_DOWNLOAD_INFO, othersDownloadInfo);
 			
 		} catch (PhrescoException e) {
 			new LogErrorReport(e, "Listing downloads");
 		}
         return APP_DOWNLOAD;
     }
     
     private Comparator sortDownloadsInAlphaOrder() {
 		return new Comparator() {
 		    public int compare(Object firstObject, Object secondObject) {
 		    	DownloadInfo downloadInfo1 = (DownloadInfo) firstObject;
 		    	DownloadInfo downloadInfo2 = (DownloadInfo) secondObject;
 		       return downloadInfo1.getName().compareToIgnoreCase(downloadInfo2.getName());
 		    }
 		};
 	}
 }
