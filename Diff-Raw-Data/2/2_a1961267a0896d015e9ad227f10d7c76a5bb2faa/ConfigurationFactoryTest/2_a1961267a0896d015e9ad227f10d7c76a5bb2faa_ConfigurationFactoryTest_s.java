 package com.operativus.senacrs.audit.model.config;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.testutils.TestBoilerplateUtils;
 
 
 public class ConfigurationFactoryTest {
 
 	private File tempFile = null;
 	
 	@Before
 	public void setUp() throws Exception {
 
 		tempFile = File.createTempFile("config", null);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		this.tempFile = null;
 	}
 
 	@Test
 	public void testCreateConfigurationString() throws IOException {
 		
 		Configuration result = null;
 		Configuration expected = null;
 		
 		expected = createRandomConfiguration();
 		putToFile(expected);
 		result = ConfigurationFactory.createConfiguration(this.tempFile.getAbsolutePath());
 		Assert.assertEquals(expected, result);
 	}
 
 	private Configuration createRandomConfiguration() {
 		
 		Configuration result = null;
 		
 		result = new Configuration();
 		result.setBaseUrl(TestBoilerplateUtils.randomString());
 		result.setVersion(TestBoilerplateUtils.randomString());
 		result.setUsername(TestBoilerplateUtils.randomString());
 		result.setPassword(TestBoilerplateUtils.randomString());
 
 		return result;
 	}
 
 	private void putToFile(Configuration obj) throws IOException {
 
 		BufferedWriter writer = null;
 		
 		writer = new BufferedWriter(new FileWriter(tempFile));
 		for (ConfigurationFactory.ConfigKey k : ConfigurationFactory.ConfigKey.values()) {
 			putLine(writer, k, obj);
 		}
 	}	
 	
 	private void putLine(BufferedWriter writer, ConfigurationFactory.ConfigKey key, Configuration obj) throws IOException {
 		
 		switch (key) {
 		case BASE_URL:
 			putLine(writer, key, obj.getBaseUrl());
 			break;
 		case PASSWORD:
 			putLine(writer, key, obj.getPassword());			
 			break;
 		case USERNAME:
 			putLine(writer, key, obj.getUsername());
 			break;
 		case VERSION:
 			putLine(writer, key, obj.getVersion());
 			break;
 		default:
 			throw new IllegalArgumentException(String.valueOf(key));
 		}
 	}
 
 	private void putLine(BufferedWriter writer, ConfigurationFactory.ConfigKey key, Object value) throws IOException {
 		
 		String line = null;
 		
 		line = key.getKey() + " = " + String.valueOf(value);
 		writer.write(line);
 		writer.newLine();		
 	}
 }
