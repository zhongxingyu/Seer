 /*
  * RapidEnv: ShellLinkIcon.java
  *
  * Copyright (C) 2011 Martin Bluemel
  *
  * Creation Date: 06/03/2011
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
 
 package org.rapidbeans.rapidenv.config.cmd;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.rapidbeans.core.type.TypeRapidBean;
 import org.rapidbeans.core.util.FileHelper;
 import org.rapidbeans.core.util.PlatformHelper;
 import org.rapidbeans.rapidenv.RapidEnvException;
 import org.rapidbeans.rapidenv.RapidEnvInterpreter;
 import org.rapidbeans.rapidenv.config.Installunit;
 import org.rapidbeans.rapidenv.config.Project;
 import org.rapidbeans.rapidenv.config.file.ConfigFileEditorProperties;
 import org.rapidbeans.rapidenv.config.file.ConfigFileProperties;
 
 /**
  * A shortcut / shell link to install, update, deinstall
  */
 public class ShellLinkIcon extends RapidBeanBaseShellLinkIcon {
 
 	/**
 	 * Check if the file configuration has been performed properly or not
 	 * 
 	 * @param execute
 	 *            if false only execute the check if the configuration is
 	 *            necessary if true execute the configuration if necessary
 	 * 
 	 * @return if the configuration has been performed properly or not
 	 */
 	@Override
 	public boolean check(final boolean execute) {
 		try {
 			final Project project = ((Installunit) this.getParentBean())
 					.getProject();
 			boolean ok = true;
 			File desktopFolder = null;
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				desktopFolder = new File(System.getenv("USERPROFILE")
 						+ "\\Desktop");
 				break;
 			case linux:
 				desktopFolder = new File(System.getenv("HOME") + "/Desktop");
 				break;
 			default:
 				throw new RapidEnvException("Icons are not supported for OS \""
 						+ PlatformHelper.getOs().name() + "\".");
 			}
 			if (this.getShowondesktop()) {
 				switch (PlatformHelper.getOs()) {
 				case windows:
 					ok = checkShellLink(execute, getTitle(), desktopFolder);
 					break;
 				case linux:
 					switch (RapidEnvInterpreter.getLinuxDesktop()) {
 					case kde:
 						ok = checkShellLink(execute, getTitle(), desktopFolder);
 						break;
 					case gnome:
 						ok = checkShellLink(execute, getTitle(), desktopFolder);
 						break;
 					default:
 						RapidEnvInterpreter.log(Level.FINE,
 								"No start menu shell link icon \""
 										+ getTitle()
 										+ "\" for Linux desktop \""
 										+ RapidEnvInterpreter.getLinuxDesktop()
 												.name());
 					}
 					break;
 				default:
 					throw new RapidEnvException(
 							"Icons are not supported for OS \""
 									+ PlatformHelper.getOs().name() + "\".");
 				}
 			} else {
 				ok = checkShellLinkDelete(execute, getTitle(), desktopFolder);
 			}
 			File startMenuFolder = null;
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				startMenuFolder = new File(ShellLinkWindows
 						.getStartMenuFolder().getAbsolutePath()
 						+ File.separator
 						+ project.getName()
 						+ File.separator
 						+ project.getTag());
 				break;
 			case linux:
 				startMenuFolder = new File(System.getenv("HOME")
 						+ "/.local/share/applications");
 				break;
 			default:
 				throw new RapidEnvException("Icons are not supported for OS \""
 						+ PlatformHelper.getOs().name() + "\".");
 			}
 			if (ok) {
 				if (this.getShowonstartmenu()) {
 					switch (PlatformHelper.getOs()) {
 					case windows:
 						ok = checkShellLink(execute, getTitle(),
 								startMenuFolder);
 						break;
 					case linux:
 						switch (RapidEnvInterpreter.getLinuxDesktop()) {
 						case kde:
 							ok = checkShellLink(execute, getTitle(),
 									startMenuFolder);
 							break;
 						case gnome:
 							ok = checkShellLink(execute, getTitle(),
 									startMenuFolder);
 							break;
 						default:
 							RapidEnvInterpreter.log(Level.FINE,
 									"No start menu shell link icon \""
 											+ getTitle()
 											+ "\" for Linux desktop \""
 											+ RapidEnvInterpreter
 													.getLinuxDesktop().name());
 						}
 						break;
 					default:
 						throw new RapidEnvException(
 								"Icons are not supported for OS \""
 										+ PlatformHelper.getOs().name() + "\".");
 					}
 				} else {
 					ok = checkShellLinkDelete(execute, getTitle(),
 							startMenuFolder);
 				}
 			}
 			this.setOk(ok);
 			return ok;
 		} catch (RuntimeException e) {
 			this.setOk(false);
 			throw e;
 		}
 	}
 
 	protected String argumentsToString(final List<Argument> arguments) {
 		final StringBuffer args = new StringBuffer();
 		if (arguments != null) {
 			int i = 0;
 			for (final Argument arg : arguments) {
 				if (i > 0) {
 					args.append(' ');
 				}
 				if (arg.getQuoted()) {
 					args.append('"');
 				}
 				args.append(arg.getValue());
 				if (arg.getQuoted()) {
 					args.append('"');
 				}
 				i++;
 			}
 		}
 		return args.toString();
 	}
 
 	/**
 	 * Check or create / update a shell link
 	 * 
 	 * @boolean execute determines if the shell link should only be checked or
 	 *          really manipulated.
 	 * 
 	 * @param title
 	 *            the shell link's title. Can not be changed since it also
 	 *            changes the file name.
 	 * 
 	 * @param shellLinkFolder
 	 *            the folder where to store the shell link
 	 * 
 	 * @return if the shell link is OK or must be updated
 	 */
 	private boolean checkShellLink(final boolean execute, final String title,
 			final File shellLinkFolder) {
 
		final File executeInFolder = new File(getExecutein());
 		final RapidEnvInterpreter interpreter = RapidEnvInterpreter
 				.getInstance();
 		File executable = getExecutableAsFile();
 		final List<Argument> args = new ArrayList<Argument>();
 		final List<Argument> configArgs = this.getArguments();
 		Argument argExePath;
 		Argument argProfileCmd;
 		switch (getExecutionmode()) {
 		case cmd:
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				executable = new File(System.getenv("SystemRoot")
 						+ File.separator + "system32" + File.separator
 						+ "cmd.exe");
 				args.add(new Argument("/C"));
 				args.add(new Argument("call"));
 				argExePath = new Argument(getExecutableAsFile()
 						.getAbsolutePath());
 				argExePath.setQuoted(true);
 				args.add(argExePath);
 				break;
 			case linux:
 				executable = new File("/bin/bash");
 				args.add(new Argument("-c"));
 				argExePath = new Argument(getExecutableAsFile()
 						.getAbsolutePath());
 				argExePath.setQuoted(true);
 				args.add(argExePath);
 				break;
 			default:
 				throw new RapidEnvException("Icons with execution mode \"cmd\""
 						+ " are not supported for OS \""
 						+ PlatformHelper.getOs().name() + "\".");
 			}
 			break;
 		case cmdenv:
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				executable = new File(System.getenv("SystemRoot")
 						+ File.separator + "system32" + File.separator
 						+ "cmd.exe");
 				args.add(new Argument("/C"));
 				args.add(new Argument("call"));
 				argProfileCmd = new Argument(interpreter.getProfileCmd()
 						.getAbsolutePath());
 				argProfileCmd.setQuoted(true);
 				args.add(argProfileCmd);
 				args.add(new Argument("&"));
 				args.add(new Argument("call"));
 				argExePath = new Argument(getExecutableAsFile()
 						.getAbsolutePath());
 				argExePath.setQuoted(true);
 				args.add(argExePath);
 				break;
 			case linux:
 				executable = new File("/bin/bash");
 				args.add(new Argument("-c"));
 				argProfileCmd = new Argument(interpreter.getProfileCmd()
 						.getAbsolutePath());
 				argProfileCmd.setQuoted(true);
 				args.add(argProfileCmd);
 				args.add(new Argument("&&"));
 				argExePath = new Argument(getExecutableAsFile()
 						.getAbsolutePath());
 				argExePath.setQuoted(true);
 				args.add(argExePath);
 				break;
 			default:
 				throw new RapidEnvException("Icons are not supported for OS \""
 						+ PlatformHelper.getOs().name() + "\".");
 			}
 			break;
 		default:
 			break;
 		}
 
 		if (configArgs != null) {
 			for (final Argument arg : configArgs) {
 				args.add(arg);
 			}
 		}
 
 		boolean ok = true;
 
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			ok = checkShellLinkWindows(execute, title, shellLinkFolder,
 					executeInFolder, executable, args);
 			break;
 		case linux:
 			ok = checkShellLinkLinux(execute, title, shellLinkFolder,
 					executeInFolder, executable, args);
 			break;
 		default:
 			throw new RapidEnvException("Icons are not supported for OS \""
 					+ PlatformHelper.getOs().name() + "\".");
 		}
 
 		return ok;
 	}
 
 	/**
 	 * Check or create / update a shell link
 	 * 
 	 * @boolean execute determines if the shell link should only be checked or
 	 *          really manipulated.
 	 * 
 	 * @param title
 	 *            the shell link's title. Can not be changed since it also
 	 *            changes the file name.
 	 * 
 	 * @param shellLinkFolder
 	 *            the folder where to store the shell link
 	 * 
 	 * @return if the shell link is OK or must be updated
 	 */
 	private boolean checkShellLinkDelete(final boolean execute,
 			final String title, final File shellLinkFolder) {
 
 		final RapidEnvInterpreter interpreter = RapidEnvInterpreter
 				.getInstance();
 		File shellLinkFile = null;
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			shellLinkFile = new File(shellLinkFolder, title + ".lnk");
 			break;
 		case linux:
 			shellLinkFile = new File(shellLinkFolder, title + ".desktop");
 			break;
 		default:
 			throw new RapidEnvException("Icons are not supported for OS \""
 					+ PlatformHelper.getOs().name() + "\".");
 		}
 
 		boolean ok = true;
 		String msgcheck = null;
 		if (shellLinkFile.exists()) {
 			if (execute) {
 				if (ok = shellLinkFile.delete()) {
 					msgcheck = "shell link icon \"" + title
 							+ "\" has been deleted in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".";
 					if (interpreter != null) {
 						interpreter.getOut().println(msgcheck);
 					}
 				} else {
 					throw new RapidEnvException("shell link icon \"" + title
 							+ "\" coud not be deleted in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".");
 				}
 			} else {
 				if (ok = !shellLinkFile.exists()) {
 					msgcheck = "shell link icon \"" + title
 							+ "\" is not desired in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".\n";
 				} else {
 					msgcheck = "shell link icon \"" + title
 							+ "\" needs to be deleted in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".\n";
 				}
 			}
 			if (!ok) {
 				setIssue(msgcheck);
 			} else {
 				if (interpreter != null) {
 					RapidEnvInterpreter.log(Level.FINE, msgcheck);
 				}
 			}
 		}
 		return ok;
 	}
 
 	private boolean checkShellLinkWindows(final boolean execute,
 			final String title, final File shellLinkFolder,
 			final File executeInFolder, final File executable,
 			final List<Argument> args) {
 		boolean ok = true;
 		final List<Argument> arguments = new ArrayList<Argument>();
 		for (final Argument arg : args) {
 			final Argument argument = new Argument(arg.getValue());
 			argument.setQuoted(arg.getQuoted());
 			arguments.add(argument);
 		}
 		final File shellLinkFile = new File(shellLinkFolder, title + ".lnk");
 		final ShellLinkWindows shellLink = new ShellLinkWindows(shellLinkFile);
 		final File iconFile = new File(getIconfile());
 		final RapidEnvInterpreter interpreter = RapidEnvInterpreter
 				.getInstance();
 
 		String msgcheck = "shell link icon \"" + title + "\" is up to date.";
 
 		if (!shellLinkFile.exists()) {
 			ok = false;
 			msgcheck = "shell link icon \"" + title + "\" needs to be created"
 					+ " in folder \"" + shellLinkFolder.getAbsolutePath()
 					+ "\".";
 		}
 
 		if (ok) {
 			shellLink.load();
 
 			final String newExe = executable.getAbsolutePath();
 			final String oldExe = shellLink.getTargetPath().getAbsolutePath();
 			if (!newExe.equals(oldExe)) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Executable has changed\n" + "      from \""
 						+ oldExe + "\"\n" + "        to \"" + newExe + "\"";
 			}
 		}
 
 		int oldArgumentsCount = 0;
 		final int newArgumentsCount = arguments.size();
 		final String newArgs = argsToString(arguments);
 		String oldArgs = "";
 		if (shellLink != null && shellLink.getArguments() != null) {
 			oldArgs = argsToString(shellLink.getArguments());
 			oldArgumentsCount = shellLink.getArguments().size();
 		}
 
 		if (ok) {
			if (newArgumentsCount != shellLink.getArguments().size()) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Arguments count has changed\n" + "      from \""
 						+ Integer.toString(oldArgumentsCount) + "\"\n"
 						+ "        to \"" + Integer.toString(newArgumentsCount)
 						+ "\"\n" + "      old arguments: " + oldArgs + "\n"
 						+ "      new arguments: " + newArgs;
 			}
 		}
 		if (ok) {
 			for (int i = 0; ok && i < newArgumentsCount; i++) {
 				final String newArg = arguments.get(i).getValue();
 				final String oldArg = shellLink.getArguments().get(i)
 						.getValue();
 				if (!newArg.equals(oldArg)) {
 					ok = false;
 					msgcheck = "shell link icon \"" + title
 							+ "\" needs to be updated" + " in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".\n"
 							+ "    Argument number " + Integer.toString(i + 1)
 							+ " has changed\n" + "      from \"" + oldArg
 							+ "\"\n" + "        to \"" + newArg + "\"";
 				}
 				if (ok) {
 					if (arguments.get(i).getQuoted() != shellLink
 							.getArguments().get(i).getQuoted()) {
 						ok = false;
 						msgcheck = "shell link icon \"" + title
 								+ "\" needs to be updated" + " in folder \""
 								+ shellLinkFolder.getAbsolutePath() + "\".\n"
 								+ "    Argument number "
 								+ Integer.toString(i + 1)
 								+ " has changed it's quoting.\n";
 					}
 				}
 			}
 		}
 		if (ok) {
 			final String oldWd = shellLink.getWorkingDirectory()
 					.getAbsolutePath();
 			final String newWd = executeInFolder.getAbsolutePath();
 			if (!newWd.equals(oldWd)) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Working directory has changed\n"
 						+ "      from \"" + oldWd + "\"\n" + "        to \""
 						+ newWd + "\"";
 			}
 		}
 		if (ok) {
 			final String oldIconFilePath = shellLink.getIconFile()
 					.getAbsolutePath();
 			final String newIconFilePath = iconFile.getAbsolutePath();
 			if (!newIconFilePath.equals(oldIconFilePath)) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Icon file has changed\n" + "    from \""
 						+ oldIconFilePath + "\"\n" + "      to \""
 						+ newIconFilePath + "\"";
 			}
 		}
 		if (ok) {
 			if (iconFile.lastModified() > shellLinkFile.lastModified()) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Icon file is newer";
 			}
 		}
 		if (execute) {
 			if (ok) {
 				RapidEnvInterpreter.log(Level.FINE, msgcheck);
 			} else {
 				try {
 					String crorud = "updated";
 					if (!shellLinkFile.exists()) {
 						if (!shellLinkFile.getParentFile().exists()) {
 							FileHelper.mkdirs(shellLinkFile.getParentFile());
 						}
 						crorud = "created";
 					}
 					shellLink.setTargetPath(executable);
 					shellLink.setArguments(arguments);
 					shellLink.setWorkingDirectory(executeInFolder);
 					shellLink.setIconFile(iconFile);
 					shellLink.save();
 					String msg = "    shell link icon \"" + title
 							+ "\" has been " + crorud + " successfully"
 							+ " in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".";
 					if (interpreter != null) {
 						interpreter.getOut().println(msg);
 						RapidEnvInterpreter.log(Level.FINE, msg);
 					}
 					ok = true;
 				} catch (RuntimeException e) {
 					ok = false;
 					if (interpreter != null) {
 						RapidEnvInterpreter.log(Level.INFO, e.getMessage());
 					}
 					throw e;
 				}
 			}
 		} else {
 			if (!ok) {
 				setIssue(msgcheck);
 			}
 			if (interpreter != null) {
 				RapidEnvInterpreter.log(Level.FINE, msgcheck);
 			}
 		}
 		return ok;
 	}
 
 	private File getExecutableAsFile() {
 		final String pathextension = getExecutein();
 		return SystemCommand.getExecutableAsFileStat(getExecutable(),
 				pathextension);
 	}
 
 	/**
 	 * Creates a single String out of a list of arguments.
 	 * 
 	 * @param args
 	 * 
 	 * @return
 	 */
 	private String argsToString(final List<Argument> args) {
 		final StringBuilder sb = new StringBuilder();
 		int i = 0;
 		for (final Argument arg : args) {
 			if (i > 0) {
 				sb.append(' ');
 			}
 			final boolean quoted = arg.getQuoted();
 			if (quoted) {
 				sb.append('"');
 			}
 			sb.append(arg.getValue());
 			if (quoted) {
 				sb.append('"');
 			}
 			i++;
 		}
 		return sb.toString();
 	}
 
 	private boolean checkShellLinkLinux(final boolean execute,
 			final String title, final File shellLinkFolder,
 			final File executeInFolder, final File executable,
 			final List<Argument> arguments) {
 		boolean ok = true;
 		final File shellLinkFile = new File(shellLinkFolder, title + ".desktop");
 		final ConfigFileEditorProperties shellLink = new ConfigFileEditorProperties(
 				new ConfigFileProperties(), shellLinkFile);
 		final File iconFile = new File(getIconfile());
 		final RapidEnvInterpreter interpreter = RapidEnvInterpreter
 				.getInstance();
 		final String args = this.argumentsToString(arguments);
 
 		String msgcheck = "shell link icon \"" + title + "\" is up to date.";
 		if (!shellLinkFile.exists()) {
 			if (execute) {
 				shellLink.setCreateIfNotExists(true);
 			} else {
 				ok = false;
 			}
 			msgcheck = "shell link icon \"" + title + "\" needs to be created"
 					+ " in folder \"" + shellLinkFolder.getAbsolutePath()
 					+ "\".";
 		}
 		if (ok) {
 			shellLink.load();
 			if (shellLink.getProperty("Type") == null
 					|| (!shellLink.getProperty("Type").equals("Application"))) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Type is different from \"Application\"";
 			}
 		}
 		if (ok) {
 			if (!title.equals(shellLink.getProperty("Name"))) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Title has changed\n" + "      from \""
 						+ shellLink.getProperty("Name") + "\"\n"
 						+ "        to \"" + title + "\"";
 			}
 		}
 		if (ok) {
 			if (!(executable.getAbsolutePath() + " " + args).equals(shellLink
 					.getProperty("Exec"))) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Executable has changed\n" + "      from \""
 						+ shellLink.getProperty("Exec") + "\"\n"
 						+ "        to \"" + executable.getAbsolutePath() + " "
 						+ args + "\"";
 			}
 		}
 		if (ok) {
 			if (!executeInFolder.getAbsolutePath().equals(
 					shellLink.getProperty("Path"))) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Working directory has changed\n"
 						+ "      from \"" + shellLink.getProperty("Path")
 						+ "\"\n" + "        to \""
 						+ executeInFolder.getAbsolutePath() + "\"";
 			}
 		}
 		if (ok) {
 			if (!iconFile.getAbsolutePath().equals(
 					shellLink.getProperty("Icon"))) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Icon file has changed";
 			}
 		}
 		if (ok) {
 			if (iconFile.lastModified() > shellLinkFile.lastModified()) {
 				ok = false;
 				msgcheck = "shell link icon \"" + title
 						+ "\" needs to be updated" + " in folder \""
 						+ shellLinkFolder.getAbsolutePath() + "\".\n"
 						+ "    Icon file is newer";
 			}
 		}
 		if (execute) {
 			if (ok) {
 				RapidEnvInterpreter.log(Level.FINE, msgcheck);
 			} else {
 				try {
 					String crorud = "updated";
 					if (!shellLinkFile.exists()) {
 						if (!shellLinkFile.getParentFile().exists()) {
 							FileHelper.mkdirs(shellLinkFile.getParentFile());
 						}
 						crorud = "created";
 					}
 					shellLink.setProperty("[Desktop Entry]", "Exec",
 							executable.getAbsolutePath() + " " + args);
 					shellLink.setProperty("[Desktop Entry]", "Icon",
 							iconFile.getAbsolutePath());
 					shellLink.setProperty("[Desktop Entry]", "Name", title);
 					shellLink.setProperty("[Desktop Entry]", "Path",
 							executeInFolder.getAbsolutePath());
 					shellLink.setProperty("[Desktop Entry]", "Type",
 							"Application");
 					shellLink.save();
 					String msg = "    shell link icon \"" + title
 							+ "\" has been " + crorud + " successfully"
 							+ " in folder \""
 							+ shellLinkFolder.getAbsolutePath() + "\".";
 					interpreter.getOut().println(msg);
 					RapidEnvInterpreter.log(Level.FINE, msg);
 					ok = true;
 				} catch (RuntimeException e) {
 					ok = false;
 					RapidEnvInterpreter.log(Level.INFO, e.getMessage());
 					throw e;
 				}
 			}
 		} else {
 			if (!ok) {
 				setIssue(msgcheck);
 			}
 			RapidEnvInterpreter.log(Level.FINE, msgcheck);
 		}
 		if (!shellLinkFile.canExecute()) {
 			if (!shellLinkFile.setExecutable(true)) {
 				throw new RapidEnvException(
 						"Could not set execution rights for shell link file \""
 								+ shellLinkFile.getAbsolutePath() + "\".");
 			}
 		}
 		return ok;
 	}
 
 	/**
 	 * default constructor.
 	 */
 	public ShellLinkIcon() {
 		super();
 	}
 
 	/**
 	 * constructor out of a string.
 	 * 
 	 * @param s
 	 *            the string
 	 */
 	public ShellLinkIcon(final String s) {
 		super(s);
 	}
 
 	/**
 	 * constructor out of a string array.
 	 * 
 	 * @param sa
 	 *            the string array
 	 */
 	public ShellLinkIcon(final String[] sa) {
 		super(sa);
 	}
 
 	/**
 	 * the bean's type (class variable).
 	 */
 	private static TypeRapidBean type = TypeRapidBean
 			.createInstance(ShellLinkIcon.class);
 
 	/**
 	 * @return the RapidBean's type
 	 */
 	@Override
 	public TypeRapidBean getType() {
 		return type;
 	}
 
 	@Override
 	public String print() {
 		return this.getExecutable();
 	}
 }
