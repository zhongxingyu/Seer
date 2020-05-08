 package me.galaran.bukkitutils.pvprealm.text;
 
 import com.google.common.collect.ImmutableMap;
 import org.bukkit.ChatColor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Properties;
 
 public class TranslationBase implements Messaging.Translation {
 
     protected final ImmutableMap<String, String> defaultTranslation;
 
     public TranslationBase(InputStream defaultPropertiesStream) {
         defaultTranslation = loadProperties(defaultPropertiesStream);
     }
 
     public TranslationBase(String jarPath) {
         this(TranslationBase.class.getResourceAsStream(jarPath));
     }
 
     public String getString(String key) {
         String result = defaultTranslation.get(key);
         if (result == null) {
             return missingKey(key);
         }
         return result;
     }
 
    protected String missingKey(String key) {
        String result = ChatColor.RED + "Missing translation for key " + ChatColor.DARK_RED + key;
         System.out.println(result);
         return result;
     }
 
     protected ImmutableMap<String, String> loadProperties(InputStream is) {
         Properties prop = new Properties();
         try {
             Reader reader = new InputStreamReader(is, "utf-8");
             prop.load(reader);
             reader.close();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
         ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
         for (String curKey : prop.stringPropertyNames()) {
             builder.put(curKey, prop.getProperty(curKey));
         }
         return builder.build();
     }
 }
