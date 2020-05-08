 /* _________________________________________________________________________
  *
  *             Vasco : A Visual Churn Exploration Tool
  *
  *
  *  This file is part of the Vasco project.
  *
  *  Vasco is distributed at:
  *      http://github.com/GEODES-UdeM/Vasco
  *
  *
  *  Copyright (c) 2012, Universite de Montreal
  *  All rights reserved.
  *
  *  This software is licensed under the following license (Modified BSD
  *  License):
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are
  *  met:
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    * Neither the name of the Universite de Montreal nor the names of its
  *      contributors may be used to endorse or promote products derived
  *      from this software without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  *  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  *  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL UNIVERSITE DE
  *  MONTREAL BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * _________________________________________________________________________
  */
 
 package vasco;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import vasco.util.Files;
 
 public class JarDriver {
     private static final boolean DEBUG = "true".equalsIgnoreCase(System.getProperty("vasco.debug"));
 
     public static void main(String[] args) throws IOException {
         ClassLoader classLoader = JarDriver.class.getClassLoader();
         Collection<String> jars = loadJarList(classLoader);
         final File extractedJarDir = Files.createTempDir();
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 Files.delete(extractedJarDir);
             }
         });
         if (DEBUG) {
             System.out.println("[Vasco] JOGL jars will be extracted to " + extractedJarDir);
         }
         URL[] extractedJarURLs = extractJars(extractedJarDir, jars, classLoader);
 
         VascoClassLoader vascoLoader = new VascoClassLoader(extractedJarURLs, classLoader);
        vascoLoader.main("vasco.Main", args);
     }
 
     private static Collection<String> loadJarList(ClassLoader classLoader) throws IOException {
         InputStream stream = classLoader.getResourceAsStream("META-INF/jars");
         if (stream == null) return Collections.emptyList();
 
         List<String> jars = new ArrayList<String>();
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         String jarName;
         while ((jarName = reader.readLine()) != null) {
             jars.add(jarName.trim());
         }
 
         return jars;
     }
 
     private static URL[] extractJars(File root, Collection<String> jars, ClassLoader resourceLoader) throws IOException {
         URL[] urls = new URL[jars.size()];
 
         int index = 0;
         for (String jar: jars) {
             File jarFile = new File(root, jar);
             urls[index++] = jarFile.toURI().toURL();
 
             OutputStream out = new BufferedOutputStream(new FileOutputStream(jarFile));
             copy(resourceLoader.getResourceAsStream("jars/" + jar), out);
             out.close();
         }
 
         return urls;
     }
 
     private static void copy(InputStream in, OutputStream out) throws IOException {
         final int BUFFER_SIZE = 2048;
 
         byte[] b = new byte[BUFFER_SIZE];
 
         int read;
         while ((read = in.read(b)) != -1) {
             out.write(b, 0, read);
         }
 
         out.flush();
     }
 
     private static class VascoClassLoader extends URLClassLoader {
         public VascoClassLoader(URL[] urls, ClassLoader parent) {
             super(urls, parent);
 
             addClasspathURLs();
         }
 
         @Override
         public Class<?> loadClass(String name) throws ClassNotFoundException {
             if (name.startsWith("vasco.")) return findClass(name);
             return super.loadClass(name);
         }
 
         public void addClasspathURLs() {
             String classpath = System.getProperty("java.class.path");
             String[] cpEntries = classpath.split(File.pathSeparator);
 
             for (int i = 0; i < cpEntries.length; i++) {
                 File entry = new File(cpEntries[i]);
                 try {
                     URL url = entry.toURI().toURL();
                     addURL(url);
                 } catch (MalformedURLException e) {
                     // Skip
                 }
             }
         }
 
         public void main(String mainClass) {
             main(mainClass, new String[0]);
         }
 
         public void main(String mainClass, String[] args) {
             Throwable exception;
             try {
                 Class<?> c = loadClass(mainClass);
                 Method mainMethod = c.getDeclaredMethod("main", String[].class);
                 mainMethod.invoke(null, (Object) args);
                 return;
             } catch (ClassNotFoundException e) {
                 exception = e;
             } catch (SecurityException e) {
                 exception = e;
             } catch (NoSuchMethodException e) {
                 exception = e;
             } catch (IllegalArgumentException e) {
                 exception = e;
             } catch (IllegalAccessException e) {
                 exception = e;
             } catch (InvocationTargetException e) {
                 exception = e.getTargetException();
             }
 
             throw new RuntimeException(exception);
         }
     }
 }
