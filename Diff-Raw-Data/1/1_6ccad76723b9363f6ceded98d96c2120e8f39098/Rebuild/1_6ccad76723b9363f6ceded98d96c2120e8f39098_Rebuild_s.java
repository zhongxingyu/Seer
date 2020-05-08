 /* The contents of this file are subject to the license and copyright terms
  * detailed in the license directory at the root of the source tree (also 
  * available online at http://www.fedora.info/license/).
  */
 
 package fedora.server.utilities.rebuild;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import fedora.common.Constants;
 
 import fedora.server.config.Configuration;
 import fedora.server.config.ModuleConfiguration;
 import fedora.server.config.Parameter;
 import fedora.server.config.ServerConfiguration;
 import fedora.server.config.ServerConfigurationParser;
 import fedora.server.storage.lowlevel.DefaultLowlevelStorage;
 import fedora.server.storage.lowlevel.FileSystem;
 import fedora.server.storage.translation.DODeserializer;
 import fedora.server.storage.translation.DOTranslationUtility;
 import fedora.server.storage.translation.FOXMLDODeserializer;
 import fedora.server.storage.types.BasicDigitalObject;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.utilities.ServerUtility;
 import fedora.utilities.FileComparator;
 
 /**
  * Entry-point for rebuilding various aspects of the repository.
  * 
  * @@version $Id$
  */
 public class Rebuild implements Constants {
 	private FileSystem fs;
 
 	private static FileComparator _REVERSE_FILE_COMPARATOR = new FileComparator(
 			true);
 
 	/**
 	 * Rebuilders that the rebuild utility knows about.
 	 */
 	public static String[] REBUILDERS = new String[] {
 			"fedora.server.resourceIndex.ResourceIndexRebuilder",
 			"fedora.server.utilities.rebuild.SQLRebuilder" };
 
 	public Rebuild(File serverDir, String profile) throws Exception {
 		ServerConfiguration serverConfig = getServerConfig(serverDir, profile);
 		// set these here so DOTranslationUtility doesn't try to get a Server
 		// instance
 		System.setProperty("fedoraServerHost", serverConfig.getParameter(
 				"fedoraServerHost").getValue());
 		System.setProperty("fedoraServerPort", serverConfig.getParameter(
 				"fedoraServerPort").getValue());
 
 		System.err.println();
 		System.err.println("                       Fedora Rebuild Utility");
 		System.err.println("                     ..........................");
 		System.err.println();
 		System.err
 				.println("WARNING: Live rebuilds are not currently supported.");
 		System.err
 				.println("         Make sure your server is stopped before continuing.");
 		System.err.println();
 		System.err.println("Server directory is " + serverDir.toString());
 		if (profile != null) {
 		    System.err.print("Server profile is " + profile);
 		}
 		System.err.println();
 		System.err
 				.println("---------------------------------------------------------------------");
 		System.err.println();
 		Rebuilder rebuilder = getRebuilder(serverDir, serverConfig);
 		if (rebuilder != null) {
 			System.err.println();
 			System.err.println(rebuilder.getAction());
 			System.err.println();
 			Map options = getOptions(rebuilder.init(serverDir, serverConfig));
 			boolean serverIsRunning = ServerUtility.pingServer("http", null, null);
 			if (serverIsRunning && rebuilder.shouldStopServer()) {
                 throw new Exception("The Fedora server appears to be running."
                         + "  It must be stopped before the rebuilder can run.");
 			}
 			if (options != null) {
 				System.err.println();
 				System.err.println("Rebuilding...");
 				try {
 					rebuilder.start(options);
 					// fedora.server.storage.lowlevel.Configuration conf =
 					// fedora.server.storage.lowlevel.Configuration.getInstance();
 					// String objStoreBaseStr = conf.getObjectStoreBase();
 					String role = "fedora.server.storage.lowlevel.ILowlevelStorage";
 					ModuleConfiguration mcfg = serverConfig
 							.getModuleConfiguration(role);
 					Iterator parameters = mcfg.getParameters().iterator();
 					Map config = new HashMap();
 					while (parameters.hasNext()) {
 						Parameter p = (Parameter) parameters.next();
 						config.put(p.getName(), p.getValue(p.getIsFilePath()));
 					}
 					getFilesystem(config);
 
 					Parameter param = mcfg
 							.getParameter(DefaultLowlevelStorage.OBJECT_STORE_BASE);
 					String objStoreBaseStr = param.getValue(param
 							.getIsFilePath());
 					File dir = new File(objStoreBaseStr);
 					rebuildFromDirectory(rebuilder, dir, "FedoraBDefObject");
 					rebuildFromDirectory(rebuilder, dir, "FedoraBMechObject");
 					rebuildFromDirectory(rebuilder, dir, "FedoraObject");
 				} finally {
 					rebuilder.finish();
 				}
 				System.err.println("Finished.");
 				System.err.println();
 			}
 		}
 	}
 
 	private void getFilesystem(Map configuration) {
 		String filesystemClassName = (String) configuration
 				.get(DefaultLowlevelStorage.FILESYSTEM);
 		Object[] parameters = new Object[] { configuration };
 		Class[] parameterTypes = new Class[] { Map.class };
 		ClassLoader loader = getClass().getClassLoader();
 		Class cclass;
 		Constructor constructor;
 
 		try {
 			cclass = loader.loadClass(filesystemClassName);
 			constructor = cclass.getConstructor(parameterTypes);
 			fs = (FileSystem) constructor.newInstance(parameters);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Recurse directories in reverse order (latest time first) looking for
 	 * files that contain searchString, and call rebuilder.addObject on them.
 	 */
 	private void rebuildFromDirectory(Rebuilder rebuilder, File dir,
 			String searchString) throws Exception {
 		String[] filenames = fs.list(dir);
 		if (filenames == null) {
 			return;
 		}
 		Arrays.sort(filenames);
 		for (int i = 0; i < filenames.length; i++) {
 			File f = new File(dir.getAbsolutePath() + File.separator
 					+ filenames[i]);
 			if (fs.isDirectory(f)) {
 				rebuildFromDirectory(rebuilder, f, searchString);
 			} else {
 				BufferedReader reader = null;
 				InputStream in;
 				try {
 					in = null;
 					reader = new BufferedReader(new InputStreamReader(fs
 							.read(f)));
 					String line = reader.readLine();
 					while (line != null) {
 						if (line.indexOf(searchString) != -1) {
 							in = fs.read(f);
 							line = null;
 						} else {
 							line = reader.readLine();
 						}
 					}
 					if (in != null) {
 						try {
 							System.out.println(f.getAbsoluteFile());
 							DigitalObject obj = new BasicDigitalObject();
 							DODeserializer deser = new FOXMLDODeserializer();
 							deser
 									.deserialize(
 											in,
 											obj,
 											"UTF-8",
 											DOTranslationUtility.SERIALIZE_STORAGE_INTERNAL);
 						    rebuilder.addObject(obj);
 						} catch (Exception e) {
                             System.out.println("WARNING: Skipped " + f.getAbsoluteFile() + " due to following exception:");
                             e.printStackTrace();
 						} finally {
 							try {
 								in.close();
 							} catch (Exception e) {
 							}
 						}
 					}
 				} finally {
 					if (reader != null)
 						try {
 							reader.close();
 						} catch (Exception e) {
 						}
 				}
 			}
 		}
 	}
 
 	private Map getOptions(Map descs) throws IOException {
 		Map options = new HashMap();
 		Iterator iter = descs.keySet().iterator();
 		while (iter.hasNext()) {
 			String name = (String) iter.next();
 			String desc = (String) descs.get(name);
 			options.put(name, getOptionValue(name, desc));
 		}
 		int c = getChoice("Start rebuilding with the above options?",
 				new String[] { "Yes", "No, let me re-enter the options.",
 						"No, exit." });
 		if (c == 0)
 			return options;
 		if (c == 1) {
 			System.err.println();
 			return getOptions(descs);
 		}
 		return null;
 	}
 
 	private String getOptionValue(String name, String desc) throws IOException {
 		System.err.println("[" + name + "]");
 		System.err.println(desc);
 		System.err.println();
 		System.err.print("Enter a value --> ");
 		String val = new BufferedReader(new InputStreamReader(System.in))
 				.readLine();
 		System.err.println();
 		return val;
 	}
 
 	private Rebuilder getRebuilder(File serverDir,
 			ServerConfiguration serverConfig) throws Exception {
 		String[] labels = new String[REBUILDERS.length + 1];
 		Rebuilder[] rebuilders = new Rebuilder[REBUILDERS.length];
 		int i = 0;
 		for (i = 0; i < REBUILDERS.length; i++) {
 			Rebuilder r = (Rebuilder) Class.forName(REBUILDERS[i])
 					.newInstance();
 			labels[i] = r.getAction();
 			rebuilders[i] = r;
 		}
 		labels[i] = "Exit";
 		int choiceNum = getChoice("What do you want to do?", labels);
 		if (choiceNum == i) {
 			return null;
 		} else {
 			return rebuilders[choiceNum];
 		}
 	}
 
 	private int getChoice(String title, String[] labels) throws IOException {
 		boolean validChoice = false;
 		int choiceIndex = -1;
 		System.err.println(title);
 		System.err.println();
 		for (int i = 1; i <= labels.length; i++) {
 			System.err.println("  " + i + ") " + labels[i - 1]);
 		}
 		System.err.println();
 		while (!validChoice) {
 			System.err.print("Enter (1-" + labels.length + ") --> ");
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					System.in));
 			String line = in.readLine();
 			try {
 				int choiceNum = Integer.parseInt(line);
 				if (choiceNum > 0 && choiceNum <= labels.length) {
 					choiceIndex = choiceNum - 1;
 					validChoice = true;
 				}
 			} catch (NumberFormatException nfe) {
 			}
 		}
 		return choiceIndex;
 	}
 
 	private static ServerConfiguration getServerConfig(File serverDir,
 			String profile) throws IOException {
 		ServerConfigurationParser parser = new ServerConfigurationParser(
 				new FileInputStream(new File(serverDir, "config/fedora.fcfg")));
 		ServerConfiguration serverConfig = parser.parse();
 		// set all the values according to the profile, if specified
 		if (profile != null) {
 			int c = setValuesForProfile(serverConfig, profile);
 			c += setValuesForProfile(serverConfig.getModuleConfigurations(),
 					profile);
 			c += setValuesForProfile(serverConfig.getDatastoreConfigurations(),
 					profile);
 			if (c == 0) {
 				throw new IOException("Unrecognized server-profile: " + profile);
 			}
 		}
 		return serverConfig;
 	}
 
 	private static int setValuesForProfile(Configuration config, String profile) {
 		int c = 0;
 		Iterator iter = config.getParameters().iterator();
 		while (iter.hasNext()) {
 			Parameter param = (Parameter) iter.next();
 			String profileValue = (String) param.getProfileValues()
 					.get(profile);
 			if (profileValue != null) {
 				param.setValue(profileValue);
 				c++;
 			}
 		}
 		return c;
 	}
 
 	private static int setValuesForProfile(List configs, String profile) {
 		Iterator iter = configs.iterator();
 		int c = 0;
 		while (iter.hasNext()) {
 			c += setValuesForProfile((Configuration) iter.next(), profile);
 		}
 		return c;
 	}
 
 	public static void fail(String message, boolean showUsage, boolean exit) {
 		System.err.println("Error: " + message);
 		System.err.println();
 		if (showUsage) {
 			System.err.println("Usage: fedora-rebuild [server-profile]");
 			System.err.println();
 			System.err
 					.println("server-profile : the argument you start Fedora with, such as 'mckoi'");
 			System.err
 					.println("                 or 'oracle'.  If you start fedora with 'fedora-start'");
 			System.err
 					.println("                 (without arguments), don't specify a server-profile here either.");
 			System.err.println();
 		}
 		if (exit) {
 			System.exit(1);
 		}
 	}
 
 	public static void main(String[] args) {
 		// tell commons-logging to use log4j
 		System.setProperty("org.apache.commons.logging.LogFactory",
 				"org.apache.commons.logging.impl.Log4jFactory");
 		System.setProperty("org.apache.commons.logging.Log",
 				"org.apache.commons.logging.impl.Log4JLogger");
 		// log4j
 		// File log4jConfig = new File(new File(homeDir), "config/log4j.xml");
 		// DOMConfigurator.configure(log4jConfig.getPath());
 		String profile = null;
 		if (args.length == 1) {
 			profile = args[0];
 		}
 		if (args.length > 1) {
 			fail("Too many arguments", true, true);
 		}
 		try {
 			File serverDir = new File(new File(Constants.FEDORA_HOME), "server");
 			if (args.length > 0)
 				profile = args[0];
 			new Rebuild(serverDir, profile);
 		} catch (Throwable th) {
 			String msg = th.getMessage();
 			if (msg == null)
 				msg = th.getClass().getName();
 			fail(msg, false, false);
 			th.printStackTrace();
 		}
 	}
 
 }
