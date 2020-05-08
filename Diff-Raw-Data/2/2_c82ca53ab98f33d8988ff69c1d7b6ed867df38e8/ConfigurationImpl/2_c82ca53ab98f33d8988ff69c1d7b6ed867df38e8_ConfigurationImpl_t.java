 package polly.configuration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 
 import de.skuzzle.polly.sdk.Configuration;
 import de.skuzzle.polly.sdk.ConfigurationValidator;
 import de.skuzzle.polly.sdk.eventlistener.ConfigurationEvent;
 
 
 
 public class ConfigurationImpl implements Configuration {
 
     private final static Logger logger = Logger.getLogger(
             ConfigurationImpl.class.getName());
     
     private Properties properties;
     private ConfigurationProviderImpl parent;
     private ConfigurationValidator validator;
     private File cfgFile;
     
     
     
     public ConfigurationImpl(File cfgFile, ConfigurationProviderImpl parent) 
                 throws FileNotFoundException, IOException {
         this(parent);
         this.cfgFile = cfgFile;
         this.properties.load(new FileInputStream(cfgFile));
     }
     
     
     
     public ConfigurationImpl(ConfigurationProviderImpl parent) {
         this.parent = parent;
         this.properties = new Properties();
     }
     
     
     
     public ConfigurationImpl(ConfigurationProviderImpl parent, 
             ConfigurationImpl defaults) {
         this(parent);
         this.properties.putAll(defaults.properties);
     }
     
     
     
     public void setValidator(ConfigurationValidator validator) {
         this.validator = validator;
     }
     
     
     
     @Override
     public ConfigurationValidator getValidator() {
         return this.validator;
     }
     
     
     
     @Override
     public boolean isValidated() {
         return this.validator != null;
     }
     
     
     
     @Override
     public <T> void setProperty(String name, T value) {
         this.properties.setProperty(name, value.toString());
         if (parent == null) {
             logger.warn("Tried to dispatch a ConfigurationEvent, but no parent was set!");
         } else {
             this.parent.fireConfigurationChanged(new ConfigurationEvent(this));
         }
     }
     
     
 
     @Override
     public String readString(String name) {
         return this.properties.getProperty(name);
     }
 
     
     
     @Override
     public String readString(String name, String defaultValue) {
         return this.properties.getProperty(name, defaultValue);
     }
 
     
     
     @Override
     public int readInt(String name) {
         return Integer.parseInt(this.readString(name));
     }
     
     
 
     @Override
     public int readInt(String name, int defaultValue) {
         try {
             return this.readInt(name);
         } catch (Exception e) {
            return defaultValue;
         }
     }
 
     
 
     @Override
     public boolean readBoolean(String name) {
         final String tmp = this.readString(name);
         return tmp != null && tmp.equals("true");
     }
     
     
     
     
     @Override
     public List<String> readStringList(String name) {
         String prop = this.readString(name);
         if (prop == null) {
             return new LinkedList<String>();
         }
         String[] list = prop.split(",");
         return Arrays.asList(list);
     }
     
     
     
     @Override
     public List<Integer> readIntList(String name) {
         String prop = this.readString(name);
         if (prop == null) {
             return new LinkedList<Integer>();
         }
         String[] parts = prop.split(",");
         ArrayList<Integer> result = new ArrayList<Integer>(parts.length);
         for (String s : parts) {
             result.add(Integer.parseInt(s));
         }
         return result;
     }
     
     
     
     @Override
     public String toString() {
         SortedMap<Object, Object> sorted = new TreeMap<Object, Object>(
                 this.properties);
         int maxLength = 0;
         
         for (Object o : sorted.keySet()) {
             maxLength = Math.max(maxLength, o.toString().length());
         }
         StringBuilder b = new StringBuilder();
 
         if (this.cfgFile != null) {
             b.append(this.cfgFile.getAbsolutePath());
         } else {
             b.append("Memory Configuration");
         }
         b.append(System.lineSeparator());
         
         for (Entry<Object, Object> e : sorted.entrySet()) {
             b.append("    ");
             b.append(e.getKey().toString());
             this.padSpaces(maxLength, e.getKey().toString().length(), b);
             b.append(" = ");
             b.append(e.getValue().toString());
             b.append(System.lineSeparator());
         }
         
         return b.toString();
     }
     
     
     
     private void padSpaces(int desiredLength, int currentLength, StringBuilder b) {
         int spaces = desiredLength - currentLength;
         if (spaces <= 0) {
             return;
         }
         for (int i = 0; i < spaces; ++i) {
             b.append(" ");
         }
     }
 }
