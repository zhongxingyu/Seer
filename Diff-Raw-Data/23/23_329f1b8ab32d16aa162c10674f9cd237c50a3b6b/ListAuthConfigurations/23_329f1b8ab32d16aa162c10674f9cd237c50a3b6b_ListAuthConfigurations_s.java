 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: ListAuthConfigurations.java,v 1.3 2008-12-16 06:47:01 veiming Exp $
  *
  */
 
 package com.sun.identity.cli.authentication;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.config.AMAuthConfigUtils;
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.ExitCodes;
 import com.sun.identity.cli.IArgument;
 import com.sun.identity.cli.LogWriter;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.log.Level;
 import com.sun.identity.sm.SMSException;
 import java.util.Iterator;
 import java.util.Set;
 
 public class ListAuthConfigurations extends AuthenticatedCommand {
     
     /**
      * Handles request.
      *
      * @param rc Request Context.
      * @throws CLIException if request cannot be processed.
      */
     public void handleRequest(RequestContext rc)
         throws CLIException {
         super.handleRequest(rc);
         ldapLogin();
         SSOToken adminSSOToken = getAdminSSOToken();
 
         String realm = getStringOptionValue(IArgument.REALM_NAME);
         String[] params = {realm};
         writeLog(LogWriter.LOG_ACCESS, Level.INFO,
             "ATTEMPT_LIST_AUTH_CONFIGURATIONS", params);
         try {
             Set configurations = AMAuthConfigUtils.getAllNamedConfig(
                 realm, adminSSOToken);
             if ((configurations != null) && !configurations.isEmpty()) {
                 getOutputWriter().printlnMessage(
                     getResourceString(
                     "authentication-list-auth-configurations-succeeded"));
                 
                 for (Iterator i = configurations.iterator(); i.hasNext(); ) {
                     String name = (String)i.next();
                     getOutputWriter().printlnMessage(name);
                 }
             } else {
                 getOutputWriter().printlnMessage(
                     getResourceString(
                   "authentication-list-auth-configurations-no-configurations"));
             }
             
             writeLog(LogWriter.LOG_ACCESS, Level.INFO,
                 "SUCCEEDED_LIST_AUTH_CONFIGURATIONS", params);
         } catch (SMSException e) {
             debugError("ListAuthConfigurations.handleRequest", e);
             writeLog(LogWriter.LOG_ERROR, Level.INFO,
                 "FAILED_LIST_AUTH_CONFIGURATIONS", params);
             throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         } catch (SSOException e) {
             debugError("ListAuthConfigurations.handleRequest", e);
             writeLog(LogWriter.LOG_ERROR, Level.INFO,
                 "FAILED_LIST_AUTH_CONFIGURATIONS", params);
             throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
 
     }
 }
