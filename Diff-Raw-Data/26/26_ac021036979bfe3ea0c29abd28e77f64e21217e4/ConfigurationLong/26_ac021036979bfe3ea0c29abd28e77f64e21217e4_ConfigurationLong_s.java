 package common.config.leaf;
 
 import common.config.IConfiguration;
 import common.config.InvalidConfigurationException;
 import common.config.display.LongDisplayType;
 
 /**
  * A configuration element handling a long value.
  * 
  * @author benobiwan
  * 
  */
 public final class ConfigurationLong extends
 		AbstractConfigurationNumber<Long, LongDisplayType>
 {
 	/**
 	 * Creates a new ConfigurationLong with a command line value.
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
 	 * @param minValue
 	 *            the minimum value of this configuration element.
 	 * @param maxValue
 	 *            the maximum value of this configuration element.
 	 * @param defaultValue
 	 *            the default value of this configuration element.
 	 * @param commandLineValue
 	 *            the command line value of this configuration element.
 	 * @throws InvalidConfigurationException
 	 *             if the value specified on the command line is invalid.
 	 */
 	public ConfigurationLong(final IConfiguration parent, final String strName,
 			final String strShortDescription, final String strLongDescription,
 			final String strInvalidMessage, final boolean bAdvanced,
 			final LongDisplayType displayType, final Long minValue,
 			final Long maxValue, final Long defaultValue,
 			final Long commandLineValue) throws InvalidConfigurationException
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, minValue, maxValue,
 				defaultValue, commandLineValue);
 	}
 
 	/**
 	 * Creates a new ConfigurationLong.
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
 	 * @param minValue
 	 *            the minimum value of this configuration element.
 	 * @param maxValue
 	 *            the maximum value of this configuration element.
 	 * @param defaultValue
 	 *            the default value of this configuration element.
 	 */
 	public ConfigurationLong(final IConfiguration parent, final String strName,
 			final String strShortDescription, final String strLongDescription,
 			final String strInvalidMessage, final boolean bAdvanced,
 			final LongDisplayType displayType, final Long minValue,
 			final Long maxValue, final Long defaultValue)
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, minValue, maxValue,
 				defaultValue);
 	}
 
 	@Override
 	public boolean validateValue(final Long value)
 	{
 		return (value == null)
				|| (value.longValue() >= _minValue.longValue() && value
						.longValue() <= _maxValue.longValue());
 	}
 }
