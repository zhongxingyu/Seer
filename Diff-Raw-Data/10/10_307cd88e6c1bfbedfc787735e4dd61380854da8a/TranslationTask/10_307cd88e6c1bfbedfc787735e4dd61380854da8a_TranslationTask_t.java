 package com.ese2013.mub.util;
 
 import java.util.Collection;
 
 import android.os.AsyncTask;
 
 import com.ese2013.mub.model.Menu;
 import com.memetix.mst.language.Language;
 import com.memetix.mst.translate.Translate;
 
 public class TranslationTask extends AsyncTask<Void, Void, Void> {
 	private Exception exception;
 	private Language newLang;
 	private Collection<Menu> menus;
 	private String[] newTitles, newDescriptions;
	private static final String NEW_LINE = " ; ";

 	public TranslationTask(Collection<Menu> menus, Language newLang) {
 		Translate.setClientId("MensaUniBe");
 		Translate.setClientSecret("T35oR9q6ukB/GbuYAg4nsL09yRsp9j5afWjULfWfmuY=");
 
 		this.newLang = newLang;
 		this.menus = menus;
 	}
 
 	@Override
 	protected Void doInBackground(Void... arg0) {
 		String[] menuTitles = new String[menus.size()];
 		String[] descriptions = new String[menus.size()];
 
 		int i = 0;
 		for (Menu menu : menus) {
 			menuTitles[i] = menu.getTitle();
 			descriptions[i] = menu.getDescription().replace("\n", NEW_LINE).replace("\"", "'");
 			i++;
 		}
 
 		try {
 			newTitles = Translate.execute(menuTitles, Language.GERMAN, newLang);
 			newDescriptions = Translate.execute(descriptions, Language.GERMAN, newLang);

 		} catch (Exception e) {
 			// Api throws general exception...
 			e.printStackTrace();
 			exception = e;
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(Void arg0) {
 		super.onPostExecute(arg0);
 		if (exception == null) {
 			int i = 0;
 			for (Menu menu : menus) {
 				menu.setTitle(newTitles[i]);
				menu.setDescription(newDescriptions[i].replace(NEW_LINE.trim(), "\n").replace("'", "\""));
 				i++;
 			}
 		} else {
 			// TODO handle error
 			exception.printStackTrace();
 		}
 	}
 }
