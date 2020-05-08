 package common.config.swing;
 
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 
 import common.config.InvalidConfigurationException;
 import common.config.display.LongDisplayType;
 import common.config.leaf.ConfigurationLong;
 
 /**
  * Object used to display a {@Link ConfigurationLong} on the
  * {@link ConfigurationPanel}.
  * 
  * @author benobiwan
  * 
  */
 public class LongEntryComponent extends
 		NumberEntryComponent<Long, LongDisplayType, ConfigurationLong>
 {
 	/**
 	 * Creates a new LongEntryComponnent.
 	 * 
 	 * @param entry
 	 *            the configuration element to display.
 	 */
 	public LongEntryComponent(final ConfigurationLong entry)
 	{
 		super(entry);
 	}
 
 	@Override
 	protected void setToValueAndShowValidity(final Long value)
 	{
 		switch (_dispType)
 		{
 		case SPINNER:
 			((JSpinner) _entryComponent).setValue(value);
 			break;
 		case TEXTFIELD:
 			((JTextField) _entryComponent).setText(value.toString());
 			break;
 		}
 		validateAndShowValidity();
 	}
 
 	@Override
 	protected void generateComponent()
 	{
		long min, max, initValue;
 		if (_entry.getMinValue() != null)
 		{
			min = _entry.getMinValue().longValue();
 		}
 		else
 		{
			min = Long.MIN_VALUE;
 		}
 		if (_entry.getMaxValue() != null)
 		{
			max = _entry.getMaxValue().longValue();
 		}
 		else
 		{
			max = Long.MAX_VALUE;
 		}
 		if (_entry.getCurrentValue() != null)
 		{
			initValue = _entry.getCurrentValue().longValue();
 		}
 		else
 		{
			initValue = 0;
 		}
 		final String strToolTip = "Min : " + min + " Max : " + max;
 		switch (_dispType)
 		{
 		case SPINNER:
 			final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
					initValue, min, max, 1);
 			_entryComponent = new JSpinner(spinnerModel);
 			spinnerModel.addChangeListener(new ValidateChangeListener());
 			_entryComponent.setToolTipText(strToolTip);
 			break;
 		case TEXTFIELD:
			_entryComponent = new JTextField(Long.toString(initValue));
 			_entryComponent.setToolTipText(strToolTip);
 			((JTextField) _entryComponent)
 					.addFocusListener(new ValidateFocusListener());
 			break;
 		}
 	}
 
 	@Override
 	protected Long getDisplayedValue() throws InvalidConfigurationException
 	{
 		Long value;
 		switch (_dispType)
 		{
 		case SPINNER:
 			value = ((Long) ((JSpinner) _entryComponent).getModel().getValue());
 			break;
 		case TEXTFIELD:
 			try
 			{
 				value = Long.valueOf(((JTextField) _entryComponent).getText());
 			}
 			catch (final NumberFormatException ex)
 			{
 				throw new InvalidConfigurationException(ex);
 			}
 			break;
 		default:
 			throw new InvalidConfigurationException();
 		}
 		return value;
 	}
 }
