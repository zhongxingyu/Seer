 package com.xmlmachines.tests;
 
 import java.io.IOException;
 
import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 //@RunWith(JUnit4ClassRunner.class)
 @RunWith(Suite.class)
 @SuiteClasses({ MarkLogicConnectionTest.class,
 		MLBuildAndConfigurationTest.class, MixedDocumentParserTest.class })
 public class TestSuite {
 
	@BeforeClass
 	public static void setUp() throws IOException {
		com.xmlmachines.providers.GrizzlyContainerProvider.getInstance();
 	}
 
 	// @AfterClass
 	public static void tearDown() {
 	}
 
 }
