 /*
  * ###
  * Service Web Archive
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
 package com.photon.phresco.service.admin.actions.components;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.DownloadInfo.Category;
 import com.photon.phresco.commons.model.PlatformType;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.client.api.ServiceManager;
 
 public class Downloads extends ServiceBaseAction { 
 
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Downloads.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 
 	private String downloadId = "";
 	private String name = "";
 	private String version = "";
 	private List<String> platform = null;
	private String category = null;
 	private String description = "";
 	private List<String> technology = null;
 	private String license = "";
 	
 	private String nameError = "";
 	private String verError = "";
 	private String platformTypeError = "";
 	private String groupError = "";
 	private String techError = "";
 	private String fileError = "";
 	private String licenseError = "";
 	private boolean errorFound = false;
 	
 	private String fromPage = "";
 	private String customerId = "";
 	private String oldVersion = "";
 	private String type = ""; // type of the file uploaded (file or image) 
 	private String versioning = "";
    
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 	private static byte[] downloadByteArray = null;
 	private static byte[] imgByteArray = null;
 
 	public String list() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Downloads.list()");
 		}
 		
 		try {
 			List<DownloadInfo> downloadInfo = getServiceManager().getDownloads(getCustomerId());
 			setReqAttribute(REQ_DOWNLOAD_INFO, downloadInfo);
 			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_LIST));
 		}
 
 		//to clear file inpustreams
 		inputStreamMap.clear();
 		downloadByteArray = null;
 		imgByteArray = null;
 		
 		return COMP_DOWNLOAD_LIST;	
 	}
 	
 	public String add() throws PhrescoException {
 		if (isDebugEnabled) {	
 			S_LOGGER.debug("Entering Method Downloads.add()");
 		}
 		
 		try {
 		    ServiceManager serviceManager = getServiceManager();
 		    List<PlatformType> platforms = serviceManager.getPlatforms();
 		    setReqAttribute(REQ_DOWNLOAD_PLATFORMS, platforms);
 			List<Technology> technologies = serviceManager.getArcheTypes(getCustomerId());
 			setReqAttribute(REQ_ARCHE_TYPES, technologies);
 			setReqAttribute(REQ_FROM_PAGE, ADD);
 			setReqAttribute(REQ_FEATURES_LICENSE, getServiceManager().getLicenses());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_ADD));
 		}
 		
 		return COMP_DOWNLOAD_ADD;
 	}
 	
 	public String edit() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Downloads.edit()");
 		}
 		
 		try {
 			ServiceManager serviceManager = getServiceManager();
             DownloadInfo downloadInfo = serviceManager.getDownload(getDownloadId(), getCustomerId());
             setReqAttribute(REQ_DOWNLOAD_INFO, downloadInfo);
             List<Technology> technologies = serviceManager.getArcheTypes(getCustomerId());
             setReqAttribute(REQ_ARCHE_TYPES, technologies);
             List<PlatformType> platforms = serviceManager.getPlatforms();
             setReqAttribute(REQ_DOWNLOAD_PLATFORMS, platforms);
 			setReqAttribute(REQ_FROM_PAGE, EDIT);
 			setReqAttribute(REQ_FEATURES_LICENSE, getServiceManager().getLicenses());
 			setReqAttribute(REQ_VERSIONING, getVersioning());	
 	} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_EDIT));
 		}
 
 		return COMP_DOWNLOAD_ADD;
 	}
 	
 	public String save() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Downloads.save()");
 		}
 		
 		try {
 			DownloadInfo downloadInfo = getDownloadInfo();
 			if(downloadByteArray != null){
 				inputStreamMap.put(downloadInfo.getName(),  new ByteArrayInputStream(downloadByteArray));
 			} 
 			if(imgByteArray != null){
 				inputStreamMap.put(downloadInfo.getName(),  new ByteArrayInputStream(imgByteArray));
 			} 
 		    
 		    
 			getServiceManager().createDownloads(getDownloadInfo(), inputStreamMap, getCustomerId());
 			addActionMessage(getText(DOWNLOAD_ADDED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_SAVE));
 		}
 		
 		return list();
 	}
 
 	public String update() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Downloads.update()");
 		}
 
 		try {
 			DownloadInfo downloadInfo = getDownloadInfo();
 			if(downloadByteArray != null){
 				inputStreamMap.put(downloadInfo.getName(),  new ByteArrayInputStream(downloadByteArray));
 			} 
 			if(imgByteArray != null){
 				inputStreamMap.put(downloadInfo.getName(),  new ByteArrayInputStream(imgByteArray));
 			} 
 			getServiceManager().updateDownload(getDownloadInfo(), inputStreamMap, getCustomerId());
 			addActionMessage(getText(DOWNLOAD_ADDED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_UPDATE));
 		}
 
 		return list();
 	}
 	
 	 private DownloadInfo getDownloadInfo() throws PhrescoException {
         DownloadInfo downloadInfo = new DownloadInfo();
         //To set the id for update
         if (StringUtils.isNotEmpty(getDownloadId())) {
             downloadInfo.setId(getDownloadId());
         }
         downloadInfo.setName(getName());
         downloadInfo.setDescription(getDescription());
         downloadInfo.setCustomerIds(Arrays.asList(getCustomerId()));
         downloadInfo.setAppliesToTechIds(getTechnology()); //To set applies to technology
         
         //To set applies to platform types
         List<String> platformTypeIds = new ArrayList<String>(getPlatform().size());
         if (CollectionUtils.isNotEmpty(getPlatform())) {
             for (String platformType : getPlatform()) {
                 platformTypeIds.add(platformType);
             }
         }
         downloadInfo.setPlatformTypeIds(platformTypeIds);
        Category category = Category.valueOf(getCategory());
        downloadInfo.setCategory(category); //To set category
 
         //To set the versions of the download items
         List<ArtifactInfo> downloadVersions = new ArrayList<ArtifactInfo>();
         ArtifactInfo downloadVersion = new ArtifactInfo();
         downloadVersion.setVersion(getVersion());
         downloadVersions.add(downloadVersion);
         ArtifactGroup artifactGroup = new ArtifactGroup();
         List<String> customerIds = new ArrayList<String>();
         customerIds.add(getCustomerId());
         artifactGroup.setCustomerIds(customerIds);
         artifactGroup.setVersions(downloadVersions);
         artifactGroup.setLicenseId(getLicense());
         downloadInfo.setArtifactGroup(artifactGroup);
         
         return downloadInfo;
     }
 
 	public String delete() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Downloads.delete()");
 		}
 
 		try {
 			String[] downloadIds = getHttpRequest().getParameterValues(REQ_DOWNLOAD_ID);
 			if (ArrayUtils.isNotEmpty(downloadIds)) {
 				for (String downloadid : downloadIds) {
 					getServiceManager().deleteDownloadInfo(downloadid, getCustomerId());
 				}
 				addActionMessage(getText(DOWNLOAD_DELETED));
 			}
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_DELETE));
 		}
 
 		return list();
 	}
 
 	public String uploadFile() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Downloads.uploadFile()");
 		}
 		
 		PrintWriter writer = null;
 		try {
             writer = getHttpResponse().getWriter();
 	        InputStream is = getHttpRequest().getInputStream();
 	        downloadByteArray = IOUtils.toByteArray(is);
         	writer.print(MAVEN_JAR_FALSE);
         	getHttpResponse().setStatus(getHttpResponse().SC_OK);
 	        writer.flush();
 	        writer.close();
 		} catch (Exception e) {
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 		}
 		
 		return SUCCESS;
 	}
 	
 	public String uploadImage() throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Downloads.uploadImage()");
         }
 	    
 		PrintWriter writer = null;
 		try {
 			writer = getHttpResponse().getWriter();
         	InputStream is = getHttpRequest().getInputStream();
         	imgByteArray = IOUtils.toByteArray(is);
         	writer.print(MAVEN_JAR_FALSE);
             getHttpResponse().setStatus(getHttpResponse().SC_OK);
 	        writer.flush();
 	        writer.close();
 		} catch (Exception e) {
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 			throw new PhrescoException(e);
 		}
 		
 		return SUCCESS;
 	}
 	
 	public void removeUploadedFile() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Downloads.removeUploadedFile()");
 		}
 		
 		if (REQ_DOWNLOAD_UPLOAD_FILE.equals(getType())) {
 			downloadByteArray = null;
 		} else {
 			imgByteArray = null;
 		}
 	}
 
 	public String validateForm() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Downloads.validateForm()");
 		}
 		
 		boolean isError = false;
 		//Empty validation for name
 		if (StringUtils.isEmpty(getName())) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
 			isError = true;
 		}
 
 		if (StringUtils.isEmpty(getVersion())) {//Empty validation for version
 			setVerError(getText(KEY_I18N_ERR_VER_EMPTY));
 			isError = true;
 		}  /*else if (StringUtils.isEmpty(getFromPage()) || (!getVersion().equals(getOldVersion()))) {
 			//To check whether the version already exist
 			List<DownloadInfo> downloads = getServiceManager().getDownloads(getCustomerId());
 			if (CollectionUtils.isNotEmpty(downloads)) {
 				for (DownloadInfo download : downloads) {
 					if (download.getName().equalsIgnoreCase(getName()) && download.getVersions().equals(getVersion())) {
 						setVerError(getText(KEY_I18N_ERR_VER_ALREADY_EXISTS));
 						isError = true;
 						break;
 					}
 				}
 			}
 		}*/
 		
 		if(StringUtils.isEmpty(getLicense())) {
         	setLicenseError(getText(KEY_I18N_ERR_LICEN_EMPTY));
             isError = true;
         }
 		
 		//Empty validation for platform 
 		if (CollectionUtils.isEmpty(getPlatform())) {
 			setPlatformTypeError(getText(KEY_I18N_ERR_APPLNPLTF_EMPTY));
 			isError = true;
 		} 
 
 		//Empty validation for group
 		if (getCategory() != null) {
 			setGroupError(getText(KEY_I18N_ERR_GROUP_EMPTY));
 			isError = true;
 		}
 		
 		//Empty validation for technology
 		if (CollectionUtils.isEmpty(getTechnology())) {
 			setTechError(getText(KEY_I18N_ERR_TECH_EMPTY));
 			isError = true;
 		}
 		
 		if (isError) {
 			setErrorFound(true);
 		}
 
 		return SUCCESS;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getNameError() {
 		return nameError;
 	}
 
 	public void setNameError(String nameError) {
 		this.nameError = nameError;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getVerError() {
 		return verError;
 	}
 
 	public void setVerError(String verError) {
 		this.verError = verError;
 	}
 
 	public List<String> getPlatform() {
 		return platform;
 	}
 
 	public void setPlatform(List<String> application) {
 		this.platform = application;
 	}
 
 	public String getPlatformTypeError() {
 		return platformTypeError;
 	}
 
 	public void setPlatformTypeError(String appltError) {
 		this.platformTypeError = appltError;
 	}
 
	public String getCategory() {
 		return category;
 	}
 
	public void setCategory(String group) {
 		this.category = group;
 	}
 	
 	public String getGroupError() {
 		return groupError;
 	}
 
 	public void setGroupError(String groupError) {
 		this.groupError = groupError;
 	}
 
 	public String getFileError() {
 		return fileError;
 	}
 
 	public void setFileError(String fileError) {
 		this.fileError = fileError;
 	}
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDownloadId() {
 		return downloadId;
 	}
 
 	public void setDownloadId(String id) {
 		this.downloadId = id;
 	}
 
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 	
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 
 	public void setOldVersion(String oldVersion) {
 		this.oldVersion = oldVersion;
 	}
 
 	public String getOldVersion() {
 		return oldVersion;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getType() {
 		return type;
 	}
 	
 	public void setTechnology(List<String> technology) {
 		this.technology = technology;
 	}
 	
 	public List<String> getTechnology() {
 		return technology;
 	}
 	
 	public void setTechError(String techError) {
 		this.techError = techError;
 	}
 	
 	public String getTechError() {
 		return techError;
 	}
 
 	public void setLicense(String license) {
 		this.license = license;
 	}
 
 	public String getLicense() {
 		return license;
 	}
 
 	public void setLicenseError(String licenseError) {
 		this.licenseError = licenseError;
 	}
 
 	public String getLicenseError() {
 		return licenseError;
 	}
 
 	public String getVersioning() {
 		return versioning;
 	}
 
 	public void setVersioning(String versioning) {
 		this.versioning = versioning;
 	}
 }
