 package net.noratargo.siJACK;
 
 import static org.junit.Assert.*;
 import net.noratargo.siJACK.ConfigurationStorage;
 import net.noratargo.siJACK.Configurator;
 import net.noratargo.siJACK.interfaces.InstantiatorManager;
 import net.noratargo.siJACK.testClasses.AlternatePrefixTestClass;
 import net.noratargo.siJACK.testClasses.ValueSettingTestClass;
 
 import org.junit.Test;
 
 public class TestInstantiation {
 
 	@Test
 	public final void testConfigurationStorage() {
 		InstantiatorManager im = new InstantiatorStorage();
 		ConfigurationStorage cs = new ConfigurationStorage(":", im);
 		Configurator c = new Configurator(cs);
 
 		c.addConfigureable(new ValueSettingTestClass());
 
		cs.setParameter(ValueSettingTestClass.class.getName(), "name", "admin");
 		ValueSettingTestClass vstc = new ValueSettingTestClass();
 
 		c.applyConfiguration(vstc);
 
 		assertEquals("admin", vstc.name);
 	}
 
 	@Test
 	public final void testConfigurationBeforeAddingConfigureable() {
 		InstantiatorManager im = new InstantiatorStorage();
 		ConfigurationStorage cs = new ConfigurationStorage(":", im);
 		Configurator c = new Configurator(cs);
 
		cs.setParameter(ValueSettingTestClass.class.getName(), "name", "admin");
 		c.addConfigureable(new ValueSettingTestClass());
 		ValueSettingTestClass vstc = new ValueSettingTestClass();
 
 		c.applyConfiguration(vstc);
 
 		assertEquals("admin", vstc.name);
 	}
 
 	@Test
 	public final void testAlternativeParameterNames() {
 		InstantiatorManager im = new InstantiatorStorage();
 		ConfigurationStorage cs = new ConfigurationStorage(":", im);
 		Configurator c = new Configurator(cs);
 		cs.setParameter("current preix:alternatePrefixParam", "prefix_");
 
 		c.addConfigureable(AlternatePrefixTestClass.class);
 
 		cs.setParameter("some.other.package.path:dateiname", "/etc/fstab");
 		cs.setParameter("some.other.package.path:username", "root");
 
 		AlternatePrefixTestClass iwp = new AlternatePrefixTestClass();
 
 		c.applyConfiguration(iwp);
 		assertEquals("prefix_", iwp.getAlternatePrefixParam());
 		assertEquals("/etc/fstab", iwp.getFilename());
 		assertEquals("root", iwp.getUsername());
 //		assertEquals("", iwp.getAlternatePrefixParam());
 //		assertEquals("", iwp.getAlternatePrefixParam());
 //		assertEquals("", iwp.getAlternatePrefixParam());
 	}
 
 }
