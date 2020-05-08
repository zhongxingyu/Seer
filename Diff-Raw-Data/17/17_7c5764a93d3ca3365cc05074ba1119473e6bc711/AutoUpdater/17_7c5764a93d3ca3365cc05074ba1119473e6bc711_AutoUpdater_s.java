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
 
 package me.Hoot215.updater;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AutoUpdater implements Runnable
   {
     private JavaPlugin plugin;
     private final String pluginName;
     private String localVersion;
     private String remoteVersion;
     private String site;
     private AtomicBoolean upToDate = new AtomicBoolean(true);
     private AutoUpdaterPlayerListener playerListener;
     
     public AutoUpdater(JavaPlugin instance)
       {
         plugin = instance;
         pluginName = plugin.getName();
         localVersion = plugin.getDescription().getVersion();
         String site = plugin.getDescription().getWebsite();
         this.site = site == null ? "" : site;
         playerListener = new AutoUpdaterPlayerListener(plugin, this);
       }
     
     public String getNewestVersion ()
       {
         return remoteVersion;
       }
     
     public String getSite ()
       {
         return site;
       }
     
     public boolean isUpToDate ()
       {
         return upToDate.get();
       }
     
     public void start ()
       {
         if ( !plugin.getConfig().isBoolean("auto-update-check")
             || plugin.getConfig().getBoolean("auto-update-check"))
           {
             plugin.getServer().getPluginManager()
                 .registerEvents(playerListener, plugin);
             new Thread(this).start();
           }
       }
     
     public void run ()
       {
         while (true)
           {
             try
               {
                 this.updateCheck();
                 Thread.sleep(3600000L);
               }
             catch (InterruptedException e)
               {
                 e.printStackTrace();
               }
           }
       }
     
     public synchronized boolean updateCheck ()
       {
         Scanner s = null;
         try
           {
             URL url =
                 new URL("http://dl.dropbox.com/u/56151340/BukkitPlugins/"
                     + plugin.getName() + "/latest");
             s = new Scanner(url.openStream());
             remoteVersion = s.nextLine();
             if (this.compareVersions())
               {
                 upToDate.set(false);
                 plugin.getServer().getScheduler()
                     .scheduleSyncDelayedTask(plugin, new Runnable()
                       {
                         public void run ()
                           {
                             plugin.getLogger()
                                 .info(
                                     "A newer version of " + pluginName
                                         + " is available! (v" + remoteVersion
                                         + ")");
                             if ( !site.isEmpty())
                               {
                                 plugin.getLogger().info(
                                     "Download it here: " + site);
                               }
                           }
                       });
                 return true;
               }
             upToDate.set(true);
           }
         catch (MalformedURLException e)
           {
             e.printStackTrace();
           }
         catch (IOException e)
           {
           }
         catch (NoSuchElementException e)
           {
           }
         finally
           {
             s.close();
           }
         return false;
       }
     
     private boolean compareVersions ()
       {
         String[] local = localVersion.split("\\.");
         String[] remote = remoteVersion.split("\\.");
         int longestNumber =
             local.length > remote.length ? local.length : remote.length;
         for (int i = 0; i < longestNumber; i++)
           {
             int l = 0;
             int r = 0;
             try
               {
                 try
                   {
                     l = Integer.valueOf(local[i]);
                   }
                 catch (ArrayIndexOutOfBoundsException e)
                   {
                   }
                 try
                   {
                     r = Integer.valueOf(remote[i]);
                   }
                 catch (ArrayIndexOutOfBoundsException e)
                   {
                   }
               }
             catch (NumberFormatException e)
               {
               }
             if (r > l)
               return true;
           }
         return false;
       }
   }
