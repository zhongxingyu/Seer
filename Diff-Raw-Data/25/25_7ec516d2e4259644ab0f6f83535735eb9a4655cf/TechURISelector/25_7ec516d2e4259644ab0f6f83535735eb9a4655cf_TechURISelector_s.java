 package editor.tech;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ConfigData.NoSuchKeyException;
 import linewars.configfilehandler.ParserKeys;
 import editor.BigFrameworkGuy;
 import editor.ConfigurationEditor;
 import editor.ListURISelector;
 import editor.ListURISelector.ListSelectorOptions;
 import editor.URISelector.SelectorOptions;
 
 /**
  * 
  * @author Taylor Bergquist
  *
  */
 public class TechURISelector implements ConfigurationEditor {
 	
 	private JPanel panel;
 	private TechKeySelector keySelector;
 	
 	private BigFrameworkGuy framework;
 	
 	ListURISelector unitSelector;
 	ListURISelector buildingSelector;
 	ListURISelector abilitySelector;
 	ListURISelector projectileSelector;
 	
 	private HashMap<String, ConfigData> modifiedURIs;
 
 	public String[] unitsCurrentlyHighlighted = new String[0];
 	public String[] buildingsCurrentlyHighlighted = new String[0];
 	public String[] abilitiesCurrentlyHighlighted = new String[0];
 	public String[] projectilesCurrentlyHighlighted = new String[0];
 	
 	private boolean inHighlightCallback = false;
 	
 	private ListSelectorOptions unitSelectorOptions = new ListSelectorOptions(){
 
 		
 		@Override
 		public String[] getOptions() {
 			return framework.getUnitURIs();
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			ConfigData toAdd = new ConfigData();
 			toAdd.set(ParserKeys.URI, uri);
 			modifiedURIs.put(uri, toAdd);
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
			modifiedURIs.remove(modifiedURIs.get(uri));
 			unitsCurrentlyHighlighted = new String[0];
 			updateCurrentlyHighlighted();
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 			if(uris.length == 0 || inHighlightCallback){
 				return;
 			}
 			inHighlightCallback = true;
 			resetHighlightedURIs();
 			unitsCurrentlyHighlighted = uris.clone();
 			updateCurrentlyHighlighted();
 			inHighlightCallback = false;
 		}
 	};
 	private ListSelectorOptions buildingSelectorOptions = new ListSelectorOptions(){
 
 		@Override
 		public String[] getOptions() {
 			return framework.getBuildingURIs();
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			ConfigData toAdd = new ConfigData();
 			toAdd.set(ParserKeys.URI, uri);
 			modifiedURIs.put(uri, toAdd);
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
			modifiedURIs.remove(modifiedURIs.get(uri));
 			buildingsCurrentlyHighlighted = new String[0];
 			updateCurrentlyHighlighted();
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 			if(uris.length == 0 || inHighlightCallback){
 				return;
 			}
 			inHighlightCallback = true;
 			resetHighlightedURIs();
 			buildingsCurrentlyHighlighted = uris.clone();
 			updateCurrentlyHighlighted();
 			inHighlightCallback = false;
 		}
 	};
 	
 	private ListSelectorOptions abilitySelectorOptions = new ListSelectorOptions(){
 
 		@Override
 		public String[] getOptions() {
 			return framework.getAbilityURIs();
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			ConfigData toAdd = new ConfigData();
 			toAdd.set(ParserKeys.URI, uri);
 			modifiedURIs.put(uri, toAdd);
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
			modifiedURIs.remove(modifiedURIs.get(uri));
 			abilitiesCurrentlyHighlighted = new String[0];
 			updateCurrentlyHighlighted();
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 			if(uris.length == 0 || inHighlightCallback){
 				return;
 			}
 			inHighlightCallback = true;
 			resetHighlightedURIs();
 			abilitiesCurrentlyHighlighted = uris.clone();
 			updateCurrentlyHighlighted();
 			inHighlightCallback = false;
 		}
 	};
 	
 	private ListSelectorOptions projectileSelectorOptions = new ListSelectorOptions(){
 
 		@Override
 		public String[] getOptions() {
 			return framework.getProjectileURIs();
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			ConfigData toAdd = new ConfigData();
 			toAdd.set(ParserKeys.URI, uri);
 			modifiedURIs.put(uri, toAdd);
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
			modifiedURIs.remove(modifiedURIs.get(uri));
 			projectilesCurrentlyHighlighted = new String[0];
 			updateCurrentlyHighlighted();
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 			if(uris.length == 0 || inHighlightCallback){
 				return;
 			}
 			inHighlightCallback = true;
 			resetHighlightedURIs();
 			projectilesCurrentlyHighlighted = uris.clone();
 			updateCurrentlyHighlighted();
 			inHighlightCallback = false;
 		}
 	};
 	
 	public TechURISelector(BigFrameworkGuy framework, TechKeySelector keySelector){
 		this.framework = framework;
 		this.keySelector = keySelector;
 		
 		panel = new JPanel();
 		panel.setLayout(new GridLayout(0, 1));
 		
 		//add the ListURISelectors
 		unitSelector = new ListURISelector("Units", unitSelectorOptions);
 		panel.add(unitSelector);
 
 		buildingSelector = new ListURISelector("Buildings", buildingSelectorOptions);
 		panel.add(buildingSelector);
 
 		abilitySelector = new ListURISelector("Abilities", abilitySelectorOptions);
 		panel.add(abilitySelector);
 
 		projectileSelector = new ListURISelector("Projectile", projectileSelectorOptions);
 		panel.add(projectileSelector);
 	}
 
 	@Override
 	public void setData(ConfigData cd) {
 		reset();
 		List<ConfigData> modifiedUpgradables = cd.getConfigList(ParserKeys.modifiedURI);
 		//for each modified uri
 		for(ConfigData modifiedUpgradable : modifiedUpgradables){
 			//make sure it is valid
 			if(!keySelector.upgradableModificationIsValid(modifiedUpgradable)){
 				throw new IllegalArgumentException("The ConfigData object is not properly formatted or is incomplete.");
 			}
 			//add it to the map at its associated uri
 			String uri = modifiedUpgradable.getString(ParserKeys.URI);
 			modifiedURIs.put(uri, modifiedUpgradable);
 			//add it to the appropriate selector's list of selected uris
 			addToSelected(uri);
 		}
 	}
 
 	@Override
 	public void forceSetData(ConfigData cd) {
 		reset();
 		List<ConfigData> modifiedUpgradables = null;
 		try{
 			modifiedUpgradables = cd.getConfigList(ParserKeys.modifiedURI);
 		}catch(NoSuchKeyException e){
 			modifiedUpgradables = new ArrayList<ConfigData>();
 		}
 		
 		for(ConfigData modifiedUpgradable : modifiedUpgradables){
 			//make it legal if that is possible
 			if(!keySelector.legalizeUpgradableModification(modifiedUpgradable)){
 				continue;
 			}
 			//add it to the map at its associated uri
 			String uri = modifiedUpgradable.getString(ParserKeys.URI);
 			modifiedURIs.put(uri, modifiedUpgradable);
 			//add it to the appropriate selector's list of selected uris
 			addToSelected(uri);
 		}
 	}
 
 	@Override
 	public void reset() {
 		keySelector.reset();
 		modifiedURIs = new HashMap<String, ConfigData>();
 		unitSelector.setSelectedURIs(new String[0]);
 		buildingSelector.setSelectedURIs(new String[0]);
 		abilitySelector.setSelectedURIs(new String[0]);
 		projectileSelector.setSelectedURIs(new String[0]);
 	}
 
 	@Override
 	public ConfigData getData() {
 		//save data currently in the keySelector
 		saveKeyData();
 		ConfigData ret = new ConfigData();
 		for(String modified : modifiedURIs.keySet()){
 			ConfigData toAdd = modifiedURIs.get(modified);
 			toAdd.set(ParserKeys.URI, modified);
 			ret.add(ParserKeys.modifiedURI, toAdd);
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean isValidConfig() {
 		for(ConfigData toVerify : modifiedURIs.values()){
 			if(!keySelector.upgradableModificationIsValid(toVerify)){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public ParserKeys getType() {
 		return ParserKeys.modifiedURI;
 	}
 
 	@Override
 	public JPanel getPanel() {
 		return panel;
 	}
 
 	private void updateCurrentlyHighlighted() {
 		unitSelector.setHighlightedURIs(unitsCurrentlyHighlighted);
 		buildingSelector.setHighlightedURIs(buildingsCurrentlyHighlighted);
 		abilitySelector.setHighlightedURIs(abilitiesCurrentlyHighlighted);
 		projectileSelector.setHighlightedURIs(projectilesCurrentlyHighlighted);
 		if(unitsCurrentlyHighlighted.length + buildingsCurrentlyHighlighted.length + abilitiesCurrentlyHighlighted.length + projectilesCurrentlyHighlighted.length != 1){
 			updateCurrentlyHighlighted(null);
 		}else if(unitsCurrentlyHighlighted.length == 1){
 			updateCurrentlyHighlighted(unitsCurrentlyHighlighted[0]);
 		}else if(buildingsCurrentlyHighlighted.length == 1){
 			updateCurrentlyHighlighted(buildingsCurrentlyHighlighted[0]);
 		}else if(abilitiesCurrentlyHighlighted.length == 1){
 			updateCurrentlyHighlighted(abilitiesCurrentlyHighlighted[0]);
 		}else if(projectilesCurrentlyHighlighted.length == 1){
 			updateCurrentlyHighlighted(projectilesCurrentlyHighlighted[0]);
 		}
 	}
 	
 	private void updateCurrentlyHighlighted(String onlyHighlightedURI){
 		//make sure this isn't getting double-called
 
 		ConfigData currentData = keySelector.getData();
 		try{
 			String currentModifiedURI = currentData.getString(ParserKeys.URI);
 			if(currentModifiedURI != null && currentModifiedURI.equals(onlyHighlightedURI)){
 				return;
 			}
 		}catch(NoSuchKeyException e){
 			
 		}
 		//we're good, this is an actual change
 		
 		//save data currently in the keySelector
 		saveKeyData();
 		
 		keySelector.reset();
 		
 		//set its visibility
 		if(onlyHighlightedURI == null){
 			keySelector.getPanel().setVisible(false);
 		}else{
 			//put new data in there
 			keySelector.forceSetData(modifiedURIs.get(onlyHighlightedURI));
 			keySelector.getPanel().setVisible(true);
 		}
 	}
 
 	private void saveKeyData() {
 		ConfigData currentData = keySelector.getData();
 		try{
 			String currentModifiedURI = currentData.getString(ParserKeys.URI);
 			modifiedURIs.put(currentModifiedURI, currentData);
 		}catch(NoSuchKeyException e){
 			//just have to discard the data, but this shouldn't ever happen...
 			System.out.println("Discarding data because there was no URI.  This should not happen, there is a bug somewhere.");
 		}
 	}
 
 	private void addToSelected(String uri) {
 		//if it is a unit
 		if(stringIsInArray(framework.getUnitURIs(), uri)){
 			//add it to the list of selected units
 			unitSelector.setSelectedURIs(addStringToArray(unitSelector.getSelectedURIs(), uri));
 		}
 		//if it is a building
 		if(stringIsInArray(framework.getBuildingURIs(), uri)){
 			//add it to the list of selected units
 			buildingSelector.setSelectedURIs(addStringToArray(buildingSelector.getSelectedURIs(), uri));
 		}
 		//if it is an ability
 		if(stringIsInArray(framework.getAbilityURIs(), uri)){
 			//add it to the list of selected units
 			abilitySelector.setSelectedURIs(addStringToArray(abilitySelector.getSelectedURIs(), uri));
 		}
 		//if it is a projectile
 		if(stringIsInArray(framework.getProjectileURIs(), uri)){
 			//add it to the list of selected units
 			projectileSelector.setSelectedURIs(addStringToArray(projectileSelector.getSelectedURIs(), uri));
 		}
 	}
 	
 	private String[] addStringToArray(String[] current, String toAdd){
 		String[] ret = new String[current.length + 1];
 		for(int i = 0; i < current.length; i++){
 			ret[i] = current[i];
 		}
 		ret[current.length] = toAdd;
 		return ret;
 	}
 	
 	private boolean stringIsInArray(String[] arr, String query){
 		for(String toTest : arr){
 			if(toTest.equals(query)){
 				return true;
 			}
 		}
 		return false;		
 	}
 
 	private void resetHighlightedURIs() {
 		unitsCurrentlyHighlighted = new String[0];
 		buildingsCurrentlyHighlighted = new String[0];
 		abilitiesCurrentlyHighlighted = new String[0];
 		projectilesCurrentlyHighlighted = new String[0];
 	}
 }
