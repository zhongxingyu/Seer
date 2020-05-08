 /*
  * Fireworks display on levelling up.
  * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.Hoot215.LevelFlare.api;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 
 import me.Hoot215.LevelFlare.LevelFlare;
 
 public class LevellerManager
   {
     private LevelFlare plugin;
     private Map<String, Leveller> levellers = new HashMap<String, Leveller>();
     private Set<String> levellerNames = new HashSet<String>();
     private Set<Leveller> playerLevelChangeEventListeners =
         new HashSet<Leveller>();
     private Map<String, Listener> eventListeners =
         new HashMap<String, Listener>();
     
     public LevellerManager(LevelFlare instance)
       {
         plugin = instance;
       }
     
     public Leveller getLeveller (String name)
       {
         return levellers.get(name);
       }
     
     public Set<String> getLevellerNames ()
       {
         return levellerNames;
       }
     
     public void onPlayerLevelChangeEvent (LevelFlarePlayerLevelChangeEvent event)
       {
         for (Leveller l : playerLevelChangeEventListeners)
           {
             l.onPlayerLevelChange(event);
           }
       }
     
     public void registerPlayerLevelChangeEvent (Leveller leveller)
       {
         playerLevelChangeEventListeners.add(leveller);
       }
     
     public void unregisterPlayerLevelChangeEvent (Leveller leveller)
       {
         playerLevelChangeEventListeners.remove(leveller);
       }
     
     public void registerBukkitEvents (Leveller leveller)
       {
         if (leveller instanceof Listener)
           {
             this.registerBukkitEvents(leveller, (Listener) leveller);
           }
       }
     
     public void registerBukkitEvents (Leveller leveller, Listener listener)
       {
         String levellerName = leveller.getName();
         if ( !eventListeners.containsKey(levellerName))
           {
             plugin.getServer().getPluginManager()
                 .registerEvents(listener, plugin);
             eventListeners.put(levellerName, listener);
           }
       }
     
     public void unregisterBukkitEvents (Leveller leveller)
       {
         if (leveller instanceof Listener)
           {
             this.unregisterBukkitEvents(leveller, (Listener) leveller);
           }
       }
     
     public void unregisterBukkitEvents (Leveller leveller, Listener listener)
       {
         String levellerName = leveller.getName();
         if (eventListeners.containsKey(levellerName))
           {
             for (Method m : listener.getClass().getMethods())
               {
                 if (m.isAnnotationPresent(EventHandler.class))
                   {
                     for (Class<?> clazz : m.getParameterTypes())
                       {
                        if (Event.class.isAssignableFrom(clazz))
                           {
                             try
                               {
                                 ((HandlerList) clazz
                                     .getMethod("getHandlerList").invoke(null))
                                     .unregister(listener);
                               }
                             catch (IllegalAccessException e)
                               {
                                 e.printStackTrace();
                               }
                             catch (InvocationTargetException e)
                               {
                                 e.printStackTrace();
                               }
                             catch (NoSuchMethodException e)
                               {
                                 e.printStackTrace();
                               }
                           }
                       }
                   }
               }
             plugin.getServer().getPluginManager();
             eventListeners.remove(levellerName);
           }
       }
     
     public void loadLevellers ()
       {
         File levellerDir = new File(plugin.getDataFolder(), "levellers");
         if ( !levellerDir.exists())
           {
             levellerDir.mkdir();
           }
         for (File f : levellerDir.listFiles())
           {
             if (f == null)
               return;
             
             this.loadLeveller(f);
           }
       }
     
     public void unloadLevellers ()
       {
         for (String s : levellerNames)
           {
             if (s == null)
               return;
             
             this.unloadLeveller(s);
           }
       }
     
     public Leveller loadLeveller (String name)
       {
         if (levellerNames.contains(name))
           return null;
         return this.loadLeveller(new File(plugin.getDataFolder(), "levellers"
             + File.separator + name + ".jar"));
       }
     
     public Leveller loadLeveller (File file)
       {
         if ( !file.exists())
           return null;
         
         Leveller leveller = null;
         try
           {
             ClassLoader child =
                 URLClassLoader.newInstance(new URL[] {file.toURI().toURL()},
                     this.getClass().getClassLoader());
             Class<?> jarClass = Class.forName("Main", true, child);
             Class<? extends LevelFlareLeveller> clazz =
                 jarClass.asSubclass(LevelFlareLeveller.class);
             Constructor<? extends LevelFlareLeveller> constructor =
                 clazz.getConstructor();
             leveller = constructor.newInstance();
             leveller.initialize(plugin);
             String levellerName = leveller.getName();
             levellerNames.add(levellerName);
             levellers.put(levellerName, leveller);
             leveller.makeLogger();
             leveller.onLoad();
           }
         catch (MalformedURLException e)
           {
             e.printStackTrace();
           }
         catch (ClassNotFoundException e)
           {
             plugin.getLogger().severe(
                 "Leveller '" + file.getName()
                     + "' does not contian Main.class!");
             e.printStackTrace();
           }
         catch (NoSuchMethodException e)
           {
             e.printStackTrace();
           }
         catch (InstantiationException e)
           {
             e.printStackTrace();
           }
         catch (IllegalAccessException e)
           {
             e.printStackTrace();
           }
         catch (InvocationTargetException e)
           {
             e.printStackTrace();
           }
         return leveller;
       }
     
     public boolean unloadLeveller (String name)
       {
         Leveller leveller = levellers.get(name);
         if (leveller == null)
           return false;
         leveller.onUnload();
         levellerNames.remove(name);
         levellers.remove(name);
         return true;
       }
   }
