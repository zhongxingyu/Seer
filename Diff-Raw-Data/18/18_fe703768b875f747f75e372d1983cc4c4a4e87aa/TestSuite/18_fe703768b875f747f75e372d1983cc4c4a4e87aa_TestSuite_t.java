 package unitTests;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 @RunWith(Suite.class)
 @SuiteClasses({
 	CalculatorTest.class,
 	SolarServletTests.class,
 	SolarSystemInfoTests.class,
	TariffTests.class	
 })
 public class TestSuite {
 
 }
