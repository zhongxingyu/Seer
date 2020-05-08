 package com.operativus.senacrs.audit.model.config;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.exceptions.MissingMinimalConfigurationEntry;
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
 		result.setBaseUrl(TestBoilerplateUtils.randomAlphanumericString());
 		result.setVersion(TestBoilerplateUtils.randomAlphanumericString());
 		result.setUsername(TestBoilerplateUtils.randomAlphanumericString());
 		result.setPassword(TestBoilerplateUtils.randomAlphanumericString());
 
 		return result;
 	}
 
 	private void putToFile(Configuration obj) throws IOException {
 
 		BufferedWriter writer = null;
 		
 		writer = new BufferedWriter(new FileWriter(tempFile));
 		for (ConfigurationFactory.ConfigKey k : ConfigurationFactory.ConfigKey.values()) {
 			putLine(writer, k, obj);
 		}
 		writer.flush();
 		writer.close();
 	}	
 	
 	private void putLine(BufferedWriter writer, ConfigurationFactory.ConfigKey key, Configuration obj) throws IOException {
 		
 		switch (key) {
 		case BASE_URL:
 			putNonNullValueLine(writer, key, obj.getBaseUrl());
 			break;
 		case PASSWORD:
 			putNonNullValueLine(writer, key, obj.getPassword());			
 			break;
 		case USERNAME:
 			putNonNullValueLine(writer, key, obj.getUsername());
 			break;
 		case VERSION:
 			putNonNullValueLine(writer, key, obj.getVersion());
 			break;
 		default:
 			throw new IllegalArgumentException(String.valueOf(key));
 		}
 	}
 
 	private void putNonNullValueLine(BufferedWriter writer, ConfigurationFactory.ConfigKey key, Object value) throws IOException {
 		
 		String line = null;
 		
 		if (value != null) {
 			line = key.getKey() + " = " + String.valueOf(value);
 			writer.write(line);
 			writer.newLine();
 		}
 	}
 
 	@Test
 	public void testCreateConfigurationNoBaseUrl() throws IOException {
 		
 		Configuration template = null;
 		
 		template = createRandomConfiguration();
 		template.setBaseUrl(null);
 		putToFile(template);
 		try {
 			ConfigurationFactory.createConfiguration(this.tempFile.getAbsolutePath());
 			Assert.fail();
 		} catch (MissingMinimalConfigurationEntry e) {
			Assert.assertTrue(true);			
 		}
 	}
 
 	@Test
 	public void testCreateConfigurationNoVersion() throws IOException {
 		
 		Configuration template = null;
 		
 		template = createRandomConfiguration();
 		template.setVersion(null);
 		putToFile(template);
 		try {
 			ConfigurationFactory.createConfiguration(this.tempFile.getAbsolutePath());
 			Assert.fail();
 		} catch (MissingMinimalConfigurationEntry e) {
			Assert.assertTrue(true);			
 		}
 	}
 
 	@Test
 	public void testCreateConfigurationNoUsername() throws IOException {
 		
 		Configuration template = null;
 		
 		template = createRandomConfiguration();
 		template.setUsername(null);
 		putToFile(template);
 		try {
 			ConfigurationFactory.createConfiguration(this.tempFile.getAbsolutePath());
 			Assert.fail();
 		} catch (MissingMinimalConfigurationEntry e) {
			Assert.assertTrue(true);			
 		}
 	}
 
 	@Test
 	public void testCreateConfigurationNoPassword() throws IOException {
 		
 		Configuration template = null;
 		Configuration result = null;
 		
 		template = createRandomConfiguration();
 		template.setPassword(null);
 		putToFile(template);
 		result = ConfigurationFactory.createConfiguration(this.tempFile.getAbsolutePath());
 		Assert.assertEquals(template, result);
 		Assert.assertNull(result.getPassword());
 	}
 }
