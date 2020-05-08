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
 package com.nesscomputing.event.amqp;
 
 import java.util.UUID;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.Stage;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.nesscomputing.amqp.AmqpConfig;
 import com.nesscomputing.config.Config;
 import com.nesscomputing.config.ConfigModule;
 import com.nesscomputing.event.NessEvent;
 import com.nesscomputing.event.NessEventModule;
 import com.nesscomputing.event.NessEventSender;
 import com.nesscomputing.event.NessEventType;
 import com.nesscomputing.event.amqp.util.CountingEventReceiver;
 import com.nesscomputing.jackson.NessJacksonModule;
 import com.nesscomputing.lifecycle.junit.LifecycleRule;
 import com.nesscomputing.lifecycle.junit.LifecycleRunner;
 import com.nesscomputing.lifecycle.junit.LifecycleStatement;
 import com.nesscomputing.testing.lessio.AllowNetworkListen;
 
 @AllowNetworkListen(ports={0})
 @RunWith(LifecycleRunner.class)
 public class TestAmqpEventTransport
 {
     @LifecycleRule
     public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();
 
     private static final NessEventType TEST_EVENT_TYPE = NessEventType.getForName("TEST_EVENT");
     private static final UUID USER = UUID.randomUUID();
     private static final NessEvent TEST_EVENT = NessEvent.createEvent(USER, TEST_EVENT_TYPE);
 
     @Inject
     private NessEventSender sender;
 
     @Inject
     private CountingEventReceiver receiver;
 
     private QPidUtils qpid = new QPidUtils();
 
     @Before
     public void setUp() throws Exception
     {
         qpid.startup();
 
         final String brokerUri = qpid.getUri();
 
         final Config config = Config.getFixedConfig(ImmutableMap.of("ness.event.amqp.enabled", "true",
                                                                     "ness.event.transport", "amqp",
                                                                     "ness.amqp.amqp-event.enabled", "true",
                                                                     "ness.amqp.amqp-event.publisher-queue-length", "500",
                                                                     "ness.amqp.amqp-event.connection-url", brokerUri));
 
         final CountingEventReceiver testEventReceiver = new CountingEventReceiver(TEST_EVENT_TYPE);
         final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                        new ConfigModule(config),
                                                        lifecycleRule.getLifecycleModule(),
                                                        new NessEventModule(),
                                                        new NessJacksonModule(),
                                                        new AmqpEventModule(config),
                                                        new Module() {
                                                            @Override
                                                            public void configure(final Binder binder) {
                                                                binder.disableCircularProxies();
                                                                binder.requireExplicitBindings();
                                                                binder.bind(CountingEventReceiver.class).toInstance(testEventReceiver);
                                                                NessEventModule.bindEventReceiver(binder).toInstance(testEventReceiver);
                                                            }
                                                        });
 
         final AmqpEventConfig eventConfig = injector.getInstance(AmqpEventConfig.class);
         final AmqpConfig amqpConfig = injector.getInstance(Key.get(AmqpConfig.class, AmqpEventModule.AMQP_EVENT_NAMED));
         Assert.assertTrue(eventConfig.isEnabled());
         Assert.assertTrue(amqpConfig.isEnabled());
 
         injector.injectMembers(this);
 
         Assert.assertNotNull(sender);
         Assert.assertNotNull(receiver);
     }
 
     @After
     public void tearDown() throws Exception
     {
         qpid.shutdown();
     }
 
     @Test
     public void testSendAndReceive() throws Exception
     {
         final int maxCount = 1000;
 
         // Warm up the ObjectMapper.
         sender.enqueue(TEST_EVENT);
         Thread.sleep(100L);
 
         for (int i = 0; i < maxCount; i++) {
             Thread.sleep(4L);
             sender.enqueue(TEST_EVENT);
         }
 
         Thread.sleep(1000L);
 
         final NessEvent testEvent = receiver.getEvent();
         Assert.assertNotNull(testEvent);
         Assert.assertEquals(TEST_EVENT, testEvent);
        Assert.assertEquals(maxCount, receiver.getCount());
     }
 }
 
 
 
