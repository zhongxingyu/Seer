 package pleocmd.cfg;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import pleocmd.Log;
 import pleocmd.exc.ConfigurationException;
 
 public final class Group {
 
 	private final String name;
 
 	private final Map<String, ConfigValue> valueMap;
 
 	private final Object user;
 
 	public Group(final String name) {
 		valueMap = new HashMap<String, ConfigValue>();
 		this.name = name;
 		user = null;
 	}
 
 	public Group(final String name, final Object user) {
 		valueMap = new HashMap<String, ConfigValue>();
 		this.name = name;
 		this.user = user;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Object getUser() {
 		return user;
 	}
 
 	public Map<String, ConfigValue> getValueMap() {
 		return Collections.unmodifiableMap(valueMap);
 	}
 
 	public boolean isEmpty() {
 		return valueMap.isEmpty();
 	}
 
 	public int getSize() {
 		return valueMap.size();
 	}
 
 	/**
 	 * Adds a {@link ConfigValue} to this {@link Group}.<br>
 	 * If one with the same label already exists, an
 	 * {@link IllegalArgumentException} will be thrown.
 	 * 
 	 * @param value
 	 *            the new {@link ConfigValue}
 	 * @return the {@link Group} itself
 	 */
 	public Group add(final ConfigValue value) {
 		if (valueMap.containsKey(value.getLabel()))
 			throw new IllegalArgumentException(String.format(
 					"Key already contained in Group: %s", value.getLabel()));
 		valueMap.put(value.getLabel(), value);
 		return this;
 	}
 
 	/**
 	 * Sets a {@link ConfigValue} in this {@link Group}.<br>
 	 * If one with the same label already exists, it will be replaced, otherwise
 	 * it will be added to the {@link Group}.
 	 * 
 	 * @param value
 	 *            the new {@link ConfigValue}
 	 * @return the {@link Group} itself
 	 */
 	public Group set(final ConfigValue value) {
 		valueMap.put(value.getLabel(), value);
 		return this;
 	}
 
 	/**
 	 * Returns the {@link ConfigValue} to a given label if one exists.
 	 * 
 	 * @param label
 	 *            the label of the {@link ConfigValue}
 	 * @return a {@link ConfigValue} or <b>null</b>
 	 */
 	public ConfigValue get(final String label) {
 		if (label == null) throw new NullPointerException();
 		return valueMap.get(label);
 	}
 
 	/**
 	 * Returns the {@link ConfigValue} to a given label or the default one if it
 	 * doesn't exist.
 	 * 
 	 * @param label
 	 *            the label of the {@link ConfigValue}
 	 * @param def
 	 *            the default {@link ConfigValue} if a fitting one could not be
 	 *            found
 	 * @return a {@link ConfigValue}, never <b>null</b>
 	 */
 	public ConfigValue get(final String label, final ConfigValue def) {
 		if (label == null || def == null) throw new NullPointerException();
 		final ConfigValue res = valueMap.get(label);
 		return res == null ? def : res;
 	}
 
 	/**
 	 * Removes a {@link ConfigValue} with the given label if it exists.
 	 * 
 	 * @param label
 	 *            the label of the {@link ConfigValue}
 	 */
 	public void remove(final String label) {
 		if (label == null) throw new NullPointerException();
 		valueMap.remove(label);
 	}
 
 	/**
 	 * Assigns all {@link ConfigValue}s from another {@link Group} to this one.<br>
 	 * Does not replace the {@link ConfigValue}s itself but only their content.
 	 * 
 	 * @param src
 	 *            the other Group from which all {@link ConfigValue}s will be
 	 *            copied.
 	 * @throws ConfigurationException
 	 *             if assigning a {@link ConfigValue} fails
 	 */
 	public void assign(final Group src) throws ConfigurationException {
 		for (final ConfigValue vt : valueMap.values()) {
 			final ConfigValue vs = src.get(vt.getLabel());
 			if (vs == null)
 				Log.error("Cannot assign to '%s', because the value does not "
 						+ "exist in the group '%s'. Using default value '%s' "
 						+ "instead.", vt.getLabel(), src.name, vt.asString());
 			else
 				try {
 					vt.assign(vs);
 				} catch (final ConfigurationException e) {
 					Log.error("Cannot assign to '%s', because the value "
 							+ "in '%s' is invalid: '%s'. Using default "
 							+ "value '%s' instead.", vt.getLabel(), src.name, e
 							.getMessage(), vt.asString());
 				}
 		}
 		for (final ConfigValue vs : src.valueMap.values())
 			if (!valueMap.containsKey(vs.getLabel()))
				Log.error("Ignoring value '%s' of group '%s' "
						+ "because of unknown label", vs, name);
 	}
 
 	@Override
 	public String toString() {
 		return name + " " + valueMap.values().toString();
 	}
 
 }
