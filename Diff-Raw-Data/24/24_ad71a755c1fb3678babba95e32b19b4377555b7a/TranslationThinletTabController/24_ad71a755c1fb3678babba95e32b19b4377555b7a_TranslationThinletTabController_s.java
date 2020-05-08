 /**
  * 
  */
 package net.frontlinesms.plugins.translation;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.MissingResourceException;
 import java.util.Map.Entry;
 
 import net.frontlinesms.plugins.BasePluginThinletTabController;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.LanguageBundle;
 
 /**
  * @author alex
  */
 public class TranslationThinletTabController extends BasePluginThinletTabController<TranslationPluginController> {
 
 //> CONSTRUCTORS
 	protected TranslationThinletTabController(TranslationPluginController pluginController, UiGeneratorController uiController) {
 		super(pluginController, uiController);
 	}
 
 	public void init() {
 		refreshLanguageList();
 	}
 
 //> UI METHODS
 	public void tabChanged() {
 		System.out.println("TranslationThinletTabController.tabChanged()");
 	}
 	public void editText(Object table) {
 		System.out.println("TranslationThinletTabController.editText()");
 	}
 	public void deleteText(Object table) {
		System.out
				.println("TranslationThinletTabController.languageSelectionChanged()");
 	}
 	public void languageSelectionChanged() {
 		System.out.println("TranslationThinletTabController.languageSelectionChanged()");
 		refreshTables();
 	}
 	
 	private void refreshTables() {
 		LanguageBundle lang = getSelectedLanguageBundle();
 		LanguageBundle defaultLang = getDefaultLanguageBundle();
 		
 		// TODO Populate the "all" table
 		Object allTable = find("tbAllTranslations");
 		removeAll(allTable);
 		uiController.setText(uiController.find(allTable, "clCurrentLanguage"), lang.getLanguageName());
 		for(Entry<String, String> defaultEntry : defaultLang.getProperties().entrySet()) {
 			String key = defaultEntry.getKey();
 			String langValue = null;
 			try {
 				langValue = lang.getValue(key);
 			} catch(MissingResourceException ex) {
 				langValue = "";
 			}
 			Object tableRow = createTableRow(key, defaultEntry.getValue(), langValue);
 			uiController.add(allTable, tableRow);
 		}
 		
 		LanguageBundleComparison comp = new LanguageBundleComparison(defaultLang, lang);
 		boolean isDefaultLang = lang.equals(defaultLang);
 		uiController.setEnabled(find("tbMissing"), !isDefaultLang);
 		uiController.setEnabled(find("tbExtra"), !isDefaultLang);
 		if(isDefaultLang) {
 			// Select the 'all' tab if we are viewing default bundle, as the others are disabled 
 			uiController.setSelected(find("tbAll"), true);
 		} else {
 			// populate and enable the missing table
 			Object missingTable = find("tbMissingTranslations");
 			removeAll(missingTable);
 			uiController.setText(uiController.find(missingTable, "clCurrentLanguage"), lang.getLanguageName());
 			for(String key : comp.getKeysIn1Only()) {
 				Object tableRow = createTableRow(key, comp.get1(key), "");
 				uiController.add(missingTable, tableRow);
 			}
 
 			// populate and enable the extra table
 			Object extraTable = find("tbExtraTranslations");
 			removeAll(extraTable);
 			uiController.setText(uiController.find(extraTable, "clCurrentLanguage"), lang.getLanguageName());
 			for(String key : comp.getKeysIn2Only()) {
 				Object tableRow = createTableRow(key, "", comp.get2(key));
 				uiController.add(extraTable, tableRow);
 			}
 		}
 	}
 	
 	private Object createTableRow(String... columnValues) {
 		Object row = uiController.createTableRow(null);
 		for(String col : columnValues) {
 			uiController.add(row, uiController.createTableCell(col));
 		}
 		return row;
 	}
 
 //> INSTANCE HELPER METHODS
 	/** Refresh UI elements */
 	private void refreshLanguageList() {
 		// Refresh language list
 		Object languageList = getLanguageList();
 		super.removeAll(languageList);
 		for (LanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
 			Object item = uiController.createListItem(languageBundle.getLanguageName(), languageBundle.getFilename());
 			uiController.setIcon(item, uiController.getFlagIcon(languageBundle));
 			int index = -1;
 			if (languageBundle.getCountry().equals("gb")) {
 				index = 0;
 			}
 			uiController.add(languageList, item, index);
 		}
 	}
 	
 //> UI ACCESSORS
 	private Object getLanguageList() {
 		return super.find("lsLanguages");
 	}
 	
 	private LanguageBundle getSelectedLanguageBundle() {
 		String languageFilename = uiController.getAttachedObject(uiController.getSelectedItem(getLanguageList()), String.class);
 		File languageFile = new File(InternationalisationUtils.getLanguageDirectoryPath() + languageFilename);
 		return InternationalisationUtils.getLanguageBundle(languageFile);
 	}
 	
 	private LanguageBundle getDefaultLanguageBundle() {
 		try {
 			return InternationalisationUtils.getDefaultLanguageBundle();
 		} catch (IOException e) {
 			throw new IllegalStateException("There was a problem loading the default language bundle.");
 		}
 	}
 }
