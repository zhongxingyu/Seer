 /*
  * RapidEnv: ConfigFile.java
  *
  * Copyright (C) 2010 Martin Bluemel
  *
  * Creation Date: 09/08/2010
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
 
 package org.rapidbeans.rapidenv.config.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 
 import org.rapidbeans.core.common.ReadonlyListCollection;
 import org.rapidbeans.core.exception.RapidBeansRuntimeException;
 import org.rapidbeans.core.type.TypeRapidBean;
 import org.rapidbeans.core.util.FileHelper;
 import org.rapidbeans.core.util.OperatingSystemFamily;
 import org.rapidbeans.core.util.PlatformHelper;
 import org.rapidbeans.rapidenv.RapidEnvException;
 import org.rapidbeans.rapidenv.RapidEnvInterpreter;
 import org.rapidbeans.rapidenv.config.ConfigurationTask;
 import org.rapidbeans.rapidenv.config.Installunit;
 import org.rapidbeans.rapidenv.config.RapidEnvConfigurationException;
 import org.rapidbeans.rapidenv.config.expr.ConfigExprTopLevel;
 
 /**
  * The file to configure in general.
  */
 public abstract class ConfigFile extends RapidBeanBaseConfigFile {
 
 	private ConfigFileEditor editor = null;
 
 	/**
 	 * @return the editor
 	 */
 	public ConfigFileEditor getEditor() {
 		return editor;
 	}
 
 	/**
 	 * Creates a configuration file editor used for automatic changes
 	 * 
 	 * @param cfgFile
 	 *            the parent file configuration
 	 * 
 	 * @return the configuration file editor
 	 */
 	public abstract ConfigFileEditor createEditor(ConfigFile cfgFile, File file);
 
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
 		final URL url = getSourceurlAsUrl();
 		File sourcefile = null;
 		final File targetfile = getPathAsFile();
 		final RapidEnvInterpreter interpreter = RapidEnvInterpreter.getInstance();
 		boolean configured = false;
 
 		if (targetfile.exists()
 		        && getCopycondition().equals(EnumFileCopyCondition.delete)) {
 			if (execute) {
 				String msg = "    deleted ";
 				if (targetfile.isDirectory()) {
 					msg += "directory";
 				} else {
 					msg += "file";
 				}
 				msg += " " + targetfile.getAbsolutePath();
 				if (!targetfile.delete()) {
 					throw new RapidEnvException("Problems to delete file / directory \""
 					        + targetfile.getAbsolutePath()
 					        + "\".");
 				}
 				interpreter.getOut().println(msg);
 				configured = true;
 			} else {
 				String msg = null;
 				if (targetfile.isDirectory()) {
 					msg = "Directory";
 				} else {
 					msg = "File";
 				}
 				msg += " \"" + targetfile.getAbsolutePath()
 				        + "\" should be deleted.";
 				RapidEnvInterpreter.log(Level.FINE, msg);
 				setIssue(msg);
 				return false;
 			}
 		} else {
 			if (getSourceurl() != null) {
 				RapidEnvInterpreter.log(Level.FINER,
 				        "general check of \"" + this.getClass().getName()
 				                + "\"\n  src = \"" + url.toString()
 				                + "\"\n  tgt = \""
 				                + targetfile.getAbsolutePath() + "\"...");
 				if (url.getProtocol().equals("file")) {
 					sourcefile = new File(url.getFile());
 					if (!sourcefile.exists()) {
 						throw new RapidEnvConfigurationException("File \"" + sourcefile.getAbsolutePath()
 						        + "\" does not exist.");
 					}
 					if (!targetfile.exists()) {
 						if (execute) {
 							if (!targetfile.getParentFile().exists()) {
 								FileHelper.mkdirs(targetfile.getParentFile());
 								final String msg = "    created directory "
 								        + targetfile.getParentFile().getAbsolutePath()
 								        + ".";
 								interpreter.getOut().println(msg);
 								configured = true;
 							}
 							FileHelper.copyFile(sourcefile, targetfile);
 							final String msg = "    copied " + sourcefile.getAbsolutePath() + " to file "
 							        + targetfile.getAbsolutePath();
 							interpreter.getOut().println(msg);
 							configured = true;
 						} else {
 							final String msg = "File to configure \"" + targetfile.getAbsolutePath()
 							        + "\" does not exist.";
 							RapidEnvInterpreter.log(Level.FINE, msg);
 							setIssue(msg);
 							return false;
 						}
 					}
 				} else {
 					throw new RapidEnvConfigurationException("Protocol different from file not supported"
 					        + "for file configuration property \"sourceurl\".");
 				}
 			} else {
 				RapidEnvInterpreter.log(Level.FINER,
 				        "general check of \"" + this.getClass().getName()
 				                + "\"\n  tgt = \"" + targetfile.getAbsolutePath() + "\"...");
 				if (!targetfile.exists()) {
 					if (execute) {
 						try {
 							if (!targetfile.getParentFile().exists()) {
 								FileHelper.mkdirs(targetfile.getParentFile());
 							}
 							createNewFile(targetfile);
 						} catch (IOException e) {
 							throw new RapidBeansRuntimeException(e);
 						}
 						final String msg = "    created new configuration file " + targetfile.getAbsolutePath();
 						interpreter.getOut().println(msg);
 						configured = true;
 					} else {
 						final String msg = "File to configure \"" + targetfile.getAbsolutePath() + "\" does not exist.";
 						RapidEnvInterpreter.log(Level.FINE, msg);
 						setIssue(msg);
 						return false;
 					}
 				}
 			}
 
 			if (sourcefile != null) {
 				switch (getCopycondition()) {
 
 				case sourcenewer:
 					if (sourcefile.lastModified() > targetfile.lastModified()) {
 						if (execute) {
 							FileHelper.copyFile(sourcefile, targetfile);
 							final String msg = "    copied " + sourcefile.getAbsolutePath() + " over outdated file "
 							        + targetfile.getAbsolutePath();
 							RapidEnvInterpreter.log(Level.FINE, msg);
 							interpreter.getOut().println(msg);
 							configured = true;
 						} else {
 							final String msg = "File to configure \"" + targetfile.getAbsolutePath()
 							        + "\" is not up to date.";
 							RapidEnvInterpreter.log(Level.FINE, msg);
 							setIssue(msg);
 							return false;
 						}
 					}
 					break;
 
 				case diff:
 					boolean equal = false;
 
 					if (this.getTasks() != null && this.getTasks().size() > 0) {
 						File tmpfile;
 						try {
 							tmpfile = File.createTempFile("rapidEnvCfgFile", ".tmp");
 						} catch (IOException e) {
 							throw new RapidEnvException(e);
 						}
 						FileHelper.copyFile(sourcefile, tmpfile, true);
 						this.editor = createEditor(this, tmpfile);
 						for (final ConfigurationTask cfgTask : this.getTasks()) {
 							if (!cfgTask.check(true, true)) {
 								throw new RapidEnvException("Problem to apply configuration " + cfgTask.toString());
 							}
 						}
 						this.editor.save();
 						this.editor = null;
 						equal = FileHelper.filesEqual(tmpfile, targetfile, true, true);
 						if (!tmpfile.delete()) {
 							throw new RapidEnvException("Problems while trying to delete " + tmpfile.getAbsolutePath());
 						}
 					} else {
						equal = FileHelper.filesEqual(sourcefile, targetfile, true, false);
 					}
 
 					if (!equal) {
 						if (execute) {
 							FileHelper.copyFile(sourcefile, targetfile, true);
 							final String msg = "    copied " + sourcefile.getAbsolutePath() + " over different file "
 							        + targetfile.getAbsolutePath();
 							RapidEnvInterpreter.log(Level.FINE, msg);
 							interpreter.getOut().println(msg);
 							configured = true;
 						} else {
 							String msg;
 							if (this.getTasks() != null && this.getTasks().size() > 0) {
 								msg = "File to configure \"" + targetfile.getAbsolutePath()
 								        + "\" differs from sourcefile \""
 								        + sourcefile.getAbsolutePath() + "\" including changes.";
 							} else {
 								msg = "File to configure \"" + targetfile.getAbsolutePath()
 								        + "\" differs from sourcefile \""
 								        + sourcefile.getAbsolutePath() + "\".";
 							}
 							RapidEnvInterpreter.log(Level.FINE, msg);
 							setIssue(msg);
 							return false;
 						}
 						break;
 					}
 				}
 			}
 
 			if (getCanread() && (!targetfile.canRead())) {
 				if (execute) {
 					interpreter.getOut().println(
 					        "Add read rights to " + "file to configure \"" + targetfile.getAbsolutePath() + "\".");
 					if (!targetfile.setReadable(true)) {
 						throw new RapidEnvException("Adding read rights" + " to configuration file \""
 						        + targetfile.getAbsolutePath() + "\" failed.");
 					} else {
 						configured = true;
 					}
 				} else {
 					final String msg = "File to configure \"" + targetfile.getAbsolutePath() + "\" is not readable.";
 					RapidEnvInterpreter.log(Level.FINE, msg);
 					setIssue(msg);
 					return false;
 				}
 			}
 			if (getCanwrite() && (!targetfile.canWrite())) {
 				if (execute) {
 					interpreter.getOut().println(
 					        "Add write rights to " + "file to configure \"" + targetfile.getAbsolutePath() + "\".");
 					if (!targetfile.setWritable(true)) {
 						throw new RapidEnvException("Adding write rights" + " to configuration file \""
 						        + targetfile.getAbsolutePath() + "\" failed.");
 					} else {
 						configured = true;
 					}
 				} else {
 					final String msg = "File to configure \"" + targetfile.getAbsolutePath() + "\" is not writeable.";
 					RapidEnvInterpreter.log(Level.FINE, msg);
 					setIssue(msg);
 					return false;
 				}
 			}
 			if (getCanexecute() && (!targetfile.canExecute())) {
 				if (execute) {
 					interpreter.getOut().println(
 					        "Add execution rights to " + "file to configure \"" + targetfile.getAbsolutePath() + "\".");
 					if (!targetfile.setExecutable(true)) {
 						throw new RapidEnvException("Adding execution rights" + " to configuration file \""
 						        + targetfile.getAbsolutePath() + "\" failed.");
 					} else {
 						configured = true;
 					}
 				} else {
 					final String msg = "File to configure \"" + targetfile.getAbsolutePath() + "\" is not executeable.";
 					RapidEnvInterpreter.log(Level.FINE, msg);
 					setIssue(msg);
 					return false;
 				}
 			}
 			if (!getCanread() && targetfile.canRead()) {
 				if (execute) {
 					interpreter.getOut().println(
 					        "Remove read rights from " + "file to configure \"" + targetfile.getAbsolutePath() + "\".");
 					if (!targetfile.setReadable(false)) {
 						throw new RapidEnvException("Withdrawing read rights" + " from configuration file \""
 						        + targetfile.getAbsolutePath() + "\" failed.");
 					} else {
 						configured = true;
 					}
 				} else {
 					final String msg = "File to configure \"" + targetfile.getAbsolutePath()
 					        + "\" is readable but should not be.";
 					RapidEnvInterpreter.log(Level.FINE, msg);
 					setIssue(msg);
 					return false;
 				}
 			}
 			if (!getCanwrite() && targetfile.canWrite()) {
 				if (execute) {
 					interpreter.getOut()
 					        .println(
 					                "Remove write rights from " + "file to configure \"" + targetfile.getAbsolutePath()
 					                        + "\".");
 					if (!targetfile.setWritable(false)) {
 						throw new RapidEnvException("Withdrawing write rights" + " from configuration file \""
 						        + targetfile.getAbsolutePath() + "\" failed.");
 					} else {
 						configured = true;
 					}
 				} else {
 					final String msg = "File to configure \"" + targetfile.getAbsolutePath()
 					        + "\" is writeable but should not be.";
 					RapidEnvInterpreter.log(Level.FINE, msg);
 					setIssue(msg);
 					return false;
 				}
 			}
 
 			if (PlatformHelper.getOsfamily() != OperatingSystemFamily.windows) {
 				if (!getCanexecute() && targetfile.canExecute()) {
 					if (execute) {
 						interpreter.getOut().println(
 						        "Remove execution rights from " + "file to configure \"" + targetfile.getAbsolutePath()
 						                + "\".");
 						if (!targetfile.setExecutable(false)) {
 							throw new RapidEnvException("Withdrawing execution rights" + " from configuration file \""
 							        + targetfile.getAbsolutePath() + "\" failed.");
 						} else {
 							configured = true;
 						}
 					} else {
 						final String msg = "File to configure \"" + targetfile.getAbsolutePath()
 						        + "\" is executeable but should not be.";
 						RapidEnvInterpreter.log(Level.FINE, msg);
 						setIssue(msg);
 						return false;
 					}
 				}
 			}
 		}
 
 		if (this.getTasks() != null && this.getTasks().size() > 0) {
 			this.editor = createEditor(this, null);
 			final ReadonlyListCollection<ConfigurationTask> cfgTasks = this.getTasks();
 			for (final ConfigurationTask cfgTask : cfgTasks) {
 				if (!cfgTask.checkOsfamily()) {
 					continue;
 				}
 				try {
 					final boolean checkResult = cfgTask.check(execute, false);
 					if (execute) {
 						if (checkResult) {
 							configured = true;
 						}
 					} else {
 						if (!checkResult) {
 							return false;
 						}
 					}
 				} catch (RuntimeException e) {
 					String msg = "Unforeseen problem while";
 					if (execute) {
 						msg += " modifying";
 					} else {
 						msg += " checking";
 					}
 					msg += " file \"" + this.getPath() + "\"";
 					this.setOk(false);
 					this.setIssue(msg);
 					RapidEnvInterpreter.log(Level.SEVERE, msg + ":");
 					e.printStackTrace();
 					return false;
 				}
 			}
 			if (configured) {
 				this.editor.save();
 			}
 			this.editor = null;
 		}
 
 		if (configured) {
 			cleanupFilesOnConfig();
 			if (getCommandonconfig() != null) {
 				getCommandonconfig().execute();
 			}
 		}
 
 		boolean ret = false;
 		if (execute) {
 			ret = configured;
 		} else {
 			ret = true;
 			this.setOk(ret);
 		}
 		return ret;
 	}
 
 	/**
 	 * Create a new configuration file.
 	 * 
 	 * @param targetfile
 	 *            the file to create
 	 * 
 	 * @throws IOException
 	 *             in case of IO problems
 	 */
 	public void createNewFile(final File targetfile) throws IOException {
 		if (!targetfile.createNewFile()) {
 			throw new RapidBeansRuntimeException("Could not create new file \"" + targetfile.getAbsolutePath() + "\"");
 		}
 	}
 
 	public File getPathAsFile() {
 		File pathfile = null;
 		final Installunit unit = ((Installunit) getParentBean());
 		if (getPath() == null || getPath().trim().length() == 0) {
 			pathfile = new File(unit.getHomedir());
 		} else {
 			pathfile = new File(getPath());
 			if (!pathfile.isAbsolute()) {
 				pathfile = new File(unit.getHomedir(), getPath());
 			}
 		}
 		return pathfile;
 	}
 
 	/**
 	 * Tweaked getter with lazy initialization and expression interpretation.
 	 */
 	@Override
 	public synchronized String getSourceurl() {
 		if (super.getSourceurl() == null) {
 			return null;
 		}
 		String sourceurl = super.getSourceurl();
 		if (super.getSourceurl() != null) {
 			sourceurl = interpret(sourceurl);
 		}
 		return sourceurl;
 	}
 
 	public String getNewlineChars() {
 		String newlineChars = null;
 		switch (getNewline()) {
 		case platform:
 			newlineChars = PlatformHelper.getLineFeed();
 			break;
 		case lf:
 			newlineChars = "\n";
 			break;
 		case crlf:
 			newlineChars = "\r\n";
 			break;
 		default:
 			throw new RapidEnvException("Can not interpret " + getNewline().name());
 		}
 		return newlineChars;
 	}
 
 	protected URL getSourceurlAsUrl() {
 		if (getSourceurl() == null) {
 			return null;
 		}
 		try {
 			return new URL(getSourceurl());
 		} catch (MalformedURLException e) {
 			throw new RapidEnvConfigurationException(
 			        "Configuration problem of property \"sourceurl\" in installunit \""
 			                + ((Installunit) this.getParentBean()).getFullyQualifiedName() + "\""
 			                + " in file configuration with sourceurl \"" + getSourceurl() + "\"", e);
 		}
 	}
 
 	/**
 	 * Interpret a configuration expression.
 	 * 
 	 * @param expression
 	 *            the configuration expression to interpret
 	 * 
 	 * @return the interpreted (expanded) configuration expression
 	 */
 	private String interpret(final String expression) {
 		return new ConfigExprTopLevel((Installunit) this.getParentBean(), null, expression,
 		        getExpressionliteralescaping()).interpret();
 	}
 
 	/**
 	 * default constructor.
 	 */
 	public ConfigFile() {
 		super();
 	}
 
 	/**
 	 * constructor out of a string.
 	 * 
 	 * @param s
 	 *            the string
 	 */
 	public ConfigFile(final String s) {
 		super(s);
 	}
 
 	/**
 	 * constructor out of a string array.
 	 * 
 	 * @param sa
 	 *            the string array
 	 */
 	public ConfigFile(final String[] sa) {
 		super(sa);
 	}
 
 	@Override
 	public String print() {
 		return this.getPathAsFile().getAbsolutePath();
 	}
 
 	/**
 	 * the bean's type (class variable).
 	 */
 	private static TypeRapidBean type = TypeRapidBean.createInstance(ConfigFile.class);
 
 	/**
 	 * @return the RapidBean's type
 	 */
 	@Override
 	public TypeRapidBean getType() {
 		return type;
 	}
 
 	public static String limit(String string, int maxlen) {
 		if (string.length() > maxlen) {
 			return string.substring(0, maxlen - 3) + "...";
 		} else {
 			return string;
 		}
 	}
 }
