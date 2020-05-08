 package org.jboss.as.undertow.extension;
 
 import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jboss.as.controller.AbstractAddStepHandler;
 import org.jboss.as.controller.OperationContext;
 import org.jboss.as.controller.OperationFailedException;
 import org.jboss.as.controller.PathAddress;
 import org.jboss.as.controller.ServiceVerificationHandler;
 import org.jboss.as.web.host.WebHost;
 import org.jboss.dmr.ModelNode;
 import org.jboss.msc.service.ServiceBuilder;
 import org.jboss.msc.service.ServiceController;
 import org.jboss.msc.service.ServiceName;
 
 /**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
 class HostAdd extends AbstractAddStepHandler {
     @Override
     protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
         HostHandlerDefinition.ALIAS.validateAndSet(operation, model);
     }
 
     @Override
     protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
         final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
         final PathAddress parent = address.subAddress(0, address.size() - 1);
         final String name = address.getLastElement().getValue();
         List<String> aliases = HostHandlerDefinition.ALIAS.unwrap(context, model);
         final String serverName = parent.getLastElement().getValue();
         final ServiceName virtualHostServiceName = UndertowServices.virtualHostName(serverName, name);
         HostService service = new HostService(name, aliases == null ? new LinkedList<String>() : aliases);
         final ServiceBuilder<HostService> builder = context.getServiceTarget().addService(virtualHostServiceName, service)
                 .addDependency(UndertowServices.SERVER.append(serverName), ServerService.class, service.getServer())
                 .addAliases(WebHost.SERVICE_NAME.append(name));
 
        for(String alias : aliases) {
            builder.addAliases(WebHost.SERVICE_NAME.append(alias));
        }
 
         builder.setInitialMode(ServiceController.Mode.ACTIVE);
 
         final ServiceController<HostService> serviceController = builder.install();
         if (newControllers != null) {
             newControllers.add(serviceController);
         }
     }
 }
