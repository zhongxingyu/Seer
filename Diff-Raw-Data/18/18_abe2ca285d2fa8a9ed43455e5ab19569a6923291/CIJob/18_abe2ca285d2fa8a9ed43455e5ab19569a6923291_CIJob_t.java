 /**
  * Phresco Framework
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
 package com.photon.phresco.commons.model;
 
 import java.util.List;
 import java.util.Map;
 
 public class CIJob {
     private String jobName;
     private String url;
     private String username;
     private String password;
     private Map<String, String> email;
     private String scheduleType;
     private String scheduleExpression;
     private String mvnCommand;
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
     private String collabNetFileReleasePattern = "";
     //confluence implementation
     private boolean enableConfluence = false;
 	private String confluenceSite;
 	private boolean confluencePublish = false;
 	private String confluenceSpace = "";
 	private String confluencePage = "";
 	private boolean confluenceArtifacts = false;
 	private String confluenceOther = "";
     
     //  CI  automation
     // whether to clone the current jobs workspace for further reference
     private boolean cloneWorkspace = false;
     private String clonnedWorkspaceName = "";
     // When to trigger down stream project
     private String downStreamCriteria = "";
     // Whether this job is used cloned workspace or normal svn
     private String usedClonnedWorkspace = "";
     // Operation like(Build, deploy, test)
     private String operation = "";
     
     // Upstream and downstream management
     private String upstreamApplication = "";
     private String downstreamApplication = "";
     
     private String successEmailIds = "";
     private String failureEmailIds = "";
     
     // for functional test
     private String pomLocation = "";
     
     private boolean enablePostBuildStep;
     private boolean enablePreBuildStep;
     
     private List<String> prebuildStepCommands;
     private List<String> postbuildStepCommands;
     
     // when report job is created need to specify the attachment pattern
     private String attachmentsPattern = "";
     // For build job alone do_not_checkin need to be archived
     private boolean enableArtifactArchiver;
     
     // all the job info
     // build job info
     private String buildName ="";
     private String buildNumber ="1";
     private String environmentName = "";
     private String buildEnvironmentName = "";
     private String logs = "";
     private String showSettings = "";
     private String packageFileBrowse = "";
     
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
     private String zipAlign = "";   
     
     // iphone unit test
     private boolean unitTestType = false;
     private String unittest = "";
     
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
     private String theme = "";
     private String deviceKeyPassword = "";
     private String emulatorKeyPassword = "";
     private String platform = "";
     private String projectModule = "";
     
     // sonar CI integration
     private String sonar = "";
     private String skipTests = "";
     private String src = "";
     
     // pdf report generation
     private String reportType = "";
     private String testType = "";
     private String logo = "";
     private String sonarUrl = "";
     private String reportName = "";
     
 	// Load Test CI integration
 	//  private String testAgainst = "";
 	//  private String showSettings = "";
 	private String headerKey = "";
 	private String headerValue = "";
 	private String addHeader = "";
 	  
 	//Performance Test CI Integration
 	private String configurations = "";
 	private String testName = "";
 	private String httpName = "";
 	private String noOfUsers = "";
 	private String rampUpPeriod = "";
 	private String loopCount = "";
 	private String context = "";
 	private String contextUrls = "";
 	private String contextType = "";
 	private String encodingType = "";
 	private String parameterName = "";
 	private String parameterValue = "";
 	private String dbName = "";
 	private String queryType = "";
 	private String query = "";
 	private String dbContextUrls = "";
 	private String loadContextUrl = "";
 	private String isFromCI = ""; 
 	private String testBasis = "";
 	private String customTestAgainst = "";
 	private String availableJmx = "";
 	private boolean coberturaPlugin = false;
 	private String authManager = "";
 	private String authorizationUrl = "";
 	private String authorizationUserName = "";
 	private String authorizationPassword = "";
 	private String authorizationDomain = "";
 	private String authorizationRealm = "";
 	private String appDirName;
 	private String appName;
 	private String templateName;
 	private String technologyName;
 	private String redirectAutomatically = "";
 	private String followRedirects = "";
 	private String keepAlive = "";
 	private String multipartData = "";
 	private String compatibleHeaders = "";
 	private String regexExtractor = "";
     private String applyTo = "";
     private String responseField = "";
 	private String referenceName = "";
 	private String regex = "";
 	private String template = "";
 	private String matchNo = "";
 	private String defaultValue = "";
	private String type = "";
   
 	// Android functional test
 	private String deviceList = "";
 	
     public CIJob() {
         super();
     }
     
     public CIJob(String name, String url, String userName, String password) {
         super();
         this.jobName = name;
         this.url = url;
         this.username = userName;
         this.password = password;
     }
     
     public String getJobName() {
         return jobName;
     }
     public void setJobName(String jobName) {
         this.jobName = jobName;
     }
     public String getUrl() {
         return url;
     }
     public void setUrl(String url) {
         this.url = url;
     }
     public String getUsername() {
         return username;
     }
     public void setUsername(String username) {
         this.username = username;
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
 
 	public String getTheme() {
 		return theme;
 	}
 
 	public void setTheme(String theme) {
 		this.theme = theme;
 	}
 
 	public String getDeviceKeyPassword() {
 		return deviceKeyPassword;
 	}
 
 	public void setDeviceKeyPassword(String deviceKeyPassword) {
 		this.deviceKeyPassword = deviceKeyPassword;
 	}
 
 	public String getEmulatorKeyPassword() {
 		return emulatorKeyPassword;
 	}
 
 	public void setEmulatorKeyPassword(String emulatorKeyPassword) {
 		this.emulatorKeyPassword = emulatorKeyPassword;
 	}
 
 	public String getPlatform() {
 		return platform;
 	}
 
 	public void setPlatform(String platform) {
 		this.platform = platform;
 	}
 
 	public String getProjectModule() {
 		return projectModule;
 	}
 
 	public void setProjectModule(String projectModule) {
 		this.projectModule = projectModule;
 	}
 
 	public String getSonar() {
 		return sonar;
 	}
 
 	public void setSonar(String sonar) {
 		this.sonar = sonar;
 	}
 
 	public String getSkipTests() {
 		return skipTests;
 	}
 
 	public void setSkipTests(String skipTests) {
 		this.skipTests = skipTests;
 	}
 
 	public String getLogo() {
 		return logo;
 	}
 
 	public void setLogo(String logo) {
 		this.logo = logo;
 	}
 
 	public String getSonarUrl() {
 		return sonarUrl;
 	}
 
 	public void setSonarUrl(String sonarUrl) {
 		this.sonarUrl = sonarUrl;
 	}
 
 	public String getReportType() {
 		return reportType;
 	}
 
 	public void setReportType(String reportType) {
 		this.reportType = reportType;
 	}
 
 	public String getTestType() {
 		return testType;
 	}
 
 	public void setTestType(String testType) {
 		this.testType = testType;
 	}
 
 	public String getAttachmentsPattern() {
 		return attachmentsPattern;
 	}
 
 	public void setAttachmentsPattern(String attachmentsPattern) {
 		this.attachmentsPattern = attachmentsPattern;
 	}
 
 	public boolean isEnableArtifactArchiver() {
 		return enableArtifactArchiver;
 	}
 
 	public void setEnableArtifactArchiver(boolean enableArtifactArchiver) {
 		this.enableArtifactArchiver = enableArtifactArchiver;
 	}
 
 	public String getSrc() {
 		return src;
 	}
 
 	public void setSrc(String src) {
 		this.src = src;
 	}
 
 	public String getHeaderKey() {
 		return headerKey;
 	}
 
 	public void setHeaderKey(String headerKey) {
 		this.headerKey = headerKey;
 	}
 
 	public String getHeaderValue() {
 		return headerValue;
 	}
 
 	public void setHeaderValue(String headerValue) {
 		this.headerValue = headerValue;
 	}
 
 	public String getAddHeader() {
 		return addHeader;
 	}
 
 	public void setAddHeader(String addHeader) {
 		this.addHeader = addHeader;
 	}
 
 	public String getConfigurations() {
 		return configurations;
 	}
 
 	public void setConfigurations(String configurations) {
 		this.configurations = configurations;
 	}
 
 	public String getTestName() {
 		return testName;
 	}
 
 	public void setTestName(String testName) {
 		this.testName = testName;
 	}
 
 	public String getNoOfUsers() {
 		return noOfUsers;
 	}
 
 	public void setNoOfUsers(String noOfUsers) {
 		this.noOfUsers = noOfUsers;
 	}
 
 	public String getRampUpPeriod() {
 		return rampUpPeriod;
 	}
 
 	public void setRampUpPeriod(String rampUpPeriod) {
 		this.rampUpPeriod = rampUpPeriod;
 	}
 
 	public String getLoopCount() {
 		return loopCount;
 	}
 
 	public void setLoopCount(String loopCount) {
 		this.loopCount = loopCount;
 	}
 
 	public String getContextUrls() {
 		return contextUrls;
 	}
 
 	public void setContextUrls(String contextUrls) {
 		this.contextUrls = contextUrls;
 	}
 
 	public String getHttpName() {
 		return httpName;
 	}
 
 	public void setHttpName(String httpName) {
 		this.httpName = httpName;
 	}
 
 	public String getContext() {
 		return context;
 	}
 
 	public void setContext(String context) {
 		this.context = context;
 	}
 
 	public String getContextType() {
 		return contextType;
 	}
 
 	public void setContextType(String contextType) {
 		this.contextType = contextType;
 	}
 
 	public String getEncodingType() {
 		return encodingType;
 	}
 
 	public void setEncodingType(String encodingType) {
 		this.encodingType = encodingType;
 	}
 
 	public String getParameterName() {
 		return parameterName;
 	}
 
 	public void setParameterName(String parameterName) {
 		this.parameterName = parameterName;
 	}
 
 	public String getParameterValue() {
 		return parameterValue;
 	}
 
 	public void setParameterValue(String parameterValue) {
 		this.parameterValue = parameterValue;
 	}
 
 	public String getDbName() {
 		return dbName;
 	}
 
 	public void setDbName(String dbName) {
 		this.dbName = dbName;
 	}
 
 	public String getQueryType() {
 		return queryType;
 	}
 
 	public void setQueryType(String queryType) {
 		this.queryType = queryType;
 	}
 
 	public String getQuery() {
 		return query;
 	}
 
 	public void setQuery(String query) {
 		this.query = query;
 	}
 
 	public String getDbContextUrls() {
 		return dbContextUrls;
 	}
 
 	public void setDbContextUrls(String dbContextUrls) {
 		this.dbContextUrls = dbContextUrls;
 	}
 
 	public String getPackageFileBrowse() {
 		return packageFileBrowse;
 	}
 
 	public void setPackageFileBrowse(String packageFileBrowse) {
 		this.packageFileBrowse = packageFileBrowse;
 	}
 
 	public String getUnittest() {
 		return unittest;
 	}
 
 	public void setUnittest(String unittest) {
 		this.unittest = unittest;
 	}
 
 	public String getDownStreamCriteria() {
 		return downStreamCriteria;
 	}
 
 	public void setDownStreamCriteria(String downStreamCriteria) {
 		this.downStreamCriteria = downStreamCriteria;
 	}
 
 	public String getDeviceList() {
 		return deviceList;
 	}
 
 	public void setDeviceList(String deviceList) {
 		this.deviceList = deviceList;
 	}
 
 	public void setIsFromCI(String isFromCI) {
 		this.isFromCI = isFromCI;
 	}
 
 	public String getIsFromCI() {
 		return isFromCI;
 	}
 
 	public String getCollabNetFileReleasePattern() {
 		return collabNetFileReleasePattern;
 	}
 
 	public void setCollabNetFileReleasePattern(String collabNetFileReleasePattern) {
 		this.collabNetFileReleasePattern = collabNetFileReleasePattern;
 	}
 
 	public void setZipAlign(String zipAlign) {
 		this.zipAlign = zipAlign;
 	}
 
 	public String getZipAlign() {
 		return zipAlign;
 	}
 
 	public void setUnitTestType(boolean unitTestType) {
 		this.unitTestType = unitTestType;
 	}
 
 	public void setCoberturaPlugin(boolean coberturaPlugin) {
 		this.coberturaPlugin = coberturaPlugin;
 	}
 
 	public boolean isCoberturaPlugin() {
 		return coberturaPlugin;
 	}
 
 	public boolean isEnableConfluence() {
 		return enableConfluence;
 	}
 
 	public void setEnableConfluence(boolean enableConfluence) {
 		this.enableConfluence = enableConfluence;
 	}
 
 	public String getConfluenceSite() {
 		return confluenceSite;
 	}
 
 	public void setConfluenceSite(String confluenceSite) {
 		this.confluenceSite = confluenceSite;
 	}
 
 	public boolean isConfluencePublish() {
 		return confluencePublish;
 	}
 
 	public void setConfluencePublish(boolean confluencePublish) {
 		this.confluencePublish = confluencePublish;
 	}
 
 	public String getConfluenceSpace() {
 		return confluenceSpace;
 	}
 
 	public void setConfluenceSpace(String confluenceSpace) {
 		this.confluenceSpace = confluenceSpace;
 	}
 
 	public String getConfluencePage() {
 		return confluencePage;
 	}
 
 	public void setConfluencePage(String confluencePage) {
 		this.confluencePage = confluencePage;
 	}
 
 	public boolean isConfluenceArtifacts() {
 		return confluenceArtifacts;
 	}
 
 	public void setConfluenceArtifacts(boolean confluenceArtifacts) {
 		this.confluenceArtifacts = confluenceArtifacts;
 	}
 
 	public String getConfluenceOther() {
 		return confluenceOther;
 	}
 
 	public void setConfluenceOther(String confluenceOther) {
 		this.confluenceOther = confluenceOther;
 	}
 
 	public void setLoadContextUrl(String loadContextUrl) {
 		this.loadContextUrl = loadContextUrl;
 	}
 
 	public String getLoadContextUrl() {
 		return loadContextUrl;
 	}
 	
 	public String getReportName() {
 		return reportName;
 	}
 
 	public void setReportName(String reportName) {
 		this.reportName = reportName;
 	}
 
 	public void setTestBasis(String testBasis) {
 		this.testBasis = testBasis;
 	}
 
 	public String getTestBasis() {
 		return testBasis;
 	}
 
 	public void setCustomTestAgainst(String customTestAgainst) {
 		this.customTestAgainst = customTestAgainst;
 	}
 
 	public String getCustomTestAgainst() {
 		return customTestAgainst;
 	}
 
 	public void setAvailableJmx(String availableJmx) {
 		this.availableJmx = availableJmx;
 	}
 
 	public String getAvailableJmx() {
 		return availableJmx;
 	}
 
 	public String getAuthManager() {
 		return authManager;
 	}
 
 	public void setAuthManager(String authManager) {
 		this.authManager = authManager;
 	}
 
 	public String getAuthorizationUrl() {
 		return authorizationUrl;
 	}
 
 	public void setAuthorizationUrl(String authorizationUrl) {
 		this.authorizationUrl = authorizationUrl;
 	}
 
 	public String getAuthorizationUserName() {
 		return authorizationUserName;
 	}
 
 	public void setAuthorizationUserName(String authorizationUserName) {
 		this.authorizationUserName = authorizationUserName;
 	}
 
 	public String getAuthorizationPassword() {
 		return authorizationPassword;
 	}
 
 	public void setAuthorizationPassword(String authorizationPassword) {
 		byte[] encodeBase64 = org.apache.commons.codec.binary.Base64.encodeBase64(authorizationPassword.getBytes());
 		String encodedString = new String(encodeBase64);
 		this.authorizationPassword = encodedString;
 	}
 
 	public String getAuthorizationDomain() {
 		return authorizationDomain;
 	}
 
 	public void setAuthorizationDomain(String authorizationDomain) {
 		this.authorizationDomain = authorizationDomain;
 	}
 
 	public String getAuthorizationRealm() {
 		return authorizationRealm;
 	}
 
 	public void setAuthorizationRealm(String authorizationRealm) {
 		this.authorizationRealm = authorizationRealm;
 	}
 
 	/**
 	 * @return the upstreamApplication
 	 */
 	public String getUpstreamApplication() {
 		return upstreamApplication;
 	}
 
 	/**
 	 * @param upstreamApplication the upstreamApplication to set
 	 */
 	public void setUpstreamApplication(String upstreamApplication) {
 		this.upstreamApplication = upstreamApplication;
 	}
 
 	/**
 	 * @return the downstreamApplication
 	 */
 	public String getDownstreamApplication() {
 		return downstreamApplication;
 	}
 
 	/**
 	 * @param downstreamApplication the downstreamApplication to set
 	 */
 	public void setDownstreamApplication(String downstreamApplication) {
 		this.downstreamApplication = downstreamApplication;
 	}
 	
 	/**
 	 * @return the downstreamApplication
 	 */
 	public String getclonnedWorkspaceName() {
 		return downstreamApplication;
 	}
 
 	/**
 	 * @param downstreamApplication the downstreamApplication to set
 	 */
 	public void setclonnedWorkspaceName(String clonnedWorkspaceName) {
 		this.clonnedWorkspaceName = clonnedWorkspaceName;
 	}
 
 	public void setAppDirName(String appDirName) {
 		this.appDirName = appDirName;
 	}
 
 	public String getAppDirName() {
 		return appDirName;
 	}
 	
 	public void setTemplateName(String templateName) {
 		this.templateName = templateName;
 	}
 
 	public String getTemplateName() {
 		return templateName;
 	}
 
 	public void setAppName(String appName) {
 		this.appName = appName;
 	}
 
 	public String getAppName() {
 		return appName;
 	}
 
 	public void setSuccessEmailIds(String successEmailIds) {
 		this.successEmailIds = successEmailIds;
 	}
 
 	public String getSuccessEmailIds() {
 		return successEmailIds;
 	}
 
 	public void setFailureEmailIds(String failureEmailIds) {
 		this.failureEmailIds = failureEmailIds;
 	}
 
 	public String getFailureEmailIds() {
 		return failureEmailIds;
 	}
 
 	public void setTechnologyName(String technologyName) {
 		this.technologyName = technologyName;
 	}
 
 	public String getTechnologyName() {
 		return technologyName;
 	}
 
 	public String getRedirectAutomatically() {
 		return redirectAutomatically;
 	}
 
 	public void setRedirectAutomatically(String redirectAutomatically) {
 		this.redirectAutomatically = redirectAutomatically;
 	}
 
 	public String getFollowRedirects() {
 		return followRedirects;
 	}
 
 	public void setFollowRedirects(String followRedirects) {
 		this.followRedirects = followRedirects;
 	}
 
 	public String getKeepAlive() {
 		return keepAlive;
 	}
 
 	public void setKeepAlive(String keepAlive) {
 		this.keepAlive = keepAlive;
 	}
 
 	public String getMultipartData() {
 		return multipartData;
 	}
 
 	public void setMultipartData(String multipartData) {
 		this.multipartData = multipartData;
 	}
 
 	public String getCompatibleHeaders() {
 		return compatibleHeaders;
 	}
 
 	public void setCompatibleHeaders(String compatibleHeaders) {
 		this.compatibleHeaders = compatibleHeaders;
 	}
 
 	public String getRegexExtractor() {
 		return regexExtractor;
 	}
 
 	public void setRegexExtractor(String regexExtractor) {
 		this.regexExtractor = regexExtractor;
 	}
 
 	public String getApplyTo() {
 		return applyTo;
 	}
 
 	public void setApplyTo(String applyTo) {
 		this.applyTo = applyTo;
 	}
 
 	public String getResponseField() {
 		return responseField;
 	}
 
 	public void setResponseField(String responseField) {
 		this.responseField = responseField;
 	}
 
 	public String getReferenceName() {
 		return referenceName;
 	}
 
 	public void setReferenceName(String referenceName) {
 		this.referenceName = referenceName;
 	}
 
 	public String getRegex() {
 		return regex;
 	}
 
 	public void setRegex(String regex) {
 		this.regex = regex;
 	}
 
 	public String getTemplate() {
 		return template;
 	}
 
 	public void setTemplate(String template) {
 		this.template = template;
 	}
 
 	public String getMatchNo() {
 		return matchNo;
 	}
 
 	public void setMatchNo(String matchNo) {
 		this.matchNo = matchNo;
 	}
 
 	public String getDefaultValue() {
 		return defaultValue;
 	}
 
 	public void setDefaultValue(String defaultValue) {
 		this.defaultValue = defaultValue;
 	}
 
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((addHeader == null) ? 0 : addHeader.hashCode());
 		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
 		result = prime * result
 				+ ((appDirName == null) ? 0 : appDirName.hashCode());
 		result = prime * result + ((appName == null) ? 0 : appName.hashCode());
 		result = prime * result + ((applyTo == null) ? 0 : applyTo.hashCode());
 		result = prime
 				* result
 				+ ((attachmentsPattern == null) ? 0 : attachmentsPattern
 						.hashCode());
 		result = prime * result
 				+ ((authManager == null) ? 0 : authManager.hashCode());
 		result = prime
 				* result
 				+ ((authorizationDomain == null) ? 0 : authorizationDomain
 						.hashCode());
 		result = prime
 				* result
 				+ ((authorizationPassword == null) ? 0 : authorizationPassword
 						.hashCode());
 		result = prime
 				* result
 				+ ((authorizationRealm == null) ? 0 : authorizationRealm
 						.hashCode());
 		result = prime
 				* result
 				+ ((authorizationUrl == null) ? 0 : authorizationUrl.hashCode());
 		result = prime
 				* result
 				+ ((authorizationUserName == null) ? 0 : authorizationUserName
 						.hashCode());
 		result = prime * result
 				+ ((availableJmx == null) ? 0 : availableJmx.hashCode());
 		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
 		result = prime * result + ((browser == null) ? 0 : browser.hashCode());
 		result = prime
 				* result
 				+ ((buildEnvironmentName == null) ? 0 : buildEnvironmentName
 						.hashCode());
 		result = prime * result
 				+ ((buildName == null) ? 0 : buildName.hashCode());
 		result = prime * result
 				+ ((buildNumber == null) ? 0 : buildNumber.hashCode());
 		result = prime * result + (cloneWorkspace ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((clonnedWorkspaceName == null) ? 0 : clonnedWorkspaceName
 						.hashCode());
 		result = prime * result + (coberturaPlugin ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((collabNetFileReleasePattern == null) ? 0
 						: collabNetFileReleasePattern.hashCode());
 		result = prime
 				* result
 				+ ((collabNetPackage == null) ? 0 : collabNetPackage.hashCode());
 		result = prime
 				* result
 				+ ((collabNetProject == null) ? 0 : collabNetProject.hashCode());
 		result = prime
 				* result
 				+ ((collabNetRelease == null) ? 0 : collabNetRelease.hashCode());
 		result = prime * result
 				+ ((collabNetURL == null) ? 0 : collabNetURL.hashCode());
 		result = prime * result + (collabNetoverWriteFiles ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((collabNetpassword == null) ? 0 : collabNetpassword
 						.hashCode());
 		result = prime
 				* result
 				+ ((collabNetusername == null) ? 0 : collabNetusername
 						.hashCode());
 		result = prime
 				* result
 				+ ((compatibleHeaders == null) ? 0 : compatibleHeaders
 						.hashCode());
 		result = prime * result
 				+ ((configuration == null) ? 0 : configuration.hashCode());
 		result = prime * result
 				+ ((configurations == null) ? 0 : configurations.hashCode());
 		result = prime * result + (confluenceArtifacts ? 1231 : 1237);
 		result = prime * result
 				+ ((confluenceOther == null) ? 0 : confluenceOther.hashCode());
 		result = prime * result
 				+ ((confluencePage == null) ? 0 : confluencePage.hashCode());
 		result = prime * result + (confluencePublish ? 1231 : 1237);
 		result = prime * result
 				+ ((confluenceSite == null) ? 0 : confluenceSite.hashCode());
 		result = prime * result
 				+ ((confluenceSpace == null) ? 0 : confluenceSpace.hashCode());
 		result = prime * result + ((context == null) ? 0 : context.hashCode());
 		result = prime * result
 				+ ((contextType == null) ? 0 : contextType.hashCode());
 		result = prime * result
 				+ ((contextUrls == null) ? 0 : contextUrls.hashCode());
 		result = prime
 				* result
 				+ ((customTestAgainst == null) ? 0 : customTestAgainst
 						.hashCode());
 		result = prime * result
 				+ ((dataBase == null) ? 0 : dataBase.hashCode());
 		result = prime * result
 				+ ((dbContextUrls == null) ? 0 : dbContextUrls.hashCode());
 		result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
 		result = prime * result
 				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
 		result = prime * result
 				+ ((deviceId == null) ? 0 : deviceId.hashCode());
 		result = prime
 				* result
 				+ ((deviceKeyPassword == null) ? 0 : deviceKeyPassword
 						.hashCode());
 		result = prime * result
 				+ ((deviceList == null) ? 0 : deviceList.hashCode());
 		result = prime * result
 				+ ((deviceType == null) ? 0 : deviceType.hashCode());
 		result = prime * result + ((devices == null) ? 0 : devices.hashCode());
 		result = prime
 				* result
 				+ ((downStreamCriteria == null) ? 0 : downStreamCriteria
 						.hashCode());
 		result = prime
 				* result
 				+ ((downstreamApplication == null) ? 0 : downstreamApplication
 						.hashCode());
 		result = prime * result + ((email == null) ? 0 : email.hashCode());
 		result = prime
 				* result
 				+ ((emulatorKeyPassword == null) ? 0 : emulatorKeyPassword
 						.hashCode());
 		result = prime * result + (enableArtifactArchiver ? 1231 : 1237);
 		result = prime * result + (enableBuildRelease ? 1231 : 1237);
 		result = prime * result + (enableConfluence ? 1231 : 1237);
 		result = prime * result + (enablePostBuildStep ? 1231 : 1237);
 		result = prime * result + (enablePreBuildStep ? 1231 : 1237);
 		result = prime * result
 				+ ((encodingType == null) ? 0 : encodingType.hashCode());
 		result = prime * result + ((encrypt == null) ? 0 : encrypt.hashCode());
 		result = prime * result
 				+ ((environmentName == null) ? 0 : environmentName.hashCode());
 		result = prime * result
 				+ ((executeSql == null) ? 0 : executeSql.hashCode());
 		result = prime * result
 				+ ((failureEmailIds == null) ? 0 : failureEmailIds.hashCode());
 		result = prime * result + ((family == null) ? 0 : family.hashCode());
 		result = prime * result
 				+ ((fetchSql == null) ? 0 : fetchSql.hashCode());
 		result = prime * result
 				+ ((followRedirects == null) ? 0 : followRedirects.hashCode());
 		result = prime * result
 				+ ((headerKey == null) ? 0 : headerKey.hashCode());
 		result = prime * result
 				+ ((headerValue == null) ? 0 : headerValue.hashCode());
 		result = prime * result
 				+ ((httpName == null) ? 0 : httpName.hashCode());
 		result = prime * result
 				+ ((isFromCI == null) ? 0 : isFromCI.hashCode());
 		result = prime * result
 				+ ((jarLocation == null) ? 0 : jarLocation.hashCode());
 		result = prime * result + ((jarName == null) ? 0 : jarName.hashCode());
 		result = prime * result
 				+ ((jenkinsPort == null) ? 0 : jenkinsPort.hashCode());
 		result = prime * result
 				+ ((jenkinsUrl == null) ? 0 : jenkinsUrl.hashCode());
 		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
 		result = prime * result
 				+ ((keepAlive == null) ? 0 : keepAlive.hashCode());
 		result = prime * result
 				+ ((keyPassword == null) ? 0 : keyPassword.hashCode());
 		result = prime * result + ((keypass == null) ? 0 : keypass.hashCode());
 		result = prime * result
 				+ ((keystore == null) ? 0 : keystore.hashCode());
 		result = prime * result
 				+ ((loadContextUrl == null) ? 0 : loadContextUrl.hashCode());
 		result = prime * result + ((logo == null) ? 0 : logo.hashCode());
 		result = prime * result + ((logs == null) ? 0 : logs.hashCode());
 		result = prime * result
 				+ ((loopCount == null) ? 0 : loopCount.hashCode());
 		result = prime * result
 				+ ((mainClassName == null) ? 0 : mainClassName.hashCode());
 		result = prime * result + ((matchNo == null) ? 0 : matchNo.hashCode());
 		result = prime * result + ((minify == null) ? 0 : minify.hashCode());
 		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
 		result = prime * result
 				+ ((multipartData == null) ? 0 : multipartData.hashCode());
 		result = prime * result
 				+ ((mvnCommand == null) ? 0 : mvnCommand.hashCode());
 		result = prime * result
 				+ ((noOfUsers == null) ? 0 : noOfUsers.hashCode());
 		result = prime * result
 				+ ((operation == null) ? 0 : operation.hashCode());
 		result = prime
 				* result
 				+ ((packMinifiedFiles == null) ? 0 : packMinifiedFiles
 						.hashCode());
 		result = prime
 				* result
 				+ ((packageFileBrowse == null) ? 0 : packageFileBrowse
 						.hashCode());
 		result = prime * result
 				+ ((parameterName == null) ? 0 : parameterName.hashCode());
 		result = prime * result
 				+ ((parameterValue == null) ? 0 : parameterValue.hashCode());
 		result = prime * result
 				+ ((password == null) ? 0 : password.hashCode());
 		result = prime * result
 				+ ((platform == null) ? 0 : platform.hashCode());
 		result = prime * result
 				+ ((plistFile == null) ? 0 : plistFile.hashCode());
 		result = prime * result
 				+ ((pomLocation == null) ? 0 : pomLocation.hashCode());
 		result = prime
 				* result
 				+ ((postbuildStepCommands == null) ? 0 : postbuildStepCommands
 						.hashCode());
 		result = prime
 				* result
 				+ ((prebuildStepCommands == null) ? 0 : prebuildStepCommands
 						.hashCode());
 		result = prime * result
 				+ ((proguard == null) ? 0 : proguard.hashCode());
 		result = prime * result
 				+ ((projectModule == null) ? 0 : projectModule.hashCode());
 		result = prime * result
 				+ ((projectType == null) ? 0 : projectType.hashCode());
 		result = prime * result + ((query == null) ? 0 : query.hashCode());
 		result = prime * result
 				+ ((queryType == null) ? 0 : queryType.hashCode());
 		result = prime * result
 				+ ((rampUpPeriod == null) ? 0 : rampUpPeriod.hashCode());
 		result = prime
 				* result
 				+ ((redirectAutomatically == null) ? 0 : redirectAutomatically
 						.hashCode());
 		result = prime * result
 				+ ((referenceName == null) ? 0 : referenceName.hashCode());
 		result = prime * result + ((regex == null) ? 0 : regex.hashCode());
 		result = prime * result
 				+ ((regexExtractor == null) ? 0 : regexExtractor.hashCode());
 		result = prime * result
 				+ ((repoType == null) ? 0 : repoType.hashCode());
 		result = prime * result
 				+ ((reportName == null) ? 0 : reportName.hashCode());
 		result = prime * result
 				+ ((reportType == null) ? 0 : reportType.hashCode());
 		result = prime * result
 				+ ((resolution == null) ? 0 : resolution.hashCode());
 		result = prime * result
 				+ ((responseField == null) ? 0 : responseField.hashCode());
 		result = prime
 				* result
 				+ ((scheduleExpression == null) ? 0 : scheduleExpression
 						.hashCode());
 		result = prime * result
 				+ ((scheduleType == null) ? 0 : scheduleType.hashCode());
 		result = prime * result + ((sdk == null) ? 0 : sdk.hashCode());
 		result = prime * result
 				+ ((sdkVersion == null) ? 0 : sdkVersion.hashCode());
 		result = prime * result
 				+ ((serialNumber == null) ? 0 : serialNumber.hashCode());
 		result = prime * result
 				+ ((showSettings == null) ? 0 : showSettings.hashCode());
 		result = prime * result + ((signing == null) ? 0 : signing.hashCode());
 		result = prime * result
 				+ ((skipTest == null) ? 0 : skipTest.hashCode());
 		result = prime * result
 				+ ((skipTests == null) ? 0 : skipTests.hashCode());
 		result = prime * result + ((sonar == null) ? 0 : sonar.hashCode());
 		result = prime * result
 				+ ((sonarUrl == null) ? 0 : sonarUrl.hashCode());
 		result = prime * result + ((src == null) ? 0 : src.hashCode());
 		result = prime * result
 				+ ((storepass == null) ? 0 : storepass.hashCode());
 		result = prime * result
 				+ ((successEmailIds == null) ? 0 : successEmailIds.hashCode());
 		result = prime * result + ((target == null) ? 0 : target.hashCode());
 		result = prime * result
 				+ ((technologyName == null) ? 0 : technologyName.hashCode());
 		result = prime * result
 				+ ((template == null) ? 0 : template.hashCode());
 		result = prime * result
 				+ ((templateName == null) ? 0 : templateName.hashCode());
 		result = prime * result
 				+ ((testAgainst == null) ? 0 : testAgainst.hashCode());
 		result = prime * result
 				+ ((testBasis == null) ? 0 : testBasis.hashCode());
 		result = prime * result
 				+ ((testName == null) ? 0 : testName.hashCode());
 		result = prime * result
 				+ ((testType == null) ? 0 : testType.hashCode());
 		result = prime * result + ((theme == null) ? 0 : theme.hashCode());
 		result = prime
 				* result
 				+ ((triggerSimulator == null) ? 0 : triggerSimulator.hashCode());
 		result = prime * result
 				+ ((triggers == null) ? 0 : triggers.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		result = prime * result + (unitTestType ? 1231 : 1237);
 		result = prime * result
 				+ ((unittest == null) ? 0 : unittest.hashCode());
 		result = prime
 				* result
 				+ ((upstreamApplication == null) ? 0 : upstreamApplication
 						.hashCode());
 		result = prime * result + ((url == null) ? 0 : url.hashCode());
 		result = prime
 				* result
 				+ ((usedClonnedWorkspace == null) ? 0 : usedClonnedWorkspace
 						.hashCode());
 		result = prime * result
 				+ ((username == null) ? 0 : username.hashCode());
 		result = prime * result
 				+ ((zipAlign == null) ? 0 : zipAlign.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		CIJob other = (CIJob) obj;
 		if (addHeader == null) {
 			if (other.addHeader != null)
 				return false;
 		} else if (!addHeader.equals(other.addHeader))
 			return false;
 		if (alias == null) {
 			if (other.alias != null)
 				return false;
 		} else if (!alias.equals(other.alias))
 			return false;
 		if (appDirName == null) {
 			if (other.appDirName != null)
 				return false;
 		} else if (!appDirName.equals(other.appDirName))
 			return false;
 		if (appName == null) {
 			if (other.appName != null)
 				return false;
 		} else if (!appName.equals(other.appName))
 			return false;
 		if (applyTo == null) {
 			if (other.applyTo != null)
 				return false;
 		} else if (!applyTo.equals(other.applyTo))
 			return false;
 		if (attachmentsPattern == null) {
 			if (other.attachmentsPattern != null)
 				return false;
 		} else if (!attachmentsPattern.equals(other.attachmentsPattern))
 			return false;
 		if (authManager == null) {
 			if (other.authManager != null)
 				return false;
 		} else if (!authManager.equals(other.authManager))
 			return false;
 		if (authorizationDomain == null) {
 			if (other.authorizationDomain != null)
 				return false;
 		} else if (!authorizationDomain.equals(other.authorizationDomain))
 			return false;
 		if (authorizationPassword == null) {
 			if (other.authorizationPassword != null)
 				return false;
 		} else if (!authorizationPassword.equals(other.authorizationPassword))
 			return false;
 		if (authorizationRealm == null) {
 			if (other.authorizationRealm != null)
 				return false;
 		} else if (!authorizationRealm.equals(other.authorizationRealm))
 			return false;
 		if (authorizationUrl == null) {
 			if (other.authorizationUrl != null)
 				return false;
 		} else if (!authorizationUrl.equals(other.authorizationUrl))
 			return false;
 		if (authorizationUserName == null) {
 			if (other.authorizationUserName != null)
 				return false;
 		} else if (!authorizationUserName.equals(other.authorizationUserName))
 			return false;
 		if (availableJmx == null) {
 			if (other.availableJmx != null)
 				return false;
 		} else if (!availableJmx.equals(other.availableJmx))
 			return false;
 		if (branch == null) {
 			if (other.branch != null)
 				return false;
 		} else if (!branch.equals(other.branch))
 			return false;
 		if (browser == null) {
 			if (other.browser != null)
 				return false;
 		} else if (!browser.equals(other.browser))
 			return false;
 		if (buildEnvironmentName == null) {
 			if (other.buildEnvironmentName != null)
 				return false;
 		} else if (!buildEnvironmentName.equals(other.buildEnvironmentName))
 			return false;
 		if (buildName == null) {
 			if (other.buildName != null)
 				return false;
 		} else if (!buildName.equals(other.buildName))
 			return false;
 		if (buildNumber == null) {
 			if (other.buildNumber != null)
 				return false;
 		} else if (!buildNumber.equals(other.buildNumber))
 			return false;
 		if (cloneWorkspace != other.cloneWorkspace)
 			return false;
 		if (clonnedWorkspaceName == null) {
 			if (other.clonnedWorkspaceName != null)
 				return false;
 		} else if (!clonnedWorkspaceName.equals(other.clonnedWorkspaceName))
 			return false;
 		if (coberturaPlugin != other.coberturaPlugin)
 			return false;
 		if (collabNetFileReleasePattern == null) {
 			if (other.collabNetFileReleasePattern != null)
 				return false;
 		} else if (!collabNetFileReleasePattern
 				.equals(other.collabNetFileReleasePattern))
 			return false;
 		if (collabNetPackage == null) {
 			if (other.collabNetPackage != null)
 				return false;
 		} else if (!collabNetPackage.equals(other.collabNetPackage))
 			return false;
 		if (collabNetProject == null) {
 			if (other.collabNetProject != null)
 				return false;
 		} else if (!collabNetProject.equals(other.collabNetProject))
 			return false;
 		if (collabNetRelease == null) {
 			if (other.collabNetRelease != null)
 				return false;
 		} else if (!collabNetRelease.equals(other.collabNetRelease))
 			return false;
 		if (collabNetURL == null) {
 			if (other.collabNetURL != null)
 				return false;
 		} else if (!collabNetURL.equals(other.collabNetURL))
 			return false;
 		if (collabNetoverWriteFiles != other.collabNetoverWriteFiles)
 			return false;
 		if (collabNetpassword == null) {
 			if (other.collabNetpassword != null)
 				return false;
 		} else if (!collabNetpassword.equals(other.collabNetpassword))
 			return false;
 		if (collabNetusername == null) {
 			if (other.collabNetusername != null)
 				return false;
 		} else if (!collabNetusername.equals(other.collabNetusername))
 			return false;
 		if (compatibleHeaders == null) {
 			if (other.compatibleHeaders != null)
 				return false;
 		} else if (!compatibleHeaders.equals(other.compatibleHeaders))
 			return false;
 		if (configuration == null) {
 			if (other.configuration != null)
 				return false;
 		} else if (!configuration.equals(other.configuration))
 			return false;
 		if (configurations == null) {
 			if (other.configurations != null)
 				return false;
 		} else if (!configurations.equals(other.configurations))
 			return false;
 		if (confluenceArtifacts != other.confluenceArtifacts)
 			return false;
 		if (confluenceOther == null) {
 			if (other.confluenceOther != null)
 				return false;
 		} else if (!confluenceOther.equals(other.confluenceOther))
 			return false;
 		if (confluencePage == null) {
 			if (other.confluencePage != null)
 				return false;
 		} else if (!confluencePage.equals(other.confluencePage))
 			return false;
 		if (confluencePublish != other.confluencePublish)
 			return false;
 		if (confluenceSite == null) {
 			if (other.confluenceSite != null)
 				return false;
 		} else if (!confluenceSite.equals(other.confluenceSite))
 			return false;
 		if (confluenceSpace == null) {
 			if (other.confluenceSpace != null)
 				return false;
 		} else if (!confluenceSpace.equals(other.confluenceSpace))
 			return false;
 		if (context == null) {
 			if (other.context != null)
 				return false;
 		} else if (!context.equals(other.context))
 			return false;
 		if (contextType == null) {
 			if (other.contextType != null)
 				return false;
 		} else if (!contextType.equals(other.contextType))
 			return false;
 		if (contextUrls == null) {
 			if (other.contextUrls != null)
 				return false;
 		} else if (!contextUrls.equals(other.contextUrls))
 			return false;
 		if (customTestAgainst == null) {
 			if (other.customTestAgainst != null)
 				return false;
 		} else if (!customTestAgainst.equals(other.customTestAgainst))
 			return false;
 		if (dataBase == null) {
 			if (other.dataBase != null)
 				return false;
 		} else if (!dataBase.equals(other.dataBase))
 			return false;
 		if (dbContextUrls == null) {
 			if (other.dbContextUrls != null)
 				return false;
 		} else if (!dbContextUrls.equals(other.dbContextUrls))
 			return false;
 		if (dbName == null) {
 			if (other.dbName != null)
 				return false;
 		} else if (!dbName.equals(other.dbName))
 			return false;
 		if (defaultValue == null) {
 			if (other.defaultValue != null)
 				return false;
 		} else if (!defaultValue.equals(other.defaultValue))
 			return false;
 		if (deviceId == null) {
 			if (other.deviceId != null)
 				return false;
 		} else if (!deviceId.equals(other.deviceId))
 			return false;
 		if (deviceKeyPassword == null) {
 			if (other.deviceKeyPassword != null)
 				return false;
 		} else if (!deviceKeyPassword.equals(other.deviceKeyPassword))
 			return false;
 		if (deviceList == null) {
 			if (other.deviceList != null)
 				return false;
 		} else if (!deviceList.equals(other.deviceList))
 			return false;
 		if (deviceType == null) {
 			if (other.deviceType != null)
 				return false;
 		} else if (!deviceType.equals(other.deviceType))
 			return false;
 		if (devices == null) {
 			if (other.devices != null)
 				return false;
 		} else if (!devices.equals(other.devices))
 			return false;
 		if (downStreamCriteria == null) {
 			if (other.downStreamCriteria != null)
 				return false;
 		} else if (!downStreamCriteria.equals(other.downStreamCriteria))
 			return false;
 		if (downstreamApplication == null) {
 			if (other.downstreamApplication != null)
 				return false;
 		} else if (!downstreamApplication.equals(other.downstreamApplication))
 			return false;
 		if (email == null) {
 			if (other.email != null)
 				return false;
 		} else if (!email.equals(other.email))
 			return false;
 		if (emulatorKeyPassword == null) {
 			if (other.emulatorKeyPassword != null)
 				return false;
 		} else if (!emulatorKeyPassword.equals(other.emulatorKeyPassword))
 			return false;
 		if (enableArtifactArchiver != other.enableArtifactArchiver)
 			return false;
 		if (enableBuildRelease != other.enableBuildRelease)
 			return false;
 		if (enableConfluence != other.enableConfluence)
 			return false;
 		if (enablePostBuildStep != other.enablePostBuildStep)
 			return false;
 		if (enablePreBuildStep != other.enablePreBuildStep)
 			return false;
 		if (encodingType == null) {
 			if (other.encodingType != null)
 				return false;
 		} else if (!encodingType.equals(other.encodingType))
 			return false;
 		if (encrypt == null) {
 			if (other.encrypt != null)
 				return false;
 		} else if (!encrypt.equals(other.encrypt))
 			return false;
 		if (environmentName == null) {
 			if (other.environmentName != null)
 				return false;
 		} else if (!environmentName.equals(other.environmentName))
 			return false;
 		if (executeSql == null) {
 			if (other.executeSql != null)
 				return false;
 		} else if (!executeSql.equals(other.executeSql))
 			return false;
 		if (failureEmailIds == null) {
 			if (other.failureEmailIds != null)
 				return false;
 		} else if (!failureEmailIds.equals(other.failureEmailIds))
 			return false;
 		if (family == null) {
 			if (other.family != null)
 				return false;
 		} else if (!family.equals(other.family))
 			return false;
 		if (fetchSql == null) {
 			if (other.fetchSql != null)
 				return false;
 		} else if (!fetchSql.equals(other.fetchSql))
 			return false;
 		if (followRedirects == null) {
 			if (other.followRedirects != null)
 				return false;
 		} else if (!followRedirects.equals(other.followRedirects))
 			return false;
 		if (headerKey == null) {
 			if (other.headerKey != null)
 				return false;
 		} else if (!headerKey.equals(other.headerKey))
 			return false;
 		if (headerValue == null) {
 			if (other.headerValue != null)
 				return false;
 		} else if (!headerValue.equals(other.headerValue))
 			return false;
 		if (httpName == null) {
 			if (other.httpName != null)
 				return false;
 		} else if (!httpName.equals(other.httpName))
 			return false;
 		if (isFromCI == null) {
 			if (other.isFromCI != null)
 				return false;
 		} else if (!isFromCI.equals(other.isFromCI))
 			return false;
 		if (jarLocation == null) {
 			if (other.jarLocation != null)
 				return false;
 		} else if (!jarLocation.equals(other.jarLocation))
 			return false;
 		if (jarName == null) {
 			if (other.jarName != null)
 				return false;
 		} else if (!jarName.equals(other.jarName))
 			return false;
 		if (jenkinsPort == null) {
 			if (other.jenkinsPort != null)
 				return false;
 		} else if (!jenkinsPort.equals(other.jenkinsPort))
 			return false;
 		if (jenkinsUrl == null) {
 			if (other.jenkinsUrl != null)
 				return false;
 		} else if (!jenkinsUrl.equals(other.jenkinsUrl))
 			return false;
 		if (jobName == null) {
 			if (other.jobName != null)
 				return false;
 		} else if (!jobName.equals(other.jobName))
 			return false;
 		if (keepAlive == null) {
 			if (other.keepAlive != null)
 				return false;
 		} else if (!keepAlive.equals(other.keepAlive))
 			return false;
 		if (keyPassword == null) {
 			if (other.keyPassword != null)
 				return false;
 		} else if (!keyPassword.equals(other.keyPassword))
 			return false;
 		if (keypass == null) {
 			if (other.keypass != null)
 				return false;
 		} else if (!keypass.equals(other.keypass))
 			return false;
 		if (keystore == null) {
 			if (other.keystore != null)
 				return false;
 		} else if (!keystore.equals(other.keystore))
 			return false;
 		if (loadContextUrl == null) {
 			if (other.loadContextUrl != null)
 				return false;
 		} else if (!loadContextUrl.equals(other.loadContextUrl))
 			return false;
 		if (logo == null) {
 			if (other.logo != null)
 				return false;
 		} else if (!logo.equals(other.logo))
 			return false;
 		if (logs == null) {
 			if (other.logs != null)
 				return false;
 		} else if (!logs.equals(other.logs))
 			return false;
 		if (loopCount == null) {
 			if (other.loopCount != null)
 				return false;
 		} else if (!loopCount.equals(other.loopCount))
 			return false;
 		if (mainClassName == null) {
 			if (other.mainClassName != null)
 				return false;
 		} else if (!mainClassName.equals(other.mainClassName))
 			return false;
 		if (matchNo == null) {
 			if (other.matchNo != null)
 				return false;
 		} else if (!matchNo.equals(other.matchNo))
 			return false;
 		if (minify == null) {
 			if (other.minify != null)
 				return false;
 		} else if (!minify.equals(other.minify))
 			return false;
 		if (mode == null) {
 			if (other.mode != null)
 				return false;
 		} else if (!mode.equals(other.mode))
 			return false;
 		if (multipartData == null) {
 			if (other.multipartData != null)
 				return false;
 		} else if (!multipartData.equals(other.multipartData))
 			return false;
 		if (mvnCommand == null) {
 			if (other.mvnCommand != null)
 				return false;
 		} else if (!mvnCommand.equals(other.mvnCommand))
 			return false;
 		if (noOfUsers == null) {
 			if (other.noOfUsers != null)
 				return false;
 		} else if (!noOfUsers.equals(other.noOfUsers))
 			return false;
 		if (operation == null) {
 			if (other.operation != null)
 				return false;
 		} else if (!operation.equals(other.operation))
 			return false;
 		if (packMinifiedFiles == null) {
 			if (other.packMinifiedFiles != null)
 				return false;
 		} else if (!packMinifiedFiles.equals(other.packMinifiedFiles))
 			return false;
 		if (packageFileBrowse == null) {
 			if (other.packageFileBrowse != null)
 				return false;
 		} else if (!packageFileBrowse.equals(other.packageFileBrowse))
 			return false;
 		if (parameterName == null) {
 			if (other.parameterName != null)
 				return false;
 		} else if (!parameterName.equals(other.parameterName))
 			return false;
 		if (parameterValue == null) {
 			if (other.parameterValue != null)
 				return false;
 		} else if (!parameterValue.equals(other.parameterValue))
 			return false;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (platform == null) {
 			if (other.platform != null)
 				return false;
 		} else if (!platform.equals(other.platform))
 			return false;
 		if (plistFile == null) {
 			if (other.plistFile != null)
 				return false;
 		} else if (!plistFile.equals(other.plistFile))
 			return false;
 		if (pomLocation == null) {
 			if (other.pomLocation != null)
 				return false;
 		} else if (!pomLocation.equals(other.pomLocation))
 			return false;
 		if (postbuildStepCommands == null) {
 			if (other.postbuildStepCommands != null)
 				return false;
 		} else if (!postbuildStepCommands.equals(other.postbuildStepCommands))
 			return false;
 		if (prebuildStepCommands == null) {
 			if (other.prebuildStepCommands != null)
 				return false;
 		} else if (!prebuildStepCommands.equals(other.prebuildStepCommands))
 			return false;
 		if (proguard == null) {
 			if (other.proguard != null)
 				return false;
 		} else if (!proguard.equals(other.proguard))
 			return false;
 		if (projectModule == null) {
 			if (other.projectModule != null)
 				return false;
 		} else if (!projectModule.equals(other.projectModule))
 			return false;
 		if (projectType == null) {
 			if (other.projectType != null)
 				return false;
 		} else if (!projectType.equals(other.projectType))
 			return false;
 		if (query == null) {
 			if (other.query != null)
 				return false;
 		} else if (!query.equals(other.query))
 			return false;
 		if (queryType == null) {
 			if (other.queryType != null)
 				return false;
 		} else if (!queryType.equals(other.queryType))
 			return false;
 		if (rampUpPeriod == null) {
 			if (other.rampUpPeriod != null)
 				return false;
 		} else if (!rampUpPeriod.equals(other.rampUpPeriod))
 			return false;
 		if (redirectAutomatically == null) {
 			if (other.redirectAutomatically != null)
 				return false;
 		} else if (!redirectAutomatically.equals(other.redirectAutomatically))
 			return false;
 		if (referenceName == null) {
 			if (other.referenceName != null)
 				return false;
 		} else if (!referenceName.equals(other.referenceName))
 			return false;
 		if (regex == null) {
 			if (other.regex != null)
 				return false;
 		} else if (!regex.equals(other.regex))
 			return false;
 		if (regexExtractor == null) {
 			if (other.regexExtractor != null)
 				return false;
 		} else if (!regexExtractor.equals(other.regexExtractor))
 			return false;
 		if (repoType == null) {
 			if (other.repoType != null)
 				return false;
 		} else if (!repoType.equals(other.repoType))
 			return false;
 		if (reportName == null) {
 			if (other.reportName != null)
 				return false;
 		} else if (!reportName.equals(other.reportName))
 			return false;
 		if (reportType == null) {
 			if (other.reportType != null)
 				return false;
 		} else if (!reportType.equals(other.reportType))
 			return false;
 		if (resolution == null) {
 			if (other.resolution != null)
 				return false;
 		} else if (!resolution.equals(other.resolution))
 			return false;
 		if (responseField == null) {
 			if (other.responseField != null)
 				return false;
 		} else if (!responseField.equals(other.responseField))
 			return false;
 		if (scheduleExpression == null) {
 			if (other.scheduleExpression != null)
 				return false;
 		} else if (!scheduleExpression.equals(other.scheduleExpression))
 			return false;
 		if (scheduleType == null) {
 			if (other.scheduleType != null)
 				return false;
 		} else if (!scheduleType.equals(other.scheduleType))
 			return false;
 		if (sdk == null) {
 			if (other.sdk != null)
 				return false;
 		} else if (!sdk.equals(other.sdk))
 			return false;
 		if (sdkVersion == null) {
 			if (other.sdkVersion != null)
 				return false;
 		} else if (!sdkVersion.equals(other.sdkVersion))
 			return false;
 		if (serialNumber == null) {
 			if (other.serialNumber != null)
 				return false;
 		} else if (!serialNumber.equals(other.serialNumber))
 			return false;
 		if (showSettings == null) {
 			if (other.showSettings != null)
 				return false;
 		} else if (!showSettings.equals(other.showSettings))
 			return false;
 		if (signing == null) {
 			if (other.signing != null)
 				return false;
 		} else if (!signing.equals(other.signing))
 			return false;
 		if (skipTest == null) {
 			if (other.skipTest != null)
 				return false;
 		} else if (!skipTest.equals(other.skipTest))
 			return false;
 		if (skipTests == null) {
 			if (other.skipTests != null)
 				return false;
 		} else if (!skipTests.equals(other.skipTests))
 			return false;
 		if (sonar == null) {
 			if (other.sonar != null)
 				return false;
 		} else if (!sonar.equals(other.sonar))
 			return false;
 		if (sonarUrl == null) {
 			if (other.sonarUrl != null)
 				return false;
 		} else if (!sonarUrl.equals(other.sonarUrl))
 			return false;
 		if (src == null) {
 			if (other.src != null)
 				return false;
 		} else if (!src.equals(other.src))
 			return false;
 		if (storepass == null) {
 			if (other.storepass != null)
 				return false;
 		} else if (!storepass.equals(other.storepass))
 			return false;
 		if (successEmailIds == null) {
 			if (other.successEmailIds != null)
 				return false;
 		} else if (!successEmailIds.equals(other.successEmailIds))
 			return false;
 		if (target == null) {
 			if (other.target != null)
 				return false;
 		} else if (!target.equals(other.target))
 			return false;
 		if (technologyName == null) {
 			if (other.technologyName != null)
 				return false;
 		} else if (!technologyName.equals(other.technologyName))
 			return false;
 		if (template == null) {
 			if (other.template != null)
 				return false;
 		} else if (!template.equals(other.template))
 			return false;
 		if (templateName == null) {
 			if (other.templateName != null)
 				return false;
 		} else if (!templateName.equals(other.templateName))
 			return false;
 		if (testAgainst == null) {
 			if (other.testAgainst != null)
 				return false;
 		} else if (!testAgainst.equals(other.testAgainst))
 			return false;
 		if (testBasis == null) {
 			if (other.testBasis != null)
 				return false;
 		} else if (!testBasis.equals(other.testBasis))
 			return false;
 		if (testName == null) {
 			if (other.testName != null)
 				return false;
 		} else if (!testName.equals(other.testName))
 			return false;
 		if (testType == null) {
 			if (other.testType != null)
 				return false;
 		} else if (!testType.equals(other.testType))
 			return false;
 		if (theme == null) {
 			if (other.theme != null)
 				return false;
 		} else if (!theme.equals(other.theme))
 			return false;
 		if (triggerSimulator == null) {
 			if (other.triggerSimulator != null)
 				return false;
 		} else if (!triggerSimulator.equals(other.triggerSimulator))
 			return false;
 		if (triggers == null) {
 			if (other.triggers != null)
 				return false;
 		} else if (!triggers.equals(other.triggers))
 			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
 		if (unitTestType != other.unitTestType)
 			return false;
 		if (unittest == null) {
 			if (other.unittest != null)
 				return false;
 		} else if (!unittest.equals(other.unittest))
 			return false;
 		if (upstreamApplication == null) {
 			if (other.upstreamApplication != null)
 				return false;
 		} else if (!upstreamApplication.equals(other.upstreamApplication))
 			return false;
 		if (url == null) {
 			if (other.url != null)
 				return false;
 		} else if (!url.equals(other.url))
 			return false;
 		if (usedClonnedWorkspace == null) {
 			if (other.usedClonnedWorkspace != null)
 				return false;
 		} else if (!usedClonnedWorkspace.equals(other.usedClonnedWorkspace))
 			return false;
 		if (username == null) {
 			if (other.username != null)
 				return false;
 		} else if (!username.equals(other.username))
 			return false;
 		if (zipAlign == null) {
 			if (other.zipAlign != null)
 				return false;
 		} else if (!zipAlign.equals(other.zipAlign))
 			return false;
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "CIJob [name=" + jobName + ", svnUrl=" + url + ", email="
 				+ email + ", scheduleExpression=" + scheduleExpression
 				+ ", mvnCommand=" + mvnCommand + ", jenkinsUrl=" + jenkinsUrl
 				+ ", jenkinsPort=" + jenkinsPort + ", triggers=" + triggers
 				+ ", repoType=" + repoType + ", downStreamApplication="
 				+ downstreamApplication + ", downStreamCriteria="
 				+ downStreamCriteria + ", usedClonnedWorkspace="
 				+ usedClonnedWorkspace + ", operation=" + operation
 				+ ", pomLocation=" + pomLocation + ", enablePostBuildStep="
 				+ enablePostBuildStep + ", enablePreBuildStep="
 				+ enablePreBuildStep + ", prebuildStepCommands="
 				+ prebuildStepCommands + ", postbuildStepCommands="
 				+ postbuildStepCommands + ", attachmentsPattern="
 				+ attachmentsPattern + ", enableArtifactArchiver="
 				+ enableArtifactArchiver + "]";
 	}
 }
