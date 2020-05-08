 package net.java.dev.cejug.classifieds.test.integration.admin;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 @RunWith(Suite.class)
 @Suite.SuiteClasses( { CategoryMaintenanceIntegrationTest.class,
 		DomainMaintenanceIntegrationTest.class,
		CheckMonitorIntegrationTest.class })
 public class AdminTestSuite {
 }
