 package org.jboss.pressgang.ccms.proxy;
 
 import java.lang.reflect.Method;
 
 import org.jboss.pressgang.ccms.provider.RESTCSNodeProvider;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 
 public class RESTCSNodeV1ProxyHandler extends RESTBaseEntityV1ProxyHandler<RESTCSNodeV1> {
     public RESTCSNodeV1ProxyHandler(final RESTProviderFactory providerFactory, final RESTCSNodeV1 entity, boolean isRevisionEntity) {
         super(providerFactory, entity, isRevisionEntity);
     }
 
     public RESTCSNodeProvider getProvider() {
         return getProviderFactory().getProvider(RESTCSNodeProvider.class);
     }
 
     @Override
     public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
         final RESTCSNodeV1 contentSpec = getEntity();
         // Check that there is an id defined and the method called is a getter otherwise we can't proxy the object
         if (contentSpec.getId() != null && thisMethod.getName().startsWith("get")) {
             Object retValue = thisMethod.invoke(contentSpec, args);
             if (retValue == null) {
                 final String methodName = thisMethod.getName();
 
                 if (methodName.equals("getRelatedFromNodes")) {
                     retValue = getProvider().getRESTCSRelatedFromNodes(contentSpec.getId(), getEntityRevision());
                 } else if (methodName.equals("getRelatedToNodes")) {
                     retValue = getProvider().getRESTCSRelatedToNodes(contentSpec.getId(), getEntityRevision());
                } else if (methodName.equals("getChildren")) {
                     retValue = getProvider().getRESTCSNodeChildren(contentSpec.getId(), getEntityRevision());
                 } else if (methodName.equals("getRevisions")) {
                     retValue = getProvider().getRESTCSNodeRevisions(contentSpec.getId(), getEntityRevision());
                 } else if (methodName.equals("getNextNode")) {
                     retValue = getProvider().getRESTCSNextNode(contentSpec.getId(), getEntityRevision());
                 } else if (methodName.equals("getParent")) {
                     retValue = getProvider().getRESTCSNodeParent(contentSpec.getId(), getEntityRevision());
                 } else if (methodName.equals("getContentSpec")) {
                     retValue = getProvider().getRESTCSNodeContentSpec(contentSpec.getId(), getEntityRevision());
                 }
             }
 
             // Check if the returned object is a collection instance, if so proxy the collections items.
             return checkAndProxyReturnValue(retValue);
         }
 
         return super.invoke(self, thisMethod, proceed, args);
     }
 }
