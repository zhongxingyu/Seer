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
 * $Id: ListCircleOfTrustMembers.java,v 1.3 2007-04-03 17:42:20 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.federation.cli;
 
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.ExitCodes;
 import com.sun.identity.cli.IOutput;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.cot.CircleOfTrustManager;
 import com.sun.identity.cot.COTException;
 import com.sun.identity.cot.COTUtils;
 import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * List members in a Circle of Trust.
  */
 public class ListCircleOfTrustMembers extends AuthenticatedCommand {
     private static Debug debug = COTUtils.debug;
     
     private String realm;
     private String cot;
     private String spec;
     
     /**
      * List members in a circle of trust.
      *
      * @param rc Request Context.
      * @throws CLIException if unable to process this request.
      */
     public void handleRequest(RequestContext rc) 
         throws CLIException {
         super.handleRequest(rc);
         ldapLogin();
         realm = getStringOptionValue(FedCLIConstants.ARGUMENT_REALM, "/");
         cot = getStringOptionValue(FedCLIConstants.ARGUMENT_COT);
         IOutput outputWriter = getOutputWriter();
         spec=FederationManager.getIDFFSubCommandSpecification(rc);
         
         try {
             CircleOfTrustManager cotManager = new CircleOfTrustManager();
            Set circleOfTrusts = cotManager.getAllCirclesOfTrust(realm, spec);
            if (!circleOfTrusts.contains(cot)){
                Object[] obj = {cot};
                throw new CLIException(MessageFormat.format(
                    getResourceString(
                    "list-circle-of-trust-members-cot-does-not-exists"), obj),
                    ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
            }

            Set members = cotManager.listCircleOfTrustMember(realm, cot, spec);
             
             if ((members == null) || members.isEmpty()) {
                 Object[] obj = {cot};
                 outputWriter.printlnMessage(MessageFormat.format(
                     getResourceString(
                         "list-circle-of-trust-members-no-members"), obj));
             } else {
                 Object[] obj = {cot};
                 outputWriter.printlnMessage(MessageFormat.format(
                     getResourceString(
                         "list-circle-of-trust-members-members"), obj));
                 
                 for (Iterator i = members.iterator(); i.hasNext();) {
                      String entityId = (String)i.next();
                      outputWriter.printlnMessage("  " + entityId);
                 }
             }
         } catch (COTException e) {
             debug.warning("ListCircleOfTrustMembers.handleRequest", e);
             throw new CLIException(e.getMessage(),
                 ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
 }
