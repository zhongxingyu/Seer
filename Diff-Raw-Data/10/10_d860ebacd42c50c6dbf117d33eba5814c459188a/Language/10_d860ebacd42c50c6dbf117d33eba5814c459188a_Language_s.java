 package app;
 
 import java.util.Hashtable;
 import java.util.Set;
 
 final class Language
 {
 	private static Hashtable<String, String> _texts = new Hashtable<String, String>();
 
 	public static void load(int languageId)
 	{
 		_texts.clear();
 
 		switch (languageId) {
 			case 1:
 				_texts.put("err_missing_config", "Einstellung com.jagex.config fehlt");
 				_texts.put("err_invalid_config", "Einstellung com.jagex.config ist ungültig");
 				_texts.put("loading_config", "Lade Konfiguration");
 				_texts.put("err_load_config", "Fehler beim Laden der Konfiguration");
 				_texts.put("err_decode_config", "Fehler beim Entschlüsseln der Konfiguration");
 				_texts.put("loaderbox_initial", "Lade...");
 				_texts.put("error", "Fehler");
 				_texts.put("quit", "Beenden");
 				break;
 			case 2:
 				_texts.put("err_missing_config", "Paramètre com.jagex.config manquant");
 				_texts.put("err_invalid_config", "Paramètre com.jagex.config non valide");
 				_texts.put("loading_config", "Chargement de la configuration");
 				_texts.put("err_load_config", "Erreur de chargement de configuration");
 				_texts.put("err_decode_config", "Erreur de décodage de configuration");
 				_texts.put("loaderbox_initial", "Chargement...");
 				_texts.put("error", "Erreur");
 				_texts.put("quit", "Quitter");
 				break;
 			case 3:
				_texts.put("err_missing_config", "Faltando configurao de com.jagex.config");
				_texts.put("err_invalid_config", "Configurao invlida de com.jagex.config");
				_texts.put("loading_config", "Carregando configurao");
				_texts.put("err_load_config", "Erro ao carregar configurao");
				_texts.put("err_decode_config", "Erro ao decodificar configurao");
 				_texts.put("loaderbox_initial", "Carregando...");
 				_texts.put("error", "Erro");
 				_texts.put("quit", "Fechar");
 				break;
 			default:
 				_texts.put("err_missing_config", "Missing com.jagex.config setting");
 				_texts.put("err_invalid_config", "Invalid com.jagex.config setting");
 				_texts.put("loading_config", "Loading configuration");
 				_texts.put("err_load_config", "Error loading configuration");
 				_texts.put("err_decode_config", "Error decoding configuration");
 				_texts.put("loaderbox_initial", "Loading...");
 				_texts.put("error", "Error");
 				_texts.put("quit", "Quit");
 				break;
 		}
 	}
 
 	public static String getText(String alias)
 	{
 		String text = _texts.get(alias);
 		if (text == null) {
 			return alias.toUpperCase();
 		}
 		return text;
 	}
 
 	public static void setText(String alias, String text)
 	{
 		_texts.put(alias, text);
 	}
 
 	public static Set<String> aliases()
 	{
 		return _texts.keySet();
 	}
 }
 
 /*
  * Location: \\.psf\Home\Documents\java\jagexappletviewer\ Qualified Name:
  * app.Class_r JD-Core Version: 0.5.4
  */
