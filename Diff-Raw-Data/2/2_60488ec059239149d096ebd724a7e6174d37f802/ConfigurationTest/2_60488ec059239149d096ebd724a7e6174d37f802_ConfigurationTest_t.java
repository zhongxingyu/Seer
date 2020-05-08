 package com.bartels.sc4j;
 
 import com.bartels.sc4j.file.PropertyFile;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Unit test for simple configurations
  */
 public class ConfigurationTest extends TestCase {
 	
 	
 	/**
 	 * the configuration interface to test
 	 * 
 	 * @author bartels
 	 *
 	 */
 	@Provider("com.bartels.sc4j.file.PropertyFileConfigurationProvider") // this is the default provider that will be used if no provider annotation is defined -> only to show the functionality
 	@PropertyFile("sc4j.properties")
 	public interface MyConfiguration {
 		
 		/**
 		 * @return the <code>email.host</code> property
 		 */
 		String emailHost();
 		
 		/**
 		 * @return the <code>email.port</code> property
 		 */
          @DefaultValue("10") // default value will be returned if the property was not found by the provider
 		int emailPort();
 		
          @DefaultValue("5")
         int commandLineArg();
          
 		long emailInterval();
 		
 		char emailIdentifier();
 		
 		short emailShort();
 		
 		byte emailByte();
 		
 		double emailDouble();
 		
 		float emailFloat();
 		
 		@PropertyPath("my.property")
 		String someOtherProp();
 		
 		@PropertyPath("my-crazY!PROP#p*a+t&h")
 		String crazyPropertyPath();
 	}
 	
 	/**
 	 * Create the test case
 	 * 
 	 * @param testName
 	 *            name of the test case
 	 */
 	public ConfigurationTest(String testName) {
 		super(testName);
 	}
 
 	/**
 	 * @return the suite of tests being tested
 	 */
 	public static Test suite() {
 		return new TestSuite(ConfigurationTest.class);
 	}
 
 	/**
 	 * Rigourous Test :-)
 	 */
 	public void testMyConfiguration() {
 		// create the configuration instance
 		MyConfiguration config = ConfigurationFactory.create(MyConfiguration.class, new String[]{"--command.line.arg", "15"});
 		assertNotNull(config);
 		
 		assertEquals("smtp@bartels.de", config.emailHost());
 		
 		assertEquals((int) 10, config.emailPort());
 		
 		assertEquals((long) 500, config.emailInterval());
 		
 		assertEquals("A".charAt(0), config.emailIdentifier());
 		
 		assertEquals((short) 277, config.emailShort());
 		
 		assertEquals((byte) 127, config.emailByte());
 		
 		assertEquals((double) 0.815, config.emailDouble());
 		
 		assertEquals((float) 3.14, config.emailFloat());
 		
 		assertEquals("MyPropertyValue", config.someOtherProp());
 		
		assertEquals((int) 15, config.commandLineArg());
 		
 		assertEquals("TestValue", config.crazyPropertyPath());
 		System.out.println("TEST finished");
 	}
 }
