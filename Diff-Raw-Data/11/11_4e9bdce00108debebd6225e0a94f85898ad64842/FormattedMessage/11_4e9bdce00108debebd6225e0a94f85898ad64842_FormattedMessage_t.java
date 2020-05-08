 package me.josvth.bukkitformatlibrary;
 
 import me.josvth.bukkitformatlibrary.formatter.Formatter;
 import org.bukkit.command.CommandSender;
 
 public class FormattedMessage {
 
 	private final String message;
 
 	public FormattedMessage(Formatter formatter, String message) {
 	   this.message = formatter.format(message);
 	}
 
 	public FormattedMessage(String message) {
 		this.message = message;
 	}
 
 	public String get() {
 		return message;
 	}
 	
 	public String get(String... arguments) {
 
 		String message = this.message;
 
 		for (int i = 1; i < arguments.length; i+= 2)
 			message = message.replaceAll(arguments[i - 1], arguments[i]);
 
 		return message;
 
 	}
 
	public void send(CommandSender sender) {
		sender.sendMessage(message);
	}

 	public void send(CommandSender sender, String... arguments) {
 		sender.sendMessage(get(arguments));
 	}
 
 	public String getRaw() {
 		return message;
 	}
 }
