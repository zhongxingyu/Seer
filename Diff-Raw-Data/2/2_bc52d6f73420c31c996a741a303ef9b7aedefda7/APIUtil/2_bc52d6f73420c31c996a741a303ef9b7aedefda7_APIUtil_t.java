 /*
  * APIUtil.java
  * Copyright (C) 2011,2012 Wannes De Smet
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.xenmaster.api.util;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.net.InetAddress;
 import java.util.Map;
 import java.util.UUID;
 import net.wgr.core.ReflectionUtils;
 import org.apache.log4j.Logger;
 import org.xenmaster.api.entity.XenApiEntity;
 
 /**
  *
  * @created Dec 14, 2011
  * @author double-u
  */
 public class APIUtil {
 
     public static Object deserializeToTargetType(Object value, Class type) throws Exception {
         switch (type.getSimpleName().toLowerCase()) {
             case "boolean":
                 return Boolean.parseBoolean(value.toString());
             case "integer":
             case "int":
                 return Integer.parseInt(value.toString());
             case "long":
                 return Long.parseLong(value.toString());
             case "double":
                 return Double.parseDouble(value.toString());
             default:
                 if (type.isEnum()) {
                     String ucase = value.toString().toUpperCase();
                     for (Object enumType : type.getEnumConstants()) {
                         if (enumType.toString().toUpperCase().equals(ucase)) {
                             return enumType;
                         }
                     }
 
                     throw new IllegalArgumentException("Argument value does not belong to enum values of " + type.getCanonicalName());
                 } else if (type.isArray()) {
                     // FIXME : This can break all too easily
                     Class t = type.getComponentType();
                     Object[] src = (Object[]) value;
                     Object[] arr = (Object[]) Array.newInstance(t, src.length);
                     for (int i = 0; i < src.length; i++) {
                         arr[i] = deserializeToTargetType(src[i], t);
                     }
                     return arr;
                 } else if (InetAddress.class.isAssignableFrom(type)) {
                     return InetAddress.getByName(value.toString());
                 } else if (XenApiMapField.class.isAssignableFrom(type)) {
                     Constructor c = type.getConstructor(Map.class);
                     return c.newInstance((Map<String, String>) value);
                 } else if (XenApiEntity.class.isAssignableFrom(type)) {
                     Constructor c = type.getConstructor(String.class, boolean.class);
                     return c.newInstance(value.toString(), false);
                 } else if (UUID.class.isAssignableFrom(type)) {
                     return UUID.fromString(value.toString());
                } else if (value != null && Map.class.isAssignableFrom(value.getClass())) {
                     Logger.getLogger(APIUtil.class).info("New transformer used");
                     Object instance = type.newInstance();
                     Map<String, Object> source = (Map<String, Object>) value;
 
                     for (Map.Entry<String, Object> entry : source.entrySet()) {
                         for (Field f : ReflectionUtils.getAllFields(type)) {
                             if (f.getName().equals(entry.getKey())) {
                                 f.setAccessible(true);
                                 f.set(instance, deserializeToTargetType(entry.getValue(), f.getType()));
                                 break;
                             }
                         }
                     }
                     
                     return instance;
                 }
                 break;
         }
         return value;
     }
 }
