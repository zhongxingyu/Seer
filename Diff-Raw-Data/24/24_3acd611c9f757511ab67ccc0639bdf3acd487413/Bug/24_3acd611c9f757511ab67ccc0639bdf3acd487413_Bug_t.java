 package com.turt2live.antishare.debug;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 
/*
 * This is used mostly while developing. When this is used, it
 * will only be useful if AntiShare.DEBUG_MODE is true.
 */
 public class Bug {
 
 	private Exception e;
 	private CommandSender sender;
 	private World world;
 	private String message;
 	private Class<?> calledFrom;
 
 	public Bug(Exception e, String message, Class<?> involvedClass){
 		this.e = e;
 		this.message = message;
 		this.calledFrom = involvedClass;
 	}
 
 	public Bug(Exception e, String message, Class<?> involvedClass, CommandSender sender){
 		this.e = e;
 		this.message = message;
 		this.calledFrom = involvedClass;
 		this.sender = sender;
 	}
 
 	public void setWorld(World world){
 		this.world = world;
 	}
 
 	public Plugin[] getPlugins(){
 		return Bukkit.getServer().getPluginManager().getPlugins();
 	}
 
 	public World getWorld(){
 		return world;
 	}
 
 	public Exception getException(){
 		return e;
 	}
 
 	public String getMessage(){
 		return message;
 	}
 
 	public CommandSender getSenderInvolved(){
 		return sender;
 	}
 
 	public Class<?> getInvolvedClass(){
 		return calledFrom;
 	}
 
 }
