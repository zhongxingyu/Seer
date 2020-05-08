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
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author mathieuancelin
  */
 public class WebFrameworkClassLoader extends ClassLoader {
 
     public WebFrameworkClassLoader(ClassLoader parent) {
         super(parent);
     }
 
     @Override
     public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("app.controller")) {
             return findClass(name);
         } else {
             return super.loadClass(name);
         }
     }
 
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
         if (name.startsWith("app.")) {
             String path = name.replace(".", "/");
             RequestCompiler.compile(path);
             File clazz = new File(WebFramework.FWK_COMPILED_CLASSES_PATH, path + ".class");
             byte[] b = getClassDefinition(clazz);
             return defineClass(name, b, 0, b.length);
         } else {
             return super.findClass(name);
         }
     }
 
     private byte[] getClassDefinition(File file) {
         InputStream is = null;
         try {
             is = new FileInputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(RequestCompiler.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (is == null) {
             return null;
         }
         try {
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             byte[] buffer = new byte[8192];
             int count;
             while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                 os.write(buffer, 0, count);
             }
             return os.toByteArray();
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 }
