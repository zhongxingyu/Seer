 package com.milo.config;
 
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Loader;
 import org.yaml.snakeyaml.Yaml;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.io.InputStream;
 import java.io.IOException;
 
 public class CollectorConfiguration {
 
     private static Config instance = null;
     private static final String DEFAULT_COLLECTOR_CONFIG = "collector.yaml";
    
     public static Config get() throws ConfigurationException, IOException {
        URL url = getCollectorConfigURL();
        InputStream input = url.openStream();
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
 
    static URL getCollectorConfigURL() throws ConfigurationException {
        URL url;
        try {
            url = new URL(DEFAULT_COLLECTOR_CONFIG);
        }
        catch (MalformedURLException e) {
            ClassLoader loader = CollectorConfiguration.class.getClassLoader();;
            url = loader.getResource(DEFAULT_COLLECTOR_CONFIG);
            if (url == null)
                throw new ConfigurationException("Cannot locate " + DEFAULT_COLLECTOR_CONFIG);
         }

        return url;
     }
 
 }
