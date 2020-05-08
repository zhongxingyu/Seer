 /*
  * RapidEnv: RapidEnvInterpreter.java
  *
  * Copyright (C) 2010 Martin Bluemel
  *
  * Creation Date: 05/25/2010
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation;
  * either version 3 of the License, or (at your option) any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copies of the GNU Lesser General Public License and the
  * GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package org.rapidbeans.rapidenv;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.rapidbeans.core.exception.RapidBeansRuntimeException;
 import org.rapidbeans.core.exception.ValidationInstanceAssocTwiceException;
 import org.rapidbeans.core.type.TypeRapidBean;
 import org.rapidbeans.core.util.EscapeMap;
 import org.rapidbeans.core.util.FileHelper;
 import org.rapidbeans.core.util.LinuxDesktop;
 import org.rapidbeans.core.util.ManifestReader;
 import org.rapidbeans.core.util.OperatingSystemFamily;
 import org.rapidbeans.core.util.PlatformHelper;
 import org.rapidbeans.core.util.StringHelper;
 import org.rapidbeans.core.util.StringHelper.FillMode;
 import org.rapidbeans.datasource.Document;
 import org.rapidbeans.rapidenv.cmd.CmdLineInteractions;
 import org.rapidbeans.rapidenv.cmd.CmdRenv;
 import org.rapidbeans.rapidenv.config.Environment;
 import org.rapidbeans.rapidenv.config.InstallControl;
 import org.rapidbeans.rapidenv.config.Installunit;
 import org.rapidbeans.rapidenv.config.Project;
 import org.rapidbeans.rapidenv.config.Property;
 import org.rapidbeans.rapidenv.config.PropertyExtension;
 import org.rapidbeans.rapidenv.config.PropertyExtensionFromInstallUnit;
 import org.rapidbeans.rapidenv.config.PropertyInterpretedString;
 import org.rapidbeans.rapidenv.config.RapidEnvConfigurationException;
 import org.rapidbeans.rapidenv.config.cmd.Argument;
 import org.rapidbeans.rapidenv.config.cmd.CommandExecutionResult;
 import org.rapidbeans.rapidenv.config.cmd.ExceptionMap;
 import org.rapidbeans.rapidenv.config.cmd.SystemCommand;
 import org.rapidbeans.rapidenv.security.Hashalgorithm;
 import org.rapidbeans.rapidenv.security.Verifyer;
 
 /**
  * The central interpreter for RapidEnv commands.
  * 
  * @author Martin Bluemel
  */
 public class RapidEnvInterpreter {
 
 	private static RapidEnvInterpreter singleInstance = null;
 
 	public static RapidEnvInterpreter getInstance() {
 		return singleInstance;
 	}
 
 	/**
 	 * Attention: use for test purposes only.
 	 */
 	public static void clearInstance() {
 		singleInstance = null;
 	}
 
 	private final Properties properties = new Properties();
 
 	/**
 	 * @param name
 	 *            the property name
 	 * 
 	 * @return the property value or null if the property is not defined
 	 */
 	public String getPropertyValue(final String name) {
 		return this.properties.getProperty(name);
 	}
 
 	/**
 	 * For test reasons only.
 	 * 
 	 * @param name
 	 *            the property name
 	 * @param value
 	 *            the new value
 	 */
 	public void setPropertyValue(final String name, final String value) {
 		this.properties.setProperty(name, value);
 	}
 
 	/**
 	 * @param name
 	 *            the property name
 	 * 
 	 *            Remove the property value with the given name.
 	 */
 	public void removeProperty(final String name) {
 		this.properties.remove(name);
 	}
 
 	private Properties propertiesPersisted = new Properties();
 
 	/**
 	 * @param name
 	 *            the property name
 	 * @return the property value or null if the property is not defined
 	 */
 	public String getPropertyValuePersisted(final String name) {
 		return this.propertiesPersisted.getProperty(name);
 	}
 
 	private Logger logger = Logger.getLogger(RapidEnvInterpreter.class.getName());
 
 	/**
 	 * Caution. Use only for testing reasons.
 	 * 
 	 * @param logger
 	 *            the logger to set
 	 */
 	protected void setLogger(final Logger logger) {
 		this.logger = logger;
 	}
 
 	/**
 	 * Convenience to get the current log level
 	 * 
 	 * @return the global logger's level.
 	 */
 	public Level getLogLevel() {
 		return logger.getLevel();
 	}
 
 	/**
 	 * Log a message using the given log level.
 	 * 
 	 * @param level
 	 *            the log level to use
 	 * @param msg
 	 *            the message to log
 	 */
 	public static void log(final Level level, final String msg) {
 		if (singleInstance != null) {
 			singleInstance.logger.log(level, msg);
 		}
 	}
 
 	// main command class reference
 	private CmdRenv renvCommand = null;
 
 	// the command to execute
 	private CmdRenvCommand command = null;
 
 	public CmdRenvCommand getCommand() {
 		return command;
 	}
 
 	private Map<CmdRenvOption, String[]> options = null;
 
 	private Document configDoc = null;
 
 	/**
 	 * 'command' 'gui' or 'test'
 	 */
 	private RunMode runMode = null;
 
 	/**
 	 * @return the runMode 'command' 'gui' or 'test'
 	 */
 	public RunMode getRunMode() {
 		return runMode;
 	}
 
 	private InputStream in = null;
 
 	/**
 	 * @return the in stream
 	 */
 	public InputStream getIn() {
 		return this.in;
 	}
 
 	private PrintStream out = null;
 
 	/**
 	 * @return the out stream
 	 */
 	public PrintStream getOut() {
 		return this.out;
 	}
 
 	private PrintStream err = null;
 
 	/**
 	 * @return the out stream
 	 */
 	public PrintStream getErr() {
 		return this.err;
 	}
 
 	/**
 	 * Set the output stream for test reasons.
 	 * 
 	 * @param out
 	 *            the new output stream
 	 */
 	public void setOut(final PrintStream out) {
 		this.out = out;
 	}
 
 	/**
 	 * The subset of install units to process during the interpreter execution.
 	 * Serves also for ordering these install units.
 	 */
 	private List<Installunit> installUnitsToProcess = null;
 
 	/**
 	 * Indicates if install units or properties were explicitly given as command
 	 * arguments or not.
 	 */
 	private boolean installUnitOrPropertyNamesExplicitelySpecified = false;
 
 	/**
 	 * @return the installUnitOrPropertyNamesExplicitelySpecified
 	 */
 	public boolean getInstallUnitOrPropertyNamesExplicitelySpecified() {
 		return this.installUnitOrPropertyNamesExplicitelySpecified;
 	}
 
 	/**
 	 * @return the install units to process
 	 */
 	public List<Installunit> getInstallunitsToProcess() {
 		return installUnitsToProcess;
 	}
 
 	/**
 	 * The subset of properties to process during the interpreter execution.
 	 */
 	private List<Property> propertiesToProcess = null;
 
 	/**
 	 * @return the properties to process
 	 */
 	public List<Property> getPropertiesToProcess() {
 		return propertiesToProcess;
 	}
 
 	/**
 	 * @return the RapidEnv configuration root
 	 */
 	public Project getProject() {
 		return (Project) this.configDoc.getRoot();
 	}
 
 	private AntGateway ant = null;
 
 	/**
 	 * @return the interperer's Ant gateway
 	 */
 	public AntGateway getAnt() {
 		return ant;
 	}
 
 	/**
 	 * Determines the Linux desktop currently used.
 	 * 
 	 * @return the Linux desktop currently used
 	 */
 	public static LinuxDesktop getLinuxDesktop() {
 		if (PlatformHelper.getOsfamily() != OperatingSystemFamily.linux) {
 			throw new AssertionError("Unexpected Operating system (family) " + PlatformHelper.getOsName() + "\"");
 		}
 		LinuxDesktop desktop = LinuxDesktop.kde;
 		if (System.getenv("WINDOWMANAGER").endsWith("kde")) {
 			desktop = LinuxDesktop.kde;
 		} else if (System.getenv("WINDOWMANAGER").endsWith("gnome")) {
 			desktop = LinuxDesktop.gnome;
 		}
 		return desktop;
 	}
 
 	/**
 	 * Execute RapidEnv command. Dispatch to private handler.
 	 */
 	public void execute() {
 		execute(System.in, System.out, System.err);
 	}
 
 	/**
 	 * Execute RapidEnv command. Dispatch to private handler.
 	 */
 	public void execute(final InputStream inStream, final PrintStream outStream) {
 		execute(inStream, outStream, System.err);
 	}
 
 	/**
 	 * Execute RapidEnv command. Dispatch to private handler.
 	 */
 	public void execute(final InputStream inStream, final PrintStream outStream, final PrintStream errStream) {
 
 		this.timeStart = System.currentTimeMillis();
 
 		try {
 			this.in = inStream;
 			this.out = outStream;
 			this.err = errStream;
 
 			log(Level.FINE, "executing command \"" + this.command.name() + "\"...");
 
 			boolean deinstallAll = false;
 
 			// initialize command execution
 			switch (this.command) {
 			case boot:
 				out.println("booting RapidEnv development environment...");
 				if (getProject().atLeastOnePersonalProperty()) {
 					out.println("\nreading personal properties:\n");
 				}
 				readProfile();
 				initProperties(this.command);
 			break;
 			case help:
 			case version:
 			case hashvalue:
 			// do nothing
 			break;
 			default:
 				out.println("\nRapidEnv development environment");
 				readProfile();
 				String tag = this.propertiesPersisted.getProperty("rapidbeans.tag");
 				if (tag == null) {
 					tag = getProject().getTag();
 				}
 				out.println("  Project: " + getProject().getName() + ", Tag: " + tag);
 				initPropertiesAndInstallunitsToProcess(this.command);
 				initProperties(this.command);
 			break;
 			}
 
 			// dispatch
 			switch (this.command) {
 			case boot:
 				execBoot();
 			break;
 			case stat:
 				execStat();
 			break;
 			case install:
 				execInstall();
 			break;
 			case deinstall:
 				deinstallAll = execDeinstall();
 			break;
 			case update:
 				execUpdate();
 			break;
 			case config:
 				execConfig();
 			break;
 			case help:
 				execHelp();
 			break;
 			case version:
 				execVersion();
 			break;
 			case hashvalue:
 				execHashvalue();
 			break;
 			default:
 				throw new RapidEnvCmdException("command \"" + this.command + "\" is not yest supported");
 			}
 
 			// finish command execution
 			switch (this.command) {
 			case boot:
 				writeProfile();
 			break;
 			case help:
 			case hashvalue:
 			case version:
 			break;
 			default:
 				if (profileChanged() && !deinstallAll) {
 					writeProfile();
 				}
 			break;
 			}
 		} finally {
 			this.timeEnd = System.currentTimeMillis();
 		}
 	}
 
 	private void execHashvalue() {
 		try {
 			final Hashalgorithm hashalg = Hashalgorithm
 			        .valueOf(this.renvCommand.getInstallunitOrPropertyNames().get(0));
 			final File file = new File(this.renvCommand.getInstallunitOrPropertyNames().get(1));
 			this.out.println(Verifyer.hashValue(file, hashalg));
 		} catch (java.lang.IllegalArgumentException e) {
 			final String messagePattern = "No enum const class org.rapidbeans.rapidenv.security.Hashalgorithm.";
 			if (e.getMessage().startsWith(messagePattern)) {
 				final StringBuilder halgList = new StringBuilder();
 				for (final Hashalgorithm halg : Hashalgorithm.values()) {
 					if (halgList.length() > 0) {
 						halgList.append("\", \"");
 					} else {
 						halgList.append("\"");
 					}
 					halgList.append(halg.name());
 				}
 				halgList.append("\"");
 				throw new RapidEnvException("Invalid hashalgorithm \""
 				        + e.getMessage().substring(messagePattern.length()) + "\"\n  Please specifiy one of {"
 				        + halgList.toString() + "}.", ExceptionMap.ERRORCODE_HASH_INVALID_ALGORITHM);
 			} else {
 				throw e;
 			}
 		}
 	}
 
 	/**
 	 * Execute the boot command.
 	 */
 	private void execBoot() {
 		boolean create = false;
 		switch (PlatformHelper.getOsfamily()) {
 		case windows:
 			create = CmdLineInteractions.promptYesNo(this.in, this.out,
 			        "\nDo you want to create a \"Command Prompt Here\" menu entry\n"
 			                + "  in Windows Explorer for this development environment?", true);
 		break;
 		case linux:
 			switch (RapidEnvInterpreter.getLinuxDesktop()) {
 			case kde:
 				create = CmdLineInteractions.promptYesNo(this.in, this.out,
 				        "\nDo you want to create an \"Open Terminal\" action\n"
 				                + "  for KDE's Dolphin file manager for this development environment?", true);
 			break;
 			case gnome:
 				create = CmdLineInteractions.promptYesNo(this.in, this.out,
 				        "\nDo you want to create a \"Open Terminal\" script\n"
 				                + "  for Gnome's Nautilus file manager for this development environment?", true);
 			break;
 			default:
 				log(Level.FINE, "No service menu entry to delete for Linux desktop \""
 				        + RapidEnvInterpreter.getLinuxDesktop().name());
 			}
 		break;
 		default:
 			throw new RapidEnvException("Operating systm \"" + PlatformHelper.getOsfamily().name()
 			        + "\" not yet supported");
 		}
 		if (create) {
 			switch (PlatformHelper.getOsfamily()) {
 			case windows:
 				createExplorerMenuEntry();
 			break;
 			case linux:
 				switch (RapidEnvInterpreter.getLinuxDesktop()) {
 				case kde:
 					createKdeServiceMenuEntry();
 				break;
 				case gnome:
 					createGnomeServiceMenuEntry();
 				break;
 				default:
 					log(Level.FINE, "No service menu entry to delete for Linux desktop \""
 					        + RapidEnvInterpreter.getLinuxDesktop().name());
 				}
 			break;
 			default:
 				throw new RapidEnvException("Operating systm \"" + PlatformHelper.getOsfamily().name()
 				        + "\" not yet supported");
 			}
 		}
 	}
 
 	private void createExplorerMenuEntry() {
 		final File tmpfile = new File("cmdMenuEntry.reg");
 		OutputStreamWriter osw = null;
 		try {
 			final String lf = PlatformHelper.getLineFeed();
 			osw = new OutputStreamWriter(new FileOutputStream(tmpfile));
 			final String rapidEnvHome = System.getenv("RAPID_ENV_HOME");
 			osw.write("Windows Registry Editor Version 5.00" + lf + lf
 			        + "[HKEY_CLASSES_ROOT\\Directory\\shell\\RapidEnv_" + getProject().getName() + "_"
 			        + getProject().getTag() + "]" + lf + "@=\"" + getProject().getName() + " Command Prompt "
 			        + getProject().getTag() + "\"" + lf + lf + "[HKEY_CLASSES_ROOT\\Directory\\shell\\" + "RapidEnv_"
 			        + getProject().getName() + "_" + getProject().getTag() + "\\command]" + lf + "@=\"cmd.exe /K"
 			        + " title " + getProject().getName() + " " + getProject().getTag() + " Command Prompt"
 			        + " & cd /D \\\"%L\\\"" + " & call \\\"" + rapidEnvHome.replace("\\", "\\\\").replace("\"", "\\\"")
 			        + "\\\\bin\\\\renv\\\"" + "\"" + lf);
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			if (osw != null) {
 				try {
 					osw.close();
 				} catch (IOException e) {
 					throw new RapidEnvException(e);
 				}
 			}
 		}
 		out.println("Creating explorer menu entry...");
 		final SystemCommand command = new SystemCommand();
 		command.setExecutable(System.getenv("SystemRoot") + File.separator + "regedit.exe");
 		final Argument arg1 = new Argument();
 		arg1.setValue("/s");
 		command.addArgument(arg1);
 		final Argument arg2 = new Argument();
 		arg2.setValue(tmpfile.getAbsolutePath());
 		command.addArgument(arg2);
 		CommandExecutionResult result = command.execute();
 		if (result.getReturncode() != 0) {
 			throw new RapidEnvException("Problem while creating explorer menu entry:" + " returncode = "
 			        + result.getReturncode());
 		}
 		// do not delete to leave a chance to execute in hindsight
 		// tmpfile.delete();
 	}
 
 	private void deleteExplorerMenuEntry() {
 		final File tmpfile = new File("newCmdMenuEntry.reg");
 		OutputStreamWriter osw = null;
 		try {
 			final String lf = PlatformHelper.getLineFeed();
 			osw = new OutputStreamWriter(new FileOutputStream(tmpfile));
 			osw.write("Windows Registry Editor Version 5.00" + lf + lf
 			        + "[-HKEY_CLASSES_ROOT\\Directory\\shell\\RapidEnv_" + getProject().getName() + "_"
 			        + getProject().getTag() + "]" + lf);
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			if (osw != null) {
 				try {
 					osw.close();
 				} catch (IOException e) {
 					throw new RapidEnvException(e);
 				}
 			}
 		}
 		out.println("Deleting explorer menu entry...");
 		final SystemCommand command = new SystemCommand();
 		command.setExecutable(System.getenv("SystemRoot") + File.separator + "regedit.exe");
 		final Argument arg1 = new Argument();
 		arg1.setValue("/s");
 		command.addArgument(arg1);
 		final Argument arg2 = new Argument();
 		arg2.setValue(tmpfile.getAbsolutePath());
 		command.addArgument(arg2);
 		command.execute();
 		CommandExecutionResult result = command.execute();
 		if (result.getReturncode() != 0) {
 			throw new RapidEnvException("Problem while deleting explorer menu entry:" + " returncode = "
 			        + result.getReturncode());
 		}
 		tmpfile.delete();
 	}
 
 	private void createKdeServiceMenuEntry() {
 		// On Linux KDE
 		createLinuxProfile();
 
 		out.println("Creating KDE \"Open Terminal\" service menu entry...");
 		final File menuEntry = new File(PlatformHelper.userhome() + File.separator
 		        + "/.kde4/share/kde4/services/ServiceMenus/konsolehere_" + getProject().getName() + "_"
 		        + getProject().getTag() + ".desktop");
 		if (!menuEntry.getParentFile().exists()) {
 			FileHelper.mkdirs(menuEntry.getParentFile());
 		}
 		OutputStreamWriter osw2 = null;
 		try {
 			final String lf = PlatformHelper.getLineFeed();
 			osw2 = new OutputStreamWriter(new FileOutputStream(menuEntry));
 			osw2.write("[Desktop Entry]" + lf + "X-SuSE-translate=true" + lf + "Type=Service" + lf
 			        + "X-KDE-ServiceTypes=KonqPopupMenu/Plugin,inode/directory" + lf + "Actions=openTerminalHere;" + lf
 			        + "X-KDE-AuthorizeAction=shell_access" + lf + lf + "[Desktop Action openTerminalHere]" + lf
 			        + "Name=Open Terminal " + getProject().getName() + " " + getProject().getTag() + lf
 			        + "Icon=utilities-terminal" + lf
 			        + "Exec=konsole --workdir \"%f\" -e /bin/bash --rcfile ~/.profile_" + getProject().getName() + "_"
 			        + getProject().getTag() + lf);
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			if (osw2 != null) {
 				try {
 					osw2.close();
 				} catch (IOException e) {
 					throw new RapidEnvException(e);
 				}
 			}
 		}
 		if (!menuEntry.setExecutable(true)) {
 			throw new RapidEnvException("Problems to make file \"" + menuEntry.getAbsolutePath() + "\" executeable.");
 		}
 	}
 
 	private void createGnomeServiceMenuEntry() {
 		// On Linux Gnome
 		createLinuxProfile();
 
 		out.println("Creating Gnome Nautilus \"Open Terminal\" script...");
 		final File menuEntry = new File(PlatformHelper.userhome() + File.separator
 		        + "/.gnome2/nautilus-scripts/Open Terminal " + getProject().getName() + " " + getProject().getTag());
 		if (!menuEntry.getParentFile().exists()) {
 			FileHelper.mkdirs(menuEntry.getParentFile());
 		}
 		OutputStreamWriter osw2 = null;
 		try {
 			final String lf = PlatformHelper.getLineFeed();
 			osw2 = new OutputStreamWriter(new FileOutputStream(menuEntry));
 			osw2.write("#!/bin/bash" + lf
 			        + "root=\"$(echo ${NAUTILUS_SCRIPT_CURRENT_URI} | cut -d'/' -f3- | sed 's/%20/ /g')\"" + lf
 			        + "if [ -z \"${NAUTILUS_SCRIPT_SELECTED_FILE_PATHS}\" ]; then" + lf + "  folder=\"${root}\"" + lf
 			        + "else" + lf + "  while [ ! -z \"$1\" -a ! -d \"${root}/$1\" ]; do" + lf + "    shift;" + lf
 			        + "  done" + lf + "  folder=\"${root}/$1\"" + lf + "fi" + lf
 			        + "gnome-terminal --working-directory=\"${folder}\""
 			        + " --command \"/bin/bash --rcfile ~/.profile_" + getProject().getName() + "_"
 			        + getProject().getTag() + "\"" + lf);
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			if (osw2 != null) {
 				try {
 					osw2.close();
 				} catch (IOException e) {
 					throw new RapidEnvException(e);
 				}
 			}
 		}
 		if (!menuEntry.setExecutable(true)) {
 			throw new RapidEnvException("Problems to make file \"" + menuEntry.getAbsolutePath() + "\" executeable.");
 		}
 	}
 
 	private void createLinuxProfile() {
 		// FILE ~/.profile_RapidBeans_main:
 		// -----------------------------------------------------------------------------
 		// . /home/martin/Projects/RapidBeans/env/profile/renv_<user>_<host>.sh
 		// . renv
 		// -----------------------------------------------------------------------------
 		out.println("Creating profile for " + getProject().getName() + " " + getProject().getTag() + " " + "...");
 		final File profile = new File(PlatformHelper.userhome() + File.separator + ".profile_" + getProject().getName()
 		        + "_" + getProject().getTag());
 		OutputStreamWriter osw1 = null;
 		try {
 			final String rapidEnvHome = System.getenv("RAPID_ENV_HOME");
 			final String lf = PlatformHelper.getLineFeed();
 			osw1 = new OutputStreamWriter(new FileOutputStream(profile));
 			osw1.write(". " + rapidEnvHome + "/profile/renv_" + PlatformHelper.username() + "_"
 			        + PlatformHelper.hostname() + ".sh" + lf + ". renv" + lf);
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			if (osw1 != null) {
 				try {
 					osw1.close();
 				} catch (IOException e) {
 					throw new RapidEnvException(e);
 				}
 			}
 		}
 	}
 
 	private void deleteKdeServiceMenuEntry() {
 		out.println("Deleting KDE \"Open Terminal\" service menu entry...");
 		final File menuEntry = new File(PlatformHelper.userhome() + File.separator
 		        + "/.kde4/share/kde4/services/ServiceMenus/konsolehere_" + getProject().getName() + "_"
 		        + getProject().getTag() + ".desktop");
 		if (menuEntry.exists()) {
 			if (!menuEntry.delete()) {
 				log(Level.WARNING, "Could not create service menu entry \"" + menuEntry.getAbsolutePath() + "\"!");
 			}
 		}
 		deleteLinuxProfile();
 	}
 
 	private void deleteGnomeServiceMenuEntry() {
 		out.println("Deleting Gnome Nautilus \"Open Terminal\" script...");
 		final File menuEntry = new File(PlatformHelper.userhome() + File.separator
 		        + "/.gnome2/nautilus-scripts/Open Terminal " + getProject().getName() + " " + getProject().getTag());
 		if (menuEntry.exists()) {
 			if (!menuEntry.delete()) {
 				log(Level.WARNING, "Could not delete script file \"" + menuEntry.getAbsolutePath() + "\"!");
 			}
 		}
 		deleteLinuxProfile();
 	}
 
 	private void deleteLinuxProfile() {
 		out.println("Deleting Gnome Nautilus \"Open Terminal\" script...");
 		final File profile = new File(PlatformHelper.userhome() + File.separator + ".profile_" + getProject().getName()
 		        + "_" + getProject().getTag());
 		if (profile.exists()) {
 			if (!profile.delete()) {
 				log(Level.WARNING, "Could not delete profile \"" + profile.getAbsolutePath() + "\"!");
 			}
 		}
 	}
 
 	/**
 	 * Execute the status command.
 	 */
 	private void execStat() {
 		if (getPropertiesToProcess().size() > 0) {
 			this.out.println("\nProperties:");
 		}
 		for (final Property property : getPropertiesToProcess()) {
 			property.stat();
 		}
 		if (getInstallunitsToProcess().size() > 0) {
 			out.println("\nInstall units:");
 		}
 		for (final Installunit unit : getInstallunitsToProcess()) {
 			unit.stat();
 		}
 	}
 
 	/**
 	 * Execute the install command.
 	 */
 	private void execInstall() {
 		if (getProject().getInstallunits() == null) {
 			return;
 		}
 		if (this.installUnitOrPropertyNamesExplicitelySpecified) {
 			for (final Installunit unit : getInstallunitsToProcess()) {
 				if (getInstallationStatus(unit, CmdRenvCommand.install) == InstallStatus.notinstalled) {
 					boolean install = true;
 					if (unit.getInstallcontrol() == InstallControl.discontinued) {
 						switch (this.runMode) {
 						case command:
 							install = CmdLineInteractions.promptYesNo(this.in, this.out,
 							        "Installation unit \"" + unit.getFullyQualifiedName()
 							                + "\" is discontinued.\n  Do you want to install anyway?", false);
 						break;
 						default:
 							throw new AssertionError("Run mode \"" + this.runMode.name() + "\" not supported");
 						}
 					}
 					if (install) {
 						unit.install(this.renvCommand.getInstallunitOrPropertyNames());
 					} else {
 						out.println("  Installation of unit \"" + unit.getFullyQualifiedName() + "\" aborted.");
 					}
 				} else {
 					out.println(" installation unit " + unit.getFullyQualifiedName() + " is already installed");
 				}
 			}
 		} else {
 			int installedUnitsCount = 0;
 			for (final Installunit unit : getInstallunitsToProcess()) {
 				if (getInstallationStatus(unit, CmdRenvCommand.install) == InstallStatus.notinstalled
 				        && unit.shouldBeInstalled()) {
 					unit.install(this.renvCommand.getInstallunitOrPropertyNames());
 					installedUnitsCount++;
 				} else {
 					unit.stat();
 				}
 			}
 			if (installedUnitsCount == 0) {
 				out.println("Nothing to install");
 			} else {
 				out.println("Installation finished successfully");
 			}
 		}
 	}
 
 	private Map<String, Map<CmdRenvCommand, InstallStatus>> installStatusMap = new HashMap<String, Map<CmdRenvCommand, InstallStatus>>();
 
 	/**
 	 * Centralized install status determination equipped with a result cache
 	 * since this determination is used very often and is very expensive.
 	 * 
 	 * @param installunit
 	 *            the installation unit to investigate.
 	 * @return the installation status of the given installation unit
 	 */
 	public InstallStatus getInstallationStatus(Installunit installunit) {
 		return getInstallationStatus(installunit, getCommand());
 	}
 
 	/**
 	 * Centralized install status determination equipped with a result cache
 	 * since this determination is used very often and is very expensive.
 	 * 
 	 * @param installunit
 	 *            the installation unit to investigate.
 	 * @param cmd
 	 *            the RapidEnv command that is currently executed
 	 * @return the installation status of the given installation unit
 	 */
 	public InstallStatus getInstallationStatus(Installunit installunit, CmdRenvCommand cmd) {
 		InstallStatus status = null;
 		Map<CmdRenvCommand, InstallStatus> commandMap = this.installStatusMap.get(installunit
 		        .getFullyQualifiedName(true));
 		if (commandMap == null) {
 			commandMap = new HashMap<CmdRenvCommand, InstallStatus>();
 			status = installunit.getInstallationStatus(cmd);
 			commandMap.put(cmd, status);
 			this.installStatusMap.put(installunit.getFullyQualifiedName(true), commandMap);
 		} else {
 			status = commandMap.get(cmd);
 			if (status == null) {
 				status = installunit.getInstallationStatus(cmd);
 				commandMap.put(cmd, status);
 			}
 		}
 		return status;
 	}
 
 	/**
 	 * Execute the deinstall command.
 	 */
 	private boolean execDeinstall() {
 		boolean deinstallAll = false;
 		if (this.installUnitOrPropertyNamesExplicitelySpecified) {
 			for (final Installunit unit : getInstallunitsToProcess()) {
 				if (getInstallationStatus(unit, CmdRenvCommand.deinstall) != InstallStatus.notinstalled) {
 					unit.deinstall();
 				} else {
 					out.println(" installation unit " + unit.getFullyQualifiedName() + " is not installed");
 				}
 			}
 		} else {
 			switch (this.runMode) {
 			case command:
 				deinstallAll = CmdLineInteractions.promptYesNo(this.in, this.out,
 				        "Do you really want do deinstall the complete evironment including:\n"
 				                + "- all install units\n" + "- your personal profile\n"
 				                + "- the RapidEnv command prompt?", false);
 			break;
 			default:
 				throw new AssertionError("Run mode \"" + this.runMode.name() + " not yet supported");
 			}
 			if (!deinstallAll) {
 				out.println("Complete deinstall aborted");
 				return false;
 			}
 			for (final Installunit unit : getInstallunitsToProcess()) {
 				if (getInstallationStatus(unit, CmdRenvCommand.deinstall) != InstallStatus.notinstalled) {
 					unit.deinstall();
 				} else {
 					unit.stat();
 				}
 			}
 			deleteProfile();
 			switch (PlatformHelper.getOsfamily()) {
 			case windows:
 				deleteExplorerMenuEntry();
 			break;
 			case linux:
 				this.out.println("Deleting profile for " + getProject().getName() + " " + getProject().getTag() + " "
 				        + "...");
 				final File profile = new File(PlatformHelper.userhome() + File.separator + ".profile_"
 				        + getProject().getName() + "_" + getProject().getTag());
 				if (profile.exists()) {
 					if (!profile.delete()) {
 						log(Level.WARNING, "Could not delete profile \"" + profile.getAbsolutePath() + "\"!");
 					}
 				}
 				switch (RapidEnvInterpreter.getLinuxDesktop()) {
 				case kde:
 					deleteKdeServiceMenuEntry();
 				break;
 				case gnome:
 					deleteGnomeServiceMenuEntry();
 				break;
 				default:
 					log(Level.FINE, "No start menu shell link icon \"" + getProject().getName() + "_"
 					        + getProject().getTag() + ".desktop" + "\" to delete for Linux desktop \""
 					        + RapidEnvInterpreter.getLinuxDesktop().name());
 				}
 			break;
 			default:
 				throw new RapidEnvException("Operating systm \"" + PlatformHelper.getOsfamily().name()
 				        + "\" not yet supported");
 			}
 			out.println("Deinstallation finished successfully");
 		}
 		return deinstallAll;
 	}
 
 	/**
 	 * Execute the update command.
 	 */
 	private void execUpdate() {
 		if (getPropertiesToProcess().size() > 0) {
 			this.out.println("\nProperties:");
 			for (final Property property : getPropertiesToProcess()) {
 				final String propValue = property.update();
 				setPropertyValue(property.getFullyQualifiedName(), propValue);
 			}
 		}
 		if (getInstallunitsToProcess().size() > 0) {
 			out.println("\nInstall units:");
 			int updatedUnitsCount = 0;
 			for (final Installunit unit : getInstallunitsToProcess()) {
 				switch (getInstallationStatus(unit, CmdRenvCommand.update)) {
 				case notinstalled:
 					if (!(unit.getInstallcontrol() == InstallControl.optional || unit.getInstallcontrol() == InstallControl.discontinued)) {
 						unit.install(this.renvCommand.getInstallunitOrPropertyNames());
 						updatedUnitsCount++;
 					} else if (!this.installUnitOrPropertyNamesExplicitelySpecified) {
 						unit.stat();
 					}
 				break;
 				case deinstallrequired:
 					unit.deinstall();
 				break;
 				case upgraderequired:
 				case downgraderequired:
 					if (unit.getInstallcontrol() == InstallControl.discontinued) {
 						unit.deinstall();
 					} else {
 						unit.updowngrade();
 						updatedUnitsCount++;
 					}
 				break;
 				case configurationrequired:
 					if (unit.getInstallcontrol() == InstallControl.discontinued) {
 						unit.deinstall();
 					} else {
 						unit.configure(true);
 						updatedUnitsCount++;
 					}
 				break;
 				case uptodate:
 					if (unit.getInstallcontrol() == InstallControl.discontinued) {
 						unit.deinstall();
 					} else {
 						if (this.installUnitOrPropertyNamesExplicitelySpecified) {
 							out.println(" installation unit \"" + unit.getFullyQualifiedName()
 							        + "\" is already up to date");
 						} else {
 							unit.stat();
 						}
 					}
 				break;
 				default:
 					throw new AssertionError("Unexpected installation status \""
 					        + getInstallationStatus(unit, CmdRenvCommand.update) + "\" for installation unit \""
 					        + unit.getFullyQualifiedName() + "\"");
 				}
 			}
 			if (!this.installUnitOrPropertyNamesExplicitelySpecified) {
 				if (updatedUnitsCount == 0) {
 					out.println("All installation units are up to date");
 				} else {
 					out.println("Update finished successfully");
 				}
 			}
 		}
 	}
 
 	private void execConfig() {
 		if (getPropertiesToProcess().size() > 0) {
 			this.out.println("\nProperties:");
 			for (final Property property : getPropertiesToProcess()) {
 				final String propValue = property.update();
 				setPropertyValue(property.getFullyQualifiedName(), propValue);
 			}
 		}
 		if (getInstallunitsToProcess().size() > 0) {
 			out.println("\nInstall units:");
 			int configuredUnitsCount = 0;
 			int iudRequieredUnitsCount = 0;
 			for (final Installunit unit : getInstallunitsToProcess()) {
 				switch (getInstallationStatus(unit, CmdRenvCommand.config)) {
 				case notinstalled:
 					unit.stat();
 					if (!this.installUnitOrPropertyNamesExplicitelySpecified) {
 						if (!(unit.getInstallcontrol() == InstallControl.optional || unit.getInstallcontrol() == InstallControl.discontinued)) {
 							iudRequieredUnitsCount++;
 						}
 					}
 				break;
 				case upgraderequired:
 				case downgraderequired:
 					unit.stat();
 					iudRequieredUnitsCount++;
 				break;
 				case configurationrequired:
 					if (this.installUnitOrPropertyNamesExplicitelySpecified) {
 						unit.configure(true);
 					} else {
 						if (unit.getInstallcontrol() == InstallControl.discontinued) {
 							getOut().println(
 							        " installation unit \"" + unit.getFullyQualifiedName()
 							                + "\" is discontinued and should be uninstalled");
 						} else {
 							unit.configure(true);
 							configuredUnitsCount++;
 						}
 					}
 				break;
 				case uptodate:
 					if (this.installUnitOrPropertyNamesExplicitelySpecified) {
 						out.println(" installation unit \"" + unit.getFullyQualifiedName()
 						        + "\" is already up to date and configured");
 					} else {
 						unit.stat();
 					}
 				break;
 				default:
 					throw new AssertionError("Unexpected installation status \""
 					        + getInstallationStatus(unit, CmdRenvCommand.config) + "\" for installation unit \""
 					        + unit.getFullyQualifiedName() + "\"");
 				}
 			}
 			if (!this.installUnitOrPropertyNamesExplicitelySpecified) {
 				if (configuredUnitsCount == 0 && iudRequieredUnitsCount == 0) {
 					out.println("All installation units are up to date and configured");
 				} else if (configuredUnitsCount == 0 && iudRequieredUnitsCount > 0 || configuredUnitsCount > 0
 				        && iudRequieredUnitsCount > 0) {
 					out.println("Some installation units are not installed or outdated");
 				} else if (configuredUnitsCount > 0 && iudRequieredUnitsCount == 0) {
 					out.println("Configuration finished successfully");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Execute the help command. Print command help.
 	 */
 	private void execHelp() {
 		out.println("\nUsage: renv [-<option> ...] [<command>] [<install unit> ...]");
 		out.println("\nCommands:");
 		for (final CmdRenvCommand command : CmdRenvCommand.values()) {
 			out.println(StringHelper.fillUp(" " + command.name() + ", " + command.getShort1(), 14, ' ',
 			        StringHelper.FillMode.right) + command.getDescription());
 		}
 		out.println("\nOptions:");
 		for (final CmdRenvOption option : CmdRenvOption.values()) {
 			out.println(StringHelper.fillUp(" -" + option.name() + ", -" + option.getShort1(), 14, ' ',
 			        StringHelper.FillMode.right) + option.getDescription());
 		}
 	}
 
 	/**
 	 * Execute the version command.
 	 */
 	private void execVersion() {
 		final Manifest manifest = ManifestReader.readManifestFromJarOfClass(this.getClass());
 		out.println("RapidEnv (c) Martin Bluemel 2011");
 		out.println("version: " + manifest.getMainAttributes().getValue("Implementation-Version"));
 	}
 
 	private boolean profileChanged() {
 		if (getProject().getPropertys() != null) {
 			for (final Property prop : getProject().getPropertys()) {
 				// new property added
 				if (getPropertyValuePersisted(prop.getFullyQualifiedName()) == null) {
 					return true;
 				}
 				// property value changed
 				if (!getPropertyValuePersisted(prop.getFullyQualifiedName()).equals(
 				        getPropertyValue(prop.getFullyQualifiedName()))) {
 					return true;
 				}
 			}
 		}
 		for (final Object key : this.propertiesPersisted.keySet()) {
 			final String propertyName = (String) key;
 			// existing property deleted
 			if (this.getProject().findPropertyConfiguration(propertyName) == null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * get the (default) values by interpreting the values from the environment
 	 * definition file and ask for user definition in case of "personal"
 	 * variables.
 	 * 
 	 * @param cmd
 	 *            the renv command
 	 */
 	protected void initProperties(final CmdRenvCommand cmd) {
 
 		switch (getRunMode()) {
 		case command:
 		case batch:
 			if (getProject().getPropertys() != null) {
 				for (final Property propCfg : getProject().getPropertys()) {
 					initProperty(cmd, propCfg);
 				}
 				// for (final Installunit unit : getProject().getInstallunits())
 				// {
 				// if (unit.shouldBeInstalled() && unit.getPropertys() != null)
 				// {
 				// for (final Property propCfg : unit.getPropertys()) {
 				// initProperty(cmd, propCfg);
 				// }
 				// }
 				// if (unit.getPropextensions() != null) {
 				// for (final PropertyExtension propext :
 				// unit.getPropextensions()) {
 				// final PropertyExtensionFromInstallUnit prfiu =
 				// (PropertyExtensionFromInstallUnit) propext;
 				// initProperty(cmd, prfiu.getProperty());
 				// }
 				// }
 				// }
 			}
 		break;
 
 		default:
 			throw new AssertionError("Run mode \"" + RapidEnvInterpreter.getInstance().getRunMode().name()
 			        + " not yet supported");
 		}
 	}
 
 	private void initProperty(final CmdRenvCommand cmd, final Property propCfg) {
 		String propValue = null;
 		switch (cmd) {
 		case boot:
 			if (propCfg.getParentInstallunit() == null
 			        && (getPropertiesToProcess() == null || getPropertiesToProcess().size() == 0 || this
 			                .getPropertiesToProcess().contains(propCfg))) {
 				propValue = propCfg.update();
 			}
 		break;
 		case update:
 		case config:
 			if (propCfg.getParentInstallunit() != null
 			        && (getPropertyValuePersisted(propCfg.getFullyQualifiedName()) == null && (((getPropertiesToProcess() == null || getPropertiesToProcess()
 			                .size() == 0) && (true)) || this.getPropertiesToProcess().contains(propCfg)))) {
 				propValue = propCfg.update();
 			} else {
 				propValue = getPropertyValuePersisted(propCfg.getFullyQualifiedName());
 			}
 		break;
 		case install:
 			// System.out.println("@@@ @@@@@@@@@@@@@@@@@@@@@ init property: " +
 			// propCfg.getFullyQualifiedName() + "...");
 			final boolean isInstallunitSpecific = propCfg.getParentInstallunit() != null;
 			// System.out.println("@@@ getInstallunitsToProcess().size() = "
 			// + getInstallunitsToProcess().size());
 			// System.out.println("@@@ all install units .size() = "
 			// +
 			// getProject().getContainer().findBeansByType("org.rapidbeans.rapidenv.config.Installunit")
 			// .size());
 			final boolean allInstallunitsToProcess = getInstallunitsToProcess().size() == getProject().getContainer()
 			        .findBeansByType("org.rapidbeans.rapidenv.config.Installunit").size();
 			boolean thisPropsInstallunitToProcess = false;
 			if ((!allInstallunitsToProcess) && getInstallunitsToProcess() != null) {
 				for (final Installunit u : getInstallunitsToProcess()) {
 					if (u.getPropertys() != null && u.getPropertys().contains(propCfg)) {
 						thisPropsInstallunitToProcess = true;
 						break;
 					}
 				}
 			}
 			boolean touchedByInstallunitToProcess = false;
 			if (getInstallunitsToProcess() != null) {
 				for (final Installunit u : getInstallunitsToProcess()) {
 					if (!u.shouldBeInstalled()) {
 						continue;
 					}
 					if (u.getPropextensions() != null) {
 						for (final PropertyExtension propext : u.getPropextensions()) {
 							if (propext instanceof PropertyExtensionFromInstallUnit) {
 								final PropertyExtensionFromInstallUnit pu = (PropertyExtensionFromInstallUnit) propext;
 								if (pu.getProperty().equals(propCfg)) {
 									touchedByInstallunitToProcess = true;
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 			final boolean controlNormal = propCfg.getParentInstallunit() != null
 			        && propCfg.getParentInstallunit().getInstallcontrol() == InstallControl.normal;
 			final boolean controlDiscontinued = propCfg.getParentInstallunit() != null
 			        && propCfg.getParentInstallunit().getInstallcontrol() == InstallControl.discontinued;
 			// System.out.println("@@@ init property: " +
 			// propCfg.getFullyQualifiedName() + "...");
 			// System.out.println("@@@ controlDiscontinued = " +
 			// controlDiscontinued);
 			// System.out.println("@@@ thisPropsInstallunitToProcess = " +
 			// thisPropsInstallunitToProcess);
 			// System.out.println("@@@ controlNormal = " + controlNormal);
 			// System.out.println("@@@ allInstallunitsToProcess = " +
 			// allInstallunitsToProcess);
 			if (controlDiscontinued) {
 				propValue = null;
 			} else if ((isInstallunitSpecific && (thisPropsInstallunitToProcess || (controlNormal && allInstallunitsToProcess)))
 			        || ((!isInstallunitSpecific) && touchedByInstallunitToProcess)) {
 				propValue = propCfg.update();
 			} else {
 				propValue = getPropertyValuePersisted(propCfg.getFullyQualifiedName());
 			}
 		break;
 		default:
 			propValue = getPropertyValuePersisted(propCfg.getFullyQualifiedName());
 		break;
 		}
 		if (propValue != null) {
 			setPropertyValue(propCfg.getFullyQualifiedName(), propValue);
 		}
 	}
 
 	/**
 	 * get the (default) values by interpreting the values from the environment
 	 * definition file and ask for user definition in case of "personal"
 	 * variables.
 	 */
 	private void readProfile() {
 		final File profileFileProps = getProfileProps();
 		log(Level.FINE, "Reading user profile from file: " + profileFileProps.getAbsolutePath());
 		if (!profileFileProps.exists()) {
 			return;
 		}
 		this.propertiesPersisted = new Properties();
 		FileReader rd = null;
 		try {
 			rd = new FileReader(profileFileProps);
 			this.propertiesPersisted.load(rd);
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			if (rd != null) {
 				try {
 					rd.close();
 				} catch (IOException e) {
 					throw new RapidEnvException(e);
 				}
 			}
 		}
 	}
 
 	private static final String LF = PlatformHelper.getLineFeed();
 
 	/**
 	 * write the variables to<br/>
 	 * - the profile and<br/>
 	 * - the environment profile.
 	 */
 	protected void writeProfile() {
 		final File profileDir = getProject().getProfiledir();
 		final File profileFileProps = new File(profileDir, "renv_" + PlatformHelper.username() + "_"
 		        + PlatformHelper.hostname() + ".properties");
 		File profileFileCmd = null;
 		switch (PlatformHelper.getOsfamily()) {
 		case windows:
 			profileFileCmd = new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname()
 			        + ".cmd");
 		break;
 		case linux:
 			profileFileCmd = new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname()
 			        + ".sh");
 		break;
 		default:
 			throw new RapidEnvException("OS platform \"" + PlatformHelper.getOsName() + "\" not yet supported");
 		}
 
 		FileWriter wrProps = null;
 		FileWriter wrCmd = null;
 		try {
 			wrProps = new FileWriter(profileFileProps);
 			wrCmd = new FileWriter(profileFileCmd);
 			// do not use properties.store() for writing the properties file for
 			// it
 			// - writes the properties unsorted
 			// - writes the unwanted time stamp
 			wrProps.write("# RapidEnv properties profile" + LF + "# Project: " + getProject().getName() + LF
 			        + "# Tag: " + getProject().getTag() + LF + "# User: " + PlatformHelper.username() + LF
 			        + "# Machine: " + PlatformHelper.hostname() + LF + "# Do not edit this file manually" + LF);
 			switch (PlatformHelper.getOsfamily()) {
 			case windows:
 				wrCmd.write(":: RapidEnv environment profile" + LF + ":: Project: " + getProject().getName() + LF
 				        + ":: Tag: " + getProject().getTag() + LF + ":: User: " + PlatformHelper.username() + LF
 				        + ":: Machine: " + PlatformHelper.hostname() + LF + ":: Do not edit this file manually" + LF);
 				wrCmd.write("@ECHO OFF" + LF);
 			break;
 			default:
 				wrCmd.write("# RapidEnv environment profile" + LF + "# Project: " + getProject().getName() + LF
 				        + "# Tag: " + getProject().getTag() + LF + "# User: " + PlatformHelper.username() + LF
 				        + "# Machine: " + PlatformHelper.hostname() + LF + "# Do not edit this file manually" + LF);
 			}
 			if (getProject().getPropertys() != null) {
 				final EscapeMap escapeMap = new EscapeMap(new String[] { "\\", "\\\\", "=", "\\=", ": ", "\\:" });
 				switch (PlatformHelper.getOsfamily()) {
 				case windows:
 					wrCmd.write("set RAPID_ENV_HOME=" + System.getenv("RAPID_ENV_HOME") + LF);
 				break;
 				default:
 					wrCmd.write("export RAPID_ENV_HOME=\"" + System.getenv("RAPID_ENV_HOME") + "\"" + LF);
 				break;
 				}
 				for (final Property prop : getProject().getPropertys()) {
 					final String propName = prop.getFullyQualifiedName();
 					final String propValue = getPropertyValue(propName);
 					if (propValue != null) {
 						wrProps.write(prop.getFullyQualifiedName() + "=" + StringHelper.escape(propValue, escapeMap)
 						        + LF);
 						final Environment environmentVar = prop.getEnvironment(PlatformHelper.getOsfamily());
 						if (environmentVar != null) {
 							switch (PlatformHelper.getOsfamily()) {
 							case windows:
 								wrCmd.write("set " + environmentVar.getName() + "=" + propValue + LF);
 							break;
 							default:
 								wrCmd.write("export " + environmentVar.getName() + "=\"" + propValue + "\"" + LF);
 							break;
 							}
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new RapidEnvException(e);
 		} finally {
 			try {
 				if (wrProps != null) {
 					wrProps.close();
 				}
 				if (wrCmd != null) {
 					wrCmd.close();
 				}
 			} catch (IOException e) {
 				throw new RapidEnvException(e);
 			}
 		}
 	}
 
 	private void deleteProfile() {
 		final File profileDir = getProject().getProfiledir();
 		final File profileFileProps = new File(profileDir, "renv_" + PlatformHelper.username() + "_"
 		        + PlatformHelper.hostname() + ".properties");
 		File profileFileCmd = null;
 		switch (PlatformHelper.getOsfamily()) {
 		case windows:
 			profileFileCmd = new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname()
 			        + ".cmd");
 		break;
 		case linux:
 			profileFileCmd = new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname()
 			        + ".sh");
 		break;
 		default:
 			throw new RapidEnvException("OS platform \"" + PlatformHelper.getOsName() + "\" not yet supported");
 		}
 		out.println("Deleting RapidEnv user profile " + profileFileProps.getAbsolutePath() + "...");
 		profileFileProps.delete();
 		out.println("Deleting RapidEnv user profile " + profileFileCmd.getAbsolutePath() + "...");
 		profileFileCmd.delete();
 	}
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param cmd
 	 *            the command to execute.
 	 */
 	public RapidEnvInterpreter(final CmdRenv cmd) {
 		init(cmd);
 	}
 
 	/**
 	 * For test reasons create with logger mock.
 	 * 
 	 * @param cmd
 	 *            the command to execute
 	 * @param logger
 	 *            the logger mock
 	 */
 	public RapidEnvInterpreter(final CmdRenv cmd, final Logger logger) {
 		this.logger = logger;
 		init(cmd);
 	}
 
 	/**
 	 * For test reasons create with changed ant gateway.
 	 * 
 	 * @param cmd
 	 *            the command to execute
 	 * @param ant
 	 *            the ant gateway to inject
 	 */
 	protected RapidEnvInterpreter(CmdRenv cmd, AntGateway ant) {
 		this.ant = ant;
 		init(cmd);
 	}
 
 	private void init(final CmdRenv cmd) {
 
 		if (this.ant == null) {
 			this.ant = new AntGateway();
 		}
 
 		this.renvCommand = cmd;
 		if (cmd != null) {
 			this.runMode = RunMode.command;
 		}
 		this.command = cmd.getCommand();
 		this.options = cmd.getOptions();
 
 		if (this.command == CmdRenvCommand.version || this.command == CmdRenvCommand.help) {
 			return;
 		}
 
 		if (this.runMode == RunMode.command && this.options.get(CmdRenvOption.yes) != null) {
 			this.runMode = RunMode.batch;
 		}
 
 		// create the profile directory if it doesn't exist
 		final String profilesHomePath = System.getenv("RAPID_ENV_PROFILES_HOME");
 		if (profilesHomePath != null) {
 			final File profileDir = new File(profilesHomePath);
 			if (!profileDir.exists()) {
 				FileHelper.mkdirs(profileDir);
 			}
 		}
 
 		// load the configuration file
 		final File configfile = cmd.getConfigfile();
 		TypeRapidBean.forName(Project.class.getName());
 		try {
 			PropertyInterpretedString.lockIntepretation();
 			this.configDoc = new Document(TypeRapidBean.forName("org.rapidbeans.rapidenv.config.Project"), configfile);
 			PropertyInterpretedString.unlockIntepretation();
 			singleInstance = this;
 			getProject().checkSemantics();
 			getProject().updateToolMap();
 		} catch (ValidationInstanceAssocTwiceException e) {
 			evalException(e);
 		} catch (RapidBeansRuntimeException e) {
 			if (e.getCause() instanceof ValidationInstanceAssocTwiceException) {
 				evalException((ValidationInstanceAssocTwiceException) e.getCause());
 			} else if (e.getCause() != null && e.getCause().getCause() != null
 			        && e.getCause().getCause() instanceof FileNotFoundException) {
 				final String notFoundPath = StringHelper.splitFirst(e.getCause().getCause().getMessage());
 				if (notFoundPath != null && notFoundPath.length() > 0
 				        && (notFoundPath.endsWith(".dtd") || notFoundPath.endsWith(".xsd"))) {
 					throw new RapidEnvConfigurationException("Syntax definition for RapidEnv configuration file \""
 					        + notFoundPath + "\" not found", e);
 				} else {
 					throw new RapidEnvConfigurationException("RapidEnv configuration file \""
 					        + configfile.getAbsolutePath() + "\" not found", e);
 				}
 			} else {
 				throw e;
 			}
 		}
 
 		// set the log level
 		if (this.options.containsKey(CmdRenvOption.debug)) {
 			logger.setLevel(Level.FINER);
 		} else if (this.options.containsKey(CmdRenvOption.verbose)) {
 			logger.setLevel(Level.FINE);
 		} else { // default
 			logger.setLevel(Level.INFO);
 		}
 	}
 
 	/**
 	 * Evaluate a ValidationInstanceAssocTwiceException.
 	 * 
 	 * @param e
 	 *            the exception to evaluate.
 	 */
 	private void evalException(ValidationInstanceAssocTwiceException e) {
 		if (e.getInstance() instanceof Installunit) {
 			throw new RapidEnvConfigurationException("Tool \""
 			        + ((Installunit) e.getInstance()).getFullyQualifiedName() + "\" specified twice", e);
 		} else if (e.getInstance() instanceof Property) {
 			throw new RapidEnvConfigurationException("Property \""
 			        + ((Property) e.getInstance()).getFullyQualifiedName() + "\" specified twice", e);
 		} else {
 			throw e;
 		}
 	}
 
 	/**
 	 * Needed privately and for testing.
 	 * 
 	 * @param command
 	 *            the command issued
 	 */
 	protected void initPropertiesAndInstallunitsToProcess(final CmdRenvCommand command) {
 
 		// collect install unit names from command
 		Collection<String> installUnitOrPropertyNames = this.renvCommand.getInstallunitOrPropertyNames();
 		if (installUnitOrPropertyNames == null || installUnitOrPropertyNames.size() == 0) {
 			// or take all top level install units configured
 			// if no specific install units are defined with the command
 			this.installUnitOrPropertyNamesExplicitelySpecified = false;
 			installUnitOrPropertyNames = new ArrayList<String>();
 			if (getProject() != null) {
 				if (getProject().getPropertys() != null) {
 					for (final Property property : getProject().getPropertys()) {
 						if (property.getParentInstallunit() == null
 						        || (getInstallationStatus(property.getParentInstallunit(), command) != InstallStatus.notinstalled && getInstallationStatus(
 						                property.getParentInstallunit(), command) != InstallStatus.deinstallrequired)) {
 							installUnitOrPropertyNames.add(property.getFullyQualifiedName());
 						}
 					}
 				}
 				if (getProject().getInstallunits() != null) {
 					for (final Installunit unit : getProject().getInstallunits()) {
 						installUnitOrPropertyNames.add(unit.getFullyQualifiedName());
 					}
 				}
 			}
 		} else {
 			this.installUnitOrPropertyNamesExplicitelySpecified = true;
 		}
 
 		final InstallunitsAndProperties entitiesToProcess = determineInstallunitsAndPropertiesToProcess(
 		        installUnitOrPropertyNames, this.renvCommand.getConfigfile().getAbsolutePath());
 
 		this.propertiesToProcess = entitiesToProcess.properties;
 		final List<Installunit> installUnitsToProc1 = entitiesToProcess.installunits;
 		final CmdRenvCommand cmd = this.renvCommand.getCommand();
 		List<Installunit> installUnitsToProc2;
 		if (cmd == CmdRenvCommand.install || cmd == CmdRenvCommand.deinstall || cmd == CmdRenvCommand.update) {
 			installUnitsToProc2 = sortAccordingToDependencies(installUnitsToProc1, this.renvCommand.getCommand());
 		} else {
 			installUnitsToProc2 = installUnitsToProc1;
 		}
 		this.installUnitsToProcess = completeAndSortInstallunitsToProcess(installUnitsToProc2,
 		        this.renvCommand.getCommand());
 		// process all properties whenever at least one installunit is specified
 		// TODO: later on replace this by only taking used (includes also
 		// transitively used) properties.
 		if (this.installUnitsToProcess.size() > 0) {
 			for (final Property prop : getProject().getPropertys()) {
 				if (!this.propertiesToProcess.contains(prop)) {
 					this.propertiesToProcess.add(prop);
 				}
 			}
 			// for (final Installunit unit : getProject().getInstallunits()) {
 			// if (unit.getPropertys() != null) {
 			// for (final Property prop : unit.getPropertys()) {
 			// if (unit.shouldBeInstalled() &&
 			// !this.propertiesToProcess.contains(prop)) {
 			// this.propertiesToProcess.add(prop);
 			// }
 			// }
 			// }
 			// }
 		}
 		checkDependenciesAll();
 		checkDependencies(this.installUnitsToProcess, this.renvCommand.getCommand());
 	}
 
 	protected List<Installunit> sortAccordingToDependencies(final List<Installunit> installUnitsToProc,
 	        final CmdRenvCommand command) {
 		if (command != CmdRenvCommand.install && command != CmdRenvCommand.deinstall
 		        && command != CmdRenvCommand.update) {
 			return installUnitsToProc;
 		}
 		final List<Installunit> installunits = new ArrayList<Installunit>();
 		for (final Installunit unit : installUnitsToProc) {
 			int extremeUnitIndex = -1;
 			final int size = installunits.size();
 			switch (command) {
 			case install:
 			case update:
 				if (unit.getDependents() != null) {
 					for (int i = size - 1; i >= 0; i--) {
 						final Installunit unit2 = installunits.get(i);
 						if (unit.getDependents().contains(unit2)) {
 							extremeUnitIndex = i;
 						}
 					}
 				}
 				if (extremeUnitIndex == -1) {
 					installunits.add(unit);
 				} else {
 					installunits.add(extremeUnitIndex, unit);
 				}
 			break;
 			case deinstall:
 				if (unit.getDepends() != null) {
 					for (int i = size - 1; i >= 0; i--) {
 						final Installunit unit2 = installunits.get(i);
 						if (unit.getDepends().contains(unit2)) {
 							extremeUnitIndex = i;
 						}
 					}
 				}
 				if (extremeUnitIndex == -1) {
 					installunits.add(unit);
 				} else {
 					installunits.add(extremeUnitIndex, unit);
 				}
 			break;
 			}
 		}
 		return installunits;
 	}
 
 	protected void checkDependencies(List<Installunit> installUnits, CmdRenvCommand command) {
 		for (final Installunit unit : installUnits) {
 			switch (command) {
 			case install:
 				if (unit.getParentUnit() != null && !installUnits.contains(unit.getParentUnit())) {
 					if (getInstallationStatus(unit, command) != null) {
 						switch (getInstallationStatus(unit.getParentUnit(), command)) {
 						case notinstalled:
 							throw new RapidEnvCmdException("Can not install unit \"" + unit.getFullyQualifiedName()
 							        + "\" because parent unit \"" + unit.getParentUnit() + "\" is not installed.");
 						case upgraderequired:
 							throw new RapidEnvCmdException("Can not install unit \"" + unit.getFullyQualifiedName()
 							        + "\" because parent unit \"" + unit.getParentUnit()
 							        + "\" is not up to date (upgrade is required).");
 						case downgraderequired:
 							throw new RapidEnvCmdException("Can not install unit \"" + unit.getFullyQualifiedName()
 							        + "\" because parent unit \"" + unit.getParentUnit()
 							        + "\" is not up to date (downgrade is required).");
 						}
 					}
 				}
 			break;
 			case update:
 			break;
 			case deinstall:
 				if (unit.getDependents() != null) {
 					for (final Installunit unit1 : unit.getDependents()) {
 						if (!installUnits.contains(unit1)) {
 							switch (getInstallationStatus(unit1, command)) {
 							case uptodate:
 							case upgraderequired:
 							case configurationrequired:
 							case downgraderequired:
 								throw new RapidEnvCmdException("Can not deinstall unit \""
 								        + unit.getFullyQualifiedName() + "\" because it is required by unit \""
 								        + unit1.getFullyQualifiedName() + "\" which is installed.");
 							}
 						}
 					}
 				}
 			break;
 			default:
 			break;
 			}
 		}
 	}
 
 	protected void checkDependenciesAll() {
 
 		// check not allowed dependencies between different subunit nodes
 		checkDependenciesBetweenSubunitTreeNodes(getProject().getInstallunits());
 
 		// check dependency graph for cycles
 		checkDependencyCyclyesForSubunitNode(getProject().getInstallunits());
 	}
 
 	private void checkDependenciesBetweenSubunitTreeNodes(final List<Installunit> units) {
 
 		// iterate over units
 		if (units != null) {
 			for (final Installunit unit : units) {
 				if (unit.getDepends() != null) {
 					for (final Installunit depunit : unit.getDepends()) {
 						if (!units.contains(depunit)) {
 							throw new RapidEnvConfigurationException(
 							        "Invalid dependency defined between install unit \"" + unit.getFullyQualifiedName()
 							                + "\" and install unit \"" + depunit.getFullyQualifiedName() + "\""
 							                + " because these install units are defined "
 							                + " on different subunit tree node levels.");
 						}
 					}
 				}
 
 				// recurse over subunits
 				if (unit.getSubunits() != null && unit.getSubunits().size() > 0) {
 					checkDependenciesBetweenSubunitTreeNodes(unit.getSubunits());
 				}
 			}
 		}
 	}
 
 	private void checkDependencyCyclyesForSubunitNode(final List<Installunit> units) {
 
 		// iterate over units of one subunit node
 		if (units != null) {
 			for (final Installunit unit : units) {
 				if (unit.getDepends() != null && unit.getDepends().size() > 0) {
 					final List<Installunit> visited = new ArrayList<Installunit>();
 					checkDependencyCyclyes(unit, visited);
 				}
 
 				// recurse over subunits
 				if (unit.getSubunits() != null && unit.getSubunits().size() > 0) {
 					checkDependencyCyclyesForSubunitNode(unit.getSubunits());
 				}
 			}
 		}
 	}
 
 	private void checkDependencyCyclyes(final Installunit unit, final List<Installunit> visited) {
 		visited.add(unit);
 		if (unit.getDepends() != null) {
 			for (final Installunit depunit : unit.getDepends()) {
 				if (depunit.equals(unit)) {
 					throw new RapidEnvConfigurationException("Invalid self dependency " + "defined for install unit \""
 					        + unit.getFullyQualifiedName() + "\"");
 				}
 				if (visited.contains(depunit)) {
 					final StringBuffer cycle = new StringBuffer();
 					boolean found = false;
 					for (final Installunit visitedUnit : visited) {
 						if (found || visitedUnit.equals(depunit)) {
 							if (found) {
 								cycle.append(", ");
 							}
 							cycle.append('"');
 							cycle.append(visitedUnit.getFullyQualifiedName());
 							cycle.append('"');
 							found = true;
 						}
 					}
 					throw new RapidEnvConfigurationException("Invalid dependency cycle " + "defined for install units "
 					        + cycle.toString());
 				}
 				checkDependencyCyclyes(depunit, visited);
 			}
 		}
 		visited.remove(unit);
 	}
 
 	/**
 	 * Complete install units to process according to the command, the subunits,
 	 * and the dependencies.
 	 * 
 	 * @param installUnitsToProc
 	 *            the install units to process
 	 * @param cmd
 	 *            the command
 	 * 
 	 * @return the completed and sorted list
 	 */
 	protected List<Installunit> completeAndSortInstallunitsToProcess(final List<Installunit> installUnitsToProc,
 	        final CmdRenvCommand cmd) {
 
 		// take top level units and subunits in defined order
 		final List<Installunit> installUnitsToProc2 = new ArrayList<Installunit>();
 		for (final Installunit unit : installUnitsToProc) {
 			if ((!unit.isSubunit()) || (!parentUnitIn(unit, installUnitsToProc))) {
 				addSubunitsRecursively(unit, installUnitsToProc2, cmd);
 			}
 		}
 		return installUnitsToProc2;
 	}
 
 	private boolean parentUnitIn(Installunit unit, List<Installunit> installUnitsToProc) {
 		final List<Installunit> parentUnits = unit.getParentUnits();
 		for (final Installunit current : installUnitsToProc) {
 			if (parentUnits.contains(current)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected void addSubunitsRecursively(final Installunit unit, final List<Installunit> installUnitsToProc,
 	        final CmdRenvCommand cmd) {
 		switch (cmd) {
 		case deinstall:
 			if (unit.getSubunits() != null) {
 				final List<Installunit> subunits = sortAccordingToDependencies(unit.getSubunits(), cmd);
 				for (final Installunit subunit : subunits) {
 					addSubunitsRecursively(subunit, installUnitsToProc, cmd);
 				}
 			}
 			installUnitsToProc.add(unit);
 		break;
 		default:
 			installUnitsToProc.add(unit);
 			if (unit.getSubunits() != null) {
 				final List<Installunit> subunits = sortAccordingToDependencies(unit.getSubunits(), cmd);
 				for (final Installunit subunit : subunits) {
 					addSubunitsRecursively(subunit, installUnitsToProc, cmd);
 				}
 			}
 		break;
 		}
 	}
 
 	private class InstallunitsAndProperties {
 		protected List<Property> properties = null;
 
 		protected List<Installunit> installunits = null;
 
 		public InstallunitsAndProperties() {
 			this.properties = new ArrayList<Property>();
 			this.installunits = new ArrayList<Installunit>();
 		}
 	}
 
 	/**
 	 * @param installUnitOrPropertyNames
 	 *            then names to process
 	 * @param configFilePath
 	 *            path of the configuration file
 	 * @return all install units and properties to process
 	 */
 	protected InstallunitsAndProperties determineInstallunitsAndPropertiesToProcess(
 	        Collection<String> installUnitOrPropertyNames, final String configFilePath) {
 
 		final InstallunitsAndProperties result = new InstallunitsAndProperties();
 		final Map<String, Installunit> installUnitsToProcMap = new HashMap<String, Installunit>();
 		final Map<String, Property> propertiesToProcMap = new HashMap<String, Property>();
 
 		// iterate over all install unit names
 		for (final String installUnitOrPropertyName : installUnitOrPropertyNames) {
 			Property property = null;
 			Installunit unit = null;
 			try {
 				property = getProject().findPropertyConfiguration(installUnitOrPropertyName);
 				unit = getProject().findInstallunitConfiguration(installUnitOrPropertyName);
 			} catch (RapidEnvConfigurationException e) {
 				if (e.getMessage().startsWith("Ambigouus tool name ")) {
 					throw new RapidEnvCmdException("Ambigouus tool name \"" + installUnitOrPropertyName
 					        + "\" has been specified with the command");
 				} else {
 					throw e;
 				}
 			}
 			if (unit == null && property == null) {
 				throw new RapidEnvCmdException("No install unit or property \"" + installUnitOrPropertyName
 				        + "\"\n  is defined in RapidEnv environment configuration file\n  \"" + configFilePath + "\"",
 				        ExceptionMap.ERRORCODE_UNKNOWN_PROP_OR_UNIT);
 			} else if (unit != null && property != null) {
 				throw new RapidEnvCmdException("Ambigouus install unit / property name \"" + installUnitOrPropertyName
 				        + "\" defined in RapidEnv environment configuration file \"" + configFilePath + "\"",
 				        ExceptionMap.ERRORCODE_AMBIGOUUS_NAME);
 			} else if (unit != null && property == null) {
 				if (installUnitsToProcMap.get(unit.getFullyQualifiedName()) == null) {
					if (unit.getOsfamily() != null && (!unit.getOsfamily().equals(PlatformHelper.getOsfamily()))) {
						log(Level.FINE, "Installation unit \"" + unit.getFullyQualifiedName()
						        + "\" excluded on OS family \"" + PlatformHelper.getOsfamily().name() + "\"");
					} else if (unit.getOs() != null && (!unit.getOs().equals(PlatformHelper.getOs()))) {
						log(Level.FINE, "Installation unit \"" + unit.getFullyQualifiedName() + "\" excluded on OS \""
						        + PlatformHelper.getOs().name() + "\"");
					} else if (unit.getArchitecture() != null
					        && (!unit.getArchitecture().equals(PlatformHelper.getArchName()))) {
						log(Level.FINE, "Installation unit \"" + unit.getFullyQualifiedName()
						        + "\" excluded on architecture \"" + PlatformHelper.getArchName() + "\"");
					} else {
						installUnitsToProcMap.put(unit.getFullyQualifiedName(), unit);
						result.installunits.add(unit);
					}
 				} else {
 					logger.warning("Install unit \"" + unit.getFullyQualifiedName()
 					        + "\" has been specified for one command multiple times");
 				}
 			} else if (unit == null && property != null) {
 				if (propertiesToProcMap.get(property.getFullyQualifiedName()) == null) {
 					propertiesToProcMap.put(property.getFullyQualifiedName(), property);
 					result.properties.add(property);
 				} else {
 					logger.warning("Property \"" + property.getFullyQualifiedName()
 					        + "\" has been specified for one command multiple times");
 				}
 			} else {
 				throw new AssertionError("What???");
 			}
 		}
 		return result;
 	}
 
 	public File getProfileCmd() {
 		final File profileDir = getProject().getProfiledir();
 		switch (PlatformHelper.getOsfamily()) {
 		case windows:
 			return new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname() + ".cmd");
 		case linux:
 			return new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname() + ".sh");
 		default:
 			throw new RapidEnvException("OS platform \"" + PlatformHelper.getOsName() + "\" not yet supported");
 		}
 	}
 
 	public File getProfileProps() {
 		final File profileDir = getProject().getProfiledir();
 		final File profile = new File(profileDir, "renv_" + PlatformHelper.username() + "_" + PlatformHelper.hostname()
 		        + ".properties");
 		return profile;
 	}
 
 	/**
 	 * Print out the statistics
 	 * 
 	 * @return the statistics string
 	 */
 	public String getStatisicsAsString() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("Started at: " + DateFormat.getInstance().format(this.timeStart) + PlatformHelper.getLineFeed());
 		sb.append("Finished at: " + DateFormat.getInstance().format(this.timeEnd) + PlatformHelper.getLineFeed());
 		sb.append("Total time: " + formatHhmm(getExecutionTime()) + " minutes:seconds" + PlatformHelper.getLineFeed());
 		return sb.toString();
 	}
 
 	private long timeStart = Long.MIN_VALUE;
 
 	private long timeEnd = Long.MIN_VALUE;
 
 	private long getExecutionTime() {
 		return timeEnd - timeStart;
 	}
 
 	protected static String formatHhmm(final long time) {
 		return StringHelper.fillUp(Long.toString(time / 60000), 2, '0', FillMode.left) + ":"
 		        + StringHelper.fillUp(Long.toString((time / 1000) % 60), 2, '0', FillMode.left);
 	}
 }
