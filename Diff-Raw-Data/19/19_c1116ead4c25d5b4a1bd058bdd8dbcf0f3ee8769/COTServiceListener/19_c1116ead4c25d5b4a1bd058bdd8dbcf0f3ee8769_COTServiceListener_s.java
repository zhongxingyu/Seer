 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: COTServiceListener.java,v 1.1 2006-10-30 23:13:58 qcheng Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.cot;
 
 
 import com.sun.identity.shared.debug.Debug;
 
 import com.sun.identity.plugin.configuration.ConfigurationListener;
 import com.sun.identity.plugin.configuration.ConfigurationActionEvent;
 
 /**
  * This interface listens for circle of trust configuration
  * changes.
  */
 public class COTServiceListener implements ConfigurationListener {
     
    private static final String CONFIG_NAME = COTConstants.COT_CONFIG_NAME;
    
     /**
      * Constructor.
      */
     public COTServiceListener() {
     }
     
     
     public void configChanged(ConfigurationActionEvent e) {
        String configName = e.getConfigurationName();
        
        if (configName == null || configName.equals(CONFIG_NAME) ||
                configName.equals(CONFIG_NAME)) {
            
            if (COTUtils.debug.messageEnabled()) {
                COTUtils.debug.message("COTServiceListener.configChanged:");
            }
            COTCache.clear();
         }
     }
 }
