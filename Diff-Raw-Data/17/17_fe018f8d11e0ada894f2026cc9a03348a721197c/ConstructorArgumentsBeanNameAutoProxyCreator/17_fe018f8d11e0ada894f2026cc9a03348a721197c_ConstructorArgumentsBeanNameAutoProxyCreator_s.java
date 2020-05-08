 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.spring;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.aop.TargetSource;
 import org.springframework.aop.framework.AdvisedSupport;
 import org.springframework.aop.framework.AopProxy;
 import org.springframework.aop.framework.AopProxyFactory;
 import org.springframework.aop.framework.DefaultAopProxyFactory;
 import org.springframework.aop.framework.ProxyFactory;
 import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
 import org.springframework.aop.support.AopUtils;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.BeanDefinitionHolder;
 import org.springframework.beans.factory.config.BeanReference;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.config.ConstructorArgumentValues;
 import org.springframework.beans.factory.config.TypedStringValue;
 import org.springframework.beans.factory.config
   .ConstructorArgumentValues.ValueHolder;
 import org.springframework.beans.factory.support.ChildBeanDefinition;
 
 /**
  * <p>
  * Extension to the {@link BeanNameAutoProxyCreator} that configures the
  * {@link ProxyFactory} to allow cglib proxies for beans that don't have a
  * default constructor.
  * </p>
  * <p>
  * What it actually does is setting a custom aop factory to the
  * {@link ProxyFactory} that will modify the created proxy to use constructor
  * arguments when necessary.
  * </p>
  *
  * @author pablo.saavedra
  */
 public class ConstructorArgumentsBeanNameAutoProxyCreator extends
     BeanNameAutoProxyCreator {
 
   /**
    * Serial version.
    */
   private static final long serialVersionUID = 6056671988239456196L;
 
   /**
    * The logger.
    */
   private static Logger log = LoggerFactory
       .getLogger(ConstructorArgumentsBeanNameAutoProxyCreator.class);
 
   /**
    * Thread local variable for storing the name of the bean that's currently
    * under creation.
    */
   private transient ThreadLocal<String> currentBeanName
     = new ThreadLocal<String>();
 
   /**
    * Create an AOP proxy for the given bean.
    *
    * @param beanClass
    *          the class of the bean
    *
    * @param beanName
    *          the name of the bean
    *
    * @param specificInterceptors
    *          the set of interceptors that is specific to this bean (may be
    *          empty, but not null)
    *
    * @param targetSource
    *          the TargetSource for the proxy, already pre-configured to access
    *          the bean
    *
    * @return the AOP proxy for the bean
    *
    * @see #buildAdvisors
    */
   @SuppressWarnings("unchecked")
   protected Object createProxy(final Class beanClass, final String beanName,
       final Object[] specificInterceptors, final TargetSource targetSource) {
     this.currentBeanName.set(beanName);
     return super.createProxy(beanClass, beanName, specificInterceptors,
         targetSource);
   }
 
   /**
    * Customizes the {@link ProxyFactory} to use a custom AOP proxy factory.
    *
    * @param proxyFactory
    *          The {@link ProxyFactory} to customize
    */
   protected void customizeProxyFactory(final ProxyFactory proxyFactory) {
     log.trace("Entering customizeProxyFactory('...')");
     super.customizeProxyFactory(proxyFactory);
     String beanName = currentBeanName.get();
     if (log.isDebugEnabled()) {
       log.debug("Bean proxy to customize is " + beanName);
     }
     proxyFactory.setAopProxyFactory(new ConstructorArgsAopProxyFactory(
         (ConfigurableListableBeanFactory) getBeanFactory(), beanName));
 
     log.trace("Leaving customizeProxyFactory");
   }
 
   /**
    * <p>
    * Custom AOP Proxy Factory that sets the constructor arguments to use for
    * Cglib proxies that are proxying beans with no default constructors.
    * </p>
    * <p>
    * It uses the {@link ConfigurableListableBeanFactory} to access the
    * {@link BeanDefinition} and its constructor arguments, if any.
    * </p>
    * <p>
    * It will not work with proxies that don't contain a method with the
    * signature setConstructorArguments(Object[],Class[]).
    * </p>
    *
    * @author pablo.saavedra
    *
    */
   static class ConstructorArgsAopProxyFactory implements AopProxyFactory {
 
     /**
      * The name of the method to set the constructor arguments.
      */
     private static final String SET_CTOR_METHOD = "setConstructorArguments";
 
     /**
      * Bean factory to obtains the bean definitions.
      */
     private ConfigurableListableBeanFactory beanFactory;
 
     /**
      * The name of the bean definition to get the constructor arguments from.
      */
     private String beanName;
 
     /**
      * Actual factory to delegate proxy creation to.
      */
     private AopProxyFactory delegate = new DefaultAopProxyFactory();
 
     /**
      * Creates a CustomAopProxyFactory with the given bean factory.
      *
      * @param theBeanFactory
      *          The bean factory to use. Cannot be <code>null</code>
      * @param theBeanName
      *          The name of the bean that's going to be proxied. Cannot be
      *          <code>null</code>
      * @param delegateFactory
      *          The aop factory to delegate to. Cannot be <code>null</code>
      */
     ConstructorArgsAopProxyFactory(
         final ConfigurableListableBeanFactory theBeanFactory,
         final String theBeanName, final AopProxyFactory delegateFactory) {
       this(theBeanFactory, theBeanName);
       Validate.notNull(delegateFactory);
       this.delegate = delegateFactory;
     }
 
     /**
      * Creates a CustomAopProxyFactory with the given bean factory.
      *
      * @param theBeanFactory
      *          The bean factory to use. Cannot be <code>null</code>
      * @param theBeanName
      *          The name of the bean that's going to be proxied. Cannot be
      *          <code>null</code>
      */
     ConstructorArgsAopProxyFactory(
         final ConfigurableListableBeanFactory theBeanFactory,
         final String theBeanName) {
       Validate.notNull(theBeanFactory);
       Validate.notEmpty(theBeanName);
       beanFactory = theBeanFactory;
       beanName = theBeanName;
     }
 
     /**
      * <p>
      * Creates an AOP proxy using the {@link DefaultAopProxyFactory}
      * implementation, and the sets the constructor arguments if the AOP proxy
      * is not a JDKDynamicProxy (i-e- is a Cglib proxy), and the proxied bean
      * has no default constructor.
      * </p>
      * <p>
      * In case of any failure during the process, it'll just return the proxy
      * returned by its delegate.
      * </p>
      *
      * @param config
      *          The proxy configuration.
      * @return An AOP proxy, possibly modified to use constructor arguments
      * @see DefaultAopProxyFactory#createAopProxy(AdvisedSupport)
      */
     @SuppressWarnings("unchecked")
     public AopProxy createAopProxy(final AdvisedSupport config) {
       // Create proxy
       AopProxy proxy = delegate.createAopProxy(config);
       if (log.isDebugEnabled()) {
         log.debug("Proxy created, its an instance of "
             + proxy.getClass().getName());
       }
       // If it's a JDK proxy, just return it
       if (AopUtils.isJdkDynamicProxy(proxy)) {
         return proxy;
       }
 
       try {
         log.debug("Proxy is a Cglib proxy, checking for default constructor");
         // If the class has a default constructor, returning the
         // unmodified proxy
         config.getTargetClass().getConstructor(new Class[0]);
         log.debug("Default constructor found, returning proxy as is");
         return proxy;
       } catch (NoSuchMethodException nsme) {
         // Class has no default constructor, let's set the parameters
         log.debug("No default constructor, starting argument filling");
       }
 
       try {
         log.debug("Starting set constructor arguments");
         Method setConstructorArgumentsMethod = proxy.getClass()
             .getDeclaredMethod(SET_CTOR_METHOD,
                 new Class[] {Object[].class, Class[].class });
         Constructor<?>[] targetConstructors = config.getTargetClass()
             .getConstructors();
         if (log.isDebugEnabled()) {
           log.debug("Found " + targetConstructors.length + " constructors");
         }
         for (Constructor<?> constructor : targetConstructors) {
           Class<?>[] argumentTypes = constructor.getParameterTypes();
           log.debug("Obtaining constructor arguments "
               + "based on parameter types");
           Object[] arguments = getConstructorArguments(argumentTypes, config,
               beanName);
 
           if (arguments == null) {
             log.debug("Arguments could not be obtained, "
                 + "will try with next constructor if any");
             continue;
           }
           log.debug("Arguments successfully obtained, "
               + "attemping to set them in the proxy");
           try {
             setConstructorArgumentsMethod.setAccessible(true);
             setConstructorArgumentsMethod.invoke(proxy, new Object[] {
                 arguments, argumentTypes });
             log.debug("Constructor arguments setted successfully, "
                 + "returning modified proxy");
             return proxy;
           } catch (Exception e) {
             log.warn("Error while invoking setConstructor method, "
                 + "trying with next constructor", e);
           }
 
         }
         log.debug("All constructors checked, "
             + "no matching arguments were found");
       } catch (SecurityException e) {
         log.warn("Security exception while obtaining " + SET_CTOR_METHOD
             + " method", e);
       } catch (NoSuchMethodException e) {
         log.warn(SET_CTOR_METHOD + " method not found", e);
       }
 
       log.warn("The bean has no default constructor, "
           + "but no matching arguments could be set, "
           + "proxy creation might fail");
       return proxy;
     }
 
     /**
      * <p>
      * Obtains the constructor arguments of the given parameter types for the
      * bean configuration defined in the given {@link AdvisedSupport}.
      * </p>
      * <p>
      * It will return <code>null</code> if the parameters cannot be obtained
      * for any reason.
      * </p>
      *
      * @param parameterTypes
      *          The types of the parameters to fetch.
      * @param config
      *          The aop proxy config
      * @param theBeanName
      *          The name of the bean definition to obtain the constructor
      *          arguments from
      * @return An object array with the actual parameter values, sorted by their
      *         type according to the parameterTypes or <code>null</code>.
      */
     protected Object[] getConstructorArguments(
         final Class<?>[] parameterTypes, final AdvisedSupport config,
         final String theBeanName) {
       if (log.isTraceEnabled()) {
         log.trace("Obtaining bean definitions for target class "
             + config.getTargetClass().getName());
       }
 
       BeanDefinition bd = beanFactory.getBeanDefinition(theBeanName);
 
       log.debug("Obtaining constructor arguments");
       ConstructorArgumentValues constructorArgs = getConstructorArguments(bd);
       if (constructorArgs.getArgumentCount() != parameterTypes.length) {
         if (log.isDebugEnabled()) {
           log.debug("Constructor arguments size doesn't "
               + "match parameter types size ("
               + constructorArgs.getArgumentCount() + " vs "
               + parameterTypes.length + "), returning to avoid an exception");
         }
         return null;
       }
       List<Object> argValues = new ArrayList<Object>(parameterTypes.length);
       log.debug("Assembling constructor arguments");
       for (int i = 0; i < parameterTypes.length; i++) {
         ValueHolder argValue = constructorArgs.getIndexedArgumentValue(i,
             parameterTypes[i]);
         if (log.isDebugEnabled()) {
          log.debug("Obtained value holder for argumente: " + argValue);
         }
         Object actualValue = getActualValue(argValue);
         if (!parameterTypes[i].isInstance(actualValue)) {
           log.warn("The actual value of the parameter does not match "
               + "the expected argument type. Got " + actualValue.getClass()
               + " but expected " + parameterTypes[i]);
           return null;
         }
         argValues.add(actualValue);
       }
       if (log.isTraceEnabled()) {
         log.trace("All the constructor arguments were obtained: "
             + argValues.toString());
       }
       return argValues.toArray();
     }
 
    /**
     * Extracts the actual value from the given {@link ValueHolder}, resolving
      * bean references through the bean factory.
      *
     * @param holder
     *          The holder to resolve
      * @return The actual value of the holder
      */
     protected Object getActualValue(final ValueHolder holder) {
       log.trace("Start getActualValue");
       Object value = holder.getValue();
       if (value instanceof BeanDefinitionHolder) {
         // Constructor argument is an inner bean
         throw new IllegalArgumentException(
             "Inner bean definitions are not supported");
       }
 
       if (value instanceof BeanReference) {
         BeanReference ref = (BeanReference) value;
         if (log.isDebugEnabled()) {
           log.debug("Value is a reference to " + ref.getBeanName()
               + ", resolving reference");
         }
         value = beanFactory.getBean(ref.getBeanName());
       } else if (value instanceof TypedStringValue) {
         TypedStringValue typedStringValue = (TypedStringValue) value;
         value = typedStringValue.getValue();
       }
       if (log.isTraceEnabled()) {
         log.trace("Finishing getActualValue, actual value is " + value);
       }
       return value;
     }
 
     /**
      * Returns the {@link ConstructorArgumentValues} for the given
      * {@link BeanDefinition}. It'll check parent bean definitions if it has
      * any and no arguments are found.
      *
      * @param bd
      *          The {@link BeanDefinition} to obtain the constructor arguments
      *          from
      * @return The constructor argument list for the bean definition, of size 0
      *         if none are found. Never <code>null</code>
      */
     protected ConstructorArgumentValues getConstructorArguments(
         final BeanDefinition bd) {
       BeanDefinition copy = bd;
       ConstructorArgumentValues args = copy.getConstructorArgumentValues();
       while ((args.getArgumentCount() == 0)
           && (copy instanceof ChildBeanDefinition)) {
         ChildBeanDefinition cbd = (ChildBeanDefinition) copy;
         copy = beanFactory.getBeanDefinition(cbd.getParentName());
         args = copy.getConstructorArgumentValues();
       }
       return args;
     }
   }
 
 }
