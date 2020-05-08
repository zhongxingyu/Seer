 package com.geNAZt.RegionShop.Config;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 12.09.13
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.logging.Level;
 
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import org.apache.commons.lang.Validate;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlRepresenter;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
 import org.yaml.snakeyaml.error.YAMLException;
 import org.yaml.snakeyaml.representer.Representer;
 
 public class YamlConfiguration extends FileConfiguration {
     protected static final String COMMENT_PREFIX = "# ";
     protected static final String BLANK_CONFIG = "{}\n";
     private final DumperOptions yamlOptions = new DumperOptions();
     private final Representer yamlRepresenter = new YamlRepresenter();
     private final Yaml yaml = new Yaml(new CustomClassLoaderConstructor(RegionShopPlugin.class.getClassLoader()), yamlRepresenter, yamlOptions);
 
     @Override
     public String saveToString() {
         yamlOptions.setIndent(2);
         yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
         yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
 
         String header = buildHeader();
         String dump = yaml.dump(getValues(false));
 
         if (dump.equals(BLANK_CONFIG)) {
             dump = "";
         }
 
         return header + dump;
     }
 
     @Override
     public void loadFromString(String contents) throws InvalidConfigurationException {
         Validate.notNull(contents, "Contents cannot be null");
 
         Map<?, ?> input;
         try {
             input = (Map<?, ?>) yaml.load(contents);
         } catch (YAMLException e) {
             throw new InvalidConfigurationException(e);
         } catch (ClassCastException e) {
             throw new InvalidConfigurationException("Top level is not a Map.");
         }
 
         String header = parseHeader(contents);
         if (header.length() > 0) {
             options().header(header);
         }
 
         if (input != null) {
             convertMapsToSections(input, this);
         }
     }
 
     @Override
     protected String buildHeader() {
        return "";
     }
 
     protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
         for (Map.Entry<?, ?> entry : input.entrySet()) {
             String key = entry.getKey().toString();
             Object value = entry.getValue();
 
             if (value instanceof Map) {
                 convertMapsToSections((Map<?, ?>) value, section.createSection(key));
             } else {
                 section.set(key, value);
             }
         }
     }
 
     protected String parseHeader(String input) {
         String[] lines = input.split("\r?\n", -1);
         StringBuilder result = new StringBuilder();
         boolean readingHeader = true;
         boolean foundHeader = false;
 
         for (int i = 0; (i < lines.length) && (readingHeader); i++) {
             String line = lines[i];
 
             if (line.startsWith(COMMENT_PREFIX)) {
                 if (i > 0) {
                     result.append("\n");
                 }
 
                 if (line.length() > COMMENT_PREFIX.length()) {
                     result.append(line.substring(COMMENT_PREFIX.length()));
                 }
 
                 foundHeader = true;
             } else if ((foundHeader) && (line.length() == 0)) {
                 result.append("\n");
             } else if (foundHeader) {
                 readingHeader = false;
             }
         }
 
         return result.toString();
     }
 
     public static YamlConfiguration loadConfiguration(File file) {
         Validate.notNull(file, "File cannot be null");
 
         YamlConfiguration config = new YamlConfiguration();
 
         try {
             config.load(file);
         } catch (FileNotFoundException ex) {
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
         } catch (InvalidConfigurationException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
         }
 
         return config;
     }
 
     public static YamlConfiguration loadConfiguration(InputStream stream) {
         Validate.notNull(stream, "Stream cannot be null");
 
         YamlConfiguration config = new YamlConfiguration();
 
         try {
             config.load(stream);
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
         } catch (InvalidConfigurationException ex) {
             Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
         }
 
         return config;
     }
 }
