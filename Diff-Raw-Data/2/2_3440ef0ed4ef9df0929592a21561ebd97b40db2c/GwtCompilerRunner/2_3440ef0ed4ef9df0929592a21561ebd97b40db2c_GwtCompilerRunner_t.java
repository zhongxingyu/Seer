 /*
  Copyright 2012 Eric Karge (eric.karge@hypoport.de)
 
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
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 
 public class GwtCompilerRunner {
 
   public static void main(String[] argv) throws Exception {
    Class<?> compiler = createGWTCompiler(readUrlsFromCommandLine(argv));
 
     runGWTCompiler(compiler);
   }
 
   private static void runGWTCompiler(Class<?> gwtCompiler) throws MalformedURLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
     Method main = gwtCompiler.getMethod("main", String[].class);
     main.invoke(null, new Object[]{new String[]{"org.example.gin.classloader.demo.MyGwtModule"}});
   }
 
   private static URL[] readUrlsFromCommandLine(String[] argv) throws MalformedURLException {
     URL[] urls = new URL[argv.length];
     for (int i = 0; i < argv.length; ++i) {
       urls[i] = new File(argv[i]).toURI().toURL();
     }
     return urls;
   }
 
   private static Class<?> createGWTCompiler(URL[] additionalClassPath) throws ClassNotFoundException {
     ClassLoader myClassLoader = new URLClassLoader(additionalClassPath, GwtCompilerRunner.class.getClassLoader());
     Thread.currentThread().setContextClassLoader(myClassLoader);
 
     return myClassLoader.loadClass("com.google.gwt.dev.Compiler");
   }
 
   private static Class<?> createGWTCompiler2(URL[] additionalClassPath) throws ClassNotFoundException {
     ClassLoader myClassLoader = new URLClassLoader(additionalClassPath, GwtCompilerRunner.class.getClassLoader());
     Thread.currentThread().setContextClassLoader(myClassLoader);
 
     return ClassLoader.getSystemClassLoader().loadClass("com.google.gwt.dev.Compiler");
   }
 
   private static Class<?> createGWTCompiler3(URL[] additionalClassPath) throws ClassNotFoundException {
     URL[] combinedClassPath = concat(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs(), additionalClassPath);
     ClassLoader myClassLoader = new URLClassLoader(combinedClassPath, null);
     Thread.currentThread().setContextClassLoader(myClassLoader);
 
     return myClassLoader.loadClass("com.google.gwt.dev.Compiler");
   }
 
   private static <T> T[] concat(T[] first, T[] second) {
     T[] result = Arrays.copyOf(first, first.length + second.length);
     System.arraycopy(second, 0, result, first.length, second.length);
     return result;
   }
 }
