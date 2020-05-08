 package ch.unibas.gwt.torrus.client.config;
 
 import java.util.ArrayList;
 
 import com.google.gwt.user.client.ui.ListBox;
 
 public abstract class ConfigHandler {
 
 	private static final String DATASET_EXCH = "Exchange";
 	private static final String DATASET_ALTIRIS = "Altiris";
 
 	public static ConfigHandler getConfig(String string) {
 		if (DATASET_ALTIRIS.equalsIgnoreCase(string)) {
 			return new AltirisConfig();
 		}
 		return new ExchangeConfig();
 	}
 
 	public static void setSetAvailableConfigs(ListBox comboBoxDataset) {
 		comboBoxDataset.addItem(DATASET_EXCH);
 		comboBoxDataset.addItem(DATASET_ALTIRIS);
 	}
 
 
 
 	
 
 	private ArrayList<String> servers = null;
 	private ArrayList<String> values = null;
 
 	public ArrayList<String> getServers() {
 		if (servers == null) {
 			servers = buildServerList();
 		}
 		return servers;
 	}
 
 	public abstract ArrayList<String> buildServerList();
 
 	public ArrayList<String> getValues() {
 		if (values == null) {
 			values = buildValueList();
 		}
 		return values;
 	}
 
 	public abstract ArrayList<String> buildValueList();
 
 }
