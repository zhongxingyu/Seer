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
 * $Id: DeleteMetaData.java,v 1.2 2007-02-16 02:02:51 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.federation.cli;
 
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.ExitCodes;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.federation.meta.IDFFMetaException;
 import com.sun.identity.federation.meta.IDFFMetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import java.text.MessageFormat;
 
 /**
  * Delete a configuration and/or descriptor.
  */
 public class DeleteMetaData extends AuthenticatedCommand {
     private static Debug debug = SAML2MetaUtils.debug;
 
     static final String ARGUMENT_REALM = "realm";
 
     private boolean extendedOnly;
     private String realm = "/";
     private String entityID;
     private SAML2MetaManager metaManager;
 
     /**
      * Deletes a configuration and/or descriptor.
      *
      * @param rc Request Context.
      * @throws CLIException if unable to process this request.
      */
     public void handleRequest(RequestContext rc) 
         throws CLIException {
         super.handleRequest(rc);
         ldapLogin();
         extendedOnly = isOptionSet(FedCLIConstants.ARGUMENT_EXTENDED_ONLY);
         realm = getStringOptionValue(FedCLIConstants.ARGUMENT_REALM);
         entityID = getStringOptionValue(FedCLIConstants.ARGUMENT_ENTITY_ID);
         String spec = FederationManager.getIDFFSubCommandSpecification(rc);
         
         if (spec.equals(FederationManager.DEFAULT_SPECIFICATION)) {
             handleSAML2Request(rc);
         } else if (spec.equals(FedCLIConstants.IDFF_SPECIFICATION)) {
             handleIDFFRequest(rc);
         } else {
             throw new CLIException(
                 getResourceString("unsupported-specification"),
                 ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
     
     private void handleSAML2Request(RequestContext rc)
         throws CLIException {
         try {
             metaManager = new SAML2MetaManager();
            
             if (extendedOnly) {
                 metaManager.deleteEntityConfig(realm, entityID);
                 Object[] objs = {entityID};
                 
                 getOutputWriter().printlnMessage(MessageFormat.format(
                     getResourceString("delete-entity-config-deleted"),
                     objs));
             } else {
                 metaManager.deleteEntityDescriptor(realm, entityID);
                 Object[] objs = {entityID};
                 
                 getOutputWriter().printlnMessage(MessageFormat.format(
                     getResourceString("delete-entity-descriptor-deleted"),
                     objs));
             }
         } catch (SAML2MetaException e) {
             throw new CLIException(e.getMessage(),
                 ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
     
     private void handleIDFFRequest(RequestContext rc)
         throws CLIException {
         try {
             IDFFMetaManager metaManager = new IDFFMetaManager(
                 getAdminSSOToken());
             if (extendedOnly) {
                 metaManager.deleteEntityConfig(entityID);
                 Object[] objs = {entityID};
                 
                 getOutputWriter().printlnMessage(MessageFormat.format(
                     getResourceString("delete-entity-config-deleted"),
                     objs));
             } else {
                 metaManager.deleteEntityDescriptor(entityID);
                 Object[] objs = {entityID};
                 
                 getOutputWriter().printlnMessage(MessageFormat.format(
                     getResourceString("delete-entity-descriptor-deleted"),
                     objs));
             }
         } catch (IDFFMetaException e) {
             throw new CLIException(e.getMessage(),
                 ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
 }
