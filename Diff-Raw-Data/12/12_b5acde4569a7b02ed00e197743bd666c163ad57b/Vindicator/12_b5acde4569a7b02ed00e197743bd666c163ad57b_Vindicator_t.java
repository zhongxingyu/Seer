 /*
  * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jgeboski.vindicator;
 
 import java.io.File;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.ensifera.animosity.craftirc.CraftIRC;
 import com.ensifera.animosity.craftirc.EndPoint;
 import com.ensifera.animosity.craftirc.RelayedMessage;
 
 import org.jgeboski.vindicator.command.*;
 import org.jgeboski.vindicator.event.VindicatorConvertEvent;
 import org.jgeboski.vindicator.event.VindicatorEvent;
 import org.jgeboski.vindicator.listener.PlayerListener;
 import org.jgeboski.vindicator.storage.engine.SQLEngine;
 import org.jgeboski.vindicator.storage.Storage;
 import org.jgeboski.vindicator.storage.StorageException;
 import org.jgeboski.vindicator.storage.StoragePlayer;
 import org.jgeboski.vindicator.storage.StorageRecord;
 import org.jgeboski.vindicator.util.Log;
 import org.jgeboski.vindicator.util.Message;
 import org.jgeboski.vindicator.util.Utils;
 
 public class Vindicator extends JavaPlugin
 {
     public Configuration config;
     public Storage       storage;
 
     public HashMap<String, StorageRecord> mutes;
     public ThreadPoolExecutor             pool;
 
     public CraftIRC craftirc;
     public VPoint   vPoint;
 
     public void onLoad()
     {
         config   = new Configuration(new File(getDataFolder(), "config.yml"));
         storage  = null;
         mutes    = new HashMap<String, StorageRecord>();
         pool     = null;
         craftirc = null;
         vPoint   = null;
     }
 
     public void onEnable()
     {
         StoragePlayer plyr;
         PluginManager pm;
         Plugin        pn;
 
         Log.init(getLogger());
         Message.init(getDescription().getName());
 
         pm = getServer().getPluginManager();
         config.load();
 
         pool = new ThreadPoolExecutor(
             config.poolMinSize, config.poolMaxSize, config.poolKeepAlive,
             TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
             Executors.defaultThreadFactory());
 
         try {
             /* For now, SQL only */
             storage = new SQLEngine(
                 config.storeURL, config.storeUser,
                 config.storePass, config.storePrefix);
         } catch (StorageException e) {
             Log.severe("Failed to enable SQLEngine: %s", e.getMessage());
             setEnabled(false);
             pool.shutdown();
             return;
         }
 
         if (storage.convertible()) {
             Log.warning("Storage data conversion required!");
 
             try {
                 queue(new VindicatorConvertEvent());
             } catch (VindicatorException e) {
                 Log.severe(e.getMessage());
                 setEnabled(false);
                 pool.shutdown();
                 storage.close();
             }
 
             return;
         }
 
         try {
             for (Player p : getServer().getOnlinePlayers()) {
                 plyr = new StoragePlayer(p.getName());
                 plyr.validate(storage, config.autoComplete);
 
                 for (StorageRecord r : storage.getRecords(plyr)) {
                     if (r.hasFlag(StorageRecord.MUTE))
                         mutes.put(StoragePlayer.getPlayerId(p), r);
                 }
             }
         } catch (StorageException e) {
             Log.severe("Failed to acquire muted players: %s", e.getMessage());
             setEnabled(false);
             return;
         }
 
         if (config.ircEnabled) {
             pn = pm.getPlugin("CraftIRC");
 
             if ((pn != null) && pn.isEnabled()) {
                 craftirc = (CraftIRC) pn;
                 vPoint   = new VPoint();
             }
 
             if (!registerEndPoint(config.ircTag, vPoint))
                 config.ircEnabled = false;
         }
 
         pm.registerEvents(new PlayerListener(this), this);
 
         getCommand("ban").setExecutor(new CBan(this));
         getCommand("kick").setExecutor(new CKick(this));
         getCommand("lookup").setExecutor(new CLookup(this));
         getCommand("lookupa").setExecutor(new CLookupA(this));
         getCommand("mute").setExecutor(new CMute(this));
         getCommand("noteadd").setExecutor(new CNoteAdd(this));
         getCommand("noterem").setExecutor(new CNoteRem(this));
         getCommand("unban").setExecutor(new CUnban(this));
         getCommand("unmute").setExecutor(new CUnmute(this));
         getCommand("vindicator").setExecutor(new CVindicator(this));
     }
 
     public void onDisable()
     {
         if (VindicatorConvertEvent.converter != null)
             return;
 
        if (config.ircEnabled && (craftirc != null))
            craftirc.unregisterEndPoint(config.ircTag);

         if (pool != null)
             pool.shutdown();
 
         if (storage != null)
             storage.close();

        mutes.clear();
     }
 
     public void reload()
     {
         PluginManager pm;
 
         pm = getServer().getPluginManager();
 
         pm.disablePlugin(this);
         pm.enablePlugin(this);
     }
 
     private boolean registerEndPoint(String tag, Object ep)
     {
         if (craftirc == null)
             return false;
 
         if (craftirc.registerEndPoint(tag, (EndPoint) ep))
             return true;
 
         Log.severe("Unable to register CraftIRC tag: %s", tag);
         return false;
     }
 
     public void broadcast(String perm, String format, Object ... args)
     {
         String msg;
 
         msg = Message.format(format, args);
         Utils.broadcast(perm, msg);
 
         if (!config.ircEnabled)
             return;
 
         RelayedMessage rmsg;
 
         /* This typecasting is needed to prevent a ClassNotFoundException
          * from being thrown over com.ensifera.animosity.craftirc.EndPoint.
          */
         rmsg = craftirc.newMsg((EndPoint) ((Object) vPoint), null, "chat");
 
         if (!config.ircColored)
             msg = ChatColor.stripColor(msg);
 
         rmsg.setField("realSender", Message.plugin);
         rmsg.setField("sender",     Message.plugin);
         rmsg.setField("message",    msg);
 
         if (rmsg.post())
             return;
 
         registerEndPoint(config.ircTag, vPoint);
         rmsg.post();
     }
 
     public void execute(VindicatorEvent event)
         throws VindicatorException
     {
         event.vind    = this;
         event.storage = storage;
 
         event.execute();
     }
 
     public void queue(VindicatorEvent event)
         throws VindicatorException
     {
         event.vind    = this;
         event.storage = storage;
 
         try {
             pool.execute(event);
         } catch (RejectedExecutionException e) {
             Log.severe(e.getMessage());
             throw new VindicatorException("Failed to queue event. " +
                                           "Notify the administrator");
         }
     }
 }
