 /*******************************************************************************
  * Copyright (c) May 16, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.io.File;
import java.text.MessageFormat;
 
 import org.zend.sdkcli.internal.mapping.CliMappingLoader;
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.application.ZendProject;
 import org.zend.sdklib.application.ZendProject.TemplateApplications;
 
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
 	@Option(opt = TEMPLATE, required = false, description = "Template name (zend | quickstart | simple). Default is zend application", argName = "name")
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
				getLogger().error(
						MessageFormat.format(
								"\"{0}\" is not a valid template name",
								getValue(TEMPLATE)));
				return false;
			}
 			final boolean create = project.create(getName(), getTemplate(),
 					getScripts());
 			if (create) {
 				getLogger().info(
 						"Project resources were created successfully for "
 								+ getName() + " under " + getDestination());
 			}
 			return create;
 		}
 		return false;
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
