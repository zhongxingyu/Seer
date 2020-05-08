 /*******************************************************************************
  * Copyright (c) May 16, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jgit.api.CloneCommand;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.JGitInternalException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.eclipse.jgit.lib.TextProgressMonitor;
 import org.zend.sdkcli.GitHelper;
 import org.zend.sdkcli.internal.mapping.CliMappingLoader;
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.application.ZendProject;
 import org.zend.sdklib.application.ZendProject.TemplateApplications;
import org.zend.sdklib.project.DeploymentScriptTypes;
 
 /**
  * Represents create-project command. In the result of calling it new PHP
  * project is created in defined location or a current location.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class CreateProjectCommand extends AbstractCommand {
 
 	public static final String NAME = "n";
 	public static final String SCRIPTS = "s";
 	public static final String DESTINATION = "d";
 	public static final String TEMPLATE = "t";
 
 	/**
 	 * @return The project destination
 	 */
 	@Option(opt = DESTINATION, required = false, description = "Project destination", argName = "path")
 	public File getDestination() {
 		final String value = getValue(DESTINATION);
 		return value == null ? resolveDestination(getCurrentDirectory(),
 				getName()) : new File(value);
 	}
 
 	@Option(opt = SCRIPTS, required = false, description = "Generate deployment scripts, "
 			+ "consider using one of these options [all|postActivate|postDeactivate|postStage|postUnstage|preActivate|preDeactivate|preStage|preUnstage]")
 	public String getScripts() {
 		return getValue(SCRIPTS);
 	}
 
 	/**
 	 * @return The project name
 	 */
 	@Option(opt = NAME, required = true, description = "Project name", argName = "name")
 	public String getName() {
 		return getValue(NAME);
 	}
 
 	/**
 	 * @return The project name
 	 */
 	@Option(opt = TEMPLATE, required = false, description = "Template name (zend | zf2 | quickstart | simple). Default is zend application", argName = "name")
 	public TemplateApplications getTemplate() {
 		TemplateApplications result = TemplateApplications.getDefault();
 		final String value = getValue(TEMPLATE);
 		if (value != null) {
 			try {
 				result = TemplateApplications.valueOf(value.toUpperCase());
 			} catch (Exception e) {
 				return null;
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public boolean doExecute() {
 		File destinationFolder = getDestination();
 		if (doOverwite(destinationFolder)) {
 			ZendProject project = new ZendProject(getDestination(),
 					new CliMappingLoader());
 			TemplateApplications template = getTemplate();
 			if (template == null) {
				getLogger()
						.error(MessageFormat
								.format("Cannot create a project. \"{0}\" is not a valid template name",
										getValue(TEMPLATE)));
 				return false;
 			}
			String scripts = getScripts();
			if (scripts != null) {
				String[] scriptsNames = scripts.split(":");
				for (String scriptName : scriptsNames) {
					final DeploymentScriptTypes n = DeploymentScriptTypes
							.byName(scriptName.trim());
					if (n == null) {
						getLogger()
								.error(MessageFormat
										.format("Cannot create a project. Script with name {0} cannot be found",
												scriptName));
						return false;
					}
				}
			}
 			boolean create = false;
 			if (template == TemplateApplications.ZF2) {
 				// clone zf2 skeleton application
 				create = cloneRepository(template.getBasePath(),
 						destinationFolder);
 				if (create) {
 					cloneGitModules(destinationFolder);
					project.update(scripts);
 				}
 			} else {
				create = project.create(getName(), getTemplate(), scripts);
 			}
 			if (create) {
 				getLogger().info(
 						"Project resources were created successfully for "
 								+ getName() + " under " + getDestination());
 			}
 			return create;
 		}
 		return false;
 	}
 
 	public boolean cloneRepository(String repo, File dir) {
 		CloneCommand clone = new CloneCommand();
 		clone.setURI(repo);
 		clone.setRemote(GitHelper.getRemote(repo));
 		clone.setDirectory(dir);
 		clone.setProgressMonitor(new TextProgressMonitor(new PrintWriter(
 				System.out)));
 		getLogger().info(
 				MessageFormat.format("Cloning into {0}...", dir.getName()));
 		try {
 			clone.call();
 		} catch (JGitInternalException e) {
 			delete(dir);
 			getLogger().error(e);
 			return false;
 		} catch (InvalidRemoteException e) {
 			delete(dir);
 			getLogger().error(e);
 			return false;
 		} catch (TransportException e) {
 			delete(dir);
 			getLogger().error(e);
 			return false;
 		} catch (GitAPIException e) {
 			delete(dir);
 			getLogger().error(e);
 			return false;
 		}
 		return true;
 	}
 
 	private void cloneGitModules(File destinationFolder) {
 		File gitModules = new File(destinationFolder, ".gitmodules");
 		if (gitModules.exists()) {
 			Map<String, String> modules = parseGitModules(gitModules);
 			Set<String> paths = modules.keySet();
 			for (String path : paths) {
 				String url = modules.get(path);
 				File destinationPath = new File(destinationFolder, path);
 				cloneRepository(url, destinationPath);
 				cloneGitModules(destinationPath);
 			}
 		}
 	}
 
 	private Map<String, String> parseGitModules(File file) {
 		Map<String, String> modules = new HashMap<String, String>();
 		List<String> lines = null;
 		try {
 			lines = readGitModules(file);
 		} catch (IOException e) {
 			getLogger().error(
 					MessageFormat.format(
 							"Error during parsing Git modules from {0}",
 							file.getAbsolutePath()));
 		}
 		if (lines != null) {
 			String path = null;
 			String url = null;
 			for (String line : lines) {
 				if (line.startsWith("url")) {
 					url = getEntryValue(line);
 				} else if (line.startsWith("path")) {
 					path = getEntryValue(line);
 				}
 				if (url != null && path != null) {
 					modules.put(path, url);
 					path = null;
 					url = null;
 				}
 			}
 		}
 		return modules;
 	}
 
 	private List<String> readGitModules(File file) throws IOException {
 		List<String> lines = new ArrayList<String>();
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				lines.add(line.trim());
 			}
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return lines;
 	}
 
 	private String getEntryValue(String entry) {
 		String[] parts = entry.split("=");
 		if (parts.length == 2) {
 			return parts[1].trim();
 		}
 		return null;
 	}
 
 	/**
 	 * If nest is on, resolve the nested destination folder
 	 * 
 	 * @param destination2
 	 * @param nest2
 	 * @return
 	 */
 	private File resolveDestination(String destination, String name) {
 		File projectRoot = new File(destination, name);
 		/*
 		 * if (!projectRoot.exists()) { final boolean mkdir =
 		 * projectRoot.mkdir(); if (!mkdir) { return null; } }
 		 */
 		return projectRoot;
 	}
 
 	private boolean doOverwite(File destination) {
 		if (destination != null && destination.exists()) {
 			while (true) {
 				String question = "File "
 						+ destination.getAbsolutePath()
 						+ " already exists. Do you want to overwrite it [y:yes][n:no]?: ";
 				String answer = String.valueOf(System.console().readLine(
 						question));
 				if ("y".equalsIgnoreCase(answer)) {
 					delete(destination);
 					return true;
 				} else if ("n".equalsIgnoreCase(answer)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean delete(File file) {
 		if (file.isDirectory()) {
 			String[] children = file.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean result = delete(new File(file, children[i]));
 				if (!result) {
 					return false;
 				}
 			}
 		}
 		return file.delete();
 	}
 
 }
