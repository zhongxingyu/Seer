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
 /*
  * $Id: Login.java 471756 2006-11-06 15:01:43Z husted $
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package com.photon.phresco.framework.actions;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.framework.actions.applications.Projects;
 import com.photon.phresco.util.Credentials;
 import com.photon.phresco.util.Utility;
 
 public class Login extends FrameworkBaseAction {
 
     private static final long serialVersionUID = -1858839078372821734L;
     private static final Logger S_LOGGER = Logger.getLogger(Login.class);
     private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
     private static Boolean debugEnabled  = S_LOGGER.isDebugEnabled();
     
     private String username = null;
     private String password = null;
     private boolean loginFirst = true;
     
     private String logoImgUrl = "";
     private Map<String, String> frameworkTheme = null;
     private String brandingColor = "";
     private String bodyBackGroundColor = "";
 	private String accordionBackGroundColor = "";
 	private String menuBackGround = "";
 	private String menufontColor = "";
 	private String buttonColor = "";
 	private String pageHeaderColor = "";
 	private String labelColor = "";
 	private String copyRightColor = "";
 	private String copyRight = "";
 	private String disabledLabelColor = "";
 	private String customerId = "";
     
     public String login() throws IOException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Login.login()");
         }
         
         User user = (User) getSessionAttribute(SESSION_USER_INFO);
         if (user != null) {
             return SUCCESS;
         }
         if (loginFirst) {
             setReqAttribute(REQ_LOGIN_ERROR, "");
             return LOGIN_FAILURE;   
         }
         if (validateLogin()) {
             return authenticate();
         }
         
         return LOGIN_FAILURE;
     }
     
     public String logout() {
         if (debugEnabled) {
             S_LOGGER.debug("Entering Method  Login.logout()");
         }
         
         removeSessionAttribute(SESSION_USER_INFO);
         String errorTxt = (String) getSessionAttribute(REQ_LOGIN_ERROR);
         if (StringUtils.isNotEmpty(errorTxt)) {
             setReqAttribute(REQ_LOGIN_ERROR, getText(errorTxt));
         } else {
             setReqAttribute(REQ_LOGIN_ERROR, getText(SUCCESS_LOGOUT));
         }
         removeSessionAttribute(REQ_LOGIN_ERROR);
         Projects projects = new Projects();
         projects.clearMap();
         
         return SUCCESS;
     }
     
     private String authenticate() throws FileNotFoundException  {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Login.authenticate()");
         }
         
         User user = null;
         try {
         	Credentials credentials = new Credentials(getUsername(), getPassword());
         	user = doLogin(credentials);
        	List<Customer> listOfCustomers = user.getCustomers();
        	if (CollectionUtils.isNotEmpty(listOfCustomers)) {
        		Collections.sort(listOfCustomers, sortCusNameInAlphaOrder());
        	}
        	setReqAttribute(REQ_CUSTOMERS_LIST, listOfCustomers);
         	
         	if (user == null) {
         		setReqAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN));
 
         		return LOGIN_FAILURE;
         	}
         	if (!user.isPhrescoEnabled()) {
         		setReqAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN_ACCESS_DENIED));
 
         		return LOGIN_FAILURE;
         	}
         	setSessionAttribute(SESSION_USER_INFO, user);
 
         	//encode the password
         	byte[] encodedPwd = Base64.encodeBase64(getPassword().getBytes());
         	String encodedString = new String(encodedPwd);
 
         	setSessionAttribute(SESSION_USER_PASSWORD, encodedString);
 
         	File tempPath = new File(Utility.getPhrescoTemp() + File.separator + USER_JSON);
         	String userId = user.getId();
         	JSONObject userjson = new JSONObject();
         	JSONParser parser = new JSONParser();
         	String customerId = PHOTON;
         	if (tempPath.exists()) {
         		FileReader reader = new FileReader(tempPath);
         		userjson = (JSONObject)parser.parse(reader);
         		customerId = (String) userjson.get(userId);
         		reader.close();
         	} 
         	userjson.put(userId, customerId);
         
         	setReqAttribute(userId, customerId);
         	FileWriter  writer = new FileWriter(tempPath);
         	writer.write(userjson.toString());
         	writer.close();
 
         } catch (PhrescoWebServiceException e) {
         	if(e.getResponse().getStatus() == 204) {
 				setReqAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN_INVALID_USER));
 				return LOGIN_FAILURE;
 			} else {
 				setReqAttribute(REQ_LOGIN_ERROR, getText(ERROR_EXCEPTION));
 				return LOGIN_FAILURE;
 			}
         } catch (IOException e) {
         	return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_FRAMEWORKSTREAM));
 		} catch (ParseException e) {
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_FRAMEWORKSTREAM));
 		} 
         return SUCCESS;
     }
     
     private Comparator sortCusNameInAlphaOrder() {
 		return new Comparator(){
 		    public int compare(Object firstObject, Object secondObject) {
 		    	Customer customerName1 = (Customer) firstObject;
 		    	Customer customerName2 = (Customer) secondObject;
 		       return customerName1.getName().compareToIgnoreCase(customerName2.getName());
 		    }
 		};
 	}
 
     private boolean validateLogin() {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method  Login.validateLogin()");
         }
         
         if (StringUtils.isEmpty(getUsername())) {
             setReqAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN_INVALID_USERNAME));
             return false;
         }
         if (StringUtils.isEmpty(getPassword())) {
             setReqAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN_INVALID_PASSWORD));
             return false;
         }
         setReqAttribute(REQ_USER_NAME, getUsername());
         setReqAttribute(REQ_PASSWORD, getPassword());
         return true;
     }
     
     public String fetchLogoImgUrl() {
     	InputStream fileInputStream = null;
     	try {
     		fileInputStream = getServiceManager().getIcon(getCustomerId());
     		byte[] imgByte = null;
     		imgByte = IOUtils.toByteArray(fileInputStream);
     	    byte[] encodedImage = Base64.encodeBase64(imgByte);
             String encodeImg = new String(encodedImage);
             setLogoImgUrl(encodeImg);
     		
     		User user = (User) getSessionAttribute(SESSION_USER_INFO);
     		List<Customer> customers = user.getCustomers();
     		for (Customer customer : customers) {
 				if (customer.getId().equals(getCustomerId())) {
 					Map<String, String> theme = customer.getFrameworkTheme();
 					setBrandingColor(theme.get(BRANDING_COLOR));
 					setAccordionBackGroundColor(theme.get(ACCORDION_BACKGROUND_COLOR));
 					setBodyBackGroundColor(theme.get(BODYBACKGROUND_COLOR));
 					setButtonColor(theme.get(BUTTON_COLOR));
 					setPageHeaderColor(theme.get(PAGEHEADER_COLOR));
 					setCopyRightColor(theme.get(COPYRIGHT_COLOR));
 					setLabelColor(theme.get(LABEL_COLOR));
 					setMenuBackGround(theme.get(MENU_BACKGROUND_COLOR));
 					setMenufontColor(theme.get(MENU_FONT_COLOR));
 					setDisabledLabelColor(theme.get(DISABLED_LABEL_COLOR));
 					setCopyRight(theme.get(COPYRIGHT));
 					break;
 				}
 			}
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, getText(EXCEPTION_FETCHLOGO_IMAGE));
     	} catch (IOException e) {
     		return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_FRAMEWORK_THEME));
 		} finally {
     		try {
     			if (fileInputStream != null) {
     				fileInputStream.close();
     			}
 			} catch (IOException e) {
 				return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_FRAMEWORKSTREAM));
 			}
     	}
     	
     	return SUCCESS;
     }
     
     @SuppressWarnings("unchecked")
 	public String fetchCustomerId() {
     	try {
 			File tempPath = new File(Utility.getPhrescoTemp() + File.separator + USER_JSON);
 			User user = (User) getSessionAttribute(SESSION_USER_INFO);
 			JSONObject userjson = null;
 			JSONParser parser = new JSONParser();
 			String userId = user.getId();
 			String customerId = getCustomerId();
 			if (tempPath.exists()) {
 				FileReader reader = new FileReader(tempPath);
 				userjson = (JSONObject)parser.parse(reader);
 				reader.close();
 			} else {
 				userjson = new JSONObject();
 			}
 			
 			userjson.put(userId, customerId);
 			FileWriter  writer = new FileWriter(tempPath);
 			writer.write(userjson.toString());
 			writer.close();
 			
 		} catch (IOException e) {
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_FRAMEWORKSTREAM));
 		} catch (ParseException e) {
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_FRAMEWORKSTREAM));
 		}
     	return SUCCESS;
     }
     
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
     
     public boolean isLoginFirst() {
         return loginFirst;
     }
 
     public void setLoginFirst(boolean loginFirst) {
         this.loginFirst = loginFirst;
     }
 
 	public String getLogoImgUrl() {
 		return logoImgUrl;
 	}
 
 	public void setLogoImgUrl(String logoImgUrl) {
 		this.logoImgUrl = logoImgUrl;
 	}
 
 	public String getBrandingColor() {
 		return brandingColor;
 	}
 
 	public void setBrandingColor(String brandingColor) {
 		this.brandingColor = brandingColor;
 	}
 
 	public String getBodyBackGroundColor() {
 		return bodyBackGroundColor;
 	}
 
 	public Map<String, String> getFrameworkTheme() {
 		return frameworkTheme;
 	}
 
 	public void setFrameworkTheme(Map<String, String> frameworkTheme) {
 		this.frameworkTheme = frameworkTheme;
 	}
 
 	public String getAccordionBackGroundColor() {
 		return accordionBackGroundColor;
 	}
 
 	public String getMenuBackGround() {
 		return menuBackGround;
 	}
 
 	public String getMenufontColor() {
 		return menufontColor;
 	}
 
 	public String getButtonColor() {
 		return buttonColor;
 	}
 
 	public String getLabelColor() {
 		return labelColor;
 	}
 
 	public String getCopyRightColor() {
 		return copyRightColor;
 	}
 
 	public void setBodyBackGroundColor(String bodyBackGroundColor) {
 		this.bodyBackGroundColor = bodyBackGroundColor;
 	}
 
 	public void setAccordionBackGroundColor(String accordionBackGroundColor) {
 		this.accordionBackGroundColor = accordionBackGroundColor;
 	}
 
 	public void setMenuBackGround(String menuBackGround) {
 		this.menuBackGround = menuBackGround;
 	}
 
 	public void setMenufontColor(String menufontColor) {
 		this.menufontColor = menufontColor;
 	}
 
 	public void setButtonColor(String buttonColor) {
 		this.buttonColor = buttonColor;
 	}
 
 	public void setLabelColor(String labelColor) {
 		this.labelColor = labelColor;
 	}
 
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 
 	public String getCopyRight() {
 		return copyRight;
 	}
 
 	public void setCopyRight(String copyRight) {
 		this.copyRight = copyRight;
 	}
 
 	public void setCopyRightColor(String copyRightColor) {
 		this.copyRightColor = copyRightColor;
 	}
 
 	public String getDisabledLabelColor() {
 		return disabledLabelColor;
 	}
 
 	public void setDisabledLabelColor(String disabledLabelColor) {
 		this.disabledLabelColor = disabledLabelColor;
 	}
 
 	public String getPageHeaderColor() {
 		return pageHeaderColor;
 	}
 
 	public void setPageHeaderColor(String pageHeaderColor) {
 		this.pageHeaderColor = pageHeaderColor;
 	}
 
 }
