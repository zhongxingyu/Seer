 /**
  * Phresco Commons
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.photon.phresco.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Dependency;
 import com.phresco.pom.model.Model.Dependencies;
 import com.phresco.pom.util.PomProcessor;
 
 public class ProjectUtils implements Constants {
 	
 	private static Map<String, String> testPomFiles = new HashMap<String, String>();
 	
 	public static void writeProjectInfo(ProjectInfo info, File phrescoFolder) throws PhrescoException {
 		BufferedWriter out = null;
 		FileWriter fstream = null;
 		try {
 			// create .project file inside the .phresco folder
 			File projectFile = new File(phrescoFolder.getPath() + File.separator + PROJECT_INFO_FILE);
 			if (!projectFile.exists()) {
 				projectFile.createNewFile();
 			}
 			// make the .phresco folder as hidden for windows
 			// for linux its enough to create the folder with '.' to make it as
 			// hidden
 			if (System.getProperty(OSNAME).startsWith(WINDOWS)) {
 				Runtime.getRuntime().exec(
 						"attrib +h " + STR_DOUBLE_QUOTES + phrescoFolder.getPath() + STR_DOUBLE_QUOTES);
 			}
 
 			// write the project info as json string into the .project file
 			Gson gson = new Gson();
 			String infoJSON = gson.toJson(info);
 			fstream = new FileWriter(projectFile.getPath());
 			out = new BufferedWriter(fstream);
 			out.write(infoJSON);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 	
 	private static void initializeTestPom() {
 		testPomFiles.put("functional", "/test/functional/pom.xml");
 		testPomFiles.put("load", "/test/load/pom.xml");
 		testPomFiles.put("performance.database", "/test/performance/database/pom.xml");
 		testPomFiles.put("performance.server", "/test/performance/server/pom.xml");
 		testPomFiles.put("performance.webservice", "/test/performance/webservices/pom.xml");
 		testPomFiles.put("unit", "/test/unit/pom.xml");
 		testPomFiles.put("performance", "/test/performance/pom.xml");
 		}
 	
	public static ProjectInfo getProjectInfoFile(File directory) throws PhrescoException {
 		StringBuilder builder  = new StringBuilder();
 		builder.append(directory.getPath())
 		.append(File.separatorChar)
 		.append(DOT_PHRESCO_FOLDER)
 		.append(File.separatorChar)
 		.append(PROJECT_INFO_FILE);
 		BufferedReader bufferedReader = null;
 		try {
 			File projectInfoFile = new File(builder.toString());
 			if (!projectInfoFile.exists()) {
 				return null;
 			}
 			bufferedReader = new BufferedReader(new FileReader(builder.toString()));
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(bufferedReader, ProjectInfo.class);
 //			ApplicationInfo applicationInfo = null;
 //			if (projectInfo != null) {
 //				applicationInfo = projectInfo.getAppInfos().get(0);
 //			}
 			return projectInfo;
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(bufferedReader);
 		}
 	}
 	
 	public  void updateTestPom(File path) throws PhrescoException {
 		try {
 			File sourcePom = new File(path + "/pom.xml");
 			if (!sourcePom.exists()) {
 				return;
 			}
 			initializeTestPom();
 			PomProcessor processor;
 			processor = new PomProcessor(sourcePom);
 			String groupId = processor.getGroupId();
 			String artifactId = processor.getArtifactId();
 			String version = processor.getVersion();
 			String name = processor.getName();
 			Set<String> keySet = testPomFiles.keySet();
 			for (String string : keySet) {
 			    File testPomFile = new File(path + testPomFiles.get(string));
 			    if (testPomFile.exists()) {
                   processor = new PomProcessor(testPomFile);
                   processor.setGroupId(groupId + "." + string);
                   processor.setArtifactId(artifactId);
                   processor.setVersion(version);
                   if (name != null && !name.isEmpty()) {
                       processor.setName(name);
                   }
                   processor.save();
               }
             }
 			
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	/**
 	 * To update the project.info file with the new info
 	 * @param projectInfo
 	 * @param projectInfoFile
 	 * @throws PhrescoException
 	 */
 	public static void updateProjectInfo(ProjectInfo projectInfo, File projectInfoFile) throws PhrescoException {
 		BufferedWriter out = null;
 		FileWriter fstream = null;
 		try {
 			Gson gson = new Gson();
 			String infoJSON = gson.toJson(projectInfo);
 			fstream = new FileWriter(projectInfoFile.getPath());
 			out = new BufferedWriter(fstream);
 			out.write(infoJSON);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 	
 	public ProjectInfo getProjectInfo(File baseDir) throws PhrescoException {
 		ProjectInfo projectinfo = null;
 		Gson gson = new Gson();
 		BufferedReader reader = null;
 		try {
 			File[] dotPhrescoFolders = baseDir.listFiles(new PhrescoFileNameFilter(DOT_PHRESCO_FOLDER));
 			if (!ArrayUtils.isEmpty(dotPhrescoFolders)) {
 				for (File dotPhrescoFolder : dotPhrescoFolders) {
 					File[] dotProjectFiles = dotPhrescoFolder.listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 					for (File dotProjectFile : dotProjectFiles) {
 						reader = new BufferedReader(new FileReader(dotProjectFile));
 						projectinfo = gson.fromJson(reader, ProjectInfo.class);
 					}
 				}
 			}
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(reader);
 		}
 		return projectinfo;
 	}
 	
 	public void updatePOMWithPluginArtifact(File pomFile, File phrescoPomFile, List<ArtifactGroup> artifactGroups) throws PhrescoException {
 		if(CollectionUtils.isEmpty(artifactGroups)) {
 			return;
 		}
 		List<ArtifactGroup> dependencies = new ArrayList<ArtifactGroup>();
 		List<ArtifactGroup> artifacts = new ArrayList<ArtifactGroup>();
 		for (ArtifactGroup artifactGroup : artifactGroups) {
 			if(artifactGroup.getPackaging().equals("zip") || artifactGroup.getPackaging().equals("war")) {
 				artifacts.add(artifactGroup);
 			} else {
 				dependencies.add(artifactGroup);
 			}
 		}
 		
 		if(CollectionUtils.isNotEmpty(dependencies)) {
 			updatePOMWithModules(pomFile, dependencies);
 		}
 		
 		if(CollectionUtils.isNotEmpty(artifacts) && phrescoPomFile.exists()) {
 			updateToDependencyPlugin(phrescoPomFile, artifacts);
 		} else {
 			updateToDependencyPlugin(pomFile, artifacts);
 		}
 	}
 	
 	public void updateToDependencyPlugin(File pomFile, List<ArtifactGroup> artifactGroups) throws PhrescoException {
 		try {
 			if(CollectionUtils.isEmpty(artifactGroups)) {
 				return;
 			}
 			
 			PomProcessor processor = new PomProcessor(pomFile);
 			List<Element> configList = new ArrayList<Element>();
 			String modulePath = "";
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			for (ArtifactGroup artifactGroup : artifactGroups) {
 				List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
 				for (CoreOption coreOption : appliesTo) {
 					if (artifactGroup != null && !coreOption.isCore()) {
 						modulePath = getModulePath(artifactGroup, processor);
 						configList = configList(modulePath, artifactGroup.getGroupId(), artifactGroup.getArtifactId(),  artifactGroup.getVersions().get(0).getVersion(), doc);
 						processor.addExecutionConfiguration(DEPENDENCY_PLUGIN_GROUPID, DEPENDENCY_PLUGIN_ARTIFACTID, EXECUTION_ID, PHASE, GOAL, configList, doc);
 						break;
 					}
 				}
 			}
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public void removeExtractedFeatures(File pomFile, File sourceFolder, List<ArtifactGroup> removedArtifacts) throws PhrescoException {
 //		String baseDir = Utility.getProjectHome() + appInfo.getAppDirName() + File.separator;
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			String modulePath = "";
 			if(CollectionUtils.isNotEmpty(removedArtifacts)) {
 				removeMarkerFiles(pomFile, removedArtifacts);
 				for (ArtifactGroup artifactGroup : removedArtifacts) {
 					if (artifactGroup != null) {
 						modulePath = getModulePath(artifactGroup, processor);
 					}
 					File sourceDirFiles = new File(sourceFolder.getPath() + modulePath) ;
 					File[] listFiles = sourceDirFiles.listFiles();
 					for (File file : listFiles) {
 						if(file.exists() && artifactGroup.getName().equalsIgnoreCase(file.getName())) {
 							delete(file);
 						}
 					}
 				}
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} 
 	}
 	
 	public void removeMarkerFiles(File pomFile, List<ArtifactGroup> removedArtifacts) {
 		File markersDir = new File(pomFile.getParent() + File.separator + DO_NOT_CHECKIN_DIRY + File.separator + MARKERS_DIR);
 		if(!markersDir.exists()) {
 			return;
 		}
 		File[] markerFiles = markersDir.listFiles();
 		for (File markerFile : markerFiles) {
 			for (ArtifactGroup removedArtifact : removedArtifacts) {
 				String markerFileFormat = getMarkerFileFormat(removedArtifact);
 				if(CollectionUtils.isNotEmpty(removedArtifacts) && markerFile.getName().equals(markerFileFormat)) {
 					delete(markerFile);
 				}
 			}
 		}
 	}
 	
 	public void deleteFeatureDependencies(File pomFile, List<ArtifactGroup> removedFeatures) throws PhrescoException {
 //		StringBuilder pomDir = new StringBuilder(Utility.getProjectHome());
 //		if(StringUtils.isNotEmpty(appInfo.getRootModule())) {
 //			pomDir.append(appInfo.getRootModule())
 //			.append(File.separator);
 //		}
 //		pomDir.append(appInfo.getAppDirName())
 //		.append(File.separator)
 //		.append(Utility.getPomFileName(appInfo));
 		
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			Dependencies dependencies = processor.getModel().getDependencies();
 			if (dependencies != null) {
 				List<Dependency> dependency = dependencies.getDependency();
 				if (CollectionUtils.isNotEmpty(dependency)) {
 					for (ArtifactGroup artifactGroup : removedFeatures) {
 						processor.deleteDependency(artifactGroup.getGroupId(), artifactGroup.getArtifactId(),
 								artifactGroup.getPackaging());
 					}
 				}
 			}
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private String getMarkerFileFormat(ArtifactGroup removedArtifact) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(removedArtifact.getGroupId());
 		builder.append(STR_HYPHEN);
 		builder.append(removedArtifact.getArtifactId());
 		builder.append(STR_HYPHEN);
 		builder.append(removedArtifact.getPackaging());
 		builder.append(STR_HYPHEN);
 		List<ArtifactInfo> artifactInfos = removedArtifact.getVersions();
 		for (ArtifactInfo artifactInfo : artifactInfos) {
 			builder.append(artifactInfo.getVersion());
 		}
 		builder.append(DOT_MARKER);
 		return builder.toString();
 	}
 	
 	private String getModulePath(ArtifactGroup artifactGroup, PomProcessor processor) throws PhrescoException {
 		try {
 		if(artifactGroup.getType().name().equals(Type.FEATURE.name())) {
 			return processor.getProperty(Constants.POM_PROP_KEY_MODULE_SOURCE_DIR);
 		} else if(artifactGroup.getType().name().equals(Type.JAVASCRIPT.name())) {
 			return processor.getProperty(Constants.POM_PROP_KEY_JSLIBS_SOURCE_DIR);
 		} else if(artifactGroup.getType().name().equals(Type.COMPONENT.name())) {
 			return processor.getProperty(Constants.POM_PROP_KEY_COMPONENTS_SOURCE_DIR);
 		}
 		}catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return "";
 	}
 	
 	public void delete(File file) {
 
 		if(file.isDirectory()){
 			//directory is empty, then delete it
 			if(file.list().length==0){
 				file.delete();
 			}else{
 				String files[] = file.list();
 				for (String temp : files) {
 					//construct the file structure
 					File fileDelete = new File(file, temp);
 					//recursive delete
 					delete(fileDelete);
 				}
 				//check the directory again, if empty then delete it
 				if(file.list().length==0){
 					file.delete();
 				}
 			}
 
 		}else{
 			//if file, then delete it
 			file.delete();
 		}
 	}
 
 	public void deletePluginExecutionFromPom(File pomFile) throws PhrescoException {
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			processor.deleteConfiguration(DEPENDENCY_PLUGIN_GROUPID, DEPENDENCY_PLUGIN_ARTIFACTID, EXECUTION_ID, GOAL);
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private List<Element> configList(String modulePath, String moduleGroupId, String moduleArtifactId, String moduleVersion, Document doc) throws PhrescoException {
 		List<Element> configList = new ArrayList<Element>();
 		Element groupId = doc.createElement(GROUP_ID);
 		groupId.setTextContent(moduleGroupId);
 		Element artifactId = doc.createElement(ARTIFACT_ID);
 		artifactId.setTextContent(moduleArtifactId);
 		Element version = doc.createElement(VERSION);
 		version.setTextContent(moduleVersion);
 		Element type = doc.createElement(TYPE);
 		type.setTextContent(ZIP);
 		Element overWrite = doc.createElement(OVER_WRITE);
 		overWrite.setTextContent(OVER_WIRTE_VALUE);
 		Element outputDirectory = doc.createElement(OUTPUT_DIR);
 		outputDirectory.setTextContent("${project.basedir}" + modulePath);
 		configList.add(groupId);
 		configList.add(artifactId);
 		configList.add(version);
 		configList.add(type);
 		configList.add(overWrite);
 		configList.add(outputDirectory);
 		return configList;
 	}
 	
 	public void updatePOMWithModules(File pomFile, List<com.photon.phresco.commons.model.ArtifactGroup> modules) throws PhrescoException {
 		if (CollectionUtils.isEmpty(modules)) {
 			return;
 		}
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			for (com.photon.phresco.commons.model.ArtifactGroup module : modules) {
 				if (module != null) {
 					processor.addDependency(module.getGroupId(), module.getArtifactId(), module.getVersions().get(0).getVersion(), 
 							module.getVersions().get(0).getScope(), module.getPackaging(), "");
 				}
 			}
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public void addServerPlugin(ApplicationInfo info, File path, String dotPhrescoFolderPath) throws PhrescoException {
 		List<ArtifactGroupInfo> servers = info.getSelectedServers();
 		if (CollectionUtils.isEmpty(servers)) {
 			return;
 		}
 		String appHandlerPath = dotPhrescoFolderPath + File.separator + APPLICATION_HANDLER_INFO_FILE;
 		File pluginInfoFile = new File(appHandlerPath);
 		MojoProcessor mojoProcessor = new MojoProcessor(pluginInfoFile);
 		ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 		String selectedServers = applicationHandler.getSelectedServer();
 		if (StringUtils.isNotEmpty(selectedServers)) {
 			Gson gson = new Gson();
 			java.lang.reflect.Type jsonType = new TypeToken<Collection<DownloadInfo>>() {
 			}.getType();
 			List<DownloadInfo> serverInfos = gson.fromJson(selectedServers, jsonType);
 			for (DownloadInfo serverInfo : serverInfos) {
 				List<ArtifactInfo> artifactInfos = serverInfo.getArtifactGroup().getVersions();
 				for (ArtifactInfo artifactInfo : artifactInfos) {
 					String version = artifactInfo.getVersion();
 					if (serverInfo.getName().contains(Constants.TYPE_WEBLOGIC)) {
 						String pluginVersion = "";
 						if (version.equals(Constants.WEBLOGIC_12c)) {
 							pluginVersion = Constants.WEBLOGIC_12c_PLUGIN_VERSION;
 						} else if (version.equals(Constants.WEBLOGIC_11gR1)) {
 							pluginVersion = Constants.WEBLOGIC_11gr1c_PLUGIN_VERSION;
 						}
 						addWebLogicPlugin(path, pluginVersion);
 					}
 				}
 			}
 		}
 	}
 
 	public void deletePluginFromPom(File path) throws PhrescoException {
 		try {
 			PomProcessor pomprocessor = new PomProcessor(path);
 			pomprocessor.deletePlugin("com.oracle.weblogic", "weblogic-maven-plugin");
 			pomprocessor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void addWebLogicPlugin(File pomFile, String pluginVersion) throws PhrescoException {
 		try {
 			PomProcessor pomProcessor = new PomProcessor(pomFile);
 			pomProcessor.addPlugin("com.oracle.weblogic", "weblogic-maven-plugin", pluginVersion);
 			List<Element> configList = new ArrayList<Element>();
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			Element adminUrl = doc.createElement("adminurl");
 			adminUrl.setTextContent("t3://${server.host}:${server.port}");
 			Element user = doc.createElement("user");
 			user.setTextContent("${server.username}");
 			Element password = doc.createElement("password");
 			password.setTextContent("${server.password}");
 			Element upload = doc.createElement("upload");
 			upload.setTextContent("true");
 			Element action = doc.createElement("action");
 			action.setTextContent("deploy");
 			Element remote = doc.createElement("remote");
 			remote.setTextContent("true");
 			Element verbose = doc.createElement("verbose");
 			verbose.setTextContent("false");
 			Element source = doc.createElement("source");
 			source.setTextContent("${project.basedir}/do_not_checkin/build/temp/${project.build.finalName}.war");
 			Element name = doc.createElement("name");
 			name.setTextContent("${project.build.finalName}");
 			Element argLineElem = doc.createElement("argLine");
 			argLineElem.setTextContent("-Xmx512m");
 
 			configList.add(adminUrl);
 			configList.add(user);
 			configList.add(password);
 			configList.add(upload);
 			configList.add(action);
 			configList.add(remote);
 			configList.add(verbose);
 			configList.add(source);
 			configList.add(name);
 			configList.add(argLineElem);
 
 			pomProcessor.addConfiguration("com.oracle.weblogic", "weblogic-maven-plugin", configList);
 			pomProcessor.save();
 
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public BufferedReader ExtractFeature(File phrescoPomFile) throws PhrescoException {
 		BufferedReader breader = null;
 //		String pomFileName = Utility.getPhrescoPomFile(appInfo);
 		StringBuilder sb = new StringBuilder();
 		sb.append(MVN_COMMAND);
 		sb.append(STR_BLANK_SPACE);
 		sb.append(PHASE);
 		if(!POM_NAME.equals(phrescoPomFile.getName())) {
 			sb.append(STR_BLANK_SPACE);
 			sb.append(HYPHEN_F);
 			sb.append(STR_BLANK_SPACE);
 			sb.append(phrescoPomFile.getName());
 		}
 //		StringBuilder stringBuilder = new StringBuilder(Utility.getProjectHome());
 //		if(StringUtils.isNotEmpty(appInfo.getRootModule())) {
 //			stringBuilder.append(appInfo.getRootModule())
 //			.append(File.separator);
 //		}
 //		stringBuilder.append(appInfo.getAppDirName());
 		breader = Utility.executeCommand(sb.toString(), phrescoPomFile.getParent());
 		return breader;
 	}
 
 }
 
 class PhrescoFileNameFilter implements FilenameFilter {
 	private String filter_;
 
 	public PhrescoFileNameFilter(String filter) {
 		filter_ = filter;
 	}
 
 	public boolean accept(File dir, String name) {
 		return name.endsWith(filter_);
 	}
 }
