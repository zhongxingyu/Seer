 package com.photon.phresco.configuration;
 
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.google.gson.Gson;
 import com.photon.phresco.exception.ConfigurationException;
 
 public class ConfigReaderTest {
 	
 	private File configFile = new File("src/test/resources/phresco-env-config-read.xml");
 	private static ConfigReader configReader = null;
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testConfigReaderInputStream() throws FileNotFoundException, Exception {
 		configReader = new ConfigReader(new FileInputStream(configFile));
 		Assert.assertEquals("Testing", configReader.getDefaultEnvName());
 		// Failure test case
 		FileInputStream  fis = null;
 		configReader = new ConfigReader(fis);
 	}
 	
 	@Test(expected=ConfigurationException.class)
 	public void testConfigReaderInputStream1() throws FileNotFoundException, Exception {
 		//To throw parser exception
 		InputStream inputStream = IOUtils.toInputStream("To throw sax exception while parsing");
 		configReader = new ConfigReader(inputStream);
 	}
 	
 	@Test()
 	public void testConfigReaderFile() throws ConfigurationException {
 		configReader = new ConfigReader(configFile);
 		Assert.assertEquals("Testing", configReader.getDefaultEnvName());
 	}
 
 	@Test
 	public void testGetDefaultEnvName() {
 		String defaultEnvName = configReader.getDefaultEnvName();
 		Assert.assertEquals("Testing", defaultEnvName);
 	}
 
 	@Test
 	public void testGetConfigByEnv() {
 		List<Configuration> configByEnv = configReader.getConfigByEnv("Testing");
 		Assert.assertEquals(3, configByEnv.size());
 		
 		List<Configuration> stageConfig = configReader.getConfigByEnv("Staging");
 		Assert.assertEquals(0, stageConfig.size());
 		
 		List<Configuration> dev1Config = configReader.getConfigByEnv("Dev1");
 		Assert.assertEquals(3, dev1Config.size());
 	}
 
 	@Test
 	public void testGetConfigurations() {
 		List<Configuration> configurations = configReader.getConfigurations("Testing", "Database");
 		Assert.assertEquals(1, configurations.size());
 		Assert.assertEquals("database", configurations.get(0).getName());
 	}
 
 	@Test
 	public void testGetEnviroments() {
 		Map<String, Element> enviroments = configReader.getEnviroments();
 		Set<String> keySet = enviroments.keySet();
 		Assert.assertEquals(6, keySet.size());
 		Assert.assertEquals(true , keySet.contains("Production"));
 	}
 
//	@Test(expected = ConfigurationException.class)
 	public void testGetConfigFile() throws ConfigurationException {
 		File file = configReader.getConfigFile();
 		Assert.assertEquals(true, file.exists());
 		new ConfigReader(new File("src/test/resources/phresco-env-config1.xml"));
 	}
 
 	@Test
 	public void testGetConfigAsJSON() {
 		String configAsJSON = configReader.getConfigAsJSON("Production", "WebService");
 		Properties props = new Gson().fromJson(configAsJSON, Properties.class);
 		String value = props.getProperty("host");
 		Assert.assertEquals("localhost", value);
 		// Failure test case
 		String configAsJSON2 = configReader.getConfigAsJSON(null, "WebService");
 		props = new Gson().fromJson(configAsJSON2, Properties.class);
 		value = props.getProperty("host");
 		Assert.assertEquals("localhost", value);
 	}
 
 	@Test
 	public void testGetEnvironmentObatined() {
 		Environment env = configReader.getEnvironmentObatined("Development");
 		Assert.assertEquals("Development", env.getName());
 		Assert.assertEquals(3, env.getConfigurations().size());
 		//Failure test case
 		env = configReader.getEnvironmentObatined("Development1");
 		Assert.assertNull(env);
 	}
 
 	@Test
 	public void testGetDocument() {
 		Document document = configReader.getDocument();
 		Element elementById = document.getElementById("environments");
 		Assert.assertNull(elementById);
 	}
 
 	@Test
 	public void testGetAllEnvironments() {
 		List<Environment> envs = configReader.getAllEnvironments();
 		Assert.assertEquals(6, envs.size());
 	}
 
 	@Test
 	public void testGetAllEnvironmentsAlone() {
 		List<Environment> envs = configReader.getAllEnvironmentsAlone();
 		Assert.assertEquals(6, envs.size());
 	}
 	
 	@Test
 	public void testCanDelete() {
 		Map<String, Element> enviroments = configReader.getEnviroments();
 		Element element = enviroments.get("Testing1");
 		boolean cd = configReader.canDelete(element);
 		Assert.assertEquals(true, cd);
 		
 		element = enviroments.get("Testing2");
 		cd = configReader.canDelete(element);
 		Assert.assertEquals(false, cd);
 	}
 	
 	@Test
 	public void coverParserException() {
 	   DocumentBuilderFactory factory = new DocumentBuilderFactory() {
 	      public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
 	         throw new ParserConfigurationException("test");
 	      }
 	      public void setAttribute(String name, Object value) throws IllegalArgumentException {
 	      }
 	      public Object getAttribute(String name) throws IllegalArgumentException {
 	         return null;
 	      }
 	      @Override
 			public boolean getFeature(String name)throws ParserConfigurationException {
 				return false;
 			}
 			@Override
 			public void setFeature(String name, boolean value) throws ParserConfigurationException {
 			}
 		 };
 	   try
 	   {
 	      configReader.setupBuilder(factory);
 	   }
 	   catch (IllegalArgumentException iae)
 	   {
 	      return;
 	   }
 	}
 	
 	@Test(expected = ConfigurationException.class)
 	public void testCanDelete1() throws Exception {
 		InputStream mock = org.mockito.Mockito.mock(InputStream.class);
 		when(mock.read()).thenThrow(IOException.class);
 		configReader = new ConfigReader(mock);
 	}
 }
