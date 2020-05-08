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
 
		cs.setParameter(ParameterApplyingTestClass.class.getName() + cs.getPrefixNameSeperator() + "c", "java.lang.Long");
		cs.setParameter(ParameterApplyingTestClass.class.getName() + cs.getPrefixNameSeperator() + "longValue", "357951");
		cs.setParameter(ParameterApplyingTestClass.class.getName() + cs.getPrefixNameSeperator() + "intValue", "24685");
 
 		ParameterApplyingTestClass patc = new ParameterApplyingTestClass();
 
 		c.applyConfiguration(patc);
 		assertEquals(Long.class, patc.c);
 		assertEquals(357951L, patc.longValue);
 		assertEquals(24685, patc.intValue);
 	}
 
 }
