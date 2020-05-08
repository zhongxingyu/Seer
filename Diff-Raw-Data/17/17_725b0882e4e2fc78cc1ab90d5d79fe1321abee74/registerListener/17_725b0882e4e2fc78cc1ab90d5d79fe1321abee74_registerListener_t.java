 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.spiceking.plugins.spoutwallet.listeners;
 
 /**
  *
  * @author Kyle
  */
 
 // Imports for MyPlugin
 import me.spiceking.plugins.spoutwallet.payment.Methods;
 import me.spiceking.plugins.spoutwallet.SpoutWallet;
 
 // Bukkit Imports
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 
 public class registerListener extends ServerListener {
     
     private SpoutWallet plugin;
     private Methods Methods = null;
 
     public registerListener(SpoutWallet plugin) {
         this.plugin = plugin;
         this.Methods = new Methods();
     }
 
     @Override
     public void onPluginDisable(PluginDisableEvent event) {
         // Check to see if the plugin thats being disabled is the one we are using
         if (this.Methods != null && this.Methods.hasMethod()) {
             Boolean check = this.Methods.checkDisabled(event.getPlugin());
 
             if(check) {
                 this.plugin.Method = null;
                 this.plugin.iConomy = null;
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method was disabled. No longer accepting payments.");
             }
         }
     }
 
     @Override
     public void onPluginEnable(PluginEnableEvent event) {
         // Check to see if we need a payment method
         if (!this.Methods.hasMethod()) {
             if(this.Methods.setMethod(event.getPlugin())) {
                 this.plugin.Method = this.Methods.getMethod();
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method found (" + this.plugin.Method.getName() + " version: " + this.plugin.Method.getVersion() + ")");
             }
         }
     }
 }
