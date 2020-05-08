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
 import java.util.Collections;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.model.Documentation;
 import com.photon.phresco.model.Documentation.DocumentationType;
 import com.photon.phresco.model.Module;
 import com.photon.phresco.model.ModuleGroup;
 import com.photon.phresco.model.Technology;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.admin.commons.LogErrorReport;
 import com.photon.phresco.service.client.api.Content;
 import com.photon.phresco.util.ServiceConstants;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.MultiPart;
 
 public class Features extends ServiceBaseAction {
 
 	private static final long serialVersionUID = 6801037145464060759L;
 	private static final Logger S_LOGGER = Logger.getLogger(Features.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private String name = null;
 	private String nameError = null;
 	private String versError = null;
 	private String fileError = null;
 	private boolean errorFound = false;
 	
     private String customerId = null;
     private String fromPage = null;
     private String techId = null;
     private String type = null;
     
     private String artifactId = "";
     private String groupId = "";
     
 	private static byte[] featureByteArray = null;
 	private static String featureJarName = null;
     
 	private String description = null;
 	private String helpText = "";
 	private List<Module> versions = null;
 	private String technology = "";
 	
 	private String version = "";
     private String verError = "";
 	private String oldVersion = "";
 	private String moduleType = "";
 	private String defaultType = "";
 	
 	private String from = "";
 	private String appJarError = "";
 
 	public String menu() {
 		if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.menu()");
     	}
 
 		getHttpRequest().setAttribute(REQ_CUST_CUSTOMER_ID, customerId);
 
     	return COMP_FEATURES_LIST;
     }
 	
     public String list() {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.list()");
     	}
     	
     	try {
       		getHttpRequest().setAttribute(REQ_CUST_CUSTOMER_ID, customerId);
 			getHttpRequest().setAttribute(REQ_FEATURES_TYPE, type);
 			if (REQ_FEATURES_MODULE.equals(type)) {
 				getHttpRequest().setAttribute(REQ_FEATURES_HEADER, getText(KEY_I18N_FEATURE_MOD_ADD));
 			} else {
 				getHttpRequest().setAttribute(REQ_FEATURES_HEADER, getText(KEY_I18N_FEATURE_JS_ADD));
 			}
     		List<Technology> technologies = getServiceManager().getArcheTypes(customerId);
     		getHttpRequest().setAttribute(REQ_ARCHE_TYPES, technologies);
     	} catch (PhrescoException e){
     		new LogErrorReport(e, FEATURE_LIST_EXCEPTION);
     		
     		return LOG_ERROR;
     	}
     	
     	return COMP_FEATURES_LIST;
     }
     
     public String featuresWithList() {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method  Features.featurelist()");
     	}
     	
     	try {
     		List<ModuleGroup> moduleGroup = getServiceManager().getFeaturesByTech(customerId, technology, type);
     		getHttpRequest().setAttribute(REQ_FEATURES_MOD_GRP, moduleGroup);
     		getHttpRequest().setAttribute(REQ_CUST_CUSTOMER_ID, customerId);
     		if (StringUtils.isNotEmpty(from)) {
     		    return COMP_FEATURES_DEPENDENCY;
     		}
     	} catch (PhrescoException e){
     		new LogErrorReport(e, FEATURE_LIST_EXCEPTION);
     		
     		return LOG_ERROR;
     	}
     	
     	return COMP_FEATURES_LIST;
     }
 	
 	public String add() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.add()");
 		}
 		
 		List<Technology> technologies = getServiceManager().getArcheTypes(customerId);
 		getHttpRequest().setAttribute(REQ_ARCHE_TYPES, technologies);
 		getHttpRequest().setAttribute(REQ_CUST_CUSTOMER_ID, customerId);
 		getHttpRequest().setAttribute(REQ_FEATURES_TYPE, type);
 		if (REQ_FEATURES_MODULE.equals(type)) {
 			getHttpRequest().setAttribute(REQ_FEATURES_HEADER, getText(KEY_I18N_FEATURE_MOD_ADD));
 		} else {
 			getHttpRequest().setAttribute(REQ_FEATURES_HEADER, getText(KEY_I18N_FEATURE_JS_ADD));
 		}
 		
 		return COMP_FEATURES_ADD;
 	}
 	
 	public String edit() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.edit()");
 		}
 		
 		try {
 		    ModuleGroup moduleGroup = getServiceManager().getFeature(techId, customerId);
 			getHttpRequest().setAttribute(REQ_FEATURES_MOD_GRP, moduleGroup);
 			getHttpRequest().setAttribute(REQ_FROM_PAGE, REQ_EDIT);
 			getHttpRequest().setAttribute(REQ_CUST_CUSTOMER_ID, customerId);
 			if (REQ_FEATURES_MODULE.equals(type)) {
 				getHttpRequest().setAttribute(REQ_FEATURES_HEADER, getText(KEY_I18N_FEATURE_MOD_EDIT));
 			} else {
 				getHttpRequest().setAttribute(REQ_FEATURES_HEADER, getText(KEY_I18N_FEATURE_JS_EDIT));
 			}
 		} catch (PhrescoException e) {
 			new LogErrorReport(e, FEATURE_EDIT_EXCEPTION);
     		
 			return LOG_ERROR;
 		}
 
 		return COMP_FEATURES_ADD;
 	}
 	
 	public String save() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.save()");
 		}
 		
 		try {
 			MultiPart multiPart = new MultiPart();
 			BodyPart jsonPart = new BodyPart();
 			jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
 			jsonPart.setEntity(createModuleGroup());
 			Content content = new Content("object", name, null, null, null, 0);
 			jsonPart.setContentDisposition(content);
 			multiPart.bodyPart(jsonPart);
 			if (StringUtils.isNotEmpty(featureJarName)) {
 				InputStream featureIs = new ByteArrayInputStream(featureByteArray);
 				BodyPart binaryPart = getServiceManager().createBodyPart(name, FILE_FOR_APPTYPE, featureIs);
 				multiPart.bodyPart(binaryPart);
 			}
 			
 			ClientResponse clientResponse = getServiceManager().createFeatures(multiPart, customerId);
 			if (clientResponse.getStatus() != ServiceConstants.RES_CODE_200 && clientResponse.getStatus() != ServiceConstants.RES_CODE_201) {
 				addActionError(getText(FEATURE_NOT_ADDED, Collections.singletonList(name)));
 			} else {
 				addActionMessage(getText(FEATURE_ADDED, Collections.singletonList(name)));
 			}
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 			new LogErrorReport(e, FEATURE_SAVE_EXCEPTION);
     		
 			return LOG_ERROR;
 		} 
 
 		return list();
 	}
 	
 	private ModuleGroup createModuleGroup() {
 		ModuleGroup moduleGroup = new ModuleGroup();
 		List<Module> modules = new ArrayList<Module>();
 		Module module = new Module();
 		moduleGroup.setName(name);
 		moduleGroup.setTechId(technology);
 		if (FEATURES_CORE.equals(moduleType)) {
 			moduleGroup.setCore(true);
 		} else {
 			moduleGroup.setCore(false);
 		}
 		moduleGroup.setType(type);
 		moduleGroup.setCustomerId(customerId);
 		module.setName(name);
 		List<Documentation> docs = new ArrayList<Documentation>();
 		Documentation descDoc = new Documentation();
 		descDoc.setType(DocumentationType.DESCRIPTION);
 		descDoc.setContent(description);
 		docs.add(descDoc);
 		Documentation helpTextDoc = new Documentation();
 		helpTextDoc.setType(DocumentationType.HELP_TEXT);
 		helpTextDoc.setContent(helpText);
 		docs.add(helpTextDoc);
 		module.setDocs(docs);
 		module.setVersion(version);
 		module.setArtifactId(artifactId);
 		module.setGroupId(groupId);
 		if (StringUtils.isNotEmpty(defaultType)) {
 			module.setRequired(true);
 		} else {
 			module.setRequired(false);
 		}
 		modules.add(module);
 		moduleGroup.setVersions(modules);
 		
 		return moduleGroup;
 	}
 	
 	public String update() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.update()");
 		}
 		
 		try {
 			MultiPart multiPart = new MultiPart();
 			
 			ModuleGroup moduleGroup = new ModuleGroup();
 			moduleGroup.setName(name);
 			moduleGroup.setTechId(technology);
 			moduleGroup.setVersions(versions);
 			moduleGroup.setCustomerId(customerId);
 			
 			BodyPart jsonPart = new BodyPart();
 			jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
 			jsonPart.setEntity(moduleGroup);
 			Content content = new Content("object", name, null, null, null, 0);
 			jsonPart.setContentDisposition(content);
 			multiPart.bodyPart(jsonPart);
 			    
 			if (StringUtils.isNotEmpty(featureJarName)) {
 				InputStream featureIs = new ByteArrayInputStream(featureByteArray);
 				BodyPart binaryPart2 = getServiceManager().createBodyPart(name, FILE_FOR_APPTYPE, featureIs);
 				multiPart.bodyPart(binaryPart2);
 			}
 			
 			getServiceManager().updateFeature(moduleGroup, techId, customerId);
 		} catch (PhrescoException e) {
 			new LogErrorReport(e, FEATURE_UPDATE_EXCEPTION);
     	
 			return LOG_ERROR;
 		}
 		
 		return list();	
 	}
 	
 	public String delete() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.delete()");
 		}
 		
 		try {
			String[] techIds = getHttpRequest().getParameterValues(REST_QUERY_TECHID);
			if (techIds != null) {
				for (String techId : techIds) {
					ClientResponse clientResponse = getServiceManager().deleteFeature(techId, customerId);
 					if (clientResponse.getStatus() != ServiceConstants.RES_CODE_200) {
 						addActionError(getText(FEATURE_NOT_DELETED));
 					}
 				}
 				addActionMessage(getText(FEATURE_DELETED));
 			}
 		} catch (PhrescoException e) {
 			new LogErrorReport(e, FEATURE_DELETE_EXCEPTION);
 			
     		return LOG_ERROR;
 		}
 		
 		return list();
 	}
 	
 	public String uploadFile() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.uploadFile()");
 		}
 		
 		PrintWriter writer = null;
 		try {
             writer = getHttpResponse().getWriter();
 	        featureJarName = getHttpRequest().getHeader(X_FILE_NAME);
 	       
 	        InputStream is = getHttpRequest().getInputStream();
 	        featureByteArray = IOUtils.toByteArray(is);
 	        getHttpResponse().setStatus(getHttpResponse().SC_OK);
 	        writer.print(SUCCESS_TRUE);
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
 			S_LOGGER.debug("Entering Method  Features.removeUploadedFile()");
 		}
 		
 		featureByteArray = null;
 		featureJarName = null;
 	}
 
 	public String validateForm() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.validateForm()");
 		}
 		
 		boolean isError = false;
 		if (StringUtils.isEmpty(name)) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
 			isError = true;
 		} 
 		
 		if (StringUtils.isEmpty(version)) {
 			setVersError(getText(KEY_I18N_ERR_VER_EMPTY));
 			isError = true;
 		} 
 		
 		if (featureByteArray == null) {
 		    setAppJarError(getText(KEY_I18N_ERR_APPLNJAR_EMPTY));
             isError = true;
 		}
 		/*else if (StringUtils.isEmpty(fromPage) || (!version.equals(oldVersion))) {
 			//To check whether the version already exist
 			List<ModuleGroup> moduleGroups = getServiceManager().getFeatures(customerId);
 			if (CollectionUtils.isNotEmpty(versions)) {
 				for (ModuleGroup moduleGroup : moduleGroups) {
 				    List<Module> versions = moduleGroup.getVersions();
 				    if (CollectionUtils.isNotEmpty(versions)) {
 				        for (Module module : versions) {
 	                        if (module.getName().equalsIgnoreCase(name) && module.getVersion().equals(version)) {
 	                            setVerError(getText(KEY_I18N_ERR_VER_ALREADY_EXISTS));
 	                            isError = true;
 	                            break;
 	                        }
                         }
 				    }
 				}
 			}
 		}*/
 		
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
 	
 	public String getVersError() {
 		return versError;
 	}
 
 	public void setVersError(String versError) {
 		this.versError = versError;
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
 	
 	public String getTechId() {
 		return techId;
 	}
 
 	public void setTechId(String techId) {
 		this.techId = techId;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List<Module> getVersions() {
 		return versions;
 	}
 
 	public void setVersions(List<Module> versions) {
 		this.versions = versions;
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
 	
 	public String getFrom() {
         return from;
     }
 
     public void setFrom(String from) {
         this.from = from;
     }
     
     public String getAppJarError() {
         return appJarError;
     }
 
     public void setAppJarError(String appJarError) {
         this.appJarError = appJarError;
     }
 }
