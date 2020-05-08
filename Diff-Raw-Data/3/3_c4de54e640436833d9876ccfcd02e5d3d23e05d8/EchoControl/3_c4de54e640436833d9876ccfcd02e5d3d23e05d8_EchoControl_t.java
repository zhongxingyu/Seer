 package org.digiplex.bukkitplugin.commander.scripting;
 
 import org.bukkit.command.CommandSender;
import org.digiplex.bukkitplugin.commander.CommanderPlugin;
 
 public abstract class EchoControl implements CommandSender {
 	boolean echoEnabled = false;
 	CommandSender wrappedSender;
 	
 	protected EchoControl(CommandSender sender) {
 		this.wrappedSender = sender;
		this.echoEnabled = CommanderPlugin.instance.config.getBoolean("options.default-echo", true);
 	}
 	
 	public CommandSender getWrappedSender() { return wrappedSender;}
 	
 	public boolean isEchoingEnabled() { return echoEnabled; }
 	public void setEchoingEnabled(boolean e){
 		echoEnabled = e;
 	}
 	
 	@Override public void sendMessage(String message) {
 		if (echoEnabled){
 			wrappedSender.sendMessage(message);
 		}
 	}
 	
 	@Override public void sendMessage(String[] messages) {
 		if (echoEnabled){
 			wrappedSender.sendMessage(messages);
 		}
 	}
 }
