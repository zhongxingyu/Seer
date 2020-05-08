 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.aptina.unit;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 import javax.annotation.processing.Processor;
 import javax.tools.FileObject;
 import javax.tools.ForwardingJavaFileManager;
 import javax.tools.JavaFileManager;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.StandardLocation;
 import javax.tools.JavaFileObject.Kind;
 
 import org.seasar.aptina.commons.util.IOUtils;
 
 import static org.seasar.aptina.commons.util.CollectionUtils.*;
 
 /**
  * {@link Processor}をテストする環境用の{@link JavaFileManager}の実装です。
  * 
  * @author koichik
  */
 class TestingJavaFileManager extends
         ForwardingJavaFileManager<StandardJavaFileManager> {
 
     final Map<String, InMemoryJavaFileObject> fileObjects = newHashMap();
 
     final Map<String, InMemoryJavaFileObject> javaFileObjects = newHashMap();
 
     final Charset charset;
 
     /**
      * インスタンスを構築します。
      * 
      * @param fileManager
      *            移譲先となる{@link StandardJavaFileManager}
      * @param charset
      *            文字セット
      */
     public TestingJavaFileManager(final StandardJavaFileManager fileManager,
             final Charset charset) {
         super(fileManager);
         this.charset = charset;
     }
 
     @Override
     public FileObject getFileForInput(final Location location,
             final String packageName, final String relativeName)
             throws IOException {
         final String key = packageName + "::" + relativeName;
         if (fileObjects.containsKey(key)) {
             return fileObjects.get(key);
         }
         return super.getFileForInput(location, packageName, relativeName);
     }
 
     @Override
     public FileObject getFileForOutput(final Location location,
             final String packageName, final String relativeName,
             final FileObject sibling) throws IOException {
         final String key = packageName + "::" + relativeName;
         if (fileObjects.containsKey(key)) {
             return fileObjects.get(key);
         }
 
         byte[] content = null;
        URI uri = null;
         try {
             final FileObject originalFileObject;
             if (location == StandardLocation.CLASS_OUTPUT) {
                 originalFileObject = getFileForInput(
                         StandardLocation.SOURCE_PATH, packageName, relativeName);
             } else {
                 originalFileObject = super.getFileForOutput(location,
                         packageName, relativeName, sibling);
             }
             content = IOUtils.readBytes(originalFileObject.openInputStream());
            uri = originalFileObject.toUri();
         } catch (final FileNotFoundException ignore) {
         }
         final InMemoryJavaFileObject fileObject = new InMemoryJavaFileObject(
                uri != null ? uri : toURI(location, packageName, relativeName),
                Kind.OTHER, charset, content);
         fileObjects.put(key, fileObject);
         return fileObject;
     }
 
     @Override
     public JavaFileObject getJavaFileForInput(final Location location,
             final String className, final Kind kind) throws IOException {
         final String key = kind.name() + "::" + className;
         if (fileObjects.containsKey(key)) {
             return fileObjects.get(key);
         }
         return super.getJavaFileForInput(location, className, kind);
     }
 
     @Override
     public JavaFileObject getJavaFileForOutput(final Location location,
             final String className, final Kind kind, final FileObject sibling)
             throws IOException {
         final String key = kind.name() + "::" + className;
         if (fileObjects.containsKey(key)) {
             return fileObjects.get(key);
         }
 
         byte[] content = null;
        URI uri = null;
         try {
             final JavaFileObject originalFileObject = super
                     .getJavaFileForOutput(location, className, kind, sibling);
             content = IOUtils.readBytes(originalFileObject.openInputStream());
            uri = originalFileObject.toUri();
         } catch (final FileNotFoundException ignore) {
         }
         final InMemoryJavaFileObject fileObject = new InMemoryJavaFileObject(
                uri != null ? uri : toURI(location, className), kind, charset,
                content);
         fileObjects.put(key, fileObject);
         return fileObject;
     }
 
     /**
      * 生成された {@link JavaFileObject} を返します．
      * 
      * @param location
      *            ロケーション
      * @param className
      *            クラスの完全限定名
      * @param kind
      *            ファイルの種類
      * @return 生成された {@link JavaFileObject}
      * @throws IOException
      *             入出力例外が発生した場合
      */
     public JavaFileObject getGeneratedJavaFile(final Location location,
             final String className, final Kind kind) throws IOException {
         final String key = kind.name() + "::" + className;
         if (fileObjects.containsKey(key)) {
             return fileObjects.get(key);
         }
         return null;
     }
 
     URI toURI(final Location location, final String packageName,
             final String relativeName) {
         try {
             return new URI(location.getName() + "/"
                     + packageName.replace('.', '/') + "/" + relativeName);
         } catch (final URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
 
     URI toURI(final Location location, final String className) {
         try {
             return new URI(location.getName() + "/"
                     + className.replace('.', '/') + ".java");
         } catch (final URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
