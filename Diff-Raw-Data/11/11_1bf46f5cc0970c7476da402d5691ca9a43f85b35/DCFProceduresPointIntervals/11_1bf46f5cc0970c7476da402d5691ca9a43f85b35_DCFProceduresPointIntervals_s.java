 package org.bh.tests.junit;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Map;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IntegerValue;
 import org.bh.data.types.IntervalValue;
 import org.bh.data.types.StringValue;
 import org.bh.plugin.apv.APVCalculator;
 import org.bh.plugin.directinput.DTODirectInput;
 import org.bh.plugin.fcf.FCFCalculator;
 import org.bh.plugin.fte.FTECalculator;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit tests for DCF procedures with point intervals. Every DCF procedure will
  * be run once with integer values as input data and once with intervals(point
  * intervals corresponding to the integer value).
  * 
  * The result interval must contain the integer (double) result
  * 
  * @author Norman
  * 
  * @version 0.1, 04.01.2010
  */
 public class DCFProceduresPointIntervals {
 
 	IShareholderValueCalculator svCalc;
 
 	Map<String, Calculable[]> res;
 	Map<String, Calculable[]> res2;
 
 	DoubleValue uw0;
 	IntervalValue uw0_2;
 
 	DTOScenario scenarioInterval;
 	DTOScenario scenarioInteger;
 	DTOPeriod period;
 	DTODirectInput dinp;
 
 	@Before
 	public void setUp() throws Exception {
 
 		DoubleValue rfk = new DoubleValue(0.10);
 		DoubleValue rek =  new DoubleValue(0.11);
 		DoubleValue btax = new DoubleValue(0.1694);
 		DoubleValue ctax = new DoubleValue(0.26375);
 		
 		// 2007; 2008; 2009; 2010
 		int[] fcf = new int[]{ 100, 110, 120, 120 };
 		int[] liabilities = new int[]{ 1000, 1100, 1200, 1200 };
 		
 		
 		
 		// Scenario with integer Values for FCF and Liabilities
 		scenarioInteger = new DTOScenario();
 		scenarioInteger.put(DTOScenario.Key.IDENTIFIER, new StringValue(
 				"Test scenarioInteger"));
 		scenarioInteger.put(DTOScenario.Key.RFK, rfk);
 		scenarioInteger.put(DTOScenario.Key.REK, rek);
 
 		scenarioInteger.put(DTOScenario.Key.BTAX, btax);
 		scenarioInteger.put(DTOScenario.Key.CTAX, ctax);
 
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2007"));
 
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(fcf[0]));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(liabilities[0]));
 
 		period.addChild(dinp);
 
 		scenarioInteger.addChild(period);
 
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2008"));
 
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(fcf[1]));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(liabilities[1]));
 
 		period.addChild(dinp);
 
 		scenarioInteger.addChild(period);
 
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2009"));
 
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(fcf[2]));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(liabilities[2]));
 
 		period.addChild(dinp);
 
 		scenarioInteger.addChild(period);
 
		period = new DTOPeriod();
		period.put(DTOPeriod.Key.NAME, new StringValue("2010"));

		dinp = new DTODirectInput();
		dinp.put(DTODirectInput.Key.FCF, new IntegerValue(fcf[3]));
		dinp.put(DTODirectInput.Key.LIABILITIES, new IntegerValue(liabilities[3]));

		period.addChild(dinp);

		scenarioInteger.addChild(period);

 		// scenario with point intervals for Free Cash Flow (FCF) and
 		// Liabilities
 		scenarioInterval = new DTOScenario();
 		scenarioInterval.put(DTOScenario.Key.IDENTIFIER, new StringValue(
 				"TestScenario"));
 		scenarioInterval.put(DTOScenario.Key.RFK, rfk);
 		scenarioInterval.put(DTOScenario.Key.REK, rek);
 
 		scenarioInterval.put(DTOScenario.Key.BTAX, btax);
 		scenarioInterval.put(DTOScenario.Key.CTAX, ctax);
 
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2007"));
 
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(fcf[0], fcf[0]));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(liabilities[0], liabilities[0]));
 
 		period.addChild(dinp);
 
 		scenarioInterval.addChild(period);
 
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2008"));
 
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(fcf[1], fcf[1]));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(liabilities[1], liabilities[1]));
 
 		period.addChild(dinp);
 
 		scenarioInterval.addChild(period);
 
 		period = new DTOPeriod();
 		period.put(DTOPeriod.Key.NAME, new StringValue("2009"));
 
 		dinp = new DTODirectInput();
 		dinp.put(DTODirectInput.Key.FCF, new IntervalValue(fcf[2], fcf[2]));
 		dinp.put(DTODirectInput.Key.LIABILITIES, new IntervalValue(liabilities[2], liabilities[2]));
 
 		period.addChild(dinp);
 
 		scenarioInterval.addChild(period);
 	}
 
 	@Test
 	public void apv() {
 		svCalc = new APVCalculator();
 		res = svCalc.calculate(scenarioInteger, true);
 		res2 = svCalc.calculate(scenarioInterval, true);
 
 		uw0 = (DoubleValue) res
 				.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0];
 		uw0_2 = ((IntervalValue) res2
 				.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0]);
 
 		System.out.println("Results APV: " + uw0 + " ; " + uw0_2);
 		assertTrue(uw0_2.contains(uw0.getValue()));
 	}
 
 	@Test
 	public void fcf() {
 		svCalc = new FCFCalculator();
 
 		res = svCalc.calculate(scenarioInteger, true);
 		res2 = svCalc.calculate(scenarioInterval, true);
 
 		uw0 = (DoubleValue) res
 				.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0];
 		uw0_2 = (IntervalValue) res2
 				.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0];
 
 		System.out.println("Results FCF: " + uw0 + " ; " + uw0_2);
 		assertTrue(uw0_2.contains(uw0.getValue()));
 	}
 
 	@Test
 	public void fte() {
 		svCalc = new FTECalculator();
 		res = svCalc.calculate(scenarioInteger, true);
 		res2 = svCalc.calculate(scenarioInterval, true);
 
 		uw0 = (DoubleValue) res
 				.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0];
 		uw0_2 = (IntervalValue) res2
 				.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0];
 
 		System.out.println("Results FTE: " + uw0 + " ; " + uw0_2);
 		assertTrue(uw0_2.contains(uw0.getValue()));
 	}
 }
