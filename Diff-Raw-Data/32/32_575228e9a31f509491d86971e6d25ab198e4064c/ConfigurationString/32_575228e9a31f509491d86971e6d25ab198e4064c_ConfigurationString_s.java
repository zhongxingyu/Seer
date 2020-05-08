 package common.config.leaf;
 
 import common.config.AbstractConfigurationLeaf;
 import common.config.IConfiguration;
 import common.config.InvalidConfigurationException;
 import common.config.display.StringDisplayType;
 
 /**
  * A configuration element handling a String value.
  * 
  * @author benobiwan
  * 
  */
 public class ConfigurationString extends
 		AbstractConfigurationLeaf<String, StringDisplayType>
 {
 	/**
 	 * Maximum length of this String.
 	 */
 	private final int _iMaxLength;
 
	// TODO add a pattern for the vérification of the String
 
 	/**
 	 * Creates a new ConfigurationString with a command line value.
 	 * 
 	 * @param parent
 	 *            the parent of this configuration element.
 	 * @param strName
 	 *            the name of this configuration element.
 	 * @param strShortDescription
 	 *            the short description of this configuration element.
 	 * @param strLongDescription
 	 *            the long description of this configuration element.
 	 * @param strInvalidMessage
 	 *            the invalid message for this configuration element.
 	 * @param bAdvanced
 	 *            a boolean indicating whether this configuration element is
 	 *            advanced.
 	 * @param displayType
 	 *            the display type of this configuration element.
 	 * @param iMaxLength
 	 *            the maximum length of the String.
 	 * @param defaultValue
 	 *            the default value of this configuration element.
 	 * @param commandLineValue
 	 *            the command line value of this configuration element.
 	 * @throws InvalidConfigurationException
 	 *             if the value specified on the command line is invalid.
 	 */
 	public ConfigurationString(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final StringDisplayType displayType,
 			final int iMaxLength, final String defaultValue,
 			final String commandLineValue) throws InvalidConfigurationException
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, defaultValue,
 				commandLineValue);
 		_iMaxLength = iMaxLength;
 	}
 
 	/**
 	 * Creates a new ConfigurationString.
 	 * 
 	 * @param parent
 	 *            the parent of this configuration element.
 	 * @param strName
 	 *            the name of this configuration element.
 	 * @param strShortDescription
 	 *            the short description of this configuration element.
 	 * @param strLongDescription
 	 *            the long description of this configuration element.
 	 * @param strInvalidMessage
 	 *            the invalid message for this configuration element.
 	 * @param bAdvanced
 	 *            a boolean indicating whether this configuration element is
 	 *            advanced.
 	 * @param displayType
 	 *            the display type of this configuration element.
 	 * @param iMaxLength
 	 *            the maximum length of the String.
 	 * @param defaultValue
 	 *            the default value of this configuration element.
 	 */
 	public ConfigurationString(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final StringDisplayType displayType,
 			final int iMaxLength, final String defaultValue)
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, defaultValue);
 		_iMaxLength = iMaxLength;
 	}
 
 	@Override
 	public boolean validateValue(final String value)
 	{
 		return (value == null) || _iMaxLength <= 0
 				|| (value.length() <= _iMaxLength);
 	}
 
 	/**
 	 * Get the maximum length of this String.
 	 * 
 	 * @return the maximum length of this String.
 	 */
 	public int getMaxLength()
 	{
 		return _iMaxLength;
 	}
 }
