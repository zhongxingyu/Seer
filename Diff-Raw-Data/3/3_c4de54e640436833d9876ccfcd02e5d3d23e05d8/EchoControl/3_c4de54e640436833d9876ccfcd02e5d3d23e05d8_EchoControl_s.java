 package org.digiplex.bukkitplugin.commander.scripting;
 
 import org.bukkit.command.CommandSender;
 
 public abstract class EchoControl implements CommandSender {
 	boolean echoEnabled = false;
 	CommandSender wrappedSender;
 	
 	protected EchoControl(CommandSender sender) {
 		this.wrappedSender = sender;
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
