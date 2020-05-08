 package tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import solarPowerCalculator.CalculatorException;
 import powerCalculations.SolarOutput;
 import environmentalSpecifications.*;
 
 
 /**
  * Test Cases for the Solar Output Class
  * @author Brendon
  *
  */
 public class SolarOutputTests {
 
 	private SystemConfiguration testSystem;
 	private LocationDetails testLocation;
 	private double delta = 0.01;
 	private double moneyDelta = 0.01;
 
 	@Before
 	public void initObjects() throws CalculatorException {
 		testSystem = new SystemConfiguration(250, 2, 0.96);
 		testLocation = new LocationDetails(4.5, 300, 0.08);
 	}
 	
 	@Test public void calculateHourlyOutputStandardValues() {
 		assertEquals(480, SolarOutput.calculateHourlyOutput(testSystem), delta);
 	}
 	
 	@Test public void calculateDailyOutputStandardValues() {
 		assertEquals(2.16, SolarOutput.calculateGrossDailyOutput(testLocation, testSystem), delta);
 	}
 	
 	@Test public void calculateMonthlyOutputStandardValues() {
 		assertEquals(64.8, SolarOutput.calculateGrossMonthlyOutput(testLocation, testSystem), delta);
 	}
 	
 	
 	//------------------- COST CALCULATIONS ------------------------------------------------------------------
 	
 	//------------------- TOTAL SYSTEM COST ------------------------------------------------------------------
 	@Test public void calculate1KWSystemCost() throws CalculatorException {
 		testSystem = new SystemConfiguration(250, 4, 0.96);
		assertEquals(3296.88, SolarOutput.calculateSystemCost(testSystem), moneyDelta);
 		
 	}
 	
 	@Test public void calculate2KWSystemCost() throws CalculatorException {
 		testSystem = new SystemConfiguration(500, 4, 0.96);
		assertEquals(5240.09, SolarOutput.calculateSystemCost(testSystem), moneyDelta);
 		
 	}
 	
 	@Test public void calculate3KWSystemCost() throws CalculatorException {
 		testSystem = new SystemConfiguration(300, 10, 0.96);
		assertEquals(7099.08, SolarOutput.calculateSystemCost(testSystem), moneyDelta);
 		
 	}
 	
 	@Test public void calculate4KWSystemCost() throws CalculatorException {
 		testSystem = new SystemConfiguration(400, 10, 0.96);
		assertEquals(8873.85, SolarOutput.calculateSystemCost(testSystem), moneyDelta);
 		
 	}
 	
 	@Test public void calculate5KWSystemCost() throws CalculatorException {
 		testSystem = new SystemConfiguration(250, 20, 0.96);
		assertEquals(10564.41, SolarOutput.calculateSystemCost(testSystem), moneyDelta);
 		
 	}
 	
 	//------------------- COMPONENT COST CALCULATIONS ------------------------------------------------------------------
 	
 	@Test public void calculatePanelCost() {
 		assertEquals(500, SolarOutput.calculateTotalPanelCost(testSystem), moneyDelta);
 	}
 	
 	@Test public void calculateFlatRateInstallCost() {
 		assertEquals(1000, SolarOutput.calculateInstallCost(), moneyDelta);
 	}
 	
 	//------------------- OUTPUT DOLLAR VALUE CALCULATIONS -------------------------------------------------------------
 
 	@Test public void calculateMonthlyOutputValue() {
 		assertEquals(5.18, SolarOutput.calculateMonthlyOutputValue(testSystem, testLocation), moneyDelta);
 	}
 	
 	//------------------- BREAKEVEN CALCULATIONS -----------------------------------------------------------------------
 	@Test public void calculateBreakeven() {
		assertEquals("36 years, 10 months", SolarOutput.calculateBreakEven(testSystem, testLocation));
 	}
 }
