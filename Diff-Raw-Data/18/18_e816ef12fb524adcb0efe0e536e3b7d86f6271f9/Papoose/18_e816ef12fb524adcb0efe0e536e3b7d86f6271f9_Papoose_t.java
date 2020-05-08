 /**
  *
  * Copyright 2007 (C) The original author or authors
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
 package org.papoose.core.framework;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.security.AccessControlContext;
 import java.security.AccessController;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.osgi.framework.Constants;
 
 import org.papoose.core.framework.filter.Parser;
 import org.papoose.core.framework.spi.BundleManager;
 import org.papoose.core.framework.spi.Store;
 import org.papoose.core.framework.spi.ThreadPool;
 
 
 /**
  * @version $Revision$ $Date: $
  */
 public final class Papoose
 {
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     private static int frameworkCounter = 0;
     private final static Map<Integer, Reference<Papoose>> frameworksById = new Hashtable<Integer, Reference<Papoose>>();
     private final static Map<String, Reference<Papoose>> frameworksByName = new Hashtable<String, Reference<Papoose>>();
 
     private final AccessControlContext acc = AccessController.getContext();
     private final BundleManager bundleManager;
     private final ThreadPool threadPool;
     private final Properties properties;
     private final String frameworkName;
     private final int frameworkId;
     private long waitPeriod;
     private Parser parser;
     private BundleResolver resolver = new BundleResolver();
 
     /**
      * Install the store, thread pool and setup a hierarchy of properties.
      * <p/>
      * The framework instance gets registered and is accessable via its unique
      * framework id.
      *
      * @param store      the bundle store to use
      * @param threadPool the thread pool to use
      * @param properties the set of framework properties to use
      */
     public Papoose(Store store, ThreadPool threadPool, Properties properties)
     {
         this(null, store, threadPool, properties);
     }
 
    /**
     * Install the store, thread pool and setup a hierarchy of properties.
     * <p/>
     * The framework instance gets registered and is accessable via its unique
     * framework id.
     *
     * @param frameworkName the name of this framework instance.  It must be
     *                      unique for the JVM that is has been instantiated
     *                      and it must be follow the same format as a bundle
     *                      symbolic name.
     * @param store         the bundle store to use
     * @param threadPool    the thread pool to use
     * @param properties    the set of framework properties to use
     */
     public Papoose(String frameworkName, Store store, ThreadPool threadPool, Properties properties)
     {
         if (store == null) throw new IllegalArgumentException("store is null");
         if (threadPool == null) throw new IllegalArgumentException("threadPool is null");
 
         this.bundleManager = new BundleManagerImpl(this, store);
         this.threadPool = threadPool;
 
         Properties defaults = new Properties(System.getProperties());
         initProperties(defaults);
 
         defaults = new Properties(defaults);
         defaults.putAll(properties);
 
         this.properties = new Properties(defaults);
 
         this.frameworkId = frameworkCounter++;
 
         if (frameworkName == null)
         {
             this.frameworkName = "Papoose." + frameworkId;
         }
         else
         {
             this.frameworkName = frameworkName;
         }
 
        frameworksById.put(this.frameworkId, new WeakReference<Papoose>(this));
        frameworksByName.put(this.frameworkName, new WeakReference<Papoose>(this));
     }
 
     AccessControlContext getAcc()
     {
         return acc;
     }
 
     BundleManager getBundleManager()
     {
         return bundleManager;
     }
 
     ThreadPool getThreadPool()
     {
         return threadPool;
     }
 
     String getFrameworkName()
     {
         return frameworkName;
     }
 
     int getFrameworkId()
     {
         return frameworkId;
     }
 
     public long getWaitPeriod()
     {
         return waitPeriod;
     }
 
     public void setWaitPeriod(long waitPeriod)
     {
         this.waitPeriod = waitPeriod;
     }
 
     public Parser getParser()
     {
         return parser;
     }
 
     public void setParser(Parser parser)
     {
         this.parser = parser;
     }
 
     Object getProperty(String key)
     {
         return properties.get(key);
     }
 
     public Dictionary getProperties()
     {
         return properties;
     }
 
     public BundleResolver getResolver()
     {
         return resolver;
     }
 
     public void setResolver(BundleResolver resolver)
     {
         this.resolver = resolver;
     }
 
     static Papoose getFramework(Integer frameworkId)
     {
         Papoose result = frameworksById.get(frameworkId).get();
 
         if (result == null) frameworksById.remove(frameworkId);
 
         return result;
     }
 
     static Papoose getFramework(String name)
     {
         Papoose result = frameworksByName.get(name).get();
 
         if (result == null) frameworksByName.remove(name);
 
         return result;
     }
 
     public void start()
     {
         logger.entering(getClass().getName(), "start");
 
         logger.exiting(getClass().getName(), "start");
     }
 
     public void stop()
     {
         logger.entering(getClass().getName(), "stop");
 
         logger.exiting(getClass().getName(), "stop");
     }
 
     void unregisterServices(AbstractBundle bundle)
     {
     }
 
     void releaseServices(AbstractBundle bundle)
     {
     }
 
     Wire resolve(ImportDescription importDescription)
     {
         return null;
     }
 
     @SuppressWarnings({ "EmptyCatchBlock" })
     private static void initProperties(Properties defaults)
     {
         InputStream inputStream = null;
         try
         {
             inputStream = Papoose.class.getClassLoader().getResourceAsStream("papoose.properties");
             if (inputStream != null)
             {
                 Properties p = new Properties();
                 p.load(inputStream);
                 defaults.putAll(p);
             }
         }
         catch (IOException doNothing)
         {
         }
         finally
         {
             try
             {
                 if (inputStream != null) inputStream.close();
             }
             catch (IOException doNothing)
             {
             }
         }
 
         defaults.put(Constants.FRAMEWORK_VERSION, "1.3");
 
         defaults.put(Constants.FRAMEWORK_PROCESSOR, System.getProperty("os.arch"));
         defaults.put(Constants.FRAMEWORK_OS_VERSION, standardizeVersion(System.getProperty("os.version")));
         defaults.put(Constants.FRAMEWORK_OS_NAME, System.getProperty("os.name"));
 
         defaults.put("org.osgi.supports.framework.extension", true);
         defaults.put("org.osgi.supports.bootclasspath.extension", true);
         defaults.put("org.osgi.supports.framework.fragment", true);
         defaults.put("org.osgi.supports.framework.requirebundle", true);
     }
 
     private static String standardizeVersion(String version)
     {
         StringBuilder builder = new StringBuilder();
         int i = 0;
         int digits = 0;
 
         do
         {
             while (i < version.length() && Character.isDigit(version.charAt(i)))
             {
                 builder.append(version.charAt(i));
                 i++;
             }
             if (++digits < 3)
             {
                 if (i < version.length() && version.charAt(i) == '.')
                 {
                     builder.append(version.charAt(i));
                     i++;
                 }
                 else
                 {
                     break;
                 }
             }
             else
             {
                 break;
             }
         }
         while (true);
 
         return builder.toString();
     }
 }
