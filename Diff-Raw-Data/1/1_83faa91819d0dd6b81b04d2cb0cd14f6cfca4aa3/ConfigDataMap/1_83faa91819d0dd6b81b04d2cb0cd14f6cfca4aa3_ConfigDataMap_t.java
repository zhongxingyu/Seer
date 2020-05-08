 package pleocmd.cfg;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import pleocmd.Log;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.FormatException;
 import pleocmd.itfc.gui.DataSequenceTriggerPanel;
 import pleocmd.itfc.gui.Layouter;
 import pleocmd.pipe.data.Data;
 
 public final class ConfigDataMap extends ConfigMap<String, Data> {
 
 	private DataSequenceTriggerPanel dataEditor;
 
 	public ConfigDataMap(final String label) {
 		super(label);
 	}
 
 	@Override
 	protected String createKey(final String keyAsString) {
 		return keyAsString;
 	}
 
 	@Override
 	protected Data createValue(final String valueAsString)
 			throws ConfigurationException {
 		try {
 			return Data.createFromAscii(valueAsString);
 		} catch (final IOException e) {
 			throw new ConfigurationException(e, "Cannot convert String to Data");
 		} catch (final FormatException e) {
 			throw new ConfigurationException(e, "Cannot convert String to Data");
 		}
 	}
 
 	@Override
 	public boolean insertGUIComponents(final Layouter lay) {
 		dataEditor = new DataSequenceTriggerPanel(this);
 		lay.addWholeLine(dataEditor, true);
 		return true;
 	}
 
 	@Override
 	public void setFromGUIComponents() {
 		dataEditor.saveChanges();
 	}
 
 	/**
 	 * Creates a deep copy of the given list while setting the parent and
 	 * changing the priority of every {@link Data} in the list which has the
 	 * default priority to the new one.
 	 * 
 	 * @param org
 	 *            the original list of {@link Data}
 	 * @param parent
 	 *            the parent for every new {@link Data} - may be <b>null</b>
 	 * @return a new list of {@link Data} copies
 	 */
 	public static List<Data> cloneDataList(final List<Data> org,
 			final Data parent) {
 		Log.detail("Cloning list '%s' with parent '%s'", org, parent);
 		final List<Data> res = new ArrayList<Data>(org.size());
 		for (final Data data : org)
 			res.add(new Data(data, parent));
 		return res;
 	}
 
 }
