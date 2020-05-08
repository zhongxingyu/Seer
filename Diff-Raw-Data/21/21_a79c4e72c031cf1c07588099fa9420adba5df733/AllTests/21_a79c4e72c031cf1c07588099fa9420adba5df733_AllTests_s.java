 package org.jboss.tools.switchyard.ui.bot.test.suite;
 
 import junit.framework.TestSuite;
 
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.tools.switchyard.ui.bot.test.BindingsTest;
 import org.jboss.tools.switchyard.ui.bot.test.BottomUpBPELTest;
 import org.jboss.tools.switchyard.ui.bot.test.BottomUpBPMN2Test;
 import org.jboss.tools.switchyard.ui.bot.test.BottomUpCamelTest;
 import org.jboss.tools.switchyard.ui.bot.test.BottomUpEJBTest;
 import org.jboss.tools.switchyard.ui.bot.test.BreakpointTest;
 import org.jboss.tools.switchyard.ui.bot.test.DeploymentTest;
 import org.jboss.tools.switchyard.ui.bot.test.DomainSettingsTest;
 import org.jboss.tools.switchyard.ui.bot.test.DroolsTest;
 import org.jboss.tools.switchyard.ui.bot.test.ImplementationCamelTest;
 import org.jboss.tools.switchyard.ui.bot.test.ImplementationsPropertiesTest;
 import org.jboss.tools.switchyard.ui.bot.test.ProjectCapabilitiesTest;
 import org.jboss.tools.switchyard.ui.bot.test.ProjectCreationTest;
 import org.jboss.tools.switchyard.ui.bot.test.ThrottlingTest;
 import org.jboss.tools.switchyard.ui.bot.test.TopDownBPMN2Test;
 import org.jboss.tools.switchyard.ui.bot.test.TransformersTest;
 import org.jboss.tools.switchyard.ui.bot.test.ValidatorsTest;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
  * Test Suite
  * 
  * @author apodhrad
  *
  */
 @SuiteClasses({
 	ProjectCreationTest.class,
 	ProjectCapabilitiesTest.class,
 	BottomUpBPELTest.class,
 	BottomUpCamelTest.class,
 	BottomUpEJBTest.class,
 	BottomUpBPMN2Test.class,
 	TopDownBPMN2Test.class,
 	DroolsTest.class,
 	ImplementationCamelTest.class,
 	ImplementationsPropertiesTest.class,
 	DomainSettingsTest.class,
 	BindingsTest.class,
 	ThrottlingTest.class,
 	DeploymentTest.class,
 	TransformersTest.class,
 	ValidatorsTest.class,
 	BreakpointTest.class,
 	DeploymentTest.class
 })
 @RunWith(RedDeerSuite.class)
 public class AllTests extends TestSuite {
 
 }
