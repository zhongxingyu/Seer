 package net.milanaleksic.guitransformer.providers.impl;
 
 import com.google.common.collect.ImmutableMap;
 import net.milanaleksic.guitransformer.providers.ShortcutsProvider;
 import net.milanaleksic.guitransformer.util.Configuration;
 
 public class ConfigurableShortcutsProvider implements ShortcutsProvider {
 
     private final ImmutableMap<String, Class<?>> mapping;
 
     public ConfigurableShortcutsProvider() {
         // this Configurable* does not need lazy load since no injection is needed
        mapping = Configuration.loadStringToClassMapping("shortcuts");
     }
 
     @Override
     public Class<?> provideClassForShortcut(String name) {
         return mapping.get(name);
     }
 
 }
