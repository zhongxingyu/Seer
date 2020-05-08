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
 package org.seasar.struts.bean;
 
 import java.beans.PropertyDescriptor;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.beanutils.PropertyUtilsBean;
 import org.apache.commons.collections.FastHashMap;
 
 /**
  * 特定のプロパティへのアクセスを抑制します。
  * 
  * @author nakamura-to
  *
  */
 public class SuppressPropertyUtilsBean extends PropertyUtilsBean {
 
    private static final List DEFAULT_PROPERTIES_TO_SUPPRESS = Arrays
            .asList(new String[] { "class", "declaringClass" });
 
     private final Collection propertiesToSuppress;
 
     private final FastHashMap descriptorsCache;
 
     /**
      * インスタンスを構築します。
      * <p>
     * このコンストラクタの呼び出しは、デフォルトで <code>class</code> と <code>declaringClass</code>
     * プロパティへのアクセスを抑制します。
      * </p>
      */
     public SuppressPropertyUtilsBean() {
        this(DEFAULT_PROPERTIES_TO_SUPPRESS);
     }
 
     /**
      * 抑制対象のプロパティを1つ指定してインスタンスを構築します。
      * 
      * @param propertyToSuppress
      *            抑制対象のプロパティ
      */
     public SuppressPropertyUtilsBean(String propertyToSuppress) {
         this(toCollection(propertyToSuppress));
     }
 
     /**
      * 抑制対象のプロパティのコレクションを指定してインスタンスを構築します。
      * 
      * @param propertiesToSuppress
      *            抑制対象のプロパティのコレクション
      */
     public SuppressPropertyUtilsBean(Collection propertiesToSuppress) {
         if (propertiesToSuppress == null) {
             this.propertiesToSuppress = Collections.EMPTY_SET;
         } else {
             this.propertiesToSuppress = Collections.unmodifiableCollection(new HashSet(propertiesToSuppress));
         }
         descriptorsCache = new FastHashMap();
         descriptorsCache.setFast(true);
     }
 
     private static Collection toCollection(String propertyToSuppress) {
         Set set = new HashSet();
         set.add(propertyToSuppress);
         return set;
     }
 
     public PropertyDescriptor[] getPropertyDescriptors(Class beanClass) {
         PropertyDescriptor[] descriptors = (PropertyDescriptor[]) descriptorsCache.get(beanClass);
         if (descriptors != null) {
             return descriptors;
         }
         descriptors = filter(super.getPropertyDescriptors(beanClass));
         descriptorsCache.put(beanClass, descriptors);
         return descriptors;
     }
 
     private PropertyDescriptor[] filter(PropertyDescriptor[] descriptors) {
         List validDescriptors = new ArrayList();
         for (int i = 0; i < descriptors.length; i++) {
             if (descriptors[i] == null) {
                 continue;
             }
             if (propertiesToSuppress.contains(descriptors[i].getName())) {
                 continue;
             }
             validDescriptors.add(descriptors[i]);
         }
         return (PropertyDescriptor[]) validDescriptors.toArray(new PropertyDescriptor[validDescriptors.size()]);
     }
 }
