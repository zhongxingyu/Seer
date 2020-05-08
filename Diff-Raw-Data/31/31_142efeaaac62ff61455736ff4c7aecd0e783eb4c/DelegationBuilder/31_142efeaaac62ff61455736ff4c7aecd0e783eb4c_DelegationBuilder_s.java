 package org.polyforms.delegation.builder;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.polyforms.delegation.builder.DelegationRegistry.Delegation;
 import org.polyforms.delegation.util.MethodUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Builder to bind delegator and delegatee methods as a {@link Delegation}.
  * 
  * {@link #delegate(Class)} method {@link #to(Class)} is a shortcut to register all abstract methods in delegator class
  * to delegatee class. For Example:
  * 
  * <code> 
  *     delegate(Delegator.class, "length").to(String.class);
  *     delegate(Delegator.class).to(Delegatee.class); 
  *     delegate(Delegator.class, "name").to(String.class, "toString"); 
  *     delegate(Delegator.class, "name").to(String.class, "length"); 
  * </code>
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 public final class DelegationBuilder {
     private final DelegationRegistry delegationRegistry;
 
     /**
      * Create an instance with {@link DelegationRegistry}.
      */
     public DelegationBuilder(final DelegationRegistry delegationRegistry) {
         this.delegationRegistry = delegationRegistry;
     }
 
     /**
      * Delegate all abstract methods in class.
      * 
      * @param delegator the delegator class
      * @return delegation builder of class
      */
    public final ClassDelegatorBuilder delegate(final Class<?> delegator) {
         return new ClassDelegatorBuilder(delegator);
     }
 
     /**
      * Delegate a method.
      * 
      * @param delegator the delegator method
      * @return delegation builder of method
      */
    public final MethodDelegatorBuilder delegate(final Method delegator) {
         return new MethodDelegatorBuilder(delegator);
     }
 
     /**
      * Delegate a method.
      * 
      * @param clazz method declaring class
      * @param methodName method name
      * @param parameterTypes parameter types of method
      * @return delegation builder of method
      */
    public final MethodDelegatorBuilder delegate(final Class<?> clazz, final String methodName,
             final Class<?>... parameterTypes) {
         return delegate(findMethod(clazz, methodName, parameterTypes));
     }
 
     private Method findMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
         try {
             return MethodUtils.findMostSpecificMethod(clazz, methodName, parameterTypes);
         } catch (final NoSuchMethodException e) {
             throw new DelegationRegistrationException(e);
         }
     }
 
     /**
      * Delegation builder of class which registers delegations for all abstract methods in specific class.
      * 
      */
     public final class ClassDelegatorBuilder extends AbstractDelegationBuilder<Class<?>> {
         private ClassDelegatorBuilder(final Class<?> delegator) {
             super(delegator);
             abstractClassOnly(delegator);
         }
 
         /**
          * Delegate method to method with same name in specific class.
          * 
          * @param delegatee the delegatee class
          */
         public ClassDelegatorBuilder to(final Class<?> delegatee) {
             register(delegatee);
             return this;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         protected void register(final Class<?> delegator, final Class<?> delegatee) {
             logRegistration(delegator, delegatee);
             for (final Method method : delegator.getMethods()) {
                 if (Modifier.isAbstract(method.getModifiers()) && !delegationRegistry.contains(method)) {
                     try {
                         final Method targetMethod = MethodUtils.findMostSpecificMethod(delegatee, method.getName());
                         logRegistration(delegator, delegatee);
                         registerDelegation(delegationRegistry, method, targetMethod);
                     } catch (final NoSuchMethodException e) {
                         // IGNORE if the method with same name can not be found in delegatee
                     }
                 }
             }
         }
     }
 
     /**
      * Delegation builder of method which registers delegation for specific method.
      * 
      */
     public final class MethodDelegatorBuilder extends AbstractDelegationBuilder<Method> {
         private MethodDelegatorBuilder(final Method delegator) {
             super(delegator);
             abstractClassOnly(delegator.getDeclaringClass());
         }
 
         /**
          * Delegate method to specific method.
          * 
          * @param delegatee the delegatee method
          */
         public MethodDelegatorBuilder to(final Method delegatee) {
             register(delegatee);
             return this;
         }
 
         /**
          * Delegate method to the method with same name in specific class.
          * 
          * If the method with specific parameter types can not find, the only method in specific class with same name is
          * used for delegatee.
          * 
          * @param clazz
          * @param parameterTypes
          */
         public MethodDelegatorBuilder to(final Class<?> clazz, final Class<?>... parameterTypes) {
             return to(clazz, getDelegator().getName(), parameterTypes);
         }
 
         /**
          * Delegate method to the method with same name in specific class.
          * 
          * If the method with specific parameter types can not find, the only method in specific class with method name
          * is used for delegatee.
          * 
          * @param clazz
          * @param methodName
          * @param parameterTypes
          */
         public MethodDelegatorBuilder to(final Class<?> clazz, final String methodName,
                 final Class<?>... parameterTypes) {
             return to(findMethod(clazz, methodName, parameterTypes));
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         protected void register(final Method delegator, final Method delegatee) {
             logRegistration(delegator, delegatee);
             registerDelegation(delegationRegistry, delegator, delegatee);
         }
     }
 
     protected abstract static class AbstractDelegationBuilder<T> {
         private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDelegationBuilder.class);
         private static final Set<Object> REGISTERED_DELEGATORS = new HashSet<Object>();
         private final List<Delegation> resigteredDelegations = new ArrayList<Delegation>();
         private final T delegator;
 
         protected AbstractDelegationBuilder(final T delegator) {
             this.delegator = delegator;
         }
 
         /**
          * Specify the name of delegatee bean in Ioc container.
          * 
          * It only can be invoked after Method {@link #to}.
          * 
          * @param name name of bean in Ioc container
          */
         public final void withName(final String name) {
             if (resigteredDelegations.isEmpty()) {
                 throw new DelegationRegistrationException("Method withName only can be invoked after method to.");
             }
             for (final Delegation delegation : resigteredDelegations) {
                 delegation.setName(name);
             }
         }
 
         protected final void abstractClassOnly(final Class<?> delegator) {
             if (!Modifier.isAbstract(delegator.getModifiers())) {
                 throw new DelegationRegistrationException("Only interface or abstract class can be a delegator.");
             }
         }
 
         protected final void logRegistration(final Object delegatee, final Object delegator) {
             LOGGER.info("Register delegations from {} to {}.", delegator, delegatee);
         }
 
         protected final void register(final T delegatee) {
             if (REGISTERED_DELEGATORS.contains(delegator)) {
                 LOGGER.warn("{} had been registed, and this registration is overriding previous one.", delegator);
             }
 
             try {
                 register(delegator, delegatee);
             } finally {
                 REGISTERED_DELEGATORS.add(delegator);
             }
         }
 
         protected abstract void register(final T delegator, final T delegatee);
 
         protected final void registerDelegation(final DelegationRegistry delegationRegistry, final Method method,
                 final Method targetMethod) {
             final Delegation delegation = new Delegation(method, targetMethod);
             delegationRegistry.register(delegation);
             resigteredDelegations.add(delegation);
         }
 
         protected final T getDelegator() {
             return delegator;
         }
     }
 }
