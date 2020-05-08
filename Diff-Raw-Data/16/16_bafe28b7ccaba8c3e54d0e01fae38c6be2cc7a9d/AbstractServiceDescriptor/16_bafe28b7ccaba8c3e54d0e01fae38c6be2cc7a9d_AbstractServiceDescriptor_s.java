 package sorcer.provider.boot;
 /**
  *
  * Copyright 2013 Rafał Krupiński.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import com.sun.jini.start.ServiceDescriptor;
 import net.jini.config.Configuration;
 import org.rioproject.config.PlatformCapabilityConfig;
 import org.rioproject.config.PlatformLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.boot.load.Activator;
 import sorcer.core.SorcerEnv;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author Rafał Krupiński
  */
 public abstract class AbstractServiceDescriptor implements ServiceDescriptor {
     protected static boolean platformLoaded = false;
     protected static AtomicInteger allDescriptors = new AtomicInteger(0);
     protected static AtomicInteger startedServices = new AtomicInteger(0);
     protected static AtomicInteger erredServices = new AtomicInteger(0);
     private static Activator activator = new Activator();
 
     protected Logger logger = LoggerFactory.getLogger(getClass());
 
     {
         allDescriptors.incrementAndGet();
     }
 
     protected void loadPlatform(Configuration config, String defaultDir, CommonClassLoader commonCL) throws Exception {
         PlatformLoader platformLoader = new PlatformLoader();
         List<URL> urlList = new LinkedList<URL>();
 
         String platformDir = (String) config.getEntry(SorcerServiceDescriptor.COMPONENT, "platformDir",
                 String.class, defaultDir);
         logger.debug("Platform dir: {}", platformDir);
         PlatformCapabilityConfig[] caps = platformLoader.parsePlatform(platformDir);
 
         logger.debug("Capabilities: {}", Arrays.toString(caps));
         for (PlatformCapabilityConfig cap : caps) {
             if (cap.getCommon()) {
                 URL[] urls = cap.getClasspathURLs();
                 urlList.addAll(Arrays.asList(urls));
             }
         }
 
         URL[] commonJARs = urlList.toArray(new URL[urlList.size()]);
 
 			/*
              * if(commonJARs.length==0) throw new
 			 * RuntimeException("No commonJARs have been defined");
 			 */
         if (logger.isDebugEnabled()) {
             StringBuffer buffer = new StringBuffer();
             for (int i = 0; i < commonJARs.length; i++) {
                 if (i > 0)
                     buffer.append("\n");
                 buffer.append(commonJARs[i].toExternalForm());
             }
             logger.debug("commonJARs=\n{}", buffer);
         }
 
         commonCL.addCommonJARs(commonJARs);
 
         activate(commonCL, commonJARs);
     }
 
     private void activate(CommonClassLoader commonCL, URL[] commonJARs) throws Exception {
         Thread thread = Thread.currentThread();
         ClassLoader contextClassLoader = thread.getContextClassLoader();
         try {
             thread.setContextClassLoader(commonCL);
             activator.activate(commonJARs);
         } finally {
             thread.setContextClassLoader(contextClassLoader);
         }
     }
 
     /**
      * @see com.sun.jini.start.ServiceDescriptor#create
      */
     public Object create(Configuration config) throws Exception {
         try {
             return doCreate(config);
         } catch (Exception x) {
             erredServices.incrementAndGet();
             logger.error("Error creating service", x);
             throw x;
         } finally {
             int i = startedServices.incrementAndGet();
             logger.info("Started " + i + '/' + allDescriptors.get() + " services; " + erredServices.get() + " errors");
         }
     }
 
     protected abstract Object doCreate(Configuration config) throws Exception;
 
     protected CommonClassLoader getCommonClassLoader(Configuration config) throws Exception {
     /* Set common JARs to the CommonClassLoader */
         String defaultDir = null;
        String fs = File.separator;
        String sorcerHome = SorcerEnv.getHomeDir().getPath();
        if (sorcerHome == null) {
            logger.info("'sorcer.home' not defined, no default platformDir");
         } else {
            defaultDir = sorcerHome + fs + "configs" + fs + "platform" + fs + "sorcer";
            if (!new File(defaultDir).exists())
                defaultDir = sorcerHome + fs + "lib" + fs + "rio" + fs + "config" + fs + "platform";
         }
 
         CommonClassLoader commonCL = CommonClassLoader.getInstance();
         // Don't load Platform Class Loader when it was already loaded before
         if (!platformLoaded) {
             loadPlatform(config, defaultDir, commonCL);
             platformLoaded = true;
         }
         return commonCL;
     }
 
     /**
      * Object returned by
      * {@link sorcer.provider.boot.SorcerServiceDescriptor#create(net.jini.config.Configuration)
      * SorcerServiceDescriptor.create()} method that returns the proxy and
      * implementation references for the created service.
      */
     public static class Created {
         /**
          * The reference to the proxy of the created service
          */
         public final Object proxy;
         /**
          * The reference to the implementation of the created service
          */
         public final Object impl;
 
         /**
          * Constructs an instance of this class.
          *
          * @param impl  reference to the implementation of the created service
          * @param proxy reference to the proxy of the created service
          */
         public Created(Object impl, Object proxy) {
             this.proxy = proxy;
             this.impl = impl;
         }
     }
 }
