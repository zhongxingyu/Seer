 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudlaunch;
 
 import java.io.File;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.fusesource.cloudlaunch.distribution.event.Event;
 import org.fusesource.cloudlaunch.distribution.event.EventClient;
 import org.fusesource.cloudlaunch.distribution.event.EventListener;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 
 /**
  * EventTest
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class EventTest extends TestCase {
 
     ClassPathXmlApplicationContext context;
     LaunchClient client;
 
     protected void setUp() throws Exception {
 
        String dataDir = "target" + File.separator + "event-test";
         String commonRepo = new File(dataDir + File.separator + "common-repo").toURI().toString();
 
         System.setProperty("basedir", dataDir);
         System.setProperty("common.repo.url", commonRepo);
 
         context = new ClassPathXmlApplicationContext("cloudlaunch-all-spring.xml");
         client = (LaunchClient) context.getBean("launch-client");
 
     }
 
     protected void tearDown() throws Exception {
 
         context.destroy();
         client = null;
     }
 
     public void testEvent() throws Exception {
         EventClient ec = client.getDistributor().getEventClient();
         final CountDownLatch eventRcvd = new CountDownLatch(1);
         final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
         final Event event = new Event();
         event.setAttachment("TestString");
         event.setSource("testSource");
         event.setType(1);
 
         ec.openEventListener(new EventListener() {
 
             public void onEvent(Event e) {
                 switch (e.getType()) {
                 case 1: {
                     try {
                         assertEquals(e.getAttachment(), event.getAttachment());
                         assertEquals(e.getSource(), event.getSource());
                     } catch (AssertionFailedError ae) {
                         failure.set(ae);
                     }
                     break;
                 }
                 default: {
                     failure.set(new Exception("Unexpected event type: " + e.getType()));
                 }
                 }
 
                 eventRcvd.countDown();
             }
 
         }, "test-event");
 
         ec.sendEvent(event, "test-event");
         eventRcvd.await(5, TimeUnit.SECONDS);
 
         if (failure.get() != null) {
             throw new Exception("Listener failed", failure.get());
         }
 
     }
 }
