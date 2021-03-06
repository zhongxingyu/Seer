 /**
  * Copyright (c) 2009 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.fedoraproject.candlepin.auth.interceptor;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.log4j.Logger;
 import org.fedoraproject.candlepin.auth.Principal;
 import org.fedoraproject.candlepin.auth.Access;
 import org.fedoraproject.candlepin.exceptions.ForbiddenException;
 import org.xnap.commons.i18n.I18n;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Provider;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 
 import org.fedoraproject.candlepin.model.Consumer;
 import org.fedoraproject.candlepin.model.ConsumerCurator;
 import org.fedoraproject.candlepin.model.Entitlement;
 import org.fedoraproject.candlepin.model.EntitlementCurator;
 import org.fedoraproject.candlepin.model.Owner;
 import org.fedoraproject.candlepin.model.OwnerCurator;
 import org.fedoraproject.candlepin.model.Pool;
 import org.fedoraproject.candlepin.model.PoolCurator;
 
 /**
  * Interceptor for enforcing role based access to REST API methods.
  * 
  * This interceptor deals with coarse grained access, it only answers the
  * question of can a principal with these roles access this method. It does not
  * support any paramaters such as verifying the call is being made on a visible
  * consumer/owner, this is handled instead by the filtering mechanism.
  */
 public class SecurityInterceptor implements MethodInterceptor {
 
     @Inject private Provider<Principal> principalProvider;
     @Inject private Provider<I18n> i18nProvider;
     @Inject private Injector injector;
 
     // TODO:  This would not really be needed if we were consistent about what
     //        we use as IDs in our urls!
     @SuppressWarnings("rawtypes")
     private final Map<Class, EntityStore> storeMap;
 
     private static Logger log = Logger.getLogger(SecurityInterceptor.class);
 
 
     @SuppressWarnings("rawtypes")
     public SecurityInterceptor() {
         this.storeMap = new HashMap<Class, EntityStore>();
 
         storeMap.put(Owner.class, new OwnerStore());
         storeMap.put(Consumer.class, new ConsumerStore());
         storeMap.put(Entitlement.class, new EntitlementStore());
         storeMap.put(Pool.class, new PoolStore());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object invoke(MethodInvocation invocation) throws Throwable {
         Principal principal = this.principalProvider.get();
        log.debug("Invoked.");
 
         Access access = findRequiredAccessType(invocation);
 
         Collection<Object> params = findVerifiedParameters(invocation);
 
         // if there are no params, then deny access if the principal doesn't
         // have full system access
         if (params.isEmpty() && !principal.hasFullAccess()) {
             denyAccess(principal, invocation);
         }
 
         for (Object param : params) {
             // if this principal cannot access any of the annotated parameters,
             // then deny access here
            if (!principal.canAccess(param, access)) {
                 denyAccess(principal, invocation);
             }
         }
 
         return invocation.proceed();
     }
 
     private void denyAccess(Principal principal, MethodInvocation invocation) {
         I18n i18n = this.i18nProvider.get();
         log.warn("Refusing principal: " + principal + " access to: " +
                     invocation.getMethod().getName());
 
         String error = "Insufficient permissions";
         throw new ForbiddenException(i18n.tr(error));
     }
 
     /**
      * Scans the method annotations for RESTEasy annotations, to determine
      * the HTTP verbs used, and converts that to a minimum required access type.
      * 
      * @param invocation method invocation object
      * @return the required minimum access type
      */
     private Access findRequiredAccessType(MethodInvocation invocation) {
         
         // Assume the minimum level to start with, and bump up as we see
         // stricter annotations
         Access minimumLevel = Access.READ_ONLY;
         
         // If we had write or delete access types, that would go here,
         // and we'd only break on the access.all type.
         for (Annotation annotation : invocation.getMethod().getAnnotations()) {
             if (annotation instanceof PUT ||
                 annotation instanceof POST ||
                 annotation instanceof DELETE) {
                 minimumLevel = Access.ALL;
                 break;
             }
             // Other annotations are GET, HEAD, and OPTIONS. assume read only for those.
         }
         return minimumLevel;
     }
     
     /**
      * Scans the parameters for the method being invoked.
      *
      * @return
      */
     private Collection<Object> findVerifiedParameters(MethodInvocation invocation) {
         List<Object> parameters = new LinkedList<Object>();
         Method m = invocation.getMethod();
 
         for (int i = 0; i < m.getParameterAnnotations().length; i++) {
             for (Annotation a : m.getParameterAnnotations()[i]) {
                 if (a instanceof Verify) {
                     @SuppressWarnings("rawtypes")
                     Class verifyType = ((Verify) a).value();
                     String verifyParam = (String) invocation.getArguments()[i];
 
                     // Use the correct curator (in storeMap) to look up the actual
                     // entity with the annotated argument
                    parameters.add(storeMap.get(verifyType).lookup(verifyParam));
                 }
             }
         }
 
         return parameters;
     }
 
     private interface EntityStore {
         Object lookup(String key);
     }
 
     private class OwnerStore implements EntityStore {
         private OwnerCurator ownerCurator;
 
         @Override
         public Object lookup(String key) {
             if (ownerCurator == null) {
                 ownerCurator = injector.getInstance(OwnerCurator.class);
             }
 
             return ownerCurator.lookupByKey(key);
         }
     }
 
     private class ConsumerStore implements EntityStore {
         private ConsumerCurator consumerCurator;
 
         @Override
         public Object lookup(String key) {
             if (consumerCurator == null) {
                 consumerCurator = injector.getInstance(ConsumerCurator.class);
             }
 
             return consumerCurator.findByUuid(key);
         }
     }
 
     private class EntitlementStore implements EntityStore {
         private EntitlementCurator entitlementCurator;
 
         @Override
         public Object lookup(String key) {
             if (entitlementCurator == null) {
                 entitlementCurator = injector.getInstance(EntitlementCurator.class);
             }
             return entitlementCurator.find(key);
         }
     }
 
     private class PoolStore implements EntityStore {
         private PoolCurator poolCurator;
 
         @Override
         public Object lookup(String key) {
             if (poolCurator == null) {
                 poolCurator = injector.getInstance(PoolCurator.class);
             }
 
             return poolCurator.find(key);
         }
     }
     
 
 }
