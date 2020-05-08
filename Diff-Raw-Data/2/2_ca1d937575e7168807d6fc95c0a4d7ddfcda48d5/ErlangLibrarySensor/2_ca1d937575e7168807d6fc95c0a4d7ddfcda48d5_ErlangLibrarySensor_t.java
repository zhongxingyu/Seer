 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.sensor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.database.DatabaseSession;
 import org.sonar.api.design.Dependency;
 import org.sonar.api.resources.Library;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.plugins.erlang.language.Erlang;
 
 public class ErlangLibrarySensor extends AbstractErlangSensor {
 	private static final Pattern depsDirPattern = Pattern.compile("\\{deps_dir, ?\\[.*?\\]\\}\\.", Pattern.DOTALL
 			+ Pattern.MULTILINE);
 	private static final Pattern allDepPattern = Pattern.compile("\\{deps, ?\\[.*?\\]\\}\\.", Pattern.DOTALL
 			+ Pattern.MULTILINE);
 	private static final Pattern oneDepPattern = Pattern.compile("\\{[^\\[]+?\\}", Pattern.DOTALL + Pattern.MULTILINE);
	private static final Pattern depNamePattern = Pattern.compile("(^\\{)([A-Za-z_0-9]*?)(\\,.*)", Pattern.DOTALL
 			+ Pattern.MULTILINE);
 	private static final Pattern depVersionInTagPattern = Pattern.compile("(.*tag.*?\\\")(.*?)(\\\".*)", Pattern.DOTALL
 			+ Pattern.MULTILINE);
 	private static final Pattern depVersionInBranchPattern = Pattern.compile("(.*branch.*?\\\")(.*?)(\\\".*)",
 			Pattern.DOTALL + Pattern.MULTILINE);
 	private static final Pattern depsGetDirPattern = Pattern.compile("(\\{deps_dir, ?\\[\\\")(.*?)(\\\"\\]\\}\\.)",
 			Pattern.DOTALL + Pattern.MULTILINE);
 	
 	DatabaseSession session;
 
 	public ErlangLibrarySensor(Erlang erlang, DatabaseSession session) {
 		super(erlang);
 		this.session = session;
 	}
 
 	private final static Logger LOG = LoggerFactory.getLogger(ErlangLibrarySensor.class);
 
 	@Override
 	public void analyse(Project project, SensorContext context) {
 		analyzeRebarConfigFile(project, context, project.getFileSystem().getBasedir(), null);
 	}
 
 	private void analyzeRebarConfigFile(Resource projectResource, SensorContext context, File rebarConfigUrl,
 			Dependency parentDep) {
 		File rebarConfigFile = new File(rebarConfigUrl, erlang.getRebarConfigUrl());
 		try {
 			String rebarConfigContent = FileUtils.readFileToString(rebarConfigFile, "UTF-8");
 
 			String depsDir = getDepsDir(rebarConfigContent);
 			Matcher allDepMatcher = allDepPattern.matcher(rebarConfigContent);
 
 			while (allDepMatcher.find()) {
 				String dependencies = rebarConfigContent.substring(allDepMatcher.start(), allDepMatcher.end() - 1).replaceAll("[\\n\\r\\t ]","");
 				Matcher deps = oneDepPattern.matcher(dependencies.trim());
 				while (deps.find()) {
 					String dep = dependencies.substring(deps.start(), deps.end());
 					String name = depNamePattern.matcher(dep).replaceFirst("$2");
 					String version = depVersionInTagPattern.matcher(dep).replaceFirst("$2");
 					if (version.length() == dep.length()) {
 						version = depVersionInBranchPattern.matcher(dep).replaceFirst("$2");
 					}
 					String[] parts = dep.split(",");
 					String key = parts[3].replaceFirst("(.*:)(.*?)(\\\")", "$2").replaceAll("[\\\\/]", ":")
 							.replaceAll("\\.git", "");
 					Library dependencyProject = new Library(key, version);
 					Resource to = context.getResource(dependencyProject);
 					if (to == null) {
 						Library lib = new Library(dependencyProject.getKey(), version);
 						context.index(lib);
 						to = context.getResource(lib);
 					}
 					Dependency dependency = new Dependency(projectResource, to);
 					dependency.setUsage("compile");
 					dependency.setWeight(1);
 					context.saveDependency(dependency);
 					File depRebarConfig = new File(rebarConfigUrl.getPath().concat(File.separator + depsDir)
 							.concat(File.separator + name));
 					analyzeRebarConfigFile(to, context, depRebarConfig, dependency);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			LOG.error("Cannot open file: " + rebarConfigUrl + e);
 		} catch (IOException e) {
 			LOG.error("Cannot open file: " + rebarConfigUrl + e);
 		}
 		LOG.debug("Libraries added: " + context);
 	}
 
 	private String getDepsDir(String rebarConfigContent) {
 		// find lib dir: {lib_dirs,["deps"]}. or deps_dir?
 		Matcher depsDirMatcher = depsDirPattern.matcher(rebarConfigContent);
 		String depDir = "deps";
 		if (depsDirMatcher.matches()) {
 			depsDirMatcher.find();
 			depDir =  depsGetDirPattern.matcher(rebarConfigContent.substring(depsDirMatcher.start(), depsDirMatcher.end() - 1)).replaceAll("$2"); 
 		}
 		return depDir;
 	}
 }
