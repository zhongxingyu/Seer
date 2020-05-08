 /*
  Copyright 2012 Eric Karge
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
 
 package org.example.gin.classloader.demo;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 public class GinBridgeClassLoaderDemo {
 
     public static void main(String[] argv) throws Exception {
         URL[] urls = readUrlsFromCommandLine(argv);
 
        ClassLoader myClassLoader = new URLClassLoader(urls);
         Thread.currentThread().setContextClassLoader(myClassLoader);
 
         Class<?> gwtCompiler = myClassLoader.loadClass("com.google.gwt.dev.Compiler");
         Method main = gwtCompiler.getMethod("main", String[].class);
         main.invoke(null, new Object[]{new String[]{"org.example.gin.classloader.demo.GinBridgeClassLoaderDemo"}});
     }
 
     private static URL[] readUrlsFromCommandLine(String[] argv) throws MalformedURLException {
         URL[] urls = new URL[argv.length];
         for (int i = 0; i < argv.length; ++i) {
             urls[i] = new File(argv[i]).toURI().toURL();
         }
         return urls;
     }
 }
