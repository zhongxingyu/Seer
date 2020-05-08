 package org.polyforms.delegation.spring;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.polyforms.delegation.DelegateTo;
 import org.polyforms.delegation.DelegatedBy;
 import org.polyforms.delegation.DelegationRegister;
 import org.polyforms.delegation.builder.DelegationBuilder;
 import org.polyforms.delegation.builder.DelegationRegistry;
 import org.polyforms.delegation.builder.DelegationRegistry.Delegation;
 import org.polyforms.di.spring.util.BeanFactoryVisitor;
 import org.polyforms.di.spring.util.BeanFactoryVisitor.BeanClassVisitor;
 import org.polyforms.di.spring.util.support.GenericBeanFactoryVisitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.StringUtils;
 
 /**
  * {@link BeanDefinitionRegistryPostProcessor} which executing {@link DelegationBuilder} to bind delegator and delegatee
  * and register delegator as a bean if necessary.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 public final class DelegationRegisterProcessor implements BeanDefinitionRegistryPostProcessor {
     private static final Logger LOGGER = LoggerFactory.getLogger(DelegationRegisterProcessor.class);
     private final BeanFactoryVisitor beanFactoryVisitor = new GenericBeanFactoryVisitor();
     private DelegationRegistry delegationRegistry;
     private DelegationBuilder delegationBuilder;
     private Set<DelegationRegister> delegationRegisters;
 
     /**
      * {@inheritDoc}
      */
     public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) {
        registerDelegations(delegationRegistry);
        registerBeansIfNecessary(registry, delegationRegistry);
     }
 
    private void registerDelegations(final DelegationRegistry delegationRegistry) {
         for (final DelegationRegister register : delegationRegisters) {
             LOGGER.info("Register delegations from register {}", register.getClass().getName());
             register.registerDelegations(getDelegationBuilder());
         }
     }
 
    private void registerBeansIfNecessary(final BeanDefinitionRegistry registry,
            final DelegationRegistry delegationRegistry) {
         final Set<Class<?>> classesForRegistration = new HashSet<Class<?>>();
         for (final Delegation delegationPair : delegationRegistry.getAll()) {
             classesForRegistration.add(delegationPair.getDelegator().getDeclaringClass());
         }
         for (final Class<?> clazz : classesForRegistration) {
             LOGGER.info("Register bean for delegator {}", clazz.getName());
             registry.registerBeanDefinition(buildDefaultBeanName(clazz), new RootBeanDefinition(clazz));
         }
     }
 
     private String buildDefaultBeanName(final Class<?> clazz) {
         final String shortClassName = ClassUtils.getShortName(clazz);
         return StringUtils.uncapitalize(shortClassName);
     }
 
     private DelegationBuilder getDelegationBuilder() {
         if (delegationBuilder == null) {
             delegationBuilder = new DelegationBuilder(delegationRegistry);
         }
 
         return delegationBuilder;
     }
 
     /**
      * {@inheritDoc}
      */
     public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
         beanFactoryVisitor.visit(beanFactory, new BeanClassVisitor() {
             /**
              * {@inheritDoc}
              */
             public void visit(final String beanName, final AbstractBeanDefinition beanDefinition, final Class<?> clazz) {
                 for (final Method method : clazz.getMethods()) {
                     registerDelegatTo(method, delegationBuilder);
                     registerDelegatedBy(beanName, method, delegationBuilder);
                 }
             }
         });
     }
 
     private void registerDelegatTo(final Method method, final DelegationBuilder delegationBuilder) {
         final DelegateTo delegateTo = method.getAnnotation(DelegateTo.class);
         if (delegateTo != null) {
             final String methodName = normalizeMethodName(delegateTo.methodName(), method);
             delegationBuilder.delegate(method).to(delegateTo.value(), methodName, delegateTo.parameterTypes())
                     .withName(delegateTo.name());
         }
     }
 
     private void registerDelegatedBy(final String beanName, final Method method,
             final DelegationBuilder delegationBuilder) {
         final DelegatedBy delegatedBy = method.getAnnotation(DelegatedBy.class);
         if (delegatedBy != null) {
             final String methodName = normalizeMethodName(delegatedBy.methodName(), method);
             delegationBuilder.delegate(delegatedBy.value(), methodName, delegatedBy.parameterTypes()).to(method)
                     .withName(beanName);
         }
     }
 
     private String normalizeMethodName(final String methodName, final Method method) {
         return StringUtils.hasText(methodName) ? methodName : method.getName();
     }
 
     public void setDelegationRegistry(final DelegationRegistry delegationRegistry) {
         this.delegationRegistry = delegationRegistry;
     }
 
     public void setDelegationRegisters(final Set<DelegationRegister> delegationRegisters) {
         this.delegationRegisters = delegationRegisters;
     }
 }
