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
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
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
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.License;
 import com.photon.phresco.commons.model.RequiredOption;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.client.api.Content;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.service.client.impl.CacheKey;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.Utility;
 
 public class Features extends ServiceBaseAction {
 
     private static final long serialVersionUID = 6801037145464060759L;
     
 	private static final Logger S_LOGGER = Logger.getLogger(Features.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 	
 	private static byte[] featureByteArray = null;
 	
 	private static byte[] newtempFeaByteArray = null;
 	
 	private String extFileName="";	
 	private String name = "";
 	private String customerId = "";
 	private String description = "";
     private String helpText = "";
     private String technology = "";
     private List<String> multiTechnology = null; 
     private String type = "";
     private String groupId = "";
     private String artifactId = "";
     private String version = "";
     private String moduleType = "";
     private String defaultType = "";
     private String oldVersion = "";
     private String moduleGroupId = "";
     private String moduleId = "";
     private String license = "";
     private String oldName = "";
 	private String versioning = "";
     private InputStream fileInputStream;
     private String contentType;
     private int contentLength;
     private String featureArtifactId = "";
     private String featureGroupId = "";
     private String featureVersions = "";
     
     private List<String> dependentModGroupId = null;
     
     private String fromPage = "";
 
 	private String nameError = "";
 	private String artifactIdError = "";
     private String groupIdError = "";
 	private String fileError = "";
 	private String verError = "";
 	private String techError = "";
 	private String licenseError = "";
 	private boolean errorFound = false;
 	private String fileType = "";
 	private static String featureJarFileName = "";
 	private static String iconName = "";
 	private boolean tempError = false;
 	private String featureUrl = "";
 	private static String versionFile = "";
     
 	public String menu() {
 		if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.menu()");
     	}
 
 		setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		inputStreamMap.clear();
 		featureByteArray = null;
 		
     	return COMP_FEATURES_LIST;
     }
 	
 	public String technologies() {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.modulesList()");
         }
 	    
 	    try {
             setReqAttribute(REQ_FEATURES_TYPE, getType()); //for handle in feature sub menu
     	    setTechnologiesInRequest();
     	    setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
     	    //To remove the selected dependent moduleIds from the session
     	    removeSessionAttribute(SESSION_FEATURES_DEPENDENT_MOD_IDS);
 	    } catch (PhrescoException e) {
 	        return showErrorPopup(e, getText(EXCEPTION_FEATURE_LIST));
 	    }
 	    
 	    return COMP_FEATURES_LIST;
 	}
 	
     private void setTechnologiesInRequest() throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.list()");
     	}
     	
     	try {
     		List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
     		setReqAttribute(REQ_ARCHE_TYPES, technologies);
     		featureByteArray = null;
     	} catch (PhrescoException e) {
     	    throw new PhrescoException(e);
     	}
     }
     
     public String listFeatures() throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.featurelist()");
     	}
     	inputStreamMap.clear();
     	featureByteArray = null;
 		List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), Type.valueOf(getType()).name());
 		setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups);
 		setReqAttribute(REQ_FEATURES_TYPE, getType());
 		setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		
 		return COMP_FEATURES_LIST;
     }
     
     public String addFeatures() {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.addModules()");
         }
       
         try {
             setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
             setReqAttribute(REQ_FEATURES_TYPE, getType());
             setReqAttribute(REQ_FROM_PAGE, ADD);
             setTechnologiesInRequest();
             setReqAttribute(REQ_FEATURES_LICENSE, getLicences());
         } catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_FEATURE_ADD));            
         }
       
         return COMP_FEATURES_ADD;
     }
     
     private List<License> getLicences() throws PhrescoException {
     	return getServiceManager().getLicenses();
 	}
 
 	public String fetchFeaturesForDependency() {
       if (isDebugEnabled) {
           S_LOGGER.debug("Entering Method  Features.featurelist()");
       }
       
       try {
     	  if(CollectionUtils.isNotEmpty(getMultiTechnology())) {
     		  for(String technologyList : getMultiTechnology()){
     			  List<ArtifactGroup>  moduleGroups = getServiceManager().getFeatures(getCustomerId(), technologyList, Type.valueOf(getType()).name());
     			  setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups); 
     		  }
     	  }
       } catch (PhrescoException e) {
           return showErrorPopup(e, getText(EXCEPTION_FEATURE_LIST));
       }
       
       return COMP_FEATURES_LIST;
   }
     
     public void saveDependentFeatures() {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.saveDependentFeatures()");
         }
        List<String> dependentModuleIds = new ArrayList<String>();
         if (CollectionUtils.isNotEmpty(dependentModGroupId)) {
             for (String dependentModGroup : dependentModGroupId) {
                 dependentModuleIds.add(getHttpRequest().getParameter(dependentModGroup));
             }
         }
         setSessionAttribute(SESSION_FEATURES_DEPENDENT_MOD_IDS, dependentModuleIds);
     }
     
     public String save() throws PhrescoException, IOException {
         try {
             ArtifactGroup moduleGroup = createModuleGroup(Type.valueOf(getType()));
             
             /*if(featureByteArray != null){
 				inputStreamMap.put(moduleGroup.getName(),  new ByteArrayInputStream(featureByteArray));
 			} */
             getServiceManager().createFeatures(moduleGroup, inputStreamMap, getCustomerId());
             setTechnologiesInRequest();
             addActionMessage(getText(FEATURE_ADDED, Collections.singletonList(getName())));
         } catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_FEATURE_SAVE));
         }
         
         return listFeatures();
     }
 	
 	public String edit() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.edit()");
 		}
 		
 		try {
 		    setTechnologiesInRequest();
 		    versionFile = getVersioning();
 		    ArtifactGroup moduleGroup = getServiceManager().getFeature(getModuleGroupId(), getCustomerId(), technology, Type.valueOf(getType()).name());
 	        setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroup);
 	        setReqAttribute(REQ_FEATURES_TYPE, getType());
             setReqAttribute(FEATURES_SELECTED_TECHNOLOGY, getTechnology());
 	        setReqAttribute(REQ_FEATURES_SELECTED_MODULEID, getModuleId());
 	        setReqAttribute(REQ_FROM_PAGE, EDIT);
 	        setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 	        setReqAttribute(REQ_FEATURES_LICENSE, getLicences());
 	        setReqAttribute(REQ_VERSIONING, getVersioning());
 		} catch (PhrescoException e) {
 			showErrorPopup(e, getText(EXCEPTION_FEATURE_EDIT));
 		}
 
 		return COMP_FEATURES_ADD;
 	}
 	
     public String update() throws PhrescoException, IOException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method Features.update()");
         }
         
         try {
             ArtifactGroup moduleGroup = createModuleGroup(Type.valueOf(getType()));
             /*if(featureByteArray != null){
 				inputStreamMap.put(moduleGroup.getName(),  new ByteArrayInputStream(featureByteArray));
 			}*/ 
             getServiceManager().updateFeature(moduleGroup, inputStreamMap, getCustomerId());
             addActionMessage(getText(FEATURE_UPDATED, Collections.singletonList(getName())));
             setTechnologiesInRequest();
         } catch (PhrescoException e) {
             showErrorPopup(e, getText(EXCEPTION_FEATURE_SAVE));
         }
         
         return listFeatures();
     }
 	
 	private ArtifactGroup createModuleGroup(Type type) throws PhrescoException {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.createModuleGroup(Type type)");
         }
         
         try {
         	String artifactId = getArtifactId();
         	String groupId = getGroupId();
         	String version = getVersion();
         	if ((StringUtils.isEmpty(artifactId) && StringUtils.isEmpty(groupId) && StringUtils.isEmpty(version))) {
         		artifactId = getFeatureArtifactId();
         		groupId = getFeatureGroupId();
         		version = getFeatureVersions();
         	}
             ArtifactGroup artifactGroup = new ArtifactGroup();
             artifactGroup.setName(getName());
             if (StringUtils.isNotEmpty(getModuleGroupId())) {
                 artifactGroup.setId(getModuleGroupId());
             }
             artifactGroup.setDescription(getDescription());
             if (StringUtils.isNotEmpty(artifactId) && StringUtils.isNotEmpty(groupId)) {
 	            artifactGroup.setGroupId(groupId);
 	            artifactGroup.setArtifactId(artifactId);
             } else {
             	throw new PhrescoException(getText(EXCEPTION_ARTIFACTINFO_MISSING));
             }
             artifactGroup.setType(type);
             artifactGroup.setPackaging(ServerUtil.getFileExtension(featureJarFileName));
             // To set appliesto tech and core
             List<CoreOption> appliesTo = new ArrayList<CoreOption>();
             CoreOption moduleCoreOption = null;
             for (String multiTech : getMultiTechnology()){
             	moduleCoreOption = new CoreOption(multiTech, Boolean.parseBoolean(getModuleType()));
             	appliesTo.add(moduleCoreOption);
             }
             
             artifactGroup.setAppliesTo(appliesTo);
             artifactGroup.setCustomerIds(Arrays.asList(getCustomerId()));
             //To set license
             artifactGroup.setLicenseId(getLicense());
             //To set the details of the version
             ArtifactInfo artifactInfo = new ArtifactInfo();
             if (StringUtils.isNotEmpty(getModuleId())) {
             	artifactInfo.setId(getModuleId());
             }
             artifactInfo.setDescription(getDescription());
             artifactInfo.setHelpText(getHelpText());
             if (StringUtils.isNotEmpty(version)) {
             	artifactInfo.setVersion(version);
             } else {
             	throw new PhrescoException(getText(EXCEPTION_ARTIFACTINFO_MISSING));
             }
             
             //To set whether the feature is default to the technology or not
             List<RequiredOption> required = new ArrayList<RequiredOption>();
             RequiredOption requiredOption = null;
             for (String technology : getMultiTechnology()) {
             	requiredOption = new RequiredOption(technology, Boolean.parseBoolean(getDefaultType()));
             	required.add(requiredOption);
             }
             artifactInfo.setAppliesTo(required);
             
             //To set dependencies
             artifactInfo.setDependencyIds((List<String>) getSessionAttribute(SESSION_FEATURES_DEPENDENT_MOD_IDS));
             
             artifactGroup.setVersions(Arrays.asList(artifactInfo));
             
             return artifactGroup;
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
     }
     
     public String delete() throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.deleteJSLibs()");
         }
         
         try {
             String[] moduleGroupIds = getHttpRequest().getParameterValues(REQ_FEATURES_MOD_GRP);
             String[] moduleIds = getHttpRequest().getParameterValues(REQ_FEATURES_SELECTED_MODULEID);
             String customerId = getCustomerId();
             String tech = getTechnology();
             String type = getType();
             CacheKey key = new CacheKey(customerId, type, tech);
             ServiceManager serviceManager = getServiceManager(); 
             
 			if (ArrayUtils.isNotEmpty(moduleIds)) {
                 for (String moduleId : moduleIds) {
                 	serviceManager.deleteFeature(moduleId, key);
                 } addActionMessage(getText(FEATURE_DELETED));
             }
             
             if (ArrayUtils.isNotEmpty(moduleGroupIds)) {
                 for (String moduleGroupid : moduleGroupIds) {
                 	serviceManager.deleteFeature(moduleGroupid, key);
                 }
                 addActionMessage(getText(FEATURE_DELETED));
             }
             setTechnologiesInRequest();
         } catch (PhrescoException e) {
             showErrorPopup(e, getText(EXCEPTION_FEATURE_DELETE));
         }
         
         return listFeatures();
     }
 	
 	public String uploadFile() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.uploadFile()");
 		}
 		
 		PrintWriter writer = null;
 		boolean zipNameValidate =true;
 		try {
             writer = getHttpResponse().getWriter();
 	        byte[] tempFeaByteArray = getByteArray();
 	        
 	        featureJarFileName = getFileName();
 	        String ext = ServerUtil.getFileExtension(featureJarFileName);
 	        if(ext.equalsIgnoreCase(FILE_FORMAT)) {
 	        	zipNameValidate = extractArchive( new ByteArrayInputStream(tempFeaByteArray));
 	        }
 	        
 	        if (REQ_FEATURES_UPLOADTYPE.equals(getFileType())) {
 	        	if(zipNameValidate) {
 	        	    uploadFeature(writer, tempFeaByteArray);
 	        	} else {
 	        		writer.print(INVALID_MODULE_NAME);
 	        	}
 	        } else {
 	        	iconName = getFileName();
 	        	inputStreamMap.put(Content.Type.ICON.name(), new ByteArrayInputStream(tempFeaByteArray));
 	        	writer.print(SUCCESS_TRUE);
 	        }
 	        writer.flush();
 	        writer.close();
 		} catch (Exception e) {
 			//If upload fails it will be shown in UI, so need not to throw error popup
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 		}
 		
 		return SUCCESS;
 		
 	}
 	
 	public boolean extractArchive(ByteArrayInputStream inputStream) throws  IOException, PhrescoException {
 		FileOutputStream fileOutputStream = null;
 		String sysTemp = Utility.getSystemTemp();
 		File uploadFile = new File(sysTemp + getFileName());
 		fileOutputStream = new FileOutputStream(uploadFile);
 		try {
 			byte[] data = new byte[1024];
 			int i = 0;
 			while ((i = inputStream.read(data)) != -1) {
 				fileOutputStream.write(data, 0, i);
 			}
 			fileOutputStream.flush();
 			Utility.closeStream(fileOutputStream);
 
 			StringBuilder builder = new StringBuilder(sysTemp);
 			builder.append(File.separator);
 			builder.append(TEMP_FOLDER);
 			File tempFolder = new File(builder.toString());
 			if (!tempFolder.exists()) {
 				tempFolder.mkdir();
 			}
 			ArchiveUtil.extractArchive(uploadFile.getPath(), tempFolder.getPath(), ArchiveType.ZIP);
 
 			File folder = new File(tempFolder.getPath());
 			File[] listOfFiles = folder.listFiles();
 
 			for (File listOfFile : listOfFiles)
 				if (listOfFile.isDirectory())
 					if(!listOfFile.getName().equals(getModuleName()) || StringUtils.isEmpty(getModuleName())) {	
 						FileUtil.delete(new File(tempFolder.getPath()));
 						FileUtil.delete(new File(sysTemp + getFileName()));
 						return false;
 					}
 		} finally {
 			Utility.closeStream(inputStream);
 			Utility.closeStream(fileOutputStream);
 		}
 
 		return true;
 	}
 
 	private void uploadFeature(PrintWriter writer, byte[] tempFeaByteArray) throws PhrescoException {
 		try {
 			if (tempFeaByteArray == null) {
 				Features.newtempFeaByteArray = new byte[0];
 			} else {
 				Features.newtempFeaByteArray = Arrays.copyOf(tempFeaByteArray, tempFeaByteArray.length);
 			}
 			featureJarFileName = getFileName();
     		featureByteArray = newtempFeaByteArray;
     		getArtifactGroupInfo(writer, newtempFeaByteArray);
     		inputStreamMap.put(Content.Type.ARCHETYPE.name(), new ByteArrayInputStream(newtempFeaByteArray));
 		} catch (Exception e) {
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 			throw new PhrescoException(e);
 		}
 	}
 
 	public String downloadFeature() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.downloadFeature()");
 		}
 		
 		try {
 			setTechnologiesInRequest();
 			ArtifactGroup artiGroup = getServiceManager().getFeature(getModuleGroupId(), getCustomerId(), technology, Type.valueOf(getType()).name());
 			featureUrl = artiGroup.getVersions().get(0).getDownloadURL();
 
 			URL url = new URL(featureUrl);
 			fileInputStream = url.openStream();
 			String[] parts = featureUrl.split("/");
 			extFileName = parts[parts.length - 1];
 			contentType = url.openConnection().getContentType();
 			contentLength = url.openConnection().getContentLength();
 		} catch(Exception e) {
 			return showErrorPopup(new PhrescoException(e), getText(DOWNLOAD_FAILED));
 		}
 		
 		return SUCCESS;
 	}
 	
 	public void removeUploadedFile() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.removeUploadedFile()");
 		}
 		 if (REQ_FEATURES_UPLOADTYPE.equals(getFileType())) {
 			 featureByteArray = null;
 			 featureJarFileName = "";
 		 } else {
 			inputStreamMap.remove(Content.Type.ICON.name());
 			iconName = "";
 		 }
 		
 	}
 
 	public String validateForm() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.validateForm()");
 		}
 		
 		boolean isError = false;
         //Empty validation for name
         isError = nameValidation(isError);
         
         //Empty Multiple Technology selection
         isError = techValidation(isError);
         
         //Validate whether file is selected during add
         if ((!EDIT.equals(getFromPage()) && featureByteArray == null) || (StringUtils.isNotEmpty(versionFile) && featureByteArray == null)) {
             setFileError(getText(KEY_I18N_ERR_APPLNJAR_EMPTY));
             isError = true;
         }
         
         if(StringUtils.isEmpty(getLicense())) {
         	setLicenseError(getText(KEY_I18N_ERR_LICEN_EMPTY));
             isError = true;
         }
         
         isError = featureValidation(isError);
         if (isError) {
             setErrorFound(true);
         }
         
         return SUCCESS;
 	}
 
 	public boolean featureValidation(boolean isError) {
 		if (featureByteArray != null) {
             //Empty validation for groupId if file is selected
             if (StringUtils.isEmpty(getGroupId())) {
                 setGroupIdError(getText(KEY_I18N_ERR_GROUPID_EMPTY));
                 tempError = true;
             }
             //Empty validation for artifactId if file is selected
             if (StringUtils.isEmpty(getArtifactId())) {
                 setArtifactIdError(getText(KEY_I18N_ERR_ARTIFACTID_EMPTY));
                 tempError = true;
             }
             //Empty validation for version if file is selected
             if (StringUtils.isEmpty(getVersion())) {
                 setVerError(getText(KEY_I18N_ERR_VER_EMPTY));
                 tempError = true;
             }
             //To check whether the version already exist
             // TODO: Lohes(check must be done by querying the DB)
             /*if (StringUtils.isNotEmpty(getVersion()) && (StringUtils.isEmpty(getFromPage()) 
                     || (!getVersion().equals(getOldVersion())))) {
                 List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), getType());
                 if (StringUtils.isNotEmpty(getVersion())) {
                     for (ArtifactGroup moduleGroup : moduleGroups) {
                         List<ArtifactInfo> versions = moduleGroup.getVersions();
                         if (CollectionUtils.isNotEmpty(versions)) {
                             for (ArtifactInfo module : versions) {
                                 if (module.getName().equalsIgnoreCase(getName())
                                         && module.getVersion().equals(getVersion())) {
                                     setVerError(getText(KEY_I18N_ERR_VER_ALREADY_EXISTS));
                                     isError = true;
                                     break;
                                 }
                             }
                         }
                     }
                 }
             }*/
         }
 		return isError;
 	}
 
 	public boolean nameValidation(boolean isError) throws PhrescoException {
 
 		if (StringUtils.isEmpty(getName())) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
 			tempError = true;
 		} else if (ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
 			// To check whether the name already exist (Application type wise)
 			 for(String technologyList : getMultiTechnology()){
 				 List<ArtifactGroup>  moduleGroups = getServiceManager().getFeatures(getCustomerId(), technologyList, Type.valueOf(getType()).name());
 				if (CollectionUtils.isNotEmpty(moduleGroups)) {
 					for (ArtifactGroup moduleGroup : moduleGroups) {
 						if (moduleGroup.getName().equalsIgnoreCase(getName())) {
 							setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST_APPTYPE));
 							tempError = true;
 							break;
 						}
 					}
 				}
 			 }
 		}
 		
 		return tempError;
 	}
 	
 	public boolean techValidation(boolean isError) {
 		if (CollectionUtils.isEmpty(getMultiTechnology())) {
             setTechError(getText(KEY_I18N_MULTI_TECH_EMPTY));
             tempError = true;
         }
 		return tempError;
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
 	
 	public String getTechError() {
 		return techError;
 	}
 
 	public void setTechError(String techError) {
 		this.techError = techError;
 	}
 
 	public String getFileError() {
 		return fileError;
 	}
 
 	public void setFileError(String fileError) {
 		this.fileError = fileError;
 	}
 
 	public boolean getErrorFound() {
 		return errorFound;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 	
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 	
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public String getTechnology() {
 		return technology;
 	}
 
 	public void setTechnology(String technology) {
 		this.technology = technology;
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
 
 	public String getOldVersion() {
 		return oldVersion;
 	}
 
 	public void setOldVersion(String oldVersion) {
 		this.oldVersion = oldVersion;
 	}
 	
 	public String getArtifactId() {
 		return artifactId;
 	}
 
 	public void setArtifactId(String artifactId) {
 		this.artifactId = artifactId;
 	}
 
 	public String getGroupId() {
 		return groupId;
 	}
 
 	public void setGroupId(String groupId) {
 		this.groupId = groupId;
 	}
 	
 	public String getModuleType() {
 		return moduleType;
 	}
 
 	public void setModuleType(String moduleType) {
 		this.moduleType = moduleType;
 	}
 	
 	public String getDefaultType() {
 		return defaultType;
 	}
 
 	public void setDefaultType(String defaultType) {
 		this.defaultType = defaultType;
 	}
 
 	public String getHelpText() {
 		return helpText;
 	}
 
 	public void setHelpText(String helpText) {
 		this.helpText = helpText;
 	}
 	
 	public String getModuleGroupId() {
         return moduleGroupId;
     }
 
     public void setModuleGroupId(String moduleGroupId) {
         this.moduleGroupId = moduleGroupId;
     }
     
     public String getArtifactIdError() {
         return artifactIdError;
     }
 
     public void setArtifactIdError(String artifactIdError) {
         this.artifactIdError = artifactIdError;
     }
 
     public String getGroupIdError() {
         return groupIdError;
     }
 
     public void setGroupIdError(String groupIdError) {
         this.groupIdError = groupIdError;
     }
     
     public String getModuleId() {
         return moduleId;
     }
 
     public void setModuleId(String moduleId) {
         this.moduleId = moduleId;
     }
     
     public List<String> getDependentModGroupId() {
         return dependentModGroupId;
     }
 
     public void setDependentModGroupId(List<String> dependentModGroupId) {
         this.dependentModGroupId = dependentModGroupId;
     }
 
 	public void setLicense(String license) {
 		this.license = license;
 	}
 
 	public String getLicense() {
 		return license;
 	}
 	
 	public String getVersioning() {
 		return versioning;
 	}
 
 	public void setVersioning(String versioning) {
 		this.versioning = versioning;
 	}
 
 	public void setLicenseError(String licenseError) {
 		this.licenseError = licenseError;
 	}
 
 	public String getLicenseError() {
 		return licenseError;
 	}
 
 	public void setFileType(String fileType) {
 		this.fileType = fileType;
 	}
 
 	public String getFileType() {
 		return fileType;
 	}
 
 	public List<String> getMultiTechnology() {
 		return multiTechnology;
 	}
 
 	public void setMultiTechnology(List<String> multiTechnology) {
 		this.multiTechnology = multiTechnology;
 	}
 
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public void setFileInputStream(InputStream fileInputStream) {
 		this.fileInputStream = fileInputStream;
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
 
 	public String getFeatureUrl() {
 		return featureUrl;
 	}
 
 	public void setFeatureUrl(String featureUrl) {
 		this.featureUrl = featureUrl;
 	}
 
 	public String getExtFileName() {
 		return extFileName;
 	}
 
 	public void setExtFileName(String extFileName) {
 		this.extFileName = extFileName;
 	}
 
 	public String getOldName() {
 		return oldName;
 	}
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
 	}
 
 	public String getFeatureArtifactId() {
 		return featureArtifactId;
 	}
 
 	public void setFeatureArtifactId(String featureArtifactId) {
 		this.featureArtifactId = featureArtifactId;
 	}
 
 	public String getFeatureGroupId() {
 		return featureGroupId;
 	}
 
 	public void setFeatureGroupId(String featureGroupId) {
 		this.featureGroupId = featureGroupId;
 	}
 
 	public String getFeatureVersions() {
 		return featureVersions;
 	}
 
 	public void setFeatureVersions(String featureVersions) {
 		this.featureVersions = featureVersions;
 	}
 	
 }
