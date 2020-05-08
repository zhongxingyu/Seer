 /*******************************************************************************
  * Copyright (c) 2009-2012 WalWare/RJ-Project (www.walware.de/goto/opensource).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.rj.servi.pool;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 
 /**
  * Configuration for an R node (of pool or embedded).
  */
 public class RServiNodeConfig implements PropertiesBean {
 	
 	
 	public static final String R_HOME_ID = "r_home.path";
 	
 	public static final String R_ARCH_ID = "r_arch.code";
 	public static final String BITS_ID = "bits.num";
 	
 	public static final String JAVA_HOME_ID = "java_home.path";
 	
 	public static final String JAVA_ARGS_ID = "java_cmd.args";
 	private static final String JAVA_ARGS_OLD_ID = "java_args.path";
 	
 	public static final String NODE_ENVIRONMENT_VARIABLES_PREFIX = "node_environment.variables.";
 	public static final String NODE_ARGS_ID = "node_cmd.args";
 	
 	public static final String BASE_WD_ID = "base_wd.path";
 	
 	/**
 	 * Property id for R startup snippet
 	 * 
 	 * @see #setRStartupSnippet(String)
 	 * @since 0.5
 	 */
 	public static final String R_STARTUP_SNIPPET_ID = "r_startup.snippet";
 	
 	public static final String CONSOLE_ENABLED_ID = "debug_console.enabled";
 	
 	public static final String VERBOSE_ENABLED_ID = "debug_verbose.enabled";
 	
 	
 	private String rHome;
 	private String rArch;
 	private int bits;
 	
 	private String javaHome;
 	private String javaArgs;
 	
 	private final Map<String, String> environmentVariables = new HashMap<String, String>();
 	private String nodeArgs;
 	
 	private String baseWd;
 	
 	private String rStartupSnippet;
 	
 	private boolean enableConsole;
 	private boolean enableVerbose;
 	
 	
 	public RServiNodeConfig() {
 		this.rHome = System.getenv("R_HOME");
 		this.rArch = System.getenv("R_ARCH");
 		this.bits = 64;
 		this.javaArgs = "-server";
 		this.nodeArgs = "";
 		this.rStartupSnippet = "";
 	}
 	
 	
 	public String getBeanId() {
 		return "rconfig";
 	}
 	
 	protected void load(final RServiNodeConfig templ) {
 		this.rHome = templ.rHome;
 		this.rArch = templ.rArch;
 		this.bits = templ.bits;
 		this.javaHome = templ.javaHome;
 		this.javaArgs = templ.javaArgs;
		this.environmentVariables.clear();
 		this.environmentVariables.putAll(templ.environmentVariables);
 		this.nodeArgs = templ.nodeArgs;
 		this.baseWd = templ.baseWd;
 		this.rStartupSnippet = templ.rStartupSnippet;
 		this.enableConsole = templ.enableConsole;
 		this.enableVerbose = templ.enableVerbose;
 	}
 	
 	public void load(final Properties map) {
 		setRHome(map.getProperty(R_HOME_ID));
 		setRArch(map.getProperty(R_ARCH_ID));
 		setBits(Integer.parseInt(map.getProperty(BITS_ID, "64")));
 		setJavaHome(map.getProperty(JAVA_HOME_ID));
 		setJavaArgs(map.getProperty(JAVA_ARGS_ID));
 		if (this.javaArgs.length() == 0) {
 			setJavaArgs(map.getProperty(JAVA_ARGS_OLD_ID));
 		}
 		this.environmentVariables.clear();
 		final int prefixLength = NODE_ENVIRONMENT_VARIABLES_PREFIX.length();
 		for (final Entry<Object, Object> p : map.entrySet()) {
 			final String name = (String) p.getKey();
 			if (name != null && name.length() > prefixLength
 					&& name.startsWith(NODE_ENVIRONMENT_VARIABLES_PREFIX)
 					&& p.getValue() instanceof String) {
 				this.environmentVariables.put(name.substring(prefixLength), (String) p.getValue());
 			}
 		}
 		setNodeArgs(map.getProperty(NODE_ARGS_ID));
 		setBaseWorkingDirectory(map.getProperty(BASE_WD_ID));
 		setRStartupSnippet(map.getProperty(R_STARTUP_SNIPPET_ID));
 		setEnableConsole(Boolean.parseBoolean(map.getProperty(CONSOLE_ENABLED_ID)));
 		setEnableVerbose(Boolean.parseBoolean(map.getProperty(VERBOSE_ENABLED_ID)));
 	}
 	
 	public void save(final Properties map) {
 		map.setProperty(R_HOME_ID, this.rHome);
 		map.setProperty(R_ARCH_ID, this.rArch);
 		map.setProperty(BITS_ID, Integer.toString(this.bits));
 		map.setProperty(JAVA_HOME_ID, (this.javaHome != null) ? this.javaHome : "");
 		map.setProperty(JAVA_ARGS_ID, this.javaArgs);
 		for (final Entry<String, String> variable : this.environmentVariables.entrySet()) {
 			map.setProperty(NODE_ENVIRONMENT_VARIABLES_PREFIX + variable.getKey(), variable.getValue());
 		}
 		map.setProperty(NODE_ARGS_ID, this.nodeArgs);
 		map.setProperty(BASE_WD_ID, (this.baseWd != null) ? this.baseWd : "");
 		map.setProperty(R_STARTUP_SNIPPET_ID, this.rStartupSnippet);
 		map.setProperty(CONSOLE_ENABLED_ID, Boolean.toString(this.enableConsole));
 		map.setProperty(VERBOSE_ENABLED_ID, Boolean.toString(this.enableVerbose));
 	}
 	
 	public void setRHome(final String path) {
 		this.rHome = path;
 	}
 	
 	public String getRHome() {
 		return this.rHome;
 	}
 	
 	public void setRArch(final String code) {
 		this.rArch = code;
 	}
 	
 	public String getRArch() {
 		return this.rArch;
 	}
 	
 	@Deprecated
 	public void setBits(final int bits) {
 		this.bits = bits;
 	}
 	
 	@Deprecated
 	public int getBits() {
 		return this.bits;
 	}
 	
 	public String getJavaHome() {
 		return this.javaHome;
 	}
 	
 	public void setJavaHome(final String javaHome) {
 		this.javaHome = (javaHome != null && javaHome.trim().length() > 0) ? javaHome : null;
 	}
 	
 	public String getJavaArgs() {
 		return this.javaArgs;
 	}
 	
 	public void setJavaArgs(final String args) {
 		this.javaArgs = (args != null) ? args : "";
 	}
 	
 	/**
 	 * Additional environment variables for the R process.
 	 * 
 	 * @return a name - value map of the environment variables
 	 */
 	public Map<String, String> getEnvironmentVariables() {
 		return this.environmentVariables;
 	}
 	
 	public void addToClasspath(final String entry) {
 		String cp = this.environmentVariables.get("CLASSPATH");
 		if (cp != null) {
 			cp += File.pathSeparatorChar + entry;
 		}
 		else {
 			cp = entry;
 		}
 		this.environmentVariables.put("CLASSPATH", cp);
 	}
 	
 	public String getNodeArgs() {
 		return this.nodeArgs;
 	}
 	
 	public void setNodeArgs(final String args) {
 		this.nodeArgs = (args != null) ? args : "";
 	}
 	
 	public void setBaseWorkingDirectory(final String path) {
 		this.baseWd = (path != null && path.trim().length() > 0) ? path : null;
 	}
 	
 	public String getBaseWorkingDirectory() {
 		return this.baseWd;
 	}
 	
 	/**
 	 * Returns the R code snippet to run at startup of a node.
 	 * 
 	 * @see #setRStartupSnippet(String)
 	 * @since 0.5
 	 */
 	public String getRStartupSnippet() {
 		return this.rStartupSnippet;
 	}
 	
 	/**
 	 * Sets the R code snippet to run at startup of a node.
 	 * <p>
 	 * Typical use case is to load required R packages. The default is an empty snippet.
 	 * If the execution of the code throws an error, the startup of the node is canceled.</p>
 	 * 
 	 * @param code the R code to run
 	 * @since 0.5
 	 */
 	public void setRStartupSnippet(final String code) {
 		this.rStartupSnippet = (code != null) ? code : "";
 	}
 	
 	public boolean getEnableConsole() {
 		return this.enableConsole;
 	}
 	
 	public void setEnableConsole(final boolean enable) {
 		this.enableConsole = enable;
 	}
 	
 	public boolean getEnableVerbose() {
 		return this.enableVerbose;
 	}
 	
 	public void setEnableVerbose(final boolean enable) {
 		this.enableVerbose = enable;
 	}
 	
 }
