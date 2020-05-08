 package org.rsbot.script.provider;
 
 import org.rsbot.Configuration;
 import org.rsbot.script.Script;
 import org.rsbot.script.provider.FileScriptSource.FileScriptDefinition;
 import org.rsbot.service.ServiceException;
 import org.rsbot.util.io.HttpClient;
 import org.rsbot.util.io.IniParser;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import static org.rsbot.script.Script.Category.valueOf;
 
 /**
  * @author Paris
  */
 public class ScriptDeliveryNetwork implements ScriptSource {
 	private static final Logger log = Logger.getLogger("Script Delivery");
 	private static ScriptDeliveryNetwork instance;
 	private URL base;
 	final File manifest;
 
 	private ScriptDeliveryNetwork() {
 		manifest = getFile("manifests");
 	}
 
 	public static ScriptDeliveryNetwork getInstance() {
 		if (instance == null) {
 			instance = new ScriptDeliveryNetwork();
 		}
 		return instance;
 	}
 
 	private static File getFile(final String name) {
 		return new File(Configuration.Paths.getCacheDirectory(), "sdn-" + name + ".txt");
 	}
 
 	private static Script.Category getCategory(final String category) {
 		return valueOf(category);
 	}
 
 	private static void parseManifests(final HashMap<String, HashMap<String, String>> entries, final List<ScriptDefinition> defs) {
 		for (final Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
 			final ScriptDefinition def = new ScriptDefinition();
 			def.path = entry.getKey();
 			final HashMap<String, String> values = entry.getValue();
 			def.id = Integer.parseInt(values.get("id"));
 			def.name = values.get("name");
 			def.version = Double.parseDouble(values.get("version"));
 			def.description = values.get("description");
 			def.authors = values.get("authors").split(ScriptList.DELIMITER);
 			def.keywords = values.get("keywords").split(ScriptList.DELIMITER);
 			def.website = values.get("website");
			if (values.get("category") != null) {
 			def.category = getCategory(values.get("category"));
			}
 			defs.add(def);
 		}
 	}
 
 	public void refresh(final boolean force) {
 		final File controlFile = getFile("control");
 		if (force || !manifest.exists()) {
 			try {
 				HttpClient.download(new URL(Configuration.Paths.URLs.SDN_CONTROL), controlFile);
 				final HashMap<String, String> control = IniParser.deserialise(controlFile).get(IniParser.emptySection);
 				if (control == null || !IniParser.parseBool(control.get("enabled")) || !control.containsKey("manifest")) {
 					throw new ServiceException("Service currently disabled");
 				}
 				base = HttpClient.download(new URL(control.get("manifest")), manifest).getURL();
 			} catch (final ServiceException e) {
 				log.severe(e.getMessage());
 			} catch (final IOException ignored) {
 				log.warning("Unable to load scripts from the network");
 			}
 		}
 	}
 
 	public List<ScriptDefinition> list() {
 		final ArrayList<ScriptDefinition> defs = new ArrayList<ScriptDefinition>();
 		refresh(false);
 		try {
 			parseManifests(IniParser.deserialise(manifest), defs);
 		} catch (final IOException ignored) {
 			log.warning("Error reading network script manifests");
 		}
 		for (final ScriptDefinition def : defs) {
 			def.source = this;
 		}
 		return defs;
 	}
 
 	private static File getCacheDirectory() {
 		final File store = new File(Configuration.Paths.getScriptsNetworkDirectory());
 		if (!store.exists()) {
 			store.mkdirs();
 		}
 		if (Configuration.getCurrentOperatingSystem() == Configuration.OperatingSystem.WINDOWS) {
 			final String path = "\"" + store.getAbsolutePath() + "\"";
 			try {
 				Runtime.getRuntime().exec("attrib +H " + path);
 			} catch (final IOException ignored) {
 			}
 		}
 		return store;
 	}
 
 	public Script load(ScriptDefinition def) throws ServiceException {
 		final File store = getCacheDirectory();
 		final File file = new File(store, def.path);
 		final LinkedList<ScriptDefinition> defs = new LinkedList<ScriptDefinition>();
 		if (file.exists()) {
 			try {
 				FileScriptSource.load(file, defs, null);
 				if (defs.size() != 1 || defs.getFirst().version < def.version) {
 					file.delete();
 				}
 			} catch (final IOException ignored) {
 			}
 		}
 		try {
 			if (!file.exists()) {
 				log.info("Downloading script " + def.name + "...");
 				HttpClient.download(new URL(base, def.path), file);
 			}
 			FileScriptSource.load(file, defs, null);
 			return FileScriptSource.load((FileScriptDefinition) defs.getFirst());
 		} catch (final Exception ignored) {
 			log.severe("Unable to load script");
 		}
 		return null;
 	}
 }
