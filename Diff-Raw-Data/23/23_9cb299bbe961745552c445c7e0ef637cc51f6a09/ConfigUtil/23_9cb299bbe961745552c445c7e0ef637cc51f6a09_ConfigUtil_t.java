 package com.miraclem4n.msocial.configs;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 public class ConfigUtil {
     static YamlConfiguration config;
     static File file;
     static Boolean changed;
 
     static ArrayList<String> sayAliases = new ArrayList<String>();
     static ArrayList<String> shoutAliases = new ArrayList<String>();
     static ArrayList<String> muteAliases = new ArrayList<String>();
     static ArrayList<String> pmAliases = new ArrayList<String>();
     static ArrayList<String> replyAliases = new ArrayList<String>();
     static ArrayList<String> inviteAliases = new ArrayList<String>();
     static ArrayList<String> acceptAliases = new ArrayList<String>();
     static ArrayList<String> denyAliases = new ArrayList<String>();
     static ArrayList<String> leaveAliases = new ArrayList<String>();
 
     static HashMap<String, List<String>> aliasMap = new HashMap<String, List<String>>();
 
     public static void initialize() {
         load();
     }
 
     public static void dispose() {
         config = null;
         file = null;
         changed = null;
         aliasMap = null;
     }
 
     public static void load() {
         file = new File("plugins/MSocial/config.yml");
 
         config = YamlConfiguration.loadConfiguration(file);
 
         config.options().indent(4);
         config.options().header("MSocial Config");
 
         changed = false;
 
         loadDefaults();
     }
 
     private static void loadDefaults() {
         checkOption("option.spoutPM", false);
 
         loadAliases();
 
         checkOption("aliases.mchatsay", sayAliases);
         checkOption("aliases.pmchat", pmAliases);
         checkOption("aliases.pmchatreply", replyAliases);
         checkOption("aliases.pmchatinvite", inviteAliases);
         checkOption("aliases.pmchataccept", acceptAliases);
         checkOption("aliases.pmchatdeny", denyAliases);
         checkOption("aliases.pmchatleave", leaveAliases);
         checkOption("aliases.mchatshout", shoutAliases);
         checkOption("aliases.mchatmute", muteAliases);
 
         setupAliasMap();
 
         save();
     }
 
     public static void set(String key, Object obj) {
         config.set(key, obj);
 
         changed = true;
     }
 
     public static Boolean save() {
         if (!changed)
             return false;
 
         try {
             config.save(file);
             changed = false;
             return true;
         } catch (Exception ignored) {
             return false;
         }
     }
 
     public static YamlConfiguration getConfig() {
         return config;
     }
 
     public static HashMap<String, List<String>> getAliasMap() {
         return aliasMap;
     }
 
     private static void checkOption(String option, Object defValue) {
         if (!config.isSet(option))
             set(option, defValue);
     }
 
     private static void editOption(String oldOption, String newOption) {
         if (config.isSet(oldOption)) {
             set(newOption, config.get(oldOption));
             set(oldOption, null);
         }
     }
 
     private static void removeOption(String option) {
         if (config.isSet(option))
             set(option, null);
     }
 
     private static void loadAliases() {
         sayAliases.add("say");
 
         shoutAliases.add("shout");
         shoutAliases.add("yell");
 
         muteAliases.add("mute");
         muteAliases.add("quiet");
 
         pmAliases.add("pm");
         pmAliases.add("msg");
         pmAliases.add("message");
         pmAliases.add("m");
         pmAliases.add("tell");
         pmAliases.add("t");
 
         replyAliases.add("reply");
         replyAliases.add("r");
 
         inviteAliases.add("invite");
 
         acceptAliases.add("accept");
 
         denyAliases.add("deny");
 
         leaveAliases.add("leave");
     }
 
     private static void setupAliasMap() {
         Set<String> keys = config.getConfigurationSection("aliases").getKeys(false);
 
         for (String key : keys)
             aliasMap.put(key, config.getStringList("aliases." + key));
     }
 }
