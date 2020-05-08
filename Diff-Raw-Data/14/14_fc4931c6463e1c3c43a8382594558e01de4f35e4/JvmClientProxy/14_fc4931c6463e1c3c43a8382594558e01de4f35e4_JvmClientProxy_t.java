 //
 // A Java VM status Watcher for Fluent
 //
 // Copyright (C) 2013 - 2013 Masayuki Miyake
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package org.fluentd.jvmwatcher.proxy;
 
 import static java.lang.management.ManagementFactory.CLASS_LOADING_MXBEAN_NAME;
 import static java.lang.management.ManagementFactory.COMPILATION_MXBEAN_NAME;
 import static java.lang.management.ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE;
 import static java.lang.management.ManagementFactory.MEMORY_MXBEAN_NAME;
 import static java.lang.management.ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE;
 import static java.lang.management.ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME;
 import static java.lang.management.ManagementFactory.RUNTIME_MXBEAN_NAME;
 import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
 import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;
 
 import java.io.IOException;
 import java.lang.management.ClassLoadingMXBean;
 import java.lang.management.CompilationMXBean;
 import java.lang.management.GarbageCollectorMXBean;
 import java.lang.management.MemoryMXBean;
 import java.lang.management.OperatingSystemMXBean;
 import java.lang.management.RuntimeMXBean;
 import java.lang.management.ThreadMXBean;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 import javax.management.IntrospectionException;
 import javax.management.InvalidAttributeValueException;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanOperationInfo;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fluentd.jvmwatcher.LocalJvmInfo;
 
 import com.sun.management.HotSpotDiagnosticMXBean;
 
 
 /**
  * This class manages the MXBens to obtain the status of the JVM. And I also do connection management of the JVM.<BR>
  * JvmClientProxy will correspond to only the local JVM.
  * @author miyake
  *
  */
 public class JvmClientProxy
 {
     private static  Log log = LogFactory.getLog(JvmClientProxy.class);
 
     /**
      * Java VM connection status.
      */
     private boolean             isConnect_ = false;
     /**
      * Target Java VM dead flag.
      */
     private boolean             isDeadServer_ = true;
     
     private boolean             hasPlatformMXBeans_ = false;
     private boolean             hasHotSpotDiagnosticMXBean_ = false;
     private boolean             hasCompilationMXBean_ = false;
     private boolean             supportsLockUsage_ = false;
 
     /**
      * Local Java VM information.
      */
     private LocalJvmInfo        localJvmInfo_ = null;
     
     // JVM server connection information
     private JMXServiceURL       jmxUrl_ = null;
     private JMXConnector        jmxc_ = null;
 
     // JVM MBean Server connection
     private MBeanServerConnection   server_ = null;
 
     // JMX Beans
     private ClassLoadingMXBean  classLoadingMBean_ = null;
     private CompilationMXBean   compilationMBean_ = null;
     private MemoryMXBean        memoryMBean_ = null;
     private OperatingSystemMXBean   operatingSystemMBean_ = null;
     private RuntimeMXBean       runtimeMBean_ = null;
     private ThreadMXBean        threadMBean_ = null;
 
     private com.sun.management.OperatingSystemMXBean    sunOperatingSystemMXBean_ = null;
     private HotSpotDiagnosticMXBean                     hotspotDiagnosticMXBean_ = null;
 
    private List<MemoryPoolClientProxy>                 memoryPoolList_ = null;
    private List<GarbageCollectorMXBean>                garbageCollectorMBeanList_ = null;
 
     final static private String HOTSPOT_DIAGNOSTIC_MXBEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
     
     
     /**
      * @param localJvmInfo
      */
     public JvmClientProxy(LocalJvmInfo localJvmInfo)
     {
         this.localJvmInfo_ = localJvmInfo;
     }
 
     /**
      * @return
      */
     public boolean connect()
     {
         if (this.localJvmInfo_ == null)
         {
             return false;
         }
 
         // connect the JVM server
         if (this.localJvmInfo_.isManageable() != true)
         {
             try
             {
                 // get JVM Agent address
                 this.localJvmInfo_.startManagementAgent();
             }    
             catch (IOException ex)
             {
                 log.error("startManagementAgent error.", ex);
                 return false;
             }
             catch (Exception ex)
             {
                 log.error("startManagementAgent error.", ex);
                 return false;
             }
         }
 
         // connect JVM MBean Server 
         try
         {
             if (this.jmxUrl_ == null)
             {
                 this.jmxUrl_ = new JMXServiceURL(this.localJvmInfo_.getAddress());
                 this.jmxc_ = JMXConnectorFactory.connect(jmxUrl_, null);
                 this.server_ = this.jmxc_.getMBeanServerConnection();
             }
         }
         catch (MalformedURLException ex)
         {
             log.error(" connect JVM MBean Server error.", ex);
             return false;
         }
         catch (IOException ex)
         {
             log.error(" connect JVM MBean Server error.", ex);
             return false;
         }
         catch (Exception ex)
         {
             log.error(" connect JVM MBean Server error.", ex);
             return false;
         }
 
         // this client have successfully connected to the JVM server.
         this.isDeadServer_ = false;
 
         try
         {
             ObjectName objName = new ObjectName(THREAD_MXBEAN_NAME);
             this.hasPlatformMXBeans_ = this.server_.isRegistered(objName);
             this.hasHotSpotDiagnosticMXBean_ = this.server_.isRegistered(new ObjectName(HOTSPOT_DIAGNOSTIC_MXBEAN_NAME));
             // check if it has 6.0 new APIs
             if (this.hasPlatformMXBeans_ = true)
             {
                 MBeanOperationInfo[] mopis = this.server_.getMBeanInfo(objName).getOperations();
                 // look for findDeadlockedThreads operations;
                 for (MBeanOperationInfo op : mopis)
                 {
                     if (op.getName().equals("findDeadlockedThreads"))
                     {
                         this.supportsLockUsage_ = true;
                         break;
                     }
                 }
 
                 objName = new ObjectName(COMPILATION_MXBEAN_NAME);
                 this.hasCompilationMXBean_ = this.server_.isRegistered(objName);
             }
 
             if (this.hasPlatformMXBeans_ == true) {
                 // WORKAROUND for bug 5056632
                 // Check if the access role is correct by getting a RuntimeMXBean
                 getRuntimeMXBean();
             }
         }
         catch (MalformedObjectNameException ex)
         {
             log.error("connect error.", ex);
             return false;
         }
         catch (IntrospectionException ex)
         {
             log.error("connect error.", ex);
             return false;
         }
         catch (InstanceNotFoundException ex)
         {
             log.error("connect error.", ex);
             return false;
         }
         catch (ReflectionException ex)
         {
             log.error("connect error.", ex);
             return false;
         }
         catch (IOException ex)
         {
             log.error("connect error.", ex);
             return false;
         }
         catch (Exception ex)
         {
             log.error("connect error.", ex);
             return false;
         }
 
         // connect success.
         this.isConnect_ = true;
 
         return true;
     }
     
     /**
      * @return
      */
     public boolean disconnect()
     {
         boolean     ret = false;
 
         // disconnect the JVM server
         // Close MBeanServer connection
         if (this.jmxc_ != null)
         {
             try
             {
                 this.jmxc_.close();
             }
             catch (IOException ex)
             {
                 // Ignore ???
                 log.warn("disconnect error.", ex);
             }
             finally
             {
                 this.jmxUrl_ = null;
                 this.server_ = null;
                 this.jmxc_ = null;
             }
         }
         // Reset platform MBean references
         this.classLoadingMBean_ = null;
         this.compilationMBean_ = null;
         this.memoryMBean_ = null;
         this.operatingSystemMBean_ = null;
         this.runtimeMBean_ = null;
         this.threadMBean_ = null;
         this.sunOperatingSystemMXBean_ = null;
         this.garbageCollectorMBeanList_ = null;
 
         // Set connection state to false
         if (this.isDeadServer_ == false) {
             this.isDeadServer_ = true;
             this.isConnect_ = false;
             ret = true;
         }
         return ret;
     }
     
     /**
      * @return
      */
     public boolean isConnect()
     {
         
         return this.isConnect_;
     }
 
     /**
      * @return
      */
     boolean isLockUsageSupported()
     {
         return this.supportsLockUsage_;
     }
     
     /**
      * @return
      * @throws IOException
      */
     public String[] getDomains() throws IOException
     {
         return this.server_.getDomains();
     }
 
     /**
      * @return
      */
     public LocalJvmInfo getLocalJvmInfo()
     {
         return this.localJvmInfo_;
     }
     
     /**
      * @param domain
      * @return
      * @throws IOException
      */
     public Map<ObjectName, MBeanInfo> getMBeans(String domain) throws IOException
     {
         ObjectName objName = null;
         if (domain != null)
         {
             try
             {
                 objName = new ObjectName(domain + ":*");
             } catch (MalformedObjectNameException e) {
                 // should not reach here
                 assert(false);
             }
         }
         Set<ObjectName>             mbeans = this.server_.queryNames(objName, null);
         Map<ObjectName,MBeanInfo>   result = new HashMap<ObjectName,MBeanInfo>(mbeans.size());
         Iterator<ObjectName>        iterator = mbeans.iterator();
         while (iterator.hasNext())
         {
             ObjectName  elem = iterator.next();
             try
             {
                 MBeanInfo info = this.server_.getMBeanInfo(elem);
                 result.put(elem, info);
             }
             catch (IntrospectionException ex)
             {
                 log.error("getMBeans error.", ex);
             }
             catch (InstanceNotFoundException ex)
             {
                 log.error("getMBeans error.", ex);
             }
             catch (ReflectionException ex)
             {
                 log.error("getMBeans error.", ex);
             }
         }
         return result;
     }
 
     /**
      * get a list of attributes of a named MBean.
      * 
      * @param name
      * @param attributes
      * @return
      * @throws IOException
      */
     public AttributeList getAttributes(ObjectName name, String[] attributes) throws IOException
     {
         AttributeList list = null;
         try
         {
             list = this.server_.getAttributes(name, attributes);
         }
         catch (InstanceNotFoundException ex)
         {
             // need to set up listener to listen for MBeanServerNotification.
             log.error("getAttributes error.", ex);
         }
         catch (ReflectionException ex)
         {
             log.error("getAttributes error.", ex);
         }
 
         return list;
     }
 
     /**
      * Set the value of a specific attribute of a named MBean.
      * 
      * @param name
      * @param attribute
      * @throws InvalidAttributeValueException
      * @throws MBeanException
      * @throws IOException
      */
     public void setAttribute(ObjectName name, Attribute attribute) throws InvalidAttributeValueException, MBeanException, IOException
     {
         try
         {
             this.server_.setAttribute(name, attribute);
         }
         catch (InstanceNotFoundException ex)
         {
             log.error("setAttribute error.", ex);
         }
         catch (AttributeNotFoundException ex)
         {
             log.error("setAttribute error.", ex);
             assert(false);
         }
         catch (ReflectionException ex)
         {
             log.error("setAttribute error.", ex);
         }
     }
 
     /**
      * @param name
      * @return
      * @throws IOException
      */
     public boolean isRegistered(ObjectName name) throws IOException
     {
         return this.server_.isRegistered(name);
     }
 
     /**
      * 
      * Invokes an operation of a named MBean.
      * 
      * @param name
      * @param operationName
      * @param params
      * @param signature
      * @return
      * @throws IOException
      * @throws MBeanException
      */
     public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws IOException, MBeanException
     {
         Object result = null;
         try
         {
             result = this.server_.invoke(name, operationName, params, signature);
         }
         catch (InstanceNotFoundException ex)
         {
             System.err.println(ex.toString());
         }
         catch (ReflectionException ex)
         {
             System.err.println(ex.toString());
         }
 
         return result;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized ClassLoadingMXBean getClassLoadingMXBean() throws IOException
     {
         if (this.hasPlatformMXBeans_ && this.classLoadingMBean_ == null)
         {
             this.classLoadingMBean_ = newPlatformMXBeanProxy(this.server_, CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
         }
 
         return this.classLoadingMBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized CompilationMXBean getCompilationMXBean() throws IOException
     {
         if (this.hasCompilationMXBean_ && this.compilationMBean_ == null)
         {
             this.compilationMBean_ = newPlatformMXBeanProxy(this.server_, COMPILATION_MXBEAN_NAME, CompilationMXBean.class);
         }
 
         return this.compilationMBean_;
     }
     
     /**
      * @return
      * @throws IOException
      */
     public synchronized MemoryMXBean getMemoryMXBean() throws IOException
     {
         if (this.hasCompilationMXBean_ && this.memoryMBean_ == null)
         {
             this.memoryMBean_ = newPlatformMXBeanProxy(this.server_, MEMORY_MXBEAN_NAME, MemoryMXBean.class);
         }
         return this.memoryMBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized RuntimeMXBean getRuntimeMXBean() throws IOException
     {
         if (this.hasPlatformMXBeans_ && this.runtimeMBean_ == null)
         {
             this.runtimeMBean_ = newPlatformMXBeanProxy(this.server_, RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
         }
         return this.runtimeMBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized ThreadMXBean getThreadMXBean() throws IOException
     {
         if (this.hasPlatformMXBeans_ && this.threadMBean_ == null)
         {
             this.threadMBean_ = newPlatformMXBeanProxy(this.server_, THREAD_MXBEAN_NAME, ThreadMXBean.class);
         }
         return this.threadMBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized OperatingSystemMXBean getOperatingSystemMXBean() throws IOException
     {
         if (this.hasPlatformMXBeans_ && this.operatingSystemMBean_ == null)
         {
             this.operatingSystemMBean_ = newPlatformMXBeanProxy(this.server_, OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
         }
         return this.operatingSystemMBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized com.sun.management.OperatingSystemMXBean getSunOperatingSystemMXBean() throws IOException
     {
         try
         {
             ObjectName objName = new ObjectName(OPERATING_SYSTEM_MXBEAN_NAME);
             if (this.sunOperatingSystemMXBean_ == null)
             {
                 if (this.server_.isInstanceOf(objName, "com.sun.management.OperatingSystemMXBean"))
                 {
                     this.sunOperatingSystemMXBean_ = newPlatformMXBeanProxy(this.server_, OPERATING_SYSTEM_MXBEAN_NAME, com.sun.management.OperatingSystemMXBean.class);
                 }
             }
         } 
         catch (InstanceNotFoundException ex)
         {
             log.error("com.sun.management.OperatingSystemMXBean get error.", ex);
              return null;
         }
         catch (MalformedObjectNameException ex)
         {
             log.error("com.sun.management.OperatingSystemMXBean get error.", ex);
              return null; // should never reach here
         }
 
         return this.sunOperatingSystemMXBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized HotSpotDiagnosticMXBean getHotSpotDiagnosticMXBean() throws IOException
     {
         if (this.hasHotSpotDiagnosticMXBean_ && this.hotspotDiagnosticMXBean_ == null)
         {
             this.hotspotDiagnosticMXBean_ = newPlatformMXBeanProxy(this.server_, HOTSPOT_DIAGNOSTIC_MXBEAN_NAME, HotSpotDiagnosticMXBean.class);
         }
 
         return this.hotspotDiagnosticMXBean_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public synchronized Collection<GarbageCollectorMXBean> getGarbageCollectorMXBeans() throws IOException
     {
         if (this.garbageCollectorMBeanList_ == null)
         {
             ObjectName  gcName = null;
 
             try
             {
                 gcName = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
             }
             catch (MalformedObjectNameException ex)
             {
                 log.error("GarbageCollectorMXBean get error.", ex);
                 return null;
             }
 
             Set<ObjectName> objNameSet = this.server_.queryNames(gcName, null);
 
             if (objNameSet != null)
             {
                 Iterator<ObjectName>    iterator = objNameSet.iterator();
 
                 while (iterator.hasNext())
                 {
                     ObjectName objName = iterator.next();
                     String name = GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name=" + objName.getKeyProperty("name");
 
                     GarbageCollectorMXBean mBean = newPlatformMXBeanProxy(this.server_, name, GarbageCollectorMXBean.class);
                     this.garbageCollectorMBeanList_.add(mBean);
                 }
             }
         }
 
         return this.garbageCollectorMBeanList_;
     }
 
     /**
      * @return
      * @throws IOException
      */
     public long[] findDeadlockedThreads() throws IOException
     {
         ThreadMXBean threadMx = getThreadMXBean();
         if (this.supportsLockUsage_ && threadMx.isSynchronizerUsageSupported())
         {
             return threadMx.findDeadlockedThreads();
         }
         else
         {
             return threadMx.findMonitorDeadlockedThreads();
         }
     }
     
     public Collection<MemoryPoolClientProxy> getMemoryPoolClientProxies() throws IOException
     {
 
         if (this.memoryPoolList_ == null)
         {
             ObjectName  poolName = null;
             try
             {
                 poolName = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*");
             }
             catch (MalformedObjectNameException ex)
             {
                 log.error("MemoryPoolClientProxy get error.", ex);
                 return null;
             }
 
             Set<ObjectName> objNameSet = this.server_.queryNames(poolName, null);
 
             if (objNameSet != null)
             {
                 Iterator<ObjectName>    iterator = objNameSet.iterator();
 
                 while (iterator.hasNext())
                 {
                     ObjectName objName = iterator.next();
                     MemoryPoolClientProxy   memoryPool = new MemoryPoolClientProxy(this);
 
                     if (memoryPool.init(objName) == true)
                     {
                         this.memoryPoolList_.add(memoryPool);
                     }
                 }
             }
         }
         
         return this.memoryPoolList_;
     }
     
     
     /**
      * @param objName
      * @param interfaceClass
      * @return
      * @throws IOException
      */
     public <T> T getMXBean(ObjectName objName, Class<T> interfaceClass) throws IOException
     {
         return newPlatformMXBeanProxy(this.server_, objName.toString(), interfaceClass);
     }
     
     
 }
