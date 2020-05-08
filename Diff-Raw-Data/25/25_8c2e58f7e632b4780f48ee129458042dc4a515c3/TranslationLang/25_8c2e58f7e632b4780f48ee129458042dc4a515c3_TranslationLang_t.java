 package me.galaran.bukkitutils.pvprealm.text;
 
 import com.google.common.collect.ImmutableMap;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 public class TranslationLang extends TranslationBase {
 
     private final Plugin plugin;
    private ImmutableMap<String, String> curTranslation = null;
 
     public TranslationLang(Plugin plugin, String defaultLang) {
         super("/" + defaultLang + ".lang");
         this.plugin = plugin;
     }
 
     public void reload(String language) {
         String langFileName = language + ".lang";
         File langFile = new File(plugin.getDataFolder(), langFileName);
 
         if (!langFile.isFile()) {
             try {
                 plugin.saveResource(langFileName, false);
             } catch (IllegalArgumentException ex) {
                 curTranslation = ImmutableMap.of();
                 plugin.getLogger().warning("No such lang: " + language + ". Using default");
                 return;
             }
         }
 
         try {
             curTranslation = loadProperties(new FileInputStream(langFile));
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         }
 
         if (curTranslation.size() < defaultTranslation.size()) {
             plugin.getLogger().warning("Lang " + language + " missing some entries. Defaults will be used");
         }
     }
 
     public String getString(String key) {
        if (curTranslation == null) { // before first reload()
            return super.getString(key);
         }
        
        String result = curTranslation.get(key);
        return result == null ? super.getString(key) : result;
     }
 }
