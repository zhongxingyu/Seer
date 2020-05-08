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
 * $Id: Step5.java,v 1.6 2008-02-04 20:57:19 jonnelson Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.config.wizard;
 
 import com.sun.identity.config.util.AjaxPage;
 import com.sun.identity.setup.AMSetupServlet;
 import com.sun.identity.setup.SetupConstants;
 import net.sf.click.control.ActionLink;
 import java.net.URL;
 import java.net.MalformedURLException;
 
 public class Step5 extends AjaxPage {
 
     public ActionLink clearLink = new ActionLink(
         "clear", this, "clear");
     public ActionLink validateLink = new ActionLink(
         "validateURL",this,"validateURL");
     public ActionLink validateSiteLink = new ActionLink(
         "validateSite",this,"validateSite");
 
     public Step5() {}
 
     public void onInit() {
         String host = (String)getContext().getSessionAttribute(
             SetupConstants.LB_SITE_NAME);
         String port = (String)getContext().getSessionAttribute(
             SetupConstants.LB_PRIMARY_URL); 
         
         if (host != null) {
             addModel("host", host);
         }
         if (port != null) {
             addModel("port", port);
         }
         super.onInit();
     }
 
     public boolean clear() {
         getContext().removeSessionAttribute(SetupConstants.LB_SITE_NAME);
         getContext().removeSessionAttribute(SetupConstants.LB_PRIMARY_URL);
         setPath(null);
         return false;
     }
 
     /** 
      * host= site config name
      * port = primary loadURL
      */
     public boolean validateURL() {
        boolean returnVal = true;
         String primaryURL = toString("port");
         if (primaryURL == null) {
            writeInvalid(getLocalizedString("missing.primary.url"));
         } else {
             try {
                 URL hostURL =  new URL(primaryURL);
                 getContext().setSessionAttribute( 
                     SetupConstants.LB_PRIMARY_URL, primaryURL);
                writeValid("OK"); 
             } catch (MalformedURLException m) {
                writeInvalid("Primary URL is not reachable");
                returnVal = false;
             }
         }
 
         setPath(null);
        return returnVal;
     }
 
     public boolean validateSite() {
         boolean returnVal = true;
         String siteName = toString("host");
         if (siteName == null) {
             writeInvalid(getLocalizedString("missing.site.name"));
             returnVal = false;
         } else {
             getContext().setSessionAttribute( 
                 SetupConstants.LB_SITE_NAME, siteName);
             writeValid(getLocalizedString("ok.label"));
         }
 
         setPath(null);
         return returnVal;
     }
 }
