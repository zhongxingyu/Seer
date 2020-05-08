 package alaTest.test;
 
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigDecimal;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import alaTest.test.domain.Operator;
 import alaTest.test.domain.PhoneNumber;
 import alaTest.test.domain.Prefix;
 import alaTest.test.searchers.RawSearch;
 import alaTest.test.service.OperatorManager;
 import alaTest.test.sortable.MergeSort;
 
 public class NoResultTest {
 	
 	private CopyOnWriteArrayList<Operator> operators = new CopyOnWriteArrayList<Operator>();
 	private OperatorManager operatorManager = new OperatorManager();
 	private long startTime;
 	
 	@Before
 	public void before()
 	{
 		System.out.println("----------- TC> "+this.getClass().getSimpleName()+"-----------");
		System.out.println("This TC is going to test when there is no prefix for the phone number. The idea is get a null ");
 		Operator operatorA = new Operator("A");
 		operatorA.addPrefix(new Prefix("1", new BigDecimal(0.9)));
 		operatorA.addPrefix(new Prefix("268", new BigDecimal(5.1)));
 		operatorA.addPrefix(new Prefix("46", new BigDecimal(0.17)));
 		operatorA.addPrefix(new Prefix("4620", new BigDecimal(0.0)));
 		operatorA.addPrefix(new Prefix("468", new BigDecimal(0.15)));
 		operatorA.addPrefix(new Prefix("4631", new BigDecimal(0.15)));
 		operatorA.addPrefix(new Prefix("4673", new BigDecimal(0.9)));
 		operatorA.addPrefix(new Prefix("46732", new BigDecimal(1.1)));
 		
 		Operator operatorB = new Operator("B");
 		operatorB.addPrefix(new Prefix("1", new BigDecimal(0.92)));
 		operatorB.addPrefix(new Prefix("44", new BigDecimal(0.5)));
 		operatorB.addPrefix(new Prefix("46", new BigDecimal(0.2)));
 		operatorB.addPrefix(new Prefix("467", new BigDecimal(1.0)));
 		operatorB.addPrefix(new Prefix("48", new BigDecimal(1.2)));
 		
 		operators.add(operatorA);
 		operators.add(operatorB);
 		
 		startTime = System.nanoTime();
 	}
 	
 	@Test
 	public void noResult() {
 		PhoneNumber phoneNumber = new PhoneNumber("2224673212345"); 
 		Prefix result = null;
 		
 		operatorManager.setOperators(operators);
 		operatorManager.setPhoneNumber(phoneNumber);
 		operatorManager.setSort(new MergeSort());
 		operatorManager.setSearch(new RawSearch());
 		
 		result = operatorManager.getCheapest();
 		
 		System.out.println("Result> "+result);
 		
 		assertNull(result);
 	
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
