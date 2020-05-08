 package pleocmd.cfg;
 
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.InternalException;
 
 public final class ConfigDouble extends ConfigNumber<Double> {
 
 	public ConfigDouble(final String label) {
 		this(label, Double.MIN_VALUE, Double.MAX_VALUE);
 	}
 
 	public ConfigDouble(final String label, final double content) {
 		this(label);
 		try {
 			setContent(content);
 		} catch (final ConfigurationException e) {
 			throw new InternalException(e);
 		}
 	}
 
 	public ConfigDouble(final String label, final double min, final double max) {
 		this(label, min, min, max, 1);
 	}
 
 	public ConfigDouble(final String label, final double content,
 			final double min, final double max) {
 		this(label, content, min, max, 1);
 	}
 
 	public ConfigDouble(final String label, final double content,
 			final double min, final double max, final double step) {
 		super(label, min, max, step);
 		try {
 			setContent(content);
 		} catch (final ConfigurationException e) {
 			throw new IllegalArgumentException(
 					"Cannot initialize default content", e);
 		}
 	}
 
 	@Override
 	String getIdentifier() {
 		return "double";
 	}
 
 	@Override
 	protected boolean lessThan(final Double nr1, final Double nr2) {
		return nr1 < nr2;
 	}
 
 	@Override
 	protected Double valueOf(final String str) throws ConfigurationException {
 		return Double.valueOf(str);
 	}
 
 }
