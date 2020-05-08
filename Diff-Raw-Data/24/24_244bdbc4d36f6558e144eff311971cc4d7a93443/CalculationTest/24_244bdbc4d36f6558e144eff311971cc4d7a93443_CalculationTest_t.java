 package org.gwtapp.ccalc.rpc.proc.calculator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gwtapp.ccalc.rpc.data.book.Calculation;
 import org.gwtapp.ccalc.rpc.data.book.Currency;
 import org.gwtapp.ccalc.rpc.data.book.Operation;
import org.gwtapp.ccalc.rpc.data.book.OperationImpl;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class CalculationTest {
 	@Test
	public void testInitState() {
 		List<Operation> operations = new ArrayList<Operation>();
 		Calculator calculator = new Calculator(Currency.PLN, operations);
 		List<Calculation> calculations = calculator.getCalculations();
 		Assert.assertNotNull(calculations);
 		Assert.assertTrue(calculations.isEmpty());
 	}

	@Test
	public void testPositiveFifo() {
		List<Operation> operations = new ArrayList<Operation>();
		operations.add(getOperation(100.0, 1.0, Currency.USD));
		operations.add(getOperation(100.0, 2.0, Currency.USD));
		operations.add(getOperation(-50.0, null, Currency.USD));
		operations.add(getOperation(-125.0, null, Currency.USD));
		Calculator calculator = new Calculator(Currency.PLN, operations);
		List<Calculation> calculations = calculator.getCalculations();
		Assert.assertNotNull(calculations);
		Assert.assertEquals(4, calculations.size());
		{
		}
	}

	private Operation getOperation(Double value, Double ratio, Currency currency) {
		return new OperationImpl(null, null, value, ratio, currency);
	}
 }
