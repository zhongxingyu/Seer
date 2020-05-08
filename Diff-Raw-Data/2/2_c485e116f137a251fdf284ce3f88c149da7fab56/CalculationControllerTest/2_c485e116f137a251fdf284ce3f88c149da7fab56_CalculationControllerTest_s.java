 package com.qut.spc.controller;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.qut.spc.api.SystemCalculationAPI;
 import com.qut.spc.calculations.ROICalculator;
 import com.qut.spc.exceptions.InvalidArgumentException;
 import com.qut.spc.model.Battery;
 import com.qut.spc.model.Inverter;
 import com.qut.spc.model.Panel;
 
 import static org.mockito.Mockito.*;
 public class CalculationControllerTest {
 	
 	private CalculationController controller;
 	private SystemCalculationAPI calculator;
 	private SystemCalculationAPI container;
 	private String result;
 	
 	@Before
 	public void setup(){
 		calculator=mock(SystemCalculationAPI.class);
 
 		controller=new CalculationController(calculator,new ROICalculator());
 		result = controller.getCalculations(32,4,42,24,"1234",2442,21,22,23,3443);
 	}
 	
 	@Test
 	public void testCalculations_validInput_PropertiesOnCalculatorIsProperlySet(){		
 		verify(calculator).setBatteryId(42);
 		verify(calculator).setInverterId(24);
 		verify(calculator).setLocation("1234");
 		verify(calculator).setPanelId(32);
		verify(calculator, times(4)).getElectricityProduction();	
 	}
 	
 	
 	@Test
 	public void testCalculations_invalidPanelIdAndPanelEfficiency_correctErrorMessageIsReturned(){
 		testCalculationInputPanels(-1, 100, 100);
 	}
 	
 	
 	@Test
 	public void testCalculations_invalidPanelIdAndPanelOutput_correctErrorMessageIsReturned(){
 		testCalculationInputPanels(100d, -1d, 100d);
 	}
 	
 	@Test
 	public void testCalculations_invalidPanelIdAndSystemCost_correctErrorMessageIsReturned(){
 		testCalculationInputPanels(100d, 100, -1);
 	}
 	
 	private void testCalculationInputPanels(double eff,double output,double cost){
 		verifyErrorMessageFromCalculation("PanelId and panelEfficiency, panelOutput or systemCost is not defined \n",-1, 1, 1, 1, "1111", cost, 100, eff,output,2);
 
 	}
 	
 	@Test
 	public void testCalculationInputBattery_invalidBatteryAndSystemCost_correctErrorMessageIsReturned(){
 		verifyErrorMessageFromCalculation("BatteryId is not defined and neither is systemCost \n",1, 1, -1, 1, "1111", -1, 100, 100,100,43);
 
 	}
 	
 	@Test
 	public void testCalculationInputInverter_invalidInverterIdAndEfficiency_correctErrorMessageIsReturned(){
 		verifyErrorMessageFromCalculation("InverterId and inverterEfficiency or systemCost is not defined \n",1, 1, 1, -1, "1111", 1, -100, 100,100,23);
 	}
 	
 	
 	@Test
 	public void testCalculationInputInverter_invalidInverterIdAndSystemCost_correctErrorMessageIsReturned(){
 		verifyErrorMessageFromCalculation("InverterId and inverterEfficiency or systemCost is not defined \n",1, 1, 1, -1, "1111", -1, 100, 100,100,23);		
 
 	}
 
 	private void verifyErrorMessageFromCalculation(String message,int pid, int pc, int bid,int iid,String poc, double Sc, double ie, double pe, double po,double usage){
 		String result="";
 		try{
 			controller.getCalculations(pid, pc, bid, iid, poc, Sc, ie, pe, po,usage);
 		}catch(InvalidArgumentException e){
 			result=e.getResponse().getEntity().toString();
 		}		
 		assertEquals(message, result);
 	}
 	
 	@Test
 	public void testGetCalculations_invalidBatteryId_newBatteryIsSet(){
 		System.out.println("WEgk");
 		controller.getCalculations(-1, 1, -1, -1, "4000", 3000, 200, 200, 200,23);
 		verify(calculator).setBattery(any(Battery.class));
 	}
 	
 	@Test
 	public void testGetCalculations_invalidPanelId_newPanelIsSet(){
 		controller.getCalculations(-1, 1, -1, -1, "4000", 3000, 200, 200, 200,42);
 		verify(calculator).setPanel(any(Panel.class));
 	}
 	@Test
 	public void testGetCalculations_invalidInverterId_newInverterIsSet(){
 		controller.getCalculations(-1, 1, -1, -1, "4000", 3000, 200, 200, 200,53);
 		verify(calculator).setInverter(any(Inverter.class));
 	}
 	
 	
 }
