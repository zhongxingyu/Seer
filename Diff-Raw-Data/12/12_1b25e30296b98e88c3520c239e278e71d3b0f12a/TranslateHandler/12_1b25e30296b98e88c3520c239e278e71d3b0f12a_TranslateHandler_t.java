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
		Translate.setClientId("92ef8ea3-08d0-4642-8f7c-af1898ac47b6");
		Translate.setClientSecret("i4nycktHpUOs5eTgvo1AabpVSUGPqbgrVydJF2nVmtM=");
 
 		String translatedText = "";
 		try {
			String response = Translate.execute(text, Language.AUTO_DETECT, Language.ENGLISH);
			if (response.startsWith("TranslateApiException:")) {
				throw new Exception();
			}
 			translatedText = Config.speech.get("TR_SUC").replace("<response>", Translate.execute(text, Language.AUTO_DETECT, Language.ENGLISH));
 		} catch (Exception e) {
			e.printStackTrace();
 			translatedText = Config.speech.get("TR_ERR");
 		}
 
 		event.getBot().sendMessage(event.getChannel(), translatedText);
 	}
 }
