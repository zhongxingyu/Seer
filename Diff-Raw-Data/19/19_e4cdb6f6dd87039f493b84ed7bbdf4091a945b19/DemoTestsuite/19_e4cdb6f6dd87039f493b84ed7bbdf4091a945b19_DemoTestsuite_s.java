 package de.pellepelster.myadmin.demo.server.test;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
import de.pellepelster.myadmin.demo.server.test.dictionary.DemoBaseEntityServiceTest;
import de.pellepelster.myadmin.demo.server.test.dictionary.DemoDictionaryTest;
import de.pellepelster.myadmin.demo.server.test.dictionary.DemoHierarchicalTest;

 @RunWith(Suite.class)
// @SuiteClasses({ DemoDictionaryServiceRemoteTest.class,
// DemoModuleServiceRemoteTest.class })
@SuiteClasses({ EntityVOMapperTest.class, DemoDictionaryTest.class, DemoHierarchicalTest.class, DemoBaseEntityServiceTest.class })
 public class DemoTestsuite
 {
 }
