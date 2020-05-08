 /*******************************************************************************
  * Copyright 2012 Christian Ternes and Thorsten Volland
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.businessmanager.web.controller.page.user;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.model.SelectItem;
 
 import org.businessmanager.domain.security.User;
 import org.businessmanager.domain.settings.ApplicationSetting;
 import org.businessmanager.geodb.Country;
 import org.businessmanager.geodb.OpenGeoDB;
 import org.businessmanager.service.security.UserService;
 import org.businessmanager.service.settings.ApplicationSettingsService;
 import org.businessmanager.web.bean.UserBean;
 import org.businessmanager.web.controller.AbstractController;
 import org.businessmanager.web.controller.LanguageController;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Component;
 
 @Component("userPreferencesController")
 @Scope("view")
 public class UserPreferencesController extends AbstractController {
 
 	@Autowired
 	private OpenGeoDB openGeoService;
 	
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private LanguageController languageController;
 	
 	@Autowired
 	private ApplicationSettingsService settingsService;
 	
 	private User currentUser;
 	private Map<String, String> userPreferences = new HashMap<String, String>();
 	private UserBean bean = new UserBean();
 	private boolean showPasswordDialog = false;
 	
 	@PostConstruct
 	public void init() {
 		String username = SecurityContextHolder.getContext().getAuthentication().getName();
 		if(username != null) {
 			currentUser = userService.getUserByName(username);
 			initUserPreferences(username);
 		}
 	}
 	
 	private void initUserPreferences(String username) {
 		List<ApplicationSetting> settingsList = settingsService.getApplicationSettingsByUsername(username);
 		for (ApplicationSetting setting : settingsList) {
 			userPreferences.put(setting.getParamKey(), setting.getParamValue());
 		}
 	}
 
 	public List<Country> getAvailableCountries() {
 		String language = facesContext.getLocale().getLanguage();
 		return openGeoService.getListOfCountries(language);
 	}
 	
 	public List<SelectItem> getAvailableLanguages() {
 		return languageController.getAvailableLanguages();
 	}
 	
 	public void changePassword() {
 		String password = bean.getPassword();
 		currentUser.setPassword(password);
 		userService.updateUser(currentUser, true, currentUser.isAdministrator());
 		
 		addMessage(FacesMessage.SEVERITY_INFO, "userpreferences_password_changed");
 		showPasswordDialog = false;
 	}
 	
 	public void saveSettings() {
 		if(userPreferences != null) {
 			Iterator<String> iter = userPreferences.keySet().iterator();
 			while(iter.hasNext()) {
 				String key = iter.next();
 				String value = userPreferences.get(key);
 				if(value != null) {
 					settingsService.setApplicationSetting(ApplicationSetting.Group.USER_PREFERENCS, key, value, currentUser.getUsername());
 				}
 				
 				//set language
 				if(key.equals(ApplicationSettingsService.GENERAL_LANGUAGE)) {
 					languageController.setLocale(new Locale(value));
 				}
 			}
 			
 			addMessage(FacesMessage.SEVERITY_INFO, "settings_saved");
 		}
 	}
 
 	public User getCurrentUser() {
 		return currentUser;
 	}
 
 	public Map<String, String> getUserPreferences() {
 		return userPreferences;
 	}
 
 	public UserBean getBean() {
 		return bean;
 	}
 
 	public boolean getShowPasswordDialog() {
 		return showPasswordDialog;
 	}
 	
 	public void showPasswordDialog() {
 		showPasswordDialog = true;
 	}
 	
 }
