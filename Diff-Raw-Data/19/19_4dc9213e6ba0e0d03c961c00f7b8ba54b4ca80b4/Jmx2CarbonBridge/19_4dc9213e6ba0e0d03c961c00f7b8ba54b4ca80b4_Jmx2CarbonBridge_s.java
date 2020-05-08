 /*****************************************************************************************
  * *** BEGIN LICENSE BLOCK *****
  *
  * Version: MPL 2.0
  *
  * echocat JeMoni, Copyright (c) 2012 echocat
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  *
  * *** END LICENSE BLOCK *****
  ****************************************************************************************/
 
 package org.echocat.jemoni.carbon.jmx;
 
 import org.echocat.jemoni.carbon.CarbonWriter;
import org.echocat.jemoni.carbon.jmx.configuration.Rule;
 import org.echocat.jemoni.carbon.jmx.configuration.Configuration;
 import org.echocat.jemoni.jmx.JmxRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.management.*;
 import javax.management.openmbean.*;
 import javax.management.relation.MBeanServerNotificationFilter;
 import java.lang.management.ManagementFactory;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import static java.lang.Boolean.TRUE;
 import static java.lang.Thread.currentThread;
 import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
 import static java.util.regex.Pattern.compile;
 import static javax.management.MBeanServerDelegate.DELEGATE_NAME;
 import static javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION;
 import static javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION;
 import static org.apache.commons.lang3.StringUtils.join;
 
 public class Jmx2CarbonBridge implements AutoCloseable {
 
     private static final Pattern NAME_REPLACE_PATTERN = compile("[,:][^=]+\\=");
     private static final Logger LOG = LoggerFactory.getLogger(Jmx2CarbonBridge.class);
 
     protected static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();
 
     private final NotificationListener _mbeanIndexChangeListener = new NotificationListener() { @Override public void handleNotification(Notification notification, Object handback) {
         final MBeanServerNotification mbs = (MBeanServerNotification) notification;
          if(REGISTRATION_NOTIFICATION.equals(mbs.getType()) || UNREGISTRATION_NOTIFICATION.equals(mbs.getType())) {
              try {
                  updateMBeanIndex();
              } catch (Exception e) {
                  LOG.warn("Could not update index.", e);
              }
          }
     }};
     private final MBeanServerNotificationFilter _notificationFilter = new MBeanServerNotificationFilter();
     private final MBeanServer _server;
     private final CarbonWriter _carbonWriter;
     private final Set<Thread> _updatingThreads = new HashSet<>();
 
     private ClassLoader _classLoader = currentThread().getContextClassLoader();
     private Configuration _configuration;
     private String _pathPrefix = getLocalhost() + ".";
 
     @Nonnull
     protected static String getLocalhost() {
         String result = "localhost";
         try {
             result = InetAddress.getLocalHost().getCanonicalHostName();
         } catch (UnknownHostException ignored) {}
         final List<String> hostParts = new ArrayList<>(asList(result.split("\\.")));
         Collections.reverse(hostParts);
         return join(hostParts, ".");
     }
 
     public Jmx2CarbonBridge(@Nonnull CarbonWriter carbonWriter) {
         this(SERVER, carbonWriter);
     }
 
     public Jmx2CarbonBridge(@Nonnull JmxRegistry jmxRegistry, @Nonnull CarbonWriter carbonWriter) {
         this(jmxRegistry.getServer(), carbonWriter);
     }
 
     public Jmx2CarbonBridge(@Nonnull MBeanServer server, @Nonnull CarbonWriter carbonWriter) {
         _server = server;
         _carbonWriter = carbonWriter;
         _notificationFilter.enableAllObjectNames();
     }
 
     public ClassLoader getClassLoader() {
         return _classLoader;
     }
 
     public void setClassLoader(ClassLoader classLoader) {
         _classLoader = classLoader;
     }
 
     public Configuration getConfiguration() {
         return _configuration;
     }
 
     public void setConfiguration(Configuration configuration) {
         _configuration = configuration;
     }
 
     public String getPathPrefix() {
         return _pathPrefix;
     }
 
     public void setPathPrefix(String pathPrefix) {
         _pathPrefix = pathPrefix;
     }
 
     @Nonnull
     public CarbonWriter getCarbonWriter() {
         return _carbonWriter;
     }
 
     @PostConstruct
     public void init() throws Exception {
         _server.addNotificationListener(DELEGATE_NAME, _mbeanIndexChangeListener, _notificationFilter, null);
         updateMBeanIndex();
     }
 
     public void updateMBeanIndex() throws Exception {
         final Configuration configuration = _configuration;
         synchronized (this) {
             final Map<Rule, Set<AttributeDefinitions>> ruleToAttributeNames = new HashMap<>();
             if (configuration != null && configuration.hasItems()) {
                 final Set<ObjectName> objectNames = _server.queryNames(null, null);
                 for (ObjectName objectName : objectNames) {
                     try {
                         final MBeanInfo mBeanInfo = _server.getMBeanInfo(objectName);
                         final MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
 
                         for (Rule rule : configuration) {
                             final Set<AttributeDefinition> singleAttributeDefinitions = new HashSet<>();
                             for (MBeanAttributeInfo attribute : attributes) {
                                 final AttributeDefinition attributeDefinition = findDefinitionFor(objectName, attribute);
                                 if (rule.apply(attributeDefinition)) {
                                     singleAttributeDefinitions.add(attributeDefinition);
                                 }
                             }
                             if (!singleAttributeDefinitions.isEmpty()) {
                                 Set<AttributeDefinitions> attributeNames = ruleToAttributeNames.get(rule);
                                 if (attributeNames == null) {
                                     attributeNames = new HashSet<>();
                                     ruleToAttributeNames.put(rule, attributeNames);
                                 }
                                 attributeNames.add(new AttributeDefinitions(objectName, singleAttributeDefinitions));
                             }
                         }
                     } catch (InstanceNotFoundException ignored) {}
                 }
             }
             startThreads(configuration, ruleToAttributeNames);
         }
     }
 
     @Nullable
     protected AttributeDefinition findDefinitionFor(@Nonnull ObjectName objectName, @Nonnull MBeanAttributeInfo info) {
         final Descriptor descriptor = info.getDescriptor();
         final OpenType<?> openType = (OpenType<?>) descriptor.getFieldValue("openType");
         return findDefinitionFor(objectName, info, info.getName(), openType);
     }
 
     @Nullable
     protected AttributeDefinition findDefinitionFor(@Nonnull ObjectName objectName, @Nonnull MBeanAttributeInfo info, @Nonnull String name, @Nullable OpenType<?> openType) {
         final AttributeDefinition result;
         if (openType instanceof SimpleType) {
             result = findDefinitionFor(objectName, name, (SimpleType<?>) openType);
         } else if (openType instanceof CompositeType) {
             result = findDefinitionFor(objectName, info, name, (CompositeType) openType);
         } else {
             final String typeName = info.getType();
             if (typeName != null && typeName.startsWith("java.")) {
                 final Class<?> type = tryLoadClassBy(typeName);
                 if (type != null && (Number.class.isAssignableFrom(type) || Boolean.class.equals(type) || Character.class.equals(type))) {
                     result = new AttributeDefinition(objectName, name, type);
                 } else {
                     result = null;
                 }
             } else {
                 result = null;
             }
         }
         return result;
     }
 
     @Nullable
     protected AttributeDefinition findDefinitionFor(@Nonnull ObjectName objectName, @Nonnull String name, @Nonnull SimpleType<?> simpleType) {
         final AttributeDefinition result;
         final Class<?> type = tryLoadClassFor(simpleType);
         if (type != null && Number.class.isAssignableFrom(type)) {
             result = new AttributeDefinition(objectName, name, type);
         } else {
             result = null;
         }
         return result;
     }
 
     @Nullable
     protected AttributeDefinition findDefinitionFor(@Nonnull ObjectName objectName, @Nonnull MBeanAttributeInfo info, @Nonnull String name, @Nonnull CompositeType compositeType) {
         final Set<AttributeDefinition> children = new HashSet<>();
         for (String key : compositeType.keySet()) {
             final OpenType<?> childType = compositeType.getType(key);
             final AttributeDefinition child = findDefinitionFor(objectName, info, key, childType);
             if (child != null) {
                 children.add(child);
             }
         }
         return children.isEmpty() ? null : new AttributeDefinition(objectName, name, CompositeData.class, children);
     }
 
     @Nullable
     protected Class<?> tryLoadClassFor(@Nullable SimpleType<?> simpleType) {
         return tryLoadClassBy(simpleType != null ? simpleType.getClassName() : null);
     }
 
     @Nullable
     protected Class<?> tryLoadClassBy(@Nullable String className) {
         Class<?> result;
         if (className == null) {
             result = null;
         } else {
             try {
                 result = _classLoader.loadClass(className);
             } catch (ClassNotFoundException ignored) {
                 result = null;
             }
         }
         return result;
     }
 
     protected void startThreads(@Nonnull Configuration configuration, @Nonnull Map<Rule, Set<AttributeDefinitions>> ruleToAttributeNames) {
         synchronized (this) {
             stopThreads();
             for (Entry<Rule, Set<AttributeDefinitions>> ruleAndAttributeNames : ruleToAttributeNames.entrySet()) {
                 final Rule rule = ruleAndAttributeNames.getKey();
                 final Worker worker = new Worker(configuration, rule, ruleAndAttributeNames.getValue());
                 final Thread thread = new Thread(worker, worker.toString());
                 thread.start();
                 _updatingThreads.add(thread);
             }
         }
     }
 
     protected void stopThreads() {
         synchronized (this) {
             final Iterator<Thread> i = _updatingThreads.iterator();
             while (i.hasNext()) {
                 final Thread thread = i.next();
                stopThread(thread);
                 i.remove();
             }
         }
     }
 
     protected void stopThread(@Nullable Thread thread) {
         if (thread != null) {
             thread.interrupt();
             try {
                 while (thread.isAlive()) {
                    thread.join(SECONDS.toMillis(10));
                    if (thread.isAlive()) {
                         LOG.info("Still wait for termination of '" + thread + "'...");
                     }
                 }
             } catch (InterruptedException ignored) {
                 currentThread().interrupt();
                 LOG.debug("Could not wait for termination of '" + thread + "' - but this thread was interrupted.");
             }
         }
     }
 
     @Override
     @PreDestroy
     public void close() throws Exception {
         try {
             _server.removeNotificationListener(DELEGATE_NAME, _mbeanIndexChangeListener, _notificationFilter, null);
         } finally {
             stopThreads();
         }
     }
 
     protected class Worker implements Runnable {
 
         @SuppressWarnings("InnerClassFieldHidesOuterClassField")
         private final Configuration _configuration;
         private final Rule _rule;
         private final Set<AttributeDefinitions> _attributeNames;
 
         public Worker(@Nonnull Configuration configuration, @Nonnull Rule rule, @Nonnull Set<AttributeDefinitions> attributeNames) {
             _configuration = configuration;
             _rule = rule;
             _attributeNames = attributeNames;
         }
 
         @Override
         public void run() {
             try {
                 while (!currentThread().isInterrupted()) {
                     if (_carbonWriter.getAddress() != null) {
                         for (AttributeDefinitions attributeDefinitions : _attributeNames) {
                             final ObjectName objectName = attributeDefinitions.getObjectName();
                             final Iterator<AttributeDefinition> i = attributeDefinitions.iterator();
                             while (i.hasNext()) {
                                 final AttributeDefinition definition = i.next();
                                 try {
                                     final Map<String, Object> keyToValue = findValuesOf(definition);
                                     for (Entry<String, Object> keyAndValue : keyToValue.entrySet()) {
                                         final String key = keyAndValue.getKey();
                                         final Object value = keyAndValue.getValue();
                                         if (value instanceof Number) {
                                             _carbonWriter.write(getPathFor(objectName, key, definition), (Number) value);
                                         }
                                     }
                                 } catch (InstanceNotFoundException ignored) {
                                     i.remove();
                                 } catch (Exception e) {
                                     if (!(e instanceof RuntimeMBeanException) || !(e.getCause() instanceof UnsupportedOperationException)) {
                                         LOG.warn("Could not read value of " + objectName + "->" + definition + ". This attribute will ignored from now.", e);
                                     }
                                     i.remove();
                                 }
                             }
                         }
                     }
                     _rule.getUpdateEvery().sleep();
                 }
             } catch (InterruptedException ignored) {
                 currentThread().interrupt();
             }
         }
 
         @Nonnull
         protected String getPathFor(@Nonnull ObjectName objectName, @Nonnull String key, @Nonnull AttributeDefinition definition) {
             final StringBuilder sb = new StringBuilder();
             final String pathPrefix = _pathPrefix;
             if (pathPrefix != null) {
                 sb.append(pathPrefix);
             }
             sb.append(getNormalizedNameFor(objectName)).append('.').append(key);
             return _configuration.format(definition, sb.toString());
         }
 
         @Nonnull
         protected String getNormalizedNameFor(@Nonnull ObjectName objectName) {
             final String nameWithoutDots = objectName.toString().replace('.', '_');
             return NAME_REPLACE_PATTERN.matcher(nameWithoutDots).replaceAll(".");
         }
 
         @Nonnull
         protected Map<String, Object> findValuesOf(@Nonnull AttributeDefinition definition) throws Exception {
             return findValuesOf(definition, null, null);
         }
 
         @Nonnull
         protected Map<String, Object> findValuesOf(@Nonnull AttributeDefinition definition, @Nullable String namePrefix, @Nullable Object parent) throws Exception {
             final Map<String, Object> nameToValue = new HashMap<>();
             final Object value = findValueOf(definition, parent);
             if (value instanceof CompositeData) {
                 final Set<AttributeDefinition> children = definition.getChildren();
                 if (children != null) {
                     final String newNamePrefix = getNameFor(definition, namePrefix);
                     for (AttributeDefinition child : children) {
                         nameToValue.putAll(findValuesOf(child, newNamePrefix, value));
                     }
                 }
             } else if (value instanceof Number) {
                 nameToValue.put(getNameFor(definition, namePrefix), value);
             } else if (value instanceof Character) {
                 // noinspection RedundantCast
                 nameToValue.put(getNameFor(definition, namePrefix), (int)((Character)value));
             } else if (value instanceof Boolean) {
                 nameToValue.put(getNameFor(definition, namePrefix), TRUE.equals(value) ? 1 : 0);
             }
             return nameToValue;
         }
 
         @Nullable
         protected Object findValueOf(@Nonnull AttributeDefinition definition, @Nullable Object parent) throws Exception {
             Object result;
             try {
                 if (parent == null) {
                     result = _server.getAttribute(definition.getObjectName(), definition.getName());
                 } else if (parent instanceof CompositeData) {
                     final CompositeData compositeData = (CompositeData) parent;
                     try {
                         result = compositeData.get(definition.getName());
                     } catch (InvalidKeyException ignored) {
                         result = null;
                     }
                 } else {
                     throw new UnsupportedOperationException();
                 }
             } catch (AttributeNotFoundException ignored) {
                 result = null;
             }
             return result != null && definition.getValueType().isInstance(result) ? result : null;
         }
 
         @Nullable
         protected String getNameFor(@Nonnull AttributeDefinition definition, @Nullable String namePrefix) {
             final StringBuilder sb = new StringBuilder();
             if (namePrefix != null) {
                 sb.append(namePrefix).append('.');
             }
             sb.append(definition.getName());
             return sb.toString();
         }
 
         @Override
         public String toString() {
             return Jmx2CarbonBridge.this.getClass().getSimpleName() + "." + getClass().getSimpleName() + "{" + _rule.getName() + "}";
         }
     }
 }
