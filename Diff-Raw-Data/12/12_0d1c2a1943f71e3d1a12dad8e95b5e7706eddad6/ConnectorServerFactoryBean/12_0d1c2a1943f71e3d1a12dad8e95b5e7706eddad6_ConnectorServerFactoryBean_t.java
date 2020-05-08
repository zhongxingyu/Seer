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
 package org.apache.servicemix.jbi.jmx;
 
 import java.util.Map;
 
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.Constants;
 
 /**
  * <code>FactoryBean</code> that creates a JSR-160 <code>JMXConnectorServer</code>,
  * optionally registers it with the <code>MBeanServer</code> and then starts it.
  *
  * <p>The <code>JMXConnectorServer</code> can be started in a separate thread by setting the
  * <code>threaded</code> property to <code>true</code>. You can configure this thread to be a
  * daemon thread by setting the <code>daemon</code> property to <code>true</code>.
  *
  * This xbean-enabled factory is a wrapper on top of the existing Spring
  * factory bean.  It also logs the serviceUrl when starting.
  * 
  * @author gnodet
  * @org.apache.xbean.XBean element="jmxConnector"
  */
 public class ConnectorServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
 
     /**
      * Constant indicating that registration should fail when
      * attempting to register an MBean under a name that already exists.
      * <p>This is the default registration behavior.
      */
     public static final int REGISTRATION_FAIL_ON_EXISTING = 0;
 
     /**
      * Constant indicating that registration should ignore the affected MBean
      * when attempting to register an MBean under a name that already exists.
      */
     public static final int REGISTRATION_IGNORE_EXISTING = 1;
 
     /**
      * Constant indicating that registration should replace the affected MBean
      * when attempting to register an MBean under a name that already exists.
      */
     public static final int REGISTRATION_REPLACE_EXISTING = 2;
 
 
     private Log log = LogFactory.getLog(ConnectorServerFactoryBean.class);
     private org.springframework.jmx.support.ConnectorServerFactoryBean csfb = new org.springframework.jmx.support.ConnectorServerFactoryBean();
     private String serviceUrl = org.springframework.jmx.support.ConnectorServerFactoryBean.DEFAULT_SERVICE_URL;
     private boolean daemon = false;
     private boolean threaded = false;
     private Map environment;
    private Object objectName;
     private int registrationBehavior = REGISTRATION_FAIL_ON_EXISTING;
     private MBeanServer server;
     private static final Constants constants = new Constants(ConnectorServerFactoryBean.class);
     
 
     /**
      * Set whether any threads started for the <code>JMXConnectorServer</code> should be
      * started as daemon threads.
      * @param daemon
      * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setDaemon(boolean)
      */
     public void setDaemon(boolean daemon) {
         this.daemon = daemon;
     }
 
     /**
      * Set the environment properties used to construct the <code>JMXConnector</code>
      * as a <code>Map</code> of String keys and arbitrary Object values.
      * @param environment
      * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setEnvironmentMap(java.util.Map)
      */
     public void setEnvironment(Map environment) {
         this.environment = environment;
     }
 
     /**
      * Set the <code>ObjectName</code> used to register the <code>JMXConnectorServer</code>
      * itself with the <code>MBeanServer</code>.
      * @param objectName
      * @throws MalformedObjectNameException if the <code>ObjectName</code> is malformed
      * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setObjectName(java.lang.String)
      */
    public void setObjectName(Object objectName) throws MalformedObjectNameException {
         this.objectName = objectName;
     }
 
     /**
      * Specify  what action should be taken when attempting to register an MBean
      * under an {@link javax.management.ObjectName} that already exists.
      * <p>Default is REGISTRATION_FAIL_ON_EXISTING.
      * @see #setRegistrationBehaviorName(String)
      * @see #REGISTRATION_FAIL_ON_EXISTING
      * @see #REGISTRATION_IGNORE_EXISTING
      * @see #REGISTRATION_REPLACE_EXISTING
      * @param registrationBehavior
      * @see org.springframework.jmx.support.MBeanRegistrationSupport#setRegistrationBehavior(int)
      */
     public void setRegistrationBehavior(int registrationBehavior) {
         this.registrationBehavior = registrationBehavior;
     }
 
     /**
      * Set the registration behavior by the name of the corresponding constant,
      * e.g. "REGISTRATION_IGNORE_EXISTING".
      * @see #setRegistrationBehavior
      * @see #REGISTRATION_FAIL_ON_EXISTING
      * @see #REGISTRATION_IGNORE_EXISTING
      * @see #REGISTRATION_REPLACE_EXISTING
      * @param registrationBehavior
      * @see org.springframework.jmx.support.MBeanRegistrationSupport#setRegistrationBehaviorName(java.lang.String)
      */
     public void setRegistrationBehaviorName(String registrationBehavior) {
         setRegistrationBehavior(constants.asNumber(registrationBehavior).intValue());
     }
 
     /**
      * Specify the <code>MBeanServer</code> instance with which all beans should
      * be registered. The <code>MBeanExporter</code> will attempt to locate an
      * existing <code>MBeanServer</code> if none is supplied.
      * @param server
      * @see org.springframework.jmx.support.MBeanRegistrationSupport#setServer(javax.management.MBeanServer)
      */
     public void setServer(MBeanServer server) {
         this.server = server;
     }
 
     /**
      * Set the service URL for the <code>JMXConnectorServer</code>.
      * @param serviceUrl
      * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setServiceUrl(java.lang.String)
      */
     public void setServiceUrl(String serviceUrl) {
         this.serviceUrl = serviceUrl;
     }
 
     /**
      * Set whether the <code>JMXConnectorServer</code> should be started in a separate thread.
      * @param threaded
      * @see org.springframework.jmx.support.ConnectorServerFactoryBean#setThreaded(boolean)
      */
     public void setThreaded(boolean threaded) {
         csfb.setThreaded(threaded);
     }
 
     public Object getObject() throws Exception {
         return csfb.getObject();
     }
 
     public Class getObjectType() {
         return csfb.getObjectType();
     }
 
     public boolean isSingleton() {
         return csfb.isSingleton();
     }
 
     public void afterPropertiesSet() throws Exception {
         csfb = new org.springframework.jmx.support.ConnectorServerFactoryBean();
         csfb.setDaemon(daemon);
         csfb.setThreaded(threaded);
         csfb.setRegistrationBehavior(registrationBehavior);
         csfb.setEnvironmentMap(environment);
         csfb.setObjectName(objectName);
         serviceUrl = serviceUrl.replaceAll(" ", "");
         csfb.setServiceUrl(serviceUrl);
         csfb.setServer(server);
         csfb.afterPropertiesSet();
         log.info("JMX connector available at: " + serviceUrl);
     }
 
     public void destroy() throws Exception {
         if (csfb != null) {
             try {
                 csfb.destroy();
             } finally {
                 csfb = null;
             }
         }
     }
 
 }
