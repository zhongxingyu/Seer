 package editor.tech;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ParserKeys;
 import editor.ConfigurationEditor;
 
 public interface ModifierConfigurationEditor extends ConfigurationEditor {
 	
 	public boolean modifierIsValid(String URI, ConfigData modifier);
 	public boolean legalizeModifier(String URI, ConfigData modifier);
 	
	public ParserKeys[] getValidUnitModifiers();
	public ParserKeys[] getValidBuildingModifiers();
	public ParserKeys[] getValidAbilityModifiers();
	public ParserKeys[] getValidProjectileModifiers();
 	
 	public String getName();
 }
