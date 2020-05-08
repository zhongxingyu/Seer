 package com.photon.phresco.commons.api;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.photon.phresco.commons.impl.ConfigManagerImpl;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.util.FileUtil;
 
 public class ConfigManagerTest {
 	
 	private ConfigManager configManager = null;
 	private File configFile = null;
 	
 	@Before
 	public void initTest() throws ConfigurationException {
 		configFile  = new File("src/test/java/com/photon/phresco/commons/api/config.xml");
 		configManager = new ConfigManagerImpl(configFile);
 	}
 	
 	@Test
 	public void testAddEnvironments() throws ConfigurationException {
 		if(configFile.exists()) {
 			FileUtil.delete(configFile);
 		}
 		List<Environment> environments = new ArrayList<Environment>();
 		Environment environment = new Environment("Production", "Test For Production", true);
 		List<Configuration> configurations = new ArrayList<Configuration>();
 		Properties properties = new Properties();
 		properties.put("host", "1010");
 		properties.put("port", "1010");
 		Configuration serverConfig  = new Configuration("Server", "server", "TestServer", properties , "php,drupl");
 		Configuration dbConfig  = new Configuration("Database", "database", "TestDatabase", properties , "php,drupl");
 		configurations.add(serverConfig);
 		configurations.add(dbConfig);
 		environment.setConfigurations(configurations);
 		environments.add(environment);
 		configManager.addEnvironments(environments);
 	}
 	
 	@Test
 	public void testGetEnvironments() throws ConfigurationException {
 		List<Environment> environments = configManager.getEnvironments(createEnvNames());
 		Assert.assertEquals(1, environments.size());
 	}
 
	@Test
 	public void testUpdateEnvironment() throws ConfigurationException {
 		Environment environment = new Environment("Production", "Test For Production", true);
 		List<Configuration> configurations = new ArrayList<Configuration>();
 		Properties properties = new Properties();
 		properties.put("host", "2020");
 		properties.put("port", "2020");
 		Configuration serverConfig  = new Configuration("Server", "server", "TestServer", properties , "php,drupl");
 		Configuration dbConfig  = new Configuration("Database", "database", "TestDatabase", properties , "php,drupl");
 		configurations.add(serverConfig);
 		configurations.add(dbConfig);
 		environment.setConfigurations(configurations);
 		configManager.updateEnvironment(environment);
 		List<Environment> environments = configManager.getEnvironments(createEnvNames());
 		Configuration configuration = environments.get(0).getConfigurations().get(0);
 		Assert.assertEquals("2020", configuration.getProperties().get("host"));
 	}
 
 //	@Test
 //	public void testDeleteEnvironment() throws ConfigurationException {
 //		configManager.deleteEnvironment("Production");
 //	}
 	
 	@Test
 	public void testGetgetConfigurations () throws ConfigurationException {
 		List<Configuration> configurations = configManager.getConfigurations("Production", "TestServer");
 		Assert.assertEquals(1, configurations.size());
 	}
 	
 	private List<String> createEnvNames() {
 		List<String> envNames = new ArrayList<String>();
 		envNames.add("Production");
 		return envNames;
 	}
 }
