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
 
 package org.picketlink.as.subsystem.model.sp;
 
 
 import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
 
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.as.controller.OperationContext;
 import org.jboss.as.controller.OperationFailedException;
 import org.jboss.as.controller.PathAddress;
 import org.jboss.as.controller.PathElement;
 import org.jboss.as.controller.ServiceVerificationHandler;
 import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
 import org.jboss.as.controller.registry.Resource.ResourceEntry;
 import org.jboss.dmr.ModelNode;
 import org.jboss.msc.service.ServiceController;
 import org.jboss.msc.service.ServiceController.Mode;
 import org.jboss.msc.service.ServiceName;
 import org.picketlink.as.subsystem.PicketLinkExtension;
 import org.picketlink.as.subsystem.model.ModelElement;
 import org.picketlink.as.subsystem.service.SPConfigurationService;
 
 /**
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  */
 public class ServiceProviderAddHandler extends AbstractResourceAddStepHandler {
 
     public static final ServiceProviderAddHandler INSTANCE = new ServiceProviderAddHandler();
 
     private ServiceProviderAddHandler() {
         super(ModelElement.SERVICE_PROVIDER);
     }
 
     @Override
     protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
             ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
             throws OperationFailedException {
         String alias = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
         String url = operation.get(ModelElement.COMMON_URL.getName()).asString();
         boolean postBinding = operation.get(ModelElement.SERVICE_PROVIDER_POST_BINDING.getName()).asBoolean();
         String fedAlias = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getElement(1).getValue();
         String idpUrl = null;
           
         PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(ModelElement.FEDERATION.getName(), fedAlias));
         
         Set<ResourceEntry> federationChilds = context.getRootResource().getChild(
                 PathElement.pathElement(
                         SUBSYSTEM, PicketLinkExtension.SUBSYSTEM_NAME))
                         .navigate(addr).getChildren(ModelElement.IDENTITY_PROVIDER.getName());
         
         for (ResourceEntry resourceEntry : federationChilds) {
             if (resourceEntry.getPathElement().getKey().equals(ModelElement.IDENTITY_PROVIDER.getName())) {
                 idpUrl = resourceEntry.getModel().get(ModelElement.COMMON_URL.getName()).asString();
                 break;
             }
         } 
         
         SPConfigurationService service = new SPConfigurationService(alias, url);
         ServiceName name = SPConfigurationService.createServiceName(alias);
         ServiceController<SPConfigurationService> controller = context.getServiceTarget().addService(name, service)
                 .addListener(verificationHandler).setInitialMode(Mode.ACTIVE).install();
 
        service.getSPConfiguration().setIdentityURL(idpUrl);
         service.getSPConfiguration().setPostBinding(postBinding);
         
         newControllers.add(controller);
     }
     
 }
