 package editor.tech;
 
import java.util.List;

 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ParserKeys;
 import editor.ConfigurationEditor;
 
 public interface ModifierConfigurationEditor extends ConfigurationEditor {
 	
	/**
	 * Computes whether the given ConfigData represents a valid (meaning both syntactically legal AND complete) modifier
	 * @param URI
	 * The URI of the Upgradable being modified.
	 * @param modifier
	 * The ConfigData representing the current configuration of the modifier.
	 * @return
	 * true iff the modifier is valid
	 */
 	public boolean modifierIsValid(String URI, ConfigData modifier);

	/**
	 * Sanitizes the given ConfigData so that it comes to represent a legal modifier, if possible.
	 * Even if this method returns false, it will change the ConfigData given to it.
	 * @param URI
	 * The URI of the Upgradable being modified.
	 * @param modifier
	 * The ConfigData representing the current configuration of the modifier, and where the valid configuration is stored
	 * @return
	 * true iff the modifier could be sanitized
	 */
 	public boolean legalizeModifier(String URI, ConfigData modifier);
 	
	public List<ParserKeys[]> getModifiableTraces(String upgradableURI);
 	
 	public String getName();
 }
