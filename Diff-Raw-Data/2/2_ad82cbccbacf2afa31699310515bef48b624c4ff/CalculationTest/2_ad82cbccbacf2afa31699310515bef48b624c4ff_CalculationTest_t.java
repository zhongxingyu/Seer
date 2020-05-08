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
 		operations.add(getOperation(-50.0, 1.0, Currency.USD));
 		operations.add(getOperation(-125.0, 1.0, Currency.USD));
 		Calculator calculator = new Calculator(Currency.PLN, operations);
 		List<Calculation> calculations = calculator.getCalculations();
 		Assert.assertNotNull(calculations);
 		Assert.assertEquals(4, calculations.size());
 		{
 			Calculation c = calculations.get(0);
 			Assert.assertEquals(new Double(100.0), c.getIncome());
 			Assert.assertEquals(null, c.getCost());
 			Assert.assertEquals(new Double(0.0), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(100.0), c.getFifoBase());
 		}
 		{
 			Calculation c = calculations.get(1);
 			Assert.assertEquals(new Double(200.0), c.getIncome());
 			Assert.assertEquals(null, c.getCost());
 			Assert.assertEquals(new Double(25.0), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(200.0), c.getFifoBase());
 		}
 		{
 			Calculation c = calculations.get(2);
 			Assert.assertEquals(null, c.getIncome());
 			Assert.assertEquals(new Double(50.0), c.getCost());
 			Assert.assertEquals(new Double(-1.0), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(-50), c.getFifoBase());
 		}
 		{
 			Calculation c = calculations.get(3);
 			Assert.assertEquals(null, c.getIncome());
 			Assert.assertEquals(new Double(125.0), c.getCost());
 			Assert.assertEquals(new Double(-1.6), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(-200), c.getFifoBase());
 		}
 	}
 
 	@Test
 	public void testNegativeFifo() {
 		List<Operation> operations = new ArrayList<Operation>();
 		operations.add(getOperation(100.0, 1.0, Currency.USD));
 		operations.add(getOperation(100.0, 7.0, Currency.USD));
 		operations.add(getOperation(-150.0, 4.0, Currency.USD));
 		operations.add(getOperation(-100.0, 4.0, Currency.USD));
 		operations.add(getOperation(200.0, 2.0, Currency.USD));
 		operations.add(getOperation(-300.0, 5.0, Currency.USD));
 		operations.add(getOperation(-100.0, 4.0, Currency.USD));
 		operations.add(getOperation(-50.0, 3.0, Currency.USD));
 		Calculator calculator = new Calculator(Currency.PLN, operations);
 		List<Calculation> calculations = calculator.getCalculations();
 		Assert.assertNotNull(calculations);
		Assert.assertEquals(8, calculations.size());
 		{
 			Calculation c = calculations.get(0);
 			Assert.assertEquals(new Double(100.0), c.getIncome());
 			Assert.assertEquals(null, c.getCost());
 			Assert.assertEquals(new Double(0.0), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(100.0), c.getFifoBase());
 		}
 		{
 			Calculation c = calculations.get(1);
 			Assert.assertEquals(new Double(700.0), c.getIncome());
 			Assert.assertEquals(null, c.getCost());
 			Assert.assertEquals(new Double(0.0), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(700.0), c.getFifoBase());
 		}
 		{
 			Calculation c = calculations.get(2);
 			Assert.assertEquals(null, c.getIncome());
 			Assert.assertEquals(new Double(600.0), c.getCost());
 			Assert.assertEquals(new Double(-3.0), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(-450), c.getFifoBase());
 		}
 		{
 			Calculation c = calculations.get(3);
 			Assert.assertEquals(null, c.getIncome());
 			Assert.assertEquals(new Double(400.0), c.getCost());
 			Assert.assertEquals(new Double(-5.5), c.getFifo(Currency.USD));
 			Assert.assertEquals(new Double(-550), c.getFifoBase());
 		}
 	}
 
 	private Operation getOperation(Double value, Double ratio, Currency currency) {
 		return new OperationImpl(null, null, value, ratio, currency);
 	}
 }
