 /**
  * Copyright 2012 Alistair Dutton
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
 package com.kelveden.rastajax.core;
 
 import org.scannotation.AnnotationDB;
 import org.scannotation.ClasspathUrlFinder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.Path;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.*;
 
 /**
  * {@link RootResourceScanner} that scans in all JAX-RS resources it can find directly from the specified packages pulled from the classpath.
  */
 public class ClassLoaderRootResourceScanner implements RootResourceScanner {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderRootResourceScanner.class);
 
     private final List<URL> scanUrls;
     private final ClassLoader classLoader;
 
     private boolean scanInterfaces;
 
     /**
      * Constructor.
      *
      * @param classLoader
      *      The {@link ClassLoader} to use to find classes.
      * @param resourcePackages
      *      The resource package(s) that contain the JAX-RS classes.
      */
     public ClassLoaderRootResourceScanner(final ClassLoader classLoader, final String... resourcePackages) {
 
         this.classLoader = classLoader;
         scanUrls = new ArrayList<URL>();
 
         for (String resourcePackage : resourcePackages) {
 
            final URL[] resourcePackageUrls = ClasspathUrlFinder.findResourceBases(resourcePackage, classLoader);
             scanUrls.addAll(Arrays.asList(resourcePackageUrls));
 
             LOGGER.debug("Package {} will be scanned from urls: {}.", resourcePackage, resourcePackageUrls);
         }
     }
 
     public ClassLoaderRootResourceScanner allowInterfaceInheritance() {
         this.scanInterfaces = true;
 
         LOGGER.info("JAX-RS annotations at class-level on interfaces will be honoured.");
 
         return this;
     }
 
     @Override
     public Set<Class<?>> scan() {
         // This is a bit of short-cut really as it only checks for the presence of a URI template annotation on the class -
         // the JAX-RS specification also requires the class to have at least one method annotated with a request method designator
         // and/or URI template. However, given that the scenario where a class has the URI template but no REST methods is both unlikely
         // and absurd, we can take that risk. The worst that will happen is that we do a little extra processing - the class won't
         // loaded for representation anyway due to having no REST methods.
 
         final Set<Class<?>> results = new HashSet<Class<?>>();
 
         final AnnotationDB annotationDb = new AnnotationDB();
         annotationDb.setScanClassAnnotations(true);
         annotationDb.setScanMethodAnnotations(false);
         annotationDb.setScanParameterAnnotations(false);
         annotationDb.setScanFieldAnnotations(false);
 
         try {
             annotationDb.scanArchives(scanUrls.toArray(new URL[scanUrls.size()]));
 
             if (scanInterfaces) {
                 annotationDb.crossReferenceImplementedInterfaces();
             }
 
         } catch (final IOException e) {
             throw new ResourceScanningException(e);
 
         } catch (final AnnotationDB.CrossReferenceException e) {
             throw new ResourceScanningException(e);
         }
 
         Set<String> classesAnnotatedWithPath = annotationDb.getAnnotationIndex().get(Path.class.getName());
         if (classesAnnotatedWithPath == null) {
             classesAnnotatedWithPath = new HashSet<String>();
         }
 
         LOGGER.debug("Found {} classes annotated with @Path: {}.", classesAnnotatedWithPath.size(), classesAnnotatedWithPath.toString());
 
         for (String className : classesAnnotatedWithPath) {
             try {
                 final Class<?> loadedClass = classLoader.loadClass(className);
                 final int classModifiers = loadedClass.getModifiers();
 
                 if (!Modifier.isAbstract(classModifiers) && !Modifier.isInterface(classModifiers)) {
                     results.add(classLoader.loadClass(className));
                 }
 
             } catch (final ClassNotFoundException e) {
                 throw new ResourceScanningException(e);
             }
         }
 
         LOGGER.debug("Scanned {} root resource classes: {}.", results.size(), results.toString());
 
         return results;
     }
 }
