 /**
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  *  contributor license agreements.  See the NOTICE file distributed with
  *  this work for additional information regarding copyright ownership.
  *  The ASF licenses this file to You under the Apache License, Version 2.0
  *  (the "License"); you may not use this file except in compliance with
  *  the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.yoko.rmispec.util;
 
 import org.apache.yoko.osgi.ProviderLocator;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.security.PrivilegedActionException;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 public class UtilLoader {
     static final Logger logger = Logger.getLogger(UtilLoader.class.getName());
 
     // Note: this field must be declared before the static intializer that calls Util.loadClass
     // since that method will call loadClass0 which uses this field... if it is below the static
     // initializer the _secman field will be null
     private static final SecMan _secman = getSecMan();
     
     static public Class<?> loadServiceClass(String delegateName, String delegateKey) throws ClassNotFoundException {
     	
     	try {
    		Class<?> cls = ProviderLocator.getServiceClass(delegateKey, null, null);
            if (cls != null) {
                return cls;
            }
     	} catch (ClassNotFoundException e){
     		// skip
     	}
     	
     	return loadClass0(delegateName, null, null);
     }
 
     static public Class loadClass(String name, String codebase, ClassLoader loader)
             throws ClassNotFoundException {
         
 
         try {
             return ProviderLocator.loadClass(name, null, loader);
         } catch (ClassNotFoundException e) {
             //skip
         }
 
         return loadClass0(name, codebase, loader);
     }
     
     private static Class<?> loadClass0(String name, String codebase, ClassLoader loader) 
     	throws ClassNotFoundException {
     	Class result = null;
     	
     	ClassLoader stackLoader = null;
         ClassLoader thisLoader = UtilLoader.class.getClassLoader(); 
         Class[] stack = _secman.getClassContext();
         for (int i = 1; i < stack.length; i++) {
             ClassLoader testLoader = stack[i].getClassLoader();
             if (testLoader != null && testLoader != thisLoader)
             {
                 stackLoader = thisLoader; 
                 break; 
             }
         }
 
         if (stackLoader != null) {
             try {
                 logger.finer("trying stack loader");
                 result = stackLoader.loadClass(name);
             } catch (ClassNotFoundException ex) {
                 // skip //
             }
 
             if (result != null) {
                 return result;
             }
         }
 
         // try loading using our loader, just in case we really were loaded
         // using the same classloader the delegate is in.
         if (thisLoader != null) {
             try {
                 logger.finer("trying UtilLoader loader");
                 result = thisLoader.loadClass(name);
             } catch (ClassNotFoundException ex) {
                 // skip //
             }
 
             if (result != null) {
                 return result;
             }
         }
 
         if (codebase != null && !"".equals(codebase)
                 && !Boolean.getBoolean("java.rmi.server.useCodeBaseOnly")) {
             try {
                 logger.finer("trying RMIClassLoader");
 
                 URLClassLoader url_loader = new URLClassLoader(
                         new URL[]{new URL(codebase)}, loader);
 
                 result = url_loader.loadClass(name);
 
                 // log.info("SUCESSFUL class download "+name+" from "+codebase,
                 // new Throwable("TRACE"));
 
             } catch (ClassNotFoundException ex) {
                 logger.log(Level.FINER, "RMIClassLoader says " + ex.getMessage(), ex);
 
                 // log.info("FAILED class download "+name+" from "+codebase,
                 // ex);
 
                 // skip //
             } catch (MalformedURLException ex) {
                 logger.log(Level.FINER, "RMIClassLoader says " + ex.getMessage(), ex);
 
                 logger.finer("FAILED class download " + name + " from "
                         + codebase + " " + ex.getMessage());
 
                 // skip //
             } catch (RuntimeException ex) {
 
                 logger.log(Level.FINER, "FAILED class download " + name + " from "
                         + codebase + " " + ex.getMessage(), ex);
 
             }
 
             if (result != null) {
                 return result;
             }
 
         } else {
 
             codebase = (String) AccessController.doPrivileged(new GetSystemPropertyAction("java.rmi.server.codebase"));
 
             if (codebase != null) {
                 try {
                     result = java.rmi.server.RMIClassLoader.loadClass(codebase,
                             name);
                 } catch (ClassNotFoundException ex) {
                     // skip //
                 } catch (MalformedURLException ex) {
                     // skip //
                 }
 
                 if (result != null) {
                     return result;
                 }
             }
         }
 
         if (loader == null) {
             loader = getContextClassLoader();
         }
 
         try {
             logger.finer("trying local loader");
             result = loader.loadClass(name);
         } catch (ClassNotFoundException ex) {
             logger.log(Level.FINER, "LocalLoader says " + ex.getMessage(), ex);
         }
 
         if (result != null) {
             return result;
         }
 
         throw new ClassNotFoundException(name);
     }
 
     static ClassLoader getContextClassLoader() {
         return (ClassLoader) AccessController
                 .doPrivileged(new PrivilegedAction() {
                     public Object run() {
                         return Thread.currentThread().getContextClassLoader();
                     }
                 });
     }
 
 
     static class SecMan extends java.rmi.RMISecurityManager {
         public Class[] getClassContext() {
             return super.getClassContext();
         }
     }
 
     private static SecMan getSecMan() {
         try {
             return (SecMan) AccessController
                     .doPrivileged(new java.security.PrivilegedExceptionAction() {
                         public Object run() {
                             return new SecMan();
                         }
                     });
         } catch (PrivilegedActionException e) {
             throw new RuntimeException(e);
         }
 
     }
 }
 
 
 
