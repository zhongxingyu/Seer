 package net.noratargo.siJACK;
 
 import static org.junit.Assert.*;
 import net.noratargo.siJACK.ConfigurationStorage;
 import net.noratargo.siJACK.Configurator;
 import net.noratargo.siJACK.interfaces.InstantiatorManager;
 import net.noratargo.siJACK.testClasses.ParameterApplyingTestClass;
 
 import org.junit.Test;
 
 public class TestParameterApplyling {
 
 	@Test
 	public final void testAlternativeParameterNames() {
 		InstantiatorManager im = new InstantiatorStorage();
 		ConfigurationStorage cs = new ConfigurationStorage(":", im);
 		Configurator c = new Configurator(cs);
 		c.addConfigureable(ParameterApplyingTestClass.class);
 
		cs.setParameter("net.noratargo.applicationFramework.testClasses.ParameterApplyingTestClass:c", "java.lang.Long");
		cs.setParameter("net.noratargo.applicationFramework.testClasses.ParameterApplyingTestClass:longValue", "357951");
		cs.setParameter("net.noratargo.applicationFramework.testClasses.ParameterApplyingTestClass:intValue", "24685");
 
 		ParameterApplyingTestClass patc = new ParameterApplyingTestClass();
 
 		c.applyConfiguration(patc);
 		assertEquals(Long.class, patc.c);
 		assertEquals(357951L, patc.longValue);
 		assertEquals(24685, patc.intValue);
 	}
 
 }
