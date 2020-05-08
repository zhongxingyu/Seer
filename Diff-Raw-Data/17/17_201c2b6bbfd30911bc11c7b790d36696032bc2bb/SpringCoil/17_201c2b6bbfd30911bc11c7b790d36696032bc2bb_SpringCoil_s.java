 package net.LoadingChunks.SpringCoil;
 
 /*
     This file is part of SpringCoil
 
     Foobar is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Foobar is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.ServicePriority;
 import org.bukkit.plugin.java.JavaPlugin;
 import net.LoadingChunks.SpringCoil.api.*;
 
 public class SpringCoil extends JavaPlugin {
 	private Coil coil;
 	
 	// Message Listener
 	private final SpringCoilMessageListener messageListener = new SpringCoilMessageListener(this);
 	private final SpringCoilMessenger messenger = new SpringCoilMessenger(this);
 	private  ArrayList<CoilListener> listeners = new ArrayList<CoilListener>();
 
 	public void onDisable() {
 		// add any code you want to be executed when your plugin is disabled
 	}
 
 	public void onEnable() { 
 		this.registerCoilAPI();
		
 		this.registerPluginChannels();
 	}
 	
 	public void registerCoilAPI() {
		if(this.coil == null)
		{
			Coil api = new SpringCoilAPI(this);
			Bukkit.getServicesManager().register(Coil.class, api, this, ServicePriority.Normal);
			this.coil = api;
 		}
 	}
 	
 	public void registerPluginChannels() {
         Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
         Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", messageListener);
 	}
 	
 	public SpringCoilMessageListener getListener() {
 		return this.messageListener;
 	}
 	
 	public SpringCoilMessenger getMessenger() {
 		return this.messenger;
 	}
 	
 	public void addListener(CoilListener l)
 	{
 		this.listeners.add(l);
 	}
 }
