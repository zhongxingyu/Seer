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
 * $Id: Step2.java,v 1.8 2008-04-17 17:27:29 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.config.wizard;
 
 import com.sun.identity.config.util.AjaxPage;
 import com.sun.identity.setup.AMSetupServlet;
 import com.sun.identity.setup.SetupConstants;
 
 public class Step2 extends AjaxPage {
 
     public Step2() {
     }
     
     public void onInit() {
         String val = (String)getContext().getSessionAttribute("serverURL");
         if (val == null) {
             val = getServerURL();
         }
         add("serverURL", val);
 
         val = (String)getContext().getSessionAttribute("cookieDomain");
         if (val == null) {
             val = getCookieDomain();
         }
         add("cookieDomain", val);
 
         val = (String)getContext().getSessionAttribute("platformLocale");
         if (val == null) {
             val = SetupConstants.DEFAULT_PLATFORM_LOCALE;
         }
         add("platformLocale", val);
 
        val = (String)getContext().getSessionAttribute("configDirectory");
        if (val == null) {
            val = getBaseDir(getContext().getRequest());
         }
        add("configDirectory", val);
         super.onInit();
     }   
 
     private String getServerURL() {        
         String hostname = (String)getContext().getRequest().getServerName();
         int portnum  = (int)getContext().getRequest().getServerPort();
         String protocol = (String)getContext().getRequest().getScheme();
         return protocol + "://" + hostname + ":" + portnum;
     }
 
     /**
      * used to add the key to the page and to the session so it can 
      * be retrieved when the final store is done
      */
     private void add(String key, String value) {
         addModel(key, value);
         getContext().setSessionAttribute(key, value);
     }
 }
