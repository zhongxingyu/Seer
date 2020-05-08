 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 /**
  * 
  */
 package com.andune.minecraft.commonlib.server.bukkit;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.andune.minecraft.commonlib.JarUtils;
 import com.andune.minecraft.commonlib.Logger;
 import com.andune.minecraft.commonlib.LoggerFactory;
 import com.andune.minecraft.commonlib.server.api.Plugin;
 
 /** 
  * 
  * @author andune
  *
  */
 @Singleton
 public class BukkitPlugin implements Plugin
 {
     protected static final Logger log = LoggerFactory.getLogger(BukkitPlugin.class);
 
     private final org.bukkit.plugin.Plugin plugin;
     private final JarUtils jarUtil;
     private String build = null;
     
     @Inject
     public BukkitPlugin(org.bukkit.plugin.Plugin plugin, JarUtils jarUtil) {
         this.plugin = plugin;
         this.jarUtil = jarUtil;
     }
 
     @Override
     public File getDataFolder() {
         return plugin.getDataFolder();
     }
 
     @Override
     public File getJarFile() {
         File file = null;
         try {
             Class<?> clazz = plugin.getClass();
             while( clazz != null && !clazz.isAssignableFrom(JavaPlugin.class) ) {
                 clazz = clazz.getSuperclass();
             }
 
             Field fileField = clazz.getDeclaredField("file");
             fileField.setAccessible(true);
             file = (File) fileField.get(plugin);
         } catch (Exception e) {
             log.error("getJarFile caught exception", e);
         }
         return file;
     }
 
     @Override
     public String getName() {
         return plugin.getDescription().getName();
     }
 
     @Override
     public ClassLoader getClassLoader() {
         ClassLoader classLoader = null;
         try {
             Class<?> clazz = plugin.getClass();
             while( clazz != null && !clazz.isAssignableFrom(JavaPlugin.class) ) {
                 clazz = clazz.getSuperclass();
             }
 
            Field classLoaderField = plugin.getClass().getDeclaredField("classLoader");
             classLoaderField.setAccessible(true);
             classLoader = (ClassLoader) classLoaderField.get(plugin);
         } catch (Exception e) {
             log.error("getClassLoader caught exception", e);
         }
         return classLoader;
     }
 
     @Override
     public String getVersion() {
         return plugin.getDescription().getVersion();
     }
 
     @Override
     public String getBuild() {
         if( build == null )
             build = jarUtil.getBuild();
         return build;
     }
     
     @Override
     public InputStream getResource(String filename) {
         return plugin.getResource(filename);
     }
 }
