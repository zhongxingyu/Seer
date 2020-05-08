 package common.config.leaf;
 
 import common.config.IConfiguration;
 import common.config.InvalidConfigurationException;
 import common.config.display.DoubleDisplayType;
 
 /**
  * A configuration element handling a double value.
  * 
  * @author benobiwan
  * 
  */
 public final class ConfigurationDouble extends
 		AbstractConfigurationNumber<Double, DoubleDisplayType>
 {
 	/**
 	 * Creates a new ConfigurationDouble with a command line value.
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
 	public ConfigurationDouble(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final DoubleDisplayType displayType,
 			final Double minValue, final Double maxValue,
 			final Double defaultValue, final Double commandLineValue)
 			throws InvalidConfigurationException
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, minValue, maxValue,
 				defaultValue, commandLineValue);
 	}
 
 	/**
 	 * Creates a new ConfigurationDouble.
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
 	public ConfigurationDouble(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final DoubleDisplayType displayType,
 			final Double minValue, final Double maxValue,
 			final Double defaultValue)
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, minValue, maxValue,
 				defaultValue);
 	}
 
 	@Override
 	public boolean validateValue(final Double value)
 	{
 		return (value == null)
				|| (value.doubleValue() >= _minValue.doubleValue() && value
						.doubleValue() <= _maxValue.doubleValue());
 	}
 }
