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
 * $Id: StartupScriptFinder.java,v 1.1 2007-08-07 01:47:46 sean_brydon Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.agents.tools.weblogic.v10;
 
 import com.sun.identity.install.tools.configurator.IStateAccess;
 import com.sun.identity.install.tools.configurator.IDefaultValueFinder;
 
 /**
  * This task locates startup script for weblogic.
  */
 public class StartupScriptFinder implements IDefaultValueFinder, IConfigKeys {
     
     public String getDefaultValue(String key, IStateAccess state,
             String value) {
         String result = null;
         if (value != null) {
             result = value;
         } else {
             result = getDefaultStartupScriptPath();
         }
         
         return result;
     }
     
     
     private String getDefaultStartupScriptPath() {
         String result = null;
         String osName = System.getProperty(STR_OS_NAME_PROPERTY);
         if (osName.toLowerCase().startsWith(STR_WINDOWS)) {
             result =
                "C:\\bea\\user_projects\\domains\\mydomain\\startWebLogic.cmd";
         } else {
             result =
                    "/usr/local/bea/user_projects/domains/mydomain/" +
                     "startWebLogic.sh";
         }
         
         return result;
     }
     
 }
