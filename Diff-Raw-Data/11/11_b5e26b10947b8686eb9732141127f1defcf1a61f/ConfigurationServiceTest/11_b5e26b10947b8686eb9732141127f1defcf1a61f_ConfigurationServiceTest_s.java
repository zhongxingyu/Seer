 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.json.simple.parser.ParseException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.model.AddCertificateInfo;
 import com.photon.phresco.framework.model.CronExpressionInfo;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.impl.HtmlApplicationProcessor;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class ConfigurationServiceTest extends LoginServiceTest {
 	
 	ConfigurationService configurationService = new ConfigurationService();
 	
 	@BeforeClass	
     public static void oneTimeSetUp() throws PhrescoException, PhrescoPomException {
     	PomProcessor pomProcessor = FrameworkServiceUtil.getPomProcessor("TestProject");
     	pomProcessor.setProperty("phresco.theme.target.dir", "/do_not_checkin/theme");
     	pomProcessor.save();
     }
 	
 	@Test
 	public void getAllEnvironmentsTest() {
 		Response response = configurationService.getAllEnvironments(appDirName, "", "true","","");
 		Assert.assertEquals(200, response.getStatus());
 //		Response responseFail = configurationService.getAllEnvironments(null);
 //		ResponseInfo<List<Environment>> entity = (ResponseInfo<List<Environment>>)responseFail.getEntity();
 //		Assert.assertEquals(400, responseFail.getStatus());
 	}
 	
 	@Test
 	public void getAllEnvironmentsTestError() {
 		Response response = configurationService.getAllEnvironments("dfgdfg", "", "false","","");
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void connectionAliveForEnv() throws PhrescoException {
 		String connectionUrl = "";
 		Response response = configurationService.connectionAliveCheck(connectionUrl);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void addEnvironmentTest() throws PhrescoException {
 		List<Environment> environments = new ArrayList<Environment>();
 		List<Configuration> configList = new ArrayList<Configuration>();
 		List<Configuration> configList1 = new ArrayList<Configuration>();
 		Environment env = new Environment();
 		env.setName("Production");
 		env.setDefaultEnv(true);
 		
 		Configuration prodConfigServer = new Configuration();
 		Configuration prodConfigDb = new Configuration();
 		Configuration prodWebService = new Configuration();
 		prodConfigServer.setName("serverconfig");
 		prodConfigServer.setType("Server");
 		Properties propProd = new Properties();
 		propProd.setProperty("protocol", "http");
 		propProd.setProperty("host", "localhost");
 		propProd.setProperty("port", "80");
 		propProd.setProperty("admin_username", "");
 		propProd.setProperty("admin_password", "");
 		propProd.setProperty("remoteDeployment", "false");
 		propProd.setProperty("certificate", "");
 		propProd.setProperty("type", "Apache Tomcat");
 		propProd.setProperty("version", "6.0.x");
 		propProd.setProperty("deploy_dir", "C:\\wamp");
 		propProd.setProperty("context", "serverprtod");
 		prodConfigServer.setProperties(propProd);
 		
 		prodConfigDb.setName("db");
 		prodConfigDb.setType("Database");
 		Properties propProddb = new Properties();
 		propProddb.setProperty("host", "localhost");
 		propProddb.setProperty("port", "3306");
 		propProddb.setProperty("username", "root");
 		propProddb.setProperty("password", "");
 		propProddb.setProperty("dbname", "testjava");
 		propProddb.setProperty("type", "MySQL");
 		propProddb.setProperty("version", "5.5.1");
 		prodConfigDb.setProperties(propProddb);
 		
 		prodWebService.setName("ws");
 		prodWebService.setType("WebService");
 		Properties propProdWs= new Properties();
 		propProdWs.setProperty("host", "localhost");
 		propProdWs.setProperty("port", "5674");
 		propProdWs.setProperty("username", "root");
 		propProdWs.setProperty("password", "");
 		propProdWs.setProperty("context", "webservice");
 		propProdWs.setProperty("protocol", "http");
 		prodWebService.setProperties(propProdWs);
 		
 		configList.add(prodConfigServer);
 		configList.add(prodConfigDb);
 		configList.add(prodWebService);
 		
 		env.setConfigurations(configList);
 		Environment environment = new Environment();
 		environment.setName("Testing");
 		environment.setDefaultEnv(false);
 		
 		Configuration testConfigServer = new Configuration();
 		Configuration testConfigdb = new Configuration();
 		Configuration testWebService = new Configuration();
 		testConfigServer.setName("testconfig");
 		testConfigServer.setType("Server");
 		Properties testServer = new Properties();
 		testServer.setProperty("protocol", "http");
 		testServer.setProperty("host", "localhost");
 		testServer.setProperty("port", "6589");
 		testServer.setProperty("admin_username", "");
 		testServer.setProperty("admin_password", "");
 		testServer.setProperty("remoteDeployment", "");
 		testServer.setProperty("certificate", "");
 		testServer.setProperty("type", "Apache Tomcat");
 		testServer.setProperty("version", "6.0.x");
 		testServer.setProperty("deploy_dir", "c:\\wamp");
 		testServer.setProperty("context", "serverprtod");
 		testConfigServer.setProperties(testServer);
 		
 		testConfigdb.setName("testdb");
 		testConfigdb.setType("Database");
 		Properties propTestdb = new Properties();
 		propTestdb.setProperty("host", "localhost");
 		propTestdb.setProperty("port", "3306");
 		propTestdb.setProperty("username", "root");
 		propTestdb.setProperty("password", "");
 		propTestdb.setProperty("dbname", "testjava1");
 		propTestdb.setProperty("type", "MySQL");
 		propTestdb.setProperty("version", "5.5.1");
 		testConfigdb.setProperties(propTestdb);
 		
 		
 		testWebService.setName("ws");
 		testWebService.setType("WebService");
 		Properties testProdWs= new Properties();
 		testProdWs.setProperty("host", "localhost");
 		testProdWs.setProperty("port", "3306");
 		testProdWs.setProperty("username", "root");
 		testProdWs.setProperty("password", "");
 		testProdWs.setProperty("context", "webservice");
 		testProdWs.setProperty("protocol", "http");
 		testWebService.setProperties(testProdWs);
 		configList1.add(testConfigServer);
 		configList1.add(testConfigdb);
 		configList1.add(testWebService);
 		environment.setConfigurations(configList1);
 		
 		Environment devEnv = new Environment();
 		devEnv.setName("dev");
 		devEnv.setDefaultEnv(false);
 		devEnv.setDesc("dev for validation");
 		environments.add(env);
 		environments.add(devEnv);
 		environments.add(environment);
		Response response = configurationService.addEnvironment(appDirName, "", environments,"","","");
 		ResponseInfo<List<Environment>> environmentList = (ResponseInfo<List<Environment>>) configurationService.getAllEnvironments(appDirName, "", "true", "","").getEntity();
 		List<Environment> data = environmentList.getData();
 		Assert.assertEquals(200, response.getStatus());
 		Assert.assertEquals(3, data.size());
 		
		Response responseSame = configurationService.addEnvironment(appDirName, "", environments,"","","");
 		Assert.assertEquals(200, responseSame.getStatus());
 		
		Response responseFail = configurationService.addEnvironment("", "", environments,"","","");
 		Assert.assertEquals(200, responseFail.getStatus());
 	}
 	
 	@Test
 	public void addNonEnvConfigTest() throws ConfigurationException, PhrescoException {
 		List<Configuration> configList = new ArrayList<Configuration>();
 		Configuration prodConfigServer = new Configuration();
 		prodConfigServer.setName("serverconfig");
 		prodConfigServer.setType("Server");
 		Properties propProd = new Properties();
 		propProd.setProperty("protocol", "http");
 		propProd.setProperty("host", "localhost");
 		propProd.setProperty("port", "8654");
 		propProd.setProperty("admin_username", "");
 		propProd.setProperty("admin_password", "");
 		propProd.setProperty("remoteDeployment", "false");
 		propProd.setProperty("certificate", "");
 		propProd.setProperty("type", "Apache Tomcat");
 		propProd.setProperty("version", "6.0.x");
 		propProd.setProperty("deploy_dir", "C:\\wamp");
 		propProd.setProperty("context", "serverprtod");
 		prodConfigServer.setProperties(propProd);
 		configList.add(prodConfigServer);
 		Response response = configurationService.updateConfiguration(userId, customerId, appDirName, "", "", "", "", configList, "false", "", "", "false", "", "", "");
 		Assert.assertEquals(200, response.getStatus());
 		
 		
 		List<Configuration> configList1 = new ArrayList<Configuration>();
 		Configuration prodConfigServer1 = new Configuration();
 		prodConfigServer1.setName("serverconfig");
 		prodConfigServer1.setType("Server");
 		Properties propPro1 = new Properties();
 		propPro1.setProperty("protocol", "http");
 		propPro1.setProperty("host", "localhost");
 		propPro1.setProperty("port", "8654");
 		propPro1.setProperty("admin_username", "");
 		propPro1.setProperty("admin_password", "");
 		propPro1.setProperty("remoteDeployment", "false");
 		propPro1.setProperty("certificate", "");
 		propPro1.setProperty("type", "Apache Tomcat");
 		propPro1.setProperty("version", "6.0.x");
 		propPro1.setProperty("deploy_dir", "C:\\wamp");
 		propPro1.setProperty("context", "serverprtod");
 		prodConfigServer1.setProperties(propPro1);
 		configList1.add(prodConfigServer1);
 		
 		Response responseUp = configurationService.updateConfiguration(userId, customerId, appDirName, "","", "", "", configList1, "false", "serverconfig", "", "false", "","","");
 		Assert.assertEquals(200, responseUp.getStatus());
 		
 		List<Configuration> configList2 = new ArrayList<Configuration>();
 		Configuration prodConfigServer2 = new Configuration();
 		prodConfigServer2.setName("Configs");
 		prodConfigServer2.setType("Server");
 		Properties propProd2 = new Properties();
 		propProd2.setProperty("protocol", "http");
 		propProd2.setProperty("host", "localhost");
 		propProd2.setProperty("port", "5555");
 		propProd2.setProperty("admin_username", "rrr");
 		propProd2.setProperty("admin_password", "sss");
 		propProd2.setProperty("remoteDeployment", "false");
 		propProd2.setProperty("certificate", "");
 		propProd2.setProperty("type", "Apache Tomcat");
 		propProd2.setProperty("version", "7.0.x");
 		propProd2.setProperty("deploy_dir", "C:\\wamp");
 		propProd2.setProperty("context", "serverprtod");
 		prodConfigServer2.setProperties(propProd2);
 		configList2.add(prodConfigServer2);
 		Response responseNonEnv = configurationService.updateConfiguration(userId, customerId, appDirName, "","", "", "", configList2, "false", "","", "false", "","","");
 		Assert.assertEquals(200, responseNonEnv.getStatus());
 		
 		
 	}
 	
 	@Test
 	public void listEnvironmentTest() {
 		Response response = configurationService.listEnvironments(appDirName, "Production", "", "", "","");
 		ResponseInfo<Environment> responseInfo = (ResponseInfo<Environment>) response.getEntity();
 		Environment environment = responseInfo.getData();
 		Assert.assertEquals("Production", environment.getName());
 		
 		Response responseNon = configurationService.listEnvironments(appDirName, "", "false", "", "serverconfig","");
 		Assert.assertEquals(200, responseNon.getStatus());
 		
 		Response responseNonEnvList = configurationService.getAllEnvironments(appDirName, "", "false", "Server","");
 		Assert.assertEquals(200, responseNonEnvList.getStatus());
 		
 		Response environmentList = configurationService.getEnvironmentList(customerId, appDirName, "","");
 		Assert.assertEquals(200, environmentList.getStatus());
 		
 	}
 	
 	@Test
 	public void listEnvironmentTestFail() {
 		Response response = configurationService.listEnvironments(appDirName, "","","", "","");
 		Assert.assertEquals(200, response.getStatus());
 		Response responseFail = configurationService.listEnvironments("", "Production","","", "","");
 		Assert.assertEquals(200, responseFail.getStatus());
 		
 	}
 	
 	@Test
 	public void listEnvironmentError() {
 		Response response = configurationService.listEnvironments("dnf", "Production", "false","", "","");
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void deleteEnvTestFail() throws ConfigurationException, PhrescoException {
 		String envName = "Production";
 		Response response = configurationService.deleteEnv(appDirName, envName, "", "");
 		ResponseInfo<Environment> responseInfo = (ResponseInfo<Environment>) response.getEntity();
 		Assert.assertEquals("failure", responseInfo.getStatus());
 //		Response responseFail = configurationService.deleteEnv(appDirName, "");
 //		Assert.assertEquals(417, responseFail.getStatus());
 	}
 	
 	@Test
 	public void deleteEnvTest() throws ConfigurationException, PhrescoException {
 		String envName = "Production";
 		Response response = configurationService.deleteEnv(appDirName, envName, "","");
 		Assert.assertEquals(200, response.getStatus());
 		
 //		Response responseFail = configurationService.deleteEnv(appDirName, envName);
 //		Assert.assertEquals(200, responseFail.getStatus());
 		
 		Response responsedel = configurationService.deleteConfiguraion(appDirName, "", "serverconfig");
 		Assert.assertEquals(200, responsedel.getStatus());
 		
 		
 	}
 	
 	@Test
 	public void  getSettingsTemplateTest() {
 		Response templateLoginFail = configurationService.getSettingsTemplate(appDirName, "", techId, "sample", "Server","","");
 		Assert.assertEquals(200, templateLoginFail.getStatus());
 		Response templateServer = configurationService.getSettingsTemplate(appDirName, "", techId, userId, "Server","","");
 		Assert.assertEquals(200, templateServer.getStatus());
 		Response responseDb = configurationService.getSettingsTemplate(appDirName, "", techId, userId, "Database","","");
 		Assert.assertEquals(200, responseDb.getStatus());
 		Response responseServer = configurationService.getSettingsTemplate(appDirName, "", techId, userId, "Email","","");
 		Assert.assertEquals(200, responseServer.getStatus());
 //		Response settingsTempFail = configurationService.getSettingsTemplate(appDirName, "", userId, "");
 //		Assert.assertEquals(400, settingsTempFail.getStatus());
 	}
 	
 	@Test
 	public void testCronExpressionSuccess() throws PhrescoException {
 		CronExpressionInfo cron = new CronExpressionInfo();
 		cron.setCronBy("Weekly");
 		cron.setEvery("false");
 		cron.setHours("5");
 		cron.setMinutes("23");
 		cron.setWeek(Arrays.asList("3"));
 		cron.setMonth(Arrays.asList("9"));
 		cron.setDay("8");
 		
 		Response cronValidation = configurationService.cronValidation(cron);
 		ResponseInfo<CronExpressionInfo> entity = (ResponseInfo<CronExpressionInfo>) cronValidation.getEntity();
 		assertEquals("23 5 * * 3" , entity.getData().getCronExpression());
 	}
 	
 	@Test
 	public void testCronExpressionSuccessOnDaily() throws PhrescoException {
 		CronExpressionInfo cron = new CronExpressionInfo();
 		cron.setCronBy("Daily");
 		cron.setEvery("false");
 		cron.setHours("5");
 		cron.setMinutes("23");
 		cron.setDay("8");
 		Response cronValidation = configurationService.cronValidation(cron);
 		assertEquals(200 , cronValidation.getStatus());
 		
 		CronExpressionInfo cron2 = new CronExpressionInfo();
 		cron2.setCronBy("Daily");
 		cron2.setEvery("false");
 		cron2.setHours("*");
 		cron2.setMinutes("*");
 		cron2.setDay("8");
 		Response cronValidation2 = configurationService.cronValidation(cron2);
 		assertEquals(200 , cronValidation2.getStatus());
 		
 		CronExpressionInfo cron3 = new CronExpressionInfo();
 		cron3.setCronBy("Daily");
 		cron3.setEvery("false");
 		cron3.setHours("*");
 		cron3.setMinutes("23");
 		cron3.setDay("8");
 		Response cronValidation3 = configurationService.cronValidation(cron3);
 		assertEquals(200 , cronValidation3.getStatus());
 		
 		CronExpressionInfo cron4 = new CronExpressionInfo();
 		cron4.setCronBy("Daily");
 		cron4.setEvery("false");
 		cron4.setHours("4");
 		cron4.setMinutes("*");
 		cron4.setDay("8");
 		Response cronValidation4 = configurationService.cronValidation(cron4);
 		assertEquals(200 , cronValidation4.getStatus());
 		
 		
 		CronExpressionInfo cronExp = new CronExpressionInfo();
 		cronExp.setCronBy("Daily");
 		cronExp.setEvery("true");
 		cronExp.setHours("5");
 		cronExp.setMinutes("23");
 		cronExp.setDay("8");
 		Response cronValdaily = configurationService.cronValidation(cronExp);
 		assertEquals(200 , cronValdaily.getStatus());
 		
 		CronExpressionInfo cronExp1 = new CronExpressionInfo();
 		cronExp1.setCronBy("Daily");
 		cronExp1.setEvery("true");
 		cronExp1.setHours("*");
 		cronExp1.setMinutes("*");
 		cronExp1.setDay("8");
 		Response cronValdaily1 = configurationService.cronValidation(cronExp1);
 		assertEquals(200 , cronValdaily1.getStatus());
 		
 		CronExpressionInfo cronExp2 = new CronExpressionInfo();
 		cronExp2.setCronBy("Daily");
 		cronExp2.setEvery("true");
 		cronExp2.setHours("*");
 		cronExp2.setMinutes("23");
 		cronExp2.setDay("8");
 		Response cronValdaily2 = configurationService.cronValidation(cronExp2);
 		assertEquals(200 , cronValdaily2.getStatus());
 		
 		
 		CronExpressionInfo cronExp3 = new CronExpressionInfo();
 		cronExp3.setCronBy("Daily");
 		cronExp3.setEvery("true");
 		cronExp3.setHours("6");
 		cronExp3.setMinutes("*");
 		cronExp3.setDay("8");
 		Response cronValdaily3 = configurationService.cronValidation(cronExp3);
 		assertEquals(200 , cronValdaily3.getStatus());
 		
 	}
 	
 	@Test
 	public void testCronExpressionFailure() throws PhrescoException {
 		CronExpressionInfo cron = new CronExpressionInfo();
 		cron.setCronBy("Weekly");
 		cron.setEvery("false");
 		cron.setHours("5");
 		cron.setMinutes("24");
 		cron.setWeek(Arrays.asList("3"));
 		cron.setMonth(Arrays.asList("9"));
 		cron.setDay("8");
 		
 		Response cronValidation = configurationService.cronValidation(cron);
 		ResponseInfo<CronExpressionInfo> entity = (ResponseInfo<CronExpressionInfo>) cronValidation.getEntity();
 		Assert.assertNotSame("CronExpression  not Matching", "23 5 * * 3" , entity.getData().getCronExpression());
 	}
 	
 	@Test
 	public void testDate() throws PhrescoException {
 		CronExpressionInfo cron = new CronExpressionInfo();
 		cron.setCronBy("Monthly");
 		cron.setEvery("false");
 		cron.setHours("2");
 		cron.setMinutes("12");
 		cron.setDay("5");
 		cron.setMonth(Arrays.asList("2,3"));
 		cron.setDay("8");
 		
 		Response cronValidation = configurationService.cronValidation(cron);
 		ResponseInfo<CronExpressionInfo> entity = (ResponseInfo<CronExpressionInfo>) cronValidation.getEntity();
 		Assert.assertEquals(4, entity.getData().getDates().size());
 	}
 	
 	@Test
 	public void listEnvironmentsByProjectId() throws PhrescoException, PhrescoPomException {
 		Response response = configurationService.listEnvironmentsByProjectId(customerId, projectId);
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	@Test
 	public void getConfigTypeTest() {
 		Response responseLoginFail = configurationService.getConfigTypes(customerId, "sample", techId, "");
 		Assert.assertEquals(200, responseLoginFail.getStatus());
 		Response response = configurationService.getConfigTypes(customerId, userId, techId, "");
 		Assert.assertEquals(200, response.getStatus());
 //		Response responseConfigType = configurationService.getConfigTypes("", userId, "");
 //		Assert.assertEquals(400, responseConfigType.getStatus());
 	}
 	
 	@Test
 	public void configurationValidationTest() throws ConfigurationException, PhrescoException {
 		Properties propertiesServer = new Properties();
 		Properties propertiesSerDupl = new Properties();
 		Properties propertiesEmail = new Properties();
 		Properties propertiesDupl = new Properties();
 		Properties propertiesSiteCore = new Properties();
 		
 		propertiesServer.setProperty("context", "CometDTesting");
 		propertiesServer.setProperty("additional_context", "");
 		propertiesServer.setProperty("port", "8888");
 		propertiesServer.setProperty("remoteDeployment", "true");
 		propertiesServer.setProperty("admin_password", "sade");
 		propertiesServer.setProperty("admin_username", "");
 		propertiesServer.setProperty("version", "7.0.x");
 		propertiesServer.setProperty("type", "NodeJs");
 		propertiesServer.setProperty("host", "localhost");
 		propertiesServer.setProperty("protocol", "http");
 		
 		
 		propertiesSerDupl.setProperty("context", "CometDTesting");
 		propertiesSerDupl.setProperty("admin_username", "");
 		propertiesSerDupl.setProperty("deploy_dir", "C:\\wamp");
 		propertiesSerDupl.setProperty("additional_context", "");
 		propertiesSerDupl.setProperty("port", "6525");
 		propertiesSerDupl.setProperty("admin_password", "");
 		propertiesSerDupl.setProperty("version", "6.0.x");
 		propertiesSerDupl.setProperty("type", "Apache Tomcat");
 		propertiesSerDupl.setProperty("remoteDeployment", "false");
 		propertiesSerDupl.setProperty("admin_username", "vvvv");
 		propertiesSerDupl.setProperty("host", "localhost");
 		propertiesSerDupl.setProperty("protocol", "http");
 		
 		propertiesEmail.setProperty("port", "8596");
 		propertiesEmail.setProperty("emailid", "");
 		propertiesEmail.setProperty("password", "test");
 		propertiesEmail.setProperty("incoming_mail_server", "test");
 		propertiesEmail.setProperty("incoming_mail_port", "test");
 		propertiesEmail.setProperty("host", "test");
 		propertiesEmail.setProperty("username", "test");
 		
 		
 		propertiesDupl.setProperty("port", "8596");
 		propertiesDupl.setProperty("emailid", "sam.as@yahoo.com");
 		propertiesDupl.setProperty("password", "test");
 		propertiesDupl.setProperty("incoming_mail_server", "test");
 		propertiesDupl.setProperty("incoming_mail_port", "test");
 		propertiesDupl.setProperty("host", "test");
 		propertiesDupl.setProperty("username", "test");
 		
 		propertiesSiteCore.setProperty("siteCoreInstPath", "");
 		propertiesSiteCore.setProperty("protocol", "http");
 		propertiesSiteCore.setProperty("host", "localhost");
 		propertiesSiteCore.setProperty("port", "2468");
 		propertiesSiteCore.setProperty("admin_username", "");
 		propertiesSiteCore.setProperty("admin_password", "");
 		propertiesSiteCore.setProperty("username", "");
 		propertiesSiteCore.setProperty("type", "IIS");
 		propertiesSiteCore.setProperty("version", "7.5");
 		propertiesSiteCore.setProperty("context", "siteCoreServer");
 		
 		
 		
 		Configuration configurationServer = new Configuration("", "serverconfig", "dev", "Server", propertiesServer);
 		Configuration configuration = new Configuration("ServerConfig", "serverconfig", "dev", "Server", propertiesSerDupl);
 		Configuration configurationEmail = new Configuration("Email", "Emailconfig", "dev", "Email", propertiesEmail);
 		Configuration configurationEmailDup = new Configuration("Email", "Emailconfig", "dev", "Email", propertiesDupl);
 		Configuration configurationSiteCorePath = new Configuration("Server", "Testconfig", "dev", "Server", propertiesSiteCore);
 		Configuration configurationServerDuplicate = new Configuration("Server2", "Serverconfig", "dev", "Server", propertiesSerDupl);
 		Configuration configurationEmailDuplicate = new Configuration("Email2", "Emailconfig", "dev", "Email", propertiesDupl);
 		Configuration configurationServerName = new Configuration("Server", "Serverconfig", "dev", "Server", propertiesSerDupl);
 		List<Configuration> configListServer = new ArrayList<Configuration>();
 		List<Configuration> configListEmail = new ArrayList<Configuration>();
 		List<Configuration> configListSiteCore = new ArrayList<Configuration>();
 		List<Configuration> configListPass = new ArrayList<Configuration>();
 		List<Configuration> configListName = new ArrayList<Configuration>();
 		
 		configListServer.add(configurationServer);
 		configListServer.add(configurationServerDuplicate);
 		
 		configListEmail.add(configurationEmail);
 		configListEmail.add(configurationEmailDuplicate);
 		
 			
 		configListSiteCore.add(configurationSiteCorePath);
 		configListSiteCore.add(configuration);
 		
 		Configuration prodConfigDb = new Configuration();
 		Configuration prodWebService = new Configuration();
 		prodConfigDb.setName("db");
 		prodConfigDb.setType("Database");
 		Properties propProddb = new Properties();
 		propProddb.setProperty("host", "localhost");
 		propProddb.setProperty("port", "3306");
 		propProddb.setProperty("username", "root");
 		propProddb.setProperty("password", "");
 		propProddb.setProperty("dbname", "testjava");
 		propProddb.setProperty("type", "MySQL");
 		propProddb.setProperty("version", "5.5.1");
 		prodConfigDb.setProperties(propProddb);
 		
 		prodWebService.setName("ws");
 		prodWebService.setType("WebService");
 		Properties propProdWs= new Properties();
 		propProdWs.setProperty("host", "localhost");
 		propProdWs.setProperty("port", "5674");
 		propProdWs.setProperty("username", "root");
 		propProdWs.setProperty("password", "");
 		propProdWs.setProperty("context", "webservice");
 		propProdWs.setProperty("protocol", "http");
 		prodWebService.setProperties(propProdWs);
 		
 		configListPass.add(configuration);
 		configListPass.add(prodConfigDb);
 //		configListPass.add(prodWebService);
 		configListPass.add(configurationEmailDup);
 		
 		configListName.add(configuration);
 		configListName.add(configurationEmailDup);
 		
 		
 		Response failName = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListServer,"","" , "dev env", "false", "","","");
 		Assert.assertEquals(200, failName.getStatus());
 		
 		Configuration configurationServer1 = new Configuration("Server2", "serverconfig", "dev", "rett", propertiesServer);
 		List<Configuration> configListServer1 = new ArrayList<Configuration>();
 		configListServer1.add(configurationServerDuplicate);
 		configListServer1.add(configurationServer1);
 		Response failType = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListServer1,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, failType.getStatus());
 		Properties propertiesServer1 = new Properties();
 		propertiesServer1.setProperty("context", "CometDTesting");
 		propertiesServer1.setProperty("admin_username", "");
 		propertiesServer1.setProperty("deploy_dir", "C:\\wamp");
 		propertiesServer1.setProperty("additional_context", "");
 		propertiesServer1.setProperty("port", "8888");
 		propertiesServer1.setProperty("admin_password", "pwd");
 		propertiesServer1.setProperty("version", "7.0.x");
 		propertiesServer1.setProperty("type", "Apache Tomcat");
 		propertiesServer1.setProperty("remoteDeployment", "false");
 		propertiesServer1.setProperty("admin_username", "vvvv");
 		propertiesServer1.setProperty("host", "localhost");
 		propertiesServer1.setProperty("protocol", "http");
 		
 		Configuration configurationServer2 = new Configuration("Serverconfig", "serverconfig", "dev", "", propertiesServer1);
 		List<Configuration> configListServer2 = new ArrayList<Configuration>();
 		configListServer2.add(configurationServer2);
 		configListServer2.add(configurationServerDuplicate);
 		Response failmailEmp = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListServer2,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, failmailEmp.getStatus());
 		
 		Configuration configurationServer3 = new Configuration("Serverconfig", "serverconfig", "dev", "Server", propertiesServer);
 		List<Configuration> configListServer3 = new ArrayList<Configuration>();
 		configListServer3.add(configurationServer3);
 		configListServer3.add(configurationEmailDup);
 		Response failmailEmp3 = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListServer3,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, failmailEmp3.getStatus());
 		
 		
 		Configuration configurationServer6 = new Configuration("Serverconfig", "serverconfig", "dev", "Server", propertiesServer1);
 		List<Configuration> configListServer6 = new ArrayList<Configuration>();
 		configListServer6.add(configurationServer6);
 		configListServer6.add(configurationServerDuplicate);
 		Response failmailEmp6 = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListServer6,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, failmailEmp6.getStatus());
 		
 		
 		Response failmailEmp7 = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListEmail,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, failmailEmp7.getStatus());
 		
 		
 		List<Configuration> configListEmail3 = new ArrayList<Configuration>();
 		Properties propertiesEmail1 = new Properties();
 		
 		propertiesEmail1.setProperty("port", "8596");
 		propertiesEmail1.setProperty("emailid", "test@!#gmail.com");
 		propertiesEmail1.setProperty("password", "test");
 		propertiesEmail1.setProperty("incoming_mail_server", "test");
 		propertiesEmail1.setProperty("incoming_mail_port", "test");
 		propertiesEmail1.setProperty("host", "test");
 		propertiesEmail1.setProperty("username", "test");
 		Configuration configurationEmail1 = new Configuration("Email", "Emailconfig", "dev", "Email", propertiesEmail1);
 		configListEmail3.add(configurationEmail1);
 		configListEmail3.add(configurationEmailDuplicate);
 		Response failmailVal = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListEmail3,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, failmailVal.getStatus());
 		
 		List<Configuration> configListEmail4 = new ArrayList<Configuration>();
 		Properties propertiesEmail2 = new Properties();
 		
 		propertiesEmail2.setProperty("port", "8596");
 		propertiesEmail2.setProperty("emailid", "test@gmail.com");
 		propertiesEmail2.setProperty("password", "test");
 		propertiesEmail2.setProperty("incoming_mail_server", "test");
 		propertiesEmail2.setProperty("incoming_mail_port", "test");
 		propertiesEmail2.setProperty("host", "test");
 		propertiesEmail2.setProperty("username", "test");
 		
 		Configuration configurationEmail2 = new Configuration("Email", "Emailconfig", "dev", "Email", propertiesEmail2);
 		configListEmail4.add(configurationEmailDuplicate);
 		configListEmail4.add(configurationEmail2);
 		Response responseServer = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListEmail4,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, responseServer.getStatus());
 		Configuration configurationEmail3 = new Configuration("Email", "Emailconfig", "dev", "Email", propertiesEmail2);
 		List<Configuration> configListEmail5 = new ArrayList<Configuration>();
 		configListEmail5.add(configurationEmail3);
 		configListEmail5.add(configurationEmailDuplicate);
 		
 		Response responseEmail = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListEmail5,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, responseEmail.getStatus());
 		
 		
 		Response responseSiteCore = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListSiteCore,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, responseSiteCore.getStatus());
 		
 		
 		Response responseName = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListName,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, responseName.getStatus());
 		
 		
 		Response responsePass = configurationService.updateConfiguration(userId, customerId, appDirName, "dev", "", "dev", "false", configListPass,"","", "dev env", "false", "","","");
 		Assert.assertEquals(200, responsePass.getStatus());
 		
 	}
 	
 	
 	@Test
 	public void addProdEnvironmentTest() throws PhrescoException {
 		List<Environment> environments = new ArrayList<Environment>();
 		List<Configuration> configList = new ArrayList<Configuration>();
 		List<Configuration> finalList = new ArrayList<Configuration>();
 		Environment env = new Environment();
 		env.setName("Production");
 		env.setDefaultEnv(true);
 		Configuration prodConfigServer = new Configuration();
 		Configuration prodConfigDb = new Configuration();
 		Configuration prodWebService = new Configuration();
 		prodConfigServer.setName("serverconfig");
 		prodConfigServer.setType("Server");
 		Properties propProd = new Properties();
 		propProd.setProperty("protocol", "http");
 		propProd.setProperty("host", "localhost");
 		propProd.setProperty("port", "80");
 		propProd.setProperty("admin_username", "");
 		propProd.setProperty("admin_password", "");
 		propProd.setProperty("remoteDeployment", "false");
 		propProd.setProperty("certificate", "");
 		propProd.setProperty("type", "Apache Tomcat");
 		propProd.setProperty("version", "6.0.x");
 		propProd.setProperty("deploy_dir", "C:\\wamp");
 		propProd.setProperty("context", "serverprtod");
 		prodConfigServer.setProperties(propProd);
 		
 		prodConfigDb.setName("db");
 		prodConfigDb.setType("Database");
 		Properties propProddb = new Properties();
 		propProddb.setProperty("host", "localhost");
 		propProddb.setProperty("port", "3306");
 		propProddb.setProperty("username", "root");
 		propProddb.setProperty("password", "");
 		propProddb.setProperty("dbname", "testjava");
 		propProddb.setProperty("type", "MySQL");
 		propProddb.setProperty("version", "5.5.1");
 		prodConfigDb.setProperties(propProddb);
 		
 		prodWebService.setName("ws");
 		prodWebService.setType("WebService");
 		Properties propProdWs= new Properties();
 		propProdWs.setProperty("host", "localhost");
 		propProdWs.setProperty("port", "5674");
 		propProdWs.setProperty("username", "root");
 		propProdWs.setProperty("password", "");
 		propProdWs.setProperty("context", "webservice");
 		propProdWs.setProperty("protocol", "http");
 		prodWebService.setProperties(propProdWs);
 		
 		configList.add(prodConfigServer);
 		configList.add(prodConfigDb);
 		configList.add(prodWebService);
 		
 		env.setConfigurations(configList);
 		
 		
 		Environment devEnv = new Environment();
 		devEnv.setName("dev");
 		devEnv.setDefaultEnv(false);
 		devEnv.setDesc("dev for validation");
 		Properties propertiesSerDupl = new Properties();
 		propertiesSerDupl.setProperty("context", "CometDTesting");
 		propertiesSerDupl.setProperty("admin_username", "");
 		propertiesSerDupl.setProperty("deploy_dir", "C:\\wamp");
 		propertiesSerDupl.setProperty("additional_context", "");
 		propertiesSerDupl.setProperty("port", "6525");
 		propertiesSerDupl.setProperty("admin_password", "");
 		propertiesSerDupl.setProperty("version", "6.0.x");
 		propertiesSerDupl.setProperty("type", "Apache Tomcat");
 		propertiesSerDupl.setProperty("remoteDeployment", "false");
 		propertiesSerDupl.setProperty("admin_username", "vvvv");
 		propertiesSerDupl.setProperty("host", "localhost");
 		propertiesSerDupl.setProperty("protocol", "http");
 		Configuration configuration = new Configuration("ServerConfig", "serverconfig", "dev", "Server", propertiesSerDupl);
 		finalList.add(configuration);
 		finalList.add(prodConfigDb);
 		devEnv.setConfigurations(finalList);
 		environments.add(env);
 		environments.add(devEnv);
		Response response = configurationService.addEnvironment(appDirName, "", environments, "", "", "");
 		ResponseInfo<List<Environment>> environmentList = (ResponseInfo<List<Environment>>) configurationService.getAllEnvironments(appDirName, "", "true", "", "").getEntity();
 		Assert.assertEquals(200, response.getStatus());
 	}
 
 	@Test
 	public void connectionAliveTest() throws PhrescoException {
 		String connectionUrl = getConnectionUrl("Production", "Server", "serverconfig");
 		Response response = configurationService.connectionAliveCheck(connectionUrl);
 		Assert.assertEquals(200, response.getStatus());
 		String connection = getConnectionUrl("Production", "WebService", "ws");
 		Response urlFail = configurationService.connectionAliveCheck(connection);
 		Assert.assertEquals(200, urlFail.getStatus());
 		String connectionUrlFail = "htp" + "," + "localhost" + "," + "7o9o";
 		Response responseFail = configurationService.connectionAliveCheck(connectionUrlFail);
 		Assert.assertEquals(200, responseFail.getStatus());
 	}
 	
 	@Test
 	public void cloneEnvironmentTest() throws ConfigurationException {
 		Environment cloneEnvironment = new Environment();
 		cloneEnvironment.setName("clone");
 		cloneEnvironment.setDefaultEnv(false);
 		Response response = configurationService.cloneEnvironment(appDirName, "Production", "", cloneEnvironment, "");
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 //	@Test
 	public void returnCertificate() throws PhrescoException {
 		Response authenticateServer = configurationService.authenticateServer("kumar_s", "8443", "TestProject", "");
 		Assert.assertEquals(200, authenticateServer.getStatus());
 	}
 	
 	@Test
 	public void addCertificate() throws PhrescoException {
 		AddCertificateInfo add = new AddCertificateInfo();
 		add.setAppDirName("TestProject");
 		add.setHost("kumar_s");
 		add.setPort("8443");
 		add.setPropValue("CN=kumar_s");
 		add.setFromPage("configuration");
 		add.setCertificateName("CN=kumar_s");
 		add.setEnvironmentName("Production");
 		add.setConfigName("Server");
 		add.setCustomerId("photon");
 //		Response authenticateServer = configurationService.addCertificate(add);
 //		Assert.assertEquals(200, authenticateServer.getStatus());
 		
 		AddCertificateInfo add1 = new AddCertificateInfo();
 		add1.setAppDirName("TestProject");
 		add1.setHost("kumar_s");
 		add1.setPort("8443");
 		add1.setPropValue("CN=kumar_s");
 		add1.setFromPage("settings");
 		add1.setCertificateName("CN=kumar_s");
 		add1.setEnvironmentName("Production");
 		add1.setConfigName("Server");
 		add1.setCustomerId("photon");
 //		Response authenticate = configurationService.addCertificate(add1);
 //		Assert.assertEquals(200, authenticate.getStatus());
 		
 		AddCertificateInfo add4 = new AddCertificateInfo();
 		add4.setHost("kumar_s");
 		add4.setPort("6356");
 		add4.setFromPage("configuraiton");
 		add4.setPropValue("");
 //		Response addFailsonValue = configurationService.addCertificate(add4);
 //		Assert.assertEquals(200, addFailsonValue.getStatus());
 		 
 		File certFile = FileUtils.toFile(this.getClass().getResource("/Production-Server.crt"));
 		AddCertificateInfo add5 = new AddCertificateInfo();
 		add5.setAppDirName("TestProject");
 		add5.setHost("localhost");
 		add5.setPort("8080");
 		add5.setPropValue(certFile.getPath());
 		add5.setFromPage("configuration");
 		add5.setCertificateName(certFile.getPath());
 		add5.setEnvironmentName("Production");
 		add5.setConfigName("Server");
 		add5.setCustomerId("photon");
 //		Response authenticateServerPath = configurationService.addCertificate(add5);
 //		Assert.assertEquals(200, authenticateServerPath.getStatus());
 		
 		AddCertificateInfo add6 = new AddCertificateInfo();
 		add6.setAppDirName("TestProject");
 		add6.setHost("localhost");
 		add6.setPort("8080");
 		add6.setPropValue(certFile.getPath());
 		add6.setFromPage("settings");
 		add6.setCertificateName(certFile.getPath());
 		add6.setEnvironmentName("Production");
 		add6.setConfigName("Server");
 		add6.setCustomerId("photon");
 //		Response authenticateServersettings = configurationService.addCertificate(add6);
 //		Assert.assertEquals(200, authenticateServersettings.getStatus());
 		
 	}
 	
 		@Test
 		public void fileBrowseTest() {
 		Response fileStructure = configurationService.returnFileBorwseFolderStructure(Utility.getProjectHome()
 		+ appDirName);
 		Assert.assertEquals(200, fileStructure.getStatus());
 		
 		Response fileStructureFail = configurationService.returnFileBorwseFolderStructure("D:\\temp\\");
 		Assert.assertEquals(200, fileStructureFail.getStatus());
 		
 		Response fileEntireStructure = configurationService.returnFileBorwseEntireStructure(appDirName, "", null);
 		Assert.assertEquals(200, fileEntireStructure.getStatus());
 		
 		Response fileEntireStructureFail = configurationService.returnFileBorwseEntireStructure("test", "", null);
 		Assert.assertEquals(200, fileEntireStructureFail.getStatus());
 		
 		}
 		
 		@Test
 		public void fileBrowseErrorTest() {
 		Response fileStructure = configurationService.returnFileBorwseFolderStructure(Utility.getProjectHome()
 		+ "dfjh");
 		Assert.assertEquals(200, fileStructure.getStatus());
 		
 		Response fileEntireStructure = configurationService.returnFileBorwseEntireStructure("ffjgh", "", null);
 		Assert.assertEquals(200, fileEntireStructure.getStatus());
 		
 		}	
 	
 	@Test
 	public void uploadFileOnTarget() {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.addHeader("appDirName", appDirName);
 		request.addHeader("actionType", "editconfig");
 		request.addHeader("envName", "Production");
 		request.addHeader("configType", "Theme");
 		request.addHeader("configName", "serverconfig");
 		request.addHeader("oldName", "test");
 		request.addHeader("propName", "port");
 		request.addHeader("X-File-Name", "test.zip");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response uploadFile = configurationService.uploadFile(httpServletRequest);
 		Assert.assertEquals(200, uploadFile.getStatus());
 	}
 	
 	@Test
 	public void removeFileOnTarget() {
 		Response removeConfigFile = configurationService.removeConfigFile(appDirName, "Theme", "sample", "", "test.zip", "Production", "Server");
 		Assert.assertEquals(200, removeConfigFile.getStatus());
 	}
 	
 	@Test
 	public void removeThemeProperty() throws PhrescoException, PhrescoPomException {
 		PomProcessor pomProcessor = FrameworkServiceUtil.getPomProcessor("TestProject");
 		boolean removeProperty = pomProcessor.removeProperty("phresco.theme.target.dir");
     	pomProcessor.save();
     	Assert.assertEquals(true, removeProperty);
 	}
 	
 	@Test
 	public void uploadFile() {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.addHeader("appDirName", appDirName);
 		request.addHeader("actionType", "editconfig");
 		request.addHeader("envName", "Production");
 		request.addHeader("configType", "Server");
 		request.addHeader("configName", "serverconfig");
 		request.addHeader("oldName", "test");
 		request.addHeader("propName", "port");
 		request.addHeader("X-File-Name", "test.zip");
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response uploadFile = configurationService.uploadFile(httpServletRequest);
 		Assert.assertEquals(200, uploadFile.getStatus());
 		
 		MockHttpServletRequest request1 = new MockHttpServletRequest();
 		request1.addHeader("appDirName", appDirName);
 		request1.addHeader("actionType", "editconfig");
 		request1.addHeader("envName", "Production");
 		request1.addHeader("configType", "Server");
 		request1.addHeader("configName", "serverconfig");
 		request1.addHeader("oldName", "");
 		request1.addHeader("propName", "port");
 		request1.addHeader("X-File-Name", "test.zip");
 		HttpServletRequest httpServletRequest1 = (HttpServletRequest)request1;
 		Response uploadFile1 = configurationService.uploadFile(httpServletRequest1);
 		Assert.assertEquals(200, uploadFile1.getStatus());
 		
 		request1.addHeader("oldName", "serverconfig");
 		HttpServletRequest httpServletRequest2 = (HttpServletRequest)request1;
 		Response uploadFile2 = configurationService.uploadFile(httpServletRequest2);
 		Assert.assertEquals(200, uploadFile2.getStatus());
 		
 	}
 
 	@Test
 	public void listFiles() {
 		Response listUploadedFiles = configurationService.listUploadedFiles(appDirName, "", "Production", "Server", "server", "port", "true");
 		Assert.assertEquals(200, listUploadedFiles.getStatus());
 		Response listUploadedFile = configurationService.listUploadedFiles(appDirName, "", "Production", "Server", "serverconfig", "port", "false");
 		Assert.assertEquals(200, listUploadedFile.getStatus());
 		
 	}
 	
 	@Test
 	public void listFilesError() {
 		Response listUploadedFiles = configurationService.listUploadedFiles("dfsdf", "", "Production", "Server", "server", "port","true");
 		Assert.assertEquals(200, listUploadedFiles.getStatus());
 		Response listUploadedFile = configurationService.listUploadedFiles("dsfdsf", "", "Production", "Server", "serverconfig", "port", "false");
 		Assert.assertEquals(200, listUploadedFile.getStatus());
 		
 	}
 	
 	@Test
 	public void removeFile() {
 		Response removeConfigFile = configurationService.removeConfigFile(appDirName, "content", "sample", "", "test.zip", "Production", "Server");
 		Assert.assertEquals(200, removeConfigFile.getStatus());
 	}
 	
 	@Test
 	public void removeFileError() {
 		Response removeConfigFile = configurationService.removeConfigFile("TestProject", "content", "sample", "", "test.zip", "Production", "ServerConfig");
 		Assert.assertEquals(200, removeConfigFile.getStatus());
 	}
 	
 	@Test
 	public void showPropertiesFeaturesTest() {
 		Response names = configurationService.showProperties(userId, "Features", "TestProject", "", "photon");
 		Assert.assertEquals(200, names.getStatus());
 	}
 	
 	@Test
 	public void showPropertiesComponentsTest() throws PhrescoException, FileNotFoundException, IOException, ParseException {
 		Response names = configurationService.showProperties(userId, "Components", "TestProject", "", "photon");
 		Assert.assertEquals(200, names.getStatus());
 	}
 	
 	@Test
 	public void showPropertiesFeaturesErrorTest() {
 		Response names = configurationService.showProperties(userId, "Features", "TestProject", "", "photon");
 		Assert.assertEquals(200, names.getStatus());
 	}
 	
 	@Test
 	public void showFeatureConfigsNonEmptyTest() throws PhrescoException {
 		ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo("TestProject");
 		List<Configuration> configurations = new ArrayList<Configuration>();
 		Configuration config = new Configuration();
 		config.setEnvName("production");
 		config.setName("test");
 		config.setType("FEATURES");
 		Properties prop = new Properties();
 		prop.setProperty("featureName", "testFeature");
 		prop.setProperty("this works", "yes");
 		config.setProperties(prop);
 		configurations.add(config);
 		
 		List<Configuration> configurations2 = new ArrayList<Configuration>();
 		Configuration config2 = new Configuration();
 		config2.setEnvName("production");
 		config2.setName("test2");
 		config2.setType("COMPONENTS");
 		Properties prop2 = new Properties();
 		prop2.setProperty("featureName", "testComponent");
 		prop2.setProperty("this works", "yes");
 		config2.setProperties(prop2);
 		configurations2.add(config2);
 		
 		HtmlApplicationProcessor writeToJson = new HtmlApplicationProcessor();
 		File jsonDir = new File(Utility.getProjectHome() + 
 				appInfo.getAppDirName() + File.separator + "src/main/webapp/json");
 		jsonDir.mkdirs();
 		writeToJson.postConfiguration(appInfo, configurations);
 		writeToJson.postConfiguration(appInfo, configurations2);
 		Response names = configurationService.showFeatureConfigs(userId, "photon", "testComponent", "TestProject", "production", "");
 		Assert.assertEquals(200, names.getStatus());
 	}
 	
 	@Test
 	public void showFeatureConfigsErrorTest() throws PhrescoException {
 		Response names = configurationService.showFeatureConfigs(userId, "photon", "testFeature", "TestProject", "production", "");
 		Assert.assertEquals(200, names.getStatus());
 	}
 	
 	private String getConnectionUrl(String envName, String type, String configName) throws PhrescoException {
 		String configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName,null);
 		try {
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			Configuration configuration = configManager.getConfiguration(envName, type, configName);
 			Properties properties = configuration.getProperties();
 			String protocol = properties.getProperty("protocol");
 			String host = properties.getProperty("host");
 			String port = properties.getProperty("port");
 			return protocol + "," +host + "," + port;
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 }
