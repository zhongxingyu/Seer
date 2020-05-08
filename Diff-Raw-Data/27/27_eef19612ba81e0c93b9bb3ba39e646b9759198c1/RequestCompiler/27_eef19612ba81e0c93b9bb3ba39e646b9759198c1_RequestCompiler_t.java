 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.compiler;
 
 import cx.ath.mancel01.webframework.WebFramework;
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import javax.tools.JavaCompiler;
 import javax.tools.ToolProvider;
 
 /**
  *
  * @author mathieuancelin
  */
 public class RequestCompiler {
 
     private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
 
     private static Map<File, Long> sourceFiles = new HashMap<File, Long>();
 
     public static Class<?> getCompiledClass(Class<?> clazz) {
         try {
             ClassLoader loader = new WebFrameworkClassLoader(RequestCompiler.class.getClassLoader());
             Class<?> loadedClazz = loader.loadClass(clazz.getName());
             loader = null;
             return loadedClazz;
         } catch (ClassNotFoundException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public static boolean compile(String clazz) {
         return compileClassIfChanged(clazz);
     }
 
     private static boolean compileClassIfChanged(String path) {
         long start = System.currentTimeMillis();
         File oldClass = new File(WebFramework.FWK_COMPILED_CLASSES_PATH, path + ".class");
         File source = new File(WebFramework.JAVA_SOURCES, path + ".java");
         if (!sourceFiles.containsKey(source)) {
             sourceFiles.put(source, source.lastModified());
             compile(source);
            WebFramework.logger.trace("class {} compilation : {} ms."
                    , path.replace("/", "."), System.currentTimeMillis() - start);
             return true;
         } else {
             long knownSourceModif = sourceFiles.get(source);
             if (knownSourceModif != source.lastModified()) {
                 /**if (oldClass.exists()) {
                     //oldClass.delete();
                 }**/
                 compile(source);
                 sourceFiles.put(source, source.lastModified());
             } else {
                 return false;
             }
         }
        WebFramework.logger.trace("class {} compilation : {} ms."
                , path.replace("/", "."), System.currentTimeMillis() - start);
         return true;
     }
 
     private static void compile(File source) {
         try {
             //Process p = Runtime.getRuntime().exec(command);
             //p.waitFor();
             javac.run(null, null, null
                     , "-encoding", "utf-8", "-source", "1.6"
                     , "-target", "1.6", "-d"
                     , WebFramework.FWK_COMPILED_CLASSES_PATH.getAbsolutePath()
                     , "-classpath", WebFramework.classpath, source.getAbsolutePath());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
