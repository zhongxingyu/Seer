 package org.polyforms.delegation.support;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.polyforms.delegation.DelegationService;
 import org.polyforms.delegation.builder.Delegation;
 import org.polyforms.util.AopUtils;
 import org.springframework.util.Assert;
 import org.springframework.util.ClassUtils;
 
 /**
  * Generic implementation of {@link DelegationService}.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 @Named
 public final class GenericDelegationService implements DelegationService {
     private final Map<Delegator, Delegator> delegatorMappingCache = new WeakHashMap<Delegator, Delegator>();
     private final DelegationExecutor delegationExecutor;
     private final DelegationResolver delegationResolver;
 
     /**
      * Create an instance with {@link DelegationExecutor} and {@link DelegationResolver}.
      */
     @Inject
     public GenericDelegationService(final DelegationExecutor delegationExecutor,
             final DelegationResolver delegationResolver) {
         this.delegationExecutor = delegationExecutor;
         this.delegationResolver = delegationResolver;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean supports(final Class<?> delegatorType, final Method delegatorMethod) {
         if (delegatorType == null || delegatorMethod == null) {
             return false;
         }
 
         return supportsWithCache(new Delegator(delegatorType, delegatorMethod));
     }
 
     private boolean supportsWithCache(final Delegator originalDelegator) {
        if (delegatorMappingCache.get(originalDelegator) != null) {
             return true;
         }
 
         return supports(originalDelegator);
     }
 
     private boolean supports(final Delegator originalDelegator) {
         for (final Class<?> clazz : AopUtils.deproxy(originalDelegator.getType())) {
             final Method method = ClassUtils.getMostSpecificMethod(originalDelegator.getMethod(), clazz);
             final Delegator delegator = new Delegator(clazz, method);
             if (delegationResolver.supports(delegator)) {
                 delegatorMappingCache.put(originalDelegator, delegator);
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object delegate(final Class<?> delegatorType, final Method delegatorMethod, final Object... arguments)
             throws Throwable {
         Assert.notNull(delegatorType);
         Assert.notNull(delegatorMethod);
 
         final Delegator candidate = new Delegator(delegatorType, delegatorMethod);
         Assert.isTrue(
                 supportsWithCache(candidate),
                 "The delegation of {} in {} is not supported. You can use 'supports' method to check whether a delegation is supported.");
         final Delegation delegation = delegationResolver.get(delegatorMappingCache.get(candidate));
         return delegationExecutor.execute(delegation, arguments);
     }
 }
