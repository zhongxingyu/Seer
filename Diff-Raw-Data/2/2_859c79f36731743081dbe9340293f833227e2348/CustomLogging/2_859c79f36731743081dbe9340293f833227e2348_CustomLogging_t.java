 
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 public class CustomLogging {
 	
 	private static final Logger LOGGER = LogManager.getLogger();
 
 	/** 
 	 * Called from net.minecraft.command.server.CommandMessage.processCommand()
 	 * 
 	 * @param from
 	 * @param to
 	 * @param message
 	 */
 	public static void logWhisper(fa from, fa to, fa message) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("[CHAT] ");
 		builder.append(from.c());
		builder.append(" whispers to ");
 		builder.append(to.c());
 		builder.append(" ");
 		builder.append(message.c());
 		builder.append(": ");
 		
 		LOGGER.info(builder.toString());
 	}
 	
 	/**
 	 * Called from net.minecraft.network.NetHandlerPlayServer.func_147354_a()
 	 * 
 	 * @param from
 	 * @param message
 	 */
 	public static void logChat(fa from, String message) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("[CHAT] ");
 		builder.append(from.c());
 		builder.append(" ");
 		builder.append(message);
 		
 		LOGGER.info(builder.toString());
 	}
 }
