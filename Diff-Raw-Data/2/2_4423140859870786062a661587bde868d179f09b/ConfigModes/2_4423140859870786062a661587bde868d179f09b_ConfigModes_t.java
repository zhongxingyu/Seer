 package nl.cyso.vcloud.client.config;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import nl.cyso.vcloud.client.Version;
 import nl.cyso.vcloud.client.types.ModeType;
 
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionGroup;
 
 public class ConfigModes {
 
 	private static Map<ModeType, ConfigMode> modeMap;
 
 	static {
 		ConfigModes.modeMap = new HashMap<ModeType, ConfigMode>(ModeType.values().length);
 
 		// Configuration file
 		ConfigParameter config = new ConfigParameter("c", "config", true, "FILE", "Use a configuration file");
 
 		// Connection information
 		ConfigParameter username = new ConfigParameter("u", "username", true, "USER", "vCloud Director username");
 		ConfigParameter password = new ConfigParameter("p", "password", true, "PASSWORD", "vCloud Director password");
 		ConfigParameter server = new ConfigParameter("s", "server", true, "SERVER", "vCloud Director server URI");
 
 		// Modes
 		ConfigParameter helpmode = new ConfigParameter("h", "help", true, "COMMAND", "Show help and examples");
 		helpmode.setOptionalArg(true);
 		ConfigParameter versionmode = new ConfigParameter("v", "version", false, "Show version information");
 		ConfigParameter listmode = new ConfigParameter("l", "list", true, "TYPE", "List vCloud objects (org|vdc|vapp|catalog|vm)");
 		ConfigParameter addmode = new ConfigParameter("a", "add-vm", false, "Add a new VM from a vApp Template to an existing vApp");
 		ConfigParameter removemode = new ConfigParameter("r", "remove-vm", false, "Remove a VM from an existing vApp");
 		ConfigParameter poweronmode = new ConfigParameter("y", "poweron-vm", false, "Start an existing VM");
 		ConfigParameter poweroffmode = new ConfigParameter("t", "poweroff-vm", false, "Stop an existing VM (hard shutdown)");
		ConfigParameter shutdownmode = new ConfigParameter("z", "shutdown-vm", false, "Shutdown an existing VM (soft shutdown)");
 		ConfigParameter resizediskmode = new ConfigParameter("w", "resize-disk", false, "Resize the disk of an existing VM");
 		ConfigParameter consolidatemode = new ConfigParameter("x", "consolidate-vm", false, "Consolidate all disks of an existing VM");
 
 		// Selectors
 		ConfigParameter organization = new ConfigParameter("organization", true, "ORG", "Select this Organization");
 		ConfigParameter vdc = new ConfigParameter("vdc", true, "VDC", "Select this Virtual Data Center");
 		ConfigParameter vapp = new ConfigParameter("vapp", true, "VAPP", "Select this vApp");
 		ConfigParameter vm = new ConfigParameter("vm", true, "VM", "Select this VM");
 
 		ConfigParameter catalog = new ConfigParameter("catalog", true, "CATALOG", "Select this catalog");
 		ConfigParameter template = new ConfigParameter("template", true, "TEMPLATE", "Select this template");
 
 		// User input
 		ConfigParameter fqdn = new ConfigParameter("fqdn", true, "FQDN", "Name of object to create");
 		ConfigParameter description = new ConfigParameter("description", true, "DESC", "Description of object to create");
 		ConfigParameter ip = new ConfigParameter("ip", true, "IP", "IP of the object to create");
 		ConfigParameter network = new ConfigParameter("network", true, "NETWORK", "Network of the object to create");
 
 		ConfigParameter diskname = new ConfigParameter("disk-name", true, "DISK", "Name of disk to resize");
 		ConfigParameter disksize = new ConfigParameter("disk-size", true, "SIZE", "New size of disk in MB");
 
 		OptionGroup modes = new OptionGroup();
 		modes.addOption(listmode);
 		modes.addOption(helpmode);
 		modes.addOption(versionmode);
 		modes.addOption(addmode);
 		modes.addOption(removemode);
 		modes.addOption(poweronmode);
 		modes.addOption(poweroffmode);
 		modes.addOption(shutdownmode);
 		modes.addOption(resizediskmode);
 		modes.addOption(consolidatemode);
 		modes.setRequired(true);
 
 		// Options for root
 		ConfigMode root = new ConfigMode();
 		root.addOptionGroup(modes);
 		root.addOption(config);
 		root.addOption(username);
 		root.addOption(password);
 		root.addOption(server);
 
 		// Options for list
 		ConfigMode list = new ConfigMode();
 		list.addRequiredOption(listmode);
 		list.addOption(server);
 		list.addOption(username);
 		list.addOption(password);
 		list.addOption(config);
 
 		// Options for add-vm
 		ConfigMode add = new ConfigMode();
 		add.addRequiredOption(addmode);
 		add.addOption(server);
 		add.addOption(username);
 		add.addOption(password);
 		add.addOption(config);
 
 		add.addRequiredOption(organization);
 		add.addRequiredOption(vdc);
 		add.addRequiredOption(vapp);
 		add.addRequiredOption(catalog);
 		add.addRequiredOption(template);
 
 		add.addRequiredOption(fqdn);
 		add.addRequiredOption(description);
 		add.addRequiredOption(network);
 		add.addRequiredOption(ip);
 
 		// Options for remove-vm
 		ConfigMode remove = new ConfigMode();
 		remove.addRequiredOption(removemode);
 		remove.addOption(server);
 		remove.addOption(username);
 		remove.addOption(password);
 		remove.addOption(config);
 
 		remove.addRequiredOption(organization);
 		remove.addRequiredOption(vdc);
 		remove.addRequiredOption(vapp);
 		remove.addRequiredOption(vm);
 
 		// Options for poweron-vm
 		ConfigMode poweron = new ConfigMode();
 		poweron.addRequiredOption(poweronmode);
 		poweron.addOption(server);
 		poweron.addOption(username);
 		poweron.addOption(password);
 		poweron.addOption(config);
 
 		poweron.addRequiredOption(organization);
 		poweron.addRequiredOption(vdc);
 		poweron.addRequiredOption(vapp);
 		poweron.addRequiredOption(vm);
 
 		// Options for poweroff-vm
 		ConfigMode poweroff = new ConfigMode();
 		poweroff.addRequiredOption(poweroffmode);
 		poweroff.addOption(server);
 		poweroff.addOption(username);
 		poweroff.addOption(password);
 		poweroff.addOption(config);
 
 		poweroff.addRequiredOption(organization);
 		poweroff.addRequiredOption(vdc);
 		poweroff.addRequiredOption(vapp);
 		poweroff.addRequiredOption(vm);
 
 		// Options for shutdown-vm
 		ConfigMode shutdown = new ConfigMode();
 		shutdown.addRequiredOption(shutdownmode);
 		shutdown.addOption(server);
 		shutdown.addOption(username);
 		shutdown.addOption(password);
 		shutdown.addOption(config);
 
 		shutdown.addRequiredOption(organization);
 		shutdown.addRequiredOption(vdc);
 		shutdown.addRequiredOption(vapp);
 		shutdown.addRequiredOption(vm);
 
 		// Options for resize-disk
 		ConfigMode resizedisk = new ConfigMode();
 		resizedisk.addRequiredOption(resizediskmode);
 		resizedisk.addOption(server);
 		resizedisk.addOption(username);
 		resizedisk.addOption(password);
 		resizedisk.addOption(config);
 
 		resizedisk.addRequiredOption(organization);
 		resizedisk.addRequiredOption(vdc);
 		resizedisk.addRequiredOption(vapp);
 		resizedisk.addRequiredOption(vm);
 		resizedisk.addRequiredOption(diskname);
 		resizedisk.addRequiredOption(disksize);
 
 		// Options for consolidate-vm
 		ConfigMode consolidate = new ConfigMode();
 		consolidate.addRequiredOption(consolidatemode);
 		consolidate.addOption(server);
 		consolidate.addOption(username);
 		consolidate.addOption(password);
 		consolidate.addOption(config);
 
 		consolidate.addRequiredOption(organization);
 		consolidate.addRequiredOption(vdc);
 		consolidate.addRequiredOption(vapp);
 		consolidate.addRequiredOption(vm);
 
 		// Options for help
 		ConfigMode help = new ConfigMode();
 		help.addRequiredOption(helpmode);
 
 		// Options for version
 		ConfigMode version = new ConfigMode();
 		version.addRequiredOption(versionmode);
 
 		ConfigModes.addMode(ModeType.ROOT, root);
 		ConfigModes.addMode(ModeType.HELP, help);
 		ConfigModes.addMode(ModeType.VERSION, version);
 		ConfigModes.addMode(ModeType.LIST, list);
 		ConfigModes.addMode(ModeType.ADDVM, add);
 		ConfigModes.addMode(ModeType.REMOVEVM, remove);
 		ConfigModes.addMode(ModeType.POWERONVM, poweron);
 		ConfigModes.addMode(ModeType.POWEROFFVM, poweroff);
 		ConfigModes.addMode(ModeType.SHUTDOWNVM, shutdown);
 		ConfigModes.addMode(ModeType.RESIZEDISK, resizedisk);
 		ConfigModes.addMode(ModeType.CONSOLIDATEVM, consolidate);
 	}
 
 	private static void addMode(ModeType type, ConfigMode mode) {
 		ConfigModes.getModes().put(type, mode);
 	}
 
 	public static ConfigMode getMode(ModeType mode) {
 		return ConfigModes.modeMap.get(mode);
 	}
 
 	public static Map<ModeType, ConfigMode> getModes() {
 		return ConfigModes.modeMap;
 	}
 
 	public static ConfigMode getConsolidatedModes() {
 		ConfigMode all = new ConfigMode();
 
 		for (ConfigMode mode : ConfigModes.modeMap.values()) {
 			for (ConfigParameter opt : mode.getAllOptions()) {
 				opt.setRequired(false);
 				all.addOption(opt);
 			}
 		}
 
 		return all;
 	}
 
 	public static void printAllHelp() {
 		HelpFormatter format = new HelpFormatter();
 
 		for (ModeType mode : ConfigModes.getModes().keySet()) {
 			format.setSyntaxPrefix(String.format("%s mode: ", mode.toString()));
 			format.printHelp(Version.PROJECT_NAME, ConfigModes.getMode(mode), true);
 		}
 	}
 
 	public static void printConfigModeHelp(ModeType mode) {
 		HelpFormatter format = new HelpFormatter();
 		if (mode != ModeType.ROOT) {
 			format.setSyntaxPrefix(String.format("Usage for %s mode: ", mode.toString()));
 		}
 		format.printHelp(Version.PROJECT_NAME, ConfigModes.getMode(mode), true);
 	}
 }
