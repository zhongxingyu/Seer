 /*
  * ###
  * Phresco Framework
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.framework.model;
 
 import java.util.List;
 import java.util.Map;
 
 public class CIJob {
     private String name;
     private String svnUrl;
     private String userName;
     private String password;
     private Map<String, String> email;
     private String scheduleType;
     private String scheduleExpression;
     private String mvnCommand;
     private String senderEmailId;
     private String senderEmailPassword;
     private String jenkinsUrl;
     private String jenkinsPort;
     private List<String> triggers;
     private String repoType;
     private String branch;
   //collabNet implementation
     private boolean enableBuildRelease = false;
     private String collabNetURL = "";
     private String collabNetusername = "";
     private String collabNetpassword = "";
     private String collabNetProject = "";
     private String collabNetPackage = "";
     private String collabNetRelease = "";
     private boolean collabNetoverWriteFiles = false;
     
     //  CI  automation
     // whether to clone the current jobs workspace for further reference
     private boolean cloneWorkspace = false;
     //when to call is optional - can be added Down stream projects
     private String downStreamProject = "";
     // Whether this job is used cloned workspace or normal svn
     private String usedClonnedWorkspace = "";
     private String operation = "";// Operation like(Build, deploy, test)
     
     // for functional test
     private String pomLocation = "";
     
     private boolean enablePostBuildStep;
     private boolean enablePreBuildStep;
     
     private List<String> prebuildStepCommands;
     private List<String> postbuildStepCommands;
     
     // all the job info
     // build job info
     private String buildName ="";
     private String buildNumber ="1";
     private String environmentName = "";
     private String buildEnvironmentName = "";
     private String logs = "";
     private String showSettings = "";
     
     private String sdk = "";
     private String target = "";
     private String mode = "";
     private String encrypt = "";
     private String plistFile = "";
     
     private String skipTest = "";
     private String proguard = "";
     private String signing = "";
     private String keystore = "";
     private String storepass = "";
     private String keypass = "";
     private String alias = "";
     private String projectType = "";
     private String jarName = "";
     private String mainClassName = "";
     private String jarLocation = "";
     private String minify = "";
     private String configuration = "";
     private String keyPassword = "";
     private String packMinifiedFiles = "";
     
     // deploy job info
     private String deviceType = "";
     private String sdkVersion = "";
     private String family = "";
 
     private String devices = "";
     private String serialNumber = "";
     
     private String testAgainst = "";
     private String browser = "";
     private String resolution = "";
     
     private String executeSql = "";
     private String dataBase = "";
     private String fetchSql = "";
     private String triggerSimulator = "false";
    private String deviceId = "";
     
     public CIJob() {
         super();
     }
     
     public CIJob(String name, String svnUrl, String userName, String password) {
         super();
         this.name = name;
         this.svnUrl = svnUrl;
         this.userName = userName;
         this.password = password;
     }
     
     public String getName() {
         return name;
     }
     public void setName(String name) {
         this.name = name;
     }
     public String getSvnUrl() {
         return svnUrl;
     }
     public void setSvnUrl(String svnUrl) {
         this.svnUrl = svnUrl;
     }
     public String getUserName() {
         return userName;
     }
     public void setUserName(String userName) {
         this.userName = userName;
     }
     public String getPassword() {
         return password;
     }
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getScheduleType() {
         return scheduleType;
     }
 
     public void setScheduleType(String scheduleType) {
         this.scheduleType = scheduleType;
     }
 
     public String getScheduleExpression() {
         return scheduleExpression;
     }
 
     public void setScheduleExpression(String scheduleExpression) {
         this.scheduleExpression = scheduleExpression;
     }
 
 	public Map<String, String> getEmail() {
 		return email;
 	}
 
 	public void setEmail(Map<String, String> email) {
 		this.email = email;
 	}
 
 	public String getMvnCommand() {
 		return mvnCommand;
 	}
 
 	public void setMvnCommand(String mvnCommand) {
 		this.mvnCommand = mvnCommand;
 	}
 
 	public String getSenderEmailId() {
 		return senderEmailId;
 	}
 
 	public void setSenderEmailId(String senderEmailId) {
 		this.senderEmailId = senderEmailId;
 	}
 
 	public String getSenderEmailPassword() {
 		return senderEmailPassword;
 	}
 
 	public void setSenderEmailPassword(String senderEmailPassword) {
 		this.senderEmailPassword = senderEmailPassword;
 	}
 
 	public String getJenkinsUrl() {
 		return jenkinsUrl;
 	}
 
 	public void setJenkinsUrl(String jenkinsUrl) {
 		this.jenkinsUrl = jenkinsUrl;
 	}
 
 	public String getJenkinsPort() {
 		return jenkinsPort;
 	}
 
 	public void setJenkinsPort(String jenkinsPort) {
 		this.jenkinsPort = jenkinsPort;
 	}
 
 	public List<String> getTriggers() {
 		return triggers;
 	}
 
 	public void setTriggers(List<String> triggers) {
 		this.triggers = triggers;
 	}
 
 	public String getRepoType() {
 		return repoType;
 	}
 
 	public void setRepoType(String repoType) {
 		this.repoType = repoType;
 	}
 
 	public String getBranch() {
 		return branch;
 	}
 
 	public void setBranch(String branch) {
 		this.branch = branch;
 	}
 
 	public boolean isEnableBuildRelease() {
 		return enableBuildRelease;
 	}
 
 	public void setEnableBuildRelease(boolean enableBuildRelease) {
 		this.enableBuildRelease = enableBuildRelease;
 	}
 
 	public String getCollabNetURL() {
 		return collabNetURL;
 	}
 
 	public void setCollabNetURL(String collabNetURL) {
 		this.collabNetURL = collabNetURL;
 	}
 
 	public String getCollabNetusername() {
 		return collabNetusername;
 	}
 
 	public void setCollabNetusername(String collabNetusername) {
 		this.collabNetusername = collabNetusername;
 	}
 
 	public String getCollabNetpassword() {
 		return collabNetpassword;
 	}
 
 	public void setCollabNetpassword(String collabNetpassword) {
 		this.collabNetpassword = collabNetpassword;
 	}
 
 	public String getCollabNetProject() {
 		return collabNetProject;
 	}
 
 	public void setCollabNetProject(String collabNetProject) {
 		this.collabNetProject = collabNetProject;
 	}
 
 	public String getCollabNetPackage() {
 		return collabNetPackage;
 	}
 
 	public void setCollabNetPackage(String collabNetPackage) {
 		this.collabNetPackage = collabNetPackage;
 	}
 
 	public String getCollabNetRelease() {
 		return collabNetRelease;
 	}
 
 	public void setCollabNetRelease(String collabNetRelease) {
 		this.collabNetRelease = collabNetRelease;
 	}
 
 	public boolean isCollabNetoverWriteFiles() {
 		return collabNetoverWriteFiles;
 	}
 
 	public void setCollabNetoverWriteFiles(boolean collabNetoverWriteFiles) {
 		this.collabNetoverWriteFiles = collabNetoverWriteFiles;
 	}
 
 	public boolean isCloneWorkspace() {
 		return cloneWorkspace;
 	}
 
 	public void setCloneWorkspace(boolean cloneWorkspace) {
 		this.cloneWorkspace = cloneWorkspace;
 	}
 
 	public String getDownStreamProject() {
 		return downStreamProject;
 	}
 
 	public void setDownStreamProject(String downStreamProject) {
 		this.downStreamProject = downStreamProject;
 	}
 
 	public String getUsedClonnedWorkspace() {
 		return usedClonnedWorkspace;
 	}
 
 	public void setUsedClonnedWorkspace(String usedClonnedWorkspace) {
 		this.usedClonnedWorkspace = usedClonnedWorkspace;
 	}
 
 	public String getOperation() {
 		return operation;
 	}
 
 	public void setOperation(String operation) {
 		this.operation = operation;
 	}
 
 	public String getPomLocation() {
 		return pomLocation;
 	}
 
 	public void setPomLocation(String pomLocation) {
 		this.pomLocation = pomLocation;
 	}
 
 	public boolean isEnablePostBuildStep() {
 		return enablePostBuildStep;
 	}
 
 	public void setEnablePostBuildStep(boolean enablePostBuildStep) {
 		this.enablePostBuildStep = enablePostBuildStep;
 	}
 
 	public boolean isEnablePreBuildStep() {
 		return enablePreBuildStep;
 	}
 
 	public void setEnablePreBuildStep(boolean enablePreBuildStep) {
 		this.enablePreBuildStep = enablePreBuildStep;
 	}
 
 	public List<String> getPrebuildStepCommands() {
 		return prebuildStepCommands;
 	}
 
 	public void setPrebuildStepCommands(List<String> prebuildStepCommands) {
 		this.prebuildStepCommands = prebuildStepCommands;
 	}
 
 	public List<String> getPostbuildStepCommands() {
 		return postbuildStepCommands;
 	}
 
 	public void setPostbuildStepCommands(List<String> postbuildStepCommands) {
 		this.postbuildStepCommands = postbuildStepCommands;
 	}
 
 	public String getBuildName() {
 		return buildName;
 	}
 
 	public void setBuildName(String buildName) {
 		this.buildName = buildName;
 	}
 
 	public String getBuildNumber() {
 		return buildNumber;
 	}
 
 	public void setBuildNumber(String buildNumber) {
 		this.buildNumber = buildNumber;
 	}
 
 	public String getEnvironmentName() {
 		return environmentName;
 	}
 
 	public void setEnvironmentName(String environmentName) {
 		this.environmentName = environmentName;
 	}
 
 	public String getLogs() {
 		return logs;
 	}
 
 	public void setLogs(String logs) {
 		this.logs = logs;
 	}
 
 	public String getSdk() {
 		return sdk;
 	}
 
 	public void setSdk(String sdk) {
 		this.sdk = sdk;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 
 	public String getMode() {
 		return mode;
 	}
 
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
 
 	public String getEncrypt() {
 		return encrypt;
 	}
 
 	public void setEncrypt(String encrypt) {
 		this.encrypt = encrypt;
 	}
 
 	public String getPlistFile() {
 		return plistFile;
 	}
 
 	public void setPlistFile(String plistFile) {
 		this.plistFile = plistFile;
 	}
 
 	public String getSkipTest() {
 		return skipTest;
 	}
 
 	public void setSkipTest(String skipTest) {
 		this.skipTest = skipTest;
 	}
 
 	public String getProguard() {
 		return proguard;
 	}
 
 	public void setProguard(String proguard) {
 		this.proguard = proguard;
 	}
 
 	public String getSigning() {
 		return signing;
 	}
 
 	public void setSigning(String signing) {
 		this.signing = signing;
 	}
 
 	public String getKeystore() {
 		return keystore;
 	}
 
 	public void setKeystore(String keystore) {
 		this.keystore = keystore;
 	}
 
 	public String getStorepass() {
 		return storepass;
 	}
 
 	public void setStorepass(String storepass) {
 		this.storepass = storepass;
 	}
 
 	public String getKeypass() {
 		return keypass;
 	}
 
 	public void setKeypass(String keypass) {
 		this.keypass = keypass;
 	}
 
 	public String getAlias() {
 		return alias;
 	}
 
 	public void setAlias(String alias) {
 		this.alias = alias;
 	}
 
 	public String getMinify() {
 		return minify;
 	}
 
 	public void setMinify(String minify) {
 		this.minify = minify;
 	}
 
 	public String getDeviceType() {
 		return deviceType;
 	}
 
 	public void setDeviceType(String deviceType) {
 		this.deviceType = deviceType;
 	}
 
 	public String getSdkVersion() {
 		return sdkVersion;
 	}
 
 	public void setSdkVersion(String sdkVersion) {
 		this.sdkVersion = sdkVersion;
 	}
 
 	public String getFamily() {
 		return family;
 	}
 
 	public void setFamily(String family) {
 		this.family = family;
 	}
 
 	public String getDevices() {
 		return devices;
 	}
 
 	public void setDevices(String devices) {
 		this.devices = devices;
 	}
 
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 	public void setSerialNumber(String serialNumber) {
 		this.serialNumber = serialNumber;
 	}
 
 	public String getTestAgainst() {
 		return testAgainst;
 	}
 
 	public void setTestAgainst(String testAgainst) {
 		this.testAgainst = testAgainst;
 	}
 
 	public String getBrowser() {
 		return browser;
 	}
 
 	public void setBrowser(String browser) {
 		this.browser = browser;
 	}
 
 	public String getResolution() {
 		return resolution;
 	}
 
 	public void setResolution(String resolution) {
 		this.resolution = resolution;
 	}
 
 	public String getShowSettings() {
 		return showSettings;
 	}
 
 	public void setShowSettings(String showSettings) {
 		this.showSettings = showSettings;
 	}
 
 	public String getProjectType() {
 		return projectType;
 	}
 
 	public void setProjectType(String projectType) {
 		this.projectType = projectType;
 	}
 
 	public String getKeyPassword() {
 		return keyPassword;
 	}
 
 	public void setKeyPassword(String keyPassword) {
 		this.keyPassword = keyPassword;
 	}
 
 	public String getBuildEnvironmentName() {
 		return buildEnvironmentName;
 	}
 
 	public void setBuildEnvironmentName(String buildEnvironmentName) {
 		this.buildEnvironmentName = buildEnvironmentName;
 	}
 
 	public String getExecuteSql() {
 		return executeSql;
 	}
 
 	public void setExecuteSql(String executeSql) {
 		this.executeSql = executeSql;
 	}
 
 	public String getDataBase() {
 		return dataBase;
 	}
 
 	public void setDataBase(String dataBase) {
 		this.dataBase = dataBase;
 	}
 
 	public String getFetchSql() {
 		return fetchSql;
 	}
 
 	public void setFetchSql(String fetchSql) {
 		this.fetchSql = fetchSql;
 	}
 
 	public String getJarName() {
 		return jarName;
 	}
 
 	public void setJarName(String jarName) {
 		this.jarName = jarName;
 	}
 
 	public String getMainClassName() {
 		return mainClassName;
 	}
 
 	public void setMainClassName(String mainClassName) {
 		this.mainClassName = mainClassName;
 	}
 
 	public String getJarLocation() {
 		return jarLocation;
 	}
 
 	public void setJarLocation(String jarLocation) {
 		this.jarLocation = jarLocation;
 	}
 
 	public String getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(String configuration) {
 		this.configuration = configuration;
 	}
 
 	public String getTriggerSimulator() {
 		return triggerSimulator;
 	}
 
 	public void setTriggerSimulator(String triggerSimulator) {
 		this.triggerSimulator = triggerSimulator;
 	}
 	
 	public void setPackMinifiedFiles(String packMinifiedFiles) {
 		this.packMinifiedFiles = packMinifiedFiles;
 	}
 
 	public String getPackMinifiedFiles() {
 		return packMinifiedFiles;
 	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
 }
