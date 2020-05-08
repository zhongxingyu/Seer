 package org.gwtapp.ccalc.rpc.proc.calculator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gwtapp.ccalc.rpc.data.book.Calculation;
 import org.gwtapp.ccalc.rpc.data.book.Currency;
 import org.gwtapp.ccalc.rpc.data.book.Operation;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class CalculationTest {
 	@Test
	public void simpleTest() {
 		List<Operation> operations = new ArrayList<Operation>();
 		Calculator calculator = new Calculator(Currency.PLN, operations);
 		List<Calculation> calculations = calculator.getCalculations();
 		Assert.assertNotNull(calculations);
 		Assert.assertTrue(calculations.isEmpty());
 	}
 }
