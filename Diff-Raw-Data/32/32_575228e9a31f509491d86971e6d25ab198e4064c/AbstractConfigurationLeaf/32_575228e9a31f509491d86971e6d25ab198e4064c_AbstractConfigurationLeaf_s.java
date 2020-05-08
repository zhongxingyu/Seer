 package common.config;
 
 /**
  * A abstract class describing the different values of a configuration element.
  * 
  * @author benobiwan
  * 
  * @param <TYPE>
  *            the type of this configuration element.
  * @param <DISP_TYPE>
  *            the display type of this configuration element.
  */
 public abstract class AbstractConfigurationLeaf<TYPE, DISP_TYPE extends Enum<DISP_TYPE>>
 		extends AbstractConfiguration implements
 		IConfigurationLeaf<TYPE, DISP_TYPE>
 {
 	/**
 	 * The current value of this configuration element.
 	 */
 	private TYPE _currentValue;
 	/**
 	 * The value specified in the interface for this configuration element.
 	 */
 	private TYPE _interfaceValue;
 	/**
 	 * The value specified on the command line for this configuration element.
 	 */
 	private final TYPE _commandLineValue;
 	/**
 	 * The value specified in the configuration file for this configuration
 	 * element.
 	 */
 	private TYPE _configurationValue;
 	/**
 	 * The default value of this configuration element.
 	 */
 	private final TYPE _defaultValue;
 	/**
 	 * A lock used when modifying one of the values.
 	 */
 	private final Object _lockValue = new Object();
 	/**
 	 * A short description of this configuration element, for use in the
 	 * interface.
 	 */
 	private final String _strShortDescription;
 	/**
 	 * A long description of this configuration element, for use in the
 	 * interface.
 	 */
 	private final String _strLongDescription;
 	/**
 	 * The message used in the exception when trying to set this configuration
 	 * element to a forbidden value.
 	 */
 	private final String _strInvalidMessage;
 	/**
 	 * The display type for this configuration element.
 	 */
 	private final DISP_TYPE _displayType;
 	/**
 	 * A boolean indicating that this configuration element is advanced.
 	 */
 	private final boolean _bAdvanced;
 
 	/**
 	 * Boolean telling whether this {@link IConfigurationLeaf} has changed.
 	 */
 	private boolean _bConfChanged = false;
 
 	/**
 	 * Creates a new AbstractConfigurationLeaf.
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
 	 * @param defaultValue
 	 *            the default value of this configuration element.
 	 */
 	protected AbstractConfigurationLeaf(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final DISP_TYPE displayType,
 			final TYPE defaultValue)
 	{
 		super(parent, strName);
 		_strShortDescription = strShortDescription;
 		_strLongDescription = strLongDescription;
 		_strInvalidMessage = strInvalidMessage;
 		_defaultValue = defaultValue;
 		_currentValue = _defaultValue;
 		_displayType = displayType;
 		_commandLineValue = null;
 		_bAdvanced = bAdvanced;
 	}
 
 	/**
 	 * Creates a new AbstractConfigurationLeaf with a command line value.
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
 	 * @param defaultValue
 	 *            the default value of this configuration element.
 	 * @param commandLineValue
 	 *            the command line value of this configuration element.
 	 * @throws InvalidConfigurationException
 	 *             if the value specified on the command line is invalid.
 	 */
 	protected AbstractConfigurationLeaf(final IConfiguration parent,
 			final String strName, final String strShortDescription,
 			final String strLongDescription, final String strInvalidMessage,
 			final boolean bAdvanced, final DISP_TYPE displayType,
 			final TYPE defaultValue, final TYPE commandLineValue)
 			throws InvalidConfigurationException
 	{
 		super(parent, strName);
 		_strShortDescription = strShortDescription;
 		_strLongDescription = strLongDescription;
 		_strInvalidMessage = strInvalidMessage;
 		if (!validateValue(commandLineValue))
 		{
 			throw new InvalidConfigurationException(_strInvalidMessage
 					+ " in command line : " + commandLineValue);
 		}
		_defaultValue = defaultValue;
		_displayType = displayType;
		_commandLineValue = commandLineValue;
		_bAdvanced = bAdvanced;
 		updateCurrentValue();
 	}
 
 	@Override
 	public final void updateCurrentValue()
 	{
 		if (!isLocked())
 		{
 			synchronized (_lockValue)
 			{
 				if (_interfaceValue != null)
 				{
 					_currentValue = _interfaceValue;
 				}
 				else if (_commandLineValue != null)
 				{
 					_currentValue = _commandLineValue;
 				}
 				else if (_configurationValue != null)
 				{
 					_currentValue = _configurationValue;
 				}
 				else
 				{
 					_currentValue = _defaultValue;
 				}
 			}
 		}
 	}
 
 	@Override
 	public final String getDescription()
 	{
 		return _strShortDescription;
 	}
 
 	@Override
 	public final String getLongDescription()
 	{
 		return _strLongDescription;
 	}
 
 	@Override
 	public final DISP_TYPE getDisplayType()
 	{
 		return _displayType;
 	}
 
 	@Override
 	public final String getInvalidMessage()
 	{
 		return _strInvalidMessage;
 	}
 
 	@Override
 	public final TYPE getCurrentValue()
 	{
 		synchronized (_lockValue)
 		{
 			return _currentValue;
 		}
 	}
 
 	@Override
 	public final TYPE getConfigurationValue()
 	{
 		synchronized (_lockValue)
 		{
 			return _configurationValue;
 		}
 	}
 
 	@Override
 	public final void setConfigurationValue(final TYPE configurationValue)
 			throws InvalidConfigurationException
 	{
 		if (validateValue(configurationValue))
 		{
 			synchronized (_lockValue)
 			{
 				_configurationValue = configurationValue;
 				_bConfChanged = true;
 				updateCurrentValue();
 			}
 		}
 		else
 		{
 			throw new InvalidConfigurationException(_strInvalidMessage
 					+ " in configuration : " + configurationValue);
 		}
 	}
 
 	@Override
 	public final TYPE getInterfaceValue()
 	{
 		synchronized (_lockValue)
 		{
 			return _interfaceValue;
 		}
 	}
 
 	@Override
 	public final void setInterfaceValue(final TYPE interfaceValue)
 			throws InvalidConfigurationException
 	{
 		if (validateValue(interfaceValue))
 		{
 			synchronized (_lockValue)
 			{
 				_interfaceValue = interfaceValue;
 				updateCurrentValue();
 			}
 		}
 		else
 		{
 			throw new InvalidConfigurationException(_strInvalidMessage
 					+ " in interface : " + interfaceValue);
 		}
 	}
 
 	@Override
 	public final TYPE getCommandLineValue()
 	{
 		synchronized (_lockValue)
 		{
 			return _commandLineValue;
 		}
 	}
 
 	@Override
 	public final TYPE getDefaultValue()
 	{
 		return _defaultValue;
 	}
 
 	@Override
 	public final boolean isAdvanced()
 	{
 		return _bAdvanced;
 	}
 
 	@Override
 	public final void setConfToCurrent()
 	{
 		synchronized (_lockValue)
 		{
 			if (_configurationValue != _currentValue)
 			{
 				_configurationValue = _currentValue;
 				_bConfChanged = true;
 			}
 		}
 	}
 
 	@Override
 	public final void setConfToDefault()
 	{
 		synchronized (_lockValue)
 		{
 			if (_configurationValue != _defaultValue)
 			{
 				_configurationValue = _defaultValue;
 				_bConfChanged = true;
 			}
 		}
 	}
 
 	@Override
 	public final void setInterfaceToDefault()
 	{
 		synchronized (_lockValue)
 		{
 			_interfaceValue = _defaultValue;
 		}
 	}
 
 	@Override
 	public final void setInterfaceToConf()
 	{
 		synchronized (_lockValue)
 		{
 			if (_configurationValue != null)
 			{
 				_interfaceValue = _configurationValue;
 			}
 		}
 	}
 
 	@Override
 	public final boolean hasConfChanged()
 	{
 		synchronized (_lockValue)
 		{
 			return _bConfChanged;
 		}
 	}
 
 	@Override
 	public int compareTo(final IConfigurationLeaf<TYPE, DISP_TYPE> o)
 	{
 		return getName().compareTo(o.getName());
 	}
 }
