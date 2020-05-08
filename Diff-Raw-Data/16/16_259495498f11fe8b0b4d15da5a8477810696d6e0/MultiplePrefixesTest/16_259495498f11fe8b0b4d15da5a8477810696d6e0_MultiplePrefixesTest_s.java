 package alaTest.test;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import alaTest.test.domain.PhoneNumber;
 import alaTest.test.domain.Prefix;
 import alaTest.test.domain.Operator;
 import alaTest.test.searchers.RawSearch;
 import alaTest.test.service.OperatorManager;
 import alaTest.test.sortable.MergeSort;
 
 public class MultiplePrefixesTest {
 
 	private CopyOnWriteArrayList<Operator> operators = new CopyOnWriteArrayList<Operator>();
 	private OperatorManager operatorManager = new OperatorManager();
 	private long startTime;
 	
 	@Before
 	public void before()
 	{
 		System.out.println("----------- TC> "+this.getClass().getSimpleName()+"-----------");
		System.out.println("This TC is going to test the example in the statement");
 		Operator operatorA = new Operator("A");
 		operatorA.addPrefix(new Prefix("8", new BigDecimal(9)));
 		operatorA.addPrefix(new Prefix("88", new BigDecimal(1)));
 		operatorA.addPrefix(new Prefix("888", new BigDecimal(0.17)));
 		operatorA.addPrefix(new Prefix("8888", new BigDecimal(0.0)));
 		operatorA.addPrefix(new Prefix("88888", new BigDecimal(5)));
 		operatorA.addPrefix(new Prefix("888888", new BigDecimal(0.2)));
 		operatorA.addPrefix(new Prefix("8888888", new BigDecimal(0.1)));
 			
 		Operator operatorB = new Operator("B");
 		operatorB.addPrefix(new Prefix("9", new BigDecimal(0.1)));
 		operatorB.addPrefix(new Prefix("99", new BigDecimal(5.1)));
 		operatorB.addPrefix(new Prefix("999", new BigDecimal(1.17)));
 		operatorB.addPrefix(new Prefix("9999", new BigDecimal(20.0)));
 		operatorB.addPrefix(new Prefix("99999", new BigDecimal(4.15)));
 		operatorB.addPrefix(new Prefix("999999", new BigDecimal(6.15)));
 		operatorB.addPrefix(new Prefix("9999999", new BigDecimal(7.9)));
 		
 		operators.add(operatorA);
 		operators.add(operatorB);
 		
 		startTime = System.nanoTime();
 	}
 	
 	@Test
 	public void multiplePrefixesTest() {         
 		PhoneNumber phoneNumber = new PhoneNumber("8888888345"); 
 		Prefix result = null;
 		
 		operatorManager.setOperators(operators);
 		operatorManager.setPhoneNumber(phoneNumber);
 		operatorManager.setSort(new MergeSort());
 		operatorManager.setSearch(new RawSearch());
 		
 		result = operatorManager.getCheapest();
 		
 		System.out.println("Result> "+result.toString());
 		
 		assertTrue(result.equals(new Prefix("8888888", new BigDecimal(0.1))));
 		
 	}
 
 	@Test
 	public void multiplePrefixes1Test() {         
 		PhoneNumber phoneNumber = new PhoneNumber("8988888345"); 
 		Prefix result = null;
 		
 		operatorManager.setOperators(operators);
 		operatorManager.setPhoneNumber(phoneNumber);
 		operatorManager.setSort(new MergeSort());
 		operatorManager.setSearch(new RawSearch());
 		
 		result = operatorManager.getCheapest();
 		
 		System.out.println("Result> "+result.toString());
 		
 		assertTrue(result.equals(new Prefix("8", new BigDecimal(9))));
 		
 	}
 	
 	@Test
 	public void multiplePrefixes2Test() {         
 		PhoneNumber phoneNumber = new PhoneNumber("99988888345"); 
 		Prefix result = null;
 		
 		operatorManager.setOperators(operators);
 		operatorManager.setPhoneNumber(phoneNumber);
 		operatorManager.setSort(new MergeSort());
 		operatorManager.setSearch(new RawSearch());
 		
 		result = operatorManager.getCheapest();
 		
 		System.out.println("Result> "+result.toString());
 		
 		assertTrue(result.equals(new Prefix("999", new BigDecimal(1.17))));
 		
 	}
 	
 	@After
 	public void after()
 	{
 	
 		long endTime = System.nanoTime();
 		long timeneeded =  ((endTime - startTime ) /1000000);
 		System.out.println("Test> "+this.getClass().getSimpleName()+" takes: "+timeneeded+" miliseconds");
 		System.out.println("--------------------------------------------------------------");
 		System.out.println("");
 		System.out.println("");
 	}
 
 }
