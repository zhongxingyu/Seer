 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.loaders.java.jsr199;
 
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.core.util.ClassUtils;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 import org.apache.myfaces.scripting.loaders.java.RecompiledClassLoader;
 
 import javax.tools.FileObject;
 import javax.tools.ForwardingJavaFileManager;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class ContainerFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
 
     StandardJavaFileManager _delegate = null;
     String _classPath = null;
     RecompiledClassLoader classLoader = null;
 
     public ContainerFileManager(StandardJavaFileManager standardJavaFileManager) {
         super(standardJavaFileManager);
         _delegate = standardJavaFileManager;
     }
 
 
     @Override
     public JavaFileObject getJavaFileForOutput(Location location, String s, JavaFileObject.Kind kind, FileObject fileObject) throws IOException {
         return super.getJavaFileForOutput(location, s, kind, fileObject);
     }
 
     @Override
     public ClassLoader getClassLoader(Location location) {
         return Thread.currentThread().getContextClassLoader();
     }
 
     public ClassLoader getClassLoader() {
         return classLoader;
     }
 
     public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
         return _delegate.getJavaFileObjects(files);
     }
 
     public Iterable<? extends JavaFileObject> getJavaFileObjects(String... files) {
         return _delegate.getJavaFileObjects(files);
     }
 
     public Iterable<? extends JavaFileObject> getJavaFileObjectsSingle(String files) {
         return _delegate.getJavaFileObjects(files);
     }
 
     public String getClassPath() {
         if (_classPath != null) {
             return _classPath;
         }
         ClassLoader cls = getClassLoader(null);
 
         StringBuilder retVal = new StringBuilder(500);
         while (cls != null) {
             if (cls instanceof URLClassLoader) {
                 URL[] urls = ((URLClassLoader) cls).getURLs();
                 int len = urls.length;
 
                 for (int cnt = 0; cnt < len; cnt++) {
 
                     retVal.append(urls[cnt].getFile());
                     if (cnt < len - 1) {
                         retVal.append(File.pathSeparator);
                     }
                 }
             }
 
             cls = cls.getParent();
         }
 
         String retStr = retVal.toString();
        if (retStr.length() > 1) {
            retStr = retStr.substring(0, retStr.length() - 1);
        }
 
         return (_classPath = retStr);
     }
 
     public File getTempDir() {
         return WeavingContext.getConfiguration().getCompileTarget();
     }
 
 }
