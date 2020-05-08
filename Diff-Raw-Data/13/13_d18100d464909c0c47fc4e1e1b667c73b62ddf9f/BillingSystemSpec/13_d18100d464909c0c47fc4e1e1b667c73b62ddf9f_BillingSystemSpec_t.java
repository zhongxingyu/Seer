 package com.acmetelecom;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.concordion.integration.junit3.ConcordionTestCase;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 
 import com.acmetelecom.BillingSystem;
 import com.acmetelecom.BillGeneratorDummy;
 import com.acmetelecom.BillGeneratorInterface;
 import com.acmetelecom.customer.Customer;
 import com.acmetelecom.customer.CustomerDatabase;
 import com.acmetelecom.customer.Tariff;
 import com.acmetelecom.customer.TariffLibrary;
 
 public class BillingSystemSpec extends ConcordionTestCase {
 
 	//config from spec
 	BillingSystem billingSystem;
 	List<Customer> customerList;
 	Map<Customer, String> totalBill;
 	Tariff tariff;
 	
 	//Mock objects
 	Mockery context;
 	CustomerDatabase customerDatabase;
 	TariffLibrary tariffLibrary;
 	BillGeneratorInterface billGenerator;
 
 	
 	public BillingSystemSpec() {
 		//initialise the mock objects
 		context = new Mockery();
 		customerDatabase = context.mock(CustomerDatabase.class);
 		tariffLibrary = context.mock(TariffLibrary.class);
 		//billGenerator = context.mock(iBillGenerator.class);
 		billGenerator = new BillGeneratorDummy();
 	}
 	
 	public void initialiseStandardTariff() {
 		tariff = Tariff.Standard;	
 	}
 	
 	public String getPeakTariff(){
 		NumberFormat n = NumberFormat.getCurrencyInstance(Locale.UK); 
 	    double doubleTariff = tariff.peakRate().doubleValue();
 	    String s = n.format(doubleTariff);
 		return s;
 	}
 	
 	public String getOffPeakTariff(){
 		NumberFormat n = NumberFormat.getCurrencyInstance(Locale.UK); 
 	    double doubleTariff = tariff.offPeakRate().doubleValue();
 	    String s = n.format(doubleTariff);
 		return s;
 	}
 	
 	public void createCustomer(String name, String number, String plan){
 		if (customerList == null)
 			customerList = new ArrayList<Customer>();
 
 		customerList.add(new Customer(name, number, plan));
 	}
 
 	public void createCallEntry(String caller, String callee, String startTime, String duration){
 		//calculate timestamp
 		int hour = Integer.parseInt(startTime.substring(0, 2));
 		int minute = Integer.parseInt(startTime.substring(3, 5));
 		int durationMinutes = Integer.parseInt(duration);
 	  DateTime startTime1=new DateTime(2011,1,1,hour,minute,0);
 	  
 		long callStartTime = startTime1.getMillis();
 		long callEndTime = startTime1.plusMinutes(durationMinutes).getMillis();
 
 	  billingSystem.startCall().atTime(callStartTime).from(caller).to(callee);
 		billingSystem.endCall().atTime(callEndTime).from(caller).to(callee);
 	}
 	
 	public void initialiseBillingSystem(){
 		billingSystem = new BillingSystem(customerDatabase,tariffLibrary,billGenerator);		
 	}
 	
 	public void calculateCallTotals(){
 		context.checking(new Expectations(){{
 			allowing (customerDatabase).getCustomers(); will(returnValue(customerList));
 			allowing (tariffLibrary).tarriffFor(with(any(Customer.class))); will(returnValue(tariff));
 		}});
 		
 		billingSystem.createCustomerBills();
 		context.assertIsSatisfied();
 		totalBill = ((BillGeneratorDummy) billGenerator).getTotals();
 	}
 	
 	public String getTotalCostsFor(String custName, String telNo){
 		//check both telephone number and name as
 		//it appears the system can have two customers
 		//with the same number
 		Set<Entry<Customer, String>> billSet = totalBill.entrySet();
 		Iterator<Entry<Customer, String>> billItr = billSet.iterator();
 		
 		while (billItr.hasNext()){
 			Entry<Customer, String> entry = (Entry<Customer, String>) billItr.next();
 			
 			if(entry.getKey().getFullName().equals(custName) && entry.getKey().getPhoneNumber().equals(telNo)){
 				return entry.getValue();
 			}
 		}
 		return null;
 	}
 	
 
 	
 }
