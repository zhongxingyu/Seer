 package common.config.leaf;
 
 import common.config.AbstractConfigurationLeaf;
 import common.config.IConfiguration;
 import common.config.IConfigurationNumberLeaf;
 import common.config.InvalidConfigurationException;
 
 /**
  * A abstract class describing the different values of a number configuration
  * element.
  * 
  * @author benobiwan
  * 
  * @param <TYPE>
  *            the type of this configuration element.
  * @param <DISP_TYPE>
  *            the display type of this configuration element.
  */
 public abstract class AbstractConfigurationNumber<TYPE extends Number, DISP_TYPE extends Enum<DISP_TYPE>>
 		extends AbstractConfigurationLeaf<TYPE, DISP_TYPE> implements
 		IConfigurationNumberLeaf<TYPE, DISP_TYPE>
 {
 	/**
 	 * Minimum value of this number.
 	 */
 	protected final TYPE _minValue;
 
 	/**
 	 * Maximum value of this number.
 	 */
 	protected final TYPE _maxValue;
 
 	/**
 	 * Creates a new AbstractConfigurationNumber with a command line value.
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
 	protected AbstractConfigurationNumber(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final DISP_TYPE displayType,
 			final TYPE minValue, final TYPE maxValue, final TYPE defaultValue,
 			final TYPE commandLineValue) throws InvalidConfigurationException
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, defaultValue,
 				commandLineValue);
 		_minValue = minValue;
 		_maxValue = maxValue;
		if (!validateValue(commandLineValue))
		{
			throw new InvalidConfigurationException(strInvalidMessage
					+ " in command line : " + commandLineValue);
		}
 	}
 
 	/**
 	 * Creates a new AbstractConfigurationNumber.
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
 	protected AbstractConfigurationNumber(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final DISP_TYPE displayType,
 			final TYPE minValue, final TYPE maxValue, final TYPE defaultValue)
 	{
 		super(parent, strName, strShortDescription, strLongDescription,
 				strInvalidMessage, bAdvanced, displayType, defaultValue);
 		_minValue = minValue;
 		_maxValue = maxValue;
 	}
 
 	@Override
 	public final TYPE getMinValue()
 	{
 		return _minValue;
 	}
 
 	@Override
 	public final TYPE getMaxValue()
 	{
 		return _maxValue;
 	}
 }
