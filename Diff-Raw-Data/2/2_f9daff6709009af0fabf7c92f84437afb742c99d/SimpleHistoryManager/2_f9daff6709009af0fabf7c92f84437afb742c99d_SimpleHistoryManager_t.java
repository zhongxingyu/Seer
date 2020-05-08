 package org.vpac.historyRepeater;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.log4j.Logger;
 import org.vpac.historyRepeater.model.HistoryNode;
 
 /**
  * Just a quick and dirty implementation of the history interface. Let's see
  * whether there are proplems.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class SimpleHistoryManager implements HistoryManager {
 
 	static final Logger myLogger = Logger.getLogger(SimpleHistoryManager.class
 			.getName());
 
 	public Map<String, HistoryNode> nodes = new HashMap<String, HistoryNode>();
 
 	PropertiesConfiguration config = null;
 
 	public SimpleHistoryManager(File configFile) {
 		try {
 			config = new PropertiesConfiguration();
 			config.setDelimiterParsingDisabled(true);
 			config.load(configFile);
 			config.setFile(configFile);
 			config.setAutoSave(true);
 		} catch (ConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void addHistoryEntry(String key, String entry) {
 		addHistoryEntry(key, entry, new Date());
 	}
 
 	public void addHistoryEntry(String key, String entry, Date date) {
 		addHistoryEntry(key, entry, date, -1);
 	}
 
 	public void addHistoryEntry(String key, String entry, Date date,
 			int numberOfEntriesForParentNode) {
 
 		if (entry == null || "".equals(entry.trim())) {
 			return;
 		}
 
 		if (numberOfEntriesForParentNode > 0) {
 			getHistoryNode(key).setMaxNumberOfEntries(
 					numberOfEntriesForParentNode);
 			// config.addProperty(key+"_max", numberOfEntriesForParentNode);
 		}
 		getHistoryNode(key).addEntry(entry, date);
 
 		config.clearProperty(key);
 		String[] new_prop = new String[getHistoryNode(key).getEntries().size()];
 		int i = 0;
 		for (Date dateKey : getHistoryNode(key).getEntriesMap().keySet()) {
 			new_prop[i] = new Long(dateKey.getTime()).toString() + ","
 					+ getHistoryNode(key).getEntriesMap().get(dateKey);
 
 			config.addProperty(key, new_prop[i]);
 			i++;
 		}
 
 	}
 
 	public int getDefaultNumberOfEntriesPerNode() {
 		try {
 			int def = config.getInt("default_maximum_entries");
 			return def;
 		} catch (Exception e) {
 			config.clearProperty("default_max");
 			config.setProperty("default_max", DEFAULT_NUMBER_OF_ENTRIES);
 			return DEFAULT_NUMBER_OF_ENTRIES;
 		}
 	}
 
 	public List<String> getEntries(String key) {
 		return getHistoryNode(key).getEntries();
 	}
 
 	private HistoryNode getHistoryNode(String key) {
 
 		HistoryNode node = nodes.get(key);
 
 		if (node == null) {
 
 			String[] entries = config.getStringArray(key);
 
 			int maxEntries = getMaxNumberOfEntries(key);
 			// try {
 			// maxEntries = config.getInt(key+"_max");
 			// } catch (Exception e) {
 			// config.clearProperty(key+"_max");
 			// config.setProperty(key+"_max", maxEntries);
 			// }
 
 			node = new HistoryNode(maxEntries);
 
 			for (String entry : entries) {
 				int index = entry.indexOf(",");
 				if (index == -1) {
 					continue;
 				}
 				Long date;
 				try {
 					date = Long.parseLong(entry.substring(0, index));
 				} catch (Exception e) {
 					myLogger.warn("Could not parse date of entry: " + entry
 							+ ". Returning null value");
 					continue;
 				}
 				String value = entry.substring(index + 1);
 				node.addEntry(value, new Date(date));
 			}
 
 			nodes.put(key, node);
 		}
 
 		return node;
 	}
 
 	public String getLastEntry(String key) {
		if (getEntries(key) == null || getEntries(key).size() == 0) {
 			return null;
 		}
 		return getEntries(key).get(0);
 	}
 
 	public int getMaxNumberOfEntries(String key) {
 		if (nodes.get(key) != null) {
 			return nodes.get(key).getMaxNumberOfEntries();
 		}
 		int entries = config.getInt("default_max_" + key, -1);
 		if (entries <= 0) {
 			return getDefaultNumberOfEntriesPerNode();
 		} else {
 			return entries;
 		}
 	}
 
 	public void setDefaultNumberOfEntriesPerNode(int i) {
 		config.clearProperty("default_maximum_entries");
 		config.setProperty("default_maximum_entries", i);
 	}
 
 	public void setMaxNumberOfEntries(String key, int max) {
 		getHistoryNode(key).setMaxNumberOfEntries(max);
 
 		config.clearProperty("default_max_" + key);
 		config.setProperty("default_max_" + key, max);
 
 	}
 
 }
