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
 
 public class ThousandOfPrefixesTest {
 
 	private CopyOnWriteArrayList<Operator> operators = new CopyOnWriteArrayList<Operator>();
 	private OperatorManager operatorManager = new OperatorManager();
 	private long startTime;
 	
 	@Before
 	public void before()
 	{
		System.out.println("This TC is going to test the example in the statement");
 		Operator operatorA = new Operator("A");
 		for(int i = 0; i < 1001; i++)
 		{
 			int value = 1 + (int)(Math.random()*300);
 			operatorA.addPrefix(new Prefix("215", new BigDecimal(value)));
 		}
 		operatorA.addPrefix(new Prefix("215", new BigDecimal(0.92)));
 		
 		Operator operatorB = new Operator("B");
 		for(int i = 0; i < 2001; i++)
 		{
 			int value = 1 + (int)(Math.random()*300);
 			operatorB.addPrefix(new Prefix("215", new BigDecimal(value)));
 		}
 
 		operators.add(operatorA);
 		operators.add(operatorB);
 		
 		startTime = System.nanoTime();
 	}
 	
 	@Test
 	public void test() {
 		PhoneNumber phoneNumber = new PhoneNumber("2153212345"); 
 		Prefix result = null;
 		
 		operatorManager.setOperators(operators);
 		operatorManager.setPhoneNumber(phoneNumber);
 		operatorManager.setSort(new MergeSort());
 		operatorManager.setSearch(new RawSearch());
 		
 		result = operatorManager.getCheapest();
 		
 		System.out.println("Result> "+result);
 		
 		assertTrue(result.equals(new Prefix("215", new BigDecimal(0.92))));
 		
 	}
 
 	@After
 	public void after()
 	{
 	
 		long endTime = System.nanoTime();
 		long timeneeded =  ((endTime - startTime ) /1000000);
 		System.out.println("Test> "+this.getClass().getCanonicalName()+" takes: "+timeneeded+" miliseconds");	
 		
 	}
 
 }
