 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.rave.portal.web.model;
 
 import org.apache.rave.portal.model.PortalPreference;
 import org.apache.rave.portal.model.impl.PortalPreferenceImpl;
 
 import java.util.Map;
 
import static org.apache.rave.portal.web.util.PortalPreferenceKeys.INITIAL_WIDGET_STATUS;
import static org.apache.rave.portal.web.util.PortalPreferenceKeys.JAVASCRIPT_DEBUG_MODE;
import static org.apache.rave.portal.web.util.PortalPreferenceKeys.PAGE_SIZE;
import static org.apache.rave.portal.web.util.PortalPreferenceKeys.TITLE_SUFFIX;
 
 /**
  * Form object for portal preferences
  */
 public class PortalPreferenceForm {
 
     public static final String DEFAULT_PAGE_SIZE = "10";
     public static final String DEFAULT_TITLE_SUFFIX = "";
     public static final String DEFAULT_JAVASCRIPT_DEBUG_MODE = "1";
     public static final String DEFAULT_INITIAL_WIDGET_STATUS = "PREVIEW";
 
 
     private Map<String, PortalPreference> preferenceMap;
 
     public PortalPreferenceForm(Map<String, PortalPreference> preferenceMap) {
         super();
         this.preferenceMap = preferenceMap;
         populateMissingPreferences();
     }
 
     private void populateMissingPreferences() {
         if (getPageSize() == null) {
             preferenceMap.put(PAGE_SIZE, new PortalPreferenceImpl(PAGE_SIZE, DEFAULT_PAGE_SIZE));
         }
         if (getTitleSuffix() == null) {
             preferenceMap.put(TITLE_SUFFIX, new PortalPreferenceImpl(TITLE_SUFFIX, DEFAULT_TITLE_SUFFIX));
         }
         if (getJavaScriptDebugMode() == null) {
             preferenceMap.put(JAVASCRIPT_DEBUG_MODE, new PortalPreferenceImpl(JAVASCRIPT_DEBUG_MODE, DEFAULT_JAVASCRIPT_DEBUG_MODE));
         }
         if (getInitialWidgetStatus() == null){
        	preferenceMap.put(INITIAL_WIDGET_STATUS, new PortalPreferenceImpl(INITIAL_WIDGET_STATUS, DEFAULT_INITIAL_WIDGET_STATUS));
         }
     }
     
     public PortalPreference getInitialWidgetStatus(){
        return preferenceMap.get(INITIAL_WIDGET_STATUS);
     }
 
     public PortalPreference getPageSize() {
         return preferenceMap.get(PAGE_SIZE);
     }
 
     public void setPageSize(PortalPreference pageSize) {
         preferenceMap.put(PAGE_SIZE, pageSize);
     }
 
     public PortalPreference getTitleSuffix() {
         return preferenceMap.get(TITLE_SUFFIX);
     }
 
     public void setTitleSuffix(PortalPreference titleSuffix) {
         preferenceMap.put(TITLE_SUFFIX, titleSuffix);
     }
 
     public PortalPreference getJavaScriptDebugMode() {
         return preferenceMap.get(JAVASCRIPT_DEBUG_MODE);
     }
 
     public void setJavaScriptDebugMode(PortalPreference javaScriptDebugMode) {
         preferenceMap.put(JAVASCRIPT_DEBUG_MODE, javaScriptDebugMode);
     }
 
     public Map<String, PortalPreference> getPreferenceMap() {
         return preferenceMap;
     }
 
     public void setPreferenceMap(Map<String, PortalPreference> preferenceMap) {
         this.preferenceMap = preferenceMap;
     }
     
     public void setInitialWidgetStatus(PortalPreference initialWidgetStatus){
     	preferenceMap.put(INITIAL_WIDGET_STATUS, initialWidgetStatus);
     }
 }
