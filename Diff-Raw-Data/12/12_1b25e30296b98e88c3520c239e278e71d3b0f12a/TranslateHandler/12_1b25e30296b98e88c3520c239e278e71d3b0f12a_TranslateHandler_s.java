 package org.snack.irc.handler;
 
 import org.pircbotx.hooks.events.MessageEvent;
 import org.snack.irc.settings.Config;
 
 import com.memetix.mst.language.Language;
 import com.memetix.mst.translate.Translate;
 
 public class TranslateHandler {
 
 	/**
 	 * Translates text using the Bing translate API
 	 * 
 	 * @param event
 	 */
 	public static void translate(MessageEvent<?> event) {
 		String text = event.getMessage().substring(11); // Cut off the command
 
 		// Set your Windows Azure Marketplace client info - See
 		// http://msdn.microsoft.com/en-us/library/hh454950.aspx
		Translate.setClientId("Snack-Ircbot");
		Translate.setClientSecret("ZCUBUZxekDrqvkKWPGriMQdHw7yGSut4YgaFvioUFEU=");
 
 		String translatedText = "";
 		try {
 			translatedText = Config.speech.get("TR_SUC").replace("<response>", Translate.execute(text, Language.AUTO_DETECT, Language.ENGLISH));
 		} catch (Exception e) {
			// e.printStackTrace();
 			translatedText = Config.speech.get("TR_ERR");
 		}
 
 		event.getBot().sendMessage(event.getChannel(), translatedText);
 	}
 }
