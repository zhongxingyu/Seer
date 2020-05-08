 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.management;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 
 import javax.management.Attribute;
 import javax.management.AttributeChangeNotification;
 import javax.management.AttributeChangeNotificationFilter;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.Descriptor;
 import javax.management.InvalidAttributeValueException;
 import javax.management.ListenerNotFoundException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.MBeanOperationInfo;
 import javax.management.MBeanRegistration;
 import javax.management.MBeanServer;
 import javax.management.NotCompliantMBeanException;
 import javax.management.Notification;
 import javax.management.NotificationBroadcasterSupport;
 import javax.management.NotificationFilter;
 import javax.management.NotificationListener;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.RuntimeOperationsException;
 import javax.management.StandardMBean;
 import javax.management.modelmbean.DescriptorSupport;
 import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
 import javax.management.modelmbean.ModelMBeanNotificationInfo;
 
 import org.apache.commons.beanutils.MethodUtils;
 import org.apache.commons.beanutils.PropertyUtilsBean;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * An MBean wrapper for an Existing Object
  * 
  * @version $Revision$
  */
 public class BaseStandardMBean extends StandardMBean implements ModelMBeanNotificationBroadcaster, MBeanRegistration,
                 PropertyChangeListener {
 
     private static final Log LOG = LogFactory.getLog(BaseStandardMBean.class);
 
     private static final Map<String, Class<?>> PRIMITIVE_CLASSES = new Hashtable<String, Class<?>>(8);
     {
         PRIMITIVE_CLASSES.put(Boolean.TYPE.toString(), Boolean.TYPE);
         PRIMITIVE_CLASSES.put(Character.TYPE.toString(), Character.TYPE);
         PRIMITIVE_CLASSES.put(Byte.TYPE.toString(), Byte.TYPE);
         PRIMITIVE_CLASSES.put(Short.TYPE.toString(), Short.TYPE);
         PRIMITIVE_CLASSES.put(Integer.TYPE.toString(), Integer.TYPE);
         PRIMITIVE_CLASSES.put(Long.TYPE.toString(), Long.TYPE);
         PRIMITIVE_CLASSES.put(Float.TYPE.toString(), Float.TYPE);
         PRIMITIVE_CLASSES.put(Double.TYPE.toString(), Double.TYPE);
     }
 
     protected ExecutorService executorService;
 
     private Map<String, CachedAttribute> cachedAttributes = new LinkedHashMap<String, CachedAttribute>();
 
     // used to maintain insertion ordering
     private PropertyUtilsBean beanUtil = new PropertyUtilsBean();
 
     private NotificationBroadcasterSupport broadcasterSupport = new NotificationBroadcasterSupport();
 
     private MBeanAttributeInfo[] attributeInfos;
 
     private MBeanInfo beanInfo;
 
     // this values are set after registering with the MBeanServer//
     private ObjectName objectName;
 
     private MBeanServer beanServer;
 
     /**
      * Constructor
      * 
      * @param object
      * @param interfaceMBean
      * @param description
      * @param attrs
      * @param ops
      * @param executorService2
      * @throws ReflectionException
      * @throws NotCompliantMBeanException
      */
    public BaseStandardMBean(Object object, Class interfaceMBean, String description, 
                              MBeanAttributeInfo[] attrs, MBeanOperationInfo[] ops,
                              ExecutorService executorService) throws ReflectionException, NotCompliantMBeanException {
         super(object, interfaceMBean);
         this.attributeInfos = attrs;
         buildAttributes(object, this.attributeInfos);
         this.beanInfo = new MBeanInfo(object.getClass().getName(), description, attrs, null, ops, getNotificationInfo());
         this.executorService = executorService;
     }
 
     /**
      * @return the MBeanINfo for this MBean
      */
     public MBeanInfo getMBeanInfo() {
         return beanInfo;
     }
 
     /**
      * Retrieve ObjectName of the MBean - set after registration
      * 
      * @return the ObjectName of the MBean
      */
     public ObjectName getObjectName() {
         return objectName;
     }
 
     /**
      * Retrieve the MBeanServer - set after registration
      * 
      * @return the beanServer
      */
     public MBeanServer getBeanServer() {
         return beanServer;
     }
 
     /**
      * Get the Value of an Attribute
      * 
      * @param name
      * @return the value of the Attribute
      * @throws AttributeNotFoundException
      * @throws MBeanException
      * @throws ReflectionException
      */
     public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
         Object result = null;
         CachedAttribute ca = cachedAttributes.get(name);
         if (ca == null) {
             // the use of proxies in MX4J has a bug - in which the caps can be
             // changed on
             // an attribute
             for (Map.Entry<String, CachedAttribute> entry : cachedAttributes.entrySet()) {
                 String key = entry.getKey();
                 if (key.equalsIgnoreCase(name)) {
                     ca = entry.getValue();
                     break;
                 }
             }
         }
         if (ca != null) {
             result = getCurrentValue(ca);
         } else {
             throw new AttributeNotFoundException("Could not locate " + name);
         }
         return result;
     }
 
     /**
      * Set the Attribute
      * 
      * @param attr
      * @throws AttributeNotFoundException
      * @throws InvalidAttributeValueException
      * @throws MBeanException
      * @throws ReflectionException
      */
     public void setAttribute(Attribute attr) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
                     ReflectionException {
         String name = attr.getName();
         CachedAttribute ca = cachedAttributes.get(name);
         if (ca != null) {
             Attribute old = ca.getAttribute();
             try {
                 ca.updateAttribute(beanUtil, attr);
                 sendAttributeChangeNotification(old, attr);
             } catch (NoSuchMethodException e) {
                 throw new ReflectionException(e);
             } catch (IllegalAccessException e) {
                 throw new ReflectionException(e);
             } catch (InvocationTargetException e) {
                 Throwable t = e.getTargetException();
                 if (t instanceof Exception) {
                     throw new MBeanException(e);
                 }
                 throw new MBeanException(e);
             }
         } else {
             throw new AttributeNotFoundException("Could not locate " + name);
         }
     }
 
     /**
      * Update a named attribute
      * 
      * @param name
      * @param value
      */
     public void updateAttribute(String name, Object value) {
         CachedAttribute ca = cachedAttributes.get(name);
         if (ca != null) {
             Attribute old = ca.getAttribute();
             ca.updateAttributeValue(value);
             try {
                 sendAttributeChangeNotification(old, ca.getAttribute());
             } catch (RuntimeOperationsException e) {
                 LOG.error("Failed to update attribute: " + name + " to new value: " + value, e);
             } catch (MBeanException e) {
                 LOG.error("Failed to update attribute: " + name + " to new value: " + value, e);
             }
         }
     }
 
     /**
      * Attribute change - fire notification
      * 
      * @param event
      */
     public void propertyChange(PropertyChangeEvent event) {
         updateAttribute(event.getPropertyName(), event.getNewValue());
     }
 
     /**
      * @param attributes -
      *            array of Attribute names
      * @return AttributeList of matching Attributes
      */
     public AttributeList getAttributes(String[] attributes) {
         AttributeList result = null;
         try {
             if (attributes != null) {
                 result = new AttributeList();
                 for (int i = 0; i < attributes.length; i++) {
                     CachedAttribute ca = cachedAttributes.get(attributes[i]);
                     ca.updateValue(beanUtil);
                     result.add(ca.getAttribute());
                 }
             } else {
                 // Do this to maintain insertion ordering
                 for (Map.Entry<String, CachedAttribute> entry : cachedAttributes.entrySet()) {
                     CachedAttribute ca = entry.getValue();
                     ca.updateValue(beanUtil);
                     result.add(ca.getAttribute());
                 }
             }
         } catch (MBeanException e) {
             LOG.error("Caught excdeption building attributes", e);
         }
         return result;
     }
 
     /**
      * Set values of Attributes
      * 
      * @param attributes
      * @return the list of Attributes set with their new values
      */
     public AttributeList setAttributes(AttributeList attributes) {
         AttributeList result = new AttributeList();
         if (attributes != null) {
             for (int i = 0; i < attributes.size(); i++) {
                 Attribute attribute = (Attribute) attributes.get(i);
                 try {
                     setAttribute(attribute);
                 } catch (AttributeNotFoundException e) {
                     LOG.warn("Failed to setAttribute(" + attribute + ")", e);
                 } catch (InvalidAttributeValueException e) {
                     LOG.warn("Failed to setAttribute(" + attribute + ")", e);
                 } catch (MBeanException e) {
                     LOG.warn("Failed to setAttribute(" + attribute + ")", e);
                 } catch (ReflectionException e) {
                     LOG.warn("Failed to setAttribute(" + attribute + ")", e);
                 }
                 result.add(attribute);
             }
         }
         return result;
     }
 
     /**
      * Invoke an operation
      * 
      * @param name
      * @param params
      * @param signature
      * @return result of invoking an operation
      * @throws MBeanException
      * @throws ReflectionException
      */
     public Object invoke(String name, Object[] params, String[] signature) throws MBeanException, ReflectionException {
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         try {
             Class<?>[] parameterTypes = new Class<?>[signature.length];
             for (int i = 0; i < parameterTypes.length; i++) {
                 parameterTypes[i] = PRIMITIVE_CLASSES.get(signature[i]);
                 if (parameterTypes[i] == null) {
                     parameterTypes[i] = Class.forName(signature[i]);
                 }
             }
             Thread.currentThread().setContextClassLoader(getImplementation().getClass().getClassLoader());
             return MethodUtils.invokeMethod(getImplementation(), name, params, parameterTypes);
         } catch (ClassNotFoundException e) {
             throw new ReflectionException(e);
         } catch (NoSuchMethodException e) {
             throw new ReflectionException(e);
         } catch (IllegalAccessException e) {
             throw new ReflectionException(e);
         } catch (InvocationTargetException e) {
             Throwable t = e.getTargetException();
             if (t instanceof Exception) {
                 throw new MBeanException((Exception) t);
             } else {
                 throw new MBeanException(e);
             }
         } finally {
             Thread.currentThread().setContextClassLoader(oldCl);
         }
     }
 
     /**
      * Called at registration
      * 
      * @param mbs
      * @param on
      * @return the ObjectName
      * @throws Exception
      */
     public ObjectName preRegister(MBeanServer mbs, ObjectName on) throws Exception {
         if (mbs != null) {
             this.beanServer = mbs;
         }
         if (on != null) {
             this.objectName = on;
         }
         return on;
     }
 
     /**
      * Caled post registration
      * 
      * @param done
      */
     public void postRegister(Boolean done) {
     }
 
     /**
      * Called before removal from the MBeanServer
      * 
      * @throws Exception
      */
     public void preDeregister() throws Exception {
     }
 
     /**
      * Called after removal from the MBeanServer
      */
     public void postDeregister() {
     }
 
     /**
      * @param notification
      * @throws MBeanException
      * @throws RuntimeOperationsException
      */
     public void sendNotification(final Notification notification) throws MBeanException, RuntimeOperationsException {
         if (notification != null && !executorService.isShutdown()) {
             executorService.execute(new Runnable() {
                 public void run() {
                     broadcasterSupport.sendNotification(notification);
                 }
             });
         }
     }
 
     /**
      * @param text
      * @throws MBeanException
      * @throws RuntimeOperationsException
      */
     public void sendNotification(String text) throws MBeanException, RuntimeOperationsException {
         if (text != null) {
             Notification myNtfyObj = new Notification("jmx.modelmbean.generic", this, 1, text);
             sendNotification(myNtfyObj);
         }
     }
 
     /**
      * @param l
      * @param attrName
      * @param handback
      * @throws MBeanException
      * @throws RuntimeOperationsException
      * @throws IllegalArgumentException
      */
     public void addAttributeChangeNotificationListener(NotificationListener l, String attrName, Object handback) throws MBeanException,
                     RuntimeOperationsException, IllegalArgumentException {
         AttributeChangeNotificationFilter currFilter = new AttributeChangeNotificationFilter();
         currFilter.enableAttribute(attrName);
         broadcasterSupport.addNotificationListener(l, currFilter, handback);
     }
 
     /**
      * @param l
      * @param attrName
      * @throws MBeanException
      * @throws RuntimeOperationsException
      * @throws ListenerNotFoundException
      */
     public void removeAttributeChangeNotificationListener(NotificationListener l, String attrName) throws MBeanException,
                     RuntimeOperationsException, ListenerNotFoundException {
         broadcasterSupport.removeNotificationListener(l);
     }
 
     /**
      * @param notification
      * @throws MBeanException
      * @throws RuntimeOperationsException
      */
     public void sendAttributeChangeNotification(AttributeChangeNotification notification) throws MBeanException, 
                                                                                                  RuntimeOperationsException {
         sendNotification(notification);
     }
 
     /**
      * @param oldAttr
      * @param newAttr
      * @throws MBeanException
      * @throws RuntimeOperationsException
      */
     public void sendAttributeChangeNotification(Attribute oldAttr, Attribute newAttr) throws MBeanException, RuntimeOperationsException {
         if (!oldAttr.equals(newAttr)) {
             AttributeChangeNotification notification = new AttributeChangeNotification(objectName, 1, (new Date()).getTime(),
                             "AttributeChange", oldAttr.getName(), newAttr.getValue().getClass().toString(), oldAttr.getValue(),
                             newAttr.getValue());
             sendAttributeChangeNotification(notification);
         }
     }
 
     /**
      * @return notificationInfo
      */
     public final MBeanNotificationInfo[] getNotificationInfo() {
         MBeanNotificationInfo[] result = new MBeanNotificationInfo[2];
         Descriptor genericDescriptor = new DescriptorSupport(new String[] {
             "name=GENERIC", "descriptorType=notification", "log=T",
             "severity=5", "displayName=jmx.modelmbean.generic" });
         result[0] = new ModelMBeanNotificationInfo(new String[] {"jmx.modelmbean.generic" }, "GENERIC",
             "A text notification has been issued by the managed resource", genericDescriptor);
         Descriptor attributeDescriptor = new DescriptorSupport(new String[] {"name=ATTRIBUTE_CHANGE", "descriptorType=notification",
             "log=T", "severity=5", "displayName=jmx.attribute.change" });
         result[1] = new ModelMBeanNotificationInfo(new String[] {"jmx.attribute.change" }, "ATTRIBUTE_CHANGE",
             "Signifies that an observed MBean attribute value has changed", attributeDescriptor);
         return result;
     }
 
     /**
      * @param l
      * @param filter
      * @param handle
      * @throws IllegalArgumentException
      */
     public void addNotificationListener(NotificationListener l, NotificationFilter filter, 
                                         Object handle) throws IllegalArgumentException {
         broadcasterSupport.addNotificationListener(l, filter, handle);
     }
 
     /**
      * @param l
      * @throws ListenerNotFoundException
      */
     public void removeNotificationListener(NotificationListener l) throws ListenerNotFoundException {
         broadcasterSupport.removeNotificationListener(l);
     }
 
     private Object getCurrentValue(CachedAttribute ca) throws MBeanException {
         Object result = null;
         if (ca != null) {
             try {
                 result = beanUtil.getProperty(ca.getBean(), ca.getName());
             } catch (IllegalAccessException e) {
                 throw new MBeanException(e);
             } catch (InvocationTargetException e) {
                 throw new MBeanException(e);
             } catch (NoSuchMethodException e) {
                 throw new MBeanException(e);
             }
         }
         return result;
     }
 
     /**
      * build internal Map of CachedAttributes
      * 
      * @param obj
      * @param attrs
      * @throws ReflectionException
      */
     private void buildAttributes(Object obj, MBeanAttributeInfo[] attrs) throws ReflectionException {
         if (attrs != null) {
             for (int i = 0; i < attrs.length; i++) {
                 try {
                     String name = attrs[i].getName();
                     PropertyDescriptor pd = beanUtil.getPropertyDescriptor(obj, name);
                     Object value = beanUtil.getProperty(obj, name);
                     Attribute attribute = new Attribute(name, value);
                     CachedAttribute ca = new CachedAttribute(attribute);
                     ca.setBean(obj);
                     ca.setPropertyDescriptor(pd);
                     cachedAttributes.put(name, ca);
                 } catch (NoSuchMethodException e) {
                     throw new ReflectionException(e);
                 } catch (IllegalAccessException e) {
                     throw new ReflectionException(e);
                 } catch (InvocationTargetException e) {
                     throw new ReflectionException(e);
                 }
             }
         }
     }
 }
