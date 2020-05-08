 package com.photon.phresco.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.codehaus.plexus.util.CollectionUtils;
 import org.xml.sax.SAXException;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 
 public class DeployEnvironmentImpl implements DynamicParameter, Constants {
	private String FILE_SEPARATOR = "/";
	private String BUILD_INFO_FILE_NAME = "build.info";
	private String DO_NOT_CHECKIN_DIR = "do_not_checkin";
	private String BUILD_DIR = DO_NOT_CHECKIN_DIR + File.separator + "build";
 	
 	@Override
 	public PossibleValues getValues(Map<String, Object> deployEnvMap) throws IOException, ParserConfigurationException, SAXException, ConfigurationException, PhrescoException {
 		PossibleValues possibleValues = new PossibleValues();
 		ApplicationInfo applicationInfo = (ApplicationInfo) deployEnvMap.get(KEY_APP_INFO);
 		String buildNumber = (String) deployEnvMap.get(KEY_BUILD_NO);
 		String buildInfoFileDirectory = getBuildInfosFilePath(applicationInfo);
 		BuildInfo buildInfo = getBuildInfo(Integer.parseInt(buildNumber), buildInfoFileDirectory);
 		
 		List<String> environments = buildInfo.getEnvironments();
 		for (String environment : environments) {
 			Value value = new Value();
     		value.setValue(environment);
     		possibleValues.getValue().add(value);
 		}
 
 		return possibleValues;
 	}
 	
 	private String getBuildInfosFilePath(ApplicationInfo applicationInfo) {
     	return getAppDirectoryPath(applicationInfo) + FILE_SEPARATOR + BUILD_DIR + FILE_SEPARATOR + BUILD_INFO_FILE_NAME;
     }
 	
 	private String getAppDirectoryPath(ApplicationInfo applicationInfo) {
     	return Utility.getProjectHome() + applicationInfo.getAppDirName();
     }
 	
 	private BuildInfo getBuildInfo(int buildNumber, String buildInfoFileDirectory) throws PhrescoException {
 		List<BuildInfo> buildInfos = getBuildInfos(new File(buildInfoFileDirectory));
 		if (buildInfos != null) {
 			for (BuildInfo buildInfo : buildInfos) {
 				if (buildInfo.getBuildNo() == buildNumber) {
 					return buildInfo;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private List<BuildInfo> getBuildInfos(File buildInfoFile) throws PhrescoException {
 		 try {
 			 return readBuildInfo(buildInfoFile);
 		 } catch (IOException e) {
 			 throw new PhrescoException(e);
 		 }
 	}
 	
 	private List<BuildInfo> readBuildInfo(File path) throws IOException {
 		 if (!path.exists()) {
 			 return new ArrayList<BuildInfo>(1);
 		 }
 
 		 BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
 		 Gson gson = new Gson();
 		 Type type = new TypeToken<List<BuildInfo>>(){}.getType();
 
 		 List<BuildInfo> buildInfos = gson.fromJson(bufferedReader, type);
 		 bufferedReader.close();
 
 		 return buildInfos;
 	 }
 }
