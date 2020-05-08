 package com.mcbouncer.bungee;
 
 import com.mcbouncer.api.MCBouncerAPI;
import com.mcbouncer.bungee.command.*;
 import com.mcbouncer.bungee.listener.ProxiedPlayerListener;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.plugin.Plugin;
 
 public class MCBouncer extends Plugin {
 
     public MCBouncerAPI api;
     public MainConfig config;
     
     @Override
     public void onEnable() {
         config = new MainConfig(this);
         api = new MCBouncerAPI("http://mcbouncer.com", config.apikey);
         getProxy().getPluginManager().registerListener(this, new ProxiedPlayerListener(this));
         getProxy().getPluginManager().registerCommand(this, new AddgnoteCommand(this));
         getProxy().getPluginManager().registerCommand(this, new AddnoteCommand(this));
         getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
         getProxy().getPluginManager().registerCommand(this, new KickCommand(this));
         getProxy().getPluginManager().registerCommand(this, new LookupCommand(this));
         getProxy().getPluginManager().registerCommand(this, new RemovenoteCommand(this));
         getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
         super.onEnable();
     }
 
     @Override
     public void onDisable() {
         super.onDisable();
     }
     
 }
