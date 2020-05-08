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
 import java.net.URL;
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
 import com.photon.phresco.service.util.ServerUtil;
 
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
 	private String groupId = "";
 	private String artifactId = "";
 	
 	private String nameError = "";
 	private String verError = "";
 	private String platformTypeError = "";
 	private String groupError = "";
 	private String techError = "";
 	private String fileError = "";
 	private String licenseError = "";
 	private String groupIdError="";
 	private String artifactIdError="";
 	private String iconError="";
 	private String downloadAtrifactId = "";
 	private String downloadGroupId = "";
 	private String downloadVersions = "";
 	private boolean errorFound = false;
 	
 	private String fromPage = "";
 	private String system = "";
 	private String customerId = "";
 	private String oldVersion = "";
 	private String oldName = "";
 	private String type = ""; // type of the file uploaded (file or image) 
 	private String versioning = "";
 	private String downloadURL = "";
 	private String extFileName = "";
 	private InputStream fileInputStream;
 	private String contentType = "";
 	private int contentLength;
    
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 	private static byte[] downloadByteArray = null;
 	private static byte[] imgByteArray = null;
 	private static String downloadZipFileName = "";
 	private static long size;
 	
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
 //			if(imgByteArray != null){
 //				inputStreamMap.put(downloadInfo.getName(),  new ByteArrayInputStream(imgByteArray));
 //			} 
 		    
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
 //			if(imgByteArray != null){
 //				inputStreamMap.put(downloadInfo.getName(),  new ByteArrayInputStream(imgByteArray));
 //			} 
 			getServiceManager().updateDownload(getDownloadInfo(), inputStreamMap, getCustomerId());
 			addActionMessage(getText(DOWNLOAD_UPDATED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_DOWNLOADS_UPDATE));
 		}
 
 		return list();
 	}
 	
 	 private DownloadInfo getDownloadInfo() throws PhrescoException {
         DownloadInfo downloadInfo = new DownloadInfo();
         
         String artifactId = getArtifactId();
     	String groupId = getGroupId();
     	//String version = getVersion();
     	 if ((StringUtils.isEmpty(artifactId) && StringUtils.isEmpty(groupId))) {
          	artifactId = getDownloadAtrifactId();
          	groupId = getDownloadGroupId();
          	//version = getDownloadVersions();
          }
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
         if (StringUtils.isNotEmpty(version)) {
         	downloadVersion.setVersion(version);
         } 
         downloadVersion.setFileSize(size);
         downloadVersions.add(downloadVersion);
         ArtifactGroup artifactGroup = new ArtifactGroup();
         artifactGroup.setId(downloadInfo.getId());
         List<String> customerIds = new ArrayList<String>();
         customerIds.add(getCustomerId());
         artifactGroup.setName(getName());
         artifactGroup.setCustomerIds(customerIds);
         artifactGroup.setVersions(downloadVersions);
         artifactGroup.setLicenseId(getLicense());
         if (StringUtils.isNotEmpty(artifactId) && StringUtils.isNotEmpty(groupId)) {
 	        artifactGroup.setGroupId(groupId);
 	        artifactGroup.setArtifactId(artifactId);
         } 
         artifactGroup.setPackaging(ServerUtil.getFileExtension(downloadZipFileName));
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
 	       
 	        downloadByteArray = getByteArray();
 	        downloadZipFileName = getFileName();
 	        size = getFileSize();
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
 
 	public String downloadArchive() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Downloads.downloadArchive()");
 		}
 
 		try {
 			DownloadInfo downloadInfo = getServiceManager().getDownload(getDownloadId(), getCustomerId());
 			String archiveUrl = downloadInfo.getArtifactGroup().getVersions().get(0).getDownloadURL();
 
 			URL url = new URL(archiveUrl);
 			fileInputStream = url.openStream();
 			String[] parts = archiveUrl.split(FORWARD_SLASH);
 			extFileName = parts[parts.length - 1];
 			contentType = url.openConnection().getContentType();
 			contentLength = url.openConnection().getContentLength();
 		} catch (Exception e) {
 			return showErrorPopup(new PhrescoException(e), getText(DOWNLOAD_FAILED));
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
 		
 		if (!Boolean.parseBoolean(getSystem())) {
 			//Empty validation for name
 			isError = nameValidation();
 			
 			isError = downloadsValidation();
 						
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
 			
 			if (StringUtils.isEmpty(getCategory())) {
 				setGroupError(getText(KEY_I18N_ERR_GROUP_EMPTY));
 				isError = true;
 			}	
 		}
 		
 		//Empty validation for technology
 		if (CollectionUtils.isEmpty(getTechnology())) {
 			setTechError(getText(KEY_I18N_ERR_TECH_EMPTY));
 			isError = true;
 		}
 		
 		//Empty validation for version
 		 if (StringUtils.isEmpty(getVersion())) {
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
 		} */
 		
 		if (isError) {
 			setErrorFound(true);
 		}
 
 		return SUCCESS;
 	}
 
 	
 	private boolean downloadsValidation() {
 		// TODO Auto-generated method stub
 		if(downloadByteArray != null ) {
 			//Empty validation for groupId if file is selected
             if (StringUtils.isEmpty(getGroupId())) {
                 setGroupIdError(getText(KEY_I18N_ERR_GROUPID_EMPTY));
                 errorFound = true;
             }
             //Empty validation for artifactId if file is selected
             if (StringUtils.isEmpty(getArtifactId())) {
                 setArtifactIdError(getText(KEY_I18N_ERR_ARTIFACTID_EMPTY));
                 errorFound = true;
             }             
         }
 		return errorFound;
 	}
 
 	private boolean nameValidation() throws PhrescoException {
 		// TODO Auto-generated method stub
 		if (StringUtils.isEmpty(getName())) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
 			errorFound = true;
 		} else if(ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
 			if(CollectionUtils.isNotEmpty(getTechnology())) {
 				for(String technologyList : getTechnology()) {
 					List<DownloadInfo> downloadInfos = getServiceManager().getDownloads(technologyList);
 					if(CollectionUtils.isNotEmpty(downloadInfos)) {
 						for (DownloadInfo downloadInfo : downloadInfos) {
 							if(downloadInfo.getName().equalsIgnoreCase(getName())) {
 								setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST));
 								errorFound = true;
 								break;
 							}
 						}
 					}
 				}
 			}
 			
 		}
 		return errorFound;
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
 	
 	public String getContentType() {
 		return contentType;
 	}
 
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
 	public int getContentLength() {
 		return contentLength;
 	}
 
 	public void setContentLength(int contentLength) {
 		this.contentLength = contentLength;
 	}
 
 	public void setFileInputStream(InputStream fileInputStream) {
 		this.fileInputStream = fileInputStream;
 	}
 	
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public String getExtFileName() {
 		return extFileName;
 	}
 
 	public void setExtFileName(String extFileName) {
 		this.extFileName = extFileName;
 	}
 
 	public void setGroupIdError(String groupIdError) {
 		this.groupIdError = groupIdError;
 	}
 
 	public String getGroupIdError() {
 		return groupIdError;
 	}
 
 	public void setArtifactIdError(String atrifactIdError) {
 		this.artifactIdError = atrifactIdError;
 	}
 
 	public String getArtifactIdError() {
 		return artifactIdError;
 	}
 
 	public void setIconError(String iconError) {
 		this.iconError = iconError;
 	}
 
 	public String getIconError() {
 		return iconError;
 	}
 
 	public String getSystem() {
 		return system;
 	}
 
 	public void setSystem(String system) {
 		this.system = system;
 	}
 
 	public String getDownloadAtrifactId() {
 		return downloadAtrifactId;
 	}
 
 	public void setDownloadAtrifactId(String downloadAtrifactId) {
 		this.downloadAtrifactId = downloadAtrifactId;
 	}
 
 	public String getDownloadGroupId() {
 		return downloadGroupId;
 	}
 
 	public void setDownloadGroupId(String downloadGroupId) {
 		this.downloadGroupId = downloadGroupId;
 	}
 
 	public String getDownloadVersions() {
 		return downloadVersions;
 	}
 
 	public void setDownloadVersions(String downloadVersions) {
 		this.downloadVersions = downloadVersions;
 	}
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
 	}
 
 	public String getOldName() {
 		return oldName;
 	}
 	
 }
