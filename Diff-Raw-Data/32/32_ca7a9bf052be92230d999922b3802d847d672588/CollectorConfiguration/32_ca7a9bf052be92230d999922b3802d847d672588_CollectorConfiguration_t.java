 package com.milo.config;
 
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Loader;
 import org.yaml.snakeyaml.Yaml;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.io.InputStream;
 import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
 
 public class CollectorConfiguration {
 
     private static Config instance = null;
     private static final String DEFAULT_COLLECTOR_CONFIG = "collector.yaml";

     public static Config get() throws ConfigurationException, IOException {
        String configYAML = getCollectorConfigURL();
        InputStream input = new FileInputStream(new File(configYAML));
         org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor(Config.class);
         TypeDescription desc = new TypeDescription(Config.class);
         TypeDescription mysql_desc = new TypeDescription(MySQL.class);
         TypeDescription cass_desc = new TypeDescription(Cassandra.class);
         TypeDescription scribe_desc = new TypeDescription(Scribe.class);
         constructor.addTypeDescription(desc);
         constructor.addTypeDescription(mysql_desc);
         constructor.addTypeDescription(cass_desc);
         constructor.addTypeDescription(scribe_desc);
         Yaml yaml = new Yaml(new Loader(constructor));
         return (Config) yaml.load(input);
     }
 
     public static Config factory() throws IOException, ConfigurationException {
         if (instance == null) {
             instance = get();
         }
         return instance;
     }
 
    static String getCollectorConfigURL() throws ConfigurationException {
        ClassLoader loader = CollectorConfiguration.class.getClassLoader();
        String configResource = System.getProperty("config") + File.separator + DEFAULT_COLLECTOR_CONFIG;
        File f = new File(configResource);
        if (f.exists() == false) {
            throw new ConfigurationException("Cannot locate " + DEFAULT_COLLECTOR_CONFIG);
        } else {
            return configResource;
         }
     }
 
 }
