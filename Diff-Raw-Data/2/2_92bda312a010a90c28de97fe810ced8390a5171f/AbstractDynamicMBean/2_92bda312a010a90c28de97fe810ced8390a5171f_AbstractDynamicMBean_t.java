 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.config;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.DynamicMBean;
 import javax.management.InvalidAttributeValueException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanConstructorInfo;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.MBeanOperationInfo;
 import javax.management.ReflectionException;
 
 /**
  * Export a Map<String, Object> as a DynamicMBean.  All values are made into Strings via
  * the toString() method.  All properties are read-only.
  */
 abstract class AbstractDynamicMBean implements DynamicMBean
 {
 
     private final Map<String, Object> attributeMap;
     private final MBeanInfo mbeanInfo;
 
     AbstractDynamicMBean(String name, Map<String, Object> attributeMap)
     {
         this.attributeMap = attributeMap;
 
         MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[attributeMap.size()];
         int i = 0;
         for (Entry<String, Object> entry : attributeMap.entrySet())
         {
             MBeanAttributeInfo attrib = new MBeanAttributeInfo(
                     entry.getKey(),
                    (entry.getValue() == null ? String.class : entry.getValue().getClass()).toString(),
                     "", true, false, false);
             attribs[i++] = attrib;
         }
 
         mbeanInfo = new MBeanInfo(name, "", attribs,
                 new MBeanConstructorInfo[0],
                 new MBeanOperationInfo[0],
                 new MBeanNotificationInfo[0]);
     }
 
     @Override
     public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
     {
         if (!attributeMap.containsKey(attribute))
         {
             throw new AttributeNotFoundException();
         }
         return attributeMap.get(attribute);
     }
 
     @Override
     public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
             MBeanException, ReflectionException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public AttributeList getAttributes(String[] attributes)
     {
         AttributeList result = new AttributeList(attributes.length);
         for (String attribute : attributes)
         {
             if (attributeMap.containsKey(attribute))
             {
                 result.add(new Attribute(attribute, attributeMap.get(attribute)));
             }
         }
         return result;
     }
 
     @Override
     public AttributeList setAttributes(AttributeList attributes)
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
             ReflectionException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public MBeanInfo getMBeanInfo()
     {
         return mbeanInfo;
     }
 }
