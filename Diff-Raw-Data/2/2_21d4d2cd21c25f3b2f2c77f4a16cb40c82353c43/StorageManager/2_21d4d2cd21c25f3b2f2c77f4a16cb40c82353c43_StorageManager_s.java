 package nl.tue.fingerpaint.client.storage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.tue.fingerpaint.client.gui.panels.NotificationPopupPanel;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.shared.GeometryNames;
 import nl.tue.fingerpaint.shared.model.MixingProtocol;
 
 import com.google.gwt.core.client.JavaScriptException;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.storage.client.StorageMap;
 
 /**
  * <p>
  * A {@code StorageManager} manages the local storage in the browser. There is
  * only one instance of the storage manager, that must be used for all
  * interaction with the storage.
  * </p>
  * 
  * <p>
  * The local storage is structured as follows:
  * </p>
  * <ul>
  * <li>{@link #KEY_INITDIST}
  * <ul>
  * <li>{@link GeometryNames#RECT_SHORT}
  * <ul>
  * <li>Name of some saved distribution for a rectangular geometry</li>
  * <li>Other name</li>
  * <li>...</li>
  * </ul>
  * </li>
  * <li>{@link GeometryNames#SQR_SHORT}
  * <ul>
  * <li>Name of saved distribution for a square geometry</li>
  * <li>Other name</li>
  * <li>...</li>
  * </ul>
  * </li>
  * <li>{@link GeometryNames#CIRC_SHORT}
  * <ul>
  * <li>Name of saved distribution for a circle geometry</li>
  * <li>Other name</li>
  * <li>...</li>
  * </ul>
  * </li>
  * <li>{@link GeometryNames#JOBE_SHORT}
  * <ul>
  * <li>Name of saved distribution for a journal bearing geometry</li>
  * <li>Other name</li>
  * <li>...</li>
  * </ul>
  * </li>
  * </ul>
  * </li>
  * <li>{@link #KEY_PROTOCOLS}
  * <ul>
  * <li>Same structure as for previous key, only with saved protocols instead of
  * saved distributions.</li>
  * </ul>
  * </li>
  * <li>{@link #KEY_RESULTS}
  * <ul>
  * <li>Name of saved result</li>
  * <li>Other name</li>
  * <li>...</li>
  * </ul>
  * </li>
  * </ul>
  * 
  * @author Group Fingerpaint
  */
 public class StorageManager {
 
 	// ---- PUBLIC CONSTANTS
 	// ----------------------------------------------------------------------
 	/** Used to indicate that the local storage cannot be used. */
 	public static final int ERROR = -1;
 	/** Used to indicate that the local storage has not yet been initialised. */
 	public static final int NOT_INITIALISED = 0;
 	/** Used to indicate that the local storage is ready for use. */
 	public static final int INITIALISED = 1;
 
 	/**
 	 * Key on the highest level of the local storage that can be used to
 	 * obtain/store a list of saved (initial) distributions.
 	 */
 	public static final String KEY_INITDIST = "INIT";
 	/**
 	 * Key on the highest level of the local storage that can be used to
 	 * obtain/store a list of saved protocols.
 	 */
 	public static final String KEY_PROTOCOLS = "PROT";
 	/**
 	 * Key on the highest level of the local storage that can be used to
 	 * obtain/store a list of saved results. A result is a complete application
 	 * state.
 	 */
 	public static final String KEY_RESULTS = "RES";
 
 	/** Error code for successful saves. */
 	public static final int SAVE_SUCCESSFUL       = 0;
 	/** Error code for when the storage is not initialised. */
 	public static final int NOT_INITIALISED_ERROR = 1;
 	/** Error code for when the name is in use. */
 	public static final int NAME_IN_USE_ERROR     = 2;
 	/** Error code for when the storage is full. */
 	public static final int QUOTA_EXCEEDED_ERROR  = 3;
 	/** Error code for an unknown key in storage. */
 	public static final int NONEXISTANT_KEY_ERROR = 4;
 	/** Error code for other errors. */
 	public static final int UNKNOWN_ERROR         = 5;
 
 	// ---- PUBLIC GLOBALS
 	// ------------------------------------------------------------------------
 	/**
 	 * Instance that must be used for interaction with local storage.
 	 */
 	public static StorageManager INSTANCE = new StorageManager();
 
 	// ---- PROTECTED GLOBALS
 	// ---------------------------------------------------------------------
 	/** GWT object that is the actual interface with the local storage. */
 	protected Storage localStorage;
 	/** State of the storage. */
 	protected int state = NOT_INITIALISED;
 
 	/** Keys that are used on the first level. */
 	protected String[] firstLevelKeys = new String[] { KEY_INITDIST,
 			KEY_PROTOCOLS, KEY_RESULTS };
 	/** Keys that are used on the second level. */
 	protected String[] secondLevelKeys = new String[] {
 			GeometryNames.CIRC_SHORT, GeometryNames.JOBE_SHORT,
 			GeometryNames.RECT_SHORT, GeometryNames.SQR_SHORT };
 
 	// ---- CONSTRUCTOR
 	// ---------------------------------------------------------------------------
 	/**
 	 * Make constructor protected to prevent instantiations of this class.
 	 */
 	protected StorageManager() {
 		localStorage = Storage.getLocalStorageIfSupported();
 		if (localStorage == null) {
 			state = ERROR;
 			return;
 		}
 
 		// Make sure that all keys are set
 		StorageMap sm = new StorageMap(localStorage);
 		for (String firstLevelKey : firstLevelKeys) {
 			// For the KEY_RESULTS, we do not have to initialise all geometries,
 			// otherwise, we do
 			if (!sm.containsKey(firstLevelKey)) {
 				if (firstLevelKey.equals(KEY_RESULTS)) {
 					try {
 						localStorage.setItem(firstLevelKey, FingerpaintJsonizer
 								.toString(new HashMap<String, Object>()));
 					} catch (JavaScriptException e) {
 						if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 							new NotificationPopupPanel(
 									"Storage capacity exceeded.").show(3000);
 						}
 					}
 				} else {
 					HashMap<String, Object> secondLevel = new HashMap<String, Object>();
 					for (String secondLevelKey : secondLevelKeys) {
 						secondLevel.put(secondLevelKey, "{}");
 					}
 					try {
 						localStorage.setItem(firstLevelKey,
 								FingerpaintJsonizer.toString(secondLevel));
 					} catch (JavaScriptException e) {
 						if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 							new NotificationPopupPanel(
 									"Storage capacity exceeded.").show(3000);
 						}
 					}
 				}
 			} else if (!firstLevelKey.equals(KEY_RESULTS)) {
 				HashMap<String, Object> secondLevel = FingerpaintJsonizer
 						.hashMapFromString(sm.get(firstLevelKey));
 				boolean changed = false;
 				for (String secondLevelKey : secondLevelKeys) {
 					if (!secondLevel.containsKey(secondLevelKey)) {
 						secondLevel.put(secondLevelKey, "{}");
 						changed = true;
 					}
 				}
 				if (changed) {
 					try {
 						localStorage.setItem(firstLevelKey,
 								FingerpaintJsonizer.toString(secondLevel));
 					} catch (JavaScriptException e) {
 						if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 							new NotificationPopupPanel(
 									"Storage capacity exceeded.").show(3000);
 						}
 					}
 				}
 			}
 		}
 
 		state = INITIALISED;
 	}
 
 	// ---- PUBLIC PART OF CLASS
 	// ------------------------------------------------------------------
 	/**
 	 * Return the distribution that is stored with given name, or {@code null}
 	 * if such a distribution does not exist.
 	 * 
 	 * @param geometry
 	 *            The geometry in which the distribution is stored.
 	 * @param key
 	 *            The name of the saved distribution.
 	 * @return The saved distribution, or {@code null} if no distribution with
 	 *         the given name was saved. This function will also return
 	 *         {@code null} if the storage cannot be used.
 	 */
 	public int[] getDistribution(String geometry, String key) {
 		if (state != INITIALISED) {
 			return null;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_INITDIST));
 		if (firstLevel.containsKey(geometry)) {
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> secondLevel = (HashMap<String, Object>) firstLevel
 					.get(geometry);
 			for (String secondLevelKey : secondLevel.keySet()) {
 				if (secondLevelKey.equals(key)) {
 					Object[] val = (Object[]) secondLevel.get(secondLevelKey);
 					int[] result = new int[val.length];
 					for (int i = 0; i < val.length; i++) {
 						result[i] = ((Double) val[i]).intValue();
 					}
 					return result;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return a list of all distributions stored under the given geometry.
 	 * 
 	 * @param geometry
 	 *            The geometry under which saved distributions need to be found.
 	 * @return A list of all saved distributions (may be an empty list), or
 	 *         {@code null} if the given geometry is not at all present in the
 	 *         storage. When the latter happens, you probably asked for a
 	 *         non-existent geometry, because all default geometries are loaded
 	 *         in the storage on initialisation. This function will also return
 	 *         {@code null} if the storage cannot be used.
 	 */
 	public List<String> getDistributions(String geometry) {
 		if (state != INITIALISED) {
 			return null;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_INITDIST), false);
 		if (firstLevel.containsKey(geometry)) {
 			HashMap<String, Object> secondLevel = FingerpaintJsonizer
 					.hashMapFromJSONObject(
 							((JSONValue) firstLevel.get(geometry)).isObject(),
 							false);
 
 			ArrayList<String> result = new ArrayList<String>();
 			result.addAll(secondLevel.keySet());
 			return result;
 		}
 		return null;
 	}
 
 	/**
 	 * Return a list of all protocols stored under the given geometry.
 	 * 
 	 * @param geometry
 	 *            The geometry under which saved protocols need to be found.
 	 * @return A list of all saved protocols (may be an empty list), or
 	 *         {@code null} if the given geometry is not at all present in the
 	 *         storage. When the latter happens, you probably asked for a
 	 *         non-existent geometry, because all default geometries are loaded
 	 *         in the storage on initialisation. This function will also return
 	 *         {@code null} if the storage cannot be used.
 	 */
 	public ArrayList<String> getProtocols(String geometry) {
 		if (state != INITIALISED) {
 			return null;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_PROTOCOLS), false);
 
 		if (firstLevel.containsKey(geometry)) {
 			HashMap<String, Object> secondLevel = FingerpaintJsonizer
 					.hashMapFromJSONObject(
 							((JSONValue) firstLevel.get(geometry)).isObject(),
 							false);
 
 			ArrayList<String> result = new ArrayList<String>();
 			result.addAll(secondLevel.keySet());
 			return result;
 		}
 		return null;
 	}
 
 	/**
 	 * Return the protocol that is stored with given name, or {@code null} if
 	 * such a protocol does not exist.
 	 * 
 	 * @param geometry
 	 *            The geometry in which the protocol is stored.
 	 * @param key
 	 *            The name of the saved protocol.
 	 * @return The saved protocol, or {@code null} if no protocol with the given
 	 *         name was saved. This function will also return {@code null} if
 	 *         the storage cannot be used.
 	 */
 	public MixingProtocol getProtocol(String geometry, String key) {
 		if (state != INITIALISED) {
 			return null;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_PROTOCOLS), false);
 		if (firstLevel.containsKey(geometry)) {
 			HashMap<String, Object> secondLevel = FingerpaintJsonizer
 					.hashMapFromString(firstLevel.get(geometry).toString(),
 							false);
 			for (String secondLevelKey : secondLevel.keySet()) {
 				if (secondLevelKey.equals(key)) {
 
 					return FingerpaintJsonizer.protocolFromString(secondLevel
 							.get(key).toString());
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return a list of all results stored in the local storage currently.
 	 * 
 	 * @return A list of all saved results (may be an empty list), or
 	 *         {@code null} if the storage cannot be used.
 	 */
 	public List<String> getResults() {
 		if (state != INITIALISED) {
 			return null;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_RESULTS), false);
 		ArrayList<String> result = new ArrayList<String>();
 		result.addAll(firstLevel.keySet());
 
 		return result;
 	}
 
 	/**
 	 * Return the result that is stored with given name, or {@code null} if such
 	 * a result does not exist.
 	 * 
 	 * @param key
 	 *            The name of the saved result.
 	 * @return The saved result, or {@code null} if no result with the given
 	 *         name was saved. This function will also return {@code null} if
 	 *         the storage cannot be used.
 	 * */
 	public ResultStorage getResult(String key) {
 		if (state != INITIALISED) {
 			return null;
 		}
 		
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_RESULTS), false);
 		for (String firstLevelKey : firstLevel.keySet()) {
 			if (firstLevelKey.equals(key)) {
 				String result = firstLevel.get(key).toString();
 				return FingerpaintJsonizer.resultStorageFromString(result);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Save an initial distribution to the local storage. If the name already
 	 * exists, do not attempt to overwrite.
 	 * 
 	 * @param geometry
 	 *            The geometry to store the distribution under.
 	 * @param key
 	 *            The name of the distribution.
 	 * @param value
 	 *            The distribution to be saved.
 	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul> 
 	 */
 	public int putDistribution(String geometry, String key, int[] value) {
 		return putDistribution(geometry, key, value, false);
 	}
 
 	/**
 	 * Save an initial distribution to the local storage. If the name already
 	 * exists, does overwrite when asked.
 	 * 
 	 * @param geometry
 	 *            The geometry to store the distribution under.
 	 * @param key
 	 *            The name of the distribution.
 	 * @param value
 	 *            The distribution to be saved.
 	 * @param overwrite
 	 *            If the value should be overwritten if the name is already in
 	 *            use.
 	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul>         
 	 */
 	public int putDistribution(String geometry, String key, int[] value,
 			boolean overwrite) {
 		if (state != INITIALISED) {
 			return NOT_INITIALISED_ERROR;
 		}
 
 		if (isNameInUse(KEY_INITDIST, geometry, key) && !overwrite) {
 			return NAME_IN_USE_ERROR;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_INITDIST), false);
 
 		if (firstLevel.containsKey(geometry)) {
 			HashMap<String, Object> secondLevel = FingerpaintJsonizer
 					.hashMapFromJSONObject(
 							((JSONValue) firstLevel.get(geometry)).isObject(),
 							false);
 			secondLevel.put(key,
 					FingerpaintZipper.zip(FingerpaintJsonizer.toString(value)));
 			firstLevel.put(geometry, FingerpaintJsonizer.toString(secondLevel));
 			try {
 				localStorage.setItem(KEY_INITDIST,
 						FingerpaintJsonizer.toString(firstLevel));
 			} catch (JavaScriptException e) {
 				if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 					return QUOTA_EXCEEDED_ERROR;
 				}
 				return UNKNOWN_ERROR;
 			}
 
 			return SAVE_SUCCESSFUL;
 		}
 
 		return NONEXISTANT_KEY_ERROR;
 	}
 
 	/**
 	 * Save a mixing protocol to the local storage. If the name already exists,
 	 * do not attempt to overwrite.
 	 * 
 	 * @param geometry
 	 *            The geometry to store the distribution under.
 	 * @param key
 	 *            The name of the protocol.
 	 * @param protocol
 	 *            The protocol to be saved.
 	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul> 
 	 */
 	public int putProtocol(String geometry, String key,
 			MixingProtocol protocol) {
 		return putProtocol(geometry, key, protocol, false);
 	}
 
 	/**
 	 * Save a mixing protocol to the local storage. If the name already exists,
 	 * do not attempt to overwrite.
 	 * 
 	 * @param geometry
 	 *            The short name of the geometry to store the distribution
 	 *            under.
 	 * @param key
 	 *            The name of the protocol.
 	 * @param protocol
 	 *            The protocol to be saved.
 	 * @param overwrite
 	 *            If the value should be overwritten if the name is already in
 	 *            use.
 	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul> 
 	 */
 	public int putProtocol(String geometry, String key,
 			MixingProtocol protocol, boolean overwrite) {
 		if (state != INITIALISED) {
 			return NOT_INITIALISED_ERROR;
 		}
 
 		if (isNameInUse(KEY_PROTOCOLS, geometry, key) && !overwrite) {
 			return NAME_IN_USE_ERROR;
 		}
 
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_PROTOCOLS));
 
 		if (firstLevel.containsKey(geometry)) {
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> secondLevel = (HashMap<String, Object>) firstLevel
 					.get(geometry);
 			secondLevel.put(key, FingerpaintJsonizer.toString(protocol));
 			firstLevel.put(geometry, FingerpaintJsonizer.toString(secondLevel));
 			try {
 				localStorage.setItem(KEY_PROTOCOLS,
 						FingerpaintJsonizer.toString(firstLevel));
 			} catch (JavaScriptException e) {
 				if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 					return QUOTA_EXCEEDED_ERROR;
 				}
 				return UNKNOWN_ERROR;
 			}
 			return SAVE_SUCCESSFUL;
 		}
 
 		return NONEXISTANT_KEY_ERROR;
 	}
 
 	/**
 	 * Save a mixing result to the local storage. If the name already exists, do
 	 * not attempt to overwrite.
 	 * 
 	 * @param key
 	 *            The name of the result.
 	 * @param result
 	 *            The result to be saved.
 	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul>
 	 */
 	public int putResult(String key, ResultStorage result) {
 		return putResult(key, result, false);
 	}
 
 	/**
 	 * Save a mixing result to the local storage. If the name already exists, do
 	 * not attempt to overwrite.
 	 * 
 	 * @param key
 	 *            The name of the result.
 	 * @param result
 	 *            The result to be saved.
 	 * @param overwrite
 	 *            If the value should be overwritten if the name is already in
 	 *            use.
 	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul> 
 	 */
 	public int putResult(String key, ResultStorage result, boolean overwrite) {
 		if (state != INITIALISED) {
 			return NOT_INITIALISED_ERROR;
 		}
 
 		if (isNameInUse(KEY_RESULTS, key, null) && !overwrite) {
 			return NAME_IN_USE_ERROR;
 		}
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
				.hashMapFromString(localStorage.getItem(KEY_RESULTS));
 		firstLevel.put(key, FingerpaintJsonizer.toString(result));
 		try {
 			localStorage.setItem(KEY_RESULTS,
 					FingerpaintJsonizer.toString(firstLevel));
 		} catch (JavaScriptException e) {
 			if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 				return QUOTA_EXCEEDED_ERROR;
 			} else {
 				Logger.getLogger("").log(Level.SEVERE,
 						"Unknown error during saving: " + e.getDescription());
 				return UNKNOWN_ERROR;
 			}
 		}
 		return SAVE_SUCCESSFUL;
 	}
 
 	/**
 	 * Remove a distribution with the given name from the local storage.
 	 * 
 	 * @param geometry
 	 *            The short name of the geometry the distribution resides under.
 	 * @param key
 	 *            The name of the distribution to remove.
 	 * @return True if a distribution with the given name is removed from the
 	 *         storage, false if no distribution with the given name is present
 	 *         in the storage.
 	 */
 	public boolean removeDistribution(String key, String geometry) {
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_INITDIST));
 		if (firstLevel.containsKey(geometry)) {
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> secondLevel = (HashMap<String, Object>) firstLevel
 					.get(geometry);
 			for (String secondLevelKey : secondLevel.keySet()) {
 				if (secondLevelKey.equals(key)) {
 					secondLevel.remove(key);
 					try {
 						localStorage.setItem(KEY_INITDIST,
 								FingerpaintJsonizer.toString(firstLevel));
 					} catch (JavaScriptException e) {
 						new NotificationPopupPanel("Storage capacity exceeded.")
 								.show(3000);
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Remove a protocol with the given name from the local storage.
 	 * 
 	 * @param geometry
 	 *            The short name of the geometry the protocol resides under.
 	 * @param key
 	 *            The name of the protocol to remove.
 	 * @return True if a protocol with the given name is removed from the
 	 *         storage, false if no protocol with the given name is present in
 	 *         the storage.
 	 */
 	public boolean removeProtocol(String key, String geometry) {
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_PROTOCOLS));
 		if (firstLevel.containsKey(geometry)) {
 			@SuppressWarnings("unchecked")
 			HashMap<String, Object> secondLevel = (HashMap<String, Object>) firstLevel
 					.get(geometry);
 			for (String secondLevelKey : secondLevel.keySet()) {
 				if (secondLevelKey.equals(key)) {
 					secondLevel.remove(key);
 					try {
 						localStorage.setItem(KEY_PROTOCOLS,
 								FingerpaintJsonizer.toString(firstLevel));
 					} catch (JavaScriptException e) {
 						new NotificationPopupPanel("Storage capacity exceeded.")
 								.show(3000);
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Remove a result with the given name from the local storage.
 	 * 
 	 * @param key
 	 *            The name of the result to remove.
 	 * @return True if a result with the given name is removed from the storage,
 	 *         false if no result with the given name is present in the storage.
 	 */
 	public boolean removeResult(String key) {
 		HashMap<String, Object> firstLevel = FingerpaintJsonizer
 				.hashMapFromString(localStorage.getItem(KEY_RESULTS));
 		for (String firstLevelKey : firstLevel.keySet()) {
 			if (firstLevelKey.equals(key)) {
 				firstLevel.remove(key);
 				try {
 					localStorage.setItem(KEY_RESULTS,
 							FingerpaintJsonizer.toString(firstLevel));
 				} catch (JavaScriptException e) {
 					if (e.getName().equals("QUOTA_EXCEEDED_ERR")) {
 						new NotificationPopupPanel("Storage capacity exceeded.")
 								.show(3000);
 					}
 				}
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	// ---- PROTECTED PART OF CLASS
 	// ---------------------------------------------------------------
 	/**
 	 * Returns if there is a key at the specified level with given name. When
 	 * you want to test a key on the second level, leave the value for the third
 	 * level {@code null}.
 	 * 
 	 * @param firstLevelKey
 	 *            The key on the highest level. Needs to be set, needs to be one
 	 *            of {@link #KEY_INITDIST}, {@link #KEY_PROTOCOLS} or
 	 *            {@link #KEY_RESULTS}.
 	 * @param secondLevelKey
 	 *            The key on the second level. Needs to be set and either some
 	 *            name, to test if that name exists among all saved results, or
 	 *            a geometry name. In the latter case, it should be one of the
 	 *            short names from the {@link GeometryNames}.
 	 * @param thirdLevelKey
 	 *            The key on the third level. Leave this {@code null} when
 	 *            looking for a result name, or provide the name to check here
 	 *            otherwise.
 	 * @return If the key exists or not. When {@code firstLevelKey} or
 	 *         {@code secondLevelKey} are not set, then this function returns
 	 *         {@code true}.
 	 */
 	protected boolean isNameInUse(String firstLevelKey, String secondLevelKey,
 			String thirdLevelKey) {
 		// Check required keys
 		if (firstLevelKey == null || secondLevelKey == null) {
 			return true;
 		}
 		// Check if looking for second or third level
 		if (thirdLevelKey == null) {
 			// Looking for a result here
 			HashMap<String, Object> results = FingerpaintJsonizer
 					.hashMapFromString(localStorage.getItem(firstLevelKey));
 			for (String key : results.keySet()) {
 				if (key.equals(secondLevelKey)) {
 					return true;
 				}
 			}
 			return false;
 		} else {
 			// Looking for a distribution or protocol here
 			HashMap<String, Object> firstLevel = FingerpaintJsonizer
 					.hashMapFromString(localStorage.getItem(firstLevelKey));
 			if (firstLevel.containsKey(secondLevelKey)
 					&& firstLevel.get(secondLevelKey) instanceof HashMap) {
 				@SuppressWarnings("unchecked")
 				HashMap<String, Object> secondLevel = (HashMap<String, Object>) firstLevel
 						.get(secondLevelKey);
 				for (String key : secondLevel.keySet()) {
 					if (key.equals(thirdLevelKey)) {
 						return true;
 					}
 				}
 				return false;
 			}
 			return false;
 		}
 	}
 }
