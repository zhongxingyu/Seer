 /*
  * This is a common dao with basic CRUD operations and is not limited to any 
  * persistent layer implementation
  * 
  * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.exim.impl;
 
 import com.smartitengineering.domain.annotations.Eager;
 import com.smartitengineering.domain.annotations.Export;
 import com.smartitengineering.domain.annotations.Id;
 import com.smartitengineering.domain.annotations.Name;
 import com.smartitengineering.domain.annotations.ResourceDomain;
 import com.smartitengineering.domain.exim.DomainSelfExporter;
 import com.smartitengineering.domain.exim.DomainSelfImporter;
 import com.smartitengineering.domain.exim.IdentityCustomizer;
 import com.smartitengineering.domain.exim.StringValueProvider;
 import com.smartitengineering.exim.AssociationConfig;
 import com.smartitengineering.exim.ClassConfigScanner;
 import com.smartitengineering.exim.ConfigRegistrar;
 import com.smartitengineering.exim.EximResourceConfig;
 import com.smartitengineering.exim.PackageConfigScanner;
 import com.smartitengineering.util.simple.IOFactory;
 import com.smartitengineering.util.simple.reflection.AnnotationConfig;
 import com.smartitengineering.util.simple.reflection.ClassAnnotationVisitorImpl;
 import com.smartitengineering.util.simple.reflection.ClassScanner;
 import com.smartitengineering.util.simple.reflection.VisitCallback;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.AnnotatedElement;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This registrar is responsible for scanning and containing configuration of
  * all resources. Whenever a class is looked up whose package has not been
  * scanned yet, registrar will scanClassForConfig its package to generate cofiguration and
  * while generating it will keep scanning until it reaches the leaves, i.e. in
  * this case association to an object that isn't {@link ResourceDomain}
  * @author imyousuf
  * @since 0.4
  */
 public class DefaultAnnotationConfigScanner
     implements ClassConfigScanner,
                PackageConfigScanner {
 
     private static final String GETTER_PREFIX = "get";
     private static final String IS_PREFIX = "is";
     private static final String HAS_PREFIX = "has";
     private static DefaultAnnotationConfigScanner registrar;
 
     static {
         ConfigRegistrar.registerClassScanner(
             DefaultAnnotationConfigScanner.class, 25);
         ConfigRegistrar.registerPackageScanner(
             DefaultAnnotationConfigScanner.class, 25);
     }
 
     public static DefaultAnnotationConfigScanner getInstance() {
         if (registrar == null) {
             registrar = new DefaultAnnotationConfigScanner();
         }
         return registrar;
     }
     protected final ClassScanner classScanner;
     protected final Map<Class, EximResourceConfig> configuraitons;
     protected final Collection<Class> scannedClasses;
     protected final Collection<String> scannedPackages;
     protected final ResourceVisitCallback resourceVisitCallback;
     protected final VisitCallback<AnnotationConfig> callbackHandler;
 
     protected DefaultAnnotationConfigScanner() {
         classScanner = IOFactory.getDefaultClassScanner();
         configuraitons = new HashMap<Class, EximResourceConfig>();
         scannedClasses = new HashSet<Class>();
         scannedPackages = new HashSet<String>();
         resourceVisitCallback = new ResourceVisitCallback();
         callbackHandler = resourceVisitCallback;
     }
 
     public synchronized Map<Class, EximResourceConfig> getConfigurations() {
         return configuraitons;
     }
 
     public synchronized Collection<Class> getConfiguredResourceClasses() {
         return configuraitons.keySet();
     }
 
     /**
      * Retrieves the configuration for the resource class, if its not already
      * generated then it will search the package of the class and generate its
      * configuraiton.
      * @param resourceClass The class to scanClassForConfig retrieve configuration for
      * @return Configuration for the resource; NULL if no resource is available
      *          or generateable
      * @throws IllegalArgumentException If resource class is null!
      */
     public synchronized EximResourceConfig getResourceConfigForClass(
         final Class resourceClass) {
         if (resourceClass == null) {
             throw new IllegalArgumentException("Resource class can't be null!");
         }
         if (configuraitons.containsKey(resourceClass)) {
             return configuraitons.get(resourceClass);
         }
         else {
             String packageName = resourceClass.getPackage().getName();
             EximResourceConfig resourceConfig = null;
             if (!scannedPackages.contains(packageName)) {
                 resourceConfig = scanPackage(Package.getPackage(packageName),
                     resourceClass);
             }
             return resourceConfig;
         }
     }
 
     /**
      * Scans and prepares all configurations in the package and makes it
      * available for future use.
      * @param resourcePackage Package to scan and gather configuration
      * @throws IllegalArgumentException If package is null
      */
     public synchronized void scanPackageForResourceConfigs(
         final Package resourcePackage) {
         if (resourcePackage == null) {
             throw new IllegalArgumentException("Resource class can't be null!");
         }
         scanPackage(resourcePackage, null);
     }
 
     protected String getPropertyNameFromMethodName(final String methodName) {
         StringBuilder propertyNameBuilder =
             new StringBuilder(methodName);
         int cutLength = -1;
         if (methodName.startsWith(GETTER_PREFIX)) {
             cutLength = GETTER_PREFIX.length();
         }
         else if (methodName.startsWith(IS_PREFIX)) {
             cutLength = IS_PREFIX.length();
         }
         else if (methodName.startsWith(HAS_PREFIX)) {
             cutLength = HAS_PREFIX.length();
         }
         else {
             throw new IllegalArgumentException(
                 "Not a valid property read accoessor");
         }
         propertyNameBuilder.delete(0, cutLength);
         char firstChar = propertyNameBuilder.charAt(0);
         propertyNameBuilder.delete(0, 1);
         propertyNameBuilder.insert(0, Character.toLowerCase(firstChar));
         return propertyNameBuilder.toString();
     }
 
     /**
      * Scan among the class's annotations to find the required configuration for
      * exporting and importing resources.
      * @param probableResourceClass The probable resource domain class.
      * @return The configuration of the class, null if not annotated with 
      *          {@link ResourceDomain}
      */
     protected EximResourceConfig scanClassForConfig(
         final Class probableResourceClass) {
         if (probableResourceClass == null) {
             return null;
         }
         if (scannedClasses.contains(probableResourceClass)) {
             return getConfigurations().get(probableResourceClass);
         }
         Annotation annotation = probableResourceClass.getAnnotation(
             ResourceDomain.class);
         if (annotation == null) {
             return null;
         }
         EximResourceConfigImpl resourceConfig = new EximResourceConfigImpl();
         resourceConfig.setDomainClass(probableResourceClass);
         Name nameAnnotation = (Name) probableResourceClass.getAnnotation(
             Name.class);
         if (nameAnnotation != null) {
             resourceConfig.setName(nameAnnotation.value());
         }
         else {
             resourceConfig.setName(probableResourceClass.getName());
         }
         ResourceDomain domainAnnotation = (ResourceDomain) annotation;
         resourceConfig.setAccessByPropertyEnabled(domainAnnotation.
             accessByProperty());
         resourceConfig.setAssociateExportPolicyAsUri(domainAnnotation.
             exportAsURIByDefault());
         resourceConfig.setPathToResource(domainAnnotation.path());
         resourceConfig.setExporterImplemented(DomainSelfExporter.class.
             isAssignableFrom(probableResourceClass));
         resourceConfig.setImporterImplemented(DomainSelfImporter.class.
             isAssignableFrom(probableResourceClass));
         resourceConfig.setIdentityCustomizerImplemented(
             IdentityCustomizer.class.isAssignableFrom(probableResourceClass));
         resourceConfig.setPriority(domainAnnotation.priority());
         scanMembers(resourceConfig, probableResourceClass);
         scannedClasses.add(probableResourceClass);
        configuraitons.put(probableResourceClass, resourceConfig);
        return resourceConfig;
     }
 
     /**
      * Scan a package for extracting configurations of resource domains.
      * @param packageName Package to scanClassForConfig.
      * @param resourceClass Main class scanClassForConfig requested for
      * @return The configuration of the resource class, Null if the class is not
      *          a domain class or resourceClass is null
      * @throws java.lang.IllegalArgumentException If package is null
      */
     protected EximResourceConfig scanPackage(final Package packageToScan,
                                              final Class resourceClass)
         throws IllegalArgumentException {
         if (packageToScan == null) {
             throw new IllegalArgumentException();
         }
         EximResourceConfig resourceConfig = null;
         classScanner.scan(new String[]{packageToScan.getName()},
             new ClassAnnotationVisitorImpl(callbackHandler,
             IOFactory.getAnnotationNameForVisitor(ResourceDomain.class)));
         Set<String> classPaths = resourceVisitCallback.getProbableResources();
         if (!classPaths.isEmpty()) {
             for (String classPath : classPaths) {
                 try {
                     Class probableResourceClass =
                         IOFactory.getClassFromVisitorName(classPath);
                     EximResourceConfig config = scanClassForConfig(
                         probableResourceClass);
                     if (config != null && resourceClass != null &&
                         probableResourceClass.equals(resourceClass)) {
                         resourceConfig = config;
                     }
                 }
                 catch (Exception ex) {
                 }
             }
         }
         scannedPackages.add(packageToScan.getName());
         return resourceConfig;
     }
 
     /**
      * It will scan all member attributes and behavior based on configuration on
      * the class. It will also scan all inherited attributes and behavior.
      * @param resourceConfig The config representing the domain class
      * @param resourceClass The domain class
      */
     protected void scanMembers(final EximResourceConfigImpl resourceConfig,
                                final Class resourceClass) {
         if (resourceConfig.isAccessByPropertyEnabled()) {
             scanMethods(resourceConfig, resourceClass);
         }
         else {
             scanFields(resourceConfig, resourceClass);
         }
     }
 
     /**
      * Scans getter methods for discovering associations of the domain and thier
      * respective configurations. It will only scan public getter methods.
      * @param resourceConfig The config of the domain resource
      * @param resourceClass The domain class
      */
     protected void scanMethods(final EximResourceConfigImpl resourceConfig,
                                final Class resourceClass) {
         Method[] methods = resourceClass.getMethods();
         if (methods == null || methods.length <= 0) {
             return;
         }
         for (Method method : methods) {
             String methodName = method.getName();
             //Only scan getter methods as of bean spec, that getter methods with
             //no paratmeters and non-void return types
             if (((methodName.startsWith(GETTER_PREFIX) && methodName.length() >
                 GETTER_PREFIX.length()) || (methodName.startsWith(IS_PREFIX) &&
                 methodName.length() > IS_PREFIX.length()) || (methodName.
                 startsWith(HAS_PREFIX) && methodName.length() > HAS_PREFIX.
                 length())) && method.getReturnType() != null && !method.
                 getReturnType().equals(Void.class) &&
                 (method.getParameterTypes() == null ||
                 method.getParameterTypes().length <= 0)) {
                 scanGetterMethod(resourceConfig, method);
             }
         }
     }
 
     /**
      * Scans a getter method for annotations which is used to cofigure the
      * nature of the export
      * @param resourceConfig The config to populate with configurations
      * @param method The getter method to scan
      * @throws java.lang.IllegalArgumentException If its not a getter method
      *                                            with non-void return type and
      *                                            with a non-zero length bean
      *                                            name length and with no
      *                                            parameter
      */
     protected void scanGetterMethod(final EximResourceConfigImpl resourceConfig,
                                     final Method method)
         throws IllegalArgumentException {
         String methodName = method.getName();
         //Ignore the getClass bean
         if (method.getName().equals("getClass")) {
             return;
         }
         if (!(((methodName.startsWith(GETTER_PREFIX) && methodName.length() >
             GETTER_PREFIX.length()) || (methodName.startsWith(IS_PREFIX) &&
             methodName.length() > IS_PREFIX.length()) || (methodName.startsWith(
             HAS_PREFIX) && methodName.length() > HAS_PREFIX.length())) &&
             method.getReturnType() != null && !method.getReturnType().equals(
             Void.class) && (method.getParameterTypes() == null || method.
             getParameterTypes().length <= 0))) {
             throw new IllegalArgumentException();
         }
         String propertyName = getPropertyNameFromMethodName(methodName);
         Class returnType = method.getReturnType();
         scanAnnotatedElement(method, propertyName, returnType, resourceConfig);
     }
 
     /**
      * Scans fields for discovering associations of the domain and thier
      * respective configurations
      * @param resourceConfig The config of the domain resource
      * @param resourceClass The domain class
      */
     protected void scanFields(EximResourceConfigImpl resourceConfig,
                               Class resourceClass) {
         Field[] fields = resourceClass.getDeclaredFields();
         for (Field field : fields) {
             scanField(resourceConfig, field);
         }
         Class parentClass = resourceClass.getSuperclass();
         if (!parentClass.equals(Object.class)) {
             scanFields(resourceConfig, parentClass);
         }
     }
 
     /**
      * A field is scanned for gathering configurations for export-import.
      * @param resourceConfig Configuraton for the field association
      * @param field The field to scan
      */
     protected void scanField(final EximResourceConfigImpl resourceConfig,
                              final Field field) {
         String propertyName = field.getName();
         Class propertyType = field.getType();
         scanAnnotatedElement(field, propertyName, propertyType, resourceConfig);
     }
 
     /**
      * Scan an ennotated element to extract configuration information
      * @param element Element to scan
      * @param propertyName The name of the property scanning
      * @param propertyType The type of the property
      * @param resourceConfig The configuration for the domain class
      * @throws java.lang.IllegalArgumentException If any argument is null
      */
     protected void scanAnnotatedElement(final AnnotatedElement element,
                                         final String propertyName,
                                         final Class propertyType,
                                         final EximResourceConfigImpl resourceConfig)
         throws IllegalArgumentException {
         if (element == null || propertyName == null || propertyType == null ||
             resourceConfig == null) {
             throw new IllegalArgumentException();
         }
         Export annotation =
             element.getAnnotation(Export.class);
         AssociationConfigImpl configImpl =
             new AssociationConfigImpl();
         configImpl.setName(propertyName);
         configImpl.setAssociationType(AssociationConfig.AssociationType.
             getAssociationType(propertyType));
         Eager eager =
             element.getAnnotation(Eager.class);
         configImpl.setStringProviderImplemented(StringValueProvider.class.
             isAssignableFrom(propertyType));
         configImpl.setEagerSet(eager != null);
         if (annotation != null) {
             configImpl.setItToBeExportedAsUri(!annotation.asObject());
             configImpl.setTransient(annotation.isTransient());
         }
         else {
             configImpl.setItToBeExportedAsUri(false);
             configImpl.setTransient(false);
         }
         resourceConfig.getAssociationConfigs().put(propertyName, configImpl);
         Id id =
             element.getAnnotation(Id.class);
         if (id != null) {
             resourceConfig.setIdPropertyName(propertyName);
         }
     }
 
     /**
      * The visitor callback to be notified for dmain classes.
      */
     protected static class ResourceVisitCallback
         implements VisitCallback<AnnotationConfig> {
 
         private Set<String> probableResources = new HashSet<String>();
 
         public void handle(AnnotationConfig config) {
             probableResources.add(config.getClassName());
         }
 
         public Set<String> getProbableResources() {
             return probableResources;
         }
     }
 }
