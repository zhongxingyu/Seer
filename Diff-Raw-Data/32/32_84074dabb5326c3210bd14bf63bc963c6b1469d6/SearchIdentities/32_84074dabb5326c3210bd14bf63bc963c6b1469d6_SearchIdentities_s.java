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
 * $Id: SearchIdentities.java,v 1.1 2006-05-31 21:49:53 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.cli.idrepo;
 
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.ExitCodes;
 import com.sun.identity.cli.IArgument;
 import com.sun.identity.cli.IOutput;
 import com.sun.identity.cli.LogWriter;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdType;
 import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  * This command searches for Identities.
  */
 public class SearchIdentities extends IdentityCommand {
     private static final String ARGUMENT_FILTER = "filter";
     private static final String ARGUMENT_RECURSIVE = "recursive";
 
     /**
      * Services a Commandline Request.
      *
      * @param rc Request Context.
      * @throw CLIException if the request cannot serviced.
      */
     public void handleRequest(RequestContext rc) 
         throws CLIException {
         super.handleRequest(rc);
 
         SSOToken adminSSOToken = getAdminSSOToken();
         IOutput outputWriter = getOutputWriter();
         String realm = getStringOptionValue(IArgument.REALM_NAME);
         String type = getStringOptionValue(ARGUMENT_ID_TYPE);
         String filter = getStringOptionValue(ARGUMENT_FILTER);
         boolean recursive = isOptionSet(ARGUMENT_RECURSIVE);
 
         String[] params = {realm, type, filter};
         writeLog(LogWriter.LOG_ACCESS, Level.INFO,
             "ATTEMPT_SEARCH_IDENTITIES", params);
 
         try {
             AMIdentityRepository amir = new AMIdentityRepository(
                 adminSSOToken, realm);
             IdSearchControl isCtl = new IdSearchControl();
             isCtl.setRecursive(recursive);
             IdType idType = convert2IdType(type);
             IdSearchResults isr = amir.searchIdentities(idType, filter, isCtl);
             Set results = isr.getSearchResults();
 
             if ((results != null) && !results.isEmpty()) {
                IdSearchResults specialUsersResults =
                    amir.getSpecialIdentities(idType);
                Set specialUsers = specialUsersResults.getSearchResults();
 
                 for (Iterator i = results.iterator(); i.hasNext(); ) {
                     AMIdentity amid = (AMIdentity)i.next();
                    if (!specialUsers.contains(amid)) {
                        String[] args = {amid.getName(), amid.getUniversalId()};
                        outputWriter.printlnMessage(MessageFormat.format(
                          getResourceString("format-search-identities-results"),
                            (Object[])args));
                    }
                 }
             }
 
             outputWriter.printlnMessage(MessageFormat.format(
                 getResourceString("search-identities-succeed"), 
                 (Object[])params));
             writeLog(LogWriter.LOG_ACCESS, Level.INFO, 
                 "SUCCEED_SEARCH_IDENTITIES", params);
         } catch (IdRepoException e) {
             String[] args = {realm, type, filter, e.getMessage()};
             debugError("SearchIdentities.handleRequest", e);
             writeLog(LogWriter.LOG_ERROR, Level.INFO,
                 "FAILED_SEARCH_IDENTITIES", args);
             throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         } catch (SSOException e) {
             String[] args = {realm, type, filter, e.getMessage()};
             debugError("SearchIdentities.handleRequest", e);
             writeLog(LogWriter.LOG_ERROR, Level.INFO,
                 "FAILED_SEARCH_IDENTITIES", args);
             throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
 }
