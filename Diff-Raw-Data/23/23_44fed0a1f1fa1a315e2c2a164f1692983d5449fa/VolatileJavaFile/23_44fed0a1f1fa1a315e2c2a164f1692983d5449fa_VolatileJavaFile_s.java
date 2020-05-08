 /*
  * Copyright 2010 @ashigeru.
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
 package com.ashigeru.lang.java.jsr199.testing;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.tools.JavaFileObject;
 import javax.tools.SimpleJavaFileObject;
 
 /**
  * メモリ上でソースファイルを取り扱うためのファイルオブジェクトクラス。
  */
 public class VolatileJavaFile extends SimpleJavaFileObject {
 
     /**
      * このファイルのスキーマ名。
      */
     public static final String URI_SCHEME = VolatileJavaFile.class.getName();
 
     volatile String contents;
 
     /**
      * 空の内容を持つインスタンスを生成する。
     * @param path このファイルのソースフォルダからの相対パス (.javaで終わること)
      * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
      */
     public VolatileJavaFile(String path) {
         this(path, "");
     }
 
     /**
      * インスタンスを生成する。
      * @param path このファイルのソースフォルダからの相対パス (.javaで終わること)
      * @param contents このファイルの内容
      * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
      */
     public VolatileJavaFile(String path, String contents) {
         super(toUriFromPath(path), JavaFileObject.Kind.SOURCE);
         if (contents == null) {
             throw new IllegalArgumentException("contents must not be null"); //$NON-NLS-1$
         }
         this.contents = contents;
     }
 
     @Override
     public CharSequence getCharContent(boolean ignoreEncodingErrors) {
         return contents;
     }
 
     @Override
     public Reader openReader(boolean ignoreEncodingErrors) {
         return new StringReader(contents);
     }
 
     @Override
     public Writer openWriter() {
         this.contents = "";
         return new StringWriter() {
             @Override
             public void close() {
                 contents = toString();
             }
         };
     }
 
     private static URI toUriFromPath(String path) {
         if (path == null) {
             throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
         }
        if (path.endsWith(JavaFileObject.Kind.SOURCE.extension) == false) {
            throw new IllegalArgumentException("path must end with \".java\""); //$NON-NLS-1$
        }
         try {
             return new URI(
                 URI_SCHEME,
                 null,
                "/" + path.replace('\\', '/'),
                 null);
         }
         catch (URISyntaxException e) {
             throw new IllegalArgumentException(e);
         }
     }
 }
