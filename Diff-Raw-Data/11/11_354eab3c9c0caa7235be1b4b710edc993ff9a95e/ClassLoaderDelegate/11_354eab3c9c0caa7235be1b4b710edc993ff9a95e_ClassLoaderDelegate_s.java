 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.dev;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 
 import org.nuxeo.osgi.application.MutableClassLoader;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class ClassLoaderDelegate implements MutableClassLoader {
 
     protected ClassLoader cl;
     protected Method addURL;
     protected Method getURLs;
     
     public ClassLoaderDelegate(ClassLoader cl) throws IllegalArgumentException {        
         initialize(cl);
         if (addURL == null) {
             throw new IllegalArgumentException("Incompatible class loader: "+cl.getClass()+". ClassLoader must provide a method: addURL(URL url)");
         }
         addURL.setAccessible(true);
         getURLs.setAccessible(true);
     }
     
     protected void initialize(ClassLoader cl) {
         Class<?> clazz = cl.getClass();
         do {
             try {
                addURL = clazz.getDeclaredMethod("addURL", URL.class);
                getURLs = clazz.getDeclaredMethod("getURLs");
             } catch (NoSuchMethodException e) {
                 clazz = clazz.getSuperclass();
             } catch (Exception e) {
                 throw new IllegalArgumentException("Failed to adapt class loader: "+cl.getClass(), e);    
             }
        } while (addURL == null && clazz != null);
         if (addURL == null) { //try parent
             ClassLoader parent = cl.getParent();
             if (parent != null) {
                 initialize(parent);
             }
         } else {            
             this.cl = cl;
         }
     }
     
     public void addURL(URL url) {
         try {
             addURL.invoke(cl, url);
         } catch (Exception e) {
             throw new RuntimeException("Failed to add URL to class loader: "+url, e);
         }
     }
 
     public URL[] getURLs() {
         try {
             return (URL[])getURLs.invoke(cl);
         } catch (Exception e) {
             throw new RuntimeException("Failed to get URLs from class loader", e);
         }
     }
     
     public ClassLoader getClassLoader() {
         return cl;
     }
 
 }
