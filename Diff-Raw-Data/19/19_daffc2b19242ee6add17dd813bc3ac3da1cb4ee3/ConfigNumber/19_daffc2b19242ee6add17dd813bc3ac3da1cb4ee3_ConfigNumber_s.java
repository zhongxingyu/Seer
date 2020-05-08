 package pleocmd.cfg;
 
 import java.util.List;
 
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 import pleocmd.Log;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.InternalException;
 import pleocmd.itfc.gui.Layouter;
 
 public abstract class ConfigNumber<E extends Number> extends ConfigValue {
 
 	private E content;
 
 	private final E min, max;
 
 	private final E step;
 
 	private JSpinner sp;
 
 	public ConfigNumber(final String label, final E min, final E max,
 			final E step) {
 		super(label);
 		if (lessThan(max, min))
 			throw new IllegalArgumentException(String.format(
 					"min (%s) must not be larger than max (%s)", min, max));
 		this.min = min;
 		this.max = max;
 		this.step = step;
 		try {
 			setContent(min);
 		} catch (final ConfigurationException e) {
 			throw new InternalException(e);
 		}
 	}
 
 	protected abstract boolean lessThan(E nr1, E nr2);
 
 	protected abstract E valueOf(String str) throws ConfigurationException;
 
 	public final E getContent() {
 		return content;
 	}
 
 	public final void setContent(final E content) throws ConfigurationException {
 		if (lessThan(content, min) || lessThan(max, content))
 			throw new ConfigurationException("%s not between %s and %s",
 					content, min, max);
 		this.content = content;
 	}
 
 	public final E getMin() {
 		return min;
 	}
 
 	public final E getMax() {
 		return max;
 	}
 
 	@Override
 	final String asString() {
 		return String.valueOf(content);
 	}
 
 	@Override
 	final void setFromString(final String string) throws ConfigurationException {
 		try {
 			setContent(valueOf(string));
 		} catch (final NumberFormatException e) {
 			throw new ConfigurationException("Invalid number: '%s'", string);
 		}
 	}
 
 	@Override
 	final List<String> asStrings() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	final void setFromStrings(final List<String> strings) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	final boolean isSingleLined() {
 		return true;
 	}
 
 	@Override
 	public final void insertGUIComponents(final Layouter lay) {
		sp = new JSpinner(new SpinnerNumberModel(content, new Comparable<E>() {
			@Override
			public int compareTo(final E nr) {
				return lessThan(getMin(), nr) ? -1 : lessThan(nr, getMin()) ? 1
						: 0;
			}
		}, new Comparable<E>() {
			@Override
			public int compareTo(final E nr) {
				return lessThan(getMax(), nr) ? -1 : lessThan(nr, getMax()) ? 1
						: 0;
			}
		}, step));
 		lay.add(sp, true);
 	}
 
 	@Override
 	public final void setFromGUIComponents() {
 		try {
 			setContent(valueOf(sp.getValue().toString()));
 		} catch (final ConfigurationException e) {
 			Log.error(e, "Cannot set value '%s'", getLabel());
 		}
 	}
 }
