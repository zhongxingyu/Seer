 /*
  * Copyright 2013 StuxCrystal
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package net.stuxcrystal.commandhandler.compat.bukkit;
 
 import net.stuxcrystal.commandhandler.CommandExecutor;
 import net.stuxcrystal.commandhandler.translations.TranslationHandler;
 import net.stuxcrystal.commandhandler.translations.TranslationManager;
 import org.bukkit.configuration.MemoryConfiguration;
 
 import java.util.Map;
 
 
 /**
  * A handler using a memory configuration.
  */
 public class BukkitConfigurationLocalizationHandler implements TranslationHandler {
 
     /**
      * Starts the memory section.
      */
     protected MemoryConfiguration section;
 
     /**
      * Initializes the command handler.
      *
      * @param configuration
      */
     public BukkitConfigurationLocalizationHandler(MemoryConfiguration configuration) {
         update(configuration);
     }
 
     /**
      * Returns the translation for the CommandHandler.
      *
      * @param sender The sender that sent the command.
      * @param key    The Key of the translation.
      * @return
      */
     @Override
     public String getTranslation(CommandExecutor sender, String key) {
         return this.section.getString(key);
     }
 
     /**
      * Updates the configuration data.
      *
     * @param configuration
      */
     public void update(MemoryConfiguration configuration) {
         this.section = configuration;
         // A little hack to enforce a cast from Map<String, String> to Map<String, Object>.
         this.section.addDefaults((Map<String, Object>) (Map) TranslationManager.getDefaults());
     }
 }
