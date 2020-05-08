 package com.mcbouncer.bungee;
 
 import com.mcbouncer.api.MCBouncerAPI;
import com.mcbouncer.bungee.command.AddnoteCommand;
import com.mcbouncer.bungee.command.AddgnoteCommand;
import com.mcbouncer.bungee.command.BanCommand;
import com.mcbouncer.bungee.command.KickCommand;
import com.mcbouncer.bungee.command.LookupCommand;
import com.mcbouncer.bungee.command.RemovenoteCommand;
import com.mcbouncer.bungee.command.UnbanCommand;
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
