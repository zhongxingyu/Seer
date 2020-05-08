 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
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
 
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 
 /**
  * @author koichik
  */
 public class TeedaExtensionConfiguration {
 
     private static final TeedaExtensionConfiguration DEFAULT_INSTANCE = new TeedaExtensionConfiguration();
 
     /**
      * outputTextを&lt;span&gt;要素にのみマッピングする場合 (Teeda 1.0.12 以前と互換) は<code>true</code>を設定します。
      */
     public boolean outputTextSpanOnly;
 
     /**
      * id属性が"～Label"のoutputTextをラベルにマッピングするのは&lt;a&gt;要素の直下の&lt;span&gt;要素に限定する場合 (Teeda 1.0.12 以前と互換) は<code>true</code>を設定します。
      */
     public boolean outputTextLabelUnderAnchorOnly;
 
    /**
     * &lt;label&gt;要素でプロパティファイルの内容を出力するためのファクトリを無効にする場合は<code>true</code>を設定します。
     */
    public boolean disableLabelFactory;

     public static TeedaExtensionConfiguration getInstance() {
         final S2Container container = SingletonS2ContainerFactory
                 .getContainer();
         if (!container.hasComponentDef(TeedaExtensionConfiguration.class)) {
             return DEFAULT_INSTANCE;
         }
         return (TeedaExtensionConfiguration) container
                 .getComponent(TeedaExtensionConfiguration.class);
     }
 
 }
