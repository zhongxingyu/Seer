 package de.cubenation.plugins.utils.chatapi;
 
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChatService {
     private static final Locale defaultLocale = Locale.GERMANY;
 
     private JavaPlugin plugin;
     private ResourceBundle resource = null;
 
     public ChatService(JavaPlugin plugin) {
         this(plugin, null);
     }
 
     public ChatService(JavaPlugin plugin, String resourceName) {
         this(plugin, resourceName, null);
     }
 
     public ChatService(JavaPlugin plugin, String resourceName, Locale locale) {
         if (plugin == null) {
             throw new NullPointerException("JavaPlugin could not be null");
         }
 
         this.plugin = plugin;
 
         if (resourceName == null) {
             resourceName = plugin.getName() + "_messages";
             plugin.getLogger().info("set default i18n resource file to: " + resourceName);
         }
 
         if (locale == null) {
             // load from config.yml
             String configLang = plugin.getConfig().getString("lang");
             if (configLang != null && !configLang.isEmpty()) {
                 locale = new Locale(configLang);
             } else {
                 locale = defaultLocale;
             }
             plugin.getLogger().info("set default i18n language to: " + locale);
         }
 
         try {
             resource = ResourceBundle.getBundle(resourceName, locale);
         } catch (MissingResourceException e) {
             locale.getCountry();
             locale.getLanguage();
             plugin.getLogger().severe(
                     "could not load one of the i18n resource files: " + resourceName + ".properties, " + resourceName + "_" + locale.getCountry()
                             + ".properties, " + resourceName + "_" + locale.getLanguage() + ".properties, " + resourceName + "_" + locale.getLanguage() + "_"
                             + locale.getCountry() + ".properties");
             plugin.getLogger().severe("i18n disabled");
         }
     }
 
     public void chatText(final Player player, final String message) {
         Bukkit.getScheduler().runTask(plugin, new Thread("ChatService->chatText") {
             @Override
             public void run() {
                 player.sendMessage(message);
             }
         });
     }
 
     public void chatTextSynchron(Player player, String message) {
         player.sendMessage(message);
     }
 
     public void chat(final Player player, final String resourceString, final Object... constructorParameter) {
         Bukkit.getScheduler().runTask(plugin, new Thread("ChatService->chat") {
             @Override
             public void run() {
                 if (constructorParameter.length > 0) {
                     MessageFormat formatter = new MessageFormat("");
                     formatter.setLocale(resource.getLocale());
                     formatter.applyPattern(resource.getString(resourceString));
                     player.sendMessage(formatter.format(constructorParameter));
                 } else {
                     player.sendMessage(resource.getString(resourceString));
                 }
             }
         });
     }
 
     public void chatSynchron(Player player, String resourceString, Object... constructorParameter) {
         if (constructorParameter.length > 0) {
             MessageFormat formatter = new MessageFormat("");
             formatter.setLocale(resource.getLocale());
             formatter.applyPattern(resource.getString(resourceString));
             player.sendMessage(formatter.format(constructorParameter));
         } else {
             player.sendMessage(resource.getString(resourceString));
         }
     }
 }
