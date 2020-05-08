 /**
  * 
  */
 package net.frontlinesms.plugins.translation;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import net.frontlinesms.events.EventObserver;
 import net.frontlinesms.events.FrontlineEventNotification;
 import net.frontlinesms.plugins.BasePluginThinletTabController;
 import net.frontlinesms.plugins.translation.ui.LanguagePropertiesHandler;
 import net.frontlinesms.plugins.translation.ui.TranslationSubmissionHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.events.TabChangedNotification;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.LanguageBundle;
 import thinlet.Thinlet;
 
 /**
  * @author Alex Anderson <alex@frontlinesms.com>
  * @author Morgan Belkadi <morgan@frontlinesms.com>
  */
 public class TranslationThinletTabController extends BasePluginThinletTabController<TranslationPluginController> implements EventObserver {
 
 	
 	//> STATIC CONSTANTS
 	/** Filename and path of the XML for the Translation tab. */
 	private static final String UI_FILE_TRANSLATE_DIALOG = "/ui/plugins/translation/dgTranslate.xml";
 
 	private static final String BUNDLE_PROPERTIES_PREFIX = "bundle.";
 	
 	private static final String I18N_TRANSLATION_DELETED = "plugins.translation.translation.file.deleted";
 	private static final String I18N_CONFIRM_RESTART = "plugins.translation.confirm.restart";
 	private static final String I18N_MESSAGE_TRANSLATION_TAB_LOADED = "plugins.translation.tab.loaded";
 	private static final String I18N_TRANSLATION_SAVED = "plugins.translation.translations.saved";
 	private static final String I18N_WARNING_TRANSLATIONS_NOT_SAVED = "plugins.translation.warning.translations.not.saved";
 	
 	private static final String UI_COMPONENT_BT_DELETE = "btDelete";
 	private static final String UI_COMPONENT_BT_EDIT = "btEdit";
 	private static final String UI_COMPONENT_BT_SAVE = "saveTranslations";
 	private static final String UI_COMPONENT_CL_CURRENT_LANGUAGE = "clCurrentLanguage";
 	private static final String UI_COMPONENT_LS_LANGUAGES = "lsLanguages";
 	private static final String UI_COMPONENT_PN_RESTART_FRONTLINE = "restartFrontline";
 	private static final String UI_COMPONENT_TF_TRANSLATION_FILTER = "tfTranslationFilter";
 	
 	private static final Object UI_TRANSLATION_TAB_NAME = ":translation";
 
 	private boolean shouldWarnWhenLostFocus = false;
 	
 
 //> INSTANCE VARIABLES
 	/** Current view in the translations tables tabs */
 	TranslationView visibleTab;
 	/**
 	 * List of all translation rows in each translation table.
 	 * For each {@link TranslationView}, this map will contain all rows that could be shown, even those which are currently filtered out.
 	 */
 	private Map<TranslationView, List<Object>> translationTableRows;
 	/** The dialog used for editing a translation.  When the dialog is not visible, this should be <code>null</code>. */
 	private Object editDialog;
 	/** The localized language file which we are currently editing/working on. */
 	MasterTranslationFile selectedLanguageFile;
 	/** The localized languages file which we are currently editing/working on. */
 	private Map<String, MasterTranslationFile> languageBundles;
 	/** The selected property in the current table. */
 	private String selectedProperty;
 
 	//> CONSTRUCTORS
 	protected TranslationThinletTabController(TranslationPluginController pluginController, UiGeneratorController uiController) {
 		super(pluginController, uiController);
 	}
 
 	public void init() {
 		this.ui.getFrontlineController().getEventBus().registerObserver(this);
 		this.visibleTab = TranslationView.ALL;
 		this.languageBundles = new HashMap<String, MasterTranslationFile>();
 		
 		refreshLanguageList();
 	}
 
 //> UI METHODS
 	/** Method called when the current translation tab is changed. */
 	public void tabChanged(int selectedTabIndex) {
 		this.visibleTab = TranslationView.getFromTabIndex(selectedTabIndex);
 	}
 
 	/** UI Event method: show the editor for the selected translation in the translation table. */
 	public void editText() {
 		System.out.println("TranslationThinletTabController.editText()");
 		String textKey = getSelectedTextKey(this.visibleTab);
 		String defaultValue = "";
 		try { defaultValue = MasterTranslationFile.getDefault().getValue(textKey); } catch(MissingResourceException ex) {};
 		
 		MasterTranslationFile selectedLanguageBundle = getSelectedLanguageBundle();
 		String localValue = "";
 		try { localValue = selectedLanguageBundle.getValue(textKey); } catch(MissingResourceException ex) {};
 		
 		// Load the dialog
 		this.editDialog = ui.loadComponentFromFile(UI_FILE_TRANSLATE_DIALOG, this);
 		ui.setText(ui.find(this.editDialog, "lbLocalTranslation"), selectedLanguageBundle.getLanguageName());
 		
 		// Initialize textfield values
 		ui.setText(ui.find(this.editDialog, "tfKey"), textKey);
 		ui.setText(ui.find(this.editDialog, "tfDefault"), defaultValue);
 		ui.setText(ui.find(this.editDialog, "tfLocal"), localValue);
 	
 		// Display the dialog
 		ui.add(this.editDialog);
 	}
 
 	/**
 	 * UI Event method: triggered when the user tries to delete the translation on the list.
 	 * The user is shown a dialog requesting that they confirm the action.
 	 */
 	public void confirmDeleteText() {
 		String textKey = getSelectedTextKey(this.visibleTab);
 		if(textKey != null) {
 			ui.showConfirmationDialog("deleteText('" + textKey + "')", this);
 		}
 		
 		removeEditDialog();
 	}
 	
 	/**
 	 * Method called when the user has confirmed his desire to delete a particular translation.
 	 * @param textKey The key of the translation to delete.
 	 * @throws IOException If there is a problem saving the translation file.
 	 */
 	public void deleteText(String textKey) throws IOException {
 		MasterTranslationFile languageBundle = this.getSelectedLanguageBundle();
 		try {
 			languageBundle.delete(textKey);
 		} catch (KeyNotFoundException e) {
 			throw new IllegalStateException("Could not delete text with key '" + textKey + "' because it does not exist.");
 		}
 		
 		if (!languageBundles.containsKey(languageBundle.getIdentifier())) {
 			languageBundles.put(languageBundle.getIdentifier(), languageBundle);
 		}
 		
 		refreshTables();
 		this.ui.setEnabled(this.ui.find(UI_COMPONENT_BT_SAVE), true);
 
 		ui.removeConfirmationDialog();
 	}
 	
 	/**
 	 * Method called when a translation has been edited.
 	 * @param textKey
 	 * @param textValue
 	 * @throws IOException
 	 */
 	public void propertyEdited(String textKey, String textValue) {
 		MasterTranslationFile languageBundle = this.getSelectedLanguageBundle();
 		languageBundle.add(textKey, textValue);
 		
 		if (!languageBundles.containsKey(languageBundle.getIdentifier())) {
 			languageBundles.put(languageBundle.getIdentifier(), languageBundle);
 		}
 		
 		removeEditDialog();
 		
 		this.refreshLanguagesAndReselect();
 		this.refreshTables();		
 		
 		this.ui.setEnabled(this.ui.find(UI_COMPONENT_BT_SAVE), true);
 		// TODO: Try to add focus on the selected line, so keyboard shortcut can be used
 	}
 	
 	/**
 	 * Refresh the languages list and reselect the previously selected item
 	 */
 	private void refreshLanguagesAndReselect() {
 		int selectedIndex = this.ui.getSelectedIndex(getLanguageList());
 		this.refreshLanguageList();
 		if (selectedIndex >= 0) {
 			this.ui.setSelectedIndex(getLanguageList(), selectedIndex);
 			this.languageSelectionChanged();
 		}
 	}
 
 	/**
 	 * Saves all edited translations in their respective files
 	 * @throws IOException
 	 */
 	public void saveTranslations () throws IOException {
 		// save all language bundles to disk
 		for (MasterTranslationFile languageBundle : languageBundles.values()) {
 			languageBundle.saveToDisk(InternationalisationUtils.getLanguageDirectory());
 		}
 		
 		this.ui.setEnabled(this.ui.find(UI_COMPONENT_BT_SAVE), false);
 		this.languageBundles.clear();
 		this.refreshLanguagesAndReselect();
 		this.ui.infoMessage(InternationalisationUtils.getI18NString(I18N_TRANSLATION_SAVED));
 		this.ui.setVisible(this.ui.find(UI_COMPONENT_PN_RESTART_FRONTLINE), true);
 	}
 	
 	/**
 	 * Removes the edit dialog.
 	 */
 	public void removeEditDialog() {
 		ui.remove(this.editDialog);
 		this.editDialog = null;
 	}
 	
 	/**
 	 * UI Event method: triggered when the user select a language on the left list.
 	 */
 	public void languageSelectionChanged() {
 		System.out.println("TranslationThinletTabController.languageSelectionChanged()");
 		this.refreshTables();
 		this.enableBottomButtons();
 		ui.setEnabled(getFilterTextfield(), true);
 	}
 	
 	/**
 	 * UI Event method: triggered when the user has finished editing a property.
 	 */
 	public void propertyItemChanged () {
 		this.selectedProperty  = this.getSelectedTextKey(this.visibleTab);
 		this.enableBottomButtons();
 	}
 	
 	/**
 	 * Enables or disables bottom buttons (Edit/Delete) functions of the selected property in the list
 	 */
 	public  void enableBottomButtons() {
 		Object btEdit = find(UI_COMPONENT_BT_EDIT);
 		Object btDelete = find(UI_COMPONENT_BT_DELETE);
 		boolean shouldEnable = (this.visibleTab != null && this.ui.getSelectedIndex(find(this.visibleTab.getTableName())) >= 0);
 		this.ui.setEnabled(btEdit, shouldEnable);
 		
 		if (shouldEnable) {
 			String propertyKey = this.getSelectedTextKey(this.visibleTab);
 			shouldEnable = this.getSelectedLanguageBundle().getProperties().containsKey(propertyKey);
 		}
 		
 		this.ui.setEnabled(btDelete, shouldEnable);
 	}
 
 	/**
 	 * UI Event method: triggered when the search changes.
 	 * @param filterText The text entered in the search bar
 	 */
 	public void filterTranslations(String filterText) {
 		System.out.println("TranslationThinletTabController.filterTranslations(" + filterText + ")");
 		filterTable(TranslationView.ALL);
 		filterTable(TranslationView.MISSING);
 	}
 	
 //>
 	/**
 	 * Filter the elements of a translation table, hiding all rows that do not match the filter text
 	 * and showing all that do.
 	 */
 	private void filterTable(TranslationView view) {
 		Object table = find(view.getTableName());
 		String filterText = getFilterText();
 		ui.removeAll(table);
 		List<Object> tableRows = this.translationTableRows.get(view);
 		int selectedPropertyIndex = -1;
 		if(tableRows != null) {
 			for(int i = 0 ; i < tableRows.size() ; ++i) {
 				Object tableRow = tableRows.get(i);
 				
 				boolean show = rowMatches(tableRow, filterText);
 				if(show) {
 					ui.add(table, tableRow);
 					if (this.visibleTab.equals(TranslationView.ALL) && this.ui.getAttachedObject(tableRow, String.class).equals(this.selectedProperty)) {
 						selectedPropertyIndex = i;
 					}
 				}
 			}
 			// if this.selectedProperty has been set before, selectedPropertyIndex should be positive
 			this.ui.setSelectedIndex(table, selectedPropertyIndex);
 		}
 	}
 
 	/**
 	 * Check if any column in the given table row matches our filter text.
 	 * @param row Thinlet ROW element.
 	 * @param filterText Text to filter with
 	 * @return <code>true</code> if the filter text is contained in any column in the supplied row; <code>false</code> otherwise.
 	 */
 	private boolean rowMatches(Object row, String filterText) {
 		assert(Thinlet.getClass(row).equals(Thinlet.ROW)) : "This method is only applicable to Thinlet <row/> components.";
 		
 		if(filterText.length() == 0) {
 			// If the filter text is empty, there is no need to check - it will match everything!
 			return true;
 		}
 		for(Object col : ui.getItems(row)) {
 			if(ui.getText(col).toLowerCase().contains(filterText.toLowerCase())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Return the text key selected in the table, or <code>null</code> if none is selected.
 	 * @param table
 	 * @return
 	 */
 	private String getSelectedTextKey(TranslationView view) {
 		Object selectedItem = getSelectedTableItem(view);
 		if(selectedItem == null) return null;
 		String selectedKey = ui.getAttachedObject(selectedItem, String.class);
 		return selectedKey;
 	}
 
 	/**
 	 * Gets the selected table item in the right pane
 	 * @param view
 	 * @return
 	 */
 	private Object getSelectedTableItem(TranslationView view) {
 		return ui.getSelectedItem(find(view.getTableName()));
 	}
 	
 	/**
 	 * Prepare lists of all and missing translations
 	 */
 	private void refreshTables() {
 		this.translationTableRows = new HashMap<TranslationView, List<Object>>();
 		
 		if (getSelectedLanguageBundle() == null) {
 			ArrayList<Object> emptyList = new ArrayList<Object>();
 			this.translationTableRows.put(TranslationView.ALL, emptyList);
 			this.translationTableRows.put(TranslationView.MISSING, emptyList);
 		} else {
 			MasterTranslationFile lang = getSelectedLanguageBundle();
 			MasterTranslationFile defaultLang = MasterTranslationFile.getDefault();
 			Comparator<Object> comparator = new PropertyRowComparator(this.ui);  
 			
 			// Generate the "all" table rows
 			ArrayList<Object> allRows = new ArrayList<Object>(defaultLang.getProperties().size());
 			for(Entry<String, String> defaultEntry : defaultLang.getProperties().entrySet()) {
 				String key = defaultEntry.getKey();
 				if (key.startsWith(BUNDLE_PROPERTIES_PREFIX)) {
 					continue;
 				}
 				
 				String langValue = null;
 				try {
 					langValue = lang.getValue(key);
 				} catch(MissingResourceException ex) {
 					langValue = "";
 				}
 				Object tableRow = createTableRow(key, defaultEntry.getValue(), langValue);
 				allRows.add(tableRow);
 			}
 			
 			Collections.sort(allRows, comparator);
 			this.translationTableRows.put(TranslationView.ALL, allRows);
 			
 			LanguageBundleComparison comp = new LanguageBundleComparison(defaultLang, lang);
 			
 			// populate and enable the missing table
 			Set<String> missingKeys = comp.getKeysIn1Only();
 			ArrayList<Object> missingRows = new ArrayList<Object>(missingKeys.size());
 			for(String key : missingKeys) {
 				if (key.startsWith(BUNDLE_PROPERTIES_PREFIX)) {
 					continue;
 				}
 				Object tableRow = createTableRow(key, comp.get1(key), "");
 				missingRows.add(tableRow);
 			}
 			
 			Collections.sort(missingRows, comparator);
 			this.translationTableRows.put(TranslationView.MISSING, missingRows);
 		}
 		
 		initTable(TranslationView.ALL);
 		initTable(TranslationView.MISSING);
 	}
 	
 	/**
 	 * Inits the table header, functions of the current language
 	 * @param view
 	 */
 	private void initTable(TranslationView view) {
 		if (getSelectedLanguageBundle() != null) {
 			Object table = find(view.getTableName());
 			ui.setText(ui.find(table, UI_COMPONENT_CL_CURRENT_LANGUAGE), getSelectedLanguageBundle().getLanguageName());
 		}
 		filterTable(view);
 	}
 	
 	/**
 	 * Reload FrontlineSMS UI, with or without confirmation (if changes are pending or not)
 	 */
 	public void restartFrontlineSMS () {
 		if (this.ui.isEnabled(find(UI_COMPONENT_BT_SAVE))) {
 			// This is the easiest way to check if some changes are pending
 			this.ui.showConfirmationDialog("reloadUi", I18N_CONFIRM_RESTART);
 		} else {
 			this.ui.reloadUi();
 		}
 	}
 	
 	/**
 	 * Opens a dialog for creating a new translation (new file/language)
 	 */
 	public void createTranslation () {
 		LanguagePropertiesHandler handler = new LanguagePropertiesHandler(this.ui, this);
 		handler.initDialog();
 		this.ui.add(handler.getDialog());
 	}
 	
 	/**
 	 * Edit the language properties (language name, code and country code)
 	 * UI event triggered when double clicking on the language or when selecting "Properties" in the menuitem in the list
 	 */
 	public void editProperties () {
 		MasterTranslationFile languageBundle = this.getSelectedLanguageBundle();
 		if (languageBundle != null) {
 			LanguagePropertiesHandler handler = new LanguagePropertiesHandler(this.ui, this);
 			handler.populate(languageBundle);
 			handler.initDialog();
 			
 			this.ui.add(handler.getDialog());	
 		}
 	}
 	
 	/**
 	 * Delete the current translation, by deleting the bundle file and the cached lists
 	 */
 	public void deleteTranslation () {
 		MasterTranslationFile languageBundle = this.getSelectedLanguageBundle();
 		this.ui.removeConfirmationDialog();
 		
 		if (languageBundle != null) {
 			// We remove the MasterTranslationFile from the current editing bundles, if it is in
 			languageBundles.remove(languageBundle.getIdentifier());
 			
 			// Then we remove the file
			if (new File(InternationalisationUtils.getLanguageDirectory() + languageBundle.getFilename()).delete()) {
 				this.ui.infoMessage(InternationalisationUtils.getI18NString(I18N_TRANSLATION_DELETED));
 			}
 			
 			// And we refresh
 			this.refreshLanguageList();
 			this.languageSelectionChanged(); // Nothing selected
 		}
 	}
 	
 	/**
 	 * Edit the language properties (language name, code and country code)
 	 */
 	public void submitTranslation () {
 		MasterTranslationFile languageBundle = this.getSelectedLanguageBundle();
 		if (languageBundle != null) {
 			TranslationSubmissionHandler handler = new TranslationSubmissionHandler(this.ui, languageBundle);
 			handler.initDialog();
 			
 			this.ui.add(handler.getDialog());	
 		}
 	}
 
 	/**
 	 * Creates a Thinlet table row for a translation.  The first column value, i.e. 
 	 * the translation key, is attached, to the row, as well as appearing in the first
 	 * column.
 	 * @param columnValues
 	 * @return
 	 */
 	private Object createTableRow(String... columnValues) {
 		assert(columnValues.length > 0) : "The translation key should be provided as the first column value.";
 		Object row = ui.createTableRow(columnValues[0]);
 		String languageFileIdentifier = ui.getAttachedObject(ui.getSelectedItem(getLanguageList()), String.class);
 		
 		boolean hasBeenEdited = languageBundles.containsKey(languageFileIdentifier) && this.getSelectedLanguageBundle().hasBeenEdited(columnValues[0]); 
 		for(int i = 0 ; i < columnValues.length ; ++i) {
 			String col = columnValues[i];
 			ui.add(row, ui.createTableCell(col, hasBeenEdited));
 		}
 		return row;
 	}
 
 //> INSTANCE HELPER METHODS
 	/** Refresh language list on the left pane */
 	public void refreshLanguageList() {
 		// Refresh language list
 		Object languageList = getLanguageList();
 		super.removeAll(languageList);
 		
 		for (MasterTranslationFile languageBundle : MasterTranslationFile.getAll()) {
 			boolean shouldBeBold = languageBundles.containsKey(languageBundle.getIdentifier());
 			Object item = ui.createListItem(languageBundle.getLanguageName(), languageBundle.getIdentifier(), shouldBeBold);
 			ui.setIcon(item, ui.getFlagIcon(languageBundle));
 			ui.add(languageList, item);
 		}
 	}
 	
 //> UI ACCESSORS
 	/**
 	 * Gets the language list component on the left pane
 	 */
 	private Object getLanguageList() {
 		return super.find(UI_COMPONENT_LS_LANGUAGES);
 	}
 	
 	/**
 	 * Gets the selected language bundle as a {@link MasterTranslationFile} 
 	 * @return A new {@link MasterTranslationFile} if the selected language is not stored in the {@link #languageBundles}, the stored
 	 * {@link MasterTranslationFile} if it is, or <code>null</code> if no item was selected
 	 */
 	private synchronized MasterTranslationFile getSelectedLanguageBundle() {
 		if (ui.getSelectedItem(getLanguageList()) == null) {
 			return null;
 		}
 		
 		String languageFileIdentifier = ui.getAttachedObject(ui.getSelectedItem(getLanguageList()), String.class);
 		if (languageBundles.containsKey(languageFileIdentifier)) {
 			return languageBundles.get(languageFileIdentifier);
 		} else {
 			return MasterTranslationFile.getFromIdentifier(languageFileIdentifier);
 		}
 	}
 	
 	/**
 	 * Gets the text typed in the search field
 	 * @return The search text
 	 */
 	private String getFilterText() {
 		return ui.getText(getFilterTextfield());
 	}
 
 	/**
 	 * Gets the search text field component 
 	 * @return
 	 */
 	private Object getFilterTextfield() {
 		return find(UI_COMPONENT_TF_TRANSLATION_FILTER);
 	}
 	
 	/**
 	 * Creates a new translation file
 	 * @param languageName The name of the language, <code>in the original language</code>
 	 * @param isoCode The ISO 639-1 Code for this language
 	 * @param countryCode The country code for the flag representing the country
 	 * @param baseLanguageCode The country code of the language file used as a base for this new translation file (not required)
 	 * @throws IOException
 	 */
 	public void createNewLanguageFile(String languageName, String isoCode, String countryCode, Object baseLanguageCode, String fontNames) throws IOException {
 		FileOutputStream fos = null;
 		OutputStreamWriter osw = null;
 		PrintWriter out = null;
 		String newFilename = "frontlineSMS_" + isoCode + ".properties";
		File newFile = new File(InternationalisationUtils.getLanguageDirectory(), newFilename);
 		MasterTranslationFile languageBundle = MasterTranslationFile.getFromLanguageCode(this.ui.getAttachedObject(baseLanguageCode, String.class));
 		
 		if (languageBundle != null) {
 			// A base language file is used, let's copy the values
 			MasterTranslationFile newLanguageBundle = new MasterTranslationFile(languageBundle.getFilename(), languageBundle.getTranslationFiles());
 			newLanguageBundle.setFilename(newFilename);
 			newLanguageBundle.setCountry(countryCode);
 			newLanguageBundle.setLanguageName(languageName);
 			newLanguageBundle.setLanguageCode(isoCode);
 			newLanguageBundle.setLanguageFont(fontNames);
 			
 			newLanguageBundle.saveToDisk(InternationalisationUtils.getLanguageDirectory());
 		} else {
 			try {
 				fos = new FileOutputStream(newFile);
 				osw = new OutputStreamWriter(fos, InternationalisationUtils.CHARSET_UTF8);
 				out = new PrintWriter(osw);
 				out.write("# The 2-letter ISO-? code for the language\n" +
 						LanguageBundle.KEY_LANGUAGE_CODE + "=" + isoCode + "\n" +
 						"# The name of the language IN THAT LANGUAGE - this is how the language will be chosen from\n" +
 						"# menus, so it's important that speakers of that language actually understand this.  If the\n" +
 						"# language name is in a non-Latin alphabet, a latinised or English version of the language\n" +
 						"# name should also be provided, as some fonts will not display the non-Latin version\n" +
 						LanguageBundle.KEY_LANGUAGE_NAME + "=" + languageName + "\n" +
 						"# 2-letter ISO-? code for the country where they speak this language.  This is used to get\n" +
 						"# a flag to represent this language\n" +
 						LanguageBundle.KEY_LANGUAGE_COUNTRY + "=" + countryCode + "\n");
 				if (fontNames != null) {
 					out.write("# The fonts used to correctly display this language\n" +
 							LanguageBundle.KEY_LANGUAGE_FONT + "=" + fontNames + "\n");
 				}
 			} finally {
 				if(out != null) out.close();
 				if(osw != null) try { osw.close(); } catch(IOException ex) {}
 				if(fos != null) try { fos.close(); } catch(IOException ex) {}
 			}
 		}
 		
 		this.refreshLanguageList();
 		this.selectLanguageFromFilename(newFilename);
 	}
 
 	/**
 	 * Updates the properties of a language
 	 * @param originalLanguageBundle The previous properties
 	 * @param languageName The name of the language, <code>in the original language</code>
 	 * @param isoCode The ISO 639-1 Code for this language
 	 * @param countryCode The country code for the flag representing the country
 	 * @throws IOException
 	 */
 	public void updateTranslationFile(MasterTranslationFile originalLanguageBundle, String languageName, String isoCode, String countryCode, String fontNames) throws IOException {
 		MasterTranslationFile newLanguageBundle = new MasterTranslationFile(originalLanguageBundle.getFilename(), originalLanguageBundle.getTranslationFiles());
 		
 		newLanguageBundle.setCountry(countryCode);
 		newLanguageBundle.setLanguageName(languageName);
 		newLanguageBundle.setLanguageCode(isoCode);
 		newLanguageBundle.setLanguageFont(fontNames);
 		
 		newLanguageBundle.saveToDisk(InternationalisationUtils.getLanguageDirectory());
 		
 		if (!isoCode.equals(originalLanguageBundle.getLanguageCode())) {
 			// If the ISO code has changed during the editing, we have to rename the file
 			File oldFile = new File(InternationalisationUtils.getLanguageDirectory(), originalLanguageBundle.getFilename());
 			String newFilename = "frontlineSMS_" + isoCode + ".properties";
 			File newFile = new File(InternationalisationUtils.getLanguageDirectory(), newFilename);
 			newLanguageBundle.setFilename(newFilename);
 			oldFile.renameTo(newFile);
 		}
 		
 		// If the Bundle was editing (shouldn't happen), we put the updated version in the map
 		if (this.languageBundles.remove(originalLanguageBundle.getIdentifier()) != null) {
 			this.languageBundles.put(newLanguageBundle.getIdentifier(), newLanguageBundle);
 		}
 		
 		this.saveTranslations();
 	}
 
 	/**
 	 * Selects a language in the language list by giving the language code
 	 * @param languageName
 	 */
 	private void selectLanguageFromFilename(String languageName) {
 		for (Object item : this.ui.getItems(this.getLanguageList())) {
 			String languageIdentifier = this.ui.getAttachedObject(item, String.class);
 			if (languageIdentifier != null && languageIdentifier.equals(MasterTranslationFile.getIdentifier(languageName))) {
 				this.ui.setSelectedItem(this.getLanguageList(), item);
 				this.languageSelectionChanged();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Warn the user if he changes to another tab and has unsaved changes 
 	 */
 	public void notify(FrontlineEventNotification notification) {
 		// This object is registered to the UIGeneratorController and get notified when the users changes tab
 		if(notification instanceof TabChangedNotification) {
 			String newTabName = ((TabChangedNotification) notification).getNewTabName();
 			if (!newTabName.equals(UI_TRANSLATION_TAB_NAME)) {
 				if (this.shouldWarnWhenLostFocus) {
 					// Focus lost
 					if (this.languageBundles.size() > 0) {
 						// Then this means we're currently editing some translations, which need to be saved
 						this.ui.alert(InternationalisationUtils.getI18NString(I18N_WARNING_TRANSLATIONS_NOT_SAVED));
 					}
 				}
 			} else {
 				this.ui.setStatus(InternationalisationUtils.getI18NString(I18N_MESSAGE_TRANSLATION_TAB_LOADED));
 			}
 			this.shouldWarnWhenLostFocus = (newTabName.equals(UI_TRANSLATION_TAB_NAME));
 		}
 	}
 	
 	/**
 	 * Classe used to sort the properties alphabetically in the lists
 	 * @author Morgan Belkadi <morgan@frontlinesms.com>
 	 */
 	public class PropertyRowComparator implements Comparator<Object> {
 		/** Instance of {@link UiGeneratorController} used to get the attached objects */
 		private UiGeneratorController ui;
 
 		public PropertyRowComparator (UiGeneratorController ui) {
 			this.ui = ui;
 		}
 		
 		/**
 		 * Inherited compare method from Comparator interface
 		 * Comparison is made on the attached object (String) 
 		 */
 		@SuppressWarnings("unchecked")
 		public int compare(Object o1, Object o2) {
 			return ((Comparable<Object>)this.ui.getAttachedObject(o1)).compareTo(this.ui.getAttachedObject(o2));
 		}
 	}
 }
 
 enum TranslationView {
 	ALL("tbAll", 0, "tbAllTranslations"),
 	MISSING("tbMissing", 1, "tbMissingTranslations");
 	
 	private final String tabName;
 	private final int tabIndex;
 	private final String tableName;
 	
 //> CONSTRUCTOR
 	private TranslationView(String tabName, int tabIndex, String tableName) {
 		this.tabName = tabName;
 		this.tabIndex = tabIndex;
 		this.tableName = tableName;
 	}
 
 //> ACCESSORS
 	public String getTabName() {
 		return tabName;
 	}
 
 	public int getTabIndex() {
 		return tabIndex;
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 	
 //> STATIC METHODS
 	static TranslationView getFromTabIndex(int tabIndex) {
 		for(TranslationView view : TranslationView.values()) {
 			if(view.getTabIndex() == tabIndex) {
 				return view;
 			}
 		}
 		// No match was found
 		return null;
 	}
 }
