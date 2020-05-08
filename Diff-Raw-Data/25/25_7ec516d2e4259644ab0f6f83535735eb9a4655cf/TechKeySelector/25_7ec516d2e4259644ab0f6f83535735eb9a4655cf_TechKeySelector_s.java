 package editor.tech;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ParserKeys;
 import linewars.configfilehandler.ConfigData.NoSuchKeyException;
 import editor.BigFrameworkGuy;
 import editor.ConfigurationEditor;
 import editor.ListURISelector;
 import editor.ListURISelector.ListSelectorOptions;
 
 /**
  * 
  * @author Taylor Bergquist
  *
  */
 public class TechKeySelector implements ConfigurationEditor {
 	
 	private HashMap<ParserKeys, ConfigData> modifiers;
 	private String URI;
 	
 	private JPanel panel;
 	
 	private ListURISelector keySelector;
 
 	private ParserKeys[] currentlyHighlightedKeys;
 	
 	private ModifierEditor modEditor;
 	
 	private ListSelectorOptions keySelectorOptions = new ListSelectorOptions(){
 
 		@Override
 		public String[] getOptions() {
 			ParserKeys[] enumForm = modEditor.getModifiableKeysForURI(URI);
 			String[] ret = new String[enumForm.length];
 			for(int i = 0; i < ret.length; i++){
 				ret[i] = enumForm[i].toString();
 			}
 			return ret;
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			ParserKeys key = ParserKeys.valueOf(uri);
 			modifiers.put(key, new ConfigData());
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
 			ParserKeys key = ParserKeys.valueOf(uri);
 			modifiers.remove(key);
 			currentlyHighlightedKeys = new ParserKeys[0];
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 			currentlyHighlightedKeys = new ParserKeys[uris.length];
 			for(int i = 0; i < uris.length; i++){
				currentlyHighlightedKeys[i] = ParserKeys.valueOf(uris[i]);
 			}
 			updateCurrentlyHighlighted();
 		}
 	};
 
 	public TechKeySelector(BigFrameworkGuy framework) {
 		//init sub editor
 		modEditor = new ModifierEditor(this, framework);
 		
 		panel = new JPanel();
 		panel.setLayout(new GridLayout(2, 1));
 
 		keySelector = new ListURISelector("Key", keySelectorOptions);
 		panel.add(keySelector);
 		
 		//set up sub editor's space
 		panel.add(modEditor.getPanel());
 		modEditor.getPanel().setVisible(false);
 		
 		reset();
 	}
 
 	protected void updateCurrentlyHighlighted() {
 		if(currentlyHighlightedKeys.length != 1){
 			modEditor.reset();
 			modEditor.getPanel().setVisible(false);
 		}else{
 			//save data currently in the modEditor
 			saveModData();
 			ConfigData toModify = modifiers.get(currentlyHighlightedKeys[0]);
 			if(toModify == null){
 				toModify = new ConfigData();
 			}
 			toModify.set(ParserKeys.key, currentlyHighlightedKeys[0].toString());
 			modEditor.forceSetData(modifiers.get(currentlyHighlightedKeys[0]));
 			modEditor.getPanel().setVisible(true);
 		}
 	}
 
 	@Override
 	public void setData(ConfigData cd) {
 		reset();
 		//get the uri; if this fails, fail
 		URI = cd.getString(ParserKeys.URI);
 
 		//check and load the modifiedKeys
 		List<ConfigData> modifiedKeys = cd.getConfigList(ParserKeys.modifiedKey);
 		for(ConfigData modifier : modifiedKeys){
 			//get the key being modified; if this isn't there or isn't a key, we're boned
 			String strKey = modifier.getString(ParserKeys.key);
 			ParserKeys key = ParserKeys.getKey(strKey);
 			//check that it is valid
 			if(!modEditor.modifierIsValid(URI, modifier)){
 				throw new IllegalArgumentException("Uninformative message does not inform.");
 			}
 			//add it to modifiers
 			modifiers.put(key, modifier);
 			//add it to keySelector
 			keySelector.setSelectedURIs(addStringToArray(keySelector.getSelectedURIs(), strKey));
 		}
 	}
 
 	@Override
 	public void forceSetData(ConfigData cd) {
 		reset();
 		//get the uri; if this fails, fail
 		URI = cd.getString(ParserKeys.URI);
 
 		//check and load the modifiedKeys
 		List<ConfigData> modifiedKeys = null;
 		try{
 			modifiedKeys = cd.getConfigList(ParserKeys.modifiedKey);			
 		}catch(NoSuchKeyException e){
 			//then nothing is defined, and we are done!
 			return;
 		}
 		for(ConfigData modifier : modifiedKeys){
 			//get the key being modified; if this isn't there or isn't a key, we're boned
 			String strKey = modifier.getString(ParserKeys.key);
 			ParserKeys key = ParserKeys.getKey(strKey);
 			
 			if(!modEditor.legalizeModifier(URI, modifier)){//if it isn't legal
 				continue;//ignore it, don't use it
 			}
 			//add it to modifiers
 			modifiers.put(key, modifier);
 			//add it to keySelector
 			keySelector.setSelectedURIs(addStringToArray(keySelector.getSelectedURIs(), strKey));
 		}
 	}
 
 	@Override
 	public void reset() {
 		//reset state
 		modifiers = new HashMap<ParserKeys, ConfigData>();
 		URI = null;
 		
 		//reset display
 		currentlyHighlightedKeys = new ParserKeys[0];
 		keySelector.setSelectedURIs(new String[0]);
 		
 		//reset child panel
 		modEditor.reset();
 		
 		modEditor.getPanel().setVisible(false);
 	}
 
 	@Override
 	public ConfigData getData() {
 		//save data currently in the ModifierEditor
 		saveModData();
 		
 		ConfigData ret = new ConfigData();
 		ret.add(ParserKeys.URI, URI);
 		for(Entry<ParserKeys, ConfigData> modifiedKey : modifiers.entrySet()){
 			modifiedKey.getValue().set(ParserKeys.key, modifiedKey.getKey().toString());
 			ret.add(ParserKeys.modifiedKey, modifiedKey.getValue());
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean isValidConfig() {
 		//make sure there is a URI
 		if(URI == null){
 			return false;
 		}
 		
 		//make sure at least one key is being modified
 		if(modifiers == null){
 			return false;
 		}
 		if(modifiers.keySet().size() == 0){
 			return false;
 		}
 		
 		//make sure that all modifications are themselves valid
 		ParserKeys[] definedKeys = modEditor.getModifiableKeysForURI(URI);
 		for(Entry<ParserKeys, ConfigData> currentEntry : modifiers.entrySet()){
 			//make sure this ParserKeys is defined for this type of upgradable
 			boolean isDefined = false;
 			for(int i = 0; i < definedKeys.length; i++){
 				if(definedKeys[i].equals(currentEntry.getKey())){
 					isDefined = true;
 				}
 			}
 			if(!isDefined){
 				return false;
 			}
 			
 			//make sure this ConfigData is a valid modification
 			if(!modEditor.modifierIsValid(URI, currentEntry.getValue())){
 				return false;
 			}
 		}
 		return true;//ran the gauntlet, yay!
 	}
 
 	@Override
 	public ParserKeys getType() {
 		return ParserKeys.modifiedKey;
 	}
 
 	@Override
 	public JPanel getPanel() {
 		return panel;
 	}
 	
 	String getCurrentURI(){
 		return URI;
 	}
 
 	
 	//returns true if the modification can be legalized
 	//modifies it so it is legal if it can be made so
 	boolean legalizeUpgradableModification(ConfigData modifiedUpgradable) {
 		String uri = null;
 		try{
 			uri = modifiedUpgradable.getString(ParserKeys.URI);
 		}catch(NoSuchKeyException e){
 			return false;
 		}
 		
 		//If nothing is modified for this URI, this is easy!
 		List<ConfigData> modifiedKeys = null;
 		try{
 			modifiedKeys = modifiedUpgradable.getConfigList(ParserKeys.modifiedKey);
 		}catch(NoSuchKeyException e){
 			return true;
 		}
 		
 		ArrayList<ConfigData> legalModifications = new ArrayList<ConfigData>();
 		//Keep only the legal modifications
 		for(ConfigData modifiedKey : modifiedKeys){
 			if(modEditor.legalizeModifier(uri, modifiedKey)){
 				legalModifications.add(modifiedKey);
 			}
 		}
 		modifiedUpgradable.set(ParserKeys.modifiedKey, legalModifications.toArray(new ConfigData[0]));
 		return true;
 	}
 
 	//means syntactically correct and complete
 	boolean upgradableModificationIsValid(ConfigData modifiedUpgradable){
 		//make sure there is a URI here
 		String uri = null;
 		try{
 			uri = modifiedUpgradable.getString(ParserKeys.URI);
 		}catch(NoSuchKeyException e){
 			return false;
 		}
 		
 		//Make sure something is modified for this URI
 		List<ConfigData> modifiedKeys = null;
 		try{
 			modifiedKeys = modifiedUpgradable.getConfigList(ParserKeys.modifiedKey);
 		}catch(NoSuchKeyException e){
 			return false;
 		}
 		
 		//Make sure all the modifications are valid
 		for(ConfigData modifiedKey : modifiedKeys){
 			if(!modEditor.modifierIsValid(uri, modifiedKey)){
 				return false;
 			}
 		}
 		
		return false;
 	}
 	
 	private String[] addStringToArray(String[] current, String toAdd){
 		String[] ret = new String[current.length + 1];
 		for(int i = 0; i < current.length; i++){
 			ret[i] = current[i];
 		}
 		ret[current.length] = toAdd;
 		return ret;
 	}
 	
 	private void saveModData() {
 		ConfigData currentData = modEditor.getData();
 		try{
 			ParserKeys currentModifiedKey = ParserKeys.getKey(currentData.getString(ParserKeys.key));
 			modifiers.put(currentModifiedKey, currentData);
 		}catch(NoSuchKeyException e){
 			//just have to discard the data, but this shouldn't ever happen...
 			//System.out.println("Discarding data because there was no key.  This should not happen, there is a bug somewhere.");
 		}
 	}
 }
