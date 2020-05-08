 /*
  * Copyright 2008-2010 Xebia and the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.xebia.management.statistics;
 
 import java.lang.reflect.Method;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.reflect.MethodSignature;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.expression.Expression;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.ParserContext;
 import org.springframework.expression.common.LiteralExpression;
 import org.springframework.expression.common.TemplateParserContext;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.jmx.export.MBeanExporter;
 import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedResource;
 import org.springframework.jmx.export.naming.SelfNaming;
 import org.springframework.jmx.support.JmxUtils;
 import org.springframework.util.StringUtils;
 
 /**
  * <p>
  * Aspect to handle the methods annotated with the {@link Profiled} annotation.
  * </p>
  * 
  * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
  */
 @ManagedResource
 @Aspect
 public class ProfileAspect implements InitializingBean, DisposableBean, BeanNameAware, SelfNaming {
 
     public enum ClassNameStyle {
         COMPACT_FULLY_QUALIFIED_NAME, FULLY_QUALIFIED_NAME, SHORT_NAME
     };
 
     protected static final class RootObject {
 
         private final ProceedingJoinPoint pjp;
 
         private RootObject(ProceedingJoinPoint pjp) {
             super();
             this.pjp = pjp;
         }
 
         public Object[] getArgs() {
             return pjp.getArgs();
         }
 
         public Object getInvokedObject() {
             return pjp.getThis();
         }
 
         public Properties getSystemProperties() {
             return System.getProperties();
         }
     }
 
     /**
      * <p>
      * Formats the given <code>fullyQualifiedName</code> according to the given
      * <code>classNameStyle</code>.
      * </p>
      * <p>
      * Samples with <code>java.lang.String</code>:
      * <ul>
      * <li>{@link ClassNameStyle#FULLY_QUALIFIED_NAME} :
      * <code>java.lang.String</code></li>
      * <li>{@link ClassNameStyle#COMPACT_FULLY_QUALIFIED_NAME} :
      * <code>j.l.String</code></li>
      * <li>{@link ClassNameStyle#SHORT_NAME} : <code>String</code></li>
      * </ul>
      * </p>
      */
     protected static String getFullyQualifiedMethodName(String fullyQualifiedClassName, String methodName, ClassNameStyle classNameStyle) {
         StringBuilder fullyQualifiedMethodName = new StringBuilder(fullyQualifiedClassName.length() + methodName.length() + 1);
         switch (classNameStyle) {
         case FULLY_QUALIFIED_NAME:
             fullyQualifiedMethodName.append(fullyQualifiedClassName);
             break;
         case COMPACT_FULLY_QUALIFIED_NAME:
             String[] splittedFullyQualifiedName = StringUtils.delimitedListToStringArray(fullyQualifiedClassName, ".");
             for (int i = 0; i < splittedFullyQualifiedName.length - 1; i++) {
                 fullyQualifiedMethodName.append(splittedFullyQualifiedName[i].charAt(0)).append(".");
             }
             fullyQualifiedMethodName.append(splittedFullyQualifiedName[splittedFullyQualifiedName.length - 1]);
             break;
         case SHORT_NAME:
             fullyQualifiedMethodName.append(StringUtils.unqualify(fullyQualifiedClassName));
             break;
         default:
             // should not occur
             fullyQualifiedMethodName.append(fullyQualifiedClassName);
             break;
         }
         fullyQualifiedMethodName.append(".").append(methodName);
         return fullyQualifiedMethodName.toString();
     }
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private ClassNameStyle classNameStyle = ClassNameStyle.COMPACT_FULLY_QUALIFIED_NAME;
 
     private ExpressionParser expressionParser = new SpelExpressionParser();
 
     /**
      * @see ObjectName#getDomain()
      */
     private String jmxDomain = "fr.xebia";
 
     private MBeanExporter mbeanExporter;
 
     private String name;
 
     private ObjectName objectName;
 
     private ParserContext parserContext = new TemplateParserContext();
 
     private ConcurrentMap<Method, Expression> profiledMethodNameAsExpressionByMethod = new ConcurrentHashMap<Method, Expression>();
 
     private MBeanServer server;
 
     /**
      * visible for tests
      */
     protected ConcurrentMap<String, ServiceStatistics> serviceStatisticsByName = new ConcurrentHashMap<String, ServiceStatistics>();
 
     public void afterPropertiesSet() throws Exception {
         if (this.server == null) {
             this.server = JmxUtils.locateMBeanServer();
         }
 
         this.mbeanExporter = new AnnotationMBeanExporter();
         this.mbeanExporter.setEnsureUniqueRuntimeObjectNames(false);
         this.mbeanExporter.setServer(this.server);
         this.mbeanExporter.setAutodetectMode(MBeanExporter.AUTODETECT_NONE);
         this.mbeanExporter.afterPropertiesSet();
     }
 
     public void destroy() throws Exception {
         this.mbeanExporter.destroy();
     }
 
     public MBeanExporter getMbeanExporter() {
         return mbeanExporter;
     }
 
     public ObjectName getObjectName() throws MalformedObjectNameException {
         if (objectName == null) {
             String objectNameAsString = jmxDomain + ":type=ProfileAspect";
             if (StringUtils.hasLength(name)) {
                 objectNameAsString += ",name=" + ObjectName.quote(name);
             }
             objectName = new ObjectName(objectNameAsString);
         }
         return objectName;
     }
 
     @ManagedAttribute
     public int getRegisteredServiceStatisticsCount() {
         return this.serviceStatisticsByName.size();
     }
 
     @Around(value = "execution(* *(..)) && @annotation(profiled)", argNames = "pjp,profiled")
     public Object profileInvocation(ProceedingJoinPoint pjp, Profiled profiled) throws Throwable {
 
         logger.trace("> profileInvocation({},{}", pjp, profiled);
 
         MethodSignature jointPointSignature = (MethodSignature) pjp.getStaticPart().getSignature();
 
         // COMPUTE SERVICE STATISTICS NAME
         Expression nameAsExpression = profiledMethodNameAsExpressionByMethod.get(jointPointSignature.getMethod());
         if (nameAsExpression == null) {
             if (StringUtils.hasLength(profiled.name())) {
                 String nameAsStringExpression = profiled.name();
                 nameAsExpression = expressionParser.parseExpression(nameAsStringExpression, parserContext);
             } else {
                 String fullyQualifiedMethodName = getFullyQualifiedMethodName(//
                         jointPointSignature.getDeclaringTypeName(), //
                         jointPointSignature.getName(), //
                         this.classNameStyle);
                 nameAsExpression = new LiteralExpression(fullyQualifiedMethodName);
             }
         }
 
         String serviceStatisticsName;
         if (nameAsExpression instanceof LiteralExpression) {
             // Optimization : prevent useless objects instantiations
             serviceStatisticsName = nameAsExpression.getExpressionString();
         } else {
             serviceStatisticsName = nameAsExpression.getValue(new RootObject(pjp), String.class);
         }
 
         // LOOKUP SERVICE STATISTICS
         ServiceStatistics serviceStatistics = serviceStatisticsByName.get(serviceStatisticsName);
 
         if (serviceStatistics == null) {
             // INSTIANCIATE NEW SERVICE STATISTICS
             ServiceStatistics newServiceStatistics = new ServiceStatistics(//
                     new ObjectName(this.jmxDomain + ":type=ServiceStatistics,name=" + serviceStatisticsName), //
                     profiled.businessExceptionsTypes(), profiled.communicationExceptionsTypes());
 
             newServiceStatistics.setSlowInvocationThresholdInMillis(profiled.slowInvocationThresholdInMillis());
             newServiceStatistics.setVerySlowInvocationThresholdInMillis(profiled.verySlowInvocationThresholdInMillis());
             int maxActive;
             if (StringUtils.hasLength(profiled.maxActiveExpression())) {
                 maxActive = expressionParser.parseExpression(profiled.maxActiveExpression(), parserContext).getValue(new RootObject(pjp),
                         Integer.class);
             } else {
                 maxActive = profiled.maxActive();
             }
             newServiceStatistics.setMaxActive(maxActive);
             newServiceStatistics.setMaxActiveSemaphoreAcquisitionMaxTimeInNanos(profiled.maxActiveSemaphoreAcquisitionMaxTimeInMillis());
 
             ServiceStatistics previousServiceStatistics = serviceStatisticsByName.putIfAbsent(serviceStatisticsName, newServiceStatistics);
             if (previousServiceStatistics == null) {
                 serviceStatistics = newServiceStatistics;
                 mbeanExporter.registerManagedResource(serviceStatistics);
             } else {
                 serviceStatistics = previousServiceStatistics;
             }
         }
 
         // INVOKE AND PROFILE INVOCATION
         long nanosBefore = System.nanoTime();
 
         Semaphore semaphore = serviceStatistics.getMaxActiveSemaphore();
         if (semaphore != null) {
             boolean acquired = semaphore.tryAcquire(serviceStatistics.getMaxActiveSemaphoreAcquisitionMaxTimeInNanos(),
                     TimeUnit.NANOSECONDS);
             if (!acquired) {
                 serviceStatistics.incrementServiceUnavailableExceptionCount();
                 throw new ServiceUnavailableException("Service '" + serviceStatisticsName + "' is unavailable: "
                         + serviceStatistics.getCurrentActive() + " invocations of are currently running");
             }
         }
         serviceStatistics.incrementCurrentActiveCount();
         try {
             
             Object returned = pjp.proceed();
             
             return returned;
         } catch (Throwable t) {
             serviceStatistics.incrementExceptionCount(t);
             throw t;
         } finally {
             if (semaphore != null) {
                 semaphore.release();
             }
             serviceStatistics.decrementCurrentActiveCount();
             long deltaInNanos = System.nanoTime() - nanosBefore;
             serviceStatistics.incrementInvocationCounterAndTotalDurationWithNanos(deltaInNanos);
            if(logger.isDebugEnabled()) {
                 logger.debug("< profileInvocation({}): {}ns", serviceStatisticsName, deltaInNanos);
             }
         }
     }
 
     public void setBeanName(String beanName) {
         this.name = beanName;
     }
 
     public void setClassNameStyle(ClassNameStyle classNameStyle) {
         this.classNameStyle = classNameStyle;
     }
 
     /**
      * 
      * @param classNameStyle
      *            one of COMPACT_FULLY_QUALIFIED_NAME, FULLY_QUALIFIED_NAME and
      *            SHORT_NAME
      */
     public void setClassNameStyle(String classNameStyle) {
         this.classNameStyle = ClassNameStyle.valueOf(classNameStyle);
     }
 
     public void setJmxDomain(String jmxDomain) {
         this.jmxDomain = jmxDomain;
     }
 
     public void setServer(MBeanServer server) {
         this.server = server;
     }
 }
