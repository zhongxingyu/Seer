 package com.gentics.cr.lucene.indexer.transformer;
 
 import java.io.File;
 import java.net.URISyntaxException;
 
 import org.junit.BeforeClass;
 
 import com.gentics.DefaultTestConfiguration;
 import com.gentics.cr.configuration.EnvironmentConfiguration;
 import com.gentics.cr.exceptions.CRException;
 
 public class AbstractTransformerTest {
 
 	@BeforeClass
 	public static void setUp() throws CRException, URISyntaxException {
		EnvironmentConfiguration.setConfigPath(new File(DefaultTestConfiguration.class.getResource("conf/gentics").toURI()).getAbsolutePath());
 		EnvironmentConfiguration.loadEnvironmentProperties();
 	}
 }
