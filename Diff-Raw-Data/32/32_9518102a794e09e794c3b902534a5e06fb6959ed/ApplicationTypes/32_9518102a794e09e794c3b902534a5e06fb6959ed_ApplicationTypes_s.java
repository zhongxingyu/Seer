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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 
 public class ApplicationTypes extends ServiceBaseAction { 
 
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(ApplicationTypes.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 
 	private String customerId = "";
 	
 	private String name = "";
 	private String description = "";
 	
 	private String appTypeId = "";
 	private String oldName = "";
 	
 	private String fromPage = "";
 	
     private String nameError = "";
     private boolean errorFound = false;
 
     public String list() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method ApplicationTypes.list()");
 	    }
 
 		try {
 		    List<ApplicationType> applicationTypes = getServiceManager().getApplicationTypes(getCustomerId());
 		    setReqAttribute(REQ_APP_TYPES, applicationTypes);
 		    setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_APPTYPES_LIST));
 		}
 
 		return COMP_APPTYPE_LIST;
 	}
 
 	public String add() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method ApplicationTypes.add()");
 	    }
 	    
 	    setReqAttribute(REQ_CUST_CUSTOMER_ID, getCustomerId());
 	    
 		return COMP_APPTYPE_ADD;
 	}
 
 	public String edit() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method ApplicationTypes.edit()");
 	    }
 		
 		try {
 		    ApplicationType appType = getServiceManager().getApplicationType(getAppTypeId(), getCustomerId());
 		    setReqAttribute(REQ_APP_TYPE, appType);
 		    setReqAttribute(REQ_FROM_PAGE, EDIT);
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_APPTYPES_EDIT));
 		}
 		
 		return COMP_APPTYPE_ADD;
 	}
 
 	public String save() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method ApplicationTypes.save()");
 	    }
 		
 		try {
 		    List<ApplicationType> appTypes = new ArrayList<ApplicationType>();
 			appTypes.add(createAppType());
 			getServiceManager().createApplicationTypes(appTypes, getCustomerId());
 			addActionMessage(getText(APPTYPES_ADDED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_APPTYPES_SAVE));
         }
 		
 		return list();
 	}
 
 	public String update() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Apptypes.update()");
 	    }
 
 		try {
 			ApplicationType appType = createAppType();
 			getServiceManager().updateApplicationType(appType, getAppTypeId(), getCustomerId());
 			addActionMessage(getText(APPTYPES_UPDATED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_APPTYPES_UPDATE));
         }
 
 		return list();
 	}
 
     private ApplicationType createAppType() throws PhrescoException {
         ApplicationType appType = new ApplicationType();
        appType.setId(getAppTypeId());
         appType.setName(getName());
         appType.setDescription(getDescription());
         appType.setCustomerIds(Arrays.asList(getCustomerId()));
         
         return appType;
     }
 
 	public String delete() throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method AppType.delete()");
 	    }
 
 		try {
 			String[] appTypeIds = getHttpRequest().getParameterValues(REQ_APP_TYPEID);
 			if (ArrayUtils.isNotEmpty(appTypeIds)) {
 				for (String appTypeid : appTypeIds) {
 					getServiceManager().deleteApplicationType(appTypeid, getCustomerId());
 				}
 				addActionMessage(getText(APPTYPES_DELETED));
 			}
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_APPTYPES_DELETE));
 		}
 
 		return list();
 	}
 
 	public String validateForm() {
 	    if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method AppType.validateForm()");
         }
 
 	    boolean isError = false;
 	    //Empty validation for name
 		if (StringUtils.isEmpty(getName())) {			
 		    setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
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
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
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
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public String getAppTypeId() {
 		return appTypeId;
 	}
 
 	public void setAppTypeId(String appTypeId) {
 		this.appTypeId = appTypeId;
 	}
 
 	public String getOldName() {
 		return oldName;
 	}
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
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
 }
