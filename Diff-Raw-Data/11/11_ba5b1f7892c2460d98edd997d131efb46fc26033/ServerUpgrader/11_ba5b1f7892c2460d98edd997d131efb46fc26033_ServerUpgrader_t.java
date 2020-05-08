 package net.batkin.forms.server.upgrade;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import net.batkin.forms.server.configuration.Configuration;
 import net.batkin.forms.server.configuration.ConfigurationOption;
 import net.batkin.forms.server.db.dataModel.Config;
 import net.batkin.forms.server.exception.ServerDataException;
 import net.batkin.forms.server.upgrade.exception.UpgradeException;
 import net.batkin.forms.server.util.ConfigBuilder;
 
 import org.apache.commons.lang.StringUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.slf4j.LoggerFactory;
 
 public class ServerUpgrader {
 	public static void upgradeDatabase() throws IOException, UpgradeException, ServerDataException {
 		Configuration config = Configuration.getInstance();
 		Set<String> appliedUpgrades = new HashSet<String>(config.getValues("appliedUpgrades"));
 		int appliedCount = appliedUpgrades.size();
 
 		String upgradeDir = ConfigBuilder.getResourceDirectory("upgrades", ConfigurationOption.CONFIG_UPGRADES_DIR);
 		String customUpgradeDir = ConfigBuilder.getResourceDirectory("custom-upgrades", ConfigurationOption.CONFIG_CUSTOM_UPGRADES_DIR);
 		loadUpgrades(upgradeDir, appliedUpgrades);
 		if (new File(customUpgradeDir).isDirectory()) {
 			loadUpgrades(customUpgradeDir, appliedUpgrades);
 		}
 
 		if (appliedCount != appliedUpgrades.size()) {
 			LoggerFactory.getLogger(ServerUpgrader.class).info("Applied upgrades: " + StringUtils.join(appliedUpgrades, ", "));
 
 			Config dbConfig = createOrFindConfig("server");
 			dbConfig.put("appliedUpgrades", new ArrayList<String>(appliedUpgrades));
 			dbConfig.save();
 		}
 	}
 
 	private static void loadUpgrades(String dir, Set<String> appliedUpgrades) throws IOException, UpgradeException {
 		File[] files = new File(dir).listFiles(new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String name) {
 				if (name.endsWith(".json")) {
 					return true;
 				}
 				return false;
 			}
 		});
 
		if (files == null) {
			return;
		}
 		Set<ServerUpgrade> upgrades = new HashSet<ServerUpgrade>();
 		for (File file : files) {
 			try {
 				ServerUpgrade upgrade = loadUpgrade(file);
 				if (!appliedUpgrades.contains(upgrade.getName())) {
 					upgrades.add(upgrade);
 				}
 			} catch (JSONException e) {
 				throw new UpgradeException("Problem while running upgrade from file " + file.getName() + ": " + e.getMessage(), e);
 			}
 		}
 
 		while (!upgrades.isEmpty()) {
 			boolean didSomething = false;
 			Iterator<ServerUpgrade> iter = upgrades.iterator();
 			while (iter.hasNext()) {
 				ServerUpgrade upgrade = iter.next();
 				String upgradeName = upgrade.getName();
 				Set<String> dependencies = upgrade.getDependencies();
 
 				boolean canApply = true;
 				for (String dependency : dependencies) {
 					if (!appliedUpgrades.contains(dependency)) {
 						canApply = false;
 						break;
 					}
 				}
 
 				if (canApply) {
 					LoggerFactory.getLogger(ServerUpgrader.class).info("Applying upgrade " + upgradeName);
 					upgrade.apply();
 					didSomething = true;
 					iter.remove();
 					appliedUpgrades.add(upgradeName);
 				}
 			}
 
 			if (!didSomething) {
 				throw new UpgradeException("Unable to apply the following upgrades due to missing dependencies: " + StringUtils.join(upgrades, ", "));
 			}
 		}
 	}
 
 	private static ServerUpgrade loadUpgrade(File file) throws IOException, JSONException, UpgradeException {
 		FileReader reader = new FileReader(file);
 		JSONObject json = new JSONObject(new JSONTokener(reader));
 		ServerUpgrade upgrade = new ServerUpgrade(file.getName(), json);
 		return upgrade;
 	}
 
 	public static Config createOrFindConfig(String name) throws ServerDataException {
 		Config dbConfig = Config.loadByName("server");
 		if (dbConfig == null) {
 			dbConfig = Config.createAndSaveNewConfig("server", new HashMap<String, List<String>>());
 		}
 		return dbConfig;
 	}
 }
