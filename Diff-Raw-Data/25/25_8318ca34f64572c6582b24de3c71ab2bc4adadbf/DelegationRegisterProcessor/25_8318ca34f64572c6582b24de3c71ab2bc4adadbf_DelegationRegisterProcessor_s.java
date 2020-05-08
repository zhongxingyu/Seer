 package org.polyforms.delegation.spring;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.polyforms.delegation.Delegate;
 import org.polyforms.delegation.builder.DelegationBuilder;
 import org.polyforms.delegation.builder.DelegationBuilderHolder;
 import org.polyforms.delegation.builder.DelegationRegister;
 import org.polyforms.delegation.builder.DelegationRegistry;
 import org.polyforms.delegation.builder.support.DefaultDelegationBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
 import org.springframework.beans.factory.support.BeanNameGenerator;
 import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.core.GenericTypeResolver;
 import org.springframework.stereotype.Component;
 
 /**
 * {@link BeanDefinitionRegistryPostProcessor} which executing {@link DelegationBuilderFactory} to bind delegator and
 * delegatee and register delegator as a bean if necessary.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 @Component
 public final class DelegationRegisterProcessor implements BeanDefinitionRegistryPostProcessor {
     private static final Logger LOGGER = LoggerFactory.getLogger(DelegationRegisterProcessor.class);
     private final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("rawtypes")
     public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) {
         if (!(registry instanceof ConfigurableListableBeanFactory)) {
             throw new IllegalStateException(
                     "DelegationRegisterProcessor must be registered in a ConfigurableListableBeanFactory.");
         }
         final ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) registry;
 
         final DelegationRegistry delegationRegistry = beanFactory.getBean(DelegationRegistry.class);
         final DelegationBuilder delegationBuilder = new DefaultDelegationBuilder(delegationRegistry);
 
         final Collection<DelegationRegister> delegationRegisters = beanFactory.getBeansOfType(DelegationRegister.class)
                 .values();
 
         final RegisteredClassCollector registeredClassCollector = new RegisteredClassCollector();
         visitBeanFactory(beanFactory, registeredClassCollector);
 
         DelegationBuilderHolder.set(delegationBuilder);
         for (final DelegationRegister register : delegationRegisters) {
             final Class<?> delegatorType = GenericTypeResolver.resolveTypeArgument(register.getClass(),
                     DelegationRegister.class);
             if (!registeredClassCollector.contains(delegatorType)) {
                 registerDelegatorIfNecessary(registry, delegatorType);
             }
             registerDelegations(delegationBuilder, register, delegatorType);
         }
         DelegationBuilderHolder.remove();
     }
 
     private void visitBeanFactory(final ConfigurableListableBeanFactory beanFactory,
             final BeanClassVisitor beanClassVisitor) {
         for (final String beanName : beanFactory.getBeanDefinitionNames()) {
             final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
             if (!beanDefinition.isAbstract()) {
                 final Class<?> clazz = beanFactory.getType(beanName);
                 if (clazz != null) {
                     beanClassVisitor.visit(clazz);
                 }
             }
         }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private void registerDelegations(final DelegationBuilder delegationBuilder, final DelegationRegister register,
             final Class<?> delegatorType) {
         LOGGER.info("Register delegations from register {}", register.getClass().getName());
         final Object source = delegationBuilder.delegateFrom(delegatorType);
         register.register(source);
         delegationBuilder.registerDelegations();
     }
 
     private void registerDelegatorIfNecessary(final BeanDefinitionRegistry registry, final Class<?> delegatorType) {
         LOGGER.info("Register bean for delegator {}", delegatorType.getName());
         final RootBeanDefinition beanDefinition = new RootBeanDefinition(delegatorType);
         registry.registerBeanDefinition(beanNameGenerator.generateBeanName(beanDefinition, registry), beanDefinition);
     }
 
     /**
      * {@inheritDoc}
      */
     public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
         final DelegationRegistry delegationRegistry = beanFactory.getBean(DelegationRegistry.class);
         final DelegationBuilder delegationBuilder = new DefaultDelegationBuilder(delegationRegistry);
         visitBeanFactory(beanFactory, new AnnotatedDelegationRegister(delegationBuilder));
     }
 
     protected static final class RegisteredClassCollector implements BeanClassVisitor {
         private final Set<Class<?>> registeredClasses = new HashSet<Class<?>>();
 
         public void visit(final Class<?> clazz) {
             registeredClasses.add(clazz);
         }
 
         public boolean contains(final Class<?> clazz) {
             return registeredClasses.contains(clazz);
         }
     }
 
     protected static class AnnotatedDelegationRegister implements BeanClassVisitor {
         private final DelegationBuilder delegationBuilder;
 
         protected AnnotatedDelegationRegister(final DelegationBuilder delegationBuilder) {
             this.delegationBuilder = delegationBuilder;
         }
 
         /**
          * {@inheritDoc}
          */
         public void visit(final Class<?> clazz) {
             final boolean annotationPresent = clazz.isAnnotationPresent(Delegate.class);
             final Set<Method> annotatedMethods = getAnnotatedMethods(clazz.getMethods());
             if (!annotationPresent && annotatedMethods.isEmpty()) {
                 return;
             }
 
             final Object source = delegationBuilder.delegateFrom(clazz);
 
             if (annotationPresent) {
                 delegationBuilder.delegate();
             }
 
             for (final Method method : annotatedMethods) {
                 registerDelegate(source, method);
             }
 
             delegationBuilder.registerDelegations();
         }
 
         private Set<Method> getAnnotatedMethods(final Method[] methods) {
             final Set<Method> annotatedMethods = new HashSet<Method>();
             for (final Method method : methods) {
                 if (method.isAnnotationPresent(Delegate.class)) {
                     annotatedMethods.add(method);
                 }
             }
             return annotatedMethods;
         }
 
         private void registerDelegate(final Object source, final Method method) {
             final Object[] arguments = new Object[method.getParameterTypes().length];
             try {
                 method.invoke(source, arguments);
             } catch (final IllegalAccessException e) {
                 throw new IllegalStateException("Should never get here");
             } catch (final InvocationTargetException e) {
                 throw new IllegalStateException("Should never get here");
             }
             delegationBuilder.delegate();
         }
     }
 
     protected interface BeanClassVisitor {
         void visit(final Class<?> clazz);
     }
 }
