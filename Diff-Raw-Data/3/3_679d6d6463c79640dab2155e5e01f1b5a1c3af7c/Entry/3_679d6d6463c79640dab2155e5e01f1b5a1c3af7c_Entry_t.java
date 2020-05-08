 package nl.cyso.vcloud.client;
 
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.cli.AlreadySelectedException;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.MissingArgumentException;
 import org.apache.commons.cli.MissingOptionException;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import com.vmware.vcloud.sdk.Task;
 import com.vmware.vcloud.sdk.VCloudException;
 
 public class Entry {
 	public static void usageError(String error, Options opts) {
 		System.out.println(error + "\n");
 		new HelpFormatter().printHelp(Version.PROJECT_NAME, opts, true);
 		System.exit(-1);
 	}
 
 	public static void waitForTaskCompletion(Task task) {
 		String message = "Waiting for task completion ";
 		String[] twirl = new String[] { "-", "\\", "|", "/" };
 		boolean wait = true;
 		int counter = 0;
 		while (wait) {
 			System.out.print("\r" + message + twirl[counter % twirl.length]);
 
 			try {
 				task.waitForTask(20);
 			} catch (TimeoutException e) {
 				// Still waiting...
 				counter++;
 				continue;
 			} catch (VCloudException vce) {
 				System.out.print("\n");
 				System.err.println("An error occured while executing task");
 				System.err.println(vce.getLocalizedMessage());
 				System.exit(-1);
 			}
 
 			System.out.print("\n");
 			System.out.println("Done");
 			wait = false;
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	public static void main(String[] args) {
 		Options opt = new Options();
 
 		// Configuration file
 		opt.addOption("c", "config", true, "Use this configuration file");
 
 		// Connection options
 		opt.addOption("u", "username", true, "vCloud Director username");
 		opt.addOption("p", "password", true, "vCloud Director password");
 		opt.addOption("s", "server", true, "vCloud Director server URI");
 
 		// Modes
 		OptionGroup modes = new OptionGroup();
 		modes.addOption(new Option("h", "help", false, "Show help"));
 		modes.addOption(new Option("v", "version", false, "Show version information"));
 		modes.addOption(new Option("l", "list", true, "List vCloud objects (org|vdc|vapp|catalog|vm)"));
 		modes.addOption(new Option("a", "add-vm", false, "Add a new VM from a vApp Template to an existing vApp"));
 		modes.addOption(new Option("r", "remove-vm", false, "Remove a VM from an existing vApp"));
 		modes.setRequired(true);
 		opt.addOptionGroup(modes);
 
 		// Selectors
 		opt.addOption(OptionBuilder.withLongOpt("organization").hasArg().withArgName("ORG").withDescription("Select this Organization").create());
 		opt.addOption(OptionBuilder.withLongOpt("vdc").hasArg().withArgName("VDC").withDescription("Select this Virtual Data Center").create());
 		opt.addOption(OptionBuilder.withLongOpt("vapp").hasArg().withArgName("VAPP").withDescription("Select this vApp").create());
 		opt.addOption(OptionBuilder.withLongOpt("vm").hasArg().withArgName("VM").withDescription("Select this VM").create());
 		opt.addOption(OptionBuilder.withLongOpt("catalog").hasArg().withArgName("CATALOG").withDescription("Select this Catalog").create());
 
 		// User input
 		opt.addOption(OptionBuilder.withLongOpt("fqdn").hasArg().withArgName("FQDN").withDescription("Name of object to create").create());
 		opt.addOption(OptionBuilder.withLongOpt("description").hasArg().withArgName("DESC").withDescription("Description of object to create").create());
 		opt.addOption(OptionBuilder.withLongOpt("template").hasArg().withArgName("TEMPLATE").withDescription("Template of object to create").create());
 		opt.addOption(OptionBuilder.withLongOpt("ip").hasArg().withArgName("IP").withDescription("IP of the object to create").create());
 		opt.addOption(OptionBuilder.withLongOpt("network").hasArg().withArgName("NETWORK").withDescription("Network of the object to create").create());
 
 		CommandLine cli = null;
 		try {
 			cli = new PosixParser().parse(opt, args);
 		} catch (MissingArgumentException me) {
 			usageError(me.getLocalizedMessage(), opt);
 			System.exit(-1);
 		} catch (MissingOptionException mo) {
 			usageError(mo.getLocalizedMessage(), opt);
 			System.exit(-1);
 		} catch (AlreadySelectedException ase) {
 			usageError(ase.getLocalizedMessage(), opt);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 
 		if (cli.hasOption("config")) {
 			Configuration.loadFile(cli.getOptionValue("config"));
 		}
 
 		Configuration.load(cli);
 
 		if (Configuration.getMode() == ModeType.HELP) {
 			new HelpFormatter().printHelp("vcloud-client", opt, true);
 			System.exit(0);
 		} else if (Configuration.getMode() == ModeType.VERSION) {
 			System.out.println(String.format("%s version %s\nBUILD_VERSION: %s", Version.PROJECT_NAME, Version.RELEASE_VERSION, Version.BUILD_VERSION));
 			System.exit(0);
 		}
 
 		if (!Configuration.hasUsername() || !Configuration.hasPassword() || !Configuration.hasServer()) {
 			usageError("No credentials were set, or server uri was missing.", opt);
 		}
 
 		vCloudClient client = new vCloudClient();
 		client.login(Configuration.getServer(), Configuration.getUsername(), Configuration.getPassword());
 
 		if (Configuration.getMode() == ModeType.LIST) {
 			if (Configuration.getListType() == null) {
 				usageError("Invalid list type was selected.", opt);
 			}
 
 			switch (Configuration.getListType()) {
 			case ORG:
 				client.listOrganizations();
 				break;
 			case VDC:
 				if (!Configuration.hasOrganization()) {
 					usageError("An organization must also be specified when listing VDCs", opt);
 				}
 				client.listVDCs(Configuration.getOrganization());
 				break;
 			case CATALOG:
 				if (!Configuration.hasOrganization()) {
 					usageError("An organization must also be specified when listing Catalogs", opt);
 				}
 				client.listCatalogs(Configuration.getOrganization());
 				break;
 			case VAPP:
 				if (!Configuration.hasOrganization()) {
 					usageError("An organization must also be specified when listing vApps", opt);
 				}
 				if (!Configuration.hasVDC()) {
 					usageError("A VDC must also be specified when listing vApps", opt);
 				}
 				client.listVApps(Configuration.getOrganization(), Configuration.getVDC());
 				break;
 			case VM:
 				if (!Configuration.hasOrganization()) {
 					usageError("An organization must also be specified when listing VMs", opt);
 				}
 				if (!Configuration.hasVDC()) {
 					usageError("A VDC must also be specified when listing VMs", opt);
 				}
 				if (!Configuration.hasVApp()) {
 					usageError("A vApp must also be specified when listing VMs", opt);
 				}
 				client.listVMs(Configuration.getOrganization(), Configuration.getVDC(), Configuration.getVApp());
 				break;
 			default:
 				System.err.println("Not yet implemented");
 				break;
 			}
 		} else if (Configuration.getMode() == ModeType.ADDVM) {
 			if (!Configuration.hasOrganization()) {
 				usageError("An existing organization has to be selected", opt);
 			}
 			if (!Configuration.hasVDC()) {
 				usageError("An existing virtual data center has to be selected", opt);
 			}
 			if (!Configuration.hasVApp()) {
 				usageError("An existing vApp has to be selected", opt);
 			}
 			if (!Configuration.hasCatalog()) {
 				usageError("An existing Catalog has to be selected", opt);
 			}
 			if (!Configuration.hasFqdn()) {
 				usageError("A FQDN has to be specified for the new VM", opt);
 			}
 			if (!Configuration.hasDescription()) {
 				usageError("A description has to be specified for the new VM", opt);
 			}
 			if (!Configuration.hasTemplate()) {
 				usageError("A template has to be specified for the new VM", opt);
 			}
 			if (!Configuration.hasIp()) {
 				usageError("An IP has to be specified for the new VM", opt);
 			}
 			if (!Configuration.hasNetwork()) {
 				usageError("A Network has to be specified for the new VM", opt);
 			}
 
 			waitForTaskCompletion(client.addVM(Configuration.getOrganization(), Configuration.getVDC(), Configuration.getVApp(), Configuration.getCatalog(), Configuration.getTemplate(), Configuration.getFqdn(), Configuration.getDescription(), Configuration.getIp().getHostAddress(), Configuration.getNetwork()));
 		} else if (Configuration.getMode() == ModeType.REMOVEVM) {
 			if (!Configuration.hasOrganization()) {
 				usageError("An existing organization has to be selected", opt);
 			}
 			if (!Configuration.hasVDC()) {
 				usageError("An existing virtual data center has to be selected", opt);
 			}
 			if (!Configuration.hasVApp()) {
 				usageError("An existing vApp has to be selected", opt);
 			}
 			if (!Configuration.hasVM()) {
 				usageError("An existing VM has to be selected", opt);
 			}
 			waitForTaskCompletion(client.removeVM(Configuration.getOrganization(), Configuration.getVDC(), Configuration.getVApp(), Configuration.getVM()));
 		} else {
 			usageError("No mode was selected", opt);
 		}
 	}
 }
