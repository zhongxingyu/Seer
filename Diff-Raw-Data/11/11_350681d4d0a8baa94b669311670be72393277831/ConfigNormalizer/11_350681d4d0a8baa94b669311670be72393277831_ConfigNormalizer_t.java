 /* 
  * JBoss, Home of Professional Open Source
  * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @author tags. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.qa.jdg.config;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.management.MBeanAttributeInfo;
 
 import org.infinispan.commons.api.BasicCacheContainer;
 import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.commons.util.TypedProperties;
 import org.infinispan.configuration.cache.Configuration;
 import org.infinispan.configuration.cache.ConfigurationBuilder;
 import org.infinispan.configuration.global.GlobalConfiguration;
 import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
 import org.infinispan.configuration.parsing.ParserRegistry;
 import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
 import org.jboss.logging.Logger;
 import org.jgroups.JChannel;
 import org.jgroups.jmx.ResourceDMBean;
 import org.jgroups.stack.Protocol;
 
 /**
  * 
  * Config normalizer. Outputs effective infinispan configuration into a flat properties-like
  * structure.
  * 
  * @author Michal Linhard (mlinhard@redhat.com)
  */
 public class ConfigNormalizer {
 
    private static Logger log = Logger.getLogger(ConfigNormalizer.class);
 
    private static Method plainToString = null;
    static {
       try {
          plainToString = Object.class.getMethod("toString");
       } catch (Exception e) {
          log.error("Error while initializing", e);
       }
    }
 
    /**
     * 
     * Returns properties made by reflection of
     * 
     * @param globalConfiguration
     *           global configuration
     * @param cacheConfigurations
     *           map cacheName -> cacheConfig
     * @param jgroupsChannel
     *           JGroups channel
     * @return configuration in form of properties
     * @throws Exception
     */
    public static Properties reflectProperties(GlobalConfiguration globalConfiguration, Map<String, Configuration> cacheConfigurations, JChannel jgroupsChannel)
          throws Exception {
       Properties p = new Properties();
       p.putAll(reflectProperties(globalConfiguration, "global"));
       for (Entry<String, Configuration> ent : cacheConfigurations.entrySet()) {
          p.putAll(reflectProperties(ent.getValue(), "cache." + ent.getKey()));
       }
       if (jgroupsChannel != null) {
          p.putAll(reflectProperties(jgroupsChannel, "jgroups"));
       }
       return p;
    }
 
    /**
     * 
     * Reflect global configuration.
     * 
     * @param globalConfiguration
     * @param prefix
     * @return Config properties
     * @throws Exception
     */
    public static Properties reflectProperties(GlobalConfiguration globalConfiguration, String prefix) throws Exception {
       Properties p = new Properties();
       reflect(globalConfiguration, p, prefix);
       return p;
    }
 
    /**
     * 
     * Reflect configuration.
     * 
     * @param config
     * @param prefix
     * @return Config properties
     * @throws Exception
     */
    public static Properties reflectProperties(Configuration config, String prefix) throws Exception {
       Properties p = new Properties();
       reflect(config, p, prefix);
       return p;
    }
 
    /**
     * 
     * Reflect JGroups channel.
     * 
     * @param jgroupsChannel
     * @param prefix
     * @return Config properties
     * @throws Exception
     */
    public static Properties reflectProperties(JChannel jgroupsChannel, String prefix) throws Exception {
       Properties p = new Properties();
       getJGroupsConfig(prefix, p, jgroupsChannel);
       return p;
    }
 
    /**
     * 
     * Stores the properties in sorted order into a regular properties file.
     * 
     * @param properties
     * @param file
     * @throws Exception
     */
    public static void storeSortedPropertiesAsXML(Properties properties, String file) throws Exception {
       Properties props = new SortedProperties();
       props.putAll(properties);
       props.storeToXML(new FileOutputStream(file), null, "UTF-8");
    }
 
    /**
     * 
     * Stores the properties in sorted order into a XML file.
     * 
     * @param properties
     * @param file
     * @throws Exception
     */
    public static void storeSortedProperties(Properties properties, String file) throws Exception {
       Properties props = new SortedProperties();
       props.putAll(properties);
       props.store(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), null);
    }
 
    private static void findJars(List<URL> jarPaths, File dir) throws MalformedURLException {
       for (File f : dir.listFiles()) {
          if (f.isFile()) {
             if (f.getName().endsWith(".jar")) {
                jarPaths.add(f.toURI().toURL());
             }
          } else if (f.isDirectory()) {
             findJars(jarPaths, f);
          }
       }
    }
 
    private static ClassLoader getConfigClassLoader(String jarDir) throws MalformedURLException {
       if (jarDir != null) {
          ClassLoader parent = Thread.currentThread().getContextClassLoader();
          List<URL> urls = new ArrayList<URL>();
          findJars(urls, new File(jarDir));
          return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
       } else {
          return Thread.currentThread().getContextClassLoader();
       }
    }
 
    private static class FakeJGroupsTransport extends JGroupsTransport {
       @Override
       public void initChannel() {
         props = TypedProperties.toTypedProperties(configuration.transport().properties());
          super.initChannel();
       }
    }
 
    private static void usage() {
       System.out.println("USAGE ConfigNormalizer [OPTIONS] <config_file>");
       System.out.println("OPTIONS:");
       System.out.println("  -o <output_file>");
       System.out.println("  -f <output_format>");
       System.out.println("     defined output formats:");
       System.out.println("        xml      - xml properties (default)");
       System.out.println("        standard - standard properties");
       System.out.println("  -c <cache_name>");
       System.out.println("     implies  output_type=cache");
       System.out.println("  -t <output_type>");
       System.out.println("     defined output types:");
       System.out.println("        all     - print all properties into one file, prefixes will be: global, cache.<name>, jgroups");
       System.out.println("        cache   - cache name needs to be specified by option -c, if not default cache is used");
       System.out.println("                  prints only specified cache configuration");
       System.out.println("        global  - prints only global configuration");
       System.out.println("        jgroups - prints only jgroups configuration");
       System.out.println("  -p <property_key_prefix>");
       System.out.println("     prefix will be appended before all property keys in the output file, default empty string");
       System.out.println("  -j <jar_dir>");
       System.out.println("     all JAR files under this directory will be added to classpath");
       System.exit(0);
    }
 
    public static void main(String[] args) throws Exception {
       if (args.length == 0) {
          usage();
          return;
       }
       String outputFile = null;
       String outputFormat = "xml";
       String cacheName = null;
       String outputType = "all";
       String prefix = "";
       String jarDir = null;
       String configFile = null;
 
       for (int i = 0; i < args.length; i++) {
          if (args[i].equals("-o")) {
             outputFile = args[i + 1];
             i++;
          } else if (args[i].equals("-f")) {
             outputFormat = args[i + 1];
             i++;
          } else if (args[i].equals("-c")) {
             cacheName = args[i + 1];
             i++;
          } else if (args[i].equals("-t")) {
             outputType = args[i + 1];
             i++;
          } else if (args[i].equals("-p")) {
             prefix = args[i + 1];
             i++;
          } else if (args[i].equals("-j")) {
             jarDir = args[i + 1];
             i++;
          } else {
             configFile = args[i];
             if (i != args.length - 1) {
                usage();
                return;
             }
          }
       }
       if (!Arrays.asList("all", "cache", "global", "jgroups").contains(outputType)) {
          System.out.println("ERROR: unknown output type: " + outputType);
          usage();
          return;
       }
       if (!Arrays.asList("xml", "standard").contains(outputFormat)) {
          System.out.println("ERROR: unknown output format: " + outputType);
          usage();
          return;
       }
       ClassLoader configClassLoader = getConfigClassLoader(jarDir);
       ConfigurationBuilderHolder holder = new ParserRegistry(configClassLoader).parse(FileLookupFactory.newInstance().lookupFileStrict(configFile,
             configClassLoader));
       if ("all".equals(outputType)) {
          Map<String, Configuration> cacheConfigurations = new HashMap<String, Configuration>();
          GlobalConfiguration globalConfiguration = holder.getGlobalConfigurationBuilder().build();
          cacheConfigurations.put(BasicCacheContainer.DEFAULT_CACHE_NAME, holder.getDefaultConfigurationBuilder().build());
          for (String cacheName1 : holder.getNamedConfigurationBuilders().keySet()) {
             cacheConfigurations.put(cacheName1, holder.getNamedConfigurationBuilders().get(cacheName1).build());
          }
          JChannel jgroupsChannel = getInitializedJChannel(globalConfiguration);
          store(outputFormat, outputFile, reflectProperties(globalConfiguration, cacheConfigurations, jgroupsChannel));
       } else if ("cache".equals(outputType)) {
          Configuration config = null;
          if (cacheName == null || BasicCacheContainer.DEFAULT_CACHE_NAME.equals(cacheName)) {
             config = holder.getDefaultConfigurationBuilder().build();
          } else {
             ConfigurationBuilder b = holder.getNamedConfigurationBuilders().get(cacheName);
             if (b == null) {
                System.out.println("ERROR: cache " + cacheName + " not found.");
                System.exit(1);
                return;
             }
             config = b.build();
          }
          store(outputFormat, outputFile, reflectProperties(config, prefix));
       } else if ("global".equals(outputType)) {
          GlobalConfiguration globalConfiguration = holder.getGlobalConfigurationBuilder().build();
          store(outputFormat, outputFile, reflectProperties(globalConfiguration, prefix));
       } else if ("jgroups".equals(outputType)) {
          GlobalConfiguration globalConfiguration = holder.getGlobalConfigurationBuilder().build();
          JChannel jgroupsChannel = getInitializedJChannel(globalConfiguration);
          store(outputFormat, outputFile, reflectProperties(jgroupsChannel, prefix));
       } else {
          System.out.println("ERROR: unknown output type: " + outputType);
          usage();
          return;
       }
    }
 
    private static void store(String outputFormat, String outputFile, Properties p) throws Exception {
       if ("xml".equals(outputFormat)) {
          storeSortedPropertiesAsXML(p, outputFile);
       } else if ("standard".equals(outputFormat)) {
          storeSortedProperties(p, outputFile);
       } else {
          System.out.println("ERROR: unknown output format: " + outputFormat);
          usage();
          return;
       }
    }
 
    private static JChannel getInitializedJChannel(GlobalConfiguration globalConfiguration) {
       FakeJGroupsTransport fTransport = new FakeJGroupsTransport();
       fTransport.setConfiguration(globalConfiguration);
       fTransport.initChannel();
       return (JChannel) fTransport.getChannel();
    }
 
    private static void getJGroupsConfig(String prefix, Properties p, JChannel jChannel) throws Exception {
       for (Protocol proto : jChannel.getProtocolStack().getProtocols()) {
          reflectJGroupsProtocol(prefix, p, proto);
       }
    }
 
    private static List<Method> getMethods(Class<?> clazz) {
       Class<?> c = clazz;
       ArrayList<Method> r = new ArrayList<Method>();
       while (c != null && c != Object.class) {
          for (Method m : c.getDeclaredMethods()) {
             r.add(m);
          }
          c = c.getSuperclass();
       }
       return r;
    }
 
    private static void reflectJGroupsProtocol(String prefix, Properties p, Protocol proto) throws Exception {
       ResourceDMBean bean = new ResourceDMBean(proto);
       for (MBeanAttributeInfo info : bean.getMBeanInfo().getAttributes()) {
          String propName = info.getName();
          Object propValue = bean.getAttribute(propName);
          String prefixDot = prefix == null || "".equals(prefix) ? "" : prefix + ".";
          p.put(prefixDot + proto.getName() + "." + propName, propValue == null ? "null" : propValue.toString());
       }
    }
 
    private static boolean hasPlainToString(Class<?> cls, Object obj) {
       try {
          if (cls.getMethod("toString") == plainToString) {
             return true;
          }
          String plainToStringValue = cls.getName() + "@" + Integer.toHexString(obj.hashCode());
          return plainToStringValue.equals(obj.toString());
       } catch (Exception e) {
          return false;
       }
    }
 
    private static void reflect(Object obj, Properties p, String prefix) throws Exception {
       if (obj == null) {
          p.put(prefix, "null");
          return;
       }
       Class<?> cls = obj.getClass();
       if (cls.getName().startsWith("org.infinispan.config") && !cls.isEnum()) {
          for (Method m : getMethods(obj.getClass())) {
             if (m.getParameterTypes().length != 0 || "toString".equals(m.getName()) || "hashCode".equals(m.getName())) {
                continue;
             }
             try {
                String prefixDot = prefix == null || "".equals(prefix) ? "" : prefix + ".";
                reflect(m.invoke(obj), p, prefixDot + m.getName());
             } catch (IllegalAccessException e) {
                // ok
             }
          }
       } else if (Collection.class.isAssignableFrom(cls)) {
          Collection<?> collection = (Collection<?>) obj;
          Iterator<?> iter = collection.iterator();
          for (int i = 0; i < collection.size(); i++) {
             reflect(iter.next(), p, prefix + "[" + i + "]");
          }
       } else if (cls.isArray()) {
          Object[] a = (Object[]) obj;
          for (int i = 0; i < a.length; i++) {
             reflect(a[i], p, prefix + "[" + i + "]");
          }
       } else if (hasPlainToString(cls, obj)) {
          // we have a class that doesn't have a nice toString implementation
          p.put(prefix, cls.getName());
       } else {
          // we have a single value
          p.put(prefix, obj.toString());
       }
    }
 }
