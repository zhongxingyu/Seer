 package pleocmd.pipe.cfg;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 
 import pleocmd.Log;
 import pleocmd.exc.PipeException;
 import pleocmd.pipe.PipePart;
 
 public final class Config extends AbstractList<ConfigValue> {
 
 	private final List<ConfigValue> list = new ArrayList<ConfigValue>();
 
 	private PipePart owner;
 
 	/**
 	 * Should only be called in a constructor of a subclass of {@link PipePart}.
 	 */
 	public Config() {
 		assert new Throwable().getStackTrace()[1].getClassName().startsWith(
				getClass().getPackage().getName() + ".");
 	}
 
 	@Override
 	public ConfigValue get(final int index) {
 		return list.get(index);
 	}
 
 	public ConfigValue getSafe(final int index) {
 		if (index < 0 || index >= list.size()) return new ConfigDummy();
 		return list.get(index);
 	}
 
 	@Override
 	public int size() {
 		return list.size();
 	}
 
 	public PipePart getOwner() {
 		return owner;
 	}
 
 	public void setOwner(final PipePart owner) {
 		if (this.owner != null)
 			throw new IllegalStateException(
 					"Config's owner has already been assigned");
 		try {
 			owner.ensureConstructing();
 		} catch (final PipeException e) {
 			throw new IllegalStateException("Cannot set Config's owner", e);
 		}
 		this.owner = owner;
 	}
 
 	public Config addV(final ConfigValue value) {
 		try {
 			if (owner != null) owner.ensureConstructing();
 		} catch (final PipeException e) {
 			throw new IllegalStateException(
 					"Cannot add ConfigValue to configuration", e);
 		}
 		list.add(value);
 		return this;
 	}
 
 	public void readFromFile(final BufferedReader in) throws IOException,
 			PipeException {
 		Log.detail("Reading config from BufferedReader");
 		for (final ConfigValue v : list) {
 			in.mark(10240);
 			final String line = in.readLine().trim();
 			final int idx = line.indexOf(':');
 			if (idx == -1)
 				throw new IOException(String.format(
 						"Missing ':' delimiter in '%s'", line));
 			final String label = line.substring(0, idx).trim();
 			if (!label.equals(v.getLabel()))
 				throw new IOException(String.format(
 						"Wrong configuration value '%s' - excepted '%s'",
 						label, v.getLabel()));
 			v.setFromString(line.substring(idx + 1).trim());
 		}
 		Log.detail("Read config from BufferedReader: %s", this);
 		owner.configure();
 	}
 
 	public void writeToFile(final Writer out) throws IOException {
 		Log.detail("Writing config to Writer: %s", this);
 		for (final ConfigValue v : list) {
 			out.write('\t');
 			out.write(v.getLabel());
 			out.write(':');
 			out.write(' ');
 			out.write(v.getContentAsString());
 			out.write('\n');
 		}
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + " owned by " + owner;
 	}
 
 }
