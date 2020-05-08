 package org.bh.tests.junit;
 
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Map;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IntegerValue;
 import org.bh.data.types.StringValue;
 import org.bh.plugin.apv.APVCalculator;
 import org.bh.plugin.directinput.DTODirectInput;
 import org.bh.plugin.fcf.FCFCalculator;
 import org.bh.plugin.fte.FTECalculator;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit tests for DCF procedures with integers
  * 
  * @author Norman
  * 
  * @version 0.1, 04.01.2010
  */
 public class DCFProceduresIntegerValues {
 
 	IShareholderValueCalculator svCalc;
 	IShareholderValueCalculator svCalc2;
 	IShareholderValueCalculator svCalc3;
 	
 	Map<String, Calculable[]> res;
 	Map<String, Calculable[]> res2;
 	Map<String, Calculable[]> res3;
 	
 	double uw0;
 	double uw0_2;
 	double uw0_3;
 	
 	DTOScenario scenario;
 	DTOPeriod period;
 	DTODirectInput dinp;
 
 	@Before
 	public void setUp() throws Exception {
 		scenario = new DTOScenario();
 		scenario.put(DTOScenario.Key.IDENTIFIER, new StringValue("TestScenario"));
 		scenario.put(DTOScenario.Key.RFK, new DoubleValue(0.10));
 		scenario.put(DTOScenario.Key.REK, new DoubleValue(0.11));
 		
 		scenario.put(DTOScenario.Key.BTAX, new DoubleValue(0.1694));
 		scenario.put(DTOScenario.Key.CTAX, new DoubleValue(0.26375));
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2007"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(100));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(1000));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 		
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2008"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(110));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(1100));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 		
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2009"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(120));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(1200));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 	
 		
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2010"));
 		
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(120));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(1200));
 		
 		period.addChild(dinp);		
 		
 		scenario.addChild(period);
 	}
 	
 	@Test
 	public void apv() {
 		svCalc = new APVCalculator();
 		svCalc.calculate(scenario);
 	}
 	
 	@Test
 	public void fcf() {
 		svCalc = new FCFCalculator();
 		svCalc.calculate(scenario);
 	}
 	
 	@Test
 	public void fte() {
 		svCalc = new FTECalculator();
 		svCalc.calculate(scenario);
 		
 	}
 	
 	@Test
 	public void compare() {
 		
 		
 		svCalc = new APVCalculator();
 		svCalc2 = new FTECalculator();
 		svCalc3 = new FCFCalculator();
 		
 		res = svCalc.calculate(scenario);
 		res2 = svCalc2.calculate(scenario);
 		res3 = svCalc3.calculate(scenario);
 		
		uw0 = ((DoubleValue)res.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0]).getValue();
		uw0_2 = ((DoubleValue)res2.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0]).getValue();
		uw0_3 = ((DoubleValue)res3.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0]).getValue();
 		
 		System.out.println("Results were " + uw0 + ", " + uw0_2 + ", " + uw0_3);
 		
 		// round
 		int decimalPlaces = 2;
 		double factor = Math.pow(10, decimalPlaces);
 		
 		uw0 = Math.round(uw0 * factor)/factor;
 		uw0_2 = Math.round(uw0_2 * factor)/factor;
 		uw0_3 = Math.round(uw0_3 * factor)/factor;
 		assertTrue("Equal results for APV, FTE, FCF procedure expected. Results were " + uw0 + ", " + uw0_2 + ", " + uw0_3 + " instead.", uw0 == uw0_2 && uw0 == uw0_3);
 	}
 }
