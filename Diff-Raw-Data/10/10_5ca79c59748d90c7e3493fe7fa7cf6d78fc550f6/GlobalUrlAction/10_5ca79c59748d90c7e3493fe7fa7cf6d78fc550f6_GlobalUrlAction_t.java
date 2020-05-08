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
 package com.photon.phresco.service.admin.actions.admin;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.Property;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 
 public class GlobalUrlAction extends ServiceBaseAction { 
 	
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(GlobalUrlAction.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private String name = "";
 	private String description = "";
 	private String url = "";
 	
 	private String nameError = "";
 	private String urlError = "";
 	private boolean errorFound = false;
 	
 	private String globalurlId = "";
 	
 	private String fromPage = "";
 	
 	//To get the all the globalURLs from the DB
 	public String list() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.list()");
 		}
 		try {
 			List<Property> globalUrls = getServiceManager().getGlobalUrls();
 			setReqAttribute(REQ_GLOBURL_URL, globalUrls);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e, getText(""));
 		}
 		return ADMIN_GLOBALURL_LIST;	
 	}
 	
 	//To return the page to add GlobalURL
 	public String add() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.add()");
 		}
 		setReqAttribute(REQ_FROM_PAGE, ADD);
 
 		return ADMIN_GLOBALURL_ADD;
 	}
 	
 	//To return the edit page with the details of the selected GlobalURL
 	public String edit() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.edit()");
 		}
 		
 		try {
 			  Property globalUrl = getServiceManager().getGlobalUrl(getGlobalurlId());
 			  setReqAttribute(REQ_GLOBURL_URL , globalUrl);
 			  setReqAttribute(REQ_FROM_PAGE, EDIT);
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_GLOBAL_URL_EDIT));
 		}
 		
 		return ADMIN_GLOBALURL_ADD;
 	}
 	
 	//To create a GlobalURL with the provided details
 	public String save() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.save()");
 		}
 
 		try  {
 			List<Property> globalURLs = new ArrayList<Property>();
 			globalURLs.add(createGlobalURL());
 			getServiceManager().createGlobalUrl(globalURLs);
 			addActionMessage(getText(URL_ADDED, Collections.singletonList(getName())));
 		}  catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_GLOBAL_URL_SAVE));
 		}
 
 		return  list();
 	}
 	
 	private Property createGlobalURL() {
 		Property globalUrl = new Property();
		if (StringUtils.isNotEmpty(getFromPage())) {
			globalUrl.setId(getGlobalurlId());
		}
 		globalUrl.setName(getName());
 		globalUrl.setDescription(getDescription());
 		globalUrl.setKey(globalUrl.getId());
 		globalUrl.setValue(getUrl());
 		
 		return globalUrl;
 	}
 
 	//To update the details of the selected GlobalURL
 	public String update() throws PhrescoException { 
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.update()");
 		}
 		try {
 			getServiceManager().updateGlobalUrl(createGlobalURL(), getGlobalurlId());
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_GLOBAL_URL_UPDATE));
 		}
 		return list();
 	}
 	
 	//To delete the selected GlobalURLs
 	public String delete() throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method GlobalUrlAction.delete()");
 	    }
 
 		try {
 			String[] globalUrlIds = getHttpRequest().getParameterValues(REQ_GLOBURL_ID);
 			if (ArrayUtils.isNotEmpty(globalUrlIds)) {
 				for (String globalUrlId : globalUrlIds) {
 					getServiceManager().deleteGlobalUrl(globalUrlId);
 				}
 				addActionMessage(getText(URL_DELETED));
 			}
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_GLOBAL_URL_DELETE));
 		}
 
 		return list();
 	}
 	
 	//To validate the form values passed from the jsp
 	public String validateForm() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.validateForm()");
 		}
 
 		boolean isError = false;
 		//Empty validation for name
 		if (StringUtils.isEmpty(getName())) {
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
 			isError = true;
 		} 
 
 		//Empty validation for url
 		if (StringUtils.isEmpty(getUrl())) {
 			setUrlError(getText(KEY_I18N_ERR_URL_EMPTY));
 			isError = true;
 		}
 		
 		if (isError) {
             setErrorFound(true);
         }
 
 		return SUCCESS;
 	}
 	
 	public String cancel() {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method GlobalUrlAction.list()");
 		}
 
 		return ADMIN_GLOBALURL_CANCEL;
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
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 	
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getUrlError() {
 		return urlError;
 	}
 
 	public void setUrlError(String urlError) {
 		this.urlError = urlError;
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
 
 	public String getGlobalurlId() {
 		return globalurlId;
 	}
 
 	public void setGlobalurlId(String globalurlId) {
 		this.globalurlId = globalurlId;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 
 	public String getFromPage() {
 		return fromPage;
 	}
 }
