 /*
  * Copyright 2010-2013 Ning, Inc.
  *
  *  Ning licenses this file to you under the Apache License, version 2.0
  *  (the "License"); you may not use this file except in compliance with the
  *  License.  You may obtain a copy of the License at:
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  *  License for the specific language governing permissions and limitations
  *  under the License.
  */
 
 package com.ning.killbill.zuora.osgi;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.servlet.Servlet;
 
 import org.osgi.framework.BundleContext;
 import org.skife.config.ConfigurationObjectFactory;
 import org.slf4j.impl.StaticLoggerBinder;
 
 import com.ning.billing.osgi.api.OSGIPluginProperties;
 import com.ning.billing.payment.plugin.api.PaymentPluginApi;
 import com.ning.killbill.osgi.libs.killbill.KillbillActivatorBase;
 import com.ning.killbill.osgi.libs.killbill.OSGIKillbillEventDispatcher.OSGIKillbillEventHandler;
 import com.ning.killbill.zuora.api.DefaultZuoraPrivateApi;
 import com.ning.killbill.zuora.api.ZuoraPaymentPluginApi;
 import com.ning.killbill.zuora.api.ZuoraPrivateApi;
 import com.ning.killbill.zuora.dao.ZuoraPluginDao;
 import com.ning.killbill.zuora.dao.dbi.JDBIZuoraPluginDao;
 import com.ning.killbill.zuora.dao.jpa.JPAZuoraPluginDao;
 import com.ning.killbill.zuora.http.ZuoraHttpServlet;
 import com.ning.killbill.zuora.killbill.DefaultKillbillApi;
 import com.ning.killbill.zuora.zuora.ConnectionFactory;
 import com.ning.killbill.zuora.zuora.ConnectionPool;
 import com.ning.killbill.zuora.zuora.ZuoraApi;
 import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.collect.ImmutableMap;
 
 /*
    pluginName = com.ning.killbill.zuora-plugin
  */
 public class ZuoraActivator extends KillbillActivatorBase {
 
     public final static String PLUGIN_NAME = "zuora";
 
     private final static String DEFAULT_INSTANCE_NAME = "default";
 
     private ZuoraConfig config;
     private ObjectMapper mapper;
     private ZuoraApi api;
     private ConnectionFactory factory;
     private ConnectionPool pool;
     private ZuoraPluginDao zuoraPluginDao;
     private ZuoraPaymentPluginApi zuoraPaymentPluginApi;
     private ZuoraHttpServlet zuoraHttpServlet;
     private ZuoraPrivateApi zuoraPrivateApi;
 
     @Override
     public void start(final BundleContext context) throws Exception {
 
         super.start(context);
 
         // Configure slf4j for libraries (e.g. config-magic)
         StaticLoggerBinder.getSingleton().setLogService(logService);
 
         config = readConfigFromSystemProperties(DEFAULT_INSTANCE_NAME);
         mapper = new ObjectMapper();
         api = new ZuoraApi(config, logService);
         factory = new ConnectionFactory(config, api, logService);
         pool = new ConnectionPool(factory, config);
 
         zuoraPluginDao = config.useJPADAOImplementation() ?
                          new JPAZuoraPluginDao(dataSource.getDataSource()) :
                          new JDBIZuoraPluginDao(dataSource.getDataSource());
 
         final DefaultKillbillApi defaultKillbillApi = new DefaultKillbillApi(killbillAPI, logService);
         zuoraPaymentPluginApi = new ZuoraPaymentPluginApi(pool, api, logService, defaultKillbillApi, zuoraPluginDao, DEFAULT_INSTANCE_NAME);
         zuoraPrivateApi = new DefaultZuoraPrivateApi(pool, api, logService, defaultKillbillApi, zuoraPluginDao, DEFAULT_INSTANCE_NAME);
 
         zuoraHttpServlet =  new ZuoraHttpServlet(zuoraPrivateApi, zuoraPluginDao, mapper);
 
         registerPaymentPluginApi(context, zuoraPaymentPluginApi);
         registerServlet(context, zuoraHttpServlet);
     }
 
     @Override
     public void stop(final BundleContext context) throws Exception {
         super.stop(context);
     }
 
     @Override
     public OSGIKillbillEventHandler getOSGIKillbillEventHandler() {
         return null;
     }
 
     private final ZuoraConfig readConfigFromSystemProperties(final String instanceName) {
         final Properties props = System.getProperties();
         final ConfigurationObjectFactory factory = new ConfigurationObjectFactory(props);
         return factory.buildWithReplacements(ZuoraConfig.class,
                                                     ImmutableMap.of("pluginInstanceName", instanceName));
     }
 
 
     private void registerServlet(final BundleContext context, final ZuoraHttpServlet servlet) {
         final Hashtable<String, String> props = new Hashtable<String, String>();
         props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
         props.put(OSGIPluginProperties.PLUGIN_SERVICE_INFO, PLUGIN_NAME);
         registrar.registerService(context, Servlet.class, servlet, props);
     }
 
     private void registerPaymentPluginApi(final BundleContext context, final PaymentPluginApi api) {
         final Dictionary props = new Hashtable();
         props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
         registrar.registerService(context, PaymentPluginApi.class, api, props);
     }
 }
