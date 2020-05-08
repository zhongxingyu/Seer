 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
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
 
 import java.io.Externalizable;
 import java.io.Serializable;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 
 import org.seasar.framework.beans.PropertyDesc;
 
 /**
  * @author shot
  * @author higa
  */
 public class PagePersistenceUtil {
 
     public static boolean isPersistenceProperty(final PropertyDesc pd) {
         if (!pd.isReadable()) {
             return false;
         }
         if (!pd.hasReadMethod() &&
                 Modifier.isTransient(pd.getField().getModifiers())) {
             return false;
         }
         return isPersistenceType(pd.getPropertyType());
     }
 
     public static boolean isPersistenceType(final Class clazz) {
         if (clazz.isPrimitive()) {
             return true;
         }
         if (Serializable.class.isAssignableFrom(clazz)) {
             return true;
         }
         if (Externalizable.class.isAssignableFrom(clazz)) {
             return true;
         }
         if (clazz.isArray()) {
             final Class componentType = clazz.getComponentType();
             return isPersistenceType(componentType);
         }
         if (Collection.class.isAssignableFrom(clazz)) {
             return true;
         }
         return false;
     }
 
 }
