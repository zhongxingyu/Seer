 package com.netflix.config.sources.test;
 
 
 import com.netflix.config.*;
 import com.netflix.config.sources.ServiceRegistryConfigurationProvider;
 import com.rackspacecloud.client.service_registry.objects.Service;
 import org.apache.commons.configuration.AbstractConfiguration;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.InetSocketAddress;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class ServiceRegistryConfigurationProviderTest {
 
     private static TestServiceRegistryConfigurationProvider testInstance;
     private static PolledConfigurationSource src;
     private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryConfigurationProviderTest.class);
     private static AbstractPollingScheduler simplePollingScheduler;
     private static DynamicConfiguration dynamicConfiguration;
 
     public String getRandomTag() {
         byte diff = 'z' - 'a';
         byte[] bytes = new byte[5];
         StringBuilder sb = new StringBuilder();
         new Random().nextBytes(bytes);
         for (byte b : bytes) {
             sb.append((char)('a' + Math.abs(b % diff)));
         }
         logger.info("Random tag generated '{}'", sb.toString());
         return sb.toString();
     }
     public void pollOnce() {
         dynamicConfiguration.startPolling(src, simplePollingScheduler);
     }
 
     @BeforeClass
     public static void beforeClass() {
         testInstance = new TestServiceRegistryConfigurationProvider();
         src = new ServiceRegistryConfigurationProvider(testInstance);
         simplePollingScheduler = new AbstractPollingScheduler() {
             @Override
             protected void schedule(Runnable pollingRunnable) {
                 logger.info("Scheduling poller");
                 pollingRunnable.run();
             }
 
             @Override
             public void stop() {
                 logger.info("Stopping logger");
             }
         };
 
         // Inject initial properties
         HashMap<String, Object> cfg = new HashMap<String, Object>();
 
         AbstractConfiguration configuration = new ConcurrentMapConfiguration(cfg);
         dynamicConfiguration = new DynamicConfiguration(src, simplePollingScheduler);
 
         ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
 
         // add them in this order to make dynamicConfig override configuration
         finalConfig.addConfiguration(dynamicConfiguration);
         finalConfig.addConfiguration(configuration);
 
         ConfigurationManager.install(finalConfig);
 
     }
 
    @Test
     public void testPolling() throws Exception {
         String host = "host";
         int port = 92;
         String tag = getRandomTag();
 
         ConfigurationManager.getConfigInstance().setProperty(ServiceRegistryConfigurationProvider.INTEREST, tag);
 
         for (int i = 0; i < 10; i++) {
             testInstance.addService(tag, ServiceRegistryConfigurationProvider.setHostPortPair(
                     host,
                     port,
                     new Service("faux" + i, 15, Collections.singletonList(tag), new HashMap<String, String>())));
         }
         String key = ServiceRegistryConfigurationProvider.PREFIX + "." + tag + ".addresses";
         pollOnce();
 
         Assert.assertEquals(new InetSocketAddress(host, port).toString(),
                 ConfigurationManager.getConfigInstance().getString(key));
     }
 
     @Test
     public void testPollingChanging() throws Exception {
         String host = "localhost";
         int port = 92;
         String[] tags = {getRandomTag(),  getRandomTag()};
         ConfigurationManager.getConfigInstance().setProperty(
                 ServiceRegistryConfigurationProvider.INTEREST, StringUtils.join(tags, ","));
         Map<String, Integer> scoreboard = new HashMap<String, Integer>();
 
         for (int i = 0; i < tags.length; i++) {
             for (int j = 0; j < 5; j++) {
                 String realHost = host + i + j;
                 testInstance.addService(tags[i], ServiceRegistryConfigurationProvider.setHostPortPair(
                         host + i + j,
                         port,
                         new Service("faux" + i + j, 15, Collections.singletonList(tags[i]), new HashMap<String, String>())));
 
 
                 Integer score = scoreboard.get(tags[i]);
                 if (score == null) {
                     scoreboard.put(tags[i], 1);
                 } else {
                     scoreboard.put(tags[i], score + 1);
                 }
             }
         }
 
         String firstKey = ServiceRegistryConfigurationProvider.PREFIX + "." + tags[0] + ".addresses";
         String secondKey = ServiceRegistryConfigurationProvider.PREFIX + "." + tags[1] + ".addresses";
         pollOnce();
 
         Assert.assertEquals((Object) scoreboard.get(tags[0]),
                 ConfigurationManager.getConfigInstance().getList(firstKey).size());
         Assert.assertEquals((Object) scoreboard.get(tags[1]),
                 ConfigurationManager.getConfigInstance().getList(secondKey).size());
     }
 }
