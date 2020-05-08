 /**
  *
  * Copyright 2009 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.papoose.framework.launch;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.framework.launch.Framework;
 import org.osgi.framework.launch.FrameworkFactory;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 
 import org.papoose.core.PapooseFrameworkFactory;
 
 
 /**
  * @version $Revision$ $Date$
  */
 public class PapooseFrameworkFactoryTest
 {
     @Test
     public void test() throws Exception
     {
         Map<String, String> configuration = new HashMap<String, String>();
        configuration.put(Constants.FRAMEWORK_STORAGE, "target/papoose");
 
         FrameworkFactory factory = new PapooseFrameworkFactory();
         final Framework framework = factory.newFramework(configuration);
 
         FrameworkEvent frameworkEvent = framework.waitForStop(0);
 
         assertNotNull(frameworkEvent);
         assertEquals(FrameworkEvent.STOPPED, frameworkEvent.getType());
         assertSame(framework, frameworkEvent.getBundle());
 
         framework.init();
 
         Bundle systemBundle = framework.getBundleContext().getBundle(0);
 
         assertEquals(Bundle.STARTING, framework.getState());
         assertEquals(Bundle.STARTING, systemBundle.getState());
 
         ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
 
         try
         {
             Future<FrameworkEvent> result = pool.submit(new Callable<FrameworkEvent>()
             {
                 public FrameworkEvent call() throws Exception
                 {
                     return framework.waitForStop(0);
                 }
             });
 
             framework.stop();
 
             frameworkEvent = result.get();
 
             assertNotNull(frameworkEvent);
             assertEquals(FrameworkEvent.STOPPED, frameworkEvent.getType());
             assertSame(systemBundle, frameworkEvent.getBundle());
 
             framework.waitForStop(0);
         }
         finally
         {
             pool.shutdown();
         }
 
         try
         {
             framework.waitForStop(-1);
             Assert.fail("Should never accept negative wait timeouts");
         }
         catch (IllegalArgumentException iae)
         {
         }
 
         frameworkEvent = framework.waitForStop(Long.MAX_VALUE);
 
         assertNotNull(frameworkEvent);
         assertEquals(FrameworkEvent.STOPPED, frameworkEvent.getType());
         assertSame(systemBundle, frameworkEvent.getBundle());
 
         framework.start();
 
         framework.stop();
 
         Thread.sleep(1);
 
         frameworkEvent = framework.waitForStop(0);
 
         assertNotNull(frameworkEvent);
         assertEquals(FrameworkEvent.STOPPED, frameworkEvent.getType());
         assertSame(systemBundle, frameworkEvent.getBundle());
     }
 }
