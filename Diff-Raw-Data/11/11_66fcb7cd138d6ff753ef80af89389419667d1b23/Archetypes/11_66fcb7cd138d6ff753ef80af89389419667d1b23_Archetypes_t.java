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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.service.util.ServerUtil;
 
 
 
 public class Archetypes extends ServiceBaseAction { 
 
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Archetypes.class);
 	private static Boolean s_isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	/* plugin and appln jar upload*/
 	private static Map<String, byte[]> s_pluginMap = new HashMap<String, byte[]>();
 	private static byte[] s_applnByteArray = null;
 	private static String s_applnJarName = null;
 
 	private String name = "";
 	private String nameError = "";
 	private String version = "";
 	
 	private String verError = "";
 	private String techvernError = "";
 	private String apptype = "";
 	private String appError = "";
 	private String fileError = "";
 	private boolean errorFound = false;
 	private String oldName = "";
 
 	private String description = "";
 	private String fromPage = "";
 	private String techId = "";
     private String customerId = "";
 	
 	private String versionComment = "";
 	private String techVersion = "";
 	
 	private String jarVersion = "";
 	private String groupId = "";
 	private String artifactId = "";
 	private String uploadPlugin = "";
 	private boolean archType = false;
 	
 	public String list() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.list()");
 		}
 
 		try {
 			List<Technology> technologies = getServiceManager().getArcheTypes(getCustomerId());
 			List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 			setReqAttribute(REQ_ARCHE_TYPES, technologies);
 			setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, EXCEPTION_ARCHETYPE_LIST);
 		}
 		
 		/* To clear appln & plugin input streams */
 		s_pluginMap.clear();
 		s_applnByteArray = null;
 		s_applnJarName = null;
 
 		return COMP_ARCHETYPE_LIST;
 	}
 
 	public String add() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Archetypes.add()");
 	    }
 
 		try {
 			List<ApplicationType> appTypes = getServiceManager().getApplicationTypes(getCustomerId());
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 			setReqAttribute(REQ_FROM_PAGE, ADD);
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, EXCEPTION_ARCHETYPE_ADD);
 		}
 
 		return COMP_ARCHETYPE_ADD;
 	}
 	
 	public String edit() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.edit()");
 		}
 
 		try {
 		    ServiceManager serviceManager = getServiceManager();
 			Technology technology = serviceManager.getArcheType(getTechId(), getCustomerId());
             List<ApplicationType> appTypes = serviceManager.getApplicationTypes(getCustomerId());
 			setReqAttribute(REQ_FROM_PAGE, EDIT);
             setReqAttribute(REQ_ARCHE_TYPE,  technology);
 			setReqAttribute(REQ_APP_TYPES, appTypes);
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, EXCEPTION_ARCHETYPE_EDIT);
 		}
 
 		return COMP_ARCHETYPE_ADD;
 	}
 	
 	public String save() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.save()");
 		}
 
 		try {
 			Technology technology = getTechnology();
 			List<InputStream> inputStreams = new ArrayList<InputStream>();
 			//save application jar files
 			if(s_applnByteArray != null){
 				inputStreams.add(new ByteArrayInputStream(s_applnByteArray));
 			} 
 			//save plugin jar files
 			if(s_pluginMap != null) {
 				Iterator iter = s_pluginMap.keySet().iterator();
 				while (iter.hasNext()) {
 					String key = (String) iter.next();
 					byte[] byteArray = (byte[]) s_pluginMap.get(key);
 					inputStreams.add(new ByteArrayInputStream(byteArray));
 				}
 			}
 			getServiceManager().createArcheTypes(technology, inputStreams, getCustomerId());
 			addActionMessage(getText(ARCHETYPE_ADDED, Collections.singletonList(name)));
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, EXCEPTION_ARCHETYPE_SAVE);
 		} 
 
 		return list();
 	}
 	
 	public String update() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.update()");
 		}
 		
 		try {
 			Technology technology = getTechnology();
 			List<InputStream> inputStreams = new ArrayList<InputStream>(); 
 			//update application jar files
 			if(s_applnByteArray != null){
 				inputStreams.add(new ByteArrayInputStream(s_applnByteArray));
 			} 
 			//update plugin jar files
 			if(s_pluginMap != null) {
 				Iterator iter = s_pluginMap.keySet().iterator();
 				while (iter.hasNext()) {
 					String key = (String) iter.next();
 					byte[] byteArray = (byte[]) s_pluginMap.get(key);
 					inputStreams.add(new ByteArrayInputStream(byteArray));
 				}
 				getServiceManager().updateArcheType(technology, inputStreams, getCustomerId());
				addActionMessage(getText(ARCHETYPE_UPDATED, Collections.singletonList(getName())));
 			}
 		}catch(PhrescoException e) {
 			return showErrorPopup(e, EXCEPTION_ARCHETYPE_UPDATE);
 		}
 
 		return list();
 	}
 
     /**
      * @return
      * @throws PhrescoException
      */
     public Technology getTechnology() throws PhrescoException {
     	if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.getTechnology()");
 		}
    	
     	String key ="";
     	List<ArtifactGroup> pluginInfos = new ArrayList<ArtifactGroup>();
     	if(s_pluginMap != null) {
 			Iterator iter = s_pluginMap.keySet().iterator();
 			while (iter.hasNext()) {
 				key = (String) iter.next();
 			    artifactId = getHttpRequest().getParameter(key+"_artifactId");
 			    groupId = getHttpRequest().getParameter(key+"_groupId");
 			    version = getHttpRequest().getParameter(key+"_version");
 
 			    ArtifactGroup pluginInfo = new ArtifactGroup();
 			    pluginInfo.setArtifactId(getArtifactId());
 			    pluginInfo.setGroupId(getGroupId());
 			    
 			    List<ArtifactInfo> artifactVersions = new ArrayList<ArtifactInfo>();
 			    ArtifactInfo artifactVersion = new ArtifactInfo();
 			    artifactVersion.setVersion(getVersion());
 			    artifactVersions.add(artifactVersion);
 			    pluginInfo.setVersions(artifactVersions);
 			    pluginInfos.add(pluginInfo);
 			}
 		}
     	
         Technology technology = new Technology();
         if (StringUtils.isNotEmpty(getTechId())) {
         	technology.setId(getTechId());
         }
         technology.setName(getName());
         technology.setDescription(getDescription());
         technology.setAppTypeId(getApptype());
         technology.setPlugins(pluginInfos);
         
         ArtifactGroup artifactGroup = new ArtifactGroup();
         artifactGroup.setArtifactId(getArtifactId());
         artifactGroup.setGroupId(getGroupId());
         artifactGroup.setPackaging(REQ_JAR_FILE);
         
         List<ArtifactInfo> artifactVersion = new ArrayList<ArtifactInfo>();
         ArtifactInfo artifactInfo = new ArtifactInfo();
         artifactInfo.setVersion(getVersion());
         artifactVersion.add(artifactInfo);
         artifactGroup.setVersions(artifactVersion);
         technology.setArchetypeInfo(artifactGroup);
         
         
         List<String> customerIds = new ArrayList<String>();
         customerIds.add(getCustomerId());
         technology.setCustomerIds(customerIds);
         
         List<String> techVersions = new ArrayList<String>();
         techVersions.add(getTechVersion());
         technology.setTechVersions(techVersions);
         
         return technology;
     }
 
 	public String delete() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.delete()");
 		}
 
 		try {
 			String[] techTypeIds = getHttpRequest().getParameterValues(REQ_ARCHE_TECHID);
 			if (ArrayUtils.isNotEmpty(techTypeIds)) {
                 ServiceManager serviceManager = getServiceManager();
 				for (String techId : techTypeIds) {
                     serviceManager.deleteArcheType(techId, getCustomerId());
 				}
 				addActionMessage(getText(ARCHETYPE_DELETED));
 			}
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, EXCEPTION_ARCHETYPE_DELETE);
 		}
 
 		return list();
 	}
 	
 	public String uploadJar() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.uploadAppJar()");
 		}
 		
 		PrintWriter writer = null;
 		try {
             writer = getHttpResponse().getWriter();
 	        byte[] tempApplnByteArray = getByteArray();
 	        
 	        if (isArchType()) {
 	        	boolean isArchetypeJar = ServerUtil.validateArchetypeJar(new ByteArrayInputStream(tempApplnByteArray));
 	        	if (isArchetypeJar) {
 	        		s_applnJarName = fileName;
 	        		s_applnByteArray = tempApplnByteArray;
 	        		getArtifactGroupInfo(writer, tempApplnByteArray);
 	        	} else {
 	        		s_applnJarName = null;
 	        		s_applnByteArray = null;
 	        		writer.print(INVALID_ARCHETYPE_JAR);
 	        	}
 	        } else {
 	        	String jarName = fileName;
 	        	byte[] byteArray = tempApplnByteArray;
 	        	getArtifactGroupInfo(writer, tempApplnByteArray);
 	        	s_pluginMap.put(jarName, byteArray);
 	        }
 	        writer.flush();
 	        writer.close();
 		} catch (Exception e) {
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
 			throw new PhrescoException(e);
 		}
 		
 		return SUCCESS;
 		
 	}
 
 	public String showPluginJarPopup() {
 		if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Archetypes.showPluginJarPopup()");
 	    }
 		
 		return uploadPlugin;
 	}
 	
 	public void removeUploadedJar() {
 		if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Archetypes.removeUploadedJar()");
 	    }
 		
 		String type = getHttpRequest().getParameter(REQ_JAR_TYPE);
 		if (REQ_PLUGIN_JAR.equals(type)) {
 			s_pluginMap.remove(getHttpRequest().getParameter(REQ_UPLOADED_JAR));
 		} else {
 			s_applnJarName = null;
 			s_applnByteArray = null;
 		}
 	}
 	
 	public String validateForm() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Archetypes.validateForm()");
 		}
 		
 		boolean isError = false;
 		if (StringUtils.isEmpty(getName())) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
 			isError = true;
 		} else if (ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
 			// To check whether the name already exist (Application type wise)
 			List<Technology> archetypes = getServiceManager().getArcheTypes(getCustomerId());
 			if (archetypes != null) {
 				for (Technology archetype : archetypes) {
 					if (archetype.getAppTypeId().equals(getApptype()) && archetype.getName().equalsIgnoreCase(getName())) {
 						setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST_APPTYPE));
 						isError = true;
 						break;
 					}
 				}
 			}
 		}
 
 		if (StringUtils.isEmpty(getVersion())) {
 			setVerError(getText(KEY_I18N_ERR_VER_EMPTY));
 			isError = true;
 		}
 		
 		if (StringUtils.isEmpty(getTechVersion())) {
 			setTechvernError(getText(KEY_I18N_ERR_TECHVER_EMPTY));
 			isError = true;
 		}
 
 		if (StringUtils.isEmpty(getApptype())) {
 			setAppError(getText(KEY_I18N_ERR_APPTYPE_EMPTY));
 			isError = true;
 		}
 		
 		if (StringUtils.isEmpty(s_applnJarName) || s_applnJarName == null) {
 			setFileError(getText(KEY_I18N_ERR_APPLNJAR_EMPTY));
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
 
 	public String getApptype() {
 		return apptype;
 	}
 
 	public void setApptype(String apptype) {
 		this.apptype = apptype;
 	}
 
 	public String getAppError() {
 		return appError;
 	}
 
 	public void setAppError(String appError) {
 		this.appError = appError;
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
 
 	public String getTechId() {
 		return techId;
 	}
 
 	public void setTechId(String techId) {
 		this.techId = techId;
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
 
 	public String getVersionComment() {
 		return versionComment;
 	}
 
 	public void setVersionComment(String versionComment) {
 		this.versionComment = versionComment;
 	}
 
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 	
 	public String getJarVersion() {
 		return jarVersion;
 	}
 
 	public void setJarVersion(String jarVersion) {
 		this.jarVersion = jarVersion;
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
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
 	}
 
 	public String getOldName() {
 		return oldName;
 	}
 
 	public String getTechVersion() {
 		return techVersion;
 	}
 
 	public void setTechVersion(String techVersion) {
 		this.techVersion = techVersion;
 	}
 
 	public String getTechvernError() {
 		return techvernError;
 	}
 
 	public void setTechvernError(String techvernError) {
 		this.techvernError = techvernError;
 	}
 
 	public String getUploadPlugin() {
 		return uploadPlugin;
 	}
 
 	public void setUploadPlugin(String uploadPlugin) {
 		this.uploadPlugin = uploadPlugin;
 	}
 
 	public void setArchType(boolean archType) {
 		this.archType = archType;
 	}
 
 	public boolean isArchType() {
 		return archType;
 	}
 }
