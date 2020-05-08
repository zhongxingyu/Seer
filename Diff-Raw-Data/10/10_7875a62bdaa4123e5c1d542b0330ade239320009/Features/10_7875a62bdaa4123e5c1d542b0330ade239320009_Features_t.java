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
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
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
 
 public class Features extends ServiceBaseAction {
 
     private static final long serialVersionUID = 6801037145464060759L;
     
 	private static final Logger S_LOGGER = Logger.getLogger(Features.class);
 	private static Boolean s_isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 	
 	private static byte[] s_featureByteArray = null;
 	
 	private String name = "";
 	private String customerId = "";
 	private String description = "";
     private String helpText = "";
     private String technology = "";
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
     
     private List<String> dependentModGroupId = null;
     
     private String fromPage = "";
 
 	private String nameError = "";
 	private String artifactIdError = "";
     private String groupIdError = "";
 	private String fileError = "";
 	private String verError = "";
 	private String licenseError = "";
 	private boolean errorFound = false;
 	private String fileType = "";
 	private static String featureJarFileName = "";
 	private static String iconName = "";
     
 	public String menu() {
 		if (s_isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.menu()");
     	}
 
 		setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		inputStreamMap.clear();
 		s_featureByteArray = null;
 		
     	return COMP_FEATURES_LIST;
     }
 	
 	public String technologies() {
 	    if (s_isDebugEnabled) {
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
     	if (s_isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.list()");
     	}
     	
     	try {
     		List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
     		setReqAttribute(REQ_ARCHE_TYPES, technologies);
     		s_featureByteArray = null;
     	} catch (PhrescoException e) {
     	    throw new PhrescoException(e);
     	}
     }
     
     public String listFeatures() throws PhrescoException {
     	if (s_isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.featurelist()");
     	}
     	
 		List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), Type.valueOf(getType()).name());
 		setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups);
 		setReqAttribute(REQ_FEATURES_TYPE, getType());
 		setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		
 		return COMP_FEATURES_LIST;
     }
     
     public String addFeatures() {
         if (s_isDebugEnabled) {
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
       if (s_isDebugEnabled) {
           S_LOGGER.debug("Entering Method  Features.featurelist()");
       }
       
       try {
           List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnology(), Type.valueOf(getType()).name());
           setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups);
       } catch (PhrescoException e) {
           return showErrorPopup(e, getText(EXCEPTION_FEATURE_LIST));
       }
       
       return COMP_FEATURES_LIST;
   }
     
     public void saveDependentFeatures() {
         if (s_isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.saveDependentFeatures()");
         }
         
         List<String> dependentModuleIds = new ArrayList<String>(dependentModGroupId.size());
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
             
             if(s_featureByteArray != null){
 				inputStreamMap.put(moduleGroup.getName(),  new ByteArrayInputStream(s_featureByteArray));
 			} 
             getServiceManager().createFeatures(moduleGroup, inputStreamMap, getCustomerId());
             setTechnologiesInRequest();
             addActionMessage(getText(FEATURE_ADDED, Collections.singletonList(getName())));
         } catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_FEATURE_SAVE));
         }
         
         return listFeatures();
     }
 	
 	public String edit() {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.edit()");
 		}
 		
 		try {
 		    setTechnologiesInRequest();
 		    ArtifactGroup moduleGroup = getServiceManager().getFeature(getModuleGroupId(), getCustomerId(), technology, Type.valueOf(getType()).name());
 	        setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroup);
 	        setReqAttribute(REQ_FEATURES_TYPE, getType());
             setReqAttribute(FEATURES_SELECTED_TECHNOLOGY, getTechnology());
 	        setReqAttribute(REQ_FEATURES_SELECTED_MODULEID, getModuleId());
 	        setReqAttribute(REQ_FROM_PAGE, EDIT);
 	        setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 	        setReqAttribute(REQ_FEATURES_LICENSE, getLicences());
 		} catch (PhrescoException e) {
 			showErrorPopup(e, getText(EXCEPTION_FEATURE_EDIT));
 		}
 
 		return COMP_FEATURES_ADD;
 	}
 	
     public String update() throws PhrescoException, IOException {
         if (s_isDebugEnabled) {
             S_LOGGER.debug("Entering Method Features.update()");
         }
         
         try {
             ArtifactGroup moduleGroup = createModuleGroup(Type.valueOf(getType()));
             if(s_featureByteArray != null){
 				inputStreamMap.put(moduleGroup.getName(),  new ByteArrayInputStream(s_featureByteArray));
 			} 
             getServiceManager().updateFeature(moduleGroup, inputStreamMap, getCustomerId());
             addActionMessage(getText(FEATURE_ADDED, Collections.singletonList(getName())));
             setTechnologiesInRequest();
         } catch (PhrescoException e) {
             showErrorPopup(e, getText(EXCEPTION_FEATURE_SAVE));
         }
         
         return listFeatures();
     }
 	
 	private ArtifactGroup createModuleGroup(Type type) throws PhrescoException {
 	    if (s_isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.createModuleGroup()");
         }
         
         try {
             ArtifactGroup artifactGroup = new ArtifactGroup();
             artifactGroup.setName(getName());
             if (StringUtils.isNotEmpty(getModuleGroupId())) {
                 artifactGroup.setId(getModuleGroupId());
             }
             artifactGroup.setDescription(getDescription());
             artifactGroup.setGroupId(getGroupId());
             artifactGroup.setArtifactId(getArtifactId());
             artifactGroup.setType(type);
             artifactGroup.setPackaging(getExtension(featureJarFileName));
             // To set appliesto tech and core
             List<CoreOption> appliesTo = new ArrayList<CoreOption>();
             CoreOption moduleCoreOption = new CoreOption(getTechnology(), Boolean.parseBoolean(getModuleType()));
             appliesTo.add(moduleCoreOption);
             artifactGroup.setAppliesTo(appliesTo);
             artifactGroup.setCustomerIds(Arrays.asList(getCustomerId()));
             //To set license
             artifactGroup.setLicenseId(getLicense());
             //To set the details of the version
             ArtifactInfo artifactInfo = new ArtifactInfo();
             artifactInfo.setDescription(getDescription());
             artifactInfo.setHelpText(getHelpText());
             artifactInfo.setVersion(getVersion());
             
             //To set whether the feature is default to the technology or not
             List<RequiredOption> required = new ArrayList<RequiredOption>();
             RequiredOption requiredOption = new RequiredOption(getTechnology(), Boolean.parseBoolean(getDefaultType()));
             required.add(requiredOption);
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
         if (s_isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Features.deleteJSLibs()");
         }
         
         try {
             String[] moduleGroupIds = getHttpRequest().getParameterValues(REQ_FEATURES_MOD_GRP);
             if (ArrayUtils.isNotEmpty(moduleGroupIds)) {
                 for (String moduleGroupId : moduleGroupIds) {
                     getServiceManager().deleteFeature(moduleGroupId, getCustomerId());
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
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.uploadFile()");
 		}
 		
 		PrintWriter writer = null;
 		try {
             writer = getHttpResponse().getWriter();
 	        byte[] tempFeaByteArray = getByteArray();
 	        
 	        if (REQ_FEATURES_UPLOADTYPE.equals(getFileType())) {
 	        	uploadFeature(writer, tempFeaByteArray);
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
 
 	private void uploadFeature(PrintWriter writer, byte[] tempFeaByteArray) throws PhrescoException {
 		try {
 			featureJarFileName = getFileName();
     		s_featureByteArray = tempFeaByteArray;
     		getArtifactGroupInfo(writer, tempFeaByteArray);
     		inputStreamMap.put(Content.Type.ARCHETYPE.name(), new ByteArrayInputStream(tempFeaByteArray));
 		} catch (Exception e) {
 			e.printStackTrace();
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private String getExtension(String fileName) {
 		String fileExt = "jar";
 		if(fileName.endsWith("zip")) {
 			fileExt = "zip";
 		} else if(fileName.endsWith("dll")) {
 			fileExt = "dll";
 		}
 		return fileExt;
 	}
 	
 	public void removeUploadedFile() {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.removeUploadedFile()");
 		}
 		 if (REQ_FEATURES_UPLOADTYPE.equals(getFileType())) {
 			 s_featureByteArray = null;
 			 featureJarFileName = "";
 		 } else {
 			inputStreamMap.remove(Content.Type.ICON.name());
 			iconName = "";
 		 }
 		
 	}
 
 	public String validateForm() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.validateForm()");
 		}
 		
 		boolean isError = false;
         //Empty validation for name
         if (StringUtils.isEmpty(getName())) {
             setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
             isError = true;
         }
         //Validate whether file is selected during add
        if (!EDIT.equals(getFromPage()) && s_featureByteArray == null) {
             setFileError(getText(KEY_I18N_ERR_APPLNJAR_EMPTY));
             isError = true;
         }
         
         if(StringUtils.isEmpty(getLicense())) {
         	setLicenseError(getText(KEY_I18N_ERR_LICEN_EMPTY));
             isError = true;
         }
         
         if (s_featureByteArray != null) {
             //Empty validation for groupId if file is selected
             if (StringUtils.isEmpty(getGroupId())) {
                 setGroupIdError(getText(KEY_I18N_ERR_GROUPID_EMPTY));
                 isError = true;
             }
             //Empty validation for artifactId if file is selected
             if (StringUtils.isEmpty(getArtifactId())) {
                 setArtifactIdError(getText(KEY_I18N_ERR_ARTIFACTID_EMPTY));
                 isError = true;
             }
             //Empty validation for version if file is selected
             if (StringUtils.isEmpty(getVersion())) {
                 setVerError(getText(KEY_I18N_ERR_VER_EMPTY));
                 isError = true;
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
 }
