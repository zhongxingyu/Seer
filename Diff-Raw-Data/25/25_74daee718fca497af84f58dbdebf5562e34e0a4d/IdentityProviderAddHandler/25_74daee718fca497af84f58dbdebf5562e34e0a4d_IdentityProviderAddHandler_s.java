 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.picketlink.as.subsystem.model.idp;
 
 import static org.picketlink.as.subsystem.model.ModelElement.COMMON_URL;
 import static org.picketlink.as.subsystem.model.ModelElement.IDENTITY_PROVIDER_IGNORE_INCOMING_SIGNATURES;
 import static org.picketlink.as.subsystem.model.ModelElement.IDENTITY_PROVIDER_SIGN_OUTGOING_MESSAGES;
 
 import java.util.List;
 
 import org.jboss.as.controller.OperationContext;
 import org.jboss.as.controller.OperationFailedException;
 import org.jboss.as.controller.PathAddress;
 import org.jboss.as.controller.ServiceVerificationHandler;
 import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
 import org.jboss.dmr.ModelNode;
 import org.jboss.msc.service.ServiceController;
 import org.jboss.msc.service.ServiceController.Mode;
 import org.jboss.msc.service.ServiceName;
 import org.picketlink.as.subsystem.model.ModelElement;
 import org.picketlink.as.subsystem.model.sp.AbstractResourceAddStepHandler;
 import org.picketlink.as.subsystem.service.IDPConfigurationService;
 
 /**
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  */
 public class IdentityProviderAddHandler extends AbstractResourceAddStepHandler {
 
     public static final IdentityProviderAddHandler INSTANCE = new IdentityProviderAddHandler();
 
     private IdentityProviderAddHandler() {
         super(ModelElement.IDENTITY_PROVIDER);
     }
 
     /* (non-Javadoc)
      * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
      */
     @Override
     protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
             ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
             throws OperationFailedException {
        String alias = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
         String url = operation.get(COMMON_URL.getName()).asString();
         boolean signOutgoingMessages = operation.get(IDENTITY_PROVIDER_SIGN_OUTGOING_MESSAGES.getName()).asBoolean();
         boolean ignoreIncomingSignatures = operation.get(IDENTITY_PROVIDER_IGNORE_INCOMING_SIGNATURES.getName()).asBoolean();

         IDPConfigurationService service = new IDPConfigurationService(alias, url);
         ServiceName name = IDPConfigurationService.createServiceName(alias);
         ServiceController<IDPConfigurationService> controller = context.getServiceTarget().addService(name, service)
                 .addListener(verificationHandler).setInitialMode(Mode.ACTIVE).install();
         
         controller.getValue().getIdpConfiguration().setSignOutgoingMessages(signOutgoingMessages);
         controller.getValue().getIdpConfiguration().setIgnoreIncomingSignatures(ignoreIncomingSignatures);
         
         newControllers.add(controller);
     }
 
 }
