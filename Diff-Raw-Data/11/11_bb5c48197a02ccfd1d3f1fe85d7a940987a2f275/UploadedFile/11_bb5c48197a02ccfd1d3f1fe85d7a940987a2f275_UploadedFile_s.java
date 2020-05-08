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
 package org.seasar.teeda.extension.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 /**
  * &lt;<input type="file"&gt; によってアップロードされたファイルを表現します。
  * <p>
  * このクラスは<a href="http://commons.apache.org/fileupload/">Apache Commons FileUpload</a>の
  * <code><a href="http://commons.apache.org/fileupload/apidocs/org/apache/commons/fileupload/FileItem.html">FileItem</a></code>クラスのラッパーです。
  * 
  * @author koichik
  */
public interface UploadedFile {
 
     /**
      * アップロードされたファイルの<strong>クライアント側</strong>ファイルシステム上のベースファイル名を返します。
      * <p>
      * 返されるベースファイル名にパスは含まれません。
      * </p>
      * 
      * @return アップロードされたファイルのクライアント側ファイルシステム上のベースファイル名
      */
     String getName();
 
     /**
      * アップロードされたファイルの<strong>クライアント側</strong>ファイルシステム上のファイル名を返します。
      * <p>
      * ブラウザによっては，返されるファイル名にはパスが含まれる場合があります。
      * </p>
      * 
      * @return アップロードされたファイルのクライアント側ファイルシステム上のベースファイル名
      */
     String getOriginalName();
 
     /**
      * アップロードされたファイルのコンテントタイプを返します。
      * 
      * @return アップロードされたファイルのコンテントタイプ
      */
     String getContentType();
 
     /**
      * アップロードされたファイルのサイズを返します。
      * 
      * @return アップロードされたファイルのサイズ
      */
     long getSize();
 
     /**
      * アップロードされたファイルがメモリ上にある場合は<code>true</code>を返します。
      * 
      * @return アップロードされたファイルがメモリ上にある場合は<code>true</code>
      */
     boolean isInMemory();
 
     /**
      * アップロードされたファイルの内容を含むバイト配列を返します。
      * <p>
      * アップロードされたファイルの読み込み中にIO例外が発生した場合は<code>null</code>が返されます。
      * </p>
      * 
      * @return アップロードされたファイルの内容を含むバイト配列、または<code>null</code>
      */
     byte[] get();
 
     /**
      * アップロードされたファイルの内容を文字列として返します。
      * <p>
      * アップロードされたファイルのコンテントタイプにエンコーディングが含まれていればそれが使われます。
      * コンテントタイプにエンコーディングが含まれていない場合はISO-8859-1が使われます。
      * </p>
      * <p>
      * アップロードされたファイルの読み込み中にIO例外が発生した場合は<code>null</code>が返されます。
      * </p>
      * 
      * @return アップロードされたファイルの内容の文字列、または<code>null</code>
      */
     String getString();
 
     /**
      * アップロードされたファイルの内容を指定のエンコーディングによる文字列として返します。
      * <p>
      * アップロードされたファイルの読み込み中にIO例外が発生した場合は<code>null</code>が返されます。
      * </p>
      * 
      * @param encoding エンコーディング
      * @return アップロードされたファイルの内容の文字列、または<code>null</code>
      * @throws UnsupportedEncodingException エンコーディングが不正な場合
      */
     String getString(String encoding) throws UnsupportedEncodingException;
 
     /**
      * アップロードされたファイルの内容を読み込むための入力ストリームを返します。
      * <p>
      * 返された入力ストリームは呼び出し側でクローズする必要があります。
      * </p>
      * 
      * @return アップロードされたファイルの内容を読み込むための{@link InputStream}
      * @throws IOException IO例外が発生した場合
      */
     InputStream getInputStream() throws IOException;
 
     /**
      * アップロードされたファイルの内容を指定のファイルに書き込みます。
      * 
      * @param file ファイル
      * @throws Exception ファイルの書込中に例外が発生した場合
      */
     void write(File file) throws Exception;
 
     /**
      * アップロードされたファイルを保存した<strong>サーバ側</strong>ファイルシステム上の{@link File}を返します。
      * <p>
      * {@link #isInMemory()}が<code>true</code>の場合、 このメソッドは<code>null</code>を返します。
      * </p>
      * 
      * @return アップロードされたファイルを保存したサーバ側ファイルシステム上の{@link File}、または<code>null</code>
      */
     File getStoreLocation();
 
     /**
      * アップロードされたファイルがサーバ側のファイルシステムに保存されている場合、それを削除します。
      */
     void delete();
 
 }
