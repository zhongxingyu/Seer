 package pleocmd.cfg;
 
 import java.util.List;
 
 import pleocmd.cfg.ConfigCollection.Type;
 import pleocmd.cfg.ConfigPath.PathType;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.itfc.gui.Layouter;
 import pleocmd.pipe.PipePart;
 
 /**
  * Base class for a value inside a {@link PipePart}'s configuration.
  * <p>
  * All of the sub classes must have a constructor which uses only a label and
  * sets all other fields to default values and should have a constructor which
  * takes an initial content as an additional parameter.<br>
  * The constructors must not throw anything but {@link RuntimeException}s (like
  * {@link NullPointerException}, {@link IllegalArgumentException} or
  * {@link IndexOutOfBoundsException}).
  * <p>
  * All of the sub classes have something like getContent() to retrieve the
  * current value and setContent() to set it to a new one.<br>
  * Implementations of setContent() may throw {@link ConfigurationException} if
  * the input is invalid.
  * <p>
  * If the {@link ConfigValue} is single-lined ({@link #isSingleLined()} returns
  * true) it must support {@link #asString()} and {@link #setFromString(String)},
  * and if it is multi-lined ( {@link #isSingleLined()} returns false) it must at
  * least support {@link #asString()}, {@link #asStrings()} and
  * {@link #setFromStrings(List)}, whereby "supporting" means not throwing an
  * {@link UnsupportedOperationException}.
  * 
  * @author oliver
  */
 public abstract class ConfigValue {
 
 	private final String label;
 
 	public ConfigValue(final String label) {
 		this.label = label;
 	}
 
 	public final String getLabel() {
 		return label;
 	}
 
 	@Override
 	public final String toString() {
 		final String str = asString();
 		return str == null ? getLabel() : getLabel() + ": " + str;
 	}
 
 	abstract String asString();
 
 	abstract void setFromString(final String string)
 			throws ConfigurationException;
 
 	abstract List<String> asStrings();
 
 	abstract void setFromStrings(final List<String> strings)
 			throws ConfigurationException;
 
 	abstract String getIdentifier();
 
 	abstract boolean isSingleLined();
 
 	public abstract void insertGUIComponents(final Layouter lay);
 
 	public abstract void setFromGUIComponents();
 
 	public final void assign(final ConfigValue src)
 			throws ConfigurationException {
 		if (isSingleLined())
 			setFromString(src.asString());
 		else
 			setFromStrings(src.asStrings());
 	}
 
 	protected final void checkValidString(final String str,
 			final boolean allowLineFeed) throws ConfigurationException {
 		final String trimmed = str.trim();
 		if (!str.equals(trimmed) && !str.equals(trimmed + "\n"))
 			throw new ConfigurationException("Cannot save this string "
 					+ "in a configuration file - will be trimmed: '%s'", str);
 		if ("{".equals(trimmed))
 			throw new ConfigurationException("Cannot save this string "
 					+ "in a configuration file - must not equal '{': '%s'", str);
 		if (str.contains("\0"))
 			throw new ConfigurationException("Cannot save this string "
 					+ "in a configuration file - must not contain "
 					+ "null-terminator: '%s'", str);
 		if (!allowLineFeed && str.contains("\n"))
 			throw new ConfigurationException("Cannot save this string "
 					+ "in a configuration file - must not contain "
 					+ "line-feed: '%s'", str);
 		if (allowLineFeed)
 			if (trimmed.contains("\n}\n") || trimmed.startsWith("}\n")
 					|| trimmed.endsWith("\n}"))
 				throw new ConfigurationException(
 						"Cannot save this string in a configuration file - "
 								+ "no line must equal '}': '%s'", str);
 	}
 
	// CS_IGNORE_NEXT NPath complexity - just a listing
 	public static ConfigValue createValue(final String identifier,
 			final String label, final boolean singleLined) {
 		if ("int".equals(identifier)) return new ConfigInt(label);
 
 		if ("long".equals(identifier)) return new ConfigLong(label);
 
 		if ("double".equals(identifier)) return new ConfigDouble(label);
 
 		if ("bool".equals(identifier)) return new ConfigBoolean(label);
 
 		if ("dir".equals(identifier))
 			return new ConfigPath(label, PathType.Directory);
 
 		if ("read".equals(identifier))
 			return new ConfigPath(label, PathType.FileForReading);
 
 		if ("write".equals(identifier))
 			return new ConfigPath(label, PathType.FileForWriting);
 
 		if ("bounds".equals(identifier)) return new ConfigBounds(label);
 
 		if ("list".equals(identifier))
 			return new ConfigCollection<String>(label, Type.List) {
 				@Override
 				protected String createItem(final String itemAsString)
 						throws ConfigurationException {
 					return itemAsString;
 				}
 			};
 
 		if ("set".equals(identifier))
 			return new ConfigCollection<String>(label, Type.Set) {
 				@Override
 				protected String createItem(final String itemAsString)
 						throws ConfigurationException {
 					return itemAsString;
 				}
 			};
 
 		// "str", "item", null
 		return new ConfigString(label, !singleLined);
 	}
 
 }
